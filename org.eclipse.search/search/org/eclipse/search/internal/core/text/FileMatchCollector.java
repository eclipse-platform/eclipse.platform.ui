/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.core.text;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Thomas Mäder
 *
 */
public class FileMatchCollector implements IMatchCollector {
	private ITextSearchResultCollector fCollector;
	private IResourceProxy fProxy;
	
	public FileMatchCollector(ITextSearchResultCollector collector, IResourceProxy proxy) {
		fCollector= collector;
		fProxy= proxy;
	}

	public void accept(String line, int start, int length, int lineNumber) throws InvocationTargetException {
		try {
			fCollector.accept(fProxy, line, start, length, lineNumber);
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		}
	}
}
