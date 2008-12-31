/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.ui.refactoring.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.core.mapping.ISynchronizationScope;
import org.eclipse.team.ui.mapping.MergeActionHandler;
import org.eclipse.team.ui.mapping.SynchronizationActionProvider;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;

import org.eclipse.core.runtime.Assert;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;
import org.eclipse.ltk.core.refactoring.model.AbstractRefactoringDescriptorResourceMapping;
import org.eclipse.ltk.core.refactoring.model.AbstractRefactoringHistoryResourceMapping;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringDescriptorProxyAdapter;
import org.eclipse.ltk.internal.ui.refactoring.actions.AcceptRefactoringsAction;
import org.eclipse.ltk.internal.ui.refactoring.actions.RejectRefactoringsAction;

/**
 * Refactoring-aware synchronization action provider which contributes an action
 * to accept pending refactorings during team synchronization.
 * <p>
 * This action provider contributes an action for refactoring history objects.
 * Additionally, existing command handlers for the <code>Merge</code>,
 * <code>Mark As Merged</code> and <code>Overwrite</code> actions are
 * wrapped and automatically disabled for refactoring history objects.
 * </p>
 * <p>
 * Note: this class is intended to be extended by clients who need refactoring
 * support in a team synchronization viewer. It needs to be be registered with
 * the <code>org.eclipse.ui.navigator.navigatorContent</code> or
 * <code>org.eclipse.ui.navigator.viewer</code> extension points in order to
 * participate in the team synchronization viewers.
 * </p>
 *
 * @see org.eclipse.team.ui.mapping.SynchronizationActionProvider
 *
 * @since 3.2
 */
public class RefactoringSynchronizationActionProvider extends SynchronizationActionProvider {

	/** Delegate for refactoring action handlers */
	private final class RefactoringHandlerDelegate extends AbstractHandler {

		/** The delegate handler */
		private final IHandler fDelegateHandler;

		/**
		 * Creates a new synchronization handler delegate.
		 *
		 * @param handler
		 *            the delegate handler
		 */
		public RefactoringHandlerDelegate(final IHandler handler) {
			Assert.isNotNull(handler);
			fDelegateHandler= handler;
		}

		/**
		 * {@inheritDoc}
		 */
		public void dispose() {
			fDelegateHandler.dispose();
			super.dispose();
		}

		/**
		 * {@inheritDoc}
		 */
		public Object execute(final ExecutionEvent event) throws ExecutionException {
			return fDelegateHandler.execute(event);
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean isEnabled() {
			return !hasRefactorings(getSynchronizationContext(), getSynchronizePageConfiguration()) && fDelegateHandler.isEnabled();
		}
	}

	/**
	 * Gets the refactoring represented by the specified proxy.
	 *
	 * @param scope
	 *            the synchronization scope
	 * @param proxy
	 *            the refactoring descriptor proxy
	 * @param set
	 *            the set of refactoring descriptor proxies
	 */
	private static void getRefactoring(final ISynchronizationScope scope, final RefactoringDescriptorProxy proxy, final Set set) {
		final ResourceMapping mapping= (ResourceMapping) proxy.getAdapter(ResourceMapping.class);
		if (mapping instanceof AbstractRefactoringDescriptorResourceMapping) {
			final AbstractRefactoringDescriptorResourceMapping extended= (AbstractRefactoringDescriptorResourceMapping) mapping;
			final IResource resource= extended.getResource();
			if (resource != null && scope.contains(resource))
				set.add(proxy);
		}
	}

	/**
	 * Returns the currently selected refactorings.
	 *
	 * @param context
	 *            the synchronization context
	 * @param configuration
	 *            the synchronize page configuration
	 * @return the selected refactorings, or the empty array
	 */
	private static RefactoringDescriptorProxy[] getRefactorings(final ISynchronizationContext context, final ISynchronizePageConfiguration configuration) {
		Assert.isNotNull(context);
		Assert.isNotNull(configuration);
		final Set set= new HashSet();
		final ISelection selection= configuration.getSite().getSelectionProvider().getSelection();
		if (selection instanceof IStructuredSelection) {
			final IStructuredSelection structured= (IStructuredSelection) selection;
			if (!structured.isEmpty()) {
				final Object[] elements= structured.toArray();
				final ISynchronizationScope scope= context.getScope();
				for (int index= 0; index < elements.length; index++) {
					if (elements[index] instanceof RefactoringHistory) {
						getRefactorings(scope, (RefactoringHistory) elements[index], set);
					} else if (elements[index] instanceof RefactoringDescriptorProxy) {
						getRefactoring(scope, (RefactoringDescriptorProxy) elements[index], set);
					} else if (elements[index] instanceof RefactoringDescriptor) {
						getRefactoring(scope, new RefactoringDescriptorProxyAdapter(((RefactoringDescriptor) elements[index])), set);
					}
				}
			}
		}
		return (RefactoringDescriptorProxy[]) set.toArray(new RefactoringDescriptorProxy[set.size()]);
	}

	/**
	 * Gets the refactorings represented by the specified history.
	 *
	 * @param scope
	 *            the synchronization scope
	 * @param history
	 *            the refactoring history
	 * @param set
	 *            the set of refactoring descriptor proxies
	 */
	private static void getRefactorings(final ISynchronizationScope scope, final RefactoringHistory history, final Set set) {
		final ResourceMapping mapping= (ResourceMapping) history.getAdapter(ResourceMapping.class);
		if (mapping instanceof AbstractRefactoringHistoryResourceMapping) {
			final AbstractRefactoringHistoryResourceMapping extended= (AbstractRefactoringHistoryResourceMapping) mapping;
			final IResource resource= extended.getResource();
			if (resource != null && scope.contains(resource))
				set.addAll(Arrays.asList(history.getDescriptors()));
		}
	}

