package org.eclipse.ui.actions;

import org.eclipse.jface.viewers.ISelection;

/**
 * An <code>ActionContext</code> represents the context used to determine
 * which actions are added by an <code>ActionGroup</code>, and what their 
 * enabled state should be.
 * <p>
 * This class encapsulates only a selection.
 * Clients may subclass this class to add more information to the context.
 * </p>
 */
public class ActionContext {
	
	/**
	 * The selection.
	 */
	private ISelection selection;
	
	/**
	 * Creates a new action context with the given selection.
	 */
	public ActionContext(ISelection selection) {
		setSelection(selection);
	}
	
	/**
	 * Returns the selection.
	 */
    public ISelection getSelection() {
    	return selection;
    }
    
    /**
     * Sets the selection.
     */
    public void setSelection(ISelection selection) {
    	this.selection = selection;
    }
}
