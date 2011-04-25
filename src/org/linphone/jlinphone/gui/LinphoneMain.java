/*
LinphoneMain.java
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


import net.rim.device.api.ui.UiApplication;

public class LinphoneMain extends UiApplication{
	 /**
     * Entry point for application
     * @param args Command line arguments (not used)
     */ 
    public static void main(String[] args)
    {

    	// Create a new instance of the application and make the currently
        // running thread the application's event dispatch thread.
    	LinphoneMain theApp = new LinphoneMain();       
    	theApp.enterEventDispatcher();
    }
    

    /**
     * Creates a new HelloWorldDemo object
     */
    public LinphoneMain()
    {        
        // Push a screen onto the UI stack for rendering.
        pushScreen(new LinphoneScreen());
    }    

}
