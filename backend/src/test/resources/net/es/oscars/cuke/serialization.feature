@wip
@unit
Feature: request deserialization

  I want to verify that i can serialize and persist connection requests

  Scenario: Basic connection
    Given I have initialized the world
    Given I can load my JSON-formatted request from "config/test/requests/simple.json"
    When I clear the request repository
    Then I can persist the request
    When I load the request from the repository
    Then the "schedule" repository has 1 entries
    Then the "fixture" repository has 2 entries
    Then the "junction" repository has 2 entries
    Then the "pipe" repository has 1 entries
    Then the "vlan" repository has 2 entries

    Then I did not receive an exception

