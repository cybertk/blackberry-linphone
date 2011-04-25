/*
TabField.java
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

import java.util.Vector;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;

import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.Background;
import net.rim.device.api.ui.decor.BackgroundFactory;

public class TabField extends VerticalFieldManager {
	HorizontalFieldManager mTabController = new HorizontalFieldManager();
	Vector mTabFields = new Vector();
	Field mCurrentField;
	int mCurrentIndex=0;
	int mDefault=0;
	int PADDING=0;
	static final public int SIZE=40;
	Background  CONTROL_NORMAL_BG=BackgroundFactory.createSolidBackground(Color.DARKGRAY);
	Background  CONTROL_ACTIVE_BG=BackgroundFactory.createSolidBackground(Color.LIGHTGREY);
	public TabField() {
		add(mTabController);
		add(new SeparatorField());
	}
	void setDefault(int index) {
		mDefault=index;
		if (mDefault <= mTabFields.size()-1) {
			display(mDefault);
		}
		
	}
	public void display(int index) {
		if (mCurrentField !=null) {
			if (mCurrentField instanceof TabFieldItem) {
				((TabFieldItem) mCurrentField).onUnSelected();
			};
			delete(mCurrentField);
		}
		mCurrentField=(Field) mTabFields.elementAt(index);
		add(mCurrentField);
		if (mCurrentField instanceof TabFieldItem) {
			((TabFieldItem) mCurrentField).onSelected();
		};
		setDirty(true);
		mTabController.getField(mCurrentIndex).setBackground(CONTROL_NORMAL_BG);
		mTabController.getField(mCurrentIndex).setDirty(true);
		mTabController.getField(index).setBackground(CONTROL_ACTIVE_BG);
		mTabController.getField(index).setDirty(true);
		mCurrentIndex = index;
		mCurrentField.setFocus();
	}
	public void addTab(Bitmap aBitmap, final Field aTabField) {
		Bitmap lBitmapScaled = new Bitmap(SIZE, SIZE);
		
		lBitmapScaled.createAlpha(Bitmap.ALPHA_BITDEPTH_MONO);
	
		aBitmap.scaleInto(	0
							,0
							,aBitmap.getWidth()
							,aBitmap.getHeight()
							,lBitmapScaled
							,PADDING
							,PADDING
							,SIZE-2*PADDING
							,SIZE-2*PADDING
							,Bitmap.FILTER_LANCZOS);
		BitmapField lButton = new BitmapField(lBitmapScaled,Field.FOCUSABLE) {

			protected boolean navigationUnclick(int status, int time) {
				if (aTabField == null) {
					return  false; //nop
				}
				display(getIndex());
				return true;
				
			}
		};
		lButton.setBackground(CONTROL_NORMAL_BG);
		lButton.setSpace(10, 10);
		mTabController.add(lButton);
		mTabFields.addElement(aTabField);

		if (mDefault == mTabFields.size()-1 && aTabField!=null) {
			display(mDefault);
			//this.add(aTabField);
			//mCurrentField=aTabField;
		}
	}
	protected boolean keyChar(char ch, int status, int time) {
		if (mCurrentField != null && mCurrentField instanceof TabFieldItem) { 
			return ((TabFieldItem)mCurrentField).keyChar(ch, status, time);
		} else {
			return super.keyChar(ch, status, time);
		}
	}
	
}
