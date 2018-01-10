<#-- @ftlvariable name="mxLsps" type="java.util.List<net.es.oscars.dto.pss.params.mx.MxLsp>" -->
<#-- @ftlvariable name="vpls" type="net.es.oscars.dto.pss.params.mx.MxVpls" -->
<#assign community = vpls.vcId >

delete policy-options community "${community}"
delete policy-options policy-statement "${vpls.policyName}"
delete routing-options forwarding-table export "${vpls.policyName}"
delete routing-instances "${vpls.serviceName}"



