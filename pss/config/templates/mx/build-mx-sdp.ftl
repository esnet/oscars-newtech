<#-- @ftlvariable name="mxLsps" type="java.util.List<net.es.oscars.dto.pss.params.mx.MxLsp>" -->
<#-- @ftlvariable name="vpls" type="net.es.oscars.dto.pss.params.mx.MxVpls" -->
<#assign communityMembers = "65000:"+vpls.community+":"+vpls.vcId>
<#assign mesh_group = "sdp-"+vpls.vcId >

set policy-options community "${vpls.community}" members ${communityMembers}

top
edit policy-options policy-statement "${vpls.policyName}" term oscars
set from community "${vpls.community}"
<#list mxLsps as mxlsp>
set then install-nexthop lsp "${mxlsp.lsp.name}"
</#list>
set then accept
top

set routing-options forwarding-table export [ "${vpls.policyName}" ]

<#list mxLsps as mxlsp>
<#assign lsp_neighbor = mxlsp.neighbor>
edit routing-instances "${vpls.serviceName}" protocols vpls mesh-group "${mesh_group}"
set vpls-id ${vpls.vcId}
edit neighbor ${lsp_neighbor}
set psn-tunnel-endpoint ${mxlsp.lsp.to}
set community "${vpls.community}"
set encapsulation-type ethernet-vlan
</#list>


