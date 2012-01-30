/*******************************************************************************
 * Copyright (c) 2010, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.core.commands.IStateListener;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.State;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.internal.workbench.Activator;
import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.internal.workbench.Policy;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledItem;
import org.eclipse.e4.ui.model.application.ui.menu.MItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
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
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.menus.IMenuStateIds;
import org.eclipse.jface.resource.DeviceResourceException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;

public class HandledContributionItem extends ContributionItem {
	static class RunnableRunner implements ISafeRunnable {
		private Runnable runnable;

		public void setRunnable(Runnable r) {
			runnable = r;
		}

		public void handleException(Throwable exception) {
			// Do not report these exceptions ATM
		}

		public void run() throws Exception {
			runnable.run();
		}

	}

	public static class ToolItemUpdateTimer implements Runnable {
		Display display = Display.getCurrent();
		RunnableRunner runner = new RunnableRunner();

		List<HandledContributionItem> itemsToCheck = new ArrayList<HandledContributionItem>();
		List<Runnable> windowRunnables = new ArrayList<Runnable>();

		public void addWindowRunnable(Runnable r) {
			windowRunnables.add(r);
		}

		public void removeWindowRunnable(Runnable r) {
			windowRunnables.remove(r);
		}

		void registerItem(HandledContributionItem item) {
			if (!itemsToCheck.contains(item)) {
				itemsToCheck.add(item);

				// Start the timer on the first item registered
				if (itemsToCheck.size() == 1)
					display.timerExec(400, this);
			}
		}

		void removeItem(HandledContributionItem item) {
			itemsToCheck.remove(item);
		}

		public void run() {
			for (HandledContributionItem hci : itemsToCheck) {
				hci.updateItemEnablement();
			}

			Runnable[] array = new Runnable[windowRunnables.size()];
			windowRunnables.toArray(array);
			for (Runnable r : array) {
				runner.setRunnable(r);
				SafeRunner.run(runner);
			}

			// repeat until the list goes empty
			if (itemsToCheck.size() > 0)
				display.timerExec(400, this);
		}
	}

	// HACK!! local 'static' timerExec...should move out of this class post 4.1
	public static ToolItemUpdateTimer toolItemUpdater = new ToolItemUpdateTimer();

	private static final String DISPOSABLE_CHECK = "IDisposable"; //$NON-NLS-1$
	private static final String WW_SUPPORT = "org.eclipse.ui.IWorkbenchWindow"; //$NON-NLS-1$
	private static final String HCI_STATIC_CONTEXT = "HCI-staticContext"; //$NON-NLS-1$
	private MHandledItem model;
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

	private ISWTResourceUtilities resUtils = null;

	private IStateListener stateListener = new IStateListener() {
		public void handleStateChange(State state, Object oldValue) {
			model.setSelected(((Boolean) state.getValue()).booleanValue());
		}
	};

	@Inject
	void setResourceUtils(IResourceUtilities utils) {
		resUtils = (ISWTResourceUtilities) utils;
	}

	private ISafeRunnable getUpdateRunner() {
		if (updateRunner == null) {
			updateRunner = new ISafeRunnable() {
				public void run() throws Exception {
					model.setEnabled(canExecuteItem(null));
					update();
				}

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
		public void menuAboutToShow(IMenuManager manager) {
			update(null);
		}
	};

	private ISafeRunnable updateRunner;

	private IEclipseContext infoContext;

	public void setModel(MHandledItem item) {
		model = item;
		setId(model.getElementId());
		generateCommand();
		updateVisible();
	}

	/**
	 * 
	 */
	private void generateCommand() {
		if (model.getCommand() == null) {
			return;
		}
		if (model.getWbCommand() == null) {
			String cmdId = model.getCommand().getElementId();
			final List<MParameter> modelParms = model.getParameters();
			if (modelParms.isEmpty()) {
				final ParameterizedCommand parmCmd = commandService
						.createCommand(cmdId, null);
				Activator
						.trace(Policy.DEBUG_MENUS, "command: " + parmCmd, null); //$NON-NLS-1$
				model.setWbCommand(parmCmd);

				State state = parmCmd.getCommand()
						.getState(IMenuStateIds.STYLE);
				if (state != null) {
					state.addListener(stateListener);
					model.setSelected(((Boolean) state.getValue())
							.booleanValue());
				}
				return;
			}
			HashMap<String, String> parms = new HashMap<String, String>();
			for (MParameter parm : modelParms) {
				parms.put(parm.getName(), parm.getValue());
			}
			final ParameterizedCommand parmCmd = commandService.createCommand(
					cmdId, parms);
			Activator.trace(Policy.DEBUG_MENUS, "command: " + parmCmd, null); //$NON-NLS-1$
			model.setWbCommand(parmCmd);

			State state = parmCmd.getCommand().getState(IMenuStateIds.STYLE);
			if (state != null) {
				state.addListener(stateListener);
				model.setSelected(((Boolean) state.getValue()).booleanValue());
			}
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

		widget = item;
		model.setWidget(widget);
		widget.setData(AbstractPartRenderer.OWNING_ME, model);

		update(null);
		updateIcons();
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
		toolItemUpdater.registerItem(this);

		update(null);
		updateIcons();
		hookCheckListener();
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
			obj = func.compute(staticContext);
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
		final String text = model.getLocalizedLabel();
		if (text != null) {
			item.setText(text);
		} else {
			item.setText(""); //$NON-NLS-1$
		}
		final String tooltip = getToolTipText(model);
		item.setToolTipText(tooltip);
		item.setSelection(model.isSelected());
		item.setEnabled(model.isEnabled());
	}

	private String getToolTipText(MItem item) {
		String text = item.getLocalizedTooltip();
		if (item instanceof MHandledItem) {
			MHandledItem handledItem = (MHandledItem) item;
			IEclipseContext context = getContext(item);
			ParameterizedCommand cmd = handledItem.getWbCommand();
			if (cmd == null) {
				cmd = generateParameterizedCommand(handledItem, context);
			}
			if (text == null) {
				try {
					text = cmd.getName();
				} catch (NotDefinedException e) {
					return null;
				}
			}
			EBindingService bs = (EBindingService) context
					.get(EBindingService.class.getName());
			if (bs != null) {
				TriggerSequence sequence = bs.getBestSequenceFor(handledItem
						.getWbCommand());
				if (sequence != null) {
					text = text + " (" + sequence.format() + ')'; //$NON-NLS-1$
				}
			}
			return text;
		}
		return text;
	}

	private ParameterizedCommand generateParameterizedCommand(
			final MHandledItem item, final IEclipseContext lclContext) {
		ECommandService cmdService = (ECommandService) lclContext
				.get(ECommandService.class.getName());
		Map<String, Object> parameters = null;
		List<MParameter> modelParms = item.getParameters();
		if (modelParms != null && !modelParms.isEmpty()) {
			parameters = new HashMap<String, Object>();
			for (MParameter mParm : modelParms) {
				parameters.put(mParm.getName(), mParm.getValue());
			}
		}
		ParameterizedCommand cmd = cmdService.createCommand(item.getCommand()
				.getElementId(), parameters);
		item.setWbCommand(cmd);
		return cmd;
	}

	private void updateIcons() {
		if (widget instanceof MenuItem) {
			MenuItem item = (MenuItem) widget;
			LocalResourceManager m = new LocalResourceManager(
					JFaceResources.getResources());
			String iconURI = model.getIconURI();
			ImageDescriptor icon = getImageDescriptor(model);
			try {
				item.setImage(icon == null ? null : m.createImage(icon));
			} catch (DeviceResourceException e) {
				icon = ImageDescriptor.getMissingImageDescriptor();
				item.setImage(m.createImage(icon));
				// as we replaced the failed icon, log the message once.
				Activator.trace(Policy.DEBUG_MENUS,
						"failed to create image " + iconURI, e); //$NON-NLS-1$
			}
			disposeOldImages();
			localResourceManager = m;
		} else if (widget instanceof ToolItem) {
			ToolItem item = (ToolItem) widget;
			LocalResourceManager m = new LocalResourceManager(
					JFaceResources.getResources());
			String iconURI = model.getIconURI();
			ImageDescriptor icon = getImageDescriptor(model);
			try {
				item.setImage(icon == null ? null : m.createImage(icon));
			} catch (DeviceResourceException e) {
				icon = ImageDescriptor.getMissingImageDescriptor();
				item.setImage(m.createImage(icon));
				// as we replaced the failed icon, log the message once.
				Activator.trace(Policy.DEBUG_MENUS,
						"failed to create image " + iconURI, e); //$NON-NLS-1$
			}
			disposeOldImages();
			localResourceManager = m;
		}
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
			unhookCheckListener();
			toolItemUpdater.removeItem(this);
			if (infoContext != null) {
				infoContext.dispose();
				infoContext = null;
			}
			widget.removeListener(SWT.Selection, getItemListener());
			widget.removeListener(SWT.Dispose, getItemListener());
			widget.removeListener(SWT.DefaultSelection, getItemListener());
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
			ParameterizedCommand command = model.getWbCommand();
			if (command != null) {
				State state = command.getCommand()
						.getState(IMenuStateIds.STYLE);
				if (state != null) {
					state.removeListener(stateListener);
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
		} else {
			final IEclipseContext lclContext = getContext(model);
			IPresentationEngine engine = lclContext
					.get(IPresentationEngine.class);
			obj = engine.createGui(mmenu, toolItem.getParent(), lclContext);
			if (obj instanceof Menu) {
				Menu menu = (Menu) obj;
				// menu.setData(AbstractPartRenderer.OWNING_ME, menu);
				return menu;
			} else {
				System.err.println("Rendering returned " + obj); //$NON-NLS-1$
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

	private ImageDescriptor getImageDescriptor(MUILabel element) {
		String iconURI = element.getIconURI();
		if (iconURI != null && iconURI.length() > 0) {
			return resUtils.imageDescriptorFromURI(URI.createURI(iconURI));
		}
		return null;
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