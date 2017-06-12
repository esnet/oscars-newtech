
const deepEqual = require('deep-equal');

function listHasChanged(oldConnectionList, newConnectionList) {
    // If new list is empty, list has only changed if old was not empty
    if($.isEmptyObject(newConnectionList)){
        return !$.isEmptyObject(oldConnectionList);
    }
    if($.isEmptyObject(oldConnectionList)){
        return !$.isEmptyObject(newConnectionList);
    }

    // Same size
    if(oldConnectionList.length !== newConnectionList.length)
        return true;

    // Same Reservations - All properties unchanged
    for(let o = 0; o < oldConnectionList.length; o++)
    {
        let oldConn = oldConnectionList[o];

        let newIndex = connectionIndex(oldConn, newConnectionList);

        if(newIndex === -1)
            return true;
    }

    return false;
}

/** Determines equality of Connections by performing deep equality checks of all contained parameters, Requested/Reserved objects and collections **/
function sameConnection(oldConn, newConn)
{
    let oldSpec = oldConn.specification;
    let oldFlow = oldSpec.requested.vlanFlow;
    let oldJuncSet = oldFlow.junctions;
    let oldPipeSet = oldFlow.pipes;

    let newSpec = newConn.specification;
    let newFlow = newSpec.requested.vlanFlow;
    let newJuncSet = newFlow.junctions;
    let newPipeSet = newFlow.pipes;

    let oldResFlow = oldConn.reserved.vlanFlow;
    let newResFlow = newConn.reserved.vlanFlow;

    let oldResJuncs = oldResFlow.junctions;
    let newResJuncs = newResFlow.junctions;
    let oldResEthPipes = oldResFlow.ethPipes;
    let newResEthPipes = newResFlow.ethPipes;
    let oldResMplsPipes = oldResFlow.mplsPipes;
    let newResMplsPipes = newResFlow.mplsPipes;
    let oldBiPaths = oldResFlow.allPaths;
    let newBiPaths = oldResFlow.allPaths;

    // Basic Parameters
    if(oldConn.id !== newConn.id)
        return false;
    if(oldConn.connectionId !== newConn.connectionId)
        return false;
    if(oldConn.states.resv !== newConn.states.resv || oldConn.states.prov !== newConn.states.prov || oldConn.states.oper !== newConn.states.oper)
        return false;
    if(oldConn.schedule.submitted !== newConn.schedule.submitted || oldConn.schedule.setup !== newConn.schedule.setup || oldConn.schedule.teardown !== newConn.schedule.teardown)
        return false;


    // Specification
    if(oldSpec.id !== newSpec.id)
        return false;
    if(oldSpec.version !== newSpec.version)
        return false;
    if(oldSpec.username !== newSpec.username)
        return false;
    if(oldSpec.description !== newSpec.description)
        return false;
    if(oldSpec.containerConnectionId !== newSpec.containerConnectionId)
        return false;
    if(!arraysEqual(oldSpec.scheduleSpec.startDates, newSpec.scheduleSpec.startDates) || !arraysEqual(oldSpec.scheduleSpec.endDates, newSpec.scheduleSpec.endDates) || oldSpec.scheduleSpec.minimumDuration !== newSpec.scheduleSpec.minimumDuration)
        return false;


    // Requested Reservation Objects
    if(oldFlow.minPipes !== newFlow.minPipes || oldFlow.maxPipes !== newFlow.maxPipes || oldFlow.containerConnectionId !== newFlow.containerConnectionId)
        return false;

    // Junctions
    if(oldJuncSet.length !== newJuncSet.length)
        return false;

    let newJuncURNs = [];

    for(let j = 0; j < newJuncSet.length; j++)
        newJuncURNs.push(newJuncSet[j].deviceUrn);

    for(let j = 0; j < oldJuncSet.length; j++)
    {
        let oldJunc = oldJuncSet[j];
        let oldJuncURN = oldJunc.deviceUrn;

        let newJ = $.inArray(oldJuncURN, newJuncURNs);

        if(newJ === -1)
            return false;

        let newJunc = newJuncSet[newJ];

        if(!sameRequestedJunction(oldJunc, newJunc))
            return false;
    }


    // Pipes
    if(oldPipeSet.length !== newPipeSet.length)
        return false;

    let newPipeIDs = [];

    for(let p = 0; p < newPipeSet.length; p++)
        newPipeIDs.push(newPipeSet[p].id);

    for(let p = 0; p < oldPipeSet.length; p++)
    {
        let oldPipe = oldPipeSet[p];
        let oldPipeID = oldPipe.id;

        let newP = $.inArray(oldPipeID, newPipeIDs);

        if(newP === -1)
            return false;

        let newPipe = newPipeSet[newP];

        let oldJuncA = oldPipe.aJunction;
        let oldJuncZ = oldPipe.zJunction;

        let newJuncA = newPipe.aJunction;
        let newJuncZ = newPipe.zJunction;

        if(oldPipe.azMbps !== newPipe.azMbps || oldPipe.zaMbps !== newPipe.zaMbps)
            return false;
        if(oldPipe.pipeType !== newPipe.pipeType || oldPipe.numDisjoint !== newPipe.numDisjoint)
            return false;
        if(oldPipe.eroPalindromic !== newPipe.eroPalindromic || oldPipe.eroSurvivability !== newPipe.eroSurvivability)
            return false;
         if(!arraysEqual(oldPipe.azERO, newPipe.azERO) || !arraysEqual(oldPipe.zaERO, newPipe.zaERO))
            return false;
         if(!setsEqual(oldPipe.urnBlacklist, newPipe.urnBlacklist))
            return false;
        if(!sameRequestedJunction(oldJuncA, newJuncA) || !sameRequestedJunction(oldJuncZ, newJuncZ))
            return false;
    }

    // Reserved Objects
    // Junctions
    if(oldResJuncs.length !== newResJuncs.length)
        return false;

    let newResJuncURNs = [];

    for(let j = 0; j < newResJuncs.length; j++)
        newResJuncURNs.push(newResJuncs[j].deviceUrn);

    for(let j = 0; j < oldResJuncs.length; j++)
    {
        let oldResJunc = oldResJuncs[j];
        let oldResJuncURN = oldResJunc.deviceUrn;

        let newJ = $.inArray(oldResJuncURN, newResJuncURNs);

        if(newJ === -1)
            return false;

        let newResJunc = newResJuncs[newJ];

        if(!sameReservedJunction(oldResJunc, newResJunc))
            return false;
    }


    // Ethernet Pipes
    if(oldResEthPipes.length !== newResEthPipes.length)
        return false;

    let newResEthPipeIDs = [];

    for(let p = 0; p < newResEthPipes.length; p++)
        newResEthPipeIDs.push(newResEthPipes[p].id)

    for(let p = 0; p < oldResEthPipes.length; p++)
    {
        let oldResPipe = oldResEthPipes[p];
        let oldResPipeID = oldResPipe.id;

        let newP = $.inArray(oldResPipeID, newResEthPipeIDs);

        if(newP === -1)
            return false;

        let newResPipe = newResEthPipes[newP];

        if(!sameReservedPipe(oldResPipe, newResPipe, "ETHERNET"))
            return false;
    }

    // Mpls Pipes
    if(oldResMplsPipes.length !== newResMplsPipes.length)
        return false;

    let newResMplsPipeIDs = [];

    for(let p = 0; p < newResMplsPipes.length; p++)
        newResMplsPipeIDs.push(newResMplsPipes[p].id)

    for(let p = 0; p < oldResMplsPipes.length; p++)
    {
        let oldResPipe = oldResMplsPipes[p];
        let oldResPipeID = oldResPipe.id;

        let newP = $.inArray(oldResPipeID, newResMplsPipeIDs);

        if(newP === -1)
            return false;

        let newResPipe = newResMplsPipes[newP];

        if(!sameReservedPipe(oldResPipe, newResPipe, "MPLS"))
            return false;
    }


    // Bidirectional Paths
    if(oldBiPaths.length != newBiPaths.length)
        return false;

    let newBiPathIDs = [];

    for(let bi = 0; bi < newBiPaths.length; bi++)
        newBiPathIDs.push(newBiPaths[bi].uniqueID);

    for(let bi = 0; bi < oldBiPaths.length; bi++)
    {
        let oldBiPath = oldBiPaths[bi];

        let oldBiPathID = oldBiPath.uniqueID;

        let newBI = $.inArray(oldBiPathID, newBiPathIDs);

        if(newBI === -1)
            return false;

        let newBiPath = newBiPaths[newBI];

        if(!arraysEqual(oldBiPath.azPath, newBiPath.azPath) || !arraysEqual(oldBiPath.zaPath, newBiPath.zaPath))
            return false;
    }


    return true;
}


