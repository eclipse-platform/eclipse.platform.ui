package org.eclipse.e4.ui.workbench.swt.modeling;

import javax.inject.Inject;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.workbench.ui.IPresentationEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;

public class MenuService implements EMenuService {
	@Inject
	private MPart myPart;

	@Inject
	private IPresentationEngine renderer;

	public void registerContextMenu(Menu menu, String menuId) {
		for (MMenu mmenu : myPart.getMenus()) {
			if (menuId.equals(mmenu.getElementId())) {
				registerMenu(menu, mmenu);
			}
		}
	}

	private void registerMenu(Menu menu, MMenu mmenu) {
		if (mmenu.getWidget() != null) {
			return;
		}
		mmenu.setWidget(menu);
		ContextMenuListener listener = new ContextMenuListener(renderer, mmenu);
		menu.addListener(SWT.Show, listener);
		menu.addListener(SWT.Hide, listener);
	}

	static class ContextMenuListener implements Listener {
		private MMenu mmenu;
		private IPresentationEngine renderer;

		public ContextMenuListener(IPresentationEngine renderer, MMenu mmenu) {
			this.renderer = renderer;
			this.mmenu = mmenu;
		}

		public void handleEvent(Event event) {
			if (event.widget == null || event.widget.isDisposed()) {
				return;
			}
			Menu menu = (Menu) event.widget;
			switch (event.type) {
			case SWT.Show:
				showMenu(renderer, menu, mmenu);
				break;
			case SWT.Hide:
				// currently a NOP
				break;
			}
		}
	}

	public static void showMenu(IPresentationEngine renderer, Menu menu,
			MMenu mmenu) {
		for (MMenuElement element : mmenu.getChildren()) {
			renderer.removeGui(element);
		}
		for (MMenuElement element : mmenu.getChildren()) {
			renderer.createGui(element, menu);
		}
	}
}
