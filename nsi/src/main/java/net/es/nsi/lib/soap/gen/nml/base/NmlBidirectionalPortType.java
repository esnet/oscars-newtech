//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.06.19 at 01:27:41 PM PDT 
//


package net.es.nsi.lib.soap.gen.nml.base;

import org.w3c.dom.Element;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for BidirectionalPortType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BidirectionalPortType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://schemas.ogf.org/nml/2013/05/base#}NetworkObject"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{http://schemas.ogf.org/nml/2013/05/base#}BaseBidirectionalPort"/&gt;
 *         &lt;any processContents='lax' namespace='##other' maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;anyAttribute processContents='lax' namespace='##other'/&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BidirectionalPortType", propOrder = {
    "rest"
})
public class NmlBidirectionalPortType
    extends NmlNetworkObject
{

    @XmlElementRefs({
        @XmlElementRef(name = "Port", namespace = "http://schemas.ogf.org/nml/2013/05/base#", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "PortGroup", namespace = "http://schemas.ogf.org/nml/2013/05/base#", type = JAXBElement.class, required = false)
    })
    @XmlAnyElement(lax = true)
    protected List<Object> rest;

    /**
     * Gets the rest of the content model. 
     * 
     * <p>
     * You are getting this "catch-all" property because of the following reason: 
     * The field name "Port" is used by two different parts of a schema. See: 
     * line 523 of file:/Users/haniotak/ij/oscars-newtech/nsi/src/main/xsd/ogf_nml_base.xsd
     * line 522 of file:/Users/haniotak/ij/oscars-newtech/nsi/src/main/xsd/ogf_nml_base.xsd
     * <p>
     * To get rid of this property, apply a property customization to one 
     * of both of the following declarations to change their names: 
     * Gets the value of the rest property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rest property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRest().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link NmlPortType }{@code >}
     * {@link JAXBElement }{@code <}{@link NmlPortGroupType }{@code >}
     * {@link Object }
     * {@link Element }
     * 
     * 
     */
    public List<Object> getRest() {
        if (rest == null) {
            rest = new ArrayList<Object>();
        }
        return this.rest;
    }

}
