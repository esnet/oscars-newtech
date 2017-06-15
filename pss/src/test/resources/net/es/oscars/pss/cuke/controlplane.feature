@controlplane @live
Feature: Control plane checking

  I want to verify I can perform the control plane checking

  Scenario: Perform control plane checking
    Given I have initialized the world
    Given I have warned the user this is a live test
    Then I set the rancid execute property to true
    Then I set the rancid proxy to "netlab-noc.es.net"
    Then I will add the control plane test commands for "netlab" to the queue
    And I will wait up to 60000 ms for the control plane commands to complete
    Then I have verified the control plane to all the devices
    Then I did not receive an exception
