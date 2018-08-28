/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.browser;

public class SystemBrowserDescriptor implements IBrowserDescriptor {
	@Override
	public String getName() {
		return Messages.prefSystemBrowser;
	}

	@Override
	public String getLocation() {
		return null;
	}

	@Override
	public String getParameters() {
		return null;
	}

	@Override
	public void delete() {
		// ignore
	}

	@Override
	public boolean isWorkingCopy() {
		return false;
	}

	@Override
	public IBrowserDescriptorWorkingCopy getWorkingCopy() {
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof SystemBrowserDescriptor;
	}
}