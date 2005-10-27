/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.menus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.menus.LeafLocationElement;
import org.eclipse.jface.menus.LocationElement;
import org.eclipse.jface.menus.SBar;
import org.eclipse.jface.menus.SItem;
import org.eclipse.jface.menus.SLocation;
import org.eclipse.jface.menus.SOrder;
import org.eclipse.jface.menus.SPart;
import org.eclipse.jface.menus.SPopup;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.commands.CommonCommandPersistence;
import org.eclipse.ui.menus.IMenuService;

/**
 * <p>
 * A static class for accessing the registry.
 * </p>
 * 
 * @since 3.2
 */
final class MenuPersistence extends CommonCommandPersistence {

	/**
	 * The name of the class attribute.
	 */
	private static final String ATTRIBUTE_CLASS = "class"; //$NON-NLS-1$

	/**
	 * The name of the image style attribute, which is used on location elements
	 * in the menus extension point.
	 */
	private static final String ATTRIBUTE_IMAGE_STYLE = "imageStyle"; //$NON-NLS-1$

	/**
	 * The name of the menu identifier attribute, which appears on items.
	 */
	private static final String ATTRIBUTE_MENU_ID = "menuId"; //$NON-NLS-1$

	/**
	 * The name of the mnemonic attribute, which appears on locations.
	 */
	private static final String ATTRIBUTE_MNEMONIC = "mnemonic"; //$NON-NLS-1$

	/**
	 * The name of the position attribute, which appears on order elements.
	 */
	private static final String ATTRIBUTE_POSITION = "position"; //$NON-NLS-1$

	/**
	 * The name of the path attribute, which appears on bar and path elements.
	 */
	private static final String ATTRIBUTE_PATH = "path"; //$NON-NLS-1$

	/**
	 * The name of the relativeTo attribute, which appears on order elements.
	 */
	private static final String ATTRIBUTE_RELATIVE_TO = "relativeTo"; //$NON-NLS-1$

	/**
	 * The name of the type attribute, which appears on bar elements.
	 */
	private static final String ATTRIBUTE_TYPE = "type"; //$NON-NLS-1$

	/**
	 * The name of the element storing an action set.
	 */
	private static final String ELEMENT_ACTION_SET = "actionSet"; //$NON-NLS-1$

	/**
	 * The name of the bar element, which appears in a location definition.
	 */
	private static final String ELEMENT_BAR = "bar"; //$NON-NLS-1$

	/**
	 * The name of the element storing a group.
	 */
	private static final String ELEMENT_GROUP = "group"; //$NON-NLS-1$

	/**
	 * The name of the element storing an item.
	 */
	private static final String ELEMENT_ITEM = "item"; //$NON-NLS-1$

	/**
	 * The name of the element storing a location.
	 */
	private static final String ELEMENT_LOCATION = "location"; //$NON-NLS-1$

	/**
	 * The name of the element storing a menu.
	 */
	private static final String ELEMENT_MENU = "menu"; //$NON-NLS-1$

	/**
	 * The name of the element storing the ordering information.
	 */
	private static final String ELEMENT_ORDER = "order"; //$NON-NLS-1$

	/**
	 * The name of the element storing the visible when condition.
	 */
	private static final String ELEMENT_VISIBLE_WHEN = "visibleWhen"; //$NON-NLS-1$

	/**
	 * The name of the element storing a widget.
	 */
	private static final String ELEMENT_WIDGET = "widget"; //$NON-NLS-1$

	/**
	 * The name of the <code>org.eclipse.ui.menus</code> extension point.
	 */
	private static final String EXTENSION_MENUS = PlatformUI.PLUGIN_ID + '.'
			+ IWorkbenchConstants.PL_MENUS;

	/**
	 * The index of the action set elements in the indexed array.
	 * 
	 * @see MenuPersistence#read(IMenuService, ICommandService)
	 */
	private static final int INDEX_ACTION_SETS = 0;

	/**
	 * The index of the group elements in the indexed array.
	 * 
	 * @see MenuPersistence#read(IMenuService, ICommandService)
	 */
	private static final int INDEX_GROUPS = 1;

