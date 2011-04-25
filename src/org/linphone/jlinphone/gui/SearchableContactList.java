/*
ContactList.java
Copyright (C) 2010  Belledonne Communications, Grenoble, France

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
package org.linphone.jlinphone.gui;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.pim.Contact;
import javax.microedition.pim.ContactList;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;
import javax.microedition.pim.PIMItem;

import net.rim.device.api.collection.util.SortedReadableList;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.KeywordFilterField;
import net.rim.device.api.ui.component.KeywordProvider;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;
import net.rim.device.api.util.Comparator;
import net.rim.device.api.util.StringUtilities;

class ContactListSource extends SortedReadableList implements KeywordProvider{
   
	static String getComparableString(Contact aContact) {
    	StringBuffer lcomparablestring = new StringBuffer();
    	String[] lContactArray = aContact.getStringArray(Contact.NAME,0);
    	if (lContactArray[Contact.NAME_GIVEN] != null) {
    		lcomparablestring.append(lContactArray[Contact.NAME_GIVEN]);
    	}
    	if (lContactArray[Contact.NAME_FAMILY] != null) {
    		lcomparablestring.append(lContactArray[Contact.NAME_FAMILY]);
    	}
		if (aContact.countValues(Contact.TEL) > 0) {
			lcomparablestring.append(aContact.getString(Contact.TEL, 0));
		}
		return lcomparablestring.toString();
    }   
	final static class ContactComparator implements Comparator
	    {   
	        public int compare(Object o1, Object o2)
	        {
	            if (o1 == null || o2 == null)
	              throw new IllegalArgumentException("Cannot compare null Contacts");
	            return getComparableString((Contact)o1).compareTo(getComparableString((Contact) o2));
	        }

	    }    
	ContactList mContacts;
	Vector mFlatContactList = new Vector();
	
	public ContactListSource() throws PIMException{
		super(new ContactComparator());
		mContacts = (ContactList) PIM.getInstance().openPIMList(PIM.CONTACT_LIST, PIM.READ_ONLY);
		Enumeration lContacts = mContacts.items(); 
		Contact lCurrent;
		while (lContacts.hasMoreElements()) {
			lCurrent=(Contact) lContacts.nextElement();
			for (int i=0;i<lCurrent.countValues(Contact.TEL);i++) {
				Contact lTargetContact = mContacts.createContact();
				lTargetContact.addStringArray(Contact.NAME, PIMItem.ATTR_NONE, lCurrent.getStringArray(Contact.NAME, 0));
				String lContactTel = lCurrent.getString(Contact.TEL, i);
				lTargetContact.addString(Contact.TEL, lCurrent.getAttributes(Contact.TEL, i), lContactTel);
				mFlatContactList.addElement(lTargetContact);
			}
		}
		
		//loadFrom(mContacts.items());
		loadFrom(mFlatContactList.elements());

	}

	public String[] getKeywords(Object element) {
	    return StringUtilities.stringToWords(ContactListSource.getComparableString((Contact) element));
	}
	ContactList getContactList() {
		return mContacts;
	}
}

public class SearchableContactList implements ListFieldCallback{
	interface Listener {
		public void onSelected(Contact selected);
	}
	final Listener mListener;
	

	ContactListSource mContactList;
	KeywordFilterField mKeywordFilterField = new KeywordFilterField() {
		protected boolean keyChar(char key, int status, int time) {
			if (key !='\n') {
				return super.keyChar(key, status, time);
			} else {
				return navigationClick(0,0);
			}
		}
		
		protected boolean navigationClick(int status, int time) {
			if (mListener != null ) {
				mListener.onSelected( (Contact) this.getCallback().get(this, getSelectedIndex()));
			}
			return true;
		}
	};
	
	public SearchableContactList(Listener aListener) throws PIMException {
		mListener = aListener;
		mContactList = new ContactListSource();
		mKeywordFilterField.setSourceList(mContactList, mContactList);
		mKeywordFilterField.setCallback(this);
	}
	public SearchableContactList() throws PIMException {
		this(null);
	}

	public KeywordFilterField getKeywordFilterField() {
		return mKeywordFilterField;
	}
	public void drawListRow(ListField listField, Graphics graphics, int index,
			int y, int width) {

		if (listField.getSelectedIndex() !=index) {
			graphics.setBackgroundColor(index%2==0?Color.LIGHTGRAY:Color.DARKGRAY);
			graphics.clear();
		}
		Contact lContact = (Contact) get(listField,index);
		String[] lContactNames = lContact.getStringArray(Contact.NAME, 0);
		int lCurrentX=0;
		graphics.setFont(Font.getDefault().derive(Font.BOLD));
		if (lContactNames[Contact.NAME_GIVEN] != null ) {
			graphics.drawText(lContactNames[Contact.NAME_GIVEN],0,y,0);
			lCurrentX=graphics.getFont().getAdvance(lContactNames[Contact.NAME_GIVEN]);
		}
		if (lCurrentX >0) {
			//Restore font
			graphics.setFont(Font.getDefault());
		}
		if (lContactNames[Contact.NAME_FAMILY] != null ) {
			graphics.drawText(lContactNames[Contact.NAME_FAMILY],lCurrentX + graphics.getFont().getAdvance(" "),y,0);
			lCurrentX+= graphics.getFont().getAdvance(" ") +graphics.getFont().getAdvance(lContactNames[Contact.NAME_FAMILY]);
		}
		if (lContact.countValues(Contact.TEL) > 0) {
			graphics.setFont(Font.getDefault().derive(Font.ITALIC,Font.getDefault().getHeight()-2));
			String lType =  "["+mContactList.getContactList().getAttributeLabel(lContact.getAttributes(Contact.TEL, 0))+"]";
			graphics.drawText(lType
								,lCurrentX + graphics.getFont().getAdvance(" ")
								,y,0);

			lCurrentX+=graphics.getFont().getAdvance(" ") + graphics.getFont().getAdvance(lType);
			
			String lContactTel = lContact.getString(Contact.TEL, 0);
			graphics.drawText(lContactTel
					,lCurrentX + graphics.getFont().getAdvance(" ")
					,y,0);
		}	
		
	}
	public Object get(ListField listField, int index) {
		return((KeywordFilterField) listField).getResultList().getAt(index);
	}
	public int getPreferredWidth(ListField listField) {
		return Display.getWidth(); 
	}
	public int indexOfList(ListField listField, String prefix, int start) {
		return -1;
	}
	
}
