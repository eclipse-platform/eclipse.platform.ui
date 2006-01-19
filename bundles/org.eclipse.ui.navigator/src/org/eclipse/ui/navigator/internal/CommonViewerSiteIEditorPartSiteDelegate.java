package org.eclipse.ui.navigator.internal;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

public class CommonViewerSiteIEditorPartSiteDelegate implements
		ICommonViewerWorkbenchSite {

	private IEditorSite editorSite;

	public CommonViewerSiteIEditorPartSiteDelegate(IEditorSite anEditorSite) {
		editorSite = anEditorSite;
	}

	public String getId() {
		return editorSite.getId();
	}

	public IActionBars getActionBars() {
		return editorSite.getActionBars();
	}

	public Object getAdapter(Class adapter) {
		return editorSite.getAdapter(adapter);
	}

	public IKeyBindingService getKeyBindingService() {
		return editorSite.getKeyBindingService();
	}

	public IWorkbenchPage getPage() {
		return editorSite.getPage();
	}

	public ISelectionProvider getSelectionProvider() {
		return editorSite.getSelectionProvider();
	}

	public void setSelectionProvider(ISelectionProvider aSelectionProvider) {
		editorSite.setSelectionProvider(aSelectionProvider);
	}

	public Shell getShell() {
		return editorSite.getShell();
	}

	public IWorkbenchWindow getWorkbenchWindow() {
		return editorSite.getWorkbenchWindow();
	}

	public void registerContextMenu(String menuId, MenuManager menuManager,
			ISelectionProvider selectionProvider) {
		editorSite.registerContextMenu(menuId, menuManager, selectionProvider);
	}

}
