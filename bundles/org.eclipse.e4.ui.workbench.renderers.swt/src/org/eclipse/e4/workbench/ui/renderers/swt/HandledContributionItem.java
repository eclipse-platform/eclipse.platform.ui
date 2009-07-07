/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.workbench.ui.renderers.swt;

import java.util.HashMap;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.model.application.ApplicationPackage;
import org.eclipse.e4.ui.model.application.MHandledItem;
import org.eclipse.e4.ui.model.application.MMenuItem;
import org.eclipse.e4.ui.model.application.MParameter;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.services.ECommandService;
import org.eclipse.e4.ui.services.EHandlerService;
import org.eclipse.e4.ui.workbench.swt.util.ISWTResourceUtiltities;
import org.eclipse.e4.workbench.ui.IResourceUtiltities;
import org.eclipse.e4.workbench.ui.internal.Activator;
import org.eclipse.e4.workbench.ui.internal.Policy;
import org.eclipse.e4.workbench.ui.renderers.PartFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.resource.DeviceResourceException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;

public class HandledContributionItem extends ContributionItem {
	/**
	 * item refresh time in milliseconds.
	 */
	private static final int VISIBLE_ITEM_REFRESH = 2000;
	private MHandledItem model;
	private Widget widget;
	private Listener menuItemListener;
	private IEclipseContext context;
	private LocalResourceManager localResourceManager;
	private AdapterImpl modelListener;

	public HandledContributionItem(MHandledItem model, IEclipseContext context) {
		this.model = model;
		this.context = context;
		generateCommand();
	}

