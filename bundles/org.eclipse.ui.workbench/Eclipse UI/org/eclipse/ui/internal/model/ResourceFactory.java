package org.eclipse.ui.internal.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
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
public class ResourceFactory implements IElementFactory, IPersistableElement {
	
	// These persistence constants are stored in XML.  Do not
	// change them.
	private static final String TAG_PATH = "path";//$NON-NLS-1$
	private static final String FACTORY_ID = "org.eclipse.ui.internal.model.ResourceFactory";//$NON-NLS-1$

	// IPersistable data.
	private IResource res;
/**
 * Create a ResourceFactory.  This constructor is typically used
 * for our IElementFactory side.
 */
public ResourceFactory() {
}
/**
 * Create a ResourceFactory.  This constructor is typically used
 * for our IPersistableElement side.
 */
public ResourceFactory(IResource input) {
	res = input;
}
/**
 * @see IElementFactory
 */
public IAdaptable createElement(IMemento memento) {

	// Get the file name.
	String fileName = memento.getString(TAG_PATH);
	if (fileName == null)
		return null;

	// Create an IResource.
	res = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(fileName));
	return res;
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
	memento.putString(TAG_PATH, res.getFullPath().toString());			
}
}
