/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ViewerSorter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import org.eclipse.search.internal.ui.util.ExceptionHandler;

/**
 * Proxy that represents a sorter.
 */
class SorterDescriptor {

	public final static String SORTER_TAG= "sorter";
	private final static String ID_ATTRIBUTE= "id";
	private final static String PAGE_ID_ATTRIBUTE= "pageId";
	private final static String ICON_ATTRIBUTE= "icon";
	private final static String CLASS_ATTRIBUTE= "class";
	private final static String LABEL_ATTRIBUTE= "label";
	private final static String TOOLTIP_ATTRIBUTE= "tooltip";
	
	private IConfigurationElement fElement;
	
	/**
	 * Creates a new sorter node with the given configuration element.
	 */
	public SorterDescriptor(IConfigurationElement element) {
		fElement= element;
	}

	/**
	 * Creates a new sorter from this node.
	 */
	public ViewerSorter createObject() {
		try {
			return (ViewerSorter)fElement.createExecutableExtension(CLASS_ATTRIBUTE);
		} catch (CoreException ex) {
			ExceptionHandler.handle(ex, SearchPlugin.getResourceBundle(), "Search.Error.createSorter.");			
			return null;
		} catch (ClassCastException ex) {
			ExceptionHandler.handle(ex, SearchPlugin.getResourceBundle(), "Search.Error.createSorter.");			
			return null;
		}
	}
	
	//---- XML Attribute accessors ---------------------------------------------
	
	/**
	 * Returns the sorter's id.
	 */
	public String getId() {
		return fElement.getAttribute(ID_ATTRIBUTE);
	}
	 
	/**
	 * Returns the sorter's image
	 */
	public ImageDescriptor getImage() {
		String imageName= fElement.getAttribute(ICON_ATTRIBUTE);
		if (imageName == null)
			return null;
		URL url;
		try {
			url= new URL(fElement.getDeclaringExtension().getDeclaringPluginDescriptor().getInstallURL(), imageName);
		} catch (java.net.MalformedURLException ex) {
			ExceptionHandler.handle(ex, SearchPlugin.getResourceBundle(), "Search.Error.createSorter.");			
			return null;
		}
		return ImageDescriptor.createFromURL(url);
	}

	/**
	 * Returns the sorter's label.
	 */
	public String getLabel() {
		return fElement.getAttribute(LABEL_ATTRIBUTE);
	}
	
	/**
	 * Returns the sorter's preferred size
	 */
	public String getToolTipText() {
		return fElement.getAttribute(TOOLTIP_ATTRIBUTE);
	}

	/**
	 * Returns the sorter's preferred size
	 */
	public String getPageId() {
		return fElement.getAttribute(PAGE_ID_ATTRIBUTE);
	}
}