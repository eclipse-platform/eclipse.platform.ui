/************************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.views.bookmarkexplorer;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Provides labels for the bookmark navigator table
 */
class BookmarkLabelProvider
	extends LabelProvider
	implements ITableLabelProvider {

	private Image image;

	public BookmarkLabelProvider(BookmarkNavigator view) {
		ImageDescriptor desc = view.getImageDescriptor("obj16/bkmrk_tsk.gif"); //$NON-NLS-1$
		image = desc.createImage();
	}
	/* (non-Javadoc)
	 * Method declared on LabelProvider.
	 */
	public void dispose() {
		if (image != null) {
			image.dispose();
			image = null;
		}
	}
	/* (non-Javadoc)
	 * Method declared on LabelProvider.
	 */
	public Image getImage(Object element) {
		return image;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (!(element instanceof IMarker))
			return ""; //$NON-NLS-1$
		IMarker marker = (IMarker) element;

		switch (columnIndex) {
			case BookmarkConstants.COLUMN_DESCRIPTION :
				return marker.getAttribute(IMarker.MESSAGE, ""); //$NON-NLS-1$
			case BookmarkConstants.COLUMN_RESOURCE :
				return marker.getResource().getName();
			case BookmarkConstants.COLUMN_FOLDER :
				return getContainerName(marker);
			case BookmarkConstants.COLUMN_LOCATION :
				{
					int line = marker.getAttribute(IMarker.LINE_NUMBER, -1);
					if (line == -1)
						return ""; //$NON-NLS-1$
					return BookmarkMessages.format("LineIndicator.text", new String[] {String.valueOf(line)});//$NON-NLS-1$
				}
		}
		return ""; //$NON-NLS-1$ 
	}

	public Image getColumnImage(Object element, int index) {
		if (index == BookmarkConstants.COLUMN_ICON)
			return image;
		return null;
	}

	/**
	 * Returns the container name if it is defined, or empty string if not.
	 */
	public static String getContainerName(IMarker marker) {
		IPath path = marker.getResource().getFullPath();
		int n = path.segmentCount() - 1;
		// n is the number of segments in container, not path
		if (n <= 0)
			return ""; //$NON-NLS-1$
		int len = 0;
		for (int i = 0; i < n; ++i)
			len += path.segment(i).length();
		// account for /'s
		if (n > 1)
			len += n - 1;
		StringBuffer sb = new StringBuffer(len);
		for (int i = 0; i < n; ++i) {
			if (i != 0)
				sb.append('/');
			sb.append(path.segment(i));
		}
		return sb.toString();
	}
}
