/*****************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *****************************************************************************/

package org.eclipse.jface.text.formatter;

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of <code>IFormattingContext</code>.
 * 
 * @since 3.0
 */
public class FormattingContext implements IFormattingContext {

	/** Map to store the properties */
	private final Map fMap= new HashMap();

	/*
	 * @see org.eclipse.jface.text.formatter.IFormattingContext#setProperty(java.lang.Object, java.lang.Object)
	 */
	public void setProperty(Object key, Object property) {
		fMap.put(key, property);
	}

	/*
	 * @see org.eclipse.jface.text.formatter.IFormattingContext#getProperty(java.lang.Object)
	 */
	public Object getProperty(Object key) {
		return fMap.get(key);
	}
	
	/*
	 * @see org.eclipse.jface.text.formatter.IFormattingContext#dispose()
	 */
	public void dispose() {
		fMap.clear();
	}
}
