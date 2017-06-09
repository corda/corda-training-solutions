package net.corda.training.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.TransactionType
import net.corda.core.identity.Party
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatedBy
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.SignedTransaction
import net.corda.flows.CollectSignaturesFlow
import net.corda.flows.FinalityFlow
import net.corda.flows.SignTransactionFlow
import net.corda.training.contract.IOUContract
import net.corda.training.state.IOUState

/**
 * This is the flow which handles issuance of new IOUs on the ledger.
 * Gathering the counterparty's signature is handled by the [CollectSignaturesFlow].
 * Notarisation (if required) and commitment to the ledger is handled by the [FinalityFlow].
 * The flow returns the [SignedTransaction] that was committed to the ledger.
 */
@InitiatingFlow
@StartableByRPC
class IOUIssueFlow(val state: IOUState, val otherParty: Party): FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        // Step 1. Get a reference to the notary service on our network and our key pair.
        val notary = serviceHub.networkMapCache.notaryNodes.single().notaryIdentity

        // Step 2. Create a new issue command.
        // Remember that a command is a CommandData object and a list of CompositeKeys
        val issueCommand = Command(IOUContract.Commands.Issue(), state.participants.map { it.owningKey })

        // Step 3. Create a new TransactionBuilder object.
        val builder = TransactionType.General.Builder(notary)

        // Step 4. Add the iou as an output state, as well as a command to the transaction builder.
        builder.withItems(state, issueCommand)

        // Step 5. Verify and sign it with our KeyPair.
        builder.toWireTransaction().toLedgerTransaction(serviceHub).verify()
        val ptx = serviceHub.signInitialTransaction(builder)

        // Step 6. Collect the other party's signature using the SignTransactionFlow.
        val stx = subFlow(CollectSignaturesFlow(ptx))

        // Step 7. Assuming no exceptions, we can now finalise the transaction.
        return subFlow(FinalityFlow(stx, setOf(serviceHub.myInfo.legalIdentity, otherParty))).single()
    }
}

/**
 * This is the flow which signs IOU issuances.
 * The signing is handled by the [CollectSignaturesFlow].
 */
@InitiatedBy(IOUIssueFlow::class)
class IOUIssueFlowResponder(val otherParty: Party): FlowLogic<Unit>() {
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