/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.views.markers;
/**
 * @since 3.4
 *	Mock Class needed for testing Sort
 */
public class MockMarkerEntry extends MarkerEntry{
	/**
	 * 
	 */
	public String name;
	/**
	 * @param name
	 */
	public MockMarkerEntry(String name) {
		super(null);
		this.name=name;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return name.equals(((MockMarkerEntry)obj).name);
	}
}