/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.menus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandEvent;
import org.eclipse.core.commands.ICommandListener;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.State;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.Scheme;
import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.jface.bindings.keys.KeyLookupFactory;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.commands.RadioState;
import org.eclipse.jface.commands.ToggleState;
import org.eclipse.jface.contexts.IContextIds;
import org.eclipse.jface.menus.IMenuStateIds;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.SelectionEnabler;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.ActionExpression;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.expressions.LegacyActionExpressionWrapper;
import org.eclipse.ui.internal.expressions.LegacyActionSetExpression;
import org.eclipse.ui.internal.expressions.LegacyEditorContributionExpression;
import org.eclipse.ui.internal.expressions.LegacySelectionEnablerWrapper;
import org.eclipse.ui.internal.expressions.LegacyViewContributionExpression;
import org.eclipse.ui.internal.expressions.LegacyViewerContributionExpression;
import org.eclipse.ui.internal.handlers.ActionDelegateHandlerProxy;
import org.eclipse.ui.internal.handlers.IActionCommandMappingService;
import org.eclipse.ui.internal.keys.BindingService;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.services.RegistryPersistence;
import org.eclipse.ui.keys.IBindingService;

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
 * 
 * @since 3.2
 */
public final class LegacyActionPersistence extends RegistryPersistence {

	/**
	 * The index of the action set elements in the indexed array.
	 * 
	 * @see LegacyActionPersistence#read()
	 */
	private static final int INDEX_ACTION_SETS = 0;

	/**
	 * The index of the editor contribution elements in the indexed array.
	 * 
	 * @see LegacyActionPersistence#read()
	 */
	private static final int INDEX_EDITOR_CONTRIBUTIONS = 1;

	/**
	 * The index of the object contribution elements in the indexed array.
	 * 
	 * @see LegacyActionPersistence#read()
	 */
	private static final int INDEX_OBJECT_CONTRIBUTIONS = 2;

	/**
	 * The index of the view contribution elements in the indexed array.
	 * 
	 * @see LegacyActionPersistence#read()
	 */
	private static final int INDEX_VIEW_CONTRIBUTIONS = 3;

	/**
	 * The index of the viewer contribution elements in the indexed array.
	 * 
	 * @see LegacyActionPersistence#read()
	 */
	private static final int INDEX_VIEWER_CONTRIBUTIONS = 4;


	/**
	 * Reads the visibility element for a contribution from the
	 * <code>org.eclipse.ui.popupMenus</code> extension point.
	 * 
	 * @param parentElement
	 *            The contribution element which contains a visibility
	 *            expression; must not be <code>null</code>.
	 * @param parentId
	 *            The identifier of the parent contribution; may be
	 *            <code>null</code>.
	 * @param warningsToLog
	 *            The list of warnings to be logged; must not be
	 *            <code>null</code>.
	 * @return An expression representing the visibility element; may be
	 *         <code>null</code>.
	 */
	private static final Expression readVisibility(
			final IConfigurationElement parentElement, final String parentId,
			final List warningsToLog) {
		final IConfigurationElement[] visibilityElements = parentElement
				.getChildren(TAG_VISIBILITY);
		if ((visibilityElements == null) || (visibilityElements.length == 0)) {
			return null;
		}

		if (visibilityElements.length != 1) {
			addWarning(warningsToLog,
					"There can only be one visibility element", parentElement, //$NON-NLS-1$
					parentId);
		}

		final IConfigurationElement visibilityElement = visibilityElements[0];
		final ActionExpression visibilityActionExpression = new ActionExpression(
				visibilityElement);
		final LegacyActionExpressionWrapper wrapper = new LegacyActionExpressionWrapper(
				visibilityActionExpression, null);
		return wrapper;
	}

	/**
	 * The binding manager which should be populated with bindings from actions;
	 * must not be <code>null</code>.
	 */
	private final BindingService bindingService;



	/**
	 * The command service which is providing the commands for the workbench;
	 * must not be <code>null</code>.
	 */
	private final ICommandService commandService;

	/**
	 * The handler activations that have come from the registry. This is used to
	 * flush the activations when the registry is re-read. This value is never
	 * <code>null</code>
	 */
	private final Collection handlerActivations = new ArrayList();

