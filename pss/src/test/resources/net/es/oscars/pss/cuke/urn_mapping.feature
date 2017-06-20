@controlplane @unit
Feature: Control plane URN to dns / ip mapping feature

  I want to verify I can map device URNs to IP addresses / DNS names.

  The mapping can be configured (through a JSON file), or can be set to use
  DNS with a configured suffix to be appended to the device URN.

  Scenario: Check whether I can load mappings from a file
    Given I have initialized the world
    Given I have cleared all mappings
    When I try to load URN mappings from "config/test/unit/unit-addrs.json"
    Then I have loaded 4 URN mappings
    Then I did not receive an exception

  Scenario: Perform dns-based mapping to DNS names
    Given I have initialized the world
    Given I have cleared all mappings
    When I set the control plane addressing method to "URN_IS_HOSTNAME"
    And I set the DNS suffix to "foo.net"
    When I ask for the router address of "foobar"
    Then the router address of "foobar" is "foobar.foo.net"
    Then I did not receive an exception

  Scenario: Perform configured mapping to DNS names
    Given I have initialized the world
    Given I have cleared all mappings
    Given I added a configured mapping from "foobar" to "foobar.foo.net" "10.0.0.1"
    When I set the control plane addressing method to "DNS_FROM_CONFIG"
    When I ask for the router address of "foobar"
    Then the router address of "foobar" is "foobar.foo.net"
    Then I did not receive an exception

  Scenario: Perform configured mapping to IP addresses
    Given I have initialized the world
    Given I have cleared all mappings
    Given I added a configured mapping from "foobar" to "foobar.foo.net" "10.0.0.1"
    When I set the control plane addressing method to "IP_FROM_CONFIG"
    When I ask for the router address of "foobar"
    Then the router address of "foobar" is "10.0.0.1"
    Then I did not receive an exception

  Scenario: missing file scenario
    Given I have initialized the world
    Given The world is expecting an exception
    When I try to load URN mappings from "missingfile.file"
    Then I did receive an exception

  Scenario: missing mapping scenario
    Given I have initialized the world
    Given The world is expecting an exception
    Given I have cleared all mappings
    When I set the control plane addressing method to "IP_FROM_CONFIG"
    When I ask for the router address of "foobar"
    Then I did receive an exception
