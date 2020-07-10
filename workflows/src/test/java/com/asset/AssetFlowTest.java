package com.asset;

import com.asset.contracts.AssetContract;
import com.asset.flows.IssueAssetFlow;
import com.asset.flows.TransferAssetFlow;
import com.google.common.collect.ImmutableList;
import com.asset.states.AssetState;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.TransactionState;
import net.corda.core.transactions.SignedTransaction;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.MockNetworkParameters;
import net.corda.testing.node.StartedMockNode;
import net.corda.testing.node.TestCordapp;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class AssetFlowTest {
    private final MockNetwork network = new MockNetwork(new MockNetworkParameters(ImmutableList.of(
            TestCordapp.findCordapp("com.asset.contracts"),
            TestCordapp.findCordapp("com.asset.flows")
    )));

    private final StartedMockNode Mint = network.createNode();
    private final StartedMockNode A = network.createNode();
    private final StartedMockNode B = network.createNode();

    @Before
    public void setup() {
        network.runNetwork();
    }

    @After
    public void tearDown() {
        network.stopNodes();
    }

    // ------------------------------------------ Issue Asset Flow Tests ----------------------------------------

    @Test
    public void transactionHasNoInputsHasOneAssetStateOutputWithTheCorrectOwnerTest() throws Exception {

        IssueAssetFlow flow = new IssueAssetFlow("Gold", 10, A.getInfo().getLegalIdentities().get(0));
        CordaFuture<SignedTransaction> future = Mint.startFlow(flow);
        setup();
        SignedTransaction signedTransaction = future.get();

        assertEquals (0, signedTransaction.getTx().getInputs().size());

        assertEquals (1, signedTransaction.getTx().getOutputStates().size());
        AssetState output = signedTransaction.getTx().outputsOfType(AssetState.class).get(0);

        assertEquals(A.getInfo().getLegalIdentities().get(0), output.getOwner());

    }

    @Test
    public void transactionHasTheCorrectContractWithOneIssueCommandAndIssuerAsSignerTest() throws Exception {

        IssueAssetFlow flow = new IssueAssetFlow("Gold", 10, A.getInfo().getLegalIdentities().get(0));
        CordaFuture<SignedTransaction> future = Mint.startFlow(flow);
        setup();
        SignedTransaction signedTransaction = future.get();

        TransactionState output = signedTransaction.getTx().getOutputs().get(0);
        assertEquals("com.asset.contracts.AssetContract", output.getContract());

        assertEquals(1, signedTransaction.getTx().getCommands().size());

        Command command = signedTransaction.getTx().getCommands().get(0);
        assert (command.getValue() instanceof AssetContract.Issue);

        assertEquals(1, command.getSigners().size());
        assertTrue(command.getSigners().contains(Mint.getInfo().getLegalIdentities().get(0).getOwningKey()));

    }


    // ------------------------------------------ Transfer Asset Flow Tests ----------------------------------------



    @Test
    public void transactionHasOneInputAndOneOutputTest() throws Exception {
        IssueAssetFlow flow = new IssueAssetFlow("Gold", 10, A.getInfo().getLegalIdentities().get(0));
        TransferAssetFlow transferFlow = new TransferAssetFlow("Gold", 10, B.getInfo().getLegalIdentities().get(0));

        CordaFuture<SignedTransaction> future = Mint.startFlow(flow);
        setup();

        CordaFuture<SignedTransaction> transferFuture = A.startFlow(transferFlow);
        setup();

        SignedTransaction signedTransaction = transferFuture.get();

        assertEquals(1, signedTransaction.getTx().getOutputStates().size());
        assertEquals(1, signedTransaction.getTx().getInputs().size());

    }


    @Test
    public void transactionHasTransferCommandWithOwnerAsSignerTest() throws Exception {
        IssueAssetFlow flow = new IssueAssetFlow("Gold", 10, A.getInfo().getLegalIdentities().get(0));
        TransferAssetFlow transferFlow = new TransferAssetFlow("Gold", 10, B.getInfo().getLegalIdentities().get(0));

        CordaFuture<SignedTransaction> future = Mint.startFlow(flow);
        setup();

        CordaFuture<SignedTransaction> transferFuture = A.startFlow(transferFlow);
        setup();

        SignedTransaction signedTransaction = transferFuture.get();

        assertEquals(1, signedTransaction.getTx().getCommands().size());
        Command command = signedTransaction.getTx().getCommands().get(0);

        assert (command.getValue() instanceof AssetContract.Transfer);
        assertTrue(command.getSigners().contains(A.getInfo().getLegalIdentities().get(0).getOwningKey()));

    }
}