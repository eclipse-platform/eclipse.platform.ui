package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugViewAdapter;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

/**
 * Functionality common to views in the debugger
 */

public abstract class AbstractDebugView extends ViewPart implements IDebugViewAdapter {
	
	protected StructuredViewer fViewer = null;

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IDebugViewAdapter.class) {
			return this;
		}
		return super.getAdapter(adapter);
	}
	/**
	 * IWorkbenchPart#dispose()
	 */
	public void dispose() {
		setViewer(null);
		super.dispose();
	}
	
	/**
	 * @see IDebugViewAdapter#getViewer()
	 */
	public StructuredViewer getViewer() {
		return fViewer;
	}
	
	/**
	 * @see IDebugViewAdapter#getPresentation(String)
	 */
	public IDebugModelPresentation getPresentation(String id) {
		return ((DelegatingModelPresentation)getViewer().getLabelProvider()).getPresentation(id);
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
		getSite().registerContextMenu(menuMgr, getViewer());
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
				if (getViewer().getControl().isDisposed()) {
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
		if (getViewer().getControl().isDisposed()) {
			return;
		}
		getViewer().getControl().getDisplay().asyncExec(r);
	}
	
	/**
	 * @see IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		getViewer().getControl().setFocus();
	}
	
	protected void setViewer(StructuredViewer viewer) {
		fViewer = viewer;
	}
	
	protected abstract void fillContextMenu(IMenuManager mgr);
	
	protected abstract void configureToolBar(IToolBarManager tbm);	
}	

