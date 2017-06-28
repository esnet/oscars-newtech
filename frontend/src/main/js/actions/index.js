export const addFixture = text => {
    return {
        type: 'ADD_FIXTURE',
        payload: {
            urn: text
        }
    }
}


export const removeFixture = text => {
    return {
        type: 'REMOVE_FIXTURE',
        payload: {
            urn: text
        }
    }
}