<#-- @ftlvariable name="loopback_ifce_name" type="java.lang.String" -->

/configure router interface "${loopback_ifce_name}" shutdown
/configure router interface "${loopback_ifce_name}" no address
/configure router no interface "${loopback_ifce_name}"

