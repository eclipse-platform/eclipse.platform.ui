/*******************************************************************************
 * Copyright (c) 2007, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Friederike Schertel <friederike@schertel.org> - Bug 478336
 ******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.renderers.swt.StackRenderer;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Shows the View Menu
 * <p>
 * Replacement for: ShowViewMenuAction
 * </p>
 *
 * @since 3.3
 *
 */
public class ShowViewMenuHandler extends AbstractEvaluationHandler {

	private Expression enabledWhen;

	public ShowViewMenuHandler() {
		registerEnablement();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part != null) {
			IWorkbenchPartSite site = part.getSite();
			if (site instanceof PartSite) {
				final MPart model = ((PartSite) site).getModel();
				Composite partContainer = (Composite) model.getWidget();
				if (partContainer != null) {
					Composite parent = partContainer.getParent();
					while (parent != null) {
						if (parent instanceof CTabFolder) {
							CTabFolder ctf = (CTabFolder) parent;
							final Control topRight = ctf.getTopRight();
							if (topRight instanceof Composite) {
								for (Control child : ((Composite) topRight).getChildren()) {
									if (child instanceof ToolBar
											&& "ViewMenu".equals(child.getData())) { //$NON-NLS-1$
										ToolBar tb = (ToolBar) child;
										ToolItem ti = tb.getItem(0);
										Event sevent = new Event();
										sevent.type = SWT.Selection;
										sevent.widget = ti;
										ti.notifyListeners(SWT.Selection, sevent);
									}
								}
							}
							return null;
						}
						parent = parent.getParent();
					}

					MMenu menuModel = StackRenderer.getViewMenu(model);
					if (menuModel != null) {
						showStandaloneViewMenu(event, model, menuModel, partContainer);
					}
				}
			}
		}
		return null;
	}

	private void showStandaloneViewMenu(ExecutionEvent event, MPart model, MMenu menuModel,
			Composite partContainer) {
		Shell shell = partContainer.getShell();
		Menu menu = (Menu) menuModel.getWidget();
		if (menu == null) {
			IPresentationEngine engine = (IPresentationEngine) HandlerUtil.getVariable(event,
					IPresentationEngine.class.getName());
			menu = (Menu) engine.createGui(menuModel, shell, model.getContext());
			if (menu != null) {
				final Menu tmpMenu = menu;
				partContainer.addDisposeListener(e -> tmpMenu.dispose());
			}
		}

		Display display = menu.getDisplay();
		Point location = display.map(partContainer, null, partContainer.getLocation());
		Point size = partContainer.getSize();
		menu.setLocation(location.x + size.x, location.y);
		menu.setVisible(true);

		while (!menu.isDisposed() && menu.isVisible()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		if (!(menu.getData() instanceof MenuManager)) {
			menu.dispose();
		}
	}

	@Override
	protected Expression getEnabledWhenExpression() {
		if (enabledWhen == null) {
			enabledWhen = new Expression() {
				@Override
				public EvaluationResult evaluate(IEvaluationContext context) throws CoreException {
					// IWorkbenchPart part = InternalHandlerUtil
					// .getActivePart(context);
					// if (part != null) {
					// PartPane pane = ((PartSite) part.getSite()).getPane();
					// if ((pane instanceof ViewPane)
					// && ((ViewPane) pane).hasViewMenu()) {
					return EvaluationResult.TRUE;
					// }
					// }
					// return EvaluationResult.FALSE;
				}

				@Override
				public void collectExpressionInfo(ExpressionInfo info) {
					info.addVariableNameAccess(ISources.ACTIVE_PART_NAME);
				}
			};
		}
		return enabledWhen;
	}

}
