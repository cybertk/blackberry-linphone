/*
AboutScreen.java
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

import net.rim.blackberry.api.browser.Browser;
import net.rim.blackberry.api.browser.BrowserSession;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.component.TextField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.BackgroundFactory;
import net.rim.device.api.ui.decor.Border;
import net.rim.device.api.ui.decor.BorderFactory;

public class AboutScreen extends MainScreen implements LinphoneResource{
	static final String LINPHONE_WWW="http://www.linphone.org";
	private static ResourceBundle mRes = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);
	public AboutScreen() {
		((VerticalFieldManager)getMainManager()).setBackground(BackgroundFactory.createSolidBackground(Color.LIGHTGREY));
		Border lBorder = BorderFactory.createSimpleBorder(new XYEdges(40,20,20,20) ); 
		
		setBorder(lBorder);
		BitmapField lLinphonebanner = new BitmapField(Bitmap.getBitmapResource("linphone-banner.png"),Field.FIELD_HCENTER|Field.FIELD_VCENTER );
		add(new TextField(Field.NON_FOCUSABLE));
		add(lLinphonebanner);
		add(new RichTextField("Linphone "+ApplicationDescriptor.currentApplicationDescriptor().getVersion()+mRes.getString(ABOUT_STRING),Field.FIELD_HCENTER|Field.FIELD_VCENTER|RichTextField.TEXT_ALIGN_HCENTER|Field.NON_FOCUSABLE));
		
		ButtonField lLinphoneAddress = new ButtonField(LINPHONE_WWW,Field.FIELD_VCENTER|Field.FIELD_HCENTER) {
			protected void paint(Graphics graphics){
				graphics.setColor(Color.BLUE);
		        graphics.drawText(LINPHONE_WWW, 0, 0);
		    }
			protected void paintBackground(Graphics graphics) {}
		};
		lLinphoneAddress.setRunnable(new Runnable() {
			public void run() {
				BrowserSession lBrowserSession = Browser.getDefaultSession();
				lBrowserSession.displayPage(LINPHONE_WWW);
			}
		});
		lLinphoneAddress.setBorder(BorderFactory.createSimpleBorder(new XYEdges(0,0,0,0)));
		lLinphoneAddress.setFont(Font.getDefault().derive(Font.UNDERLINED));
		add(lLinphoneAddress);
		add(new LabelField("© 2010 Belledonne Communications",Field.FIELD_VCENTER|Field.FIELD_HCENTER));
	}
}
