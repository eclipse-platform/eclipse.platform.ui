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

package org.eclipse.jface.text.source;

/**
 * @since 2.1
 */
public interface ISourceViewerExtension {
	
	/**
	 * Shows/hides an overview representation of all annotations.
	 * 
	 * @param show the flag that indicates whether the 
	 */
	void showAnnotationsOverview(boolean show);
}
