package org.eclipse.ui.internal;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IEditorPart;

public class ReuseEditorAction extends ActiveEditorAction {

/**
 * Constructor for ReuseEditorAction
 */
protected ReuseEditorAction(IWorkbenchWindow window) {
	super(WorkbenchMessages.getString("ReuseEditorAction.text"), window); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("ReuseEditorAction.toolTip")); //$NON-NLS-1$
	setId("org.eclipse.ui.internal.ReuseEditorAction"); //$NON-NLS-1$
//	WorkbenchHelp.setHelp(this, new Object[] {IHelpContextIds.SAVE_ACTION});
}

/**
 * @see Action#run()
 */
public void run() {
	Object editor = getActiveEditor();
	if(editor instanceof IReusableEditor)
		((IReusableEditor)editor).setReuseEditor(isChecked());
}
/**
 * @see ActiveEdirorAction#updateState()
 */
protected void updateState() {
	Object editor = getActiveEditor();
	boolean enabled = (editor != null) && (editor instanceof IReusableEditor);
	setEnabled(enabled);
	if(enabled)
		setChecked(((IReusableEditor)editor).getReuseEditor());
	else
		setChecked(false);
}
}

