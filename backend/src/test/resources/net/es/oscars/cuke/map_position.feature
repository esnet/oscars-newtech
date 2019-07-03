@wip @live
Feature: map positions

  I want to verify that I pass the correct map coordinates

  Scenario: All positions are different
    Given I load my positionMap from "config/test/map_positions/one.json"
    Given I add nodes for devices "bois-cr1", "pnwg-cr5" and "kans-cr5"
    Then I can verify my results
    Then I did not receive an exception

  Scenario: Multiple positions with same keys
    Given I load my positionMap from "config/test/map_positions/two.json"
    Given I add nodes for devices "bois-cr1", "pnwg-cr5" and "pnwg-cr4"
    Then I can verify my results
    Then I did not receive an exception

  Scenario: When one device matches multiple keys
    Given I load my positionMap from "config/test/map_positions/three.json"
    Given I add nodes for devices "bois-cr1", "llnl-mr2" and "llnldc-rt5"
    Then I can verify my results
    Then I did not receive an exception
