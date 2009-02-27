/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.workbench.ui.renderers.swt;

import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.model.application.MCommand;
import org.eclipse.e4.ui.model.application.MHandledItem;
import org.eclipse.e4.ui.model.application.MHandler;
import org.eclipse.e4.ui.model.application.MMenu;
import org.eclipse.e4.ui.model.application.MMenuItem;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MToolBarItem;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.workbench.ui.IHandlerService;
import org.eclipse.e4.workbench.ui.renderers.PartFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;

public abstract class SWTPartFactory extends PartFactory {

	public SWTPartFactory() {
		super();
	}

	protected void activate(MPart part) {
		//System.out.print("Activating"); //$NON-NLS-1$
		//if (part instanceof MContributedPart) {
		//	System.out.println(": " + ((MContributedPart) part).getName()); //$NON-NLS-1$
		//} else {
		//	System.out.println(" part without name"); //$NON-NLS-1$
		//}
		internalActivate(part, getContext(part));
	}

	private void internalActivate(MPart<?> part, IEclipseContext context) {
		MPart parent = part.getParent();
		if (parent != null) {
			parent.setActiveChild(part);
			IEclipseContext parentContext = getContext(parent);
			if (parentContext != null) {
				if (context != null && context != parentContext) {
					parentContext.set(IServiceConstants.ACTIVE_CHILD, context);
				}
				context = parentContext;
			}
			internalActivate(parent, context);
		}
	}

	public void createMenu(MPart<?> part, Object widgetObject, MMenu menu) {
		Widget widget = (Widget) widgetObject;
		org.eclipse.swt.widgets.Menu swtMenu;
		if (widget instanceof MenuItem) {
			swtMenu = new org.eclipse.swt.widgets.Menu(((MenuItem) widget)
					.getParent().getShell(), SWT.DROP_DOWN);
			((MenuItem) widget).setMenu(swtMenu);
		} else if (widget instanceof Decorations) {
			swtMenu = new org.eclipse.swt.widgets.Menu((Decorations) widget,
					SWT.BAR);
			((Decorations) widget).setMenuBar(swtMenu);
		} else if (widget instanceof Control) {
			swtMenu = new org.eclipse.swt.widgets.Menu((Control) widget);
			((Control) widget).setMenu(swtMenu);
		} else {
			throw new IllegalArgumentException(
					"The widget must be MenuItem, Decorations, or Control but is: " //$NON-NLS-1$
							+ widgetObject);
		}
		for (MMenuItem menuItem : menu.getItems()) {
			createMenuItem(part, swtMenu, menuItem);
		}
	}

	public void createToolBar(MPart<?> part, Object widgetObject,
			org.eclipse.e4.ui.model.application.MToolBar toolbar) {
		Composite composite = (Composite) widgetObject;
		org.eclipse.swt.widgets.ToolBar swtToolBar = new ToolBar(composite,
				SWT.FLAT | SWT.NO_FOCUS);
		for (MToolBarItem toolBarItem : toolbar.getItems()) {
			createToolBarItem(part, swtToolBar, toolBarItem);
		}
	}

	private MHandler getHandler(Display display, MHandledItem item) {
		MHandler h = null;
		MCommand command = item.getCommand();
		if (command == null) {
			return h;
		}
		// find the first useful part in the model
		Control control = display.getFocusControl();
		MPart<?> part = null;
		while (control != null && part == null) {
			part = (MPart<?>) control.getData(OWNING_ME);
			control = control.getParent();
		}
		if (part == null) {
			return null;
		}
		// get the applicable context (or parent)
		IEclipseContext partContext = getContext(part);
		if (partContext != null) {
			IHandlerService hs = (IHandlerService) partContext
					.get(IHandlerService.class.getName());
			if (hs != null) {
				h = hs.getHandler(command);
			}
		}
		return h;
	}

	private void createToolBarItem(MPart<?> part, ToolBar swtMenu,
			final org.eclipse.e4.ui.model.application.MToolBarItem toolBarItem) {
		int style = SWT.PUSH;
		final ToolItem newToolItem = new ToolItem(swtMenu, style);
		newToolItem.setText(toolBarItem.getName());
		newToolItem.setToolTipText(toolBarItem.getTooltip());
		newToolItem.setImage(getImage(toolBarItem));
		if (toolBarItem.getCommand() != null) {
			final IEclipseContext localContext = getContext(part);
			newToolItem.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					Object result = canExecuteItem(localContext, newToolItem
							.getDisplay(), toolBarItem);
					if (Boolean.TRUE.equals(result)) {
						executeItem(localContext, newToolItem.getDisplay(),
								toolBarItem);
					}
				}
			});
			newToolItem.getDisplay().timerExec(250, new Runnable() {
				public void run() {
					if (newToolItem.isDisposed()) {
						return;
					}
					Object result = canExecuteItem(localContext, newToolItem
							.getDisplay(), toolBarItem);
					((ToolItem) newToolItem).setEnabled(Boolean.TRUE
							.equals(result));
					newToolItem.getDisplay().timerExec(100, this);
				}
			});
		}
	}

	protected Object canExecuteItem(final IEclipseContext context,
			Display display, final MHandledItem item) {
		MHandler h = getHandler(display, item);
		if (h == null) {
			return Boolean.TRUE;
		}

		Object result = contributionFactory.call(h.getObject(), h.getURI(),
				"canExecute", context, Boolean.TRUE); //$NON-NLS-1$
		return result;
	}

	protected void executeItem(final IEclipseContext context, Display display,
			final MHandledItem item) {
		MHandler h = getHandler(display, item);
		if (h == null) {
			return;
		}
		contributionFactory.call(h.getObject(), h.getURI(), "execute", //$NON-NLS-1$
				context, null);
	}

	private void createMenuItem(MPart<?> part, final org.eclipse.swt.widgets.Menu parentMenu,
			final MHandledItem handledItem) {
		int style = SWT.PUSH;
		if (handledItem instanceof MMenuItem) {
			if (((MMenuItem) handledItem).isSeparator()) {
				style = SWT.SEPARATOR;
			} else if (((MMenuItem) handledItem).getMenu() != null) {
				style = SWT.CASCADE;
			}
		}

		final MenuItem newMenuItem = new MenuItem(parentMenu, style);
		if (style != SWT.SEPARATOR) {
			newMenuItem.setText(handledItem.getName());
			newMenuItem.setImage(getImage(handledItem));
			newMenuItem.setEnabled(true);
		}

		if (handledItem.getMenu() != null) {
			createMenu(part, newMenuItem, handledItem.getMenu());
		}
		final IEclipseContext localContext = getContext(part);

		if (handledItem.getCommand() != null) {
			newMenuItem.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					Object result = canExecuteItem(localContext, newMenuItem
							.getDisplay(), handledItem);
					if (Boolean.TRUE.equals(result)) {
						executeItem(localContext, newMenuItem.getDisplay(),
								handledItem);
					}
				}
			});
			parentMenu.addListener(SWT.Show, new Listener() {

				public void handleEvent(Event event) {
					if (newMenuItem.isDisposed()) {
						return;
					}
					Object result = canExecuteItem(localContext, newMenuItem
							.getDisplay(), handledItem);

					((MenuItem) newMenuItem).setEnabled(Boolean.TRUE
							.equals(result));
				}
			});
		}
	}
}
