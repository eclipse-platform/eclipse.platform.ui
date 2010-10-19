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

import java.util.HashSet;
import java.util.Set;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.internal.contexts.EclipseContext;
import org.eclipse.e4.core.internal.contexts.IEclipseContextDebugger;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class ContextTreeProvider implements IEclipseContextDebugger, ITreeContentProvider {

	static private Set<EclipseContext> activeContexts = new HashSet<EclipseContext>();

	static private ContextsView view; // we have maximum one view

	public ContextTreeProvider() {
		// used by Declarative Services
	}

	public ContextTreeProvider(ContextsView contextView) {
		view = contextView;
	}

	public void dispose() {
		view = null;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// nothing to do
	}

	public Object[] getElements(Object inputElement) {
		return activeContexts.toArray();
	}

	public Object[] getChildren(Object parentElement) {
		Set<EclipseContext> children = ((EclipseContext) parentElement).getChildren();
		if (children == null)
			return null;
		return children.toArray();
	}

	public Object getParent(Object element) {
		return ((EclipseContext) element).getParent();
	}

	public boolean hasChildren(Object element) {
		return (((EclipseContext) element).getChildren() != null);
	}

	public void notify(EclipseContext context, IEclipseContextDebugger.EventType type, Object data) {
		switch (type) {
			case CONSTRUCTED :
				AllocationRecorder.getDefault().allocated(context, new Exception());
				activeContexts.add(getRoot(context));
				if (view != null)
					view.refresh(context.getParent());
				break;
			case DISPOSED :
				AllocationRecorder.getDefault().disposed(context);
				activeContexts.remove(context);
				if (view != null)
					view.refresh(context.getParent());
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
