/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
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
		try {
			fgIconLocation= new URL(SearchPlugin.getDefault().getDescriptor().getInstallURL(), "icons/");
		} catch (MalformedURLException ex) {
			ExceptionHandler.handle(ex, SearchPlugin.getResourceBundle(), "Search.Error.incorrectIconLocation.");
		}
	}

	// The plugin registry
	private final static ImageRegistry PLUGIN_REGISTRY= SearchPlugin.getDefault().getImageRegistry();

	private static final String T= "full/";

	public static final String T_OBJ= T + "obj16/";
	public static final String T_WIZBAN= T + "wizban/";
	public static final String T_LCL= "lcl16/";
	public static final String T_TOOL= "tool16/";
	public static final String T_VIEW= "view16/";

	private static final String NAME_PREFIX= "org.eclipse.search.ui.";
	private static final int    NAME_PREFIX_LENGTH= NAME_PREFIX.length();

	// Define image names
	public static final String IMG_TOOL_SEARCH= NAME_PREFIX + "search.gif";

	public static final String IMG_LCL_SEARCH_STOP= NAME_PREFIX + "search_stop.gif";
	public static final String IMG_LCL_SEARCH_REM= NAME_PREFIX + "search_rem.gif";
	public static final String IMG_LCL_SEARCH_REM_ALL= NAME_PREFIX + "search_remall.gif";
	public static final String IMG_LCL_SEARCH_NEXT= NAME_PREFIX + "search_next.gif";
	public static final String IMG_LCL_SEARCH_PREV= NAME_PREFIX + "search_prev.gif";
	public static final String IMG_LCL_SEARCH_GOTO= NAME_PREFIX + "search_goto.gif";
	public static final String IMG_LCL_SEARCH_SORT= NAME_PREFIX + "search_sortmatch.gif";
	public static final String IMG_LCL_SEARCH_HISTORY= NAME_PREFIX + "search_history.gif";

	public static final String IMG_VIEW_SEARCHRES= NAME_PREFIX + "searchres.gif";

	public static final String IMG_OBJ_TSEARCH= NAME_PREFIX + "tsearch_obj.gif";
	public static final String IMG_OBJ_TSEARCH_DPDN= NAME_PREFIX + "tsearch_dpdn_obj.gif";
	public static final String IMG_OBJ_SEARCHMARKER= NAME_PREFIX + "searchm_obj.gif";

	
	
	// Define images
	public static final ImageDescriptor DESC_OBJ_TSEARCH= createManaged(T_OBJ, IMG_OBJ_TSEARCH);
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
			ExceptionHandler.handle(ex, SearchPlugin.getResourceBundle(), "Search.Error.incorrectIconLocation.");
			return null;
		}
	}

	/**
	 * Sets all available image descriptors for the given action.
	 */	
	public static void setImageDescriptors(IAction action, String type, String relPath) {
		relPath= relPath.substring(NAME_PREFIX_LENGTH);
		action.setDisabledImageDescriptor(create(T + "d" + type, relPath));
		action.setHoverImageDescriptor(create(T + "c" + type, relPath));
		action.setImageDescriptor(create(T + "e" + type, relPath));
	}
}
