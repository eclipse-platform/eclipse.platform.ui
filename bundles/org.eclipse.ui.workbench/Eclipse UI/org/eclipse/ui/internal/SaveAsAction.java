package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
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
	super(WorkbenchMessages.getString("SaveAs.text"), window); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("SaveAs.toolTip")); //$NON-NLS-1$
	setId(IWorkbenchActionConstants.SAVE_AS);
	WorkbenchHelp.setHelp(this, IHelpContextIds.SAVE_AS_ACTION);
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
		String title = editor.getTitle();
		String label = WorkbenchMessages.format("SaveAs.textOneArg", new Object[] {title}); //$NON-NLS-1$
		setText(label);
		setEnabled(editor.isSaveAsAllowed());
	} else {
		setText(WorkbenchMessages.getString("SaveAs.text")); //$NON-NLS-1$
		setEnabled(false);
	}
}
}
