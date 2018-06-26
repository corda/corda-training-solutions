package net.corda.training.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.utilities.unwrap
import net.corda.training.state.IOUState

class NotificationFlowResponder(val session: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val state = session.receive<IOUState>().unwrap { it }
        //send stx via API to integration layer
        println("received iou state for integration $state")
    }
}