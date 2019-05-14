import React, { Component } from "react";

import { observer, inject } from "mobx-react";
import { action, toJS } from "mobx";

import { Label, FormGroup, Input } from "reactstrap";

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
                // TODO : Can't do multivalue in text - does that mean text area?

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
            }
        }

        return inputs;
    }
}

export default TagControls;
