/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 440810
 *******************************************************************************/

package org.eclipse.ui.internal.handlers;

import java.util.Objects;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandEvent;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.ICommandListener;
import org.eclipse.core.commands.INamedHandleStateIds;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.State;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.action.AbstractAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.commands.RadioState;
import org.eclipse.jface.commands.ToggleState;
import org.eclipse.jface.menus.IMenuStateIds;
import org.eclipse.jface.menus.TextState;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.commands.ICommandImageService;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.internal.commands.CommandImageManager;
import org.eclipse.ui.internal.commands.CommandImageService;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.services.IServiceLocator;

/**
 * <p>
 * A wrapper around the new command infrastructure that imitates the old
 * <code>IAction</code> interface.
 * </p>
 * <p>
 * Clients may instantiate this class, but must not extend.
 * </p>
 * <p>
 * <strong>PROVISIONAL</strong>. This class or interface has been added as part
 * of a work in progress. There is a guarantee neither that this API will work
 * nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * <p>
 * This class is eventually intended to exist in
 * <code>org.eclipse.ui.handlers</code>.
 * </p>
 *
 * @since 3.2
 */
public final class CommandLegacyActionWrapper extends AbstractAction {

	/**
	 * Listens to changes to one or more commands, and forwards them out through the
	 * property change event mechanism.
	 */
	private final class CommandListener implements ICommandListener {
		@Override
		public void commandChanged(final CommandEvent commandEvent) {
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
					firePropertyChange(IAction.HANDLED, Boolean.FALSE, Boolean.TRUE);
				} else {
					firePropertyChange(IAction.HANDLED, Boolean.TRUE, Boolean.FALSE);
				}
			}
		}

	}

	/**
	 * The command with which this action is associated; never <code>null</code>.
	 */
	private ParameterizedCommand command;

	/**
	 * Listens to changes in a command, and forwards them out through the property
	 * change event mechanism.
	 */
	private final ICommandListener commandListener = new CommandListener();

	/**
	 * Whether this action has been marked as enabled.
	 */
	private boolean enabled = true;

	/**
	 * The identifier for the action. This may be <code>null</code>.
	 */
	private String id;

	/**
	 * A service locator that can be used for retrieving command-based services.
	 * This value is <code>null</code> after disposal.
	 */
	private IServiceLocator serviceLocator;

	/**
	 * The image style to use for this action. This value may be <code>null</code>.
	 */
	private final String style;

	/**
	 * Constructs a new instance of <code>ActionProxy</code>.
	 *
	 * @param id             The initial action identifier; may be
	 *                       <code>null</code>.
	 * @param command        The command with which this action is associated; must
	 *                       not be <code>null</code>.
	 * @param style          The image style to use for this action, may be
	 *                       <code>null</code>.
	 * @param serviceLocator A service locator that can be used to find various
	 *                       command-based services; must not be <code>null</code>.
	 */
	public CommandLegacyActionWrapper(final String id, final ParameterizedCommand command, final String style,
			final IServiceLocator serviceLocator) {
		if (command == null) {
			throw new NullPointerException("An action proxy can't be created without a command"); //$NON-NLS-1$
		}

		if (serviceLocator == null) {
			throw new NullPointerException("An action proxy can't be created without a service locator"); //$NON-NLS-1$
		}

		this.command = command;
		this.id = id;
		this.style = style;
		this.serviceLocator = serviceLocator;

		// TODO Needs to listen to command, state, binding and image changes.
		command.getCommand().addCommandListener(commandListener);
	}

	@Override
	public int getAccelerator() {
		final String commandId = getActionDefinitionId();
		final IBindingService bindingService = serviceLocator.getService(IBindingService.class);
		final TriggerSequence triggerSequence = bindingService.getBestActiveBindingFor(commandId);
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

	@Override
	public String getActionDefinitionId() {
		return command.getId();
	}

	@Override
	public String getDescription() {
		try {
			return command.getCommand().getDescription();
		} catch (final NotDefinedException e) {
			return null;
		}
	}

	@Override
	public ImageDescriptor getDisabledImageDescriptor() {
		final String commandId = getActionDefinitionId();
		final ICommandImageService commandImageService = serviceLocator.getService(ICommandImageService.class);
		return commandImageService.getImageDescriptor(commandId, CommandImageManager.TYPE_DISABLED, style);
	}

	@Override
	public HelpListener getHelpListener() {
		// TODO Help. Addressing help on commands.
		return null;
	}

	@Override
	public ImageDescriptor getHoverImageDescriptor() {
		final String commandId = getActionDefinitionId();
		final ICommandImageService commandImageService = serviceLocator.getService(ICommandImageService.class);
		return commandImageService.getImageDescriptor(commandId, CommandImageManager.TYPE_HOVER, style);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		final String commandId = getActionDefinitionId();
		final ICommandImageService commandImageService = serviceLocator.getService(ICommandImageService.class);
		return commandImageService.getImageDescriptor(commandId, style);
	}

	@Override
	public IMenuCreator getMenuCreator() {
		// TODO Pulldown. What kind of callback is needed here?
		return null;
	}

	@Override
	public int getStyle() {
		// TODO Pulldown. This does not currently support the pulldown style.
		final State state = command.getCommand().getState(IMenuStateIds.STYLE);
		if (state instanceof RadioState) {
			return IAction.AS_RADIO_BUTTON;
		} else if (state instanceof ToggleState) {
			return IAction.AS_CHECK_BOX;
		}

		return IAction.AS_PUSH_BUTTON;
	}

	@Override
	public String getText() {
		try {
			return command.getName();
		} catch (final NotDefinedException e) {
			return null;
		}
	}

	@Override
	public String getToolTipText() {
		return getDescription();
	}

	@Override
	public boolean isChecked() {
		final State state = command.getCommand().getState(IMenuStateIds.STYLE);
		if (state instanceof ToggleState) {
			final Boolean currentValue = (Boolean) state.getValue();
			return currentValue.booleanValue();
		}

		return false;
	}

	@Override
	public boolean isEnabled() {
		return isEnabledDisregardingCommand();
	}

	/**
	 * Whether this action's local <code>enabled</code> property is set. This can be
	 * used by handlers that are trying to check if {@link #setEnabled(boolean)} has
	 * been called. This is typically used by legacy action proxies who are trying
	 * to avoid a
	 * <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=117496">stack
	 * overflow</a>.
	 *
	 * @return <code>false</code> if someone has called {@link #setEnabled(boolean)}
	 *         with <code>false</code>; <code>true</code> otherwise.
	 */
	public boolean isEnabledDisregardingCommand() {
		return enabled;
	}

	@Override
	public boolean isHandled() {
		final Command baseCommand = command.getCommand();
		return baseCommand.isHandled();
	}

	@Override
	public void run() {
		runWithEvent(null);
	}

	@Override
	public void runWithEvent(final Event event) {
		final Command baseCommand = command.getCommand();
		final ExecutionEvent executionEvent = new ExecutionEvent(command.getCommand(), command.getParameterMap(), event,
				null);
		try {
			baseCommand.execute(executionEvent);
			firePropertyChange(IAction.RESULT, null, Boolean.TRUE);

		} catch (final NotHandledException | ExecutionException e) {
			firePropertyChange(IAction.RESULT, null, Boolean.FALSE);
			// TODO Should this be logged?

		}
	}

	@Override
	public void setAccelerator(final int keycode) {
		// TODO Binding. This is hopefully not essential.
	}

	@Override
	public void setActionDefinitionId(final String id) {
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
		final ICommandService commandService = serviceLocator.getService(ICommandService.class);
		final Command newBaseCommand = commandService.getCommand(id);
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

		if (!Objects.equals(oldDescription, newDescription)) {
			firePropertyChange(IAction.DESCRIPTION, oldDescription, newDescription);
			firePropertyChange(IAction.TOOL_TIP_TEXT, oldDescription, newDescription);
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

		if (!Objects.equals(oldDefaultImage, newDefaultImage)) {
			firePropertyChange(IAction.IMAGE, oldDefaultImage, newDefaultImage);
		}

		if (!Objects.equals(oldDisabledImage, newDisabledImage)) {
			firePropertyChange(IAction.IMAGE, oldDisabledImage, newDisabledImage);
		}

		if (!Objects.equals(oldHoverImage, newHoverImage)) {
			firePropertyChange(IAction.IMAGE, oldHoverImage, newHoverImage);
		}

		if (!Objects.equals(oldText, newText)) {
			firePropertyChange(IAction.TEXT, oldText, newText);
		}
	}

	@Override
	public void setChecked(final boolean checked) {
		final State state = command.getCommand().getState(IMenuStateIds.STYLE);
		if (state instanceof ToggleState) {
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

	@Override
	public void setDescription(final String text) {
		final State state = command.getCommand().getState(INamedHandleStateIds.DESCRIPTION);
		if (state instanceof TextState) {
			final String currentValue = (String) state.getValue();
			if (!Objects.equals(text, currentValue)) {
				state.setValue(text);
			}
		}
	}

	@Override
	public void setDisabledImageDescriptor(final ImageDescriptor newImage) {
		final String commandId = getActionDefinitionId();
		final int type = CommandImageManager.TYPE_DISABLED;
		final ICommandImageService commandImageService = serviceLocator.getService(ICommandImageService.class);
		if (commandImageService instanceof CommandImageService) {
			((CommandImageService) commandImageService).bind(commandId, type, style, newImage);
		}
	}

	@Override
	public void setEnabled(final boolean enabled) {
		if (enabled != this.enabled) {
			final Boolean oldValue = this.enabled ? Boolean.TRUE : Boolean.FALSE;
			final Boolean newValue = enabled ? Boolean.TRUE : Boolean.FALSE;
			this.enabled = enabled;
			firePropertyChange(ENABLED, oldValue, newValue);
		}
	}

	@Override
	public void setHelpListener(final HelpListener listener) {
		// TODO Help Haven't even started to look at help yet.

	}

	@Override
	public void setHoverImageDescriptor(final ImageDescriptor newImage) {
		final String commandId = getActionDefinitionId();
		final int type = CommandImageManager.TYPE_HOVER;
		final ICommandImageService commandImageService = serviceLocator.getService(ICommandImageService.class);
		if (commandImageService instanceof CommandImageService) {
			((CommandImageService) commandImageService).bind(commandId, type, style, newImage);
		}
	}

	@Override
	public void setId(final String id) {
		this.id = id;
	}

	@Override
	public void setImageDescriptor(final ImageDescriptor newImage) {
		final String commandId = getActionDefinitionId();
		final int type = CommandImageManager.TYPE_DEFAULT;
		final ICommandImageService commandImageService = serviceLocator.getService(ICommandImageService.class);
		if (commandImageService instanceof CommandImageService) {
			((CommandImageService) commandImageService).bind(commandId, type, style, newImage);
		}
	}

	@Override
	public void setMenuCreator(final IMenuCreator creator) {
		// TODO Pulldown. This is complicated
	}

	@Override
	public void setText(final String text) {
		final State state = command.getCommand().getState(INamedHandleStateIds.NAME);
		if (state instanceof TextState) {
			final String currentValue = (String) state.getValue();
			if (!Objects.equals(text, currentValue)) {
				state.setValue(text);
			}
		}
	}

	@Override
	public void setToolTipText(final String text) {
		setDescription(text);
	}

	void dispose() {
		serviceLocator = null;
	}
}
