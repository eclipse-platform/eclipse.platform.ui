/*******************************************************************************
 * Copyright (c) 2010, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Joseph Carroll <jdsalingerjr@gmail.com> - Bug 385414 Contributing wizards 
 *     to toolbar always displays icon and text
 ******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import javax.inject.Inject;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.ui.internal.workbench.Activator;
import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.internal.workbench.Policy;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MRenderedMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.IResourceUtilities;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.swt.util.ISWTResourceUtilities;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.resource.DeviceResourceException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;

public class DirectContributionItem extends ContributionItem {
	/** Internal key for transient maps to provide a runnable on widget disposal */
	public static final String DISPOSABLE = "IDisposable"; //$NON-NLS-1$

	private static final String FORCE_TEXT = "FORCE_TEXT"; //$NON-NLS-1$
	private static final String ICON_URI = "iconURI"; //$NON-NLS-1$
	private static final String DISABLED_URI = "disabledURI"; //$NON-NLS-1$
	private static final String DCI_STATIC_CONTEXT = "DCI-staticContext"; //$NON-NLS-1$

	private MItem model;
	private Widget widget;
	private Listener menuItemListener;
	private LocalResourceManager localResourceManager;
	private IEclipseContext infoContext;

	@Inject
	private IContributionFactory contribFactory;

	@Inject
	private EModelService modelService;

	private ISWTResourceUtilities resUtils = null;

	@Inject
	void setResourceUtils(IResourceUtilities utils) {
		resUtils = (ISWTResourceUtilities) utils;
	}

	private IMenuListener menuListener = new IMenuListener() {
		public void menuAboutToShow(IMenuManager manager) {
			update(null);
		}
	};

	public void setModel(MItem item) {
		model = item;
		setId(model.getElementId());
		updateVisible();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.action.ContributionItem#fill(org.eclipse.swt.widgets
	 * .Menu, int)
	 */
	@Override
	public void fill(Menu menu, int index) {
		if (model == null) {
			return;
		}
		if (widget != null) {
			return;
		}
		int style = SWT.PUSH;
		if (model.getType() == ItemType.PUSH)
			style = SWT.PUSH;
		else if (model.getType() == ItemType.CHECK)
			style = SWT.CHECK;
		else if (model.getType() == ItemType.RADIO)
			style = SWT.RADIO;
		MenuItem item = null;
		if (index >= 0) {
			item = new MenuItem(menu, style, index);
		} else {
			item = new MenuItem(menu, style);
		}
		item.setData(this);

		item.addListener(SWT.Dispose, getItemListener());
		item.addListener(SWT.Selection, getItemListener());
		item.addListener(SWT.DefaultSelection, getItemListener());

		widget = item;
		model.setWidget(widget);
		widget.setData(AbstractPartRenderer.OWNING_ME, model);

		update(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.action.ContributionItem#fill(org.eclipse.swt.widgets
	 * .ToolBar, int)
	 */
	@Override
	public void fill(ToolBar parent, int index) {
		if (model == null) {
			return;
		}
		if (widget != null) {
			return;
		}
		boolean isDropdown = false;
		if (model instanceof MToolItem) {
			MMenu menu = ((MToolItem) model).getMenu();
			isDropdown = menu != null;
		}
		int style = SWT.PUSH;
		if (isDropdown)
			style = SWT.DROP_DOWN;
		else if (model.getType() == ItemType.CHECK)
			style = SWT.CHECK;
		else if (model.getType() == ItemType.RADIO)
			style = SWT.RADIO;
		ToolItem item = null;
		if (index >= 0) {
			item = new ToolItem(parent, style, index);
		} else {
			item = new ToolItem(parent, style);
		}
		item.setData(this);

		item.addListener(SWT.Dispose, getItemListener());
		item.addListener(SWT.Selection, getItemListener());
		item.addListener(SWT.DefaultSelection, getItemListener());

		widget = item;
		model.setWidget(widget);
		widget.setData(AbstractPartRenderer.OWNING_ME, model);

		update(null);
	}

	private void updateVisible() {
		setVisible((model).isVisible());
		final IContributionManager parent = getParent();
		if (parent != null) {
			parent.markDirty();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ContributionItem#update()
	 */
	@Override
	public void update() {
		update(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ContributionItem#update(java.lang.String)
	 */
	@Override
	public void update(String id) {
		updateIcons();
		if (widget instanceof MenuItem) {
			updateMenuItem();
		} else if (widget instanceof ToolItem) {
			updateToolItem();
		}
	}

	private void updateMenuItem() {
		MenuItem item = (MenuItem) widget;
		String text = model.getLocalizedLabel();
		if (text != null) {
			item.setText(text);
		} else {
			item.setText(""); //$NON-NLS-1$
		}
		item.setSelection(model.isSelected());
		item.setEnabled(model.isEnabled());
	}

	private void updateToolItem() {
		ToolItem item = (ToolItem) widget;
		final String text = model.getLocalizedLabel();
		Image icon = item.getImage();
		boolean mode = model.getTags().contains(FORCE_TEXT);
		if ((icon == null || mode) && text != null) {
			item.setText(text);
		} else {
			item.setText(""); //$NON-NLS-1$
		}
		final String tooltip = model.getLocalizedTooltip();
		item.setToolTipText(tooltip);
		item.setSelection(model.isSelected());
		item.setEnabled(model.isEnabled());
	}

	private void updateIcons() {
		if (!(widget instanceof Item)) {
			return;
		}
		Item item = (Item) widget;
		String iconURI = model.getIconURI() != null ? model.getIconURI() : ""; //$NON-NLS-1$
		String disabledURI = getDisabledIconURI(model);
		if (!iconURI.equals(item.getData(ICON_URI))
				|| !disabledURI.equals(item.getData(DISABLED_URI))) {
			LocalResourceManager resourceManager = new LocalResourceManager(
					JFaceResources.getResources());
			Image iconImage = getImage(iconURI, resourceManager);
			item.setImage(iconImage);
			item.setData(ICON_URI, iconURI);
			if (item instanceof ToolItem) {
				iconImage = getImage(disabledURI, resourceManager);
				((ToolItem) item).setDisabledImage(iconImage);
				item.setData(DISABLED_URI, disabledURI);
			}
			disposeOldImages();
			localResourceManager = resourceManager;
		}
	}

	private Image getImage(String iconURI, LocalResourceManager resourceManager) {
		Image image = null;

		if (iconURI != null && iconURI.length() > 0) {
			ImageDescriptor iconDescriptor = resUtils
					.imageDescriptorFromURI(URI.createURI(iconURI));
			if (iconDescriptor != null) {
				try {
					image = resourceManager.createImage(iconDescriptor);
				} catch (DeviceResourceException e) {
					iconDescriptor = ImageDescriptor
							.getMissingImageDescriptor();
					image = resourceManager.createImage(iconDescriptor);
					// as we replaced the failed icon, log the message once.
					Activator.trace(Policy.DEBUG_MENUS,
							"failed to create image " + iconURI, e); //$NON-NLS-1$
				}
			}
		}
		return image;
	}

	private String getDisabledIconURI(MItem toolItem) {
		Object obj = toolItem.getTransientData().get(
				IPresentationEngine.DISABLED_ICON_IMAGE_KEY);
		return obj instanceof String ? (String) obj : ""; //$NON-NLS-1$
	}

	private void disposeOldImages() {
		if (localResourceManager != null) {
			localResourceManager.dispose();
			localResourceManager = null;
		}
	}

	private Listener getItemListener() {
		if (menuItemListener == null) {
			menuItemListener = new Listener() {
				public void handleEvent(Event event) {
					switch (event.type) {
					case SWT.Dispose:
						handleWidgetDispose(event);
						break;
					case SWT.DefaultSelection:
					case SWT.Selection:
						if (event.widget != null) {
							handleWidgetSelection(event);
						}
						break;
					}
				}
			};
		}
		return menuItemListener;
	}

	private void handleWidgetDispose(Event event) {
		if (event.widget == widget) {
			if (infoContext != null) {
				infoContext.dispose();
				infoContext = null;
			}
			widget.removeListener(SWT.Selection, getItemListener());
			widget.removeListener(SWT.Dispose, getItemListener());
			widget.removeListener(SWT.DefaultSelection, getItemListener());
			widget = null;
			Object obj = model.getTransientData().get(DISPOSABLE);
			if (obj instanceof Runnable) {
				((Runnable) obj).run();
			}
			model.setWidget(null);
			disposeOldImages();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ContributionItem#dispose()
	 */
	@Override
	public void dispose() {
		if (widget != null) {
			widget.dispose();
			widget = null;
			model.setWidget(null);
		}
	}

	private void handleWidgetSelection(Event event) {
		if (widget != null && !widget.isDisposed()) {
			if (dropdownEvent(event)) {
				return;
			}
			if (model.getType() == ItemType.CHECK
					|| model.getType() == ItemType.RADIO) {
				boolean selection = false;
				if (widget instanceof MenuItem) {
					selection = ((MenuItem) widget).getSelection();
				} else if (widget instanceof ToolItem) {
					selection = ((ToolItem) widget).getSelection();
				}
				model.setSelected(selection);
			}
			if (canExecuteItem(event)) {
				executeItem(event);
			}
		}
	}

	private boolean dropdownEvent(Event event) {
		if (event.detail == SWT.ARROW && model instanceof MToolItem) {
			ToolItem ti = (ToolItem) event.widget;
			MMenu mmenu = ((MToolItem) model).getMenu();
			if (mmenu == null) {
				return false;
			}
			Menu menu = getMenu(mmenu, ti);
			if (menu == null) {
				return true;
			}
			Rectangle itemBounds = ti.getBounds();
			Point displayAt = ti.getParent().toDisplay(itemBounds.x,
					itemBounds.y + itemBounds.height);
			menu.setLocation(displayAt);
			menu.setVisible(true);

			Display display = menu.getDisplay();
			while (menu.isVisible()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
			return true;
		}
		return false;
	}

	protected Menu getMenu(final MMenu mmenu, ToolItem toolItem) {
		Object obj = mmenu.getWidget();
		if (obj instanceof Menu && !((Menu) obj).isDisposed()) {
			return (Menu) obj;
		}
		// this is a temporary passthrough of the IMenuCreator
		if (mmenu instanceof MRenderedMenu) {
			obj = ((MRenderedMenu) mmenu).getContributionManager();
			if (obj instanceof IContextFunction) {
				final IEclipseContext lclContext = getContext(mmenu);
				obj = ((IContextFunction) obj).compute(lclContext);
				((MRenderedMenu) mmenu).setContributionManager(obj);
			}
			if (obj instanceof IMenuCreator) {
				final IMenuCreator creator = (IMenuCreator) obj;
				final Menu menu = creator.getMenu(toolItem.getParent()
						.getShell());
				if (menu != null) {
					toolItem.addDisposeListener(new DisposeListener() {
						public void widgetDisposed(DisposeEvent e) {
							if (menu != null && !menu.isDisposed()) {
								creator.dispose();
								((MRenderedMenu) mmenu).setWidget(null);
							}
						}
					});
					// mmenu.setWidget(menu);
					menu.setData(AbstractPartRenderer.OWNING_ME, menu);
					return menu;
				}
			}
		}
		return null;
	}

	private IEclipseContext getStaticContext(Event event) {
		if (infoContext == null) {
			infoContext = EclipseContextFactory.create(DCI_STATIC_CONTEXT);
			ContributionsAnalyzer.populateModelInterfaces(model, infoContext,
					model.getClass().getInterfaces());
		}
		if (event == null) {
			infoContext.remove(Event.class);
		} else {
			infoContext.set(Event.class, event);
		}
		return infoContext;
	}

	private void executeItem(Event trigger) {
		final IEclipseContext lclContext = getContext(model);
		if (!checkContribution(lclContext)) {
			return;
		}
		MContribution contrib = (MContribution) model;
		IEclipseContext staticContext = getStaticContext(trigger);
		ContextInjectionFactory.invoke(contrib.getObject(), Execute.class,
				getExecutionContext(lclContext), staticContext, null);
	}

	private boolean canExecuteItem(Event trigger) {
		final IEclipseContext lclContext = getContext(model);
		if (!checkContribution(lclContext)) {
			return false;
		}
		MContribution contrib = (MContribution) model;
		IEclipseContext staticContext = getStaticContext(trigger);
		Boolean result = ((Boolean) ContextInjectionFactory.invoke(
				contrib.getObject(), CanExecute.class,
				getExecutionContext(lclContext), staticContext, Boolean.TRUE));
		return result.booleanValue();
	}

	/**
	 * Return the execution context for the @CanExecute and @Execute methods.
	 * This should be the same as the execution context used by the
	 * EHandlerService.
	 * 
	 * @param context
	 *            the context for this item
	 * @return the execution context
	 */
	private IEclipseContext getExecutionContext(IEclipseContext context) {
		return context.getActiveLeaf();
	}

	private boolean checkContribution(IEclipseContext lclContext) {
		if (!(model instanceof MContribution)) {
			return false;
		}
		MContribution contrib = (MContribution) model;
		if (contrib.getObject() == null) {
			contrib.setObject(contribFactory.create(
					contrib.getContributionURI(), lclContext));
		}
		return contrib.getObject() != null;
	}

	public void setParent(IContributionManager parent) {
		if (getParent() instanceof IMenuManager) {
			IMenuManager menuMgr = (IMenuManager) getParent();
			menuMgr.removeMenuListener(menuListener);
		}
		if (parent instanceof IMenuManager) {
			IMenuManager menuMgr = (IMenuManager) parent;
			menuMgr.addMenuListener(menuListener);
		}
		super.setParent(parent);
	}

	/**
	 * Return a parent context for this part.
	 * 
	 * @param element
	 *            the part to start searching from
	 * @return the parent's closest context, or global context if none in the
	 *         hierarchy
	 */
	protected IEclipseContext getContextForParent(MUIElement element) {
		return modelService.getContainingContext(element);
	}

	/**
	 * Return a context for this part.
	 * 
	 * @param part
	 *            the part to start searching from
	 * @return the closest context, or global context if none in the hierarchy
	 */
	protected IEclipseContext getContext(MUIElement part) {
		if (part instanceof MContext) {
			return ((MContext) part).getContext();
		}
		return getContextForParent(part);
	}

	public Widget getWidget() {
		return widget;
	}
}