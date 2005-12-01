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
package org.eclipse.ui.navigator.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.internal.extensions.NavigatorContentExtension;

/**
 * <p>
 * Provides relevant content based on the associated
 * {@link org.eclipse.ui.navigator.internal.NavigatorContentService}&nbsp; for a TreeViewer .
 * </p>
 * <p>
 * Except for the dependency on
 * {@link org.eclipse.ui.navigator.internal.NavigatorContentService}, this class has no
 * dependencies on the rest of the Common Navigator framework. Tree viewers that would like to use
 * the extensions defined by the Common Navigator, without using the actual view part or other
 * pieces of functionality (filters, sorting, etc) may choose to use this class, in effect using an
 * extensible, aggregating, delegate content provider.
 * </p>
 * 
 * @see org.eclipse.ui.navigator.internal.NavigatorContentService
 * @see org.eclipse.ui.navigator.internal.NavigatorContentServiceLabelProvider
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 *  
 */
public class NavigatorContentServiceContentProvider implements ITreeContentProvider {


	private static final Object[] NO_CHILDREN = new Object[0];
	private final NavigatorContentService contentService;
	private final boolean isContentServiceSelfManaged;

	/**
	 * <p>
	 * Creates a cached {@link NavigatorContentService}&nbsp;from the given viewer Id.
	 * </p>
	 * 
	 * @param aViewerId
	 *            The associated viewer id that this NavigatorContentServiceContentProvider will
	 *            provide content for
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
	 *            The associated NavigatorContentService that should be used to acquire information.
	 */
	public NavigatorContentServiceContentProvider(NavigatorContentService aContentService) {
		super();
		contentService = aContentService;
		isContentServiceSelfManaged = false;
	}

