/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.presentations.newapi;

import org.eclipse.jface.util.Geometry;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.presentations.IStackPresentationSite;

/**
 * @since 3.1
 */
public class StandardSystemToolbar {
    private ToolBar toolbar;
    private Composite control;
    
    private ToolItem paneMenu;
    private ToolItem showToolbar;
    private ToolItem min;
    private ToolItem max;
    private ToolItem close;
    
    private ListenerList listeners = new ListenerList();
    
    private int state = IStackPresentationSite.STATE_RESTORED;
    private boolean showingToolbar = true;
    
    private SelectionAdapter selectionListener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			if (e.widget == paneMenu) {
			    fireEvent(TabFolderEvent.EVENT_PANE_MENU);
			} else if (e.widget == showToolbar) {
			    if (showingToolbar) {
			        fireEvent(TabFolderEvent.EVENT_HIDE_TOOLBAR);
			    } else {
			        fireEvent(TabFolderEvent.EVENT_SHOW_TOOLBAR);
			    }
			} else if (e.widget == min) {
			    if (state == IStackPresentationSite.STATE_MINIMIZED) {
			        fireEvent(TabFolderEvent.EVENT_RESTORE);
			    } else {
			        fireEvent(TabFolderEvent.EVENT_MINIMIZE);
			    }
			} else if (e.widget == max) {
			    if (state == IStackPresentationSite.STATE_MAXIMIZED) {
			        fireEvent(TabFolderEvent.EVENT_RESTORE);
			    } else {
			        fireEvent(TabFolderEvent.EVENT_MAXIMIZE);
			    }			    
			} else if (e.widget == close) {
			    fireEvent(TabFolderEvent.EVENT_CLOSE);
			}
		}
    };
    
    public StandardSystemToolbar(Composite parent, boolean showPaneMenu, 
            boolean showHideToolbar, boolean showMinimize, boolean showMaximize, boolean enableClose) {
        
        control = new Composite(parent, SWT.NONE);
        control.setLayout(new EnhancedFillLayout());
        
        toolbar = new ToolBar(control, SWT.FLAT);
        
        if (showPaneMenu) {
	        paneMenu = new ToolItem(toolbar, SWT.PUSH);
	        paneMenu.addSelectionListener(selectionListener);
	        paneMenu.setImage(WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_VIEW_MENU_THIN));
        }
        
        if (showHideToolbar) {
            showToolbar = new ToolItem(toolbar, SWT.PUSH);
            showToolbar.addSelectionListener(selectionListener);
            showToolbar.setImage(WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_HIDE_TOOLBAR_THIN));
        }
        
        if (showMinimize) {
            min = new ToolItem(toolbar, SWT.PUSH);
            min.addSelectionListener(selectionListener);
            min.setImage(WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_MIN_VIEW_THIN));            
        }
        
        if (showMaximize) {
            max = new ToolItem(toolbar, SWT.PUSH);
            max.addSelectionListener(selectionListener);
            max.setImage(WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_MAX_VIEW_THIN));            
        }
        
        if (enableClose) {
	        close = new ToolItem(toolbar, SWT.PUSH);
	        close.addSelectionListener(selectionListener);
	        close.setImage(WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_CLOSE_VIEW_THIN));
        }
    }
    
    public Point getPaneMenuLocation() {
        
        Rectangle bounds = Geometry.toDisplay(paneMenu.getParent(), paneMenu.getBounds());
        return new Point(bounds.x, bounds.y + bounds.height);
    }
    
    public void enableClose(boolean enabled) {
        if (close != null) {
            close.setEnabled(enabled);
        }        
    }
    
    public void enableMinimize(boolean enabled) {
        if (min != null) {
            min.setEnabled(enabled);
        }
    }
    
    public void enableMaximize(boolean enabled) {
        if (max != null) {
            max.setEnabled(enabled);
        }
    }
    
    public void enableShowToolbar(boolean enabled) {
        if (showToolbar != null) {
            showToolbar.setEnabled(enabled);
        }
    }
    
    public void enablePaneMenu(boolean enabled) {
        if (paneMenu != null) {
            paneMenu.setEnabled(enabled);
        }
    }
    
    /**
     * Updates the icons on the state buttons to match the given state
     * @param newState
     * @since 3.1
     */
    public void setState(int newState) {
        if (min != null) {
            if (newState == IStackPresentationSite.STATE_MINIMIZED) {
                min.setImage(WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_RESTORE_VIEW_THIN));
            } else {
                min.setImage(WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_MIN_VIEW_THIN));
            }
        }
        if (max != null) {
            if (newState == IStackPresentationSite.STATE_MAXIMIZED) {
                max.setImage(WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_RESTORE_VIEW_THIN));
            } else {
                max.setImage(WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_MAX_VIEW_THIN));
            }
        }
        
        state = newState;
    }
    
    public void setToolbarShowing(boolean isShowing) {
        showingToolbar = isShowing;
        if (showToolbar != null) {
            if (isShowing) {
                showToolbar.setImage(WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_HIDE_TOOLBAR_THIN));
            } else {
                showToolbar.setImage(WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_SHOW_TOOLBAR_THIN));
            }
        } 
    }
    
    public void addListener(IPropertyListener propertyListener) {
        listeners.add(propertyListener);
    }
    
    public void removeListener(IPropertyListener propertyListener) {
        listeners.remove(propertyListener);
    }
    
    public Control getControl() {
        return control;
    }
    
    private void fireEvent(int event) {
        Object[] list = listeners.getListeners();
        
        for (int i = 0; i < list.length; i++) {
            IPropertyListener listener = (IPropertyListener)list[i];
            
            listener.propertyChanged(this, event);
        }
    }
}