	/**
	 * The index of the item elements in the indexed array.
	 * 
	 * @see MenuPersistence#read(IMenuService, ICommandService)
	 */
	private static final int INDEX_ITEMS = 2;

	/**
	 * The index of the menu elements in the indexed array.
	 * 
	 * @see MenuPersistence#read(IMenuService, ICommandService)
	 */
	private static final int INDEX_MENUS = 3;

	/**
	 * The index of the widget elements in the indexed array.
	 * 
	 * @see MenuPersistence#read(IMenuService, ICommandService)
	 */
	private static final int INDEX_WIDGETS = 4;

	/**
	 * The type of bar which references the menu bar.
	 */
	private static final String TYPE_MENU = "menu"; //$NON-NLS-1$

	/**
	 * The type of bar which references the status line.
	 */
	private static final String TYPE_STATUS = "status"; //$NON-NLS-1$

	/**
	 * The type of bar which reference the tool bar.
	 */
	private static final String TYPE_TOOL = "tool"; //$NON-NLS-1$

	/**
	 * The menu contributions that have come from the registry. This is used to
	 * flush the contributions when the registry is re-read. This value is never
	 * <code>null</code>
	 */
	private static final Collection menuContributions = new ArrayList();

	/**
	 * The constant for the position attribute corresponding to
	 * {@link SOrder#POSITION_AFTER}.
	 */
	private static final String POSITION_AFTER = "after"; //$NON-NLS-1$

	/**
	 * The constant for the position attribute corresponding to
	 * {@link SOrder#POSITION_BEFORE}.
	 */
	private static final String POSITION_BEFORE = "before"; //$NON-NLS-1$

	/**
	 * The constant for the position attribute corresponding to
	 * {@link SOrder#POSITION_END}.
	 */
	private static final String POSITION_END = "end"; //$NON-NLS-1$

	/**
	 * The constant for the position attribute corresponding to
	 * {@link SOrder#POSITION_START}.
	 */
	private static final String POSITION_START = "start"; //$NON-NLS-1$

	/**
	 * Removes all of the contributions made by this class, and then clears the
	 * collection. This should be called before every read.
	 * 
	 * @param menuService
	 *            The service handling the contributions; must not be
	 *            <code>null</code>.
	 */
	private static final void clearContributions(final IMenuService menuService) {
		menuService.removeContributions(menuContributions);
		menuContributions.clear();
	}

	/**
	 * Reads the bar element from a location or part element.
	 * 
	 * @param parentElement
	 *            The parent element from which to read; must not be
	 *            <code>null</code>.
	 * @param warningsToLog
	 *            The list of the warnings to log; must not be <code>null</code>.
	 * @param id
	 *            The identifier of the menu element, for logging purposes; may
	 *            be <code>null</code>.
	 * @return The bar element, if any; <code>null</code> if none.
	 */
	private static final SBar readBar(
			final IConfigurationElement parentElement,
			final List warningsToLog, final String id) {
		// Check to see if we have a bar element.
		final IConfigurationElement[] barElements = parentElement
				.getChildren(ELEMENT_BAR);
		if (barElements.length > 0) {
			// Check if we have too many bar elements.
			if (barElements.length > 1) {
				// There should only be one bar element
				addWarning(warningsToLog,
						"Location elements should only have one bar element", //$NON-NLS-1$
						parentElement.getNamespace(), id);
				return null;
			}

			final IConfigurationElement barElement = barElements[0];

			// Read the type attribute.
			final String type = readRequired(barElement, ATTRIBUTE_TYPE,
					warningsToLog, "Bar elements require a type element", id); //$NON-NLS-1$
			final int typeInteger;
			if (TYPE_MENU.equals(type)) {
				typeInteger = SBar.TYPE_MENU;
			} else if (TYPE_TOOL.equals(type)) {
				typeInteger = SBar.TYPE_TOOL;
			} else if (TYPE_STATUS.equals(type)) {
				typeInteger = SBar.TYPE_STATUS;
			} else {
				// The position was not understood.
				addWarning(warningsToLog, "The bar type was not understood", //$NON-NLS-1$
						parentElement.getNamespace(), id, "type", //$NON-NLS-1$
						type);
				return null;
			}

			// Read the path attribute.
			final String path = readOptional(barElement, ATTRIBUTE_PATH);

			return new SBar(typeInteger, path);
		}

		return null;
	}

