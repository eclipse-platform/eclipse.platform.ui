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
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.ui.internal.WorkbenchWindow;
/**
 * The ProgressRegion is class for the region of the workbench where the
 * progress line and the animation item are shown.
 */
public class ProgressRegion {
	ProgressViewer viewer;
	AnimationItem item;
	Composite region;
	
	/**
	 * Create a new instance of the receiver.
	 */
	public ProgressRegion() {
		//No default behavior.
	}
	/**
	 * Create the contents of the receiver in the parent. Use the
	 * window for the animation item. 
	 * @param parent The parent widget of the composite.
	 * @param window The WorkbenchWindow this is in.
	 * @return
	 */
	public Control createContents(Composite parent, WorkbenchWindow window) {
		region = new Composite(parent, SWT.BORDER);
		
		FormLayout regionLayout = new FormLayout();
		region.setLayout(regionLayout);
		region.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_CYAN));
		
		item = new AnimationItem(window);
		item.createControl(region);
		Control itemControl = item.getControl();
		setInfoColors(itemControl);

		FormData itemData = new FormData();
		itemData.right = new FormAttachment(100);
		itemData.top = new FormAttachment(0);
		itemData.width = AnimationManager.getInstance().getPreferredWidth() + 5;
		itemControl.setLayoutData(itemData);
		
		viewer = new ProgressViewer(region, SWT.NONE, 1);
		viewer.setUseHashlookup(true);
		Control viewerControl = viewer.getControl();
		setInfoColors(viewerControl);

		int margin = 2;
		
		FormData viewerData = new FormData();
		viewerData.left = new FormAttachment(0);
		viewerData.right = new FormAttachment(itemControl, margin);
		viewerData.top = new FormAttachment(0);
		viewerData.bottom = new FormAttachment(itemControl,0,SWT.BOTTOM);
		Point preferredSize = viewer.getSizeHints();
		viewerData.width = preferredSize.x + margin;
		viewerData.height = preferredSize.y;
		viewerControl.setLayoutData(viewerData);
		
		IContentProvider provider = new ProgressViewerContentProvider(viewer);
		viewer.setContentProvider(provider);
		viewer.setInput(provider);
		viewer.setLabelProvider(new ProgressViewerLabelProvider(viewerControl));
		
		return region;
	}
	/**
	 * Set the info colors opf the control
	 * 
	 * @param control
	 *            The Control to color.
	 */
	private void setInfoColors(Control control) {
		control.setBackground(control.getDisplay().getSystemColor(
				SWT.COLOR_INFO_BACKGROUND));
		control.setForeground(control.getDisplay().getSystemColor(
				SWT.COLOR_INFO_FOREGROUND));
	}
	
	/**
	 * Return the animationItem for the receiver.
	 * @return
	 */
	public AnimationItem getAnimationItem(){
		return item;
	}
	
	/**
	 * Return the control for the receiver.
	 * @return
	 */
	public Control getControl(){
		return region;
	}
}
