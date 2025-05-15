/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jonas Helming, Dirk Fauth - Bug 410087
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.renderers.swt;

import jakarta.inject.Inject;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.e4.core.contexts.IContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.internal.workbench.RenderedElementUtil;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.internal.workbench.swt.Policy;
import org.eclipse.e4.ui.internal.workbench.swt.WorkbenchSWTActivator;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
import org.eclipse.e4.ui.services.help.EHelpService;
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

/**
 * Common super class for HandledContributionItem and DirectContributionItem
 */
public abstract class AbstractContributionItem extends ContributionItem {

	protected static final String FORCE_TEXT = "FORCE_TEXT"; //$NON-NLS-1$
	protected static final String ICON_URI = "iconURI"; //$NON-NLS-1$
	protected static final String DISABLED_URI = "disabledURI"; //$NON-NLS-1$
	/**
	 * Internal key for transient maps to provide a runnable on widget disposal
	 */
	public static final String DISPOSABLE = "IDisposable"; //$NON-NLS-1$

	@Inject
	@Optional
	protected Logger logger;

	@Inject
	private EModelService modelService;

	@Inject
	@Optional
	protected EHelpService helpService;

	@Inject
	protected IContributionFactory contributionFactory;

	protected Widget widget;
	protected Listener menuItemListener;
	protected LocalResourceManager localResourceManager;

	MItem modelItem;

	private ISafeRunnable updateRunner;

	private ISWTResourceUtilities resUtils = null;

	protected IMenuListener menuListener = manager -> update(null);

	/**
	 * Flag to ensure that an error during updates are only logged once to
	 * prevent spamming the log. Is set to <code>true</code> after an error on
	 * update was logged the first time.
	 */
	private boolean logged = false;

	public AbstractContributionItem() {
		super();
	}

	public AbstractContributionItem(String id) {
		super(id);
	}

	@Override
	public void update() {
		update(null);
	}

	@Override
	public void update(String id) {
		updateIcons();
		if (widget instanceof MenuItem) {
			updateMenuItem();
		} else if (widget instanceof ToolItem) {
			updateToolItem();
		}
	}

	@Override
	public boolean isVisible() {
		String contributorURI = modelItem.getContributorURI();
		if (contributorURI == null || contributionFactory.isEnabled(contributorURI)) {
			return super.isVisible();
		}
		return false;
	}

	protected abstract void updateMenuItem();

	protected abstract void updateToolItem();

	@Inject
	void setResourceUtils(IResourceUtilities<ImageDescriptor> utils) {
		resUtils = (ISWTResourceUtilities) utils;
	}

	protected Image getImage(String iconURI, LocalResourceManager resourceManager) {
		return createImage(iconURI, resourceManager, false);
	}

	private Image createImage(String iconURI, LocalResourceManager resourceManager, boolean disabled) {
		Image image = null;

		if (iconURI != null && iconURI.length() > 0) {
			ImageDescriptor iconDescriptor = resUtils.imageDescriptorFromURI(URI.createURI(iconURI));
			if (disabled) {
				iconDescriptor = ImageDescriptor.createWithFlags(iconDescriptor, SWT.IMAGE_DISABLE);
			}
			if (iconDescriptor != null) {
				try {
					image = resourceManager.create(iconDescriptor);
				} catch (DeviceResourceException e) {
					iconDescriptor = ImageDescriptor.getMissingImageDescriptor();
					image = resourceManager.create(iconDescriptor);
					// as we replaced the failed icon, log the message once.
					if (Policy.DEBUG_MENUS) {
						WorkbenchSWTActivator.trace(Policy.DEBUG_MENUS_FLAG, "failed to create image " + iconURI, e); //$NON-NLS-1$
					}
				}
			}
		}
		return image;
	}

	private Image getDisabledImage(String iconURI, LocalResourceManager resourceManager) {
		return createImage(iconURI, resourceManager, true);
	}

