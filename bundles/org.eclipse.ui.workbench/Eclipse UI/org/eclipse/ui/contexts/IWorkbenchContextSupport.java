/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.contexts;

import java.util.List;

import org.eclipse.swt.widgets.Shell;

/**
 * An instance of this interface provides support for managing contexts at the
 * <code>IWorkbench</code> level.
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * 
 * @since 3.0
 */
public interface IWorkbenchContextSupport {

    /**
     * The identifier for the context that is active when a shell registered as
     * a dialog.
     */
    public static final String CONTEXT_ID_DIALOG = "org.eclipse.ui.contexts.dialog"; //$NON-NLS-1$

    /**
     * The identifier for the context that is active when a shell is registered
     * as either a window or a dialog.
     */
    public static final String CONTEXT_ID_DIALOG_AND_WINDOW = "org.eclipse.ui.contexts.dialogAndWindow"; //$NON-NLS-1$

    /**
     * The identifier for the context that is active when a shell is registered
     * as a window.
     */
    public static final String CONTEXT_ID_WINDOW = "org.eclipse.ui.contexts.window"; //$NON-NLS-1$

    /**
     * The type used for registration indicating that the shell should be
     * treated as a dialog. When the given shell is active, the "In Dialogs"
     * context should also be active.
     */
    public static final int TYPE_DIALOG = 0;

    /**
     * The type used for registration indicating that the shell should not
     * receive any key bindings be default. When the given shell is active, we
     * should not provide any <code>EnabledSubmission</code> instances for the
     * "In Dialogs" or "In Windows" contexts.
     *  
     */
    public static final int TYPE_NONE = 1;

    /**
     * The type used for registration indicating that the shell should be
     * treated as a window. When the given shell is active, the "In Windows"
     * context should also be active.
     */
    public static final int TYPE_WINDOW = 2;

    /**
     * TODO
     * 
     * @param enabledSubmissions
     */
    void addEnabledSubmissions(List enabledSubmissions);

    /**
     * Returns the context manager for the workbench.
     * 
     * @return the context manager for the workbench. Guaranteed not to be
     *         <code>null</code>.
     */
    IContextManager getContextManager();

    /**
     * Tests whether the global key binding architecture is currently active.
     * 
     * @return <code>true</code> if the key bindings are active;
     *         <code>false</code> otherwise.
     */
    public boolean isKeyFilterEnabled();

    /**
     * <p>
     * Registers a shell to automatically promote or demote some basic types of
     * contexts. The "In Dialogs" and "In Windows" contexts are provided by the
     * system. This a convenience method to ensure that these contexts are
     * promoted when the given is shell is active.
     * </p>
     * <p>
     * If a shell is registered as a window, then the "In Windows" context is
     * enabled when that shell is active. If a shell is registered as a dialog --
     * or is not registered, but has a parent shell -- then the "In Dialogs"
     * context is enabled when that shell is active. If the shell is registered
     * as none -- or is not registered, but has no parent shell -- then the
     * neither of the contexts will be enabled (by us -- someone else can always
     * enabled them).
     * </p>
     * <p>
     * If the provided shell has already been registered, then this method will
     * change the registration.
     * </p>
     * 
     * @param shell
     *            The shell to register for key bindings; must not be
     *            <code>null</code>.
     * @param type
     *            The type of shell being registered. This value must be one of
     *            the constants given in this interface.
     * 
     * @return <code>true</code> if the shell had already been registered
     *         (i.e., the registration has changed); <code>false</code>
     *         otherwise.
     */
    public boolean registerShell(final Shell shell, final int type);

    /**
     * TODO
     * 
     * @param enabledSubmissions
     */
    void removeEnabledSubmissions(List enabledSubmissions);

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
     */
    public void setKeyFilterEnabled(final boolean enabled);

    /**
     * <p>
     * Unregisters a shell that was previously registered. After this method
     * completes, the shell will be treated as if it had never been registered
     * at all. If you have registered a shell, you should ensure that this
     * method is called when the shell is disposed. Otherwise, a potential
     * memory leak will exist.
     * </p>
     * <p>
     * If the shell was never registered, or if the shell is <code>null</code>,
     * then this method returns <code>false</code> and does nothing.
     * 
     * @param shell
     *            The shell to unregistered; does nothing if this value is
     *            <code>null</code>.
     * 
     * @return <code>true</code> if the shell had been registered;
     *         <code>false</code> otherwise.
     */
    public boolean unregisterShell(final Shell shell);
}