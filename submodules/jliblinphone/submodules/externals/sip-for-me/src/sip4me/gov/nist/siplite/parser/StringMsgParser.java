/*******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD)        *
 ******************************************************************************/
package sip4me.gov.nist.siplite.parser;

import java.io.UnsupportedEncodingException;
import java.util.Vector;

import sip4me.gov.nist.core.Debug;
import sip4me.gov.nist.core.Host;
import sip4me.gov.nist.core.HostNameParser;
import sip4me.gov.nist.core.HostPort;
import sip4me.gov.nist.core.LogWriter;
import sip4me.gov.nist.core.ParseException;
import sip4me.gov.nist.core.StringTokenizer;
import sip4me.gov.nist.siplite.SIPConstants;
import sip4me.gov.nist.siplite.address.Address;
import sip4me.gov.nist.siplite.address.SipURI;
import sip4me.gov.nist.siplite.address.TelephoneNumber;
import sip4me.gov.nist.siplite.address.URI;
import sip4me.gov.nist.siplite.header.ExtensionHeader;
import sip4me.gov.nist.siplite.header.Header;
import sip4me.gov.nist.siplite.header.NameMap;
import sip4me.gov.nist.siplite.header.RequestLine;
import sip4me.gov.nist.siplite.header.StatusLine;
import sip4me.gov.nist.siplite.message.Message;
import sip4me.gov.nist.siplite.message.Request;
import sip4me.gov.nist.siplite.message.Response;


/**
 * Parse SIP message and parts of SIP messages such as URI's etc
 * from memory and return a structure.
 * Intended use:  UDP message processing.
 * This class is used when you have an entire SIP message or Header
 * or SIP URL in memory and you want to generate a parsed structure from
 * it. For SIP messages, the payload can be binary or String.
 * If you have a binary payload,
 * use parseMessage(byte[]) else use parseSIPMessage(String)
 * The payload is accessible from the parsed message using the getContent and
 * getContentBytes methods provided by the Message class.
 * Currently only eager parsing of the message is supported (i.e. the
 * entire message is parsed in one feld swoop).
 *
 *
 *@version  JAIN-SIP-1.1
 *
 *@author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */

public class StringMsgParser {
    
    protected boolean readBody;
    
    private String rawMessage;
    // Unprocessed message  (for error reporting)
    private String rawMessage1;
    // Unprocessed message  (for error reporting)
    private String currentMessage;
    // the message being parsed. (for error reporting)
    private ParseExceptionListener parseExceptionListener;
    
    private Vector messageHeaders; // Message headers
    
    private int bufferPointer;
    
    private boolean bodyIsString;
    
    private byte[] currentMessageBytes;
    
    protected int contentLength;
    
    private boolean debugFlag;
    
    private int currentLine;
    
    private String currentHeader;
    
    
    
    
    
    /**
     *@since v0.9
     */
    public StringMsgParser() {
        super();
        messageHeaders = new Vector(10,10);
        bufferPointer = 0;
        currentLine = 0;
        readBody = true;
    }
    
    
    /**
     *Constructor (given a parse exception handler).
     *@since 1.0
     *@param exhandler is the parse exception listener for the message parser.
     */
    public StringMsgParser( ParseExceptionListener exhandler) {
        this();
        parseExceptionListener = exhandler;
    }
    
    
    
    
    
    /** Get the message body.
     */
    protected String getMessageBody() {
        
        if (this.contentLength == 0 ) {
            return null;
        } else {
        	// just return the rest of the message, because in some cases
        	// the index could be wrong and cut the message (encodings? wrong C-L?)
        	// JAIN-SIP 1.2 also does a similar thing.
            String body = currentMessage.substring(bufferPointer);
            bufferPointer = currentMessage.length();
            
            this.contentLength =  0;
            return body;
        }
        
    }
    
