package org.eclipse.ui.internal;

import org.eclipse.ui.internal.misc.*;
/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.ui.*;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import java.util.*;

/**
 * A dynamic contribution item which supports to switch to other Contexts.
 */
public class ShowFastViewContribution extends ContributionItem {
	private IWorkbenchWindow window;	
/**
 * Create a new menu item.
 */
public ShowFastViewContribution(IWorkbenchWindow window) {
	super("showFastViewContr");
	this.window = window;
}
/**
 * The default implementation of this <code>IContributionItem</code>
 * method does nothing. Subclasses may override.
 */
public void fill(ToolBar parent, int index) {
	// Get page.
	WorkbenchPage page = (WorkbenchPage)window.getActivePage();
	if (page == null)
		return;

	// Get views.
	IViewPart [] views = page.getFastViews();

	// Create tool item for each view.
	int size = views.length;
	for (int nX = 0; nX < size; nX ++) {
		final IViewPart view = views[nX];
		final ToolItem item = new ToolItem(parent, SWT.PUSH, index);
		item.setImage(view.getTitleImage());
		item.setToolTipText(view.getTitle());
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				showView(view);
			}
		});
		index ++;
	}
}
/**
 * Returns whether the contribution is dynamic.
 */
public boolean isDynamic() {
	return true;
}
/**
 * Open a view.
 */
private void showView(IViewPart view) {
	WorkbenchPage page = (WorkbenchPage)view.getSite().getPage();
	page.toggleFastView(view);
}
}
