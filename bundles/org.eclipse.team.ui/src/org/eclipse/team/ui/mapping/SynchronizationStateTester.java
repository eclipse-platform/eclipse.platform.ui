/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui.mapping;

import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.mapping.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.viewers.IDecorationContext;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.Team;
import org.eclipse.team.core.diff.IDiffNode;
import org.eclipse.team.core.diff.IThreeWayDiff;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.registry.TeamDecoratorManager;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.PlatformUI;

/**
 * A state change tester is used by logical models to communicate the
 * synchronization state of their logical model elements to 
 * the lightweight label decorators of team providers. 
 * <p>
 * There are two different types of elements being decorated: those
 * that have a one-to-one mapping to a resource and those that do not.
 * Those that do should adapt to their corresponding resource. Doing
 * so will ensure that label updates occur when the state of that
 * resource changes (i.e. the team provider will generate label updates
 * for those resources and the modle can translate them to appropriate
 * label updates of their model elements).
 * <p>
 * For those elements that do not have a one-to-one mapping to resources,
 * the model must do extra work. The purpose of this class is to allow
 * the model to decide when a label update for a logical model element is
 * required and to communicate the dirty state of their logical model
 * elements to the team decorator. For logical model elements, the team decorator
 * will only decorate based on the supervised state and the dirty state.
 * This class provides methods for determining both of these so that 
 * logical models can track whether a label update is required for a
 * model element. 
 * <p>
 * Model providers need to re-evaluate the state of a 
 * model element whenever a change in the resources occurs by listening
 * to both resource deltas and subscriber change events.
 * <p>
 * Decoration enablement changes and decoration configuration changes
 * are handled by the {@link IDecoratorManager#update(String)} API.
 * A call to this method will result in label changes to all elements.
 * The {@link #isDecorationEnabled(Object)} API on this class can 
 * be used to determine if an element will receive team decorations.
 * If decoration is disabled. team state changes on the element can
 * be ignored.
 *
 * @since 3.2
 * @see IWorkspace#addResourceChangeListener(IResourceChangeListener)
 * @see Subscriber#addListener(org.eclipse.team.core.subscribers.ISubscriberChangeListener)
 */
public class SynchronizationStateTester {

	/**
	 * Constant that is used as the property key on an {@link IDecorationContext}.
	 * If a context passed to a team decorator has this property, the associated
	 * state tester will be used by the deocator to determine whether elements
	 * have an outgoing change.
	 */
	public static final String PROP_TESTER = "org.eclipse.team.ui.syncStateTester"; //$NON-NLS-1$
	
	private Subscriber subscriber;
	
	/**
	 * Create a tester that uses the workspace subscriber to
	 * obtain the synchronization state of resources.
	 * @see Team#getWorksaceSubscriber()
	 */
	public SynchronizationStateTester() {
		this(Team.getWorksaceSubscriber());
	}
	
	/**
	 * Create a tester that obtains the synchroniation state
	 * from the given subscriber.
	 * @param subscriber the subscriber
	 */
	public SynchronizationStateTester(Subscriber subscriber) {
		this.subscriber = subscriber;
	}

	/**
	 * Return whether decoration is enabled for the given
	 * model element. If decoration is not enabled, the model
	 * does not need to fire label change events when the team state
	 * of the element changes.
	 * @param element the model element
	 * @return whether decoration is enabled for the given
	 * model element
	 */
	public final boolean isDecorationEnabled(Object element) {
		ResourceMapping mapping = Utils.getResourceMapping(element);
		if (mapping != null) {
			IProject[] projects = mapping.getProjects();
			return internalIsDecorationEnabled(projects);
		}
		return false;
	}
	
	private boolean internalIsDecorationEnabled(IProject[] projects) {
		Set providerIds = new HashSet();
		for (int i = 0; i < projects.length; i++) {
			IProject project = projects[i];
			String id = getProviderId(project);
			if (id != null)
				providerIds.add(id);
		}
		for (Iterator iter = providerIds.iterator(); iter.hasNext();) {
			String providerId = (String) iter.next();
			if (internalIsDecorationEnabled(providerId)) {
				return true;
			}
		}
		return false;
	}
	
	private String getProviderId(IProject project) {
		RepositoryProvider provider = RepositoryProvider.getProvider(project);
		if (provider != null)
			return provider.getID();
		return null;
	}

	private boolean internalIsDecorationEnabled(String providerId) {
		String decoratorId = getDecoratorId(providerId);
		if (decoratorId != null) {
			return PlatformUI.getWorkbench().getDecoratorManager().getEnabled(decoratorId);
		}
		return false;
	}

	private String getDecoratorId(String providerId) {
		return TeamDecoratorManager.getInstance().getDecoratorId(providerId);
	}

	/**
	 * Return whether the given element has an outgoing change. By default, the
	 * element is adapted to a resource mapping and, using the mapping's
	 * traversals, the subcriber is consulted to determine if the element has
	 * outgoing changes. Subclasses may override either in order to cache the
	 * dirty state for performance reasons or to change how dirty state is
	 * determined (e.g. if the element is represented in only a portion
	 * of a file or if the dirty state decoration is being done by the 
	 * label provider).
	 * 
	 * @param element the element being tested
	 * @return whether the given element has an outgoing change
	 * @throws CoreException if an error occurs
	 */
	public boolean hasOutgoingChange(Object element, boolean deep) throws CoreException {
		if (deep) {
			ResourceMapping mapping = Utils.getResourceMapping(element);
			if (mapping != null) {
				ResourceTraversal[] traversals = mapping.getTraversals(ResourceMappingContext.LOCAL_CONTEXT, null);
				return subscriber.hasLocalChanges(traversals, null);
			}
		} else {
			IResource resource = Utils.getResource(element);
			if (resource != null) {
				IDiffNode node = subscriber.getDiff(resource);
				if (node != null) {
					if (node instanceof IThreeWayDiff) {
						IThreeWayDiff twd = (IThreeWayDiff) node;
						return twd.getDirection() == IThreeWayDiff.OUTGOING 
							|| twd.getDirection() == IThreeWayDiff.CONFLICTING;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Return whether the given element is supervised. To determine, the
	 * element is adapted to a resource mapping and, using the mapping's
	 * traversals, the subcriber is consulted to determine if the element is supervised.
	 * An element is supervised if all the resources covered by it's traversals are
	 * supervised.
	 * @see Subscriber#isSupervised(org.eclipse.core.resources.IResource)
	 * 
	 * @param element the element being tested
	 * @return whether the given element is supervisied.
	 * @throws CoreException if an error occurres
	 */
	public final boolean isSupervised(Object element) throws CoreException {
		ResourceMapping mapping = Utils.getResourceMapping(element);
		if (mapping != null) {
			ResourceTraversal[] traversals = mapping.getTraversals(ResourceMappingContext.LOCAL_CONTEXT, null);
			for (int i = 0; i < traversals.length; i++) {
				ResourceTraversal traversal = traversals[i];
				final CoreException shared = new CoreException(new Status(IStatus.OK, TeamUIPlugin.ID, 0, "", null)); //$NON-NLS-1$
				try {
					traversal.accept(new IResourceVisitor() {
						public boolean visit(IResource resource) throws CoreException {
							if (subscriber.isSupervised(resource))
								throw shared;
							return false;
						}
					});
				} catch (CoreException e) {
					if (e == shared)
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * Return the subscriber associated with this tester.
	 * @return the subscriber associated with this tester.
	 */
	public Subscriber getSubscriber() {
		return subscriber;
	}
}
