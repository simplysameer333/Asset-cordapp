package com.asset.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.asset.states.AssetState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.InitiatingFlow;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;

import java.util.List;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class SearchVaultFlow extends FlowLogic<Void> {


    void searchForAllState () {

        //    ----------------------------------------------- Search for CONSUMED States-----------------------------------------------


        QueryCriteria consumedCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.CONSUMED);
        List<StateAndRef<AssetState>> consumedMetalStates = getServiceHub().getVaultService().queryBy(AssetState.class, consumedCriteria).getStates();

        if (consumedMetalStates.size() < 1) {
            System.out.println("\n No CONSUMED Asset States found.");
        } else {
            System.out.println("\n Total CONSUMED Asset States found = " + consumedMetalStates.size());
        }

        int c = consumedMetalStates.size();
        for (int i=0; i<c; i++) {
            System.out.println("\n Name: " + consumedMetalStates.get(i).getState().getData().getAssetName());
            System.out.println(" Owner: " + consumedMetalStates.get(i).getState().getData().getOwner());
            System.out.println(" Weight: " + consumedMetalStates.get(i).getState().getData().getWeight());
            System.out.println(" Issuer: " + consumedMetalStates.get(i).getState().getData().getIssuer());
        }




        //    ----------------------------------------------- Search for UNCONSUMED States-----------------------------------------------



        QueryCriteria unConsumedCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
        List<StateAndRef<AssetState>> unConsumedMetalStates = getServiceHub().getVaultService().queryBy(AssetState.class, unConsumedCriteria).getStates();

        if (unConsumedMetalStates.size() < 1) {
            System.out.println("\n No UNCONSUMED Metal States found.");
        } else {
            System.out.println("\n Total UNCONSUMED Metal States found = " + unConsumedMetalStates.size());
        }

        int u = unConsumedMetalStates.size();
        for (int i=0; i<u; i++) {
            System.out.println("\n Name: " + unConsumedMetalStates.get(i).getState().getData().getAssetName());
            System.out.println(" Owner: " + unConsumedMetalStates.get(i).getState().getData().getOwner());
            System.out.println(" Weight: " + unConsumedMetalStates.get(i).getState().getData().getWeight());
            System.out.println(" Issuer: " + unConsumedMetalStates.get(i).getState().getData().getIssuer());
        }


    }



    @Suspendable
    @Override
    public Void call() throws FlowException {
        // Initiator flow logic goes here.

        searchForAllState();


        return null;
    }
}
