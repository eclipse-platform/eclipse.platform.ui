package org.eclipse.ui.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;

/**
 *	Workbench common Save As dialog
 */
public class SaveAsAction extends BaseSaveAction {
/**
 *	Create an instance of this class
 */
public SaveAsAction(IWorkbenchWindow window) {
	super("Save &As...", window);
	setToolTipText("Save to another location");
	setId(IWorkbenchActionConstants.SAVE_AS);
	WorkbenchHelp.setHelp(this, new Object[] {IHelpContextIds.SAVE_AS_ACTION});
}
/**
 * Performs a save as action.
 */
public void run() {
	getActiveEditor().doSaveAs();
}
/* (non-Javadoc)
 * Method declared on ActiveEditorAction.
 */
protected void updateState() {
	IEditorPart editor = getActiveEditor();
	if (editor != null) {
		// This is not good NL style.
		org.eclipse.ui.internal.misc.UIHackFinder.fixUI();
		String title = editor.getTitle();
		String label = "Save " + title + " &As...";
		setText(label);
		setEnabled(editor.isSaveAsAllowed());
	} else {
		setText("Save &As...");
		setEnabled(false);
	}
}
}
