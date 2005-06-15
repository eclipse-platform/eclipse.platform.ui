/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.presentations.util;

import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.presentations.IStackPresentationSite;

/**
 * @since 3.1
 */
public class TabFolderEvent {
    public static final int EVENT_PANE_MENU = 1;
    public static final int EVENT_HIDE_TOOLBAR = 2;
    public static final int EVENT_SHOW_TOOLBAR = 3;
    public static final int EVENT_RESTORE = 4;
    public static final int EVENT_MINIMIZE = 5;
    public static final int EVENT_CLOSE = 6;
    public static final int EVENT_MAXIMIZE = 7;
    public static final int EVENT_TAB_SELECTED = 8;
    public static final int EVENT_GIVE_FOCUS_TO_PART = 9;
    public static final int EVENT_DRAG_START = 10;
    public static final int EVENT_SHOW_LIST = 11;
    public static final int EVENT_SYSTEM_MENU = 12;
    
    public static int eventIdToStackState(int eventId) {
        switch(eventId) {
        	case EVENT_RESTORE: return IStackPresentationSite.STATE_RESTORED;
        	case EVENT_MINIMIZE: return IStackPresentationSite.STATE_MINIMIZED;
        	case EVENT_MAXIMIZE: return IStackPresentationSite.STATE_MAXIMIZED;
        }
        
        return 0;
    }
    
    public static int stackStateToEventId(int stackState) {
        switch(stackState) {
	    	case IStackPresentationSite.STATE_RESTORED: return EVENT_RESTORE;
	    	case IStackPresentationSite.STATE_MINIMIZED: return EVENT_MINIMIZE;
	    	case IStackPresentationSite.STATE_MAXIMIZED: return EVENT_MAXIMIZE;
        }
    
        return 0;        
    }
    
    public AbstractTabItem tab;
    public int type;
    public int x;
    public int y;
    
    public TabFolderEvent(int type) {
        this.type = type;
    }
    
    public TabFolderEvent(int type, AbstractTabItem w, int x, int y) {
        this.type = type;
        this.tab = w;
        this.x = x;
        this.y = y;
    }
    
    public TabFolderEvent(int type, AbstractTabItem w, Point pos) {
        this.type = type;
        this.tab = w;
        this.x = pos.x;
        this.y = pos.y;
    }
}
