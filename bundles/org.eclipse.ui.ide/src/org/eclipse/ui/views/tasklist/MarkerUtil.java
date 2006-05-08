/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.tasklist;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.views.tasklist.TaskListMessages;

/**
 * Utility class for accessing marker attributes.
 */
class MarkerUtil implements IMarkerConstants {
	
   private static Map imageDescriptors;

    private static ImageRegistry imageRegistry = new ImageRegistry();

    private static MessageFormat line = new MessageFormat(TaskListMessages.TaskList_line);

    private static MessageFormat lineAndLocation = new MessageFormat(
            TaskListMessages.TaskList_lineAndLocation); 

    static {
        createImageDescriptors();
    }

    /**
     * Don't allow instantiation.
     */
    private MarkerUtil() {
    }

    /**
     * Creates the map of image descriptors.
     */
    static void createImageDescriptors() {
    

        imageDescriptors = new HashMap(51);
        imageDescriptors
                .put(
                        "header_complete", IDEWorkbenchPlugin.getIDEImageDescriptor("obj16/header_complete.gif"));//$NON-NLS-2$//$NON-NLS-1$
        imageDescriptors
                .put(
                        "header_priority", IDEWorkbenchPlugin.getIDEImageDescriptor("obj16/header_priority.gif"));//$NON-NLS-2$//$NON-NLS-1$
        imageDescriptors.put(
                "task", IDEWorkbenchPlugin.getIDEImageDescriptor("obj16/taskmrk_tsk.gif"));//$NON-NLS-2$//$NON-NLS-1$
        imageDescriptors.put(
                "error", IDEWorkbenchPlugin.getIDEImageDescriptor("obj16/error_tsk.gif"));//$NON-NLS-2$//$NON-NLS-1$
        imageDescriptors.put(
                "warn", IDEWorkbenchPlugin.getIDEImageDescriptor("obj16/warn_tsk.gif"));//$NON-NLS-2$//$NON-NLS-1$
        imageDescriptors.put(
                "info", IDEWorkbenchPlugin.getIDEImageDescriptor("obj16/info_tsk.gif"));//$NON-NLS-2$//$NON-NLS-1$
        imageDescriptors.put(
                "hprio", IDEWorkbenchPlugin.getIDEImageDescriptor("obj16/hprio_tsk.gif"));//$NON-NLS-2$//$NON-NLS-1$
        imageDescriptors.put(
                "lprio", IDEWorkbenchPlugin.getIDEImageDescriptor("obj16/lprio_tsk.gif"));//$NON-NLS-2$//$NON-NLS-1$
        imageDescriptors
                .put(
                        "complete_tsk", IDEWorkbenchPlugin.getIDEImageDescriptor("obj16/complete_tsk.gif"));//$NON-NLS-2$//$NON-NLS-1$
        imageDescriptors
                .put(
                        "incomplete_tsk", IDEWorkbenchPlugin.getIDEImageDescriptor("obj16/incomplete_tsk.gif"));//$NON-NLS-2$//$NON-NLS-1$
        imageDescriptors.put(
                "gotoobj", IDEWorkbenchPlugin.getIDEImageDescriptor("elcl16/gotoobj_tsk.gif"));//$NON-NLS-2$//$NON-NLS-1$
        imageDescriptors.put(
                "addtsk", IDEWorkbenchPlugin.getIDEImageDescriptor("elcl16/addtsk_tsk.gif"));//$NON-NLS-2$//$NON-NLS-1$
        imageDescriptors
                .put(
                        "addtsk_disabled", IDEWorkbenchPlugin.getIDEImageDescriptor("dlcl16/addtsk_tsk.gif"));//$NON-NLS-2$//$NON-NLS-1$
        imageDescriptors
                .put(
                        "showcomplete", IDEWorkbenchPlugin.getIDEImageDescriptor("elcl16/showcomplete_tsk.gif"));//$NON-NLS-2$//$NON-NLS-1$
        imageDescriptors
                .put(
                        "selected_mode", IDEWorkbenchPlugin.getIDEImageDescriptor("elcl16/selected_mode.gif"));//$NON-NLS-2$//$NON-NLS-1$
        imageDescriptors
                .put(
                        "selected_mode_disabled", IDEWorkbenchPlugin.getIDEImageDescriptor("dlcl16/selected_mode.gif"));//$NON-NLS-2$//$NON-NLS-1$
        imageDescriptors
                .put(
                        "showchild_mode", IDEWorkbenchPlugin.getIDEImageDescriptor("elcl16/showchild_mode.gif"));//$NON-NLS-2$//$NON-NLS-1$
        imageDescriptors
                .put(
                        "showchild_mode_disabled", IDEWorkbenchPlugin.getIDEImageDescriptor("dlcl16/showchild_mode.gif"));//$NON-NLS-2$//$NON-NLS-1$
        imageDescriptors
                .put(
                        "showerr_disabled", IDEWorkbenchPlugin.getIDEImageDescriptor("dlcl16/showerr_tsk.gif"));//$NON-NLS-2$//$NON-NLS-1$
        imageDescriptors
                .put(
                        "showwarn_disabled", IDEWorkbenchPlugin.getIDEImageDescriptor("dlcl16/showwarn_tsk.gif"));//$NON-NLS-2$//$NON-NLS-1$
        imageDescriptors
                .put(
                        "showtsk_disabled", IDEWorkbenchPlugin.getIDEImageDescriptor("dlcl16/showtsk_tsk.gif"));//$NON-NLS-2$//$NON-NLS-1$
        imageDescriptors.put(
                "filter", IDEWorkbenchPlugin.getIDEImageDescriptor("elcl16/filter_ps.gif"));//$NON-NLS-2$//$NON-NLS-1$
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
            if (isComplete(marker)) {
				return getImage("complete_tsk");//$NON-NLS-1$
			} else if (!isReadOnly(marker)) {
				return getImage("incomplete_tsk");//$NON-NLS-1$
			}
        }
        return null;
    }

    /**
     * Returns the text to be used for the complete state of a task. 
     * Returns the empty string for markers that are not tasks.
     */
    public static String getCompleteText(IMarker marker) {
        if (isMarkerType(marker, IMarker.TASK)) {
            if (isComplete(marker)) {
				return TaskListMessages.TaskList_completed;
			} else {
				return TaskListMessages.TaskList_notCompleted;
			}
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * Returns the text to be used for the kind of marker.
     */
    public static String getKindText(IMarker marker) {
        if (isMarkerType(marker, IMarker.TASK)) {
            return TaskListMessages.TaskList_task; 
        }
        switch (getSeverity(marker)) {
        case IMarker.SEVERITY_ERROR:
            return TaskListMessages.TaskList_error; 
        case IMarker.SEVERITY_WARNING:
            return TaskListMessages.TaskList_warning; 
        case IMarker.SEVERITY_INFO:
            return TaskListMessages.TaskList_info; 
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * Returns the container name if it is defined, or empty string if not.
     */
    public static String getContainerName(IMarker marker) {
        IPath path = marker.getResource().getFullPath();
        int n = path.segmentCount() - 1; // n is the number of segments in container, not path
        if (n <= 0) {
			return ""; //$NON-NLS-1$
		}
        int len = 0;
        for (int i = 0; i < n; ++i) {
			len += path.segment(i).length();
		}
        // account for /'s
        if (n > 1) {
			len += n - 1;
		}
        StringBuffer sb = new StringBuffer(len);
        for (int i = 0; i < n; ++i) {
            if (i != 0) {
				sb.append('/');
			}
            sb.append(path.segment(i));
        }
        return sb.toString();
    }

    /**
     * Returns the image with the given key, or <code>null</code> if not found.
     */
    static Image getImage(String key) {
        Image image = imageRegistry.get(key);
        if (image == null) {
            ImageDescriptor desc = getImageDescriptor(key);
            if (desc != null) {
                image = desc.createImage(false);
                if (image == null) {
                    System.err
                            .println("TaskList: Error creating image for " + key);//$NON-NLS-1$
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
        } else if (isMarkerType(marker, IMarker.TASK)) {
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
        		IDEWorkbenchPlugin.log("TaskList: No image descriptor for " + key); //$NON-NLS-1$
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
            } else {
                return location;
            }
        } else {
            if (location.equals("")) {//$NON-NLS-1$
                return line
                        .format(new Object[] { Integer.toString(lineNumber) });
            } else {
                return lineAndLocation.format(new Object[] {
                        Integer.toString(lineNumber), location });
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
        if (i < len && value.charAt(i) == '#') {
			++i;
		}

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
            return null; // no image for normal priority
        case IMarker.PRIORITY_LOW:
            return getImage("lprio");//$NON-NLS-1$
        }
        return null;
    }

    /**
     * Returns the text for the given marker's priority.
     */
    public static String getPriorityText(IMarker marker) {
        if (!isMarkerType(marker, IMarker.TASK)) {
			return ""; //$NON-NLS-1$
		}

        switch (getPriority(marker)) {
        case IMarker.PRIORITY_HIGH:
            return TaskListMessages.TaskList_high; 
        case IMarker.PRIORITY_NORMAL:
            return TaskListMessages.TaskList_normal;
        case IMarker.PRIORITY_LOW:
            return TaskListMessages.TaskList_low;
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
            return new Integer(IMarker.PRIORITY_HIGH - getPriority(marker));
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
     * Returns whether the given marker is editable.
     * Only tasks which have not been marked as read-only are editable.
     */
    public static boolean isEditable(IMarker marker) {
        return isMarkerType(marker, IMarker.TASK) && !isReadOnly(marker);
    }

    /**
     * Returns whether the given marker is of the given type (either directly or indirectly).
     */
    public static boolean isMarkerType(IMarker marker, String type) {
        try {
            return marker.isSubtypeOf(type);
        } catch (CoreException e) {
            return false;
        }
    }

    /**
     * Returns whether the given marker is read-only.
     */
    public static boolean isReadOnly(IMarker marker) {
        return !marker.getAttribute(IMarker.USER_EDITABLE, true);
    }

    /**
     * Returns the creation time of the marker as a string.
     */
    public static String getCreationTime(IMarker marker) {
        try {
            return DateFormat.getDateTimeInstance(DateFormat.LONG,
                    DateFormat.MEDIUM).format(
                    new Date(marker.getCreationTime()));
        } catch (CoreException e) {
            return null;
        }
    }
}
