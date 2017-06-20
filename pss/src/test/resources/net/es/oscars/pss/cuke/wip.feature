@wip
Feature: Control plane checking

  I want to verify I can perform router configuration


  Scenario: Perform router config for a single MX router
    Given I have initialized the world
    Given I have warned the user this is a live test
    Then I set the rancid execute property to true
    Then I set the rancid proxy to "netlab-noc.es.net"
    Then I set the test specification directory to "config/test/netlab"
    Then I will add the "build-mx-single.json" test spec
    Then I will generate and run the "BUILD" commands from test specs
    Then I will clear all the loaded test specs
    Then I will add the "dismantle-mx-single.json" test spec
    Then I will generate and run the "DISMANTLE" commands from test specs
    Then I did not receive an exception