	/**
	 * The menu contributions that have come from the registry. This is used to
	 * flush the contributions when the registry is re-read. This value is never
	 * <code>null</code>
	 */
	private final Collection menuContributions = new ArrayList();

	/**
	 * The service locator from which services can be retrieved in the future;
	 * must not be <code>null</code>.
	 */
	private final IWorkbenchWindow window;
	
	/**
	 * Help action sets with enable/disable.
	 */
	private ICommandListener actionSetListener = new ICommandListener() {
		public void commandChanged(CommandEvent commandEvent) {
			Command cmd = commandEvent.getCommand();
			String commandId = cmd.getId();
			Binding binding = (Binding) commandIdToBinding.get(commandId);
			if (binding != null) {
				if (cmd.isEnabled()) {
					if (!actionSetActiveBindings.contains(binding)) {
						bindingService.addBinding(binding);
						actionSetActiveBindings.add(binding);
					}
				} else if (actionSetActiveBindings.contains(binding)) {
					bindingService.removeBinding(binding);
					actionSetActiveBindings.remove(binding);
				}
			}
		}
	};
	
	/**
	 * Map every commandId to its binding.
	 */
	private HashMap commandIdToBinding = new HashMap();
	
	/**
	 * Which bindings do we currently have outstanding.
	 */
	private HashSet actionSetActiveBindings = new HashSet();

	/**
	 * Constructs a new instance of {@link LegacyActionPersistence}.
	 * 
	 * @param window
	 *            The window from which the services should be retrieved; must
	 *            not be <code>null</code>.
	 */
	public LegacyActionPersistence(final IWorkbenchWindow window) {
		// TODO Blind casts are bad.
		this.bindingService = (BindingService) window
				.getService(IBindingService.class);

		this.commandService = (ICommandService) window
				.getService(ICommandService.class);
		this.window = window;
	}

	/**
	 * Deactivates all of the activations made by this class, and then clears
	 * the collection. This should be called before every read.
	 */
	private final void clearActivations() {
		final IHandlerService service = (IHandlerService) window
				.getService(IHandlerService.class);
		service.deactivateHandlers(handlerActivations);
		final Iterator activationItr = handlerActivations.iterator();
		while (activationItr.hasNext()) {
			final IHandlerActivation activation = (IHandlerActivation) activationItr
					.next();
			final IHandler handler = activation.getHandler();
			if (handler != null) {
				handler.dispose();
			}
		}
		handlerActivations.clear();
	}

