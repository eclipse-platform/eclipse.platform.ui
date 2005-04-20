/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.jobs.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.progress.IElementCollector;

public class SlowElementAdapter implements IDeferredWorkbenchAdapter {

	private static boolean serializeFetching = false;
	private static boolean batchFetchedChildren = false;

	final ISchedulingRule mutexRule = new ISchedulingRule() {
		public boolean isConflicting(ISchedulingRule rule) {
			return rule == mutexRule;
		}
		public boolean contains(ISchedulingRule rule) {
			return rule == mutexRule;
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#fetchDeferredChildren(java.lang.Object,
	 *           org.eclipse.jface.progress.IElementCollector,
	 *           org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void fetchDeferredChildren(Object object, IElementCollector collector, IProgressMonitor monitor) {
		if (object instanceof SlowElement) {
			Object[] children = ((SlowElement) object).getChildren();
			if (isBatchFetchedChildren()) {
				sleep(4000);
				collector.add(children, monitor);
			} else {
				for (int i = 0; i < children.length; i++) {
					if (monitor.isCanceled()) {
						return;
					}
					collector.add(children[i], monitor);
					sleep(4000);
				}
			}
		}
	}

	private void sleep(long mills) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			//ignore
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#isContainer()
	 */
	public boolean isContainer() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#getRule(java.lang.Object)
	 */
	public ISchedulingRule getRule(final Object object) {
		if (isSerializeFetching())
			return mutexRule;
		// Allow several SlowElement parents to fetch children concurrently
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object object) {
		if (object instanceof SlowElement) {
			return ((SlowElement) object).getChildren();
		}
		return new Object[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
	 */
	public String getLabel(Object o) {
		if (o instanceof SlowElement) {
			return ((SlowElement) o).getName();
		}
		return "unknown"; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
	 */
	public Object getParent(Object o) {
		if (o instanceof SlowElement) {
			return ((SlowElement) o).getParent();
		}
		return null;
	}

	/**
	 * @return Returns the batchFetchedChildren.
	 */
	public static boolean isBatchFetchedChildren() {
		return batchFetchedChildren;
	}

	/**
	 * @param batchFetchedChildren
	 *                   The batchFetchedChildren to set.
	 */
	public static void setBatchFetchedChildren(boolean batchFetchedChildren) {
		SlowElementAdapter.batchFetchedChildren = batchFetchedChildren;
	}

	/**
	 * @return Returns the serializeFetching.
	 */
	public static boolean isSerializeFetching() {
		return serializeFetching;
	}

	/**
	 * @param serializeFetching
	 *                   The serializeFetching to set.
	 */
	public static void setSerializeFetching(boolean serializeFetching) {
		SlowElementAdapter.serializeFetching = serializeFetching;
	}

}
