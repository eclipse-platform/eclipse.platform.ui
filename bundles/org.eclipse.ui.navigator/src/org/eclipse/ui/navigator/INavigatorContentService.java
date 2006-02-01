/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator;

import java.util.Set;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ui.IMemento;

/**
 * 
 * The INavigatorContentService manages extensions for extensible viewers. The
 * service can locate the appropriate providers (for contents or labels) for an
 * element and provide a ready-to-go {@link ITreeContentProvider} and
 * {@link ILabelProvider} for viewers that wish to take advantage of the
 * <b>org.eclipse.ui.navigator.navigatorContent</b> extensions defined for a
 * particular <i>viewerId</i>.
 * 
 * <p>
 * Clients should create instances of the this class using the factory ({@link NavigatorContentServiceFactory}).
 * </p>
 * 
 * <p>
 * Clients may contribute logical extensions using
 * <b>org.eclipse.ui.navigator.navigatorContent</b>. Each extension has three
 * states which determine whether the extension is used by the content service:
 * <ul>
 * <li><i>visible</i>: If a content extension id matches a
 * <b>viewerContentBinding</b> for the <i>viewerId</i> of this content
 * service, then the extension is 'visible'. Visible extensions may only be
 * configured through <b>viewerContentBinding</b>s. </li>
 * <li><i>active</i>: The active state may be set to a default using the
 * <i>activeByDefault</i> attribute of <b>navigatorContent</b>. Users may
 * toggle the 'active' state through the 'Available extensions' dialog. Clients
 * may also configure the active extensions using
 * {@link #activateExtensions(String[], boolean)} or
 * {@link #deactivateExtensions(String[], boolean)}. </li>
 * <li><i>enabled</i>: An extension is 'enabled' for an element if the
 * extension contributed that element or if the element is described in the
 * <i>triggerPoints</i> element of the <b>navigatorContent</b> extension. The
 * findXXX() methods search for 'enabled' extensions. </li>
 * </ul>
 * </p>
 * <p>
 * A new instance of the content service should be created for each viewer.
 * Clients should use {@link #createCommonContentProvider()} and
 * {@link #createCommonLabelProvider()} for the viewer. Each content service
 * tracks the viewer it is attached to. Clients may create the content service
 * with a viewer using ({@link NavigatorContentServiceFactory#createContentService(String, org.eclipse.jface.viewers.StructuredViewer)}).
 * Alternatively, when the content provider is created and set on a viewer,
 * {@link IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, Object, Object)}
 * will be called and the content provider will update the viewer used by its
 * backing content service. Therefore, only each content service has exactly one
 * content provider and one label provider.
 * </p>
 * <p>
 * Extensions may also coordinate their behavior through a
 * {@link IExtensionStateModel state model}. The state model holds properties
 * and supports property change listeners. Actions can toggle the setting of
 * properties and the corresponding content/label providers will respond to
 * property change event. Each <b>navigatorContent</b> extension has its own
 * contained state model keyed off of the content extension id.
 * </p>
 * <p>
 * Clients may respond when content extensions are loaded by attaching a
 * {@link INavigatorContentServiceListener} to the content service.
 * </p>
 * <p>
 * Some extensions may provide content or label providers which implement
 * {@link IMemento}. Clients must call {@link #restoreState(IMemento)} and
 * {@link #saveState(IMemento)} at the appropriate times for these extensions to
 * prepare themselves with the memento.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @since 3.2
 * 
 */
public interface INavigatorContentService {

