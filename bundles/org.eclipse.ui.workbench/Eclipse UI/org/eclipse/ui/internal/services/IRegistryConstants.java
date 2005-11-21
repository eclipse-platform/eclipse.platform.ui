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

package org.eclipse.ui.internal.services;

import org.eclipse.jface.menus.SOrder;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchConstants;

/**
 * <p>
 * Common constants for the parsing the registry as it relates to the commands
 * architecture.
 * </p>
 * 
 * @since 3.2
 */
public interface IRegistryConstants {

	/**
	 * The name of the accelerator attribute, which is a (very old) attempt to
	 * binding an accelerator to a command.
	 */
	public static String ATTRIBUTE_ACCELERATOR = "accelerator"; //$NON-NLS-1$

	/**
	 * The name of the adaptable attribute, which appears on object
	 * contributions.
	 */
	public static String ATTRIBUTE_ADAPTABLE = "adaptable"; //$NON-NLS-1$

	/**
	 * The name of the category attribute, which appears on a command
	 * definition.
	 */
	public static String ATTRIBUTE_CATEGORY = "category"; //$NON-NLS-1$

	/**
	 * The name of the category identifier attribute, which appears on a command
	 * definition.
	 */
	public static String ATTRIBUTE_CATEGORY_ID = "categoryId"; //$NON-NLS-1$

	/**
	 * The name of the class attribute, which appears on executable extensions.
	 */
	public static String ATTRIBUTE_CLASS = "class"; //$NON-NLS-1$

	/**
	 * The name of the attribute storing the command id for a binding.
	 */
	public static String ATTRIBUTE_COMMAND = "command"; //$NON-NLS-1$

	/**
	 * The name of the attribute storing the command id.
	 */
	public static String ATTRIBUTE_COMMAND_ID = "commandId"; //$NON-NLS-1$

	/**
	 * The name of the configuration attribute storing the scheme id for a
	 * binding.
	 */
	public static String ATTRIBUTE_CONFIGURATION = "configuration"; //$NON-NLS-1$

	/**
	 * The name of the attribute storing the context id for a binding.
	 */
	public static String ATTRIBUTE_CONTEXT_ID = "contextId"; //$NON-NLS-1$

	/**
	 * The name of the default handler attribute, which appears on a command
	 * definition.
	 */
	public static String ATTRIBUTE_DEFAULT_HANDLER = "defaultHandler"; //$NON-NLS-1$

	/**
	 * The name of the definitionId attribute, which is a reference to a command
	 * identifier on actions.
	 */
	public static String ATTRIBUTE_DEFINITION_ID = "definitionId"; //$NON-NLS-1$

	/**
	 * The name of the description attribute, which appears on named handle
	 * objects.
	 */
	public static String ATTRIBUTE_DESCRIPTION = "description"; //$NON-NLS-1$

	/**
	 * The name of the attribute storing the disabled icon for a command image.
	 */
	public static String ATTRIBUTE_DISABLED_ICON = "disabledIcon"; //$NON-NLS-1$

	/**
	 * The name of the enablesFor attribute, which is a legacy mechanism for
	 * controlling enablement.
	 */
	public static String ATTRIBUTE_ENABLES_FOR = "enablesFor"; //$NON-NLS-1$

	/**
	 * The name of the attribute storing the hover icon for a command image.
	 */
	public static String ATTRIBUTE_HOVER_ICON = "hoverIcon"; //$NON-NLS-1$

	/**
	 * The name of the attribute storing the default icon for a command image.
	 */
	public static String ATTRIBUTE_ICON = "icon"; //$NON-NLS-1$

	/**
	 * The name of the id attribute, which is used on handle objects.
	 */
	public static String ATTRIBUTE_ID = "id"; //$NON-NLS-1$

	/**
	 * The name of the image style attribute, which is used on location elements
	 * in the menus extension point.
	 */
	public static String ATTRIBUTE_IMAGE_STYLE = "imageStyle"; //$NON-NLS-1$

