import React, { Component } from "react";

import { observer, inject } from "mobx-react";
import { action } from "mobx";

import { Label, FormGroup, Input } from "reactstrap";
// import { WithContext as ReactTags } from 'react-tag-input';

import myClient from "../../agents/client";
import validator from "../../lib/validation";

@inject("controlsStore")
@observer
class TagControls extends Component {
    componentWillMount() {
        myClient.submitWithToken("GET", "/api/tag/categories/config").then(
            action(response => {
                let categories = JSON.parse(response);
                let tags = this.setDefaultValues(categories);
                let params = {
                    categories: categories,
                    tags: tags
                };
                this.props.controlsStore.setParamsForConnection(params);
            })
        );
    }

    handleAddition = (e, category) => {
        let value = [e.text];
        this.props.controlsStore.setCategory(category, value, "TEXT");
    };

    handleDelete = (i, category) => {
        const tags = this.props.controlsStore.getTags(category);
        const filteredTags = tags.filter((tag, index) => index !== i);

        let value = [];
        for (let i = 0, l = filteredTags.length; i < l; i++) {
            value.push(filteredTags[i].text);
        }

        this.props.controlsStore.setCategory(category, value);
    };

    onCategoryChange = (e, category, multivalue) => {
        let options = e.target.options;
        let value = [];

        if (options === undefined) {
            value = e.target.value;
        } else {
            if (!multivalue) {
                for (let i = 0, l = options.length; i < l; i++) {
                    if (options[i].selected) {
                        value = options[i].value;
                    }
                }
            } else {
                for (let i = 0, l = options.length; i < l; i++) {
                    if (options[i].selected) {
                        value.push(options[i].value);
                    }
                }
            }
        }
        this.props.controlsStore.setCategory(category, value);

        // TO DO : Hack
        this.forceUpdate();
    };

    // Set default values only once
    setDefaultValues(categories) {
        let tags = [];
        for (let key in categories) {
            let { category, input, mandatory, options, multivalue } = categories[key];
            if (input === "SELECT") {
                if (mandatory) {
                    if (multivalue) {
                        categories[key].selected = [options[0]];
                    } else {
                        categories[key].selected = options[0];
                    }
                    tags.push({
                        category: category,
                        contents: options[0]
                    });
                } else {
                    if (!multivalue) {
                        if (!options.includes("-")) {
                            options.unshift("-");
                        }
                        categories[key].selected = "";
                    } else {
                        categories[key].selected = [];
                    }
                }
            } else if (input === "TEXT") {
                categories[key].selected = "";
            }
        }
        return tags;
    }

    render() {
        const conn = this.props.controlsStore.connection;

        let categories = conn.categories;

        let inputs = [];

        for (let key in categories) {
            let {
                category,
                description,
                input,
                mandatory,
                multivalue,
                options,
                selected
            } = categories[key];

            if (input === "SELECT") {
                let selectOptions = [];

                // Generate list of options
                for (let i in options) {
                    let option = (
                        <option key={i} value={options[i]}>
                            {options[i]}
                        </option>
                    );
                    if (options[i] === "-") {
                        option = (
                            <option key={i} value="">
                                {options[i]}
                            </option>
                        );
                    }
                    selectOptions.push(option);
                }

                // Create the input field
                let inputTag = (
                    <FormGroup key={category}>
                        <Label>{description}</Label>
                        <Input
                            type="select"
                            name={category}
                            id={category}
                            multiple={multivalue}
                            valid={
                                validator.tagsControl(conn.categories, category, mandatory) ===
                                "success"
                            }
                            invalid={
                                validator.tagsControl(conn.categories, category, mandatory) !==
                                "success"
                            }
                            value={selected}
                            onChange={e => this.onCategoryChange(e, category, multivalue)}
                        >
                            {selectOptions}
                        </Input>
                    </FormGroup>
                );

                inputs.push(inputTag);
            } else if (input === "TEXT") {
                if (!multivalue) {

                    // Create the input field
                    let inputTag = (
                        <FormGroup key={category}>
                            <Label>{description}</Label>
                            <Input
                                type="text"
                                placeholder={"Enter " + category}
                                name={category}
                                id={category}
                                valid={
                                    validator.tagsControl(conn.categories, category, mandatory) ===
                                    "success"
                                }
                                invalid={
                                    validator.tagsControl(conn.categories, category, mandatory) !==
                                    "success"
                                }
                                onChange={e => this.onCategoryChange(e, category)}
                            />
                        </FormGroup>
                    );

                    inputs.push(inputTag);

                } else {

                    /*
                    // For multivalue text inputs
                    const KeyCodes = {
                        comma: 188,
                        enter: 13,
                    };

                    const delimiters = [KeyCodes.comma, KeyCodes.enter];

                    // Get Tags for a particular category
                    let tags = this.props.controlsStore.getTags(category);
                    let reactTags = <ReactTags
                        tags={tags}
                        placeholder={"Enter value(s) separated by a comma"}
                        handleDelete={e => this.handleDelete(e, category)}
                        handleAddition={e => this.handleAddition(e, category)}
                        allowDragDrop={false}
                        delimiters={delimiters}
                        classNames={{
                            tagInputField: (validator.tagsControl(conn.categories, category, mandatory) === "success") ? 'form-control is-valid' : 'form-control is-invalid',
                            tag: 'btn btn-primary',
                        }}
                    />;

                    let inputTag = (
                        <FormGroup key={category}>
                            <Label>{description}</Label>
                            {reactTags}
                        </FormGroup>
                    );



                    // inputs.push(inputTag);
                    */

                }
            }
        }
        return <div>{inputs}</div>;
    }
}

export default TagControls;
