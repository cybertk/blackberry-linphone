/*
SettingField.java
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

import org.linphone.jlinphone.gui.SettingsScreen.SettingsFieldContent;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.VerticalFieldManager;

public class SettingField extends VerticalFieldManager implements LinphoneResource{
	private static ResourceBundle mRes = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);
	public SettingField(final SettingsFieldContent aContentSettings) {
		add(aContentSettings.getRootField());
		add( new SeparatorField());
		ButtonField lButtonField = new ButtonField(mRes.getString(SAVE),Field.FOCUSABLE);
		lButtonField.setRunnable(new Runnable() {
			public void run() {
				aContentSettings.save();
				Dialog.alert("Saved");
			}
		});
		add (lButtonField);
	}
}
