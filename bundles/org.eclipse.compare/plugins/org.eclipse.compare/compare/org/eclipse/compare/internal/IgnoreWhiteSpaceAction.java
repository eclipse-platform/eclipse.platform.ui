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

import java.util.ResourceBundle;

import org.eclipse.compare.*;

/**
 * Toggles the <code>ICompareConfiguration.IGNORE_WS</code> property of an
 * <code>ICompareConfiguration</code>.
 */
public class IgnoreWhiteSpaceAction extends ChangePropertyAction {

	public IgnoreWhiteSpaceAction(ResourceBundle bundle, CompareConfiguration cc) {
		super(bundle, cc, "action.IgnoreWhiteSpace.", CompareConfiguration.IGNORE_WHITESPACE); //$NON-NLS-1$
	}
}
