package org.eclipse.ui.views.tasklist;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.IBasicPropertyConstants;

/**
 * Utility class for accessing marker attributes.
 */
/* package */ class MarkerUtil implements IMarkerConstants {
	private static Map imageDescriptors;
	private static ImageRegistry imageRegistry = new ImageRegistry();
	
	static {
		createImageDescriptors();
	}
/**
 * Don't allow instantiation.
 */
private MarkerUtil() {
}
/**
 * Creates an image descriptor for the image file referred to by the
 * given relative path (relative to the icons directory for the plug-in).
 */
static ImageDescriptor createImageDescriptor(String relativePath) {
	String iconPath = "icons/full/";//$NON-NLS-1$
	try {
		URL URL_BASIC = TaskList.getPlugin().getDescriptor().getInstallURL();
		URL url = new URL(URL_BASIC, iconPath + relativePath);
		return ImageDescriptor.createFromURL(url);
	} catch (MalformedURLException e) {
		Assert.isTrue(false);
		return null;
	}
}
/**
 * Creates the map of image descriptors.
 */
static void createImageDescriptors() {
	String CTOOL = "ctool16/";//$NON-NLS-1$
	String LOCAL = "clcl16/";//$NON-NLS-1$
	String LOCAL_GREY = "elcl16/";//$NON-NLS-1$
	String LOCAL_DISABLED = "dlcl16/";//$NON-NLS-1$
	String OBJ = "obj16/";//$NON-NLS-1$
	
	imageDescriptors = new HashMap(51);
	imageDescriptors.put("copy", 			createImageDescriptor(CTOOL + "copy_edit.gif"));//$NON-NLS-2$//$NON-NLS-1$
	imageDescriptors.put("paste", 			createImageDescriptor(CTOOL + "paste_edit.gif"));//$NON-NLS-2$//$NON-NLS-1$
	imageDescriptors.put("task", 			createImageDescriptor(OBJ + "taskmrk_tsk.gif"));//$NON-NLS-2$//$NON-NLS-1$
	imageDescriptors.put("error", 			createImageDescriptor(OBJ + "error_tsk.gif"));//$NON-NLS-2$//$NON-NLS-1$
	imageDescriptors.put("warn", 			createImageDescriptor(OBJ + "warn_tsk.gif"));//$NON-NLS-2$//$NON-NLS-1$
	imageDescriptors.put("info", 			createImageDescriptor(OBJ + "info_tsk.gif"));//$NON-NLS-2$//$NON-NLS-1$
	imageDescriptors.put("hprio", 			createImageDescriptor(OBJ + "hprio_tsk.gif"));//$NON-NLS-2$//$NON-NLS-1$
	imageDescriptors.put("lprio", 			createImageDescriptor(OBJ + "lprio_tsk.gif"));//$NON-NLS-2$//$NON-NLS-1$
	imageDescriptors.put("complete_tsk", 	createImageDescriptor(OBJ + "complete_tsk.gif"));//$NON-NLS-2$//$NON-NLS-1$
	imageDescriptors.put("incomplete_tsk",	createImageDescriptor(OBJ + "incomplete_tsk.gif"));//$NON-NLS-2$//$NON-NLS-1$
	imageDescriptors.put("gotoobj", 		createImageDescriptor(LOCAL + "gotoobj_tsk.gif"));//$NON-NLS-2$//$NON-NLS-1$
	imageDescriptors.put("gotoobj_grey", 	createImageDescriptor(LOCAL_GREY + "gotoobj_tsk.gif"));//$NON-NLS-2$//$NON-NLS-1$
	imageDescriptors.put("addtsk", 			createImageDescriptor(LOCAL + "addtsk_tsk.gif"));//$NON-NLS-2$//$NON-NLS-1$
	imageDescriptors.put("addtsk_grey", 	createImageDescriptor(LOCAL_GREY + "addtsk_tsk.gif"));//$NON-NLS-2$//$NON-NLS-1$
	imageDescriptors.put("addtsk_disabled", createImageDescriptor(LOCAL_DISABLED + "addtsk_tsk.gif"));//$NON-NLS-2$//$NON-NLS-1$
	imageDescriptors.put("remtsk", 			createImageDescriptor(LOCAL + "remtsk_tsk.gif"));//$NON-NLS-2$//$NON-NLS-1$
	imageDescriptors.put("remtsk_grey", 	createImageDescriptor(LOCAL_GREY + "remtsk_tsk.gif"));//$NON-NLS-2$//$NON-NLS-1$
	imageDescriptors.put("remtsk_disabled",	createImageDescriptor(LOCAL_DISABLED + "remtsk_tsk.gif"));//$NON-NLS-2$//$NON-NLS-1$
	imageDescriptors.put("showcomplete", 	createImageDescriptor(LOCAL + "showcomplete_tsk.gif"));//$NON-NLS-2$//$NON-NLS-1$
	imageDescriptors.put("selected_mode", 	createImageDescriptor(LOCAL + "selected_mode.gif"));//$NON-NLS-2$//$NON-NLS-1$
	imageDescriptors.put("selected_mode_grey", createImageDescriptor(LOCAL_GREY + "selected_mode.gif"));//$NON-NLS-2$//$NON-NLS-1$
	imageDescriptors.put("selected_mode_disabled", createImageDescriptor(LOCAL_DISABLED + "selected_mode.gif"));//$NON-NLS-2$//$NON-NLS-1$
	imageDescriptors.put("showchild_mode",	createImageDescriptor(LOCAL + "showchild_mode.gif"));//$NON-NLS-2$//$NON-NLS-1$
	imageDescriptors.put("showchild_mode_grey", createImageDescriptor(LOCAL_GREY + "showchild_mode.gif"));//$NON-NLS-2$//$NON-NLS-1$
	imageDescriptors.put("showchild_mode_disabled",	createImageDescriptor(LOCAL_DISABLED + "showchild_mode.gif"));//$NON-NLS-2$//$NON-NLS-1$
	imageDescriptors.put("showerr_grey", 	createImageDescriptor(LOCAL_GREY + "showerr_tsk.gif"));//$NON-NLS-2$//$NON-NLS-1$
	imageDescriptors.put("showerr_disabled", createImageDescriptor(LOCAL_DISABLED + "showerr_tsk.gif"));//$NON-NLS-2$//$NON-NLS-1$
	imageDescriptors.put("showwarn_grey", 	createImageDescriptor(LOCAL_GREY + "showwarn_tsk.gif"));//$NON-NLS-2$//$NON-NLS-1$
	imageDescriptors.put("showwarn_disabled", createImageDescriptor(LOCAL_DISABLED + "showwarn_tsk.gif"));//$NON-NLS-2$//$NON-NLS-1$
	imageDescriptors.put("showtsk_grey",	createImageDescriptor(LOCAL_GREY + "showtsk_tsk.gif"));//$NON-NLS-2$//$NON-NLS-1$
	imageDescriptors.put("showtsk_disabled", createImageDescriptor(LOCAL_DISABLED + "showtsk_tsk.gif"));//$NON-NLS-2$//$NON-NLS-1$
	imageDescriptors.put("delete_edit", 	createImageDescriptor(CTOOL + "delete_edit.gif"));//$NON-NLS-2$//$NON-NLS-1$
	imageDescriptors.put("filter", 			createImageDescriptor(LOCAL + "filter_ps.gif"));//$NON-NLS-2$//$NON-NLS-1$
}
/**
 * Returns the ending character offset of the given marker.
 */
public static int getCharEnd(IMarker marker) {
	return marker.getAttribute(IMarker.CHAR_END, -1);
}
/**
 * Returns the starting character offset of the given marker.
 */
public static int getCharStart(IMarker marker) {
	return marker.getAttribute(IMarker.CHAR_START, -1);
}
/**
 * Returns the icon to be used in the tasklist
 * for the complete state of a task. Returns null
 * for markers that are not tasks.
 */
public static Image getCompleteImage(IMarker marker) {
	if (isMarkerType(marker, IMarker.TASK)) {
		if (isComplete(marker))
			return getImage("complete_tsk");//$NON-NLS-1$
		else
			return getImage("incomplete_tsk");//$NON-NLS-1$
	}
	return null;
}

/**
 * Returns the text to be used for the complete state of a task. 
 * Returns the empty string for markers that are not tasks.
 */
public static String getCompleteText(IMarker marker) {
	if (isMarkerType(marker, IMarker.TASK)) {
		if (isComplete(marker)) 
			return TaskListMessages.getString("TaskList.completed"); //$NON-NLS-1$
		else	
			return TaskListMessages.getString("TaskList.notCompleted"); //$NON-NLS-1$
	}
	return ""; //$NON-NLS-1$
}

/**
 * Returns the text to be used for the kind of marker.
 */
public static String getKindText(IMarker marker) {
	if (isMarkerType(marker, IMarker.TASK)) {
		return TaskListMessages.getString("TaskList.task"); //$NON-NLS-1$
	}
	switch (getSeverity(marker)) {
		case IMarker.SEVERITY_ERROR:
			return TaskListMessages.getString("TaskList.error"); //$NON-NLS-1$
		case IMarker.SEVERITY_WARNING:
			return TaskListMessages.getString("TaskList.warning"); //$NON-NLS-1$
		case IMarker.SEVERITY_INFO:
			return TaskListMessages.getString("TaskList.info"); //$NON-NLS-1$
	}
	return ""; //$NON-NLS-1$
}

/**
 * Returns the container name if it is defined, or empty string if not.
 */
public static String getContainerName(IMarker marker) {
	// taking substring from resource's path string is 40x faster 
	// than getting relative path string from resource's parent
	String path = marker.getResource().getFullPath().toString();
	int i = path.lastIndexOf(IPath.SEPARATOR);
	if (i == 0)
		return ""; //$NON-NLS-1$
	return path.substring(1, i);
}
	
/**
 * Returns the image with the given key, or <code>null</code> if not found.
 */
static Image getImage(String key) {
	Image image = (Image) imageRegistry.get(key);
	if (image == null) {
		ImageDescriptor desc = getImageDescriptor(key);
		if (desc != null) {
			image = desc.createImage(false);
			if (image == null) {
				System.err.println("TaskList: Error creating image for " + key);//$NON-NLS-1$
			}
			imageRegistry.put(key, image);
		}
	}
	return image;
}
/**
 * Returns the image that should be used to visually represent
 * the marker, based on its type and priority.
 */
static public Image getImage(IMarker marker) {
	if (isMarkerType(marker, IMarker.PROBLEM)) {
		switch (getSeverity(marker)) {
			case IMarker.SEVERITY_ERROR:
				return getImage("error");//$NON-NLS-1$
			case IMarker.SEVERITY_WARNING:
				return getImage("warn");//$NON-NLS-1$
			case IMarker.SEVERITY_INFO:
				return getImage("info");//$NON-NLS-1$
		}
	}
	else if (isMarkerType(marker, IMarker.TASK)) {
		return getImage("task");//$NON-NLS-1$
	}
	return null;
}
/**
 * Returns the image descriptor with the given key, or <code>null</code> if not found.
 */
static ImageDescriptor getImageDescriptor(String key) {
	ImageDescriptor desc = (ImageDescriptor) imageDescriptors.get(key);
	if (desc == null) {
		System.err.println("TaskList: No image descriptor for " + key); //$NON-NLS-1$
	}
	return desc;
}

/**
 * Returns the text for the line and location column given the marker.
 */
public static String getLineAndLocation(IMarker marker) {
	int lineNumber = getLineNumber(marker);
	String location = getLocation(marker);
	return getLineAndLocation(lineNumber, location);
}

/**
 * Returns the text for the line and location column given the line number and location text.
 */
public static String getLineAndLocation(int lineNumber, String location) {
	if (lineNumber == -1) {
		if (location.equals("")) {//$NON-NLS-1$
			return "";//$NON-NLS-1$
		}
		else {
			return location;
		}
	}
	else {
		if (location.equals("")) {//$NON-NLS-1$
			return TaskListMessages.format("TaskList.line", new Object[] { new Integer(lineNumber) }); //$NON-NLS-1$
		}
		else {
			return TaskListMessages.format("TaskList.lineAndLocation", new Object[] { new Integer(lineNumber), location }); //$NON-NLS-1$
		}
	}
}
/**
 * Returns the line number of the given marker.
 */
public static int getLineNumber(IMarker marker) {
	return marker.getAttribute(IMarker.LINE_NUMBER, -1);
}
/**
 * Returns the text for the location field.
 */
public static String getLocation(IMarker marker) {
	return marker.getAttribute(IMarker.LOCATION, "");//$NON-NLS-1$
}
/**
 * Returns the message attribute of the given marker,
 * or the empty string if the message attribute is not defined.
 */
public static String getMessage(IMarker marker) {
	return marker.getAttribute(IMarker.MESSAGE, "");//$NON-NLS-1$
}
/**
 * Returns the numeric value of the given string, which is assumed to represent a numeric value.
 *
 * @return <code>true</code> if numeric, <code>false</code> if not
 */
static public int getNumericValue(String value) {
	boolean negative = false;
	int i = 0;
	int len = value.length();
	
	// skip any leading '#'
	// workaround for 1GCE69U: ITPJCORE:ALL - Java problems should not have '#' in location.
	if (i < len && value.charAt(i) == '#') 
		++i;

	if (i < len && value.charAt(i) == '-') {
		negative = true;
		++i;
	}
	
	int result = 0;
	while (i < len) {
		int digit = Character.digit(value.charAt(i++), 10);
		if (digit < 0) {
			return result;
		}
		result = result * 10 + digit;
	}
	if (negative) {
		result = -result;
	}
	return result;
}
/**
 * Returns the priority of the given marker.  Default is PRIORITY_NORMAL.
 */
public static int getPriority(IMarker marker) {
	return marker.getAttribute(IMarker.PRIORITY, IMarker.PRIORITY_NORMAL);
}
/**
 * Returns an image that should visually represent marker's priority
 * in the tasklist.
 */
public static Image getPriorityImage(IMarker marker) {
	switch (getPriority(marker)) {
		case IMarker.PRIORITY_HIGH:
			return getImage("hprio");//$NON-NLS-1$
		case IMarker.PRIORITY_NORMAL:
			return null;  // no image for normal priority
		case IMarker.PRIORITY_LOW:
			return getImage("lprio");//$NON-NLS-1$
	}
	return null;
}

/**
 * Returns the text for the given marker's priority.
 */
public static String getPriorityText(IMarker marker) {
	switch (getPriority(marker)) {
		case IMarker.PRIORITY_HIGH:
			return TaskListMessages.getString("TaskList.high"); //$NON-NLS-1$
		case IMarker.PRIORITY_NORMAL:
			return "";  //$NON-NLS-1$
		case IMarker.PRIORITY_LOW:
			return TaskListMessages.getString("TaskList.low"); //$NON-NLS-1$
	}
	return ""; //$NON-NLS-1$		
}

/**
 * Implements IProvider interface by supporting a number of
 * properties required for visual representation of markers
 * in the tasklist.
 */

static public Object getProperty(Object object, String key) {
	IMarker marker = (IMarker) object;
	
	// optimizations:
	// - check properties needed for TaskListLabelProvider first,
	//   then those needed for cell modifiers
	// - use == instead of equals for efficiency
	if (IBasicPropertyConstants.P_IMAGE == key) {
		return getImage(marker);
	}
	if (IMarkerConstants.P_COMPLETE_IMAGE == key) {
		return getCompleteImage(marker);
	}
	if (IMarkerConstants.P_PRIORITY_IMAGE == key) {
		return getPriorityImage(marker);
	}
	if (IMarker.MESSAGE == key) {
		return getMessage(marker);
	}
	if (IMarkerConstants.P_RESOURCE_NAME == key) {
		return getResourceName(marker);
	}
	if (IMarkerConstants.P_CONTAINER_NAME == key) {
		return getContainerName(marker);
	}
	if (IMarkerConstants.P_LINE_AND_LOCATION == key) {
		return getLineAndLocation(marker);
	}
	if (IMarker.PRIORITY == key) {
		// this property is used only by cell editor, where order is High, Normal, Low
		return new Integer(IMarker.PRIORITY_HIGH-getPriority(marker));
	}
	if (IMarker.DONE == key) {
		return isComplete(marker) ? Boolean.TRUE : Boolean.FALSE;
	}
	if (IBasicPropertyConstants.P_TEXT == key) {
		return getMessage(marker);
	}
	return null;
}
/**
 * Returns name if it is defined, or
 * blank string if not.
 */
public static String getResourceName(IMarker marker) {
	return marker.getResource().getName();
}
/**
 * Returns the severity of the given marker.  Default is SEVERITY_WARNING.
 */
public static int getSeverity(IMarker marker) {
	return marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
}
/**
 * A helper method that returns true if the given
 * marker is marked as complete in the tasklist.
 * Only tasks can be complete.
 */
public static boolean isComplete(IMarker marker) {
	return marker.getAttribute(IMarker.DONE, false);
}
/**
 * Returns whether the given marker is of the given type (either directly or indirectly).
 */
public static boolean isMarkerType(IMarker marker, String type) {
	try {
		return marker.isSubtypeOf(type);
	}
	catch (CoreException e) {
		return false;
	}
}
/**
 * Returns whether the given string represents a numeric value.
 *
 * @return <code>true</code> if numeric, <code>false</code> if not
 */
static public boolean isNumeric(String value) {
	int i = 0;
	int len = value.length();
	
	// skip any leading '#'
	// workaround for 1GCE69U: ITPJCORE:ALL - Java problems should not have '#' in location.
	if (i < len && value.charAt(i) == '#') 
		++i;

	// skip any '-'
	if (i < len && value.charAt(i) == '-') 
		++i;
		
	// if no digits, consider it not to be numeric
	if (i == len) 
		return false;

	while (i < len) {
		if (Character.digit(value.charAt(i++), 10) == -1)
			return false;
	}
	return true;
}
}
