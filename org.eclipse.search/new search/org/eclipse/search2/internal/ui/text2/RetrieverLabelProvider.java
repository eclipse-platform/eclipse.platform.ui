/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Markus Schorn - initial API and implementation 
 *******************************************************************************/

package org.eclipse.search2.internal.ui.text2;

import java.text.MessageFormat;

import org.eclipse.core.resources.IFile;

import org.eclipse.swt.graphics.Image;

import org.eclipse.search.ui.text.AbstractTextSearchViewPage;

import org.eclipse.search.internal.ui.SearchPluginImages;
import org.eclipse.search.internal.ui.text.FileLabelProvider;

import org.eclipse.search2.internal.ui.SearchMessages;

public class RetrieverLabelProvider extends FileLabelProvider {
	private boolean fAppendContainer= false;

	public RetrieverLabelProvider(AbstractTextSearchViewPage page, int orderFlag) {
		super(page, orderFlag);
	}

	public void setAppendFileContainer(boolean val) {
		fAppendContainer= val;
	}

	public Image getImage(Object element) {
		if (element instanceof RetrieverLine) {
			return SearchPluginImages.get(SearchPluginImages.IMG_OBJ_TEXT_SEARCH_LINE);
		}
		return super.getImage(element);
	}

	public String getText(Object element) {
		if (element instanceof RetrieverLine) {
			RetrieverLine line= (RetrieverLine) element;
			StringBuffer buf= new StringBuffer();
			buf.append(String.valueOf(line.getLineNumber()));
			buf.append(": "); //$NON-NLS-1$
			buf.append(convertChars(line.getString()));
			return buf.toString();
		}
		if (element instanceof int[]) {
			int[] matchCount= (int[]) element;
			Integer hidden= new Integer(matchCount[0] - matchCount[1]);
			return MessageFormat.format(SearchMessages.RetrieverLabelProvider_FilterHidesMatches_label, new Object[] {hidden});
		}
		if (fAppendContainer && element instanceof IFile) {
			IFile file= (IFile) element;
			StringBuffer buf= new StringBuffer();
			buf.append(file.getName());
			buf.append(" - "); //$NON-NLS-1$
			buf.append(file.getParent().getFullPath().toString());
			return buf.toString();
		}
		return super.getText(element);
	}

	static String convertChars(CharSequence input) {
		StringBuffer result= new StringBuffer();
		for (int i= 0; i < input.length(); i++) {
			char c= input.charAt(i);
			switch (c) {
				case '\r':
					result.append("\\r");break; //$NON-NLS-1$
				case '\n':
					result.append("\\n");break; //$NON-NLS-1$
				case '\t':
					result.append("    ");break; //$NON-NLS-1$
				default:
					result.append(c);
					break;
			}
		}
		return result.toString();
	}
}
