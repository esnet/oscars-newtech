/*
reservation = {
    junctions: {},
    pipes: {},
    startAt: Date(),
    endAt: Date(),
    description: "",
    connectionId: ""
};
Junction: {id: ~~, label: ~~, fixtures: {}}
fixtures: {id: {id: ~~, selected: true or false, bandwidth: ~~, vlan: ~~}, id: ~~, ....}
Pipe: {id: ~~, from: ~~, to: ~~, bw: ~~}
*/


function validateReservation(reservation){
    let junctions = reservation.junctions;
    let startAt = reservation.startAt;
    let endAt = reservation.endAt;
    let description = reservation.description;

    let errorMessages = [];

    let descriptionValid = description.length > 0;
    if(!descriptionValid){
        errorMessages.push("Description must be provided.");
    }
    let datesValid = startAt < endAt;
    if(!datesValid){
        errorMessages.push("Start date/time must be before end date/time.");
    }

    let junctionStatus = validateJunctions(junctions);
    errorMessages = errorMessages.concat(junctionStatus.errorMessages);

    return {isValid: junctionStatus.isValid && datesValid && descriptionValid, errorMessages: errorMessages};
}

function validateJunctions(junctions){
    let totalValid = 0;
    let totalValidFixtures = 0;
    let totalSelectedFixtures = 0;
    let junctionNameList = Object.keys(junctions);
    let errorMessages = [];

    for(let index = 0; index < junctionNameList.length; index++){
        let junction = junctions[junctionNameList[index]];
        let countResponse = countValidFixtures(junction);
        if(countResponse.numValidFixtures > 0){
            totalValidFixtures += countResponse.numValidFixtures;
        }
        totalSelectedFixtures += countResponse.numSelectedFixtures;
        if(junction.id != "--" && junction.id != ""){
            totalValid++;
        }
    }

    if(totalValid != junctionNameList.length || junctionNameList.length == 0){
        errorMessages.push("Make sure that at least one junction has been selected.");
    }

    if(totalValidFixtures < totalSelectedFixtures){
        errorMessages.push("All selected fixtures must have bandwidth >= 0 across reservation.");
    }

    let valid = totalValid > 0 && totalValid == junctionNameList.length && totalValidFixtures >= totalSelectedFixtures;
    return {isValid: valid, errorMessages: errorMessages};
}

function countValidFixtures(junction){
    let numValidSelectedFixtures = 0;
    let numSelectedFixtures = 0;
    let fixtures = junction.fixtures;
    let fixtureNameList = Object.keys(fixtures);
    for(let index = 0; index < fixtureNameList.length; index++){
        let fixture = fixtures[fixtureNameList[index]];
        if(fixture.selected){
            numSelectedFixtures++;
            if(validateFixture(fixture)){
                numValidSelectedFixtures++;
            }
        }
    }
    return {numValidFixtures: numValidSelectedFixtures, numSelectedFixtures: numSelectedFixtures};
}

// fixture: {id: ~~, selected: true or false, bandwidth: ~~, vlan: ~~}
function validateFixture(fixture){
    return fixture.selected && fixture.azbw > -1 && fixture.zabw > -1 && fixture.vlan.length > 0;
}

module.exports = {validateReservation};