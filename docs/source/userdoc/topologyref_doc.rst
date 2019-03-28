.. _topologyref:

Topology Assumptions Reference
==============================

The figure below provides a basic reference to the primary network topology assumptions used throughout the illustrative examples found in remainder of this documentation suite. For example, these assumptions apply to the documents on :ref:`pce_doc` and :ref`advanced_service`.

.. figure:: ../.static/topology_ref.png
    :width: 50%
    :alt: Topology Assumptions
    :align: center

    *Assumptions of topology components.*


All network links are assumed to be comprised of a bidirectional pair of individual unidirectional fibers in opposite directions such that circuit established in one direction do not necessarily indicate the same in reverse. 

.. note::

   The End-points are illustrated separately from the devices to which they belong. This is purely for clarity in how the source and destination points of a circuit are represented. The OSCARS topology is implemented as a collection of nodes and adjacencies, where the nodes may either be devices or ports. The illustration therefore aims to represent a bit more precisely the relationships between devices and ports. Note however that there is no cost to establish a route from a port to a device or vice-versa.
