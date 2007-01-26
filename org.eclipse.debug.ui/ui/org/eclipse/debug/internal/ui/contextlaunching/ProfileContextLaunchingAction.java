/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.contextlaunching;

import org.eclipse.debug.core.ILaunchManager;

/**
 * Specialization of <code>ContextLaunchingAction</code> for profile mode
 * 
 * @see ContextLaunchingAction
 * @see ILaunchManager
 * @see RunContextLaunchingAction
 * @see DebugContextLaunchingAction
 * 
 * @since 3.3
 * EXPERIMENTAL
 * CONTEXTLAUNCHING
 */
public class ProfileContextLaunchingAction extends ContextLaunchingAction {

	/**
	 * Constructor
	 */
	public ProfileContextLaunchingAction() {
		super(ILaunchManager.PROFILE_MODE);
	}

}
