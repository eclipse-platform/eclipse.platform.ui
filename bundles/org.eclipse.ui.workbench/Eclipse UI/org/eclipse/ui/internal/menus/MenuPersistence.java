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
import org.eclipse.core.commands.common.HandleObject;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.menus.IDynamicMenu;
import org.eclipse.jface.menus.IWidget;
import org.eclipse.jface.menus.LeafLocationElement;
import org.eclipse.jface.menus.LocationElement;
import org.eclipse.jface.menus.SActionSet;
import org.eclipse.jface.menus.SBar;
import org.eclipse.jface.menus.SGroup;
import org.eclipse.jface.menus.SItem;
import org.eclipse.jface.menus.SLocation;
import org.eclipse.jface.menus.SMenu;
import org.eclipse.jface.menus.SOrder;
import org.eclipse.jface.menus.SPart;
import org.eclipse.jface.menus.SPopup;
import org.eclipse.jface.menus.SReference;
import org.eclipse.jface.menus.SWidget;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.services.RegistryPersistence;
import org.eclipse.ui.menus.IMenuService;

/**
 * <p>
 * A static class for accessing the registry.
 * </p>
 * 
 * @since 3.2
 */
final class MenuPersistence extends RegistryPersistence {

	/**
	 * The index of the action set elements in the indexed array.
	 * 
	 * @see MenuPersistence#read()
	 */
	private static final int INDEX_ACTION_SETS = 0;

	/**
	 * The index of the group elements in the indexed array.
	 * 
	 * @see MenuPersistence#read()
	 */
	private static final int INDEX_GROUPS = 1;

	/**
	 * The index of the item elements in the indexed array.
	 * 
	 * @see MenuPersistence#read()
	 */
	private static final int INDEX_ITEMS = 2;

	/**
	 * The index of the menu elements in the indexed array.
	 * 
	 * @see MenuPersistence#read()
	 */
	private static final int INDEX_MENUS = 3;

	/**
	 * The index of the widget elements in the indexed array.
	 * 
	 * @see MenuPersistence#read()
	 */
	private static final int INDEX_WIDGETS = 4;

	/**
	 * The menu contributions that have come from the registry. This is used to
	 * flush the contributions when the registry is re-read. This value is never
	 * <code>null</code>
	 */
	private static final Collection menuContributions = new ArrayList();

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
	 * Reads the action sets from an array of action set elements from the menus
	 * extension point.
	 * 
	 * @param configurationElements
	 *            The configuration elements in the extension point; must not be
	 *            <code>null</code>, but may be empty.
	 * @param configurationElementCount
	 *            The number of configuration elements that are really in the
	 *            array.
	 * @param menuService
	 *            The menu service to which the action sets should be added;
	 *            must not be <code>null</code>.
	 */
	private static final void readActionSetsFromRegistry(
			final IConfigurationElement[] configurationElements,
			final int configurationElementCount, final IMenuService menuService) {
		// Undefine all the previous handle objects.
		final HandleObject[] handleObjects = menuService.getDefinedActionSets();
		if (handleObjects != null) {
			for (int i = 0; i < handleObjects.length; i++) {
				handleObjects[i].undefine();
			}
		}

		final List warningsToLog = new ArrayList(1);

		for (int i = 0; i < configurationElementCount; i++) {
			final IConfigurationElement configurationElement = configurationElements[i];

			// Read the menu identifier.
			final String id = readRequired(configurationElement, ATTRIBUTE_ID,
					warningsToLog, "Action sets need an id"); //$NON-NLS-1$
			if (id == null)
				continue;

			// Read the label.
			final String label = readRequired(configurationElement,
					ATTRIBUTE_LABEL, warningsToLog, "Action sets need a label"); //$NON-NLS-1$
			if (label == null) {
				continue;
			}

			// Read the description.
			final String description = readOptional(configurationElement,
					ATTRIBUTE_DESCRIPTION);

			// Read the whether the action set is visible by default.
			final boolean visible = readBoolean(configurationElement,
					ATTRIBUTE_VISIBLE, false);

			// Read the references.
			final SReference[] references = readReferencesFromRegistry(
					configurationElement, id, warningsToLog);

			final SActionSet actionSet = menuService.getActionSet(id);
			actionSet.define(label, description, visible, references);
		}

		logWarnings(
				warningsToLog,
				"Warnings while parsing the action sets from the 'org.eclipse.ui.menus' extension point"); //$NON-NLS-1$
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
	private static final SBar readBarFromRegistry(
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
						parentElement, id);
				return null;
			}

