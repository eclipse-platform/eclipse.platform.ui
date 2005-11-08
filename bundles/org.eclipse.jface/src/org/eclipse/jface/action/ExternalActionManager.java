/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.action;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandEvent;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.ICommandListener;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.BindingManagerEvent;
import org.eclipse.jface.bindings.IBindingManagerListener;
import org.eclipse.jface.bindings.Trigger;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.Util;

/**
 * <p>
 * A manager for a callback facility which is capable of querying external
 * interfaces for additional information about actions and action contribution
 * items. This information typically includes things like accelerators and
 * textual representations.
 * </p>
 * <p>
 * <em>It is only necessary to use this mechanism if you will be using a mix of
 * actions and commands, and wish the interactions to work properly.</em>
 * </p>
 * <p>
 * For example, in the Eclipse workbench, this mechanism is used to allow the
 * command architecture to override certain values in action contribution items.
 * </p>
 * <p>
 * This class is not intended to be called or extended by any external clients.
 * This API is still under flux, and is expected to change in 3.1.
 * </p>
 * 
 * @since 3.0
 */
public final class ExternalActionManager {

	/**
	 * A simple implementation of the <code>ICallback</code> mechanism that
	 * simply takes a <code>BindingManager</code> and a
	 * <code>CommandManager</code>.
	 * 
	 * @since 3.1
	 */
	public static final class CommandCallback implements
			IBindingManagerListener, IBindingManagerCallback {

		/**
		 * The internationalization bundle for text produced by this class.
		 */
		private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
				.getBundle(ExternalActionManager.class.getName());

		/**
		 * The callback capable of responding to whether a command is active.
		 */
		private final IActiveChecker activeChecker;

		/**
		 * The binding manager for your application. Must not be
		 * <code>null</code>.
		 */
		private final BindingManager bindingManager;

		/**
		 * Whether a listener has been attached to the binding manager yet.
		 */
		private boolean bindingManagerListenerAttached = false;

		/**
		 * The command manager for your application. Must not be
		 * <code>null</code>.
		 */
		private final CommandManager commandManager;

		/**
		 * A set of all the command identifiers that have been logged as broken
		 * so far. For each of these, there will be a listener on the
		 * corresponding command. If the command ever becomes defined, the item
		 * will be removed from this set and the listener removed. This value
		 * may be empty, but never <code>null</code>.
		 */
		private final Set loggedCommandIds = new HashSet();

		/**
		 * The list of listeners that have registered for property change
		 * notification. This is a map of command identifiers (<code>String</code>)
		 * to listeners (<code>IPropertyChangeListener</code>).
		 */
		private final Map registeredListeners = new HashMap();

		/**
		 * Constructs a new instance of <code>CommandCallback</code> with the
		 * workbench it should be using. All commands will be considered active.
		 * 
		 * @param bindingManager
		 *            The binding manager which will provide the callback; must
		 *            not be <code>null</code>.
		 * @param commandManager
		 *            The command manager which will provide the callback; must
		 *            not be <code>null</code>.
		 * 
		 * @since 3.1
		 */
		public CommandCallback(final BindingManager bindingManager,
				final CommandManager commandManager) {
			this(bindingManager, commandManager, new IActiveChecker() {
				public boolean isActive(String commandId) {
					return true;
				}

			});
		}

		/**
		 * Constructs a new instance of <code>CommandCallback</code> with the
		 * workbench it should be using.
		 * 
		 * @param bindingManager
		 *            The binding manager which will provide the callback; must
		 *            not be <code>null</code>.
		 * @param commandManager
		 *            The command manager which will provide the callback; must
		 *            not be <code>null</code>.
		 * @param activeChecker
		 *            The callback mechanism for checking whether a command is
		 *            active; must not be <code>null</code>.
		 * 
		 * @since 3.1
		 */
		public CommandCallback(final BindingManager bindingManager,
				final CommandManager commandManager,
				final IActiveChecker activeChecker) {
			if (bindingManager == null) {
				throw new NullPointerException(
						"The callback needs a binding manager"); //$NON-NLS-1$
			}

			if (commandManager == null) {
				throw new NullPointerException(
						"The callback needs a command manager"); //$NON-NLS-1$
			}

			if (activeChecker == null) {
				throw new NullPointerException(
						"The callback needs an active callback"); //$NON-NLS-1$
			}

			this.activeChecker = activeChecker;
			this.bindingManager = bindingManager;
			this.commandManager = commandManager;
		}

		/**
		 * @see org.eclipse.jface.action.ExternalActionManager.ICallback#addPropertyChangeListener(String,
		 *      IPropertyChangeListener)
		 */
		public final void addPropertyChangeListener(final String commandId,
				final IPropertyChangeListener listener) {
			registeredListeners.put(commandId, listener);
			if (!bindingManagerListenerAttached) {
				bindingManager.addBindingManagerListener(this);
				bindingManagerListenerAttached = true;
			}
		}

		public final void bindingManagerChanged(final BindingManagerEvent event) {
			if (event.isActiveBindingsChanged()) {
				final Iterator listenerItr = registeredListeners.entrySet()
						.iterator();
				while (listenerItr.hasNext()) {
					final Map.Entry entry = (Map.Entry) listenerItr.next();
					final String commandId = (String) entry.getKey();
					final Command command = commandManager
							.getCommand(commandId);
					final ParameterizedCommand parameterizedCommand = new ParameterizedCommand(
							command, null);
					if (event.isActiveBindingsChangedFor(parameterizedCommand)) {
						final IPropertyChangeListener listener = (IPropertyChangeListener) entry
								.getValue();
						listener.propertyChange(new PropertyChangeEvent(event
								.getManager(), IAction.TEXT, null, null));
					}
				}
			}
		}

		/**
		 * @see org.eclipse.jface.action.ExternalActionManager.ICallback#getAccelerator(String)
		 */
		public final Integer getAccelerator(final String commandId) {
			final TriggerSequence triggerSequence = bindingManager
					.getBestActiveBindingFor(commandId);
			if (triggerSequence != null) {
				final Trigger[] triggers = triggerSequence.getTriggers();
				if (triggers.length == 1) {
					final Trigger trigger = triggers[0];
					if (trigger instanceof KeyStroke) {
						final KeyStroke keyStroke = (KeyStroke) trigger;
						final int accelerator = SWTKeySupport
								.convertKeyStrokeToAccelerator(keyStroke);
						return new Integer(accelerator);
					}
				}
			}

			return null;
		}

		/**
		 * @see org.eclipse.jface.action.ExternalActionManager.ICallback#getAcceleratorText(String)
		 */
		public final String getAcceleratorText(final String commandId) {
			final TriggerSequence triggerSequence = bindingManager
					.getBestActiveBindingFor(commandId);
			if (triggerSequence == null) {
				return null;
			}

			return triggerSequence.format();
		}

		/**
		 * Returns the active bindings for a particular command identifier.
		 * 
		 * @param commandId
		 *            The identifier of the command whose bindings are
		 *            requested. This argument may be <code>null</code>. It
		 *            is assumed that the command has no parameters.
		 * @return The array of active triggers (<code>TriggerSequence</code>)
		 *         for a particular command identifier. This value is guaranteed
		 *         not to be <code>null</code>, but it may be empty.
		 * @since 3.2
		 */
		public final TriggerSequence[] getActiveBindingsFor(
				final String commandId) {
			return bindingManager.getActiveBindingsFor(commandId);
		}

		/**
		 * @see org.eclipse.jface.action.ExternalActionManager.ICallback#isAcceleratorInUse(int)
		 */
		public final boolean isAcceleratorInUse(final int accelerator) {
			final KeySequence keySequence = KeySequence
					.getInstance(SWTKeySupport
							.convertAcceleratorToKeyStroke(accelerator));
			return bindingManager.isPerfectMatch(keySequence)
					|| bindingManager.isPartialMatch(keySequence);
		}

		/**
		 * Calling this method with an undefined command id will generate a log
		 * message.
		 * 
		 * @see org.eclipse.jface.action.ExternalActionManager.ICallback#isActive(String)
		 */
		public final boolean isActive(final String commandId) {
			if (commandId != null) {
				final Command command = commandManager.getCommand(commandId);

				if (!command.isDefined()
						&& (!loggedCommandIds.contains(commandId))) {
					// The command is not yet defined, so we should log this.
					final String message = MessageFormat.format(Util
							.translateString(RESOURCE_BUNDLE,
									"undefinedCommand.WarningMessage", null), //$NON-NLS-1$
							new String[] { command.getId() });
					IStatus status = new Status(IStatus.ERROR,
							"org.eclipse.jface", //$NON-NLS-1$
							0, message, new Exception());
					Policy.getLog().log(status);

					// And remember this item so we don't log it again.
					loggedCommandIds.add(commandId);
					command.addCommandListener(new ICommandListener() {
						/*
						 * (non-Javadoc)
						 * 
						 * @see org.eclipse.ui.commands.ICommandListener#commandChanged(org.eclipse.ui.commands.CommandEvent)
						 */
						public final void commandChanged(
								final CommandEvent commandEvent) {
							if (command.isDefined()) {
								command.removeCommandListener(this);
								loggedCommandIds.remove(commandId);
							}
						}
					});

					return true;
				}

				return activeChecker.isActive(commandId);
			}

			return true;
		}

		/**
		 * @see org.eclipse.jface.action.ExternalActionManager.ICallback#removePropertyChangeListener(String,
		 *      IPropertyChangeListener)
		 */
		public final void removePropertyChangeListener(final String commandId,
				final IPropertyChangeListener listener) {
			final IPropertyChangeListener existingListener = (IPropertyChangeListener) registeredListeners
					.get(commandId);
			if (existingListener == listener) {
				registeredListeners.remove(commandId);
				if (registeredListeners.isEmpty()) {
					bindingManager.removeBindingManagerListener(this);
					bindingManagerListenerAttached = false;
				}
			}
		}
	}

