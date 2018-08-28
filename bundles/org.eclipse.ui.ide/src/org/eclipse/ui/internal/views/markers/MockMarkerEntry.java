/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return name.equals(((MockMarkerEntry)obj).name);
	}
}