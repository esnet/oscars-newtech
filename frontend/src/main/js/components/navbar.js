import React, {useContext} from "react";

import {
    Navbar,
    NavbarBrand,
    Nav,
    UncontrolledDropdown,
    DropdownMenu,
    DropdownToggle,
    NavLink
} from "reactstrap";
import {withRouter} from "react-router-dom";
import {UserContext} from "../index";



export function OscarsNavBar() {
    const userCtx = useContext(UserContext);
    console.log(userCtx);
    if (userCtx.isLoggedIn()) {
        return <LoggedInNavBar/>;
    } else {
        return <LoggedOutNavBar/>;
    }
}

function LoggedOutNavBar() {
    return <Navbar color="faded" light expand="md">
        <NavbarBrand href="/pages/about">OSCARS</NavbarBrand>

        <Nav navbar>
            <NavLink href="/login">Login</NavLink>
        </Nav>
    </Navbar>;
}

function LoggedInNavBar() {
    return <Navbar color="faded" light expand="md">

        <NavbarBrand href="/pages/about">OSCARS</NavbarBrand>

        <Nav navbar>
            <NavLink
                href="/pages/list"
            >
                List
            </NavLink>
            <NavLink
                href="/pages/newDesign"
            >
                New
            </NavLink>
            <NavLink
                href="/pages/migrations"
            >
                Migrations
            </NavLink>
            <NavLink
                href="/pages/status"
            >
                Status
            </NavLink>
            <AdminNavBar/>
            <NavLink
                href="/pages/account"
            >
                My Account
            </NavLink>
            <Help/>
            <NavLink href="/pages/logout">Logout</NavLink>
        </Nav>
    </Navbar>;


}

function AdminNavBar() {
    const userCtx = useContext(UserContext);
    if (userCtx.isAdmin()) {
        return null;
    } else {
        return <UncontrolledDropdown>
            <DropdownToggle nav caret>
                Admin
            </DropdownToggle>
            <DropdownMenu>
                <NavLink href="/pages/admin/users">Users</NavLink>
                <NavLink href="/pages/admin/tags">Tags</NavLink>
            </DropdownMenu>
        </UncontrolledDropdown>;
    }

}

function Help() {
    return <UncontrolledDropdown>
        <DropdownToggle nav caret>
            Help
        </DropdownToggle>
        <DropdownMenu>
            <NavLink
                href="//github.com/esnet/oscars/issues/new"
                target="_blank"
                rel="noopener noreferrer"
            >
                Report an issue
            </NavLink>
        </DropdownMenu>
    </UncontrolledDropdown>;
}

export default withRouter(OscarsNavBar);