	/**
	 * Defines a callback mechanism for developer who wish to further control
	 * the visibility of legacy action-based contribution items.
	 * 
	 * @since 3.1
	 */
	public static interface IActiveChecker {
		/**
		 * Checks whether the command with the given identifier should be
		 * considered active. This can be used in systems using some kind of
		 * user interface filtering (e.g., activities in the Eclipse workbench).
		 * 
		 * @param commandId
		 *            The identifier for the command; must not be
		 *            <code>null</code>
		 * @return <code>true</code> if the command is active;
		 *         <code>false</code> otherwise.
		 */
		public boolean isActive(String commandId);
	}

	/**
	 * <p>
	 * A callback which communicates with the applications binding manager. This
	 * interface provides more information from the binding manager, which
	 * allows greater integration. Implementing this interface is preferred over
	 * {@link ExternalActionManager.ICallback}.
	 * </p>
	 * <p>
	 * Clients may implement this interface, but must not extend.
	 * </p>
	 * <p>
	 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
	 * part of a work in progress. There is a guarantee neither that this API
	 * will work nor that it will remain the same. Please do not use this API
	 * without consulting with the Platform/UI team.
	 * </p>
	 * 
	 * @since 3.2
	 */
	public static interface IBindingManagerCallback extends ICallback {

