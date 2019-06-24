@routerconfig @live
Feature: Control plane checking

  I want to verify I can perform router configuration

  Scenario: Perform router config
    Given I have initialized the world
    Given I have warned the user this is a live test

    Given I create profile "netlab"
    When I set the mapping method to "MATCH" on profile "netlab"
    Given I added a mapping from "netlab-mx960-rt1" to "netlab-mx960-rt1-es1.es.net" on profile "netlab"
    Then I set rancid perform to true on profile "netlab"
    Then I set rancid host to "netlab-noc.es.net" on profile "netlab"
    Then I set rancid dir to "/home/rancid/bin" on profile "netlab"
    Then I set rancid username to "oscars" on profile "netlab"
    Then I set rancid cloginrc to "/home/oscars/oscars-credentials/cloginrc" on profile "netlab"

    Given I create profile "testbed"
    When I set the mapping method to "IDENTITY" on profile "testbed"
    Then I set rancid perform to true on profile "testbed"
    Then I set rancid host to "oscars-west.es.net" on profile "testbed"
    Then I set rancid dir to "/opt/rancid" on profile "testbed"
    Then I set rancid cloginrc to "/home/haniotak/.cloginrc" on profile "testbed"


    Then I did not receive an exception
