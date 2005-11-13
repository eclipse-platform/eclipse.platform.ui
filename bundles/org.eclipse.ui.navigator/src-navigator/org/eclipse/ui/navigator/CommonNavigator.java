/***************************************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/
package org.eclipse.ui.navigator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.navigator.internal.CommonNavigatorActionGroup;
import org.eclipse.ui.navigator.internal.CommonSorter;
import org.eclipse.ui.navigator.internal.NavigatorContentService;
import org.eclipse.ui.navigator.internal.extensions.NavigatorViewerDescriptor;
import org.eclipse.ui.navigator.internal.filters.CommonViewerFilter;
import org.eclipse.ui.navigator.internal.filters.ExtensionFilterRegistryManager;
import org.eclipse.ui.part.ISetSelectionTarget;
import org.eclipse.ui.part.ViewPart;

/**
 * <p>
 * Provides the hook for the Common Navigator framework into the Eclipse workbench. It also serves
 * as the backbone for the rest of the framework, including the following components:
 * <ul>
 * <li>
 * <p>
 * {@link org.eclipse.ui.navigator.CommonViewer}: The UI component that renders the
 * extensible tree. Also creates and manages the lifecylce of the Navigator Content Service
 * (described below).
 * </p>
 * </li>
 * <li>
 * <p>
 * {@link org.eclipse.ui.navigator.CommonNavigatorManager}: Handles auxillary
 * functions, such as updating the status bar, populating popup menus, and managing the Navigator
 * Action Service (described below). Not expected to be needed by clients.
 * </p>
 * </li>
 * <li>
 * <p>
 * {@link org.eclipse.ui.navigator.internal.NavigatorActionService}: Manages instances of
 * {@link org.eclipse.wst.common.navigator.internal.views.actions.ICommonActionProvider}&nbsp;provided
 * by individual extensions and content extensions.
 * </p>
 * </li>
 * <li>
 * <p>
 * {@link org.eclipse.ui.navigator.internal.NavigatorContentService}: Manages instances of
 * Navigator Content Extensions. Instances are created as needed, and disposed of upon the disposal
 * of the Common Navigator.
 * </p>
 * </li>
 * </ul>
 * <p>
 * Clients are not expected to subclass CommonNavigator. Clients that which to define their own
 * custom extensible navigator view need to specify an instance of the <b>org.eclipse.ui.views </b>
 * extension point:
 * 
 * <pre> 
 *        &lt;extension
 *        		point=&quot;org.eclipse.ui.views&quot;&gt;
 *        	&lt;view
 *        		name=&quot;My Custom View&quot;
 *        		icon=&quot;relative/path/to/icon.gif&quot;
 *        		category=&quot;org.acme.mycategory&quot;
 *        		class=&quot;org.eclipse.ui.navigator.CommonNavigator&quot;
 *        		id=&quot;org.acme.MyCustomNavigatorID&quot;&gt;
 *        	&lt;/view&gt;
 *        &lt;/extension&gt; 
 *  
 * </pre>
 * 
 * </p>
 * <p>
 * In the event that a consumer of the Common Navigator does need to change the actual behavior,
 * methods are provided to override specific pieces of functionality. Each of these methods begin
 * with <i>create </i> and explain what modular component they are responsible for creating. Each of
 * these pieces may take the instance of the Common Viewer (
 * {@link org.eclipse.ui.navigator.CommonViewer}) and have depedencies to that object,
 * but the instance of the Common Viewer should never assume more than the standard Eclipse
 * interfaces when working with these components.
 * </p>
 * <p>
 * Clients that wish to extend the view menu provided via the org.eclipse.ui.popupMenus extension
 * may specify the {@link POPUP_MENU_ID}&nbsp;as their target menu id.
 * 
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p> 
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * @since 3.2
 */
public class CommonNavigator extends ViewPart implements ISetSelectionTarget {

	/**
	 * <p>
	 * Provides a constant for the pop-up menu id for extensions that wish to contribute to the view
	 * menu. The defualt value specified is "CommonNavigatorPopupMenu". The
	 * <b>org.eclipse.common.navigator.views.navigatorViewer </b> may override the value to be used.
	 * </p>
	 */
	public static final String POPUP_MENU_ID = NavigatorViewerDescriptor.DEFAULT_POPUP_MENU_ID;

