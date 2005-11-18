/***************************************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/
package org.eclipse.ui.navigator.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.navigator.ICommonActionProvider;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.navigator.INavigatorActionService;
import org.eclipse.ui.navigator.internal.actions.CommonActionProviderDescriptor;
import org.eclipse.ui.navigator.internal.extensions.NavigatorContentExtension;
import org.eclipse.ui.navigator.internal.extensions.RegistryReader;
import org.eclipse.ui.navigator.internal.extensions.SkeletonActionProvider;

/**
 * <p>
 * The following class is experimental until fully documented.
 * </p>
 */
public class NavigatorActionService implements INavigatorActionService {
	

	private static final CommonActionProviderDescriptor[] NO_DESCRIPTORS= new CommonActionProviderDescriptor[0];

	private static final IContributionItem NEW_GROUP= new Separator(ICommonMenuConstants.GROUP_NEW);
	private static final IContributionItem GOTO_GROUP= new GroupMarker(ICommonMenuConstants.GROUP_GOTO);
	private static final IContributionItem OPEN_GROUP= new Separator(ICommonMenuConstants.GROUP_OPEN);
	private static final IContributionItem SHOW_GROUP= new GroupMarker(ICommonMenuConstants.GROUP_SHOW);
	private static final IContributionItem EDIT_GROUP= new Separator(ICommonMenuConstants.COMMON_MENU_EDIT_ACTIONS);
	private static final IContributionItem REORGANIZE_GROUP= new GroupMarker(ICommonMenuConstants.GROUP_REORGANIZE);
	private static final IContributionItem PORT_GROUP= new GroupMarker(ICommonMenuConstants.GROUP_PORT);
	private static final IContributionItem GENERATE_GROUP= new Separator(ICommonMenuConstants.GROUP_GENERATE);
	private static final IContributionItem SEARCH_GROUP= new Separator(ICommonMenuConstants.GROUP_SEARCH);
	private static final IContributionItem BUILD_GROUP= new Separator(ICommonMenuConstants.GROUP_BUILD);
	private static final IContributionItem ADDITIONS_GROUP= new Separator(ICommonMenuConstants.GROUP_ADDITIONS);
	private static final IContributionItem VIEWER_GROUP= new Separator(ICommonMenuConstants.GROUP_VIEWER_SETUP);
	private static final IContributionItem PROPERTIES_GROUP= new Separator(ICommonMenuConstants.GROUP_PROPERTIES);

	private final IViewPart viewPart;
	private final StructuredViewer structuredViewer;
	private final NavigatorContentService contentService;
	private final Set actionProviderDescriptors= new HashSet();
	private final Map actionProviderInstances= new HashMap();
	private boolean isApplyingActionsFromContentExtensions;

	private IMemento memento;

	private boolean isDisposed= false;
	private boolean isInitialized= false;

	private MenuManager contextMenu;

	/**
	 * 
	 */
	public NavigatorActionService(IViewPart aViewPart, StructuredViewer aStructuredViewer, NavigatorContentService aContentService) {
		this(aViewPart, aStructuredViewer, aContentService, true);
	}

