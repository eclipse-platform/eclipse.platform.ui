/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.internal.contexts.debug.ui;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.internal.contexts.EclipseContext;
import org.eclipse.e4.core.internal.contexts.IEclipseContextDebugger;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;

public class ContextTreeProvider implements IEclipseContextDebugger, ITreeContentProvider {

	private class RefreshJob extends UIJob {
		public RefreshJob(Display display) {
			super(display, "Context debug update job"); //$NON-NLS-1$
			setSystem(true);
		}

		public IStatus runInUIThread(IProgressMonitor monitor) {
			if (monitor.isCanceled())
				return Status.OK_STATUS;
			if (view != null)
				view.refresh();
			return Status.OK_STATUS;
		}
	}

	final private static int REFRESH_DELAY = 5000; // 5sec delay between refreshes

	static private Set<WeakReference<EclipseContext>> activeContexts = new HashSet<WeakReference<EclipseContext>>();

	static protected ContextsView view; // we have maximum one view

	static protected RefreshJob refreshJob;

	private Display display;

	public ContextTreeProvider() {
		// used by Declarative Services
	}

	public ContextTreeProvider(ContextsView contextView, Display display) {
		view = contextView;
		this.display = display;
		refreshJob = new RefreshJob(display);
	}

	public void setAutoUpdates(boolean update) {
		if (update) {
			if (refreshJob == null && display != null)
				refreshJob = new RefreshJob(display);
		} else {
			if (refreshJob != null) {
				refreshJob.cancel();
				refreshJob = null;
			}
		}
	}

	public void dispose() {
		if (refreshJob != null)
			refreshJob.cancel();
		view = null;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// nothing to do
	}

	public Object[] getElements(Object inputElement) {
		for (Iterator<WeakReference<EclipseContext>> i = activeContexts.iterator(); i.hasNext();) {
			WeakReference<EclipseContext> ref = i.next();
			EclipseContext storedRoot = ref.get();
			if (storedRoot == null)
				i.remove();
		}
		return activeContexts.toArray();
	}

	public Object[] getChildren(Object parentElement) {
		if (!(parentElement instanceof WeakReference<?>))
			return null;
		@SuppressWarnings("unchecked")
		WeakReference<EclipseContext> ref = (WeakReference<EclipseContext>) parentElement;
		EclipseContext parentContext = ref.get();
		if (parentContext == null)
			return null;
		Set<EclipseContext> children = parentContext.getChildren();
		if (children == null)
			return null;
		Set<WeakReference<EclipseContext>> childrenRef = new HashSet<WeakReference<EclipseContext>>(children.size());
		for (EclipseContext child : children) {
			childrenRef.add(new WeakContextRef(child));
		}
		return childrenRef.toArray();
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		if (!(element instanceof WeakReference<?>))
			return false;
		@SuppressWarnings("unchecked")
		WeakReference<EclipseContext> ref = (WeakReference<EclipseContext>) element;
		EclipseContext parentContext = ref.get();
		if (parentContext == null)
			return false;
		return (parentContext.getChildren() != null);
	}

	synchronized public void notify(EclipseContext context, IEclipseContextDebugger.EventType type, Object data) {
		switch (type) {
			case CONSTRUCTED :
				AllocationRecorder.getDefault().allocated(context, new Exception());
				EclipseContext newRoot = getRoot(context);
				boolean found = false;
				for (Iterator<WeakReference<EclipseContext>> i = activeContexts.iterator(); i.hasNext();) {
					WeakReference<EclipseContext> ref = i.next();
					EclipseContext storedRoot = ref.get();
					if (storedRoot == null) {
						i.remove();
						continue;
					}
					if (storedRoot == newRoot) {
						found = true;
						break;
					}
				}
				if (!found)
					activeContexts.add(new WeakContextRef(context));
				if (view != null && refreshJob != null)
					refreshJob.schedule(REFRESH_DELAY);
				break;
			case DISPOSED :
				for (Iterator<WeakReference<EclipseContext>> i = activeContexts.iterator(); i.hasNext();) {
					WeakReference<EclipseContext> ref = i.next();
					EclipseContext storedRoot = ref.get();
					if (storedRoot == null) {
						i.remove();
						continue;
					}
					if (storedRoot == context) {
						i.remove();
						break;
					}
				}
				if (view != null && refreshJob != null)
					refreshJob.schedule(REFRESH_DELAY);
				break;
		}
	}

	private EclipseContext getRoot(IEclipseContext context) {
		IEclipseContext root = context;
		while (context != null) {
			root = context;
			context = context.getParent();
		}
		return (EclipseContext) root;
	}

}