	/**
	 * The name of the attribute storing the identifier for the active key
	 * configuration identifier. This provides legacy support for the
	 * <code>activeKeyConfiguration</code> element in the commands extension
	 * point.
	 */
	public static String ATTRIBUTE_KEY_CONFIGURATION_ID = "keyConfigurationId"; //$NON-NLS-1$

	/**
	 * The name of the attribute storing the trigger sequence for a binding.
	 * This is called a 'keySequence' for legacy reasons.
	 */
	public static String ATTRIBUTE_KEY_SEQUENCE = "keySequence"; //$NON-NLS-1$

	/**
	 * The name of the label attribute, which appears on menus.
	 */
	public static String ATTRIBUTE_LABEL = "label"; //$NON-NLS-1$

	/**
	 * The name of the attribute storing the locale for a binding.
	 */
	public static String ATTRIBUTE_LOCALE = "locale"; //$NON-NLS-1$

	/**
	 * The name of the menu identifier attribute, which appears on items.
	 */
	public static String ATTRIBUTE_MENU_ID = "menuId"; //$NON-NLS-1$

	/**
	 * The name of the toolbarPath attribute, which is a location of an action
	 * in the menu bar.
	 */
	public static String ATTRIBUTE_MENUBAR_PATH = "menubarPath"; //$NON-NLS-1$

	/**
	 * The name of the mnemonic attribute, which appears on locations.
	 */
	public static String ATTRIBUTE_MNEMONIC = "mnemonic"; //$NON-NLS-1$

	/**
	 * The name of the name attribute, which appears on named handle objects
	 */
	public static String ATTRIBUTE_NAME = "name"; //$NON-NLS-1$

	/**
	 * The name of the nameFilter attribute, which appears on object
	 * contributions.
	 */
	public static String ATTRIBUTE_NAME_FILTER = "nameFilter"; //$NON-NLS-1$

	/**
	 * The name of the objectClass attribute, which appears on object
	 * contributions.
	 */
	public static String ATTRIBUTE_OBJECT_CLASS = "objectClass"; //$NON-NLS-1$

	/**
	 * The name of the optional attribute, which appears on parameter
	 * definitions.
	 */
	public static String ATTRIBUTE_OPTIONAL = "optional"; //$NON-NLS-1$

	/**
	 * The name of the deprecated parent attribute, which appears on scheme
	 * definitions.
	 */
	public static String ATTRIBUTE_PARENT = "parent"; //$NON-NLS-1$

	/**
	 * The name of the parent id attribute, which appears on scheme definitions.
	 */
	public static String ATTRIBUTE_PARENT_ID = "parentId"; //$NON-NLS-1$

	/**
	 * The name of the deprecated parent scope attribute, which appears on
	 * contexts definitions.
	 */
	public static String ATTRIBUTE_PARENT_SCOPE = "parentScope"; //$NON-NLS-1$

	/**
	 * The name of the path attribute, which appears on bar and path elements.
	 */
	public static String ATTRIBUTE_PATH = "path"; //$NON-NLS-1$

	/**
	 * The name of the attribute storing the platform for a binding.
	 */
	public static String ATTRIBUTE_PLATFORM = "platform"; //$NON-NLS-1$

	/**
	 * The name of the position attribute, which appears on order elements.
	 */
	public static String ATTRIBUTE_POSITION = "position"; //$NON-NLS-1$

	/**
	 * The name of the pulldown attribute, which indicates whether the class is
	 * a pulldown delegate.
	 */
	public static String ATTRIBUTE_PULLDOWN = "pulldown"; //$NON-NLS-1$

	/**
	 * The name of the relativeTo attribute, which appears on order elements.
	 */
	public static String ATTRIBUTE_RELATIVE_TO = "relativeTo"; //$NON-NLS-1$

