package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Removes all expressions from the expressions view.
 */
public class RemoveAllExpressionsAction extends AbstractRemoveAllAction implements IUpdate {
	
	public RemoveAllExpressionsAction() {
		super(ActionMessages.getString("RemoveAllExpressionsAction.Remove_All_1"), ActionMessages.getString("RemoveAllExpressionsAction.Remove_All_Expressions_2")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see IAction
	 */
	public void run() {
		IExpressionManager manager = DebugPlugin.getDefault().getExpressionManager();
		IExpression[] expressions= manager.getExpressions();
		for (int i= 0; i < expressions.length; i++) {
			manager.removeExpression(expressions[i]);
		}
	}
	
	/**
	 * @see IUpdate#update()
	 */
	public void update() {
		setEnabled(DebugPlugin.getDefault().getExpressionManager().getExpressions().length == 0 ? false : true);
	}
	
}
