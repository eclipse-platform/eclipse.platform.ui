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
package org.eclipse.debug.internal.ui.actions;


import java.util.Iterator;

import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.WatchExpression;
import org.eclipse.jface.action.IAction;

/**
 * Ask to re-evaluate one or more watch expressions in the context of the
 * currently selected thread.
 */
public class ReevaluateWatchExpressionAction extends WatchExpressionAction {

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		IDebugElement context = getContext();
		for (Iterator iter= getCurrentSelection().iterator(); iter.hasNext();) {
			((WatchExpression) iter.next()).setExpressionContext(context);
		}
	}

}
