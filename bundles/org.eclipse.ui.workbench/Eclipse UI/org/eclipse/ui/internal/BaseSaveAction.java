package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.*;

/**
 * The abstract superclass for save actions that depend on the active editor.
 */
public abstract class BaseSaveAction extends ActiveEditorAction
	implements IPropertyListener 
{
/**
 * Creates a new action with the given text.
 *
 * @param text the string used as the text for the action, 
 *   or <code>null</code> if there is no text
 * @param window the workbench window this action is
 *   registered with.
 */
protected BaseSaveAction(String text, IWorkbenchWindow window) {
	super(text, window);
}
/* (non-Javadoc)
 * Method declared on ActiveEditorAction.
 */
protected void editorActivated(IEditorPart part) {
	if (part != null)
		part.addPropertyListener(this);
}
/* (non-Javadoc)
 * Method declared on ActiveEditorAction.
 */
protected void editorDeactivated(IEditorPart part) {
	if (part != null)
		part.removePropertyListener(this);
}
/* (non-Javadoc)
 * Method declared on IPropertyListener.
 */
public void propertyChanged(Object source, int propID) {
	if (source == getActiveEditor()) {
		if (propID == IWorkbenchPart.PROP_TITLE || propID == IEditorPart.PROP_DIRTY)
			updateState();
	}
}
}
