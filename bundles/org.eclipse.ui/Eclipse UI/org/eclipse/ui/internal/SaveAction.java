package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.SWT;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.jface.dialogs.*;

/**
 */
public class SaveAction extends BaseSaveAction {
/**
 *	Create an instance of this class
 */
public SaveAction(IWorkbenchWindow window) {
	super(WorkbenchMessages.getString("SaveAction.text"), window); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("SaveAction.toolTip")); //$NON-NLS-1$
	setId(IWorkbenchActionConstants.SAVE);
	WorkbenchHelp.setHelp(this, IHelpContextIds.SAVE_ACTION);
	setAccelerator(SWT.CTRL |'s');
}
/**
 * Performs the save.
 */
public void run() {
	IEditorPart part = getActiveEditor();
	IWorkbenchPage page = part.getSite().getPage();
	page.saveEditor(part, false);
}
/* (non-Javadoc)
 * Method declared on ActiveEditorAction.
 */
protected void updateState() {
	IEditorPart editor = getActiveEditor();
	if (editor != null) {
		String title = editor.getTitle();
		String label = WorkbenchMessages.format("SaveAction.textOneArg", new Object[] {title}); //$NON-NLS-1$
		setText(label);
		setEnabled(editor.isDirty());
	} else {
		setText(WorkbenchMessages.getString("SaveAction.text")); //$NON-NLS-1$
		setEnabled(false);
	}
}
}
