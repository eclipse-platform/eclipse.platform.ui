package org.eclipse.e4.ui.workbench.renderers.swt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.IContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.internal.workbench.Activator;
import org.eclipse.e4.ui.internal.workbench.Policy;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledItem;
import org.eclipse.e4.ui.model.application.ui.menu.MItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MRenderedMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
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
	private MHandledItem model;
	private Widget widget;
	private Listener menuItemListener;
	private LocalResourceManager localResourceManager;

	@Inject
	private ECommandService commandService;

	@Inject
	private EModelService modelService;

	@Inject
	private EBindingService bindingService;

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

		update(null);
		updateIcons();
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
			EBindingService bs = (EBindingService) context
					.get(EBindingService.class.getName());
			ParameterizedCommand cmd = handledItem.getWbCommand();
			if (cmd == null) {
				cmd = generateParameterizedCommand(handledItem, context);
			}
			TriggerSequence sequence = bs.getBestSequenceFor(handledItem
					.getWbCommand());
			if (sequence != null) {
				if (text == null) {
					try {
						text = cmd.getName();
					} catch (NotDefinedException e) {
						return null;
					}
				}
				text = text + " (" + sequence.format() + ')'; //$NON-NLS-1$
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
			if (canExecuteItem()) {
				executeItem();
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

	private void executeItem() {
		ParameterizedCommand cmd = model.getWbCommand();
		if (cmd == null) {
			return;
		}
		final IEclipseContext lclContext = getContext(model);
		EHandlerService service = (EHandlerService) lclContext
				.get(EHandlerService.class.getName());
		lclContext.set(MItem.class.getName(), model);
		service.executeHandler(cmd);
		lclContext.remove(MItem.class.getName());
	}

	private boolean canExecuteItem() {
		ParameterizedCommand cmd = model.getWbCommand();
		if (cmd == null) {
			return false;
		}
		final IEclipseContext lclContext = getContext(model);
		EHandlerService service = lclContext.get(EHandlerService.class);
		return service.canExecute(cmd);
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