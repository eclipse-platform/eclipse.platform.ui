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
package org.eclipse.debug.internal.ui.actions.breakpoints;

import org.eclipse.debug.internal.ui.actions.RetargetAction;
import org.eclipse.debug.ui.actions.*;


/**
 * Retargettable breakpoint action.
 * 
 * @since 3.0
 */
public abstract class RetargetBreakpointAction extends RetargetAction {
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.RetargetAction#getAdapterClass()
	 */
	protected Class getAdapterClass() {
		return IToggleBreakpointsTarget.class;
	}
}
