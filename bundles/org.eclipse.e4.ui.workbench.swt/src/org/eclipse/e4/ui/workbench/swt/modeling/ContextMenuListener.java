package org.eclipse.e4.ui.workbench.swt.modeling;

import java.util.ArrayList;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.internal.expressions.ReferenceExpression;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.contexts.IContextConstants;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MCoreExpression;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MPopupMenu;
import org.eclipse.e4.workbench.modeling.ExpressionContext;
import org.eclipse.e4.workbench.ui.IPresentationEngine;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;

class ContextMenuListener implements Listener {
	private MPopupMenu mmenu;
	private IPresentationEngine renderer;
	private IEclipseContext tmpContext;
	private MPart part;
	private MApplication application;
	private ArrayList<MMenuElement> menuContributionsToRemove = new ArrayList<MMenuElement>();

	public ContextMenuListener(IPresentationEngine renderer, MPopupMenu mmenu,
			MPart part, MApplication application) {
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
			showMenu(menu);
			break;
		case SWT.Hide:
			hideMenu(menu);
			break;
		case SWT.Dispose:
			mmenu.getContext().dispose();
			mmenu.setContext(null);
			break;
		}
	}

	private void showMenu(Menu menu) {
		ExpressionContext eContext = new ExpressionContext(mmenu.getContext());
		for (MMenuContribution menuContribution : application
				.getMenuContributions()) {
			if (isVisible(menuContribution, eContext)) {
				for (MMenuElement item : menuContribution.getChildren()) {
					MMenuElement copy = (MMenuElement) EcoreUtil
							.copy((EObject) item);
					menuContributionsToRemove.add(copy);
					mmenu.getChildren().add(copy);
				}
			}
		}
		for (MMenuElement element : mmenu.getChildren()) {
			renderer.createGui(element, menu);
		}
	}

	private boolean isVisible(MMenuContribution menuContribution,
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

	private void hideMenu(final Menu menu) {
		final IEclipseContext oldContext = tmpContext;
		tmpContext = null;
		final ArrayList<MMenuElement> tmpList = menuContributionsToRemove;
		menuContributionsToRemove = new ArrayList<MMenuElement>();
		menu.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (!menu.isDisposed()) {
					for (MMenuElement element : mmenu.getChildren()) {
						renderer.removeGui(element);
					}

					for (MMenuElement item : tmpList) {
						mmenu.getChildren().remove(item);
					}
				}
				part.getContext().set(IContextConstants.ACTIVE_CHILD,
						oldContext);
			}
		});
	}
}