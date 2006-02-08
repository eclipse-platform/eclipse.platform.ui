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
package org.eclipse.ui.internal.navigator.extensions;

import java.util.Set;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.internal.navigator.NavigatorContentService;
import org.eclipse.ui.navigator.ICommonContentProvider;
import org.eclipse.ui.navigator.IExtensionStateModel;
import org.eclipse.ui.navigator.IMementoAware;
import org.eclipse.ui.navigator.IPipelinedTreeContentProvider;
import org.eclipse.ui.navigator.PipelinedShapeModification;
import org.eclipse.ui.navigator.PipelinedViewerUpdate;

/**
 * @since 3.2
 */
public class SafeDelegateTreeContentProvider implements
		IPipelinedTreeContentProvider {

	private final ITreeContentProvider contentProvider;

	private NavigatorContentService contentService;

	private NavigatorContentDescriptor descriptor;

	SafeDelegateTreeContentProvider(ITreeContentProvider aContentProvider,
			NavigatorContentDescriptor aDescriptor,
			NavigatorContentService theContentService) {
		super();
		contentProvider = aContentProvider;
		contentService = theContentService;
		descriptor = aDescriptor;
	}

	/**
	 * 
	 */
	public void dispose() {
		contentProvider.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object anObject) {
		return contentProvider.equals(anObject);
	}

	public Object[] getChildren(Object aParentElement) {
		Object[] children = contentProvider.getChildren(aParentElement);
		contentService.rememberContribution(descriptor, children);
		return children;
	}

	public Object[] getElements(Object anInputElement) {
		Object[] elements = contentProvider.getElements(anInputElement);
		contentService.rememberContribution(descriptor, elements);
		return elements;
	}

	public Object getParent(Object anElement) {
		return contentProvider.getParent(anElement);
	}

	public boolean hasChildren(Object anElement) {
		return contentProvider.hasChildren(anElement);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return contentProvider.hashCode();
	}

	public void inputChanged(Viewer aViewer, Object anOldInput, Object aNewInput) {
		contentProvider.inputChanged(aViewer, anOldInput, aNewInput);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
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

	public void restoreState(IMemento aMemento) {
		if (contentProvider != null && contentProvider instanceof IMementoAware)
			((IMementoAware) contentProvider).restoreState(aMemento);

	}

	public void saveState(IMemento aMemento) {
		if (contentProvider != null && contentProvider instanceof IMementoAware)
			((IMementoAware) contentProvider).saveState(aMemento);

	}

	public void init(IExtensionStateModel aStateModel, IMemento aMemento) {
		if (contentProvider instanceof ICommonContentProvider) {
			((ICommonContentProvider) contentProvider).init(aStateModel,
					aMemento);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#getPipelinedChildren(java.lang.Object,
	 *      java.util.Set)
	 */
	public void getPipelinedChildren(Object aParent, Set theCurrentChildren) {
		if (contentProvider instanceof IPipelinedTreeContentProvider)
			((IPipelinedTreeContentProvider) contentProvider)
					.getPipelinedChildren(aParent, theCurrentChildren);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#getPipelinedElements(java.lang.Object,
	 *      java.util.Set)
	 */
	public void getPipelinedElements(Object anInput, Set theCurrentElements) {
		if (contentProvider instanceof IPipelinedTreeContentProvider)
			((IPipelinedTreeContentProvider) contentProvider)
					.getPipelinedElements(anInput, theCurrentElements);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#getPipelinedParent(java.lang.Object,
	 *      java.lang.Object)
	 */
	public Object getPipelinedParent(Object anObject, Object aSuggestedParent) {
		if (contentProvider instanceof IPipelinedTreeContentProvider)
			return ((IPipelinedTreeContentProvider) contentProvider)
					.getPipelinedParent(anObject, aSuggestedParent);
		return anObject;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#interceptAdd(org.eclipse.ui.navigator.PipelinedShapeModification)
	 */
	public PipelinedShapeModification interceptAdd(
			PipelinedShapeModification anAddModification) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#interceptRemove(org.eclipse.ui.navigator.PipelinedShapeModification)
	 */
	public PipelinedShapeModification interceptRemove(
			PipelinedShapeModification aRemoveModification) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#interceptRefresh(org.eclipse.ui.navigator.PipelinedViewerUpdate)
	 */
	public boolean interceptRefresh(
			PipelinedViewerUpdate aRefreshSynchronization) {
		if (contentProvider instanceof IPipelinedTreeContentProvider)
			return ((IPipelinedTreeContentProvider) contentProvider)
					.interceptRefresh(aRefreshSynchronization);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#interceptUpdate(org.eclipse.ui.navigator.PipelinedViewerUpdate)
	 */
	public boolean interceptUpdate(
			PipelinedViewerUpdate anUpdateSynchronization) {
		if (contentProvider instanceof IPipelinedTreeContentProvider)
			return ((IPipelinedTreeContentProvider) contentProvider)
					.interceptRefresh(anUpdateSynchronization);
		return false;
	}

}
