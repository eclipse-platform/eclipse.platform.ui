/*
 * Created on Jun 11, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.ui.model;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * The PendingUpdateAdapter is the object the represents an update about to
 * occur.
 */
public class PendingUpdateAdapter implements IWorkbenchAdapter, IAdaptable {

	public PendingUpdateAdapter() {
	}

	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class)
			return this;
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object o) {
		return new Object[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		// XXX Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
	 */
	public String getLabel(Object o) {
		// XXX Auto-generated method stub
		return "Pending...";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
	 */
	public Object getParent(Object o) {
		// XXX Auto-generated method stub
		return null;
	}

}
