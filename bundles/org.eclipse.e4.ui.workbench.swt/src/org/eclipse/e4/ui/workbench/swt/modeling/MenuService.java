package org.eclipse.e4.ui.workbench.swt.modeling;

import javax.inject.Inject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MPopupMenu;
import org.eclipse.e4.workbench.ui.IPresentationEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;

public class MenuService implements EMenuService {
	@Inject
	private MPart myPart;

	@Inject
	private MApplication application;

	@Inject
	private IPresentationEngine renderer;

	public MPopupMenu registerContextMenu(Menu menu, String menuId) {
		for (MMenu mmenu : myPart.getMenus()) {
			if (menuId.equals(mmenu.getElementId())
					&& mmenu instanceof MPopupMenu) {
				if (registerMenu(menu, (MPopupMenu) mmenu)) {
					return (MPopupMenu) mmenu;
				} else {
					return null;
				}
			}
		}
		return null;
	}

	private boolean registerMenu(Menu menu, MPopupMenu mmenu) {
		if (mmenu.getWidget() != null) {
			return false;
		}
		mmenu.setWidget(menu);
		IEclipseContext popupContext = myPart.getContext().createChild(
				"popup:" + mmenu.getElementId());
		mmenu.setContext(popupContext);
		ContextMenuListener listener = new ContextMenuListener(renderer, mmenu,
				myPart, application);
		menu.addListener(SWT.Show, listener);
		menu.addListener(SWT.Hide, listener);
		menu.addListener(SWT.Dispose, listener);
		return true;
	}
}
