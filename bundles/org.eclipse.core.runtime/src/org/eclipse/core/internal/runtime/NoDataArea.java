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

public class NoDataArea extends DataArea {
	public boolean hasInstanceData() {
		return false;
	}
	public boolean isInstanceDataLocationInitiliazed() {
		return true;
	}
	public IPath getMetadataLocation() throws IllegalStateException {
		throw new IllegalStateException(Policy.bind("meta.noDataModeSpecified"));
	}
	public IPath getLogLocation() throws IllegalStateException {
		return getTemporaryLogLocation();
	}
	public void setInstanceDataLocation(IPath location) throws IllegalStateException {
		throw new IllegalStateException(Policy.bind("meta.noDataModeSpecified")); 
	}
	public IPath getInstanceDataLocation() throws IllegalStateException {
		throw new IllegalStateException(Policy.bind("meta.noDataModeSpecified"));
	}	
}
