/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.breakpoints;


public class DisableBreakpointsAction extends EnableBreakpointsAction {
	
	/**
	 * If this action can enable breakpoints
	 * @return always <code>false</code>
	 */
	protected boolean isEnableAction() {
		return false;
	}
}
