/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.commands;

import java.util.Collection;

import org.eclipse.swt.widgets.Shell;

/**
 * An instance of this interface provides support for managing commands at the
 * <code>IWorkbench</code> level.
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public interface IWorkbenchCommandSupport {

    /**
     * TODO
     * 
     * @param handlerSubmission
     */
    void addHandlerSubmission(HandlerSubmission handlerSubmission);

    /**
     * TODO
     * 
     * @param handlerSubmissions
     */
    void addHandlerSubmissions(Collection handlerSubmissions);

    /**
     * Deregisters the given <code>shell</code> from the global key bindings.
     * This is not strictly necessary (as the internal storage uses a weak hash
     * map), but is good for cleanliness.
     * 
     * @param shell
     *            The shell to deregister; may be <code>null</code>.
     * @deprecated to be removed for 3.0 Use the equivalent methods on IWorkbenchContextSupport
     * @see org.eclipse.ui.contexts.IWorkbenchContextSupport#unregisterShell(Shell)
     */
    void deregisterFromKeyBindings(Shell shell);

    /**
     * Returns the command manager for the workbench.
     * 
     * @return the command manager for the workbench. Guaranteed not to be
     *         <code>null</code>.
     */
    ICommandManager getCommandManager();

    /**
     * Tests whether the global key binding architecture is currently active.
     * 
     * @return <code>true</code> if the key bindings are active;
     *         <code>false</code> otherwise.
     * @deprecated Please use the equivalent method on IWorkbenchContextSupport
     * @see org.eclipse.ui.contexts.IWorkbenchContextSupport#isKeyFilterEnabled()
     */
    boolean isKeyFilterEnabled();

    /**
     * Indicates that the given <code>shell</code> wishes to participate in
     * the global key binding architecture. This means that key and traversal
     * events might be intercepted before reaching any of the widgets on the
     * shell.
     * 
     * @param shell
     *            The shell to register for key bindings; may be
     *            <code>null</code>.
     * @param dialogOnly
     *            Whether the shell only wants the restricted set of key
     *            bindings normally used in dialogs (e.g., text editing
     *            commands). All workbench windows say <code>false</code>
     *            here.
     * @deprecated to be removed for 3.0. Use the equivalent methods on IWorkbenchContextSupport
     * @see org.eclipse.ui.contexts.IWorkbenchContextSupport#registerShell(Shell,
     *      int)
     */
    void registerForKeyBindings(Shell shell, boolean dialogOnly);

    /**
     * TODO
     * 
     * @param handlerSubmission
     */
    void removeHandlerSubmission(HandlerSubmission handlerSubmission);

    /**
     * TODO
     * 
     * @param handlerSubmissions
     */
    void removeHandlerSubmissions(Collection handlerSubmissions);

    /**
     * Enables or disables the global key binding architecture. The architecture
     * should be enabled by default.
     * 
     * When enabled, keyboard shortcuts are active, and that key events can
     * trigger commands. This also means that widgets may not see all key events
     * (as they might be trapped as a keyboard shortcut).
     * 
     * When disabled, no key events will trapped as keyboard shortcuts, and that
     * no commands can be triggered by keyboard events. (Exception: it is
     * possible that someone listening for key events on a widget could trigger
     * a command.)
     * 
     * @param enabled
     *            Whether the key filter should be enabled.
     * 
     * @deprecated Please use the equivalent method on IWorkbenchContextSupport
     * @see org.eclipse.ui.contexts.IWorkbenchContextSupport#setKeyFilterEnabled(boolean)
     */
    void setKeyFilterEnabled(boolean enabled);
}