	/**
	 * The name of the attribute storing the identifier for the active scheme.
	 * This is called a 'keyConfigurationId' for legacy reasons.
	 */
	public static String ATTRIBUTE_SCHEME_ID = "schemeId"; //$NON-NLS-1$

	/**
	 * The name of the scope attribute for a binding.
	 */
	public static String ATTRIBUTE_SCOPE = "scope"; //$NON-NLS-1$

	/**
	 * The name of the separatorsVisible attribute, which appears on group
	 * elements.
	 */
	public static String ATTRIBUTE_SEPARATORS_VISIBLE = "separatorsVisible"; //$NON-NLS-1$

	/**
	 * The name of the sequence attribute for a key binding.
	 */
	public static String ATTRIBUTE_SEQUENCE = "sequence"; //$NON-NLS-1$

	/**
	 * The name of the state attribute, which is used for radio buttons and
	 * toggle buttons to indicate the initial state.
	 */
	public static String ATTRIBUTE_STATE = "state"; //$NON-NLS-1$

	/**
	 * The name of the string attribute (key sequence) for a binding in the
	 * commands extension point.
	 */
	public static String ATTRIBUTE_STRING = "string"; //$NON-NLS-1$

	/**
	 * The name of the attribute storing the style for a command image, or
	 * storing the legacy widget style for an action.
	 */
	public static String ATTRIBUTE_STYLE = "style"; //$NON-NLS-1$

	/**
	 * The editor that must be active for the editor contribution to appear.
	 */
	public static String ATTRIBUTE_TARGET_ID = "targetID"; //$NON-NLS-1$

	/**
	 * The name of the toolbarPath attribute, which is a location of an action
	 * in the tool bar.
	 */
	public static String ATTRIBUTE_TOOLBAR_PATH = "toolbarPath"; //$NON-NLS-1$

	/**
	 * The name of the tooltip attribute, which equates to the description for a
	 * command.
	 */
	public static String ATTRIBUTE_TOOLTIP = "tooltip"; //$NON-NLS-1$

	/**
	 * The name of the type attribute, which appears on bar elements.
	 */
	public static String ATTRIBUTE_TYPE = "type"; //$NON-NLS-1$

	/**
	 * The name of the value attributed, used in several places.
	 */
	public static String ATTRIBUTE_VALUE = "value"; //$NON-NLS-1$

	/**
	 * The name of the visible attribute, which appears on action set elements.
	 */
	public static String ATTRIBUTE_VISIBLE = "visible"; //$NON-NLS-1$

	/**
	 * The prefix that all auto-generated identifiers start with. This makes the
	 * identifier recognizable as auto-generated, and further helps ensure that
	 * it does not conflict with existing identifiers.
	 */
	public static String AUTOGENERATED_PREFIX = "AUTOGEN:::"; //$NON-NLS-1$

	/**
	 * The name of the deprecated accelerator configuration element. This
	 * element was used in 2.1.x and earlier to define groups of what are now
	 * called schemes.
	 */
	public static String ELEMENT_ACCELERATOR_CONFIGURATION = "acceleratorConfiguration"; //$NON-NLS-1$

	/**
	 * The name of the element storing a deprecated accelerator scope.
	 */
	public static String ELEMENT_ACCELERATOR_SCOPE = "acceleratorScope"; //$NON-NLS-1$

	/**
	 * The name of the deprecated action element.
	 */
	public static String ELEMENT_ACTION = "action"; //$NON-NLS-1$

	/**
	 * The name of the element storing an action definition. This element only
	 * existed in
	 */
	public static String ELEMENT_ACTION_DEFINITION = "actionDefinition"; //$NON-NLS-1$

	/**
	 * The name of the element storing an action set.
	 */
	public static String ELEMENT_ACTION_SET = "actionSet"; //$NON-NLS-1$

	/**
	 * The name of the element storing the active key configuration from the
	 * commands extension point.
	 */
	public static String ELEMENT_ACTIVE_KEY_CONFIGURATION = "activeKeyConfiguration"; //$NON-NLS-1$

