/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.menus;

import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.ICommandListener;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandImageService;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementReference;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.help.IWorkbenchHelpSystem;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.services.IServiceLocator;

/**
 * A contribution item which delegates to a command. It can be used in
 * {@link AbstractContributionFactory#createContributionItems(IServiceLocator, IContributionRoot)}.
 * <p>
 * It currently supports placement in menus and toolbars.
 * </p>
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @since 3.3
 */
public final class CommandContributionItem extends ContributionItem {
	/**
	 * A push button tool item or menu item.
	 */
	public static final int STYLE_PUSH = SWT.PUSH;

	/**
	 * A checked tool item or menu item.
	 */
	public static final int STYLE_CHECK = SWT.CHECK;

	/**
	 * A radio-button style menu item.
	 */
	public static final int STYLE_RADIO = SWT.RADIO;

	/**
	 * A ToolBar pulldown item.
	 */
	public static final int STYLE_PULLDOWN = SWT.DROP_DOWN;

	/**
	 * Mode bit: Show text on tool items or buttons, even if an image is
	 * present. If this mode bit is not set, text is only shown on tool items if
	 * there is no image present.
	 * 
	 * @since 3.4
	 */
	public static int MODE_FORCE_TEXT = 1;

	private LocalResourceManager localResourceManager;

	private Listener menuItemListener;

	private Widget widget;

	private IMenuService menuService;

	private ICommandService commandService;

	private IHandlerService handlerService;

	private IBindingService bindingService;

	private ParameterizedCommand command;

	private ImageDescriptor icon;

	private String label;

	private String tooltip;

	private ImageDescriptor disabledIcon;

	private ImageDescriptor hoverIcon;

	private String mnemonic;

	private IElementReference elementRef;

	private boolean checkedState;

	private int style;

	private ICommandListener commandListener;

	private String dropDownMenuOverride;

	private IWorkbenchHelpSystem workbenchHelpSystem;

	private String helpContextId;

	private int mode = 0;

	/**
	 * This is <code>true</code> when the menu contribution's visibleWhen
	 * checkEnabled attribute is <code>true</code>.
	 */
	private boolean visibleEnabled;

