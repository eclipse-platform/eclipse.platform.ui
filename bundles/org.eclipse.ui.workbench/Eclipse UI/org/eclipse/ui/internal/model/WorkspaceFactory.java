package org.eclipse.ui.internal.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * The ResourceFactory is used to save and recreate an IResource object.
 * As such, it implements the IPersistableElement interface for storage
 * and the IElementFactory interface for recreation.
 *
 * @see IMemento
 * @see IPersistableElement
 * @see IElementFactory
 */
public class WorkspaceFactory implements IElementFactory, IPersistableElement {
	private static final String FACTORY_ID = "org.eclipse.ui.internal.model.WorkspaceFactory";//$NON-NLS-1$
/**
 * Create a ResourceFactory.  This constructor is typically used
 * for our IElementFactory side.
 */
public WorkspaceFactory() {
}
/**
 * @see IElementFactory
 */
public IAdaptable createElement(IMemento memento) {
	return ResourcesPlugin.getWorkspace();
}
/**
 * @see IPersistableElement.
 */
public String getFactoryId() {
	return FACTORY_ID;
}
/**
 * @see IPersistableElement
 */
public void saveState(IMemento memento) {
}
}