	/**
	 * Reads the items from an array of item elements from the menus extension
	 * point.
	 * 
	 * @param configurationElements
	 *            The configuration elements in the extension point; must not be
	 *            <code>null</code>, but may be empty.
	 * @param configurationElementCount
	 *            The number of configuration elements that are really in the
	 *            array.
	 * @param menuService
	 *            The menu service to which the items should be added; must not
	 *            be <code>null</code>.
	 * @param commandService
	 *            The command service providing commands for the workbench; must
	 *            not be <code>null</code>.
	 */
	private static final void readItemsFromExtensionPoint(
			final IConfigurationElement[] configurationElements,
			final int configurationElementCount,
			final IMenuService menuService, final ICommandService commandService) {
		/*
		 * If necessary, this list of status items will be constructed. It will
		 * only contains instances of <code>IStatus</code>.
		 */
		final List warningsToLog = new ArrayList(1);

		for (int i = 0; i < configurationElementCount; i++) {
			final IConfigurationElement configurationElement = configurationElements[i];

			// Read the item identifier.
			final String id = readRequired(configurationElement, ATTRIBUTE_ID,
					warningsToLog, "Items need an id"); //$NON-NLS-1$
			if (id == null)
				continue;

			// Read the parameterized command.
			final ParameterizedCommand command = readParameterizedCommand(
					configurationElement, commandService, warningsToLog,
					"Items need a command id", id); //$NON-NLS-1$

			// Read the menu identifier.
			final String menuId = readOptional(configurationElement,
					ATTRIBUTE_MENU_ID);

			// Read out the visibleWhen expression.
			final Expression visibleWhenExpression = readWhenElements(
					configurationElement, ELEMENT_VISIBLE_WHEN, id,
					warningsToLog);

			// Read out the location elements.
			final SLocation[] locations = readLocationElements(
					configurationElement, id, warningsToLog);

			SItem item = menuService.getItem(id);
			item.define(command, menuId, locations);
			menuContributions.add(menuService.contributeMenu(item,
					visibleWhenExpression));
		}

		logWarnings(warningsToLog,
				"Warnings while parsing the items from the registry"); //$NON-NLS-1$
	}

	/**
	 * Reads the <code>location</code> child elements from the given
	 * configuration element. Warnings will be appended to
	 * <code>warningsToLog</code>.
	 * 
	 * @param parentElement
	 *            The configuration element which might have
	 *            <code>location</code> elements as a child; never
	 *            <code>null</code>.
	 * @param id
	 *            The identifier of the menu element whose <code>location</code>
	 *            elements are being read; never <code>null</code>.
	 * @param warningsToLog
	 *            The list of warnings while parsing the extension point; never
	 *            <code>null</code>.
	 * @return The locations for the <code>configurationElement</code>, if
	 *         any; otherwise, <code>null</code>.
	 */
	private static final SLocation[] readLocationElements(
			final IConfigurationElement parentElement, final String id,
			final List warningsToLog) {
		// Check to see if we have an activeWhen expression.
		final IConfigurationElement[] locationElements = parentElement
				.getChildren(ELEMENT_LOCATION);
		if (locationElements.length < 1) {
			return null;
		}

		// Convert the location elements in an SLocation array.
		final Collection locations = new ArrayList(locationElements.length);
		for (int i = 0; i < locationElements.length; i++) {
			final IConfigurationElement locationElement = locationElements[i];

			// Read the mnemonic.
			final String mnemonic = readOptional(locationElement,
					ATTRIBUTE_MNEMONIC);
			final char mnemonicChar;
			if (mnemonic == null) {
				mnemonicChar = SLocation.MNEMONIC_NONE;
			} else if (mnemonic.length() != 1) {
				addWarning(warningsToLog,
						"The mnemonic should only be one character", //$NON-NLS-1$
						parentElement.getNamespace(), id, "mnemonic", //$NON-NLS-1$
						mnemonic);
				mnemonicChar = SLocation.MNEMONIC_NONE;
			} else {
				mnemonicChar = mnemonic.charAt(0);
			}

			// Read the image style.
			final String imageStyle = readOptional(locationElement,
					ATTRIBUTE_IMAGE_STYLE);

			// Read the position and the relativeTo attributes.
			final SOrder ordering = readOrdering(parentElement, id,
					warningsToLog);

			// Read the menu location information.
			final LocationElement menuLocation = readMenuLocation(
					parentElement, warningsToLog, id);
			if (menuLocation == null) {
				continue;
			}

			final SLocation location = new SLocation(mnemonicChar, imageStyle,
					ordering);
			locations.add(location);
		}

		return (SLocation[]) locations.toArray(new SLocation[locations.size()]);
	}

