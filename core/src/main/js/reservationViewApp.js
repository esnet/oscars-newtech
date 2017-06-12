const React = require('react');
const NavBar = require('./navbar');
const client = require('./client');
const networkVis = require('./networkVis');
const ReservationList = require("./reservationList");

class ReservationViewApp extends React.Component{

    constructor(props){
        super(props);
        this.state = {reservations: []};
    }

    componentDidMount(){
        let url = "/resv/get/" + this.props.params.connectionId;
        client.loadJSON({method: "GET", url: url})
            .then(
                (successResponse) => {
                    let reservation = JSON.parse(successResponse);
                    this.setState({reservations: [reservation]});
                },
                (failResponse) => {
                    console.log("Error: " + failResponse.status + " - " + failResponse.statusText);
                }
            );
    }

    render(){
        return(
            <div>
                <NavBar isAuthenticated={this.props.route.isAuthenticated} isAdmin={this.props.route.isAdmin}/>
                <div><a href={`/resv/view/${this.props.params.connectionId}`}>More details</a></div>
                <ReservationList reservations={this.state.reservations} alwaysExpanded={true}/>

            </div>
        );
    }
}


module.exports = ReservationViewApp;