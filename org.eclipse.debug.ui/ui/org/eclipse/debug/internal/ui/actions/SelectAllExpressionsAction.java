package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.ui.IDebugView;

public class SelectAllExpressionsAction extends SelectAllAction {

	protected void update() {
		getAction().setEnabled(
			DebugPlugin.getDefault().getExpressionManager().hasExpressions());
	}
	
	protected String getActionId() {
		return IDebugView.SELECT_ALL_ACTION + ".Variables"; //$NON-NLS-1$
	}
}
