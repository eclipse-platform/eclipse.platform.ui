/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.internal.layout.IWindowTrim;

/**
 * This is the base class to use to represent a LayoutPart when
 * it is being displayed in the trim.
 * <p>
 * It manages the life-cycle of the control and its contents but
 * requres that subclasses populate the ToolBar with its own items
 * and the logic to control them.
 * 
 * @since 3.3
 *
 */
public abstract class TrimPart implements IWindowTrim {
	// State
	protected WorkbenchWindow window;
	protected LayoutPart part;
	
	// Trim State
	protected TrimFrame tf = null;
	protected ToolBar toolBar = null;
	protected int curSide;

	/**
	 * Add the items appropiate for the particular
	 * subclass.
	 */
	protected abstract void addItems();
	
	/**
	 * Constructs a trim element for some LayoutPart
	 * 
	 * @param window The window hosting this trim element
	 * @param part The LayoutPart being represented
	 */
	public TrimPart(WorkbenchWindow window, LayoutPart part) {
		this.window = window;		
		this.part = part;
	}   	

	// TrimPart Life-cycle
	
	/**
	 * Put the stack back into the presentation
	 */
	protected void restorePart() {
		Perspective persp = window.getActiveWorkbenchPage().getActivePerspective();
		
		if (part != null)
			persp.restoreTrimPart(part);
	}

	/**
	 * Clear items added by the subclass. The subclass should
	 * add a dispose listener if it needs to clean up...
	 */
	private void clearContributions() {
		// Clear all items -except- the restore button
		ToolItem[] items = toolBar.getItems();
		for (int i = 1; i < items.length; i++) {
			items[i].dispose();
		}
	}
	
	/**
	 * Cycle the contents of the part in order to pick
	 * up any changes in the actual part that the trim
	 * is representing. 
	 */
	public void refresh() {
		clearContributions();
		addItems();
	}
	
	// IWindowTrim handling

	/**
	 * Create the Toolbar
	 */
	private void createControl() {
		tf = new TrimFrame(window.getShell());
		int orientation = (curSide == SWT.TOP || curSide == SWT.BOTTOM) ? 
				SWT.HORIZONTAL : SWT.VERTICAL;
		toolBar = new ToolBar(tf.getComposite(), orientation);
		
		toolBar.setData(this);
		
        // Construct the 'restore' button
        ToolItem restoreItem = new  ToolItem(toolBar, SWT.PUSH, 0);        
        Image tbImage = WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_ETOOL_RESTORE_TRIMPART);
        restoreItem.setImage(tbImage);       
        String menuTip = WorkbenchMessages.StandardSystemToolbar_Restore;
        restoreItem.setToolTipText(menuTip);
        restoreItem.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				restorePart();
			}
			public void widgetSelected(SelectionEvent e) {
				restorePart();
			}
        });
        
		// refresh the bar's contents
		addItems();
	}

	private void dispose() {
		if (tf != null && tf.getComposite() != null && !tf.getComposite().isDisposed())
			tf.getComposite().dispose();
		tf = null;
		
		// The toolbar is disposed when its parent is
		toolBar = null;
	}
	
	public void dock(int dropSide) {
		curSide = dropSide;
	
		// Re-create the toolBar
		dispose();
		createControl();
        toolBar.pack();
        tf.getComposite().pack();
	}

	public Control getControl() {
		return tf.getComposite();
	}

	public String getDisplayName() {
		return part.getID();
	}

	public int getHeightHint() {
		Point cs =  tf.getComposite().computeSize(SWT.DEFAULT, SWT.DEFAULT);
		return cs.y;
	}

	public String getId() {
		return part.getID();
	}

	public int getValidSides() {
		return SWT.TOP | SWT.BOTTOM | SWT.LEFT | SWT.RIGHT;
	}

	public int getWidthHint() {
		return tf.getComposite().computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
	}

	public void handleClose() {
	}

	public boolean isCloseable() {
		return false;
	}

	public boolean isResizeable() {
		return false;
	}

	public int getCurrentSide() {
		return curSide;
	}

}
