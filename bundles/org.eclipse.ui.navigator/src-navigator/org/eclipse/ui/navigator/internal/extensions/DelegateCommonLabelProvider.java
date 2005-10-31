/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.internal.extensions;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonLabelProvider;
import org.eclipse.ui.navigator.IExtensionStateModel;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class DelegateCommonLabelProvider implements ICommonLabelProvider {

	private final ILabelProvider delegateLabelProvider;

	/**
	 * <p>
	 * Requires a non-null label provider as the parameter.
	 * </p>
	 */
	public DelegateCommonLabelProvider(ILabelProvider aLabelProvider) {
		super();
		delegateLabelProvider = aLabelProvider;
	} 

	/**
	 * <p>
	 * No-op.
	 * </p>
	 * 
	 * @see org.eclipse.ui.navigator.ICommonLabelProvider#initialize(java.lang.String)
	 */
	public void init(IExtensionStateModel aStateModel, ITreeContentProvider aContentProvider) {
	}

	/**
	 * <p>
	 * Returns <b>null </b>, forcing the CommonNavigator to provide the default message.
	 * </p>
	 * 
	 * @see org.eclipse.ui.navigator.ICommonLabelProvider#getDescription(java.lang.Object)
	 */
	public String getDescription(Object element) {
		return null;
	}

	/**
	 * @param listener
	 */
	public void addListener(ILabelProviderListener listener) {
		delegateLabelProvider.addListener(listener);
	}

	/**
	 *  
	 */
	public void dispose() {
		delegateLabelProvider.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return delegateLabelProvider.equals(obj);
	}

	/**
	 * @param element
	 * @return
	 */
	public Image getImage(Object element) {
		return delegateLabelProvider.getImage(element);
	}

	/**
	 * @param element
	 * @return
	 */
	public String getText(Object element) {
		return delegateLabelProvider.getText(element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return delegateLabelProvider.hashCode();
	}

	/**
	 * @param element
	 * @param property
	 * @return
	 */
	public boolean isLabelProperty(Object element, String property) {
		return delegateLabelProvider.isLabelProperty(element, property);
	}

	/**
	 * @param listener
	 */
	public void removeListener(ILabelProviderListener listener) {
		delegateLabelProvider.removeListener(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return delegateLabelProvider.toString();
	}

	public void initialize(String aViewerId, ITreeContentProvider aContentProvider) {
		// TODO Auto-generated method stub
		
	}

	public void restoreState(IMemento aMemento) {
		// TODO Auto-generated method stub
		
	}

	public void saveState(IMemento aMemento) {
		// TODO Auto-generated method stub
		
	}
}