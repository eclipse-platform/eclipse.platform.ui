/*******************************************************************************
 * Copyright (c) 2003, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.extensions;

import java.util.ArrayList;
import java.util.Set;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreePathContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.internal.navigator.NavigatorSafeRunnable;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonContentProvider;
import org.eclipse.ui.navigator.IMementoAware;
import org.eclipse.ui.navigator.IPipelinedTreeContentProvider;
import org.eclipse.ui.navigator.IPipelinedTreeContentProvider2;
import org.eclipse.ui.navigator.PipelinedShapeModification;
import org.eclipse.ui.navigator.PipelinedViewerUpdate;

/**
 * @since 3.2
 */
public class SafeDelegateTreeContentProvider implements
		IPipelinedTreeContentProvider2, ITreePathContentProvider {

	private static final TreePath[] NO_PATHS = new TreePath[0];

	private final ITreeContentProvider contentProvider;

	private StructuredViewer viewer;

	SafeDelegateTreeContentProvider(ITreeContentProvider aContentProvider) {
		super();
		contentProvider = aContentProvider;
	}
	
	/**
	 * @return true if the underlying content provider implements IPipelinedTreeContentProvider
	 */
	public boolean isPipelined() {
		return contentProvider instanceof IPipelinedTreeContentProvider;
	}

	/**
	 * @return true if the underlying content provider implements IPipelinedTreeContentProviderHasChildren
	 */
	public boolean isPipelinedHasChildren() {
		return contentProvider instanceof IPipelinedTreeContentProvider2;
	}

	/**
	 * @return true if the underlying content provider implements ITreePathContentProvider
	 */
	public boolean isTreePath() {
		return contentProvider instanceof ITreePathContentProvider;
	}

	/**
	 * 
	 */
	@Override
	public void dispose() {
		SafeRunner.run(new NavigatorSafeRunnable() {
			@Override
			public void run() throws Exception {
				contentProvider.dispose(); 
			}
		});
		
	}

	@Override
	public boolean equals(Object anObject) {
		return contentProvider.equals(anObject);
	}

	@Override
	public Object[] getChildren(Object aParentElement) {
		if (aParentElement instanceof TreePath) {
			TreePath tp = (TreePath) aParentElement;
			return getChildren(tp);
		}
		Object[] children = contentProvider.getChildren(aParentElement);
		return children;
	}

	@Override
	public Object[] getElements(Object anInputElement) {
		Object[] elements = contentProvider.getElements(anInputElement);
		return elements;
	}

	@Override
	public Object getParent(Object anElement) {
		return contentProvider.getParent(anElement);
	}

	@Override
	public boolean hasChildren(Object anElement) {
		return contentProvider.hasChildren(anElement);
	}

	@Override
	public int hashCode() {
		return contentProvider.hashCode();
	}

	@Override
	public void inputChanged(final Viewer aViewer, final Object anOldInput, final Object aNewInput) {
		viewer = (StructuredViewer) aViewer;
		
		SafeRunner.run(new NavigatorSafeRunnable() {
			@Override
			public void run() throws Exception {
				contentProvider.inputChanged(aViewer, anOldInput, aNewInput);
			}
		});
	}

	@Override
	public String toString() {
		return contentProvider.toString();
	}

	/**
	 * 
	 * @return The real content provider.
	 */
	public ITreeContentProvider getDelegateContentProvider() {
		return contentProvider;
	}

	@Override
	public void restoreState(IMemento aMemento) {
		if (contentProvider != null && contentProvider instanceof IMementoAware) {
			((IMementoAware) contentProvider).restoreState(aMemento);
		}

	}

	@Override
	public void saveState(IMemento aMemento) {
		if (contentProvider != null && contentProvider instanceof IMementoAware) {
			((IMementoAware) contentProvider).saveState(aMemento);
		}

	}

	@Override
	public void init(ICommonContentExtensionSite aConfig) {
		if (contentProvider instanceof ICommonContentProvider) {
			((ICommonContentProvider) contentProvider).init(aConfig);
		}
	}

	@Override
	public void getPipelinedChildren(Object aParent, Set theCurrentChildren) {
		if (contentProvider instanceof IPipelinedTreeContentProvider) {
			((IPipelinedTreeContentProvider) contentProvider)
					.getPipelinedChildren(aParent, theCurrentChildren);
		}

	}

	@Override
	public void getPipelinedElements(Object anInput, Set theCurrentElements) {
		if (contentProvider instanceof IPipelinedTreeContentProvider) {
			((IPipelinedTreeContentProvider) contentProvider)
					.getPipelinedElements(anInput, theCurrentElements);
		}
	}

	@Override
	public Object getPipelinedParent(Object anObject, Object aSuggestedParent) {
		if (contentProvider instanceof IPipelinedTreeContentProvider) {
			return ((IPipelinedTreeContentProvider) contentProvider)
					.getPipelinedParent(anObject, aSuggestedParent);
		}
		return anObject;
	}

	@Override
	public PipelinedShapeModification interceptAdd(
			PipelinedShapeModification anAddModification) { 
		if (contentProvider instanceof IPipelinedTreeContentProvider) {
			return ((IPipelinedTreeContentProvider) contentProvider)
					.interceptAdd(anAddModification);
		}
		return anAddModification;
	}

	@Override
	public PipelinedShapeModification interceptRemove(
			PipelinedShapeModification aRemoveModification) { 
		if (contentProvider instanceof IPipelinedTreeContentProvider) {
			return ((IPipelinedTreeContentProvider) contentProvider)
					.interceptRemove(aRemoveModification);
		}
		return aRemoveModification;
	}

	@Override
	public boolean interceptRefresh(
			PipelinedViewerUpdate aRefreshSynchronization) {
		if (contentProvider instanceof IPipelinedTreeContentProvider) {
			return ((IPipelinedTreeContentProvider) contentProvider)
					.interceptRefresh(aRefreshSynchronization);
		}
		return false;
	}

	@Override
	public boolean interceptUpdate(
			PipelinedViewerUpdate anUpdateSynchronization) {
		if (contentProvider instanceof IPipelinedTreeContentProvider) {
			return ((IPipelinedTreeContentProvider) contentProvider)
					.interceptUpdate(anUpdateSynchronization);
		}
		return false;
	}

	@Override
	public Object[] getChildren(TreePath parentPath) {
		if (contentProvider instanceof ITreePathContentProvider) {
			ITreePathContentProvider tpcp = (ITreePathContentProvider) contentProvider;
			Object[] children = tpcp.getChildren(parentPath);
			return children;
		}
		return getChildren(parentPath.getLastSegment());
	}

	@Override
	public boolean hasChildren(TreePath path) {
		if (contentProvider instanceof ITreePathContentProvider) {
			ITreePathContentProvider tpcp = (ITreePathContentProvider) contentProvider;
			return tpcp.hasChildren(path);
		}
		return hasChildren(path.getLastSegment());
	}

	@Override
	public TreePath[] getParents(Object element) {
		if (contentProvider instanceof ITreePathContentProvider) {
			ITreePathContentProvider tpcp = (ITreePathContentProvider) contentProvider;
			return tpcp.getParents(element);
		}
		ArrayList segments = new ArrayList();
		Object parent = element;
		do {
			parent = contentProvider.getParent(parent);
			if (parent != null && parent != viewer.getInput())
				segments.add(0, parent);
		} while (parent != null && parent != viewer.getInput());
		if (!segments.isEmpty()) {
			// Loop backwards over the array to create the path.			
			return new TreePath[] { new TreePath(segments.toArray()) };
		}
		return NO_PATHS;
	}

	@Override
	public boolean hasPipelinedChildren(Object anInput, boolean currentHasChildren) {
		if (contentProvider instanceof IPipelinedTreeContentProvider2) {
			return ((IPipelinedTreeContentProvider2) contentProvider)
					.hasPipelinedChildren(anInput, currentHasChildren);
		}
		return currentHasChildren;
	}

}
