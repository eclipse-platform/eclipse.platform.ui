/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.core.internal.resources;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.team.IMoveDeleteHook;

/**
 * Provides special internal access to the workspace resource implementation.
 * This class is to be used for testing purposes only.
 * 
 * @since 2.0
 */
public class TestingSupport {

	/* 
	 * Class cannot be instantiated.
	 */
	private TestingSupport() {
	}

	/**
	 * Installs the given move-delete hook implementation in the given 
	 * workspace. This overrides the normal workspace behavior. Subsequent
	 * calls to <code>IResource.delete</code> and <code>move</code> will call
	 * the given hook instead of the one contributed to the extension point.
	 * Use <code>null</code> to restore the default workspace behavior.
	 * 
	 * @param workspace the workspace
	 * @param hook the hook implementation, or <code>null</code> to restore
	 *    the default workspace behavior
	 */
	public void installMoveDeleteHook(IWorkspace workspace, IMoveDeleteHook hook) {
		if (hook != null) {
			Workspace.moveDeleteHook = hook;
		} else {
			Workspace.moveDeleteHook = null;
			Workspace.initializeMoveDeleteHook();
		}
	}
}
