/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Andrey Loskutov <loskutov@gmx.de> - Bug 445538
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472654
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 489250
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs.cpd;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.ui.workbench.renderers.swt.HandledContributionItem;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.dialogs.cpd.CustomizePerspectiveDialog.ActionSet;
import org.eclipse.ui.internal.dialogs.cpd.CustomizePerspectiveDialog.DisplayItem;
import org.eclipse.ui.internal.dialogs.cpd.CustomizePerspectiveDialog.DynamicContributionItem;
import org.eclipse.ui.internal.dialogs.cpd.CustomizePerspectiveDialog.ShortcutItem;
import org.eclipse.ui.internal.dialogs.cpd.TreeManager.TreeItem;
import org.eclipse.ui.internal.keys.BindingService;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.IBindingService;

/**
 * A tooltip with useful information based on the type of ContributionItem
 * the cursor hovers over in a Tree. In addition to the content provided by
 * the {@link NameAndDescriptionToolTip} this includes action set
 * information and key binding data.
 *
 * @since 3.5
 */
class ItemDetailToolTip extends NameAndDescriptionToolTip {
	private Tree tree;
	private boolean showActionSet;
	private boolean showKeyBindings;
	private ViewerFilter filter;
	private TreeViewer v;
	private CustomizePerspectiveDialog dialog;

	/**
	 * @param dialog
	 * @param tree
	 *            The tree for the tooltip to hover over
	 */
	ItemDetailToolTip(CustomizePerspectiveDialog dialog, TreeViewer v, Tree tree, boolean showActionSet,
			boolean showKeyBindings, ViewerFilter filter) {
		super(tree,NO_RECREATE);
		this.dialog = dialog;
		this.tree = tree;
		this.v = v;
		this.showActionSet = showActionSet;
		this.showKeyBindings = showKeyBindings;
		this.filter = filter;
		this.setHideOnMouseDown(false);
	}

	@Override
	public Point getLocation(Point tipSize, Event event) {
		// try to position the tooltip at the bottom of the cell
		ViewerCell cell = v.getCell(new Point(event.x, event.y));

		if( cell != null ) {
			return tree.toDisplay(event.x,cell.getBounds().y+cell.getBounds().height);
		}

		return super.getLocation(tipSize, event);
	}

	@Override
	protected Object getToolTipArea(Event event) {
		// Ensure that the tooltip is hidden when the cell is left
		return v.getCell(new Point(event.x, event.y));
	}

	@Override
	protected void addContent(Composite destination, Object modelElement) {
		final DisplayItem item = (DisplayItem) modelElement;

		// Show any relevant action set info
		if (showActionSet) {
			String text = null;
			Image image = null;

			if(CustomizePerspectiveDialog.isEffectivelyAvailable(item, filter)) {
				if(item.actionSet != null) {
					//give information on which command group the item is in

					final String actionSetName = item.getActionSet().descriptor
							.getLabel();

					text = NLS.bind(
							WorkbenchMessages.HideItems_itemInActionSet,
							actionSetName);
				}
			} else {
				//give feedback on why item is unavailable

				image = dialog.warningImageDescriptor.createImage();

				if (item.getChildren().isEmpty() && item.getActionSet() != null) {
					//i.e. is a leaf

					final String actionSetName = item.getActionSet().
							descriptor.getLabel();

					text = NLS.bind(
							WorkbenchMessages.HideItems_itemInUnavailableActionSet,
							actionSetName);

				} else if (item.getChildren().isEmpty() && item.getActionSet() == null
						&& item.getIContributionItem() instanceof HandledContributionItem) {
					text = WorkbenchMessages.HideItems_itemInUnavailableCommand;
				} else {
					//i.e. has children
					Set<ActionSet> actionGroup = new LinkedHashSet<>();
					ItemDetailToolTip.collectDescendantCommandGroups(actionGroup, item,
							filter);

					if (actionGroup.size() == 1) {
						//i.e. only one child
						ActionSet actionSet = actionGroup.
								iterator().next();
						text = NLS.bind(
								WorkbenchMessages.HideItems_unavailableChildCommandGroup,
								actionSet.descriptor.getId(),
								actionSet.descriptor.getLabel());
					} else {
						//i.e. multiple children
						String commandGroupList = null;

						for (ActionSet actionSet : actionGroup) {
							// For each action set, make a link for it, set
							// the href to its id
							String commandGroupLink = MessageFormat.format(
									"<a href=\"{0}\">{1}</a>", //$NON-NLS-1$
									actionSet.descriptor.getId(), actionSet.descriptor.getLabel());

							if (commandGroupList == null) {
								commandGroupList = commandGroupLink;
							} else {
								commandGroupList = Util.createList(
										commandGroupList, commandGroupLink);
							}
						}

						commandGroupList = NLS.bind(
								"{0}{1}", new Object[] { CustomizePerspectiveDialog.NEW_LINE, commandGroupList }); //$NON-NLS-1$
						text = NLS.bind(
								WorkbenchMessages.HideItems_unavailableChildCommandGroups,
								commandGroupList);
					}
				}
			}

			if(text != null) {
				Link link = createEntryWithLink(destination, image, text);
				link.addSelectionListener(new SelectionListener() {
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
						widgetSelected(e);
					}

					@Override
					public void widgetSelected(SelectionEvent e) {
						ActionSet actionSet = dialog.idToActionSet.get(e.text);
						if (actionSet == null) {
							hide();
							dialog.showActionSet(item);
						} else {
							hide();
							dialog.showActionSet(actionSet);
						}
					}
				});
			}
		}

