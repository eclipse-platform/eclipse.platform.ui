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

package org.eclipse.ui.internal.contexts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.core.commands.util.Tracing;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ActiveShellExpression;
import org.eclipse.ui.ISources;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.internal.misc.Policy;
import org.eclipse.ui.internal.services.ExpressionAuthority;

/**
 * <p>
 * A central authority for deciding activation of contexts. This authority
 * listens to a variety of incoming sources, and updates the underlying context
 * manager if changes occur.
 * </p>
 * 
 * @since 3.1
 */
public final class ContextAuthority extends ExpressionAuthority {
	public static final String DEFER_EVENTS = "org.eclipse.ui.internal.contexts.deferEvents"; //$NON-NLS-1$
	public static final String SEND_EVENTS = "org.eclipse.ui.internal.contexts.sendEvents"; //$NON-NLS-1$


	/**
	 * The default size of the set containing the activations to recompute. This
	 * is more than enough to cover the average case.
	 */
	private static final int ACTIVATIONS_TO_RECOMPUTE_SIZE = 4;

	/**
	 * Whether the context authority should kick into debugging mode. This
	 * causes the unresolvable handler conflicts to be printed to the console.
	 */
	private static final boolean DEBUG = Policy.DEBUG_CONTEXTS;

	/**
	 * Whether the performance information should be printed about the
	 * performance of the context authority.
	 */
	private static final boolean DEBUG_PERFORMANCE = Policy.DEBUG_CONTEXTS_PERFORMANCE;

	/**
	 * The name of the data tag containing the dispose listener information.
	 */
	private static final String DISPOSE_LISTENER = "org.eclipse.ui.internal.contexts.ContextAuthority"; //$NON-NLS-1$

	/**
	 * The component name to print when displaying tracing information.
	 */
	private static final String TRACING_COMPONENT = "CONTEXTS"; //$NON-NLS-1$

	/**
	 * A bucket sort of the context activations based on source priority. Each
	 * activation will appear only once per set, but may appear in multiple
	 * sets. If no activations are defined for a particular priority level, then
	 * the array at that index will only contain <code>null</code>.
	 */
	private final Set[] activationsBySourcePriority = new Set[33];

	/**
	 * This is a map of context activations (<code>Collection</code> of
	 * <code>IContextActivation</code>) sorted by context identifier (<code>String</code>).
	 * If there is only one context activation for a context, then the
	 * <code>Collection</code> is replaced by a
	 * <code>IContextActivation</code>. If there is no activation, the entry
	 * should be removed entirely.
	 */
	private final Map contextActivationsByContextId = new HashMap();

	/**
	 * The context manager that should be updated when the contexts are
	 * changing.
	 */
	private final ContextManager contextManager;

	/**
	 * The context service that should be used for authority-managed
	 * shell-related contexts. This value is never <code>null</code>.
	 */
	private final IContextService contextService;

	/**
	 * This is a map of shell to a list of activations. When a shell is
	 * registered, it is added to this map with the list of activation that
	 * should be submitted when the shell is active. When the shell is
	 * deactivated, this same list should be withdrawn. A shell is removed from
	 * this map using the {@link #unregisterShell(Shell)}method. This value may
	 * be empty, but is never <code>null</code>. The <code>null</code> key
	 * is reserved for active shells that have not been registered but have a
	 * parent (i.e., default dialog service).
	 */
	private final Map registeredWindows = new WeakHashMap();

	/**
	 * Constructs a new instance of <code>ContextAuthority</code>.
	 * 
	 * @param contextManager
	 *            The context manager from which contexts can be retrieved (to
	 *            update their active state); must not be <code>null</code>.
	 * @param contextService
	 *            The workbench context service for which this authority is
	 *            acting. This allows the authority to manage shell-specific
	 *            contexts. This value must not be <code>null</code>.
	 */
	ContextAuthority(final ContextManager contextManager,
			final IContextService contextService) {
		if (contextManager == null) {
			throw new NullPointerException(
					"The context authority needs a context manager"); //$NON-NLS-1$
		}
		if (contextService == null) {
			throw new NullPointerException(
					"The context authority needs an evaluation context"); //$NON-NLS-1$
		}

		this.contextManager = contextManager;
		this.contextService = contextService;
	}

