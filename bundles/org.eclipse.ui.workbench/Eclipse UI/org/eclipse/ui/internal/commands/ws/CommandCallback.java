/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.commands.ws;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ExternalActionManager.ICallback;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.commands.CommandEvent;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.ICommandListener;
import org.eclipse.ui.commands.ICommandManager;
import org.eclipse.ui.commands.IKeySequenceBinding;
import org.eclipse.ui.commands.NotDefinedException;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.KeySequence;
import org.eclipse.ui.keys.KeyStroke;
import org.eclipse.ui.keys.SWTKeySupport;

/**
 * @since 3.0
 */
public final class CommandCallback implements ICallback {

    /**
     * The internationalization bundle for text produced by this class.
     */
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
            .getBundle(CommandCallback.class.getName());

    /**
     * The list of listeners that have registered for property change
     * notification. This is a map os <code>IPropertyChangeListener</code> to
     * <code>ICommandListener</code>.
     */
    private final Map registeredListeners = new HashMap();

    /**
     * The workbench to query about command and context information. This value
     * should never be <code>null</code>.
     */
    private final IWorkbench workbench;

    /**
     * Constructs a new instance of <code>CommandCallback</code> with the
     * workbench it should be using.
     * 
     * @param workbenchToUse
     *            The workbench that should be used for resolving command
     *            information; must not be <code>null</code>.
     */
    public CommandCallback(final IWorkbench workbenchToUse) {
        workbench = workbenchToUse;
    }

    /**
     * @see org.eclipse.jface.action.ExternalActionManager.ICallback#addPropertyChangeListener(String,
     *      IPropertyChangeListener)
     */
    public void addPropertyChangeListener(final String commandId,
            final IPropertyChangeListener listener) {
        final ICommand command = workbench.getCommandSupport()
                .getCommandManager().getCommand(commandId);
        final ICommandListener commandListener = new ICommandListener() {

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.ui.commands.ICommandListener#commandChanged(org.eclipse.ui.commands.CommandEvent)
             */
            public void commandChanged(CommandEvent commandEvent) {
                // Check if the text has changed.
                if (commandEvent.hasNameChanged()
                        || commandEvent.haveKeySequenceBindingsChanged()) {
                    PropertyChangeEvent event;
                    try {
                        event = new PropertyChangeEvent(command, IAction.TEXT,
                                null /* TODO Don't have enough information */,
                                command.getName());
                    } catch (final NotDefinedException e) {
                        event = new PropertyChangeEvent(command, IAction.TEXT,
                                null /* TODO Don't have enough information */,
                                null /* Couldn't get the name */);
                    }
                    listener.propertyChange(event);
                }

                // TODO Add enabled property change.
            }
        };

        command.addCommandListener(commandListener);
        Assert.isTrue(!registeredListeners.containsKey(listener));
        registeredListeners.put(listener, commandListener);
    }

    /**
     * @see org.eclipse.jface.action.ExternalActionManager.ICallback#getAccelerator(String)
     */
    public final Integer getAccelerator(final String commandId) {
        final ICommand command = workbench.getCommandSupport()
                .getCommandManager().getCommand(commandId);
        Integer accelerator = null;

        if (command.isDefined()) {
            List keySequenceBindings = command.getKeySequenceBindings();
            final int size = keySequenceBindings.size();

            for (int i = 0; i < size; i++) {
                IKeySequenceBinding keySequenceBinding = (IKeySequenceBinding) keySequenceBindings
                        .get(i);
                List keyStrokes = keySequenceBinding.getKeySequence()
                        .getKeyStrokes();

                if (keyStrokes.size() == 1) {
                    KeyStroke keyStroke = (KeyStroke) keyStrokes.get(0);
                    accelerator = new Integer(SWTKeySupport
                            .convertKeyStrokeToAccelerator(keyStroke));
                    break;
                }
            }
        }

        return accelerator;
    }

    /**
     * @see org.eclipse.jface.action.ExternalActionManager.ICallback#getAcceleratorText(String)
     */
    public final String getAcceleratorText(final String commandId) {
        final ICommand command = workbench.getCommandSupport()
                .getCommandManager().getCommand(commandId);
        String acceleratorText = null;

        if (command.isDefined()) {
            List keySequenceBindings = command.getKeySequenceBindings();

            if (!keySequenceBindings.isEmpty()) {
                IKeySequenceBinding keySequenceBinding = (IKeySequenceBinding) keySequenceBindings
                        .get(0);
                acceleratorText = keySequenceBinding.getKeySequence().format();
            }
        }

        return acceleratorText;
    }

    /**
     * @see org.eclipse.jface.action.ExternalActionManager.ICallback#isAcceleratorInUse(int)
     */
    public boolean isAcceleratorInUse(int accelerator) {
        final KeySequence keySequence = KeySequence.getInstance(SWTKeySupport
                .convertAcceleratorToKeyStroke(accelerator));
        final ICommandManager commandManager = workbench.getCommandSupport()
                .getCommandManager();
        return commandManager.isPerfectMatch(keySequence)
                || commandManager.isPartialMatch(keySequence);
    }

    /**
     * Calling this method with an undefined command id will generate a log
     * message.
     * 
     * @see org.eclipse.jface.action.ExternalActionManager.ICallback#isActive(String)
     */
    public final boolean isActive(final String commandId) {
        if (commandId != null) {
            final ICommand command = workbench.getCommandSupport()
                    .getCommandManager().getCommand(commandId);

            if (!command.isDefined()) {
                // The command is not yet defined, so we should log this.
                final StringBuffer message = new StringBuffer();
                message.append("The command '"); //$NON-NLS-1$
                message.append(command.getId());
                message
                        .append("' is not defined, but is being asked if it is active.  Are you using an actionDefinitionId without defining a command?"); //$NON-NLS-1$
                IStatus status = new Status(IStatus.ERROR,
                        WorkbenchPlugin.PI_WORKBENCH, 0, Util.translateString(
                                RESOURCE_BUNDLE,
                                "undefinedCommand.WarningMessage"), null); //$NON-NLS-1$
                WorkbenchPlugin.log(message.toString(), status);
                return false;
            }

            return workbench.getActivitySupport().getActivityManager()
                    .getIdentifier(command.getId()).isEnabled();
        }

        return true;
    }

    /**
     * @see org.eclipse.jface.action.ExternalActionManager.ICallback#removePropertyChangeListener(String,
     *      IPropertyChangeListener)
     */
    public final void removePropertyChangeListener(final String commandId,
            final IPropertyChangeListener listener) {
        final ICommand command = workbench.getCommandSupport()
                .getCommandManager().getCommand(commandId);
        final Object associatedListener = registeredListeners.remove(listener);
        if (associatedListener instanceof ICommandListener) {
            final ICommandListener commandListener = (ICommandListener) associatedListener;
            command.removeCommandListener(commandListener);
        }
    }
}