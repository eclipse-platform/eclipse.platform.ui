package org.eclipse.search.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;

import org.eclipse.core.resources.IResource;

import org.eclipse.search.ui.ISearchResultViewEntry;

class SearchResultLabelProvider extends LabelProvider implements ILabelProvider {
	
	private static class FileLabelProvider extends LabelProvider {

		private Image fImage= SearchPluginImages.get(SearchPluginImages.IMG_OBJS_TSEARCH_DPDN);
		
		public String getText(Object element) {
			if (!(element instanceof ISearchResultViewEntry))
				return "";
			
			IResource resource= ((ISearchResultViewEntry) element).getResource();

			// PR 1G47GDO
			if (resource == null)
				return SearchPlugin.getResourceString("SearchResultView.removed_resource");
		
			return ((IResource)resource).getLocation().lastSegment();
		}

		public Image getImage(Object element) {
			return fImage;
		}
	}


	private static final FileLabelProvider DEFAULT_LABEL_PROVIDER= new FileLabelProvider();
	private ILabelProvider fLabelProvider;
	
	public SearchResultLabelProvider() {
		fLabelProvider= DEFAULT_LABEL_PROVIDER;
	}
	
	public ILabelProvider getLabelProvider() {
		return fLabelProvider;
	}
	
	public void setLabelProvider(ILabelProvider provider) {
		if (provider == null)
			provider= DEFAULT_LABEL_PROVIDER;
		fLabelProvider= provider;
	}
	
	public String getText(Object rowElement) {
		StringBuffer text= new StringBuffer(fLabelProvider.getText(rowElement));
		int count= ((ISearchResultViewEntry)rowElement).getMatchCount();
		if (count > 1) {
			text.append(" (");
			text.append(count);
			text.append(" matches)");
		}
		return text.toString();			
	}
	
	public Image getImage(Object rowElement) {
		return fLabelProvider.getImage(rowElement);	
	}

	public void dispose() {
		if (fLabelProvider != DEFAULT_LABEL_PROVIDER) {
			fLabelProvider.dispose();
			setLabelProvider(null);
		}
		super.dispose();
	}
}
