import React, { Component } from "react";
import { observer, inject } from "mobx-react";
import { withRouter } from "react-router-dom";
import { Button, Card, CardBody, CardHeader, FormGroup, Label, Input } from "reactstrap";

@inject("connsStore", "commonStore")
@observer
class DetailsStatus extends Component {
    constructor(props) {
        super(props);
    }

    getMoreInfo = (e) => {
        if (e === "individual") {
            // Show panel with individual information
        } else if (e === "overall") {
            // Show panel with overall information
        }
    };

    render() {
        return (
            <Card>
                <CardHeader> Current Status </CardHeader>
                <CardBody>
                    <FormGroup>
                        <Input
                            type="textarea"
                        />
                    </FormGroup>

                    <Button
                        color="primary"
                        onClick={() => {
                            this.getMoreInfo("individual");
                        }}
                        className="float-left"
                    >
                        Individual Information
                    </Button>

                    <Button
                        color="primary"
                        onClick={() => {
                            this.getMoreInfo("overall");
                        }}
                        className="float-right"
                    >
                        Overall Information
                    </Button>
                </CardBody>
            </Card>
        );
    }
}

export default withRouter(DetailsStatus);