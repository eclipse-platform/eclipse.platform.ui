/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
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

import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.commands.contexts.IContextManagerListener;
import org.eclipse.core.expressions.Expression;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.services.IServiceWithSources;

/**
 * <p>
 * Provides services related to contexts in the Eclipse workbench. This provides
 * access to contexts.
 * </p>
 * <p>
 * This interface should not be implemented or extended by clients.
 * </p>
 * 
 * @since 3.1
 */
public interface IContextService extends IServiceWithSources {

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
	 * <p>
	 * Activates the given context within the context of this service. If this
	 * service was retrieved from the workbench, then this context will be
	 * active globally. If the service was retrieved from a nested component,
	 * then the context will only be active within that component.
	 * </p>
	 * <p>
	 * Also, it is guaranteed that the contexts submitted through a particular
	 * service will be cleaned up when that services is destroyed. So, for
	 * example, a service retrieved from a <code>IWorkbenchPartSite</code>
	 * would deactivate all of its contexts when the site is destroyed.
	 * </p>
	 * 
	 * @param contextId
	 *            The identifier for the context which should be activated; must
	 *            not be <code>null</code>.
	 * @return A token which can be used to later cancel the activation. Only
	 *         someone with access to this token can cancel the activation. The
	 *         activation will automatically be cancelled if the context from
	 *         which this service was retrieved is destroyed.
	 */
	public IContextActivation activateContext(String contextId);

	/**
	 * <p>
	 * Activates the given context within the context of this service. The
	 * context becomes active when <code>expression</code> evaluates to
	 * <code>true</code>. This is the same as calling
	 * {@link #activateContext(String, Expression, boolean)} with global==<code>false</code>.
	 * </p>
	 * <p>
	 * Also, it is guaranteed that the context submitted through a particular
	 * service will be cleaned up when that services is destroyed. So, for
	 * example, a service retrieved from a <code>IWorkbenchPartSite</code>
	 * would deactivate all of its handlers when the site is destroyed.
	 * </p>
	 * 
	 * @param contextId
	 *            The identifier for the context which should be activated; must
	 *            not be <code>null</code>.
	 * @param expression
	 *            This expression must evaluate to <code>true</code> before
	 *            this context will really become active. The expression may be
	 *            <code>null</code> if the context should always be active.
	 * @return A token which can be used to later cancel the activation. Only
	 *         someone with access to this token can cancel the activation. The
	 *         activation will automatically be cancelled if the context from
	 *         which this service was retrieved is destroyed.
	 * 
	 * @see org.eclipse.ui.ISources
	 * @since 3.2
	 */
	public IContextActivation activateContext(String contextId,
			Expression expression);

	/**
	 * <p>
	 * Activates the given context within the context of this service. The
	 * context becomes active when <code>expression</code> evaluates to
	 * <code>true</code>. If global==<code>false</code> then this service
	 * must also be the active service to activate the context.
	 * </p>
	 * <p>
	 * Also, it is guaranteed that the context submitted through a particular
	 * service will be cleaned up when that services is destroyed. So, for
	 * example, a service retrieved from a <code>IWorkbenchPartSite</code>
	 * would deactivate all of its handlers when the site is destroyed.
	 * </p>
	 * 
	 * @param contextId
	 *            The identifier for the context which should be activated; must
	 *            not be <code>null</code>.
	 * @param expression
	 *            This expression must evaluate to <code>true</code> before
	 *            this context will really become active. The expression may be
	 *            <code>null</code> if the context should always be active.
	 * @param global
	 *            Indicates that the handler should be activated irrespectively
	 *            of whether the corresponding workbench component (e.g.,
	 *            window, part, etc.) is active.
	 * @return A token which can be used to later cancel the activation. Only
	 *         someone with access to this token can cancel the activation. The
	 *         activation will automatically be cancelled if the context from
	 *         which this service was retrieved is destroyed.
	 * 
	 * @see org.eclipse.ui.ISources
	 * @since 3.2
	 */
	public IContextActivation activateContext(String contextId,
			Expression expression, boolean global);

	/**
	 * <p>
	 * Activates the given context within the context of this service. The
	 * context becomes active when <code>expression</code> evaluates to
	 * <code>true</code>.
	 * </p>
	 * <p>
	 * Also, it is guaranteed that the context submitted through a particular
	 * service will be cleaned up when that services is destroyed. So, for
	 * example, a service retrieved from a <code>IWorkbenchPartSite</code>
	 * would deactivate all of its handlers when the site is destroyed.
	 * </p>
	 * 
	 * @param contextId
	 *            The identifier for the context which should be activated; must
	 *            not be <code>null</code>.
	 * @param expression
	 *            This expression must evaluate to <code>true</code> before
	 *            this context will really become active. The expression may be
	 *            <code>null</code> if the context should always be active.
	 * @param sourcePriorities
	 *            The source priorities for the expression.
	 * @return A token which can be used to later cancel the activation. Only
	 *         someone with access to this token can cancel the activation. The
	 *         activation will automatically be cancelled if the context from
	 *         which this service was retrieved is destroyed.
	 * 
	 * @see org.eclipse.ui.ISources
	 * @deprecated Use
	 *             {@link IContextService#activateContext(String, Expression)}
	 *             instead.
	 */
	public IContextActivation activateContext(String contextId,
			Expression expression, int sourcePriorities);