	/**
	 * Create a Content Provider which will use an enhanced delegation model to
	 * locate extension content providers using this content service for each
	 * element in the tree.
	 * 
	 * <p>
	 * The content provider returned will populate the root of the viewer in one
	 * of two ways.
	 * <p>
	 * In the first approach, the content provider will seek out content
	 * extensions which are bound using a <b>viewerContentBinding</b>. If any
	 * of the found <b>viewerContentBindings</b> declare the <i>isRoot</i>
	 * attribute on as true, then that set of extensions will be consulted for
	 * the root elements of the tree. The input of the viewer will be supplied
	 * to each of their {@link IStructuredContentProvider#getElements(Object)}
	 * methods and aggregate the results for the root of the viewer.
	 * </p>
	 * <p>
	 * In the second approach, if no <b>viewerContentBindings</b> declare
	 * <i>isRoot</i> as true, then all matching extensions are consulted based
	 * on their <b>triggerPoints</b> expression in the <b>navigatorContent</b>
	 * extension. Any matching extensions are then consulted via their
	 * {@link IStructuredContentProvider#getElements(Object)} methods and the
	 * results are aggregated into the root.
	 * </p>
	 * <p>
	 * After the root is populated, the children of each root element are
	 * determined by consulting the source extension and all extension which
	 * describe the element in their <b>triggerPoints</b> expression.
	 * </p>
	 * <p>
	 * If clients wish to use a viewer other than the CommonViewer, then they
	 * are responsible for creating the content provider, and setting it on
	 * their viewer.
	 * </p>
	 * 
	 * @return An enhanced content provider that will use this content service
	 *         to drive the viewer.
	 */
	ITreeContentProvider createCommonContentProvider();

	/**
	 * Create a Label Provider which will use an enhanced delegation model to
	 * locate extension label providers using this content service for each
	 * element in the tree.
	 * 
	 * <p>
	 * The label of each element is determined by consulting the source of the
	 * element. If the source chooses to return null, then other extensions
	 * which declare the element in their <b>triggerPoints</b> extension are
	 * consulted. The first non-null value is used (including the empty label).
	 * </p>
	 * 
	 * <p>
	 * If clients wish to use a viewer other than the CommonViewer, then they
	 * are responsible for creating the label provider, and setting it on their
	 * viewer.
	 * </p>
	 * 
	 * @return An enhanced label provider that will use this content service to
	 *         drive labels in the viewer.
	 */
	ILabelProvider createCommonLabelProvider();

	/**
	 * The state model stores properties associated with the extension. Each
	 * content extension has its own contained state model. Components of the
	 * extension (content provider, label provider, action providers, etc) may
	 * attach themselves as listeners to the model ({@link IExtensionStateModel#addPropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)})
	 * and respond to changes to the values of the properties.
	 * 
	 * @param anExtensionId
	 *            The extension id defined by a <b>navigatorContent</b>
	 *            extension.
	 * @return The state model for the given extension id.
	 */
	IExtensionStateModel findStateModel(String anExtensionId);

	/**
	 * Return a set of content providers that could provide a parent for the
	 * given element. These content extensions are determined by consulting the
	 * <b>possibleChildren</b> expression in the <b>navigatorContent</b>
	 * extension.
	 * 
	 * <p>
	 * Clients that wish to tap into the link with editor support must describe
	 * all of their possible children in their <b>possibleChildren</b>
	 * expressions.
	 * </p>
	 * 
	 * @param anElement
	 *            An element from the tree (generally from a setSelection()
	 *            method).
	 * @return The set of content providers that may be able to provide a
	 *         parent.
	 */
	ITreeContentProvider[] findParentContentProviders(Object anElement);

	/**
	 * <p>
	 * Return all of the content providers that are relevant for the viewer. The
	 * viewer is determined by the ID used to create the
	 * INavigatorContentService ({@link #getViewerId() }). See
	 * {@link #createCommonContentProvider() } for more information about how
	 * content providers are located for the root of the viewer. The root
	 * content providers are calculated once. If a new element is supplied, a
	 * client must call {@link #update() } prior in order to reset the
	 * calculated root providers.
	 * </p>
	 * 
	 * @param anElement
	 *            An element from the tree (generally the input of the viewer)
	 * @return The set of content providers that can provide root elements for a
	 *         viewer.
	 */
	ITreeContentProvider[] findRootContentProviders(Object anElement);

