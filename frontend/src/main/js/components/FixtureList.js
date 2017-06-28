import React from 'react';
import PropTypes from 'prop-types';
import Fixture from './fixture';

const FixtureList = ({fixtures, onFixtureClick}) => (
    <div>
        <p>this is a list of fixtures</p>
        <ul>
            {fixtures.map(fixture => (
                <Fixture key={fixture.urn} {...fixture} onClick={() => onFixtureClick(fixture.urn)}/>
            ))}
        </ul>
    </div>
)

FixtureList.propTypes = {
    fixtures: PropTypes.arrayOf(
        PropTypes.string
    ).isRequired,
    onFixtureClick: PropTypes.func.isRequired
}

export default FixtureList