
<#assign svcId = vpls.svcId>
show service id ${svcId} sap
show service id ${svcId} sdp

show service id ${svcId} fdb detail

<#list vpls.saps as sap>
    <#assign sapId = sap.port+":"+sap.vlan>
show service id ${svcId} sap ${sapId} stats
</#list>

<#list lsps as lsp>
<#assign lspName = lsp.name>
show router mpls lsp "${lspName}" detail
</#list>

<#list paths as path>
show router mpls path "${path.name}"
show router mpls path "${path.name}" lsp-binding
</#list>

<#list sdps as sdp>
    <#assign sdpId = sdp.sdpId>
show service sdp ${sdpId} detail
</#list>

<#list vpls.saps as sap>
    <#assign sapId = sap.port+":"+sap.vlan>
show service id ${svcId} sap ${sapId} detail
</#list>