	/**
	 * The name of the element storing the active scheme. This is called a
	 * 'keyConfiguration' for legacy reasons.
	 */
	public static String ELEMENT_ACTIVE_SCHEME = ELEMENT_ACTIVE_KEY_CONFIGURATION;

	/**
	 * The name of the active when element, which appears on a handler
	 * definition.
	 */
	public static String ELEMENT_ACTIVE_WHEN = "activeWhen"; //$NON-NLS-1$

	/**
	 * The name of the bar element, which appears in a location definition.
	 */
	public static String ELEMENT_BAR = "bar"; //$NON-NLS-1$

	/**
	 * The name of the element storing the binding. This is called a
	 * 'keyBinding' for legacy reasons.
	 */
	public static String ELEMENT_BINDING = "keyBinding"; //$NON-NLS-1$

	/**
	 * The name of the element storing a category.
	 */
	public static String ELEMENT_CATEGORY = "category"; //$NON-NLS-1$

	/**
	 * The name of the class element, which appears on an executable extension.
	 */
	public static String ELEMENT_CLASS = ATTRIBUTE_CLASS;

	/**
	 * The name of the element storing a command.
	 */
	public static String ELEMENT_COMMAND = "command"; //$NON-NLS-1$

	/**
	 * The name of the element storing a parameter.
	 */
	public static String ELEMENT_COMMAND_PARAMETER = "commandParameter"; //$NON-NLS-1$

	/**
	 * The name of the element storing a context.
	 */
	public static String ELEMENT_CONTEXT = "context"; //$NON-NLS-1$

	/**
	 * The name of the default handler element, which appears on a command
	 * definition.
	 */
	public static String ELEMENT_DEFAULT_HANDLER = ATTRIBUTE_DEFAULT_HANDLER;

	/**
	 * The name of the dynamic menu element, which appears in a group or menu
	 * definition.
	 */
	public static String ELEMENT_DYNAMIC = "dynamic"; //$NON-NLS-1$

	/**
	 * The name of the deprecated editorContribution element. This is used for
	 * contributing actions to the top-level menus and tool bars when particular
	 * editors are visible.
	 */
	public static String ELEMENT_EDITOR_CONTRIBUTION = "editorContribution"; //$NON-NLS-1$

	/**
	 * The name of the enabled when element, which appears on a handler
	 * definition.
	 */
	public static String ELEMENT_ENABLED_WHEN = "enabledWhen"; //$NON-NLS-1$

	/**
	 * The name of the deprecated enablement element.
	 */
	public static String ELEMENT_ENABLEMENT = "enablement"; //$NON-NLS-1$

	/**
	 * The name of the element storing a group.
	 */
	public static String ELEMENT_GROUP = "groupMarker"; //$NON-NLS-1$

	/**
	 * The name of the element storing a handler.
	 */
	public static String ELEMENT_HANDLER = "handler"; //$NON-NLS-1$

	/**
	 * The name of the element storing a handler submission.
	 */
	public static String ELEMENT_HANDLER_SUBMISSION = "handlerSubmission"; //$NON-NLS-1$

	/**
	 * The name of the element storing an image.
	 */
	public static String ELEMENT_IMAGE = "image"; //$NON-NLS-1$

	/**
	 * The name of the element storing an item.
	 */
	public static String ELEMENT_ITEM = "item"; //$NON-NLS-1$

	/**
	 * The name of the element storing a key binding.
	 */
	public static String ELEMENT_KEY = "key"; //$NON-NLS-1$

	/**
	 * The name of the key binding element in the commands extension point.
	 */
	public static String ELEMENT_KEY_BINDING = "keyBinding"; //$NON-NLS-1$

	/**
	 * The name of the deprecated key configuration element in the commands
	 * extension point. This element has been replaced with the scheme element
	 * in the bindings extension point.
	 */
	public static String ELEMENT_KEY_CONFIGURATION = "keyConfiguration"; //$NON-NLS-1$

