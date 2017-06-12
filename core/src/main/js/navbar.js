const React = require('react');
const Link = require('react-router').Link;

// tag::navbar[]
class NavBar extends React.Component{
    render() {
        return (
            <nav className="navbar navbar-default">
                <div className="container-fluid">
                    <div className="navbar-header">
                        <button type="button" className="navbar-toggle collapsed" data-toggle="collapse"
                                data-target="#bs-example-navbar-collapse-1" aria-expanded="false">
                            <span className="sr-only">Toggle navigation</span>
                            <span className="icon-bar"> </span>
                            <span className="icon-bar"> </span>
                            <span className="icon-bar"> </span>
                        </button>
                        <a className="navbar-brand" href="#">OSCARS</a>
                    </div>

                    <div className="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
                        <ul className="nav navbar-nav">
                            <li><Link to="/react/resv/list">Reservation List</Link></li>
                            <li><Link to="/react/resv/gui">New Reservation</Link></li>
                            <li><Link to="/react/resv/whatif">What-If?</Link></li>
                            {this.props.isAuthenticated ?
                                <AuthenticatedNavBarElement /> :
                                <div></div>
                            }
                            {this.props.isAdmin ?
                                <AdminNavBarElement /> :
                                <div></div>
                            }
                            {this.props.isAuthenticated ?
                                <li><a href="/logout">Log out</a></li> :
                                <div></div>
                            }

                        </ul>
                    </div>
                </div>
            </nav>
        )
    }
}

// <li><Link to="/">Reservation List</Link></li>
// <li><Link to="/resv/gui">New Reservation</Link></li>
// <li><Link to="/resv/timebw">What-If?</Link></li>

// <li><a href="/">Reservation List</a></li>
// <li><a href="/resv/gui">New Reservation</a></li>
// <li><a href="/resv/timebw">What-If?</a></li>

class AuthenticatedNavBarElement extends React.Component{

    render(){
        return (
            <li className="dropdown">
                <a href="#" className="dropdown-toggle" data-toggle="dropdown"
                   role="button" aria-haspopup="true" aria-expanded="false">
                    Reservations<span className="caret"> </span></a>

                <ul className="dropdown-menu">
                    <li><a href="/resv/list">List</a></li>
                    <li><a href="/resv/gui">New</a></li>
                </ul>
            </li>
        )
    }
}

class AdminNavBarElement extends React.Component{

    render(){
        return (
            <li className="dropdown">
                <a href="#" className="dropdown-toggle" data-toggle="dropdown"
                   role="button" aria-haspopup="true" aria-expanded="false">Administration
                    <span className="caret"> </span></a>
                <ul className="dropdown-menu">
                    <li><a href="/admin/user_list">User List</a></li>
                    <li><a href="/admin/user_add">Add a user</a></li>
                    <li><a href="/admin/comp_list">Components</a></li>
                    <li><a href="/admin/group_list">Topology</a></li>
                    <li><a href="/admin/cust_list">Accounting</a></li>
                </ul>
            </li>
        )
    }
}
// end::navbar[]

module.exports = NavBar;