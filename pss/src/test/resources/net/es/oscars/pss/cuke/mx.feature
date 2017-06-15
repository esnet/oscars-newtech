@mx @unit
Feature: MX command generation

  I want to verify I can generate commands for Juniper MX

  Scenario: Generate single-device MX "BUILD" commands
    Given I have initialized the world
    Given I have loaded the "BUILD" test commands
    Given I choose the commands matching device model "JUNIPER_MX"
    Given I choose the commands that should "SUCCEED"
    Then the command list is not empty
    When I "BUILD" on the "JUNIPER_MX" command generator with the test commands
    Then I did not receive an exception

  Scenario: Generate single-device MX "DISMANTLE" commands
    Given I have initialized the world
    Given I have loaded the "DISMANTLE" test commands
    Given I choose the commands matching device model "JUNIPER_MX"
    Given I choose the commands that should "SUCCEED"
    Then the command list is not empty
    When I "BUILD" on the "JUNIPER_MX" command generator with the test commands
    Then I did not receive an exception
