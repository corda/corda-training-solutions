package net.corda.training.contract

import com.nhaarman.mockito_kotlin.mock
import net.corda.core.contracts.*
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.CordaX500Name
import net.corda.finance.*
import net.corda.node.internal.StartedNode
import net.corda.testing.*
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNodeParameters
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import net.corda.training.state.IOUState
import org.junit.*

/**
 * Practical exercise instructions for Contracts Part 2.
 * The objective here is to write some contract code that verifies a transaction to issue an [IOUState].
 * As with the [IOUIssueTests] uncomment each unit test and run them one at a time. Use the body of the tests and the
 * task description to determine how to get the tests to pass.
 */
class IOUTransferTests {
    // A pre-made dummy state we may need for some of the tests.
    class DummyState : ContractState {
        override val participants: List<AbstractParty> get() = listOf()
    }
    // A dummy command.
    class DummyCommand : CommandData
    lateinit var ledgerServices: MockServices
    lateinit var mockNetwork: MockNetwork
    lateinit var a: StartedNode<MockNetwork.MockNode>
    lateinit var b: StartedNode<MockNetwork.MockNode>
    lateinit var alice: TestIdentity
    lateinit var bob: TestIdentity
    lateinit var charlie: TestIdentity
    lateinit var miniCorp: TestIdentity
    lateinit var dummy: TestIdentity

    @Before
    fun setup() {
        ledgerServices = MockServices(listOf("net.corda.training"))
        mockNetwork = MockNetwork(listOf("net.corda.training"),
                notarySpecs = listOf(MockNetwork.NotarySpec(CordaX500Name("Notary","London","GB"))))
        a = mockNetwork.createNode(MockNodeParameters())
        b = mockNetwork.createNode(MockNodeParameters())
        mockNetwork.runNetwork()
        alice = TestIdentity(CordaX500Name(organisation = "Alice", locality = "TestLand", country = "US"))
        bob = TestIdentity(CordaX500Name(organisation = "Bob", locality = "TestCity", country = "US"))
        charlie = TestIdentity(CordaX500Name(organisation = "Charlie", locality = "TestVillage", country = "US"))
        miniCorp = TestIdentity(CordaX500Name(organisation = "MiniCorp", locality = "MiniLand", country = "US"))
        dummy = TestIdentity(CordaX500Name(organisation = "Dummy", locality = "FakeLand", country = "US"))
    }

    @After
    fun tearDown() {
        mockNetwork.stopNodes()
    }

    /**
     * Task 1.
     * Now things are going to get interesting!
     * We need the [IOUContract] to not only handle Issues of IOUs but now also Transfers.
     * Of course, we'll need to add a new Command and add some additional contract code to handle Transfers.
     * TODO: Add a "Transfer" command to the IOUState and update the verify() function to handle multiple commands.
     * Hint:
     * - As with the [Issue] command, add the [Transfer] command within the [IOUContract.Commands].
     * - Again, we only care about the existence of the [Transfer] command in a transaction, therefore it should
     *   subclass the [TypeOnlyCommandData].
     * - You can use the [requireSingleCommand] function to check for the existence of a command which implements a
     *   specified interface. Instead of using
     *
     *       tx.commands.requireSingleCommand<Commands.Issue>()
     *
     *   You can instead use:
     *
     *       tx.commands.requireSingleCommand<Commands>()
     *
     *   To match any command that implements [IOUContract.Commands]
     * - We then need to switch on the type of [Command.value], in Kotlin you can do this using a "when" block
     * - For each "when" block case, you can check the type of [Command.value] using the "is" keyword:
     *
     *       val command = ...
     *       when (command.value) {
     *           is Commands.X -> doSomething()
     *           is Commands.Y -> doSomethingElse()
     *       }
     * - The [requireSingleCommand] function will handle unrecognised types for you (see first unit test).
     */
    @Test
    fun mustHandleMultipleCommandValues() {
        val iou = IOUState(10.POUNDS, alice.party, bob.party)
        ledgerServices.ledger {
            transaction {
                output(IOUContract::class.java.name, iou)
                command(listOf(alice.publicKey, bob.publicKey), DummyCommand())
                this `fails with` "Required net.corda.training.contract.IOUContract.Commands command"
            }
            transaction {
                output(IOUContract::class.java.name, iou)
                command(listOf(alice.publicKey, bob.publicKey), IOUContract.Commands.Issue())
                this.verifies()
            }
            transaction {
                input(IOUContract::class.java.name, iou)
                output(IOUContract::class.java.name, iou.withNewLender(charlie.party))
                command(listOf(alice.publicKey, bob.publicKey, charlie.publicKey), IOUContract.Commands.Transfer())
                this.verifies()
            }
        }
    }

