package com.asset.states;

import com.asset.contracts.AssetContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;

import java.util.Arrays;
import java.util.List;

// *********
// * State *
// *********
@BelongsToContract(AssetContract.class)
public class AssetState implements ContractState {

    private String assetName;
    private int weight;
    private Party issuer;
    private Party owner;

    public AssetState(String assetName, int weight, Party issuer, Party owner) {
        this.assetName = assetName;
        this.weight = weight;
        this.issuer = issuer;
        this.owner = owner;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(issuer, owner);
    }

    public String getAssetName() {return assetName;}
    public int getWeight() {return weight;}
    public Party getIssuer() {return issuer;}
    public Party getOwner() {return owner;}

}