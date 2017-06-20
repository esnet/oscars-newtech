@routerconfig @live
Feature: Control plane checking

  I want to verify I can perform router configuration

  Scenario: Perform router config for a single ALU router
    Given I have initialized the world
    Given I have warned the user this is a live test
    Then I set the rancid execute property to true
    Then I set the rancid proxy to "netlab-noc.es.net"
    Then I set the test specification directory to "config/test/netlab"
    Then I will add the "build-alu-single.json" test spec
    Then I will generate and run the "BUILD" commands from test specs
    Then I will clear all the loaded test specs
    Then I will add the "dismantle-alu-single.json" test spec
    Then I will generate and run the "DISMANTLE" commands from test specs
    Then I did not receive an exception

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


  Scenario: Perform router config for two routers
    Given I have initialized the world
    Given I have warned the user this is a live test
    Then I set the rancid execute property to true
    Then I set the rancid proxy to "noc-west.es.net"
    Then I set the test specification directory to "config/test/testbed"
    Then I will clear all the loaded test specs
    Then I will add the "build-alu_alu_a_z.json" test spec
    Then I will add the "build-alu_alu_z_a.json" test spec
    Then I will generate and run the "BUILD" commands from test specs
    Then I will clear all the loaded test specs
    Then I will add the "dismantle-alu_alu_a_z.json" test spec
    Then I will add the "dismantle-alu_alu_z_a.json" test spec
    Then I will generate and run the "DISMANTLE" commands from test specs
    Then I did not receive an exception
