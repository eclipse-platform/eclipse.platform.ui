/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;

import org.eclipse.search.internal.ui.util.FileLabelProvider;
import org.eclipse.search.ui.ISearchResultViewEntry;


class SearchResultLabelProvider extends LabelProvider implements ILabelProvider {

	private static SearchResultLabelProvider fgInstance= new SearchResultLabelProvider();
	private static final FileLabelProvider DEFAULT_LABEL_PROVIDER= new FileLabelProvider(FileLabelProvider.SHOW_PATH_LABEL);
	private static final String MATCHES_POSTFIX= " " + SearchMessages.getString("SearchResultView.matches") + ")"; //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
	private static ILabelProvider fLabelProvider= DEFAULT_LABEL_PROVIDER;

	private SearchResultLabelProvider() {
	}

	public static SearchResultLabelProvider getInstance () {
		return fgInstance;
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
			text.append(" ("); //$NON-NLS-1$
			text.append(count);
			text.append(MATCHES_POSTFIX);
		}
		return text.toString();			
	}
	
	public Image getImage(Object rowElement) {
		return fLabelProvider.getImage(rowElement);	
	}
}
