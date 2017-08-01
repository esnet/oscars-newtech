@unit
Feature: topology deserialization

  I want to verify that i can load and persist topology

  Scenario: One node loading
    Given I have initialized the world
    Given I clear the "devices" repository
    Then the "adjacencies" repository has 0 entries
    Given I import devices from "config/test/topo/one.json"
    Then the "devices" repository has 1 entries
    Then the "ports" repository has 2 entries
    Given I import adjacencies from "config/test/topo/adj_none.json"
    Then the "adjacencies" repository has 0 entries

    Then I did not receive an exception


  Scenario: Two routers loading
    Given I have initialized the world
    Given I clear the "devices" repository
    Then the "adjacencies" repository has 0 entries
    Given I import devices from "config/test/topo/two_routers.json"
    Then the "devices" repository has 2 entries
    Then the "ports" repository has 4 entries
    Given I import adjacencies from "config/test/topo/adj_a_b_mpls.json"
    Then the "adjacencies" repository has 2 entries

    Then I did not receive an exception


  Scenario: Router and switch loading
    Given I have initialized the world
    Given I clear the "devices" repository
    Then the "adjacencies" repository has 0 entries
    Given I import devices from "config/test/topo/router_and_switch.json"
    Then the "devices" repository has 2 entries
    Then the "ports" repository has 4 entries
    Given I import adjacencies from "config/test/topo/adj_a_b_eth.json"
    Then the "adjacencies" repository has 2 entries

    Then I did not receive an exception