	/**
	 * <p>
	 * Used to track changes to the {@link #isLinkingEnabled}&nbsp;property.
	 * </p>
	 */
	public static final int IS_LINKING_ENABLED_PROPERTY = 1;

	private CommonViewer commonViewer;
	private CommonNavigatorManager commonManager;
	private ActionGroup commonActionGroup;

	private IMemento memento;
	private boolean isLinkingEnabled = false;
	private String LINKING_ENABLED = "CommonNavigator.LINKING_ENABLED"; //$NON-NLS-1$ 

	/**
	 * 
	 */
	public CommonNavigator() {
		super();
	}

	/**
	 * <p>
	 * Create the CommonViewer part control and setup the default providers as necessary.
	 * </p>
	 * 
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite aParent) {

		commonViewer = createCommonViewer(aParent);
		commonViewer.addFilter(createCommonFilter(commonViewer));
		commonViewer.setSorter(createCommonSorter(commonViewer));
 

		/* make sure input is set after sorters and filters to avoid unnecessary refreshes */
		commonViewer.setInput(getInitialInput());
		commonViewer.getControl().addDisposeListener(createDisposeListener());

		getSite().setSelectionProvider(commonViewer);


		updateTitle();

		/*
		 * Create the CommonNavigatorManager last because information about the state of the
		 * CommonNavigator is required for the initialization of the CommonNavigatorManager
		 */
		commonManager = createCommonManager();
		if (memento != null) {
			commonManager.restoreState(memento);
			commonViewer.getNavigatorContentService().restoreState(memento);
		}

