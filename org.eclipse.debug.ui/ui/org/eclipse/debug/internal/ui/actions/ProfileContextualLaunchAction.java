/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;

import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.actions.ContextualLaunchAction;

/**
 * @author DWright
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ProfileContextualLaunchAction extends ContextualLaunchAction {
	
	public ProfileContextualLaunchAction() {
		super(ILaunchManager.PROFILE_MODE);
	}

}