    /** Get the message body as a byte array.
     */
    protected byte[] getBodyAsBytes() {
        if (this.contentLength == 0 ) {
            return null;
        } else {
            int endIndex = bufferPointer + this.contentLength;
            // guard against bad specifications.
            if (endIndex > currentMessageBytes.length) {
                endIndex = currentMessageBytes.length;
            }
            byte[] body = new byte[endIndex - bufferPointer];
            System.arraycopy
            (currentMessageBytes,bufferPointer,body,0, body.length);
            bufferPointer = endIndex;
            this.contentLength =  0;
            return  body;
        }
        
    }
    
    
    
    /** Return the contents till the end of the buffer (this is useful when
     * you encounter an error.
     */
    protected String readToEnd() {
        String body = currentMessage.substring(bufferPointer);
        bufferPointer += body.length();
        return body;
    }
    
    /** Return tbe bytes to the end of the message.
     * This is invoked when the parser is invoked with an array of bytes
     * rather than with a string.
     */
    protected byte[] readBytesToEnd() {
        byte[] body = new byte[currentMessageBytes.length - bufferPointer];
        int endIndex = currentMessageBytes.length;
        for (int i = bufferPointer, k = 0; i < endIndex; i++,k++) {
            body[k] = currentMessageBytes[i];
        }
        bufferPointer = endIndex;
        this.contentLength =  0;
        return  body;
    }
    
    
    /**
     * add a handler for header parsing errors.
     * @param  pexhadler is a class
     *  	that implements the ParseExceptionListener interface.
     */
    
    public void setParseExceptionListener
    ( ParseExceptionListener pexhandler ) {
        parseExceptionListener = pexhandler;
    }
    
    /** Return true if the body is encoded as a string.
     * If the parseMessage(String) method is invoked then the body
     * is assumed to be a string.
     */
    protected boolean isBodyString() { return bodyIsString; }
    
    
    /** Parse a buffer containing a single SIP Message where the body
     * is an array of un-interpreted bytes. This is intended for parsing
     * the message from a memory buffer when the buffer.
     * Incorporates a bug fix for a bug that was noted by Will Sullin of
     * Callcast
     * @param msgBuffer a byte buffer containing the messages to be parsed.
     *   This can consist of multiple SIP Messages concatenated together.
     * @return a Message[] structure (request or response)
     * 			containing the parsed SIP message.
     * @exception SIPIllegalMessageException is thrown when an
     * 			illegal message has been encountered (and
     *			the rest of the buffer is discarded).
     * @see ParseExceptionListener
     */
	public Message parseSIPMessage(byte[] msgBuffer) throws ParseException {
		
        bufferPointer = 0;
        bodyIsString = false;
        Vector retval = new Vector();
        currentMessageBytes = msgBuffer;
        int s;
        // Squeeze out leading CRLF
        // Squeeze out the leading nulls (otherwise the parser will crash)
        // Bug noted by Will Sullin of Callcast
        for (s = bufferPointer; s < msgBuffer.length  ; s++) {
            if ((char)msgBuffer[s] != '\r'  &&
            (char)msgBuffer[s] != '\n'  &&
            (char)msgBuffer[s] != '\0' ) break;
        }
        
        
        if (s == msgBuffer.length) return null;
        
        // Find the end of the SIP message.
        int  f;
        for (f = s ; f < msgBuffer.length -4 ; f ++) {
            if ( (char) msgBuffer[f]   == '\r' &&
            (char) msgBuffer[f+1] == '\n' &&
            (char) msgBuffer[f+2] == '\r' &&
            (char) msgBuffer[f+3] == '\n') {
                break;
            }
        }
        if (f < msgBuffer.length) f +=4;
        else {
            // Could not find CRLFCRLF end of message so look for LFLF
            for (f = s; f < msgBuffer.length -2 ; f++) {
                if ((char)msgBuffer[f] == '\n' &&
                (char)msgBuffer[f +1 ] == '\n') break;
            }
            if (f < msgBuffer.length) f += 2;
            else throw new ParseException("Message not terminated" , 0 );
        }
        
        // Encode the body as a UTF-8 string.
        String messageString = null;
        try {
            messageString = new String(msgBuffer,s, f - s, "UTF-8");
        } catch( UnsupportedEncodingException ex) {
            throw new ParseException("Bad message encoding!",0);
        }
        bufferPointer = f;
        StringBuffer message = new StringBuffer(messageString);
        int length = message.length();
        // Get rid of CR to make it uniform for the parser.
        for ( int k = 0; k < length ; k++ ) {
            if (message.charAt(k) == '\r' ) {
                message.deleteCharAt(k);
                length --;
            }
        }
        
        
        if (Parser.debug) {
            for (int k = 0 ; k < length; k++) {
                rawMessage1 = rawMessage1 + "[" + message.charAt(k) +"]";
            }
        }
        
        // The following can be written more efficiently in a single pass
        // but it is somewhat tricky.
        StringTokenizer tokenizer = new StringTokenizer
        (message.toString(),'\n');
        StringBuffer cooked_message = new StringBuffer();
        while( tokenizer.hasMoreChars() ) {
             String nexttok = tokenizer.nextToken();
            // Ignore blank lines with leading spaces or tabs.
             if (nexttok.trim().equals("")) cooked_message.append("\n");
             else cooked_message.append(nexttok);
        }
        
        String message1 = cooked_message.toString();
        length = message1.indexOf("\n\n") + 2;
        
        // Handle continuations - look for a space or a tab at the start
        // of the line and append it to the previous line.
        
        
        for ( int k = 0 ; k < length - 1 ;  ) {
            if (cooked_message.charAt(k) == '\n') {
                if ( cooked_message.charAt(k+1) == '\t' ||
                cooked_message.charAt(k+1) == ' ') {
                    cooked_message.deleteCharAt(k);
                    cooked_message.deleteCharAt(k);
                    length --;
                    length --;
                    if ( k == length) break;
                    continue;
                }
                
                if ( cooked_message.charAt(k+1) == '\n') {
                    cooked_message.insert(k,'\n');
                    length ++;
                    k ++;
                }
            }
            k++;
        }
        cooked_message.append("\n\n");
        
        // Separate the string out into substrings for
        // error reporting.
        currentMessage = cooked_message.toString();
        Message sipmsg = this.parseMessage(currentMessage);
        if (readBody && sipmsg.getContentLengthHeader() != null
        && sipmsg.getContentLengthHeader().getContentLength() != 0) {
            this.contentLength =
            sipmsg.getContentLengthHeader().getContentLength();
            byte body[] = getBodyAsBytes();
            sipmsg.setMessageContent(body);
        }
        // System.out.println("Parsed = " + sipmsg);
        return sipmsg;
        
    }
    
