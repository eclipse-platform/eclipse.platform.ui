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
package org.eclipse.ui.navigator.internal.extensions;

import java.util.Set;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentProvider;
import org.eclipse.ui.navigator.IExtensionStateModel;
import org.eclipse.ui.navigator.IMementoAware;
import org.eclipse.ui.navigator.IPipelinedTreeContentProvider;
import org.eclipse.ui.navigator.PipelinedShapeModification;
import org.eclipse.ui.navigator.PipelinedViewerUpdate;
import org.eclipse.ui.navigator.internal.NavigatorContentService;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class NavigatorContentProvider implements IPipelinedTreeContentProvider {

	private final ITreeContentProvider contentProvider;
	private NavigatorContentService contentService;
	private NavigatorContentDescriptor descriptor; 

	/**
	 *  
	 */
	public NavigatorContentProvider(ITreeContentProvider aContentProvider, NavigatorContentDescriptor aDescriptor, NavigatorContentService theContentService) {
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

	/**
	 * @param aParentElement
	 * @return
	 */
	public Object[] getChildren(Object aParentElement) {
		Object[] children = contentProvider.getChildren(aParentElement);
		contentService.rememberContribution(descriptor, children);
		return children;
	}

	/**
	 * @param anInputElement
	 * @return
	 */
	public Object[] getElements(Object anInputElement) {
		Object[] elements = contentProvider.getElements(anInputElement);
		contentService.rememberContribution(descriptor, elements);
		return elements;
	}

	/**
	 * @param anElement
	 * @return
	 */
	public Object getParent(Object anElement) {
		return contentProvider.getParent(anElement);
	}

	/**
	 * @param anElement
	 * @return
	 */
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

	/**
	 * @param aViewer
	 * @param anOldInput
	 * @param aNewInput
	 */
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
	
	public ITreeContentProvider getDelegateContentProvider() {
		return contentProvider;
	}

	public void restoreState(IMemento aMemento) {
		if(contentProvider != null && contentProvider instanceof IMementoAware)
			((IMementoAware)contentProvider).restoreState(aMemento);
		
	}

	public void saveState(IMemento aMemento) { 
		if(contentProvider != null && contentProvider instanceof IMementoAware)
			((IMementoAware)contentProvider).saveState(aMemento);
		
	}

	public void init(IExtensionStateModel aStateModel, IMemento aMemento) {
		if(contentProvider instanceof ICommonContentProvider) {
			((ICommonContentProvider)contentProvider).init(aStateModel, aMemento);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#getPipelinedChildren(java.lang.Object, java.util.Set)
	 */
	public Set getPipelinedChildren(Object aParent, Set theCurrentChildren) {
		if(contentProvider instanceof IPipelinedTreeContentProvider)
			return ((IPipelinedTreeContentProvider)contentProvider).getPipelinedChildren(aParent, theCurrentChildren);
		return theCurrentChildren;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#getPipelinedElements(java.lang.Object, java.util.Set)
	 */
	public Set getPipelinedElements(Object anInput, Set theCurrentElements) {
		if(contentProvider instanceof IPipelinedTreeContentProvider)
			return ((IPipelinedTreeContentProvider)contentProvider).getPipelinedElements(anInput, theCurrentElements);
		return theCurrentElements;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#getPipelinedParent(java.lang.Object, java.lang.Object)
	 */
	public Object getPipelinedParent(Object anObject, Object aSuggestedParent) {
		if(contentProvider instanceof IPipelinedTreeContentProvider)
			return ((IPipelinedTreeContentProvider)contentProvider).getPipelinedParent(anObject, aSuggestedParent);
		return anObject;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#interceptAdd(org.eclipse.ui.navigator.PipelinedShapeModification)
	 */
	public PipelinedShapeModification interceptAdd(PipelinedShapeModification anAddModification) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#interceptRemove(org.eclipse.ui.navigator.PipelinedShapeModification)
	 */
	public PipelinedShapeModification interceptRemove(PipelinedShapeModification aRemoveModification) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#interceptRefresh(org.eclipse.ui.navigator.PipelinedViewerUpdate)
	 */
	public PipelinedViewerUpdate interceptRefresh(PipelinedViewerUpdate aRefreshSynchronization) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#interceptUpdate(org.eclipse.ui.navigator.PipelinedViewerUpdate)
	 */
	public PipelinedViewerUpdate interceptUpdate(PipelinedViewerUpdate anUpdateSynchronization) {
		// TODO Auto-generated method stub
		return null;
	}
	 
}
