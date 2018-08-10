package net.corda.training.contract;

import net.corda.core.contracts.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.transactions.LedgerTransaction;
import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

import net.corda.training.state.IOUState;

import java.security.PublicKey;
import java.util.stream.Collectors;
import java.util.HashSet;



/**
 * The IOUContract can handle three transaction types involving [IOUState]s.
 * - Issuance: Issuing a new [IOUState] on the ledger, which is a bilateral agreement between two parties.
 * - Transfer: Re-assigning the lender/beneficiary.
 * - Settle: Fully or partially settling the [IOUState] using the Corda [Cash] contract.
 *
 * LegalProseReference: this is just a dummy string for the time being.
 */
@LegalProseReference(uri = "<prose_contract_uri>")
public class IOUContract implements Contract {
    
    public static final String IOU_CONTRACT_ID = "net.corda.training.contract.IOUContract";


    public interface Commands extends CommandData {
        class Issue extends TypeOnlyCommandData implements Commands{}
    }


    @Override
    public void verify(LedgerTransaction tx) {
        final CommandWithParties<Commands> command = requireSingleCommand(tx.getCommands(), Commands.class);
        final Commands commandData = command.getValue();

        requireThat(req -> {
            req.using("No inputs should be consumed when issuing an IOU.", tx.getInputs().isEmpty());
            req.using("Only one output state should be created when issuing an IOU.", (tx.getOutputs().size() == 1));
            final IOUState iou = (IOUState)tx.getOutputStates().get(0);
            req.using("A newly issued IOU must have a positive amount.", iou.getAmount().getQuantity() > 0);
            req.using("The lender and borrower cannot have the same identity.", iou.getBorrower() != iou.getLender());
            HashSet<PublicKey> signers = new HashSet<>(command.getSigners());
            HashSet<PublicKey> participants = new HashSet<>(iou.getParticipants()
                    .stream().map(el -> el.getOwningKey())
                    .collect(Collectors.toList()));
            req.using("Both lender and borrower together only may sign IOU issue transaction.", signers.equals(participants));
            return null;
        });
    }
}


