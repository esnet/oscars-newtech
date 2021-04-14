import React, { Component } from "react";
import { Popover, PopoverHeader, PopoverBody } from "reactstrap";
import PropTypes from "prop-types";
import Octicon from "react-octicon";

export default class HelpPopover extends Component {
    constructor(props) {
        super(props);
    }

    componentDidMount() {
        this.setState({
            showPopover: false
        });
    }

    toggle = () => {
        this.setState({
            showPopover: !this.state.showPopover
        });
    };

    render() {
        return (
            <span>
                <Octicon
                    name="info"
                    style={{ height: "18px", width: "18px", cursor: "pointer", margin: "2px" }}
                    onMouseOver={this.toggle}
                    onMouseOut={this.toggle}
                    id={this.props.popoverId}
                />
                <Popover
                    placement={this.props.placement}
                    isOpen={this.state.showPopover}
                    target={this.props.popoverId}
                    toggle={this.toggle}
                >
                    <PopoverHeader>{this.props.header}</PopoverHeader>
                    <PopoverBody>{this.props.body}</PopoverBody>
                </Popover>
            </span>
        );
    }
}

HelpPopover.propTypes = {
    popoverId: PropTypes.string.isRequired,
    header: PropTypes.element.isRequired,
    body: PropTypes.element.isRequired,
    placement: PropTypes.string.isRequired
};
