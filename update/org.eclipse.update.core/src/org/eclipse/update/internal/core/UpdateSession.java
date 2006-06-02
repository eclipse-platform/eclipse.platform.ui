/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/

package org.eclipse.update.internal.core;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class UpdateSession {
	
	private Set visitedURLs = new HashSet();
	
	UpdateSession() {
	}
	
	public boolean isVisited(URL url) {
		return visitedURLs.contains(url.toExternalForm());
	}

	public void markVisited(URL url) {
		visitedURLs.add(url.toExternalForm());
	}
	
	public void reset() {
		visitedURLs.clear();
	}

}
