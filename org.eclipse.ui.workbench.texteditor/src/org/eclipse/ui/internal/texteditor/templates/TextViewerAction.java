package org.eclipse.ui.internal.texteditor.templates;

import org.eclipse.jface.action.Action;

import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;

import org.eclipse.ui.texteditor.IUpdate;

public class TextViewerAction extends Action implements IUpdate {

	private int fOperationCode = -1;
	private ITextOperationTarget fOperationTarget;

	/**
	 * Creates a new action.
	 *
	 * @param viewer        the viewer
	 * @param operationCode the opcode
	 */
	public TextViewerAction(ITextViewer viewer, int operationCode) {
		fOperationCode = operationCode;
		fOperationTarget = viewer.getTextOperationTarget();
		update();
	}

	/**
	 * Updates the enabled state of the action. Fires a property change if the
	 * enabled state changes.
	 *
	 * @see Action#firePropertyChange(String, Object, Object)
	 */
	@Override
	public void update() {
		// XXX: workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=206111
		if (fOperationCode == ITextOperationTarget.UNDO || fOperationCode == ITextOperationTarget.REDO)
			return;

		boolean wasEnabled = isEnabled();
		boolean isEnabled = (fOperationTarget != null && fOperationTarget.canDoOperation(fOperationCode));
		setEnabled(isEnabled);

		if (wasEnabled != isEnabled) {
			firePropertyChange(ENABLED, wasEnabled ? Boolean.TRUE : Boolean.FALSE,
					isEnabled ? Boolean.TRUE : Boolean.FALSE);
		}
	}

	/**
	 * @see Action#run()
	 */
	@Override
	public void run() {
		if (fOperationCode != -1 && fOperationTarget != null) {
			fOperationTarget.doOperation(fOperationCode);
		}
	}
}