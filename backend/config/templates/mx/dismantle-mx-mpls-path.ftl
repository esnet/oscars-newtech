<#-- @ftlvariable name="paths" type="java.util.List<net.es.oscars.pss.params.MplsPath>" -->

<#list paths as path>
delete protocols mpls path "${path.name}"
</#list>