	/**
	 * Return all of the content providers that are enabled for the given
	 * element. An 'enabled' content provider is either the (1) source of the
	 * element (the element was returned as a child of its parent by the content
	 * provider) or (2) a content extension which describes the element in its
	 * <b>triggerPoints</b> expression.
	 * 
	 * @param anElement
	 *            An element from the tree (generally the element expanded by
	 *            the user)
	 * @return The set of content providers that can provide valid children for
	 *         the element.
	 */
	ITreeContentProvider[] findRelevantContentProviders(Object anElement);

	/**
	 * 
	 * Return all of the label providers that are enabled for the given element.
	 * A label provider is 'enabled' if its corresponding content provider
	 * returned the element, or the element is described in the content
	 * extension's <b>triggerPoints</b> expression.
	 * 
	 * @param anElement
	 *            An element from the tree (any element contributed to the
	 *            tree).
	 * @return The set of label providers that may be able to provide a valid
	 *         (non-null) label.
	 */

	ILabelProvider[] findRelevantLabelProviders(Object anElement);

	/**
	 * The viewer id is used to locate matching <b>viewerContentBindings</b>.
	 * In general, this would be the id of the view defined by a
	 * <b>org.eclipse.ui.views</b> extension. However, there is no formal
	 * requirement that this is the case.
	 * 
	 * @return The viewerId used to create this content service.
	 */
	String getViewerId();

	/**
	 * The viewer descriptor provides some basic information about the abstract
	 * viewer that uses this content service.
	 * 
	 * @return The viewer descriptor for this content service.
	 * @see INavigatorViewerDescriptor
	 */
	INavigatorViewerDescriptor getViewerDescriptor();

	/**
	 * @param anExtensionId
	 *            The unqiue identifier from a content extension.
	 * @return True if and only if the given extension id is <i>active</i> for
	 *         this content service.
	 * @see INavigatorContentService For more information on what <i>active</i>
	 *      means.
	 * 
	 */
	boolean isActive(String anExtensionId);

	/**
	 * @param anExtensionId
	 *            The unqiue identifier from a content extension.
	 * @return True if and only if the given extension id is <i>visible</i> to
	 *         this content service.
	 * @see INavigatorContentService For more information on what <i>visible</i>
	 *      means.
	 */
	boolean isVisible(String anExtensionId);

	/**
	 * @return The set of <i>visible</i> extension ids for this content service
	 */
	String[] getVisibleExtensionIds();

	/**
	 * @return The set of <i>visible</i> content descriptors for this content
	 *         service
	 */
	INavigatorContentDescriptor[] getVisibleExtensions();

	/**
	 * Bind the set of given extensions to this content service. Programmatic
	 * bindings allow clients to make extensions <i>visible</i> to an instance
	 * of the content service by appending to the bindings declared through
	 * <b>org.eclipse.ui.navigator.viewer</b>. Programmtic bindings are not
	 * persisted and are not remembered or propagated to other instances of the
	 * INavigatorContentService in the same session. Programmatic bindings
	 * cannot be undone for a given instance of the INavigatorContentService and
	 * do not override declarative bindings.
	 * <p>
	 * Once a content extension has been bound to the INavigatorContentService,
	 * clients may use {@link #activateExtensions(String[], boolean) } or
	 * {@link #deactivateExtensions(String[], boolean) } to control the
	 * <i>activation</i> state of the extension. See
	 * {@link INavigatorContentService} for more information on the difference
	 * between <i>visible</i> and <i>active</i>.
	 * </p>
	 * 
	 * @param extensionIds
	 *            The list of extensions to make visible.
	 * @param isRoot
	 *            whether the context provider shold be a root content provider
	 * @return A list of all INavigatorContentDescriptors that correspond to the
	 *         given extensionIds.
	 */
	INavigatorContentDescriptor[] bindExtensions(String[] extensionIds,
			boolean isRoot);

