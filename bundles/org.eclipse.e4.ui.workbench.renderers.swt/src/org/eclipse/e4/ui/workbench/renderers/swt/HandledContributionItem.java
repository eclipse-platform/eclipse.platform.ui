package org.eclipse.e4.ui.workbench.renderers.swt;

import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.internal.workbench.Activator;
import org.eclipse.e4.ui.internal.workbench.Policy;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledItem;
import org.eclipse.e4.ui.model.application.ui.menu.MItem;
import org.eclipse.e4.ui.workbench.IResourceUtilities;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.swt.util.ISWTResourceUtilities;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.resource.DeviceResourceException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
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
		int style = SWT.PUSH;
		if (model.getType() == ItemType.PUSH)
			style = SWT.PUSH;
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
		String text = model.getLabel();
		ParameterizedCommand parmCmd = model.getWbCommand();
		String keyBindingText = null;
		if (parmCmd != null) {
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
		final String text = model.getLabel();
		if (text != null) {
			item.setText(text);
		} else {
			item.setText(""); //$NON-NLS-1$
		}
		final String tooltip = model.getTooltip();
		if (tooltip != null) {
			item.setToolTipText(tooltip);
		}
		item.setSelection(model.isSelected());
		item.setEnabled(model.isEnabled());
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
			if (model.getType() == ItemType.CHECK
					|| model.getType() == ItemType.RADIO) {
				model.setSelected(((MenuItem) widget).getSelection());
			}
			if (canExecuteItem()) {
				executeItem();
			}
		}
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