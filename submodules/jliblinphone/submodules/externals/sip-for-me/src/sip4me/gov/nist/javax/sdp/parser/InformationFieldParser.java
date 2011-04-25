/*
 * InformationFieldParser.java
 *
 * Created on February 19, 2002, 5:28 PM
 */

package sip4me.gov.nist.javax.sdp.parser;
import sip4me.gov.nist.core.ParseException;
import sip4me.gov.nist.javax.sdp.fields.InformationField;
import sip4me.gov.nist.javax.sdp.fields.SDPField;
/**
 *
 * @author  deruelle
 * @version 1.0
 */
public class InformationFieldParser extends SDPParser {

    /** Creates new InformationFieldParser */
    public InformationFieldParser(String informationField) {
	this.lexer = new Lexer("charLexer",informationField);
    }
    
    protected InformationFieldParser() {
	super();
    }
    
 
    
    public InformationField informationField() throws ParseException  {
        try{
            this.lexer.match ('i');
            this.lexer.SPorHT();
            this.lexer.match('=');
            this.lexer.SPorHT();
            
            InformationField informationField=new InformationField();
            String rest= lexer.getRest(); 
            informationField.setInformation(rest.trim());
            
            return informationField;
        }
        catch(Exception e) {
            throw new ParseException(lexer.getBuffer(),lexer.getPtr());
        }  
    }

    public SDPField parse() throws ParseException { 
	return this.informationField();
    }
	
    
/**
    public static void main(String[] args) throws ParseException {
	    String information[] = {
			"i=A Seminar on the session description protocol\n"
                };

	    for (int i = 0; i < information.length; i++) {
	       InformationFieldParser informationFieldParser=new InformationFieldParser(
                information[i] );
		InformationField informationField=
                    informationFieldParser.informationField();
                
		System.out.println("encoded: " +informationField.encode());
	    }

	}
**/



}
