/*
 * AttributeFieldParser.java
 *
 * Created on February 19, 2002, 10:09 AM
 */

package sip4me.gov.nist.javax.sdp.parser;
import sip4me.gov.nist.core.NameValue;
import sip4me.gov.nist.core.ParseException;
import sip4me.gov.nist.javax.sdp.fields.AttributeField;
import sip4me.gov.nist.javax.sdp.fields.SDPField;

/**
 *
 * @author  Olivier Deruelle
 * @version 1.0
 */
public class AttributeFieldParser extends SDPParser {

    /** Creates new AttributeFieldParser */
    public AttributeFieldParser(String attributeField) {
       
	this.lexer = new Lexer("charLexer",attributeField);
    }

    protected AttributeFieldParser() {

   }



    public AttributeField attributeField() throws ParseException  {
        try{
		AttributeField attributeField = new AttributeField();
			
                this.lexer.match ('a');
          
		this.lexer.SPorHT();
		this.lexer.match('=');
          
		this.lexer.SPorHT();
                
                  
		NameValue nameValue = new NameValue();

		int ptr =   this.lexer.markInputPosition();
	        try {
                  String name=lexer.getNextToken(':');
		  this.lexer.consume(1);
		  String value = lexer.getRest();
		  nameValue = new NameValue
				(name.trim(),value.trim());
		} catch (ParseException ex) {
		   this.lexer.rewindInputPosition(ptr);
		   String rest = this.lexer.getRest();
		   if ( rest == null) 
			throw new ParseException(this.lexer.getBuffer(),
					this.lexer.getPtr());
                   nameValue=new NameValue(rest.trim(),null);
		}
                attributeField.setAttribute(nameValue);
               
                this.lexer.SPorHT();
               
		return attributeField;
        }
        catch(Exception e) {
            e.printStackTrace();
            throw new ParseException(e.getMessage(),0);
        }
    }

    public  SDPField parse() 
	throws ParseException 
	{ return this.attributeField(); }

/**
	public static void main(String[] args) throws ParseException {
	    String attribute[] = {
			"a=rtpmap:0 PCMU/8000\n",
			"a=rtpmap:31 LPC\n",
			"a=rtpmap:0 PCMU/8000\n",
                        "a=rtpmap:4 G723/8000\n",
                        "a=rtpmap:18 G729A/8000\n",
                        "a=ptime:30\n",
                        "a=orient:landscape\n",
                        "a=recvonly\n",
                        "a=T38FaxVersion:0\n",
                        "a=T38maxBitRate:14400\n",
                        "a=T38FaxFillBitRemoval:0\n",
                        "a=T38FaxTranscodingMMR:0\n",
                        "a=T38FaxTranscodingJBIG:0\n",
                        "a=T38FaxRateManagement:transferredTCF\n",
                        "a=T38FaxMaxBuffer:72\n",
                        "a=T38FaxMaxDatagram:316\n",
                        "a=T38FaxUdpEC:t38UDPRedundancy\n"
                };

	    for (int i = 0; i < attribute.length; i++) {
	       	AttributeFieldParser attributeFieldParser = new AttributeFieldParser(
                attribute[i] );
		AttributeField attributeField= attributeFieldParser.attributeField();
		System.out.println("encoded: " + attributeField.encode());
	    }

	}
**/


    
    
}
