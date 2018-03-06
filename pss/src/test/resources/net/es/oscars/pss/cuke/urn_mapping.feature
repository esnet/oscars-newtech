@controlplane @unit
Feature: Control plane URN to dns / ip mapping feature

  I want to verify I can map device URNs to IP addresses / DNS names.

  The mapping can be configured (through a JSON file), or can be set to use
  DNS with a configured suffix to be appended to the device URN.

  Scenario: Perform identity mapping
    Given I have initialized the world
    Given I clear all profiles
    Given I create profile "default"
    Given I have cleared all mappings on profile "default"
    When I set the mapping method to "IDENTITY" on profile "default"
    When I ask for the router address of "foobar" on profile "default"
    Then the router address of "foobar" is "foobar" on profile "default"
    Then I did not receive an exception

  Scenario: Perform suffix append
    Given I have initialized the world
    Given I clear all profiles
    Given I create profile "default"
    Given I have cleared all mappings on profile "default"
    When I set the mapping method to "APPEND_SUFFIX" on profile "default"
    When I set the suffix ".foo.net" on profile "default"
    When I ask for the router address of "foobar" on profile "default"
    Then the router address of "foobar" is "foobar.foo.net" on profile "default"
    Then I did not receive an exception

  Scenario: Perform match mapping
    Given I have initialized the world
    Given I clear all profiles
    Given I create profile "default"
    Given I have cleared all mappings on profile "default"
    When I set the mapping method to "MATCH" on profile "default"
    Given I added a mapping from "foobar" to "10.0.0.1" on profile "default"
    When I ask for the router address of "foobar" on profile "default"
    Then the router address of "foobar" is "10.0.0.1" on profile "default"
    Then I did not receive an exception

  Scenario: missing match scenario
    Given I have initialized the world
    Given I clear all profiles
    Given I create profile "default"
    Given I have cleared all mappings on profile "default"
    When I set the mapping method to "MATCH" on profile "default"
    Given I added a mapping from "fooobar" to "10.0.0.1" on profile "default"
    Given The world is expecting an exception
    When I ask for the router address of "foobar" on profile "default"
    Then I did receive an exception
