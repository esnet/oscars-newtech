'use strict';
const moment = require('moment');
/*
 opts = {
    method: String,
    url: String,
    params: String | Object,
    headers: Object
 }
 */
function loadJSON(opts) {
    return new Promise(function(resolve, reject){
        let xhr = new XMLHttpRequest();
        xhr.overrideMimeType('application/json');
        xhr.open(opts.method, opts.url);
        xhr.onload = function(){
            if (this.status >= 200 && this.status < 300) {
                resolve(xhr.response);
            } else {
                reject({
                    status: this.status,
                    statusText: xhr.statusText
                });
            }
        };
        xhr.onerror = function () {
            reject({
                status: this.status,
                statusText: xhr.statusText
            });
        };
        if(opts.headers){
            Object.keys(opts.headers).forEach(function (key){
                xhr.setRequestHeader(key, opts.headers[key]);
            });
        }
        xhr.setRequestHeader("Content-type", "application/json; charset=UTF-8");
        let params = opts.params;
        if(params){
            if(typeof params === 'object'){
                params = JSON.stringify(params);
            }
            xhr.send(params);
        }
        else{
            xhr.send();
        }
    });
}

function submitReservation(url, reservation){
    // Must convert start/end dates to Integers
    let modifiedRes = formatReservation(reservation);
    return submit("POST", url, modifiedRes)
}

function submit(method, url, payload){
    let csrfToken = $("meta[name='_csrf']").attr("content");
    let csrfHeader = $("meta[name='_csrf_header']").attr("content");

    let headers = {};
    headers[csrfHeader] = csrfToken;

    return loadJSON({method: method, url: url, headers: headers, params: payload});
}

function formatReservation(reservation){

    let filteredJunctions = {};
    let junctionKeys = Object.keys(reservation.junctions);
    for(let index = 0; index < junctionKeys.length; index++){
        let junction = reservation.junctions[junctionKeys[index]];
        let filteredJunction = {id: junction.id, label: junction.label, fixtures: filterFixtures(junction.fixtures)};
        filteredJunctions[filteredJunction.id] = filteredJunction;
    }
    return {
        junctions: filteredJunctions,
        pipes: reservation.pipes,
        startAt: parseInt(moment(reservation.startAt).unix()),
        endAt: parseInt(moment(reservation.endAt).unix()),
        description: reservation.description,
        connectionId: reservation.connectionId
    };
}

function filterFixtures(fixtures){
    let filteredFixtures = {};
    let fixtureKeys = Object.keys(fixtures);
    for(let index = 0; index < fixtureKeys.length; index++){
        let fixture = fixtures[fixtureKeys[index]];
        if(fixture.selected){
            filteredFixtures[fixture.id] = fixture;
        }
    }
    return filteredFixtures;
}

module.exports = {loadJSON, submitReservation, submit};