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

final class Envelope implements Comparable {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = Envelope.class.getName().hashCode();	

	private String id;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	
	Envelope(String id) {
		this.id = id;
	}
	
	public int compareTo(Object object) {
		Envelope envelope = (Envelope) object;
		int compareTo = Util.compare(id, envelope.id);
		return compareTo;
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Envelope))
			return false;

		Envelope envelope = (Envelope) object;	
		boolean equals = true;
		equals &= Util.equals(id, envelope.id);
		return equals;		
	}

	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(id);
			hashCodeComputed = true;
		}
			
		return hashCode;		
	}
	
	public String toString() {
		return id;
	}

	String getId() {
		return id;	
	}
}
