/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui.util;

import java.text.MessageFormat;
import org.eclipse.core.resources.IResource;

import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.viewers.LabelProvider;

import org.eclipse.ui.model.WorkbenchLabelProvider;

import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.ui.ISearchResultViewEntry;


public class FileLabelProvider extends LabelProvider {
		
	public static final int SHOW_LABEL= 1;
	public static final int SHOW_LABEL_PATH= 2;
	public static final int SHOW_PATH_LABEL= 3;
	public static final int SHOW_PATH= 4;
	
	private static final String fgSeparatorFormat= SearchMessages.getString("FileLabelProvider.dashSeparated");
	
	private int fOrder;
	private String[] fArgs= new String[2];

	private WorkbenchLabelProvider fWorkbenchLabelProvider= new WorkbenchLabelProvider();
	
	public FileLabelProvider(int orderFlag) {
		fOrder= orderFlag;
	}

	public void setOrder(int orderFlag) {
		fOrder= orderFlag;
	}
	
	public String getText(Object element) {
		if (!(element instanceof ISearchResultViewEntry))
			return ""; //$NON-NLS-1$
		IResource resource= ((ISearchResultViewEntry) element).getResource();

		if (resource == null || !resource.exists())
			return SearchMessages.getString("SearchResultView.removed_resource"); //$NON-NLS-1$
		
		IPath path= resource.getFullPath().removeLastSegments(1);
		if (path.getDevice() == null)
			path= path.makeRelative();
		if (fOrder == SHOW_LABEL || fOrder == SHOW_LABEL_PATH) {
			String resourceString= fWorkbenchLabelProvider.getText(resource);
			if (path != null && fOrder == SHOW_LABEL_PATH) {
				fArgs[0]= resourceString;
				fArgs[1]= path.toString();
				return MessageFormat.format(fgSeparatorFormat, fArgs);
			}
			else
				return resourceString;
		} else {
			String pathString;
			if (path != null)
				pathString= path.toString();
			else
				pathString= "";
			if (fOrder == SHOW_PATH_LABEL) {
				fArgs[0]= pathString;
				fArgs[1]= fWorkbenchLabelProvider.getText(resource);
				return MessageFormat.format(fgSeparatorFormat, fArgs);
			} else
			 return pathString;
		}
	}

	public Image getImage(Object element) {
		if (!(element instanceof ISearchResultViewEntry))
			return null; //$NON-NLS-1$
		return fWorkbenchLabelProvider.getImage(((ISearchResultViewEntry) element).getResource());
	}
}
