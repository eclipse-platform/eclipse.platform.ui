/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProvider;

import org.eclipse.ui.PlatformUI;

import org.eclipse.search.ui.ISearchResultViewEntry;

class SearchResultLabelProvider extends LabelProvider {
	
	private static final String MATCHES_POSTFIX= " " + SearchMessages.getString("SearchResultView.matches") + ")"; //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$		

	private ILabelProvider fLabelProvider;

	
	SearchResultLabelProvider(ILabelProvider provider) {
		fLabelProvider= provider;
	}

	public String getText(Object element) {
		StringBuffer buf= new StringBuffer(getLabelProvider().getText(element));
		int count= ((ISearchResultViewEntry)element).getMatchCount();
		if (count > 1) {
			buf.append(" ("); //$NON-NLS-1$
			buf.append(count);
			buf.append(MATCHES_POSTFIX);
		}
		return buf.toString();			
	}
	
	public Image getImage(Object element) {
		return fLabelProvider.getImage(element);
	}
	
	// Don't dispose since label providers are reused.
	public void dispose() {
	}

	ILabelProvider getLabelProvider() {
		return fLabelProvider;
	}

	public void addListener(ILabelProviderListener listener) {
		super.addListener(listener);
		fLabelProvider.addListener(listener);
		PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator().addListener(listener);
	}

	public boolean isLabelProperty(Object element, String property) {
		return fLabelProvider.isLabelProperty(element, property);
	}

	public void removeListener(ILabelProviderListener listener) {
		super.removeListener(listener);
		fLabelProvider.removeListener(listener);
		PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator().removeListener(listener);
	}
}
