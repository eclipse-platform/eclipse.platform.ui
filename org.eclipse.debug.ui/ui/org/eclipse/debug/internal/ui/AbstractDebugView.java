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
	
	private StructuredViewer fViewer = null;

	/**
	 * The debug selection provider associated with this view
	 * or <code>null</code> if none.
	 */
	private DebugSelectionProvider fDebugSelectionProvider;
	
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
		MenuManager menuMgr= new MenuManager("#PopUp"); //$NON-NLS-1$
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
		StructuredViewer viewer= getViewer();
		if (viewer != null) {
			viewer.getControl().setFocus();
		}
	}
	
	protected void setViewer(StructuredViewer viewer) {
		fViewer = viewer;
		if (viewer != null) {
			if (getDebugSelectionProvider() != null) {
				getViewer().addSelectionChangedListener(getDebugSelectionProvider());
			}				
		}
	}
	
	protected abstract void fillContextMenu(IMenuManager mgr);
	
	protected abstract void configureToolBar(IToolBarManager tbm);	
	
	/**
	 * Sets the debug selection provider for this debug view,
	 * possibly <code>null</code>
	 * 
	 * @param provider debug selection provider
	 */
	protected void setDebugSelectionProvider(DebugSelectionProvider provider) {
		fDebugSelectionProvider = provider;
		if (getViewer() != null) {
			getViewer().addSelectionChangedListener(provider);
		}
	}
	
	/**
	 * Returns the debug selection provider for this debug view,
	 * possibly <code>null</code>
	 * 
	 * @return  debug selection provider, or <code>null</code>
	 */
	protected DebugSelectionProvider getDebugSelectionProvider() {
		return fDebugSelectionProvider;
	}	
	
	protected void controlCreated() {

	}	
}	