    /**
     * Parse a buffer containing one or more SIP Messages  and return an array of
     * Message parsed structures. Note that the current limitation is that
     * this does not handle content encoding properly. The message content is
     * just assumed to be encoded using the same encoding as the sip message
     * itself (i.e. binary encodings such as gzip are not supported).
     * @param sipMessages a String containing the messages to be parsed.
     *   This can consist of multiple SIP Messages concatenated together.
     * @return a Message structure (request or response)
     * 			containing the parsed SIP message.
     * @exception SIPIllegalMessageException is thrown when an
     * 			illegal message has been encountered (and
     *			the rest of the buffer is discarded).
     * @see ParseExceptionListener
     */
    
    public Message  parseSIPMessage(String sipMessages )
    throws ParseException {
        // Handle line folding and evil DOS CR-LF sequences
        rawMessage = sipMessages;
        String pmessage = sipMessages.trim();

        bodyIsString = true;
        
        this.contentLength = 0;
        if (pmessage.trim().equals("")) return null;
        
        pmessage += "\n\n";
        StringBuffer message = new StringBuffer(pmessage);
        // squeeze out the leading crlf sequences.
        while(message.charAt(0) == '\r' || message.charAt(0) == '\n') {
            bufferPointer ++;
            message.deleteCharAt(0);
        }
        
        // squeeze out the crlf sequences and make them uniformly CR
        String message1 = message.toString();
        int length;
        length = message1.indexOf("\r\n\r\n");
        if (length > 0 ) length += 4;
        if (length == -1) {
            length = message1.indexOf("\n\n");
            if (length == -1)
                throw new ParseException("no trailing crlf",0);
        } else length += 2;
        
        
        // Get rid of CR to make it uniform.
        for ( int k = 0; k < length ; k++ ) {
            if (message.charAt(k) == '\r' ) {
                message.deleteCharAt(k);
                length --;
            }
        }
        if (LogWriter.needsLogging)
        	LogWriter.logMessage(LogWriter.TRACE_DEBUG, "[StringMsgParser]-> Parsing SIP Message:\n" + pmessage + "\n---END---\n");
 
        if (debugFlag ) {
            for (int k = 0 ; k < length; k++) {
                rawMessage1 = rawMessage1 + "[" + message.charAt(k) +"]";
            }
        }
        
        // The following can be written more efficiently in a single pass
        // but it is somewhat tricky.
        StringTokenizer tokenizer = new StringTokenizer 
			(message.toString(),'\n');
        StringBuffer cooked_message = new StringBuffer();
        while( tokenizer.hasMoreChars() ) {
            String nexttok = tokenizer.nextToken();
            // Ignore blank lines with leading spaces or tabs.
            if (nexttok.trim().equals("")) cooked_message.append("\n");
            else cooked_message.append(nexttok);
       }
        
        message1 = cooked_message.toString();
        length = message1.indexOf("\n\n") + 2;
        
        // Handle continuations - look for a space or a tab at the start
        // of the line and append it to the previous line.
        
        
        for ( int k = 0 ; k < length - 1 ;  ) {
            if (cooked_message.charAt(k) == '\n') {
                if ( cooked_message.charAt(k+1) == '\t' ||
                cooked_message.charAt(k+1) == ' ') {
                    cooked_message.deleteCharAt(k);
                    cooked_message.deleteCharAt(k);
                    length --;
                    length --;
                    if ( k == length) break;
                    continue;
                }
                if ( cooked_message.charAt(k+1) == '\n') {
                    cooked_message.insert(k,'\n');
                    length ++;
                    k ++;
                }
            }
            k++;
        }
        cooked_message.append("\n\n");
        
        
        // Separate the string out into substrings for
        // error reporting.
        currentMessage = cooked_message.toString();
        if (Parser.debug) Debug.println(currentMessage);
        bufferPointer = currentMessage.indexOf("\n\n") + 3 ;
        
        Message sipmsg = this.parseMessage(currentMessage);
        if (readBody && sipmsg.getContentLengthHeader() != null &&
        sipmsg.getContentLengthHeader().getContentLength() != 0) {
            this.contentLength =
            sipmsg.getContentLengthHeader().getContentLength();
            String body = this.getMessageBody();
            sipmsg.setMessageContent(body);
        }
        return sipmsg;
        
        
    }
    
    
    /** This is called repeatedly by parseMessage to parse
     * the contents of a message buffer. This assumes the message
     * already has continuations etc. taken care of.
     * prior to its being called.
     */
    private Message parseMessage(String currentMessage )
    throws  ParseException {
        // position line counter at the end of the
        // sip messages.        
        int sip_message_size = 0; // # of lines in the sip message
        Message sipmsg = null ;
        StringTokenizer tokenizer = new StringTokenizer
        (currentMessage,'\n');
        messageHeaders = new Vector(); // A list of headers for error reporting
        while( tokenizer.hasMoreChars() ) {
              String nexttok = tokenizer.nextToken();
              if (nexttok.equals("\n")) {
                  String nextnexttok = tokenizer.nextToken();
                  if (nextnexttok.equals("\n") ) {
                      break;
                  } else messageHeaders.addElement(nextnexttok);
              } else messageHeaders.addElement(nexttok);
              sip_message_size ++;
        }
        currentLine = 0;
        currentHeader = (String) messageHeaders.elementAt(currentLine);
        String firstLine = currentHeader;
        
        if (!firstLine.startsWith(SIPConstants.SIP_VERSION_STRING))  {
            sipmsg = new Request();
            try {
                RequestLine rl =
                new RequestLineParser(firstLine+ "\n").parse();
                ((Request) sipmsg).setRequestLine(rl);
            } catch (ParseException ex) {
                if (this.parseExceptionListener != null)
                    this.parseExceptionListener.handleException
                    (ex,sipmsg, new RequestLine().getClass(),
                    firstLine,currentMessage);
                else throw ex;
                
            }
        } else {
            sipmsg = new Response();
            try {
                StatusLine sl = new StatusLineParser(firstLine + "\n").parse();
                ((Response) sipmsg).setStatusLine(sl);
            } catch (ParseException ex) {
                if (this.parseExceptionListener != null)   {
                    this.parseExceptionListener.handleException
                    (ex,sipmsg,
                    new StatusLine().getClass(),
                    firstLine,currentMessage);
                } else throw ex;
                
            }
        }
        
        for (int i = 1;  i < messageHeaders.size(); i++){
            String hdrstring = (String) messageHeaders.elementAt(i);
            if (hdrstring == null || hdrstring.trim().equals("")) continue;
            HeaderParser hdrParser = null;
            try {
		//System.out.println(hdrstring + "\n");
                hdrParser = ParserFactory.createParser(hdrstring  + "\n" );
            } catch (ParseException ex)  {
                this.parseExceptionListener.handleException
                ( ex, sipmsg,  null , hdrstring,currentMessage);
                continue;
            }
            try {
                Header sipHeader = hdrParser.parse();
                sipmsg.attachHeader(sipHeader,false);
            } catch (ParseException ex) {
		ex.printStackTrace();
                if (this.parseExceptionListener != null) {
                    String hdrName = Lexer.getHeaderName(hdrstring);
                    Class hdrClass = NameMap.getClassFromName(hdrName);
                    
                    if (hdrClass == null) {
                        hdrClass =  ExtensionHeader.clazz;
                    }
                    this.parseExceptionListener.handleException
                    (ex,sipmsg, hdrClass,
                    hdrstring,currentMessage);
                    
                }
                
            }
        }
        
        return sipmsg;
        
    }
    
    
    
    
    
    
    /**
     * Parse an address (nameaddr or address spec)  and return and address
     * structure.
     * @param address is a String containing the address to be parsed.
     * @return a parsed address structure.
     * @since v1.0
     * @exception  ParseException when the address is badly formatted.
     */
    
