/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.diff.IThreeWayDiff;
import org.eclipse.team.core.mapping.IMergeContext;
import org.eclipse.team.core.mapping.ISynchronizationContext;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceMappingContext;
import org.eclipse.core.resources.mapping.ResourceTraversal;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;

import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIPlugin;
import org.eclipse.ltk.internal.ui.refactoring.model.ModelMessages;
import org.eclipse.ltk.internal.ui.refactoring.model.RefactoringDescriptorSynchronizationProxy;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.operation.IRunnableWithProgress;

import org.eclipse.ui.PlatformUI;

/**
 * Action to reject a pending refactoring and to just store it in the history.
 * 
 * @since 3.2
 */
public final class RejectRefactoringsAction extends Action {

	/**
	 * Returns the resource mapping for the element.
	 * 
	 * @param element
	 *            the element to get the resource mapping
	 * @return the resource mapping
	 */
	private static ResourceMapping getResourceMapping(final Object element) {
		if (element instanceof IAdaptable) {
			final IAdaptable adaptable= (IAdaptable) element;
			final Object adapted= adaptable.getAdapter(ResourceMapping.class);
			if (adapted instanceof ResourceMapping)
				return (ResourceMapping) adapted;
		}
		return null;
	}

	/**
	 * Returns the resource traversals for the element.
	 * 
	 * @param element
	 *            the element to get the resource traversals
	 * @return the resource traversals
	 */
	private static ResourceTraversal[] getResourceTraversals(final Object element) {
		final ResourceMapping mapping= getResourceMapping(element);
		if (mapping != null) {
			try {
				return mapping.getTraversals(ResourceMappingContext.LOCAL_CONTEXT, new NullProgressMonitor());
			} catch (CoreException exception) {
				RefactoringUIPlugin.log(exception);
			}
		}
		return new ResourceTraversal[0];
	}

	/** The synchronization context to use */
	private final ISynchronizationContext fContext;

	/** The refactoring descriptor proxies, or <code>null</code> */
	private RefactoringDescriptorProxy[] fProxies= null;

	/**
	 * Creates a new reject refactorings action.
	 * 
	 * @param context
	 *            the synchronization context
	 */
	public RejectRefactoringsAction(final ISynchronizationContext context) {
		Assert.isNotNull(context);
		fContext= context;
		setText(ModelMessages.RejectRefactoringsAction_title);
		setToolTipText(ModelMessages.RejectRefactoringsAction_tool_tip);
		setDescription(ModelMessages.RejectRefactoringsAction_description);
	}

	/**
	 * Returns the diffs associated with the model element.
	 * 
	 * @param element
	 *            the model element
	 * @return an array of diffs
	 */
	private IDiff[] getDiffs(final Object element) {
		return fContext.getDiffTree().getDiffs(getResourceTraversals(element));
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isEnabled() {
		if (fProxies != null && fProxies.length > 0 && fContext instanceof IMergeContext) {
			for (int index= 0; index < fProxies.length; index++) {
				if (fProxies[index] instanceof RefactoringDescriptorSynchronizationProxy) {
					final RefactoringDescriptorSynchronizationProxy proxy= (RefactoringDescriptorSynchronizationProxy) fProxies[index];
					if (proxy.getDirection() == IThreeWayDiff.INCOMING)
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public void run() {
		if (fProxies != null && fProxies.length > 0 && fContext instanceof IMergeContext) {
			final IMergeContext context= (IMergeContext) fContext;
			final Set set= new HashSet();
			for (int index= 0; index < fProxies.length; index++) {
				final IDiff[] diffs= getDiffs(fProxies[index]);
				if (diffs != null && diffs.length > 0)
					set.addAll(Arrays.asList(diffs));
			}
			try {
				PlatformUI.getWorkbench().getProgressService().run(true, true, new IRunnableWithProgress() {

					public final void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						try {
							context.merge((IDiff[]) set.toArray(new IDiff[set.size()]), false, monitor);
						} catch (CoreException exception) {
							throw new InvocationTargetException(exception);
						}
					}
				});
			} catch (InvocationTargetException exception) {
				RefactoringUIPlugin.log(exception);
			} catch (InterruptedException exception) {
				// Do nothing
			}
		}
	}

	/**
	 * Sets the refactoring descriptor proxies to accept.
	 * 
	 * @param proxies
	 *            the refactoring descriptor proxies
	 */
	public void setRefactoringDescriptors(final RefactoringDescriptorProxy[] proxies) {
		Assert.isNotNull(proxies);
		fProxies= proxies;
	}
}