	/**
	 * Reads the location information from a location element. This is either a
	 * <code>bar</code>, <code>part</code>, or <code>popup</code>
	 * element.
	 * 
	 * @param parentElement
	 *            The parent element from which to read; must not be
	 *            <code>null</code>.
	 * @param warningsToLog
	 *            The list of warnings to log at some future point in time; must
	 *            not be <code>null</code>.
	 * @param id
	 *            The identifier of the menu element, for logging purposes; may
	 *            be <code>null</code>.
	 * @return The element providing the location for the menu; may be
	 *         <code>null</code> if none.
	 */
	private static final LocationElement readMenuLocation(
			final IConfigurationElement parentElement,
			final List warningsToLog, final String id) {
		LocationElement locationElement = null;
		locationElement = readBar(parentElement, warningsToLog, id);
		if (locationElement == null) {
			locationElement = readPart(parentElement, warningsToLog, id);
			if (locationElement == null) {
				locationElement = readPopup(parentElement, warningsToLog, id);
			}
		}
		if (locationElement == null) {
			addWarning(warningsToLog,
					"A bar, part or popup element is required", parentElement //$NON-NLS-1$
							.getNamespace(), id);
		}

		return locationElement;
	}

	/**
	 * Reads the ordering information for a location element.
	 * 
	 * @param parentElement
	 *            The location configuration element; must not be
	 *            <code>null</code>.
	 * @param id
	 *            The identifier of the menu element for which the location is
	 *            being read; may be <code>null</code>.
	 * @param warningsToLog
	 *            The collection of warnings to log; must not be
	 *            <code>null</code>.
	 * @return The ordering for this location element; may be <code>null</code>
	 *         if none.
	 */
	private static final SOrder readOrdering(
			final IConfigurationElement parentElement, final String id,
			final List warningsToLog) {
		// Check to see if we have an order element.
		final IConfigurationElement[] orderingElements = parentElement
				.getChildren(ELEMENT_ORDER);
		if (orderingElements.length > 0) {
			// Check if we have too many order elements.
			if (orderingElements.length > 1) {
				// There should only be one order element
				addWarning(warningsToLog,
						"Location elements should only have one order element", //$NON-NLS-1$
						parentElement.getNamespace(), id);
				return null;
			}

			final IConfigurationElement orderingElement = orderingElements[0];

			// Read the position attribute.
			final String position = readRequired(orderingElement,
					ATTRIBUTE_POSITION, warningsToLog,
					"Order elements require a position element", id); //$NON-NLS-1$
			final int positionInteger;
			if (POSITION_AFTER.equals(position)) {
				positionInteger = SOrder.POSITION_AFTER;
			} else if (POSITION_BEFORE.equals(position)) {
				positionInteger = SOrder.POSITION_BEFORE;
			} else if (POSITION_START.equals(position)) {
				positionInteger = SOrder.POSITION_START;
			} else if (POSITION_END.equals(position)) {
				positionInteger = SOrder.POSITION_END;
			} else {
				// The position was not understood.
				addWarning(warningsToLog, "The position was not understood", //$NON-NLS-1$
						parentElement.getNamespace(), id, "position", //$NON-NLS-1$
						position);
				return null;
			}

			// Read the relativeTo attribute.
			String relativeTo = null;
			if ((positionInteger == SOrder.POSITION_AFTER)
					|| (positionInteger == SOrder.POSITION_BEFORE)) {
				relativeTo = readRequired(
						parentElement,
						ATTRIBUTE_RELATIVE_TO,
						warningsToLog,
						"A relativeTo attribute is required is the position is 'after' or 'before'", //$NON-NLS-1$
						id);
			} else {
				// There should be no relativeTo attribute.
				final String value = parentElement
						.getAttribute(ATTRIBUTE_RELATIVE_TO);
				if (value != null) {
					addWarning(warningsToLog,
							"The position was not understood", //$NON-NLS-1$
							parentElement.getNamespace(), id, "position", //$NON-NLS-1$
							position);
					return null;

				}
			}

			return new SOrder(positionInteger, relativeTo);
		}

		return null;
	}

