/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.commands;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.commands.common.NamedHandleObject;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.internal.commands.util.Util;

/**
 * <p>
 * A command is an abstract representation for some semantic behaviour. It is
 * not the actual implementation of this behaviour, nor is it the visual
 * appearance of this behaviour in the user interface. Instead, it is a bridge
 * between the two.
 * </p>
 * <p>
 * The concept of a command is based on the command design pattern. The notable
 * difference is how the command delegates responsibility for execution. Rather
 * than allowing concrete subclasses, it uses a handler mechanism (see the
 * <code>handlers</code> extension point). This provides another level of
 * indirection.
 * </p>
 * <p>
 * A command will exist in two states: defined and undefined. A command is
 * defined if it is declared in the XML of a resolved plug-in. If the plug-in is
 * unloaded or the command is simply not declared, then it is undefined. Trying
 * to reference an undefined command will succeed, but trying to access any of
 * its functionality will fail with a <code>NotDefinedException</code>. If
 * you need to know when a command changes from defined to undefined (or vice
 * versa), then attach a command listener.
 * </p>
 * <p>
 * Commands are mutable and will change as their definition changes.
 * </p>
 * 
 * @since 3.1
 */
public final class Command extends NamedHandleObject implements Comparable {

	/**
	 * This flag can be set to <code>true</code> if commands should print
	 * information to <code>System.out</code> when executing.
	 */
	public static boolean DEBUG_COMMAND_EXECUTION = false;

	/**
	 * This flag can be set to <code>true</code> if commands should print
	 * information to <code>System.out</code> when changing handlers.
	 */
	public static boolean DEBUG_HANDLERS = false;

	/**
	 * This flag can be set to a particular command identifier if only that
	 * command should print information to <code>System.out</code> when
	 * changing handlers.
	 */
	public static String DEBUG_HANDLERS_COMMAND_ID = null;

	/**
	 * The category to which this command belongs. This value should not be
	 * <code>null</code> unless the command is undefined.
	 */
	private Category category = null;

	/**
	 * A collection of objects listening to changes to this command. This
	 * collection is <code>null</code> if there are no listeners.
	 */
	private transient Collection commandListeners = null;

	/**
	 * A collection of objects listening to the execution of this command. This
	 * collection is <code>null</code> if there are no listeners.
	 */
	private transient Collection executionListeners = null;

	/**
	 * The handler currently associated with this command. This value may be
	 * <code>null</code> if there is no handler currently.
	 */
	private transient IHandler handler = null;

	/**
	 * The ordered array of parameters understood by this command. This value
	 * may be <code>null</code> if there are no parameters, or if the command
	 * is undefined. It may also be empty.
	 */
	private IParameter[] parameters = null;

	/**
	 * Constructs a new instance of <code>Command</code> based on the given
	 * identifier. When a command is first constructed, it is undefined.
	 * Commands should only be constructed by the <code>CommandManager</code>
	 * to ensure that the identifier remains unique.
	 * 
	 * @param id
	 *            The identifier for the command. This value must not be
	 *            <code>null</code>, and must be unique amongst all commands.
	 */
	Command(final String id) {
		super(id);
	}

	/**
	 * Adds a listener to this command that will be notified when this command's
	 * state changes.
	 * 
	 * @param commandListener
	 *            The listener to be added; must not be <code>null</code>.
	 */
	public final void addCommandListener(final ICommandListener commandListener) {
		if (commandListener == null) {
			throw new NullPointerException("Cannot add a null command listener"); //$NON-NLS-1$
		}

		if (commandListeners == null) {
			commandListeners = new ArrayList(1);
		} else if (commandListeners.contains(commandListener)) {
			return;
		}

		commandListeners.add(commandListener);
	}

	/**
	 * Adds a listener to this command that will be notified when this command
	 * is about to execute.
	 * 
	 * @param executionListener
	 *            The listener to be added; must not be <code>null</code>.
	 */
	public final void addExecutionListener(
			final IExecutionListener executionListener) {
		if (executionListener == null) {
			throw new NullPointerException(
					"Cannot add a null execution listener"); //$NON-NLS-1$
		}

		if (executionListeners == null) {
			executionListeners = new ArrayList(1);
		} else if (executionListeners.contains(executionListener)) {
			return;
		}

		executionListeners.add(executionListener);
	}

