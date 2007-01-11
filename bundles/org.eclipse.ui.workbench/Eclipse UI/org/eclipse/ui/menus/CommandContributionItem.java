/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.menus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.action.ContributionItem;
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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * A contribution item which delegates to a command. It can be used in
 * {@link AbstractContributionFactory#createContributionItems(IMenuService, java.util.List)}.
 * <p>
 * It currently supports placement in menus and toolbars.
 * </p>
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * <p>
 * <strong>PROVISIONAL</strong>. This class or interface has been added as part
 * of a work in progress. There is a guarantee neither that this API will work
 * nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.3
 */
public final class CommandContributionItem extends ContributionItem {
	/**
	 * 
	 */
	private LocalResourceManager localResourceManager;

	private Listener menuItemListener;

	private Widget widget;

	private ICommandService commandService;

	private IHandlerService handlerService;

	private ParameterizedCommand command;

	private ImageDescriptor icon;

	private String label;

	private String tooltip;

	private final ImageDescriptor disabledIcon;

	private final ImageDescriptor hoverIcon;

	private final String mnemonic;

	/**
	 * Create a CommandContributionItem to place in a ContributionManager.
	 * 
	 * @param id
	 *            The id for this item. May be <code>null</code>. Items
	 *            without an id cannot be referenced later.
	 * @param commandId
	 *            A command id for a defined command. Must not be
	 *            <code>null</code>.
	 * @param parameters
	 *            A map of strings to strings which represent parameter names to
	 *            values. The parameter names must match those in the command
	 *            definition.
	 * @param icon
	 *            An icon for this item. May be <code>null</code>.
	 * @param disabledIcon
	 *            A disabled icon for this item. May be <code>null</code>.
	 * @param hoverIcon
	 *            A hover icon for this item. May be <code>null</code>.
	 * @param label
	 *            A label for this item. May be <code>null</code>.
	 * @param mnemonic
	 *            A mnemonic for this item to be applied to the label. May be
	 *            <code>null</code>.
	 * @param tooltip
	 *            A tooltip for this item. May be <code>null</code>. Tooltips
	 *            are currently only valid for toolbar contributions.
	 */
	public CommandContributionItem(String id, String commandId, Map parameters,
			ImageDescriptor icon, ImageDescriptor disabledIcon,
			ImageDescriptor hoverIcon, String label, String mnemonic,
			String tooltip) {
		super(id);
		this.icon = icon;
		this.disabledIcon = disabledIcon;
		this.hoverIcon = hoverIcon;
		this.label = label;
		this.mnemonic = mnemonic;
		this.tooltip = tooltip;
		commandService = (ICommandService) PlatformUI.getWorkbench()
				.getService(ICommandService.class);
		handlerService = (IHandlerService) PlatformUI.getWorkbench()
				.getService(IHandlerService.class);
		createCommand(commandId, parameters);
	}

	ParameterizedCommand getCommand() {
		return command;
	}

	void createCommand(String commandId, Map parameters) {
		if (commandId == null) {
			WorkbenchPlugin.log("Unable to create menu item \"" + getId() //$NON-NLS-1$
					+ "\", no command id"); //$NON-NLS-1$
			return;
		}
		Command cmd = commandService.getCommand(commandId);
		if (!cmd.isDefined()) {
			WorkbenchPlugin.log("Unable to create menu item \"" + getId() //$NON-NLS-1$
					+ "\", command \"" + commandId + "\" not defined"); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		if (parameters == null || parameters.size() == 0) {
			command = new ParameterizedCommand(cmd, null);
			return;
		}

		try {
			ArrayList parmList = new ArrayList();
			Iterator i = parameters.entrySet().iterator();
			while (i.hasNext()) {
				Map.Entry entry = (Map.Entry) i.next();
				String parmName = (String) entry.getKey();
				IParameter parm;
				parm = cmd.getParameter(parmName);
				if (parm == null) {
					WorkbenchPlugin
							.log("Unable to create menu item \"" + getId() //$NON-NLS-1$
									+ "\", parameter \"" + parmName + "\" for command \"" //$NON-NLS-1$ //$NON-NLS-2$
									+ commandId + "\" is not defined"); //$NON-NLS-1$
					return;
				}
				parmList.add(new Parameterization(parm, (String) entry
						.getValue()));
			}
			command = new ParameterizedCommand(cmd,
					(Parameterization[]) parmList
							.toArray(new Parameterization[parmList.size()]));
		} catch (NotDefinedException e) {
			// this shouldn't happen as we checked for !defined, but we
			// won't take the chance
			WorkbenchPlugin.log("Failed to create menu item " //$NON-NLS-1$
					+ getId(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ContributionItem#fill(org.eclipse.swt.widgets.Menu,
	 *      int)
	 */
	public void fill(Menu parent, int index) {
		if (command == null) {
			return;
		}
		if (widget != null || parent == null) {
			return;
		}

		MenuItem item = null;
		if (index >= 0) {
			item = new MenuItem(parent, SWT.PUSH, index);
		} else {
			item = new MenuItem(parent, SWT.PUSH);
		}
		item.setData(this);

		item.addListener(SWT.Dispose, getItemListener());
		item.addListener(SWT.Selection, getItemListener());
		widget = item;

		update(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ContributionItem#fill(org.eclipse.swt.widgets.ToolBar,
	 *      int)
	 */
	public void fill(ToolBar parent, int index) {
		if (command == null) {
			return;
		}
		if (widget != null || parent == null) {
			return;
		}

		ToolItem item = null;
		if (index >= 0) {
			item = new ToolItem(parent, SWT.PUSH, index);
		} else {
			item = new ToolItem(parent, SWT.PUSH);
		}

		item.setData(this);

		item.addListener(SWT.Selection, getItemListener());
		item.addListener(SWT.Dispose, getItemListener());
		widget = item;

		update(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ContributionItem#update()
	 */
	public void update() {
		update(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ContributionItem#update(java.lang.String)
	 */
	public void update(String id) {
		if (widget != null) {
			if (widget instanceof MenuItem) {
				MenuItem item = (MenuItem) widget;

				String text = label;
				if (text == null) {
					if (command != null) {
						try {
							text = command.getCommand().getName();
						} catch (NotDefinedException e) {
							WorkbenchPlugin.log("Update item failed " //$NON-NLS-1$
									+ getId(), e);
						}
					}
				}
				if (mnemonic != null) {
					// convert label to have mnemonic
				}
				if (text != null && !item.getText().equals(text)) {
					item.setText(text);
				}

				if (icon != null) {
					LocalResourceManager m = new LocalResourceManager(
							JFaceResources.getResources());
					item.setImage(icon == null ? null : m.createImage(icon));
					disposeOldImages();
					localResourceManager = m;
				}
				if (item.isEnabled() != isEnabled()) {
					item.setEnabled(isEnabled());
				}
			} else if (widget instanceof ToolItem) {
				ToolItem item = (ToolItem) widget;

				if (icon != null) {
					LocalResourceManager m = new LocalResourceManager(
							JFaceResources.getResources());
					item.setDisabledImage(disabledIcon == null ? null : m
							.createImage(disabledIcon));
					item.setHotImage(hoverIcon == null ? null : m
							.createImage(hoverIcon));
					item.setImage(icon == null ? null : m.createImage(icon));
					disposeOldImages();
					localResourceManager = m;
				} else if (label != null) {
					item.setText(label);
				}

				if (tooltip != null)
					item.setToolTipText(tooltip);
				else {
					String text = label;
					if (text == null) {
						if (command != null) {
							try {
								text = command.getCommand().getName();
							} catch (NotDefinedException e) {
								WorkbenchPlugin.log("Update item failed " //$NON-NLS-1$
										+ getId(), e);
							}
						}
					}
					if (text != null) {
						item.setToolTipText(text);
					}
				}

				if (item.isEnabled() != isEnabled()) {
					item.setEnabled(isEnabled());
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.menus.AuthorityContributionItem#dispose()
	 */
	public void dispose() {
		disposeOldImages();
		super.dispose();
	}

	/**
	 * 
	 */
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

	private void handleWidgetSelection(Event event) {
		try {
			handlerService.executeCommand(command, event);
		} catch (ExecutionException e) {
			WorkbenchPlugin.log("Failed to execute item " //$NON-NLS-1$
					+ getId(), e);
		} catch (NotDefinedException e) {
			WorkbenchPlugin.log("Failed to execute item " //$NON-NLS-1$
					+ getId(), e);
		} catch (NotEnabledException e) {
			WorkbenchPlugin.log("Failed to execute item " //$NON-NLS-1$
					+ getId(), e);
		} catch (NotHandledException e) {
			WorkbenchPlugin.log("Failed to execute item " //$NON-NLS-1$
					+ getId(), e);
		}
	}

	/**
	 * Update the icon on this command contribution item.
	 * 
	 * @param desc
	 *            The descriptor for the new icon to display.
	 */
	public void setIcon(ImageDescriptor desc) {
		icon = desc;
		if (widget instanceof MenuItem) {
			MenuItem item = (MenuItem) widget;
			disposeOldImages();
			if (desc != null) {
				LocalResourceManager m = new LocalResourceManager(
						JFaceResources.getResources());
				item.setImage(m.createImage(desc));
				localResourceManager = m;
			}
		} else if (widget instanceof ToolItem) {
			ToolItem item = (ToolItem) widget;
			disposeOldImages();
			if (desc != null) {
				LocalResourceManager m = new LocalResourceManager(
						JFaceResources.getResources());
				item.setImage(m.createImage(desc));
				localResourceManager = m;
			}
		}
	}

	/**
	 * Update the label on this command contribution item.
	 * 
	 * @param text
	 *            The new label to display.
	 */
	public void setLabel(String text) {
		label = text;
		if (widget instanceof MenuItem) {
			((MenuItem) widget).setText(text);
		} else if (widget instanceof ToolItem) {
			((ToolItem) widget).setText(text);
		}
	}

	/**
	 * Update the tooltip on this command contribution item. Tooltips are
	 * currently only valid for toolbar contributions.
	 * 
	 * @param text
	 *            The new tooltip to display.
	 */
	public void setTooltip(String text) {
		tooltip = text;
		if (widget instanceof ToolItem) {
			((ToolItem) widget).setToolTipText(text);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ContributionItem#isEnabled()
	 */
	public boolean isEnabled() {
		if (command != null) {
			return command.getCommand().isEnabled();
		}
		return false;
	}
}
