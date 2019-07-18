<#-- @ftlvariable name="lsps" type="java.util.List" -->
<#-- @ftlvariable name="lsp" type="net.es.oscars.pss.params.Lsp" -->

<#list lsps as lsp>
/configure router mpls lsp "${lsp.name}" shutdown
/configure router mpls no lsp "${lsp.name}"
</#list>

