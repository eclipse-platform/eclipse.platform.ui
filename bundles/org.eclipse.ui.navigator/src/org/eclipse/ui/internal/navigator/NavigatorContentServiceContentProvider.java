/*******************************************************************************
 * Copyright (c) 2003, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreePathContentProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.internal.navigator.extensions.NavigatorContentDescriptor;
import org.eclipse.ui.internal.navigator.extensions.NavigatorContentExtension;
import org.eclipse.ui.internal.navigator.extensions.NavigatorViewerDescriptor;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.INavigatorViewerDescriptor;
import org.eclipse.ui.navigator.IPipelinedTreeContentProvider;
import org.eclipse.ui.navigator.OverridePolicy;

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
 * @since 3.2
 * 
 */
public class NavigatorContentServiceContentProvider implements
		ITreeContentProvider, ITreePathContentProvider {

	private static final Object[] NO_CHILDREN = new Object[0];

	private final NavigatorContentService contentService;

	private final boolean isContentServiceSelfManaged;
	
	private final boolean enforceHasChildren;

	private Viewer viewer;

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
		INavigatorViewerDescriptor vDesc = contentService.getViewerDescriptor();
		enforceHasChildren = vDesc.getBooleanConfigProperty(NavigatorViewerDescriptor.PROP_ENFORCE_HAS_CHILDREN);
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
		INavigatorViewerDescriptor vDesc = contentService.getViewerDescriptor();
		enforceHasChildren = vDesc.getBooleanConfigProperty(NavigatorViewerDescriptor.PROP_ENFORCE_HAS_CHILDREN);
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
		contentService.resetContributionMemory();
		Set rootContentExtensions = contentService
				.findRootContentExtensions(anInputElement);
		if (rootContentExtensions.size() == 0) {
			return NO_CHILDREN;
		}
		ContributorTrackingSet finalElementsSet = new ContributorTrackingSet(contentService);
		ContributorTrackingSet localSet = new ContributorTrackingSet(contentService);		

		Object[] contributedChildren = null;
		NavigatorContentExtension foundExtension;
		NavigatorContentExtension[] overridingExtensions;
		for (Iterator itr = rootContentExtensions.iterator(); itr.hasNext();) {
			foundExtension = (NavigatorContentExtension) itr.next();
			try {

				if (!isOverridingExtensionInSet(foundExtension.getDescriptor(),
						rootContentExtensions)) {

					contributedChildren = foundExtension.internalGetContentProvider()
							.getElements(anInputElement);
					
					localSet.setContents(contributedChildren);

					overridingExtensions = foundExtension
							.getOverridingExtensionsForTriggerPoint(anInputElement);

					if (overridingExtensions.length > 0) { 
						localSet = pipelineChildren(anInputElement,
								overridingExtensions, localSet, ELEMENTS);						
					}
					finalElementsSet.addAll(localSet);
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
		return internalGetChildren(aParentElement);
	}

	private Object[] internalGetChildren(Object aParentElementOrPath) {
		contentService.resetContributionMemory();
		Object aParentElement = internalAsElement(aParentElementOrPath);
		Set enabledExtensions = contentService
				.findContentExtensionsByTriggerPoint(aParentElement);
		if (enabledExtensions.size() == 0) {
			return NO_CHILDREN;
		}
		ContributorTrackingSet finalChildrenSet = new ContributorTrackingSet(contentService);
		ContributorTrackingSet localSet = new ContributorTrackingSet(contentService);

		Object[] contributedChildren = null;
		NavigatorContentExtension foundExtension;
		NavigatorContentExtension[] overridingExtensions;
		for (Iterator itr = enabledExtensions.iterator(); itr.hasNext();) {
			foundExtension = (NavigatorContentExtension) itr.next();
			try {

				if (!isOverridingExtensionInSet(foundExtension.getDescriptor(),
						enabledExtensions)) {

					contributedChildren = foundExtension.internalGetContentProvider()
							.getChildren(aParentElementOrPath);

					overridingExtensions = foundExtension
							.getOverridingExtensionsForTriggerPoint(aParentElement);
					
					localSet.setContents(contributedChildren);

					if (overridingExtensions.length > 0) {
						// TODO: could pass tree path through pipeline						
						localSet = pipelineChildren(aParentElement,
								overridingExtensions, localSet, !ELEMENTS);
					}
					finalChildrenSet.addAll(localSet);
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

	private static final boolean ELEMENTS = true;
	
	/**
	 * Query each of <code>theOverridingExtensions</code> for children, and
	 * then pipe them through the Pipeline content provider.
	 * 
	 * @param aParentOrPath
	 *            The parent element in the tree
	 * @param theOverridingExtensions
	 *            The set of overriding extensions that should participate in
	 *            the pipeline chain
	 * @param theCurrentChildren
	 *            The current children to return to the viewer (should be
	 *            modifiable)
	 * @return The set of children to return to the viewer
	 */
	private ContributorTrackingSet pipelineChildren(Object aParentOrPath,
			NavigatorContentExtension[] theOverridingExtensions,
			ContributorTrackingSet theCurrentChildren, boolean elements) {
		IPipelinedTreeContentProvider pipelinedContentProvider;
		NavigatorContentExtension[] overridingExtensions;
		ContributorTrackingSet pipelinedChildren = theCurrentChildren;
		for (int i = 0; i < theOverridingExtensions.length; i++) {

						
			if (theOverridingExtensions[i].getContentProvider() instanceof IPipelinedTreeContentProvider) {
				pipelinedContentProvider = (IPipelinedTreeContentProvider) theOverridingExtensions[i]
						.getContentProvider();
				pipelinedChildren.setContributor((NavigatorContentDescriptor) theOverridingExtensions[i].getDescriptor());	
				if (elements) {
					pipelinedContentProvider.getPipelinedElements(aParentOrPath,
							pipelinedChildren);
				} else {
					pipelinedContentProvider.getPipelinedChildren(aParentOrPath,
							pipelinedChildren);
				}
				
				pipelinedChildren.setContributor(null);
				
				overridingExtensions = theOverridingExtensions[i]
						.getOverridingExtensionsForTriggerPoint(aParentOrPath);
				if (overridingExtensions.length > 0) {
					pipelinedChildren = pipelineChildren(aParentOrPath,
							overridingExtensions, pipelinedChildren, elements);
				}
			}
		}

		return pipelinedChildren;

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
	private boolean isOverridingExtensionInSet(
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
	 * Currently this method only checks one level deep. If the suppressed
	 * extension of the given descriptor is contained lower in the tree, then
	 * the extension could still be invoked twice.
	 * 
	 * @param aDescriptor
	 *            The descriptor which may be overriding other extensions.
	 * @param theEnabledDescriptors
	 *            The other available descriptors.
	 * @return True if the results should be pipelined through the downstream
	 *         extensions.
	 */
	private boolean isOverridingDescriptorInSet(
			INavigatorContentDescriptor aDescriptor, Set theEnabledDescriptors) {

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
			if (theEnabledDescriptors.contains(aDescriptor.getOverriddenDescriptor())) {
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
		contentService.resetContributionMemory();
		Set extensions = contentService
				.findContentExtensionsWithPossibleChild(anElement);

		Object parent;
		NavigatorContentExtension foundExtension;
		NavigatorContentExtension[] overridingExtensions;
		for (Iterator itr = extensions.iterator(); itr.hasNext();) {
			foundExtension = (NavigatorContentExtension) itr.next();
			try {

				if (!isOverridingExtensionInSet(foundExtension.getDescriptor(),
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
	 * @param theCurrentParent
	 *            The current elements to return to the viewer (should be
	 *            modifiable)
	 * @return The set of elements to return to the viewer
	 */
	private Object pipelineParent(Object anInputElement,
			NavigatorContentExtension[] theOverridingExtensions,
			Object theCurrentParent) {
		IPipelinedTreeContentProvider pipelinedContentProvider;
		NavigatorContentExtension[] overridingExtensions;
		Object aSuggestedParent = null;
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
		return aSuggestedParent != null ? aSuggestedParent : theCurrentParent;
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
			if (!ext.isLoaded() && !enforceHasChildren) {
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
	 * element and any initialization associated with the new input element.
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
		viewer = aViewer;
		contentService.updateService(aViewer, anOldInput, aNewInput);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreePathContentProvider#getChildren(org.eclipse.jface.viewers.TreePath)
	 */
	public Object[] getChildren(TreePath parentPath) {
		return internalGetChildren(parentPath);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreePathContentProvider#hasChildren(org.eclipse.jface.viewers.TreePath)
	 */
	public boolean hasChildren(TreePath path) {
		Object anElement = internalAsElement(path);
		Set resultInstances = contentService
				.findContentExtensionsByTriggerPoint(anElement);

		NavigatorContentExtension ext;
		for (Iterator itr = resultInstances.iterator(); itr.hasNext();) {
			ext = (NavigatorContentExtension) itr.next();
			if (!ext.isLoaded() && !enforceHasChildren)
				return true;
			ITreeContentProvider cp = ext.internalGetContentProvider();
			if (cp instanceof ITreePathContentProvider) {
				ITreePathContentProvider tpcp = (ITreePathContentProvider) cp;
				if (tpcp.hasChildren(path)) {
					return true;
				}
			} else if (cp.hasChildren(anElement))
				return true;
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreePathContentProvider#getParents(java.lang.Object)
	 */
	public TreePath[] getParents(Object anElement) {
		
		List paths = new ArrayList();
		TreePathCompiler compiler = new TreePathCompiler(anElement); 
		Set compilers = findPaths(compiler);
		for (Iterator iter = compilers.iterator(); iter.hasNext();) {
			TreePathCompiler c = (TreePathCompiler) iter.next();
			paths.add(c.createParentPath());
			
		}
		return (TreePath[]) paths.toArray(new TreePath[paths.size()]);
		 
	}
  

	/**
	 * Get the element from an element or tree path argument.
	 * @param parentElementOrPath the element or tree path
	 * @return the element
	 */
	private Object internalAsElement(Object parentElementOrPath) {
		if (parentElementOrPath instanceof TreePath) {
			TreePath tp = (TreePath) parentElementOrPath;
			if (tp.getSegmentCount() > 0) {
				return tp.getLastSegment();
			}
			// If the path is empty, the parent element is the root
			return viewer.getInput();
		}
		return parentElementOrPath;
	}
	

	class CyclicPathException extends Exception {

		private static final long serialVersionUID = 2111962579612444989L;

		protected CyclicPathException(TreePathCompiler compiler, Object invalidSegment, boolean asChild) {
			super("Cannot add " + invalidSegment + //$NON-NLS-1$ 
					" to the list of segments in " + compiler +  //$NON-NLS-1$ 
					(asChild ? " as a child." : " as a parent.") ); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	class TreePathCompiler {


		private final LinkedList segments = new LinkedList();

		protected TreePathCompiler(Object segment) {
			segments.add(segment);
		}

		protected TreePathCompiler(TreePathCompiler aCompiler) {
			segments.addAll(aCompiler.segments);
		}

		protected TreePathCompiler(TreePath aPath) {
			for (int i = 0; i < aPath.getSegmentCount(); i++) {
				segments.addLast(aPath.getSegment(i));
			}
		}

		protected void addParent(Object segment) throws CyclicPathException {
			if(segments.contains(segment)) {
				throw new CyclicPathException(this, segment, false);
			}
			segments.addFirst(segment);
		}

		protected void addChild(Object segment) throws CyclicPathException {
			if(segments.contains(segment)) {
				throw new CyclicPathException(this, segment, false);
			}
			segments.addLast(segment);
		}

		/**
		 * Create the full tree path.
		 * 
		 * @return A TreePath with all segments from the compiler.
		 */
		public TreePath createPath() {
			return new TreePath(segments.toArray());
		}

		/**
		 * Create parent tree path.
		 * 
		 * @return A TreePath with all segments but the last from the compiler
		 */
		public TreePath createParentPath() {
			LinkedList parentSegments = new LinkedList(segments);
			parentSegments.removeLast();
			return new TreePath(parentSegments.toArray());
		}
		
		public Object getLastSegment() {
			return segments.getLast();
		}
		
		public Object getFirstSegment() {
			return segments.getFirst();
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
		
			StringBuffer buffer = new StringBuffer();
			for (Iterator iter = segments.iterator(); iter.hasNext();) {
				Object segment = iter.next();
				buffer.append(segment).append("::"); //$NON-NLS-1$  
			}
			return buffer.toString();
		}

	}

	private Set findPaths(TreePathCompiler aPathCompiler) {

		Set/* <Object> */ parents = findParents(aPathCompiler.getFirstSegment());
		Set/* <TreePathCompiler> */ parentPaths = new LinkedHashSet();
		Set/* <TreePathCompiler> */ foundPaths = Collections.EMPTY_SET;
		if (parents.size() > 0) {
			for (Iterator parentIter = parents.iterator(); parentIter.hasNext();) {
				Object parent = (Object) parentIter.next();
				TreePathCompiler c = new TreePathCompiler(aPathCompiler);
				try {
					c.addParent(parent); 
					foundPaths = findPaths(c);
				} catch(CyclicPathException cpe) {
					String msg = cpe.getMessage() != null ? cpe.getMessage() : cpe.toString();
					NavigatorPlugin.logError(0, msg, cpe);
				}
				if (foundPaths.isEmpty())
					parentPaths.add(c);
				else
					parentPaths.addAll(foundPaths);
			}
		}
		return parentPaths;

	}

	private Set findParents(Object anElement) {

		Set descriptors = contentService.findDescriptorsWithPossibleChild(
				anElement, false);
		Set parents = new LinkedHashSet();
		NavigatorContentDescriptor foundDescriptor;
		NavigatorContentExtension foundExtension;
		Object parent = null;
		for (Iterator itr = descriptors.iterator(); itr.hasNext();) {
			foundDescriptor = (NavigatorContentDescriptor) itr.next();
			foundExtension = contentService.getExtension(foundDescriptor);
			try {

				if (!isOverridingDescriptorInSet(
						foundExtension.getDescriptor(), descriptors)) {

					/* internalGetContentProvider returns the real delegate */
					if (foundExtension.getContentProvider() instanceof ITreePathContentProvider) {
						/*
						 * but we use the safe version to automatically handle
						 * errors
						 */
						TreePath[] parentTreePaths = ((ITreePathContentProvider) foundExtension
								.internalGetContentProvider())
								.getParents(anElement);

						for (int i = 0; i < parentTreePaths.length; i++) {

							parent = parentTreePaths[i].getLastSegment();
							if ((parent = findParent(foundExtension, anElement,
									parent)) != null)
								parents.add(parent);
						}

					} else {

						parent = foundExtension.internalGetContentProvider()
								.getParent(anElement);
						if ((parent = findParent(foundExtension, anElement,
								parent)) != null)
							parents.add(parent);
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
		
		return parents;
		
	}
	 
	
	private Object findParent(NavigatorContentExtension anExtension, Object anElement, Object aSuggestedParent) {
		
		/* the last valid (non-null) parent for the anElement */
		Object lastValidParent = aSuggestedParent;
		/* used to keep track of new suggestions */
		Object suggestedOverriddenParent = null;
		IPipelinedTreeContentProvider piplineContentProvider; 
		NavigatorContentExtension[] overridingExtensions = anExtension.getOverridingExtensionsForPossibleChild(anElement); 
		for (int i = 0; i < overridingExtensions.length; i++) {
			if(overridingExtensions[i].getContentProvider() instanceof IPipelinedTreeContentProvider) {
				piplineContentProvider = (IPipelinedTreeContentProvider) overridingExtensions[i].getContentProvider(); 
				suggestedOverriddenParent = piplineContentProvider.getPipelinedParent(anElement, lastValidParent);
				
				if(suggestedOverriddenParent != null)
					lastValidParent = suggestedOverriddenParent; 
				
				// should never return null 
				lastValidParent = findParent(overridingExtensions[i], anElement, lastValidParent);
			}
				
		} 
		return lastValidParent;
	}

}
