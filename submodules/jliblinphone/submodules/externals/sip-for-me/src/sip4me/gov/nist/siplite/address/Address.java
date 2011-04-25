/*****************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).     *
 *****************************************************************************/
package sip4me.gov.nist.siplite.address;
import sip4me.gov.nist.core.GenericObject;
import sip4me.gov.nist.core.HostPort;
import sip4me.gov.nist.core.Separators;

/**
 * Address structure. Imbeds a URI and adds a display name.
 *
 *@author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 *@version JAIN-SIP-1.1
 *
 */
public class Address extends GenericObject {
    
    
    /** Constant field.
     */
    public static final int NAME_ADDR   = 1;
    
    /** constant field.
     */
    public static final int ADDRESS_SPEC = 2;
    
    /** Constant field.
     */
    public static final int WILD_CARD = 3;
    
    protected int    addressType;
    
    /** displayName field
     */
    protected String displayName;
    
    /** address field
     */
    protected URI	 address;
    
    
    /** Get the host port portion of the address spec.
     *@return host:port in a HostPort structure.
     */
    public HostPort getHostPort() {
        if (! (address instanceof SipURI) )
            throw new RuntimeException
            ("address is not a SipURI");
        SipURI uri =  (SipURI) address;
        return uri.getHostPort();
    }
    
    
    /** Get the port from the imbedded URI. This assumes that a SIP URL
     * is encapsulated in this address object.
     *
     *@return the port from the address.
     *
     */
    public int getPort() {
        if (! (address instanceof SipURI) )
            throw new RuntimeException
            ("address is not a SipURI");
        SipURI uri = (SipURI) address;
        return uri.getHostPort().getPort();
    }
    
    
    /** Get the user@host:port for the address field. This assumes
     * that the encapsulated object is a SipURI. 
     *
     * BUG Fix from Antonis Kadris.
     *
     *@return string containing user@host:port.
     */
    public String getUserAtHostPort() {
        if (address instanceof SipURI)  {
           SipURI uri = (SipURI) address;
           return uri.getUserAtHostPort();
	} else return address.toString();
    }

    /** Get the host name from the address.
     *
     *@return the host name.
     */
     public String getHost() {
        if (! (address instanceof SipURI) )
            throw new RuntimeException
            ("address is not a SipURI");
        SipURI uri = (SipURI) address;
        return uri.getHostPort().getHost().getHostname();
    }
    
    
    
    /** Remove a parameter from the address.
     *
     *@param parameterName is the name of the parameter to remove.
     */
    public void removeParameter(String parameterName) {
        if (! (address instanceof SipURI) )
            throw new RuntimeException
            ("address is not a SipURI");
        SipURI uri = (SipURI) address;
        uri.removeParameter(parameterName);
    }
    
    
    /**
     * Encode the address as a string and return it.
     * @return String canonical encoded version of this address.
     */
    public String encode() {
        if (this.addressType == WILD_CARD) return "*";
        StringBuffer encoding = new StringBuffer();
        if (displayName != null) {
            encoding.append(Separators.DOUBLE_QUOTE)
            .append(displayName)
            .append(Separators.DOUBLE_QUOTE)
            .append(Separators.SP);
        }
        if (address != null) {
            if (addressType == NAME_ADDR || displayName != null)
                encoding.append(Separators.LESS_THAN);
            encoding.append(address.encode());
            if (addressType == NAME_ADDR || displayName != null)
                encoding.append(Separators.GREATER_THAN);
        }
        return encoding.toString();
    }
    
    public Address() { this.addressType = NAME_ADDR; }
    
    /**
     * Get the address type;
     * @return int
     */
    public int getAddressType() {
        return addressType;
    }
    
    
    /**
     * Set the address type. The address can be NAME_ADDR, ADDR_SPEC or
     * WILD_CARD
     *
     * @param atype int to set
     *
     */
    public void setAddressType( int atype) {
        addressType = atype;
    }
    
    /**
     * get the display name
     *
     * @return String
     *
     */
    public String getDisplayName() {
        return displayName ;
    }
    
    
    
    /**
     * Set the displayName member
     *
     * @param displayName String to set
     *
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName ;
        this.addressType = NAME_ADDR;
    }
    
    
    /**
     * Set the address field
     *
     * @param address SipURI to set
     *
     */
    public void setURI(URI address) {
        this.address = (URI)address ;
    }
    
    /**
     * Compare two address specs for equality.
     *
     * @param other Object to compare this this address
     *
     * @return boolean
     *
     */
    public boolean equals(Object other) {
        
        if (! this.getClass().equals(other.getClass())) {
            return false;
        }
        Address that = (Address) other;
        if (this.addressType == WILD_CARD &&
        that.addressType != WILD_CARD) return false;
        
        // Ignore the display name; only compare the address spec.
        boolean retval =  this.address.equals(that.address);
        return retval;
    }
    
    /** return true if DisplayName exist.
     *
     * @return boolean
     */
    public boolean hasDisplayName() {
        return (displayName!=null);
    }
    
    /** remove the displayName field
     */
    public void removeDisplayName() {
        displayName=null;
    }
    
    /** Return true if the imbedded URI is a sip URI.
     *
     * @return true if the imbedded URI is a SIP URI.
     *
     */
    public boolean isSIPAddress() {
        return address instanceof SipURI;
    }
    
    /** Returns the URI address of this Address. The type of URI can be
     * determined by the scheme.
     *
     * @return address parmater of the Address object
     */
    public URI getURI() {
        return this.address;
    }
    
    /** This determines if this address is a wildcard address. That is
     * <code>Address.getAddress.getUserInfo() == *;</code>
     *
     * @return true if this name address is a wildcard, false otherwise.
     */
    public boolean isWildcard() {
        return this.addressType == WILD_CARD;
    }
    

    
    
    /** Set the user name for the imbedded URI.
     *
     *@param user -- user name to set for the imbedded URI.
     */
    public void setUser(String user){
        ((SipURI)this.address).setUser(user);
    }
    
       /** Clone this structure.
        * @return Object Address
	*/
	public Object clone() {
		Address retval = new Address();
                retval.addressType=this.addressType;
		if (this.displayName != null) 
			retval.displayName = new String(this.displayName);
		if (this.address != null) 
			retval.address =(URI)this.address.clone();
		return (Object) retval;

	}
    
}
