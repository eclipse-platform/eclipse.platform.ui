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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.navigator.internal.NavigatorContentService;
import org.eclipse.ui.navigator.internal.NavigatorPlugin;
import org.eclipse.ui.navigator.internal.actions.CommonActionDescriptorManager;
import org.eclipse.ui.navigator.internal.actions.CommonActionProviderDescriptor;
import org.eclipse.ui.navigator.internal.extensions.InsertionPoint;
import org.eclipse.ui.navigator.internal.extensions.SkeletonActionProvider;

/**
 * <p>
 * Provides context menu items and {@link IActionBars} contributions for a
 * particular abstract viewer. The interface matches that of {@link ActionGroup}
 * and may be used in the same manner. Clients must call
 * {@link NavigatorActionService#prepareMenuForPlatformContributions(MenuManager, ISelectionProvider, boolean)}
 * when using this class to allow object or viewer contributions. The
 * <b>org.eclipse.ui.navigator.viewer/viewer/popupMenu</b> element may override
 * whether platform contributions are allowed to the menu with its
 * 'allowsPlatformContributions' attribute. "Platform Contributions" are menu
 * items that are added through the <b>org.eclipse.ui.popupMenus</b> extension
 * point.
 * </p>
 * <p>
 * A {@link CommonActionProvider} has opportunities to contribute to the context
 * menu and {@link org.eclipse.ui.IActionBars} whenever the selection in the
 * viewer changes. Action Providers are selected based on the enablement
 * expressions of their associated content extension or their own enablement
 * expression if it is declared as a top-level &lt;actionProvider /&gt; element
 * (of the <b>org.eclipse.ui.navigator.navigatorContent</b> extension point).
 * See the schema documentation of <b>org.eclipse.ui.navigator.navigatorContent</b>
 * for more information on how to specify an Action Provider.
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
			new Separator(ICommonMenuConstants.GROUP_PROPERTIES) };

	private final ICommonViewerSite commonViewerSite;

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

	private boolean disposed = false;

	/**
	 * @param aCommonViewerSite
	 *            A site that provides information about the context for
	 *            extensions.
	 * @param aStructuredViewer
	 *            The associated StructuredViewer. Used to initialize
	 *            extensions. <b>May NOT be null.</b>
	 * @param aContentService
	 *            The associated INavigatorContentService (for extensions that
	 *            coordinate behavior with content extensions -- either nested
	 *            or top-level action providers). <b>May NOT be null.</b>
	 */
	public NavigatorActionService(ICommonViewerSite aCommonViewerSite,
			StructuredViewer aStructuredViewer,
			INavigatorContentService aContentService) {
		super();
		Assert.isNotNull(aCommonViewerSite);
		Assert.isNotNull(aStructuredViewer);
		Assert.isNotNull(aContentService);

		commonViewerSite = aCommonViewerSite;
		contentService = (NavigatorContentService) aContentService;
		structuredViewer = aStructuredViewer;
		viewerDescriptor = contentService.getViewerDescriptor();

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
		Assert.isTrue(!disposed);

		if(commonViewerSite instanceof ICommonViewerWorkbenchSite) {
			/*
			 * Hooks into the Eclipse framework for Object contributions, and View
			 * contributions.
			 */
			if (force
					|| viewerDescriptor.allowsPlatformContributionsToContextMenu())
				((ICommonViewerWorkbenchSite)commonViewerSite).registerContextMenu(contentService
						.getViewerDescriptor().getPopupMenuId(), menu,
						aSelectionProvider);
		}
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
		Assert.isTrue(!disposed);

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

		CommonActionProviderDescriptor[] providerDescriptors = CommonActionDescriptorManager
				.getInstance().findRelevantActionDescriptors(contentService,
						getContext());
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
		Assert.isTrue(!disposed);

		theActionBars.clearGlobalActionHandlers();

		CommonActionProviderDescriptor[] providerDescriptors = CommonActionDescriptorManager
				.getInstance().findRelevantActionDescriptors(contentService,
						getContext());
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
		disposed = false;
	}

	/**
	 * Use the given memento to restore the state of each Action Provider as it
	 * is initialized.
	 * 
	 * @param aMemento
	 *            The memento retrieved from the dialog settings
	 */
	public void restoreState(IMemento aMemento) {
		Assert.isTrue(!disposed);
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
		Assert.isTrue(!disposed);

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

	private void initialize(String id, CommonActionProvider anActionProvider) {
		if (anActionProvider != null
				&& anActionProvider != SkeletonActionProvider.INSTANCE) {
			CommonActionProviderConfig configuration = new CommonActionProviderConfig(
					id, commonViewerSite, contentService, structuredViewer);
			anActionProvider.init(configuration);
			anActionProvider.restoreState(memento);
			anActionProvider.setContext(new ActionContext(
					StructuredSelection.EMPTY));
			if(commonViewerSite instanceof ICommonViewerWorkbenchSite)
				anActionProvider.fillActionBars(((ICommonViewerWorkbenchSite)commonViewerSite).getActionBars());
		}

	}
}
