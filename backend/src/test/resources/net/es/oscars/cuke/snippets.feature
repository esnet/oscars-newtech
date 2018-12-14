@wip
@unit
Feature: snippets and stuff

  I want to verify that my business logic for snippets works

  Scenario: hello snippets
    Given I load design from "config/test/resvs/for_snippets.json"
    Then I load the component
    Then I generate a delta for setting ip address "10.0.0.1/24" on junction "/xyzzy/j/A"
    Then I generate snippets for the previous delta
    Then the latest set of generated snippets has 3 members
    Then I didn't receive an exception

