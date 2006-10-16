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
package org.eclipse.debug.internal.ui.commands.actions;

import org.eclipse.debug.internal.ui.commands.provisional.IBooleanRequestMonitor;
import org.eclipse.jface.action.IAction;

/**
 * Request monitor for a command action.
 * 
 * @since 3.3
 */
public class CommandMonitor extends AbstractRequestMonitor implements IBooleanRequestMonitor {
	
	private IAction fAction;
	
	public CommandMonitor(IAction action) {
		fAction = action;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor#setResult(boolean)
	 */
	public void setResult(boolean result) {
		setCanceled(!result);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#done()
	 */
	public void done() {
		fAction.setEnabled(!isCanceled());
	}
}
