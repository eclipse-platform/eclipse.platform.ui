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
import org.eclipse.e4.ui.model.application.MContributedPart;
import org.eclipse.e4.ui.model.application.MHandledItem;
import org.eclipse.e4.ui.model.application.MItem;
import org.eclipse.e4.ui.model.application.MItemPart;
import org.eclipse.e4.ui.model.application.MMenu;
import org.eclipse.e4.ui.model.application.MMenuItem;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MToolBar;
import org.eclipse.e4.ui.model.application.MToolBarItem;
import org.eclipse.e4.ui.services.EHandlerService;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.swt.util.ISWTResourceUtiltities;
import org.eclipse.e4.workbench.ui.IResourceUtiltities;
import org.eclipse.e4.workbench.ui.renderers.PartFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;

public abstract class SWTPartFactory extends PartFactory {

	public Object createMenu(MPart<?> part, Object widgetObject, MMenu menu) {
		Widget widget = (Widget) widgetObject;
		Menu swtMenu;

		if (widget instanceof MenuItem) {
			swtMenu = new Menu(((MenuItem) widget).getParent().getShell(),
					SWT.DROP_DOWN);
			((MenuItem) widget).setMenu(swtMenu);
		} else if (widget instanceof ToolItem) {
			swtMenu = new Menu(((ToolItem) widget).getParent().getShell(),
					SWT.POP_UP);
		} else if (widget instanceof Decorations) {
			swtMenu = new Menu((Decorations) widget, SWT.BAR);
			((Decorations) widget).setMenuBar(swtMenu);
		} else if (widget instanceof Control) {
			swtMenu = new Menu((Control) widget);
			// ((Control) widget).setMenu(swtMenu);
		} else {
			throw new IllegalArgumentException(
					"The widget must be MenuItem, Decorations, or Control but is: " //$NON-NLS-1$
							+ widgetObject);
		}
		for (MMenuItem menuItem : menu.getItems()) {
			createMenuItem(part, swtMenu, menuItem);
		}

		return swtMenu;
	}

	public Object createToolBar(MPart<?> part, Object widgetObject,
			MToolBar toolbar) {

		int orientation = SWT.HORIZONTAL;
		Composite composite = (Composite) widgetObject;
		if (composite.getLayout() instanceof RowLayout) {
			RowLayout rl = (RowLayout) composite.getLayout();
			orientation = rl.type;
		}
		ToolBar swtToolBar = new ToolBar(composite, SWT.FLAT | orientation);
		swtToolBar.setData(PartFactory.OWNING_ME, toolbar);

		EList<MToolBarItem> items = toolbar.getItems();
		if (items != null && items.size() > 0) {
			for (MToolBarItem toolBarItem : toolbar.getItems()) {
				createToolBarItem(part, swtToolBar, toolBarItem);
			}
		}

		return swtToolBar;
	}

	private IEclipseContext getFocusContext(Display display) {
		// find the first useful part in the model
		Control control = display.getFocusControl();
		Object partObj = null;
		while (control != null && !(partObj instanceof MPart<?>)) {
			partObj = control.getData(OWNING_ME);
			control = control.getParent();
		}
		if (partObj == null) {
			return context;
		}
		// get the applicable context (or parent)
		MPart<?> part = (MPart<?>) partObj;
		return getContext(part);
	}

	private void createToolBarItem(MPart<?> part, ToolBar swtTB,
			final MToolBarItem toolBarItem) {
		int style = SWT.PUSH;
		final ToolItem newToolItem = new ToolItem(swtTB, style);

		if (toolBarItem.getName() != null)
			newToolItem.setText(toolBarItem.getName());
		newToolItem.setToolTipText(toolBarItem.getTooltip());
		newToolItem.setImage(getImage(toolBarItem));
		newToolItem.setData(PartFactory.OWNING_ME, toolBarItem);

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
			newToolItem.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					if (canExecuteItem(newToolItem.getDisplay(), toolBarItem)) {
						executeItem(newToolItem.getDisplay(), toolBarItem);
					}
				}
			});
			newToolItem.getDisplay().timerExec(250, new Runnable() {
				public void run() {
					if (newToolItem.isDisposed()) {
						return;
					}
					newToolItem.setEnabled(canExecuteItem(newToolItem
							.getDisplay(), toolBarItem));
					newToolItem.getDisplay().timerExec(100, this);
				}
			});
		}
	}

	protected boolean canExecuteItem(Display display, final MHandledItem item) {
		IEclipseContext execContext = null;
		Object o = context.get(IServiceConstants.ACTIVE_PART);
		if (o instanceof MContributedPart) {
			execContext = ((MContributedPart) o).getContext();
		} else {
			execContext = getFocusContext(display);
		}
		EHandlerService hs = (EHandlerService) execContext
				.get(EHandlerService.class.getName());
		return hs.canExecute(item.getCommand().getId());
	}

	protected Object executeItem(Display display, final MHandledItem item) {
		IEclipseContext execContext = null;
		Object o = context.get(IServiceConstants.ACTIVE_PART);
		if (o instanceof MContributedPart) {
			execContext = ((MContributedPart) o).getContext();
		} else {
			execContext = getFocusContext(display);
		}
		EHandlerService hs = (EHandlerService) execContext
				.get(EHandlerService.class.getName());
		return hs.executeHandler(item.getCommand().getId());
	}

	private void createMenuItem(MPart<?> part, final Menu parentMenu,
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
		if (handledItem.getCommand() != null) {
			newMenuItem.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					if (canExecuteItem(newMenuItem.getDisplay(), handledItem)) {
						executeItem(newMenuItem.getDisplay(), handledItem);
					}
				}
			});
			parentMenu.addListener(SWT.Show, new Listener() {

				public void handleEvent(Event event) {
					if (newMenuItem.isDisposed()) {
						return;
					}
					newMenuItem.setEnabled(canExecuteItem(newMenuItem
							.getDisplay(), handledItem));
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

	public Object unbindWidget(MPart<?> me) {
		Widget widget = (Widget) me.getWidget();
		if (widget != null) {
			me.setWidget(null);
			widget.setData(OWNING_ME, null);
		}

		// Clear the factory reference
		me.setOwner(null);

		return widget;
	}

	protected Widget getParentWidget(MPart<?> element) {
		return (element.getParent() instanceof MPart) ? (Widget) ((MPart<?>) (element
				.getParent())).getWidget()
				: null;
	}

	public void disposeWidget(MPart<?> part) {
		Widget curWidget = (Widget) part.getWidget();
		if (curWidget != null && !curWidget.isDisposed()) {
			unbindWidget(part);
			curWidget.dispose();
		}
	}

	public void hookControllerLogic(final MPart<?> me) {
		Widget widget = (Widget) me.getWidget();

		// Clean up if the widget is disposed
		widget.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				MPart<?> model = (MPart<?>) e.widget.getData(OWNING_ME);
				if (model != null)
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.ui.renderers.PartFactory#childAdded(org.eclipse
	 * .e4.ui.model.application.MPart,
	 * org.eclipse.e4.ui.model.application.MPart)
	 */
	@Override
	public void childAdded(MPart<?> parentElement, MPart<?> element) {
		// TODO Auto-generated method stub
	}
}
