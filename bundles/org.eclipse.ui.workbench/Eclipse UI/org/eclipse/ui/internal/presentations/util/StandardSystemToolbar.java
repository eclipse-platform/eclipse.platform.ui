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

import org.eclipse.core.commands.common.EventManager;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.presentations.IStackPresentationSite;

/**
 * @since 3.1
 */
public class StandardSystemToolbar extends EventManager {
    private ToolBarManager toolbarManager;
    private Composite control;
    
    private class SystemMenuContribution extends ContributionItem {
    	ToolItem item;
    	Image img;
    	String text;
    	int flags;
    	
    	public SystemMenuContribution() {
    		this(SWT.PUSH);
    	}
    	
    	public SystemMenuContribution(int flags) {
    		this.flags = flags;
    	}
    	
    	public void setToolTipText(String text) {
    		this.text = text;
    		if (item != null) {
    			item.setToolTipText(text);
    		}
    	}
    	
    	public void setImage(Image img) {
    		this.img = img;
    		
    		if (item != null) {
    			item.setImage(img);
    		}
    	}
    	
    	/* (non-Javadoc)
		 * @see org.eclipse.jface.action.ContributionItem#setVisible(boolean)
		 */
		public void setVisible(boolean visible) {
			if (visible != isVisible()) {
				toolbarManager.markDirty();
			}
			
			super.setVisible(visible);
		}
    	
    	/* (non-Javadoc)
		 * @see org.eclipse.jface.action.ContributionItem#fill(org.eclipse.swt.widgets.ToolBar, int)
		 */
		public void fill(ToolBar parent, int index) {
			if (!isVisible()) {
				return;
			}
			
			item = new ToolItem(parent, flags, index);
			if (img != null) {
				item.setImage(img);
			}
			if (text != null) {
				item.setToolTipText(text);
			}
			item.addSelectionListener(selectionListener);
		}
    }
    
    private class PaneMenu extends SystemMenuContribution {
    	
    	public PaneMenu() {
    		super(SWT.NONE);
    	}
    	    	
     	public void setToolTipText(String text) {
     		super.setToolTipText(text);
    	}
    	
    	public void setImage(Image img) {
    		
    		super.setImage(img);
    	}
    	
