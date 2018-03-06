@controlplane @live
Feature: Control plane checking

  I want to verify I can perform the control plane checking

  Scenario: Perform control plane checking
    Given I have initialized the world
    Given I have warned the user this is a live test
    Given I clear all profiles
    Given I create profile "netlab"
    When I set the mapping method to "MATCH" on profile "netlab"
    Given I added a mapping from "dev-7750sr12-rt1" to "dev-7750sr12-rt1-es1.es.net" on profile "netlab"
    Then I set rancid perform to true on profile "netlab"
    Then I set rancid host to "netlab-noc.es.net" on profile "netlab"
    Then I set rancid dir to "/home/rancid/bin" on profile "netlab"
    Then I set rancid username to "oscars" on profile "netlab"
    Then I set rancid cloginrc to "/home/oscars/oscars-credentials/cloginrc" on profile "netlab"

    Then I will enqueue a control plane check for device "dev-7750sr12-rt1" model "ALCATEL_SR7750" profile "netlab"
    And I will wait up to 60000 ms for the commands to complete
    Then I have verified the control plane to all the devices
    Then I did not receive an exception
