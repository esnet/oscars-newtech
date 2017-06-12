const React = require('react');
const ReservationHeatMap = require('./reservationHeatMap');
const NavBar = require('./navbar');
const client = require('./client');
const connHelper = require('./connectionHelper');
const networkVis = require('./networkVis');
const vis = require('../../../node_modules/vis/dist/vis');
const ReservationList = require("./reservationList");
import Dropdown from 'react-dropdown';
const DateTime = require('react-datetime');

let timeoutId = -1;
let resStatusFilter = "Reservation Status";
let provStatusFilter = "Provision Status";
let operStatusFilter = "Operation Status";
let minBandwidthFilter = "Minimum Bandwidth (Mbps)";
let maxBandwidthFilter = "Maximum Bandwidth (Mbps)";
let userNameFilter = "User Name";
let startFilter = "Start After Date";
let endFilter = "End Before Date";
let idFilter = "Connection ID";

class ReservationListApp extends React.Component{

    constructor(props){
        super(props);
        this.state = {
            reservations: [],
            timeoutId: -1,
            filters: [],
            newFilter: {id: 0, text: "", type: "Connection ID"},
            filterTypes: [idFilter, userNameFilter, minBandwidthFilter, maxBandwidthFilter, startFilter, endFilter, resStatusFilter,
                provStatusFilter, operStatusFilter],
            resvStates: ["IDLE_WAIT", "SUBMITTED", "HELD", "COMMITTING", "ABORTING", "ABORT_FAILED"],
            provStates: ["INITIAL", "READY_TO_GENERATE", "GENERATING", "DISMANTLED_MANUAL", "DISMANTLED_AUTO",
                "BUILDING_MANUAL", "BUILDING_AUTO", "BUILT_MANUAL", "BUILT_AUTO", "DISMANTLING_MANUAL", "DISMANTLING_AUTO",
                "FAILED"],
            operStates: ["ADMIN_DOWN_OPER_DOWN", "ADMIN_DOWN_OPER_UP", "ADMIN_UP_OPER_UP", "ADMIN_UP_OPER_DOWN"],
            updateHeatMap: false
        };
        this.setState = this.setState.bind(this);
        this.componentDidMount = this.componentDidMount.bind(this);
        this.updateReservations = this.updateReservations.bind(this);
        this.evaluateReservationList = this.evaluateReservationList.bind(this);
        this.componentWillUnmount = this.componentWillUnmount.bind(this);
        this.handleFilterTypeSelect = this.handleFilterTypeSelect.bind(this);
        this.handleAddFilter = this.handleAddFilter.bind(this);
        this.handleDeleteFilter = this.handleDeleteFilter.bind(this);
        this.handleFilterTextChange = this.handleFilterTextChange.bind(this);
        this.handleFilterDateChange = this.handleFilterDateChange.bind(this);
        this.handleStateDropdownSelect = this.handleStateDropdownSelect.bind(this);
        this.updateReservationList = this.updateReservationList.bind(this);
        this.sortNewestSubmissionFirst = this.sortNewestSubmissionFirst.bind(this);
    }

    componentDidMount(){
        this.updateReservations();
    }

    componentWillUnmount(){
        clearTimeout(timeoutId);
    }

    updateReservations(){
        this.updateReservationList();
        timeoutId = setTimeout(this.updateReservations, 30000);   // Updates every 30 seconds
    }

    updateReservationList(){
        let combinedFilter = this.makeCombinedFilter();
        client.submit("POST", '/resv/list/filter', combinedFilter)
            .then(
                (successResponse) => {
                    this.evaluateReservationList(successResponse);
                },
                (failResponse) => {
                    console.log("Error: " + failResponse.status + " - " + failResponse.statusText);
                }
            );
    };

    makeCombinedFilter(){
        let filters = this.state.filters;
        let combinedFilter = {
            numFilters: filters.length,
            userNames: [],
            connectionIds: [],
            minBandwidths: [],
            maxBandwidths: [],
            startDates: [],
            endDates: [],
            resvStates: [],
            provStates: [],
            operStates: []
        };
        for(let i = 0; i < filters.length; i++){
            let filter = filters[i];
            switch(filter.type){
                case userNameFilter:
                    combinedFilter.userNames.push(filter.text);
                    break;
                case minBandwidthFilter:
                    combinedFilter.minBandwidths.push(filter.text);
                    break;
                case maxBandwidthFilter:
                    combinedFilter.maxBandwidths.push(filter.text);
                    break;
                case idFilter:
                    combinedFilter.connectionIds.push(filter.text);
                    break;
                case startFilter:
                    combinedFilter.startDates.push(filter.text);
                    break;
                case endFilter:
                    combinedFilter.endDates.push(filter.text);
                    break;
                case resStatusFilter:
                    combinedFilter.resvStates.push(filter.text);
                    break;
                case provStatusFilter:
                    combinedFilter.provStates.push(filter.text);
                    break;
                case operStatusFilter:
                    combinedFilter.operStates.push(filter.text);
                    break;
            }
        }
        return combinedFilter;
    }

    evaluateReservationList(response){
        let resvs = JSON.parse(response);
        if(connHelper.listHasChanged(this.state.reservations, resvs)){
            this.sortNewestSubmissionFirst(resvs);
            this.setState({reservations: resvs, updateHeatMap: true});
        }
        else{
            this.setState({updateHeatMap: false})
        }
    }

    sortNewestSubmissionFirst(resvs){
        let dateMap = {};
        resvs.forEach((r) => {dateMap[r.connectionId] = new Date(r.schedule.submitted)});
        resvs.sort((r1, r2) => {
            return dateMap[r2.connectionId].getTime() -dateMap[r1.connectionId].getTime();
        });
    }