	/**
	 * 
	 */
	public NavigatorActionService(IViewPart aViewPart, StructuredViewer aStructuredViewer, NavigatorContentService aContentService, boolean toApplyActionsFromContentExtensions) {
		super();
		viewPart= aViewPart;
		contentService= aContentService;
		structuredViewer= aStructuredViewer;
		isApplyingActionsFromContentExtensions= toApplyActionsFromContentExtensions;

		init();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.internal.INavigatorActionService#refresh()
	 */
	public void refresh() {
		IStructuredSelection sSelection= (IStructuredSelection) structuredViewer.getSelection();
		fillActionBars(viewPart.getViewSite().getActionBars(), sSelection);
		fillContextMenu(contextMenu, sSelection);

	}

	private void init() {
		if (isInitialized)
			return;
		try {
			new CommonActionRegistry().readRegistry();

			Collection contentDescriptors= contentService.getExtensions();
			if (contentDescriptors.size() > 0) { 
				NavigatorContentExtension descriptorInstance= null;
				for (Iterator iter= contentDescriptors.iterator(); iter.hasNext();) {
					descriptorInstance= (NavigatorContentExtension) iter.next();
					descriptorInstance.getActionProvider(this);
// moved initialization to getActionProvider(this); 					
//					initialize(descriptorInstance.getId(), provider);
				}
			}

		} catch (RuntimeException re) {
			NavigatorPlugin.log(CommonNavigatorMessages.NavigatorActionService_0 + contentService.getViewerId() + CommonNavigatorMessages.NavigatorActionService_1 + re.getMessage());
			re.printStackTrace();
		}
		isInitialized= true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.internal.INavigatorActionService#fillContextMenu(org.eclipse.jface.action.IMenuManager, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void fillContextMenu(IMenuManager aMenu, IStructuredSelection aStructuredSelection) {
		complainIfDisposed();

		aMenu.add(NEW_GROUP);
		aMenu.add(GOTO_GROUP);
		aMenu.add(OPEN_GROUP);
		aMenu.add(SHOW_GROUP);
		aMenu.add(EDIT_GROUP);
		aMenu.add(REORGANIZE_GROUP);
		aMenu.add(PORT_GROUP);
		aMenu.add(GENERATE_GROUP);
		aMenu.add(SEARCH_GROUP);
		aMenu.add(BUILD_GROUP);
		aMenu.add(ADDITIONS_GROUP);
		aMenu.add(VIEWER_GROUP);
		aMenu.add(PROPERTIES_GROUP);

		if (aStructuredSelection == null || aStructuredSelection.isEmpty())
			aStructuredSelection= new StructuredSelection(structuredViewer.getInput());
		ActionContext context= new ActionContext(aStructuredSelection);
		addContentDescriptorMenu(aMenu, aStructuredSelection, context);
		addCommonActionProviderMenu(aMenu, aStructuredSelection, context);

		aMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/**
	 * @param aMenu
	 * @param aStructuredSelection
	 * @param aContext
	 */
	private void addCommonActionProviderMenu(IMenuManager aMenu, IStructuredSelection aStructuredSelection, ActionContext aContext) {
		CommonActionProviderDescriptor[] providerDescriptors= findRelevantActionDescriptors(aStructuredSelection);
		if (providerDescriptors.length > 0) {
			ICommonActionProvider provider= null;
			for (int i= 0; i < providerDescriptors.length; i++) {
				try {
					provider= getActionProviderInstance(providerDescriptors[i]);
					provider.setActionContext(aContext);
					provider.fillContextMenu(aMenu);
				} catch (RuntimeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * @param aMenu
	 * @param aStructuredSelection
	 * @param aContext
	 */
	private void addContentDescriptorMenu(IMenuManager aMenu, IStructuredSelection aStructuredSelection, ActionContext aContext) {
		if (isApplyingActionsFromContentExtensions) {
			NavigatorContentExtension[] contentDescriptorInstances= contentService.findRelevantContentExtensions(aStructuredSelection);
			NavigatorContentExtension contentDescriptorInstance= null;
			ICommonActionProvider provider= null;
			for (int x= 0; x < contentDescriptorInstances.length; ++x) {
				try {
					contentDescriptorInstance= contentDescriptorInstances[x];
					provider= contentDescriptorInstance.getActionProvider(this);
					if (provider != SkeletonActionProvider.INSTANCE) {
						provider.setActionContext(aContext);
						provider.fillContextMenu(aMenu);
					}
				} catch (RuntimeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.internal.INavigatorActionService#fillActionBars(org.eclipse.ui.IActionBars, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void fillActionBars(IActionBars theActionBars, IStructuredSelection aStructuredSelection) {
		complainIfDisposed();

		boolean actionBarsChanged= false;
		ActionContext context= new ActionContext(aStructuredSelection);
		if (isApplyingActionsFromContentExtensions) {
			NavigatorContentExtension[] contentDescriptorInstances= contentService.findRelevantContentExtensions(aStructuredSelection);
			NavigatorContentExtension contentDescriptorInstance= null;
			ICommonActionProvider provider= null;
			for (int x= 0; x < contentDescriptorInstances.length; ++x) {
				try {
					contentDescriptorInstance= contentDescriptorInstances[x];
					provider= contentDescriptorInstance.getActionProvider(this);
					if (provider != SkeletonActionProvider.INSTANCE) {
						provider.setActionContext(context);
						actionBarsChanged|= provider.fillActionBars(theActionBars);
					}
				} catch (RuntimeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		CommonActionProviderDescriptor[] providerDescriptors= findRelevantActionDescriptors(aStructuredSelection);
		if (providerDescriptors.length > 0) {
			ICommonActionProvider provider= null;
			for (int i= 0; i < providerDescriptors.length; i++) {
				try {
					provider= getActionProviderInstance(providerDescriptors[i]);
					provider.setActionContext(context);
					actionBarsChanged|= provider.fillActionBars(theActionBars);
				} catch (RuntimeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if (actionBarsChanged) {
			theActionBars.updateActionBars();
			theActionBars.getMenuManager().update();
		}
	}

	private CommonActionProviderDescriptor[] findRelevantActionDescriptors(IStructuredSelection aStructuredSelection) {
		CommonActionProviderDescriptor actionDescriptor= null;
		List providers= new ArrayList();
		for (Iterator providerItr= actionProviderDescriptors.iterator(); providerItr.hasNext();) {
			actionDescriptor= (CommonActionProviderDescriptor) providerItr.next();
			if (actionDescriptor.isEnabledFor(aStructuredSelection))
				providers.add(actionDescriptor);
		}
		if (providers.size() > 0)
			return (CommonActionProviderDescriptor[]) providers.toArray(new CommonActionProviderDescriptor[providers.size()]);
		return NO_DESCRIPTORS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.internal.INavigatorActionService#dispose()
	 */
	public void dispose() {
		if (isDisposed)
			return;
		synchronized (actionProviderInstances) {
			for (Iterator iter= actionProviderInstances.values().iterator(); iter.hasNext();) {
				ICommonActionProvider element= (ICommonActionProvider) iter.next();
				element.dispose();
			}
			actionProviderInstances.clear();
		}
		actionProviderDescriptors.clear();
		isDisposed= true;
	}

	private ICommonActionProvider getActionProviderInstance(CommonActionProviderDescriptor aProviderDescriptor) {
		ICommonActionProvider provider= (ICommonActionProvider) actionProviderInstances.get(aProviderDescriptor);
		if (provider != null)
			return provider;
		synchronized (actionProviderInstances) {
			provider= (ICommonActionProvider) actionProviderInstances.get(aProviderDescriptor);
			if (provider == null) {
				provider= aProviderDescriptor.createActionProvider();
				if (provider != null) {
					initialize(null, provider);
					actionProviderInstances.put(aProviderDescriptor, provider);
				} else
					actionProviderInstances.put(aProviderDescriptor, (provider= SkeletonActionProvider.INSTANCE));
			}
		}
		return provider;
	}

	private void addActionDescriptor(CommonActionProviderDescriptor aDescriptor) {
		actionProviderDescriptors.add(aDescriptor);
	}

	private void complainIfDisposed() {
		if (isDisposed)
			throw new IllegalStateException(CommonNavigatorMessages.NavigatorActionService_2);
	}

	private class CommonActionRegistry extends RegistryReader {

		private static final String ACTION_PROVIDER= "actionProvider"; //$NON-NLS-1$

		public CommonActionRegistry() {
			super(NavigatorPlugin.PLUGIN_ID, ACTION_PROVIDER);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.wst.common.navigator.internal.views.extensions.RegistryReader#readElement(org.eclipse.core.runtime.IConfigurationElement)
		 */
		protected boolean readElement(IConfigurationElement anElement) {
			if (ACTION_PROVIDER.equals(anElement.getName())) {
				addActionDescriptor(new CommonActionProviderDescriptor(anElement));
			}
			return true;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.internal.INavigatorActionService#restoreState(org.eclipse.ui.IMemento)
	 */
	public void restoreState(IMemento aMemento) {
		memento= aMemento;

		synchronized (actionProviderInstances) {
			for (Iterator actionProviderIterator= actionProviderInstances.values().iterator(); actionProviderIterator.hasNext();) {
				final ICommonActionProvider provider= (ICommonActionProvider) actionProviderIterator.next();
				ISafeRunnable runnable= new ISafeRunnable() {
					public void run() throws Exception {
						provider.restoreState(memento);

					}

					public void handleException(Throwable exception) {
						NavigatorPlugin.logError(0, CommonNavigatorMessages.NavigatorActionService_3 + provider.getClass() + CommonNavigatorMessages.NavigatorActionService_4, exception);

					}
				};
				Platform.run(runnable);

			}
		}

		Collection contentDescriptors= contentService.getExtensions();
		if (contentDescriptors.size() > 0) {
			NavigatorContentExtension element= null;
			for (Iterator iter= contentDescriptors.iterator(); iter.hasNext();) {
				element= (NavigatorContentExtension) iter.next();
				final ICommonActionProvider provider= element.getActionProvider(this);
				ISafeRunnable runnable= new ISafeRunnable() {
					public void run() throws Exception {
						provider.restoreState(memento);

					}

					public void handleException(Throwable exception) {
						NavigatorPlugin.logError(0, CommonNavigatorMessages.NavigatorActionService_5 + provider.getClass() + CommonNavigatorMessages.NavigatorActionService_6, exception);

					}
				};
				Platform.run(runnable);
			}
		}
	}


	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.internal.INavigatorActionService#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento aMemento) {
		memento= aMemento;
		ICommonActionProvider provider= null;
		synchronized (actionProviderInstances) {
			for (Iterator actionProviderIterator= actionProviderInstances.values().iterator(); actionProviderIterator.hasNext();) {
				provider= (ICommonActionProvider) actionProviderIterator.next();
				provider.saveState(memento);
			}
		}

		Collection contentDescriptors= contentService.getExtensions();
		if (contentDescriptors.size() > 0) {
			NavigatorContentExtension element= null;
			for (Iterator iter= contentDescriptors.iterator(); iter.hasNext();) {
				element= (NavigatorContentExtension) iter.next();
				provider= element.getActionProvider(this);
				provider.saveState(memento);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.internal.INavigatorActionService#initialize(java.lang.String, org.eclipse.ui.navigator.ICommonActionProvider)
	 */
	public void initialize(String anExtensionId, ICommonActionProvider anActionProvider) {
		if (anActionProvider != null && anActionProvider != SkeletonActionProvider.INSTANCE) {
			anActionProvider.init(anExtensionId, viewPart, contentService, structuredViewer);
			anActionProvider.restoreState(memento);
			anActionProvider.setActionContext(new ActionContext(StructuredSelection.EMPTY));
			anActionProvider.fillActionBars(viewPart.getViewSite().getActionBars());
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.internal.INavigatorActionService#setUpdateMenu(org.eclipse.jface.action.MenuManager)
	 */
	public void setUpdateMenu(MenuManager menuMgr) {
		contextMenu= menuMgr;

	}
}
