package org.eclipse.ui.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.runtime.*;
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
	super("&Save@Ctrl+S", window);
	setToolTipText("Save the open editor contents");
	setId(IWorkbenchActionConstants.SAVE);
	WorkbenchHelp.setHelp(this, new Object[] {IHelpContextIds.SAVE_ACTION});
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
		// This is not good NL style.
		org.eclipse.ui.internal.misc.UIHackFinder.fixUI();
		String title = editor.getTitle();
		String label = "&Save " + title + "@Ctrl+S";
		setText(label);
		setEnabled(editor.isDirty());
	} else {
		setText("&Save@Ctrl+S");
		setEnabled(false);
	}
}
}
