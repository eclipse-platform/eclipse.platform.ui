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

 
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.model.IExpression;

/**
 * Removes all expressions from the expressions view.
 */
public class RemoveAllExpressionsAction extends AbstractRemoveAllActionDelegate {

	protected void doAction() {
		IExpressionManager manager = DebugPlugin.getDefault().getExpressionManager();
		IExpression[] expressions= manager.getExpressions();
		manager.removeExpressions(expressions);
	}
	
	protected void update() {
		getAction().setEnabled(
			DebugPlugin.getDefault().getExpressionManager().hasExpressions());
	}
}
