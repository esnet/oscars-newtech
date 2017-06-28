const fixtures = (state = [], action) => {
    switch (action.type) {
        case 'ADD_FIXTURE':
            return [
                ...state,
                {
                    urn: action.payload.urn
                }
            ]
        case 'REMOVE_FIXTURE':
            return state.filter((item) => item.urn !== action.payload.urn)
        default:
            return state
    }
}

export default fixtures