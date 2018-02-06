@wip @live
Feature: Retrieve & verify config
    I want to make sure I can collect configuration for parsing and verification

  Scenario: Retrieve Juniper config
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

    When I retrieve config for "netlab-mx960-rt1" model "JUNIPER_MX"
    Then I verify that "$['chassis']" must be "PRESENT"
    Then I verify that "$['foobar']" must be "ABSENT"

    Then I verify that "$.interfaces.interface[?(@.name=='lo0')].name" must contain "lo0"
    Then I verify that "$.interfaces.interface[?(@.name=='lo0')].unit[?(@.name == '0')].name" must contain "0"
    Then I verify that "$.interfaces.interface[?(@.name=='lo0')].unit.family.inet.address[?(@.name == '127.0.0.1/32')].name" must contain "127.0.0.1/32"

    Then I did not receive an exception