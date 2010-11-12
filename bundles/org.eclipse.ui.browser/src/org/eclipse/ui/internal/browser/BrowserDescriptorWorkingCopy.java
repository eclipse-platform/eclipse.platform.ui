/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.browser;
/**
 * 
 */
public class BrowserDescriptorWorkingCopy extends BrowserDescriptor implements IBrowserDescriptorWorkingCopy {
	protected BrowserDescriptor browser;

	// creation
	public BrowserDescriptorWorkingCopy() {
		// do nothing
	}

	// working copy
	public BrowserDescriptorWorkingCopy(BrowserDescriptor browser) {
		this.browser = browser;
		setInternal(browser);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.browser.IBrowserDescriptorWorkingCopy#setName(java.lang.String)
	 */
	public void setName(String name) {
		if (name == null)
			throw new IllegalArgumentException();
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.browser.IBrowserDescriptorWorkingCopy#setLocation(java.lang.String)
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.browser.IBrowserDescriptorWorkingCopy#setParameters(java.lang.String)
	 */
	public void setParameters(String params) {
		this.parameters = params;
	}

	public boolean isWorkingCopy() {
		return true;
	}
	
	public IBrowserDescriptorWorkingCopy getWorkingCopy() {
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.browser.IBrowserDescriptorWorkingCopy#save()
	 */
	public IBrowserDescriptor save() {
		if (browser != null) {
			browser.setInternal(this);
		} else {
			browser = new BrowserDescriptor();
			browser.setInternal(this);
			BrowserManager.getInstance().addBrowser(browser);
		}
		return browser;
	}
}