    /**
     * Task 2.
     * The transfer transaction should only have one input state and one output state.
     * TODO: Add constraints to the contract code to ensure a transfer transaction has only one input and output state.
     * Hint:
     * - Look at the contract code for "Issue".
     */
    @Test
    fun mustHaveOneInputAndOneOutput() {
        val iou = IOUState(10.POUNDS, alice.party, bob.party)
        ledgerServices.ledger {
            transaction {
                input(IOUContract::class.java.name, iou)
                input(IOUContract::class.java.name, DummyState())
                output(IOUContract::class.java.name, iou.withNewLender(charlie.party))
                command(listOf(alice.publicKey, bob.publicKey, charlie.publicKey), IOUContract.Commands.Transfer())
                this `fails with` "An IOU transfer transaction should only consume one input state."
            }
            transaction {
                output(IOUContract::class.java.name, iou)
                command(listOf(alice.publicKey, bob.publicKey, charlie.publicKey), IOUContract.Commands.Transfer())
                this `fails with` "An IOU transfer transaction should only consume one input state."
            }
            transaction {
                input(IOUContract::class.java.name, iou)
                command(listOf(alice.publicKey, bob.publicKey, charlie.publicKey), IOUContract.Commands.Transfer())
                this `fails with` "An IOU transfer transaction should only create one output state."
            }
            transaction {
                input(IOUContract::class.java.name, iou)
                output(IOUContract::class.java.name, iou.withNewLender(charlie.party))
                output(IOUContract::class.java.name, DummyState())
                command(listOf(alice.publicKey, bob.publicKey, charlie.publicKey), IOUContract.Commands.Transfer())
                this `fails with` "An IOU transfer transaction should only create one output state."
            }
            transaction {
                input(IOUContract::class.java.name, iou)
                output(IOUContract::class.java.name, iou.withNewLender(charlie.party))
                command(listOf(alice.publicKey, bob.publicKey, charlie.publicKey), IOUContract.Commands.Transfer())
                this.verifies()
            }
        }
    }

    /**
     * Task 3.
     * TODO: Add a constraint to the contract code to ensure only the lender property can change when transferring IOUs.
     * Hint:
     * - You can use the [IOUState.copy] method.
     * - You can compare a copy of the input to the output with the lender of the output as the lender of the input.
     * - You'll need references to the input and output ious.
     * - Remember you need to cast the [ContractState]s to [IOUState]s.
     * - It's easier to take this approach then check all properties other than the lender haven't changed, including
     *   the [linearId] and the [contract]!
     */
    @Test
    fun onlyTheLenderMayChange() {
        val iou = IOUState(10.POUNDS, alice.party, bob.party)
        ledgerServices.ledger {
            transaction {
                input(IOUContract::class.java.name, IOUState(10.DOLLARS, alice.party, bob.party))
                output(IOUContract::class.java.name, IOUState(1.DOLLARS, alice.party, bob.party))
                command(listOf(alice.publicKey, bob.publicKey, charlie.publicKey), IOUContract.Commands.Transfer())
                this `fails with` "Only the lender property may change."
            }
            transaction {
                input(IOUContract::class.java.name, IOUState(10.DOLLARS, alice.party, bob.party))
                output(IOUContract::class.java.name, IOUState(10.DOLLARS, alice.party, charlie.party))
                command(listOf(alice.publicKey, bob.publicKey, charlie.publicKey), IOUContract.Commands.Transfer())
                this `fails with` "Only the lender property may change."
            }
            transaction {
                input(IOUContract::class.java.name, IOUState(10.DOLLARS, alice.party, bob.party, 5.DOLLARS))
                output(IOUContract::class.java.name, IOUState(10.DOLLARS, alice.party, bob.party, 10.DOLLARS))
                command(listOf(alice.publicKey, bob.publicKey, charlie.publicKey), IOUContract.Commands.Transfer())
                this `fails with` "Only the lender property may change."
            }
            transaction {
                input(IOUContract::class.java.name, iou)
                output(IOUContract::class.java.name, iou.withNewLender(charlie.party))
                command(listOf(alice.publicKey, bob.publicKey, charlie.publicKey), IOUContract.Commands.Transfer())
                this.verifies()
            }
        }
    }