    handleFilterTypeSelect(type){
        this.setState({newFilter: {id: this.state.newFilter.id, text: "", type: type.value}, updateHeatMap: false});
    }

    handleAddFilter(){
        let filter = $.extend(true, {}, this.state.newFilter);
        if(filter.text == "" || (filter.type == minBandwidthFilter || filter.type == maxBandwidthFilter) && isNaN(filter.text)) {
            console.log("Invalid filter input: must not be empty, and must not have non numeric characters for bandwidth.")
        }
        else{
            this.state.filters.push(filter);
            this.setState({newFilter: {id: filter.id + 1, text: "", type: filter.type}, updateHeatMap: false}, this.updateReservationList);
        }
    }

    handleDeleteFilter(filter){
        let id = filter.id;
        let filteredList = this.state.filters.filter((f) => {return f.id != id});
        this.setState({filters: filteredList, updateHeatMap: false}, this.updateReservationList);
    }

    handleStateDropdownSelect(type){
        this.setState({newFilter: {id: this.state.newFilter.id, text: type.value, type: this.state.newFilter.type}, updateHeatMap: false});
    }

    handleFilterTextChange(event){
        let newText = event.target.value;
        this.setState({newFilter: {id: this.state.newFilter.id, text: newText, type: this.state.newFilter.type}, updateHeatMap: false});
    }

    handleFilterDateChange(newMoment){
        let newDate = newMoment.toDate();
        this.setState({newFilter: {id: this.state.newFilter.id, text: newDate, type: this.state.newFilter.type}, updateHeatMap: false});
    }

    render() {
        return (
            <div>
                <NavBar isAuthenticated={this.props.route.isAuthenticated} isAdmin={this.props.route.isAdmin}/>
                <FilterPanel newFilter={this.state.newFilter}
                             filters={this.state.filters}
                             filterTypes={this.state.filterTypes}
                             resvStates={this.state.resvStates}
                             provStates={this.state.provStates}
                             operStates={this.state.operStates}
                             handleAddFilter={this.handleAddFilter}
                             handleDeleteFilter={this.handleDeleteFilter}
                             handleFilterTypeSelect={this.handleFilterTypeSelect}
                             handleFilterTextChange={this.handleFilterTextChange}
                             handleFilterDateChange={this.handleFilterDateChange}
                             handleStateDropdownSelect={this.handleStateDropdownSelect}
                />
                <ReservationHeatMap updateHeatMap={this.state.updateHeatMap}/>
                <p style={{marginLeft: '40px', color: '#2c5699'}}> Select a connection to view additional reservation details.</p>
                <ReservationList reservations={this.state.reservations} alwaysExpanded={false}/>
            </div>
        );
    }
}

class FilterPanel extends React.Component{

    render(){
        let input = null;
        switch(this.props.newFilter.type){
            case startFilter:
            case endFilter:
                input = <DateTime
                    value={this.props.newFilter.text}
                    onChange={this.props.handleFilterDateChange}
                    inputProps={{readOnly: true, style:{backgroundColor: "white"}}}/>;
                break;
            case resStatusFilter:
                input = <Dropdown options={this.props.resvStates}
                          value={this.props.newFilter.text}
                          placeholder="Select a Reservation Status."
                          onChange={this.props.handleStateDropdownSelect}/>;
                break;
            case provStatusFilter:
                input = <Dropdown options={this.props.provStates}
                                  value={this.props.newFilter.text}
                                  placeholder="Select a Provisioning Status."
                                  onChange={this.props.handleStateDropdownSelect}/>;
                break;
            case operStatusFilter:
                input = <Dropdown options={this.props.operStates}
                                  value={this.props.newFilter.text}
                                  placeholder="Select an Operation Status."
                                  onChange={this.props.handleStateDropdownSelect}/>;
                break;
            default:
                input = <input style={{ width: "14.25em" }}
                       className="form-control input-md"
                       value={this.props.newFilter.text}
                       onChange={this.props.handleFilterTextChange}/>;
        }


        return(
            <div style={{marginLeft: "5px"}}>
                <p>Filter Reservations By: </p>
                <div style={{ display: "flex", flexWrap: "wrap"}}>
                    <Dropdown options={this.props.filterTypes}
                              value={this.props.newFilter.type}
                              placeholder="Select a filter type"
                              onChange={this.props.handleFilterTypeSelect}
                    />
                    {input}
                    <input type="button" className="btn btn-primary" value="Add" onClick={this.props.handleAddFilter} />
                </div>
                <FilterList filters={this.props.filters} handleDeleteFilter={this.props.handleDeleteFilter}/>
            </div>
        );
    }
}


class FilterList extends React.Component{

    render(){
        let listItems = this.props.filters.map((filter) => <FilterItem key={filter.id} filter={filter} handleDeleteFilter={this.props.handleDeleteFilter}/>);
        return(
            <div style={{ display: "flex" , marginTop: "20px"}}>
                <p>List connections that match these tags: <br/> (Click to delete)</p>
                <ul style={{ display: "flex" , flexDirection: "row", listStyle: "none", flexWrap: "wrap"}}>{listItems}</ul>
            </div>
        );
    }
}

class FilterItem extends React.Component{

    render(){
        return(
            <li key={this.props.filter.id}
                onClick={this.props.handleDeleteFilter.bind(this, this.props.filter)}
                className="btn btn-secondary"
                style={{marginRight: "10px", marginTop: "5px", backgroundColor: "lightBlue"}}
            >{this.props.filter.type + ": " + this.props.filter.text}</li>
        );
    }
}


module.exports = ReservationListApp;