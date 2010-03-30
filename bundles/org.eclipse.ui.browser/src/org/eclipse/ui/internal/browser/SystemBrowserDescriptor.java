/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.browser;

public class SystemBrowserDescriptor implements IBrowserDescriptor {
	public String getName() {
		return Messages.prefSystemBrowser;
	}

	public String getLocation() {
		return null;
	}

	public String getParameters() {
		return null;
	}

	public void delete() {
		// ignore
	}

	public boolean isWorkingCopy() {
		return false;
	}

	public IBrowserDescriptorWorkingCopy getWorkingCopy() {
		return null;
	}
	
	public boolean equals(Object obj) {
		return obj instanceof SystemBrowserDescriptor;
	}
}