	/**
	 * Is the specified refactoring in the scope?
	 *
	 * @param scope
	 *            the synchronization scope
	 * @param proxy
	 *            the refactoring descriptor proxy
	 * @return <code>true</code> if the refactoring is in the scope,
	 *         <code>false</code> otherwise
	 */
	private static boolean hasRefactoring(final ISynchronizationScope scope, final RefactoringDescriptorProxy proxy) {
		final ResourceMapping mapping= (ResourceMapping) proxy.getAdapter(ResourceMapping.class);
		if (mapping instanceof AbstractRefactoringDescriptorResourceMapping) {
			final AbstractRefactoringDescriptorResourceMapping extended= (AbstractRefactoringDescriptorResourceMapping) mapping;
			final IResource resource= extended.getResource();
			if (resource != null)
				return scope.contains(resource);
		}
		return false;
	}

	/**
	 * Returns whether any refactorings from the given synchronization context
	 * are selected.
	 *
	 * @param context
	 *            the synchronization context
	 * @param configuration
	 *            the synchronize page configuration
	 * @return <code>true</code> if any refactorings are selected,
	 *         <code>false</code> otherwise
	 */
	private static boolean hasRefactorings(final ISynchronizationContext context, final ISynchronizePageConfiguration configuration) {
		Assert.isNotNull(context);
		Assert.isNotNull(configuration);
		final ISelection selection= configuration.getSite().getSelectionProvider().getSelection();
		if (selection instanceof IStructuredSelection) {
			final IStructuredSelection structured= (IStructuredSelection) selection;
			if (!structured.isEmpty()) {
				final Object[] elements= structured.toArray();
				final ISynchronizationScope scope= context.getScope();
				for (int index= 0; index < elements.length; index++) {
					if (elements[index] instanceof RefactoringHistory) {
						return hasRefactorings(scope, (RefactoringHistory) elements[index]);
					} else if (elements[index] instanceof RefactoringDescriptorProxy) {
						return hasRefactoring(scope, (RefactoringDescriptorProxy) elements[index]);
					} else if (elements[index] instanceof RefactoringDescriptor) {
						return hasRefactoring(scope, new RefactoringDescriptorProxyAdapter((RefactoringDescriptor) elements[index]));
					}
				}
			}
		}
		return false;
	}

	/**
	 * Does the specified refactoring history contain any refactorings in the
	 * scope?
	 *
	 * @param scope
	 *            the synchronization scope
	 * @param history
	 *            the refactoring history
	 * @return <code>true</code> if any refactorings are in the scope,
	 *         <code>false</code> otherwise
	 */
	private static boolean hasRefactorings(final ISynchronizationScope scope, final RefactoringHistory history) {
		final ResourceMapping mapping= (ResourceMapping) history.getAdapter(ResourceMapping.class);
		if (mapping instanceof AbstractRefactoringHistoryResourceMapping) {
			final AbstractRefactoringHistoryResourceMapping extended= (AbstractRefactoringHistoryResourceMapping) mapping;
			final IResource resource= extended.getResource();
			if (resource != null)
				return scope.contains(resource);
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public void fillContextMenu(final IMenuManager menu) {
		super.fillContextMenu(menu);
		if (isRefactoringElementSelected()) {
			final ISynchronizationContext context= getSynchronizationContext();
			final RefactoringDescriptorProxy[] proxies= getRefactorings(context, getSynchronizePageConfiguration());
			final AcceptRefactoringsAction accept= new AcceptRefactoringsAction(context, getExtensionSite().getViewSite().getShell());
			accept.setRefactoringDescriptors(proxies);
			menu.add(accept);
			final RejectRefactoringsAction reject= new RejectRefactoringsAction(context);
			reject.setRefactoringDescriptors(proxies);
			menu.add(reject);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected void initialize() {
		super.initialize();
		final ISynchronizePageConfiguration configuration= getSynchronizePageConfiguration();
		registerHandler(MERGE_ACTION_ID, new RefactoringHandlerDelegate(MergeActionHandler.getDefaultHandler(MERGE_ACTION_ID, configuration)));
		registerHandler(OVERWRITE_ACTION_ID, new RefactoringHandlerDelegate(MergeActionHandler.getDefaultHandler(OVERWRITE_ACTION_ID, configuration)));
		registerHandler(MARK_AS_MERGE_ACTION_ID, new RefactoringHandlerDelegate(MergeActionHandler.getDefaultHandler(MARK_AS_MERGE_ACTION_ID, configuration)));
	}

	/**
	 * {@inheritDoc}
	 */
	protected void initializeOpenActions() {
		if (!hasRefactorings(getSynchronizationContext(), getSynchronizePageConfiguration()))
			super.initializeOpenActions();
	}

	private boolean isRefactoringElementSelected() {
		final ISelection selection= getContext().getSelection();
		if (selection instanceof IStructuredSelection) {
			final IStructuredSelection extended= (IStructuredSelection) selection;
			for (final Iterator iterator= extended.iterator(); iterator.hasNext();) {
				final Object element= iterator.next();
				if (element instanceof RefactoringDescriptorProxy || element instanceof RefactoringDescriptor || element instanceof RefactoringHistory) {
					return true;
				}
			}
		}
		return false;
	}
}