/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.internal;

public interface INavigatable {
	
	static final String NAVIGATOR_PROPERTY= "org.eclipse.compare.internal.Navigator"; //$NON-NLS-1$
	
	/*
	 * Returns true if at end or beginning.
	 */
	boolean gotoDifference(boolean next);
}
