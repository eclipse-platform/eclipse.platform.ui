package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.net.URL;


/**
 * Interface for information that can have a short description as a text
 * and a long one in a URL.
 */
public interface IInfo {
	String getText();
	URL getURL();
}

