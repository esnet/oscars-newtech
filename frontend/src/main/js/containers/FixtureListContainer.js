import { connect } from 'react-redux'
import FixtureList from '../components/FixtureList'
import { removeFixture } from '../actions'

const getFixtures = (fixtures) => {
    return fixtures
}

const mapStateToProps = state => {
    return {
        fixtures: getFixtures(state.fixtures)
    }
}

const mapDispatchToProps = dispatch => {
    return {
        onFixtureClick: id => {
            dispatch(removeFixture(id))
        }
    }
}

const FixtureListContainer = connect(
    mapStateToProps,
    mapDispatchToProps
) (FixtureList)

export default FixtureListContainer