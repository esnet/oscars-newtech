//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.06.19 at 01:27:41 PM PDT 
//


package net.es.nsi.lib.soap.gen.nml.base;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for SwitchingServiceRelationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SwitchingServiceRelationType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element ref="{http://schemas.ogf.org/nml/2013/05/base#}Port" maxOccurs="unbounded"/&gt;
 *         &lt;element ref="{http://schemas.ogf.org/nml/2013/05/base#}PortGroup" maxOccurs="unbounded"/&gt;
 *         &lt;element ref="{http://schemas.ogf.org/nml/2013/05/base#}SwitchingService" maxOccurs="unbounded"/&gt;
 *         &lt;element ref="{http://schemas.ogf.org/nml/2013/05/base#}Link" maxOccurs="unbounded"/&gt;
 *         &lt;element ref="{http://schemas.ogf.org/nml/2013/05/base#}LinkGroup" maxOccurs="unbounded"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="type" use="required"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;enumeration value="http://schemas.ogf.org/nml/2013/05/base#hasInboundPort"/&gt;
 *             &lt;enumeration value="http://schemas.ogf.org/nml/2013/05/base#hasOutboundPort"/&gt;
 *             &lt;enumeration value="http://schemas.ogf.org/nml/2013/05/base#isAlias"/&gt;
 *             &lt;enumeration value="http://schemas.ogf.org/nml/2013/05/base#providesLink"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SwitchingServiceRelationType", propOrder = {
    "port",
    "portGroup",
    "switchingService",
    "link",
    "linkGroup"
})
public class NmlSwitchingServiceRelationType {

    @XmlElement(name = "Port")
    protected List<NmlPortType> port;
    @XmlElement(name = "PortGroup")
    protected List<NmlPortGroupType> portGroup;
    @XmlElement(name = "SwitchingService")
    protected List<NmlSwitchingServiceType> switchingService;
    @XmlElement(name = "Link")
    protected List<NmlLinkType> link;
    @XmlElement(name = "LinkGroup")
    protected List<NmlLinkGroupType> linkGroup;
    @XmlAttribute(name = "type", required = true)
    protected String type;

    /**
     * Gets the value of the port property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the port property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPort().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link NmlPortType }
     * 
     * 
     */
    public List<NmlPortType> getPort() {
        if (port == null) {
            port = new ArrayList<NmlPortType>();
        }
        return this.port;
    }

    /**
     * Gets the value of the portGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the portGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPortGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link NmlPortGroupType }
     * 
     * 
     */
    public List<NmlPortGroupType> getPortGroup() {
        if (portGroup == null) {
            portGroup = new ArrayList<NmlPortGroupType>();
        }
        return this.portGroup;
    }

    /**
     * Gets the value of the switchingService property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the switchingService property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSwitchingService().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link NmlSwitchingServiceType }
     * 
     * 
     */
    public List<NmlSwitchingServiceType> getSwitchingService() {
        if (switchingService == null) {
            switchingService = new ArrayList<NmlSwitchingServiceType>();
        }
        return this.switchingService;
    }

    /**
     * Gets the value of the link property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the link property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLink().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link NmlLinkType }
     * 
     * 
     */
    public List<NmlLinkType> getLink() {
        if (link == null) {
            link = new ArrayList<NmlLinkType>();
        }
        return this.link;
    }

    /**
     * Gets the value of the linkGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the linkGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLinkGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link NmlLinkGroupType }
     * 
     * 
     */
    public List<NmlLinkGroupType> getLinkGroup() {
        if (linkGroup == null) {
            linkGroup = new ArrayList<NmlLinkGroupType>();
        }
        return this.linkGroup;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

}