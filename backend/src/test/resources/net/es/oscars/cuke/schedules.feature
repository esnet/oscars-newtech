@unit
Feature: schedules logic

  I want to verify that my schedule business logic works

  Scenario: Basic schedule overlapping
    Given I have initialized the world

    Given I clear all schedules
    Given I add these schedules
      | A | 10 | 40 | 45 | HELD |
      | B | 50 | 90 | 95 | HELD |
      | C | 30 | 60 | 65 | RESERVED |
      | D | 40 | 80 | 85 | HELD |
    Then a schedule between 0 and 5 does not overlap
    Then a schedule between 100 and 200 does not overlap
    Then a schedule between 90 and 120 does overlap
      | B |
    Then a schedule between 0 and 40 does overlap
      | A |
      | C |
      | D |
    Then a schedule between 0 and 50 does overlap
      | A |
      | B |
      | C |
      | D |
    Then a schedule between 55 and 100 does overlap
      | B |
      | C |
      | D |
    Then a schedule between 70 and 100 does overlap
      | B |
      | D |
    Then a schedule between 0 and 25 does overlap
      | A |

    Then I did not receive an exception

