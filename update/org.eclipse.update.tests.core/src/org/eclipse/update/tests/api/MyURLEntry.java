/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.tests.api;
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