			final IConfigurationElement barElement = barElements[0];

			// Read the type attribute.
			final String type = readRequired(barElement, ATTRIBUTE_TYPE,
					warningsToLog, "Bar elements require a type element", id); //$NON-NLS-1$
			if (!((SBar.TYPE_MENU.equals(type)) || (SBar.TYPE_TRIM.equals(type)))) {
				// The position was not understood.
				addWarning(warningsToLog, "The bar type was not understood", //$NON-NLS-1$
						parentElement, id, "type", type); //$NON-NLS-1$
				return null;
			}

			// Read the path attribute.
			final String path = readOptional(barElement, ATTRIBUTE_PATH);

			return new SBar(type, path);
		}

		return null;
	}

	/**
	 * Reads the dynamic menu information for a group or menu element.
	 * 
	 * @param parentElement
	 *            The group or menu element; must not be <code>null</code>.
	 * @param id
	 *            The identifier of the menu element for which the location is
	 *            being read; may be <code>null</code>.
	 * @param warningsToLog
	 *            The collection of warnings to log; must not be
	 *            <code>null</code>.
	 * @return The dynamic menu for this menu element; may be <code>null</code>
	 *         if none.
	 */
	private static final IDynamicMenu readDynamicMenuFromRegistry(
			final IConfigurationElement parentElement, final String id,
			final List warningsToLog) {
		// Check to see if we have an element.
		final IConfigurationElement[] dynamicMenuElements = parentElement
				.getChildren(ELEMENT_DYNAMIC);
		if (dynamicMenuElements.length > 0) {
			// Check if we have too many elements.
			if (dynamicMenuElements.length > 1) {
				// There should only be one element
				addWarning(
						warningsToLog,
						"Group and menu elements should only have one dynamic element", //$NON-NLS-1$
						parentElement, id);
				return null;
			}

			final IConfigurationElement dynamicMenuElement = dynamicMenuElements[0];

			if (!checkClass(dynamicMenuElement, warningsToLog,
					"Dynamic menu needs a class", id)) { //$NON-NLS-1$
				return null;
			}

			return new DynamicMenuProxy(dynamicMenuElement, ATTRIBUTE_CLASS);
		}

		return null;
	}

	/**
	 * Reads the groups from an array of group elements from the menus extension
	 * point.
	 * 
	 * @param configurationElements
	 *            The configuration elements in the extension point; must not be
	 *            <code>null</code>, but may be empty.
	 * @param configurationElementCount
	 *            The number of configuration elements that are really in the
	 *            array.
	 * @param menuService
	 *            The menu service to which the groups should be added; must not
	 *            be <code>null</code>.
	 */
	private static final void readGroupsFromRegistry(
			final IConfigurationElement[] configurationElements,
			final int configurationElementCount, final IMenuService menuService) {
		// Undefine all the previous handle objects.
		final HandleObject[] handleObjects = menuService.getDefinedGroups();
		if (handleObjects != null) {
			for (int i = 0; i < handleObjects.length; i++) {
				handleObjects[i].undefine();
			}
		}

		final List warningsToLog = new ArrayList(1);

		for (int i = 0; i < configurationElementCount; i++) {
			final IConfigurationElement configurationElement = configurationElements[i];

			// Read the menu identifier.
			final String id = readRequired(configurationElement, ATTRIBUTE_ID,
					warningsToLog, "Groups need an id"); //$NON-NLS-1$
			if (id == null)
				continue;

			// Read whether the separators are visible.
			final boolean separatorsVisible = readBoolean(configurationElement,
					ATTRIBUTE_SEPARATORS_VISIBLE, true);

			// Read out the visibleWhen expression.
			final Expression visibleWhenExpression = readWhenElement(
					configurationElement, ELEMENT_VISIBLE_WHEN, id,
					warningsToLog);
			if (visibleWhenExpression == ERROR_EXPRESSION) {
				continue;
			}

			// Read out the location elements.
			final SLocation[] locations = readLocationElementsFromRegistry(
					configurationElement, id, warningsToLog);

			// Read out the dynamic elements.
			final IDynamicMenu dynamicMenu = readDynamicMenuFromRegistry(
					configurationElement, id, warningsToLog);

			final SGroup group = menuService.getGroup(id);
			group.define(separatorsVisible, locations, dynamicMenu);
			menuContributions.add(menuService.contributeMenu(group,
					visibleWhenExpression));
		}

		logWarnings(
				warningsToLog,
				"Warnings while parsing the groups from the 'org.eclipse.ui.menus' extension point"); //$NON-NLS-1$
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
	private static final void readItemsFromRegistry(
			final IConfigurationElement[] configurationElements,
			final int configurationElementCount,
			final IMenuService menuService, final ICommandService commandService) {
		// Undefine all the previous handle objects.
		final HandleObject[] handleObjects = menuService.getDefinedItems();
		if (handleObjects != null) {
			for (int i = 0; i < handleObjects.length; i++) {
				handleObjects[i].undefine();
			}
		}

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
			final Expression visibleWhenExpression = readWhenElement(
					configurationElement, ELEMENT_VISIBLE_WHEN, id,
					warningsToLog);
			if (visibleWhenExpression == ERROR_EXPRESSION) {
				continue;
			}

			// Read out the location elements.
			final SLocation[] locations = readLocationElementsFromRegistry(
					configurationElement, id, warningsToLog);

			final SItem item = menuService.getItem(id);
			item.define(command, menuId, locations);
			menuContributions.add(menuService.contributeMenu(item,
					visibleWhenExpression));
		}

		logWarnings(
				warningsToLog,
				"Warnings while parsing the items from the 'org.eclipse.ui.menus' extension point"); //$NON-NLS-1$
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
	private static final SLocation[] readLocationElementsFromRegistry(
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
						parentElement, id, "mnemonic", mnemonic); //$NON-NLS-1$
				mnemonicChar = SLocation.MNEMONIC_NONE;
			} else {
				mnemonicChar = mnemonic.charAt(0);
			}

			// Read the image style.
			final String imageStyle = readOptional(locationElement,
					ATTRIBUTE_IMAGE_STYLE);

			// Read the position and the relativeTo attributes.
			final SOrder ordering = readOrderingFromRegistry(locationElement,
					id, warningsToLog);

			// Read the menu location information.
			final LocationElement menuLocation = readMenuLocationFromRegistry(
					locationElement, warningsToLog, id);
			if (menuLocation == null) {
				continue;
			}

			final SLocation location = new SLocation(menuLocation, ordering,
					mnemonicChar, imageStyle);
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
	private static final LocationElement readMenuLocationFromRegistry(
			final IConfigurationElement parentElement,
			final List warningsToLog, final String id) {
		LocationElement locationElement = null;
		locationElement = readBarFromRegistry(parentElement, warningsToLog, id);
		if (locationElement == null) {
			locationElement = readPartFromRegistry(parentElement,
					warningsToLog, id);
			if (locationElement == null) {
				locationElement = readPopupFromRegistry(parentElement,
						warningsToLog, id);
			}
		}
		if (locationElement == null) {
			addWarning(warningsToLog,
					"A bar, part or popup element is required", parentElement, //$NON-NLS-1$
					id);
		}

		return locationElement;
	}

	/**
	 * Reads the menus from an array of menu elements from the menus extension
	 * point.
	 * 
	 * @param configurationElements
	 *            The configuration elements in the extension point; must not be
	 *            <code>null</code>, but may be empty.
	 * @param configurationElementCount
	 *            The number of configuration elements that are really in the
	 *            array.
	 * @param menuService
	 *            The menu service to which the menus should be added; must not
	 *            be <code>null</code>.
	 */
	private static final void readMenusFromRegistry(
			final IConfigurationElement[] configurationElements,
			final int configurationElementCount, final IMenuService menuService) {
		// Undefine all the previous handle objects.
		final HandleObject[] handleObjects = menuService.getDefinedMenus();
		if (handleObjects != null) {
			for (int i = 0; i < handleObjects.length; i++) {
				handleObjects[i].undefine();
			}
		}

		final List warningsToLog = new ArrayList(1);

		for (int i = 0; i < configurationElementCount; i++) {
			final IConfigurationElement configurationElement = configurationElements[i];

			// Read the menu identifier.
			final String id = readRequired(configurationElement, ATTRIBUTE_ID,
					warningsToLog, "Menus need an id"); //$NON-NLS-1$
			if (id == null)
				continue;

			// Read the label.
			final String label = readOptional(configurationElement,
					ATTRIBUTE_LABEL);

			// Read out the visibleWhen expression.
			final Expression visibleWhenExpression = readWhenElement(
					configurationElement, ELEMENT_VISIBLE_WHEN, id,
					warningsToLog);
			if (visibleWhenExpression == ERROR_EXPRESSION) {
				continue;
			}

			// Read out the location elements.
			final SLocation[] locations = readLocationElementsFromRegistry(
					configurationElement, id, warningsToLog);

			// Read out the dynamic elements.
			final IDynamicMenu dynamicMenu = readDynamicMenuFromRegistry(
					configurationElement, id, warningsToLog);

			final SMenu menu = menuService.getMenu(id);
			menu.define(label, locations, dynamicMenu);
			menuContributions.add(menuService.contributeMenu(menu,
					visibleWhenExpression));
		}

		logWarnings(
				warningsToLog,
				"Warnings while parsing the menus from the 'org.eclipse.ui.menus' extension point"); //$NON-NLS-1$
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
	 * @return The ordering constraint for this location element; may be
	 *         <code>null</code> if none.
	 */
	private static final SOrder readOrderingFromRegistry(
			final IConfigurationElement parentElement, final String id,
			final List warningsToLog) {
		// Check to see if we have an order element.
		final IConfigurationElement[] orderingElements = parentElement
				.getChildren(ELEMENT_ORDER);
		final int length = orderingElements.length;
		if (length < 1) {
			return null;
		} else if (length > 1) {
			addWarning(warningsToLog,
					"There can only be one ordering constraint", parentElement, //$NON-NLS-1$
					id);
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
					parentElement, id, "position", position); //$NON-NLS-1$
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
						"The position was not understood", parentElement, //$NON-NLS-1$
						id, "position", position); //$NON-NLS-1$
				return null;

			}
		}

		final SOrder order = new SOrder(positionInteger, relativeTo);
		return order;
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
	private static final SPart readPartFromRegistry(
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
						parentElement, id);
				return null;
			}

			final IConfigurationElement partElement = partElements[0];

			// Read the leaf location element.
			LeafLocationElement leafLocationElement = null;
			leafLocationElement = readBarFromRegistry(parentElement,
					warningsToLog, id);
			if (leafLocationElement == null) {
				leafLocationElement = readPopupFromRegistry(parentElement,
						warningsToLog, id);
			}
			if (leafLocationElement == null) {
				addWarning(warningsToLog, "A bar or popup element is required", //$NON-NLS-1$
						parentElement, id);
				return null;
			}

			// Read the two optional attributes.
			final String partId = readOptional(partElement, ATTRIBUTE_ID);
			final String clazz = readOptional(partElement, ATTRIBUTE_CLASS);
			if ((partId == null) && (clazz == null)) {
				addWarning(warningsToLog,
						"A part id or a part class is required", parentElement, //$NON-NLS-1$
						id);
				return null;
			} else if ((partId != null) && (clazz != null)) {
				addWarning(warningsToLog,
						"Only a part id or a part class is allowed, not both", //$NON-NLS-1$
						parentElement, id);
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
	private static final SPopup readPopupFromRegistry(
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
						parentElement, id);
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
	 * Reads the <code>reference</code> child elements from the given
	 * configuration element. Warnings will be appended to
	 * <code>warningsToLog</code>.
	 * 
	 * @param parentElement
	 *            The configuration element which might have
	 *            <code>reference</code> elements as a child; never
	 *            <code>null</code>.
	 * @param id
	 *            The identifier of the action set whose <code>reference</code>
	 *            elements are being read; never <code>null</code>.
	 * @param warningsToLog
	 *            The list of warnings while parsing the extension point; never
	 *            <code>null</code>.
	 * @return The references for the <code>configurationElement</code>, if
	 *         any; otherwise, <code>null</code>.
	 */
	private static final SReference[] readReferencesFromRegistry(
			final IConfigurationElement parentElement, final String id,
			final List warningsToLog) {
		final IConfigurationElement[] referenceElements = parentElement
				.getChildren(ELEMENT_REFERENCE);
		if (referenceElements.length < 1) {
			return null;
		}

		final Collection references = new ArrayList(referenceElements.length);
		for (int i = 0; i < referenceElements.length; i++) {
			final IConfigurationElement referenceElement = referenceElements[i];

			// Read the id.
			final String referenceId = readRequired(referenceElement,
					ATTRIBUTE_ID, warningsToLog, "References are required", id); //$NON-NLS-1$
			if (referenceId == null) {
				continue;
			}

			// Read the type attribute.
			final String type = readRequired(referenceElement, ATTRIBUTE_TYPE,
					warningsToLog, "Reference elements require a type", id); //$NON-NLS-1$
			final int typeInteger;
			if (TYPE_ITEM.equals(type)) {
				typeInteger = SReference.TYPE_ITEM;
			} else if (TYPE_MENU.equals(type)) {
				typeInteger = SReference.TYPE_MENU;
			} else if (TYPE_GROUP.equals(type)) {
				typeInteger = SReference.TYPE_GROUP;
			} else if (TYPE_WIDGET.equals(type)) {
				typeInteger = SReference.TYPE_WIDGET;
			} else {
				// The position was not understood.
				addWarning(warningsToLog,
						"The reference type was not understood", parentElement, //$NON-NLS-1$
						id, "type", type); //$NON-NLS-1$
				return null;
			}

			final SReference reference = new SReference(typeInteger,
					referenceId);
			references.add(reference);
		}

		return (SReference[]) references.toArray(new SReference[references
				.size()]);
	}

	/**
	 * Reads the widgets from an array of widget elements from the menus
	 * extension point.
	 * 
	 * @param configurationElements
	 *            The configuration elements in the extension point; must not be
	 *            <code>null</code>, but may be empty.
	 * @param configurationElementCount
	 *            The number of configuration elements that are really in the
	 *            array.
	 * @param menuService
	 *            The menu service to which the widgets should be added; must
	 *            not be <code>null</code>.
	 */
	private static final void readWidgetsFromRegistry(
			final IConfigurationElement[] configurationElements,
			final int configurationElementCount, final IMenuService menuService) {
		// Undefine all the previous handle objects.
		final HandleObject[] handleObjects = menuService.getDefinedWidgets();
		if (handleObjects != null) {
			for (int i = 0; i < handleObjects.length; i++) {
				handleObjects[i].undefine();
			}
		}

		final List warningsToLog = new ArrayList(1);

		for (int i = 0; i < configurationElementCount; i++) {
			final IConfigurationElement configurationElement = configurationElements[i];

			// Read the widget identifier.
			final String id = readRequired(configurationElement, ATTRIBUTE_ID,
					warningsToLog, "Widgets need an id"); //$NON-NLS-1$
			if (id == null)
				continue;

			// Read the widget class.
			if (!checkClass(configurationElement, warningsToLog,
					"Widget needs a class", id)) { //$NON-NLS-1$
				continue;
			}
			final IWidget widgetClass = new WidgetProxy(configurationElement,
					ATTRIBUTE_CLASS);

			// Read out the visibleWhen expression.
			final Expression visibleWhenExpression = readWhenElement(
					configurationElement, ELEMENT_VISIBLE_WHEN, id,
					warningsToLog);
			if (visibleWhenExpression == ERROR_EXPRESSION) {
				continue;
			}

			// Read out the location elements.
			final SLocation[] locations = readLocationElementsFromRegistry(
					configurationElement, id, warningsToLog);

			final SWidget widget = menuService.getWidget(id);
			widget.define(widgetClass, locations);
			menuContributions.add(menuService.contributeMenu(widget,
					visibleWhenExpression));
		}

		logWarnings(
				warningsToLog,
				"Warnings while parsing the items from the 'org.eclipse.ui.menus' extension point"); //$NON-NLS-1$
	}

	/**
	 * The command service which is providing the commands for the workbench;
	 * must not be <code>null</code>.
	 */
	private final ICommandService commandService;

	/**
	 * The menu service which should be populated with the values from the
	 * registry; must not be <code>null</code>.
	 */
	private final IMenuService menuService;

	/**
	 * Constructs a new instance of {@link MenuPersistence}.
	 * 
	 * @param menuService
	 *            The menu service which should be populated with the values
	 *            from the registry; must not be <code>null</code>.
	 * @param commandService
	 *            The command service which is providing the commands for the
	 *            workbench; must not be <code>null</code>.
	 */
	MenuPersistence(final IMenuService menuService,
			final ICommandService commandService) {
		if (menuService == null) {
			throw new NullPointerException("The menu service cannot be null"); //$NON-NLS-1$
		}

		if (commandService == null) {
			throw new NullPointerException("The command service cannot be null"); //$NON-NLS-1$
		}

		this.commandService = commandService;
		this.menuService = menuService;
	}

	public final void dispose() {
		super.dispose();
		clearContributions(menuService);
	}

	protected final boolean isChangeImportant(final IRegistryChangeEvent event) {
		/*
		 * TODO Menus will need to be re-read (i.e., re-verified) if any of the
		 * menu extensions change (i.e., menus), or if any of the command
		 * extensions change (i.e., action definitions).
		 */
		final IExtensionDelta[] menuDeltas = event.getExtensionDeltas(
				PlatformUI.PLUGIN_ID, IWorkbenchConstants.PL_MENUS);
		if (menuDeltas.length == 0) {
			return false;
		}

		return true;
	}

	/**
	 * <p>
	 * Reads all of the menu elements and action sets from the registry.
	 * </p>
	 * <p>
	 * TODO Add support for modifications.
	 * </p>
	 */
	protected final void read() {
		super.read();

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
		readItemsFromRegistry(indexedConfigurationElements[INDEX_ITEMS],
				itemCount, menuService, commandService);
		readMenusFromRegistry(indexedConfigurationElements[INDEX_MENUS],
				menuCount, menuService);
		readGroupsFromRegistry(indexedConfigurationElements[INDEX_GROUPS],
				groupCount, menuService);
		readWidgetsFromRegistry(indexedConfigurationElements[INDEX_WIDGETS],
				widgetCount, menuService);
		readActionSetsFromRegistry(
				indexedConfigurationElements[INDEX_ACTION_SETS],
				actionSetCount, menuService);
	}
}
