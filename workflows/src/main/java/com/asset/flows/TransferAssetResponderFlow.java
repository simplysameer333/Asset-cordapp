package com.asset.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;
import net.corda.core.transactions.SignedTransaction;

// ******************
// * Responder flow *
// ******************
@InitiatedBy(TransferAssetFlow.class)
public class TransferAssetResponderFlow extends FlowLogic<SignedTransaction> {
    private FlowSession otherPartySession;

    public TransferAssetResponderFlow(FlowSession otherPartySession) {
        this.otherPartySession = otherPartySession;
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        // Responder flow logic goes here.

        System.out.println("Received transferred Metal.");

        return subFlow(new ReceiveFinalityFlow(otherPartySession));


    }
}
