/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.internal.core.target;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class UrlUtil {
	
	public static IPath getTrailingPath(URL fullURL, URL startingURL) {
		IPath fullPath = new Path(fullURL.getPath());
		IPath startingPath = new Path(startingURL.getPath());
		int matchingCount = fullPath.matchingFirstSegments(startingPath);
		return fullPath.removeFirstSegments(matchingCount);
	}
	
	public static URL concat(String root, IPath end) throws MalformedURLException {
		return new URL(concatString(root, end));
	}

	private static String concatString(String root, IPath end) {
		if(end.isEmpty())
			return root;
		if(root.length() == 0)
			return end.toString();
		boolean rootHasTrailing = root.charAt(root.length() - 1) == Path.SEPARATOR;  //has trailing '/'
		boolean endHasLeading = end.isAbsolute();	// has leading '/'
			
		if(rootHasTrailing && endHasLeading) //http://mysite/ + /myFolder
			return root + end.toString().substring(1);	 // we have two seperators, drop one
			
		if(!rootHasTrailing && !endHasLeading) //http://mysite + myFolder
			return root + Path.SEPARATOR + end.toString();
				
		return root + end.toString();	//have one separator between the two, just concat end
	}
}
