@unit
@wip
Feature: template versioning

  I want to verify that my templates have versions, and they are consistent

  Scenario: Single template has a version tag, which gets stripped on output
    Given I have initialized the world
    Given I set the template directory to "config/test/templates/single"
    When I load the template "with_version.ftl"
    Then the template processed output does not contain the version tag
    Then the version tag for loaded template(s) "is" consistent

  Scenario: Single template does not have a version tag
    Given I have initialized the world
    Given I set the template directory to "config/test/templates/single"
    When I load the template "no_version.ftl"
    Then the version tag for loaded template(s) "is not" consistent

  Scenario: All templates have a version tag, and it is consistent
    Given I have initialized the world
    Given I set the template directory to "config/test/templates/same_version"
    When I load all templates in the template directory
    Then the version tag for loaded template(s) "is" consistent

  Scenario: Not all templates have a version tag
    Given I set the template directory to "config/test/templates/partial_version"
    When I load all templates in the template directory
    Then the version tag for loaded template(s) "is not" consistent

  Scenario: Not all templates have the same version tag
    Given I set the template directory to "config/test/templates/diff_version"
    When I load all templates in the template directory
    Then the version tag for loaded template(s) "is not" consistent