		/**
		 * <p>
		 * Returns the active bindings for a particular command identifier.
		 * </p>
		 * 
		 * @param commandId
		 *            The identifier of the command whose bindings are
		 *            requested. This argument may be <code>null</code>. It
		 *            is assumed that the command has no parameters.
		 * @return The array of active triggers (<code>TriggerSequence</code>)
		 *         for a particular command identifier. This value is guaranteed
		 *         not to be <code>null</code>, but it may be empty.
		 */
		public TriggerSequence[] getActiveBindingsFor(String commandId);
	}

	/**
	 * A callback mechanism for some external tool to communicate extra
	 * information to actions and action contribution items.
	 * 
	 * @since 3.0
	 */
	public static interface ICallback {

		/**
		 * <p>
		 * Adds a listener to the object referenced by <code>identifier</code>.
		 * This listener will be notified if a property of the item is to be
		 * changed. This identifier is specific to mechanism being used. In the
		 * case of the Eclipse workbench, this is the command identifier.
		 * </p>
		 * <p>
		 * A single instance of the listener may only ever be associated with
		 * one identifier. Attempts to add the listener twice (without a removal
		 * inbetween) has undefined behaviour.
		 * </p>
		 * 
		 * @param identifier
		 *            The identifier of the item to which the listener should be
		 *            attached; must not be <code>null</code>.
		 * @param listener
		 *            The listener to be added; must not be <code>null</code>.
		 */
		public void addPropertyChangeListener(String identifier,
				IPropertyChangeListener listener);

