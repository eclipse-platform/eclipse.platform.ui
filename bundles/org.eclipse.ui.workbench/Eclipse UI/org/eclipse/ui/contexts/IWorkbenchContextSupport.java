/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.contexts;

import java.util.Collection;

import org.eclipse.swt.widgets.Shell;

/**
 * <p>
 * An instance of this interface provides support for managing contexts at the
 * <code>IWorkbench</code> level. This provides the functionality necessary to
 * enabled contexts, disable or enabled the key binding service, as well as
 * register shells as particular types of windows.
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * 
 * @since 3.0
 * @deprecated Please use <code>IBindingService</code> and
 *             <code>IContextService</code> instead.
 * @see org.eclipse.ui.contexts.IContextService
 * @see org.eclipse.ui.keys.IBindingService
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IWorkbenchContextSupport {

	/**
	 * The identifier for the context that is active when a shell registered as
	 * a dialog.
	 */
	public static final String CONTEXT_ID_DIALOG = IContextService.CONTEXT_ID_DIALOG;

	/**
	 * The identifier for the context that is active when a shell is registered
	 * as either a window or a dialog.
	 */
	public static final String CONTEXT_ID_DIALOG_AND_WINDOW = IContextService.CONTEXT_ID_DIALOG_AND_WINDOW;

	/**
	 * The identifier for the context that is active when a shell is registered
	 * as a window.
	 */
	public static final String CONTEXT_ID_WINDOW = IContextService.CONTEXT_ID_WINDOW;

	/**
	 * The type used for registration indicating that the shell should be
	 * treated as a dialog. When the given shell is active, the "In Dialogs"
	 * context should also be active.
	 */
	public static final int TYPE_DIALOG = IContextService.TYPE_DIALOG;

	/**
	 * The type used for registration indicating that the shell should not
	 * receive any key bindings be default. When the given shell is active, we
	 * should not provide any <code>EnabledSubmission</code> instances for the
	 * "In Dialogs" or "In Windows" contexts.
	 * 
	 */
	public static final int TYPE_NONE = IContextService.TYPE_NONE;

	/**
	 * The type used for registration indicating that the shell should be
	 * treated as a window. When the given shell is active, the "In Windows"
	 * context should also be active.
	 */
	public static final int TYPE_WINDOW = IContextService.TYPE_WINDOW;

	/**
	 * <p>
	 * Add a single enabled submission for consideration. An enabled submission
	 * is a description of certain criteria under which a particular context
	 * should become active. All added submissions will be check when the
	 * conditions in the workbench change, and zero or more contexts will be
	 * selected as active.
	 * </p>
	 * <p>
	 * Just because an enabled submission is added, it does not mean that the
	 * corresponding context will become active. The workbench will consider the
	 * request, but other factors (such as conflicts) may prevent the context
	 * from becoming active.
	 * </p>
	 * 
	 * @param enabledSubmission
	 *            The enabled submission to be considered; must not be
	 *            <code>null</code>.
	 */
	void addEnabledSubmission(EnabledSubmission enabledSubmission);

	/**
	 * <p>
	 * Adds zero or more enabled submissions for consideration. An enabled
	 * submission is a description of certain criteria under which a particular
	 * context should become active. All added submissions will be check when
	 * the conditions in the workbench change, and zero or more contexts will be
	 * selected as active.
	 * </p>
	 * <p>
	 * Just because an enabled submission is added, it does not mean that the
	 * corresponding context will become active. The workbench will consider the
	 * request, but other factors (such as conflicts) may prevent the context
	 * from becoming active.
	 * </p>
	 * 
	 * @param enabledSubmissions
	 *            The enabled submissions to be considered; must not be
	 *            <code>null</code>, but may be empty. Every element in the
	 *            collection must be an instance of
	 *            <code>EnabledSubmission</code>.
	 */
	void addEnabledSubmissions(Collection enabledSubmissions);

	/**
	 * Returns the context manager for the workbench.
	 * 
	 * @return the context manager for the workbench. Guaranteed not to be
	 *         <code>null</code>.
	 */
	IContextManager getContextManager();

	/**
	 * Returns the shell type for the given shell.
	 * 
	 * @param shell
	 *            The shell for which the type should be determined. If this
	 *            value is <code>null</code>, then
	 *            <code>IWorkbenchContextSupport.TYPE_NONE</code> is returned.
	 * @return <code>IWorkbenchContextSupport.TYPE_WINDOW</code>,
	 *         <code>IWorkbenchContextSupport.TYPE_DIALOG</code>, or
	 *         <code>IWorkbenchContextSupport.TYPE_NONE</code>.
	 * @since 3.1
	 */
	public int getShellType(final Shell shell);

	/**
	 * Tests whether the global key binding architecture is currently active.
	 * 
	 * @return <code>true</code> if the key bindings are active;
	 *         <code>false</code> otherwise.
	 */
	public boolean isKeyFilterEnabled();

	/**
	 * Opens the key assistant dialog positioned near the key binding entry in
	 * the status bar.
	 * 
	 * @since 3.1
	 */
	public void openKeyAssistDialog();

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
	 * <p>
	 * Removes a single enabled submission from consideration. Only the same
	 * enabled submission will be removed; equivalent submissions will not be
	 * removed. Removing an enabled submission does not necessarily mean that
	 * the corresponding context will become inactive. It is possible that other
	 * parts of the application have requested that the context be enabled.
	 * </p>
	 * <p>
	 * There is no way to disable a context. It is only possible to not enable
	 * it.
	 * </p>
	 * 
	 * @param enabledSubmission
	 *            The enabled submission to be removed; must not be
	 *            <code>null</code>.
	 */
	void removeEnabledSubmission(EnabledSubmission enabledSubmission);

	/**
	 * <p>
	 * Removes a collection of enabled submissions from consideration. Only the
	 * same enabled submissions will be removed; equivalent submissions will not
	 * be removed. Removing an enabled submission does not necessarily mean that
	 * the corresponding context will become inactive. It is possible that other
	 * parts of the application have requested that the context be enabled.
	 * </p>
	 * <p>
	 * There is no way to disable a context. It is only possible to not enable
	 * it.
	 * </p>
	 * 
	 * @param enabledSubmissions
	 *            The enabled submissions to be removed; must not be
	 *            <code>null</code>, but may be empty. The collection must
	 *            only contain instances of <code>EnabledSubmission</code>.
	 */
	void removeEnabledSubmissions(Collection enabledSubmissions);

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
	 *            The shell to be unregistered; does nothing if this value is
	 *            <code>null</code>.
	 * 
	 * @return <code>true</code> if the shell had been registered;
	 *         <code>false</code> otherwise.
	 */
	public boolean unregisterShell(final Shell shell);
}
