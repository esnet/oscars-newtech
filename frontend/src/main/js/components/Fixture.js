import React from 'react'
import PropTypes from 'prop-types'

const Fixture = ({ onClick, urn }) => (
    <li
        onClick={onClick}
    >
        {urn}
    </li>
)

Fixture.propTypes = {
    onClick: PropTypes.func.isRequired,
    urn: PropTypes.string.isRequired
}

export default Fixture