package org.eclipse.ui.internal.menus;

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
 * @since 3.3
 * 
 */
public final class CommandDataContributionItem extends
		AuthorityContributionItem implements IMenuCallback {
	/**
	 * 
	 */
	private LocalResourceManager localResourceManager;

	private Listener menuItemListener;

	private Widget widget;

	private ICommandService commandService;

	private IHandlerService handlerService;

	private ParameterizedCommand command;

	private ItemData item;

	/**
	 * @param id
	 * @param data
	 */
	CommandDataContributionItem(String id, ItemData data) {
		super(id);
		commandService = (ICommandService) PlatformUI.getWorkbench()
				.getService(ICommandService.class);
		handlerService = (IHandlerService) PlatformUI.getWorkbench()
				.getService(IHandlerService.class);
		item = data;
		createCommand();
	}

	ParameterizedCommand getCommand() {
		return command;
	}

	void createCommand() {

		String commandId = item.getCommandId();
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
		Map parameters = item.getParameters();
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

	public void fill(Menu parent, int index) {
		if (command == null) {
			return;
		}
		MenuItem newItem = new MenuItem(parent, SWT.PUSH, index);
		newItem.setData(this);
		newItem.setData(IMenuCallback.CALLBACK, this);
		newItem.setText(item.getLabel());

		ImageDescriptor iconDescriptor = item.getIcon();
		if (iconDescriptor != null) {
			LocalResourceManager m = new LocalResourceManager(JFaceResources
					.getResources());
			newItem.setImage(m.createImage(iconDescriptor));
			disposeOldImages();
			localResourceManager = m;
		}

		newItem.addListener(SWT.Dispose, getItemListener());
		newItem.addListener(SWT.Selection, getItemListener());
		widget = newItem;
		setDirty(false);
	}

	public void fill(ToolBar parent, int index) {
		if (command == null) {
			return;
		}
		ToolItem newItem = new ToolItem(parent, SWT.PUSH, index);
		newItem.setData(this);
		newItem.setData(IMenuCallback.CALLBACK, this);

		ImageDescriptor iconDescriptor = item.getIcon();
		if (iconDescriptor != null) {
			LocalResourceManager m = new LocalResourceManager(JFaceResources
					.getResources());
			newItem.setImage(m.createImage(iconDescriptor));
			disposeOldImages();
			localResourceManager = m;
		} else if (item.getLabel() != null) {
			newItem.setText(item.getLabel());
		}

		if (item.getTooltip() != null)
			newItem.setToolTipText(item.getTooltip());
		else
			newItem.setToolTipText(item.getLabel());
		// TBD...Listener support
		newItem.addListener(SWT.Selection, getItemListener());
		newItem.addListener(SWT.Dispose, getItemListener());

		widget = newItem;
		setDirty(false);
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
		if (getParent() != null) {
			getParent().update(true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.menus.AuthorityContributionItem#dispose()
	 */
	public void dispose() {
		disposeOldImages();
		item = null;
		command = null;
		setDirty(false);
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
			dispose();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.menus.IMenuData#setIcon(org.eclipse.jface.resource.ImageDescriptor)
	 */
	public void setIcon(ImageDescriptor icon) {
		if (widget instanceof MenuItem) {
			MenuItem item = (MenuItem) widget;
			if (icon != null) {
				LocalResourceManager m = new LocalResourceManager(
						JFaceResources.getResources());
				item.setImage(m.createImage(icon));
				disposeOldImages();
				localResourceManager = m;
			}
		} else if (widget instanceof ToolItem) {
			ToolItem item = (ToolItem) widget;
			if (icon != null) {
				LocalResourceManager m = new LocalResourceManager(
						JFaceResources.getResources());
				item.setImage(m.createImage(icon));
				disposeOldImages();
				localResourceManager = m;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.menus.IMenuData#setLabel(java.lang.String)
	 */
	public void setLabel(String text) {
		if (widget instanceof MenuItem) {
			((MenuItem) widget).setText(text);
		} else if (widget instanceof ToolItem) {
			((ToolItem) widget).setText(text);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.menus.IMenuData#setTooltip(java.lang.String)
	 */
	public void setTooltip(String text) {
		if (widget instanceof ToolItem) {
			((ToolItem) widget).setToolTipText(text);
		}
	}
}