	/**
	 * Compares this command with another command by comparing each of its
	 * non-transient attributes.
	 * 
	 * @param object
	 *            The object with which to compare; must be an instance of
	 *            <code>Command</code>.
	 * @return A negative integer, zero or a postivie integer, if the object is
	 *         greater than, equal to or less than this command.
	 */
	public final int compareTo(final Object object) {
		final Command castedObject = (Command) object;
		int compareTo = Util.compare(category, castedObject.category);
		if (compareTo == 0) {
			compareTo = Util.compare(defined, castedObject.defined);
			if (compareTo == 0) {
				compareTo = Util.compare(description, castedObject.description);
				if (compareTo == 0) {
					compareTo = Util.compare(handler, castedObject.handler);
					if (compareTo == 0) {
						compareTo = Util.compare(id, castedObject.id);
						if (compareTo == 0) {
							compareTo = Util.compare(name, castedObject.name);
							if (compareTo == 0) {
								compareTo = Util.compare(parameters,
										castedObject.parameters);
							}
						}
					}
				}
			}
		}
		return compareTo;
	}

	/**
	 * <p>
	 * Defines this command by giving it a name, and possibly a description as
	 * well. The defined property automatically becomes <code>true</code>.
	 * </p>
	 * <p>
	 * Notification is sent to all listeners that something has changed.
	 * </p>
	 * 
	 * @param name
	 *            The name of this command; must not be <code>null</code>.
	 * @param description
	 *            The description for this command; may be <code>null</code>.
	 * @param category
	 *            The category for this command; may be <code>null</code>.
	 * @param parameters
	 *            The parameters understood by this command. This value may be
	 *            either <code>null</code> or empty if the command does not
	 *            accept parameters.
	 */
	public final void define(final String name, final String description,
			final Category category, final IParameter[] parameters) {
		if (name == null) {
			throw new NullPointerException(
					"The name of a command cannot be null"); //$NON-NLS-1$
		}

		if (category == null) {
			throw new NullPointerException(
					"The category of a command cannot be null"); //$NON-NLS-1$
		}

		final boolean definedChanged = !this.defined;
		this.defined = true;

		final boolean nameChanged = !Util.equals(this.name, name);
		this.name = name;

		final boolean descriptionChanged = !Util.equals(this.description,
				description);
		this.description = description;

		final boolean categoryChanged = !Util.equals(this.category, category);
		this.category = category;

		final boolean parametersChanged = !Util.equals(this.parameters,
				parameters);
		this.parameters = parameters;

		fireCommandChanged(new CommandEvent(this, categoryChanged,
				definedChanged, descriptionChanged, false, nameChanged,
				parametersChanged));
	}

	/**
     * Executes this command by delegating to the current handler, if any. If
     * the debugging flag is set, then this method prints information about
     * which handler is selected for performing this command.
     * 
     * @param event
     *            An event containing all the information about the current
     *            state of the application; must not be <code>null</code>.
     * @return The result of the execution; may be <code>null</code>. This
     *         result will be available to the client executing the command, and
     *         execution listeners.
     * @throws ExecutionException
     *             If the handler has problems executing this command.
     * @throws NotHandledException
     *             If there is no handler.
     */
	public final Object execute(final ExecutionEvent event)
			throws ExecutionException, NotHandledException {
		firePreExecute(event);
		final IHandler handler = this.handler;

		// Debugging output
		if (DEBUG_COMMAND_EXECUTION) {
			System.out.print("COMMANDS >>> executing "); //$NON-NLS-1$ 
			if (handler == null) {
				System.out.print("no handler"); //$NON-NLS-1$
			} else {
				System.out.print('\'');
				System.out.print(handler.getClass().getName());
				System.out.print("'("); //$NON-NLS-1$" +
				System.out.print(handler.hashCode());
				System.out.print(')');
			}
			System.out.println();
		}

		// Perform the execution, if there is a handler.
		if ((handler != null) && (handler.isHandled())) {
			try {
				final Object returnValue = handler.execute(event);
				firePostExecuteSuccess(returnValue);
				return returnValue;
			} catch (final ExecutionException e) {
				firePostExecuteFailure(e);
				throw e;
			}
		}

		final NotHandledException e = new NotHandledException(
				"There is no handler to execute."); //$NON-NLS-1$
		fireNotHandled(e);
		throw e;
	}

	/**
     * Notifies the listeners for this command that it has changed in some way.
     * 
     * @param commandEvent
     *            The event to send to all of the listener; must not be
     *            <code>null</code>.
     */
    private final void fireCommandChanged(final CommandEvent commandEvent) {
        if (commandEvent == null) {
            throw new NullPointerException("Cannot fire a null event"); //$NON-NLS-1$
        }

        if (commandListeners != null) {
            final int commandListenersSize = commandListeners.size();
            if (commandListenersSize > 0) {
                /*
                 * Bug 88629. Copying to an array avoids a
                 * ConcurrentModificationException if someone tries to remove
                 * the listener while handling the event.
                 */
                final ICommandListener[] listeners = (ICommandListener[]) commandListeners
                        .toArray(new ICommandListener[commandListenersSize]);
                for (int i = 0; i < commandListenersSize; i++) {
                    final ICommandListener listener = listeners[i];
                    listener.commandChanged(commandEvent);
                }
            }
        }
    }

