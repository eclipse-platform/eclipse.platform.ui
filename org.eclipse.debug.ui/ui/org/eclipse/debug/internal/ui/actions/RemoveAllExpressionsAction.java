package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
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
		for (int i= 0; i < expressions.length; i++) {
			manager.removeExpression(expressions[i]);
		}
	}
	
	protected void update() {
		getAction().setEnabled(
			DebugPlugin.getDefault().getExpressionManager().hasExpressions());
	}
}
