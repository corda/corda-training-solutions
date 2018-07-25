package net.corda.training.contract;

import net.corda.training.state.IOUState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.LegalProseReference;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.identity.AbstractParty;
import net.corda.core.transactions.LedgerTransaction;

import java.util.stream.Collectors;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;


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
        class Issue implements Commands{}
        class Transfer implements Commands{}
        class Settle implements Commands{}
    }


    @Override
    public void verify(LedgerTransaction tx) {
        final CommandWithParties<Commands> command = requireSingleCommand(tx.getCommands(), Commands.class);
        final Commands commandData = command.getValue();

        if(commandData instanceof Commands.Issue){
            requireThat(req -> {
                req.using("No inputs should be consumed when issuing an IOU.", tx.getInputs().isEmpty());
                req.using("Only one output state should be created when issuing an IOU.", (tx.getOutputs().size() == 1));
                IOUState iou = (IOUState)tx.getOutputStates().get(0);
                req.using("A newly issued IOU must have a positive amount.", iou.getAmount().getQuantity() > 0);
                req.using("The lender and borrower cannot have the same identity.", iou.getBorrower() != iou.getLender());
                req.using("Both lender and borrower together only may sign IOU issue transaction.", (command.getSigners() == iou.getParticipants().stream().map(el -> el.getOwningKey()).collect(Collectors.toList())));
                return null;
            });

        }else if(commandData instanceof Commands.Transfer){

        }else if(commandData instanceof Commands.Transfer){

        }

    }
}


