package org.eclipse.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.IAction;

/**
 * Interface for an action that is contributed into an editor-activated menu or
 * tool bar. It extends <code>IActionDelegate</code> and adds a method for 
 * connecting the delegate to the editor it should work with. Since there is
 * always only one action delegate per editor type, this method supplies the
 * link to the currently active editor instance.
 */
public interface IEditorActionDelegate extends IActionDelegate {
/**
 * Sets the active editor for the delegate.  
 * Implementors should disconnect from the old editor, connect to the 
 * new editor, and update the action to reflect the new editor.
 *
 * @param action the action proxy that handles presentation portion of the action
 * @param targetEditor the new editor target
 */
public void setActiveEditor(IAction action, IEditorPart targetEditor);
}