	/**
	 * 
	 */
	private void generateCommand() {
		if (model.getCommand() == null) {
			return;
		}
		if (model.getWbCommand() == null) {
			ECommandService cs = (ECommandService) context
					.get(ECommandService.class.getName());
			final Command cmd = cs.getCommand(model.getCommand().getId());
			final EList<MParameter> modelParms = model.getParameters();
			if (modelParms.isEmpty()) {
				Activator.trace(Policy.DEBUG_MENUS, "command: " + cmd, null); //$NON-NLS-1$
				model.setWbCommand(new ParameterizedCommand(cmd, null));
				return;
			}
			HashMap<String, String> parms = new HashMap<String, String>();
			for (MParameter parm : modelParms) {
				parms.put(parm.getName(), parm.getValue());
			}
			final ParameterizedCommand parmCmd = ParameterizedCommand
					.generateCommand(cmd, parms);
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
		MenuItem item = null;
		if (index >= 0) {
			item = new MenuItem(menu, style, index);
		} else {
			item = new MenuItem(menu, style);
		}
		item.setData(this);

		item.addListener(SWT.Dispose, getItemListener());
		item.addListener(SWT.Selection, getItemListener());

		widget = item;

		update(null);
		updateIcons();
		addModelListener();
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
		ToolItem item = null;
		if (index >= 0) {
			item = new ToolItem(parent, style, index);
		} else {
			item = new ToolItem(parent, style);
		}
		item.setData(this);

		item.addListener(SWT.Dispose, getItemListener());
		item.addListener(SWT.Selection, getItemListener());

		widget = item;

		update(null);
		updateIcons();
		addModelListener();
		widget.getDisplay().timerExec(VISIBLE_ITEM_REFRESH, new Runnable() {
			public void run() {
				if (widget == null || widget.isDisposed()) {
					return;
				}
				ToolItem item = (ToolItem) widget;
				item.setEnabled(canExecuteItem(widget.getDisplay()));
				widget.getDisplay().timerExec(VISIBLE_ITEM_REFRESH, this);
			}
		});
	}

	/**
	 * 
	 */
	private void addModelListener() {
		if (modelListener == null) {
			modelListener = new AdapterImpl() {
				@Override
				public void notifyChanged(Notification msg) {
					if (ApplicationPackage.Literals.MITEM__NAME.equals(msg
							.getFeature())
							|| ApplicationPackage.Literals.MITEM__TOOLTIP
									.equals(msg.getFeature())) {
						if (widget != null) {
							widget.getDisplay().asyncExec(new Runnable() {
								public void run() {
									update(null);
								}
							});
						}
					} else if (ApplicationPackage.Literals.MITEM__ICON_URI
							.equals(msg.getFeature())) {
						if (widget != null) {
							widget.getDisplay().asyncExec(new Runnable() {
								public void run() {
									updateIcons();
								}
							});
						}
					} else if (ApplicationPackage.Literals.MMENU_ITEM__VISIBLE
							.equals(msg.getFeature())) {
						updateVisible();
					}
				}
			};
			model.eAdapters().add(modelListener);
		}
	}

	private void updateVisible() {
		if (model instanceof MMenuItem) {
			setVisible(((MMenuItem) model).isVisible());
			final IContributionManager parent = getParent();
			if (parent != null) {
				parent.markDirty();
			}
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
		item.setText(model.getName());
		item.setEnabled(canExecuteItem(widget.getDisplay()));
	}

	private void updateToolItem() {
		ToolItem item = (ToolItem) widget;
		final String text = model.getName();
		if (text != null) {
			item.setText(text);
		}
		final String tooltip = model.getTooltip();
		if (tooltip != null) {
			item.setToolTipText(tooltip);
		}
		item.setEnabled(canExecuteItem(widget.getDisplay()));
	}

	private void updateIcons() {
		if (widget instanceof MenuItem) {
			MenuItem item = (MenuItem) widget;
			LocalResourceManager m = new LocalResourceManager(JFaceResources
					.getResources());
			String iconURI = model.getIconURI();
			if (iconURI != null && !iconURI.equals("null")) { //$NON-NLS-1$
				ISWTResourceUtiltities resUtils = (ISWTResourceUtiltities) context
						.get(IResourceUtiltities.class.getName());
				ImageDescriptor icon = resUtils.imageDescriptorFromURI(URI
						.createURI(iconURI));
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
		} else if (widget instanceof ToolItem) {
			ToolItem item = (ToolItem) widget;
			LocalResourceManager m = new LocalResourceManager(JFaceResources
					.getResources());
			String iconURI = model.getIconURI();
			if (iconURI != null && !iconURI.equals("null")) { //$NON-NLS-1$
				ISWTResourceUtiltities resUtils = (ISWTResourceUtiltities) context
						.get(IResourceUtiltities.class.getName());
				ImageDescriptor icon = resUtils.imageDescriptorFromURI(URI
						.createURI(iconURI));
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
			widget = null;
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
		}
	}

	private void handleWidgetSelection(Event event) {
		if (widget != null && !widget.isDisposed()) {
			if (canExecuteItem(widget.getDisplay())) {
				executeItem(widget.getDisplay());
			}
		}
	}

	private IEclipseContext getFocusContext(Display display) {
		// find the first useful part in the model
		Control control = display.getFocusControl();
		Object partObj = null;
		while (control != null && !(partObj instanceof MPart<?>)) {
			partObj = control.getData(PartFactory.OWNING_ME);
			control = control.getParent();
		}
		if (partObj == null) {
			return context;
		}
		// get the applicable context (or parent)
		MPart<?> part = (MPart<?>) partObj;
		return getContext(part);
	}

	private IEclipseContext getContext(MPart<?> part) {
		IEclipseContext c = null;
		while (c == null && part != null) {
			c = part.getContext();
			part = part.getParent();
		}
		return c;
	}

	protected boolean canExecuteItem(Display display) {
		final ParameterizedCommand parmCmd = model.getWbCommand();
		if (parmCmd == null) {
			return false;
		}
		IEclipseContext context = getFocusContext(display);
		EHandlerService hs = (EHandlerService) context
				.get(EHandlerService.class.getName());
		if (hs == null) {
			return true;
		}
		return hs.canExecute(parmCmd);
	}

	protected Object executeItem(Display display) {
		final ParameterizedCommand parmCmd = model.getWbCommand();
		if (parmCmd == null) {
			return false;
		}
		IEclipseContext context = getFocusContext(display);
		EHandlerService hs = (EHandlerService) context
				.get(EHandlerService.class.getName());
		if (hs == null) {
			return null;
		}
		return hs.executeHandler(parmCmd);
	}
}