    /**
     * Notifies the execution listeners for this command that an attempt to
     * execute has failed because there is no handler.
     * 
     * @param e
     *            The exception that is about to be thrown; never
     *            <code>null</code>.
     */
    private final void fireNotHandled(final NotHandledException e) {
        if (executionListeners != null) {
            final int executionListenersSize = executionListeners.size();
            if (executionListenersSize > 0) {
                /*
                 * Bug 88629. Copying to an array avoids a
                 * ConcurrentModificationException if someone tries to remove
                 * the listener while handling the event.
                 */
                final IExecutionListener[] listeners = (IExecutionListener[]) executionListeners
                        .toArray(new IExecutionListener[executionListenersSize]);
                for (int i = 0; i < executionListenersSize; i++) {
                    final IExecutionListener listener = listeners[i];
                    listener.notHandled(getId(), e);
                }
            }
        }
    }

    /**
     * Notifies the execution listeners for this command that an attempt to
     * execute has failed during the execution.
     * 
     * @param e
     *            The exception that has been thrown; never <code>null</code>.
     *            After this method completes, the exception will be thrown
     *            again.
     */
    private final void firePostExecuteFailure(final ExecutionException e) {
        if (executionListeners != null) {
            final int executionListenersSize = executionListeners.size();
            if (executionListenersSize > 0) {
                /*
                 * Bug 88629. Copying to an array avoids a
                 * ConcurrentModificationException if someone tries to remove
                 * the listener while handling the event.
                 */
                final IExecutionListener[] listeners = (IExecutionListener[]) executionListeners
                        .toArray(new IExecutionListener[executionListenersSize]);
                for (int i = 0; i < executionListenersSize; i++) {
                    final IExecutionListener listener = listeners[i];
                    listener.postExecuteFailure(getId(), e);
                }
            }
        }
    }

    /**
     * Notifies the execution listeners for this command that an execution has
     * completed successfully.
     * 
     * @param returnValue
     *            The return value from the command; may be <code>null</code>.
     */
    private final void firePostExecuteSuccess(final Object returnValue) {
        if (executionListeners != null) {
            final int executionListenersSize = executionListeners.size();
            if (executionListenersSize > 0) {
                /*
                 * Bug 88629. Copying to an array avoids a
                 * ConcurrentModificationException if someone tries to remove
                 * the listener while handling the event.
                 */
                final IExecutionListener[] listeners = (IExecutionListener[]) executionListeners
                        .toArray(new IExecutionListener[executionListenersSize]);
                for (int i = 0; i < executionListenersSize; i++) {
                    final IExecutionListener listener = listeners[i];
                    listener.postExecuteSuccess(getId(), returnValue);
                }
            }
        }
    }

	/**
	 * Notifies the execution listeners for this command that an attempt to
	 * execute is about to start.
	 * 
	 * @param event
	 *            The execution event that will be used; never <code>null</code>.
	 */
	private final void firePreExecute(final ExecutionEvent event) {
        if (executionListeners != null) {
            final int executionListenersSize = executionListeners.size();
            if (executionListenersSize > 0) {
                /*
                 * Bug 88629. Copying to an array avoids a
                 * ConcurrentModificationException if someone tries to remove
                 * the listener while handling the event.
                 */
                final IExecutionListener[] listeners = (IExecutionListener[]) executionListeners
                        .toArray(new IExecutionListener[executionListenersSize]);
                for (int i = 0; i < executionListenersSize; i++) {
                    final IExecutionListener listener = listeners[i];
                    listener.preExecute(getId(), event);
                }
            }
        }
	}

	/**
	 * Returns the category for this command.
	 * 
	 * @return The category for this command; never <code>null</code>.
	 * @throws NotDefinedException
	 *             If the handle is not currently defined.
	 */
	public final Category getCategory() throws NotDefinedException {
		if (!isDefined()) {
			throw new NotDefinedException(
					"Cannot get the category from an undefined command"); //$NON-NLS-1$
		}

		return category;
	}

	/**
	 * Returns the parameters for this command. This call triggers provides a
	 * copy of the array, so excessive calls to this method should be avoided.
	 * 
	 * @return The parameters for this command. This value might be
	 *         <code>null</code>, if the command has no parameters.
	 * @throws NotDefinedException
	 *             If the handle is not currently defined.
	 */
	public final IParameter[] getParameters() throws NotDefinedException {
		if (!isDefined()) {
			throw new NotDefinedException(
					"Cannot get the parameters from an undefined command"); //$NON-NLS-1$
		}

		if ((parameters == null) || (parameters.length == 0)) {
			return null;
		}

		final IParameter[] returnValue = new IParameter[parameters.length];
		System.arraycopy(parameters, 0, returnValue, 0, parameters.length);
		return returnValue;
	}

