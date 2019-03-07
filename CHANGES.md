# OSCARS Release Notes

## v1.0.27

> Mar 6, 2019
- Fixes in MX LSPs templates (priority and metrics)
- Topology python script fixes for incorrect loopbacks
- Add API for historical data [#285](https://github.com/esnet/oscars-newtech/issues/285)
- Parameterize minimum connection duration [#283](https://github.com/esnet/oscars-newtech/issues/283)
- Conditionally set output-vlan-map swap  [#282](https://github.com/esnet/oscars-newtech/issues/282)
- Adds NSI forced-end message 

## v1.0.26

> Feb 21, 2019

- Fix latitude / longitude inversion in config file nsi.nsa-location [#273](https://github.com/esnet/oscars-newtech/issues/273)
- Deparallelize NSI callbacks [#272](https://github.com/esnet/oscars-newtech/issues/272)
- Handle if-modified-since header in requests [#238](https://github.com/esnet/oscars-newtech/issues/238)
- Add feature to allow service MTU override [#258](https://github.com/esnet/oscars-newtech/issues/258)
- Fix NSI version handling [#266](https://github.com/esnet/oscars-newtech/issues/266)
- Make port/device geo locations available thru API (/api/topo/locations) [#275](https://github.com/esnet/oscars-newtech/issues/275)
- Update frontend to 1.0.21

## v1.0.25

> Feb 7, 2019

- Add physical port locations to NML topology [#264](https://github.com/esnet/oscars-newtech/issues/264)
- Fix a topology loopback address bug
- Fix for source / destination flip
- Correct filter behavior for empty phase
- Update frontend to 1.0.20
