package org.eclipse.update.core;

import java.net.URL;


/**
 * Interface for information that can have a short description as a text
 * and a long one in a URL.
 */
public interface IInfo {
	String getText();
	URL getURL();
}

