/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui;

import org.eclipse.swt.graphics.Image;

import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;

import org.eclipse.core.resources.IResource;

import org.eclipse.search.ui.ISearchResultViewEntry;

class SearchResultLabelProvider extends LabelProvider implements ILabelProvider {
	
	private static class FileLabelProvider extends LabelProvider {

		private WorkbenchLabelProvider fWorkbenchLabelProvider= new WorkbenchLabelProvider();
		
		public String getText(Object element) {
			if (!(element instanceof ISearchResultViewEntry))
				return ""; //$NON-NLS-1$
			IResource resource= ((ISearchResultViewEntry) element).getResource();
			// PR 1G47GDO
			if (resource == null)
				return SearchMessages.getString("SearchResultView.removed_resource"); //$NON-NLS-1$
			return fWorkbenchLabelProvider.getText(resource);
		}

		public Image getImage(Object element) {
			if (!(element instanceof ISearchResultViewEntry))
				return null; //$NON-NLS-1$
			return fWorkbenchLabelProvider.getImage(((ISearchResultViewEntry) element).getResource());
		}
	}

	private static final FileLabelProvider DEFAULT_LABEL_PROVIDER= new FileLabelProvider();
	private static final String MATCHES_POSTFIX= " " + SearchMessages.getString("SearchResultView.matches") + ")"; //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
	private static ILabelProvider fgLabelProvider= DEFAULT_LABEL_PROVIDER;
	
	public ILabelProvider getLabelProvider() {
		return fgLabelProvider;
	}
	
	public void setLabelProvider(ILabelProvider provider) {
		if (provider == null)
			provider= DEFAULT_LABEL_PROVIDER;
		fgLabelProvider= provider;
	}
	
	public String getText(Object rowElement) {
		StringBuffer text= new StringBuffer(fgLabelProvider.getText(rowElement));
		int count= ((ISearchResultViewEntry)rowElement).getMatchCount();
		if (count > 1) {
			text.append(" ("); //$NON-NLS-1$
			text.append(count);
			text.append(MATCHES_POSTFIX);
		}
		return text.toString();			
	}
	
	public Image getImage(Object rowElement) {
		return fgLabelProvider.getImage(rowElement);	
	}
}
