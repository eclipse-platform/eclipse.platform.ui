package org.eclipse.ui.internal;

import org.eclipse.ui.*;

public class PinEditorAction extends ActiveEditorAction {

	private IWorkbenchWindow window;

/**
 * Creates a PinEditorAction.
 */
protected PinEditorAction(IWorkbenchWindow window) {
	super(WorkbenchMessages.getString("PinEditorAction.text"), window); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("PinEditorAction.toolTip")); //$NON-NLS-1$
	setId("org.eclipse.ui.internal.PinEditorAction"); //$NON-NLS-1$
	this.window = window;
//	WorkbenchHelp.setHelp(this, new Object[] {IHelpContextIds.SAVE_ACTION});
}

/**
 * @see Action#run()
 */
public void run() {
	IEditorPart editor = getActiveEditor();
	((EditorSite)editor.getEditorSite()).setReuseEditor(!isChecked());
}
/**
 * @see ActiveEdirorAction#updateState()
 */
protected void updateState() {
	if(window == null) {
		setChecked(false);
		setEnabled(false);
		return;
	}
	IWorkbenchPage page = window.getActivePage();
	if(page == null) {
		setChecked(false);
		setEnabled(false);
		return;
	}
	IEditorPart editor = getActiveEditor();
	boolean enabled = editor != null;
	setEnabled(enabled);
	if(enabled)
		setChecked(!((EditorSite)editor.getEditorSite()).getReuseEditor());
	else
		setChecked(false);
}
}

