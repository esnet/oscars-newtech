@wip
@unit
Feature: availability calculations

  I want to verify that I can apply reservations to baseline topology


  Scenario: Two routers loading
    Given I have initialized the world
    Given I clear the "devices" repository
    Given I import devices from "config/test/topo/two_routers.json"
    Given I import adjacencies from "config/test/topo/adj_a_b_mpls.json"
    Then the "devices" repository has 2 entries
    Then the "adjacencies" repository has 2 entries
    Given I set these "INGRESS" bandwidth reservations
#     | urn | bw | beg | end |
      | A:1 | 10 | 100 | 250 |
      | A:2 | 10 | 100 | 250 |
      | B:2 | 10 | 100 | 250 |
      | B:1 | 10 | 100 | 250 |
    Given I set these eternal vlan reservations
      | A:1 | 101 |
      | B:1 | 101 |
    Then the "INGRESS" bw availability map between 0 and 80 is
#     | urn | in  |
      | A:1 | 100 |
      | B:1 | 100 |
      | A:2 | 100 |
      | B:2 | 100 |
    Then the "INGRESS" bw availability map between 70 and 300 is
#     | urn | in  |
      | A:1 | 90  |
      | B:1 | 90  |
      | A:2 | 90  |
      | B:2 | 90  |
    Then the vlan availability map between 0 and 5 is
#     | urn | bw          |
      | A:1 | 100,102:200 |
      | B:1 | 100,102:200 |
