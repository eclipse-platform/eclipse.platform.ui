package org.eclipse.ui.examples.readmetool;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

/**
 * This class is used to demonstrate editor action extensions.
 * An extension should be defined in the readme plugin.xml.
 */
public class EditorActionDelegate implements IEditorActionDelegate {
	private IEditorPart editor;
/**
 * Creates a new EditorActionDelegate.
 */
public EditorActionDelegate() {
}
/* (non-Javadoc)
 * Method declared on IActionDelegate
 */
public void run(IAction action) {
	MessageDialog.openInformation(editor.getSite().getShell(),
		MessageUtil.getString("Readme_Editor"),  //$NON-NLS-1$
		MessageUtil.getString("Editor_Action_executed")); //$NON-NLS-1$
}
/** 
 * The <code>EditorActionDelegate</code> implementation of this
 * <code>IActionDelegate</code> method 
 *
 * Selection in the desktop has changed. Plugin provider
 * can use it to change the availability of the action
 * or to modify other presentation properties.
 *
 * <p>Action delegate cannot be notified about
 * selection changes before it is loaded. For that reason,
 * control of action's enable state should also be performed
 * through simple XML rules defined for the extension
 * point. These rules allow enable state control before
 * the delegate has been loaded.</p>
 */
public void selectionChanged(IAction action, ISelection selection) {
}
/** 
 * The <code>EditorActionDelegate</code> implementation of this
 * <code>IEditorActionDelegate</code> method 
 *
 * The matching editor has been activated. Notification
 * guarantees that only editors that match the type for which 
 * this action has been registered will be tracked.
 *
 * @param action action proxy that represents this delegate in the desktop
 * @param editor the matching editor that has been activated
 */
public void setActiveEditor(IAction action, IEditorPart editor) {
	this.editor = editor;
}
}
