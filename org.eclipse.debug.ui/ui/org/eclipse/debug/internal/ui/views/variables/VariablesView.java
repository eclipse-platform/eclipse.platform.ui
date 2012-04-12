/*******************************************************************************
 *  Copyright (c) 2000, 2012 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems - Mikhail Khodjaiants - Registers View (Bug 53640)
 *     Wind River - Pawel Piech - Drag/Drop to Expressions View (Bug 184057)
 * 	   Wind River - Pawel Piech - Busy status while updates in progress (Bug 206822)
 * 	   Wind River - Pawel Piech - NPE when closing the Variables view (Bug 213719)
 *     Wind River - Pawel Piech - Fix viewer input race condition (Bug 234908)
 *     Wind River - Anton Leherbauer - Fix selection provider (Bug 254442)
 *     Patrick Chuong (Texas Instruments) - Improve usability of the breakpoint view (Bug 238956)
 *     Patrick Chuong (Texas Instruments) and Pawel Piech (Wind River) - 
 *     		Allow multiple debug views and multiple debug context providers (Bug 327263)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.variables;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DelegatingModelPresentation;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.LazyModelPresentation;
import org.eclipse.debug.internal.ui.SWTFactory;
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
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewActionProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerInputRequestor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerInputUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdateListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ViewerInputService;
import org.eclipse.debug.internal.ui.views.DebugModelPresentationContext;
import org.eclipse.debug.internal.ui.views.IDebugExceptionHandler;
import org.eclipse.debug.internal.ui.views.variables.details.AvailableDetailPanesAction;
import org.eclipse.debug.internal.ui.views.variables.details.DetailPaneProxy;
import org.eclipse.debug.internal.ui.views.variables.details.IDetailPaneContainer2;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.handlers.CollapseAllHandler;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * This view shows variables and their values for a particular stack frame
 */
