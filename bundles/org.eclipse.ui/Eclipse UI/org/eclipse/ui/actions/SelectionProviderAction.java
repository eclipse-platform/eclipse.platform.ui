package org.eclipse.ui.actions;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.Action;

/**
 * The abstract superclass for actions that listen to selection changes
 * from a particular selection provider. This implementation splits the current
 * selection along structured/unstructured lines, providing a convenient place
 * to monitor selection changes that require adjusting action state.
 * <p>
 * Subclasses must implement the following <code>IAction</code> method:
 * <ul>
 *   <li><code>run</code> - to do the action's work</li>
 * </ul>
 * </p>
 * <p>
 * Subclasses may reimplement either of the following methods:
 * <ul>
 *   <li><code>selectionChanged(IStructuredSelection)</code></li> 
 *   <li><code>selectionChanged(ISelection)</code></li> 
 * </ul>
 * </p>
 */
public abstract class SelectionProviderAction extends Action
	implements ISelectionChangedListener
{
	
	/**
	 * The selection provider that is the target of this action.
	 */
	private ISelectionProvider provider;
/**
 * Creates a new action with the given text that monitors selection changes
 * within the given selection provider.
 *
 * @param provider the selection provider that will provide selection notification
 * @param text the string used as the text for the action, 
 *   or <code>null</code> if these is no text
 */
protected SelectionProviderAction(ISelectionProvider provider, String text) {
	super(text);
	this.provider = provider;
	provider.addSelectionChangedListener(this);
}
/**
 * Returns the current selection in the selection provider.
 *
 * @return the current selection in the selection provider
 */
public ISelection getSelection() {
	return provider.getSelection();
}
/**
 * Returns the selection provider that is the target of this action.
 *
 * @return the target selection provider of this action
 */
public ISelectionProvider getSelectionProvider() {
	return provider;
}
/**
 * Returns the current structured selection in the selection provider, or an
 * empty selection if nothing is selected or if selection does not include
 * objects (for example, raw text).
 *
 * @return the current structured selection in the selection provider
 */
public IStructuredSelection getStructuredSelection() {
	ISelection selection = provider.getSelection();
	if (selection instanceof IStructuredSelection)
		return (IStructuredSelection) selection;
	else
		return new StructuredSelection();
}
/**
 * Notifies this action that the given (non-structured) selection has changed
 * in the selection provider.
 * <p>
 * The <code>SelectionProviderAction</code> implementation of this method
 * does nothing. Subclasses may reimplement to react to this selection change.
 * </p>
 *
 * @param selection the new selection
 */
public void selectionChanged(ISelection selection) {
}
/**
 * Notifies this action that the given structured selection has changed
 * in the selection provider.
 * <p>
 * The <code>SelectionProviderAction</code> implementation of this method
 * does nothing. Subclasses may reimplement to react to this selection change.
 * </p>
 *
 * @param selection the new selection
 */
public void selectionChanged(IStructuredSelection selection) {
	// Hook in subclass.
}
/**
 * The <code>SelectionProviderAction</code> implementation of this 
 * <code>ISelectionChangedListener</code> method calls 
 * <code>selectionChanged(IStructuredSelection)</code> if the selection is
 * a structured selection but <code>selectionChanged(ISelection)</code> if it is
 * not. Subclasses should override either of those methods method to react to
 * selection changes.
 */
public final void selectionChanged(SelectionChangedEvent event) {
	ISelection selection = event.getSelection();
	if (selection instanceof IStructuredSelection)
		selectionChanged((IStructuredSelection)selection);
	else
		selectionChanged(selection);
}
}
