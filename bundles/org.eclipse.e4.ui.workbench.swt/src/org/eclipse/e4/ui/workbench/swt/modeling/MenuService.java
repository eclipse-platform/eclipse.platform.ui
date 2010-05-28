package org.eclipse.e4.ui.workbench.swt.modeling;

import javax.inject.Inject;
import org.eclipse.e4.core.contexts.IContextConstants;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MPopupMenu;
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
				myPart);
		menu.addListener(SWT.Show, listener);
		menu.addListener(SWT.Hide, listener);
		menu.addListener(SWT.Dispose, listener);
		return true;
	}

	static class ContextMenuListener implements Listener {
		private MPopupMenu mmenu;
		private IPresentationEngine renderer;
		private IEclipseContext tmpContext;
		private MPart part;

		public ContextMenuListener(IPresentationEngine renderer,
				MPopupMenu mmenu, MPart part) {
			this.renderer = renderer;
			this.mmenu = mmenu;
			this.part = part;
		}

		public void handleEvent(Event event) {
			if (event.widget == null || event.widget.isDisposed()) {
				return;
			}
			Menu menu = (Menu) event.widget;
			switch (event.type) {
			case SWT.Show:
				showMenu(renderer, menu, mmenu);
				tmpContext = (IEclipseContext) part.getContext().getLocal(
						IContextConstants.ACTIVE_CHILD);
				part.getContext().set(IContextConstants.ACTIVE_CHILD,
						mmenu.getContext());
				break;
			case SWT.Hide:
				final IEclipseContext oldContext = tmpContext;
				tmpContext = null;
				menu.getDisplay().asyncExec(new Runnable() {
					public void run() {
						part.getContext().set(IContextConstants.ACTIVE_CHILD,
								oldContext);
					}
				});
				break;
			case SWT.Dispose:
				mmenu.getContext().dispose();
				mmenu.setContext(null);
				break;
			}
		}
	}

	public static void showMenu(IPresentationEngine renderer, Menu menu,
			MPopupMenu mmenu) {
		for (MMenuElement element : mmenu.getChildren()) {
			renderer.removeGui(element);
		}
		for (MMenuElement element : mmenu.getChildren()) {
			renderer.createGui(element, menu);
		}
	}
}
