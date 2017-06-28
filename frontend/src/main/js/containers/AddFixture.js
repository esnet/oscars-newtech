import React from 'react'
import { connect } from 'react-redux'
import { addFixture } from '../actions'

let AddFixture = ({ dispatch }) => {
    let input;

    return (
        <div>
            <form
                onSubmit={e => {
                    e.preventDefault();
                    if (!input.value.trim()) {
                        return
                    }
                    dispatch(addFixture(input.value));
                    input.value = ''
                }}
            >
                <input
                    ref={node => {
                        input = node
                    }}
                />
                <button type="submit">
                    Add a fixture
                </button>
            </form>
        </div>
    )
}
AddFixture = connect()(AddFixture)

export default AddFixture