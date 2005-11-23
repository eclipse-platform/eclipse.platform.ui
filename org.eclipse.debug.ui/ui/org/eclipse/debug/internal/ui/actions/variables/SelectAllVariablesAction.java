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
package org.eclipse.debug.internal.ui.actions.variables;

import org.eclipse.debug.internal.ui.actions.SelectAllAction;
import org.eclipse.debug.ui.IDebugView;

public class SelectAllVariablesAction extends SelectAllAction {

	
	protected String getActionId() {
		return IDebugView.SELECT_ALL_ACTION + ".Variables"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.selection.AbstractRemoveAllActionDelegate#initialize()
	 */
	protected void initialize() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.selection.AbstractRemoveAllActionDelegate#isEnabled()
	 */
	protected boolean isEnabled() {
		// TODO: only enable when stuff present
		return true;
	}
}
