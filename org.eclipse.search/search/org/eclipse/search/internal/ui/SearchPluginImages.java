/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;

import org.eclipse.search.internal.ui.util.ExceptionHandler;

/**
 * Bundle of all images used by the Search UI plugin.
 */
public class SearchPluginImages {

	private static URL fgIconLocation;

	static {
		String pathSuffix= "icons/full/"; //$NON-NLS-1$
		try {
			fgIconLocation= new URL(SearchPlugin.getDefault().getDescriptor().getInstallURL(), pathSuffix);
		} catch (MalformedURLException ex) {
			ExceptionHandler.log(ex, SearchMessages.Search_Error_incorrectIconLocation_message); 
		}
	}

	// The plugin registry
	private final static ImageRegistry PLUGIN_REGISTRY= SearchPlugin.getDefault().getImageRegistry();

	public static final String T_OBJ= "obj16/"; //$NON-NLS-1$
	public static final String T_WIZBAN= "wizban/"; //$NON-NLS-1$
	public static final String T_LCL= "lcl16/"; //$NON-NLS-1$
	public static final String T_TOOL= "tool16/"; //$NON-NLS-1$
	public static final String T_VIEW= "view16/"; //$NON-NLS-1$

	private static final String NAME_PREFIX= "org.eclipse.search.ui."; //$NON-NLS-1$
	private static final int    NAME_PREFIX_LENGTH= NAME_PREFIX.length();

	// Define image names
	public static final String IMG_TOOL_SEARCH= NAME_PREFIX + "search.gif"; //$NON-NLS-1$

	public static final String IMG_LCL_SEARCH_REM= NAME_PREFIX + "search_rem.gif"; //$NON-NLS-1$
	public static final String IMG_LCL_SEARCH_REM_ALL= NAME_PREFIX + "search_remall.gif"; //$NON-NLS-1$
	public static final String IMG_LCL_SEARCH_NEXT= NAME_PREFIX + "search_next.gif"; //$NON-NLS-1$
	public static final String IMG_LCL_SEARCH_PREV= NAME_PREFIX + "search_prev.gif"; //$NON-NLS-1$
	public static final String IMG_LCL_SEARCH_GOTO= NAME_PREFIX + "search_goto.gif"; //$NON-NLS-1$
	public static final String IMG_LCL_SEARCH_SORT= NAME_PREFIX + "search_sortmatch.gif"; //$NON-NLS-1$
	public static final String IMG_LCL_SEARCH_HISTORY= NAME_PREFIX + "search_history.gif"; //$NON-NLS-1$
	public static final String IMG_LCL_SEARCH_FLAT_LAYOUT= NAME_PREFIX + "flatLayout.gif"; //$NON-NLS-1$
	public static final String IMG_LCL_SEARCH_HIERARCHICAL_LAYOUT= NAME_PREFIX + "hierarchicalLayout.gif"; //$NON-NLS-1$
	public static final String IMG_LCL_SEARCH_CANCEL= NAME_PREFIX + "stop.gif"; //$NON-NLS-1$
	public static final String IMG_LCL_SEARCH_COLLAPSE_ALL= NAME_PREFIX + "collapseall.gif"; //$NON-NLS-1$
	public static final String IMG_LCL_SEARCH_EXPAND_ALL= NAME_PREFIX + "expandall.gif"; //$NON-NLS-1$

	public static final String IMG_VIEW_SEARCHRES= NAME_PREFIX + "searchres.gif"; //$NON-NLS-1$

	public static final String IMG_OBJ_TSEARCH_DPDN= NAME_PREFIX + "tsearch_dpdn_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJ_SEARCHMARKER= NAME_PREFIX + "searchm_obj.gif"; //$NON-NLS-1$

	
	
	// Define images
	public static final ImageDescriptor DESC_OBJ_TSEARCH_DPDN= createManaged(T_OBJ, IMG_OBJ_TSEARCH_DPDN);
	public static final ImageDescriptor DESC_OBJ_SEARCHMARKER= createManaged(T_OBJ, IMG_OBJ_SEARCHMARKER);

	public static Image get(String key) {
		return PLUGIN_REGISTRY.get(key);
	}
	
	private static ImageDescriptor createManaged(String prefix, String name) {
			ImageDescriptor result= ImageDescriptor.createFromURL(makeIconFileURL(prefix, name.substring(NAME_PREFIX_LENGTH)));
			PLUGIN_REGISTRY.put(name, result);
			return result;
	}
	
	private static ImageDescriptor create(String prefix, String name) {
		return ImageDescriptor.createFromURL(makeIconFileURL(prefix, name));
	}
	
	private static URL makeIconFileURL(String prefix, String name) {
		StringBuffer buffer= new StringBuffer(prefix);
		buffer.append(name);
		try {
			return new URL(fgIconLocation, buffer.toString());
		} catch (MalformedURLException ex) {
			ExceptionHandler.log(ex, SearchMessages.Search_Error_incorrectIconLocation_message); 
			return null;
		}
	}

	/**
	 * Sets all available image descriptors for the given action.
	 */	
	public static void setImageDescriptors(IAction action, String type, String relPath) {
		relPath= relPath.substring(NAME_PREFIX_LENGTH);
		action.setDisabledImageDescriptor(create("d" + type, relPath)); //$NON-NLS-1$
		action.setHoverImageDescriptor(create("e" + type, relPath)); //$NON-NLS-1$
		action.setImageDescriptor(create("e" + type, relPath)); //$NON-NLS-1$
	}
}
