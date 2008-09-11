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
package org.eclipse.search.internal.ui;

import org.osgi.framework.Bundle;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ViewerSorter;

import org.eclipse.search.internal.ui.util.ExceptionHandler;

/**
 * Proxy that represents a sorter.
 */
class SorterDescriptor {

	public final static String SORTER_TAG= "sorter"; //$NON-NLS-1$
	private final static String ID_ATTRIBUTE= "id"; //$NON-NLS-1$
	private final static String PAGE_ID_ATTRIBUTE= "pageId"; //$NON-NLS-1$
	private final static String ICON_ATTRIBUTE= "icon"; //$NON-NLS-1$
	private final static String CLASS_ATTRIBUTE= "class"; //$NON-NLS-1$
	private final static String LABEL_ATTRIBUTE= "label"; //$NON-NLS-1$
	private final static String TOOLTIP_ATTRIBUTE= "tooltip"; //$NON-NLS-1$

	private IConfigurationElement fElement;

	/**
	 * Creates a new sorter node with the given configuration element.
	 *
	 * @param element the configuration element
	 */
	public SorterDescriptor(IConfigurationElement element) {
		fElement= element;
	}

	/**
	 * Creates a new sorter from this node.
	 * @return new sorter
	 */
	public ViewerSorter createObject() {
		try {
			return (ViewerSorter)fElement.createExecutableExtension(CLASS_ATTRIBUTE);
		} catch (CoreException ex) {
			ExceptionHandler.handle(ex, SearchMessages.Search_Error_createSorter_title, SearchMessages.Search_Error_createSorter_message);
			return null;
		} catch (ClassCastException ex) {
			ExceptionHandler.displayMessageDialog(ex, SearchMessages.Search_Error_createSorter_title, SearchMessages.Search_Error_createSorter_message);
			return null;
		}
	}

	//---- XML Attribute accessors ---------------------------------------------

	/**
	 * Returns the sorter's id.
	 * @return the sorter's id.
	 */
	public String getId() {
		return fElement.getAttribute(ID_ATTRIBUTE);
	}

	/**
	 * Returns the sorter's image
	 * @return the sorter's image
	 */
	public ImageDescriptor getImage() {
		String imageName= fElement.getAttribute(ICON_ATTRIBUTE);
		if (imageName == null)
			return null;
		Bundle bundle = Platform.getBundle(fElement.getContributor().getName());
		return SearchPluginImages.createImageDescriptor(bundle, new Path(imageName), true);
	}

	/**
	 * Returns the sorter's label.
	 * @return the sorter's label.
	 */
	public String getLabel() {
		return fElement.getAttribute(LABEL_ATTRIBUTE);
	}

	/**
	 * Returns the sorter's tooltip.
	 * @return the sorter's tooltip.
	 */
	public String getToolTipText() {
		return fElement.getAttribute(TOOLTIP_ATTRIBUTE);
	}

	/**
	 * Returns the sorter's page id
	 * @return the page id
	 */
	public String getPageId() {
		return fElement.getAttribute(PAGE_ID_ATTRIBUTE);
	}
}
