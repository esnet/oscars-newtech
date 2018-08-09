<#-- @ftlvariable name="mxLsps" type="java.util.List<net.es.oscars.dto.pss.params.mx.MxLsp>" -->
<#-- @ftlvariable name="vpls" type="net.es.oscars.dto.pss.params.mx.MxVpls" -->
<#assign communityMembers = "65000:672277L:"+vpls.vcId>
<#assign mesh_group = "sdp-"+vpls.vcId >
<#assign community = vpls.vcId >

set policy-options community "${vpls.vcId}" members ${communityMembers}

top
edit policy-options policy-statement "${vpls.policyName}" term oscars
set from community "${community}"
<#list mxLsps as mxlsp>
set then install-nexthop lsp "${mxlsp.lsp.name}"
</#list>
set then accept
top

set routing-options forwarding-table export [ "${vpls.policyName}" ]

<#list mxLsps as mxlsp>
    <#if mxlsp.primary>
        <#assign lsp_neighbor = mxlsp.neighbor>
edit routing-instances "${vpls.serviceName}" protocols vpls mesh-group "${mesh_group}"
set local-switching
set vpls-id ${vpls.vcId}
edit neighbor ${lsp_neighbor}
set psn-tunnel-endpoint ${mxlsp.lsp.to}
set community "${community}"
set encapsulation-type ethernet-vlan
top
    </#if>
</#list>


<#if vpls.protectEnabled>
    <#assign prt_mesh_group = "sdp-"+vpls.protectVcId >
    <#list mxLsps as mxlsp>
        <#if !mxlsp.primary>
        <#assign lsp_neighbor = mxlsp.neighbor>
edit routing-instances "${vpls.serviceName}" protocols vpls mesh-group "${prt_mesh_group}"
set local-switching
set vpls-id ${vpls.protectVcId}
edit neighbor ${lsp_neighbor}
set psn-tunnel-endpoint ${mxlsp.lsp.to}
set community "${community}"
set encapsulation-type ethernet-vlan
top
        </#if>
    </#list>

</#if>