public class VariablesView extends AbstractDebugView implements IDebugContextListener,
	IPropertyChangeListener, IDebugExceptionHandler,
	IPerspectiveListener, IModelChangedListener,
		IViewerUpdateListener, IDetailPaneContainer2, ISaveablePart2 {
	
	private static final String COLLAPSE_ALL = "CollapseAll"; //$NON-NLS-1$

	/**
	 * Selection provider wrapping an exchangeable active selection provider.
	 * Sends out a selection changed event when the active selection provider changes.
	 * Forwards all selection changed events of the active selection provider.
	 */
	private static class SelectionProviderWrapper implements ISelectionProvider {
		private final ListenerList fListenerList = new ListenerList(ListenerList.IDENTITY);
		private final ISelectionChangedListener fListener = new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				fireSelectionChanged(event);
			}
		};
		private ISelectionProvider fActiveProvider;
		
		private SelectionProviderWrapper(ISelectionProvider provider) {
			setActiveProvider(provider);
		}

		private void setActiveProvider(ISelectionProvider provider) {
			if (fActiveProvider == provider || this == provider) {
				return;
			}
			if (fActiveProvider != null) {
				fActiveProvider.removeSelectionChangedListener(fListener);
			}
			if (provider != null) {
				provider.addSelectionChangedListener(fListener);
			}
			fActiveProvider = provider;
			fireSelectionChanged(new SelectionChangedEvent(this, getSelection()));
		}

		private void dispose() {
			fListenerList.clear();
			setActiveProvider(null);
		}

		private void fireSelectionChanged(SelectionChangedEvent event) {
			Object[] listeners = fListenerList.getListeners();
			for (int i = 0; i < listeners.length; i++) {
				ISelectionChangedListener listener = (ISelectionChangedListener) listeners[i];
				listener.selectionChanged(event);
			}
		}

		/*
		 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
		 */
		public void addSelectionChangedListener(ISelectionChangedListener listener) {
			fListenerList.add(listener);			
		}

		/*
		 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
		 */
		public ISelection getSelection() {
			if (fActiveProvider != null) {
				return fActiveProvider.getSelection();
			}
			return StructuredSelection.EMPTY;
		}

		/*
		 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
		 */
		public void removeSelectionChangedListener(ISelectionChangedListener listener) {
			fListenerList.remove(listener);
		}

		/*
		 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
		 */
		public void setSelection(ISelection selection) {
			if (fActiveProvider != null) {
				fActiveProvider.setSelection(selection);
			}
		}
	}
	
	/**
	 * The model presentation used as the label provider for the tree viewer,
	 * and also as the detail information provider for the detail pane.
	 */
	protected VariablesViewModelPresentation fModelPresentation;
	
	/**
	 * The UI construct that provides a sliding sash between the variables tree
	 * and the detail pane.
	 */
	private SashForm fSashForm;
	
	/**
	 * Composite that holds the details pane and always remains
	 */
	private Composite fDetailsAnchor;
	
	/**
	 * Composite that holds the separator container and detail pane control.
	 * Gets disposed/created as the layout changes.
	 */
	private Composite fDetailsComposiste;
	
	/**
	 * Separator used when detail pane background colors of tree/detail pane are different.
	 */
	private Label fSepearator;
	
	/**
	 * Parent of the viewer, used to detect re-sizing for automatic layout
	 */
	private Composite fParent;
	
	/**
	 * Whether the detail pane has been built yet.
	 */
	private boolean fPaneBuilt = false;
	
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
	private boolean fTreeHasFocus = true;
	
	/**
	 * Various listeners used to update the enabled state of actions and also to
	 * populate the detail pane.
	 */
	private ISelectionChangedListener fTreeSelectionChangedListener;
	
	/**
	 * Listener added to the control of the detail pane, allows view to keep track of which
	 * part last had focus, the tree or the detail pane.
	 */
	private Listener fDetailPaneActivatedListener;	
	
	/**
	 * Viewer input service used to translate active debug context to viewer input.
	 */
	private ViewerInputService fInputService;
	
	private Map fGlobalActionMap = new HashMap();
	
	/**
	 * Viewer input requester used to update the viewer once the viewer input has been
	 * resolved.
	 */
	private IViewerInputRequestor fRequester = new IViewerInputRequestor() {
		public void viewerInputComplete(IViewerInputUpdate update) {
			if (!update.isCanceled()) {
			    viewerInputUpdateComplete(update);
			}
		}
	};
	
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
	 * Sash weights for a specific detail pane type
	 */
	protected static final String DETAIL_PANE_TYPE = "DETAIL_PANE_TYPE"; //$NON-NLS-1$
		
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
			refreshDetailPaneContents();
			return Status.OK_STATUS;
		}
	};

	/**
	 * Selection provider registered with the view site.
	 */
	private SelectionProviderWrapper fSelectionProvider;
    
	/**
	 * Presentation context for this view.
	 */
	private IPresentationContext fPresentationContext;
	
	/**
	 * Remove myself as a selection listener
	 * and preference change listener.
	 *
	 * @see IWorkbenchPart#dispose()
	 */
	public void dispose() {
		
        DebugUITools.removePartDebugContextListener(getSite(), this);
		getSite().getWorkbenchWindow().removePerspectiveListener(this);
		DebugUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
		JFaceResources.getFontRegistry().removeListener(this);
		TreeModelViewer viewer = getVariablesViewer();
		if (viewer != null) {
			viewer.removeModelChangedListener(this);
			viewer.removeViewerUpdateListener(this);
		}
		if (fPresentationContext != null) {
		    fPresentationContext.dispose();
		    fPresentationContext = null;
		}
		if (fDetailPane != null) fDetailPane.dispose();
        fInputService.dispose();
        fSelectionProvider.dispose();
		super.dispose();
	}

	/**
	 * Called when the viewer input update is completed.  Unlike 
	 * {@link #setViewerInput(Object)}, it allows overriding classes
	 * to examine the context for which the update was calculated.
	 * 
	 * @param update Completed viewer input update.
	 */
	protected void viewerInputUpdateComplete(IViewerInputUpdate update) {
	    setViewerInput(update.getInputElement());
        updateAction(FIND_ACTION);
	}
	
	/**
	 * Sets the input to the viewer
	 * @param context the object context
	 */
	protected void setViewerInput(Object context) {
        if (context == null) {
            // Clear the detail pane
        	refreshDetailPaneContents();
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
        updateObjects();
	}
	
	/**
	 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		String propertyName= event.getProperty();
		if (propertyName.equals(IDebugUIConstants.PREF_CHANGED_DEBUG_ELEMENT_COLOR) || 
				propertyName.equals(IDebugUIConstants.PREF_CHANGED_VALUE_BACKGROUND) ||
				propertyName.equals(IDebugUIConstants.PREF_VARIABLE_TEXT_FONT)) {
			getViewer().refresh();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#createViewer(Composite)
	 */
	public Viewer createViewer(Composite parent) {
		addResizeListener(parent);
		fParent = parent;
		fTriggerDetailsJob.setSystem(true);
		
		// create the sash form that will contain the tree viewer & text viewer
		fSashForm = new SashForm(parent, SWT.NONE);
		
		getModelPresentation();
		DebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
		JFaceResources.getFontRegistry().addListener(this);

		TreeModelViewer variablesViewer = createTreeViewer(fSashForm);
		fInputService = new ViewerInputService(variablesViewer, fRequester);
			
		fSashForm.setMaximizedControl(variablesViewer.getControl());
		fDetailsAnchor = SWTFactory.createComposite(fSashForm, parent.getFont(), 1, 1, GridData.FILL_BOTH, 0, 0);
		fSashForm.setWeights(getLastSashWeights());
		
		fSelectionProvider = new SelectionProviderWrapper(variablesViewer);
		getSite().setSelectionProvider(fSelectionProvider);
		
		createOrientationActions(variablesViewer);
		IPreferenceStore prefStore = DebugUIPlugin.getDefault().getPreferenceStore();
		String orientation = prefStore.getString(getDetailPanePreferenceKey());
		for (int i = 0; i < fToggleDetailPaneActions.length; i++) {
			fToggleDetailPaneActions[i].setChecked(fToggleDetailPaneActions[i].getOrientation().equals(orientation));
		}
		
		fDetailPane = new DetailPaneProxy(this);
		fDetailPane.addProperyListener(new IPropertyListener() {
			public void propertyChanged(Object source, int propId) {
				firePropertyChange(propId);
			}
		});
		setDetailPaneOrientation(orientation);
		
		IMemento memento = getMemento();
		if (memento != null) {
			variablesViewer.initState(memento);
		}
		
		variablesViewer.addModelChangedListener(this);
		variablesViewer.addViewerUpdateListener(this);
		
        initDragAndDrop(variablesViewer);

		return variablesViewer;
	}

    /**
     * Initializes the drag and/or drop adapters for this view.  Called from createViewer().
     * 
     * @param viewer the viewer to add drag/drop support to.
     * @since 3.4
     */
    protected void initDragAndDrop(TreeModelViewer viewer) {
        // Drag only
        viewer.addDragSupport(DND.DROP_COPY, new Transfer[] {LocalSelectionTransfer.getTransfer()}, new SelectionDragAdapter(viewer));
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
			int[] weights = getWeights(mem);
			if (weights != null) {
				setLastSashWeights(weights);
			}
		}
		site.getWorkbenchWindow().addPerspectiveListener(this);
    }
	
	/**
	 * Returns sash weights stored in the given memento or <code>null</code> if none.
	 * 
	 * @param memento Memento to read sash weights from
	 * @return sash weights or <code>null</code>
	 */
	private int[] getWeights(IMemento memento) {
		Integer sw = memento.getInteger(SASH_VIEW_PART);
		if(sw != null) {
			int view = sw.intValue();
			sw = memento.getInteger(SASH_DETAILS_PART);
			if(sw != null) {
				int details = sw.intValue();
				if(view > -1 & details > -1) {
					return new int[] {view, details};
				}
			}
		}
		return null;
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
	 * @param parent Viewer's parent control
	 * @return The created viewer.
	 */
	protected TreeModelViewer createTreeViewer(Composite parent) {
		
		int style = getViewerStyle();
		fPresentationContext = new DebugModelPresentationContext(getPresentationContextId(), this, fModelPresentation); 
		final TreeModelViewer variablesViewer = new TreeModelViewer(parent, style, fPresentationContext);
		
		variablesViewer.getControl().addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				fTreeHasFocus = true;
				fSelectionProvider.setActiveProvider(variablesViewer);
				setGlobalActions();
			}
			
			public void focusLost(FocusEvent e){
			    // Do not reset the selection provider with the provider proxy.
			    // This should allow toolbar actions to remain active when the view
			    // is de-activated but still visible.
			    // Bug 316850.
				clearGlobalActions();
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
		DebugUITools.addPartDebugContextListener(getSite(), this);
				
		return variablesViewer;
	}

	private void setGlobalActions() {
		for (Iterator entryItr = fGlobalActionMap.entrySet().iterator(); entryItr.hasNext();) {
			Map.Entry entry = (Map.Entry)entryItr.next();
			String actionID = (String)entry.getKey();
			IAction action = getOverrideAction(actionID);
			if (action == null) {
				action = (IAction)entry.getValue();
			}
			setAction(actionID, action);
		}
		getViewSite().getActionBars().updateActionBars();		
	}

	private void clearGlobalActions() {
		for (Iterator keyItr = fGlobalActionMap.keySet().iterator(); keyItr.hasNext();) {
			String id = (String)keyItr.next();
			setAction(id, null);
		}
		getViewSite().getActionBars().updateActionBars();		
	}

	/**
	 * Returns the active debug context for this view based on the view's 
	 * site IDs.
	 * 
	 * @return Active debug context for this view.
	 * 
	 * @since 3.7
	 */
	protected ISelection getDebugContext() {
	    IViewSite site = (IViewSite)getSite();
		IDebugContextService contextService = DebugUITools.getDebugContextManager().getContextService(site.getWorkbenchWindow());
		return contextService.getActiveContext(site.getId(), site.getSecondaryId());
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
	 * Returns the presentation context secondary id for this view.
	 * 
	 * @return context secondary id.
	 */
	protected String getPresentationContextSecondaryId() {
		return ((IViewSite)getSite()).getSecondaryId();
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
	
	private void addResizeListener(Composite parent) {
		parent.addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {
			}
			public void controlResized(ControlEvent e) {
				if (IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_AUTO.equals(fCurrentDetailPaneOrientation)) {
					setDetailPaneOrientation(IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_AUTO);
				}
			}
		});
	}
	
	/**
	 * Returns vertical or horizontal based on view size.
	 *  
	 * @return vertical or horizontal
	 */
	int computeOrientation() {
		Point size= fParent.getSize();
		if (size.x != 0 && size.y != 0) {
			if ((size.x / 3)> size.y)
				return SWT.HORIZONTAL;
			else
				return SWT.VERTICAL;
		}
		return SWT.HORIZONTAL;
	}	
	
	/**
	 * Set the orientation of the details pane so that is one of:
	 * - underneath the main tree view
	 * - to the right of the main tree view
	 * - not visible
	 * @param orientation Detail pane orientation to set.
	 * 
	 * @see IDebugPreferenceConstants#VARIABLES_DETAIL_PANE_AUTO
	 * @see IDebugPreferenceConstants#VARIABLES_DETAIL_PANE_HIDDEN
	 * @see IDebugPreferenceConstants#VARIABLES_DETAIL_PANE_UNDERNEATH
	 */
	public void setDetailPaneOrientation(String orientation) {
		if (!IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_AUTO.equals(orientation) && orientation.equals(fCurrentDetailPaneOrientation)) {
			return;
		}
		fCurrentDetailPaneOrientation  = orientation;
		DebugUIPlugin.getDefault().getPreferenceStore().setValue(getDetailPanePreferenceKey(), orientation);
		if (orientation.equals(IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_HIDDEN)) {
			hideDetailPane();
		} else {
			int vertOrHoriz = -1;
			if (orientation.equals(IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_AUTO)) {
				vertOrHoriz = computeOrientation();
				if (fPaneBuilt && fSashForm.getOrientation() == vertOrHoriz) {
					return;
				}
			} else {
				vertOrHoriz = orientation.equals(IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_UNDERNEATH) ? SWT.VERTICAL : SWT.HORIZONTAL;
				
			}
			buildDetailPane(vertOrHoriz);
			revealTreeSelection();
		}
	}
	
	private void buildDetailPane(int orientation) {
		try {
			fDetailsAnchor.setRedraw(false);
			if (fDetailsComposiste != null) {
				fDetailPane.dispose();
				fDetailsComposiste.dispose();
			}
			fSashForm.setOrientation(orientation);
			if (orientation == SWT.VERTICAL) {
				fDetailsComposiste = SWTFactory.createComposite(fDetailsAnchor, fDetailsAnchor.getFont(), 1, 1, GridData.FILL_BOTH, 0, 0);
				GridLayout layout = (GridLayout) fDetailsComposiste.getLayout();
				layout.verticalSpacing = 0;
				fSepearator = new Label(fDetailsComposiste, SWT.SEPARATOR| SWT.HORIZONTAL);
				fSepearator.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			} else {
				fDetailsComposiste = SWTFactory.createComposite(fDetailsAnchor, fDetailsAnchor.getFont(), 2, 1, GridData.FILL_BOTH, 0, 0);
				GridLayout layout = (GridLayout) fDetailsComposiste.getLayout();
				layout.horizontalSpacing = 0;
				fSepearator= new Label(fDetailsComposiste, SWT.SEPARATOR | SWT.VERTICAL);
				fSepearator.setLayoutData(new GridData(SWT.TOP, SWT.FILL, false, true));
			}
			// force update so detail pane can adapt to orientation change
			showDetailPane();
		} finally {
			fDetailsAnchor.layout(true);
			fDetailsAnchor.setRedraw(true);
			fPaneBuilt = true;
		}
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
		refreshDetailPaneContents();
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
	 * @return The last sash weights.
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
	 * @param weights Weight to add.
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
		
		action = new CollapseAllAction((TreeModelViewer)getViewer());
		setAction(COLLAPSE_ALL, action); 
		IHandlerService hs = (IHandlerService) getSite().getService(IHandlerService.class);
		if (hs != null) {
			hs.activateHandler(CollapseAllHandler.COMMAND_ID, new ActionHandler(action));
		}

		action = new ChangeVariableValueAction(this);
		action.setEnabled(false);
		setAction("ChangeVariableValue", action); //$NON-NLS-1$
		
		action= new VirtualFindAction(getVariablesViewer());
		setGlobalAction(FIND_ACTION, action);
	} 	

	/**
	 * Adds the given action to the set of global actions managed by this 
	 * variables view.  Global actions are cleard and reset whenever the detail 
	 * pane is activated to allow the detail pane to set the actions as 
	 * well.
	 * 
	 * @param actionID Action ID that the given action implements.
	 * @param action Action implementation.
	 * 
	 * @since 3.8
	 */
	protected void setGlobalAction(String actionID, IAction action) {
		fGlobalActionMap.put(actionID, action);
	}
	
	public IAction getAction(String actionID) {
		// Check if model overrides the action. Global action overrides are 
		// checked in setGlobalActions() so skip them here.
		if (!fGlobalActionMap.containsKey(actionID)) {
			IAction overrideAction = getOverrideAction(actionID);
			if (overrideAction != null) {
				return overrideAction;
			}
		}
		return super.getAction(actionID);
	}
	
	private IAction getOverrideAction(String actionID) {
		Viewer viewer = getViewer();
		if (viewer != null) {
			IViewActionProvider actionProvider = (IViewActionProvider) DebugPlugin.getAdapter(
					viewer.getInput(), IViewActionProvider.class);
			if (actionProvider != null) {
				IAction action = actionProvider.getAction(getPresentationContext(), actionID);
				if (action != null) {
					return action;
				}
			}
		}
		return null;
	}
	
	public void updateObjects() {
		super.updateObjects();
		if (fTreeHasFocus) {
			setGlobalActions();
			getViewSite().getActionBars().updateActionBars();
		}
	}
	
	/**
	 * Creates the actions that allow the orientation of the detail pane to be changed.
	 * 
	 * @param viewer Viewer to create actions for.
	 */
	private void createOrientationActions(TreeModelViewer viewer) {
		IActionBars actionBars = getViewSite().getActionBars();
		IMenuManager viewMenu = actionBars.getMenuManager();
		
		fToggleDetailPaneActions = new ToggleDetailPaneAction[4];
		fToggleDetailPaneActions[0] = new ToggleDetailPaneAction(this, IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_UNDERNEATH, null);
		fToggleDetailPaneActions[1] = new ToggleDetailPaneAction(this, IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_RIGHT, null);
		fToggleDetailPaneActions[2] = new ToggleDetailPaneAction(this, IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_AUTO, null);
		fToggleDetailPaneActions[3] = new ToggleDetailPaneAction(this, IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_HIDDEN, getToggleActionLabel());
		viewMenu.add(new Separator());
		final MenuManager layoutSubMenu = new MenuManager(VariablesViewMessages.VariablesView_40);
		layoutSubMenu.setRemoveAllWhenShown(true);
		layoutSubMenu.add(fToggleDetailPaneActions[0]);
		layoutSubMenu.add(fToggleDetailPaneActions[1]);
		layoutSubMenu.add(fToggleDetailPaneActions[2]);
		layoutSubMenu.add(fToggleDetailPaneActions[3]);
		viewMenu.add(layoutSubMenu);
		viewMenu.add(new Separator());
		
		fConfigureColumnsAction = new ConfigureColumnsAction(viewer);
		setAction("ToggleColmns", new ToggleShowColumnsAction(viewer)); //$NON-NLS-1$
		
		layoutSubMenu.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				layoutSubMenu.add(fToggleDetailPaneActions[0]);
				layoutSubMenu.add(fToggleDetailPaneActions[1]);
				layoutSubMenu.add(fToggleDetailPaneActions[2]);
				layoutSubMenu.add(fToggleDetailPaneActions[3]);
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
	
	/**
	 * Returns the label to use for the action that toggles the view layout to be the tree viewer only (detail pane is hidden).
	 * Should be of the style '[view name] View Only'.
	 * 
	 * @return action label for toggling the view layout to tree viewer only
	 */
	protected String getToggleActionLabel(){
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
		tbm.add(getAction(COLLAPSE_ALL)); 
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
		menu.add(getAction(FIND_ACTION));
		ChangeVariableValueAction changeValueAction = (ChangeVariableValueAction)getAction("ChangeVariableValue"); //$NON-NLS-1$
		if (changeValueAction.isApplicable()) {
		    menu.add(changeValueAction); 
		}
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
	 * 
	 * @return Created selection listener
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
                        refreshDetailPaneContents();
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

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.variables.details.IDetailPaneContainer#getCurrentPaneID()
	 */
	public String getCurrentPaneID() {
		return fDetailPane.getCurrentPaneID();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.variables.details.IDetailPaneContainer#getCurrentSelection()
	 */
	public IStructuredSelection getCurrentSelection() {
		if (getViewer() != null){
			return (IStructuredSelection)getViewer().getSelection();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.variables.details.IDetailPaneContainer#getParentComposite()
	 */
	public Composite getParentComposite() {
		return fDetailsComposiste;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.variables.details.IDetailPaneContainer#getWorkbenchPartSite()
	 */
	public IWorkbenchPartSite getWorkbenchPartSite() {
		return getSite();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.variables.details.IDetailPaneContainer#refreshDetailPaneContents()
	 */
	public void refreshDetailPaneContents() {
		if (isDetailPaneVisible()) {
			String currentPaneID = getCurrentPaneID();
			if (currentPaneID != null) {
				fLastSashWeights = fSashForm.getWeights();
			}
			fDetailPane.display(getCurrentSelection());
			// Adjust sash background color settings and separator based on detail pane background color:
			//   When the backgrounds are the same, the sash should have a default background, else it should be
			//   invisible and the label separator should appear with the same background color as the detail pane
			Control control = fDetailPane.getCurrentControl();
			if (control.getBackground().equals(fDetailsAnchor.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND))) {
				// don't show the label separator
				if (!fSepearator.isDisposed()) {
					getDefaultControl().setBackground(fDetailsAnchor.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
					fSepearator.dispose();
					fDetailsComposiste.layout(true);
				}
			} else {
				// show the label separator and make sash invisible
				if (fSepearator.isDisposed()) {
					// re-build the detail pane with the separator
					buildDetailPane(fSashForm.getOrientation());
					return;
				}
				getDefaultControl().setBackground(fDetailsAnchor.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
				fSepearator.setBackground(control.getBackground());
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.variables.details.IDetailPaneContainer#paneChanged(java.lang.String)
	 */
	public void paneChanged(String newPaneID) {
		if (fDetailPaneActivatedListener == null){
			fDetailPaneActivatedListener = 	new Listener() {
				public void handleEvent(Event event) {
					fTreeHasFocus = false;
				}
			};
		}
		fDetailPane.getCurrentControl().addListener(SWT.Activate, fDetailPaneActivatedListener);
	}
	
	/**
	 * @return the model presentation to be used for this view
	 */
	protected IDebugModelPresentation getModelPresentation() {
		if (fModelPresentation == null) {
			fModelPresentation = new VariablesViewModelPresentation();
		}
		return fModelPresentation;
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

	/**
	 * If possible, calls the update method of the action associated with the given ID.
	 * 
	 * @param actionId the ID of the action to update
	 */
	protected void updateAction(String actionId) {
		IAction action= getAction(actionId);
		if (action == null) {
			action = (IAction)fGlobalActionMap.get(actionId);
		}
		if (action instanceof IUpdate) {
			((IUpdate) action).update();
		}
	}
	
	/**
	 * @return whether the detail pane is visible to the user
	 */
	protected boolean isDetailPaneVisible() {
		return !fToggleDetailPaneActions[3].isChecked();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#getDefaultControl()
	 */
	protected Control getDefaultControl() {
		return fSashForm;
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.IDebugExceptionHandler#handleException(org.eclipse.debug.core.DebugException)
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

	/**
	 * Updates actions and sets the viewer input when a context is activated.
	 * @param selection New selection to activate.
	 */
	protected void contextActivated(ISelection selection) {
		if (!isAvailable() || !isVisible()) {
			return;
		}
		if (selection instanceof IStructuredSelection) {
			Object source = ((IStructuredSelection)selection).getFirstElement();
			fInputService.resolveViewerInput(source);
		}
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
		} else {
			ISelection selection = getVariablesViewer().getSelection();
			if (selection instanceof TreeSelection) {
				TreeSelection ss = (TreeSelection) selection;
				if (ss.size() == 1) {
					Widget item = getVariablesViewer().findItem(ss.getPaths()[0]);
					if (item instanceof TreeItem) {
						TreeItem ti = (TreeItem) item;
						if (ti.getExpanded()) {
							ti.setExpanded(false);
						} else {
							// need to trigger proper children updates when expanding
							getVariablesViewer().expandToLevel(ss.getPaths()[0], 1);
						}
					}
					
				}
			}
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
	 * @see org.eclipse.debug.ui.IDetailSite#isMainViewerAvailable()
	 */
	public boolean isMainViewerAvailable() {
		return isAvailable();
	}
	
	/**
	 * @return the presentation context of the viewer
	 */
	protected IPresentationContext getPresentationContext() {
		return getVariablesViewer().getPresentationContext();
	}
	
	/** 
	 * Sets whether logical structures are being displayed
	 * @param flag If true, turns the logical structures on.
	 */
	public void setShowLogicalStructure(boolean flag) {
	    getPresentationContext().setProperty(PRESENTATION_SHOW_LOGICAL_STRUCTURES, Boolean.valueOf(flag));
	}	
	
	/** 
	 * Returns whether logical structures are being displayed
	 * @return Returns true if logical structures should be shown. 
	 */
	public boolean isShowLogicalStructure() {
		Boolean show = (Boolean) getPresentationContext().getProperty(PRESENTATION_SHOW_LOGICAL_STRUCTURES);
		return show != null && show.booleanValue();
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#becomesHidden()
	 */
	protected void becomesHidden() {
        fInputService.resolveViewerInput(ViewerInputService.NULL_INPUT);
		super.becomesHidden();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#becomesVisible()
	 */
	protected void becomesVisible() {
		super.becomesVisible();
		ISelection selection = getDebugContext();
		contextActivated(selection);
	}

	/**
	 * @return the tree model viewer displaying variables
	 */
	protected TreeModelViewer getVariablesViewer() {
		return (TreeModelViewer) getViewer();
	}
	
	/**
	 * Clears the status line of all messages and errors
	 */
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
			fSashForm.layout(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.IModelChangedListener#modelChanged(org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta)
	 */
	public void modelChanged(IModelDelta delta, IModelProxy proxy) {
		fVisitor.reset();
		delta.accept(fVisitor);
		
		updateAction(FIND_ACTION);
        updateAction(COLLAPSE_ALL);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.IViewerUpdateListener#updateComplete(org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousRequestMonitor)
	 */
	public void updateComplete(IViewerUpdate update) {
		IStatus status = update.getStatus();
		if (!update.isCanceled()) {
			if (status != null && !status.isOK()) {
				showMessage(status.getMessage());
			} else {
				showViewer();
			}
			if (TreePath.EMPTY.equals(update.getElementPath())) {
			    updateAction(FIND_ACTION);
			    updateAction(COLLAPSE_ALL);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.IViewerUpdateListener#updateStarted(org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousRequestMonitor)
	 */
	public void updateStarted(IViewerUpdate update) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.IViewerUpdateListener#viewerUpdatesBegin()
	 */
	public synchronized void viewerUpdatesBegin() {
		fTriggerDetailsJob.cancel();
        IWorkbenchSiteProgressService progressService = 
            (IWorkbenchSiteProgressService)getSite().getAdapter(IWorkbenchSiteProgressService.class);
        if (progressService != null) {
            progressService.incrementBusy();
        }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.IViewerUpdateListener#viewerUpdatesComplete()
	 */
	public synchronized void viewerUpdatesComplete() {
		if (fVisitor.isTriggerDetails()) {
			fTriggerDetailsJob.schedule();
		}
        IWorkbenchSiteProgressService progressService = 
            (IWorkbenchSiteProgressService)getSite().getAdapter(IWorkbenchSiteProgressService.class);
        if (progressService != null) {
            progressService.decrementBusy();
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
	
	protected ToggleDetailPaneAction getToggleDetailPaneAction(String orientation)
	{
		for (int i=0; i<fToggleDetailPaneActions.length; i++)
			if (fToggleDetailPaneActions[i].getOrientation().equals(orientation))
				return fToggleDetailPaneActions[i];
		
		return null;
	}

	/*
	 * @see org.eclipse.debug.internal.ui.views.variables.details.IDetailPaneContainer2#setSelectionProvider(org.eclipse.jface.viewers.ISelectionProvider)
	 */
	public void setSelectionProvider(ISelectionProvider provider) {
		// Workaround for legacy detail pane implementations (bug 254442)
		// set selection provider wrapper again in case it got overridden by detail pane
		getSite().setSelectionProvider(fSelectionProvider);
		// change active provider
		fSelectionProvider.setActiveProvider(provider);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) {
		fDetailPane.doSave(monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#doSaveAs()
	 */
	public void doSaveAs() {
		fDetailPane.doSaveAs();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#isDirty()
	 */
	public boolean isDirty() {
		return fDetailPane.isDirty();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
	 */
	public boolean isSaveAsAllowed() {
		return fDetailPane.isSaveAsAllowed();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#isSaveOnCloseNeeded()
	 */
	public boolean isSaveOnCloseNeeded() {
		return fDetailPane.isSaveOnCloseNeeded();
	}

	/*
	 * @see org.eclipse.ui.ISaveablePart2#promptToSaveOnClose()
	 * @since 3.7
	 */
	public int promptToSaveOnClose() {
		return ISaveablePart2.YES;
	}
}
