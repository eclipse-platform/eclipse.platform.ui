/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui.util;

import java.text.MessageFormat;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import org.eclipse.search.ui.ISearchResultViewEntry;

import org.eclipse.search.internal.ui.SearchMessages;


public class FileLabelProvider extends DecoratingLabelProvider {
		
	public static final int SHOW_LABEL= 1;
	public static final int SHOW_LABEL_PATH= 2;
	public static final int SHOW_PATH_LABEL= 3;
	public static final int SHOW_PATH= 4;
	
	private static final String fgSeparatorFormat= SearchMessages.getString("FileLabelProvider.dashSeparated"); //$NON-NLS-1$
	
	private int fOrder;
	private String[] fArgs= new String[2];

	public FileLabelProvider(int orderFlag) {
		super(new WorkbenchLabelProvider(), getDecoratorManager());
		fOrder= orderFlag;
	}

	public void setOrder(int orderFlag) {
		fOrder= orderFlag;
	}
	
	public String getText(Object element) {
		if (!(element instanceof ISearchResultViewEntry))
			return ""; //$NON-NLS-1$

		IResource resource= ((ISearchResultViewEntry) element).getResource();
		String text= null;

		if (resource == null || !resource.exists())
			text= SearchMessages.getString("SearchResultView.removed_resource"); //$NON-NLS-1$
		
		else {
			IPath path= resource.getFullPath().removeLastSegments(1);
			if (path.getDevice() == null)
				path= path.makeRelative();
			if (fOrder == SHOW_LABEL || fOrder == SHOW_LABEL_PATH) {
				text= getLabelProvider().getText(resource);
				if (path != null && fOrder == SHOW_LABEL_PATH) {
					fArgs[0]= text;
					fArgs[1]= path.toString();
					text= MessageFormat.format(fgSeparatorFormat, fArgs);
				}
			} else {
				if (path != null)
					text= path.toString();
				else
					text= ""; //$NON-NLS-1$
				if (fOrder == SHOW_PATH_LABEL) {
					fArgs[0]= text;
					fArgs[1]= getLabelProvider().getText(resource);
					text= MessageFormat.format(fgSeparatorFormat, fArgs);
				}
			}
		}
		
		// Do the decoration
		if (getLabelDecorator() != null) {
			String decorated= getLabelDecorator().decorateText(text, element);
		if (decorated != null)
			return decorated;
		}
		return text;
	}

	public Image getImage(Object element) {
		if (!(element instanceof ISearchResultViewEntry))
			return null; //$NON-NLS-1$
		return super.getImage(((ISearchResultViewEntry) element).getResource());
	}

	private static ILabelDecorator getDecoratorManager() {
		return PlatformUI.getWorkbench().getDecoratorManager();
	}
}
