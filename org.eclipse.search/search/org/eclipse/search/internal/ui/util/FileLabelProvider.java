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
package org.eclipse.search.internal.ui.util;

import com.ibm.icu.text.MessageFormat;

import org.eclipse.swt.graphics.Image;

import org.eclipse.core.runtime.IPath;

import org.eclipse.core.resources.IResource;

import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProvider;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.ui.ISearchResultViewEntry;

/**
 * @deprecated Old search view
 */
public class FileLabelProvider extends LabelProvider {

	public static final int SHOW_LABEL= 1;
	public static final int SHOW_LABEL_PATH= 2;
	public static final int SHOW_PATH_LABEL= 3;
	public static final int SHOW_PATH= 4;

	private static final String fgSeparatorFormat= SearchMessages.FileLabelProvider_dashSeparated;

	private WorkbenchLabelProvider fLabelProvider;
	private ILabelDecorator fDecorator;

	private int fOrder;
	private String[] fArgs= new String[2];

	public FileLabelProvider(int orderFlag) {
		fDecorator= PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator();
		fLabelProvider= new WorkbenchLabelProvider();
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
			text= SearchMessages.SearchResultView_removed_resource;

		else {
			IPath path= resource.getFullPath().removeLastSegments(1);
			if (path.getDevice() == null)
				path= path.makeRelative();
			if (fOrder == SHOW_LABEL || fOrder == SHOW_LABEL_PATH) {
				text= fLabelProvider.getText(resource);
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
					fArgs[1]= fLabelProvider.getText(resource);
					text= MessageFormat.format(fgSeparatorFormat, fArgs);
				}
			}
		}

		// Do the decoration
		if (fDecorator != null) {
			String decoratedText= fDecorator.decorateText(text, resource);
		if (decoratedText != null)
			return decoratedText;
		}
		return text;
	}

	public Image getImage(Object element) {
		if (!(element instanceof ISearchResultViewEntry))
			return null;

		IResource resource= ((ISearchResultViewEntry) element).getResource();
		Image image= fLabelProvider.getImage(resource);
		if (fDecorator != null) {
			Image decoratedImage= fDecorator.decorateImage(image, resource);
			if (decoratedImage != null)
				return decoratedImage;
		}
		return image;
	}

	public void dispose() {
		super.dispose();
		fLabelProvider.dispose();
	}

	public boolean isLabelProperty(Object element, String property) {
		return fLabelProvider.isLabelProperty(element, property);
	}

	public void removeListener(ILabelProviderListener listener) {
		super.removeListener(listener);
		fLabelProvider.removeListener(listener);
	}

	public void addListener(ILabelProviderListener listener) {
		super.addListener(listener);
		fLabelProvider.addListener(listener);
	}
}
