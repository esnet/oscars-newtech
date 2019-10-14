<#-- @ftlvariable name="lsps" type="java.util.List<net.es.oscars.pss.params.mx.MxLsp>" -->
@version: 1.0.37

<#list lsps as mxlsp>
delete protocols mpls label-switched-path "${mxlsp.lsp.name}"
</#list>