/** Determines equality of Requested Junctions by performing deep equality checks of all contained parameters, objects, and collections **/
function sameRequestedJunction(oldJunc, newJunc)
{
    if(oldJunc.deviceUrn !== newJunc.deviceUrn || oldJunc.junctionType !== newJunc.junctionType)
        return false;

    // Fixtures
    let oldFixSet = oldJunc.fixtures;
    let newFixSet = newJunc.fixtures;
    let newFixURNs = [];

    if(oldFixSet.length !== newFixSet.length)
        return false;

    for(let f = 0; f < newFixSet.length; f++)
        newFixURNs.push(newFixSet[f].portUrn);

    for(let f = 0; f < oldFixSet.length; f++)
    {
        let oldFix = oldFixSet[f];
        let oldFixURN = oldFix.portUrn;

        let newF = $.inArray(oldFixURN, newFixURNs);

        if(newF === -1)
            return false;

        let newFix = newFixSet[newF];

        if(oldFix.portUrn !== newFix.portUrn || oldFix.fixtureType !== newFix.fixtureType)
            return false;
        if(oldFix.vlanId !== newFix.vlanId || oldFix.vlanExpression !== newFix.vlanExpression)
            return false;
        if(oldFix.inMbps !== newFix.inMbps || oldFix.egMbps !== newFix.egMbps)
            return false;
    }

    return true;
}

