package com.asset.contracts;

import com.asset.states.AssetState;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.testing.core.TestIdentity;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class AssetStateTest {

    private Party Mint = new TestIdentity(new CordaX500Name("mint","","GB")).getParty();
    private Party Trader = new TestIdentity(new CordaX500Name("trader","","GB")).getParty();

    @Test
    public void assetStateImplementsContractStateTest() {
        assertTrue(new AssetState("Gold", 10, Mint, Trader) instanceof ContractState);
    }

    @Test
    public void assetStateHasTwoParticipantsTheIssuerAndOwnerTest() {
        AssetState assetState = new AssetState("Gold", 10, Mint, Trader);
        assertEquals(2, assetState.getParticipants().size());
        assertTrue(assetState.getParticipants().contains(Mint));
        assertTrue(assetState.getParticipants().contains(Trader));

    }

    @Test
    public void assetStateHasGettersForAllFieldsTest() {
        AssetState assetState = new AssetState("Gold", 10, Mint, Trader);

        assertEquals("Gold", assetState.getAssetName());
        assertEquals(10, assetState.getWeight());
        assertEquals(Mint, assetState.getIssuer());
        assertEquals(Trader, assetState.getOwner());


    }
}