	/**
	 * Activates a context on the workbench. This will add it to a master list.
	 * 
	 * @param activation
	 *            The activation; must not be <code>null</code>.
	 */
	final void activateContext(final IContextActivation activation) {
		// First we update the contextActivationsByContextId map.
		final String contextId = activation.getContextId();
		if (DEFER_EVENTS.equals(contextId) || SEND_EVENTS.equals(contextId)) {
			contextManager.addActiveContext(contextId);
			return;
		}
		final Object value = contextActivationsByContextId.get(contextId);
		if (value instanceof Collection) {
			final Collection contextActivations = (Collection) value;
			if (!contextActivations.contains(activation)) {
				contextActivations.add(activation);
				updateContext(contextId, containsActive(contextActivations));
			}
		} else if (value instanceof IContextActivation) {
			if (value != activation) {
				final Collection contextActivations = new ArrayList(2);
				contextActivations.add(value);
				contextActivations.add(activation);
				contextActivationsByContextId
						.put(contextId, contextActivations);
				updateContext(contextId, containsActive(contextActivations));
			}
		} else {
			contextActivationsByContextId.put(contextId, activation);
			updateContext(contextId, evaluate(activation));
		}

		// Next we update the source priority bucket sort of activations.
		final int sourcePriority = activation.getSourcePriority();
		for (int i = 1; i <= 32; i++) {
			if ((sourcePriority & (1 << i)) != 0) {
				Set activations = activationsBySourcePriority[i];
				if (activations == null) {
					activations = new HashSet(1);
					activationsBySourcePriority[i] = activations;
				}
				activations.add(activation);
			}
		}
	}

	/**
	 * Checks whether the new active shell is registered. If it is already
	 * registered, then it does no work. If it is not registered, then it checks
	 * what type of contexts the shell should have by default. This is
	 * determined by parenting. A shell with no parent receives no contexts. A
	 * shell with a parent, receives the dialog contexts.
	 * 
	 * @param newShell
	 *            The newly active shell; may be <code>null</code> or
	 *            disposed.
	 * @param oldShell
	 *            The previously active shell; may be <code>null</code> or
	 *            disposed.
	 */
	private final void checkWindowType(final Shell newShell,
			final Shell oldShell) {
		/*
		 * If the previous active shell was recognized as a dialog by default,
		 * then remove its submissions.
		 */
		Collection oldActivations = (Collection) registeredWindows
				.get(oldShell);
		if (oldActivations == null) {
			/*
			 * The old shell wasn't registered. So, we need to check if it was
			 * considered a dialog by default.
			 */
			oldActivations = (Collection) registeredWindows.get(null);
			if (oldActivations != null) {
				final Iterator oldActivationItr = oldActivations.iterator();
				while (oldActivationItr.hasNext()) {
					final IContextActivation activation = (IContextActivation) oldActivationItr
							.next();
					deactivateContext(activation);
				}
			}
		}

		/*
		 * If the new active shell is recognized as a dialog by default, then
		 * create some submissions, remember them, and submit them for
		 * processing.
		 */
		if ((newShell != null) && (!newShell.isDisposed())) {
			final Collection newActivations;

			if ((newShell.getParent() != null)
					&& (registeredWindows.get(newShell) == null)) {
				// This is a dialog by default.
				newActivations = new ArrayList();
				final Expression expression = new ActiveShellExpression(
						newShell);
				final IContextActivation dialogWindowActivation = new ContextActivation(
						IContextService.CONTEXT_ID_DIALOG_AND_WINDOW,
						expression, contextService);
				activateContext(dialogWindowActivation);
				newActivations.add(dialogWindowActivation);
				final IContextActivation dialogActivation = new ContextActivation(
						IContextService.CONTEXT_ID_DIALOG, expression,
						contextService);
				activateContext(dialogActivation);
				newActivations.add(dialogActivation);
				registeredWindows.put(null, newActivations);

				/*
				 * Make sure the submissions will be removed in event of
				 * disposal. This is really just a paranoid check. The
				 * "oldSubmissions" code above should take care of this.
				 */
				newShell.addDisposeListener(new DisposeListener() {

					/*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
					 */
					public void widgetDisposed(DisposeEvent e) {
						registeredWindows.remove(null);
						if (!newShell.isDisposed()) {
							newShell.removeDisposeListener(this);
						}

						/*
						 * In the case where a dispose has happened, we are
						 * expecting an activation event to arrive at some point
						 * in the future. If we process the submissions now,
						 * then we will update the activeShell before
						 * checkWindowType is called. This means that dialogs
						 * won't be recognized as dialogs.
						 */
						final Iterator newActivationItr = newActivations
								.iterator();
						while (newActivationItr.hasNext()) {
							deactivateContext((IContextActivation) newActivationItr
									.next());
						}
					}
				});

			} else {
				// Shells that are not dialogs by default must register.
				newActivations = null;

			}
		}
	}