    public Address parseAddress(String address)
    throws ParseException {
        AddressParser addressParser = new AddressParser(address);
        return addressParser.address();
    }
    
    
    
    /**
     * Parse a host:port and return a parsed structure.
     * @param hostport is a String containing the host:port to be parsed
     * @return a parsed address structure.
     * @since v1.0
     * @exception throws a ParseException when the address is badly formatted.
     */
    public HostPort parseHostPort(String hostport )
    throws ParseException {
        Lexer lexer = new Lexer("charLexer",hostport);
        return new HostNameParser(lexer).hostPort();
        
    }
    
    /**
     * Parse a host name and return a parsed structure.
     * @param host is a String containing the host name to be parsed
     * @return a parsed address structure.
     * @since v1.0
     * @exception throws a ParseException when the hostname is badly formatted.
     */
    public Host parseHost(String host )
    throws ParseException {
        Lexer lexer = new Lexer("charLexer",host);
        return new HostNameParser(lexer).host();
        
    }
    
    
    /**
     * Parse a telephone number return a parsed structure.
     * @param telphone_number is a String containing the telephone # to be parsed
     * @return a parsed address structure.
     * @since v1.0
     * @exception throws a ParseException when the address is badly formatted.
     */
    public TelephoneNumber parseTelephoneNumber(String telephone_number )
    throws ParseException {
        // Bug fix contributed by Will Scullin
        return new URLParser(telephone_number).parseTelephoneNumber();
        
    }
    
    
    /**
     * Parse a  SIP url from a string and return a URI structure for it.
     * @param url a String containing the URI structure to be parsed.
     * @return A parsed URI structure
     * @exception ParseException  if there was an error parsing the message.
     */
    
