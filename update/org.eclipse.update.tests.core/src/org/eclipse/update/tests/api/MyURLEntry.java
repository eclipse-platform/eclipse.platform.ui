package org.eclipse.update.tests.api;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.update.core.URLEntry;

/**
 * Wrapper around URLEntry
 */
public class MyURLEntry extends URLEntry {
	
	public MyURLEntry(String annotation, URL url) throws MalformedURLException {
		super();
		setAnnotation(annotation);
		if (url!=null) {
			setURLString(url.toExternalForm());
			resolve(null,null);
		}
	}

}
