package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugPlugin;

public class SelectAllExpressionsAction extends SelectAllAction {

	protected void update() {
		getAction().setEnabled(
			DebugPlugin.getDefault().getExpressionManager().getExpressions().length != 0);
	}
}
