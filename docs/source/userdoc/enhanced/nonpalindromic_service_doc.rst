.. _nonpalindromic_pce_service:

Non-Palindromic Return Path Service
===================================

It is possible for the MPLS layer to support non-identical A-Z and Z-A paths. The user can request that the path computation performed for a particulat pipe be Non-Palindromic, allowing the PCE to use (possibly) different paths through the MPLS layer to provision the request. All Ethernet devices/ports/links used in the forward path, **must** be represented in the return path. Only MPLS devices/ports/links may differ in forward-/reverse-direction paths. This is accomplished by creating a :ref:`virtual_topo` for each Non-Palindromic request. This Service-Layer topology abstracts out the MPLS-Layer devices, and replaces them with a pair of abstract links between each pair of Service-Layer elements adjacent to the MPLS-Layer. These abstract links are computed independently such they they each correspond to the shortest path between the end-points. Therefore, the forward and return paths *may* contain different routing elements.

.. note::

	There is currently no support for *forcing* Non-Palindromic solutions. It's entirely possible for the PCE to compute Palindromic solutions even when Non-Palindromic service is requested. 

.. figure:: ../../.static/pce_nonpal.gif
    :scale: 75%
    :alt: Non-Palindromic Circuit
    :align: center

    *Non-Palindromic circuit with a different return path.*

.. warning::

	The Non-Palindromic service *may* return false-negatives. The PCE first computes a forward-direction route on the Service-Layer topology, and then uses the Palindromic return path on the Service-Layer to compute and translate the physical return path. If the corresponding physical return path cannot be reserved, due to bandwidth or VLAN availability, the entire circuit reservation fails. There is currently no attempt to try alternative path pairs.


Service Details
^^^^^^^^^^^^^^^

**Can be combined with:**

- :ref:`asymm_pce_service`
- :ref:`multipoint_pce_service`
- :ref:`ero_pce_services`

**Relevant PCE Module(s)**

- :ref:`pce_nonpalindrome`
- :ref:`pce_ero`


.. toctree::
   :hidden:

   virtual_topo_doc