	/**
	 * Removes all of the bindings made by this class, and then clears the
	 * collection. This should be called before every read.
	 */
	private final void clearBindings() {
		Iterator i = commandIdToBinding.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry entry = (Map.Entry) i.next();
			String commandId = (String) entry.getKey();
			Binding binding = (Binding) entry.getValue();
			commandService.getCommand(commandId).removeCommandListener(actionSetListener);
			if (binding!=null && actionSetActiveBindings.contains(binding)) {
				bindingService.removeBinding(binding);
			}
		}
		commandIdToBinding.clear();
		actionSetActiveBindings.clear();
	}

	/**
	 * Removes all of the image bindings made by this class, and then clears the
	 * collection. This should be called before every read.
	 * 
	 */
	private final void clearImages() {
		// TODO Implement
	}

	/**
	 * Removes all of the contributions made by this class, and then clears the
	 * collection. This should be called before every read.
	 */
	private final void clearMenus() {
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
	 */
	private final void convertActionToBinding(
			final IConfigurationElement element,
			final ParameterizedCommand command, final List warningsToLog) {
		// Figure out which accelerator text to use.
		String acceleratorText = readOptional(element, ATT_ACCELERATOR);
		if (acceleratorText == null) {
			final String label = readOptional(element, ATT_LABEL);
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

			final Scheme activeScheme = bindingService.getActiveScheme();

			try {
				final Binding binding = new KeyBinding(keySequence, command,
						activeScheme.getId(), IContextIds.CONTEXT_ID_WINDOW,
						null, null, null, Binding.SYSTEM);
				commandIdToBinding.put(command.getCommand().getId(), binding);

				if (command.getCommand().isEnabled()) {
					bindingService.addBinding(binding);
					actionSetActiveBindings.add(binding);
				}

				command.getCommand().addCommandListener(actionSetListener);
			} catch (IllegalArgumentException e) {
				addWarning(warningsToLog,
						"invalid keybinding: " + e.getMessage(), element, //$NON-NLS-1$
						command.getCommand().getId());
			}
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
	 * @param warningsToLog
	 *            The collection of warnings logged while reading the extension
	 *            point; must not be <code>null</code>.
	 * @return the fully-parameterized command; <code>null</code> if an error
	 *         occurred.
	 */
	private final ParameterizedCommand convertActionToCommand(
			final IConfigurationElement element, final String primaryId,
			final String secondaryId, final List warningsToLog) {
		String commandId = readOptional(element, ATT_DEFINITION_ID);
		Command command = null;
		if (commandId != null) {
			command = commandService.getCommand(commandId);
		}

		final IActionCommandMappingService mappingService = (IActionCommandMappingService) window
				.getService(IActionCommandMappingService.class);
		
		String label = null;
		if ((commandId == null) || (!command.isDefined())) {
			// Add a mapping from this action id to the command id.
			if (commandId == null && mappingService != null) {
				commandId = mappingService.getGeneratedCommandId(primaryId,
						secondaryId);
			}
			if (commandId == null) {
				WorkbenchPlugin.log("MappingService unavailable"); //$NON-NLS-1$
				return null;
			}

			// Read the label attribute.
			label = readRequired(element, ATT_LABEL, warningsToLog,
					"Actions require a non-empty label or definitionId", //$NON-NLS-1$
					commandId);
			if (label == null) {
				label = WorkbenchMessages.LegacyActionPersistence_AutogeneratedCommandName;
			}

			/*
			 * Read the tooltip attribute. The tooltip is really the description
			 * of the command.
			 */
			final String tooltip = readOptional(element, ATT_TOOLTIP);

			// Define the command.
			command = commandService.getCommand(commandId);
			final Category category = commandService.getCategory(null);
			final String name = LegacyActionTools.removeAcceleratorText(Action
					.removeMnemonics(label));
			command.define(name, tooltip, category, null);

			// TODO Decide the command state.
			final String style = readOptional(element, ATT_STYLE);
			if (STYLE_RADIO.equals(style)) {
				final State state = new RadioState();
				// TODO How to set the id?
				final boolean checked = readBoolean(element, ATT_STATE, false);
				state.setValue((checked) ? Boolean.TRUE : Boolean.FALSE);
				command.addState(IMenuStateIds.STYLE, state);

			} else if (STYLE_TOGGLE.equals(style)) {
				final State state = new ToggleState();
				final boolean checked = readBoolean(element, ATT_STATE, false);
				state.setValue((checked) ? Boolean.TRUE : Boolean.FALSE);
				command.addState(IMenuStateIds.STYLE, state);
			}
		}
		// this allows us to look up a "commandId" give the contributionId
		// and the actionId
		if (mappingService != null && commandId != null) {
			mappingService.map(mappingService.getGeneratedCommandId(primaryId,
					secondaryId), commandId);
		}

		return new ParameterizedCommand(command, null);
	}

	/**
	 * <p>
	 * Extracts the handler information from the given action element. These are
	 * registered with the handler service. They are always active.
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
	 * @param activeWhenExpression
	 *            The expression controlling when the handler is active; may be
	 *            <code>null</code>.
	 * @param viewId
	 *            The view to which this handler is associated. This value is
	 *            required if this is a view action; otherwise it can be
	 *            <code>null</code>.
	 * @param warningsToLog
	 *            The collection of warnings while parsing this extension point;
	 *            must not be <code>null</code>.
	 */
	private final void convertActionToHandler(
			final IConfigurationElement element, final String actionId,
			final ParameterizedCommand command,
			final Expression activeWhenExpression, final String viewId,
			final List warningsToLog) {
		// Check to see if this is a retargettable action.
		final boolean retarget = readBoolean(element, ATT_RETARGET, false);

		final boolean classAvailable = (element.getAttribute(ATT_CLASS) != null)
				|| (element.getChildren(TAG_CLASS).length != 0);
		// Read the class attribute.
		String classString = readOptional(element, ATT_CLASS);
		if (classAvailable && classString == null) {
			classString = readOptional(element.getChildren(TAG_CLASS)[0],
					ATT_CLASS);
		}

		if (retarget) {
			if (classAvailable && !isPulldown(element)) {
				addWarning(warningsToLog,
						"The class was not null but retarget was set to true", //$NON-NLS-1$
						element, actionId, ATT_CLASS, classString);
			}

			// Add a mapping from this action id to the command id.
			final IActionCommandMappingService mappingService = (IActionCommandMappingService) window
					.getService(IActionCommandMappingService.class);
			if (mappingService != null) {
				mappingService.map(actionId, command.getId());
			} else {
				// this is probably the shutdown case where the service has
				// already disposed.
				addWarning(
						warningsToLog,
						"Retarget service unavailable", //$NON-NLS-1$
						element, actionId);
			}
			return; // This is nothing more to be done.

		} else if (!classAvailable) {
			addWarning(
					warningsToLog,
					"There was no class provided, and the action is not retargettable", //$NON-NLS-1$
					element, actionId);
			return; // There is nothing to be done.
		}

		// Read the enablesFor attribute, and enablement and selection elements.
		SelectionEnabler enabler = null;
		if (element.getAttribute(ATT_ENABLES_FOR) != null) {
			enabler = new SelectionEnabler(element);
		} else {
			IConfigurationElement[] kids = element.getChildren(TAG_ENABLEMENT);
			if (kids.length > 0) {
				enabler = new SelectionEnabler(element);
			}
		}
		final Expression enabledWhenExpression;
		if (enabler == null) {
			enabledWhenExpression = null;
		} else {
			enabledWhenExpression = new LegacySelectionEnablerWrapper(enabler,
					window);
		}

		/*
		 * Create the handler. TODO The image style is read at the workbench
		 * level, but it is hard to communicate this information to this point.
		 * For now, I'll pass null, but this ultimately won't work.
		 */
		final ActionDelegateHandlerProxy handler = new ActionDelegateHandlerProxy(
				element, ATT_CLASS, actionId, command, window, null,
				enabledWhenExpression, viewId);

		// Read the help context id.
		final String helpContextId = readOptional(element, ATT_HELP_CONTEXT_ID);
		if (helpContextId != null) {
			commandService.setHelpContextId(handler, helpContextId);
		}

		// Activate the handler.
		final String commandId = command.getId();
		final IHandlerService service = (IHandlerService) window
				.getService(IHandlerService.class);
		final IHandlerActivation handlerActivation;
		if (activeWhenExpression == null) {
			handlerActivation = service.activateHandler(commandId, handler);
		} else {
			handlerActivation = service.activateHandler(commandId, handler,
					activeWhenExpression);
		}
		handlerActivations.add(handlerActivation);
	}



	public final void dispose() {
		super.dispose();
		clear();
	}

	private void clear() {
		clearActivations();
		clearBindings();
		clearImages();
		clearMenus();
	}

	protected final boolean isChangeImportant(final IRegistryChangeEvent event) {
		return !((event.getExtensionDeltas(PlatformUI.PLUGIN_ID,
				IWorkbenchRegistryConstants.PL_ACTION_SETS).length == 0)
				&& (event.getExtensionDeltas(PlatformUI.PLUGIN_ID,
						IWorkbenchRegistryConstants.PL_EDITOR_ACTIONS).length == 0)
				&& (event.getExtensionDeltas(PlatformUI.PLUGIN_ID,
						IWorkbenchRegistryConstants.PL_POPUP_MENU).length == 0) && (event
				.getExtensionDeltas(PlatformUI.PLUGIN_ID,
						IWorkbenchRegistryConstants.PL_VIEW_ACTIONS).length == 0));
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
	 */
	public final void read() {
		clear();
		LegacyActionPersistence.super.read();

		// Create the extension registry mementos.
		final IExtensionRegistry registry = Platform.getExtensionRegistry();
		int actionSetCount = 0;
		int editorContributionCount = 0;
		int objectContributionCount = 0;
		int viewContributionCount = 0;
		int viewerContributionCount = 0;
		final IConfigurationElement[][] indexedConfigurationElements = new IConfigurationElement[5][];

		// Sort the actionSets extension point.
		final IConfigurationElement[] actionSetsExtensionPoint = registry
				.getConfigurationElementsFor(EXTENSION_ACTION_SETS);
		for (int i = 0; i < actionSetsExtensionPoint.length; i++) {
			final IConfigurationElement element = actionSetsExtensionPoint[i];
			final String name = element.getName();
			if (TAG_ACTION_SET.equals(name)) {
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
			if (TAG_EDITOR_CONTRIBUTION.equals(name)) {
				addElementToIndexedArray(element, indexedConfigurationElements,
						INDEX_EDITOR_CONTRIBUTIONS, editorContributionCount++);
			}
		}

		// Sort the popupMenus extension point.
		final IConfigurationElement[] popupMenusExtensionPoint = registry
				.getConfigurationElementsFor(EXTENSION_POPUP_MENUS);
		for (int i = 0; i < popupMenusExtensionPoint.length; i++) {
			final IConfigurationElement element = popupMenusExtensionPoint[i];
			final String name = element.getName();
			if (TAG_OBJECT_CONTRIBUTION.equals(name)) {
				addElementToIndexedArray(element, indexedConfigurationElements,
						INDEX_OBJECT_CONTRIBUTIONS, objectContributionCount++);
			} else if (TAG_VIEWER_CONTRIBUTION.equals(name)) {
				addElementToIndexedArray(element, indexedConfigurationElements,
						INDEX_VIEWER_CONTRIBUTIONS, viewerContributionCount++);
			}
		}

		// Sort the viewActions extension point.
		final IConfigurationElement[] viewActionsExtensionPoint = registry
				.getConfigurationElementsFor(EXTENSION_VIEW_ACTIONS);
		for (int i = 0; i < viewActionsExtensionPoint.length; i++) {
			final IConfigurationElement element = viewActionsExtensionPoint[i];
			final String name = element.getName();
			if (TAG_VIEW_CONTRIBUTION.equals(name)) {
				addElementToIndexedArray(element, indexedConfigurationElements,
						INDEX_VIEW_CONTRIBUTIONS, viewContributionCount++);
			}
		}

		readActionSets(indexedConfigurationElements[INDEX_ACTION_SETS],
				actionSetCount);
		readEditorContributions(
				indexedConfigurationElements[INDEX_EDITOR_CONTRIBUTIONS],
				editorContributionCount);
		readObjectContributions(
				indexedConfigurationElements[INDEX_OBJECT_CONTRIBUTIONS],
				objectContributionCount);
		readViewContributions(
				indexedConfigurationElements[INDEX_VIEW_CONTRIBUTIONS],
				viewContributionCount);
		readViewerContributions(
				indexedConfigurationElements[INDEX_VIEWER_CONTRIBUTIONS],
				viewerContributionCount);
		
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
	 * @param visibleWhenExpression
	 *            The expression controlling visibility of the corresponding
	 *            menu elements; may be <code>null</code>.
	 * @param viewId
	 *            The view to which this handler is associated. This value is
	 *            required if this is a view action; otherwise it can be
	 *            <code>null</code>.
	 * @return References to the created menu elements; may be <code>null</code>,
	 *         and may be empty.
	 */
	private final void readActions(final String primaryId,
			final IConfigurationElement[] elements, final List warningsToLog,
			final Expression visibleWhenExpression, final String viewId) {
		for (int i = 0; i < elements.length; i++) {
			final IConfigurationElement element = elements[i];

			/*
			 * We might need the identifier to generate the command, so we'll
			 * read it out now.
			 */
			final String id = readRequired(element, ATT_ID, warningsToLog,
					"Actions require an id"); //$NON-NLS-1$
			if (id == null) {
				continue;
			}

			// Try to break out the command part of the action.
			final ParameterizedCommand command = convertActionToCommand(
					element, primaryId, id, warningsToLog);
			if (command == null) {
				continue;
			}

			convertActionToHandler(element, id, command, visibleWhenExpression,
					viewId, warningsToLog);
			// TODO Read the overrideActionId attribute

			convertActionToBinding(element, command, warningsToLog);

		}
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
	 * @param visibleWhenExpression
	 *            The expression controlling visibility of the corresponding
	 *            menu elements; may be <code>null</code>.
	 * @param viewId
	 *            The view to which this handler is associated. This value is
	 *            required if this is a view action; otherwise it can be
	 *            <code>null</code>.
	 * @return An array of references to the created menu elements. This value
	 *         may be <code>null</code> if there was a problem parsing the
	 *         configuration element.
	 */
	private final void readActionsAndMenus(
			final IConfigurationElement element, final String id,
			final List warningsToLog, 
			final Expression visibleWhenExpression, final String viewId) {

		// Read its child elements.
		final IConfigurationElement[] actionElements = element
				.getChildren(TAG_ACTION);
		readActions(id, actionElements,
				warningsToLog, visibleWhenExpression, viewId);

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
	 */
	private final void readActionSets(
			final IConfigurationElement[] configurationElements,
			final int configurationElementCount) {
		// 
		// this was an even dumber fix than modifying the path
		// 
		// stupid navigate group
		// SGroup nav = menuService.getGroup(STUPID_NAVIGATE);
		// if (!nav.isDefined()) {
		// nav.define(new SLocation(new SBar(SBar.TYPE_MENU, null)));
		// }
		// stupid navigate group

		final List warningsToLog = new ArrayList(1);

		for (int i = 0; i < configurationElementCount; i++) {
			final IConfigurationElement element = configurationElements[i];

			// Read the action set identifier.
			final String id = readRequired(element, ATT_ID, warningsToLog,
					"Action sets need an id"); //$NON-NLS-1$
			if (id == null) {
				continue;
			}

			// Read the label.
			final String label = readRequired(element, ATT_LABEL,
					warningsToLog, "Actions set need a label", //$NON-NLS-1$
					id);
			if (label == null) {
				continue;
			}

			// Restrict the handler to when the action set is active.
			final LegacyActionSetExpression expression = new LegacyActionSetExpression(
					id, window);


			// Read all of the child elements.
			readActionsAndMenus(element, id,
					warningsToLog, expression, null);
			// Define the action set.
		}

		logWarnings(
				warningsToLog,
				"Warnings while parsing the action sets from the 'org.eclipse.ui.actionSets' extension point"); //$NON-NLS-1$
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
	 */
	private final void readEditorContributions(
			final IConfigurationElement[] configurationElements,
			final int configurationElementCount) {
		final List warningsToLog = new ArrayList(1);

		for (int i = 0; i < configurationElementCount; i++) {
			final IConfigurationElement element = configurationElements[i];

			// Read the editor contribution identifier.
			final String id = readRequired(element, ATT_ID, warningsToLog,
					"Editor contributions need an id"); //$NON-NLS-1$
			if (id == null) {
				continue;
			}

			/*
			 * Read the target id. This is the identifier of the editor with
			 * which these contributions are associated.
			 */
			final String targetId = readRequired(element, ATT_TARGET_ID,
					warningsToLog, "Editor contributions need a target id", id); //$NON-NLS-1$
			if (targetId == null) {
				continue;
			}
			final Expression visibleWhenExpression = new LegacyEditorContributionExpression(
					targetId, window);

			// Read all of the child elements from the registry.
			readActionsAndMenus(element, id, warningsToLog,
					visibleWhenExpression, null);
		}

		logWarnings(
				warningsToLog,
				"Warnings while parsing the editor contributions from the 'org.eclipse.ui.editorActions' extension point"); //$NON-NLS-1$
	}




	/**
	 * Reads the deprecated object contributions from an array of elements from
	 * the popup menus extension point.
	 * 
	 * @param configurationElements
	 *            The configuration elements in the extension point; must not be
	 *            <code>null</code>, but may be empty.
	 * @param configurationElementCount
	 *            The number of configuration elements that are really in the
	 *            array.
	 */
	private final void readObjectContributions(
			final IConfigurationElement[] configurationElements,
			final int configurationElementCount) {
		final List warningsToLog = new ArrayList(1);

		for (int i = 0; i < configurationElementCount; i++) {
			final IConfigurationElement element = configurationElements[i];

			// Read the object contribution identifier.
			final String id = readRequired(element, ATT_ID, warningsToLog,
					"Object contributions need an id"); //$NON-NLS-1$
			if (id == null) {
				continue;
			}

			// Read the object class. This influences the visibility.
			final String objectClass = readRequired(element, ATT_OBJECTCLASS,
					warningsToLog,
					"Object contributions need an object class", id); //$NON-NLS-1$
			if (objectClass == null) {
				continue;
			}

			// TODO Read the name filter. This influences the visibility.
			// final String nameFilter = readOptional(element,
			// ATT_NAME_FILTER);

			// TODO Read the object class. This influences the visibility.
			// final boolean adaptable = readBoolean(element,
			// ATT_ADAPTABLE,
			// false);


			// TODO Read the filter elements.
			// TODO Read the enablement elements.

			// TODO Figure out an appropriate visibility expression.
			// Read the visibility element, if any.
			final Expression visibleWhenExpression = readVisibility(element,
					id, warningsToLog);

			// Read all of the child elements from the registry.
			readActionsAndMenus(element, id, warningsToLog,
					visibleWhenExpression, null);
		}

		logWarnings(
				warningsToLog,
				"Warnings while parsing the object contributions from the 'org.eclipse.ui.popupMenus' extension point"); //$NON-NLS-1$
	}

	/**
	 * Reads the deprecated view contributions from an array of elements from
	 * the view actions extension point.
	 * 
	 * @param configurationElements
	 *            The configuration elements in the extension point; must not be
	 *            <code>null</code>, but may be empty.
	 * @param configurationElementCount
	 *            The number of configuration elements that are really in the
	 *            array.
	 */
	private final void readViewContributions(
			final IConfigurationElement[] configurationElements,
			final int configurationElementCount) {
		final List warningsToLog = new ArrayList(1);

		for (int i = 0; i < configurationElementCount; i++) {
			final IConfigurationElement element = configurationElements[i];

			// Read the view contribution identifier.
			final String id = readRequired(element, ATT_ID, warningsToLog,
					"View contributions need an id"); //$NON-NLS-1$
			if (id == null) {
				continue;
			}

			/*
			 * Read the target id. This is the identifier of the view with which
			 * these contributions are associated.
			 */
			final String targetId = readRequired(element, ATT_TARGET_ID,
					warningsToLog, "View contributions need a target id", id); //$NON-NLS-1$
			if (targetId == null) {
				continue;
			}
			final Expression visibleWhenExpression = new LegacyViewContributionExpression(
					targetId, window);

			// Read all of the child elements from the registry.
			readActionsAndMenus(element, id, warningsToLog,
					visibleWhenExpression, targetId);
		}

		logWarnings(
				warningsToLog,
				"Warnings while parsing the view contributions from the 'org.eclipse.ui.viewActions' extension point"); //$NON-NLS-1$
	}

	/**
	 * Reads the deprecated viewer contributions from an array of elements from
	 * the popup menus extension point.
	 * 
	 * @param configurationElements
	 *            The configuration elements in the extension point; must not be
	 *            <code>null</code>, but may be empty.
	 * @param configurationElementCount
	 *            The number of configuration elements that are really in the
	 *            array.
	 */
	private final void readViewerContributions(
			final IConfigurationElement[] configurationElements,
			final int configurationElementCount) {
		final List warningsToLog = new ArrayList(1);

		for (int i = 0; i < configurationElementCount; i++) {
			final IConfigurationElement element = configurationElements[i];

			// Read the viewer contribution identifier.
			final String id = readRequired(element, ATT_ID, warningsToLog,
					"Viewer contributions need an id"); //$NON-NLS-1$
			if (id == null) {
				continue;
			}

			/*
			 * Read the target id. This is the identifier of the view with which
			 * these contributions are associated.
			 */
			final String targetId = readRequired(element, ATT_TARGET_ID,
					warningsToLog, "Viewer contributions need a target id", id); //$NON-NLS-1$
			if (targetId == null) {
				continue;
			}

			// Read the visibility element, if any.
			final Expression visibleWhenExpression = readVisibility(element,
					id, warningsToLog);
			final Expression menuVisibleWhenExpression = new LegacyViewerContributionExpression(
					targetId, window, visibleWhenExpression);

			// Read all of the child elements from the registry.
			readActionsAndMenus(element, id, warningsToLog,
					menuVisibleWhenExpression, targetId);
		}

		logWarnings(
				warningsToLog,
				"Warnings while parsing the viewer contributions from the 'org.eclipse.ui.popupMenus' extension point"); //$NON-NLS-1$
	}
}
