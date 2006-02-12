/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.fieldassist;

/**
 * Decoration ids for common field decorations used by the workbench which may
 * be useful to other plug-ins.
 * <p>
 * This interface is not intended to be implemented by clients.
 * 
 * @since 3.2
 */
public interface IWorkbenchFieldDecorationConstants {
	/**
	 * Decoration that cues the user that content assist is available (value
	 * <code>"org.eclipse.ui.fieldassist.contentassistcue"</code>).
	 */
	public static final String CONTENT_ASSIST_CUE = "org.eclipse.ui.fieldassist.contentassistcue"; //$NON-NLS-1$
}
