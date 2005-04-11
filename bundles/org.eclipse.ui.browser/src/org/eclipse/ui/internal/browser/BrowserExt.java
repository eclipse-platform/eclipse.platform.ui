/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.ui.internal.browser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.browser.BrowserFactory;
import org.eclipse.ui.browser.IWebBrowser;
/**
 * 
 * @since 1.0
 */
public class BrowserExt implements IBrowserExt {
	private IConfigurationElement element;
	private BrowserFactory delegate;

	/**
	 * BrowserExt constructor comment.
	 */
	public BrowserExt(IConfigurationElement element) {
		super();
		this.element = element;
	}

	/**
	 * Returns the id of this browser.
	 *
	 * @return java.lang.String
	 */
	public String getId() {
		return element.getAttribute("id");
	}

	public String getName() {
		String label = element.getAttribute("name");
		if (label == null)
			return "n/a";
		return label;
	}

	public String getParameters() {
		return element.getAttribute("parameters");
	}

	public String getExecutable() {
		return element.getAttribute("executable");
	}

	public String getOS() {
		String os = element.getAttribute("os");
		if (os == null)
			os = "";
		return os;
	}

	public String[] getDefaultLocations() {
		List list = new ArrayList();
		IConfigurationElement[] children = element.getChildren("location");
		if (children != null) {
			int size = children.length;
			for (int i = 0; i < size; i++) {
				list.add(children[i].getValue());
			}
		}
		
		String[] s = new String[list.size()];
		list.toArray(s);
		return s;
	}

	protected BrowserFactory getDelegate() {
		if (delegate == null) {
			try {
				delegate = (BrowserFactory) element.createExecutableExtension("factoryclass");
			} catch (Exception e) {
				Trace.trace(Trace.SEVERE, "Could not create delegate" + toString() + ": " + e.getMessage());
			}
		}
		return delegate;
	}
	
	/**
	 * Checks whether the factory can work on the user system.
	 * 
	 * @return false if the factory cannot work on this system; for example the
	 *    required native browser required by browser adapters that it
	 *    creates is not installed
	 */
	public boolean isAvailable() {
		try {
			return getDelegate().isAvailable();
		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Error calling delegate " + toString() + ": " + e.getMessage());
			return false;
		}
	}

	/**
	 * Obtains a new instance of a web browser.
	 * 
	 * @return instance of IBrowser
	 */
	public IWebBrowser createBrowser(String id, String location, String parameters) {
		try {
			return getDelegate().createBrowser(id, location, parameters);
		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Error calling delegate " + toString() + ": " + e.getMessage());
			return null;
		}
	}

	/**
	 * Return a string representation of this object.
	 * 
	 * @return java.lang.String
	 */
	public String toString() {
		String s = "BrowserExt: " + getId() + ", " + getName() + ", " + getOS() + ", " + getExecutable() + ", " + getParameters() + ", ";
		String[] locations = getDefaultLocations();
		if (locations != null) {
			int size = locations.length;
			for (int i = 0; i < size; i++) {
				s += locations[i] + ";";
			}
		}
		return s;
	}
}