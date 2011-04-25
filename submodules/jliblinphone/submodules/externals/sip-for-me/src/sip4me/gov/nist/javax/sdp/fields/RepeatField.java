/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package sip4me.gov.nist.javax.sdp.fields;
import java.util.Vector;

import sip4me.gov.nist.core.Separators;
import sip4me.gov.nist.javax.sdp.SdpException;
import sip4me.gov.nist.javax.sdp.SdpParseException;

/**
* Repeat SDP Field (part of the time field).
*
*@version  JSR141-PUBLIC-REVIEW (subject to change).
*
*@author Olivier Deruelle <deruelle@antd.nist.gov>
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class RepeatField  extends SDPField {
	protected TypedTime repeatInterval;
	protected TypedTime activeDuration;
	protected SDPObjectList offsets;

	public Object clone() {
		RepeatField retval = new RepeatField();
		if (this.repeatInterval != null) 
			retval.repeatInterval = 
			(TypedTime) this.repeatInterval.clone();
		if (this.activeDuration != null) 
			retval.activeDuration = 
			(TypedTime)this.activeDuration.clone();
		retval.offsets = 
			(SDPObjectList) this.offsets.clone();
		return retval;
	}
			
	public RepeatField() { 
		super(REPEAT_FIELD); 
	        offsets = new SDPObjectList();
	}

	
	public void setRepeatInterval( TypedTime interval) 
	{ repeatInterval = interval; }



	public void setActiveDuration( TypedTime duration)  
	{ activeDuration = duration; }


	public void addOffset(TypedTime offset) {
	   offsets.addElement(offset);
	}

	
	public Vector getOffsets() 
	{ return offsets; }

      /** Returns the "repeat interval" in seconds.
     * @throws SdpParseException
     * @return the "repeat interval" in seconds.
     */    
    public int getRepeatInterval()
    throws SdpParseException {
        if (repeatInterval ==null) return -1;
        else {
            return repeatInterval.getTime();
        }
    }
    
    /** Set the "repeat interval" in seconds.
     * @param repeatInterval the "repeat interval" in seconds.
     * @throws SdpException if repeatInterval is <0
     */    
    public void setRepeatInterval(int repeatInterval)
    throws SdpException {
        if (repeatInterval <0 ) 
		throw new SdpException("The repeat interval is <0");
        else {
              if (this.repeatInterval==null) 
			this.repeatInterval=new TypedTime();
              this.repeatInterval.setTime(repeatInterval);
        }
    }
    
    /** Returns the "active duration" in seconds.
     * @throws SdpParseException
     * @return the "active duration" in seconds.
     */    
    public int getActiveDuration()
    throws SdpParseException {
        if (activeDuration ==null) return -1;
        else {
            return activeDuration.getTime();
        }
    }
    
    /** Sets the "active duration" in seconds.
     * @param activeDuration the "active duration" in seconds.
     * @throws SdpException if the active duration is <0
     */    
    public void setActiveDuration(int activeDuration)
    throws SdpException {
        if (activeDuration <0 ) 
		throw new SdpException("The active Duration is <0");
        else {
              if (this.activeDuration ==null) 
			this.activeDuration =new TypedTime();
              this.activeDuration.setTime(activeDuration);
        }
    }
    
    /** Returns the list of offsets. These are relative to the start-time given
     * in the Time object (t=
     *     field) with which this RepeatTime is associated.
     * @throws SdpParseException
     * @return the list of offsets
     */    
    public int[] getOffsetArray()
    throws SdpParseException {
       int[] result= new int[offsets.size()];
       for (int i=0;i<offsets.size();i++) {
            TypedTime typedTime=(TypedTime)offsets.elementAt(i);
            result[i]=typedTime.getTime();
       }
       return result;
    }
    
    /** Set the list of offsets. These are relative to the start-time given in the 
     * Time object (t=
     *     field) with which this RepeatTime is associated.
     * @param offsets array of repeat time offsets
     * @throws SdpException
     */    
    public void setOffsetArray(int[] offsets)
    throws SdpException {
        for (int i=0;i<offsets.length;i++) {
            TypedTime typedTime=new TypedTime();
            typedTime.setTime(offsets[i]);
            addOffset(typedTime);
        }
        
    }
    
/** Returns whether the field will be output as a typed time or a integer value.
 *
 *     Typed time is formatted as an integer followed by a unit character. The unit indicates an
 *     appropriate multiplier for the integer.
 *
 *     The following unit types are allowed.
 *          d - days (86400 seconds)
 *          h - hours (3600 seconds)
 *          m - minutes (60 seconds)
 *          s - seconds ( 1 seconds)
 * @throws SdpParseException
 * @return true, if the field will be output as a typed time; false, if as an integer value.
 */    
    public boolean getTypedTime()
    throws SdpParseException{
        return true;
    }
    
/** Sets whether the field will be output as a typed time or a integer value.
 *
 *     Typed time is formatted as an integer followed by a unit character. The unit indicates an
 *     appropriate multiplier for the integer.
 *
 *     The following unit types are allowed.
 *          d - days (86400 seconds)
 *          h - hours (3600 seconds)
 *          m - minutes (60 seconds)
 *          s - seconds ( 1 seconds)
 * @param typedTime typedTime - if set true, the start and stop times will be output in an optimal typed
 *          time format; if false, the times will be output as integers.
 */    
    public void setTypedTime(boolean typedTime) {
        
    }

	public String encode() {
		String retval =  REPEAT_FIELD + repeatInterval.encode() 
			+ Separators.SP +
			activeDuration.encode();
		for (int i = 0; i < offsets.size(); i++ ) {
		   TypedTime off = (TypedTime) offsets.elementAt(i);
		   retval += Separators.SP + off.encode();
		}
		retval += Separators.NEWLINE;
		return retval;
	}


}
