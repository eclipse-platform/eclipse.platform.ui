/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.ui.internal.browser;
import org.eclipse.help.ui.browser.IBrowserFactory;
class BrowserDescriptor {
	private String browserID;
	private String browserLabel;
	private IBrowserFactory factory;
	/**
	 * @param id ID of a browser as specified in plugin.xml
	 * @param label name of the browser
	 * @param factory the factory that creates instances
	 *  of this browser
	 */
	public BrowserDescriptor(String id, String label, IBrowserFactory factory) {
		this.browserID = id;
		this.browserLabel = label;
		this.factory = factory;
	}
	public String getID() {
		return browserID;
	}
	public String getLabel() {
		return browserLabel;
	}
	public IBrowserFactory getFactory() {
		return factory;
	}
}