/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.ide;

import org.eclipse.ui.IMarkerHelpRegistry;

/**
 * Placeholder for IDE-specific APIs to be factored out of existing workbench.
 * 
 * @since 3.0
 */
public final class IDE {
	// @issue
	private static IMarkerHelpRegistry markerHelpRegistry;

	/**
	 * @issue get doc from IWorkbench
	 * 
	 * @return
	 */	
	public static IMarkerHelpRegistry getMarkerHelpRegistry() {
		return markerHelpRegistry;
	}
	
}