	/**
	 * Create a CommandContributionItem to place in a ContributionManager.
	 * 
	 * @param contributionParameters
	 *            parameters necessary to render this contribution item.
	 * @since 3.4
	 */
	public CommandContributionItem(
			CommandContributionItemParameter contributionParameters) {
		super(contributionParameters.id);

		this.icon = contributionParameters.icon;
		this.disabledIcon = contributionParameters.disabledIcon;
		this.hoverIcon = contributionParameters.hoverIcon;
		this.label = contributionParameters.label;
		this.mnemonic = contributionParameters.mnemonic;
		this.tooltip = contributionParameters.tooltip;
		this.style = contributionParameters.style;
		this.helpContextId = contributionParameters.helpContextId;
		this.visibleEnabled = contributionParameters.visibleEnabled;
		this.mode = contributionParameters.mode;

		menuService = (IMenuService) contributionParameters.serviceLocator
				.getService(IMenuService.class);
		commandService = (ICommandService) contributionParameters.serviceLocator
				.getService(ICommandService.class);
		handlerService = (IHandlerService) contributionParameters.serviceLocator
				.getService(IHandlerService.class);
		bindingService = (IBindingService) contributionParameters.serviceLocator
				.getService(IBindingService.class);
		createCommand(contributionParameters.commandId,
				contributionParameters.parameters);

		if (command != null) {
			try {
				UIElement callback = new UIElement(
						contributionParameters.serviceLocator) {

					public void setChecked(boolean checked) {
						CommandContributionItem.this.setChecked(checked);
					}

					public void setDisabledIcon(ImageDescriptor desc) {
						CommandContributionItem.this.setDisabledIcon(desc);
					}

					public void setHoverIcon(ImageDescriptor desc) {
						CommandContributionItem.this.setHoverIcon(desc);
					}

					public void setIcon(ImageDescriptor desc) {
						CommandContributionItem.this.setIcon(desc);
					}

					public void setText(String text) {
						CommandContributionItem.this.setText(text);
					}

					public void setTooltip(String text) {
						CommandContributionItem.this.setTooltip(text);
					}

					public void setDropDownId(String id) {
						dropDownMenuOverride = id;
					}
				};
				elementRef = commandService.registerElementForCommand(command,
						callback);
				command.getCommand().addCommandListener(getCommandListener());
				setImages(contributionParameters.serviceLocator,
						contributionParameters.iconStyle);

				if (contributionParameters.helpContextId == null) {
					try {
						this.helpContextId = commandService
								.getHelpContextId(contributionParameters.commandId);
					} catch (NotDefinedException e) {
						// it's OK to not have a helpContextId
					}
				}
				IWorkbench workbench = (IWorkbench) contributionParameters.serviceLocator
						.getService(IWorkbench.class);
				if (workbench != null && helpContextId != null) {
					this.workbenchHelpSystem = workbench.getHelpSystem();
				}
			} catch (NotDefinedException e) {
				WorkbenchPlugin
						.log("Unable to register menu item \"" + getId() //$NON-NLS-1$
								+ "\", command \"" + contributionParameters.commandId + "\" not defined"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

	}

	/**
	 * Create a CommandContributionItem to place in a ContributionManager.
	 * 
	 * @param serviceLocator
	 *            a service locator that is most appropriate for this
	 *            contribution. Typically the local {@link IWorkbenchWindow} or
	 *            {@link IWorkbenchPartSite} will be sufficient.
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
	 * @param style
	 *            The style of this menu contribution. See the STYLE_* contants.
	 * @deprecated create the {@link CommandContributionItemParameter}
	 */
	public CommandContributionItem(IServiceLocator serviceLocator, String id,
			String commandId, Map parameters, ImageDescriptor icon,
			ImageDescriptor disabledIcon, ImageDescriptor hoverIcon,
			String label, String mnemonic, String tooltip, int style) {
		this(new CommandContributionItemParameter(serviceLocator, id,
				commandId, parameters, icon, disabledIcon, hoverIcon, label,
				mnemonic, tooltip, style, null, false));
	}

	private void setImages(IServiceLocator locator, String iconStyle) {
		if (icon == null) {
			ICommandImageService service = (ICommandImageService) locator
					.getService(ICommandImageService.class);
			icon = service.getImageDescriptor(command.getId(),
					ICommandImageService.TYPE_DEFAULT, iconStyle);
			disabledIcon = service.getImageDescriptor(command.getId(),
					ICommandImageService.TYPE_DISABLED, iconStyle);
			hoverIcon = service.getImageDescriptor(command.getId(),
					ICommandImageService.TYPE_HOVER, iconStyle);
		}
	}

	private ICommandListener getCommandListener() {
		if (commandListener == null) {
			commandListener = new ICommandListener() {
				public void commandChanged(CommandEvent commandEvent) {
					if (commandEvent.isHandledChanged()
							|| commandEvent.isEnabledChanged()
							|| commandEvent.isDefinedChanged()) {
						updateCommandProperties(commandEvent);
					}
				}
			};
		}
		return commandListener;
	}

	private void updateCommandProperties(final CommandEvent commandEvent) {
		if (commandEvent.isHandledChanged()) {
			dropDownMenuOverride = null;
		}
		if (widget == null || widget.isDisposed()) {
			return;
		}
		Display display = widget.getDisplay();
		Runnable update = new Runnable() {
			public void run() {
				if (commandEvent.getCommand().isDefined()) {
					update(null);
				}
				if (commandEvent.isEnabledChanged()
						|| commandEvent.isHandledChanged()) {
					if (visibleEnabled) {
						IContributionManager parent = getParent();
						if (parent != null) {
							parent.update(true);
						}
					}
				}
			}
		};
		if (display.getThread() == Thread.currentThread()) {
			update.run();
		} else {
			display.asyncExec(update);
		}
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
		command = ParameterizedCommand.generateCommand(cmd, parameters);
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

		// Menus don't support the pulldown style
		int tmpStyle = style;
		if (tmpStyle == STYLE_PULLDOWN)
			tmpStyle = STYLE_PUSH;

		MenuItem item = null;
		if (index >= 0) {
			item = new MenuItem(parent, tmpStyle, index);
		} else {
			item = new MenuItem(parent, tmpStyle);
		}
		item.setData(this);
		if (workbenchHelpSystem != null) {
			workbenchHelpSystem.setHelp(item, helpContextId);
		}
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
			item = new ToolItem(parent, style, index);
		} else {
			item = new ToolItem(parent, style);
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
				text = updateMnemonic(text);

				String keyBindingText = null;
				if (command != null) {
					TriggerSequence binding = bindingService
							.getBestActiveBindingFor(command);
					if (binding != null) {
						keyBindingText = binding.format();
					}
				}
				if (text != null) {
					if (keyBindingText == null) {
						item.setText(text);
					} else {
						item.setText(text + '\t' + keyBindingText);
					}
				}

				updateIcons();
				if (item.getSelection() != checkedState) {
					item.setSelection(checkedState);
				}

				boolean shouldBeEnabled = isEnabled();
				if (item.getEnabled() != shouldBeEnabled) {
					item.setEnabled(shouldBeEnabled);
				}
			} else if (widget instanceof ToolItem) {
				ToolItem item = (ToolItem) widget;

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
				if (icon != null) {
					updateIcons();
				}

				if ((icon == null || (mode & MODE_FORCE_TEXT) == MODE_FORCE_TEXT)
						&& text != null) {
					item.setText(text);
				}

				if (tooltip != null)
					item.setToolTipText(tooltip);
				else {
					if (text != null) {
						item.setToolTipText(text);
					}
				}

				if (item.getSelection() != checkedState) {
					item.setSelection(checkedState);
				}

				boolean shouldBeEnabled = isEnabled();
				if (item.getEnabled() != shouldBeEnabled) {
					item.setEnabled(shouldBeEnabled);
				}
			}
		}
	}

	private String updateMnemonic(String s) {
		if (mnemonic == null || s == null) {
			return s;
		}
		int idx = s.indexOf(mnemonic);
		if (idx == -1) {
			return s;
		}

		return s.substring(0, idx) + '&' + s.substring(idx);
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
	public void dispose() {
		if (elementRef != null) {
			commandService.unregisterElement(elementRef);
			elementRef = null;
		}
		if (commandListener != null) {
			command.getCommand().removeCommandListener(commandListener);
			commandListener = null;
		}
		command = null;
		commandService = null;
		bindingService = null;
		menuService = null;
		handlerService = null;
		disposeOldImages();
		super.dispose();
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

	private void handleWidgetSelection(Event event) {
		// Special check for ToolBar dropdowns...
		if (openDropDownMenu(event))
			return;

		if ((style & (SWT.TOGGLE | SWT.CHECK)) != 0) {
			if (event.widget instanceof ToolItem) {
				checkedState = ((ToolItem) event.widget).getSelection();
			} else if (event.widget instanceof MenuItem) {
				checkedState = ((MenuItem) event.widget).getSelection();
			}
		}

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
	 * Determines if the selection was on the dropdown affordance and, if so,
	 * opens the drop down menu (populated using the same id as this item...
	 * 
	 * @param event
	 *            The <code>SWT.Selection</code> event to be tested
	 * 
	 * @return <code>true</code> iff a drop down menu was opened
	 */
	private boolean openDropDownMenu(Event event) {
		Widget item = event.widget;
		if (item != null) {
			int style = item.getStyle();
			if ((style & SWT.DROP_DOWN) != 0) {
				if (event.detail == 4) { // on drop-down button
					ToolItem ti = (ToolItem) item;

					final MenuManager menuManager = new MenuManager();
					Menu menu = menuManager.createContextMenu(ti.getParent());
					if (workbenchHelpSystem != null) {
						workbenchHelpSystem.setHelp(menu, helpContextId);
					}
					menuManager.addMenuListener(new IMenuListener() {
						public void menuAboutToShow(IMenuManager manager) {
							String id = getId();
							if (dropDownMenuOverride != null) {
								id = dropDownMenuOverride;
							}
							menuService.populateContributionManager(
									menuManager, "menu:" + id); //$NON-NLS-1$
						}
					});

					// position the menu below the drop down item
					Point point = ti.getParent().toDisplay(
							new Point(event.x, event.y));
					menu.setLocation(point.x, point.y); // waiting for SWT
					// 0.42
					menu.setVisible(true);
					return true; // we don't fire the action
				}
			}
		}

		return false;
	}

	private void setIcon(ImageDescriptor desc) {
		icon = desc;
		updateIcons();
	}

	private void updateIcons() {
		if (widget instanceof MenuItem) {
			MenuItem item = (MenuItem) widget;
			LocalResourceManager m = new LocalResourceManager(JFaceResources
					.getResources());
			item.setImage(icon == null ? null : m.createImage(icon));
			disposeOldImages();
			localResourceManager = m;
		} else if (widget instanceof ToolItem) {
			ToolItem item = (ToolItem) widget;
			LocalResourceManager m = new LocalResourceManager(JFaceResources
					.getResources());
			item.setDisabledImage(disabledIcon == null ? null : m
					.createImage(disabledIcon));
			item.setHotImage(hoverIcon == null ? null : m
					.createImage(hoverIcon));
			item.setImage(icon == null ? null : m.createImage(icon));
			disposeOldImages();
			localResourceManager = m;
		}
	}

	private void setText(String text) {
		label = text;
		update(null);
	}

	private void setChecked(boolean checked) {
		if (checkedState == checked) {
			return;
		}
		checkedState = checked;
		if (widget instanceof MenuItem) {
			((MenuItem) widget).setSelection(checkedState);
		} else if (widget instanceof ToolItem) {
			((ToolItem) widget).setSelection(checkedState);
		}
	}

	private void setTooltip(String text) {
		tooltip = text;
		if (widget instanceof ToolItem) {
			((ToolItem) widget).setToolTipText(text);
		}
	}

	private void setDisabledIcon(ImageDescriptor desc) {
		disabledIcon = desc;
		updateIcons();
	}

	private void setHoverIcon(ImageDescriptor desc) {
		hoverIcon = desc;
		updateIcons();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ContributionItem#isEnabled()
	 */
	public boolean isEnabled() {
		if (command != null) {
			command.getCommand().setEnabled(menuService.getCurrentState());
			return command.getCommand().isEnabled();
		}
		return false;
	}

	/**
	 * @since 3.4
	 */
	public boolean isVisible() {
		if (visibleEnabled) {
			return super.isVisible() && isEnabled();
		}
		return super.isVisible();
	}
}