	protected void updateIcons() {
		if (!(widget instanceof Item)) {
			return;
		}
		Item item = (Item) widget;
		String iconURI = modelItem.getIconURI() != null ? modelItem.getIconURI() : ""; //$NON-NLS-1$
		String disabledURI = getDisabledIconURI(modelItem);
		Object disabledData = item.getData(DISABLED_URI);
		boolean enabledIconChanged = !iconURI.equals(item.getData(ICON_URI));
		boolean disabledIconChanged = disabledData != null && !disabledURI.equals(disabledData);
		if (enabledIconChanged || disabledIconChanged) {
			LocalResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());
			Image iconImage = getImage(iconURI, resourceManager);
			item.setImage(iconImage);
			item.setData(ICON_URI, iconURI);
			disabledData = null;
			item.setData(DISABLED_URI, null);
			disposeOldImages();
			localResourceManager = resourceManager;
		}
		// If item is disabled, create disabled image if not done yet
		if (!modelItem.isEnabled() && disabledData == null && item instanceof ToolItem toolItem) {
			Image disabledImage = getImage(disabledURI, localResourceManager);
			if (disabledImage == null) {
				disabledImage = getDisabledImage(iconURI, localResourceManager);
			}
			toolItem.setDisabledImage(disabledImage);
			toolItem.setData(DISABLED_URI, disabledURI);
		}
	}

	private String getDisabledIconURI(MItem toolItem) {
		Object obj = toolItem.getTransientData().get(IPresentationEngine.DISABLED_ICON_IMAGE_KEY);
		return obj instanceof String ? (String) obj : ""; //$NON-NLS-1$
	}

	protected void disposeOldImages() {
		if (localResourceManager != null) {
			localResourceManager.dispose();
			localResourceManager = null;
		}
	}

	/**
	 * @param item
	 *            the model item
	 */
	public void setModel(MItem item) {
		modelItem = item;
		setId(modelItem.getElementId());
		updateVisible();
	}

	/**
	 * @return the model
	 */
	public MItem getModel() {
		return modelItem;
	}

	@Override
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

	@Override
	public void fill(Menu menu, int index) {
		if (modelItem == null) {
			return;
		}
		if (widget != null) {
			return;
		}
		int style = SWT.PUSH;
		if (modelItem.getType() == ItemType.PUSH)
			style = SWT.PUSH;
		else if (modelItem.getType() == ItemType.CHECK)
			style = SWT.CHECK;
		else if (modelItem.getType() == ItemType.RADIO)
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
		item.addListener(SWT.Help, getItemListener());

		widget = item;
		modelItem.setWidget(widget);
		widget.setData(AbstractPartRenderer.OWNING_ME, modelItem);

		update(null);

		postMenuFill();
	}

	/**
	 * This method is intended to perform actions additionally to the common
	 * actions in {@link AbstractContributionItem#fill(Menu, int)}
	 */
	protected void postMenuFill() {
	}

	@Override
	public void fill(ToolBar parent, int index) {
		if (modelItem == null) {
			return;
		}
		if (widget != null) {
			return;
		}
		boolean isDropdown = false;
		if (modelItem instanceof MToolItem) {
			MMenu menu = ((MToolItem) modelItem).getMenu();
			isDropdown = menu != null;
		}
		int style = SWT.PUSH;
		if (isDropdown)
			style = SWT.DROP_DOWN;
		else if (modelItem.getType() == ItemType.CHECK)
			style = SWT.CHECK;
		else if (modelItem.getType() == ItemType.RADIO)
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
		modelItem.setWidget(widget);
		widget.setData(AbstractPartRenderer.OWNING_ME, modelItem);

		ToolItemUpdater updater = getUpdater();
		if (updater != null) {
			updater.registerItem(this);
		}

		update(null);

		postToolbarFill();
	}

	/**
	 * This method is intended to perform actions additionally to the common
	 * actions in {@link AbstractContributionItem#fill(ToolBar, int)}
	 */
	protected void postToolbarFill() {
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

	/**
	 * @return the widgets of the contribution
	 */
	public Widget getWidget() {
		return widget;
	}

	protected Menu getMenu(final MMenu mmenu, ToolItem toolItem) {
		Object obj = mmenu.getWidget();
		if (obj instanceof Menu && !((Menu) obj).isDisposed()) {
			return (Menu) obj;
		}
		// this is a temporary passthrough of the IMenuCreator
		if (RenderedElementUtil.isRenderedMenu(mmenu)) {
			obj = RenderedElementUtil.getContributionManager(mmenu);
			if (obj instanceof IContextFunction) {
				final IEclipseContext lclContext = getContext(mmenu);
				obj = ((IContextFunction) obj).compute(lclContext, null);
				RenderedElementUtil.setContributionManager(mmenu, obj);
			}
			if (obj instanceof IMenuCreator) {
				final IMenuCreator creator = (IMenuCreator) obj;
				final Menu menu = creator.getMenu(toolItem.getParent().getShell());
				if (menu != null) {
					toolItem.addDisposeListener(e -> {
						if (menu != null && !menu.isDisposed()) {
							creator.dispose();
							mmenu.setWidget(null);
						}
					});
					menu.setData(AbstractPartRenderer.OWNING_ME, menu);
					return menu;
				}
			}
		} else {
			final IEclipseContext lclContext = getContext(getModel());
			IPresentationEngine engine = lclContext.get(IPresentationEngine.class);
			obj = engine.createGui(mmenu, toolItem.getParent(), lclContext);
			if (obj instanceof Menu) {
				return (Menu) obj;
			}
			if (logger != null) {
				logger.debug("Rendering returned " + obj); //$NON-NLS-1$
			}
		}
		return null;
	}

	/**
	 * @return whether the event was a drop down on a toolitem
	 */
	protected boolean dropdownEvent(Event event) {
		if (event.detail == SWT.ARROW && modelItem instanceof MToolItem) {
			ToolItem ti = (ToolItem) event.widget;
			MMenu mmenu = ((MToolItem) modelItem).getMenu();
			if (mmenu == null) {
				return false;
			}
			Menu menu = getMenu(mmenu, ti);
			if (menu == null || menu.isDisposed()) {
				return true;
			}
			Rectangle itemBounds = ti.getBounds();
			Point displayAt = ti.getParent().toDisplay(itemBounds.x, itemBounds.y + itemBounds.height);
			menu.setLocation(displayAt);
			menu.setVisible(true);

			Display display = menu.getDisplay();
			while (!menu.isDisposed() && menu.isVisible()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
			return true;
		}
		return false;
	}

	protected void handleWidgetSelection(Event event) {
		if (widget != null && !widget.isDisposed()) {
			if (dropdownEvent(event)) {
				return;
			}
			if (modelItem.getType() == ItemType.CHECK || modelItem.getType() == ItemType.RADIO) {
				boolean selection = false;
				if (widget instanceof MenuItem) {
					selection = ((MenuItem) widget).getSelection();
				} else if (widget instanceof ToolItem) {
					selection = ((ToolItem) widget).getSelection();
				}
				modelItem.setSelected(selection);
			}
			if (canExecuteItem(event)) {
				executeItem(event);
			}
		}
	}

	protected abstract void executeItem(Event event);

	/**
	 * @return if the item can be executed
	 */
	protected abstract boolean canExecuteItem(Event event);

	protected Listener getItemListener() {
		if (menuItemListener == null) {
			menuItemListener = event -> {
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
				case SWT.Help:
					handleHelpRequest();
					break;
				}
			};
		}
		return menuItemListener;
	}

	protected void handleHelpRequest() {
		if (helpService == null)
			return;
		String helpContextId = getModel().getPersistedState().get(EHelpService.HELP_CONTEXT_ID);
		if (helpContextId != null)
			helpService.displayHelp(helpContextId);
	}

	protected abstract void handleWidgetDispose(Event event);

	protected void updateVisible() {
		setVisible((modelItem).isVisible());
		final IContributionManager parent = getParent();
		if (parent != null) {
			parent.markDirty();
		}
	}

	private ISafeRunnable getUpdateRunner() {
		if (updateRunner == null) {
			updateRunner = new ISafeRunnable() {
				@Override
				public void run() throws Exception {
					boolean shouldEnable = canExecuteItem(null);
					if (shouldEnable != modelItem.isEnabled()) {
						modelItem.setEnabled(shouldEnable);
						update();
					}
				}

				@Override
				public void handleException(Throwable exception) {
					if (!logged) {
						logged = true;
						if (logger != null) {
							logger.error(exception,
									"Internal error during tool item enablement updating, this is only logged once per tool item."); //$NON-NLS-1$
						}
					}
				}
			};
		}
		return updateRunner;
	}

	protected ToolItemUpdater getUpdater() {
		if (modelItem != null) {
			Object obj = modelItem.getRenderer();
			if (obj instanceof ToolBarManagerRenderer) {
				return ((ToolBarManagerRenderer) obj).getUpdater();
			}
		}
		return null;
	}


	protected void updateItemEnablement() {
		if (!(modelItem.getWidget() instanceof ToolItem))
			return;

		ToolItem widget = (ToolItem) modelItem.getWidget();
		if (widget == null || widget.isDisposed())
			return;

		SafeRunner.run(getUpdateRunner());
	}

}
