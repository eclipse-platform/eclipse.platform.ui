package org.eclipse.ui.internal;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * This contribution item creates a menu to open perspectives within
 * a workbench window.
 */
public class PerspectiveContributionItem extends ContributionItem {
	private static final Image image = 
		WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_NEW_PAGE).createImage();
	private static final Image hotImage = 
		WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_NEW_PAGE_HOVER).createImage();

	private ToolItem widget = null;
	private ToolBar parentWidget = null;
	private IWorkbenchWindow window;

	/**
	 * Creates a new contribution item from the given action.
	 * The id of the action is used as the id of the item.
	 */
	public PerspectiveContributionItem(IWorkbenchWindow window) {
		super();
		this.window = window;
	}
	
	/* (non-Javadoc)
	 * Method declared on IContributionItem.
	 */
	public void fill(ToolBar parent, int index) {
		if (widget == null && parent != null) {
			if (index >= 0)
				widget = new ToolItem(parent, SWT.PUSH, index);
			else
				widget = new ToolItem(parent, SWT.PUSH);
			widget.setToolTipText(WorkbenchMessages.getString("PerspectiveContributionItem.toolTip")); //$NON-NLS-1$
			widget.setImage(image);
			widget.setHotImage(hotImage);
			widget.setData(this);
			widget.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					Menu menu = new Menu(parentWidget);
					ChangeToPerspectiveMenu menuDescription =
						new ChangeToPerspectiveMenu(window);
					menuDescription.fill(menu, 0);
					popUpMenu(event, menu);
				}
			});
			parentWidget = parent;
		}
	}
	
	/**
	 * Pop up the supplied menu at the point where the event occured.
	 */
	private void popUpMenu(SelectionEvent event, Menu menu) {
		Point pt = new Point(event.x, event.y);
		pt = parentWidget.toDisplay(pt);
		menu.setLocation(pt.x, pt.y);
		menu.setVisible(true);
	}
}