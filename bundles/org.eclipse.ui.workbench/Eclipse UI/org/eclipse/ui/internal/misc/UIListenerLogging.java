/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.misc;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.util.Util;


/**
 * To whom it may concern: Please stop deleting the logging code. This is important for debugging
 * event ordering issues.
 */
public class UIListenerLogging {
    
    // Types of listeners that can be logged (the names of the options that enable/disable their logging)
    private final static String LISTENER_EVENTS = PlatformUI.PLUGIN_ID + "/debug"; //$NON-NLS-1$
    public  final static String PAGE_PARTLISTENER_EVENTS = PlatformUI.PLUGIN_ID + "/listeners/IWorkbenchPage.IPartListener"; //$NON-NLS-1$
    public  final static String PAGE_PARTLISTENER2_EVENTS = PlatformUI.PLUGIN_ID + "/listeners/IWorkbenchPage.IPartListener2"; //$NON-NLS-1$
    private final static String PAGE_PROPERTY_EVENTS = PlatformUI.PLUGIN_ID + "/listeners/IWorkbenchPage.IPropertyChangeListener"; //$NON-NLS-1$
    private final static String WINDOW_PAGE_EVENTS = PlatformUI.PLUGIN_ID + "/listeners/IWorkbenchWindow.IPageListener"; //$NON-NLS-1$
    private final static String WINDOW_PERSPECTIVE_EVENTS = PlatformUI.PLUGIN_ID + "/listeners/IWorkbenchWindow.IPerspectiveListener"; //$NON-NLS-1$
    public  final static String WINDOW_PARTLISTENER_EVENTS = PlatformUI.PLUGIN_ID + "/listeners/IWorkbenchWindow.IPartListener"; //$NON-NLS-1$
    public  final static String WINDOW_PARTLISTENER2_EVENTS = PlatformUI.PLUGIN_ID + "/listeners/IWorkbenchWindow.IPartListener2"; //$NON-NLS-1$
    private final static String PARTREFERENCE_PROPERTY_EVENTS = PlatformUI.PLUGIN_ID + "/listeners/IWorkbenchPartReference"; //$NON-NLS-1$
    
    public final static boolean enabled = internal_isEnabled(LISTENER_EVENTS);
    
    // IPartListener events
    public final static String PE_ACTIVATED = "partActivated"; //$NON-NLS-1$
    public final static String PE_PART_BROUGHT_TO_TOP = "partBroughtToTop"; //$NON-NLS-1$
    public final static String PE_PART_CLOSED = "partClosed"; //$NON-NLS-1$
    public final static String PE_PART_DEACTIVATED = "partDeactivated"; //$NON-NLS-1$
    public final static String PE_PART_OPENED = "partOpened"; //$NON-NLS-1$
    
    // IPartListener2 events
    public final static String PE2_ACTIVATED = "partActivated"; //$NON-NLS-1$
    public final static String PE2_PART_VISIBLE = "partVisible"; //$NON-NLS-1$
    public final static String PE2_PART_HIDDEN = "partHidden"; //$NON-NLS-1$
    public final static String PE2_PART_BROUGHT_TO_TOP = "partBroughtToTop"; //$NON-NLS-1$
    public final static String PE2_PART_CLOSED = "partClosed"; //$NON-NLS-1$
    public final static String PE2_PART_DEACTIVATED = "partDectivated"; //$NON-NLS-1$
    public final static String PE2_PART_OPENED = "partOpened"; //$NON-NLS-1$
    public static final String PE2_PART_INPUT_CHANGED = "partInputChanged"; //$NON-NLS-1$
    
    // IPageListener events
    public final static String WPE_PAGE_ACTIVATED = "pageActivated"; //$NON-NLS-1$
    public final static String WPE_PAGE_OPENED = "pageOpened"; //$NON-NLS-1$ 
    public final static String WPE_PAGE_CLOSED = "pageClosed"; //$NON-NLS-1$ 
    
