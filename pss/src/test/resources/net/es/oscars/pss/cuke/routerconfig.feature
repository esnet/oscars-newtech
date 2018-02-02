@routerconfig @live
Feature: Control plane checking

  I want to verify I can perform router configuration

  Scenario: Perform router config
    Given I have initialized the world
    Given I have warned the user this is a live test

    Given I create profile "netlab"
    Given I configure a match for urn "netlab-mx960-rt1" to profile "netlab"
    When I set the mapping method to "MATCH" on profile "netlab"
    Given I added a mapping from "netlab-mx960-rt1" to "netlab-mx960-rt1-es1.es.net" on profile "netlab"
    Then I set rancid perform to true on profile "netlab"
    Then I set rancid host to "netlab-noc.es.net" on profile "netlab"
    Then I set rancid dir to "/home/rancid/bin" on profile "netlab"
    Then I set rancid username to "oscars" on profile "netlab"
    Then I set rancid cloginrc to "/home/oscars/oscars-credentials/cloginrc" on profile "netlab"

    Given I create profile "testbed"
    Given I configure a match for urn "nersc-tb1" to profile "testbed"
    Given I configure a match for urn "star-tb1" to profile "testbed"
    When I set the mapping method to "IDENTITY" on profile "testbed"
    Then I set rancid perform to true on profile "testbed"
    Then I set rancid host to "oscars-west.es.net" on profile "testbed"
    Then I set rancid dir to "/opt/rancid" on profile "testbed"
    Then I set rancid cloginrc to "/home/haniotak/.cloginrc" on profile "testbed"


    Then I set the test specification directory to "config/test/netlab"
    Then I will add the "build-mx-single.json" test spec
    Then I will generate and run the "BUILD" commands from test specs

    Then I will clear all the loaded test specs
    Then I will add the "dismantle-mx-single.json" test spec
    Then I will generate and run the "DISMANTLE" commands from test specs


    Then I will add the "build-alu-single.json" test spec
    Then I will generate and run the "BUILD" commands from test specs

    Then I will clear all the loaded test specs
    Then I will add the "dismantle-alu-single.json" test spec
    Then I will generate and run the "DISMANTLE" commands from test specs


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
