package org.eclipse.ui.internal;

import org.eclipse.ui.*;

public class ReuseEditorAction extends ActiveEditorAction {

	private IWorkbenchWindow window;

/**
 * Creates a ReuseEditorAction.
 */
protected ReuseEditorAction(IWorkbenchWindow window) {
	super(WorkbenchMessages.getString("ReuseEditorAction.text"), window); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("ReuseEditorAction.toolTip")); //$NON-NLS-1$
	setId("org.eclipse.ui.internal.ReuseEditorAction"); //$NON-NLS-1$
	this.window = window;
//	WorkbenchHelp.setHelp(this, new Object[] {IHelpContextIds.SAVE_ACTION});
}

/**
 * @see Action#run()
 */
public void run() {
	IEditorPart editor = getActiveEditor();
	EditorSite site = (EditorSite)editor.getEditorSite();
	WorkbenchPage page = (WorkbenchPage)site.getPage();
	IEditorPart oldEditor = page.getReusableEditor();
	if(isChecked())
		page.setReusableEditor(editor);
	else
		page.setReusableEditor(null);
	
	EditorPane pane = (EditorPane)site.getPane();
	pane.updateTitles();
	
	if(oldEditor != null) {
		site = (EditorSite)oldEditor.getEditorSite();
		pane = (EditorPane)site.getPane();
		pane.updateTitles();
	}
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
	WorkbenchPage page = (WorkbenchPage)window.getActivePage();
	if(page == null) {
		setChecked(false);
		setEnabled(false);
		return;
	}
	IEditorPart editor = getActiveEditor();
	boolean enabled = editor != null;
	setEnabled(enabled);
	if(enabled) {
		EditorSite site = (EditorSite)editor.getEditorSite();
		EditorPane pane = (EditorPane)site.getPane();
		pane.setReuseEditorAction(this);
		setChecked(editor == page.getReusableEditor());
	} else {
		setChecked(false);
	}
}
}

