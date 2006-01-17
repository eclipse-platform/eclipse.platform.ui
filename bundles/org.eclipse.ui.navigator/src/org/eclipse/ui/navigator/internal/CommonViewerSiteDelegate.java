package org.eclipse.ui.navigator.internal;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.IMenuRegistration;

public class CommonViewerSiteDelegate implements ICommonViewerSite {
	
	
	private String id;
	private IWorkbenchPage page;
	private ISelectionProvider selectionProvider;
	private IActionBars actionBars;
	private IMenuRegistration menuRegistration;

	public CommonViewerSiteDelegate(String anId, IWorkbenchPage aPage, IMenuRegistration aMenuRegistration, ISelectionProvider aSelectionProvider, IActionBars theActionBars) {
		Assert.isNotNull(anId);
		Assert.isNotNull(aPage);
		Assert.isNotNull(aSelectionProvider);
		Assert.isNotNull(theActionBars);
		id = anId;
		page = aPage;
		selectionProvider = aSelectionProvider;
		actionBars = theActionBars;
		menuRegistration = aMenuRegistration;
	}
	

	public String getId() {
		return id;
	}

	public IWorkbenchWindow getWorkbenchWindow() {
		return page.getWorkbenchWindow();
		
	}

	public Shell getShell() {
		return page.getWorkbenchWindow().getShell();
	}

	public ISelectionProvider getSelectionProvider() {
		return selectionProvider;
	}

	public IWorkbenchPage getPage() {
		return page;
	}

	public void registerContextMenu(String menuId, MenuManager menuManager,
			ISelectionProvider selectionProvider) {
		menuRegistration.registerContextMenu(menuId, menuManager, selectionProvider);
	}

	public IActionBars getActionBars() {
		return actionBars;
	}

	public Object getAdapter(Class adapter) { 
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}


	public void setSelectionProvider(ISelectionProvider aSelectionProvider) {
		selectionProvider = aSelectionProvider;
	}

}
