package org.eclipse.core.runtime.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * An object which represents the a named URL in a component or configuration
 * manifest.
 * <p>
 * This class may be instantiated and further subclassed.
 * </p>
 */

public class URLModel extends PluginModelObject {
	// DTD properties (included in install manifest)
	private String url = null;

/**
 * Returns the URL specification.
 *
 * @return the URL specification or <code>null</code>.
 */
public String getURL() {
	return url;
}

/**
 * Sets the URL specification.
 * This object must not be read-only.
 *
 * @param value the URL specification.
 *		May be <code>null</code>.
 */
public void setURL(String value) {
	assertIsWriteable();
	url = value;
}

}
