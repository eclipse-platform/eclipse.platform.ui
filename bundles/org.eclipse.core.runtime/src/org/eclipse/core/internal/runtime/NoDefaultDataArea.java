/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.runtime;

import org.eclipse.core.runtime.IPath;

public class NoDefaultDataArea extends DataArea {
	private boolean locationSet = false;
	
	protected void assertLocationInitialized() throws IllegalStateException {
		if (locationSet)
			super.assertLocationInitialized();
		else 
			throw new IllegalStateException(Policy.bind("meta.instanceDataUnspecified"));
	}
	public void setInstanceDataLocation(IPath loc) throws IllegalStateException {
		locationSet = true;
		super.setInstanceDataLocation(loc);
	}
}
