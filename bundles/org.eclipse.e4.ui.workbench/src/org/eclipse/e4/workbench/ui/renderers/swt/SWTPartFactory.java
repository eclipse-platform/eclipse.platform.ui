package org.eclipse.e4.workbench.ui.renderers.swt;

import org.eclipse.e4.core.services.Context;
import org.eclipse.e4.ui.model.application.Command;
import org.eclipse.e4.ui.model.application.HandledItem;
import org.eclipse.e4.ui.model.application.Handler;
import org.eclipse.e4.ui.model.application.Menu;
import org.eclipse.e4.ui.model.application.ToolBarItem;
import org.eclipse.e4.ui.services.ISelectionService;
import org.eclipse.e4.workbench.ui.IHandlerService;
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

	public void createMenu(Object widgetObject, Menu menu) {
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
					"The widget must be MenuItem, Decorations, or Control but is: "
							+ widgetObject);
		}
		for (org.eclipse.e4.ui.model.application.MenuItem menuItem : menu
				.getItems()) {
			createMenuItem(swtMenu, menuItem);
		}
	}

	public void createToolBar(Object widgetObject,
			org.eclipse.e4.ui.model.application.ToolBar toolbar) {
		Composite composite = (Composite) widgetObject;
		org.eclipse.swt.widgets.ToolBar swtToolBar = new ToolBar(composite,
				SWT.FLAT | SWT.NO_FOCUS);
		for (ToolBarItem toolBarItem : toolbar.getItems()) {
			createToolBarItem(swtToolBar, toolBarItem);
		}
	}

	private static Handler getHandler(Display display, HandledItem item) {
		Handler h = null;
		Command command = item.getCommand();
		if (command == null) {
			return h;
		}
		Control control = display.getFocusControl();
		while (control != null && h == null) {
			Context l = (Context) control.getData("LOCATOR");
			if (l != null) {
				IHandlerService hs = (IHandlerService) l
						.get(IHandlerService.class.getName());
				if (hs != null) {
					h = hs.getHandler(command);
				}
			}
			control = control.getParent();
		}
		return h;
	}

	private void createToolBarItem(ToolBar swtMenu,
			final org.eclipse.e4.ui.model.application.ToolBarItem toolBarItem) {
		int style = SWT.PUSH;
		final ToolItem newToolItem = new ToolItem(swtMenu, style);
		newToolItem.setText(toolBarItem.getName());
		newToolItem.setToolTipText(toolBarItem.getTooltip());
		newToolItem.setImage(getImage(toolBarItem));
		if (toolBarItem.getCommand() != null) {
			final ISelectionService selectionService = (ISelectionService) context
				.get(ISelectionService.class.getName());
			newToolItem.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					Object result = canExecuteItem(context, newToolItem
							.getDisplay(), toolBarItem, selectionService);
					if (Boolean.TRUE.equals(result)) {
						executeItem(context, newToolItem.getDisplay(),
								toolBarItem, selectionService);
					}
				}
			});
			newToolItem.getDisplay().timerExec(250, new Runnable() {
				public void run() {
					if (newToolItem.isDisposed()) {
						return;
					}
					Object result = canExecuteItem(context, newToolItem
							.getDisplay(), toolBarItem, selectionService);
					((ToolItem) newToolItem).setEnabled(Boolean.TRUE
							.equals(result));
					newToolItem.getDisplay().timerExec(100, this);
				}
			});
		}
	}

	protected Object canExecuteItem(final Context context,
			Display display, final HandledItem item,
			final ISelectionService selectionService) {
		Handler h = getHandler(display, item);
		if (h==null) {
			return Boolean.TRUE;
		}
		
		Object result = contributionFactory.call(h.getObject(), h.getURI(),
				"canExecute", context, Boolean.TRUE);
		return result;
	}

	protected void executeItem(final Context context,
			Display display, final HandledItem item,
			final ISelectionService selectionService) {
		Handler h = getHandler(display, item);
		if (h==null) {
			return;
		}
		contributionFactory.call(h.getObject(), h.getURI(), "execute",
				context, null);
	}

	private void createMenuItem(final org.eclipse.swt.widgets.Menu parentMenu,
			final HandledItem handledItem) {
		int style = SWT.PUSH;
		if (handledItem instanceof org.eclipse.e4.ui.model.application.MenuItem) {
			if (((org.eclipse.e4.ui.model.application.MenuItem) handledItem)
					.isSeparator()) {
				style = SWT.SEPARATOR;
			} else if (((org.eclipse.e4.ui.model.application.MenuItem) handledItem)
					.getMenu() != null) {
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
			createMenu(newMenuItem, handledItem.getMenu());
		}

		if (handledItem.getCommand() != null) {
			final ISelectionService selectionService = (ISelectionService) context
					.get(ISelectionService.class.getName());
			newMenuItem.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					Object result = canExecuteItem(context, newMenuItem
							.getDisplay(), handledItem, selectionService);
					if (Boolean.TRUE.equals(result)) {
						executeItem(context, newMenuItem.getDisplay(),
								handledItem, selectionService);
					}
				}
			});
			parentMenu.addListener(SWT.Show, new Listener() {

				public void handleEvent(Event event) {
					if (newMenuItem.isDisposed()) {
						return;
					}
					Object result = canExecuteItem(context, newMenuItem
							.getDisplay(), handledItem, selectionService);

					((MenuItem) newMenuItem).setEnabled(Boolean.TRUE
							.equals(result));
				}
			});
		}
	}
}
