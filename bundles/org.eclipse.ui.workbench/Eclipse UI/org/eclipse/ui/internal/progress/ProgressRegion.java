/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.ui.internal.progress;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;
/**
 * The ProgressRegion is class for the region of the workbench where the
 * progress line and the animation item are shown.
 */
public class ProgressRegion {
	ProgressViewer viewer;
	AnimationItem item;
	Composite region;
	WorkbenchWindow workbenchWindow;
	/**
	 * Create a new instance of the receiver.
	 */
	public ProgressRegion() {
		//No default behavior.
	}
	/**
	 * Create the contents of the receiver in the parent. Use the window for the
	 * animation item.
	 * 
	 * @param parent
	 *            The parent widget of the composite.
	 * @param window
	 *            The WorkbenchWindow this is in.
	 * @return
	 */
	public Control createContents(Composite parent, WorkbenchWindow window) {
		workbenchWindow = window;
		region = new Composite(parent, SWT.NONE);
		FormLayout regionLayout = new FormLayout();
		region.setLayout(regionLayout);
		Label seperator = new Label(region, SWT.SEPARATOR);
		item = new ProgressAnimationItem(this);
		item.createControl(region);
		Control itemControl = item.getControl();
		
		viewer = new ProgressViewer(region, SWT.NO_FOCUS, 1, 36);
		viewer.setUseHashlookup(true);
		Control viewerControl = viewer.getControl();
		int widthPreference = AnimationManager.getInstance()
				.getPreferredWidth();
		Point preferredSize = viewer.getSizeHints();
		int margin = 2;
		FormData labelData = new FormData();
		labelData.left = new FormAttachment(0, margin);
		labelData.top = new FormAttachment(0);
		seperator.setLayoutData(labelData);
		FormData itemData = new FormData();
		itemData.right = new FormAttachment(100, (-1 * margin));
		itemData.top = new FormAttachment(viewerControl, margin, SWT.TOP);
		itemData.width = widthPreference + (margin * 2);
		itemData.height = preferredSize.y;
		itemControl.setLayoutData(itemData);
		FormData viewerData = new FormData();
		viewerData.left = new FormAttachment(seperator, margin);
		viewerData.right = new FormAttachment(itemControl, margin);
		viewerData.top = new FormAttachment(0);
		viewerData.bottom = new FormAttachment(100);
		viewerData.width = preferredSize.x + margin;
		viewerData.height = preferredSize.y;
		viewerControl.setLayoutData(viewerData);
		viewerControl.addMouseListener(new MouseAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.MouseAdapter#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
			 */
			public void mouseDoubleClick(MouseEvent e) {
				processDoubleClick();
			}
		});
		viewerControl.addMouseTrackListener(new MouseTrackListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.MouseTrackListener#mouseEnter(org.eclipse.swt.events.MouseEvent)
			 */
			public void mouseEnter(MouseEvent e) {
				//Do nothing
			}
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.MouseTrackListener#mouseExit(org.eclipse.swt.events.MouseEvent)
			 */
			public void mouseExit(MouseEvent e) {
				//Do nothing
			}
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.MouseTrackListener#mouseHover(org.eclipse.swt.events.MouseEvent)
			 */
			public void mouseHover(MouseEvent e) {
				item.openFloatingWindow();
			}
		});
		IContentProvider provider = new ProgressViewerContentProvider(viewer);
		viewer.setContentProvider(provider);
		viewer.setInput(provider);
		viewer.setLabelProvider(new ProgressViewerLabelProvider(viewerControl));
		viewer.setSorter(ProgressManagerUtil.getProgressViewerSorter());
		return region;
	}
	/**
	 * Return the animationItem for the receiver.
	 * 
	 * @return
	 */
	public AnimationItem getAnimationItem() {
		return item;
	}
	/**
	 * Return the control for the receiver.
	 * 
	 * @return
	 */
	public Control getControl() {
		return region;
	}
	/**
	 * Process the double click event.
	 */
	public void processDoubleClick() {
		boolean userMode = WorkbenchPlugin.getDefault().getPreferenceStore()
				.getBoolean(IPreferenceConstants.SHOW_USER_JOBS_IN_DIALOG);
		if (userMode) {
			Object[] items = viewer.displayedItems;
			if (items.length > 0 && items[0] instanceof JobInfo) {
				JobInfo info = (JobInfo) items[0];
				if (info.getJob().isUser()) {
					ProgressMonitorFocusJobDialog dialog = new ProgressMonitorFocusJobDialog(
							workbenchWindow.getShell());
					dialog.setJobAndOpen(info.getJob(),true);
					
					return;
				}
			}
		}
		ProgressManagerUtil.openProgressView(workbenchWindow);
	}
}