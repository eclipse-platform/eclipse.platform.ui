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
package org.eclipse.jface.text.source.projection;

/**
 * Internal class. Do not use.
 * 
 * @since 3.0
 */
public interface IProjectionListener {
	
	/**
	 * Tells this listener that projection has been enabled.
	 */
	void projectionEnabled();
	
	/**
	 * Tells this listener that projection has been disabled.
	 */
	void projectionDisabled();
}