		// Show key binding info
		if (showKeyBindings && CustomizePerspectiveDialog.getCommandID(item) != null) {
			// See if there is a command associated with the command id
			ICommandService commandService = dialog.window
					.getService(ICommandService.class);
			Command command = commandService.getCommand(CustomizePerspectiveDialog.getCommandID(item));

			if (command != null && command.isDefined()) {
				// Find the bindings and list them as a string
				Binding[] bindings = ItemDetailToolTip.getKeyBindings(dialog.window, item);
				String keybindings = ItemDetailToolTip.keyBindingsAsString(bindings);

				String text = null;

				// Is it possible for this item to be visible?
				final boolean available = (item.getActionSet() == null)
						|| (item.getActionSet().isActive());

				if (bindings.length > 0) {
					if (available) {
						text = NLS.bind(
								WorkbenchMessages.HideItems_keyBindings,
								keybindings);
					} else {
						text = NLS
								.bind(
										WorkbenchMessages.HideItems_keyBindingsActionSetUnavailable,
										keybindings);
					}
				} else {
					if (available) {
						text = WorkbenchMessages.HideItems_noKeyBindings;
					} else {
						text = WorkbenchMessages.HideItems_noKeyBindingsActionSetUnavailable;
					}
				}

				// Construct link to go to the preferences page for key
				// bindings
				final Object highlight;
				if (bindings.length == 0) {
					Map<String, String> parameters = new HashMap<>();

					// If item is a shortcut, need to add a parameter to go
					// to
					// the correct item
					if (item instanceof ShortcutItem) {
						if (CustomizePerspectiveDialog.isNewWizard(item)) {
							parameters.put(
											IWorkbenchCommandConstants.FILE_NEW_PARM_WIZARDID,
									CustomizePerspectiveDialog.getParamID(item));
						} else if (CustomizePerspectiveDialog.isShowPerspective(item)) {
							parameters
									.put(
											IWorkbenchCommandConstants.PERSPECTIVES_SHOW_PERSPECTIVE_PARM_ID,
											CustomizePerspectiveDialog.getParamID(item));
						} else if (CustomizePerspectiveDialog.isShowView(item)) {
							parameters.put(
									IWorkbenchCommandConstants.VIEWS_SHOW_VIEW_PARM_ID,
									CustomizePerspectiveDialog.getParamID(item));
						}
					}

					ParameterizedCommand pc = ParameterizedCommand
							.generateCommand(command, parameters);
					highlight = pc;
				} else {
					highlight = bindings[0];
				}

				Link bindingLink = createEntryWithLink(destination, null,
						text);

				bindingLink.addSelectionListener(new SelectionListener() {
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
						widgetSelected(e);
					}

					@Override
					public void widgetSelected(SelectionEvent e) {
						PreferenceDialog pdialog = PreferencesUtil.createPreferenceDialogOn(dialog.getShell(),
										CustomizePerspectiveDialog.KEYS_PREFERENCE_PAGE_ID,
										new String[0], highlight);
						hide();
						pdialog.open();
					}
				});
			}
		}

		// Show dynamic menu item info
		if (item instanceof DynamicContributionItem) {
			DynamicContributionItem dynamic = ((DynamicContributionItem) item);
			StringBuffer text = new StringBuffer();
			final List<MenuItem> currentItems = dynamic.getCurrentItems();

			if (currentItems.size() > 0) {
				// Create a list of the currently displayed items
				text.append(WorkbenchMessages.HideItems_dynamicItemList);
				for (MenuItem menuItem : currentItems) {
					text.append(CustomizePerspectiveDialog.NEW_LINE).append("- ") //$NON-NLS-1$
							.append(menuItem.getText());
				}
			} else {
				text
						.append(WorkbenchMessages.HideItems_dynamicItemEmptyList);
			}
			createEntry(destination, null, text.toString());
		}
	}

	@Override
	protected Object getModelElement(Event event) {
		org.eclipse.swt.widgets.TreeItem treeItem = tree.getItem(new Point(
				event.x, event.y));
		if (treeItem == null) {
			return null;
		}
		return treeItem.getData();
	}

	/**
	 * @param collection
	 *            a collection, into which all command groups (action sets)
	 *            which contribute <code>item</code> or its descendants will be
	 *            placed
	 * @param item
	 *            the item to collect descendants of
	 * @param filter
	 *            the filter currently being used
	 */
	static void collectDescendantCommandGroups(Collection<ActionSet> collection,
			DisplayItem item, ViewerFilter filter) {
		List<TreeItem> children = item.getChildren();
		for (TreeItem treeItem : children) {
			DisplayItem child = (DisplayItem) treeItem;
			if ((filter == null || filter.select(null, null, child))
					&& child.getActionSet() != null) {
				collection.add(child.getActionSet());
			}
			collectDescendantCommandGroups(collection, child, filter);
		}
	}

	/**
	 * @param bindings
	 * @return a String representing the key bindings in <code>bindings</code>
	 */
	static String keyBindingsAsString(Binding[] bindings) {
		String keybindings = null;
		for (int i = 0; i < bindings.length; i++) {
			// Unfortunately, bindings may be reported more than once:
			// check to see if this one has already been recorded.
			boolean alreadyRecorded = false;
			for (int j = 0; j < i && !alreadyRecorded; j++) {
				if (bindings[i].getTriggerSequence().equals(
						bindings[j].getTriggerSequence())) {
					alreadyRecorded = true;
				}
			}
			if (!alreadyRecorded) {
				String keybinding = bindings[i].getTriggerSequence().format();
				if (i == 0) {
					keybindings = keybinding;
				} else {
					keybindings = Util.createList(keybindings, keybinding);
				}
			}
		}
		return keybindings;
	}

	/**
	 * Gets the keybindings associated with a ContributionItem.
	 */
	static Binding[] getKeyBindings(WorkbenchWindow window, DisplayItem item) {
		IBindingService bindingService = window.getService(IBindingService.class);

		if (!(bindingService instanceof BindingService)) {
			return new Binding[0];
		}

		String id = CustomizePerspectiveDialog.getCommandID(item);
		String param = CustomizePerspectiveDialog.getParamID(item);

		BindingManager bindingManager = ((BindingService) bindingService)
				.getBindingManager();

		Collection<?> allBindings = bindingManager
				.getActiveBindingsDisregardingContextFlat();

		List<Binding> foundBindings = new ArrayList<>(2);

		for (Object name : allBindings) {
			Binding binding = (Binding) name;
			if (binding.getParameterizedCommand() == null) {
				continue;
			}
			if (binding.getParameterizedCommand().getId() == null) {
				continue;
			}
			if (binding.getParameterizedCommand().getId().equals(id)) {
				if (param == null) {
					// We found it!
					foundBindings.add(binding);
				} else {
					// command parameters are only used in the shortcuts
					Map<?, ?> m = binding.getParameterizedCommand().getParameterMap();
					String key = null;
					if (CustomizePerspectiveDialog.isNewWizard(item)) {
						key = IWorkbenchCommandConstants.FILE_NEW_PARM_WIZARDID;
					} else if (CustomizePerspectiveDialog.isShowView(item)) {
						key = IWorkbenchCommandConstants.VIEWS_SHOW_VIEW_PARM_ID;
					} else if (CustomizePerspectiveDialog.isShowPerspective(item)) {
						key = IWorkbenchCommandConstants.PERSPECTIVES_SHOW_PERSPECTIVE_PARM_ID;
					}

					if (key != null) {
						if (param.equals(m.get(key))) {
							foundBindings.add(binding);
						}
					}
				}
			}
		}

		Binding[] bindings = foundBindings
				.toArray(new Binding[foundBindings.size()]);

		return bindings;
	}
}