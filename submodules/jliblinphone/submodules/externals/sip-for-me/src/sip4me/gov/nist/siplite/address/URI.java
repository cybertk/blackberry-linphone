package sip4me.gov.nist.siplite.address;
import sip4me.gov.nist.core.GenericObject;
import sip4me.gov.nist.core.ParseException;

/** Implementation of the URI class. 
 *
 *@author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class URI  extends GenericObject {
    public static final String SIP = ParameterNames.SIP_URI_SCHEME;
    public static final String SIPS = ParameterNames.SIPS_URI_SCHEME;
    public static final String TEL = ParameterNames.TEL_URI_SCHEME;
    public static final String POSTDIAL  =  ParameterNames.POSTDIAL;
    public static final String PHONE_CONTEXT_TAG 
				= ParameterNames.PHONE_CONTEXT_TAG;
    public static final String ISUB 	  = ParameterNames.ISUB;
    public static final String PROVIDER_TAG    = ParameterNames.PROVIDER_TAG;
    public static final String USER = ParameterNames.USER;
    public static final String TRANSPORT = ParameterNames.TRANSPORT;
    public static final String METHOD = ParameterNames.METHOD;
    public static final String TTL = ParameterNames.TTL;
    public static final String MADDR = ParameterNames.MADDR;
    public static final String LR = ParameterNames.LR;
 
    
    /** Embedded URI
     */    
    protected String uriString;
    
    protected String scheme;
    
    /** Constructor
     */    
    protected URI() {}
    
    /** Constructor given the URI string
     * @param uriString The imbedded URI string.
     * @throws URISyntaxException When there is a syntaz error in the imbedded URI.
     */    
    public  URI(String uriString) throws ParseException {
        try{
            this.uriString = uriString;
            int i=uriString.indexOf(":");
            scheme=uriString.substring(0,i);
        }
        catch(Exception e) {
            throw new ParseException("URI, Bad URI format",0);
        }
    }
    
    /** Encode the URI.
     * @return The encoded URI
     */    
    public String encode() {
       return uriString;
       
    }
    
    /** Encode this URI.
     * @return The encoded URI
     */
    public String toString() {
        return this.encode(); 
     
    }
    
    /** Overrides the base clone method
     * @return The Cloned strucutre,
     */    
    public Object clone()  {
        try {
            return new URI(this.uriString);
            
        }
        catch ( Exception ex){
        
            throw new RuntimeException(ex.getMessage() + this.uriString);
        }
    }
   
    /** Returns the value of the "scheme" of
     * this URI, for example "sip", "sips" or "tel".
     *
     * @return the scheme paramter of the URI
     */
    public String getScheme() {
       return scheme;
    }
    
    /** This method determines if this is a URI with a scheme of
     * "sip" or "sips".
     *
     * @return true if the scheme is "sip" or "sips", false otherwise.
     */
    public boolean isSipURI() {
        return this instanceof SipURI;
       
    }
    
}

