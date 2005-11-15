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

package org.eclipse.jface.action;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandEvent;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.ICommandListener;
import org.eclipse.core.commands.INamedHandleStateIds;
import org.eclipse.core.commands.IState;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.commands.CommandImageManager;
import org.eclipse.jface.commands.RadioHandlerState;
import org.eclipse.jface.commands.ToggleHandlerState;
import org.eclipse.jface.menus.IMenuStateIds;
import org.eclipse.jface.menus.LabelHandlerState;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.widgets.Event;

/**
 * <p>
 * A wrapper around the new command infrastructure that imitates the old
 * <code>IAction</code> interface.
 * </p>
 * <p>
 * Clients may instantiate this class, but must not extend.
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
public final class CommandAction extends AbstractAction {

	/**
	 * Listens to changes to one or more commands, and forwards them out through
	 * the property change event mechanism.
	 */
	private final class CommandListener implements ICommandListener {
		public final void commandChanged(final CommandEvent commandEvent) {
			final Command baseCommand = commandEvent.getCommand();

			// Check if the name changed.
			if (commandEvent.isNameChanged()) {
				String newName = null;
				if (baseCommand.isDefined()) {
					try {
						newName = baseCommand.getName();
					} catch (final NotDefinedException e) {
						// Not defined, so leave as null.
					}
				}
				firePropertyChange(IAction.TEXT, null, newName);
			}

			// Check if the description changed.
			if (commandEvent.isDescriptionChanged()) {
				String newDescription = null;
				if (baseCommand.isDefined()) {
					try {
						newDescription = baseCommand.getDescription();
					} catch (final NotDefinedException e) {
						// Not defined, so leave as null.
					}
				}
				firePropertyChange(IAction.DESCRIPTION, null, newDescription);
				firePropertyChange(IAction.TOOL_TIP_TEXT, null, newDescription);
			}

			// Check if the handled property changed.
			if (commandEvent.isHandledChanged()) {
				if (baseCommand.isHandled()) {
					firePropertyChange(IAction.HANDLED, Boolean.FALSE,
							Boolean.TRUE);
				} else {
					firePropertyChange(IAction.HANDLED, Boolean.TRUE,
							Boolean.FALSE);
				}
			}
		}

	}

	/**
	 * The binding manager providing bindings for this action; never
	 * <code>null</code>.
	 */
	private final BindingManager bindingManager;

	/**
	 * The command with which this action is associated; never <code>null</code>.
	 */
	private ParameterizedCommand command;

	/**
	 * Listens to changes in a command, and forwards them out through the
	 * property change event mechanism.
	 */
	private final ICommandListener commandListener = new CommandListener();

	/**
	 * The manager providing all of the possible commands for this action; never
	 * <code>null</code>. This is required for
	 * {@link #setActionDefinitionId(String)}.
	 */
	private final CommandManager commandManager;

	/**
	 * The command image manager providing images for this action; never
	 * <code>null</code>.
	 */
	private final CommandImageManager commandImageManager;

	/**
	 * Whether this action has been marked as enabled.
	 */
	private boolean enabled = true;

	/**
	 * The identifier for the action. This may be <code>null</code>.
	 */
	private String id;

	/**
	 * The image style to use for this action. This value may be
	 * <code>null</code>.
	 */
	private final String style;

	/**
	 * Constructs a new instance of <code>ActionProxy</code>.
	 * 
	 * @param id
	 *            The initial action identifier; may be <code>null</code>.
	 * @param command
	 *            The command with which this action is associated; must not be
	 *            <code>null</code>.
	 * @param commandManager
	 *            The manager providing all of the possible commands for this
	 *            action; must not be <code>null</code>.
	 * @param bindingManager
	 *            The binding manager providing binding support for this action;
	 *            must not be <code>null</code>.
	 * @param commandImageManager
	 *            The manager providing the images for this action; must not be
	 *            <code>null</code>.
	 * @param style
	 *            The image style to use for this action, may be
	 *            <code>null</code>.
	 */
	public CommandAction(final String id, final ParameterizedCommand command,
			final CommandManager commandManager,
			final BindingManager bindingManager,
			final CommandImageManager commandImageManager, final String style) {
		if (bindingManager == null) {
			throw new NullPointerException(
					"An action proxy can't be created without a binding manager"); //$NON-NLS-1$
		}

		if (commandManager == null) {
			throw new NullPointerException(
					"An action proxy can't be created without a command manager"); //$NON-NLS-1$
		}

		if (command == null) {
			throw new NullPointerException(
					"An action proxy can't be created without a command"); //$NON-NLS-1$
		}

		if (commandImageManager == null) {
			throw new NullPointerException(
					"An action proxy can't be created without an image manager"); //$NON-NLS-1$
		}

		this.bindingManager = bindingManager;
		this.command = command;
		this.commandManager = commandManager;
		this.commandImageManager = commandImageManager;
		this.id = id;
		this.style = style;

		// TODO Needs to listen to command, state, binding and image changes.
		command.getCommand().addCommandListener(commandListener);
	}

	public final int getAccelerator() {
		final String commandId = getActionDefinitionId();
		final TriggerSequence triggerSequence = bindingManager
				.getBestActiveBindingFor(commandId);
		if (triggerSequence instanceof KeySequence) {
			final KeySequence keySequence = (KeySequence) triggerSequence;
			final KeyStroke[] keyStrokes = keySequence.getKeyStrokes();
			if (keyStrokes.length == 1) {
				final KeyStroke keyStroke = keyStrokes[0];
				return keyStroke.getModifierKeys() | keyStroke.getNaturalKey();
			}
		}

		return 0;
	}

	public final String getActionDefinitionId() {
		return command.getId();
	}

	public final String getDescription() {
		try {
			return command.getCommand().getDescription();
		} catch (final NotDefinedException e) {
			return null;
		}
	}

	public final ImageDescriptor getDisabledImageDescriptor() {
		final String commandId = getActionDefinitionId();
		return commandImageManager.getImageDescriptor(commandId,
				CommandImageManager.TYPE_DISABLED, style);
	}

	public final HelpListener getHelpListener() {
		// TODO Help. Addressing help on commands.
		return null;
	}

	public final ImageDescriptor getHoverImageDescriptor() {
		final String commandId = getActionDefinitionId();
		return commandImageManager.getImageDescriptor(commandId,
				CommandImageManager.TYPE_HOVER, style);
	}

	public final String getId() {
		return id;
	}

	public final ImageDescriptor getImageDescriptor() {
		final String commandId = getActionDefinitionId();
		return commandImageManager.getImageDescriptor(commandId, style);
	}

	public final IMenuCreator getMenuCreator() {
		// TODO Pulldown. What kind of callback is needed here?
		return null;
	}

	public final int getStyle() {
		// TODO Pulldown. This does not currently support the pulldown style.
		final IState state = command.getCommand().getState(IMenuStateIds.STYLE);
		if (state instanceof RadioHandlerState) {
			return IAction.AS_RADIO_BUTTON;
		} else if (state instanceof ToggleHandlerState) {
			return IAction.AS_CHECK_BOX;
		}

		return IAction.AS_PUSH_BUTTON;
	}

	public final String getText() {
		try {
			return command.getName();
		} catch (final NotDefinedException e) {
			return null;
		}
	}

	public final String getToolTipText() {
		return getDescription();
	}

	public final boolean isChecked() {
		final IState state = command.getCommand().getState(IMenuStateIds.STYLE);
		if (state instanceof ToggleHandlerState) {
			final Boolean currentValue = (Boolean) state.getValue();
			return currentValue.booleanValue();
		}

		return false;
	}

	public final boolean isEnabled() {
		final Command baseCommand = command.getCommand();
		return baseCommand.isEnabled() && enabled;
	}

	public final boolean isHandled() {
		final Command baseCommand = command.getCommand();
		return baseCommand.isHandled();
	}

	public final void run() {
		runWithEvent(null);
	}

	public final void runWithEvent(final Event event) {
		final Command baseCommand = command.getCommand();
		final ExecutionEvent executionEvent = new ExecutionEvent(command
				.getParameterMap(), event, null);
		try {
			baseCommand.execute(executionEvent);
			firePropertyChange(IAction.RESULT, null, Boolean.TRUE);

		} catch (final NotHandledException e) {
			firePropertyChange(IAction.RESULT, null, Boolean.FALSE);

		} catch (final ExecutionException e) {
			firePropertyChange(IAction.RESULT, null, Boolean.FALSE);
			// TODO Should this be logged?

		}
	}

	public final void setAccelerator(final int keycode) {
		// TODO Binding. This is hopefully not essential.
	}

	public final void setActionDefinitionId(final String id) {
		// Get the old values.
		final boolean oldChecked = isChecked();
		final String oldDescription = getDescription();
		final boolean oldEnabled = isEnabled();
		final boolean oldHandled = isHandled();
		final ImageDescriptor oldDefaultImage = getImageDescriptor();
		final ImageDescriptor oldDisabledImage = getDisabledImageDescriptor();
		final ImageDescriptor oldHoverImage = getHoverImageDescriptor();
		final String oldText = getText();

		// Update the command.
		final Command oldBaseCommand = command.getCommand();
		oldBaseCommand.removeCommandListener(commandListener);
		final Command newBaseCommand = commandManager.getCommand(id);
		command = new ParameterizedCommand(newBaseCommand, null);
		newBaseCommand.addCommandListener(commandListener);

		// Get the new values.
		final boolean newChecked = isChecked();
		final String newDescription = getDescription();
		final boolean newEnabled = isEnabled();
		final boolean newHandled = isHandled();
		final ImageDescriptor newDefaultImage = getImageDescriptor();
		final ImageDescriptor newDisabledImage = getDisabledImageDescriptor();
		final ImageDescriptor newHoverImage = getHoverImageDescriptor();
		final String newText = getText();

		// Fire property change events, as necessary.
		if (newChecked != oldChecked) {
			if (oldChecked) {
				firePropertyChange(IAction.CHECKED, Boolean.TRUE, Boolean.FALSE);
			} else {
				firePropertyChange(IAction.CHECKED, Boolean.FALSE, Boolean.TRUE);
			}
		}

		if (!Util.equals(oldDescription, newDescription)) {
			firePropertyChange(IAction.DESCRIPTION, oldDescription,
					newDescription);
			firePropertyChange(IAction.TOOL_TIP_TEXT, oldDescription,
					newDescription);
		}

		if (newEnabled != oldEnabled) {
			if (oldEnabled) {
				firePropertyChange(IAction.ENABLED, Boolean.TRUE, Boolean.FALSE);
			} else {
				firePropertyChange(IAction.ENABLED, Boolean.FALSE, Boolean.TRUE);
			}
		}

		if (newHandled != oldHandled) {
			if (oldHandled) {
				firePropertyChange(IAction.HANDLED, Boolean.TRUE, Boolean.FALSE);
			} else {
				firePropertyChange(IAction.HANDLED, Boolean.FALSE, Boolean.TRUE);
			}
		}

		if (!Util.equals(oldDefaultImage, newDefaultImage)) {
			firePropertyChange(IAction.IMAGE, oldDefaultImage, newDefaultImage);
		}

		if (!Util.equals(oldDisabledImage, newDisabledImage)) {
			firePropertyChange(IAction.IMAGE, oldDisabledImage,
					newDisabledImage);
		}

		if (!Util.equals(oldHoverImage, newHoverImage)) {
			firePropertyChange(IAction.IMAGE, oldHoverImage, newHoverImage);
		}

		if (!Util.equals(oldText, newText)) {
			firePropertyChange(IAction.TEXT, oldText, newText);
		}
	}

	public final void setChecked(final boolean checked) {
		final IState state = command.getCommand().getState(IMenuStateIds.STYLE);
		if (state instanceof ToggleHandlerState) {
			final Boolean currentValue = (Boolean) state.getValue();
			if (checked != currentValue.booleanValue()) {
				if (checked) {
					state.setValue(Boolean.TRUE);
				} else {
					state.setValue(Boolean.FALSE);
				}
			}
		}
	}

	public final void setDescription(final String text) {
		final IState state = command.getCommand().getState(
				INamedHandleStateIds.DESCRIPTION);
		if (state instanceof LabelHandlerState) {
			final String currentValue = (String) state.getValue();
			if (!Util.equals(text, currentValue)) {
				state.setValue(text);
			}
		}
	}

	public final void setDisabledImageDescriptor(final ImageDescriptor newImage) {
		final String commandId = getActionDefinitionId();
		final int type = CommandImageManager.TYPE_DISABLED;
		commandImageManager.bind(commandId, type, style, newImage);
	}

	public final void setEnabled(final boolean enabled) {
		if (enabled != this.enabled) {
			final Boolean oldValue = this.enabled ? Boolean.TRUE
					: Boolean.FALSE;
			final Boolean newValue = enabled ? Boolean.TRUE : Boolean.FALSE;
			this.enabled = enabled;
			firePropertyChange(ENABLED, oldValue, newValue);
		}
	}

	public final void setHelpListener(final HelpListener listener) {
		// TODO Help Haven't even started to look at help yet.

	}

	public final void setHoverImageDescriptor(final ImageDescriptor newImage) {
		final String commandId = getActionDefinitionId();
		final int type = CommandImageManager.TYPE_HOVER;
		commandImageManager.bind(commandId, type, style, newImage);
	}

	public final void setId(final String id) {
		this.id = id;
	}

	public final void setImageDescriptor(final ImageDescriptor newImage) {
		final String commandId = getActionDefinitionId();
		final int type = CommandImageManager.TYPE_DEFAULT;
		commandImageManager.bind(commandId, type, style, newImage);
	}

	public final void setMenuCreator(final IMenuCreator creator) {
		// TODO Pulldown. This is complicated
	}

	public final void setText(final String text) {
		final IState state = command.getCommand().getState(
				INamedHandleStateIds.NAME);
		if (state instanceof LabelHandlerState) {
			final String currentValue = (String) state.getValue();
			if (!Util.equals(text, currentValue)) {
				state.setValue(text);
			}
		}
	}

	public final void setToolTipText(final String text) {
		setDescription(text);
	}

}
