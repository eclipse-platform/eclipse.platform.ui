package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.ui.*;
/**
 * A simple factory for the welcome editor
 */
public class WelcomeEditorInputFactory implements IElementFactory {
/**
 * WelcomeEditorInputFactory constructor comment.
 */
public WelcomeEditorInputFactory() {
	super();
}
/**
 * Re-creates and returns an object from the state captured within the given 
 * memento. 
 * <p>
 * Under normal circumstances, the resulting object can be expected to be
 * persistable; that is,
 * <pre>
 * result.getAdapter(org.eclipse.ui.IPersistableElement.class)
 * </pre>
 * should not return <code>null</code>.
 * </p>
 *
 * @param memento a memento containing the state for the object
 * @return an object, or <code>null</code> if the element could not be created
 */
public IAdaptable createElement(IMemento memento) {
	return new WelcomeEditorInput();
}
}
