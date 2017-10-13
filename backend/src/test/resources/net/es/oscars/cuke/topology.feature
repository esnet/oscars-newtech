@unit
Feature: topology deserialization

  I want to verify that i can load and persist topology

  Scenario: One node loading
    Given I have initialized the world
    Given I clear the topology
    Then the "adjacencies" repository has 0 entries
    Then the "devices" repository has 0 entries
    Then the "ports" repository has 0 entries
    Then the current topology is empty
    Given I load topology from "config/test/topo/one.json" and "config/test/topo/adj_none.json"
    When I merge the new topology
    Then the "devices" repository has 1 entries
    Then the "ports" repository has 2 entries
    Then the "adjacencies" repository has 0 entries
    Then I did not receive an exception


  Scenario: Two routers loading
    Given I have initialized the world
    Given I clear the topology
    Given I load topology from "config/test/topo/two_routers.json" and "config/test/topo/adj_a_b_mpls.json"
    When I merge the new topology
    Then the "devices" repository has 2 entries
    Then the "ports" repository has 4 entries
    Then the "adjacencies" repository has 2 entries
    Then I did not receive an exception


  Scenario: Router and switch loading
    Given I have initialized the world
    Given I clear the topology
    Given I load topology from "config/test/topo/router_and_switch.json" and "config/test/topo/adj_a_b_eth.json"
    When I merge the new topology
    Then the "devices" repository has 2 entries
    Then the "ports" repository has 4 entries
    Then the "adjacencies" repository has 2 entries
    Then I did not receive an exception



  Scenario: topology deltas: adding
    Given I have initialized the world
    Given I clear the topology
    Given I load topology from "config/test/topo/one.json" and "config/test/topo/adj_none.json"
    Then the "device" delta has 1 entries "added"
    Then the "port" delta has 2 entries "added"
    Then the "adjcy" delta has 0 entries "added"
    When I merge the new topology
    Then the "devices" repository has 1 entries
    Then the "ports" repository has 2 entries
    Given I load topology from "config/test/topo/two_routers.json" and "config/test/topo/adj_a_b_mpls.json"
    Then the "device" delta has 1 entries "added"
    Then the "port" delta has 2 entries "added"
    Then the "port" delta has 1 entries "modified"
    Then the "adjcy" delta has 2 entries "added"
    When I merge the new topology
    Then the "devices" repository has 2 entries
    Then the "ports" repository has 4 entries
    Then the "adjacencies" repository has 2 entries
    Then I did not receive an exception


  Scenario: topology deltas: removing
    Given I have initialized the world
    Given I clear the topology
    Given I load topology from "config/test/topo/two_routers.json" and "config/test/topo/adj_a_b_mpls.json"
    When I merge the new topology
    Given I load topology from "config/test/topo/one.json" and "config/test/topo/adj_none.json"
    Then the "device" delta has 1 entries "removed"
    Then the "port" delta has 2 entries "removed"
    Then the "port" delta has 1 entries "modified"
    Then the "adjcy" delta has 2 entries "removed"
    When I merge the new topology
    Then I did not receive an exception
