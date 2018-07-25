package net.corda.training.flow;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndContract;
import net.corda.core.contracts.ContractState;
import java.util.List;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.training.contract.IOUContract;
import net.corda.training.state.IOUState;
import java.util.stream.Collectors;

import static net.corda.core.contracts.ContractsDSL.requireThat;
//import static net.corda.training.contract.IOUContract.Commands.*;

/**
 * This is the flow which handles issuance of new IOUs on the ledger.
 * Gathering the counterparty's signature is handled by the [CollectSignaturesFlow].
 * Notarisation (if required) and commitment to the ledger is handled by the [FinalityFlow].
 * The flow returns the [SignedTransaction] that was committed to the ledger.
 */
public class IOUIssueFlow{
	
	@InitiatingFlow
	@StartableByRPC
	public static class IssueFlow extends FlowLogic<SignedTransaction> {
		private final IOUState state;

		public IssueFlow(IOUState state){
			this.state = state;
		}

		@Suspendable
		@Override
		public SignedTransaction call() throws FlowException {
			// Step 1. Get a reference to the notary service on our network and our key pair.
       		// Note: ongoing work to support multiple notary identities is still in progress.
			final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
			
			// Step 2. Create a new issue command.
        	// Remember that a command is a CommandData object and a list of CompositeKeys
			final Command<IOUContract.Commands.Issue> issueCommand = new Command<IOUContract.Commands.Issue>(
				new IOUContract.Commands.Issue(), state.getParticipants().stream().map(el -> el.getOwningKey()).collect(Collectors.toList()));

        	// Step 3. Create a new TransactionBuilder object.
        	final TransactionBuilder builder = new TransactionBuilder(notary);

	        // Step 4. Add the iou as an output state, as well as a command to the transaction builder.
        	builder.addOutputState(state, IOUContract.IOU_CONTRACT_ID);
        	builder.addCommand(issueCommand);


			// Step 5. Verify and sign it with our KeyPair.
			builder.verify(getServiceHub());
			final SignedTransaction ptx = getServiceHub().signInitialTransaction(builder);

        	
        	// Step 6. Collect the other party's signature using the SignTransactionFlow.
        	Party ourIdentity = getServiceHub().getMyInfo().getLegalIdentities().get(0);
        	
        	List<Party> otherParties = state.getParticipants().stream().map(el -> (Party)el).collect(Collectors.toList());
       		otherParties.remove(ourIdentity);

	        List<FlowSession> sessions = otherParties.stream().map(el -> initiateFlow(el)).collect(Collectors.toList());
	        // 	        ArrayList<FlowSession> sessions = state.getParticipants().stream().filter(el-> el != ourIdentity).filter(el -> initateFlow(el)).collect(Collectors.toList());

        	SignedTransaction stx = subFlow(new CollectSignaturesFlow(ptx, sessions));

			// Step 7. Assuming no exceptions, we can now finalise the transaction.
			return subFlow(new FinalityFlow(stx));
		}
	}

	/**
	 * This is the flow which signs IOU issuances.
	 * The signing is handled by the [SignTransactionFlow].
	 */
	@InitiatedBy(IssueFlow.class)
	public static class RespondFlow extends FlowLogic<SignedTransaction>{
		private final FlowSession flowSession;

		public RespondFlow(FlowSession flowSession){
			this.flowSession = flowSession;
		}

		@Suspendable
		@Override
        public SignedTransaction call() throws FlowException {
        	class SignTxFlow extends SignTransactionFlow{
        		private SignTxFlow(FlowSession flowSession){
        			super(flowSession, null);
        		}

        		@Override
        		protected void checkTransaction(SignedTransaction stx){
        			requireThat(req -> {
        				ContractState output = stx.getTx().getOutputs().get(0).getData();
        				req.using("This must be an IOU transaction", output instanceof IOUState);
        				return null;
        			});
        		}
        	}
        	return subFlow(new SignTxFlow(flowSession));
        }
	}
}


// @InitiatedBy(IOUIssueFlow::class)
// class IOUIssueFlowResponder(val flowSession: FlowSession): FlowLogic<Unit>() {
//     @Suspendable
//     override fun call() {
//         val signedTransactionFlow = object : SignTransactionFlow(flowSession) {
//             override fun checkTransaction(stx: SignedTransaction) = requireThat {
//                 val output = stx.tx.outputs.single().data
//                 "This must be an IOU transaction" using (output is IOUState)
//             }
//         }
//         subFlow(signedTransactionFlow)
//     }
// }