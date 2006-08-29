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
package org.eclipse.debug.internal.ui.actions.context;

import org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor;
import org.eclipse.jface.action.IAction;

/**
 * Boolean Request monitor that collects votes from a number of voters.
 * All participants vote, as each votes the results are or'd together, allowing
 * for the pattern: if at least one of...
 * 
 * @since 3.3
 */
public class BooleanOrWiseRequestMonitor extends AbstractRequestMonitor implements IBooleanRequestMonitor {

	private boolean fResult = false;
	private IAction fAction = null;
	
	/**
	 * Constructor
	 * @param action the action to set enabled state for after voting completes
	 */
	public BooleanOrWiseRequestMonitor(IAction action) {
		fAction = action;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor#setResult(boolean)
	 */
	public void setResult(boolean result) {
		fResult |= result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#done()
	 */
	public void done() {
		if(fAction != null) {
			fAction.setEnabled(fResult);
		}
	}

}
