import React, {Component} from "react";
import {Row, Col, Card, CardHeader, CardBody} from "reactstrap";

import {QueryClient, QueryClientProvider} from "react-query";
import useBackendVersion from "../hooks/useVersion";

function AboutApp() {
    const queryClient = new QueryClient()
    return (
        // Provide the client to your App
        <QueryClientProvider client={queryClient}>
            <AboutVersion/>
        </QueryClientProvider>
    )
}

function AboutVersion() {
    const {isLoading, isError, data, error} = useBackendVersion();

    console.log(data);

    if (isLoading) {
        return <span>Loading...</span>
    }

    if (isError) {
        return <span>Error: {error.message}</span>
    }

    return <Row>
        <Col md={{size: 10, offset: 1}}>
            <Card>
                <CardHeader>About OSCARS</CardHeader>
                <CardBody>
                    <div>
                        Frontend version: <u>{__VERSION__}</u>
                    </div>
                    <div>
                        Backend version: <u>{data}</u>
                    </div>
                </CardBody>
            </Card>
        </Col>
    </Row>


}

export default AboutApp;
