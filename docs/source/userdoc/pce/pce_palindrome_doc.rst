.. _pce_palindrome:

Palindromic PCE Module
======================

The Palindromic PCE finds a palindromic path through the network, such that a palindromic path exhibits a reverse-direction route which mirrors the forward-direction path. All devices, ports, and links traversed by the circuit in one direction **must** be incorporated in exactly reverse order in the other direction. This is similar to palindromes, which are words that read the same in forward and reverse, such as "level", or "racecar", or more patriotically "a man a plan a canal Panama." The reverse-direction path may only be necessary for control signals, and the user may therefore desire an asymmetric bandwidth constraint on the two directions (different bandwidth forward than reverse). The Palindromic PCE also supports this functionality as long as the structure of the physical routes conforms to the expectation of a perfect palindrome.

This module comprises the majority of the :ref:`basic_pce_service` as well as the :ref:`asymm_pce_service`.


Module Details
--------------
**Calls:**

- :ref:`pce_dijkstra`
- :ref:`service_topology`
- :ref:`service_pruning`

**Called By:** 

- :ref:`pce_top`

**API Specification:**

- :java:ref:`PalindromicalPCE`
