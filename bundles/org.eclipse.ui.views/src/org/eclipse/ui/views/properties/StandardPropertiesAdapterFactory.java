package org.eclipse.ui.views.properties;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IAdapterFactory;

/**
 * Dispenses an <code>IPropertySource</code> adapter for the core resource objects.
 */
/* package */ class StandardPropertiesAdapterFactory implements IAdapterFactory {
/* (non-Javadoc)
 * Method declared on IAdapterFactory.
 */
public Object getAdapter(Object o, Class adapterType) {
	if (adapterType.isInstance(o)) {
		return o;
	}
	if (adapterType == IPropertySource.class) {
		if (o instanceof IResource) {
			IResource resource = (IResource) o;
			if (resource.getType() == IResource.FILE)
				return new FilePropertySource((IFile) o);
			else
				return new ResourcePropertySource((IResource) o);
		}
	}
	return null;
}
/* (non-Javadoc)
 * Method declared on IAdapterFactory.
 */
public Class[] getAdapterList() {
	return new Class[] {
		IPropertySource.class
	};
}
}