	/**
	 * Adds a listener to this context service. The listener will be notified
	 * when the set of defined contexts changes. This can be used to track the
	 * global appearance and disappearance of contexts.
	 * <p>
	 * <b>Note:</b> listeners should be removed when no longer necessary. If
	 * not, they will be removed when the IServiceLocator used to acquire this
	 * service is disposed.
	 * </p>
	 * 
	 * @param listener
	 *            The listener to attach; must not be <code>null</code>.
	 * @since 3.2
	 * @see #removeContextManagerListener(IContextManagerListener)
	 */
	public void addContextManagerListener(IContextManagerListener listener);

	/**
	 * Deactivates the given context within the context of this service. If the
	 * handler was context with a different service, then it must be deactivated
	 * from that service instead. It is only possible to retract a context
	 * activation with this method. That is, you must have the same
	 * <code>IContextActivation</code> used to activate the context.
	 * 
	 * @param activation
	 *            The token that was returned from a call to
	 *            <code>activateContext</code>; must not be <code>null</code>.
	 */
	public void deactivateContext(IContextActivation activation);

	/**
	 * Deactivates the given contexts within the context of this service. If the
	 * contexts were activated with a different service, then they must be
	 * deactivated from that service instead. It is only possible to retract
	 * context activations with this method. That is, you must have the same
	 * <code>IContextActivation</code> instances used to activate the
	 * contexts.
	 * 
	 * @param activations
	 *            The tokens that were returned from a call to
	 *            <code>activateContext</code>. This collection must only
	 *            contain instances of <code>IContextActivation</code>. The
	 *            collection must not be <code>null</code>.
	 */
	public void deactivateContexts(Collection activations);

	/**
	 * Returns the set of active context identifiers.
	 * 
	 * @return The set of active context identifiers; this value may be
	 *         <code>null</code> if no active contexts have been set yet. If
	 *         the set is not <code>null</code>, then it contains only
	 *         instances of <code>String</code>.
	 * @since 3.2
	 */
	public Collection getActiveContextIds();

	/**
	 * Retrieves the context with the given identifier. If no such context
	 * exists, then an undefined context with the given id is created.
	 * 
	 * @param contextId
	 *            The identifier to find; must not be <code>null</code>.
	 * @return A context with the given identifier, either defined or undefined.
	 */
	public Context getContext(String contextId);

	/**
	 * Returns the collection of all of the defined contexts in the workbench.
	 * 
	 * @return The collection of contexts (<code>Context</code>) that are
	 *         defined; never <code>null</code>, but may be empty.
	 * @since 3.2
	 */
	public Context[] getDefinedContexts();

	/**
	 * Returns the collection of the identifiers for all of the defined contexts
	 * in the workbench.
	 * 
	 * @return The collection of context identifiers (<code>String</code>)
	 *         that are defined; never <code>null</code>, but may be empty.
	 */
	public Collection getDefinedContextIds();

	/**
	 * Returns the shell type for the given shell.
	 * 
	 * @param shell
	 *            The shell for which the type should be determined. If this
	 *            value is <code>null</code>, then
	 *            <code>IContextService.TYPE_NONE</code> is returned.
	 * @return <code>IContextService.TYPE_WINDOW</code>,
	 *         <code>IContextService.TYPE_DIALOG</code>, or
	 *         <code>IContextService.TYPE_NONE</code>.
	 */
	public int getShellType(Shell shell);

	/**
	 * <p>
	 * Reads the context information from the registry and the preferences. This
	 * will overwrite any of the existing information in the context service.
	 * This method is intended to be called during start-up. When this method
	 * completes, this context service will reflect the current state of the
	 * registry and preference store.
	 * </p>
	 */
	public void readRegistry();

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
	public boolean registerShell(Shell shell, int type);

	/**
	 * Removes a listener from this context service.
	 * 
	 * @param listener
	 *            The listener to be removed; must not be <code>null</code>.
	 * @since 3.2
	 */
	public void removeContextManagerListener(IContextManagerListener listener);

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
	public boolean unregisterShell(Shell shell);
}
