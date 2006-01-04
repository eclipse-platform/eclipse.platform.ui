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
package org.eclipse.ui.navigator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.navigator.internal.NavigatorContentService;
import org.eclipse.ui.navigator.internal.NavigatorPlugin;
import org.eclipse.ui.navigator.internal.Utilities;
import org.eclipse.ui.navigator.internal.actions.CommonActionProviderDescriptor;
import org.eclipse.ui.navigator.internal.extensions.InsertionPoint;
import org.eclipse.ui.navigator.internal.extensions.RegistryReader;
import org.eclipse.ui.navigator.internal.extensions.SkeletonActionProvider;

/**
 * <p>
 * This service manages {@link CommonActionProvider}s.
 * </p>
 * <p>
 * See the documentation of the <b>org.eclipse.ui.navigator.navigatorContent</b>
 * extension point and {@link CommonActionProvider} for more information on
 * declaring {@link CommonActionProvider}s.
 * </p>
 * </p>
 * <p>
 * An {@link CommonActionProvider} has opportunities to contribute to the
 * context menu and {@link org.eclipse.ui.IActionBars} whenever the selection in
 * the viewer changes. Action Providers are selected based on the enablement
 * expressions of their associated content extension or their own enablement
 * expression if it is declared as a top-level actionProvider element (of the
 * <b>org.eclipse.ui.navigator</b> extension point). See the schema
 * documentation of <b>org.eclipse.ui.navigator.navigatorContent</b> for more
 * information on how to specify an Action Provider.
 * </p>
 * <p>
 * Clients that wish to expose the menu to object or viewer contributions are
 * required to call
 * {@link #prepareMenuForPlatformContributions(MenuManager, ISelectionProvider, boolean)}.
 * The menu will only be prepared for platform contributions if the
 * <b>allowsPlatformContributions</b> attribute of popupMenu is not set to
 * false. By default, the attribute is true.
 * </p>
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
public final class NavigatorActionService extends ActionGroup implements
		IMementoAware {

	private static final CommonActionProviderDescriptor[] NO_DESCRIPTORS = new CommonActionProviderDescriptor[0];

	private static final IContributionItem[] DEFAULT_GROUPS = new IContributionItem[] {
			new Separator(ICommonMenuConstants.GROUP_NEW),
			new GroupMarker(ICommonMenuConstants.GROUP_GOTO),
			new Separator(ICommonMenuConstants.GROUP_OPEN),
			new Separator(ICommonMenuConstants.GROUP_OPEN_WITH),
			new GroupMarker(ICommonMenuConstants.GROUP_SHOW),
			new GroupMarker(ICommonMenuConstants.GROUP_REORGANIZE),
			new GroupMarker(ICommonMenuConstants.GROUP_PORT),
			new Separator(ICommonMenuConstants.GROUP_GENERATE),
			new Separator(ICommonMenuConstants.GROUP_SEARCH),
			new Separator(ICommonMenuConstants.GROUP_BUILD),
			new Separator(ICommonMenuConstants.GROUP_ADDITIONS),
			new Separator(ICommonMenuConstants.GROUP_VIEWER_SETUP),
			new Separator(ICommonMenuConstants.GROUP_PROPERTIES) };

	private final IViewPart viewPart;

	private final StructuredViewer structuredViewer;

	private final NavigatorContentService contentService;

	private final INavigatorViewerDescriptor viewerDescriptor;

	private final Set actionProviderDescriptors = new HashSet();

	/*
	 * Map of CommonActionProviderDescriptors to CommonActionProviders
	 */
	private final Map actionProviderInstances = new HashMap();

	private IMemento memento;

	private IContributionItem[] menuGroups;

	/*
	 * Indicate a READY state. That is, init() has been called, but dispose()
	 * has not.
	 */
	private static final int STATE_READY = 0;

	/* Indicate a DISPOSED state. That is, dispose() has been called. */
	private static final int STATE_DISPOSED = 1;

	private int state = STATE_READY;

	/**
	 * 
	 * @param aViewPart
	 *            The associated IViewPart (for the IActionBars). <b>May NOT be
	 *            null.</b>
	 * @param aStructuredViewer
	 *            The associated StructuredViewer. Used to initialize
	 *            extensions. <b>May NOT be null.</b>
	 * @param aContentService
	 *            The associated INavigatorContentService (for extensions that
	 *            coordinate behavior with content extensions -- either nested
	 *            or top-level action providers). <b>May NOT be null.</b>
	 */
	public NavigatorActionService(IViewPart aViewPart,
			StructuredViewer aStructuredViewer,
			INavigatorContentService aContentService) {
		super();
		Assert.isNotNull(aViewPart);
		Assert.isNotNull(aStructuredViewer);
		Assert.isNotNull(aContentService);

		viewPart = aViewPart;
		contentService = (NavigatorContentService) aContentService;
		structuredViewer = aStructuredViewer;
		viewerDescriptor = contentService.getViewerDescriptor();

		init();
	}

	private void init() {
		try {
			new CommonActionRegistry().readRegistry();
		} catch (RuntimeException re) {
			NavigatorPlugin
					.logError(
							0,
							"Could not initialize NavigatorActionService for " + contentService.getViewerId(), re); //$NON-NLS-1$
		}

		state = STATE_READY;
	}

	/**
	 * Prepares the menu for object contributions, if the option is set in the
	 * extension. The option is controlled by the &lgt;popupMenu /&gt; element's
	 * 'allowPlatformContributions' attribute. Clients may choose to ignore this
	 * setting by supplying a value of <b>true</b> for the <code>force</code>
	 * attribute.
	 * 
	 * @param menu
	 *            The context menu of the IViewPart
	 * @param aSelectionProvider
	 *            The selection provider that will supplement actions with a
	 *            valid, current selection.
	 * @param force
	 *            A value of 'true' forces the menu to be registered for
	 *            object/view contributions. Otherwise, the option from the
	 *            extension point will be respected. See
	 *            <b>org.eclipse.ui.navigator.viewer/viewer</b> for more
	 *            information.
	 */
	public void prepareMenuForPlatformContributions(MenuManager menu,
			ISelectionProvider aSelectionProvider, boolean force) {

		/*
		 * Hooks into the Eclipse framework for Object contributions, and View
		 * contributions.
		 */
		if (force
				|| viewerDescriptor.allowsPlatformContributionsToContextMenu())
			viewPart.getViewSite().registerContextMenu(
					contentService.getViewerDescriptor().getPopupMenuId(),
					menu, aSelectionProvider);

	}

	/**
	 * Requests that the service invoke extensions to fill the given menu with
	 * Action Providers that are interested in elements from the given
	 * selection.
	 * 
	 * <p>
	 * Object contributions (see <b>org.eclipes.ui.popupMenus</b>) may also
	 * respected by this method if <code>toRespectObjectContributions</code>
	 * is true.
	 * </p>
	 * 
	 * @param aMenu
	 *            The menu being presented to the user.
	 * @param aStructuredSelection
	 *            The current selection from the viewer.
	 * @see ActionGroup#fillContextMenu(IMenuManager)
	 */
	public void fillContextMenu(IMenuManager aMenu) {
		complainIfDisposed();

		if (menuGroups == null)
			createMenuGroups();

		for (int i = 0; i < menuGroups.length; i++)
			aMenu.add(menuGroups[i]);

		addCommonActionProviderMenu(aMenu);

	}

	private void createMenuGroups() {
		InsertionPoint[] customPoints = viewerDescriptor
				.getCustomInsertionPoints();

		if (customPoints == null)
			menuGroups = DEFAULT_GROUPS;
		else {
			menuGroups = new IContributionItem[customPoints.length];
			for (int i = 0; i < customPoints.length; i++) {
				if (customPoints[i].isSeparator())
					menuGroups[i] = new Separator(customPoints[i].getName());
				else
					menuGroups[i] = new GroupMarker(customPoints[i].getName());
			}
		}
	}

	/**
	 * @param aMenu
	 */
	private void addCommonActionProviderMenu(IMenuManager aMenu) {

		CommonActionProviderDescriptor[] providerDescriptors = findRelevantActionDescriptors(getContext());
		if (providerDescriptors.length > 0) {
			CommonActionProvider provider = null;
			for (int i = 0; i < providerDescriptors.length; i++) {
				try {
					provider = getActionProviderInstance(providerDescriptors[i]);
					provider.setContext(getContext());
					provider.fillContextMenu(aMenu);
				} catch (RuntimeException e) {
					NavigatorPlugin.logError(0, e.getMessage(), e);
				}
			}
		}
	}

	private boolean isVisible(CommonActionProviderDescriptor descriptor) {
		if (descriptor.isNested()) {
			return Utilities.isActive(viewerDescriptor, descriptor.getId())
					&& Utilities
							.isVisible(viewerDescriptor, descriptor.getId());
		}
		return viewerDescriptor.isVisibleActionExtension(descriptor.getId());
	}

	/**
	 * Request that the service invoke extensions to fill the given IActionBars
	 * with retargetable actions or view menu contributions from Action
	 * Providers that are interested in the given selection.
	 * 
	 * @param theActionBars
	 *            The action bars in use by the current view site.
	 * @param aStructuredSelection
	 *            The current selection from the viewer.
	 * @see ActionGroup#fillActionBars(IActionBars)
	 */
	public void fillActionBars(IActionBars theActionBars) {
		complainIfDisposed();

		theActionBars.clearGlobalActionHandlers();

		CommonActionProviderDescriptor[] providerDescriptors = findRelevantActionDescriptors(getContext());
		if (providerDescriptors.length > 0) {
			CommonActionProvider provider = null;
			for (int i = 0; i < providerDescriptors.length; i++) {
				try {
					provider = getActionProviderInstance(providerDescriptors[i]);
					provider.setContext(getContext());
					provider.fillActionBars(theActionBars);

				} catch (RuntimeException e) {
					NavigatorPlugin.logError(0, e.getMessage(), e);
				}
			}
		}
		theActionBars.updateActionBars();
		theActionBars.getMenuManager().update();
	}

	/**
	 * Dispose of any state or resources held by the service.
	 * 
	 * @see ActionGroup#dispose()
	 */
	public void dispose() {
		if (state == STATE_DISPOSED)
			return;
		synchronized (actionProviderInstances) {
			for (Iterator iter = actionProviderInstances.values().iterator(); iter
					.hasNext();) {
				CommonActionProvider element = (CommonActionProvider) iter
						.next();
				element.dispose();
			}
			actionProviderInstances.clear();
		}
		actionProviderDescriptors.clear();
		state = STATE_DISPOSED;
	}

	/**
	 * Use the given memento to restore the state of each Action Provider as it
	 * is initialized.
	 * 
	 * @param aMemento
	 *            The memento retrieved from the dialog settings
	 */
	public void restoreState(IMemento aMemento) {
		memento = aMemento;

		synchronized (actionProviderInstances) {
			for (Iterator actionProviderIterator = actionProviderInstances
					.values().iterator(); actionProviderIterator.hasNext();) {
				final CommonActionProvider provider = (CommonActionProvider) actionProviderIterator
						.next();
				ISafeRunnable runnable = new ISafeRunnable() {
					public void run() throws Exception {
						provider.restoreState(memento);
					}

					public void handleException(Throwable exception) {
						NavigatorPlugin
								.logError(
										0,
										"Could not restore state for action provider " + provider.getClass(), exception); //$NON-NLS-1$

					}
				};
				Platform.run(runnable);

			}
		}
	}

	/**
	 * Request that Action Providers save any state that they find interesting.
	 * 
	 * @param aMemento
	 *            The memento retrieved from the dialog settings
	 */
	public void saveState(IMemento aMemento) {
		memento = aMemento;
		CommonActionProvider provider = null;
		synchronized (actionProviderInstances) {
			for (Iterator actionProviderIterator = actionProviderInstances
					.values().iterator(); actionProviderIterator.hasNext();) {
				provider = (CommonActionProvider) actionProviderIterator.next();
				provider.saveState(memento);
			}
		}
	}

	private CommonActionProviderDescriptor[] findRelevantActionDescriptors(
			ActionContext aContext) {
		IStructuredSelection structuredSelection = null;
		if (aContext.getSelection() instanceof IStructuredSelection)
			structuredSelection = (IStructuredSelection) aContext
					.getSelection();
		else
			structuredSelection = StructuredSelection.EMPTY;

		CommonActionProviderDescriptor actionDescriptor = null;
		List providers = new ArrayList();
		for (Iterator providerItr = actionProviderDescriptors.iterator(); providerItr
				.hasNext();) {
			actionDescriptor = (CommonActionProviderDescriptor) providerItr
					.next();
			if (isVisible(actionDescriptor)
					&& actionDescriptor.isEnabledFor(structuredSelection))
				providers.add(actionDescriptor);
		}
		if (providers.size() > 0)
			return (CommonActionProviderDescriptor[]) providers
					.toArray(new CommonActionProviderDescriptor[providers
							.size()]);
		return NO_DESCRIPTORS;
	}

	private CommonActionProvider getActionProviderInstance(
			CommonActionProviderDescriptor aProviderDescriptor) {
		CommonActionProvider provider = (CommonActionProvider) actionProviderInstances
				.get(aProviderDescriptor);
		if (provider != null)
			return provider;
		synchronized (actionProviderInstances) {
			provider = (CommonActionProvider) actionProviderInstances
					.get(aProviderDescriptor);
			if (provider == null) {
				provider = aProviderDescriptor.createActionProvider();
				if (provider != null) {
					initialize(aProviderDescriptor.getId(), provider);
					actionProviderInstances.put(aProviderDescriptor, provider);
				} else
					actionProviderInstances.put(aProviderDescriptor,
							(provider = SkeletonActionProvider.INSTANCE));
			}
		}
		return provider;
	}

	private void addActionDescriptor(CommonActionProviderDescriptor aDescriptor) {
		actionProviderDescriptors.add(aDescriptor);
	}

	private void complainIfDisposed() {
		if (state == STATE_DISPOSED)
			throw new IllegalStateException(
					"INavigatorActionService has already been disposed!"); //$NON-NLS-1$
	}

	private class CommonActionRegistry extends RegistryReader {

		private static final String TAG_ACTION_PROVIDER = "actionProvider"; //$NON-NLS-1$

		private static final String TAG_NAVIGATOR_CONTENT = "navigatorContent"; //$NON-NLS-1$

		private static final String TAG_ENABLEMENT = "enablement"; //$NON-NLS-1$

		private static final String TAG_TRIGGER_POINTS = "triggerPoints"; //$NON-NLS-1$

		private static final String ATT_ID = "id"; //$NON-NLS-1$

		protected CommonActionRegistry() {
			super(NavigatorPlugin.PLUGIN_ID, TAG_NAVIGATOR_CONTENT);
		}

		protected boolean readElement(IConfigurationElement anElement) {
			if (TAG_ACTION_PROVIDER.equals(anElement.getName())) {
				addActionDescriptor(new CommonActionProviderDescriptor(
						anElement));
				return true;
			} else if (TAG_NAVIGATOR_CONTENT.equals(anElement.getName())) {
				IConfigurationElement[] actionProviders = anElement
						.getChildren(TAG_ACTION_PROVIDER);
				if (actionProviders.length == 0)
					return true;
				IConfigurationElement defaultEnablement = null;
				IConfigurationElement[] enablement = anElement
						.getChildren(TAG_ENABLEMENT);
				if (enablement.length == 0)
					enablement = anElement.getChildren(TAG_TRIGGER_POINTS);
				if (enablement.length == 1)
					defaultEnablement = enablement[0];
				for (int i = 0; i < actionProviders.length; i++)
					addActionDescriptor(new CommonActionProviderDescriptor(
							actionProviders[i], defaultEnablement, anElement
									.getAttribute(ATT_ID), true));
				return true;
			}
			return false;
		}
	}

	private void initialize(String id, CommonActionProvider anActionProvider) {
		if (anActionProvider != null
				&& anActionProvider != SkeletonActionProvider.INSTANCE) {
			CommonActionProviderConfig configuration = new CommonActionProviderConfig(
					id, viewPart, contentService, structuredViewer);
			anActionProvider.init(configuration);
			anActionProvider.restoreState(memento);
			anActionProvider.setContext(new ActionContext(
					StructuredSelection.EMPTY));
			anActionProvider.fillActionBars(viewPart.getViewSite()
					.getActionBars());
		}

	}
}