/** Determines equality of Reserved Junctions by performing deep equality checks of all contained parameters, objects, and collections **/
function sameReservedJunction(oldJunc, newJunc)
{
    if(oldJunc.deviceUrn !== newJunc.deviceUrn || oldJunc.junctionType !== newJunc.junctionType)
        return false;

    // Fixtures
    let oldFixSet = oldJunc.fixtures;
    let newFixSet = newJunc.fixtures;
    let newFixURNs = [];

    if(oldFixSet.length !== newFixSet.length)
        return false;

    for(let f = 0; f < newFixSet.length; f++)
        newFixURNs.push(newFixSet[f].portUrn);

    for(let f = 0; f < oldFixSet.length; f++)
    {
        let oldFix = oldFixSet[f];
        let oldFixURN = oldFix.portUrn;

        let newF = $.inArray(oldFixURN, newFixURNs);

        if(newF === -1)
            return false;

        let newFix = newFixSet[newF];

        if(oldFix.portUrn !== newFix.portUrn || oldFix.fixtureType !== newFix.fixtureType)
            return false;
        if(oldFix.vlanId !== newFix.vlanId || oldFix.vlanExpression !== newFix.vlanExpression)
            return false;
        if(oldFix.inMbps !== newFix.inMbps || oldFix.egMbps !== newFix.egMbps)
            return false;
    }



    // Vlans
    let oldVlanSet = oldJunc.reservedVlans;
    let newVlanSet = newJunc.reservedVlans;
    let newVlanUrns = [];

    return deepEqual(oldVlanSet, newVlanSet);
}


