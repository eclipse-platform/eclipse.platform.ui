package org.eclipse.search.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.swt.graphics.Image;

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

	private static final String T_OBJS= "full/obj16/";
	private static final String T_OVR= "full/ovr16/";
	private static final String T_WIZBAN= "full/wizban/";
	private static final String T_CLCL= "full/clcl16/";
	private static final String T_CTOOL= "full/ctool16/";
	private static final String T_VIEW= "full/view16/";

	private static final String NAME_PREFIX= "org.eclipse.search.ui";
	private static final int    NAME_PREFIX_LENGTH= NAME_PREFIX.length();

	// Define image names
	public static final String IMG_CTOOL_SEARCH= NAME_PREFIX + "search.gif";

	public static final String IMG_CLCL_SEARCH_STOP= NAME_PREFIX + "search_stop.gif";
	public static final String IMG_CLCL_SEARCH_REM= NAME_PREFIX + "search_rem.gif";
	public static final String IMG_CLCL_SEARCH_REM_ALL= NAME_PREFIX + "search_remall.gif";
	public static final String IMG_CLCL_SEARCH_NEXT= NAME_PREFIX + "search_next.gif";
	public static final String IMG_CLCL_SEARCH_PREV= NAME_PREFIX + "search_prev.gif";
	public static final String IMG_CLCL_SEARCH_GOTO= NAME_PREFIX + "search_goto.gif";
	public static final String IMG_CLCL_SEARCH_SORT= NAME_PREFIX + "search_sortmatch.gif";
	public static final String IMG_CLCL_SEARCH_HISTORY= NAME_PREFIX + "search_history.gif";

	public static final String IMG_CVIEW_SEARCHRES= NAME_PREFIX + "searchres.gif";

	public static final String IMG_OBJS_TSEARCH= NAME_PREFIX + "tsearch_obj.gif";
	public static final String IMG_OBJS_TSEARCH_DPDN= NAME_PREFIX + "tsearch_dpdn_obj.gif";
	public static final String IMG_OBJS_SEARCHMARKER= NAME_PREFIX + "searchm_obj.gif";

	
	
	// Define images
	public static final ImageDescriptor DESC_CTOOL_SEARCH= createManaged(T_CTOOL, IMG_CTOOL_SEARCH);

	public static final ImageDescriptor DESC_CLCL_SEARCH_STOP= createManaged(T_CLCL, IMG_CLCL_SEARCH_STOP);
	public static final ImageDescriptor DESC_CLCL_SEARCH_REM= createManaged(T_CLCL, IMG_CLCL_SEARCH_REM);
	public static final ImageDescriptor DESC_CLCL_SEARCH_REM_ALL= createManaged(T_CLCL, IMG_CLCL_SEARCH_REM_ALL);
	public static final ImageDescriptor DESC_CLCL_SEARCH_NEXT= createManaged(T_CLCL, IMG_CLCL_SEARCH_NEXT);
	public static final ImageDescriptor DESC_CLCL_SEARCH_PREV= createManaged(T_CLCL, IMG_CLCL_SEARCH_PREV);
	public static final ImageDescriptor DESC_CLCL_SEARCH_GOTO= createManaged(T_CLCL, IMG_CLCL_SEARCH_GOTO);
	public static final ImageDescriptor DESC_CLCL_SEARCH_SORT= createManaged(T_CLCL, IMG_CLCL_SEARCH_SORT);
	public static final ImageDescriptor DESC_CLCL_SEARCH_HISTROY= createManaged(T_CLCL, IMG_CLCL_SEARCH_HISTORY);

	public static final ImageDescriptor DESC_CVIEW_SEARCHRES= createManaged(T_CLCL, IMG_CVIEW_SEARCHRES);
	
	public static final ImageDescriptor DESC_OBJS_TSEARCH= createManaged(T_OBJS, IMG_OBJS_TSEARCH);
	public static final ImageDescriptor DESC_OBJS_TSEARCH_DPDN= createManaged(T_OBJS, IMG_OBJS_TSEARCH_DPDN);
	public static final ImageDescriptor DESC_OBJS_SEARCHMARKER= createManaged(T_OBJS, IMG_OBJS_SEARCHMARKER);

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
}