	/**
	 * Reads the part element from a location element.
	 * 
	 * @param parentElement
	 *            The parent element from which to read; must not be
	 *            <code>null</code>.
	 * @param warningsToLog
	 *            The list of the warnings to log; must not be <code>null</code>.
	 * @param id
	 *            The identifier of the menu element, for logging purposes; may
	 *            be <code>null</code>.
	 * @return The part element, if any; <code>null</code> if none.
	 */
	private static final SPart readPart(
			final IConfigurationElement parentElement,
			final List warningsToLog, final String id) {
		// Check to see if we have a part element.
		final IConfigurationElement[] partElements = parentElement
				.getChildren(ELEMENT_BAR);
		if (partElements.length > 0) {
			// Check if we have too many part elements.
			if (partElements.length > 1) {
				// There should only be one part element
				addWarning(warningsToLog,
						"Location elements should only have one part element", //$NON-NLS-1$
						parentElement.getNamespace(), id);
				return null;
			}

			final IConfigurationElement partElement = partElements[0];

			// Read the leaf location element.
			LeafLocationElement leafLocationElement = null;
			leafLocationElement = readBar(parentElement, warningsToLog, id);
			if (leafLocationElement == null) {
				leafLocationElement = readPopup(parentElement, warningsToLog,
						id);
			}
			if (leafLocationElement == null) {
				addWarning(warningsToLog,
						"A bar or popup element is required", parentElement //$NON-NLS-1$
								.getNamespace(), id);
				return null;
			}

			// Read the two optional attributes.
			final String partId = readOptional(partElement, ATTRIBUTE_ID);
			final String clazz = readOptional(partElement, ATTRIBUTE_CLASS);
			if ((partId == null) && (clazz == null)) {
				addWarning(warningsToLog,
						"A part id or a part class is required", parentElement //$NON-NLS-1$
								.getNamespace(), id);
				return null;
			} else if ((partId != null) && (clazz != null)) {
				addWarning(warningsToLog,
						"Only a part id or a part class is allowed, not both", //$NON-NLS-1$
						parentElement.getNamespace(), id);
				return null;
			} else if (partId != null) {
				return new SPart(partId, SPart.TYPE_ID, leafLocationElement);
			} else {
				return new SPart(clazz, SPart.TYPE_CLASS, leafLocationElement);
			}

		}

		return null;
	}

	/**
	 * Reads the popup element from a location or part element.
	 * 
	 * @param parentElement
	 *            The parent element from which to read; must not be
	 *            <code>null</code>.
	 * @param warningsToLog
	 *            The list of the warnings to log; must not be <code>null</code>.
	 * @param id
	 *            The identifier of the menu element, for logging purposes; may
	 *            be <code>null</code>.
	 * @return The popup element, if any; <code>null</code> if none.
	 */
	private static final SPopup readPopup(
			final IConfigurationElement parentElement,
			final List warningsToLog, final String id) {
		// Check to see if we have a popup element.
		final IConfigurationElement[] popupElements = parentElement
				.getChildren(ELEMENT_BAR);
		if (popupElements.length > 0) {
			// Check if we have too many popup elements.
			if (popupElements.length > 1) {
				// There should only be one popup element
				addWarning(warningsToLog,
						"Location elements should only have one popup element", //$NON-NLS-1$
						parentElement.getNamespace(), id);
				return null;
			}

			final IConfigurationElement popupElement = popupElements[0];
			final String popupId = readOptional(popupElement, ATTRIBUTE_ID);
			final String path = readOptional(popupElement, ATTRIBUTE_PATH);
			return new SPopup(popupId, path);
		}

		return null;
	}

