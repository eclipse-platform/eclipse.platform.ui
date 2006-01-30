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
 * </p>
 * <p>
 * This API is considered experimental. It is still evolving during 3.2 and is
 * subject to change. It is being released to obtain feedback from early
 * adopters.
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
