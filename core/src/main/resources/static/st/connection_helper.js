/** Determines equality of Connections by performing deep equality checks of all contained parameters, Requested/Reserved objects and collections **/
function sameConnection(oldConn, newConn)
{
    var oldSpec = oldConn.specification;
    var oldFlow = oldSpec.requested.vlanFlow;
    var oldJuncSet = oldFlow.junctions;
    var oldPipeSet = oldFlow.pipes;

    var newSpec = newConn.specification;
    var newFlow = newSpec.requested.vlanFlow;
    var newJuncSet = newFlow.junctions;
    var newPipeSet = newFlow.pipes;

    var oldResFlow = oldConn.reserved.vlanFlow;
    var newResFlow = newConn.reserved.vlanFlow;

    var oldResJuncs = oldResFlow.junctions;
    var newResJuncs = newResFlow.junctions;
    var oldResEthPipes = oldResFlow.ethPipes;
    var newResEthPipes = newResFlow.ethPipes;
    var oldResMplsPipes = oldResFlow.mplsPipes;
    var newResMplsPipes = newResFlow.mplsPipes;
    var oldBiPaths = oldResFlow.allPaths;
    var newBiPaths = oldResFlow.allPaths;

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

    var newJuncURNs = [];

    for(var j = 0; j < newJuncSet.length; j++)
        newJuncURNs.push(newJuncSet[j].deviceUrn);

    for(var j = 0; j < oldJuncSet.length; j++)
    {
        var oldJunc = oldJuncSet[j];
        var oldJuncURN = oldJunc.deviceUrn;

        var newJ = $.inArray(oldJuncURN, newJuncURNs);

        if(newJ === -1)
            return false;

        var newJunc = newJuncSet[newJ];

        if(!sameRequestedJunction(oldJunc, newJunc))
            return false;
    }

    // Pipes
    if(oldPipeSet.length !== newPipeSet.length)
        return false;

    var newPipeIDs = [];

    for(var p = 0; p < newPipeSet.length; p++)
        newPipeIDs.push(newPipeSet[p].id);

    for(var p = 0; p < oldPipeSet.length; p++)
    {
        var oldPipe = oldPipeSet[p];
        var oldPipeID = oldPipe.id;

        var newP = $.inArray(oldPipeID, newPipeIDs);

        if(newP === -1)
            return false;

        var newPipe = newPipeSet[newP];

        var oldJuncA = oldPipe.aJunction;
        var oldJuncZ = oldPipe.zJunction;

        var newJuncA = newPipe.aJunction;
        var newJuncZ = newPipe.zJunction;

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

    var newResJuncURNs = [];

    for(var j = 0; j < newResJuncs.length; j++)
        newResJuncURNs.push(newResJuncs[j].deviceUrn);

    for(var j = 0; j < oldResJuncs.length; j++)
    {
        var oldResJunc = oldResJuncs[j];
        var oldResJuncURN = oldResJunc.deviceUrn;

        var newJ = $.inArray(oldResJuncURN, newResJuncURNs)

        if(newJ === -1)
            return false;

        var newResJunc = newResJuncs[newJ];

        if(!sameReservedJunction(oldResJunc, newResJunc))
            return false;
    }

    // Ethernet Pipes
    if(oldResEthPipes.length !== newResEthPipes.length)
        return false;

    var newResEthPipeIDs = [];

    for(var p = 0; p < newResEthPipes.length; p++)
        newResEthPipeIDs.push(newResEthPipes[p].id)

    for(var p = 0; p < oldResEthPipes.length; p++)
    {
        var oldResPipe = oldResEthPipes[p];
        var oldResPipeID = oldResPipe.id;

        var newP = $.inArray(oldResPipeID, newResEthPipeIDs);

        if(newP === -1)
            return false;

        var newResPipe = newResEthPipes[newP];

        if(!sameReservedPipe(oldResPipe, newResPipe, "ETHERNET"))
            return false;
    }

    // Mpls Pipes
    if(oldResMplsPipes.length !== newResMplsPipes.length)
        return false;

    var newResMplsPipeIDs = [];

    for(var p = 0; p < newResMplsPipes.length; p++)
        newResMplsPipeIDs.push(newResMplsPipes[p].id)

    for(var p = 0; p < oldResMplsPipes.length; p++)
    {
        var oldResPipe = oldResMplsPipes[p];
        var oldResPipeID = oldResPipe.id;

        var newP = $.inArray(oldResPipeID, newResMplsPipeIDs);

        if(newP === -1)
            return false;

        var newResPipe = newResMplsPipes[newP];

        if(!sameReservedPipe(oldResPipe, newResPipe, "MPLS"))
            return false;
    }

    // Bidirectional Paths
    if(oldBiPaths.length != newBiPaths.length)
        return false;

    var newBiPathIDs = [];

    for(var bi = 0; bi < newBiPaths.length; bi++)
        newBiPathIDs.push(newBiPaths[bi].uniqueID);

    for(var bi = 0; bi < oldBiPaths.length; bi++)
    {
        var oldBiPath = oldBiPaths[bi];
        var oldBiPathID = oldBiPath.uniqueID;

        var newBI = $.inArray(oldBiPathID, newBiPathIDs);

        if(newBI === -1)
            return false;

        var newBiPath = newBiPaths[newBI];

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
    var oldFixSet = oldJunc.fixtures;
    var newFixSet = newJunc.fixtures;
    var newFixURNs = [];

    if(oldFixSet.length !== newFixSet.length)
        return false;

    for(var f = 0; f < newFixSet.length; f++)
        newFixURNs.push(newFixSet[f].portUrn);

    for(var f = 0; f < oldFixSet.length; f++)
    {
        var oldFix = oldFixSet[f];
        var oldFixURN = oldFix.portUrn;

        var newF = $.inArray(oldFixURN, newFixURNs);

        if(newF === -1)
            return false;

        var newFix = newFixSet[newF];

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
    var oldFixSet = oldJunc.fixtures;
    var newFixSet = newJunc.fixtures;
    var newFixURNs = [];

    if(oldFixSet.length !== newFixSet.length)
        return false;

    for(var f = 0; f < newFixSet.length; f++)
        newFixURNs.push(newFixSet[f].portUrn);

    for(var f = 0; f < oldFixSet.length; f++)
    {
        var oldFix = oldFixSet[f];
        var oldFixURN = oldFix.portUrn;

        var newF = $.inArray(oldFixURN, newFixURNs);

        if(newF === -1)
            return false;

        var newFix = newFixSet[newF];

        if(oldFix.portUrn !== newFix.portUrn || oldFix.fixtureType !== newFix.fixtureType)
            return false;
        if(oldFix.vlanId !== newFix.vlanId || oldFix.vlanExpression !== newFix.vlanExpression)
            return false;
        if(oldFix.inMbps !== newFix.inMbps || oldFix.egMbps !== newFix.egMbps)
            return false;
    }

    // Vlans
    var oldVlanSet = oldJunc.reservedVlans;
    var newVlanSet = newJunc.reservedVlans;
    var newVlanInts = []

    if(oldVlanSet.length !== newVlanSet.length)
        return false;

    for(var v = 0; v < newVlanSet.length; v++)
        newVlanInts.push(newVlanSet[v].vlan);

    for(var v = 0; v < oldVlanSet.length; v++)
    {
        var oldVlan = oldVlanSet[v];
        var oldVlanInt = oldVlan.vlan;

        var newV = $.inArray(oldVlanInt, newVlanInts);

        if(newV === -1)
            return false;

        var newVlan = newVlanSet[newV];

        if(oldVlan.urn !== newVlan.urn || oldVlan.vlan !== newVlan.vlan || oldVlan.beginning !== newVlan.beginning || oldVlan.ending !== newVlan.ending)
            return false;
    }

    return true;
}


/** Determines equality of Reserved Pipes by performing deep equality checks of all contained parameters, objects, and collections **/
function sameReservedPipe(oldResPipe, newResPipe, pipeType)
{
    if(oldResPipe.pipeType !== newResPipe.pipeType)
        return false;
    if(!arraysEqual(oldResPipe.azERO, newResPipe.azERO) || !arraysEqual(oldResPipe.zaERO, newResPipe.zaERO))
        return false;

    // Junctions
    var oldJuncA = oldResPipe.aJunction;
    var oldJuncZ = oldResPipe.zJunction;

    var newJuncA = newResPipe.aJunction;
    var newJuncZ = newResPipe.zJunction;

    if(!sameReservedJunction(oldJuncA, newJuncA) || !sameReservedJunction(oldJuncZ, newJuncZ))
        return false;

    // ReservedBandwidths
    var oldResBwSet = oldResPipe.reservedBandwidths;
    var newResBwSet = newResPipe.reservedBandwidths;

    if(oldResBwSet.length !== newResBwSet.length)
        return false;

    var newResBwURNs = [];

    for(bw = 0; bw < newResBwSet.length; bw++)
        newResBwURNs.push(newResBwSet[bw].urn);

    for(bw = 0; bw < oldResBwSet.length; bw++)
    {
        var oldResBw = oldResBwSet[bw];
        var oldResBwURN = oldResBw.urn;

        var newBW = $.inArray(oldResBwURN, newResBwURNs);

        if(newBW === -1)
            return false;

        var newResBw = newResBwSet[newBW];

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

    var oldVlanSet = oldResPipe.reservedVlans;
    var newVlanSet = newResPipe.reservedVlans;
    var newVlanURNs = []

    if(oldVlanSet.length !== newVlanSet.length)
        return false;

    for(var v = 0; v < newVlanSet.length; v++)
        newVlanURNs.push(newVlanSet[v].urn);

    for(var v = 0; v < oldVlanSet.length; v++)
    {
        var oldVlan = oldVlanSet[v];
        var oldVlanURN = oldVlan.urn;

        var newV = $.inArray(oldVlanURN, newVlanURNs);

        if(newV === -1)
            return false;

        var newVlan = newVlanSet[newV];

        if(oldVlan.urn !== newVlan.urn || oldVlan.vlan !== newVlan.vlan || oldVlan.beginning !== newVlan.beginning || oldVlan.ending !== newVlan.ending)
            return false;
    }

    return true;
}


function connectionIndex(theConnection, theSet)
{
    for(var c = 0; c < theSet.length; c++)
    {
        var oneConn = theSet[c];

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

    for(var i = 0; i < arr1.length; i++)
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

    for(var i = 0; i < set1.length; i++)
    {
        var el1 = set1[i];
        if($.inArray(el1, set2) === -1)
            return false;
    }

    return true;
}
