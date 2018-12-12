@wip
@unit
Feature: snippets and stuff

  I want to verify that my snippets business logic works

  Scenario: hello snippets
    Given I have initialized the world
    # TODO sartaj: create that file :)
    Then load components from "config/test/resvs/to/components.json"
    Then I gen a delta for setting ip address "10.0.0.1/24" to "XYZZY" on junction "foo"
    Then I gen snippets for the previous delta
    Then the latest set of generated snippets has 3 members



    Then I did not receive an exception