    public SipURI parseSIPUrl(String url)
    throws ParseException {
        try {
            return (SipURI) new URLParser(url).parse();
        } catch (ClassCastException ex) {
            throw new ParseException(url + " Not a SIP URL ",0);
        }
    }
    
    
    /**
     * Parse a  uri from a string and return a URI structure for it.
     * @param url a String containing the URI structure to be parsed.
     * @return A parsed URI structure
     * @exception ParseException  if there was an error parsing the message.
     */
    
    public URI parseUrl(String url)
    throws ParseException {
        return new URLParser(url).parse();
    }
    
    /**
     * Parse an individual SIP message header from a string.
     * @param header String containing the SIP header.
     * @return a Header structure.
     * @exception ParseException  if there was an error parsing the message.
     */
    public Header parseHeader(String header )
    throws ParseException {
        header += "\n\n";
        // Handle line folding.
        String nmessage = "";
        int counter = 0;
        // eat leading spaces and carriage returns (necessary??)
        int i = 0;
        while( header.charAt(i) == '\n' || header.charAt(i) == '\t'
        || header.charAt(i) == ' ') i++;
        for ( ; i < header.length(); i++) {
            if ( i < header.length() - 1 &&
            ( header.charAt(i) == '\n' && ( header.charAt(i+1) == '\t'
            || header.charAt(i+1) == ' ') ) ) {
                nmessage += ' ';
                i++;
            } else {
                nmessage += header.charAt(i);
            }
        }
        
        nmessage += "\n";
        
        HeaderParser hp = ParserFactory.createParser(nmessage);
        if (hp == null) throw new ParseException("could not create parser",0);
        return hp.parse();
    }
    
