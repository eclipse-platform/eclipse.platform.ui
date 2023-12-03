/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.browser;

import org.eclipse.ui.IMemento;
public class BrowserDescriptor implements IBrowserDescriptor {
	private static final String MEMENTO_NAME = "name"; //$NON-NLS-1$
	private static final String MEMENTO_LOCATION = "location"; //$NON-NLS-1$
	private static final String MEMENTO_PARAMETERS = "parameters"; //$NON-NLS-1$

	protected String name;
	protected String location;
	protected String parameters;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getLocation() {
		return location;
	}

	@Override
	public String getParameters() {
		return parameters;
	}

	@Override
	public void delete() {
		BrowserManager.getInstance().removeWebBrowser(this);
	}

	@Override
	public boolean isWorkingCopy() {
		return false;
	}

	@Override
	public IBrowserDescriptorWorkingCopy getWorkingCopy() {
		return new BrowserDescriptorWorkingCopy(this);
	}

	protected void setInternal(IBrowserDescriptor browser) {
		name = browser.getName();
		location = browser.getLocation();
		parameters = browser.getParameters();
	}

	protected void save(IMemento memento) {
		memento.putString(MEMENTO_NAME, name);
		memento.putString(MEMENTO_LOCATION, location);
		memento.putString(MEMENTO_PARAMETERS, parameters);
	}

	protected void load(IMemento memento) {
		name = memento.getString(MEMENTO_NAME);
		location = memento.getString(MEMENTO_LOCATION);
		parameters = memento.getString(MEMENTO_PARAMETERS);
	}

	@Override
	public String toString() {
		return "External Web browser: " + getName() + " / " + getLocation() + " / " + getParameters();   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
	}
}
