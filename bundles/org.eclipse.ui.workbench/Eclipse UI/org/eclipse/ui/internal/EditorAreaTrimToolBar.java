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
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.internal.layout.TrimToolBarBase;

public class EditorAreaTrimToolBar extends TrimToolBarBase {
	private LayoutPart editorArea;
	private boolean restoreOnUnzoom = false;
	
	// The orientation of the fast view pane when showing a view
	private int paneOrientation;

	public EditorAreaTrimToolBar(WorkbenchWindow wbw, LayoutPart editorArea) {
		super(IPageLayout.ID_EDITOR_AREA, SWT.TOP, wbw);
		
		this.editorArea = editorArea;		
		dock(SWT.TOP);
	}
	
	/**
	 * Put the stack back into the presentation
	 */
	protected void restoreToPresentation() {
		EditorSashContainer esc = (EditorSashContainer)editorArea;
		EditorStack curStack = esc.getUpperRightEditorStack(esc.getChildren());
		curStack.setMinimized(false);
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

		// Set up the ToolBar with a button represing the Editor Area
		IContributionItem eaContrib = new ContributionItem() {
			public void fill(ToolBar parent, int index) {
		        ToolItem editorAreaItem = new  ToolItem(mgr.getControl(), SWT.PUSH, index);        
		        Image tbImage = WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_ETOOL_EDITOR_TRIMPART);
		        editorAreaItem.setImage(tbImage);       
		        String menuTip = WorkbenchMessages.EditorArea_Tooltip;
		        editorAreaItem.setToolTipText(menuTip);
		        editorAreaItem.addSelectionListener(new SelectionListener() {
					public void widgetDefaultSelected(SelectionEvent e) {
						restoreToPresentation();
					}
					public void widgetSelected(SelectionEvent e) {
						restoreToPresentation();
					}
		        });
			}
		};
		mgr.add(eaContrib);
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
		if (item != null)
			item.setSelection(selected);
	}

	/**
	 * @return Returns the paneOrientation.
	 */
	public int getPaneOrientation() {
		return paneOrientation;
	}
}
