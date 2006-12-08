/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems - Mikhail Khodjaiants - Registers View (Bug 53640)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.variables;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DelegatingModelPresentation;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.LazyModelPresentation;
import org.eclipse.debug.internal.ui.VariablesViewModelPresentation;
import org.eclipse.debug.internal.ui.actions.CollapseAllAction;
import org.eclipse.debug.internal.ui.actions.ConfigureColumnsAction;
import org.eclipse.debug.internal.ui.actions.variables.ChangeVariableValueAction;
import org.eclipse.debug.internal.ui.actions.variables.ShowTypesAction;
import org.eclipse.debug.internal.ui.actions.variables.ToggleDetailPaneAction;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.internal.ui.viewers.model.VirtualFindAction;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelChangedListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDeltaVisitor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdateListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.debug.internal.ui.views.DebugModelPresentationContext;
import org.eclipse.debug.internal.ui.views.IDebugExceptionHandler;
import org.eclipse.debug.internal.ui.views.variables.details.AvailableDetailPanesAction;
import org.eclipse.debug.internal.ui.views.variables.details.DetailPaneProxy;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.commands.IStatusMonitor;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.texteditor.IUpdate;


/**
 * This view shows variables and their values for a particular stack frame
 */
public class VariablesView extends AbstractDebugView implements IDebugContextListener,
	IPropertyChangeListener, IDebugExceptionHandler,
	IPerspectiveListener, IModelChangedListener,
	IViewerUpdateListener {
	
	/**
	 * The model presentation used as the label provider for the tree viewer,
	 * and also as the detail information provider for the detail pane.
	 */
	private VariablesViewModelPresentation fModelPresentation;
	
	/**
	 * The UI construct that provides a sliding sash between the variables tree
	 * and the detail pane.
	 */
	private SashForm fSashForm;
	
	/**
	 * The detail pane that displays detailed information about the current selection
	 * @since 3.3
	 */
	private DetailPaneProxy fDetailPane;
	
	/**
	 * Stores whether the tree viewer was the last control to have focus in the
	 * view. Used to give focus to the correct component if the user leaves the view.    
	 * @since 3.3
	 */
	private boolean fTreeHasFocus;
	
	/**
	 * Various listeners used to update the enabled state of actions and also to
	 * populate the detail pane.
	 */
	private ISelectionChangedListener fTreeSelectionChangedListener;
	
	/**
	 * These are used to initialize and persist the position of the sash that
	 * separates the tree viewer from the detail pane.
	 */
	private static final int[] DEFAULT_SASH_WEIGHTS = {13, 6};
	private int[] fLastSashWeights;
	private boolean fToggledDetailOnce;
	private String fCurrentDetailPaneOrientation = IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_HIDDEN;
	private ToggleDetailPaneAction[] fToggleDetailPaneActions;
	private ConfigureColumnsAction fConfigureColumnsAction;
    
    protected String PREF_STATE_MEMENTO = "pref_state_memento."; //$NON-NLS-1$

	public static final String LOGICAL_STRUCTURE_TYPE_PREFIX = "VAR_LS_"; //$NON-NLS-1$
	
	/**
	 * Presentation context property.
	 * TODO: make API
	 * @since 3.3
	 */
	public static final String PRESENTATION_SHOW_LOGICAL_STRUCTURES = "PRESENTATION_SHOW_LOGICAL_STRUCTURES"; //$NON-NLS-1$
	
	/**
	 * the preference name for the view part of the sash form
	 * @since 3.2 
	 */
	protected static final String SASH_VIEW_PART = DebugUIPlugin.getUniqueIdentifier() + ".SASH_VIEW_PART"; //$NON-NLS-1$
	/**
	 * the preference name for the details part of the sash form
	 * @since 3.2
	 */
	protected static final String SASH_DETAILS_PART = DebugUIPlugin.getUniqueIdentifier() + ".SASH_DETAILS_PART"; //$NON-NLS-1$
	
	/**
	 * Key for "Find..." action.
	 */
	protected static final String VARIABLES_FIND_ELEMENT_ACTION = FIND_ACTION + ".Variables"; //$NON-NLS-1$

	/**
	 * Key for "Select All" action.
	 */
	protected static final String VARIABLES_SELECT_ALL_ACTION = SELECT_ALL_ACTION + ".Variables"; //$NON-NLS-1$
	
	/**
	 * Key for "Select All" action.
	 */
	protected static final String VARIABLES_COPY_ACTION = COPY_ACTION + ".Variables"; //$NON-NLS-1$	
	
    /**
     * Visits deltas to determine if details should be displayed
     */
    class Visitor implements IModelDeltaVisitor {
        /**
         * Whether to trigger details display.
         * 
         * @since 3.3
         */
        private boolean fTriggerDetails = false;
		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.viewers.provisional.IModelDeltaVisitor#visit(org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta, int)
		 */
		public boolean visit(IModelDelta delta, int depth) {
			if ((delta.getFlags() & IModelDelta.CONTENT) > 0) {
				fTriggerDetails = true;
				return false;
			}
			return true;
		}
		
		public void reset() {
			fTriggerDetails = false;
		}
		
		public boolean isTriggerDetails() {
			return fTriggerDetails;
		}
    	
    }
    /**
     * Delta visitor
     */
    private Visitor fVisitor = new Visitor();
    
    /**
     * Job to update details in the UI thread.
     */
    private Job fTriggerDetailsJob = new UIJob("trigger details") { //$NON-NLS-1$
	
		public IStatus runInUIThread(IProgressMonitor monitor) {
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			} 
			populateDetailPane();
			return Status.OK_STATUS;
		}
	
	};
    
	/**
	 * Remove myself as a selection listener
	 * and preference change listener.
	 *
	 * @see IWorkbenchPart#dispose()
	 */
	public void dispose() {
		
		DebugUITools.getDebugContextManager().getContextService(getSite().getWorkbenchWindow()).removeDebugContextListener(this);
		getSite().getWorkbenchWindow().removePerspectiveListener(this);
		DebugUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
		JFaceResources.getFontRegistry().removeListener(this);
		TreeModelViewer viewer = getVariablesViewer();
		if (viewer != null) {
			viewer.removeModelChangedListener(this);
			viewer.removeViewerUpdateListener(this);
		}
		if (fDetailPane != null) fDetailPane.dispose();
		super.dispose();
	}

	/**
	 * Sets the input to the viewer
	 * @param context the object context
	 */
	protected void setViewerInput(Object context) {
		
		if (context == null) {
			clearDetails();
		}
		
		Object current = getViewer().getInput();
		
		if (current == null && context == null) {
			return;
		}

		if (current != null && current.equals(context)) {
			return;
		}
		
		showViewer();
		getViewer().setInput(context);
	}
	
	/**
	 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		String propertyName= event.getProperty();
		if (propertyName.equals(IDebugUIConstants.PREF_CHANGED_DEBUG_ELEMENT_COLOR) || 
				propertyName.equals(IInternalDebugUIConstants.PREF_CHANGED_VALUE_BACKGROUND)) {
			getViewer().refresh();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#createViewer(Composite)
	 */
	public Viewer createViewer(Composite parent) {
		
		fTriggerDetailsJob.setSystem(true);
		
		// create the sash form that will contain the tree viewer & text viewer
		fSashForm = new SashForm(parent, SWT.NONE);
		
		fModelPresentation = new VariablesViewModelPresentation();
		DebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);

		TreeModelViewer variablesViewer = createTreeViewer(fSashForm);
			
		fSashForm.setMaximizedControl(variablesViewer.getControl());
			
		Listener activateListener = new Listener() {
			public void handleEvent(Event event) {
				fTreeHasFocus = false;
			}
		};
		
		fDetailPane = new DetailPaneProxy(fSashForm,this.getSite(),activateListener);
		
		
		createOrientationActions(variablesViewer);
		IPreferenceStore prefStore = DebugUIPlugin.getDefault().getPreferenceStore();
		String orientation = prefStore.getString(getDetailPanePreferenceKey());
		for (int i = 0; i < fToggleDetailPaneActions.length; i++) {
			fToggleDetailPaneActions[i].setChecked(fToggleDetailPaneActions[i].getOrientation().equals(orientation));
		}
		setDetailPaneOrientation(orientation);
		IMemento memento = getMemento();
		if (memento != null) {
			variablesViewer.initState(memento);
		}
		
		variablesViewer.addModelChangedListener(this);
		variablesViewer.addViewerUpdateListener(this);
		
		return variablesViewer;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		PREF_STATE_MEMENTO = PREF_STATE_MEMENTO + site.getId();
        IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore();
        String string = store.getString(PREF_STATE_MEMENTO);
        if(string.length() > 0) {
        	ByteArrayInputStream bin = new ByteArrayInputStream(string.getBytes());
        	InputStreamReader reader = new InputStreamReader(bin);
        	try {
        		XMLMemento stateMemento = XMLMemento.createReadRoot(reader);
        		setMemento(stateMemento);
        	} catch (WorkbenchException e) {
        	} finally {
        		try {
        			reader.close();
        			bin.close();
        		} catch (IOException e){}
        	}
        }
        IMemento mem = getMemento();
        // check the weights to makes sure they are valid -- bug 154025
        setLastSashWeights(DEFAULT_SASH_WEIGHTS);
		if (mem != null) {
			Integer sw = mem.getInteger(SASH_VIEW_PART);
			if(sw != null) {
				int view = sw.intValue();
				sw = mem.getInteger(SASH_DETAILS_PART);
				if(sw != null) {
					int details = sw.intValue();
					if(view > -1 & details > -1) {
						setLastSashWeights(new int[] {view, details});
					}
				}
			}
		}
		site.getWorkbenchWindow().addPerspectiveListener(this);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.part.PageBookView#partDeactivated(org.eclipse.ui.IWorkbenchPart)
     */
    public void partDeactivated(IWorkbenchPart part) {
		String id = part.getSite().getId();
		if (id.equals(getSite().getId())) {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(bout);

			try {
				XMLMemento memento = XMLMemento.createWriteRoot("VariablesViewMemento"); //$NON-NLS-1$
				saveViewerState(memento);
				memento.save(writer);

				IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore();
				String xmlString = bout.toString();
				store.putValue(PREF_STATE_MEMENTO, xmlString);
			} catch (IOException e) {
			} finally {
				try {
					writer.close();
					bout.close();
				} catch (IOException e) {
				}
			}
		}
		super.partDeactivated(part);
	}

	/**
	 * Saves the current state of the viewer
	 * @param memento the memento to write the viewer state into
	 */
	public void saveViewerState(IMemento memento) {
		if (fSashForm != null && !fSashForm.isDisposed()) {
	        int[] weights = fSashForm.getWeights();
			memento.putInteger(SASH_VIEW_PART, weights[0]);
			memento.putInteger(SASH_DETAILS_PART, weights[1]);
		}
		getVariablesViewer().saveState(memento);
	}

	/**
	 * @return the pref key for the variables view details pane
	 */
	protected String getDetailPanePreferenceKey() {
		return IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_ORIENTATION;
	}
		
	/**
	 * Create and return the main tree viewer that displays variable.
	 */
	protected TreeModelViewer createTreeViewer(Composite parent) {
		
		int style = getViewerStyle();
		final TreeModelViewer variablesViewer = new TreeModelViewer(parent, style,
				new DebugModelPresentationContext(getPresentationContextId(), fModelPresentation));
		
		variablesViewer.getControl().addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				fTreeHasFocus = true;
				getViewSite().setSelectionProvider(variablesViewer);
				setAction(SELECT_ALL_ACTION, getAction(VARIABLES_SELECT_ALL_ACTION));
				setAction(COPY_ACTION, getAction(VARIABLES_COPY_ACTION));
				setAction(FIND_ACTION, getAction(VARIABLES_FIND_ELEMENT_ACTION));
				getViewSite().getActionBars().updateActionBars();
			}
			
			public void focusLost(FocusEvent e){
				getViewSite().setSelectionProvider(null);
				setAction(SELECT_ALL_ACTION, null);
				setAction(COPY_ACTION,null);
				setAction(FIND_ACTION, null);
				getViewSite().getActionBars().updateActionBars();
			}
		});
		variablesViewer.getPresentationContext().addPropertyChangeListener(
				new IPropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent event) {
						if (IPresentationContext.PROPERTY_COLUMNS.equals(event.getProperty())) {
							IAction action = getAction("ShowTypeNames"); //$NON-NLS-1$
							if (action != null) {
								action.setEnabled(event.getNewValue() == null);
							}
						}
					}
				});
		
		variablesViewer.addPostSelectionChangedListener(getTreeSelectionChangedListener());
		DebugUITools.getDebugContextManager().getContextService(getSite().getWorkbenchWindow()).addDebugContextListener(this);
		return variablesViewer;
	}

	/**
	 * Returns the presentation context id for this view.
	 * 
	 * @return context id
	 */
	protected String getPresentationContextId() {
		return IDebugUIConstants.ID_VARIABLE_VIEW;
	}
	
	/**
	 * Returns the style bits for the viewer.
	 * 
	 * @return SWT style
	 */
	protected int getViewerStyle() {
		return SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.VIRTUAL | SWT.FULL_SELECTION;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#getHelpContextId()
	 */
	protected String getHelpContextId() {
		return IDebugHelpContextIds.VARIABLE_VIEW;		
	}
	
	/**
	 * Set the orientation of the details pane so that is one of:
	 * - underneath the main tree view
	 * - to the right of the main tree view
	 * - not visible
	 */
	public void setDetailPaneOrientation(String orientation) {
		if (orientation.equals(fCurrentDetailPaneOrientation)) {
			return;
		}
		if (orientation.equals(IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_HIDDEN)) {
			hideDetailPane();
		} else {
			int vertOrHoriz = orientation.equals(IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_UNDERNEATH) ? SWT.VERTICAL : SWT.HORIZONTAL;
			fSashForm.setOrientation(vertOrHoriz);	
			if (IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_HIDDEN.equals(fCurrentDetailPaneOrientation)) {
				showDetailPane();	
			}
		}
		fCurrentDetailPaneOrientation  = orientation;
		DebugUIPlugin.getDefault().getPreferenceStore().setValue(getDetailPanePreferenceKey(), orientation);
	}
	
	/**
	 * Hides the details pane
	 */
	private void hideDetailPane() {
		if (fToggledDetailOnce) {
			setLastSashWeights(fSashForm.getWeights());
		}
		fSashForm.setMaximizedControl(getViewer().getControl());		
	}
	
	/**
	 * Shows the details pane 
	 */
	private void showDetailPane() {
		fSashForm.setMaximizedControl(null);
		fSashForm.setWeights(getLastSashWeights());
		populateDetailPane();
		revealTreeSelection();
		fToggledDetailOnce = true;
	}

	/**
	 * Make sure the currently selected item in the tree is visible.
	 */
	protected void revealTreeSelection() {
		StructuredViewer viewer = (StructuredViewer) getViewer();
		if (viewer != null) {
			ISelection selection = viewer.getSelection();
			if (selection instanceof IStructuredSelection) {
				Object selected = ((IStructuredSelection)selection).getFirstElement();
				if (selected != null) {
					viewer.reveal(selected);
				}
			}
		}
	}

	/**
	 * Return the relative weights that were in effect the last time both panes were
	 * visible in the sash form, or the default weights if:
	 * <ul>
	 * <li> both panes have not yet been made visible</li>
	 * <li> one of the values persisted before is an invalid value</li>
	 * </ul>
	 */
	protected int[] getLastSashWeights() {
		if (fLastSashWeights == null) {
			fLastSashWeights = DEFAULT_SASH_WEIGHTS;
		}
		//check the weights to makes sure they are valid -- bug 154025
		else if(fLastSashWeights[0] < 0 || fLastSashWeights[1] < 0) {
			fLastSashWeights = DEFAULT_SASH_WEIGHTS;
		}
		return fLastSashWeights;
	}
	
	/**
	 * Set the current relative weights of the controls in the sash form, so that
	 * the sash form can be reset to this layout at a later time.
	 */
	protected void setLastSashWeights(int[] weights) {
		fLastSashWeights = weights;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#createActions()
	 */
	protected void createActions() {
		IAction action = new ShowTypesAction(this);
		setAction("ShowTypeNames",action); //$NON-NLS-1$
				
		action = new ToggleLogicalStructureAction(this);
		setAction("ToggleContentProviders", action); //$NON-NLS-1$
		
		action = new CollapseAllAction((TreeViewer)getViewer());
		setAction("CollapseAll", action); //$NON-NLS-1$

		action = new ChangeVariableValueAction(this);
		action.setEnabled(false);
		setAction("ChangeVariableValue", action); //$NON-NLS-1$
		
		action= new VirtualFindAction(getVariablesViewer());
		setAction(VARIABLES_FIND_ELEMENT_ACTION, action);
	} 	
	
	/* (non-Javadoc)
	 * 
	 * Save the copy action so we can restore it on focus lost/gain
	 * 
	 * @see org.eclipse.debug.ui.AbstractDebugView#createContextMenu(org.eclipse.swt.widgets.Control)
	 */
	protected void createContextMenu(Control menuControl) {
		super.createContextMenu(menuControl);
		setAction(VARIABLES_COPY_ACTION, getAction(COPY_ACTION));
	}

	private void createOrientationActions(TreeModelViewer viewer) {
		IActionBars actionBars = getViewSite().getActionBars();
		IMenuManager viewMenu = actionBars.getMenuManager();
		
		fToggleDetailPaneActions = new ToggleDetailPaneAction[3];
		fToggleDetailPaneActions[0] = new ToggleDetailPaneAction(this, IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_UNDERNEATH, null);
		fToggleDetailPaneActions[1] = new ToggleDetailPaneAction(this, IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_RIGHT, null);
		fToggleDetailPaneActions[2] = new ToggleDetailPaneAction(this, IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_HIDDEN, getToggleActionLabel());
		viewMenu.add(new Separator());
		final MenuManager layoutSubMenu = new MenuManager(VariablesViewMessages.VariablesView_40);
		layoutSubMenu.setRemoveAllWhenShown(true);
		layoutSubMenu.add(fToggleDetailPaneActions[0]);
		layoutSubMenu.add(fToggleDetailPaneActions[1]);
		layoutSubMenu.add(fToggleDetailPaneActions[2]);
		viewMenu.add(layoutSubMenu);
		viewMenu.add(new Separator());
		
		fConfigureColumnsAction = new ConfigureColumnsAction(viewer);
		setAction("ToggleColmns", new ToggleShowColumnsAction(viewer)); //$NON-NLS-1$
		
		layoutSubMenu.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				layoutSubMenu.add(fToggleDetailPaneActions[0]);
				layoutSubMenu.add(fToggleDetailPaneActions[1]);
				layoutSubMenu.add(fToggleDetailPaneActions[2]);
				IAction action = getAction("ToggleColmns"); //$NON-NLS-1$
				((IUpdate)action).update();
				if (action.isEnabled()) {
					layoutSubMenu.add(action);
				}
				fConfigureColumnsAction.update();
				if (fConfigureColumnsAction.isEnabled()) {
					layoutSubMenu.add(fConfigureColumnsAction);
				}
			}
		});
	}
	
	protected String getToggleActionLabel() {
		return VariablesViewMessages.VariablesView_41; 
	}
	
	/**
	 * Configures the toolBar.
	 * 
	 * @param tbm The toolbar that will be configured
	 */
	protected void configureToolBar(IToolBarManager tbm) {
		tbm.add(new Separator(this.getClass().getName()));
		tbm.add(new Separator(IDebugUIConstants.RENDER_GROUP));
		tbm.add(getAction("ShowTypeNames")); //$NON-NLS-1$
		tbm.add(getAction("ToggleContentProviders")); //$NON-NLS-1$
		tbm.add(getAction("CollapseAll")); //$NON-NLS-1$
	}

   /**
	* Adds items to the tree viewer's context menu including any extension defined
	* actions.
	* 
	* @param menu The menu to add the item to.
	*/
	protected void fillContextMenu(IMenuManager menu) {

		menu.add(new Separator(IDebugUIConstants.EMPTY_VARIABLE_GROUP));
		menu.add(new Separator(IDebugUIConstants.VARIABLE_GROUP));
		menu.add(getAction(VARIABLES_FIND_ELEMENT_ACTION));
		menu.add(getAction("ChangeVariableValue")); //$NON-NLS-1$
		menu.add(new Separator());
		IAction action = new AvailableLogicalStructuresAction(this);
		if (action.isEnabled()) {
			menu.add(action);
		}
		action = new AvailableDetailPanesAction(this);
		if (isDetailPaneVisible() && action.isEnabled()) {
			menu.add(action);
		}
		menu.add(new Separator(IDebugUIConstants.EMPTY_RENDER_GROUP));
		menu.add(new Separator(IDebugUIConstants.EMPTY_NAVIGATION_GROUP));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
   /**
	 * Lazily instantiate and return a selection listener that populates the detail pane,
	 * but only if the detail is currently visible. 
	 */
    protected ISelectionChangedListener getTreeSelectionChangedListener() {
        if (fTreeSelectionChangedListener == null) {
            fTreeSelectionChangedListener = new ISelectionChangedListener() {
                public void selectionChanged(final SelectionChangedEvent event) {
                    if (event.getSelectionProvider().equals(getViewer())) {
                        clearStatusLine();	
                        // if the detail pane is not visible, don't waste time retrieving details
                        if (fSashForm.getMaximizedControl() == getViewer().getControl()) {
                            return;
                        }	
                        populateDetailPaneFromSelection((IStructuredSelection) event.getSelection());
                        treeSelectionChanged(event);
                    }
                }					
            };
        }
        return fTreeSelectionChangedListener;
    }
    
	/**
	 * Selection in the variable tree changed. Perform any updates.
	 * 
	 * @param event
	 */
	protected void treeSelectionChanged(SelectionChangedEvent event) {}
	
	/**
	 * Ask the variables tree for its current selection, and use this to populate
	 * the detail pane.
	 */
	public void populateDetailPane() {
		if (isDetailPaneVisible()) {
            Viewer viewer = getViewer();
            if (viewer != null) {
                IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
                populateDetailPaneFromSelection(selection);
            }
        }
    }
	
	/**
	 * Show the details associated with the first of the selected variables in the 
	 * detail pane.
	 */
	protected void populateDetailPaneFromSelection(IStructuredSelection selection) {
       	fDetailPane.display(selection);
	}

	/**
	 * Clears the detail pane
	 */
	private void clearDetails() {
       fDetailPane.display(null);      
	}
	
	protected IDebugModelPresentation getModelPresentation() {
		if (fModelPresentation == null) {
			fModelPresentation = new VariablesViewModelPresentation();
		}
		return fModelPresentation;
	}
	
	/**
	 * Returns the sash form
	 * @return the current sash form
	 */
	protected SashForm getSashForm() {
		return fSashForm;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#getAdapter(Class)
	 */
	public Object getAdapter(Class required) {
		if (IDebugModelPresentation.class.equals(required)) {
			return getModelPresentation();
		}
		else if (fDetailPane != null){
			Object adapter = fDetailPane.getAdapter(required);
			if (adapter != null) return adapter;
		}
		return super.getAdapter(required);
	}

	protected void updateAction(String actionId) {
		IAction action= getAction(actionId);
		if (action instanceof IUpdate) {
			((IUpdate) action).update();
		}
	}
	
	protected boolean isDetailPaneVisible() {
		return !fToggleDetailPaneActions[2].isChecked();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#getDefaultControl()
	 */
	protected Control getDefaultControl() {
		return fSashForm;
	}	
	
	/**
	 * @see IDebugExceptionHandler#handleException(DebugException)
	 */
	public void handleException(DebugException e) {
		showMessage(e.getMessage());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.contexts.provisional.IDebugContextListener#contextEvent(org.eclipse.debug.internal.ui.contexts.provisional.DebugContextEvent)
	 */
	public void debugContextChanged(DebugContextEvent event) {
		if ((event.getFlags() & DebugContextEvent.ACTIVATED) > 0) {
			contextActivated(event.getContext());
		}
	}

	protected void contextActivated(ISelection selection) {
		if (!isAvailable() || !isVisible()) {
			return;
		}
		
		if (selection instanceof IStructuredSelection) {
			setViewerInput(((IStructuredSelection)selection).getFirstElement());
		}
		showViewer();
		
		updateAction(VARIABLES_FIND_ELEMENT_ACTION);
		updateAction(FIND_ACTION);
	}
		
	/**
	 * Delegate to the <code>DOUBLE_CLICK_ACTION</code>,
	 * if any.
	 *  
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(DoubleClickEvent)
	 */
	public void doubleClick(DoubleClickEvent event) {
		IAction action = getAction(DOUBLE_CLICK_ACTION);
		if (action != null && action.isEnabled()) {
			action.run();
		}
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugView#getPresentation(String)
	 */
	public IDebugModelPresentation getPresentation(String id) {
		if (getViewer() instanceof StructuredViewer) {
			IDebugModelPresentation lp = getModelPresentation();
			if (lp instanceof DelegatingModelPresentation) {
				return ((DelegatingModelPresentation)lp).getPresentation(id);
			}
			if (lp instanceof LazyModelPresentation) {
				if (((LazyModelPresentation)lp).getDebugModelIdentifier().equals(id)) {
					return lp;
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDetailSite#getDetailViewerParent()
	 */
	public Composite getDetailViewerParent() {
		return fSashForm;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDetailSite#isMainViewerAvailable()
	 */
	public boolean isMainViewerAvailable() {
		return isAvailable();
	}
	
	protected IPresentationContext getPresentationContext() {
		return getVariablesViewer().getPresentationContext();
	}
	
	/** 
	 * Sets whether logical structures are being displayed
	 */
	public void setShowLogicalStructure(boolean flag) {
	    getPresentationContext().setProperty(PRESENTATION_SHOW_LOGICAL_STRUCTURES, Boolean.valueOf(flag));
	}	
	
	/** 
	 * Returns whether logical structures are being displayed 
	 */
	public boolean isShowLogicalStructure() {
		Boolean show = (Boolean) getPresentationContext().getProperty(PRESENTATION_SHOW_LOGICAL_STRUCTURES);
		return show != null && show.booleanValue();
	}	

	/**
	 * Returns the number of entries that should be displayed in each
	 * partition of an indexed collection.
	 * 
	 * @return the number of entries that should be displayed in each
	 * partition of an indexed collection
	 */
	protected int getArrayPartitionSize() {
		// TODO: this should be a view setting
		return 100;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#becomesHidden()
	 */
	protected void becomesHidden() {
		setViewerInput(null);
		super.becomesHidden();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#becomesVisible()
	 */
	protected void becomesVisible() {
		super.becomesVisible();
		ISelection selection = DebugUITools.getDebugContextManager().getContextService(getSite().getWorkbenchWindow()).getActiveContext();
		contextActivated(selection);
	}

	protected TreeModelViewer getVariablesViewer() {
		return (TreeModelViewer) getViewer();
	}
	
	protected void clearStatusLine() {
		IStatusLineManager manager = getViewSite().getActionBars().getStatusLineManager(); 
		manager.setErrorMessage(null);
		manager.setMessage(null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveListener#perspectiveActivated(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor)
	 */
	public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveListener#perspectiveChanged(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor, java.lang.String)
	 */
	public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
		if(changeId.equals(IWorkbenchPage.CHANGE_RESET)) {
			setLastSashWeights(DEFAULT_SASH_WEIGHTS);
			fSashForm.setWeights(DEFAULT_SASH_WEIGHTS);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.IModelChangedListener#modelChanged(org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta)
	 */
	public void modelChanged(IModelDelta delta, IModelProxy proxy) {
		fVisitor.reset();
		delta.accept(fVisitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.IViewerUpdateListener#updateComplete(org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousRequestMonitor)
	 */
	public void updateComplete(IStatusMonitor update) {
		IStatus status = update.getStatus();
		if (!update.isCanceled()) {
			if (status != null && status.getCode() != IStatus.OK) {
				showMessage(status.getMessage());
			} else {
				showViewer();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.IViewerUpdateListener#updateStarted(org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousRequestMonitor)
	 */
	public void updateStarted(IStatusMonitor update) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.IViewerUpdateListener#viewerUpdatesBegin()
	 */
	public synchronized void viewerUpdatesBegin() {
		fTriggerDetailsJob.cancel();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.IViewerUpdateListener#viewerUpdatesComplete()
	 */
	public synchronized void viewerUpdatesComplete() {
		if (fVisitor.isTriggerDetails()) {
			fTriggerDetailsJob.schedule();
		}
	}	
	
	/**
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		boolean success = false; 
		if (!fTreeHasFocus && fDetailPane != null){
			success = fDetailPane.setFocus();
		}
		// Unless the detail pane successfully set focus to a control, set focus to the variables tree
		if (!success && getViewer() != null){
			getViewer().getControl().setFocus();
		}
	}

	/**
	 * Returns the detail pane or null.
	 */
	public DetailPaneProxy getDetailPane() {
		return fDetailPane;
	}
	
	
}
