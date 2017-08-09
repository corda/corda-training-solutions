package net.corda.training.flow

import co.paralleluniverse.fibers.Suspendable
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
import net.corda.core.transactions.SignedTransaction
import net.corda.flows.CollectSignaturesFlow
import net.corda.flows.FinalityFlow
import net.corda.flows.SignTransactionFlow
import net.corda.training.contract.IOUContract
import net.corda.training.state.IOUState

/**
 * This is the flow which handles transfers of existing IOUs on the ledger.
 * Gathering the counterparty's signature is handled by the [CollectSignaturesFlow].
 * Notarisation (if required) and commitment to the ledger is handled vy the [FinalityFlow].
 * The flow returns the [SignedTransaction] that was committed to the ledger.
 */
@InitiatingFlow
@StartableByRPC
class IOUTransferFlow(val linearId: UniqueIdentifier, val newLender: Party): FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        // Stage 1. Retrieve IOU specified by linearId from the vault.
        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId))
        val iouStateAndRef = serviceHub.vaultQueryService.queryBy<IOUState>(queryCriteria).states.single()
        val inputIou = iouStateAndRef.state.data

        // Stage 2. This flow can only be initiated by the current recipient.
        if (serviceHub.myInfo.legalIdentity != inputIou.lender) {
            throw IllegalArgumentException("IOU transfer can only be initiated by the IOU lender.")
        }

        // Stage 3. Create the new IOU state reflecting a new lender.
        val outputIou = inputIou.withNewLender(newLender)

        // Stage 4. Create the transfer command.
        val signers = (inputIou.participants + newLender).map { it.owningKey }
        val transferCommand = Command(IOUContract.Commands.Transfer(), signers)

        // Stage 5. Get a reference to a transaction builder.
        val notary = serviceHub.networkMapCache.notaryNodes.single().notaryIdentity
        val builder = TransactionType.General.Builder(notary)

        // Stage 6. Create the transaction which comprises: one input, one output and one command.
        builder.withItems(iouStateAndRef, outputIou, transferCommand)

        // Stage 7. Verify and sign the transaction.
        builder.toWireTransaction().toLedgerTransaction(serviceHub).verify()
        val ptx = serviceHub.signInitialTransaction(builder)

        // Stage 8. Collect signature from borrower and the new lender and add it to the transaction.
        // This also verifies the transaction and checks the signatures.
        val stx = subFlow(CollectSignaturesFlow(ptx))

        // Stage 9. Notarise and record, the transaction in our vaults.
        return subFlow(FinalityFlow(stx, setOf(inputIou.lender, inputIou.borrower, newLender))).single()
    }
}

/**
 * This is the flow which signs IOU transfers.
 * The signing is handled by the [CollectSignaturesFlow].
 */
@InitiatedBy(IOUTransferFlow::class)
class IOUTransferFlowResponder(val otherParty: Party): FlowLogic<Unit>() {

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