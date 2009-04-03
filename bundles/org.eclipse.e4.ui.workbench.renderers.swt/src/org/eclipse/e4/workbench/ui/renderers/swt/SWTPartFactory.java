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

import java.util.Iterator;
import java.util.List;

import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.model.application.ApplicationPackage;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MCommand;
import org.eclipse.e4.ui.model.application.MHandledItem;
import org.eclipse.e4.ui.model.application.MHandler;
import org.eclipse.e4.ui.model.application.MItem;
import org.eclipse.e4.ui.model.application.MItemPart;
import org.eclipse.e4.ui.model.application.MMenu;
import org.eclipse.e4.ui.model.application.MMenuItem;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MToolBar;
import org.eclipse.e4.ui.model.application.MToolBarItem;
import org.eclipse.e4.ui.workbench.swt.util.ISWTResourceUtiltities;
import org.eclipse.e4.workbench.ui.IHandlerService;
import org.eclipse.e4.workbench.ui.ILegacyHook;
import org.eclipse.e4.workbench.ui.IResourceUtiltities;
import org.eclipse.e4.workbench.ui.renderers.PartFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
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

	public void createMenu(MPart<?> part, Object widgetObject, MMenu menu) {
		Widget widget = (Widget) widgetObject;
		org.eclipse.swt.widgets.Menu swtMenu;

		if (menu != null && menu.getId() != null
				&& menu.getId().equals("org.eclipse.ui.main.menu") //$NON-NLS-1$
				&& menu.getItems().size() == 0) {
			// Pre-populate the main menu
			ILegacyHook lh = (ILegacyHook) context.get(ILegacyHook.class
					.getName());
			if (part.getContext() != null) {
				lh.loadMenu(part.getContext(), menu);
			} else {
				lh.loadMenu(context, menu);
			}
		}

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
			MToolBar toolbar) {

		if (toolbar != null && toolbar.getId() != null
				&& toolbar.getId().equals("org.eclipse.ui.main.toolbar") //$NON-NLS-1$
				&& toolbar.getItems().size() == 0) {
			// Pre-populate the main toolbar
			ILegacyHook lh = (ILegacyHook) context.get(ILegacyHook.class
					.getName());
			lh.loadToolbar(toolbar);
		}

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

		if (toolBarItem.getName() != null)
			newToolItem.setText(toolBarItem.getName());
		newToolItem.setToolTipText(toolBarItem.getTooltip());
		newToolItem.setImage(getImage(toolBarItem));
		toolBarItem.eAdapters().add(new AdapterImpl() {
			@Override
			public void notifyChanged(Notification msg) {
				if (ApplicationPackage.Literals.MITEM__NAME.equals(msg
						.getFeature())) {
					final MHandledItem i = (MHandledItem) msg.getNotifier();
					newToolItem.getDisplay().asyncExec(new Runnable() {
						public void run() {
							if (!newToolItem.isDisposed()) {
								newToolItem.setText(i.getName());
								newToolItem.getParent().pack();
							}
						}
					});
				} else if (ApplicationPackage.Literals.MITEM__ICON_URI
						.equals(msg.getFeature())) {
					final MHandledItem i = (MHandledItem) msg.getNotifier();
					newToolItem.getDisplay().asyncExec(new Runnable() {
						public void run() {
							if (!newToolItem.isDisposed()) {
								newToolItem.setImage(getImage(i));
								newToolItem.getParent().pack();
							}
						}
					});
				}
			}
		});
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

	private void createMenuItem(MPart<?> part,
			final org.eclipse.swt.widgets.Menu parentMenu,
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
			handledItem.eAdapters().add(new AdapterImpl() {
				@Override
				public void notifyChanged(Notification msg) {
					if (ApplicationPackage.Literals.MITEM__NAME.equals(msg
							.getFeature())) {
						final MHandledItem i = (MHandledItem) msg.getNotifier();
						newMenuItem.getDisplay().asyncExec(new Runnable() {
							public void run() {
								if (!newMenuItem.isDisposed()) {
									newMenuItem.setText(i.getName());
								}
							}
						});
					} else if (ApplicationPackage.Literals.MITEM__ICON_URI
							.equals(msg.getFeature())) {
						final MHandledItem i = (MHandledItem) msg.getNotifier();
						newMenuItem.getDisplay().asyncExec(new Runnable() {
							public void run() {
								if (!newMenuItem.isDisposed()) {
									newMenuItem.setImage(getImage(i));
								}
							}
						});
					}
				}
			});
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

	public <P extends MPart<?>> void processContents(MPart<P> me) {
		Widget parentWidget = (Widget) me.getWidget();
		if (parentWidget == null)
			return;

		// Process any contents of the newly created ME
		List<P> parts = me.getChildren();
		if (parts != null) {
			for (Iterator<P> childIter = parts.iterator(); childIter.hasNext();) {
				MPart<?> childME = childIter.next();
				renderer.createGui(childME);
			}
		}
	}

	public void bindWidget(MPart<?> me, Object widget) {
		me.setWidget(widget);
		((Widget) widget).setData(OWNING_ME, me);
	}

	protected Widget getParentWidget(MPart<?> element) {
		return (element.getParent() instanceof MPart) ? (Widget) ((MPart<?>) (element
				.getParent())).getWidget()
				: null;
	}

	public void disposeWidget(MPart<?> part) {
		Widget curWidget = (Widget) part.getWidget();
		part.setWidget(null);
		if (curWidget != null)
			curWidget.dispose();
	}

	public void hookControllerLogic(final MPart<?> me) {
		Widget widget = (Widget) me.getWidget();

		// Clean up if the widget is disposed
		widget.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				MPart<?> model = (MPart<?>) e.widget.getData(OWNING_ME);
				model.setWidget(null);
			}
		});

		// add an accessibility listener (not sure if this is in the wrong place
		// (factory?)
		if (widget instanceof Control && me instanceof MItemPart<?>) {
			((Control) widget).getAccessible().addAccessibleListener(
					new AccessibleAdapter() {
						public void getName(AccessibleEvent e) {
							e.result = ((MItemPart<?>) me).getName();
						}
					});
		}
	}

	public void childAdded(MPart<?> parentElement, MPart<?> element) {
		// Ensure the child's widget is under the new parent
		if (parentElement.getWidget() instanceof Composite
				&& element.getWidget() instanceof Control) {
			Composite comp = (Composite) parentElement.getWidget();
			Control ctrl = (Control) element.getWidget();
			ctrl.setParent(comp);
		}
	}

	protected Image getImage(MApplicationElement element) {
		if (element instanceof MItem) {
			IEclipseContext localContext = context;
			if (element instanceof MPart<?>) {
				localContext = getContext((MPart<?>) element);
			}
			String iconURI = ((MItem) element).getIconURI();
			if (iconURI != null && !iconURI.equals("null")) { //$NON-NLS-1$
				ISWTResourceUtiltities resUtils = (ISWTResourceUtiltities) localContext
						.get(IResourceUtiltities.class.getName());
				ImageDescriptor desc = resUtils.imageDescriptorFromURI(URI
						.createURI(iconURI));
				if (desc != null)
					return desc.createImage();
			}
		}
		return null;
	}
}
