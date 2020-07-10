package com.asset.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.asset.contracts.AssetContract;
import com.asset.states.AssetState;
import net.corda.core.contracts.Command;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class IssueAssetFlow extends FlowLogic<SignedTransaction> {

    private String assetName;
    private int weight; 
    private Party owner;

    public IssueAssetFlow(String assetName, int weight, Party owner) {
        this.assetName = assetName;
        this.weight = weight;
        this.owner = owner;
    }

    private final ProgressTracker.Step RETRIEVING_NOTARY = new ProgressTracker.Step("Retrieving the Notary.");
    private final ProgressTracker.Step GENERATING_TRANSACTION = new ProgressTracker.Step("Generating transaction.");
    private final ProgressTracker.Step SIGNING_TRANSACTION = new ProgressTracker.Step("Signing transaction with our private key.");
    private final ProgressTracker.Step COUNTERPARTY_SESSION = new ProgressTracker.Step("Sending flow to counterparty.");
    private final ProgressTracker.Step FINALISING_TRANSACTION = new ProgressTracker.Step("Obtaining notary signature and recording transaction");

    private final ProgressTracker progressTracker = new ProgressTracker(
            RETRIEVING_NOTARY,
            GENERATING_TRANSACTION,
            SIGNING_TRANSACTION,
            COUNTERPARTY_SESSION,
            FINALISING_TRANSACTION
    );

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        // Initiator flow logic goes here.

        // Retrieve Notary Identity
        progressTracker.setCurrentStep(RETRIEVING_NOTARY);
        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);


        //Create transaction components
        AssetState outputState = new AssetState(assetName, weight, getOurIdentity(), owner);
        Command cmd = new Command(new AssetContract.Issue(), getOurIdentity().getOwningKey());


        // Create transaction builder
        progressTracker.setCurrentStep(GENERATING_TRANSACTION);
        TransactionBuilder txB = new TransactionBuilder(notary)
                .addOutputState(outputState, AssetContract.CID)
                .addCommand(cmd);


        // Sign the transaction
        progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        SignedTransaction signedTx = getServiceHub().signInitialTransaction(txB);


        // Create session with CounterParty
        progressTracker.setCurrentStep(COUNTERPARTY_SESSION);
        FlowSession otherPartySession = initiateFlow(owner);


        // Finalize and send to CounterParty
        progressTracker.setCurrentStep(FINALISING_TRANSACTION);
        return subFlow(new FinalityFlow(signedTx, otherPartySession));


    }
}