    /**
     * Task 4.
     * It is fairly obvious that in a transfer IOU transaction the lender must change.
     * TODO: Add a constraint to check the lender has changed in the output IOU.
     */
    @Test
    fun theLenderMustChange() {
        val iou = IOUState(10.POUNDS, alice.party, bob.party)
        ledgerServices.ledger {
            transaction {
                input(IOUContract::class.java.name, iou)
                output(IOUContract::class.java.name, iou)
                command(listOf(alice.publicKey, bob.publicKey, charlie.publicKey), IOUContract.Commands.Transfer())
                this `fails with` "The lender property must change in a transfer."
            }
            transaction {
                input(IOUContract::class.java.name, iou)
                output(IOUContract::class.java.name, iou.withNewLender(charlie.party))
                command(listOf(alice.publicKey, bob.publicKey, charlie.publicKey), IOUContract.Commands.Transfer())
                this.verifies()
            }
        }
    }

    /**
     * Task 5.
     * All the participants in a transfer IOU transaction must sign.
     * TODO: Add a constraint to check the old lender, the new lender and the recipient have signed.
     */
    @Test
    fun allParticipantsMustSign() {
        val iou = IOUState(10.POUNDS, alice.party, bob.party)
        ledgerServices.ledger {
            transaction {
                input(IOUContract::class.java.name, iou)
                output(IOUContract::class.java.name, iou.withNewLender(charlie.party))
                command(listOf(alice.publicKey, bob.publicKey), IOUContract.Commands.Transfer())
                this `fails with` "The borrower, old lender and new lender only must sign an IOU transfer transaction"
            }
            transaction {
                input(IOUContract::class.java.name, iou)
                output(IOUContract::class.java.name, iou.withNewLender(charlie.party))
                command(listOf(alice.publicKey, charlie.publicKey), IOUContract.Commands.Transfer())
                this `fails with` "The borrower, old lender and new lender only must sign an IOU transfer transaction"
            }
            transaction {
                input(IOUContract::class.java.name, iou)
                output(IOUContract::class.java.name, iou.withNewLender(charlie.party))
                command(listOf(bob.publicKey, charlie.publicKey), IOUContract.Commands.Transfer())
                this `fails with` "The borrower, old lender and new lender only must sign an IOU transfer transaction"
            }
            transaction {
                input(IOUContract::class.java.name, iou)
                output(IOUContract::class.java.name, iou.withNewLender(charlie.party))
                command(listOf(alice.publicKey, bob.publicKey, miniCorp.publicKey), IOUContract.Commands.Transfer())
                this `fails with` "The borrower, old lender and new lender only must sign an IOU transfer transaction"
            }
            transaction {
                input(IOUContract::class.java.name, iou)
                output(IOUContract::class.java.name, iou.withNewLender(charlie.party))
                command(listOf(alice.publicKey, bob.publicKey, charlie.publicKey, miniCorp.publicKey), IOUContract.Commands.Transfer())
                this `fails with` "The borrower, old lender and new lender only must sign an IOU transfer transaction"
            }
            transaction {
                input(IOUContract::class.java.name, iou)
                output(IOUContract::class.java.name, iou.withNewLender(charlie.party))
                command(listOf(alice.publicKey, bob.publicKey, charlie.publicKey), IOUContract.Commands.Transfer())
                this.verifies()
            }
        }
    }
}