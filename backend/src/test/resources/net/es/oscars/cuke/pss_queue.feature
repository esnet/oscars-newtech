@unit @wip
Feature: PSS task queueing

  I want to verify that my task queueing logic is correct

  Scenario: Add single task to task queue
    Given I have initialized the world
    Given I clear all sets
    When I add a "BUILD" task for "XYZZY" on "foo-cr5"
    Then the "waiting" set has 1 entries
    Then the "running" set has 0 entries
    Then the "done" set has 0 entries
    Then I did not receive an exception

  Scenario: Fully process single task
    Given I have initialized the world
    Given I clear all sets
    When I add a "BUILD" task for "XYZZY" on "foo-cr5"
    When I trigger the queue processor
    Then the "waiting" set has 0 entries
    Then the "running" set has 1 entries
    Then the "done" set has 0 entries
    When I make all running tasks complete
    Then the "waiting" set has 0 entries
    Then the "running" set has 0 entries
    Then the "done" set has 1 entries
    Then I did not receive an exception


  Scenario: Avoid double-inserting into waiting set
    Given I have initialized the world
    Given I clear all sets
    When I add a "BUILD" task for "XYZZY" on "foo-cr5"
    When I add a "BUILD" task for "XYZZY" on "foo-cr5"
    Then the "waiting" set has 1 entries
    Then the "running" set has 0 entries
    Then I did not receive an exception

  Scenario: Avoid inserting already running task into waiting set
    Given I have initialized the world
    Given I clear all sets
    When I add a "BUILD" task for "XYZZY" on "foo-cr5"
    Then the "waiting" set has 1 entries
    Then the "running" set has 0 entries
    When I trigger the queue processor
    Then the "waiting" set has 0 entries
    Then the "running" set has 1 entries
    When I add a "BUILD" task for "XYZZY" on "foo-cr5"
    When I trigger the queue processor
    Then the "waiting" set has 0 entries
    Then the "running" set has 1 entries
    Then I did not receive an exception

  Scenario: Anti-tasks
    Given I have initialized the world
    Given I clear all sets
    When I add a "BUILD" task for "XYZZY" on "foo-cr5"
    When I add a "DISMANTLE" task for "XYZZY" on "foo-cr5"
    Then the "waiting" set has 2 entries
    Then the "running" set has 0 entries
    Then the "done" set has 0 entries
    When I trigger the queue processor
    Then the "waiting" set has 0 entries
    Then the "running" set has 0 entries
    Then the "done" set has 0 entries
    Then I did not receive an exception



  Scenario: Parallel tasks on different devices
    Given I have initialized the world
    Given I clear all sets
    When I add a "BUILD" task for "XYZZY" on "foo-cr5"
    When I add a "BUILD" task for "XYZZY" on "bar-cr5"
    When I trigger the queue processor
    Then the "waiting" set has 0 entries
    Then the "running" set has 2 entries
    Then the "done" set has 0 entries
    When I make all running tasks complete
    Then the "waiting" set has 0 entries
    Then the "running" set has 0 entries
    Then the "done" set has 2 entries
    Then I did not receive an exception

  Scenario: Sequential tasks for same device
    Given I have initialized the world
    Given I clear all sets
    When I add a "BUILD" task for "XYZZY" on "foo-cr5"
    When I add a "BUILD" task for "ABCDE" on "foo-cr5"
    When I trigger the queue processor
    Then the "waiting" set has 1 entries
    Then the "running" set has 1 entries
    Then the "done" set has 0 entries
    When I make all running tasks complete
    Then the "waiting" set has 1 entries
    Then the "running" set has 0 entries
    Then the "done" set has 1 entries
    When I trigger the queue processor
    Then the "waiting" set has 0 entries
    Then the "running" set has 1 entries
    Then the "done" set has 1 entries
    When I make all running tasks complete
    Then the "waiting" set has 0 entries
    Then the "running" set has 0 entries
    Then the "done" set has 2 entries
    Then I did not receive an exception