	/**
	 * Activate the extensions specified by the extensionIds array. Clients may
	 * also choose to disable all other extensions. The set of descriptors
	 * returned is the set that were activated as a result of this call. In the
	 * case of this method, that means that a descriptor will be returned for
	 * each extensionId in the array, regardless of whether that extension is
	 * already enabled.
	 * 
	 * @param extensionIds
	 *            The list of extensions to activate
	 * @param toDeactivateAllOthers
	 *            True will deactivate all other extensions; False will leave
	 *            the other activations as-is
	 * @return A list of all INavigatorContentDescriptors that were activated as
	 *         a result of this call. This will be the set of
	 *         INavigatorContentDescriptors that corresponds exactly to the set
	 *         of given extensionIds.
	 */
	INavigatorContentDescriptor[] activateExtensions(String[] extensionIds,
			boolean toDeactivateAllOthers);

	/**
	 * Deactivate the extensions specified by the extensionIds. Clients may
	 * choose to activate all other extensions which are not explicitly
	 * disabled. If toActivateAllOthers is true, the array of returned
	 * descriptors will be the collection of all extensions not specified in the
	 * extensionIds array. If it is false, the array will be empty.
	 * 
	 * @param extensionIds
	 *            The list of extensions to activate
	 * @param toActivateAllOthers
	 *            True will activate all other extensions; False will leave the
	 *            other activations as-is
	 * @return A list of all INavigatorContentDescriptors that were activated as
	 *         a result of this call. If toActivateAllOthers is false, the
	 *         result will be an empty array. Otherwise, it will be the set of
	 *         all visible extensions minus those given in the 'extensionIds'
	 *         parameter.
	 */
	INavigatorContentDescriptor[] deactivateExtensions(String[] extensionIds,
			boolean toActivateAllOthers);

	/**
	 * 
	 * @param aMemento
	 *            The memento for extensions to use when restoring previous
	 *            settings.
	 */
	void restoreState(IMemento aMemento);

	/**
	 * 
	 * @param aMemento
	 *            The memento for extensions to use when persisting previous
	 *            settings.
	 */
	void saveState(IMemento aMemento);

	/**
	 * Add a listener to be notified whenever an extension is loaded.
	 * 
	 * @param aListener
	 *            A listener to be attached.
	 */
	void addListener(INavigatorContentServiceListener aListener);

	/**
	 * Remove a listener (by identity) from the set of listeners.
	 * 
	 * @param aListener
	 *            A listener to be detached.
	 */
	void removeListener(INavigatorContentServiceListener aListener);

	/**
	 * The root content providers are calculated once in
	 * {@link #findRootContentProviders(Object) } and reset by this method. The
	 * attached viewer is also refreshed as a result of this method.
	 * 
	 */
	void update();

	/**
	 * Release any acquired resources and instantiated content extensions.
	 * 
	 */
	void dispose();

	/**
	 * @param anElement
	 *            An element from the tree (any element contributed to the
	 *            tree).
	 * @return A set of {@link INavigatorContentDescriptor}s that are
	 *         <i>visible</i> and <i>active</i> for this content service and
	 *         enabled for the given element.
	 */
	Set findEnabledContentDescriptors(Object anElement);

	/**
	 * 
	 * @return An INavigatorFilterService that can provide information to a
	 *         viewer about what filters are 'visible' and 'active'.
	 */
	INavigatorFilterService getFilterService();

	/**
	 * By default, a {@link CommonViewer} uses the sorter service to sort
	 * elements in the tree. Clients do not need to provide their own
	 * {@link ViewerSorter} unless they wish to override this functionality.
	 * 
	 * @return An INavigatorSorterService that can provide {@link ViewerSorter}
	 *         based on the context of the parent.
	 */
	INavigatorSorterService getSorterService();

}