    // IPerspectiveListener events
    public static final String PLE_PERSP_PRE_DEACTIVATE = "perspectivePreDeactivate"; //$NON-NLS-1$
    public static final String PLE_PERSP_DEACTIVATED = "perspectiveDeactivated"; //$NON-NLS-1$
    public static final String PLE_PERSP_ACTIVATED = "perspectiveActivated"; //$NON-NLS-1$
    public static final String PLE_PERSP_OPENED = "perspectiveOpened"; //$NON-NLS-1$
    public static final String PLE_PERSP_CLOSED = "perspectiveClosed"; //$NON-NLS-1$
    public static final String PLE_PERSP_SAVED_AS = "perspectiveSavedAs"; //$NON-NLS-1$

    private static String getSourceId(Object source) {
        return Util.safeString(Integer.toString(source.hashCode() % 1000));
    }

    private static String getWindowId(IWorkbenchWindow source) {
        return "window " + Util.safeString(Integer.toString(source.hashCode() % 1000)); //$NON-NLS-1$
    }
    
    private static String getPageId(IWorkbenchPage page) {
        return "page " + Util.safeString(Integer.toString(page.hashCode() % 1000)); //$NON-NLS-1$
    }
    
    private static String getPerspectiveId(IPerspectiveDescriptor descriptor) {
        return Util.safeString(descriptor.getId());
    }
    
