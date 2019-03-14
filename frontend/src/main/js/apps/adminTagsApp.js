import React, { Component } from "react";
import {
    Row,
    Col,
    Card,
    CardHeader,
    CardBody,
    ListGroup,
    ListGroupItem,
    Button,
    Label,
    Input,
    Form,
    FormGroup
} from "reactstrap";
import myClient from "../agents/client";
import { observer, inject } from "mobx-react";
import { action } from "mobx";
import Octicon from "react-octicon";
import ConfirmModal from "../components/confirmModal";
import { size } from "lodash-es";

@inject("tagStore")
@observer
class AdminTagsApp extends Component {
    constructor(props) {
        super(props);
    }

    componentWillMount() {
        this.refreshTagCategories();
    }

    componentWillUnmount() {
        clearTimeout(this.tagUpdateTimeout);
    }

    refreshTagCategories = () => {
        myClient.submitWithToken("GET", "/protected/tag/categories").then(
            action(response => {
                let parsed = JSON.parse(response);
                this.props.tagStore.updateCategories(parsed);
            })
        );
        this.tagUpdateTimeout = setTimeout(this.refreshTagCategories, 5000);
    };

    delete = id => {
        myClient.submitWithToken("GET", "/protected/tag/categories/delete/" + id).then(
            action(() => {
                this.refreshTagCategories();
            })
        );
    };

    add = () => {
        const edit = this.props.tagStore.editCtg;
        let ctg = {
            id: null,
            category: edit.category,
            source: edit.source
        };
        myClient.submitWithToken("POST", "/protected/tag/categories/update", ctg).then(
            action(() => {
                this.refreshTagCategories();
            })
        );
    };

    onCategoryChange = val => {
        this.props.tagStore.setEditedCtg(val, null);
    };

    onSourceChange = val => {
        this.props.tagStore.setEditedSource(val, null);
    };

    // key presses
    handleKeyPress = e => {
        const edit = this.props.tagStore.editCtg;
        if (e.key === "Enter" && size(edit.category) > 0) {
            this.add();
        }
    };

    render() {
        const categories = this.props.tagStore.store.categories;
        const edit = this.props.tagStore.editCtg;

        return (
            <Row>
                <Col xs={{ size: 8, offset: 2 }} md={{ size: 8, offset: 2 }}>
                    <Card>
                        <CardHeader>Tag Administration</CardHeader>
                        <CardBody>
                            <b>Categories</b>
                            <ListGroup>
                                {categories.map(c => {
                                    let source = c.source;
                                    if (c.source === null) {
                                        c.source = "";
                                    }
                                    if (c.category === null || c.category === "") {
                                        return null;
                                    }
                                    if (c.source.length > 40) {
                                        source = c.source.substr(0, 40) + "...";
                                    }
                                    return (
                                        <ListGroupItem className="p-1 m-1" key={c.id}>
                                            {c.category}{" "}
                                            <span className="pull-right">{source}</span>
                                            <ConfirmModal
                                                body="This will delete the tag category. It will not delete tags on connections"
                                                header="Delete category"
                                                uiElement={
                                                    <Octicon
                                                        name="trashcan"
                                                        style={{ height: "16px", width: "16px" }}
                                                    />
                                                }
                                                onConfirm={() => this.delete(c.id)}
                                            />
                                        </ListGroupItem>
                                    );
                                })}
                            </ListGroup>

                            <Form
                                inline
                                onSubmit={e => {
                                    e.preventDefault();
                                }}
                            >
                                <FormGroup className="mb-2 mr-sm-2 mb-sm-0">
                                    <Label for="category" hidden>
                                        Category:{" "}
                                    </Label>{" "}
                                    <Input
                                        type="text"
                                        id="category"
                                        placeholder="category"
                                        onKeyPress={this.handleKeyPress}
                                        onChange={e => this.onCategoryChange(e.target.value)}
                                    />
                                </FormGroup>
                                <FormGroup className="mb-2 mr-sm-2 mb-sm-0">
                                    <Label for="category" hidden>
                                        Source:{" "}
                                    </Label>{" "}
                                    <Input
                                        type="text"
                                        id="Source"
                                        placeholder="source"
                                        onKeyPress={this.handleKeyPress}
                                        onChange={e => this.onSourceChange(e.target.value)}
                                    />
                                </FormGroup>{" "}
                                <Button
                                    className="float-right"
                                    disabled={!size(edit.category)}
                                    onClick={this.add}
                                >
                                    Add
                                </Button>
                            </Form>
                        </CardBody>
                    </Card>
                </Col>
            </Row>
        );
    }
}

export default AdminTagsApp;
