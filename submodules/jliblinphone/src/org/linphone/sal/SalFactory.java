/*
SalFactory.java
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
package org.linphone.sal;

abstract public class SalFactory {

	private static String defaulfFactory = "org.linphone.jlinphone.sal.jsr180.SalFactoryImpl";

	static SalFactory theSalFactory; 
	/**
	 * Indicate the name of the class used by this factory
	 * @param pathName
	 */
	public static void setFactoryClassName (String className) {
		defaulfFactory = className;
	}

	public static SalFactory instance() {
		try {
			if (theSalFactory == null) {
				Class lFactoryClass = Class.forName(defaulfFactory);
				theSalFactory = (SalFactory) lFactoryClass.newInstance();
			}
		} catch (Exception e) {
			System.err.println("cannot instanciate factory ["+defaulfFactory+"]");
		}
		return theSalFactory;
	}
	abstract public Sal createSal();
	
	/**
	 * create a Sall Address from a parcable string
	 * @param address in case of sip, <Display name> toto@linphone.org:5060
	 * @return
	 */
	abstract public SalAddress createSalAddress(String address) throws IllegalArgumentException ;
	
	abstract public SalAddress createSalAddress(String displayName, String username,String domain, int port) throws IllegalArgumentException ;
	
	abstract public SalMediaDescription createSalMediaDescription();
	
	abstract public  void setDebugMode(boolean enable);

}
