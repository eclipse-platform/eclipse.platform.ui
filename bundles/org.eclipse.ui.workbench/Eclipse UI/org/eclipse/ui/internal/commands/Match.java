/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.commands;

import org.eclipse.ui.internal.util.Util;

final class Match {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = Match.class.getName().hashCode();

	private String commandId;
	private int value;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private transient String string;
	
	Match(String commandId, int value) {	
		if (value < 0)
			throw new IllegalArgumentException();

		this.commandId = commandId;
		this.value = value;
	}

	public int compareTo(Object object) {
		Match match = (Match) object;
		int compareTo = Util.compare(commandId, match.commandId);
		
		if (compareTo == 0)
			compareTo = value - match.value;
					
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Match))
			return false;

		Match match = (Match) object;	
		boolean equals = true;
		equals &= Util.equals(commandId, match.commandId);
		equals &= value == match.value;
		return equals;
	}

	public String getCommandId() {
		return commandId;
	}
		
	public int getValue() {
		return value;	
	}
	
	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(commandId);
			hashCode = hashCode * HASH_FACTOR + value;			
			hashCodeComputed = true;
		}
			
		return hashCode;		
	}

	public String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append('[');
			stringBuffer.append(commandId);
			stringBuffer.append(',');
			stringBuffer.append(value);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}
	
		return string;			
	}
}
