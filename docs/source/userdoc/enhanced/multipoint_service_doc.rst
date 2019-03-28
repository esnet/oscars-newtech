.. _multipoint_pce_service:

Multipoint Routing Service
==========================

As mentioned in the :ref:`pce_doc`, and the :ref:`plumbing`, multiple pipes may be requested within one single :ref:`requestspec`. By etsbalishing pipes between a set consisting of more than two end-points, a circuit can be established as a Multipoint, or Multicast, or Mesh service. The figure below illustrates the plumbing illustration which can be used to specifiy such a setup as well as the physical routing solutions that will provision the requested circuit. This example shows a Multicast circuit with a single source and multiple destinations.


.. figure:: ../../.static/pce_multicast.gif
    :scale: 60%
    :alt: Multicast Circuit
    :align: center

    *Multipoint circuit with a single shared source node transmitting to two distinct destinations.*

The following figure shows a similar setup, but with a Mesh structure such that all nodes are transferring to and from one another within the end-point set.

.. figure:: ../../.static/pce_mesh.gif
    :scale: 60%
    :alt: Mesh Circuit
    :align: center

    *Multipoint circuit with a set of end-points all communicating directly with one another.*



.. warning::

	Users can submit any number of pipes within a circuit request, and the PCE will attempt to provision them all. As such, the reservations corresponding to individual pipes will compete with each other for resources. In the implementation of this service, the pipes are first sorted and pathfinding is tried in order. If it is impossible to provision them in that sequence, the order will be reversed and pathfinding is attempted again. If it is still not possible to find a path for every pipe, the request fails.


Service Details
^^^^^^^^^^^^^^^

**Can be combined with:**

- :ref:`basic_pce_service`
- :ref:`nonpalindromic_pce_service`
- :ref:`ero_pce_services`
- :ref:`surv_pce_services`

**Relevant PCE Module(s)**

- :ref:`pce_palindrome`
- :ref:`pce_nonpalindrome`
- :ref:`pce_ero`
- :ref:`pce_survivability`

