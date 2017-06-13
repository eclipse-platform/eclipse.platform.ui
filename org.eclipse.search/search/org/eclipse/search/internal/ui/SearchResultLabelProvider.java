/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.ui;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProvider;

import org.eclipse.ui.PlatformUI;

import org.eclipse.search.ui.ISearchResultViewEntry;

/**
 * @deprecated old search
 */
@Deprecated
class SearchResultLabelProvider extends LabelProvider {

	private static final String MATCHES_POSTFIX= " " + SearchMessages.SearchResultView_matches + ")";  //$NON-NLS-1$ //$NON-NLS-2$

	private ILabelProvider fLabelProvider;


	SearchResultLabelProvider(ILabelProvider provider) {
		fLabelProvider= provider;
	}

	@Override
	public String getText(Object element) {
		StringBuilder buf= new StringBuilder(getLabelProvider().getText(element));
		int count= ((ISearchResultViewEntry)element).getMatchCount();
		if (count > 1) {
			buf.append(" ("); //$NON-NLS-1$
			buf.append(count);
			buf.append(MATCHES_POSTFIX);
		}
		return buf.toString();
	}

	@Override
	public Image getImage(Object element) {
		return fLabelProvider.getImage(element);
	}

	// Don't dispose since label providers are reused.
	@Override
	public void dispose() {
	}

	ILabelProvider getLabelProvider() {
		return fLabelProvider;
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
		super.addListener(listener);
		fLabelProvider.addListener(listener);
		PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator().addListener(listener);
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return fLabelProvider.isLabelProperty(element, property);
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		super.removeListener(listener);
		fLabelProvider.removeListener(listener);
		PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator().removeListener(listener);
	}
}
