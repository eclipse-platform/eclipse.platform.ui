package org.eclipse.debug.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 */

import java.util.ResourceBundle;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.ui.texteditor.TextEditorAction;

public class ConsoleViewerAction extends TextEditorAction {

	private int fOperationCode= -1;
	private ITextOperationTarget fOperationTarget;

	public ConsoleViewerAction(ResourceBundle bundle, String prefix, ConsoleViewer viewer, int operationCode) {
		super(bundle, prefix, null);
		fOperationCode= operationCode;
		fOperationTarget= viewer.getTextOperationTarget();
		update();
	}

	/**
	 * @see TextEditorAction
	 */
	public void update() {

		boolean wasEnabled= isEnabled();
		boolean isEnabled= (fOperationTarget != null && fOperationTarget.canDoOperation(fOperationCode));
		setEnabled(isEnabled);

		if (wasEnabled != isEnabled) {
			firePropertyChange(ENABLED, wasEnabled ? Boolean.TRUE : Boolean.FALSE, isEnabled ? Boolean.TRUE : Boolean.FALSE);
		}
	}
	
	/**
	 * @see Action
	 */
	public void run() {
		if (fOperationCode != -1 && fOperationTarget != null) {
			fOperationTarget.doOperation(fOperationCode);
		}
	}
}