	/**
	 * <p>
	 * Reads all of the menu elements and action sets from the registry.
	 * </p>
	 * <p>
	 * TODO Add support for modifications.
	 * </p>
	 * 
	 * @param menuService
	 *            The menu service which should be populated with the values
	 *            from the registry; must not be <code>null</code>.
	 * @param commandService
	 *            The command service which is providing the commands for the
	 *            workbench; must not be <code>null</code>.
	 */
	final void read(final IMenuService menuService,
			final ICommandService commandService) {
		// Create the extension registry mementos.
		final IExtensionRegistry registry = Platform.getExtensionRegistry();
		int itemCount = 0;
		int menuCount = 0;
		int groupCount = 0;
		int widgetCount = 0;
		int actionSetCount = 0;
		final IConfigurationElement[][] indexedConfigurationElements = new IConfigurationElement[5][];

		// Sort the commands extension point based on element name.
		final IConfigurationElement[] menusExtensionPoint = registry
				.getConfigurationElementsFor(EXTENSION_MENUS);
		for (int i = 0; i < menusExtensionPoint.length; i++) {
			final IConfigurationElement configurationElement = menusExtensionPoint[i];
			final String name = configurationElement.getName();

			// Check if it is a handler submission or a command definition.
			if (ELEMENT_ITEM.equals(name)) {
				addElementToIndexedArray(configurationElement,
						indexedConfigurationElements, INDEX_ITEMS, itemCount++);
			} else if (ELEMENT_MENU.equals(name)) {
				addElementToIndexedArray(configurationElement,
						indexedConfigurationElements, INDEX_MENUS, menuCount++);
			} else if (ELEMENT_GROUP.equals(name)) {
				addElementToIndexedArray(configurationElement,
						indexedConfigurationElements, INDEX_GROUPS,
						groupCount++);
			} else if (ELEMENT_WIDGET.equals(name)) {
				addElementToIndexedArray(configurationElement,
						indexedConfigurationElements, INDEX_WIDGETS,
						widgetCount++);
			} else if (ELEMENT_ACTION_SET.equals(name)) {
				addElementToIndexedArray(configurationElement,
						indexedConfigurationElements, INDEX_ACTION_SETS,
						actionSetCount++);
			}
		}

		clearContributions(menuService);
		readItemsFromExtensionPoint(indexedConfigurationElements[INDEX_ITEMS],
				itemCount, menuService, commandService);
		// TODO
		// readMenusFromExtensionPoint(indexedConfigurationElements[INDEX_MENUS],
		// menuCount, menuService);
		// readGroupsFromExtensionPoint(
		// indexedConfigurationElements[INDEX_GROUPS], menuCount,
		// menuService);
		// readWidgetsFromExtensionPoint(
		// indexedConfigurationElements[INDEX_WIDGETS], widgetCount,
		// menuService);
		// readActionSetsFromExtensionPoint(
		// indexedConfigurationElements[INDEX_ACTION_SETS],
		// actionSetCount, menuService);

		/*
		 * Adds listener so that future registry changes trigger an update of
		 * the command manager automatically.
		 */
		if (!listenersAttached) {
			registry.addRegistryChangeListener(new IRegistryChangeListener() {
				public final void registryChanged(
						final IRegistryChangeEvent event) {
					/*
					 * Menus will need to be re-read (i.e., re-verified) if any
					 * of the menu extensions change (i.e., menus), or if any of
					 * the command extensions change (i.e., action definitions).
					 */
					final IExtensionDelta[] menuDeltas = event
							.getExtensionDeltas(PlatformUI.PLUGIN_ID,
									IWorkbenchConstants.PL_MENUS);
					if (menuDeltas.length == 0) {
						return;
					}

					/*
					 * At least one of the deltas is non-zero, so re-read all of
					 * the bindings.
					 */
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							read(menuService, commandService);
						}
					});
				}
			}, PlatformUI.PLUGIN_ID);

			listenersAttached = true;
		}
	}
}
