/*
CallLogsField.java
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

import org.linphone.core.CallDirection;
import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneCallLog;
import org.linphone.core.LinphoneCore;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.ContextMenu;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;
import net.rim.device.api.ui.container.MainScreen;

public class CallLogsField extends SelectableListField implements TabFieldItem, LinphoneResource{

	final LinphoneCore mCore;
	private static ResourceBundle mRes = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);
	MenuItem mClearMenu;
	boolean isViewable = false;
	CallLogsField(LinphoneCore aCore,Listener aListener) {
		super (aListener);
		mCore = aCore;
		final Bitmap lOutBitmap = new Bitmap(this.getRowHeight(), this.getRowHeight());
		lOutBitmap.createAlpha(Bitmap.ALPHA_BITDEPTH_MONO);
		Bitmap.getBitmapResource("out_call.png").scaleInto( lOutBitmap
							,Bitmap.FILTER_LANCZOS);
		final Bitmap lInBitmap = new Bitmap(this.getRowHeight(), this.getRowHeight());
		lInBitmap.createAlpha(Bitmap.ALPHA_BITDEPTH_MONO);
		Bitmap.getBitmapResource("in_call.png").scaleInto( lInBitmap
							,Bitmap.FILTER_LANCZOS);
		
		mClearMenu = new MenuItem(mRes.getString(CLEAR_LOGS), 110, 10) {
			public void run() {
				mCore.clearCallLogs();
				refresh();
				CallLogsField.this.invalidate();
			}
		};
		
		setCallback(new ListFieldCallback() { 
		    public void drawListRow(ListField list, Graphics g, int index, int y, int w) { 
				if (list.getSelectedIndex() !=index) {
					g.setBackgroundColor(index%2==0?Color.LIGHTGRAY:Color.DARKGRAY);
					g.clear();
				}
				int lXCurrentPosistion=0;
		    	LinphoneCallLog lCallLog = (LinphoneCallLog) get(list,index);
		    	LinphoneAddress lAddressToDisplay;
		    	if (lCallLog.getDirection() == CallDirection.Incoming) {
		    		g.drawBitmap(0, y,lInBitmap.getWidth(),lInBitmap.getHeight(),lInBitmap, 0, 0);
		    		lAddressToDisplay=lCallLog.getFrom();
		    	} else {
		    		g.drawBitmap(0, y,lOutBitmap.getWidth(),lOutBitmap.getHeight(),lOutBitmap, 0, 0);
		    		lAddressToDisplay=lCallLog.getTo();
		    	}
		    	lXCurrentPosistion=lInBitmap.getWidth()+5;
		    	if (lAddressToDisplay.getDisplayName() != null) {
		    		g.setFont(Font.getDefault().derive(Font.BOLD));
		    		g.drawText(lAddressToDisplay.getDisplayName(),lXCurrentPosistion,y,0,w);
		    		lXCurrentPosistion+=g.getFont().getAdvance(lAddressToDisplay.getDisplayName())+g.getFont().getAdvance(" ");
		    		g.setFont(Font.getDefault());
		    	}
		    	g.drawText((lAddressToDisplay!=null?lAddressToDisplay.getUserName():"unknown"), lXCurrentPosistion,y,0,w);
		    	
		    } 
		    public Object get(ListField list, int index) {
		        return mCore.getCallLogs().elementAt(getLenth()-index-1); 
		    } 
		    public int indexOfList(ListField list, String prefix, int string) { 
		        return mCore.getCallLogs().indexOf(prefix, string); 
		    } 
		    public int getPreferredWidth(ListField list) { 
		        return Display.getWidth(); 
		    }
		    private int getLenth() {
		    	return  mCore.getCallLogs().size();
		    }
		});
		refresh();
	}
	public void refresh() {
		setSize(mCore.getCallLogs().size());
	}
	public void onSelected() {
		refresh();
	}
	public void onUnSelected() {
	}
	protected void makeContextMenu(ContextMenu contextMenu) {
		super.makeContextMenu(contextMenu);
		contextMenu.addItem(mClearMenu);
	}
	public boolean keyChar(char ch, int status, int time) {
		return super.keyChar(ch, status, time);
	}
}