	/**
	 * 
	 * <p>
	 * Return the root objects for the supplied anInputElement. anInputElement is the root thing
	 * that the viewer visualizes.
	 * </p>
	 * <p>
	 * This method will call out to its {@link NavigatorContentService}&nbsp;for extensions that are
	 * enabled on the supplied anInputElement or enabled on the viewerId supplied when the
	 * {@link NavigatorContentService}&nbsp; was created (either by this class or its client). The
	 * extensions will then be queried for relevant content. The children returned from each
	 * extension will be aggregated and returned as is -- there is no additional sorting or
	 * filtering at this level.
	 * </p>
	 * <p>
	 * The results of this method will be displayed in the root of the TreeViewer.
	 * </p>
	 * {@inheritDoc}
	 * 
	 * @param anInputElement
	 *            The relevant element that a client would like children for - the input element of
	 *            the TreeViewer
	 * @return A non-null array of objects that are logical children of anInputElement
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public synchronized Object[] getElements(Object anInputElement) {
		ITreeContentProvider[] delegateProviders = contentService.findRootContentProviders(anInputElement);
		if (delegateProviders.length == 0)
			return NO_CHILDREN;
		List resultElements = new ArrayList();
		Object[] delegateChildren = null;
		for (int i = 0; i < delegateProviders.length; i++) {
			try {
				delegateChildren = delegateProviders[i].getElements(anInputElement);
				if (delegateChildren != null && delegateChildren.length > 0) 
					resultElements.addAll(Arrays.asList(delegateChildren)); 
			} catch (RuntimeException re) {
				String msg = CommonNavigatorMessages.NavigatorContentServiceContentProvider_0 + delegateProviders[i].getClass();
				NavigatorPlugin.log(msg, new Status(IStatus.ERROR, NavigatorPlugin.PLUGIN_ID, 0, msg, re));
			}
		}
		return resultElements.toArray();
	}

	/**
	 * <p>
	 * Return the children of the supplied aParentElement
	 * </p>
	 * 
	 * <p>
	 * This method will call out to its {@link NavigatorContentService}&nbsp;for extensions that are
	 * enabled on the supplied aParentElement. The extensions will then be queried for children for
	 * aParentElement. The children returned from each extension will be aggregated and returned as
	 * is -- there is no additional sorting or filtering at this level.
	 * </p>
	 * <p>
	 * The results of this method will be displayed as children of the supplied element in the
	 * TreeViewer.
	 * </p>
	 * {@inheritDoc}
	 * 
	 * @param aParentElement
	 *            An element that requires children content in the viewer (e.g. an end-user expanded
	 *            a node)
	 * @return A non-null array of objects that are logical children of aParentElement
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public synchronized Object[] getChildren(Object aParentElement) {
		ITreeContentProvider[] delegateProviders = contentService.findRelevantContentProviders(aParentElement);
		if (delegateProviders.length == 0)
			return NO_CHILDREN;
		List resultChildren = new ArrayList();
		Object[] delegateChildren = null;
		for (int i = 0; i < delegateProviders.length; i++) {
			try {
				delegateChildren = delegateProviders[i].getChildren(aParentElement);
				if (delegateChildren != null && delegateChildren.length > 0)
					resultChildren.addAll(Arrays.asList(delegateChildren));
			} catch (RuntimeException re) {
				String msg = CommonNavigatorMessages.NavigatorContentServiceContentProvider_1 + delegateProviders[i].getClass();
				NavigatorPlugin.log(msg, new Status(IStatus.ERROR, NavigatorPlugin.PLUGIN_ID, 0, msg, re));
			} catch (Error e) {
				String msg = CommonNavigatorMessages.NavigatorContentServiceContentProvider_2 + delegateProviders[i].getClass();
				NavigatorPlugin.log(msg, new Status(IStatus.ERROR, NavigatorPlugin.PLUGIN_ID, 0, msg, e));

			}
		}
		return resultChildren.toArray();
	}

	/**
	 * <p>
	 * Returns the logical parent of anElement.
	 * </p>
	 * <p>
	 * This method requires that any extension that would like an opportunity to supply a parent for
	 * anElement expressly indicate that in the action expression &lt;enables&gt; statement of the
	 * <b>org.eclipse.ui.navigator.navigatorContent </b> extension point.
	 * </p>
	 * {@inheritDoc}
	 * 
	 * @param anElement
	 *            An element that requires its logical parent - generally as a result of
	 *            setSelection(expand=true) on the viewer
	 * @return The logical parent if available or null if the parent cannot be determined
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public synchronized Object getParent(Object anElement) {
		ITreeContentProvider[] delegateProviders = contentService.findParentContentProviders(anElement);

		Object parent = null;
		for (int i = 0; i < delegateProviders.length; i++) {
			try {
				if ((parent = delegateProviders[i].getParent(anElement)) != null)
					return parent;
			} catch (RuntimeException re) {
				String msg = CommonNavigatorMessages.NavigatorContentServiceContentProvider_3 + delegateProviders[i].getClass();
				NavigatorPlugin.log(msg, new Status(IStatus.ERROR, NavigatorPlugin.PLUGIN_ID, 0, msg, re));
			}
		}
		return null;
	}

	/**
	 * <p>
	 * Used to determine of anElement should be displayed with a '+' or not.
	 * </p>
	 * {@inheritDoc}
	 * 
	 * @param anElement
	 *            The element in question
	 * @return True if anElement has logical children as returned by this content provider.
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public synchronized boolean hasChildren(Object anElement) {
		NavigatorContentExtension[] resultInstances = contentService.findRelevantContentExtensions(anElement);

		for (int i = 0; i < resultInstances.length; i++) {
			if (!resultInstances[i].isLoaded())
				return true;
			else if (resultInstances[i].getContentProvider().hasChildren(anElement))
				return true;
		}

		return false;
	}

	/**
	 * <p>
	 * Handles any necessary clean up of the {@link NavigatorContentService}
	 * </p>
	 * 
	 * <p>
	 * <b>If a client uses this class outside of the framework of {@link CommonViewer}, the client must ensure that this method
	 * is called when finished. </b>
	 * </p>
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public synchronized void dispose() {
		if (isContentServiceSelfManaged)
			contentService.dispose();
	}

	/**
	 * <p>
	 * Indicates that the current content provider is now representing a different input element.
	 * The input element is the root thing that the viewer displays.
	 * </p>
	 * <p>
	 * This method should handle any cleanup associated with the old input element and any
	 * initiailization associated with the new input element.
	 * </p>
	 * {@inheritDoc}
	 * 
	 * @param aViewer
	 *            The viewer that the current content provider is associated with
	 * @param anOldInput
	 *            The original input element that the viewer was visualizing
	 * @param aNewInput
	 *            The new input element that the viewer will visualize.
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object, java.lang.Object)
	 *  
	 */
	public synchronized void inputChanged(Viewer aViewer, Object anOldInput, Object aNewInput) {
		contentService.updateService(aViewer, anOldInput, aNewInput);
	}


}