		commonActionGroup = createCommonActionGroup();
		commonActionGroup.fillActionBars(getViewSite().getActionBars());

	}

	/**
	 * <p>
	 * Note: This method is for internal use only. Clients should not call this method.
	 * </p>
	 * <p>
	 * This method will be invoked when the DisposeListener is notified of the disposal of the
	 * Eclipse view part.
	 * </p>
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	public void dispose() {
		if (commonManager != null)
			commonManager.dispose();
		super.dispose();
	}

	/**
	 * <p>
	 * Note: This method is for internal use only. Clients should not call this method.
	 * </p>
	 * 
	 * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
	 */
	public void init(IViewSite aSite, IMemento aMemento) throws PartInitException {
		super.init(aSite, aMemento);
		memento = aMemento;
		if (memento != null) {
			Integer linkingEnabledInteger = memento.getInteger(LINKING_ENABLED);
			setLinkingEnabled(((linkingEnabledInteger != null) ? linkingEnabledInteger.intValue() == 1 : false));
		}

	}

	/**
	 * 
	 * <p>
	 * Note: This method is for internal use only. Clients should not call this method.
	 * </p>
	 * 
	 * @see org.eclipse.ui.part.ViewPart#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento aMemento) {
		aMemento.putInteger(LINKING_ENABLED, (isLinkingEnabled) ? 1 : 0);
		super.saveState(aMemento);
		commonManager.saveState(aMemento);
		commonViewer.getNavigatorContentService().saveState(aMemento);
	}

	/**
	 * <p>
	 * Force the workbench to focus on the Common Navigator tree.
	 * </p>
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	public void setFocus() {
		if (commonViewer != null)
			commonViewer.getTree().setFocus();
	}

	/**
	 * <p>
	 * Set the selection to the Common Navigator tree, and expand nodes if necessary. Use caution
	 * when invoking this method as it can cause Navigator Content Extensions to load, thus causing
	 * plugin activation.
	 * </p>
	 * 
	 * @see org.eclipse.ui.part.ISetSelectionTarget#selectReveal(org.eclipse.jface.viewers.ISelection)
	 */
	public void selectReveal(ISelection selection) {
		if (commonViewer != null)
			commonViewer.setSelection(selection, true);
	}

	/**
	 * <p>
	 * Linking is handled by
	 * {@link org.eclipse.wst.common.navigator.internal.views.actions.LinkEditorAction}, which
	 * listens for changes to the {@link CommonNavigator#IS_LINKING_ENABLED_PROPERTY}. Custom
	 * implementations that wish to override this functionality, need to override the action used by
	 * CommonNavigatorActionGroup and listen for changes to the above property.
	 * 
	 * @param toEnableLinking
	 *            True enables linking the current selection with open editors
	 */
	public final void setLinkingEnabled(boolean toEnableLinking) {
		isLinkingEnabled = toEnableLinking;
		firePropertyChange(IS_LINKING_ENABLED_PROPERTY);
	}

	/**
	 * @return Whether linking the current selection with open editors is enabled.
	 */
	public final boolean isLinkingEnabled() {
		return isLinkingEnabled;
	}

	/**
	 * <p>
	 * Provides access to the commonViewer used by the current CommonNavigator. The field will not
	 * be valid until after {@link #init(IViewSite, IMemento)}&nbsp;has been called by the Workbench.
	 * </p>
	 * 
	 * @see CommonNavigator#createCommonViewer(Composite)
	 * @return The (already created) instance of Common Viewer.
	 */
	public CommonViewer getCommonViewer() {
		return commonViewer;
	}

	/**
	 * @return The Navigator Content Service which populates this instance of Common Navigator
	 */
	public NavigatorContentService getNavigatorContentService() {
		return getCommonViewer().getNavigatorContentService();
	}
	
	public Object getAdapter(Class adapter) {
		if(adapter == CommonViewer.class)
			return getCommonViewer();
		else if(adapter == NavigatorContentService.class)
			return getCommonViewer().getNavigatorContentService();
		return super.getAdapter(adapter);
	}
	

	/**
	 * @return The Navigator Content Service which populates this instance of Common Navigator
	 */
	public INavigatorActionService getNavigatorActionService() {
		return commonManager.getNavigatorActionService();
	}

	/**
	 * <p>
	 * Constructs and returns an instance of {@link CommonViewer}. The ID of the Eclipse view part
	 * will be used to create the viewer. The ID is important as some extensions indicate they
	 * should only be used with a particular viewer ID.
	 * <p>
	 * 
	 * @param aParent
	 *            A composite parent to contain the Common Viewer
	 * @return An initialized instance of CommonViewer
	 */
	protected CommonViewer createCommonViewer(Composite aParent) {
		CommonViewer aViewer = new CommonViewer(getViewSite().getId(), aParent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		initListeners(aViewer);
		aViewer.getNavigatorContentService().restoreState(memento);
		return aViewer;
	}

	/**
	 * <p>
	 * Adds the listeners to the Common Viewer.
	 * </p>
	 * 
	 * @param viewer
	 *            The viewer
	 * @since 2.0
	 */
	protected void initListeners(TreeViewer viewer) {

		viewer.addDoubleClickListener(new IDoubleClickListener() {
			
			public void doubleClick(DoubleClickEvent event) {
				try {
					handleDoubleClick(event);
				} catch(RuntimeException re) {
					re.printStackTrace();
				}
			}
		});
	}

	/**
	 * <p>
	 * Note: This method is for internal use only. Clients should not call this method.
	 * </p>
	 * 
	 * @param anEvent
	 *            Supplied by the DoubleClick listener.
	 */
	protected void handleDoubleClick(DoubleClickEvent anEvent) {
		
		IStructuredSelection selection = (IStructuredSelection) anEvent.getSelection();
		Object element = selection.getFirstElement();

		TreeViewer viewer = getCommonViewer();
		if (viewer.isExpandable(element)) {
			viewer.setExpandedState(element, !viewer.getExpandedState(element));
		}
	}

	/**
	 * <p>
	 * The Common Navigator Manager handles the setup of the Common Navigator Menu, manages updates
	 * to the ActionBars from
	 * {@link org.eclipse.wst.common.navigator.internal.views.actions.ICommonActionProvider}&nbsp;
	 * extensions as the user's selection changes, and also updates the status bar based on the
	 * current selection.
	 * 
	 * @return
	 */
	protected CommonNavigatorManager createCommonManager() {
		return new CommonNavigatorManager(this);
	}

	/**
	 * <p>
	 * The ActionGroup is used to populate the ActionBars of Common Navigator View Part, and the
	 * returned implementation will have an opportunity to fill the ActionBars of the view as soon
	 * as it is created. ({@link ActionGroup#fillActionBars(org.eclipse.ui.IActionBars)}.
	 * </p>
	 * <p>
	 * The default implementation returns an instance of {@link CommonNavigatorActionGroup}&nbsp;which
	 * will add the following actions:
	 * <ul>
	 * <li>
	 * <p>
	 * Link with editor support (
	 * {@link org.eclipse.wst.common.navigator.internal.views.actions.LinkEditorAction}). Allows the
	 * user to toggling linking the current selection with the active editors.
	 * </p>
	 * <li>
	 * <p>
	 * Collapse all (
	 * {@link org.eclipse.wst.common.navigator.internal.views.actions.CollapseAllAction}). Collapses
	 * all expanded nodes.
	 * </p>
	 * <li>
	 * <p>
	 * Select Filters (
	 * {@link org.eclipse.wst.common.navigator.internal.views.filters.SelectFiltersAction}).
	 * Provides access to the "Select Filters" dialog that allows users to enable/disable filters
	 * and also the Content Extension activations.
	 * </p>
	 * </ul>
	 * 
	 * @return The Action Group to be associated with the Common Navigator View Part.
	 */
	protected ActionGroup createCommonActionGroup() {
		return new CommonNavigatorActionGroup(this, commonViewer);
	}

	/**
	 * <p>
	 * The default implementation hooks into the extensible navigator's framework for extensions to
	 * provide filters. Custom implementations will probably require some changes to
	 * {@link org.eclipse.wst.common.navigator.internal.views.filters.SelectFiltersAction}&nbsp;as
	 * well.
	 * </p>
	 * 
	 * @see CommonViewerFilter
	 * @see ExtensionFilterRegistryManager
	 * @return The ViewerFilter to provide the desired extensibility for Filters
	 */
	protected ViewerFilter createCommonFilter(CommonViewer aViewer) {
		return new CommonViewerFilter(aViewer);
	}



	/**
	 * <p>
	 * The following method creates a basic sorter for the M3 release. This functionality will
	 * change substantially for the M4 release.
	 * </p>
	 * 
	 * @return The ViewerSorter to sort the contents of the Common Viewer
	 */
	protected ViewerSorter createCommonSorter(CommonViewer aViewer) {
		return new CommonSorter(aViewer.getNavigatorContentService());
	}

	/**
	 * @return A listener to track the disposal of the Eclipse view part in order to dispose of the
	 *         framework state.
	 */
	protected DisposeListener createDisposeListener() {
		return new DisposeListener() {
			/**
			 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
			 */
			public void widgetDisposed(DisposeEvent e) {
				dispose();
			}
		};
	}

	/**
	 * @return The initial input for the viewer. Defaults to getSite().getPage().getInput()
	 */
	protected IAdaptable getInitialInput() {
		return getSite().getPage().getInput();
	}


	/**
	 * <p>
	 * Updates the title text and title tool tip. Called whenever the input of the viewer changes.
	 * </p>
	 */
	protected void updateTitle() {

		if (commonViewer == null)
			return;

		Object input = commonViewer.getInput();
		String viewName = getConfigurationElement().getAttribute("name"); //$NON-NLS-1$ 
		// IWorkingSet workingSet = workingSetFilter.getWorkingSet();

		if (input == null) {
			setPartName(viewName);
			setTitleToolTip(""); //$NON-NLS-1$ 
		} else {
			ILabelProvider labelProvider = (ILabelProvider) commonViewer.getLabelProvider();
			String inputToolTip = getFrameToolTipText(input);

			setPartName(labelProvider.getText(input));
			setTitleToolTip(inputToolTip);
		}
	}

	/**
	 * <p>
	 * Returns the tool tip text for the given element. Used as the tool tip text for the current
	 * frame, and for the view title tooltip.
	 * </p>
	 */
	protected String getFrameToolTipText(Object anElement) {
		if (commonViewer != null)
			return ((ILabelProvider) commonViewer.getLabelProvider()).getText(anElement);
		return ""; //$NON-NLS-1$
	}

}