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
package org.eclipse.ui.internal.navigator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.internal.navigator.extensions.NavigatorContentExtension;
import org.eclipse.ui.internal.navigator.extensions.OverridePolicy;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.IPipelinedTreeContentProvider;

/**
 * <p>
 * Provides relevant content based on the associated
 * {@link org.eclipse.ui.internal.navigator.NavigatorContentService}&nbsp; for
 * a TreeViewer .
 * </p>
 * <p>
 * Except for the dependency on
 * {@link org.eclipse.ui.internal.navigator.NavigatorContentService}, this
 * class has no dependencies on the rest of the Common Navigator framework. Tree
 * viewers that would like to use the extensions defined by the Common
 * Navigator, without using the actual view part or other pieces of
 * functionality (filters, sorting, etc) may choose to use this class, in effect
 * using an extensible, aggregating, delegate content provider.
 * </p>
 * 
 * @see org.eclipse.ui.internal.navigator.NavigatorContentService
 * @see org.eclipse.ui.internal.navigator.NavigatorContentServiceLabelProvider
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 * 
 */
public class NavigatorContentServiceContentProvider implements
		ITreeContentProvider {

	private static final Object[] NO_CHILDREN = new Object[0];

	private final NavigatorContentService contentService;

	private final boolean isContentServiceSelfManaged;

	/**
	 * <p>
	 * Creates a cached {@link NavigatorContentService}&nbsp;from the given
	 * viewer Id.
	 * </p>
	 * 
	 * @param aViewerId
	 *            The associated viewer id that this
	 *            NavigatorContentServiceContentProvider will provide content
	 *            for
	 */
	public NavigatorContentServiceContentProvider(String aViewerId) {
		super();
		contentService = new NavigatorContentService(aViewerId);
		isContentServiceSelfManaged = true;
	}

	/**
	 * <p>
	 * Uses the supplied content service to acquire the available extensions.
	 * </p>
	 * 
	 * @param aContentService
	 *            The associated NavigatorContentService that should be used to
	 *            acquire information.
	 */
	public NavigatorContentServiceContentProvider(
			NavigatorContentService aContentService) {
		super();
		contentService = aContentService;
		isContentServiceSelfManaged = false;
	}

	/**
	 * 
	 * <p>
	 * Return the root objects for the supplied anInputElement. anInputElement
	 * is the root thing that the viewer visualizes.
	 * </p>
	 * <p>
	 * This method will call out to its {@link NavigatorContentService}&nbsp;for
	 * extensions that are enabled on the supplied anInputElement or enabled on
	 * the viewerId supplied when the {@link NavigatorContentService}&nbsp; was
	 * created (either by this class or its client). The extensions will then be
	 * queried for relevant content. The children returned from each extension
	 * will be aggregated and returned as is -- there is no additional sorting
	 * or filtering at this level.
	 * </p>
	 * <p>
	 * The results of this method will be displayed in the root of the
	 * TreeViewer.
	 * </p>
	 * {@inheritDoc}
	 * 
	 * @param anInputElement
	 *            The relevant element that a client would like children for -
	 *            the input element of the TreeViewer
	 * @return A non-null array of objects that are logical children of
	 *         anInputElement
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public synchronized Object[] getElements(Object anInputElement) {
		Set rootContentExtensions = contentService
				.findRootContentExtensions(anInputElement);
		if (rootContentExtensions.size() == 0) {
			return NO_CHILDREN;
		}
		Set finalElementsSet = new LinkedHashSet();

		Object[] contributedChildren = null;
		NavigatorContentExtension foundExtension;
		NavigatorContentExtension[] overridingExtensions;
		for (Iterator itr = rootContentExtensions.iterator(); itr.hasNext();) {
			foundExtension = (NavigatorContentExtension) itr.next();
			try {

				if (!shouldDeferToOverridePath(foundExtension.getDescriptor(),
						rootContentExtensions)) {

					contributedChildren = foundExtension.internalGetContentProvider()
							.getElements(anInputElement);

					overridingExtensions = foundExtension
							.getOverridingExtensionsForTriggerPoint(anInputElement);

					if (overridingExtensions.length > 0) {
						contributedChildren = pipelineElements(anInputElement,
								overridingExtensions,
								new HashSet(Arrays.asList(contributedChildren)))
								.toArray();
					}

					if (contributedChildren != null
							&& contributedChildren.length > 0) {
						finalElementsSet.addAll(Arrays
								.asList(contributedChildren));
					}
				}
			} catch (RuntimeException re) {
				NavigatorPlugin
						.logError(
								0,
								NLS
										.bind(
												CommonNavigatorMessages.Could_not_provide_children_for_element,
												new Object[] { foundExtension
														.getDescriptor()
														.getId() }), re);
			} catch (Error e) {
				NavigatorPlugin
						.logError(
								0,
								NLS
										.bind(
												CommonNavigatorMessages.Could_not_provide_children_for_element,
												new Object[] { foundExtension
														.getDescriptor()
														.getId() }), e);

			}
		}
		return finalElementsSet.toArray();
	}

	/**
	 * <p>
	 * Return the children of the supplied aParentElement
	 * </p>
	 * 
	 * <p>
	 * This method will call out to its {@link NavigatorContentService}&nbsp;for
	 * extensions that are enabled on the supplied aParentElement. The
	 * extensions will then be queried for children for aParentElement. The
	 * children returned from each extension will be aggregated and returned as
	 * is -- there is no additional sorting or filtering at this level.
	 * </p>
	 * <p>
	 * The results of this method will be displayed as children of the supplied
	 * element in the TreeViewer.
	 * </p>
	 * {@inheritDoc}
	 * 
	 * @param aParentElement
	 *            An element that requires children content in the viewer (e.g.
	 *            an end-user expanded a node)
	 * @return A non-null array of objects that are logical children of
	 *         aParentElement
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public synchronized Object[] getChildren(Object aParentElement) {
		Set enabledExtensions = contentService
				.findContentExtensionsByTriggerPoint(aParentElement);
		if (enabledExtensions.size() == 0) {
			return NO_CHILDREN;
		}
		Set finalChildrenSet = new LinkedHashSet();

		Object[] contributedChildren = null;
		NavigatorContentExtension foundExtension;
		NavigatorContentExtension[] overridingExtensions;
		for (Iterator itr = enabledExtensions.iterator(); itr.hasNext();) {
			foundExtension = (NavigatorContentExtension) itr.next();
			try {

				if (!shouldDeferToOverridePath(foundExtension.getDescriptor(),
						enabledExtensions)) {

					contributedChildren = foundExtension.internalGetContentProvider()
							.getChildren(aParentElement);

					overridingExtensions = foundExtension
							.getOverridingExtensionsForTriggerPoint(aParentElement);

					if (overridingExtensions.length > 0) {
						contributedChildren = pipelineChildren(aParentElement,
								overridingExtensions,
								new HashSet(Arrays.asList(contributedChildren)))
								.toArray();
					}

					if (contributedChildren != null
							&& contributedChildren.length > 0) {
						finalChildrenSet.addAll(Arrays
								.asList(contributedChildren));
					}
				}
			} catch (RuntimeException re) {
				NavigatorPlugin
						.logError(
								0,
								NLS
										.bind(
												CommonNavigatorMessages.Could_not_provide_children_for_element,
												new Object[] { foundExtension
														.getDescriptor()
														.getId() }), re);
			} catch (Error e) {
				NavigatorPlugin
						.logError(
								0,
								NLS
										.bind(
												CommonNavigatorMessages.Could_not_provide_children_for_element,
												new Object[] { foundExtension
														.getDescriptor()
														.getId() }), e);

			}
		}
		return finalChildrenSet.toArray();
	}

	/**
	 * Query each of <code>theOverridingExtensions</code> for children, and
	 * then pipe them through the Pipeline content provider.
	 * 
	 * @param aParent
	 *            The parent element in the tree
	 * @param theOverridingExtensions
	 *            The set of overriding extensions that should participate in
	 *            the pipeline chain
	 * @param theCurrentChildren
	 *            The current children to return to the viewer (should be
	 *            modifiable)
	 * @return The set of children to return to the viewer
	 */
	private Set pipelineChildren(Object aParent,
			NavigatorContentExtension[] theOverridingExtensions,
			Set theCurrentChildren) {
		IPipelinedTreeContentProvider pipelinedContentProvider;
		NavigatorContentExtension[] overridingExtensions;
		Set pipelinedChildren = theCurrentChildren;
		for (int i = 0; i < theOverridingExtensions.length; i++) {

			if (theOverridingExtensions[i].getContentProvider() instanceof IPipelinedTreeContentProvider) {
				pipelinedContentProvider = (IPipelinedTreeContentProvider) theOverridingExtensions[i]
						.getContentProvider();
				pipelinedContentProvider.getPipelinedChildren(aParent,
						pipelinedChildren);
				overridingExtensions = theOverridingExtensions[i]
						.getOverridingExtensionsForTriggerPoint(aParent);
				if (overridingExtensions.length > 0) {
					pipelinedChildren = pipelineChildren(aParent,
							overridingExtensions, pipelinedChildren);
				}
			}
		}

		return pipelinedChildren;

	}

	/**
	 * Query each of <code>theOverridingExtensions</code> for elements, and
	 * then pipe them through the Pipeline content provider.
	 * 
	 * @param anInputElement
	 *            The input element in the tree
	 * @param theOverridingExtensions
	 *            The set of overriding extensions that should participate in
	 *            the pipeline chain
	 * @param theCurrentElements
	 *            The current elements to return to the viewer (should be
	 *            modifiable)
	 * @return The set of elements to return to the viewer
	 */
	private Set pipelineElements(Object anInputElement,
			NavigatorContentExtension[] theOverridingExtensions,
			Set theCurrentElements) {
		IPipelinedTreeContentProvider pipelinedContentProvider;
		NavigatorContentExtension[] overridingExtensions;
		Set pipelinedElements = theCurrentElements;
		for (int i = 0; i < theOverridingExtensions.length; i++) {

			if (theOverridingExtensions[i].getContentProvider() instanceof IPipelinedTreeContentProvider) {
				pipelinedContentProvider = (IPipelinedTreeContentProvider) theOverridingExtensions[i]
						.getContentProvider();

				pipelinedContentProvider.getPipelinedElements(anInputElement,
						pipelinedElements);

				overridingExtensions = theOverridingExtensions[i]
						.getOverridingExtensionsForTriggerPoint(anInputElement);
				if (overridingExtensions.length > 0) {
					pipelinedElements = pipelineElements(anInputElement,
							overridingExtensions, pipelinedElements);
				}
			}
		}
		return pipelinedElements;
	}

	/**
	 * Currently this method only checks one level deep. If the suppressed
	 * extension of the given descriptor is contained lower in the tree, then
	 * the extension could still be invoked twice.
	 * 
	 * @param aDescriptor
	 *            The descriptor which may be overriding other extensions.
	 * @param theEnabledExtensions
	 *            The other available extensions.
	 * @return True if the results should be pipelined through the downstream
	 *         extensions.
	 */
	private boolean shouldDeferToOverridePath(
			INavigatorContentDescriptor aDescriptor, Set theEnabledExtensions) {

		if (aDescriptor.getSuppressedExtensionId() != null /*
															 * The descriptor is
															 * an override
															 * descriptor
															 */
				&& aDescriptor.getOverridePolicy() == OverridePolicy.InvokeAlwaysRegardlessOfSuppressedExt) {
			/*
			 * if the policy is set as such, it can lead to this extension being
			 * invoked twice; once as a first class extension, and once an
			 * overriding extension.
			 */
			if (theEnabledExtensions.contains(contentService
					.getExtension(aDescriptor.getOverriddenDescriptor()))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * <p>
	 * Returns the logical parent of anElement.
	 * </p>
	 * <p>
	 * This method requires that any extension that would like an opportunity to
	 * supply a parent for anElement expressly indicate that in the action
	 * expression &lt;enables&gt; statement of the
	 * <b>org.eclipse.ui.navigator.navigatorContent </b> extension point.
	 * </p>
	 * {@inheritDoc}
	 * 
	 * @param anElement
	 *            An element that requires its logical parent - generally as a
	 *            result of setSelection(expand=true) on the viewer
	 * @return The logical parent if available or null if the parent cannot be
	 *         determined
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public synchronized Object getParent(Object anElement) {
		Set extensions = contentService
				.findContentExtensionsWithPossibleChild(anElement);

		Object parent;
		NavigatorContentExtension foundExtension;
		NavigatorContentExtension[] overridingExtensions;
		for (Iterator itr = extensions.iterator(); itr.hasNext();) {
			foundExtension = (NavigatorContentExtension) itr.next();
			try {

				if (!shouldDeferToOverridePath(foundExtension.getDescriptor(),
						extensions)) {

					parent = foundExtension.internalGetContentProvider().getParent(
							anElement);

					overridingExtensions = foundExtension
							.getOverridingExtensionsForPossibleChild(anElement);

					if (overridingExtensions.length > 0) {
						parent = pipelineParent(anElement,
								overridingExtensions, parent);
					}

					if (parent != null) {
						return parent;
					}
				}
			} catch (RuntimeException re) {
				NavigatorPlugin
						.logError(
								0,
								NLS
										.bind(
												CommonNavigatorMessages.Could_not_provide_children_for_element,
												new Object[] { foundExtension
														.getDescriptor()
														.getId() }), re);
			} catch (Error e) {
				NavigatorPlugin
						.logError(
								0,
								NLS
										.bind(
												CommonNavigatorMessages.Could_not_provide_children_for_element,
												new Object[] { foundExtension
														.getDescriptor()
														.getId() }), e);

			}
		}

		return null;
	}

	/**
	 * Query each of <code>theOverridingExtensions</code> for elements, and
	 * then pipe them through the Pipeline content provider.
	 * 
	 * @param anInputElement
	 *            The input element in the tree
	 * @param theOverridingExtensions
	 *            The set of overriding extensions that should participate in
	 *            the pipeline chain
	 * @param aSuggestedParent
	 *            The current elements to return to the viewer (should be
	 *            modifiable)
	 * @return The set of elements to return to the viewer
	 */
	private Object pipelineParent(Object anInputElement,
			NavigatorContentExtension[] theOverridingExtensions,
			Object aSuggestedParent) {
		IPipelinedTreeContentProvider pipelinedContentProvider;
		NavigatorContentExtension[] overridingExtensions;
		for (int i = 0; i < theOverridingExtensions.length; i++) {

			if (theOverridingExtensions[i].getContentProvider() instanceof IPipelinedTreeContentProvider) {
				pipelinedContentProvider = (IPipelinedTreeContentProvider) theOverridingExtensions[i]
						.getContentProvider();

				aSuggestedParent = pipelinedContentProvider.getPipelinedParent(
						anInputElement, aSuggestedParent);

				overridingExtensions = theOverridingExtensions[i]
						.getOverridingExtensionsForTriggerPoint(anInputElement);
				if (overridingExtensions.length > 0) {
					aSuggestedParent = pipelineParent(anInputElement,
							overridingExtensions, aSuggestedParent);
				}
			}
		}
		return aSuggestedParent;
	}

	/**
	 * <p>
	 * Used to determine of anElement should be displayed with a '+' or not.
	 * </p>
	 * {@inheritDoc}
	 * 
	 * @param anElement
	 *            The element in question
	 * @return True if anElement has logical children as returned by this
	 *         content provider.
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public synchronized boolean hasChildren(Object anElement) {
		Set resultInstances = contentService
				.findContentExtensionsByTriggerPoint(anElement);

		NavigatorContentExtension ext;
		for (Iterator itr = resultInstances.iterator(); itr.hasNext();) {
			ext = (NavigatorContentExtension) itr.next();
			if (!ext.isLoaded()) {
				return true;
			} else if (ext.internalGetContentProvider().hasChildren(anElement)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * <p>
	 * Handles any necessary clean up of the {@link NavigatorContentService}
	 * </p>
	 * 
	 * <p>
	 * <b>If a client uses this class outside of the framework of
	 * {@link CommonViewer}, the client must ensure that this method is called
	 * when finished. </b>
	 * </p>
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public synchronized void dispose() {
		if (isContentServiceSelfManaged) {
			contentService.dispose();
		}
	}

	/**
	 * <p>
	 * Indicates that the current content provider is now representing a
	 * different input element. The input element is the root thing that the
	 * viewer displays.
	 * </p>
	 * <p>
	 * This method should handle any cleanup associated with the old input
	 * element and any initiailization associated with the new input element.
	 * </p>
	 * {@inheritDoc}
	 * 
	 * @param aViewer
	 *            The viewer that the current content provider is associated
	 *            with
	 * @param anOldInput
	 *            The original input element that the viewer was visualizing
	 * @param aNewInput
	 *            The new input element that the viewer will visualize.
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object, java.lang.Object)
	 * 
	 */
	public synchronized void inputChanged(Viewer aViewer, Object anOldInput,
			Object aNewInput) {
		contentService.updateService(aViewer, anOldInput, aNewInput);
	}

}
