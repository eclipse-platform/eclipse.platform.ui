package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * A dynamic contribution item which supports to switch to other Contexts.
 */
public class ShowFastViewContribution extends ContributionItem {
	public static final String FAST_VIEW = "FastView";
	private IWorkbenchWindow window;
	/**
	 * Create a new menu item.
	 */
	public ShowFastViewContribution(IWorkbenchWindow window) {
		super("showFastViewContr"); //$NON-NLS-1$
		this.window = window;
	}
	/**
	 * The default implementation of this <code>IContributionItem</code>
	 * method does nothing. Subclasses may override.
	 */
	public void fill(ToolBar parent, int index) {
		// Get page.
		WorkbenchPage page = (WorkbenchPage) window.getActivePage();
		if (page == null)
			return;

		// Get views.
		IViewPart[] views = page.getFastViews();

		// Create tool item for each view.
		int size = views.length;
		for (int nX = 0; nX < size; nX++) {
			final IViewPart view = views[nX];
			final ToolItem item = new ToolItem(parent, SWT.PUSH, index);
			item.setImage(view.getTitleImage());
			item.setToolTipText(view.getTitle());
			item.setData(FAST_VIEW, view);
			item.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					showView(view);
				}
			});
			index++;
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
		WorkbenchPage page = (WorkbenchPage) view.getSite().getPage();
		page.toggleFastView(view);
	}
}