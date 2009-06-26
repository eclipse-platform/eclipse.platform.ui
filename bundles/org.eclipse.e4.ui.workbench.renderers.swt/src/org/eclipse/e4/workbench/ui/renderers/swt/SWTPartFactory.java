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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.model.application.ApplicationPackage;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MHandledItem;
import org.eclipse.e4.ui.model.application.MItem;
import org.eclipse.e4.ui.model.application.MItemPart;
import org.eclipse.e4.ui.model.application.MMenu;
import org.eclipse.e4.ui.model.application.MMenuItem;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MToolBar;
import org.eclipse.e4.ui.model.application.MToolBarItem;
import org.eclipse.e4.ui.model.workbench.MMenuItemRenderer;
import org.eclipse.e4.ui.services.EHandlerService;
import org.eclipse.e4.ui.workbench.swt.util.ISWTResourceUtiltities;
import org.eclipse.e4.workbench.ui.IResourceUtiltities;
import org.eclipse.e4.workbench.ui.renderers.PartFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;

public abstract class SWTPartFactory extends PartFactory {

	private static Shell limbo;

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
		Composite composite = null;
		while (!(widgetObject instanceof Composite)
				&& widgetObject instanceof Control) {
			widgetObject = ((Control) widgetObject).getParent();
		}
		if (!(widgetObject instanceof Composite)) {
			return null;
		}
		composite = (Composite) widgetObject;
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
		IEclipseContext context = getFocusContext(display);
		EHandlerService hs = (EHandlerService) context
				.get(EHandlerService.class.getName());
		return hs.canExecute(item.getCommand().getId());
	}

	protected Object executeItem(Display display, final MHandledItem item) {
		IEclipseContext context = getFocusContext(display);
		EHandlerService hs = (EHandlerService) context
				.get(EHandlerService.class.getName());
		return hs.executeHandler(item.getCommand().getId());
	}

	private void createMenuItem(MPart<?> part, final Menu parentMenu,
			final MHandledItem handledItem) {

		int style = SWT.PUSH;
		if (handledItem instanceof MMenuItemRenderer) {
			final MenuItem newMenuItem = new MenuItem(parentMenu, style);
			newMenuItem.setData(handledItem);
			newMenuItem.setText(handledItem.getId());
			final IContributionItem renderer = (IContributionItem) ((MMenuItemRenderer) handledItem)
					.getRenderer();
			final ArrayList<IMenuListener> fakeListeners = new ArrayList<IMenuListener>();
			final MenuManager fakeManager = new MenuManager() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see
				 * org.eclipse.jface.action.MenuManager#addMenuListener(org.
				 * eclipse.jface.action.IMenuListener)
				 */
				@Override
				public void addMenuListener(IMenuListener listener) {
					fakeListeners.add(listener);
				}
			};
			fakeManager.add(renderer);
			parentMenu.addListener(SWT.Show, new Listener() {
				public void handleEvent(Event event) {
					final MenuItem[] items = parentMenu.getItems();
					int idx = 0;
					for (; idx < items.length && items[idx] != newMenuItem; idx++)
						;
					idx++;
					for (int i = idx; i < items.length
							&& !(items[i].getData() instanceof MHandledItem); i++) {
						items[i].dispose();
					}
					final IMenuListener[] array = fakeListeners
							.toArray(new IMenuListener[fakeListeners.size()]);
					for (IMenuListener im : array) {
						im.menuAboutToShow(fakeManager);
					}
					renderer.fill(parentMenu, idx);
				}
			});
			return;
		}

		if (handledItem instanceof MMenuItem) {
			final MMenuItem mItem = (MMenuItem) handledItem;
			if (mItem.isSeparator()) {
				if (!mItem.isVisible()) {
					return;
				}
				style = SWT.SEPARATOR;
			} else if (mItem.getMenu() != null) {
				style = SWT.CASCADE;
			}
		}

		final MenuItem newMenuItem = new MenuItem(parentMenu, style);
		newMenuItem.setData(handledItem);
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

		// If we're disposing a control find its 'outermost'
		if (curWidget instanceof Control)
			curWidget = getOutermost((Control) curWidget);

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
		if (parentElement != null && element != null)
			return;
		// if the new part already has a Control then re-parent it under the
		// element
		if (element.getWidget() instanceof Control
				&& parentElement.getWidget() instanceof Composite) {
			Composite parentComp = (Composite) parentElement.getWidget();
			Control ctrl = (Control) element.getWidget();
			locallyShow(parentComp, ctrl);
		} else if (element.isVisible()) {
			// Ensure the widget for the element exists
			renderer.createGui(element);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.ui.renderers.PartFactory#childRemoved(org.eclipse
	 * .e4.ui.model.application.MPart,
	 * org.eclipse.e4.ui.model.application.MPart)
	 */
	@Override
	public void childRemoved(MPart<?> parentElement, MPart<?> child) {
		super.childRemoved(parentElement, child);

		if (child.getWidget() instanceof Control) {
			Control ctrl = (Control) child.getWidget();
			locallyHide(ctrl);
		}
	}

	private Control getOutermost(Control ctrl) {
		// Find the 'outermost' Composite that is *not* bound
		// to a model element
		Composite curComposite = ctrl.getParent();
		Control outerMost = ctrl;
		while (curComposite != null
				&& curComposite.getData(PartFactory.OWNING_ME) == null
				&& !(curComposite instanceof Shell)) {
			outerMost = curComposite;
			curComposite = curComposite.getParent();
		}

		return outerMost;
	}

	/**
	 * Restore an existing control to the UI by reparenting it (from 'limbo')
	 * under its new owner
	 * 
	 * @param parent
	 *            The composite to place the control under
	 * @param ctrl
	 *            The control to be restored
	 */
	private void locallyShow(Composite parent, Control ctrl) {
		// Find the 'outermost' Composite that is *not* bound
		// to a model element
		Control toReparent = getOutermost(ctrl);

		// Prevent No-ops
		if (toReparent.getParent() != parent) {
			toReparent.setParent(parent);
			parent.layout(true);
		}
	}

	/**
	 * If a model element containing a widget is removed from its parent we have
	 * to remove the control immediately from its parent's structure (to get the
	 * layout correct) so we'll place it in 'limbo' (an invisible shell).
	 * 
	 * @param ctrl
	 */
	private void locallyHide(Control ctrl) {
		if (limbo == null) {
			limbo = new Shell(ctrl.getShell(), SWT.NONE);
			limbo.setVisible(false);
		}

		// Find the 'outermost' Composite that is *not* bound
		// to a model element
		Control toReparent = getOutermost(ctrl);

		Composite curParent = toReparent.getParent();
		toReparent.setParent(limbo);
		curParent.layout(true);
	}

	/*
	 * HACK: Create a wrapper composite with appropriate layout for the purpose
	 * of styling margins. See bug #280632
	 */
	protected Composite createWrapperForStyling(Composite parentWidget) {
		Composite layoutHolder = new Composite(parentWidget, SWT.NONE);
		addLayoutForStyling(layoutHolder);
		layoutHolder.setData("org.eclipse.e4.ui.css.swt.marginWrapper", true); //$NON-NLS-1$
		return layoutHolder;
	}

	/*
	 * HACK: Add layout information to the composite for the purpose of styling
	 * margins. See bug #280632
	 */
	protected void addLayoutForStyling(Composite composite) {
		GridLayout gl = new GridLayout(1, true);
		composite.setLayout(gl);
		gl.horizontalSpacing = 0;
		gl.verticalSpacing = 0;
		gl.marginHeight = 0;
		gl.marginWidth = 0;
	}

	/*
	 * HACK: Prep the control with layout information for the purpose of styling
	 * margins. See bug #280632
	 */
	protected void configureForStyling(Control control) {
		control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}
}
