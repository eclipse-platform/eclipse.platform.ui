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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.navigator.internal.CommonNavigatorActionGroup;
import org.eclipse.ui.navigator.internal.CommonNavigatorManager;
import org.eclipse.ui.navigator.internal.NavigatorContentService;
import org.eclipse.ui.navigator.internal.actions.CollapseAllAction;
import org.eclipse.ui.navigator.internal.actions.LinkEditorAction;
import org.eclipse.ui.navigator.internal.filters.SelectFiltersAction;
import org.eclipse.ui.part.ISetSelectionTarget;
import org.eclipse.ui.part.ViewPart;

/**
 * <p>
 * This class provides the IViewPart for the Common Navigator 
 * framework in the Eclipse workbench. This class also serves
 * as the backbone for navigational viewers. The following types
 * are used by this class to render the Common Navigator:
 * <ul>
 * <li>
 * <p>
 * {@link org.eclipse.ui.navigator.CommonViewer}: The viewer
 * that renders the extensible tree. Creates and manages the 
 * lifecylce of the Navigator Content Service (described below).
 * </p>
 * </li> 
 * <li>
 * <p>
 * {@link org.eclipse.ui.navigator.NavigatorActionService}: 
 * Manages instances of {@link org.eclipse.ui.navigator.CommonActionProvider}s
 * provided by individual extensions and content extensions.
 * </p>
 * </li>
 * <li>
 * <p>
 * {@link org.eclipse.ui.navigator.INavigatorContentService}: 
 * Manages instances of Navigator Content Extensions. Instances are 
 * created as needed, and disposed of upon the disposal of the 
 * Navigator Content Service.
 * </p>
 * </li>
 * </ul>
 * <p>
 * Clients are not expected to subclass CommonNavigator. Clients 
 * that wish to define their own custom extensible navigator 
 * view need to specify an instance of the <b>org.eclipse.ui.views</b>
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
 * Clients that wish to extend the view menu provided via the <b>org.eclipse.ui.popupMenu</b>s extension
 * may specify the the popupMenuId specified by <b>org.eclipse.ui.navigator.viewer</b> of their
 * target viewer as their target menu id.
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
		//commonViewer.addFilter(new DuplicateContentFilter(commonViewer));
		
		INavigatorFilterService filterService = commonViewer.getNavigatorContentService().getFilterService();
		ViewerFilter[] visibleFilters = filterService.getVisibleFilters(true);
		for(int i=0; i<visibleFilters.length; i++)
			commonViewer.addFilter(visibleFilters[i]);
		 
		/* no sorter is necessary as the commonViewer defaults to the commonSorter automagically. */

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
	 * {@link LinkEditorAction}, which
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
	public INavigatorContentService getNavigatorContentService() {
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
	public NavigatorActionService getNavigatorActionService() {
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
	 * {@link ICommonActionProvider}&nbsp;
	 * extensions as the user's selection changes, and also updates the status bar based on the
	 * current selection.
	 * 
	 * @return The Common Navigator Manager class which handles menu population and ActionBars
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
	 * {@link LinkEditorAction}). Allows the
	 * user to toggling linking the current selection with the active editors.
	 * </p>
	 * <li>
	 * <p>
	 * Collapse all (
	 * {@link CollapseAllAction}). Collapses
	 * all expanded nodes.
	 * </p>
	 * <li>
	 * <p>
	 * Select Filters (
	 * {@link SelectFiltersAction}).
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
			String inputToolTip = getFrameToolTipText(input);
			
			setPartName(viewName);
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
