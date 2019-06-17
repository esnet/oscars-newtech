@unit @alu
Feature: Alcatel command generation

  I want to verify I can generate commands for ALU SR7750s

  Scenario: Generate single-device ALU "BUILD" commands
    Given I have initialized the world
    Then I set the test specification directory to "config/test/pss/unit"
    Given I have loaded the "BUILD" test commands
    Given I choose the commands matching device model "ALCATEL_SR7750"
    Given I choose the commands that should "SUCCEED"
    Then the command list is not empty
    When I "BUILD" on the "ALCATEL_SR7750" command generator with the test commands
    Then I did not receive an exception

  Scenario: Generate single-device ALU "DISMANTLE" commands
    Given I have initialized the world
    Then I set the test specification directory to "config/test/pss/unit"
    Given I have loaded the "DISMANTLE" test commands
    Given I choose the commands matching device model "ALCATEL_SR7750"
    Given I choose the commands that should "SUCCEED"
    Then the command list is not empty
    When I "DISMANTLE" on the "ALCATEL_SR7750" command generator with the test commands
    Then I did not receive an exception


  Scenario: Fail to generate ALU commands
    Given I have initialized the world
    Then I set the test specification directory to "config/test/pss/unit"
    Given I have loaded the "BUILD" test commands
    Given I choose the commands matching device model "ALCATEL_SR7750"
    Given I choose the commands that should "FAIL"
    Then the command list is not empty
    Given The world is expecting an exception
    When I "BUILD" on the "ALCATEL_SR7750" command generator with the test commands
    Then I did receive an exception
    And all the test commands generated an exception