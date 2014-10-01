/*******************************************************************************
 * Copyright (c) 2010, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Joseph Carroll <jdsalingerjr@gmail.com> - Bug 385414 Contributing wizards to toolbar always displays icon and text
 *     Snjezana Peco <snjezana.peco@redhat.com> - Memory leaks in Juno when opening and closing XML Editor - http://bugs.eclipse.org/397909
 *     Marco Descher <marco@descher.at> - Bug 397677
 *     Dmitry Spiridenok - Bug 429756
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 445723
 ******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.core.commands.IStateListener;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.State;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.commands.internal.ICommandHelpService;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.internal.workbench.Activator;
import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.internal.workbench.EHelpService;
import org.eclipse.e4.ui.internal.workbench.Policy;
import org.eclipse.e4.ui.internal.workbench.RenderedElementUtil;
import org.eclipse.e4.ui.internal.workbench.renderers.swt.IUpdateService;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledItem;
import org.eclipse.e4.ui.model.application.ui.menu.MItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
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
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.menus.IMenuStateIds;
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

public class HandledContributionItem extends ContributionItem {
	/**
	 * Constant from org.eclipse.ui.handlers.RadioState.PARAMETER_ID
	 */
	private static final String ORG_ECLIPSE_UI_COMMANDS_RADIO_STATE_PARAMETER = "org.eclipse.ui.commands.radioStateParameter"; //$NON-NLS-1$

	/**
	 * Constant from org.eclipse.ui.handlers.RadioState.STATE_ID
	 */
	private static final String ORG_ECLIPSE_UI_COMMANDS_RADIO_STATE = "org.eclipse.ui.commands.radioState"; //$NON-NLS-1$

	/**
	 * Constant from org.eclipse.ui.handlers.RegistryToggleState.STATE_ID
	 */
	private static final String ORG_ECLIPSE_UI_COMMANDS_TOGGLE_STATE = "org.eclipse.ui.commands.toggleState"; //$NON-NLS-1$

	private static final String FORCE_TEXT = "FORCE_TEXT"; //$NON-NLS-1$
	private static final String ICON_URI = "iconURI"; //$NON-NLS-1$
	private static final String DISABLED_URI = "disabledURI"; //$NON-NLS-1$
	private static final String DISPOSABLE_CHECK = "IDisposable"; //$NON-NLS-1$
	private static final String WW_SUPPORT = "org.eclipse.ui.IWorkbenchWindow"; //$NON-NLS-1$
	private static final String HCI_STATIC_CONTEXT = "HCI-staticContext"; //$NON-NLS-1$
	MHandledItem model;
	private Widget widget;
	private Listener menuItemListener;
	private LocalResourceManager localResourceManager;

	@Inject
	@Optional
	private Logger logger;

	// We'll only ever log an error during update once to prevent spamming the
	// log
	private boolean logged = false;

	@Inject
	private ECommandService commandService;

	@Inject
	private EModelService modelService;

	@Inject
	private EBindingService bindingService;

	@Inject
	@Optional
	private IUpdateService updateService;

	@Inject
	@Optional
	private EHelpService helpService;

	@Inject
	@Optional
	@SuppressWarnings("restriction")
	private ICommandHelpService commandHelpService;

	private Runnable unreferenceRunnable;

	private ISWTResourceUtilities resUtils = null;

	private IStateListener stateListener = new IStateListener() {
		@Override
		public void handleStateChange(State state, Object oldValue) {
			updateState();
		}
	};

	@Inject
	void setResourceUtils(IResourceUtilities utils) {
		resUtils = (ISWTResourceUtilities) utils;
	}

	private ISafeRunnable getUpdateRunner() {
		if (updateRunner == null) {
			updateRunner = new ISafeRunnable() {
				@Override
				public void run() throws Exception {
					boolean shouldEnable = canExecuteItem(null);
					if (shouldEnable != model.isEnabled()) {
						model.setEnabled(shouldEnable);
						update();
					}
				}

				@Override
				public void handleException(Throwable exception) {
					if (!logged) {
						logged = true;
						if (logger != null) {
							logger.error(
									exception,
									"Internal error during tool item enablement updating, this is only logged once per tool item."); //$NON-NLS-1$
						}
					}
				}
			};
		}
		return updateRunner;
	}

	protected void updateItemEnablement() {
		if (!(model.getWidget() instanceof ToolItem))
			return;

		ToolItem widget = (ToolItem) model.getWidget();
		if (widget == null || widget.isDisposed())
			return;

		SafeRunner.run(getUpdateRunner());
	}

	private IMenuListener menuListener = new IMenuListener() {
		@Override
		public void menuAboutToShow(IMenuManager manager) {
			update(null);
		}
	};

	private ISafeRunnable updateRunner;

	private IEclipseContext infoContext;

	private State styleState;

	private State toggleState;

	private State radioState;

	public void setModel(MHandledItem item) {
		model = item;
		setId(model.getElementId());
		generateCommand();
		if (model.getCommand() == null) {
			if (logger != null) {
				logger.error("Element " + model.getElementId() + " invalid, no command defined."); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		updateVisible();
	}

	/**
	 * 
	 */
	private void generateCommand() {
		if (model.getCommand() != null && model.getWbCommand() == null) {
			String cmdId = model.getCommand().getElementId();
			List<MParameter> modelParms = model.getParameters();
			Map<String, Object> parameters = new HashMap<String, Object>(4);
			for (MParameter mParm : modelParms) {
				parameters.put(mParm.getName(), mParm.getValue());
			}
			ParameterizedCommand parmCmd = commandService.createCommand(cmdId,
					parameters);
			Activator.trace(Policy.DEBUG_MENUS, "command: " + parmCmd, null); //$NON-NLS-1$
			if (parmCmd == null) {
				Activator.log(IStatus.ERROR,
						"Unable to generate parameterized command for " + model //$NON-NLS-1$
								+ " with " + parameters); //$NON-NLS-1$
				return;
			}

			model.setWbCommand(parmCmd);

			styleState = parmCmd.getCommand().getState(IMenuStateIds.STYLE);
			toggleState = parmCmd.getCommand().getState(
					ORG_ECLIPSE_UI_COMMANDS_TOGGLE_STATE);
			radioState = parmCmd.getCommand().getState(
					ORG_ECLIPSE_UI_COMMANDS_RADIO_STATE);
			updateState();

			if (styleState != null) {
				styleState.addListener(stateListener);
			} else if (toggleState != null) {
				toggleState.addListener(stateListener);
			} else if (radioState != null) {
				radioState.addListener(stateListener);
			}
		}
	}

	private void updateState() {
		if (styleState != null) {
			model.setSelected(((Boolean) styleState.getValue()).booleanValue());
		} else if (toggleState != null) {
			model.setSelected(((Boolean) toggleState.getValue()).booleanValue());
		} else if (radioState != null && model.getWbCommand() != null) {
			ParameterizedCommand c = model.getWbCommand();
			Object parameter = c.getParameterMap().get(
					ORG_ECLIPSE_UI_COMMANDS_RADIO_STATE_PARAMETER);
			String value = (String) radioState.getValue();
			model.setSelected(value != null && value.equals(parameter));
		}
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
		item.addListener(SWT.Help, getItemListener());

		widget = item;
		model.setWidget(widget);
		widget.setData(AbstractPartRenderer.OWNING_ME, model);

		update(null);

		if (updateService != null) {
			unreferenceRunnable = updateService.registerElementForUpdate(
					model.getWbCommand(), model);
		}
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
		ToolItemUpdater updater = getUpdater();
		if (updater != null) {
			updater.registerItem(this);
		}

		update(null);
		hookCheckListener();

		if (updateService != null) {
			unreferenceRunnable = updateService.registerElementForUpdate(
					model.getWbCommand(), model);
		}
	}

	private void hookCheckListener() {
		if (model.getType() != ItemType.CHECK) {
			return;
		}
		Object obj = model.getTransientData().get(ItemType.CHECK.toString());
		if (obj instanceof IContextFunction) {
			IEclipseContext context = getContext(model);
			IEclipseContext staticContext = getStaticContext(null);
			staticContext.set(MPart.class, context.get(MPart.class));
			staticContext.set(WW_SUPPORT, context.get(WW_SUPPORT));

			IContextFunction func = (IContextFunction) obj;
			obj = func.compute(staticContext, null);
			if (obj != null) {
				model.getTransientData().put(DISPOSABLE_CHECK, obj);
			}
		}
	}

	private void unhookCheckListener() {
		if (model.getType() != ItemType.CHECK) {
			return;
		}
		final Object obj = model.getTransientData().remove(DISPOSABLE_CHECK);
		if (obj == null) {
			return;
		}
		((Runnable) obj).run();
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
		ParameterizedCommand parmCmd = model.getWbCommand();
		String keyBindingText = null;
		if (parmCmd != null) {
			if (text == null) {
				try {
					text = parmCmd.getName();
				} catch (NotDefinedException e) {
					// we'll just ignore a failure
				}
			}
			if (bindingService != null) {
				TriggerSequence binding = bindingService
						.getBestSequenceFor(parmCmd);
				if (binding != null)
					keyBindingText = binding.format();
			}
		}
		if (text != null) {
			if (model instanceof MMenuElement) {
				String mnemonics = ((MMenuElement) model).getMnemonics();
				if (mnemonics != null) {
					int idx = text.indexOf(mnemonics);
					if (idx != -1) {
						text = text.substring(0, idx) + '&'
								+ text.substring(idx);
					}
				}
			}
			if (keyBindingText == null)
				item.setText(text);
			else
				item.setText(text + '\t' + keyBindingText);
		} else {
			item.setText(""); //$NON-NLS-1$
		}
		item.setSelection(model.isSelected());
		item.setEnabled(model.isEnabled());
	}

	private void updateToolItem() {
		ToolItem item = (ToolItem) widget;

		if (item.getImage() == null || model.getTags().contains(FORCE_TEXT)) {
			final String text = model.getLocalizedLabel();
			if (text == null || text.length() == 0) {
				final MCommand command = model.getCommand();
				if (command == null) {
					// Set some text so that the item stays visible in the menu
					item.setText("UnLabled"); //$NON-NLS-1$
				} else {
					item.setText(command.getCommandName());
				}
			} else {
				item.setText(text);
			}
		} else {
			item.setText(""); //$NON-NLS-1$
		}

		final String tooltip = getToolTipText();
		item.setToolTipText(tooltip);
		item.setSelection(model.isSelected());
		item.setEnabled(model.isEnabled());
	}

	private String getToolTipText() {
		String text = model.getLocalizedTooltip();
		ParameterizedCommand parmCmd = model.getWbCommand();
		if (parmCmd == null) {
			generateCommand();
			parmCmd = model.getWbCommand();
		}

		if (parmCmd != null && text == null) {
			try {
				text = parmCmd.getName();
			} catch (NotDefinedException e) {
				return null;
			}
		}

		TriggerSequence sequence = bindingService.getBestSequenceFor(parmCmd);
		if (sequence != null) {
			text = text + " (" + sequence.format() + ')'; //$NON-NLS-1$
		}
		return text;
	}

	private void updateIcons() {
		if (!(widget instanceof Item)) {
			return;
		}
		Item item = (Item) widget;
		String iconURI = model.getIconURI() != null ? model.getIconURI() : ""; //$NON-NLS-1$
		String disabledURI = getDisabledIconURI(model);
		Object disabledData = item.getData(DISABLED_URI);
		if (disabledData == null)
			disabledData = ""; //$NON-NLS-1$
		if (!iconURI.equals(item.getData(ICON_URI))
				|| !disabledURI.equals(disabledData)) {
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

	private String getDisabledIconURI(MItem toolItem) {
		Object obj = toolItem.getTransientData().get(
				IPresentationEngine.DISABLED_ICON_IMAGE_KEY);
		return obj instanceof String ? (String) obj : ""; //$NON-NLS-1$
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

	private void disposeOldImages() {
		if (localResourceManager != null) {
			localResourceManager.dispose();
			localResourceManager = null;
		}
	}

	private Listener getItemListener() {
		if (menuItemListener == null) {
			menuItemListener = new Listener() {
				@Override
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
					case SWT.Help:
						handleHelpRequest();
						break;
					}
				}
			};
		}
		return menuItemListener;
	}

	private void handleWidgetDispose(Event event) {
		if (event.widget == widget) {
			if (unreferenceRunnable != null) {
				unreferenceRunnable.run();
				unreferenceRunnable = null;
			}
			unhookCheckListener();
			ToolItemUpdater updater = getUpdater();
			if (updater != null) {
				updater.removeItem(this);
			}
			if (infoContext != null) {
				infoContext.dispose();
				infoContext = null;
			}
			widget.removeListener(SWT.Selection, getItemListener());
			widget.removeListener(SWT.Dispose, getItemListener());
			widget.removeListener(SWT.DefaultSelection, getItemListener());
			widget.removeListener(SWT.Help, getItemListener());
			widget = null;
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
			if (unreferenceRunnable != null) {
				unreferenceRunnable.run();
				unreferenceRunnable = null;
			}

			ParameterizedCommand command = model.getWbCommand();
			if (command != null) {
				if (styleState != null) {
					styleState.removeListener(stateListener);
					styleState = null;
				}
				if (toggleState != null) {
					toggleState.removeListener(stateListener);
					toggleState = null;
				}
				if (radioState != null) {
					radioState.removeListener(stateListener);
					radioState = null;
				}
			}
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

	@SuppressWarnings("restriction")
	private void handleHelpRequest() {
		MCommand command = model.getCommand();
		if (command == null || helpService == null
				|| commandHelpService == null) {
			return;
		}

		String contextHelpId = commandHelpService.getHelpContextId(
				command.getElementId(), getContext(model));
		if (contextHelpId != null) {
			helpService.displayHelp(contextHelpId);
		}
	}

	/**
	 * @param event
	 * @return
	 */
	private boolean dropdownEvent(Event event) {
		if (event.detail == SWT.ARROW && model instanceof MToolItem) {
			ToolItem ti = (ToolItem) event.widget;
			MMenu mmenu = ((MToolItem) model).getMenu();
			if (mmenu == null) {
				return false;
			}
			Menu menu = getMenu(mmenu, ti);
			if (menu == null || menu.isDisposed()) {
				return true;
			}
			Rectangle itemBounds = ti.getBounds();
			Point displayAt = ti.getParent().toDisplay(itemBounds.x,
					itemBounds.y + itemBounds.height);
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
				final Menu menu = creator.getMenu(toolItem.getParent()
						.getShell());
				if (menu != null) {
					toolItem.addDisposeListener(new DisposeListener() {
						@Override
						public void widgetDisposed(DisposeEvent e) {
							if (menu != null && !menu.isDisposed()) {
								creator.dispose();
								mmenu.setWidget(null);
							}
						}
					});
					// mmenu.setWidget(menu);
					menu.setData(AbstractPartRenderer.OWNING_ME, menu);
					return menu;
				}
			}
		} else {
			final IEclipseContext lclContext = getContext(model);
			IPresentationEngine engine = lclContext
					.get(IPresentationEngine.class);
			obj = engine.createGui(mmenu, toolItem.getParent(), lclContext);
			if (obj instanceof Menu) {
				Menu menu = (Menu) obj;
				// menu.setData(AbstractPartRenderer.OWNING_ME, menu);
				return menu;
			}
			if (logger != null) {
				logger.debug("Rendering returned " + obj); //$NON-NLS-1$
			}
		}
		return null;
	}

	private IEclipseContext getStaticContext(Event event) {
		if (infoContext == null) {
			infoContext = EclipseContextFactory.create(HCI_STATIC_CONTEXT);
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
		ParameterizedCommand cmd = model.getWbCommand();
		if (cmd == null) {
			return;
		}
		final IEclipseContext lclContext = getContext(model);
		EHandlerService service = (EHandlerService) lclContext
				.get(EHandlerService.class.getName());
		final IEclipseContext staticContext = getStaticContext(trigger);
		service.executeHandler(cmd, staticContext);
	}

	private boolean canExecuteItem(Event trigger) {
		ParameterizedCommand cmd = model.getWbCommand();
		if (cmd == null) {
			return false;
		}
		final IEclipseContext lclContext = getContext(model);
		EHandlerService service = lclContext.get(EHandlerService.class);
		if (service == null) {
			return false;
		}
		final IEclipseContext staticContext = getStaticContext(trigger);
		return service.canExecute(cmd, staticContext);
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

	/**
	 * @return the model
	 */
	public MHandledItem getModel() {
		return model;
	}

	private ToolItemUpdater getUpdater() {
		if (model != null) {
			Object obj = model.getRenderer();
			if (obj instanceof ToolBarManagerRenderer) {
				return ((ToolBarManagerRenderer) obj).getUpdater();
			}
		}
		return null;
	}
}