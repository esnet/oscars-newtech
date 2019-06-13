@ex @unit
Feature: EX command generation

  I want to verify I can generate commands for Juniper EX

  Scenario: Generate single-device EX "BUILD" commands
    Given I have initialized the world
    Then I set the test specification directory to "config/test/unit"
    Given I have loaded the "BUILD" test commands
    Given I choose the commands matching device model "JUNIPER_EX"
    Given I choose the commands that should "SUCCEED"
    Then the command list is not empty
    When I "BUILD" on the "JUNIPER_EX" command generator with the test commands
    Then I did not receive an exception

  Scenario: Generate single-device EX "DISMANTLE" commands
    Given I have initialized the world
    Then I set the test specification directory to "config/test/unit"
    Given I have loaded the "DISMANTLE" test commands
    Given I choose the commands matching device model "JUNIPER_EX"
    Given I choose the commands that should "SUCCEED"
    Then the command list is not empty
    When I "BUILD" on the "JUNIPER_EX" command generator with the test commands
    Then I did not receive an exception
