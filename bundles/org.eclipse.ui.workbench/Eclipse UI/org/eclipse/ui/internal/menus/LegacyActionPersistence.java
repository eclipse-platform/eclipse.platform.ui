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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IState;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.Scheme;
import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.jface.bindings.keys.KeyLookupFactory;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.commands.CommandImageManager;
import org.eclipse.jface.commands.RadioState;
import org.eclipse.jface.commands.ToggleState;
import org.eclipse.jface.contexts.IContextIds;
import org.eclipse.jface.menus.IMenuStateIds;
import org.eclipse.jface.menus.IWidget;
import org.eclipse.jface.menus.LeafLocationElement;
import org.eclipse.jface.menus.LocationElement;
import org.eclipse.jface.menus.SActionSet;
import org.eclipse.jface.menus.SBar;
import org.eclipse.jface.menus.SItem;
import org.eclipse.jface.menus.SLocation;
import org.eclipse.jface.menus.SMenu;
import org.eclipse.jface.menus.SReference;
import org.eclipse.jface.menus.SWidget;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.SelectionEnabler;
import org.eclipse.ui.commands.ICommandImageService;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.commands.CommonCommandPersistence;
import org.eclipse.ui.internal.handlers.ActionDelegateHandlerProxy;
import org.eclipse.ui.internal.sources.LegacySelectionEnablerExpression;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.ui.menus.IMenuContribution;
import org.eclipse.ui.menus.IMenuService;

