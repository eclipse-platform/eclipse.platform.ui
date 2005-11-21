/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Jan 31, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.ui.navigator.internal;

import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.navigator.internal.deferred.IDeferredElementAdapter;
import org.eclipse.ui.navigator.internal.extensions.NavigatorContentExtension;

/**
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class CommonElementAdapter implements IDeferredElementAdapter, ISchedulingRule {

	private NavigatorContentExtension extension = null;

	public CommonElementAdapter(NavigatorContentExtension extension) {
		this.extension = extension;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.wst.common.navigator.views.IDeferredElementAdapter#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object element) {
		Object[] children = null;
		try {
			children = extension.getContentProvider().getChildren(element);
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		return children;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
	 */
	public String getLabel(Object o) {
		return NavigatorMessages.getString("CommonElementAdapter.0"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		return extension.getContentProvider().getParent(element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.wst.common.navigator.views.IDeferredElementAdapter#isContainer()
	 */
	public boolean isContainer() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.wst.common.navigator.views.IDeferredElementAdapter#getRule(java.lang.Object)
	 */
	public ISchedulingRule getRule(Object object) {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#contains(org.eclipse.core.runtime.jobs.ISchedulingRule)
	 */
	public boolean contains(ISchedulingRule rule) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#isConflicting(org.eclipse.core.runtime.jobs.ISchedulingRule)
	 */
	public boolean isConflicting(ISchedulingRule rule) {
		return false;
	}

}
