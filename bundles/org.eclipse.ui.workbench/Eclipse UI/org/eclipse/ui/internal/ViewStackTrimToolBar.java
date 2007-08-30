/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.internal.layout.TrimToolBarBase;

/**
 * @since 3.3
 *
 */
public class ViewStackTrimToolBar extends TrimToolBarBase {
	private boolean restoreOnUnzoom = false;
	
	// The orientation of the fast view pane when showing a view
	private int paneOrientation;
	
	// The id of the part that was showing when we minimized
	private String selectedTabId;

	public ViewStackTrimToolBar(String id, int curSide, int paneOrientation, WorkbenchWindow wbw) {
		super(id, curSide, wbw);
		
		this.paneOrientation = paneOrientation;		
		dock(curSide);
	}
	
	/**
	 * Put the stack back into the presentation
	 */
	protected void restoreToPresentation() {
		Perspective persp = wbw.getActiveWorkbenchPage().getActivePerspective();
		//FastViewManager fvMgr = persp.getFastViewManager();
		
		LayoutPart part = persp.getPresentation().findPart(getId(), null);
		if (part instanceof ContainerPlaceholder) {
			ViewStack stack = (ViewStack) ((ContainerPlaceholder)part).getRealContainer();
			stack.setMinimized(false);
		}
		//fvMgr.restoreToPresentation(getId());
	}

	public void initToolBarManager(final ToolBarManager mgr) {
		// Set up the ToolBar with a restore button
		IContributionItem restoreContrib = new ContributionItem() {
			public void fill(ToolBar parent, int index) {
		        ToolItem restoreItem = new  ToolItem(mgr.getControl(), SWT.PUSH, index);        
		        Image tbImage = WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_ETOOL_RESTORE_TRIMPART);
		        restoreItem.setImage(tbImage);       
		        String menuTip = WorkbenchMessages.StandardSystemToolbar_Restore;
		        restoreItem.setToolTipText(menuTip);
		        restoreItem.addSelectionListener(new SelectionListener() {
					public void widgetDefaultSelected(SelectionEvent e) {
						restoreToPresentation();
					}
					public void widgetSelected(SelectionEvent e) {
						restoreToPresentation();
					}
		        });
			}
		};
		mgr.add(restoreContrib);
		
		ShowFastViewContribution sfvc = new ShowFastViewContribution(wbw, getId());
		mgr.add(sfvc);
		
		// Add context menu items
		mgr.setContextMenuManager(new MenuManager());
		MenuManager menuMgr = mgr.getContextMenuManager();
		
		final IContributionItem closeContrib = new ContributionItem() {
			public void fill(Menu parent, int index) {
		        MenuItem closeItem = new MenuItem(parent, SWT.NONE, index++);
		        closeItem.setText(WorkbenchMessages.WorkbenchWindow_close); 
		        closeItem.addSelectionListener(new SelectionAdapter() {
		            public void widgetSelected(SelectionEvent e) {
		            	IViewReference selectedView = null;
		            	if (contextToolItem != null) {
		            		selectedView = (IViewReference) contextToolItem.getData(ShowFastViewContribution.FAST_VIEW);
		            	}
		            	
		                if (selectedView != null) {
		                    WorkbenchPage page = wbw.getActiveWorkbenchPage();
		                    if (page != null) {
		                        page.hideView(selectedView);
		                    }
		                }
		            }
		        });
			}
		};
		
		// We have to manage the visiblity this way...?
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
            	IViewReference selectedView = null;
            	if (contextToolItem != null) {
            		selectedView = (IViewReference) contextToolItem.getData(ShowFastViewContribution.FAST_VIEW);
            	}
            	
            	// Only show the 'close' item if we've clicked on a view
        		Perspective persp = wbw.getActiveWorkbenchPage().getActivePerspective();
            	closeContrib.setVisible(selectedView != null && persp.isCloseable(selectedView));
            	manager.update(true);
			}
		});
		
		menuMgr.add(closeContrib);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.layout.TrimToolBarBase#hookControl(org.eclipse.swt.widgets.ToolBar)
	 */
	public void hookControl(ToolBarManager mgr) {
		// Hook a drop Listener to the control
		// NOTE: the drop target is self-managing...it
		// both hooks the new target and removes it on dispose
		new FastViewDnDHandler(id, mgr, wbw);
	}
	
	/**
	 * Sets whether or not the stack gets restored on an unzoom
	 * operation.
	 * 
	 * @param restoreOnUnzoom
	 */
	public void setRestoreOnUnzoom(boolean restoreOnUnzoom) {
		this.restoreOnUnzoom = restoreOnUnzoom;
	}
	
	public boolean restoreOnUnzoom() {
		return restoreOnUnzoom;
	}

	/**
	 * @param ref
	 * @param selected
	 */
	public void setIconSelection(IViewReference ref, boolean selected) {
		ToolItem item = ShowFastViewContribution.getItem(tbMgr.getControl(), ref);
		if (item != null) {
			item.setSelection(selected);
			
			if (selected) {
				selectedTabId = ref.getId();
				
				// Create a 'compound' id if this is a multi-instance part
				if (ref.getSecondaryId() != null)
					selectedTabId = selectedTabId + ':' + ref.getSecondaryId();
			}
		}
	}

	/**
	 * @return Returns the paneOrientation.
	 */
	public int getPaneOrientation() {
		return paneOrientation;
	}

	/**
	 * Cache the tba that was on top when we were minimized
	 * @param selectedTab The id of the PartPane for the tab 
	 */
	public void setSelectedTabId(String id) {
		selectedTabId = id;
	}
	
	/**
	 * @return The id of the layout part representing the 'top' tab
	 */
	public String getSelectedTabId() {
		return selectedTabId;
	}

	/**
	 * @param newOrientation The new orientation for the fact view display
	 * @param wbw The currently active WorkbenchWindow
	 */
	public void setOrientation(int newOrientation, WorkbenchWindow wbw) {
		if (newOrientation == paneOrientation)
			return;
		
		paneOrientation = newOrientation;
		
		// If there's a fast view showing, toggle it to pick up the change
		if (wbw.getActivePage() instanceof WorkbenchPage) {
			WorkbenchPage wbp = (WorkbenchPage) wbw.getActivePage();
			Perspective persp = wbp.getActivePerspective();
			if (persp != null) {
				IViewReference curRef = persp.getActiveFastView();
				if (curRef != null) { 
					persp.setActiveFastView(null);
					persp.setActiveFastView(curRef);
				}
			}
			
		}
		
	}
}
