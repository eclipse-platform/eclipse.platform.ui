/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.roles;


/**
 * The Role is a set of information about the current static
 * working state.
 */
class Role {

	String name;
	String id;
	String[] patterns;
	
	boolean enabled = false;

	/**
	 * Create a new instance of the receiver.
	 * @param newName String
	 * @param newId String
	 * @param newPattern String - the patterns that plugin ids will be mapped to.
	 */
	Role(String newName, String newId, String[] newPatterns) {
		name = newName;
		id = newId;
		patterns = newPatterns;
	}

	/**
	 * Set the enabled state of this role.
	 * @param set boolean
	 */
	public void setEnabled(boolean set) {
		enabled = set;
	}
	
	/**
	 * Return whether or not this role is enabled.
	 * @return
	 */
	public boolean isEnabled(){
		return enabled;
	}
	
	/**
	 * Return whether or not one of the patterns matches the supplied value.
	 * @param value
	 * @return
	 */
	boolean patternMatches(String value){
		for(int i = 0; i < patterns.length; i++){
			if(value.matches(patterns[i]))
				return true;
		}
		return false;		
	}
}
