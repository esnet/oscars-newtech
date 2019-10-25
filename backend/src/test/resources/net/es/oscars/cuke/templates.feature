@unit
@wip
Feature: template versioning

  I want to verify that my templates have versions, and they are consistent

  Scenario: Single template has a version tag, which gets stripped on output
    Then I "save" the template directory property
    Given I set the template directory to "config/test/templates/single"
    When I load the template "with_version.ftl"
    Then the template processed output does not contain the version tag
    Then the version tag for loaded template(s) "is" consistent
    Then I "restore" the template directory property

  Scenario: Single template does not have a version tag
    Then I "save" the template directory property
    Given I set the template directory to "config/test/templates/single"
    When I load the template "no_version.ftl"
    Then the version tag for loaded template(s) "is not" consistent
    Then I "restore" the template directory property

  Scenario: All templates have a version tag, and it is consistent
    Then I "save" the template directory property
    Given I set the template directory to "config/test/templates/same_version"
    When I load all templates in the template directory
    Then the version tag for loaded template(s) "is" consistent
    Then I "restore" the template directory property

  Scenario: Not all templates have a version tag
    Then I "save" the template directory property
    Given I set the template directory to "config/test/templates/partial_version"
    When I load all templates in the template directory
    Then the version tag for loaded template(s) "is not" consistent
    Then I "restore" the template directory property

  Scenario: Not all templates have the same version tag
    Then I "save" the template directory property
    Given I set the template directory to "config/test/templates/diff_version"
    When I load all templates in the template directory
    Then the version tag for loaded template(s) "is not" consistent
    Then I "restore" the template directory property

  Scenario: Ensure the production-grade templates have consistent versions
    Then I "save" the template directory property
    Given I set the template directory to "config/templates/mx"
    When I load all templates in the template directory
    Then the version tag for loaded template(s) "is" consistent
    Given I set the template directory to "config/templates/ex"
    When I load all templates in the template directory
    Then the version tag for loaded template(s) "is" consistent
    Given I set the template directory to "config/templates/alu"
    When I load all templates in the template directory
    Then the version tag for loaded template(s) "is" consistent
    Then I "restore" the template directory property

