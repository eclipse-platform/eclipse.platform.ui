/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;

import org.eclipse.search.ui.ISearchResultViewEntry;

class SearchResultLabelProvider extends DecoratingLabelProvider {
	
	private static final String MATCHES_POSTFIX= " " + SearchMessages.getString("SearchResultView.matches") + ")"; //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$		

	private static class MatchCountDecorator extends LabelProvider implements ILabelDecorator {
		/*
		 * @see ILabelDecorator#decorateImage(Image, Object)
		 */
		public Image decorateImage(Image image, Object element) {
			return null;
		}
	
		/*
		 * @see ILabelDecorator#decorateText(String, Object)
		 */
		public String decorateText(String text, Object element) {
			StringBuffer buf= new StringBuffer(text);
			int count= ((ISearchResultViewEntry)element).getMatchCount();
			if (count > 1) {
				buf.append(" ("); //$NON-NLS-1$
				buf.append(count);
				buf.append(MATCHES_POSTFIX);
			}
			return buf.toString();			
		}
	}
	
	SearchResultLabelProvider(ILabelProvider provider, ILabelDecorator decorator) {
		super(provider, new MatchCountDecorator());
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
	
	// Don't dispose since label providers are reused.
	public void dispose() {
	}
}
