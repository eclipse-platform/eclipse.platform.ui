/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.modeling;

import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;

/**
 * This service is used to resolve references from MPlaceholders.
 * 
 * The issue is that we may be storing a cloned snippet which contains references to 'shared
 * elements' but instantiating the snippet in a new window requires that the shared elements list be
 * updated.
 */
public interface EPlaceholderResolver {
	/**
	 * This method is used to re-resolve a placeholder's reference to a 'shared part' within the
	 * context of a particular window. This is necessary because placeholders must be referencing an
	 * element in that window's 'sharedParts' list.
	 * <p>
	 * Implementors may presume that the if the placeholder's reference is already non-null then it
	 * has already been resolved.
	 * </p>
	 * 
	 * @param ph
	 *            The placeholder to set the reference for (if necessary)
	 * @param refWin
	 *            The window the whose shared parts are to be referenced
	 */
	public void resolvePlaceholderRef(MPlaceholder ph, MWindow refWin);
}
