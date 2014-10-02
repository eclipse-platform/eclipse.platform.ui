/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.navigator.filters;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.navigator.extensions.NavigatorContentDescriptor;
import org.eclipse.ui.internal.navigator.extensions.NavigatorContentDescriptorManager;
import org.eclipse.ui.navigator.ICommonFilterDescriptor;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;
/**
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2 
 *
 */
public class CommonFilterLabelProvider implements ITableLabelProvider, ILabelProvider {

	private static final NavigatorContentDescriptorManager CONTENT_DESCRIPTOR_REGISTRY = NavigatorContentDescriptorManager.getInstance();

	 

	@Override
	public Image getImage(Object element) {
		if (element instanceof NavigatorContentDescriptor) {
			return CONTENT_DESCRIPTOR_REGISTRY.getImage(((INavigatorContentDescriptor) element).getId());
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof NavigatorContentDescriptor) {
			return ((INavigatorContentDescriptor) element).getName();
		} else if (element instanceof ICommonFilterDescriptor) {
			return ((ICommonFilterDescriptor) element).getName();
		}
		return null;
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {

	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {

	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		switch (columnIndex) {
			case 0 :
				return getImage(element);

		}
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		switch (columnIndex) {
			case 0 :
				return getText(element);
			case 1 : {
				if (element instanceof ICommonFilterDescriptor) {
					String d = ((ICommonFilterDescriptor) element).getDescription();
					return d == null ? "" : d; //$NON-NLS-1$
				}
			}
		}
		return ""; //$NON-NLS-1$
	}

}
