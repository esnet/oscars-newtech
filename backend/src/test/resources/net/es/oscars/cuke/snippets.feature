@wip
@unit
Feature: snippets and stuff

  I want to verify that my business logic for snippets works

  Scenario: test with empty component and add a VPLS junction to it
    Given I load components from "config/test/resvs/for_test0.json"
    Then I generate a delta for adding a junction with refId "/xyzzy/j/A" and connectionId "xyzzy"
    Then based on the delta I generate a list of modifications
    Then I commit those modifications

  Scenario: test with only 1 junction
    Given I load components from "config/test/resvs/for_test1.json"
    Then I generate a delta for building the new components
    Then based on the delta I generate a list of modifications
    Then I commit those modifications

  Scenario: test with adding a fixture to the junction
    Given I load components from "config/test/resvs/for_test2.json"
    Given I load the config state for "xyzzy" from "config/test/configstate/onejunction.json"
    Then I generate a delta for adding a fixture with connectionId "xyzzy" on junction "/xyzzy/j/A"
    Then based on the delta I generate a list of modifications
    Then I commit those modifications

#  Scenario: test with adding a junction and a fixture
#    Given I load components from "config/test/resvs/for_test0.json"
#    Then I generate a delta for adding a junction with refId "/xyzzy/j/A" and connectionId "xyzzy"
#    Then I generate a delta for adding a fixture with connectionId "xyzzy" on junction "/xyzzy/j/A"
#    Then based on the delta I generate a list of modifications
#    Then I commit those modifications

#  Scenario: test with adding an ip address to an existing set of components
#    Given I load components from "config/test/resvs/for_test3.json"
#    Then I generate a delta for setting ip address "10.0.0.1/24" on junction "/xyzzy/j/A"
#    Then based on the delta I generate a list of modifications
#    Then I commit those modifications