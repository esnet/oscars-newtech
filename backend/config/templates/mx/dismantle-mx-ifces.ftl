<#-- @ftlvariable name="ifces" type="java.util.List<net.es.oscars.pss.params.mx.TaggedIfce>" -->
@version: 1.0.35

<#list ifces as ifce>
delete interfaces ${ifce.port} unit ${ifce.vlan}
</#list>

