package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.ui.IDebugModelPresentation;import org.eclipse.debug.ui.IDebugViewAdapter;import org.eclipse.jface.action.*;import org.eclipse.jface.viewers.StructuredViewer;import org.eclipse.swt.widgets.Control;import org.eclipse.swt.widgets.Menu;import org.eclipse.ui.*;import org.eclipse.ui.part.ViewPart;

/**
 * Functionality common to views in the debugger
 */

public abstract class AbstractDebugView extends ViewPart implements IDebugViewAdapter, IPartListener {
	
	protected final static String TITLE_TOOLTIPTEXT= "title_toolTipText";
	
	protected StructuredViewer fViewer = null;

	/**
	 * @see IAdaptable
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IDebugViewAdapter.class) {
			return this;
		}
		return super.getAdapter(adapter);
	}
	
	/**
	 * @see IViewPart
	 */
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		getSite().getPage().addPartListener(this);
	}

	public void dispose() {
		getSite().getPage().removePartListener(this);
		fViewer= null;
		super.dispose();
	}
	
	/**
	 * @see IDebugViewAdapter
	 */
	public StructuredViewer getViewer() {
		return fViewer;
	}
	
	/**
	 * @see IDebugViewAdapter
	 */
	public IDebugModelPresentation getPresentation(String id) {
		return ((DelegatingModelPresentation)fViewer.getLabelProvider()).getPresentation(id);
	}
	
	protected void createContextMenu(Control menuControl) {
		MenuManager menuMgr= new MenuManager("#PopUp");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				fillContextMenu(mgr);
			}
		});
		Menu menu= menuMgr.createContextMenu(menuControl);
		menuControl.setMenu(menu);

		// register the context menu such that other plugins may contribute to it
		getSite().registerContextMenu(menuMgr, fViewer);
	}

	/**
	 * Configures the toolBar
	 */
	protected void initializeToolBar() {
		final IToolBarManager tbm= getViewSite().getActionBars().getToolBarManager();
		configureToolBar(tbm);
		getViewSite().getActionBars().updateActionBars();
		
		// this is in a runnable to be run after this view's pane
		// is created
		Runnable r = new Runnable() {
			public void run() {
				if (fViewer.getControl().isDisposed()) {
					return;
				}
				IContributionItem[] items = tbm.getItems();
				if (items != null) {
					for (int i = 0; i < items.length; i++) {
						if (items[i] instanceof ActionContributionItem) {
							IAction action = ((ActionContributionItem)items[i]).getAction();
							if (action.isChecked()) {
								action.run();
							}
						}
					}
				}
			}
		};
		if (fViewer.getControl().isDisposed()) {
			return;
		}
		fViewer.getControl().getDisplay().asyncExec(r);
	}
	
	/**
	 * @see IWorkbenchPart
	 */
	public void setFocus() {
		fViewer.getControl().setFocus();
	}
	
	/**
	 * Returns the title tooltip for the View icon of this view part.
	 */
	protected String getTitleToolTipText(String prefix) {
		return DebugUIUtils.getResourceString(prefix + TITLE_TOOLTIPTEXT);
	}
	
	protected abstract void fillContextMenu(IMenuManager mgr);
	
	protected abstract void configureToolBar(IToolBarManager tbm);	
	/**
	 * @see IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partOpened(IWorkbenchPart arg0) {
	}

	/**
	 * @see IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partDeactivated(IWorkbenchPart arg0) {
	}

	/**
	 * @see IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partClosed(IWorkbenchPart arg0) {
	}

	/**
	 * @see IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partBroughtToTop(IWorkbenchPart arg0) {
	}

	/**
	 * @see IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partActivated(IWorkbenchPart arg0) {
	}
}	