    	/* (non-Javadoc)
		 * @see org.eclipse.jface.action.ContributionItem#fill(org.eclipse.swt.widgets.ToolBar, int)
		 */
		public void fill(ToolBar parent, int index) {
			if (!isVisible()) {
				return;
			}
			super.fill(parent, index);
		}
    }
    
    
    private MouseListener mouseListener = new MouseAdapter() {
    	/* (non-Javadoc)
		 * @see org.eclipse.swt.events.MouseAdapter#mouseDown(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseDown(MouseEvent e) {
			ToolItem item = toolbarManager.getControl().getItem(new Point(e.x, e.y));
			
    		if (item == paneMenu.item && e.button == 1) {
    			fireEvent(TabFolderEvent.EVENT_PANE_MENU);
    		}
		}
    };
    
    private SystemMenuContribution paneMenu = new PaneMenu();
    private SystemMenuContribution showToolbar = new SystemMenuContribution();
    private SystemMenuContribution min = new SystemMenuContribution();
    private SystemMenuContribution max = new SystemMenuContribution();
    private SystemMenuContribution close = new SystemMenuContribution();
    
    private int state = IStackPresentationSite.STATE_RESTORED;
    private boolean showingToolbar = true;
    
    private SelectionAdapter selectionListener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			if (e.widget == paneMenu.item) {
			    //fireEvent(TabFolderEvent.EVENT_PANE_MENU);
			} else if (e.widget == showToolbar.item) {
			    if (showingToolbar) {
			        fireEvent(TabFolderEvent.EVENT_HIDE_TOOLBAR);
			    } else {
			        fireEvent(TabFolderEvent.EVENT_SHOW_TOOLBAR);
			    }
			} else if (e.widget == min.item) {
			    if (state == IStackPresentationSite.STATE_MINIMIZED) {
			        fireEvent(TabFolderEvent.EVENT_RESTORE);
			    } else {
			        fireEvent(TabFolderEvent.EVENT_MINIMIZE);
			    }
			} else if (e.widget == max.item) {
			    if (state == IStackPresentationSite.STATE_MAXIMIZED) {
			        fireEvent(TabFolderEvent.EVENT_RESTORE);
			    } else {
			        fireEvent(TabFolderEvent.EVENT_MAXIMIZE);
			    }			    
			} else if (e.widget == close.item) {
			    fireEvent(TabFolderEvent.EVENT_CLOSE);
			}
		}
    };
    
    public StandardSystemToolbar(Composite parent, boolean showPaneMenu, 
            boolean showHideToolbar, boolean showMinimize, boolean showMaximize, boolean enableClose) {
        
        control = new Composite(parent, SWT.NONE);
        control.setLayout(new EnhancedFillLayout());
        
        toolbarManager = new ToolBarManager(SWT.FLAT);
        toolbarManager.createControl(control);
        toolbarManager.getControl().addMouseListener(mouseListener);
        
        toolbarManager.add(paneMenu);
        paneMenu.setImage(WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_VIEW_MENU_THIN));
        paneMenu.setVisible(showPaneMenu);
        paneMenu.setToolTipText(WorkbenchMessages.Menu); 

        toolbarManager.add(showToolbar);
        showToolbar.setImage(WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_HIDE_TOOLBAR_THIN));
        showToolbar.setVisible(showHideToolbar);

        toolbarManager.add(min);
        min.setVisible(showMinimize);

        toolbarManager.add(max);
        max.setVisible(showMaximize);

        toolbarManager.add(close);
        close.setImage(WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_CLOSE_VIEW_THIN));
        close.setVisible(enableClose);
        
        setState(IStackPresentationSite.STATE_RESTORED);
        
        toolbarManager.update(true);
    }
    
    public Point getPaneMenuLocation() {
        
        Rectangle bounds = Geometry.toDisplay(paneMenu.item.getParent(), paneMenu.item.getBounds());
        return new Point(bounds.x, bounds.y + bounds.height);
    }
    
    public void enableClose(boolean enabled) {
    	close.setVisible(enabled);
    	toolbarManager.update(false);
    }
    
    public void enableMinimize(boolean enabled) {
    	min.setVisible(enabled);
    	toolbarManager.update(false);
    }
    
    public void enableMaximize(boolean enabled) {
    	max.setVisible(enabled);
    	toolbarManager.update(false);
    }
    
   public void enableShowToolbar(boolean enabled) {
   		showToolbar.setVisible(enabled);
   		toolbarManager.update(false);
    }
    
    public void enablePaneMenu(boolean enabled) {
    	paneMenu.setVisible(enabled);
    	toolbarManager.update(false);
    }
    
    /**
     * Updates the icons on the state buttons to match the given state
     * @param newState
     * @since 3.1
     */
    public void setState(int newState) {
        if (min != null) {
            if (newState == IStackPresentationSite.STATE_MINIMIZED) {
            	min.setToolTipText(WorkbenchMessages.StandardSystemToolbar_Restore);
                min.setImage(WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_RESTORE_VIEW_THIN));
            } else {
            	min.setToolTipText(WorkbenchMessages.StandardSystemToolbar_Minimize); 
                min.setImage(WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_MIN_VIEW_THIN));
            }
        }
        if (max != null) {
            if (newState == IStackPresentationSite.STATE_MAXIMIZED) {
            	max.setToolTipText(WorkbenchMessages.StandardSystemToolbar_Restore);
                max.setImage(WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_RESTORE_VIEW_THIN));
            } else {
            	max.setToolTipText(WorkbenchMessages.StandardSystemToolbar_Maximize);
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
        addListenerObject(propertyListener);
    }
    
    public void removeListener(IPropertyListener propertyListener) {
        removeListenerObject(propertyListener);
    }
    
    public Control getControl() {
        return control;
    }
    
    private void fireEvent(int event) {
        Object[] list = getListeners();
        
        for (int i = 0; i < list.length; i++) {
            IPropertyListener listener = (IPropertyListener)list[i];
            
            listener.propertyChanged(this, event);
        }
    }
}