    public static final void logPageEvent(IWorkbenchWindow window, IWorkbenchPage page, String eventId) {
        if (isEnabled(WINDOW_PAGE_EVENTS)) {
            System.out.println(WINDOW_PAGE_EVENTS 
                    + " " + getWindowId(window) //$NON-NLS-1$
                    + " " + eventId + " (" + getPageId(page) + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
        }
    }

    public static final void logPerspectiveEvent(IWorkbenchWindow window, IWorkbenchPage page, 
            IPerspectiveDescriptor descriptor, String eventId) {
        
        if (isEnabled(WINDOW_PERSPECTIVE_EVENTS)) {
            System.out.println(WINDOW_PERSPECTIVE_EVENTS 
                    + " " + getWindowId(window) //$NON-NLS-1$
                    + " " + eventId + " (" + getPageId(page) + ", " + getPerspectiveId(descriptor) + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ 
        }
    }

    public static final void logPerspectiveChangedEvent(IWorkbenchWindow window, IWorkbenchPage page, 
            IPerspectiveDescriptor descriptor, IWorkbenchPartReference ref, String changeId) {
        
        if (isEnabled(WINDOW_PERSPECTIVE_EVENTS)) {
            System.out.println(WINDOW_PERSPECTIVE_EVENTS 
                    + " "+ getWindowId(window) //$NON-NLS-1$
                    + " perspectiveChanged (" + getPageId(page) + ", " + getPerspectiveId(descriptor)  //$NON-NLS-1$ //$NON-NLS-2$
                    + ", " + getPartId(ref) + ", " + changeId + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            
        }
    }

    public static final void logPerspectiveSavedAs(IWorkbenchWindow window, IWorkbenchPage page, 
    		IPerspectiveDescriptor oldDescriptor, IPerspectiveDescriptor newDescriptor) {
        
        if (isEnabled(WINDOW_PERSPECTIVE_EVENTS)) {
            System.out.println(WINDOW_PERSPECTIVE_EVENTS 
                    + " " + getWindowId(window) //$NON-NLS-1$
                    + " " + PLE_PERSP_SAVED_AS + " (" + getPageId(page) + ", " + getPerspectiveId(oldDescriptor)  + ", " + getPerspectiveId(newDescriptor) + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ 
        }
    }

    
    private static String getPartId(IWorkbenchPart part) {
        if (part == null) {
            return "null part"; //$NON-NLS-1$
        }
        
        return Util.safeString(part.getTitle()) + " - "  //$NON-NLS-1$
            + Util.safeString(part.getSite().getId());
    }
    
    private static String getPartId(IWorkbenchPartReference ref) {
        if (ref == null) {
            return "null part"; //$NON-NLS-1$
        }
        
        return Util.safeString(ref.getPartName()) + " - "  //$NON-NLS-1$
            + Util.safeString(ref.getId());
    }

    /**
     * Log a partListener event fired from the workbench window
     *
     * @param page
     * @param eventId
     */
    public static final void logPartListenerEvent(String sourceType, Object source, IWorkbenchPart part, String eventId) {
        if (isEnabled(sourceType)) {
            System.out.println(sourceType + " " + getSourceId(source) //$NON-NLS-1$
                    + ", " + eventId + "(" + getPartId(part) + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }

    /**
     * Log a partListener2 event fired from the workbench window
     *
     * @param page
     * @param eventId
     */
    public static final void logPartListener2Event(String sourceType, Object source, IWorkbenchPartReference part, String eventId) {
        if (isEnabled(sourceType)) {
            System.out.println(sourceType + " " + getSourceId(source) //$NON-NLS-1$
                    + ", " + eventId + "(" + getPartId(part) + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }
    
    /**
     * Log an event fired from the workbench page
     *
     * @param page
     * @param eventId
     */
    public static final void logPartListenerEvent(IWorkbenchPage page, IWorkbenchPart part, String eventId) {
        if (isEnabled(PAGE_PARTLISTENER_EVENTS)) {
            System.out.println(PAGE_PARTLISTENER_EVENTS + " page "  //$NON-NLS-1$
                    + Util.safeString(page.getLabel()) 
                    + ", " + eventId + "(" + getPartId(part) + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }

    /**
     * Log an event fired from the workbench page
     *
     * @param page
     * @param eventId
     */
    public static final void logPartListener2Event(IWorkbenchPage page, IWorkbenchPartReference part, String eventId) {
        if (isEnabled(PAGE_PARTLISTENER2_EVENTS)) {
            System.out.println(PAGE_PARTLISTENER2_EVENTS + " page "  //$NON-NLS-1$
                    + Util.safeString(page.getLabel()) 
                    + ", " + eventId + "(" + getPartId(part) + ")");          //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }
    
    /**
     * Log an event fired from the workbench page
     *
     * @param page
     * @param eventId
     */
    public static final void logPagePropertyChanged(IWorkbenchPage page, String changeId, Object oldValue, Object newValue) {
        if (isEnabled(PAGE_PROPERTY_EVENTS)) {
            System.out.println(PAGE_PROPERTY_EVENTS + " page "  //$NON-NLS-1$
                    + Util.safeString(page.getLabel()) 
                    + ", " + changeId + " = " + Util.safeString(newValue.toString()) + "( old value = " + newValue.toString() + " )"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$        
        }
    }
    
    public static final void logPartReferencePropertyChange(IWorkbenchPartReference ref, int changeId) {
        if (isEnabled(PARTREFERENCE_PROPERTY_EVENTS)) {
            String eventDescription;
            
            switch(changeId) {
                case IWorkbenchPartConstants.PROP_TITLE: eventDescription = "title"; break; //$NON-NLS-1$
                case IWorkbenchPartConstants.PROP_DIRTY: eventDescription = "dirty"; break; //$NON-NLS-1$
                case IWorkbenchPartConstants.PROP_INPUT: eventDescription = "input"; break; //$NON-NLS-1$
                case IWorkbenchPartConstants.PROP_PART_NAME: eventDescription = "part_name"; break; //$NON-NLS-1$
                case IWorkbenchPartConstants.PROP_CONTENT_DESCRIPTION: eventDescription = "content_description"; break; //$NON-NLS-1$
                default:
                    eventDescription = "unknown event id = " + changeId; //$NON-NLS-1$
            }
            
            System.out.println(PARTREFERENCE_PROPERTY_EVENTS + " "  //$NON-NLS-1$
                    + getPartId(ref)
                    + ", property " + eventDescription);         //$NON-NLS-1$
        }        
    }
    
    private static boolean isEnabled(String eventName) {
        return enabled && internal_isEnabled(eventName);
    }
    
    private static boolean internal_isEnabled(String eventName) {
        String option = Platform.getDebugOption(eventName);
        return option != null && !option.equalsIgnoreCase("false") && !option.equalsIgnoreCase("-1"); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
