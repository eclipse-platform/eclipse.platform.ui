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

/*
 * Fix for http://dev.eclipse.org/bugs/show_bug.cgi?id=20106
 */ 
public interface IOpenable {
	
	static final String OPENABLE_PROPERTY= "org.eclipse.compare.internal.Openable"; //$NON-NLS-1$
	
	/**
	 * Opens the selected element
	 */
	void openSelected();
}