	/**
	 * Returns whether this command has a handler, and whether this handler is
	 * also handled and enabled.
	 * 
	 * @return <code>true</code> if the command is handled; <code>false</code>
	 *         otherwise.
	 */
	public final boolean isEnabled() {
		if (handler == null) {
			return false;
		}

		return handler.isHandled() && handler.isEnabled();
	}

	/**
	 * Returns whether this command has a handler, and whether this handler is
	 * also handled.
	 * 
	 * @return <code>true</code> if the command is handled; <code>false</code>
	 *         otherwise.
	 */
	public final boolean isHandled() {
		if (handler == null)
			return false;

		return handler.isHandled();
	}

	/**
	 * Removes a listener from this command.
	 * 
	 * @param commandListener
	 *            The listener to be removed; must not be <code>null</code>.
	 * 
	 */
	public final void removeCommandListener(
			final ICommandListener commandListener) {
		if (commandListener == null) {
			throw new NullPointerException(
					"Cannot remove a null command listener"); //$NON-NLS-1$
		}

		if (commandListeners != null) {
			commandListeners.remove(commandListener);
		}
	}

	/**
	 * Removes a listener from this command.
	 * 
	 * @param executionListener
	 *            The listener to be removed; must not be <code>null</code>.
	 * 
	 */
	public final void removeExecutionListener(
			final IExecutionListener executionListener) {
		if (executionListener == null) {
			throw new NullPointerException(
					"Cannot remove a null execution listener"); //$NON-NLS-1$
		}

		if (executionListeners != null) {
			executionListeners.remove(executionListener);
		}
	}

	/**
	 * Changes the handler for this command. If debugging is turned on, then
	 * this will also print information about the change to
	 * <code>System.out</code>.
	 * 
	 * @param handler
	 *            The new handler; may be <code>null</code> if none.
	 * @return <code>true</code> if the handler changed; <code>false</code>
	 *         otherwise.
	 */
	public final boolean setHandler(final IHandler handler) {
		if (Util.equals(handler, this.handler)) {
			return false;
		}

		// Update the handler, and flush the string representation.
		this.handler = handler;
		string = null;

		// Debugging output
		if ((DEBUG_HANDLERS)
				&& ((DEBUG_HANDLERS_COMMAND_ID == null) || (DEBUG_HANDLERS_COMMAND_ID
						.equals(id)))) {
			System.out.print("HANDLERS >>> Command('" + id //$NON-NLS-1$
					+ "') has changed to "); //$NON-NLS-1$
			if (handler == null) {
				System.out.println("no handler"); //$NON-NLS-1$
			} else {
				System.out.print("'"); //$NON-NLS-1$
				System.out.print(handler);
				System.out.println("' as its handler"); //$NON-NLS-1$
			}
		}

		// Send notification
		fireCommandChanged(new CommandEvent(this, false, false, false, true,
				false, false));

		return true;
	}

	/**
	 * The string representation of this command -- for debugging purposes only.
	 * This string should not be shown to an end user.
	 * 
	 * @return The string representation; never <code>null</code>.
	 */
	public final String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("Command("); //$NON-NLS-1$
            stringBuffer.append(id);
            stringBuffer.append(',');
            stringBuffer.append(name);
            stringBuffer.append(',');
            stringBuffer.append(description);
            stringBuffer.append(',');
			stringBuffer.append(category);
			stringBuffer.append(',');
			stringBuffer.append(handler);
			stringBuffer.append(',');
			stringBuffer.append(parameters);
            stringBuffer.append(',');
            stringBuffer.append(defined);
			stringBuffer.append(')');
			string = stringBuffer.toString();
		}
		return string;
	}

	/**
	 * Makes this command become undefined. This has the side effect of changing
	 * the name and description to <code>null</code>. Notification is sent to
	 * all listeners.
	 */
	public final void undefine() {
		string = null;

		final boolean definedChanged = defined;
		defined = false;

		final boolean nameChanged = name != null;
		name = null;

		final boolean descriptionChanged = description != null;
		description = null;

		final boolean categoryChanged = category != null;
		category = null;

		final boolean parametersChanged = parameters != null;
		parameters = null;

		fireCommandChanged(new CommandEvent(this, categoryChanged,
				definedChanged, descriptionChanged, false, nameChanged,
				parametersChanged));
	}
}
