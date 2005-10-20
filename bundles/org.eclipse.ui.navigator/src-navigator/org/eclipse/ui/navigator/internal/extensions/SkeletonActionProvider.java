/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.internal.extensions;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.ICommonActionProvider;
import org.eclipse.ui.navigator.internal.actions.CommonActionProvider;

/**
 * <p>
 * The following class is experimental until fully documented.
 * </p>
 */
public class SkeletonActionProvider extends CommonActionProvider implements ICommonActionProvider {

	public static final ICommonActionProvider INSTANCE = new SkeletonActionProvider();

	/**
	 *  
	 */
	private SkeletonActionProvider() {
		super();
	}

	public boolean fillContextMenu(IMenuManager menu) {
		return false;
	}

	public boolean fillActionBars(IActionBars actionBars) {
		return false;
	}

}