/**
 * <p>
 * A static class for reading actions from the registry. Actions were the
 * mechanism in 3.1 and earlier for contributing to menus and tool bars in the
 * Eclipse workbench. They have since been replaced with commands.
 * </p>
 * <p>
 * This class is not intended for use outside of the
 * <code>org.eclipse.ui.workbench</code> plug-in.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public final class LegacyActionPersistence extends CommonCommandPersistence {

	/**
	 * The name of the accelerator attribute, which is a (very old) attempt to
	 * binding an accelerator to a command.
	 */
	private static final String ATTRIBUTE_ACCELERATOR = "accelerator"; //$NON-NLS-1$

	/**
	 * The name of the definitionId attribute, which is a reference to a command
	 * identifier on actions.
	 */
	private static final String ATTRIBUTE_DEFINITION_ID = "definitionId"; //$NON-NLS-1$

	/**
	 * The name of the enablesFor attribute, which is a legacy mechanism for
	 * controlling enablement.
	 */
	private static final String ATTRIBUTE_ENABLES_FOR = "enablesFor"; //$NON-NLS-1$

	/**
	 * The name of the toolbarPath attribute, which is a location of an action
	 * in the menu bar.
	 */
	private static final String ATTRIBUTE_MENUBAR_PATH = "menubarPath"; //$NON-NLS-1$

	/**
	 * The name of the pulldown attribute, which indicates whether the class is
	 * a pulldown delegate.
	 */
	private static final String ATTRIBUTE_PULLDOWN = "pulldown"; //$NON-NLS-1$

	/**
	 * The name of the state attribute, which is used for radio buttons and
	 * toggle buttons to indicate the initial state.
	 */
	private static final String ATTRIBUTE_STATE = "state"; //$NON-NLS-1$

	/**
	 * The editor that must be active for the editor contribution to appear.
	 */
	private static final String ATTRIBUTE_TARGET_ID = "targetID"; //$NON-NLS-1$

	/**
	 * The name of the toolbarPath attribute, which is a location of an action
	 * in the tool bar.
	 */
	private static final String ATTRIBUTE_TOOLBAR_PATH = "toolbarPath"; //$NON-NLS-1$

	/**
	 * The name of the tooltip attribute, which equates to the description for a
	 * command.
	 */
	private static final String ATTRIBUTE_TOOLTIP = "tooltip"; //$NON-NLS-1$

	/**
	 * The prefix that all auto-generated identifiers start with. This makes the
	 * identifier recognizable as auto-generated, and further helps ensure that
	 * it does not conflict with existing identifiers.
	 */
	private static final String AUTOGENERATED_PREFIX = "AUTOGEN:::"; //$NON-NLS-1$

	/**
	 * The name of the deprecated action element.
	 */
	private static final String ELEMENT_ACTION = "action"; //$NON-NLS-1$

	/**
	 * The name of the deprecated editorContribution element. This is used for
	 * contributing actions to the top-level menus and tool bars when particular
	 * editors are visible.
	 */
	private static final String ELEMENT_EDITOR_CONTRIBUTION = "editorContribution"; //$NON-NLS-1$

	/**
	 * The name of the deprecated enablement element.
	 */
	private static final String ELEMENT_ENABLEMENT = "enablement"; //$NON-NLS-1$

	/**
	 * The name of the <code>org.eclipse.ui.actionSets</code> extension point.
	 */
	private static final String EXTENSION_ACTION_SETS = PlatformUI.PLUGIN_ID
			+ '.' + IWorkbenchConstants.PL_ACTION_SETS;

	/**
	 * The name of the <code>org.eclipse.ui.editorActions</code> extension
	 * point.
	 */
	private static final String EXTENSION_EDITOR_ACTIONS = PlatformUI.PLUGIN_ID
			+ '.' + IWorkbenchConstants.PL_EDITOR_ACTIONS;

	/**
	 * The handler activations that have come from the registry. This is used to
	 * flush the activations when the registry is re-read. This value is never
	 * <code>null</code>
	 */
	private static final Collection handlerActivations = new ArrayList();

	/**
	 * The index of the action set elements in the indexed array.
	 * 
	 * @see LegacyActionPersistence#read(ICommandService, IHandlerService,
	 *      BindingManager, ICommandImageService, IMenuService)
	 */
	private static final int INDEX_ACTION_SETS = 0;

	/**
	 * The index of the editor contribution elements in the indexed array.
	 * 
	 * @see LegacyActionPersistence#read(ICommandService, IHandlerService,
	 *      BindingManager, ICommandImageService, IMenuService)
	 */
	private static final int INDEX_EDITOR_CONTRIBUTIONS = 1;

	/**
	 * The menu contributions that have come from the registry. This is used to
	 * flush the contributions when the registry is re-read. This value is never
	 * <code>null</code>
	 */
	private static final Collection menuContributions = new ArrayList();

	/**
	 * The action style for drop-down menus.
	 */
	private static final String STYLE_PULLDOWN = "pulldown"; //$NON-NLS-1$

	/**
	 * The action style for radio buttons.
	 */
	private static final String STYLE_RADIO = "radio"; //$NON-NLS-1$

	/**
	 * The action style for check boxes.
	 */
	private static final String STYLE_TOGGLE = "toggle"; //$NON-NLS-1$

	/**
	 * Deactivates all of the activations made by this class, and then clears
	 * the collection. This should be called before every read.
	 * 
	 * @param handlerService
	 *            The service handling the activations; must not be
	 *            <code>null</code>.
	 */
	private static final void clearActivations(
			final IHandlerService handlerService) {
		handlerService.deactivateHandlers(handlerActivations);
		handlerActivations.clear();
	}

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
	 * Extracts any key bindings from the action. If such a binding exists, it
	 * is added to the binding manager.
	 * 
	 * @param element
	 *            The action from which the binding should be read; must not be
	 *            <code>null</code>.
	 * @param command
	 *            The fully-parameterized command for which a binding should be
	 *            made; must not be <code>null</code>.
	 * @param bindingManager
	 *            The manager to which the binding should be added; must not be
	 *            <code>null</code>.
	 */
	private static final void convertActionToBinding(
			final IConfigurationElement element,
			final ParameterizedCommand command,
			final BindingManager bindingManager) {
		// Figure out which accelerator text to use.
		String acceleratorText = readOptionalFromRegistry(element,
				ATTRIBUTE_ACCELERATOR);
		if (acceleratorText == null) {
			final String label = readOptionalFromRegistry(element,
					ATTRIBUTE_LABEL);
			if (label != null) {
				acceleratorText = LegacyActionTools
						.extractAcceleratorText(label);
			}
		}

		// If there is some accelerator text, generate a key sequence from it.
		if (acceleratorText != null) {
			final IKeyLookup lookup = KeyLookupFactory.getSWTKeyLookup();
			final int acceleratorInt = LegacyActionTools
					.convertAccelerator(acceleratorText);
			final int modifierMask = lookup.getAlt() | lookup.getCommand()
					| lookup.getCtrl() | lookup.getShift();
			final int modifierKeys = acceleratorInt & modifierMask;
			final int naturalKey = acceleratorInt & ~modifierMask;
			final KeyStroke keyStroke = KeyStroke.getInstance(modifierKeys,
					naturalKey);
			final KeySequence keySequence = KeySequence.getInstance(keyStroke);

			final Scheme activeScheme = bindingManager.getActiveScheme();

			final Binding binding = new KeyBinding(keySequence, command,
					activeScheme.getId(), IContextIds.CONTEXT_ID_WINDOW, null,
					null, null, Binding.SYSTEM);
			bindingManager.addBinding(binding);
		}
	}

	/**
	 * Determine which command to use. This is slightly complicated as actions
	 * do not have to have commands, but the new architecture requires it. As
	 * such, we will auto-generate a command for the action if the definitionId
	 * is missing or points to a command that does not yet exist. All such
	 * command identifiers are prefixed with AUTOGENERATED_COMMAND_ID_PREFIX.
	 * 
	 * @param element
	 *            The action element from which a command must be generated;
	 *            must not be <code>null</code>.
	 * @param primaryId
	 *            The primary identifier to use when auto-generating a command;
	 *            must not be <code>null</code>.
	 * @param secondaryId
	 *            The secondary identifier to use when auto-generating a
	 *            command; must not be <code>null</code>.
	 * @param commandManager
	 *            The command manager in which the command should be defined;
	 *            must not be <code>null</code>.
	 * @param warningsToLog
	 *            The collection of warnings logged while reading the extension
	 *            point; must not be <code>null</code>.
	 * @return the fully-parameterized command; <code>null</code> if an error
	 *         occurred.
	 */
	private static final ParameterizedCommand convertActionToCommand(
			final IConfigurationElement element, final String primaryId,
			final String secondaryId, final CommandManager commandManager,
			final List warningsToLog) {
		String commandId = readOptionalFromRegistry(element,
				ATTRIBUTE_DEFINITION_ID);
		Command command = null;
		if (commandId != null) {
			command = commandManager.getCommand(commandId);
		}

		String label = null;
		if ((commandId == null) || (!command.isDefined())) {
			if (commandId == null) {
				commandId = AUTOGENERATED_PREFIX + primaryId + '/'
						+ secondaryId;
			}

			// Read the label attribute.
			label = readRequiredFromRegistry(element, ATTRIBUTE_LABEL,
					warningsToLog, "Actions require a label", commandId); //$NON-NLS-1$
			if (label == null) {
				label = WorkbenchMessages.LegacyActionPersistence_AutogeneratedCommandName;
			}

			/*
			 * Read the tooltip attribute. The tooltip is really the description
			 * of the command.
			 */
			final String tooltip = readOptionalFromRegistry(element,
					ATTRIBUTE_TOOLTIP);

			// Define the command.
			command = commandManager.getCommand(commandId);
			final Category category = commandManager.getCategory(null);
			final String name = LegacyActionTools.removeAcceleratorText(Action
					.removeMnemonics(label));
			command.define(name, tooltip, category, null);

			// TODO Decide the command state.
			final String style = readOptionalFromRegistry(element,
					ATTRIBUTE_STYLE);
			if (STYLE_RADIO.equals(style)) {
				final IState state = new RadioState();
				// TODO How to set the id?
				final boolean checked = readBooleanFromRegistry(element,
						ATTRIBUTE_STATE, false);
				state.setValue((checked) ? Boolean.TRUE : Boolean.FALSE);
				command.addState(IMenuStateIds.STYLE, state);

			} else if (STYLE_TOGGLE.equals(style)) {
				final IState state = new ToggleState();
				final boolean checked = readBooleanFromRegistry(element,
						ATTRIBUTE_STATE, false);
				state.setValue((checked) ? Boolean.TRUE : Boolean.FALSE);
				command.addState(IMenuStateIds.STYLE, state);
			}
		}

		return new ParameterizedCommand(command, null);
	}

	/**
	 * <p>
	 * Extracts the handler information from the given action element. These are
	 * registered with the handler service. They are always active.
	 * </p>
	 * <p>
	 * TODO We need to hook up listeners for all the various things delegates
	 * listen to.
	 * </p>
	 * 
	 * @param element
	 *            The action element from which the handler should be read; must
	 *            not be <code>null</code>.
	 * @param actionId
	 *            The identifier of the action for which a handler is being
	 *            created; must not be <code>null</code>.
	 * @param command
	 *            The command for which this handler applies; must not be
	 *            <code>null</code>.
	 * @param commandManager
	 *            The manager providing the commands; must not be
	 *            <code>null</code>.
	 * @param handlerService
	 *            The handler service with which the action should be
	 *            registered; must not be <code>null</code>.
	 * @param bindingManager
	 *            The manager providing bindings for the command; must not be
	 *            <code>null</code>.
	 * @param commandImageManager
	 *            The image manager providing icons for the command; must not be
	 *            <code>null</code>.
	 * @param style
	 *            The style of images to use for this action; may be
	 *            <code>null</code>.
	 */
	private static final void convertActionToHandler(
			final IConfigurationElement element, final String actionId,
			final ParameterizedCommand command,
			final CommandManager commandManager,
			final IHandlerService handlerService,
			final BindingManager bindingManager,
			final CommandImageManager commandImageManager, final String style) {
		// Read the class attribute.
		final String classString = readOptionalFromRegistry(element,
				ATTRIBUTE_CLASS);
		if (classString == null) {
			return;
		}
		final IHandler handler = new ActionDelegateHandlerProxy(element,
				ATTRIBUTE_CLASS, actionId, command, commandManager,
				bindingManager, commandImageManager, style);

		// Read the enablesFor attribute, and enablement and selection elements.
		SelectionEnabler enabler = null;
		if (element.getAttribute(ATTRIBUTE_ENABLES_FOR) != null) {
			enabler = new SelectionEnabler(element);
		} else {
			IConfigurationElement[] kids = element
					.getChildren(ELEMENT_ENABLEMENT);
			if (kids.length > 0)
				enabler = new SelectionEnabler(element);
		}

		// Activate the handler.
		if (enabler == null) {
			handlerActivations.add(handlerService.activateHandler(command
					.getId(), handler));
		} else {
			final Expression enabledWhenExpression = new LegacySelectionEnablerExpression(
					enabler);
			handlerActivations.add(handlerService.activateHandler(command
					.getId(), handler, enabledWhenExpression));
		}
	}

	/**
	 * Extracts any image definitions from the action. These are defined as
	 * image bindings on the given command with an auto-generated style.
	 * 
	 * @param element
	 *            The action element from which the images should be read; must
	 *            not be <code>null</code>.
	 * @param command
	 *            The command to which the images should be bound; must not be
	 *            <code>null</code>.
	 * @param commandImageManager
	 *            The manager with which the images need to be registered; must
	 *            not be <code>null</code>.
	 * @return The image style used to define these images; may be
	 *         <code>null</code>.
	 */
	private static final String convertActionToImages(
			final IConfigurationElement element,
			final ParameterizedCommand command,
			final CommandImageManager commandImageManager) {
		final String commandId = command.getId();

		// Read the icon attributes.
		final String icon = readOptionalFromRegistry(element, ATTRIBUTE_ICON);
		final String disabledIcon = readOptionalFromRegistry(element,
				ATTRIBUTE_DISABLED_ICON);
		final String hoverIcon = readOptionalFromRegistry(element,
				ATTRIBUTE_HOVER_ICON);

		// Check if at least one is defined.
		if ((icon == null) && (disabledIcon == null) && (hoverIcon == null)) {
			return null;
		}

		final String style = commandImageManager.generateUnusedStyle(commandId);

		// Bind the images.
		if (icon != null) {
			final URL iconURL = BundleUtility
					.find(element.getNamespace(), icon);
			commandImageManager.bind(commandId,
					ICommandImageService.TYPE_DEFAULT, style, iconURL);
		}
		if (disabledIcon != null) {
			final URL disabledIconURL = BundleUtility.find(element
					.getNamespace(), disabledIcon);
			commandImageManager.bind(commandId,
					ICommandImageService.TYPE_DISABLED, style, disabledIconURL);
		}
		if (hoverIcon != null) {
			final URL hoverIconURL = BundleUtility.find(element.getNamespace(),
					hoverIcon);
			commandImageManager.bind(commandId,
					ICommandImageService.TYPE_HOVER, style, hoverIconURL);
		}

		return style;
	}

	/**
	 * Extracts the item part of the action, and registers it with the given
	 * menu service.
	 * 
	 * @param element
	 *            The action element from which the item should be read; must
	 *            not be <code>null</code>.
	 * @param warningsToLog
	 *            The list of warnings logged while parsing this extension
	 *            point; must not be <code>null</code>.
	 * @param command
	 *            The command with which the item should be associated; must not
	 *            be <code>null</code>.
	 * @param imageStyle
	 *            The image style to use; may be <code>null</code>.
	 * @param menuService
	 *            The menu service with which to register; must not be
	 *            <code>null</code>.
	 * @param leadingPart
	 *            The <code>part</code> string for an <code>SPart</code>.
	 *            This value is <code>null</code> if it is not in an
	 *            <code>SPart</code>.
	 * @param locationInfo
	 *            The information required to create the non-leaf portion of the
	 *            location element; may be <code>null</code> if there is no
	 *            non-leaf component.
	 * @param visibleWhenExpression
	 *            The visibility crtieria for the corresponding item; may be
	 *            <code>null</code>.
	 */
	private static final void convertActionToItem(
			final IConfigurationElement element, final List warningsToLog,
			final ParameterizedCommand command, final String imageStyle,
			final IMenuService menuService,
			final LegacyLocationInfo locationInfo,
			final Expression visibleWhenExpression) {
		final String commandId = command.getId();

		// Read the id attribute.
		final String id = readRequiredFromRegistry(element, ATTRIBUTE_ID,
				warningsToLog, "Actions require an id", commandId); //$NON-NLS-1$
		if (id == null) {
			return;
		}

		// Figure out the mnemonic, if any.
		final String label = readOptionalFromRegistry(element, ATTRIBUTE_LABEL);
		final char mnemonic = LegacyActionTools.extractMnemonic(label);

		// Count how many locations there will be.
		final String menubarPath = readOptionalFromRegistry(element,
				ATTRIBUTE_MENUBAR_PATH);
		final String toolbarPath = readOptionalFromRegistry(element,
				ATTRIBUTE_TOOLBAR_PATH);
		int locationCount = 0;
		if (menubarPath != null) {
			locationCount++;
		}
		if (toolbarPath != null) {
			locationCount++;
		}

		// Create the locations.
		final SLocation[] locations;
		if (locationCount == 0) {
			locations = null;
		} else {
			locations = new SLocation[locationCount];
			int i = 0;

			if (menubarPath != null) {
				locations[i++] = createLocation(SBar.TYPE_MENU, menubarPath,
						locationInfo, mnemonic, imageStyle);
			}
			if (toolbarPath != null) {
				locations[i++] = createLocation(SBar.TYPE_TOOL, toolbarPath,
						locationInfo, mnemonic, imageStyle);
			}
		}

		/*
		 * Figure out whether this a pulldown or not. If it is a pulldown, then
		 * we are going to need to make an SWidget rather than an SItem.
		 */
		final String style = readOptionalFromRegistry(element, ATTRIBUTE_STYLE);
		final boolean pulldown = readBooleanFromRegistry(element,
				ATTRIBUTE_PULLDOWN, false);
		if (pulldown || STYLE_PULLDOWN.equals(style)) {
			final SWidget widget = menuService.getWidget(id);
			final IWidget proxy = new PulldownDelegateWidgetProxy(element,
					ATTRIBUTE_CLASS);
			widget.define(proxy, locations);
			// TODO Cannot duplicate the class instance between handler and item
			final IMenuContribution contribution = menuService.contributeMenu(
					widget, visibleWhenExpression);
			menuContributions.add(contribution);

		} else {
			final SItem item = menuService.getItem(id);
			item.define(command, id, locations);
			final IMenuContribution contribution = menuService.contributeMenu(
					item, visibleWhenExpression);
			menuContributions.add(contribution);

		}
	}

	/**
	 * Constructs a new instance of <code>SLocation</code> with the
	 * information provided.
	 * 
	 * @param barType
	 *            The type of <code>SBar</code> to create as the leaf element;
	 *            must be one of the types defined in <code>SBar</code>
	 * @param path
	 *            The path to use with <code>SBar</code>; must not be
	 *            <code>null</code>.
	 * @param locationInfo
	 *            The information needed to construct the non-leaf portion of
	 *            the location; may be <code>null</code>.
	 * @param mnemonic
	 *            The mnemonic; may be {@link SLocation#MNEMONIC_NONE}.
	 * @param imageStyle
	 *            The image style to use; may be <code>null</code>.
	 * @return A location instance encapsulating this information; never
	 *         <code>null</code>.
	 */
	private static final SLocation createLocation(final int barType,
			final String path, final LegacyLocationInfo locationInfo,
			final char mnemonic, final String imageStyle) {
		final LeafLocationElement leafElement = new SBar(SBar.TYPE_MENU, path);
		final LocationElement locationElement;
		if (locationInfo == null) {
			locationElement = leafElement;
		} else {
			locationElement = locationInfo.append(leafElement);
		}
		return new SLocation(mnemonic, imageStyle, null, locationElement);
	}

	/**
	 * Reads all of the action and menu child elements from the given element.
	 * 
	 * @param element
	 *            The configuration element from which the actions and menus
	 *            should be read; must not be <code>null</code>, but may be
	 *            empty.
	 * @param id
	 *            The identifier of the contribution being made. This could be
	 *            an action set, an editor contribution, a view contribution, a
	 *            viewer contribution or an object contribution. This value must
	 *            not be <code>null</code>.
	 * @param warningsToLog
	 *            The list of warnings already logged for this extension point;
	 *            must not be <code>null</code>.
	 * @param commandManager
	 *            The command manager to which the command should be added; must
	 *            not be <code>null</code>.
	 * @param handlerService
	 *            The service to which the handler should be added; must not be
	 *            <code>null</code>.
	 * @param bindingManager
	 *            The binding manager to which the bindings should be added;
	 *            must not be <code>null</code>.
	 * @param commandImageManager
	 *            The command image service to which the images should be added;
	 *            must not be <code>null</code>.
	 * @param menuService
	 *            The menu service to which the menu elements should be added;
	 *            must not be <code>null</code>.
	 * @param locationInfo
	 *            The information required to create the non-leaf portion of the
	 *            location element; may be <code>null</code> if there is no
	 *            non-leaf component.
	 * @param visibleWhenExpression
	 *            The expression controlling visibility of the corresponding
	 *            menu elements; may be <code>null</code>.
	 * @return An array of references to the created menu elements. This value
	 *         may be <code>null</code> if there was a problem parsing the
	 *         configuration element.
	 */
	private static final SReference[] readActionsAndMenusFromRegistry(
			final IConfigurationElement element, final String id,
			final List warningsToLog, final CommandManager commandManager,
			final IHandlerService handlerService,
			final BindingManager bindingManager,
			final CommandImageManager commandImageManager,
			final IMenuService menuService,
			final LegacyLocationInfo locationInfo,
			final Expression visibleWhenExpression) {
		// Read its child elements.
		final IConfigurationElement[] actionElements = element
				.getChildren(ELEMENT_ACTION);
		final SReference[] itemReferences = readActionsFromRegistry(id,
				actionElements, warningsToLog, commandManager, handlerService,
				bindingManager, commandImageManager, menuService, locationInfo,
				visibleWhenExpression);
		if ((itemReferences == null) || (itemReferences.length == 0)) {
			addWarning(warningsToLog,
					"Contributions require one or more actions", //$NON-NLS-1$
					element.getNamespace(), id);
			return null;
		}

		// Read out the menus and groups, if any.
		final IConfigurationElement[] menuElements = element
				.getChildren(ELEMENT_MENU);
		if ((menuElements != null) && (menuElements.length > 0)) {
			final SReference[] menuAndGroupReferences = readMenusAndGroupsFromRegistry(
					menuElements, id, warningsToLog, menuService,
					visibleWhenExpression);
			final SReference[] references = new SReference[itemReferences.length
					+ menuAndGroupReferences.length];
			System.arraycopy(itemReferences, 0, references, 0,
					itemReferences.length);
			System.arraycopy(menuAndGroupReferences, 0, references,
					itemReferences.length, menuAndGroupReferences.length);
			return references;

		}

		// There were neither menus nor groups.
		return itemReferences;
	}

	/**
	 * Reads the deprecated actions from an array of elements from the action
	 * sets extension point.
	 * 
	 * @param configurationElements
	 *            The configuration elements in the extension point; must not be
	 *            <code>null</code>, but may be empty.
	 * @param configurationElementCount
	 *            The number of configuration elements that are really in the
	 *            array.
	 * @param commandManager
	 *            The command manager to which the command should be added; must
	 *            not be <code>null</code>.
	 * @param handlerService
	 *            The service to which the handler should be added; must not be
	 *            <code>null</code>.
	 * @param bindingManager
	 *            The binding manager to which the bindings should be added;
	 *            must not be <code>null</code>.
	 * @param commandImageManager
	 *            The command image service to which the images should be added;
	 *            must not be <code>null</code>.
	 * @param menuService
	 *            The menu service to which the menu elements should be added;
	 *            must not be <code>null</code>.
	 */
	private static final void readActionSetsFromRegistry(
			final IConfigurationElement[] configurationElements,
			final int configurationElementCount,
			final CommandManager commandManager,
			final IHandlerService handlerService,
			final BindingManager bindingManager,
			final CommandImageManager commandImageManager,
			final IMenuService menuService) {
		final List warningsToLog = new ArrayList(1);

		for (int i = 0; i < configurationElementCount; i++) {
			final IConfigurationElement element = configurationElements[i];

			// Read the action set identifier.
			final String id = readRequiredFromRegistry(element, ATTRIBUTE_ID,
					warningsToLog, "Action sets need an id"); //$NON-NLS-1$
			if (id == null)
				continue;

			// Read the label.
			final String label = readRequiredFromRegistry(element,
					ATTRIBUTE_LABEL, warningsToLog, "Actions set need a label", //$NON-NLS-1$
					id);
			if (label == null)
				continue;

			// Read the description.
			final String description = readOptionalFromRegistry(element,
					ATTRIBUTE_DESCRIPTION);

			// Read whether the action set should be visible by default.
			final boolean visible = readBooleanFromRegistry(element,
					ATTRIBUTE_VISIBLE, false);

			// Read all of the child elements.
			final SReference[] references = readActionsAndMenusFromRegistry(
					element, id, warningsToLog, commandManager, handlerService,
					bindingManager, commandImageManager, menuService, null,
					null);
			if (references == null) {
				continue;
			}

			// Define the action set.
			final SActionSet actionSet = menuService.getActionSet(id);
			actionSet.define(label, description, visible, references);
		}

		logWarnings(
				warningsToLog,
				"Warnings while parsing the action sets from the 'org.eclipse.ui.actionSets' extension point"); //$NON-NLS-1$
	}

	/**
	 * Reads the actions, and defines all the necessary subcomponents in terms
	 * of the command architecture. For each action, there could be a command, a
	 * command image binding, a handler and a menu item.
	 * 
	 * @param primaryId
	 *            The identifier of the primary object to which this action
	 *            belongs. This is used to auto-generate command identifiers
	 *            when required. The <code>primaryId</code> must not be
	 *            <code>null</code>.
	 * @param elements
	 *            The action elements to be read; must not be <code>null</code>.
	 * @param warningsToLog
	 *            The collection of warnings while parsing this extension point;
	 *            must not be <code>null</code>.
	 * @param commandManager
	 *            The command manager for the workbench; must not be
	 *            <code>null</code>.
	 * @param handlerService
	 *            The service to which the handler should be added; must not be
	 *            <code>null</code>.
	 * @param bindingManager
	 *            The binding manager for the workbench; must not be
	 *            <code>null</code>.
	 * @param commandImageManager
	 *            The command image manager for the workbench; must not be
	 *            <code>null</code>.
	 * @param menuService
	 *            The menu service for the workbench; must not be
	 *            <code>null</code>.
	 * @param locationInfo
	 *            The information required to create the non-leaf portion of the
	 *            location element; may be <code>null</code> if there is no
	 *            non-leaf component.
	 * @param visibleWhenExpression
	 *            The expression controlling visibility of the corresponding
	 *            menu elements; may be <code>null</code>.
	 * @return References to the created menu elements; may be <code>null</code>,
	 *         and may be empty.
	 */
	private static final SReference[] readActionsFromRegistry(
			final String primaryId, final IConfigurationElement[] elements,
			final List warningsToLog, final CommandManager commandManager,
			final IHandlerService handlerService,
			final BindingManager bindingManager,
			final CommandImageManager commandImageManager,
			final IMenuService menuService,
			final LegacyLocationInfo locationInfo,
			final Expression visibleWhenExpression) {
		final Collection references = new ArrayList(elements.length);
		for (int i = 0; i < elements.length; i++) {
			final IConfigurationElement element = elements[i];

			/*
			 * We might need the identifier to generate the command, so we'll
			 * read it out now.
			 */
			final String id = readRequiredFromRegistry(element, ATTRIBUTE_ID,
					warningsToLog, "Actions require an id"); //$NON-NLS-1$
			if (id == null) {
				continue;
			}

			// Try to break out the command part of the action.
			final ParameterizedCommand command = convertActionToCommand(
					element, primaryId, id, commandManager, warningsToLog);
			if (command == null) {
				continue;
			}

			convertActionToBinding(element, command, bindingManager);
			final String imageStyle = convertActionToImages(element, command,
					commandImageManager);
			convertActionToHandler(element, id, command, commandManager,
					handlerService, bindingManager, commandImageManager,
					imageStyle);
			convertActionToItem(element, warningsToLog, command, imageStyle,
					menuService, locationInfo, visibleWhenExpression);

			references.add(new SReference(SReference.TYPE_ITEM, id));
		}

		return (SReference[]) references.toArray(new SReference[references
				.size()]);
	}

	/**
	 * Reads the deprecated editor contributions from an array of elements from
	 * the editor actions extension point.
	 * 
	 * @param configurationElements
	 *            The configuration elements in the extension point; must not be
	 *            <code>null</code>, but may be empty.
	 * @param configurationElementCount
	 *            The number of configuration elements that are really in the
	 *            array.
	 * @param commandManager
	 *            The command manager to which the command should be added; must
	 *            not be <code>null</code>.
	 * @param handlerService
	 *            The service to which the handler should be added; must not be
	 *            <code>null</code>.
	 * @param bindingManager
	 *            The binding manager to which the bindings should be added;
	 *            must not be <code>null</code>.
	 * @param commandImageManager
	 *            The command image service to which the images should be added;
	 *            must not be <code>null</code>.
	 * @param menuService
	 *            The menu service to which the menu elements should be added;
	 *            must not be <code>null</code>.
	 */
	private static final void readEditorContributionsFromRegistry(
			final IConfigurationElement[] configurationElements,
			final int configurationElementCount,
			final CommandManager commandManager,
			final IHandlerService handlerService,
			final BindingManager bindingManager,
			final CommandImageManager commandImageManager,
			final IMenuService menuService) {
		final List warningsToLog = new ArrayList(1);

		for (int i = 0; i < configurationElementCount; i++) {
			final IConfigurationElement element = configurationElements[i];

			// Read the action set identifier.
			final String id = readRequiredFromRegistry(element, ATTRIBUTE_ID,
					warningsToLog, "Editor contributions need an id"); //$NON-NLS-1$
			if (id == null)
				continue;

			/*
			 * Read the target id. This is the identifier of the editor with
			 * which these contributions are associated.
			 */
			final String targetId = readRequiredFromRegistry(element,
					ATTRIBUTE_TARGET_ID, warningsToLog,
					"Editor contributions need a target id", id); //$NON-NLS-1$
			if (targetId == null)
				continue;
			final Expression visibleWhenExpression = new LegacyEditorContributionExpression(
					targetId);

			// Read all of the child elements from the registry.
			readActionsAndMenusFromRegistry(element, id, warningsToLog,
					commandManager, handlerService, bindingManager,
					commandImageManager, menuService, null,
					visibleWhenExpression);
		}

		logWarnings(
				warningsToLog,
				"Warnings while parsing the editor contributions from the 'org.eclipse.ui.editorActions' extension point"); //$NON-NLS-1$
	}

	/**
	 * 
	 * @param menuElements
	 *            The menu elements to parse; must not be <code>null</code>.
	 * @param contributionId
	 *            The identifier of the contribution being made. This could be
	 *            an action set, an editor contribution, a view contribution, a
	 *            viewer contribution or an object contribution. This value must
	 *            not be <code>null</code>.
	 * @param warningsToLog
	 *            The list of warnings already logged for this extension point;
	 *            must not be <code>null</code>.
	 * @param menuService
	 *            The menu service with which the menu and group elements should
	 *            be registered; must not be <code>null</code>.
	 * @param visibleWhenExpression
	 *            An expression controlling the visibility. If this value is
	 *            <code>null</code>, then the menus and groups are always
	 *            visible.
	 * @return An array of references to the menus and groups created. This
	 *         value is never <code>null</code>.
	 */
	private static final SReference[] readMenusAndGroupsFromRegistry(
			final IConfigurationElement[] menuElements,
			final String contributionId, final List warningsToLog,
			final IMenuService menuService,
			final Expression visibleWhenExpression) {
		final int length = menuElements.length;
		final Collection references = new ArrayList(length);
		for (int i = 0; i < length; i++) {
			final IConfigurationElement menuElement = menuElements[i];

			// Read the id attribute.
			final String menuId = readRequiredFromRegistry(menuElement,
					ATTRIBUTE_ID, warningsToLog,
					"Menus require an id", contributionId); //$NON-NLS-1$
			if (menuId == null) {
				continue;
			}

			// Read the label attribute, and extract the mnemonic.
			String label = readRequiredFromRegistry(menuElement,
					ATTRIBUTE_LABEL, warningsToLog,
					"Menus require a label", menuId); //$NON-NLS-1$
			if (label == null) {
				continue;
			}
			final char mnemonic = LegacyActionTools.extractMnemonic(label);
			label = LegacyActionTools.removeMnemonics(label);

			// Read the path attribute.
			final String path = readOptionalFromRegistry(menuElement,
					ATTRIBUTE_PATH);

			// TODO Read children.

			// Define the menu.
			final SMenu menu = menuService.getMenu(menuId);
			final LocationElement bar = new SBar(SBar.TYPE_MENU, path);
			final SLocation location = new SLocation(mnemonic, null, null, bar);
			final SLocation[] locations = new SLocation[] { location };
			menu.define(label, locations, null);
			final IMenuContribution contribution = menuService.contributeMenu(
					menu, visibleWhenExpression);
			menuContributions.add(contribution);
			references.add(new SReference(SReference.TYPE_MENU, menuId));
		}

		return (SReference[]) references.toArray(new SReference[references
				.size()]);
	}

	/**
	 * Constructs a new instance of <code>DeprecatedActionPersistence</code>.
	 */
	public LegacyActionPersistence() {
		// Does nothing.
	}

	/**
	 * <p>
	 * Reads all of the actions from the deprecated extension points. Actions
	 * have been replaced with commands, command images, handlers, menu elements
	 * and action sets.
	 * </p>
	 * <p>
	 * TODO Before this method is called, all of the extension points must be
	 * cleared.
	 * </p>
	 * 
	 * @param commandManager
	 *            The command manager which is providing the commands for the
	 *            workbench; must not be <code>null</code>.
	 * @param handlerService
	 *            The service to which the handler should be added; must not be
	 *            <code>null</code>.
	 * @param bindingManager
	 *            The binding manager which should be populated with bindings
	 *            from actions; must not be <code>null</code>.
	 * @param commandImageManager
	 *            The command image manager which should be populated with the
	 *            images from the actions; must not be <code>null</code>.
	 * @param menuService
	 *            The menu service which should be populated with the values
	 *            from the registry; must not be <code>null</code>.
	 */
	public final void read(final CommandManager commandManager,
			final IHandlerService handlerService,
			final BindingManager bindingManager,
			final CommandImageManager commandImageManager,
			final IMenuService menuService) {
		// Create the extension registry mementos.
		final IExtensionRegistry registry = Platform.getExtensionRegistry();
		int actionSetCount = 0;
		int editorContributionCount = 0;
		final IConfigurationElement[][] indexedConfigurationElements = new IConfigurationElement[2][];

		// Sort the actionSets extension point.
		final IConfigurationElement[] actionSetsExtensionPoint = registry
				.getConfigurationElementsFor(EXTENSION_ACTION_SETS);
		for (int i = 0; i < actionSetsExtensionPoint.length; i++) {
			final IConfigurationElement element = actionSetsExtensionPoint[i];
			final String name = element.getName();

			// Check if it is a handler submission or a command definition.
			if (ELEMENT_ACTION_SET.equals(name)) {
				addElementToIndexedArray(element, indexedConfigurationElements,
						INDEX_ACTION_SETS, actionSetCount++);
			}
		}

		// Sort the editorActions extension point.
		final IConfigurationElement[] editorActionsExtensionPoint = registry
				.getConfigurationElementsFor(EXTENSION_EDITOR_ACTIONS);
		for (int i = 0; i < editorActionsExtensionPoint.length; i++) {
			final IConfigurationElement element = editorActionsExtensionPoint[i];
			final String name = element.getName();

			// Check if it is a handler submission or a command definition.
			if (ELEMENT_EDITOR_CONTRIBUTION.equals(name)) {
				addElementToIndexedArray(element, indexedConfigurationElements,
						INDEX_EDITOR_CONTRIBUTIONS, editorContributionCount++);
			}
		}

		// TODO Sort the popupMenus extension point.

		// TODO Sort the viewActions extension point.

		clearActivations(handlerService);
		clearContributions(menuService);
		readActionSetsFromRegistry(
				indexedConfigurationElements[INDEX_ACTION_SETS],
				actionSetCount, commandManager, handlerService, bindingManager,
				commandImageManager, menuService);
		readEditorContributionsFromRegistry(
				indexedConfigurationElements[INDEX_EDITOR_CONTRIBUTIONS],
				editorContributionCount, commandManager, handlerService,
				bindingManager, commandImageManager, menuService);
		// TODO Read other extension points from the registry.

		/*
		 * Adds listener so that future registry changes trigger an update of
		 * the command manager automatically.
		 */
		if (!listenersAttached) {
			registry.addRegistryChangeListener(new IRegistryChangeListener() {
				public final void registryChanged(
						final IRegistryChangeEvent event) {
					// TODO Determine if something changed.

					/*
					 * At least one of the deltas is non-zero, so re-read all of
					 * the bindings.
					 */
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							read(commandManager, handlerService,
									bindingManager, commandImageManager,
									menuService);
						}
					});
				}
			}, PlatformUI.PLUGIN_ID);

			listenersAttached = true;
		}
	}
}
