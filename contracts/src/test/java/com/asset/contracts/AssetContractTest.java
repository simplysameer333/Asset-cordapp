package com.asset.contracts;

import com.asset.states.AssetState;
import net.corda.core.contracts.Contract;
import net.corda.core.identity.CordaX500Name;
import net.corda.testing.contracts.DummyState;
import net.corda.testing.core.DummyCommandData;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.MockServices;
import org.junit.Test;

import static net.corda.testing.node.NodeTestUtils.transaction;

public class AssetContractTest {

    private final TestIdentity Mint = new TestIdentity (new CordaX500Name ("mint", "", "GB"));
    private final TestIdentity TraderA = new TestIdentity (new CordaX500Name ("traderA", "", "GB"));
    private final TestIdentity TraderB = new TestIdentity (new CordaX500Name ("traderB", "", "GB"));


    private final MockServices ledgerServices = new MockServices();

    private AssetState assetState = new AssetState("Gold", 10, Mint.getParty(), TraderA.getParty());
    private AssetState assetStateInput = new AssetState("Gold", 10, Mint.getParty(), TraderA.getParty());
    private AssetState assetStateOutput = new AssetState("Gold", 10, Mint.getParty(), TraderB.getParty());

    @Test
    public void assetContractImplementsContractTestTest() {
        assert (new AssetContract() instanceof Contract);
    }

//    ------------------------------------- Issue Command Tests -------------------------------------


    @Test
    public void assetContractRequiresZeroInputsInIssueTransactionTest() {

        transaction(ledgerServices, tx -> {
            // Has an input, will fail
            tx.input(AssetContract.CID, assetState);
            tx.command(Mint.getPublicKey(), new AssetContract.Issue());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Does not have an input, will verify
            tx.output(AssetContract.CID, assetState);
            tx.command(Mint.getPublicKey(), new AssetContract.Issue());
            tx.verifies();
            return null;
        });

    }



    @Test
    public void AssetContractRequiresOneOutputInIssueTransactionTestTest() {

        transaction(ledgerServices, tx -> {
            // Has two outputs, will fail
            tx.output(AssetContract.CID, assetState);
            tx.output(AssetContract.CID, assetState);
            tx.command(Mint.getPublicKey(), new AssetContract.Issue());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has one output, will verify
            tx.output(AssetContract.CID, assetState);
            tx.command(Mint.getPublicKey(), new AssetContract.Issue());
            tx.verifies();
            return null;
        });

    }



    @Test
    public void assetContractRequiresTheTransactionOutputToBeAassetStateTestTest() {

        transaction(ledgerServices, tx -> {
            // Has wrong output, will fail
            tx.output(AssetContract.CID, new DummyState());
            tx.command(Mint.getPublicKey(), new AssetContract.Issue());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has Correct output, will verify
            tx.output(AssetContract.CID, assetState);
            tx.command(Mint.getPublicKey(), new AssetContract.Issue());
            tx.verifies();
            return null; });

    }


    @Test
    public void assetContractRequiresTheTransactionCommandToBeAnIssueCommandTest() {

        transaction(ledgerServices, tx -> {
            // Has wrong command, will fail
            tx.output(AssetContract.CID, assetState);
            tx.command(Mint.getPublicKey(), DummyCommandData.INSTANCE);
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has correct command, will verify
            tx.output(AssetContract.CID, assetState);
            tx.command(Mint.getPublicKey(), new AssetContract.Issue());
            tx.verifies();
            return null;
        });

    }


    @Test
    public void assetContractRequiresTheIssuerToBeARequiredSignerInTheTransactionTest() {

        transaction(ledgerServices, tx -> {
            // Issuer is not a required signer, will fail
            tx.output(AssetContract.CID, assetState);
            tx.command(TraderA.getPublicKey(), new AssetContract.Issue());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Issuer is a required, will verify
            tx.output(AssetContract.CID, assetState);
            tx.command(Mint.getPublicKey(), new AssetContract.Issue());
            tx.verifies();
            return null;
        });

    }



//    ------------------------------------- Transfer Command Tests -------------------------------------


    @Test
    public void assetContractRequiresOneInputAndOneOutputInTransferTransactionTest() {

        transaction(ledgerServices, tx -> {
            // Has an input and output, will verify
            tx.input(AssetContract.CID, assetStateInput);
            tx.output(AssetContract.CID, assetStateOutput);
            tx.command(TraderA.getPublicKey(), new AssetContract.Transfer());
            tx.verifies();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Does not have an input, will fail
            tx.output(AssetContract.CID, assetStateOutput);
            tx.command(TraderA.getPublicKey(), new AssetContract.Transfer());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Does not have an output, will fail
            tx.input(AssetContract.CID, assetStateInput);
            tx.command(TraderA.getPublicKey(), new AssetContract.Transfer());
            tx.fails();
            return null;
        });

    }



    @Test
    public void assetContractRequiresTheTransactionCommandToBeATransferCommandTest() {

        transaction(ledgerServices, tx -> {
            // Has wrong command, will fail
            tx.input(AssetContract.CID, assetStateInput);
            tx.output(AssetContract.CID, assetStateOutput);
            tx.command(TraderA.getPublicKey(), DummyCommandData.INSTANCE);
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has correct command type, will verify
            tx.input(AssetContract.CID, assetStateInput);
            tx.output(AssetContract.CID, assetStateOutput);
            tx.command(TraderA.getPublicKey(), new AssetContract.Transfer());
            tx.verifies();
            return null;
        });

    }


    @Test
    public void assetContractRequiresTheOwnerToBeARequiredSignerTest() {

        transaction(ledgerServices, tx -> {
            // Owner is required signer, will verify
            tx.input(AssetContract.CID, assetStateInput);
            tx.output(AssetContract.CID, assetStateOutput);
            tx.command(TraderA.getPublicKey(), new AssetContract.Transfer());
            tx.verifies();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Owner is not required signer, will fail
            tx.input(AssetContract.CID, assetStateInput);
            tx.output(AssetContract.CID, assetStateOutput);
            tx.command(Mint.getPublicKey(), new AssetContract.Transfer());
            tx.fails();
            return null;
        });

    }
}