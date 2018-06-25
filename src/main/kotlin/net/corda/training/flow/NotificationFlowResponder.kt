package net.corda.training.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatedBy
import net.corda.core.utilities.unwrap

@InitiatedBy(NotificationFlow::class)
class NotificationFlowResponder(val session: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val state = session.receive<Boolean>().unwrap { it }
            //send stx via API to integration layer
        println("received iou state for integration $state")
    }
}