	/**
	 * The name of the element storing a location.
	 */
	public static String ELEMENT_LOCATION = "location"; //$NON-NLS-1$

	/**
	 * The name of the element storing a menu.
	 */
	public static String ELEMENT_MENU = "menu"; //$NON-NLS-1$

	/**
	 * The name of the element storing an object contribution.
	 */
	public static String ELEMENT_OBJECT_CONTRIBUTION = "objectContribution"; //$NON-NLS-1$

	/**
	 * The name of the element storing the ordering information.
	 */
	public static String ELEMENT_ORDER = "order"; //$NON-NLS-1$

	/**
	 * The name of the element storing a parameter.
	 */
	public static String ELEMENT_PARAMETER = "parameter"; //$NON-NLS-1$

	/**
	 * The name of the element storing the a menu element reference.
	 */
	public static String ELEMENT_REFERENCE = "reference"; //$NON-NLS-1$

	/**
	 * The name of the scheme element in the bindings extension point.
	 */
	public static String ELEMENT_SCHEME = "scheme"; //$NON-NLS-1$

	/**
	 * The name of the element storing a deprecated scope.
	 */
	public static String ELEMENT_SCOPE = "scope"; //$NON-NLS-1$

	/**
	 * The name of the separator element which appears in legacy contributions.
	 */
	public static String ELEMENT_SEPARATOR = "separator"; //$NON-NLS-1$

	/**
	 * The name of the element storing some state.
	 */
	public static String ELEMENT_STATE = "state"; //$NON-NLS-1$

	/**
	 * The name of the element storing the visibility condition for legacy popup
	 * menu contributions.
	 */
	public static String ELEMENT_VISIBILITY = "visibility"; //$NON-NLS-1$

	/**
	 * The name of the element storing the visible when condition.
	 */
	public static String ELEMENT_VISIBLE_WHEN = "visibleWhen"; //$NON-NLS-1$

	/**
	 * The name of the element storing a view contribution.
	 */
	public static String ELEMENT_VIEW_CONTRIBUTION = "viewContribution"; //$NON-NLS-1$

	/**
	 * The name of the element storing a viewer contribution.
	 */
	public static String ELEMENT_VIEWER_CONTRIBUTION = "viewerContribution"; //$NON-NLS-1$

	/**
	 * The name of the element storing a widget.
	 */
	public static String ELEMENT_WIDGET = "widget"; //$NON-NLS-1$

	/**
	 * The name of the deprecated accelerator configurations extension point.
	 */
	public static String EXTENSION_ACCELERATOR_CONFIGURATIONS = PlatformUI.PLUGIN_ID
			+ '.' + IWorkbenchConstants.PL_ACCELERATOR_CONFIGURATIONS;

	/**
	 * The name of the accelerator scopes extension point.
	 */
	public static String EXTENSION_ACCELERATOR_SCOPES = PlatformUI.PLUGIN_ID
			+ '.' + IWorkbenchConstants.PL_ACCELERATOR_SCOPES;

	/**
	 * The name of the action definitions extension point.
	 */
	public static String EXTENSION_ACTION_DEFINITIONS = PlatformUI.PLUGIN_ID
			+ '.' + IWorkbenchConstants.PL_ACTION_DEFINITIONS;

	/**
	 * The name of the <code>org.eclipse.ui.actionSets</code> extension point.
	 */
	public static String EXTENSION_ACTION_SETS = PlatformUI.PLUGIN_ID + '.'
			+ IWorkbenchConstants.PL_ACTION_SETS;

	/**
	 * The name of the bindings extension point.
	 */
	public static String EXTENSION_BINDINGS = PlatformUI.PLUGIN_ID + '.'
			+ IWorkbenchConstants.PL_BINDINGS;

	/**
	 * The name of the commands extension point.
	 */
	public static String EXTENSION_COMMAND_IMAGES = PlatformUI.PLUGIN_ID + '.'
			+ IWorkbenchConstants.PL_COMMAND_IMAGES;

