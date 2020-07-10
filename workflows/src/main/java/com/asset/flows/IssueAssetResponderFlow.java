package com.asset.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;
import net.corda.core.transactions.SignedTransaction;

// ******************
// * Responder flow *
// ******************
@InitiatedBy(IssueAssetFlow.class)
public class IssueAssetResponderFlow extends FlowLogic<SignedTransaction> {
    private FlowSession otherPartySession;

    public IssueAssetResponderFlow(FlowSession otherPartySession) {
        this.otherPartySession = otherPartySession;
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        // Responder flow logic goes here.

        System.out.println("Received precious Metal.");

        return subFlow(new ReceiveFinalityFlow(otherPartySession));


    }
}
