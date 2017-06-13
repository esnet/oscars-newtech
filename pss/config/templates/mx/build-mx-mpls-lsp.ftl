<#-- @ftlvariable name="lsps" type="java.util.List<net.es.oscars.dto.pss.params.mx.MxLsp>" -->
<#list lsps as mxlsp>
top
edit protocols mpls label-switched-path ${mxlsp.lsp.name}
set to ${mxlsp.lsp.to}
set metric 65000
set no-cspf
set priority 4 4
set primary ${mxlsp.lsp.pathName}
<#if mxlsp.policeFilter??>
set policing filter ${mxlsp.policeFilter}
</#if>
</#list>