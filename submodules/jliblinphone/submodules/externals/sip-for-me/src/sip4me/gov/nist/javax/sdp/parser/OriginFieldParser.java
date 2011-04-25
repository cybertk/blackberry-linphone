
package sip4me.gov.nist.javax.sdp.parser;
import sip4me.gov.nist.core.Host;
import sip4me.gov.nist.core.HostNameParser;
import sip4me.gov.nist.core.LexerCore;
import sip4me.gov.nist.core.ParseException;
import sip4me.gov.nist.core.Token;
import sip4me.gov.nist.javax.sdp.fields.OriginField;
import sip4me.gov.nist.javax.sdp.fields.SDPField;
/**
 *
 * @author  deruelle
 * @version 1.0
 */
public class OriginFieldParser extends SDPParser {

    /** Creates new OriginFieldParser */
    public OriginFieldParser(String originField) {
        lexer = new Lexer("charLexer",originField);
    }

    protected OriginFieldParser() {
	super();
    }
    
    public OriginField originField() throws ParseException  {
        try{
            OriginField originField=new OriginField();
            
            lexer.match ('o');
            lexer.SPorHT();
            lexer.match('=');
            lexer.SPorHT();
            
            lexer.match(LexerCore.ID);
            Token userName= lexer.getNextToken();
            originField.setUsername(userName.getTokenValue());
            this.lexer.SPorHT();
            
            lexer.match(LexerCore.ID);
            Token sessionId= lexer.getNextToken();
            originField.setSessionId(sessionId.getTokenValue());
            this.lexer.SPorHT();
            
            lexer.match(LexerCore.ID);
            Token sessionVersion= lexer.getNextToken();
            originField.setSessionVersion(Long.parseLong(sessionVersion
					.getTokenValue()));
            this.lexer.SPorHT();
            
            lexer.match(LexerCore.ID);
            Token networkType=lexer.getNextToken();
            originField.setNettype(networkType.getTokenValue());
            this.lexer.SPorHT();
            
            lexer.match(LexerCore.ID);
            Token addressType=lexer.getNextToken();
            originField.setAddrtype(addressType.getTokenValue());
            this.lexer.SPorHT();
            
            String host=lexer.getRest();
	    Lexer lexer = new Lexer("charLexer",host);
            HostNameParser hostNameParser=new HostNameParser (lexer);
            Host h=hostNameParser.host();
            originField.setAddress(h);
            
            return originField;
        }
        catch(Exception e) {
            throw new ParseException(lexer.getBuffer(),lexer.getPtr());
        }
    }
    
    public SDPField parse() throws ParseException {
 	return this.originField();
    }

    /*
    public static void main(String[] args) throws ParseException {
        String origin[] = {
	    "o=4855 13760799956958020 13760799956958020 IN IP4 166.35.224.216\r\n" ,
            "o=mhandley 2890844526 2890842807 IN IP4 126.16.64.4\n",
            "o=UserB 2890844527 2890844527 IN IP4 everywhere.com\n",
            "o=UserA 2890844526 2890844526 IN IP4 here.com\n",
                        "o=IFAXTERMINAL01 2890844527 2890844527 IN IP4 ift.here.com\n",
                        "o=GATEWAY1 2890844527 2890844527 IN IP4 gatewayone.wcom.com\n",
                        "o=- 2890844527 2890844527 IN IP4 gatewayone.wcom.com\n"
                };

	    for (int i = 0; i < origin.length; i++) {
	       OriginFieldParser originFieldParser=new OriginFieldParser(
                origin[i] );
	        OriginField originField=originFieldParser.originField();
                System.out.println("toParse :" + origin[i]);
		System.out.println("encoded: " +originField.encode());
	    }

	}
	*/

}
