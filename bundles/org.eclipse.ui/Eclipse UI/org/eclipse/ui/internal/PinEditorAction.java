package org.eclipse.ui.internal;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IEditorPart;

public class PinEditorAction extends ActiveEditorAction {

/**
 * Constructor for ReuseEditorAction
 */
protected PinEditorAction(IWorkbenchWindow window) {
	super(WorkbenchMessages.getString("PinEditorAction.text"), window); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("PinEditorAction.toolTip")); //$NON-NLS-1$
	setId("org.eclipse.ui.internal.PinEditorAction"); //$NON-NLS-1$
//	WorkbenchHelp.setHelp(this, new Object[] {IHelpContextIds.SAVE_ACTION});
}

/**
 * @see Action#run()
 */
public void run() {
	IEditorPart editor = getActiveEditor();
	if(editor instanceof IReusableEditor)
		((EditorSite)editor.getEditorSite()).setReuseEditor(!isChecked());
}
/**
 * @see ActiveEdirorAction#updateState()
 */
protected void updateState() {
	IEditorPart editor = getActiveEditor();
	boolean enabled = (editor != null) && (editor instanceof IReusableEditor);
	setEnabled(enabled);
	if(enabled)
		setChecked(!((EditorSite)editor.getEditorSite()).getReuseEditor());
	else
		setChecked(false);
}
}

