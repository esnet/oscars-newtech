import React, { Component } from "react";
import { inject, observer } from "mobx-react";
import Collapsible from 'react-collapsible';
import { Card, CardBody, CardHeader } from "reactstrap";
import topologyStore from "../../stores/topologyStore";
import { toJS } from "mobx";
import Moment from "moment";

const format = "Y/MM/DD HH:mm:ss";

@inject("topologyStore")
@observer
class TopoStatus extends Component {
    constructor(props) {
        super(props);
    }

    componentDidMount() {
        this.props.topologyStore.loadReport();
    }

    renderItem = ({ item, collapseIcon, handler }) => {
        return (
            <div>
                {handler}
                {collapseIcon}
                {item.text}
            </div>
        );
    };

    render() {
        const report = toJS(this.props.topologyStore.report);
        const when = Moment(report.generated * 1000);
        const humanWhen = when.format(format) + " (" + when.fromNow() + ")";

        let items = [];

        let anyIssues = false;
        let byConnId = { id: "by connId", text: "By connectionId", children: [] };
        if ("issuesByConnectionId" in report) {
            for (let [connId, issues] of Object.entries(report.issuesByConnectionId)) {
                let connEntry = { id: connId, text: connId, children: [] };
                let i = 0;
                for (let issue of issues) {
                    anyIssues = true;
                    let issueEntry = { id: connId + ":" + i, text: issue };
                    connEntry.children.push(issueEntry);
                    i++;
                }
                byConnId.children.push(connEntry);
            }
        }

        let byUrn = { id: "by urn", text: "By Urn", children: [] };
        if ("issuesByUrn" in report) {
            for (let [urn, issues] of Object.entries(report.issuesByUrn)) {
                let urnEntry = { id: urn, text: urn, children: [] };
                let i = 0;
                for (let issue of issues) {
                    anyIssues = true;
                    let issueEntry = { id: urn + ":" + i, text: issue };
                    urnEntry.children.push(issueEntry);
                    i++;
                }
                byUrn.children.push(urnEntry);
            }
        }
        let issuesView = <span>No issues</span>;

        if (anyIssues) {
            items.push(byConnId);
            items.push(byUrn);
            issuesView = <Collapsible items={items} renderItem={this.renderItem} />;
        }

        return (
            <Card>
                <CardHeader className="p-1">Topology report ({humanWhen})</CardHeader>
                <CardBody>{issuesView}</CardBody>
            </Card>
        );
    }
}

export default TopoStatus;