		/**
		 * An accessor for the accelerator associated with the item indicated by
		 * the identifier. This identifier is specific to mechanism being used.
		 * In the case of the Eclipse workbench, this is the command identifier.
		 * 
		 * @param identifier
		 *            The identifier of the item from which the accelerator
		 *            should be obtained ; must not be <code>null</code>.
		 * @return An integer representation of the accelerator. This is the
		 *         same accelerator format used by SWT.
		 */
		public Integer getAccelerator(String identifier);

		/**
		 * An accessor for the accelerator text associated with the item
		 * indicated by the identifier. This identifier is specific to mechanism
		 * being used. In the case of the Eclipse workbench, this is the command
		 * identifier.
		 * 
		 * @param identifier
		 *            The identifier of the item from which the accelerator text
		 *            should be obtained ; must not be <code>null</code>.
		 * @return A string representation of the accelerator. This is the
		 *         string representation that should be displayed to the user.
		 */
		public String getAcceleratorText(String identifier);

		/**
		 * Checks to see whether the given accelerator is being used by some
		 * other mechanism (outside of the menus controlled by JFace). This is
		 * used to keep JFace from trying to grab accelerators away from someone
		 * else.
		 * 
		 * @param accelerator
		 *            The accelerator to check -- in SWT's internal accelerator
		 *            format.
		 * @return <code>true</code> if the accelerator is already being used
		 *         and shouldn't be used again; <code>false</code> otherwise.
		 */
		public boolean isAcceleratorInUse(int accelerator);

		/**
		 * Checks whether the item matching this identifier is active. This is
		 * used to decide whether a contribution item with this identifier
		 * should be made visible. An inactive item is not visible.
		 * 
		 * @param identifier
		 *            The identifier of the item from which the active state
		 *            should be retrieved; must not be <code>null</code>.
		 * @return <code>true</code> if the item is active; <code>false</code>
		 *         otherwise.
		 */
		public boolean isActive(String identifier);

		/**
		 * Removes a listener from the object referenced by
		 * <code>identifier</code>. This identifier is specific to mechanism
		 * being used. In the case of the Eclipse workbench, this is the command
		 * identifier.
		 * 
		 * @param identifier
		 *            The identifier of the item to from the listener should be
		 *            removed; must not be <code>null</code>.
		 * @param listener
		 *            The listener to be removed; must not be <code>null</code>.
		 */
		public void removePropertyChangeListener(String identifier,
				IPropertyChangeListener listener);
	}

	/**
	 * The singleton instance of this class. This value may be <code>null</code>--
	 * if it has not yet been initialized.
	 */
	private static ExternalActionManager instance;

	/**
	 * Retrieves the current singleton instance of this class.
	 * 
	 * @return The singleton instance; this value is never <code>null</code>.
	 */
	public static ExternalActionManager getInstance() {
		if (instance == null)
			instance = new ExternalActionManager();

		return instance;
	}

	/**
	 * The callback mechanism to use to retrieve extra information.
	 */
	private ICallback callback;

	/**
	 * Constructs a new instance of <code>ExternalActionManager</code>.
	 */
	private ExternalActionManager() {
		// This is a singleton class. Only this class should create an instance.
	}

	/**
	 * An accessor for the current call back.
	 * 
	 * @return The current callback mechanism being used. This is the callback
	 *         that should be queried for extra information about actions and
	 *         action contribution items. This value may be <code>null</code>
	 *         if there is no extra information.
	 */
	public ICallback getCallback() {
		return callback;
	}

	/**
	 * A mutator for the current call back
	 * 
	 * @param callbackToUse
	 *            The new callback mechanism to use; this value may be
	 *            <code>null</code> if the default is acceptable (i.e., no
	 *            extra information will provided to actions).
	 */
	public void setCallback(ICallback callbackToUse) {
		callback = callbackToUse;
	}
}
