
package sip4me.gov.nist.siplite.address;

import sip4me.gov.nist.core.ParseException;

/** Implementation of the TelURL interface.
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
 */
public class TelURL extends URI {


    protected String scheme;
    
    protected TelephoneNumber telephoneNumber;
    
    protected String phoneContext;
    

    
    /** Creates a new instance of TelURLImpl */
    public TelURL() {
        this.scheme = "tel";
    }

	
    /** Set the telephone number.
     *@param telephoneNumber -- telephone number to set.
     */

    public void setTelephoneNumber(TelephoneNumber telephoneNumber) {
	this.telephoneNumber = telephoneNumber;
    }

    
    /** Returns the value of the <code>isdnSubAddress</code> parameter, or null
     * if it is not set.
     *
     * @return  the value of the <code>isdnSubAddress</code> parameter
     */
    public String getIsdnSubAddress() {
        return telephoneNumber.getIsdnSubaddress();
    }
    
    /** Returns the value of the <code>postDial</code> parameter, or null if it
     * is not set.
     *
     * @return  the value of the <code>postDial</code> parameter
     */
    public String getPostDial() {
        return telephoneNumber.getPostDial();
    }
    
    /** Returns the value of the "scheme" of this URI, for example "sip", "sips"
     * or "tel".
     *
     * @return the scheme paramter of the URI
     */
    public String getScheme() {
        return this.scheme;
    }
    
    /** Returns <code>true</code> if this TelURL is global i.e. if the TelURI
     * has a global phone user.
     *
     * @return <code>true</code> if this TelURL represents a global phone user,
     * and <code>false</code> otherwise.
     */
    public boolean isGlobal() {
        return telephoneNumber.isGlobal();
    }
    
    /** This method determines if this is a URI with a scheme of "sip" or "sips".
     *
     * @return true if the scheme is "sip" or "sips", false otherwise.
     */
    public boolean isSipURI() {
        return false;
    }
    
    /** Sets phone user of this TelURL to be either global or local. The default
     * value is false, hence the TelURL is defaulted to local.
     *
     * @param global - the boolean value indicating if the TelURL has a global
     * phone user.
     */
    public void setGlobal(boolean global) {
        this.telephoneNumber.setGlobal(true);
    }
    
    /** Sets ISDN subaddress of this TelURL. If a subaddress is present, it is
     * appended to the phone number after ";isub=".
     *
     * @param isdnSubAddress - new value of the <code>isdnSubAddress</code>
     * parameter
     */
    public void setIsdnSubAddress(String isdnSubAddress) {
        this.telephoneNumber.setIsdnSubaddress(isdnSubAddress);
    }
    
    /** Sets post dial of this TelURL. The post-dial sequence describes what and
     * when the local entity should send to the phone line.
     *
     * @param postDial - new value of the <code>postDial</code> parameter
     */
    public void setPostDial(String postDial) {
        this.telephoneNumber.setPostDial(postDial);
    }
    

    /** Set the telephone number.
     * @param telphoneNumber -- long phone number to set.
     */
     public void setPhoneNumber(String telephoneNumber) {
	this.telephoneNumber.setPhoneNumber(telephoneNumber);
     }

     /** Get the telephone number. 
      *
      *@return -- the telephone number.
      */
     public String getPhoneNumber() {
	return this.telephoneNumber.getPhoneNumber();
     }
     

     /**
      * Set the phone context.
      * @param phoneContext
      * @throws ParseException
      */
     public void setPhoneContext(String phoneContext) {
    	 this.phoneContext = phoneContext;
     }

     /**
      * Get the phone context.
      * @return
      */
     public String getPhoneContext() {
         
         return phoneContext;
     }
     
     
    
    /** Return the string encoding.
     *
     *@return -- the string encoding.
     */
    public String toString() {
        return this.scheme + ":" + telephoneNumber.encode();
    }

    public String encode() {
        return this.scheme + ":" + telephoneNumber.encode();
    }

	
    /** Deep copy clone operation.
    *
    *@return -- a cloned version of this telephone number.
    */
    public Object clone() {
	  TelURL retval = new TelURL();
	  retval.scheme = this.scheme;
	  if (this.telephoneNumber != null) 
	  retval.telephoneNumber = 
			(TelephoneNumber)this.telephoneNumber.clone();
	  return retval;
     }
    
    
    
}
