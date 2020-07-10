package com.asset.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.asset.contracts.AssetContract;
import com.asset.states.AssetState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.util.List;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class TransferAssetFlow extends FlowLogic<SignedTransaction> {

    private String assetName;
    private int weight;
    private Party newOwner;
    private int input = 0;

    public TransferAssetFlow(String assetName, int weight, Party newOwner) {
        this.assetName = assetName;
        this.weight = weight;
        this.newOwner = newOwner;
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

    //    ----------------------------------------------- Check for Asset States Starts-----------------------------------------------

    StateAndRef<AssetState> checkForAssetStates() throws FlowException {


        QueryCriteria generalCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);

        List<StateAndRef<AssetState>> AssetStates = getServiceHub().getVaultService().queryBy(AssetState.class, generalCriteria).getStates();

        boolean inputFound = false;
        int t = AssetStates.size();

        for (int x = 0; x < t; x++) {
            if (AssetStates.get(x).getState().getData().getAssetName().equals(assetName)
            && AssetStates.get(x).getState().getData().getWeight() == weight) {
                input = x;
                inputFound = true;
            }
        }


        if (inputFound) {
            System.out.println("\n Input Found");
        } else {
            System.out.println("\n Input not found");
            throw new FlowException();
        }

        return AssetStates.get(input);
    }



    //    ----------------------------------------------- Check for Asset States Ends-----------------------------------------------

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        // Initiator flow logic goes here.

        // Retrieve Notary Identity
        progressTracker.setCurrentStep(RETRIEVING_NOTARY);
        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

        StateAndRef<AssetState> inputState = null;

        inputState = checkForAssetStates();

        Party issuer = inputState.getState().getData().getIssuer();

        //Create transaction components
        AssetState outputState = new AssetState(assetName, weight, issuer, newOwner);
        Command cmd = new Command(new AssetContract.Transfer(), getOurIdentity().getOwningKey());


        // Create transaction builder
        progressTracker.setCurrentStep(GENERATING_TRANSACTION);
        TransactionBuilder txB = new TransactionBuilder(notary)
                .addOutputState(outputState, AssetContract.CID)
                .addCommand(cmd);

        txB.addInputState(inputState);


        // Sign the transaction
        progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        SignedTransaction signedTx = getServiceHub().signInitialTransaction(txB);


        // Create session with CounterParty
        progressTracker.setCurrentStep(COUNTERPARTY_SESSION);
        FlowSession otherPartySession = initiateFlow(newOwner);
        FlowSession mintPartySession = initiateFlow(issuer);


        // Finalize and send to CounterParty
        progressTracker.setCurrentStep(FINALISING_TRANSACTION);
        return subFlow(new FinalityFlow(signedTx, otherPartySession, mintPartySession));


    }
}
