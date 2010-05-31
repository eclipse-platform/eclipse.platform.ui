package org.eclipse.e4.ui.workbench.swt.modeling;

import javax.inject.Inject;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.internal.expressions.ReferenceExpression;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.contexts.IContextConstants;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MCoreExpression;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MPopupMenu;
import org.eclipse.e4.workbench.modeling.ExpressionContext;
import org.eclipse.e4.workbench.ui.IPresentationEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

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

	static class ContextMenuListener implements Listener {
		private MPopupMenu mmenu;
		private IPresentationEngine renderer;
		private IEclipseContext tmpContext;
		private MPart part;
		private MApplication application;

		public ContextMenuListener(IPresentationEngine renderer,
				MPopupMenu mmenu, MPart part, MApplication application) {
			this.renderer = renderer;
			this.mmenu = mmenu;
			this.part = part;
			this.application = application;
		}

		public void handleEvent(Event event) {
			if (event.widget == null || event.widget.isDisposed()) {
				return;
			}
			final Menu menu = (Menu) event.widget;
			switch (event.type) {
			case SWT.Show:
				tmpContext = (IEclipseContext) part.getContext().getLocal(
						IContextConstants.ACTIVE_CHILD);
				part.getContext().set(IContextConstants.ACTIVE_CHILD,
						mmenu.getContext());
				showMenu(renderer, menu, mmenu, application);
				break;
			case SWT.Hide:
				final IEclipseContext oldContext = tmpContext;
				tmpContext = null;
				menu.getDisplay().asyncExec(new Runnable() {
					public void run() {
						if (!menu.isDisposed()) {
							for (MenuItem mi : menu.getItems()) {
								mi.dispose();
							}
						}
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
			MPopupMenu mmenu, MApplication application) {
		for (MMenuElement element : mmenu.getChildren()) {
			renderer.createGui(element, menu);
		}
		if (application.getMenuContributions().isEmpty()) {
			return;
		}
		ExpressionContext eContext = new ExpressionContext(mmenu.getContext());
		for (MMenuContribution menuContribution : application
				.getMenuContributions()) {
			if (isVisible(menuContribution, eContext)) {
				for (MMenuElement item : menuContribution.getChildren()) {
					renderer.createGui(item, menu);
				}
			}
		}
	}

	private static boolean isVisible(MMenuContribution menuContribution,
			ExpressionContext eContext) {
		if (menuContribution.getVisibleWhen() == null) {
			return false;
		}
		MCoreExpression exp = (MCoreExpression) menuContribution
				.getVisibleWhen();
		ReferenceExpression ref = new ReferenceExpression(
				exp.getCoreExpressionId());
		try {
			return EvaluationResult.TRUE == ref.evaluate(eContext);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return false;
	}
}