    /**
     * Parse the SIP Request Line
     * @param  requestLine a String  containing the request line to be parsed.
     * @return  a RequestLine structure that has the parsed RequestLine
     * @exception ParseException  if there was an error parsing the requestLine.
     */
    
    public RequestLine parseRequestLine( String requestLine)
    throws ParseException {
        requestLine += "\n";
        return new RequestLineParser(requestLine).parse();
    }
    
    /**
     * Parse the SIP Response message status line
     * @param statusLine a String containing the Status line to be parsed.
     * @return StatusLine class corresponding to message
     * @exception ParseException  if there was an error parsing
     * @see StatusLine
     */
    
    public StatusLine parseSIPStatusLine(String statusLine)
    throws ParseException {
        statusLine  += "\n";
        return new StatusLineParser(statusLine).parse();
    }
    
    
    
    /**
     * Get the current header.
     */
    public String getCurrentHeader() {
        return currentHeader;
    }
    
    
    /**
     * Get the current line number.
     */
    public int getCurrentLineNumber() { return currentLine; }
    
    
     // A little unit text.
    /*public static void main(String[] args) throws ParseException {
    	String messages[] = {
//    			"INVITE sip:littleguy@there.com:5060 SIP/2.0\r\n"+
//    			"Via: SIP/2.0/UDP 65.243.118.100:5050\r\n" +
//    			"From: M. Ranganathan  <sip:M.Ranganathan@sipbakeoff.com>;tag=1234\r\n"+
//    			"To: \"littleguy@there.com\" <sip:littleguy@there.com:5060> \r\n" +
//    			"Session-Expires: 1000;refresher=uac \r\n" +
//    			"Call-ID: Q2AboBsaGn9!?x6@sipbakeoff.com \r\n" +
//    			"CSeq: 1 INVITE \r\n" +
//    			"Content-Length: 0 \r\n\r\n"
    	};
    	
    	for (int i = 0; i < messages.length; i++) {
    		StringMsgParser smp = new StringMsgParser();

    		Message sipMessage = null;
    		try {
    			sipMessage = smp.parseSIPMessage(messages[i]);
    			System.out.println(sipMessage.encode());
    		} catch(Exception e) {
    			e.printStackTrace();
    		}

    	}

    }*/
     
    
    
}

