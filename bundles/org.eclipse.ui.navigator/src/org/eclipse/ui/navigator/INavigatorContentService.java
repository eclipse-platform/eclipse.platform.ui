/*******************************************************************************
 * Copyright (c) 2003, 2011 IBM Corporation and others.
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
import org.eclipse.ui.ISaveablesSource;

/**
 * 
 * Manages content extensions for extensible viewers and provides reusable
 * services for filters, sorting, the activation of content extensions, and DND.
 * The service can locate the appropriate providers (for contents or labels) for
 * an element and provide a ready-to-go {@link ITreeContentProvider} and
 * {@link ILabelProvider} for viewers that wish to take advantage of the
 * <b>org.eclipse.ui.navigator.navigatorContent</b> extensions defined for a
 * particular <i>viewerId</i>.
 * 
 * <p>
 * Clients can get the instance of this associated with the {@link CommonNavigator} using
 * {@link CommonNavigator#getNavigatorContentService()}.
 * </p>
 * 
 * <p>
 * Clients may contribute logical extensions using
 * <b>org.eclipse.ui.navigator.navigatorContent</b>. Each extension has three
 * states which determine whether the extension is used by the content service:
 * <ul>
 * <li><a name="visible"><i>visible</i>: If a content extension id matches a
 * <b>viewerContentBinding</b> for the <i>viewerId</i> of this content
 * service, then the extension is <i>visible</i>. Visible extensions may only
 * be configured through <b>viewerContentBinding</b>s. </li>
 * 
 * <li><a name="active"><i>active</i>: The active state may be set to a default using the
 * <i>activeByDefault</i> attribute of <b>navigatorContent</b>. Users may
 * toggle the <i>active</i> state through the "Available customizations"
 * dialog. Clients may also configure the active extensions using
 * {@link INavigatorActivationService#activateExtensions(String[], boolean)} or
 * {@link INavigatorActivationService#deactivateExtensions(String[], boolean)}
 * from the {@link #getActivationService() Activation Service} </li>
 * 
 * <li><a name="enabled"><i>enabled</i>: An extension is <i>enabled</i> for an element if the
 * extension contributed that element or if the element is described in the
 * <i>triggerPoints</i> element of the <b>navigatorContent</b> extension. The
 * findXXX() methods search for <i>enabled</i> extensions. </li>
 * </ul>
 * </p>
 * <p>
 * A new instance of the content service should be created for each viewer.
 * Clients should use {@link #createCommonContentProvider()} and
 * {@link #createCommonLabelProvider()} for the viewer. Each content service
 * tracks the viewer it is attached to. Clients may create the content service
 * with a viewer using ({@link NavigatorContentServiceFactory#createContentService(String)}).
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
 * 
 * @since 3.2
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
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
	 * 
	 * @return The description provider for this content service.
	 */
	IDescriptionProvider createCommonDescriptionProvider();

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
	 * See <a href="INavigatorContentService.html#active">above</a> for the
	 * definition of <i>active</i>.
	 * 
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
	 * See <a href="INavigatorContentService.html#visible">above</a> for the
	 * definition of <i>visible</i>.
	 * 
	 * @param anExtensionId
	 *            The unqiue identifier from a content extension.
	 * @return True if and only if the given extension id is <i>visible</i> to
	 *         this content service.
	 * @see INavigatorContentService For more information on what <i>visible</i>
	 *      means.
	 */
	boolean isVisible(String anExtensionId);

	/**
	 * Return the set of <i>visible</i> extension ids for this content service,
	 * which includes those that are bound through <b>viewerContentBinding</b>s
	 * and those that are bound through
	 * {@link #bindExtensions(String[], boolean)}.
	 * 
	 * @return The set of <i>visible</i> extension ids for this content service
	 */
	String[] getVisibleExtensionIds();

	/**
	 * Return the set of <i>visible</i> content descriptors for this content
	 * service, which includes those that are bound through
	 * <b>viewerContentBinding</b>s and those that are bound through
	 * {@link #bindExtensions(String[], boolean)}.
	 * 
	 * @return The set of <i>visible</i> content descriptors for this content
	 *         service
	 */
	INavigatorContentDescriptor[] getVisibleExtensions();

	/**
	 * Bind the set of given extensions to this content service. Programmatic
	 * bindings allow clients to make extensions <i>visible</i> to an instance
	 * of the content service by appending to the bindings declared through
	 * <b>org.eclipse.ui.navigator.viewer</b>. Programmatic bindings are not
	 * persisted and are not remembered or propagated to other instances of the
	 * INavigatorContentService in the same session. Programmatic bindings
	 * cannot be undone for a given instance of the INavigatorContentService and
	 * do not override declarative bindings.
	 * <p>
	 * Once a content extension has been bound to the INavigatorContentService,
	 * clients may use
	 * {@link INavigatorActivationService#activateExtensions(String[], boolean) }
	 * or
	 * {@link  INavigatorActivationService#deactivateExtensions(String[], boolean) }
	 * to control the <i>activation</i> state of the extension. See
	 * {@link INavigatorContentService} for more information on the difference
	 * between <i>visible</i> and <i>active</i>.
	 * </p>
	 * 
	 * @param extensionIds
	 *            The list of extensions to make visible.
	 * @param isRoot
	 *            whether the context provider should be a root content provider
	 * @return A list of all INavigatorContentDescriptors that correspond to the
	 *         given extensionIds.
	 */
	INavigatorContentDescriptor[] bindExtensions(String[] extensionIds,
			boolean isRoot);

	/**
	 * Restore the state associated with the memento.
	 * 
	 * @param aMemento
	 *            The memento for extensions to use when restoring previous
	 *            settings.
	 */
	void restoreState(IMemento aMemento);

	/**
	 * Persist any session-to-session state with the memento.
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
	 * The root content providers are recalculated by this method. The attached
	 * viewer is also refreshed as a result of this method.
	 * 
	 */
	void update();

	/**
	 * Release any acquired resources and instantiated content extensions.
	 * 
	 */
	void dispose();

	/**
	 * Search for extensions that declare the given element in their
	 * <b>triggerPoints</b> expression or that indicate they should be bound as
	 * a root extension.
	 * 
	 * @param anElement
	 *            The element to use in the query
	 * @return The set of {@link INavigatorContentExtension}s that are
	 *         <i>visible</i> and <i>active</i> for this content service and
	 *         either declared through a
	 *         <b>org.eclipse.ui.navigator.viewer/viewerContentBinding</b> to
	 *         be a root element or have a <b>triggerPoints</b> expression that
	 *         is <i>enabled</i> for the given element.
	 */
	Set findRootContentExtensions(Object anElement);

	/**
	 * Search for extensions that declare the given element in their
	 * <b>triggerPoints</b> expression.
	 * 
	 * @param anElement
	 *            The element to use in the query
	 * @return The set of {@link INavigatorContentExtension}s that are
	 *         <i>visible</i> and <i>active</i> for this content service and
	 *         have a <b>triggerPoints</b> expression that is <i>enabled</i>
	 *         for the given element.
	 */
	Set findContentExtensionsByTriggerPoint(Object anElement);

	/**
	 * Search for extensions that declare the given element in their
	 * <b>possibleChildren</b> expression.
	 * 
	 * @param anElement
	 *            The element to use in the query
	 * @return The set of {@link INavigatorContentExtension}s that are
	 *         <i>visible</i> and <i>active</i> for this content service and
	 *         have a <b>possibleChildren</b> expression that is <i>enabled</i>
	 *         for the given element.
	 */
	Set findContentExtensionsWithPossibleChild(Object anElement);

	/**
	 * The filter service can provide the available filters for the viewer, and
	 * manage which filters are <i>active</i>.
	 * 
	 * @return An {@link INavigatorFilterService} that can provide information
	 *         to a viewer about what filters are <i>visible</i> and <i>active</i>.
	 */
	INavigatorFilterService getFilterService();

	/**
	 * The sorter service provides the appropriate sorter based on the current
	 * items being sorted. By default, the CommonViewer uses
	 * {@link CommonViewerSorter} which delegates to this service. Clients do
	 * not need to provide their own {@link ViewerSorter} unless they wish to
	 * override this functionality.
	 * 
	 * @return An {@link INavigatorSorterService} that can provide
	 *         {@link ViewerSorter} based on the context of the parent.
	 */
	INavigatorSorterService getSorterService();

	/**
	 * The pipeline service calculates the appropriate viewer modification or
	 * refresh that should be applied for viewers that wish to take advantage of
	 * the model pipelining that some extensions use to massage or reshape
	 * contents in the viewer. Clients that use the {@link CommonViewer} do not
	 * need to be concerned with this service as the refreshes are automatically
	 * computed using this service.
	 * 
	 * 
	 * @return The {@link INavigatorPipelineService} which can determine the
	 *         correct updates to apply to a viewer.
	 */
	INavigatorPipelineService getPipelineService();

	/**
	 * The DND Service provides instances of {@link CommonDragAdapterAssistant}
	 * and {@link CommonDropAdapterAssistant} for this content service.
	 * 
	 * @return The {@link INavigatorDnDService} which can add additional
	 *         TransferTypes for the DragAdapter and setup the data correctly
	 *         for those extended Transfer Types.
	 */
	INavigatorDnDService getDnDService();

	/**
	 * The activation service is used to toggle whether certain extensions have
	 * the opportunity to contribute content and/or actions.
	 * 
	 * @return The {@link INavigatorActivationService} for this content service.
	 */
	INavigatorActivationService getActivationService();
	
	/**
	 * The saveable service helps implementing {@link ISaveablesSource}.
	 * 
	 * @return the {@link INavigatorSaveablesService} for this content service.
	 */
	INavigatorSaveablesService getSaveablesService();
	
	/** 
	 * Return the content extension for the given id. 
	 * 
	 * @param anExtensionId The id used to define the <b>org.eclipse.ui.navigator.navigatorContent/navigatorContent</b> extension.
	 * @return An instance of the content extension for the given extension id. May return <b>null</b> if the id is invalid.
	 */
	public INavigatorContentExtension getContentExtensionById(String anExtensionId);
	
	/** 
	 * Return the content extension for the given id. 
	 * 
	 * @param anExtensionId The id used to define the <b>org.eclipse.ui.navigator.navigatorContent/navigatorContent</b> extension.
	 * @return An instance of the content extension for the given extension id. May return <b>null</b> if the id is invalid.
	 * @since 3.3
	 */
	public INavigatorContentDescriptor getContentDescriptorById(String anExtensionId);



}
