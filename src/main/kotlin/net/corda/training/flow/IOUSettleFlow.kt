package net.corda.training.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.contracts.asset.Cash
import net.corda.core.contracts.Amount
import net.corda.core.contracts.Command
import net.corda.core.contracts.TransactionType
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatedBy
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.serialization.OpaqueBytes
import net.corda.core.transactions.SignedTransaction
import net.corda.flows.CashIssueFlow
import net.corda.flows.CollectSignaturesFlow
import net.corda.flows.FinalityFlow
import net.corda.flows.SignTransactionFlow
import net.corda.training.contract.IOUContract
import net.corda.training.state.IOUState
import java.util.*

/**
 * This is the flow which handles the (partial) settlement of existing IOUs on the ledger.
 * Gathering the counterparty's signature is handled by the [CollectSignaturesFlow].
 * Notarisation (if required) and commitment to the ledger is handled vy the [FinalityFlow].
 * The flow returns the [SignedTransaction] that was committed to the ledger.
 */
@InitiatingFlow
@StartableByRPC
class IOUSettleFlow(val linearId: UniqueIdentifier, val amount: Amount<Currency>): FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val me = serviceHub.myInfo.legalIdentity

        // Step 1. Retrieve the IOU state from the vault.
        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId))
        val iouToSettle = serviceHub.vaultQueryService.queryBy<IOUState>(queryCriteria).states.single()
        val counterparty = iouToSettle.state.data.lender

        // Step 2. Check the party running this flow is the borrower.
        if (me != iouToSettle.state.data.borrower) {
            throw IllegalArgumentException("IOU settlement flow must be initiated by the borrower.")
        }

        // Step 3. Create a transaction builder.
        val notary = iouToSettle.state.notary
        val builder = TransactionType.General.Builder(notary)

        // Step 4. Check we have enough cash to settle the requested amount.
        val cashBalance = serviceHub.vaultService.cashBalances[amount.token]
        if (cashBalance == null) {
            throw IllegalArgumentException("Borrower has no ${amount.token} to settle.")
        } else if (cashBalance < amount) {
            throw IllegalArgumentException("Borrower has only $cashBalance but needs $amount to settle.")
        } else if (amount > (iouToSettle.state.data.amount - iouToSettle.state.data.paid)) {
            throw IllegalArgumentException("Borrower tried to settle with $amount but only needs ${ (iouToSettle.state.data.amount - iouToSettle.state.data.paid) }")
        }

        // Step 5. Get some cash from the vault and add a spend to our transaction builder.
        serviceHub.vaultService.generateSpend(builder, amount, counterparty)

        // Step 6. Add the IOU input state and settle command to the transaction builder.
        val settleCommand = Command(IOUContract.Commands.Settle(), listOf(counterparty.owningKey, me.owningKey))
        // Add the input IOU and IOU settle command.
        builder.addCommand(settleCommand)
        builder.addInputState(iouToSettle)

        // Step 7. Only add an output IOU state of the IOU has not been fully settled.
        val amountRemaining = iouToSettle.state.data.amount - iouToSettle.state.data.paid - amount
        if (amountRemaining > Amount(0, amount.token)) {
            val settledIOU: IOUState = iouToSettle.state.data.pay(amount)
            builder.addOutputState(settledIOU)
        }

        // Step 8. Verify and sign the transaction.
        builder.toWireTransaction().toLedgerTransaction(serviceHub).verify()
        val ptx = serviceHub.signInitialTransaction(builder)

        // Step 9. Get counterparty signature.
        val stx = subFlow(CollectSignaturesFlow(ptx))

        // Step 10. Finalize the transaction.
        return subFlow(FinalityFlow(stx, setOf(counterparty, me))).single()
    }
}

/**
 * This is the flow which signs IOU settlements.
 * The signing is handled by the [CollectSignaturesFlow].
 */
@InitiatedBy(IOUSettleFlow::class)
class IOUSettleFlowResponder(val otherParty: Party): FlowLogic<Unit>() {

    @Suspendable
    override fun call() {
        val signTransactionFlow = object : SignTransactionFlow(otherParty) {
            override fun checkTransaction(stx: SignedTransaction) {
                // Define checking logic.
            }
        }

        subFlow(signTransactionFlow)
    }
}

@InitiatingFlow
@StartableByRPC
/**
 * Self issues the calling node an amount of cash in the desired currency.
 * Only used for demo/sample/training purposes!
 */
class SelfIssueCashFlow(val amount: Amount<Currency>) : FlowLogic<Cash.State>() {
    @Suspendable
    override fun call(): Cash.State {
        /** Create the cash issue command. */
        val issueRef = OpaqueBytes.of(0)
        val notary = serviceHub.networkMapCache.notaryNodes.single().notaryIdentity
        val me = serviceHub.myInfo.legalIdentity
        /** Create the cash issuance transaction. */
        val cashIssueTransaction = subFlow(CashIssueFlow(amount, issueRef, me, notary))
        /** Return the cash output. */
        return cashIssueTransaction.tx.outputs.single().data as Cash.State
    }
}