	/**
	 * The name of the commands extension point, and the name of the key for the
	 * commands preferences.
	 */
	public static String EXTENSION_COMMANDS = PlatformUI.PLUGIN_ID + '.'
			+ IWorkbenchConstants.PL_COMMANDS;

	/**
	 * The name of the contexts extension point.
	 */
	public static String EXTENSION_CONTEXTS = PlatformUI.PLUGIN_ID + '.'
			+ IWorkbenchConstants.PL_CONTEXTS;

	/**
	 * The name of the <code>org.eclipse.ui.editorActions</code> extension
	 * point.
	 */
	public static String EXTENSION_EDITOR_ACTIONS = PlatformUI.PLUGIN_ID + '.'
			+ IWorkbenchConstants.PL_EDITOR_ACTIONS;

	/**
	 * The name of the commands extension point.
	 */
	public static String EXTENSION_HANDLERS = PlatformUI.PLUGIN_ID + '.'
			+ IWorkbenchConstants.PL_HANDLERS;

	/**
	 * The name of the <code>org.eclipse.ui.menus</code> extension point.
	 */
	public static String EXTENSION_MENUS = PlatformUI.PLUGIN_ID + '.'
			+ IWorkbenchConstants.PL_MENUS;

	/**
	 * The name of the <code>org.eclipse.ui.popupMenus</code> extension point.
	 */
	public static String EXTENSION_POPUP_MENUS = PlatformUI.PLUGIN_ID + '.'
			+ IWorkbenchConstants.PL_POPUP_MENU;

	/**
	 * The name of the <code>org.eclipse.ui.viewActions</code> extension
	 * point.
	 */
	public static String EXTENSION_VIEW_ACTIONS = PlatformUI.PLUGIN_ID + '.'
			+ IWorkbenchConstants.PL_VIEW_ACTIONS;

	/**
	 * The constant for the position attribute corresponding to
	 * {@link SOrder#POSITION_AFTER}.
	 */
	public static String POSITION_AFTER = "after"; //$NON-NLS-1$

	/**
	 * The constant for the position attribute corresponding to
	 * {@link SOrder#POSITION_BEFORE}.
	 */
	public static String POSITION_BEFORE = "before"; //$NON-NLS-1$

	/**
	 * The constant for the position attribute corresponding to
	 * {@link SOrder#POSITION_END}.
	 */
	public static String POSITION_END = "end"; //$NON-NLS-1$

	/**
	 * The constant for the position attribute corresponding to
	 * {@link SOrder#POSITION_START}.
	 */
	public static String POSITION_START = "start"; //$NON-NLS-1$

	/**
	 * The action style for drop-down menus.
	 */
	public static String STYLE_PULLDOWN = "pulldown"; //$NON-NLS-1$

	/**
	 * The action style for radio buttons.
	 */
	public static String STYLE_RADIO = "radio"; //$NON-NLS-1$

	/**
	 * The action style for check boxes.
	 */
	public static String STYLE_TOGGLE = "toggle"; //$NON-NLS-1$

	/**
	 * The type of reference which refers to a group.
	 */
	public static String TYPE_GROUP = "group"; //$NON-NLS-1$

	/**
	 * The type of reference which refers to an item.
	 */
	public static String TYPE_ITEM = "item"; //$NON-NLS-1$

	/**
	 * The type of bar or reference which refers to the menu.
	 */
	public static String TYPE_MENU = "menu"; //$NON-NLS-1$

	/**
	 * The type of bar which references the status line.
	 */
	public static String TYPE_STATUS = "status"; //$NON-NLS-1$

	/**
	 * The type of bar which reference the tool bar.
	 */
	public static String TYPE_TOOL = "tool"; //$NON-NLS-1$

	/**
	 * The type of reference which refers to the widget.
	 */
	public static String TYPE_WIDGET = "widget"; //$NON-NLS-1$
}