	/**
	 * Returns a subset of the given <code>activations</code> containing only
	 * those that are active
	 * 
	 * @param activations
	 *            The activations to trim; must not be <code>null</code>, but
	 *            may be empty.
	 * @return <code>true</code> if there is at least one active context;
	 *         <code>false</code> otherwise.
	 */
	private final boolean containsActive(final Collection activations) {
		final Iterator activationItr = activations.iterator();
		while (activationItr.hasNext()) {
			final IContextActivation activation = (IContextActivation) activationItr
					.next();
			if (evaluate(activation)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Removes an activation for a context on the workbench. This will remove it
	 * from the master list, and update the appropriate context, if necessary.
	 * 
	 * @param activation
	 *            The activation; must not be <code>null</code>.
	 */
	final void deactivateContext(final IContextActivation activation) {
		// First we update the handlerActivationsByCommandId map.
		final String contextId = activation.getContextId();
		if (DEFER_EVENTS.equals(contextId) || SEND_EVENTS.equals(contextId)) {
			return;
		}
		final Object value = contextActivationsByContextId.get(contextId);
		if (value instanceof Collection) {
			final Collection contextActivations = (Collection) value;
			if (contextActivations.contains(activation)) {
				contextActivations.remove(activation);
				if (contextActivations.isEmpty()) {
					contextActivationsByContextId.remove(contextId);
					updateContext(contextId, false);

				} else if (contextActivations.size() == 1) {
					final IContextActivation remainingActivation = (IContextActivation) contextActivations
							.iterator().next();
					contextActivationsByContextId.put(contextId,
							remainingActivation);
					updateContext(contextId, evaluate(remainingActivation));

				} else {
					updateContext(contextId, containsActive(contextActivations));
				}
			}
		} else if (value instanceof IContextActivation) {
			if (value == activation) {
				contextActivationsByContextId.remove(contextId);
				updateContext(contextId, false);
			}
		}

		// Next we update the source priority bucket sort of activations.
		final int sourcePriority = activation.getSourcePriority();
		for (int i = 1; i <= 32; i++) {
			if ((sourcePriority & (1 << i)) != 0) {
				final Set activations = activationsBySourcePriority[i];
				if (activations == null) {
					continue;
				}
				activations.remove(activation);
				if (activations.isEmpty()) {
					activationsBySourcePriority[i] = null;
				}
			}
		}
	}

	/**
	 * Returns the currently active shell.
	 * 
	 * @return The currently active shell; may be <code>null</code>.
	 */
	final Shell getActiveShell() {
		return (Shell) getVariable(ISources.ACTIVE_SHELL_NAME);
	}

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
	 */
	public final int getShellType(final Shell shell) {
		// If the shell is null, then return none.
		if (shell == null) {
			return IContextService.TYPE_NONE;
		}

		final Collection activations = (Collection) registeredWindows
				.get(shell);
		if (activations != null) {
			// The shell is registered, so check what type it was registered as.
			if (activations.isEmpty()) {
				// It was registered as none.
				return IContextService.TYPE_NONE;
			}

			// Look for the right type of context id.
			final Iterator activationItr = activations.iterator();
			while (activationItr.hasNext()) {
				final IContextActivation activation = (IContextActivation) activationItr
						.next();
				final String contextId = activation.getContextId();
				if (contextId == IContextService.CONTEXT_ID_DIALOG) {
					return IContextService.TYPE_DIALOG;
				} else if (contextId == IContextService.CONTEXT_ID_WINDOW) {
					return IContextService.TYPE_WINDOW;
				}
			}

			// This shouldn't be possible.
			Assert
					.isTrue(
							false,
							"A registered shell should have at least one submission matching TYPE_WINDOW or TYPE_DIALOG"); //$NON-NLS-1$
			return IContextService.TYPE_NONE; // not reachable

		} else if (shell.getParent() != null) {
			/*
			 * The shell is not registered, but it has a parent. It is therefore
			 * considered a dialog by default.
			 */
			return IContextService.TYPE_DIALOG;

		} else {
			/*
			 * The shell is not registered, but has no parent. It gets no key
			 * bindings.
			 */
			return IContextService.TYPE_NONE;
		}
	}

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
	public final boolean registerShell(final Shell shell, final int type) {
		// We do not allow null shell registration. It is reserved.
		if (shell == null) {
			throw new NullPointerException("The shell was null"); //$NON-NLS-1$
		}

		// Debugging output
		if (DEBUG) {
			final StringBuffer buffer = new StringBuffer("register shell '"); //$NON-NLS-1$
			buffer.append(shell);
			buffer.append("' as "); //$NON-NLS-1$
			switch (type) {
			case IContextService.TYPE_DIALOG:
				buffer.append("dialog"); //$NON-NLS-1$
				break;
			case IContextService.TYPE_WINDOW:
				buffer.append("window"); //$NON-NLS-1$
				break;
			case IContextService.TYPE_NONE:
				buffer.append("none"); //$NON-NLS-1$
				break;
			default:
				buffer.append("unknown"); //$NON-NLS-1$
				break;
			}
			Tracing.printTrace(TRACING_COMPONENT, buffer.toString());
		}

		// Build the list of submissions.
		final List activations = new ArrayList();
		Expression expression;
		IContextActivation dialogWindowActivation;
		switch (type) {
		case IContextService.TYPE_DIALOG:
			expression = new ActiveShellExpression(shell);
			dialogWindowActivation = new ContextActivation(
					IContextService.CONTEXT_ID_DIALOG_AND_WINDOW, expression,
					contextService);
			activateContext(dialogWindowActivation);
			activations.add(dialogWindowActivation);
			final IContextActivation dialogActivation = new ContextActivation(
					IContextService.CONTEXT_ID_DIALOG, expression,
					contextService);
			activateContext(dialogActivation);
			activations.add(dialogActivation);
			break;
		case IContextService.TYPE_NONE:
			break;
		case IContextService.TYPE_WINDOW:
			expression = new ActiveShellExpression(shell);
			dialogWindowActivation = new ContextActivation(
					IContextService.CONTEXT_ID_DIALOG_AND_WINDOW, expression,
					contextService);
			activateContext(dialogWindowActivation);
			activations.add(dialogWindowActivation);
			final IContextActivation windowActivation = new ContextActivation(
					IContextService.CONTEXT_ID_WINDOW, expression,
					contextService);
			activateContext(windowActivation);
			activations.add(windowActivation);
			break;
		default:
			throw new IllegalArgumentException("The type is not recognized: " //$NON-NLS-1$
					+ type);
		}

		// Check to see if the activations are already present.
		boolean returnValue = false;
		final Collection previousActivations = (Collection) registeredWindows
				.get(shell);
		if (previousActivations != null) {
			returnValue = true;
			final Iterator previousActivationItr = previousActivations
					.iterator();
			while (previousActivationItr.hasNext()) {
				final IContextActivation activation = (IContextActivation) previousActivationItr
						.next();
				deactivateContext(activation);
			}
		}

		// Add the new submissions, and force some reprocessing to occur.
		registeredWindows.put(shell, activations);

		/*
		 * Remember the dispose listener so that we can remove it later if we
		 * unregister the shell.
		 */
		final DisposeListener shellDisposeListener = new DisposeListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
			 */
			public void widgetDisposed(DisposeEvent e) {
				registeredWindows.remove(shell);
				if (!shell.isDisposed()) {
					shell.removeDisposeListener(this);
				}

				/*
				 * In the case where a dispose has happened, we are expecting an
				 * activation event to arrive at some point in the future. If we
				 * process the submissions now, then we will update the
				 * activeShell before checkWindowType is called. This means that
				 * dialogs won't be recognized as dialogs.
				 */
				final Iterator activationItr = activations.iterator();
				while (activationItr.hasNext()) {
					deactivateContext((IContextActivation) activationItr.next());
				}
			}
		};

		// Make sure the submissions will be removed in event of disposal.
		shell.addDisposeListener(shellDisposeListener);
		shell.setData(DISPOSE_LISTENER, shellDisposeListener);

		return returnValue;
	}

	/**
	 * Carries out the actual source change notification. It assumed that by the
	 * time this method is called, <code>context</code> is up-to-date with the
	 * current state of the application.
	 * 
	 * @param sourcePriority
	 *            A bit mask of all the source priorities that have changed.
	 */
	protected final void sourceChanged(final int sourcePriority) {
		// If tracing, then track how long it takes to process the activations.
		long startTime = 0L;
		if (DEBUG_PERFORMANCE) {
			startTime = System.currentTimeMillis();
		}

		/*
		 * In this first phase, we cycle through all of the activations that
		 * could have potentially changed. Each such activation is added to a
		 * set for future processing. We add it to a set so that we avoid
		 * handling any individual activation more than once.
		 */
		final Set activationsToRecompute = new HashSet(
				ACTIVATIONS_TO_RECOMPUTE_SIZE);
		for (int i = 1; i <= 32; i++) {
			if ((sourcePriority & (1 << i)) != 0) {
				final Collection activations = activationsBySourcePriority[i];
				if (activations != null) {
					final Iterator activationItr = activations.iterator();
					while (activationItr.hasNext()) {
						activationsToRecompute.add(activationItr.next());
					}
				}
			}
		}

		/*
		 * For every activation, we recompute its active state, and check
		 * whether it has changed. If it has changed, then we take note of the
		 * context identifier so we can update the context later.
		 */
		final Collection changedContextIds = new ArrayList(
				activationsToRecompute.size());
		final Iterator activationItr = activationsToRecompute.iterator();
		while (activationItr.hasNext()) {
			final IContextActivation activation = (IContextActivation) activationItr
					.next();
			final boolean currentActive = evaluate(activation);
			activation.clearResult();
			final boolean newActive = evaluate(activation);
			if (newActive != currentActive) {
				changedContextIds.add(activation.getContextId());
			}
		}

		try {
			contextManager.addActiveContext(DEFER_EVENTS);
			/*
			 * For every context identifier with a changed activation, we
			 * resolve conflicts and trigger an update.
			 */
			final Iterator changedContextIdItr = changedContextIds.iterator();
			while (changedContextIdItr.hasNext()) {
				final String contextId = (String) changedContextIdItr.next();
				final Object value = contextActivationsByContextId
						.get(contextId);
				if (value instanceof IContextActivation) {
					final IContextActivation activation = (IContextActivation) value;
					updateContext(contextId, evaluate(activation));
				} else if (value instanceof Collection) {
					updateContext(contextId, containsActive((Collection) value));
				} else {
					updateContext(contextId, false);
				}
			}
		} finally {
			contextManager.addActiveContext(SEND_EVENTS);
		}

		// If tracing performance, then print the results.
		if (DEBUG_PERFORMANCE) {
			final long elapsedTime = System.currentTimeMillis() - startTime;
			final int size = activationsToRecompute.size();
			if (size > 0) {
				Tracing.printTrace(TRACING_COMPONENT, size
						+ " activations recomputed in " + elapsedTime + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

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
	public final boolean unregisterShell(final Shell shell) {
		// Don't allow this method to play with the special null slot.
		if (shell == null) {
			return false;
		}

		/*
		 * If we're unregistering the shell but we're not about to dispose it,
		 * then we'll end up leaking the DisposeListener unless we remove it
		 * here.
		 */
		if (!shell.isDisposed()) {
			final DisposeListener oldListener = (DisposeListener) shell
					.getData(DISPOSE_LISTENER);
			if (oldListener != null) {
				shell.removeDisposeListener(oldListener);
			}
		}

		Collection previousActivations = (Collection) registeredWindows
				.get(shell);
		if (previousActivations != null) {
			registeredWindows.remove(shell);

			final Iterator previousActivationItr = previousActivations
					.iterator();
			while (previousActivationItr.hasNext()) {
				final IContextActivation activation = (IContextActivation) previousActivationItr
						.next();
				deactivateContext(activation);
			}
			return true;
		}

		return false;
	}

	/**
	 * Updates the context with the given context activation.
	 * 
	 * @param contextId
	 *            The identifier of the context which should be updated; must
	 *            not be <code>null</code>.
	 * @param active
	 *            Whether the context should be active; <code>false</code>
	 *            otherwise.
	 */
	private final void updateContext(final String contextId,
			final boolean active) {
		if (active) {
			contextManager.addActiveContext(contextId);
		} else {
			contextManager.removeActiveContext(contextId);
		}
	}

	/**
	 * Updates this authority's evaluation context. If the changed variable is
	 * the <code>ISources.ACTIVE_SHELL_NAME</code> variable, then this also
	 * triggers an update of the shell-specific contexts. For example, if a
	 * dialog becomes active, then the dialog context will be activated by this
	 * method.
	 * 
	 * @param name
	 *            The name of the variable to update; must not be
	 *            <code>null</code>.
	 * @param value
	 *            The new value of the variable. If this value is
	 *            <code>null</code>, then the variable is removed.
	 */
	protected final void updateEvaluationContext(final String name,
			final Object value) {
		/*
		 * Bug 84056. If we update the active workbench window, then we risk
		 * falling back to that shell when the active shell has registered as
		 * "none".
		 */
		if ((name != null)
				&& (!ISources.ACTIVE_WORKBENCH_WINDOW_SHELL_NAME.equals(name))) {
			/*
			 * We need to track shell activation ourselves, as some special
			 * contexts are automatically activated in response to different
			 * types of shells becoming active.
			 */
			if (ISources.ACTIVE_SHELL_NAME.equals(name)) {
				checkWindowType((Shell) value,
						(Shell) getVariable(ISources.ACTIVE_SHELL_NAME));
			}

			// Update the evaluation context itself.
			changeVariable(name, value);
		}
	}

	/**
	 * <p>
	 * Bug 95792. A mechanism by which the key binding architecture can force an
	 * update of the contexts (based on the active shell) before trying to
	 * execute a command. This mechanism is required for GTK+ only.
	 * </p>
	 * <p>
	 * DO NOT CALL THIS METHOD.
	 * </p>
	 */
	final void updateShellKludge() {
		updateCurrentState();
		sourceChanged(ISources.ACTIVE_SHELL);
	}
}
