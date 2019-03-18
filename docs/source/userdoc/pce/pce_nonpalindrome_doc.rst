.. _pce_nonpalindrome:

Non-Palindromic PCE Module
==========================

The Non-Palindromic PCE finds a pair of paths through the network (forward-direction and reverse-direction), such that they are **not** required to mirror each other, i.e. they are not required to be palindromes. The concept is that if there is sufficient bandwidth availability in the forward-direction on a route, but insufficient bandwidth to support the reverse-direction setup, an alternate reverse path can be established instead. This is particularly useful when the reverse path will be used primarily for control signals which are not essential to business operations, but *are* required for communication to be properly established. From the user's perspective, the circuit is logically identical as long as some reverse path can be established. 

Some caveats exist to this flexibility however. **All** Ethernet devices and ports found in the forward path, as well as those network links connected to them **must** be represented in the return path. Ethernet ports are configured with a given VLAN tag, and the remote port is required to use the same VLAN configuration. **Only** segments of the network which consist of exclusively MPLS routers/ports and the links that connect them may differ. Therefore, the forward path will be computed and then the MPLS subpaths on the reverse path will be computed independently. If a solution cannot be found for the reverse path given the computed forward path, the circuit reservation will fail. 

Because the reverse path's MPLS segments are computed independently, it's quite possible that they will mirror those in the forward path. There is currently no support for requiring that the paths must be non-palindromic.

This module comprises the majority of the :ref:`nonpalindromic_pce_service` and also supports combining that service with the :ref:`asymm_pce_service`.


Module Details
--------------
**Calls:**

- :ref:`pce_dijkstra`
- :ref:`service_topology`
- :ref:`service_pruning`
- :ref:`virtual_topo`

**Called By:** 

- :ref:`pce_top`

**API Specification:**

- :java:ref:`NonPalindromicalPCE`