/** Determines equality of Reserved Pipes by performing deep equality checks of all contained parameters, objects, and collections **/
function sameReservedPipe(oldResPipe, newResPipe, pipeType)
{
    if(oldResPipe.pipeType !== newResPipe.pipeType)
        return false;
    if(!arraysEqual(oldResPipe.azERO, newResPipe.azERO) || !arraysEqual(oldResPipe.zaERO, newResPipe.zaERO))
        return false;

    // Junctions
    let oldJuncA = oldResPipe.aJunction;
    let oldJuncZ = oldResPipe.zJunction;

    let newJuncA = newResPipe.aJunction;
    let newJuncZ = newResPipe.zJunction;

    if(!sameReservedJunction(oldJuncA, newJuncA) || !sameReservedJunction(oldJuncZ, newJuncZ))
        return false;

    // ReservedBandwidths
    let oldResBwSet = oldResPipe.reservedBandwidths;
    let newResBwSet = newResPipe.reservedBandwidths;

    if(oldResBwSet.length !== newResBwSet.length)
        return false;

    let newResBwURNs = [];

    for(let bw = 0; bw < newResBwSet.length; bw++)
        newResBwURNs.push(newResBwSet[bw].urn);

    for(let bw = 0; bw < oldResBwSet.length; bw++)
    {
        let oldResBw = oldResBwSet[bw];
        let oldResBwURN = oldResBw.urn;

        let newBW = $.inArray(oldResBwURN, newResBwURNs);

        if(newBW === -1)
            return false;

        let newResBw = newResBwSet[newBW];

        if(oldResBw.urn !== newResBw.urn || oldResBw.containerConnectionId !== newResBw.containerConnectionId)
            return false;
        if(oldResBw.inBandwidth !== newResBw.inBandwidth || oldResBw.egBandwidth !== newResBw.egBandwidth)
            return false;
        if(oldResBw.beginning !== newResBw.beginning || oldResBw.ending !== newResBw.ending)
            return false;
    }

    //ReservedVlans
    if(pipeType !== "ETHERNET")
        return true;

    let oldVlanSet = oldResPipe.reservedVlans;
    let newVlanSet = newResPipe.reservedVlans;
    let newVlanURNs = [];

    if(oldVlanSet.length !== newVlanSet.length)
        return false;

    for(let v = 0; v < newVlanSet.length; v++)
        newVlanURNs.push(newVlanSet[v].urn);

    for(let v = 0; v < oldVlanSet.length; v++)
    {
        let oldVlan = oldVlanSet[v];
        let oldVlanURN = oldVlan.urn;

        let newV = $.inArray(oldVlanURN, newVlanURNs);

        if(newV === -1)
            return false;

        let newVlan = newVlanSet[newV];

        if(oldVlan.urn !== newVlan.urn || oldVlan.vlan !== newVlan.vlan || oldVlan.beginning !== newVlan.beginning || oldVlan.ending !== newVlan.ending)
            return false;
    }

    return true;
}


function connectionIndex(theConnection, theSet)
{
    for(let c = 0; c < theSet.length; c++)
    {
        let oneConn = theSet[c];

        if(sameConnection(theConnection, oneConn))
            return c;
    }

    return -1;
}

/** Simple helper function to determine array equality by value **/
function arraysEqual(arr1, arr2)
{
    if(arr1.length !== arr2.length)
        return false;

    for(let i = 0; i < arr1.length; i++)
    {
        if(arr1[i] !== arr2[i])
            return false;
    }

    return true;
}

/** Simple helper function to determine set equality by value (but ignoring order) **/
function setsEqual(set1, set2)
{
    if(set1.length !== set2.length)
        return false;

    for(let i = 0; i < set1.length; i++)
    {
        let el1 = set1[i];
        if($.inArray(el1, set2) === -1)
            return false;
    }

    return true;
}

module.exports = {connectionIndex, listHasChanged};