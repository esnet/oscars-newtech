<#-- @ftlvariable name="ifces" type="java.util.List<net.es.oscars.pss.params.mx.TaggedIfce>" -->

<#list ifces as ifce>
delete interfaces ${ifce.port} unit ${ifce.vlan}
</#list>

