package org.eclipse.update.ui.internal.manager;

import org.eclipse.core.runtime.*;
import org.eclipse.ui.*;

/**
 * A simple factory for the update manager
 */
public class UpdateManagerInputFactory implements IElementFactory {
/**
 * UpdateManagerInputFactory constructor comment.
 */
public UpdateManagerInputFactory() {
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
	return new UpdateManagerInput();
}
}

