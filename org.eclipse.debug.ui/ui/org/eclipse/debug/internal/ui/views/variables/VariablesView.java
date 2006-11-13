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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DelegatingModelPresentation;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.LazyModelPresentation;
import org.eclipse.debug.internal.ui.VariablesViewModelPresentation;
import org.eclipse.debug.internal.ui.actions.CollapseAllAction;
import org.eclipse.debug.internal.ui.actions.ConfigureColumnsAction;
import org.eclipse.debug.internal.ui.actions.variables.AssignValueAction;
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
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.debug.ui.commands.IStatusMonitor;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.jface.action.Action;
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
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.IUndoManagerExtension;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension2;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.console.actions.TextViewerAction;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.operations.OperationHistoryActionHandler;
import org.eclipse.ui.operations.RedoActionHandler;
import org.eclipse.ui.operations.UndoActionHandler;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.progress.WorkbenchJob;
import org.eclipse.ui.texteditor.FindReplaceAction;
import org.eclipse.ui.texteditor.IAbstractTextEditorHelpContextIds;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.IUpdate;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;

import com.ibm.icu.text.MessageFormat;

/**
 * This view shows variables and their values for a particular stack frame
 */
public class VariablesView extends AbstractDebugView implements IDebugContextListener,
	IPropertyChangeListener, IDebugExceptionHandler,
	IPerspectiveListener, IModelChangedListener,
	IViewerUpdateListener {

	/**
	 * Internal interface for a cursor listener. I.e. aggregation 
	 * of mouse and key listener.
	 * @since 3.0
	 */
	interface ICursorListener extends MouseListener, KeyListener {
	}
	
	/**
	 * The selection provider for the variables view changes depending on whether
	 * the variables viewer or detail pane source viewer have focus. This "super" 
	 * provider ensures the correct selection is sent to all listeners.
	 */
	class VariablesViewSelectionProvider implements ISelectionProvider {
		private ListenerList fListeners= new ListenerList();
		private ISelectionProvider fUnderlyingSelectionProvider;
		/**
		 * @see ISelectionProvider#addSelectionChangedListener(ISelectionChangedListener)
		 */
		public void addSelectionChangedListener(ISelectionChangedListener listener) {
			fListeners.add(listener);
		}

		/**
		 * @see ISelectionProvider#getSelection()
		 */
		public ISelection getSelection() {
			return getUnderlyingSelectionProvider().getSelection();
		}

		/**
		 * @see ISelectionProvider#removeSelectionChangedListener(ISelectionChangedListener)
		 */
		public void removeSelectionChangedListener(ISelectionChangedListener listener) {
			fListeners.remove(listener);
		}

		/**
		 * @see ISelectionProvider#setSelection(ISelection)
		 */
		public void setSelection(ISelection selection) {
			getUnderlyingSelectionProvider().setSelection(selection);
		}
		
		protected ISelectionProvider getUnderlyingSelectionProvider() {
			return fUnderlyingSelectionProvider;
		}

		protected void setUnderlyingSelectionProvider(ISelectionProvider underlyingSelectionProvider) {
			fUnderlyingSelectionProvider = underlyingSelectionProvider;
		}
		
		protected void fireSelectionChanged(SelectionChangedEvent event) {
			Object[] listeners= fListeners.getListeners();
			for (int i = 0; i < listeners.length; i++) {
				ISelectionChangedListener listener = (ISelectionChangedListener)listeners[i];
				listener.selectionChanged(event);
			}
		}
	}
	
	/**
	 * Re-targets the find action to the details area or tree viewer.
	 * 
	 * @since 3.2
	 */
	class DelegatingFindAction extends Action implements IUpdate {
			private IAction getFindAction() {
				if (getDetailViewer().getTextWidget().isFocusControl()) {
					return getAction("FindReplaceText"); //$NON-NLS-1$
				} else {
					return getAction(FIND_ELEMENT);
				}
			}
			public void run() {
				IAction findAction = getFindAction();
				if (findAction != null) {
					findAction.run();
				}
			}

			/* (non-Javadoc)
			 * @see org.eclipse.ui.texteditor.IUpdate#update()
			 */
			public void update() {
				IAction findAction = getFindAction();
				if (findAction != null) {
					setEnabled(findAction.isEnabled());
				} else {
					setEnabled(false);
				}
			}
	}
	
	/**
	 * Job to compute the details for a selection
	 */
	class DetailJob extends Job implements IValueDetailListener {
		
		private IStructuredSelection fElements;
		private boolean fFirst = true;
		private IProgressMonitor fMonitor;
		
		public DetailJob(IStructuredSelection elements) {
			super("compute variable details"); //$NON-NLS-1$
			setSystem(true);
			fElements = elements;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		protected IStatus run(IProgressMonitor monitor) {
			fMonitor = monitor;
			Iterator iterator = fElements.iterator();
			while (iterator.hasNext()) {
				if (monitor.isCanceled()) {
					break;
				}
				Object element = iterator.next();
				IValue val = null;
				if (element instanceof IVariable) {
					try {
						val = ((IVariable)element).getValue();
					} catch (DebugException e) {
						detailComputed(null, e.getStatus().getMessage());
					}
				} else if (element instanceof IExpression) {
					val = ((IExpression)element).getValue();
				}
				if (val instanceof IndexedValuePartition) {
					val = null;
				}
				if (val != null && !monitor.isCanceled()) {
					if (monitor.isCanceled()) {
						break;
					}
					getModelPresentation().computeDetail(val, this);
					synchronized (this) {
						try {
							wait();
						} catch (InterruptedException e) {
							break;
						}
					}
				}				
			}
			return Status.OK_STATUS;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.ui.IValueDetailListener#detailComputed(org.eclipse.debug.core.model.IValue, java.lang.String)
		 */
		public void detailComputed(IValue value, final String result) {
			if (!fMonitor.isCanceled()) {
				WorkbenchJob append = new WorkbenchJob("append details") { //$NON-NLS-1$
					public IStatus runInUIThread(IProgressMonitor monitor) {
						if (!fMonitor.isCanceled()) {
							String insert = result;
							int length = 0;
							if (!fFirst) {
								length = getDetailDocument().getLength();
							}
							if (length > 0) {
								insert = "\n" + result; //$NON-NLS-1$
							}
							try {
								int max = DebugUIPlugin.getDefault().getPreferenceStore().getInt(IInternalDebugUIConstants.PREF_MAX_DETAIL_LENGTH);
								if (max > 0 && insert.length() > max) {
									insert = insert.substring(0, max) + "..."; //$NON-NLS-1$
								}
								if (fFirst) {
									setDetails(insert);
									fFirst = false;
								} else {
									getDetailDocument().replace(length, 0,insert);
								}
							} catch (BadLocationException e) {
								DebugUIPlugin.log(e);
							}
						}
						return Status.OK_STATUS;
					}
				};
				append.setSystem(true);
				append.schedule();
			}
			synchronized (this) {
				notifyAll();
			}
			
		}
		
	}
	
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
	 * The detail pane viewer and its associated document.
	 */
	private SourceViewer fDetailViewer;
	private IDocument fDetailDocument;
	
	/**
	 * Job computing details for selected variables
	 */
	private DetailJob fDetailsJob = null;
	
	/**
	 * The identifier of the debug model that is/was being displayed
	 * in this view. When the type of model being displayed changes,
	 * the details area needs to be reconfigured.
	 */
	private String fDebugModelIdentifier;
	
	/**
	 * The configuration being used in the details area
	 */
	private SourceViewerConfiguration fSourceViewerConfiguration;
	
	/**
	 * Various listeners used to update the enabled state of actions and also to
	 * populate the detail pane.
	 */
	private ISelectionChangedListener fTreeSelectionChangedListener;
	private ISelectionChangedListener fDetailSelectionChangedListener;
	private IDocumentListener fDetailDocumentListener;
	
	/**
	 * Selection provider for this view.
	 */
	private VariablesViewSelectionProvider fSelectionProvider= new VariablesViewSelectionProvider();
	
	/**
	 * Collections for tracking actions.
	 */
	private List fSelectionActions = new ArrayList(3);
	
	/**
	 * Remembers which viewer (tree viewer or details viewer) had focus, so we
	 * can reset the focus properly when re-activated.
	 */
	private Viewer fFocusViewer = null;
	
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

	protected static final String DETAIL_SELECT_ALL_ACTION = SELECT_ALL_ACTION + ".Detail"; //$NON-NLS-1$
	protected static final String VARIABLES_SELECT_ALL_ACTION=  SELECT_ALL_ACTION + ".Variables"; //$NON-NLS-1$
	
	protected static final String DETAIL_COPY_ACTION = ActionFactory.COPY.getId() + ".Detail"; //$NON-NLS-1$

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
	protected static final String FIND_ELEMENT = "FindElement"; //$NON-NLS-1$
	
	private StatusLineContributionItem fStatusLineItem;
	private ICursorListener fCursorListener;
	/**
	 * Data structure for the position label value.
	 */
	private static class PositionLabelValue {
		
		public int fValue;
		
		public String toString() {
			return String.valueOf(fValue);
		}
	}
	/** The pattern used to show the position label in the status line. */
	private final String fPositionLabelPattern= VariablesViewMessages.VariablesView_56; 
	/** The position label value of the current line. */
	private final PositionLabelValue fLineLabel= new PositionLabelValue();
	/** The position label value of the current column. */
	private final PositionLabelValue fColumnLabel= new PositionLabelValue();
	/** The arguments for the position label pattern. */
	private final Object[] fPositionLabelPatternArguments= new Object[] { fLineLabel, fColumnLabel };

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
		getViewSite().getActionBars().getStatusLineManager().remove(fStatusLineItem);
		DebugUITools.getDebugContextManager().getContextService(getSite().getWorkbenchWindow()).removeDebugContextListener(this);
		getSite().getWorkbenchWindow().removePerspectiveListener(this);
		DebugUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
		JFaceResources.getFontRegistry().removeListener(this);
		TreeModelViewer viewer = getVariablesViewer();
		if (viewer != null) {
			getDetailDocument().removeDocumentListener(getDetailDocumentListener());
			viewer.removeModelChangedListener(this);
			viewer.removeViewerUpdateListener(this);
		}
		super.dispose();
	}

	/**
	 * Sets the input to the viewer
	 * @param context the object context
	 */
	protected void setViewerInput(Object context) {
		
		getDetailViewer().setEditable(context != null);
		if (context == null) {
			setDetails(""); //$NON-NLS-1$
		}
		
		Object current= getViewer().getInput();
		
		if (current == null && context == null) {
			return;
		}

		if (current != null && current.equals(context)) {
			return;
		}	
		
		if (context instanceof IDebugElement) {
			setDebugModel(((IDebugElement)context).getModelIdentifier());
		}
		showViewer();
		getViewer().setInput(context);
	}
		
	/**
	 * Configures the details viewer for the debug model
	 * currently being displayed
	 */
	protected void configureDetailsViewer() {
		LazyModelPresentation mp = (LazyModelPresentation)fModelPresentation.getPresentation(getDebugModel());
		SourceViewerConfiguration svc = null;
		if (mp != null) {
			try {
				svc = mp.newDetailsViewerConfiguration();
			} catch (CoreException e) {
				DebugUIPlugin.errorDialog(getSite().getShell(), VariablesViewMessages.VariablesView_Error_1, VariablesViewMessages.VariablesView_Unable_to_configure_variable_details_area__2, e); 
			}
		}
		ISourceViewer detailViewer = getDetailViewer();
        if (svc == null) {
			svc = new SourceViewerConfiguration();
			detailViewer.setEditable(false);
		}
        if (detailViewer instanceof ISourceViewerExtension2) {
            ISourceViewerExtension2 sourceViewer = (ISourceViewerExtension2) detailViewer;
            sourceViewer.unconfigure();
        }
		detailViewer.configure(svc);
		//update actions that depend on the configuration of the details viewer
		updateAction("ContentAssist"); //$NON-NLS-1$
		setDetailViewerConfiguration(svc);
		createUndoRedoActions();
	}
	
	/**
	 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		String propertyName= event.getProperty();
		if (propertyName.equals(IDebugUIConstants.PREF_CHANGED_DEBUG_ELEMENT_COLOR) || 
				propertyName.equals(IInternalDebugUIConstants.PREF_CHANGED_VALUE_BACKGROUND)) {
			getViewer().refresh();
		} else if (propertyName.equals(IInternalDebugUIConstants.DETAIL_PANE_FONT)) {
			getDetailViewer().getTextWidget().setFont(JFaceResources.getFont(IInternalDebugUIConstants.DETAIL_PANE_FONT));			
		} else if (propertyName.equals(IInternalDebugUIConstants.PREF_MAX_DETAIL_LENGTH)) {
			populateDetailPane();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#createViewer(Composite)
	 */
	public Viewer createViewer(Composite parent) {
		fTriggerDetailsJob.setSystem(true);
		TreeModelViewer variablesViewer = createTreeViewer(parent);
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
		
		createDetailsViewer();
		fSashForm.setMaximizedControl(variablesViewer.getControl());

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
	 * @return the preference key for the variables view details pane
	 */
	protected String getDetailPanePreferenceKey() {
		return IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_ORIENTATION;
	}
		
	/**
	 * Create and return the main tree viewer that displays variable.
	 */
	protected TreeModelViewer createTreeViewer(Composite parent) {
		fModelPresentation = new VariablesViewModelPresentation();
		DebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
		JFaceResources.getFontRegistry().addListener(this);
		
		// create the sash form that will contain the tree viewer & text viewer
		fSashForm = new SashForm(parent, SWT.NONE);

		// add tree viewer
		int style = getViewerStyle();
		final TreeModelViewer variablesViewer = new TreeModelViewer(fSashForm, style,
				new DebugModelPresentationContext(getPresentationContextId(), fModelPresentation));
		variablesViewer.getControl().addFocusListener(new FocusAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.FocusListener#focusGained(FocusEvent)
			 */
			public void focusGained(FocusEvent e) {
				getVariablesViewSelectionProvider().setUnderlyingSelectionProvider(variablesViewer);
				setAction(SELECT_ALL_ACTION, getAction(VARIABLES_SELECT_ALL_ACTION));
				setAction(COPY_ACTION, getAction(IDebugView.COPY_ACTION));
				getViewSite().getActionBars().updateActionBars();
				setFocusViewer(getViewer());
			}
		});
		variablesViewer.addPostSelectionChangedListener(getTreeSelectionChangedListener());
		getVariablesViewSelectionProvider().setUnderlyingSelectionProvider(variablesViewer);
		getSite().setSelectionProvider(getVariablesViewSelectionProvider());

		// listen to debug context
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
	
	/**
	 * Create the widgets for the details viewer.
	 */
	protected void createDetailsViewer() {
		// Create & configure a SourceViewer
		SourceViewer detailsViewer= new SourceViewer(fSashForm, null, SWT.V_SCROLL | SWT.H_SCROLL);
		fDetailViewer = detailsViewer;
		detailsViewer.setDocument(getDetailDocument());
		detailsViewer.getTextWidget().setFont(JFaceResources.getFont(IInternalDebugUIConstants.DETAIL_PANE_FONT));
		getDetailDocument().addDocumentListener(getDetailDocumentListener());
		detailsViewer.setEditable(false);
		Control control = detailsViewer.getControl();
		GridData gd = new GridData(GridData.FILL_BOTH);
		control.setLayoutData(gd);
		
		detailsViewer.getSelectionProvider().addSelectionChangedListener(getDetailSelectionChangedListener());
		detailsViewer.getControl().addFocusListener(new FocusAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.FocusListener#focusGained(FocusEvent)
			 */
			public void focusGained(FocusEvent e) {
				getVariablesViewSelectionProvider().setUnderlyingSelectionProvider(getDetailViewer().getSelectionProvider());
				setAction(SELECT_ALL_ACTION, getAction(DETAIL_SELECT_ALL_ACTION));
				setAction(COPY_ACTION, getAction(DETAIL_COPY_ACTION));
				getViewSite().getActionBars().updateActionBars();
				setFocusViewer((Viewer)getDetailViewer());
			}
		});
		
		// add a context menu to the detail area
		createDetailContextMenu(detailsViewer.getTextWidget());
		
		detailsViewer.getTextWidget().addMouseListener(getCursorListener());
		detailsViewer.getTextWidget().addKeyListener(getCursorListener());
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
	 * Set on or off the word wrap flag for the detail pane.
	 */
	public void toggleDetailPaneWordWrap(boolean on) {
		fDetailViewer.getTextWidget().setWordWrap(on);
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
	
	/**
	 * Create the context menu particular to the detail pane.  Note that anyone
	 * wishing to contribute an action to this menu must use
	 * <code>IDebugUIConstants.VARIABLE_VIEW_DETAIL_ID</code> as the
	 * <code>targetID</code> in the extension XML.
	 */
	protected void createDetailContextMenu(Control menuControl) {
		MenuManager menuMgr= new MenuManager(); 
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				fillDetailContextMenu(mgr);
			}
		});
		Menu menu= menuMgr.createContextMenu(menuControl);
		menuControl.setMenu(menu);

		// register the context menu such that other plug-ins may contribute to it
		getSite().registerContextMenu(IDebugUIConstants.VARIABLE_VIEW_DETAIL_ID, menuMgr, getDetailViewer().getSelectionProvider());		
		addContextMenuManager(menuMgr);
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
		
		TextViewerAction textAction= new TextViewerAction(getDetailViewer(), ISourceViewer.CONTENTASSIST_PROPOSALS);
		textAction.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		textAction.configureAction(VariablesViewMessages.VariablesView_Co_ntent_Assist_3, "",""); //$NON-NLS-1$ //$NON-NLS-2$ 
		textAction.setImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_ELCL_CONTENT_ASSIST));
		textAction.setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_CONTENT_ASSIST));
		textAction.setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_DLCL_CONTENT_ASSIST));
		setAction("ContentAssist", textAction); //$NON-NLS-1$
        
        ActionHandler actionHandler = new ActionHandler(textAction);
        IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
        handlerService.activateHandler(textAction.getActionDefinitionId(), actionHandler);
		
		textAction= new TextViewerAction(getDetailViewer(), ITextOperationTarget.SELECT_ALL);
		textAction.configureAction(VariablesViewMessages.VariablesView_Select__All_5, "", ""); //$NON-NLS-1$ //$NON-NLS-2$ 
		textAction.setActionDefinitionId(IWorkbenchActionDefinitionIds.SELECT_ALL);
		setAction(DETAIL_SELECT_ALL_ACTION, textAction);
		
		textAction= new TextViewerAction(getDetailViewer(), ITextOperationTarget.COPY);
		textAction.configureAction(VariablesViewMessages.VariablesView__Copy_8, "", "");  //$NON-NLS-1$ //$NON-NLS-2$
		textAction.setActionDefinitionId(IWorkbenchActionDefinitionIds.COPY);
		setAction(DETAIL_COPY_ACTION, textAction);
		
		textAction= new TextViewerAction(getDetailViewer(), ITextOperationTarget.CUT);
		textAction.configureAction(VariablesViewMessages.VariablesView_Cu_t_11, "", ""); //$NON-NLS-1$ //$NON-NLS-2$ 
		textAction.setActionDefinitionId(IWorkbenchActionDefinitionIds.CUT);
		setAction(ActionFactory.CUT.getId(), textAction);
		
		textAction= new TextViewerAction(getDetailViewer(), ITextOperationTarget.PASTE);
		textAction.configureAction(VariablesViewMessages.VariablesView__Paste_14, "", ""); //$NON-NLS-1$ //$NON-NLS-2$ 
		textAction.setActionDefinitionId(IWorkbenchActionDefinitionIds.PASTE);
		setAction(ActionFactory.PASTE.getId(), textAction);
		
		action= new VirtualFindAction(getVariablesViewer());
		setAction(FIND_ELEMENT, action);
		
		// TODO: Still using "old" resource access
		ResourceBundle bundle= ResourceBundle.getBundle("org.eclipse.debug.internal.ui.views.variables.VariablesViewResourceBundleMessages"); //$NON-NLS-1$
		action = new FindReplaceAction(bundle, "find_replace_action_", VariablesView.this);	 //$NON-NLS-1$
		action.setActionDefinitionId(IWorkbenchActionDefinitionIds.FIND_REPLACE);
		setAction("FindReplaceText", action); //$NON-NLS-1$
		
		setAction(FIND_ACTION, new DelegatingFindAction());
		
		fSelectionActions.add(ActionFactory.COPY.getId());
		fSelectionActions.add(ActionFactory.CUT.getId());
		fSelectionActions.add(ActionFactory.PASTE.getId());
		fSelectionActions.add("FindReplaceText"); //$NON-NLS-1$
		updateAction("FindReplaceText"); //$NON-NLS-1$
		
		action = new AssignValueAction(this, fDetailViewer);
		setAction("AssignValue", action); //$NON-NLS-1$
		
		fStatusLineItem = new StatusLineContributionItem("ModeContributionItem"); //$NON-NLS-1$
		IActionBars actionBars = getViewSite().getActionBars();
		IStatusLineManager manager= actionBars.getStatusLineManager();
		manager.add(fStatusLineItem);
	} 
	
	/**
	 * Creates this editor's undo/re-do actions.
	 * <p>
	 * Subclasses may override or extend.</p>
	 *
	 * @since 3.2
	 */
	protected void createUndoRedoActions() {
		disposeUndoRedoAction(ITextEditorActionConstants.UNDO);
		disposeUndoRedoAction(ITextEditorActionConstants.REDO);
		IUndoContext undoContext= getUndoContext();
		if (undoContext != null) {
			// Use actions provided by global undo/re-do
			
			// Create the undo action
			OperationHistoryActionHandler undoAction= new UndoActionHandler(getSite(), undoContext);
			PlatformUI.getWorkbench().getHelpSystem().setHelp(undoAction, IAbstractTextEditorHelpContextIds.UNDO_ACTION);
			undoAction.setActionDefinitionId(IWorkbenchActionDefinitionIds.UNDO);
			setAction(ITextEditorActionConstants.UNDO, undoAction);

			// Create the re-do action.
			OperationHistoryActionHandler redoAction= new RedoActionHandler(getSite(), undoContext);
			PlatformUI.getWorkbench().getHelpSystem().setHelp(redoAction, IAbstractTextEditorHelpContextIds.REDO_ACTION);
			redoAction.setActionDefinitionId(IWorkbenchActionDefinitionIds.REDO);
			setAction(ITextEditorActionConstants.REDO, redoAction);
		}
	}	
	
	private void disposeUndoRedoAction(String actionId) {
		OperationHistoryActionHandler action = (OperationHistoryActionHandler) getAction(actionId);
		if (action != null) {
			action.dispose();
			setAction(actionId, null);
		}
	}
	/**
	 * Returns this editor's viewer's undo manager undo context.
	 *
	 * @return the undo context or <code>null</code> if not available
	 * @since 3.2
	 */
	private IUndoContext getUndoContext() {
		IUndoManager undoManager= fDetailViewer.getUndoManager();
		if (undoManager instanceof IUndoManagerExtension)
			return ((IUndoManagerExtension)undoManager).getUndoContext();
		return null;
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
		menu.add(getAction(FIND_ELEMENT));
		menu.add(getAction("ChangeVariableValue")); //$NON-NLS-1$
		IAction action = new AvailableLogicalStructuresAction(this);
		if (action.isEnabled()) {
			menu.add(action);
		}
		menu.add(new Separator(IDebugUIConstants.EMPTY_RENDER_GROUP));
		menu.add(new Separator(IDebugUIConstants.EMPTY_NAVIGATION_GROUP));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
   /**
	* Adds items to the detail area's context menu including any extension defined
	* actions.
	* 
	* @param menu The menu to add the item to.
	*/
	protected void fillDetailContextMenu(IMenuManager menu) {
		
		menu.add(new Separator(IDebugUIConstants.VARIABLE_GROUP));		
		menu.add(getAction("AssignValue")); //$NON-NLS-1$
		menu.add(getAction("ContentAssist")); //$NON-NLS-1$
		menu.add(new Separator());
		menu.add(getAction(ActionFactory.CUT.getId()));
		menu.add(getAction(ActionFactory.COPY.getId() + ".Detail")); //$NON-NLS-1$
		menu.add(getAction(ActionFactory.PASTE.getId()));
		menu.add(getAction(DETAIL_SELECT_ALL_ACTION));
		menu.add(new Separator("FIND")); //$NON-NLS-1$
		menu.add(getAction("FindReplaceText")); //$NON-NLS-1$
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
                        getVariablesViewSelectionProvider().fireSelectionChanged(event);				
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
        synchronized (this) {
        	if (fDetailsJob != null) {
        		fDetailsJob.cancel();
        	}
        	if (selection.isEmpty()) {
        		setDetails(""); //$NON-NLS-1$
        	} else {
	            fDetailsJob = new DetailJob(selection);
	            fDetailsJob.schedule();
        	}
        }
	}

	/**
	 * Clears the detail pane
	 */
	private void setDetails(final String value) {
       getDetailDocument().set(value);        
	}
	
	/**
	 * Lazily instantiate and return a selection listener that updates the enabled
	 * state of the selection oriented actions in this view.
	 */
	protected ISelectionChangedListener getDetailSelectionChangedListener() {
		if (fDetailSelectionChangedListener == null) {
			fDetailSelectionChangedListener = new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					if (event.getSelectionProvider().equals(getVariablesViewSelectionProvider().getUnderlyingSelectionProvider())) {
						getVariablesViewSelectionProvider().fireSelectionChanged(event);
						updateSelectionDependentActions();				
					}
				}
			};
		}
		return fDetailSelectionChangedListener;
	}
	
	/**
	 * Lazily instantiate and return a document listener that updates the enabled state
	 * of the 'Find/Replace' action.
	 */
	protected IDocumentListener getDetailDocumentListener() {
		if (fDetailDocumentListener == null) {
			fDetailDocumentListener = new IDocumentListener() {
				public void documentAboutToBeChanged(DocumentEvent event) {
				}
				public void documentChanged(DocumentEvent event) {
					updateAction(ActionFactory.FIND.getId());
				}
			};
		}
		return fDetailDocumentListener;
	}
	
	/**
	 * Lazily instantiate and return a Document for the detail pane text viewer.
	 */
	protected IDocument getDetailDocument() {
		if (fDetailDocument == null) {
			fDetailDocument = new Document();
		}
		return fDetailDocument;
	}
	
	protected IDebugModelPresentation getModelPresentation() {
		if (fModelPresentation == null) {
			fModelPresentation = new VariablesViewModelPresentation();
		}
		return fModelPresentation;
	}
	
	/**
	 * Returns the viewer used to display value details
	 * 
	 * @return source viewer
	 */
	protected ISourceViewer getDetailViewer() {
		return fDetailViewer;
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
		if (IFindReplaceTarget.class.equals(required)) {
			return getDetailViewer().getFindReplaceTarget();
		}
		if (ITextViewer.class.equals(required)) {
			return getDetailViewer();
		}
		if (IDebugModelPresentation.class.equals(required)) {
			return getModelPresentation();
		}
		return super.getAdapter(required);
	}

	protected void updateSelectionDependentActions() {
		Iterator iterator= fSelectionActions.iterator();
		while (iterator.hasNext()) {
			updateAction((String)iterator.next());		
		}
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
	
	/**
	 * Sets the identifier of the debug model being displayed
	 * in this view, or <code>null</code> if none.
	 * 
	 * @param id debug model identifier of the type of debug
	 *  elements being displayed in this view
	 */
	protected void setDebugModel(String id) {
		if (id != fDebugModelIdentifier) {
			fDebugModelIdentifier = id;
			configureDetailsViewer();	
			// force actions to initialize
			getVariablesViewSelectionProvider().fireSelectionChanged(new SelectionChangedEvent(
					getVariablesViewer(), new StructuredSelection(new Object())));
			getVariablesViewSelectionProvider().fireSelectionChanged(new SelectionChangedEvent(
					getVariablesViewer(), getSelectionProvider().getSelection()));
		} else {
			updateAction("ContentAssist"); //$NON-NLS-1$
		}
	}
	
	/**
	 * Returns the identifier of the debug model being displayed
	 * in this view, or <code>null</code> if none.
	 * 
	 * @return debug model identifier
	 */
	protected String getDebugModel() {
		return fDebugModelIdentifier;
	}	
	
	
	/**
	 * Sets the current configuration being used in the
	 * details area.
	 * 
	 * @param config source viewer configuration
	 */
	private void setDetailViewerConfiguration(SourceViewerConfiguration config) {
		fSourceViewerConfiguration = config;
	}
	
	/**
	 * Returns the current configuration being used in the
	 * details area.
	 * 
	 * @return source viewer configuration
	 */	
	protected SourceViewerConfiguration getDetailViewerConfiguration() {
		return fSourceViewerConfiguration;
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
	
	protected VariablesViewSelectionProvider getVariablesViewSelectionProvider() {
		return fSelectionProvider;
	}
	
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
		
		updateAction("ContentAssist"); //$NON-NLS-1$
		updateAction(FIND_ELEMENT);
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
	/**
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		if (getFocusViewer() == null) {
			super.setFocus();
		} else {
			getFocusViewer().getControl().setFocus();
		}
	}
	
	/**
	 * Sets the viewer that has focus.
	 * 
	 * @param viewer
	 */
	protected void setFocusViewer(Viewer viewer) {
		fFocusViewer = viewer;
	}
	
	/**
	 * Returns the viewer that has focus, or <code>null</code>.
	 * 
	 * @return Viewer
	 */
	protected Viewer getFocusViewer() {
		return fFocusViewer;
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
	
	/**
	 * Returns this view's "cursor" listener to be installed on the view's
	 * associated details viewer. This listener is listening to key and mouse button events.
	 * It triggers the updating of the status line.
	 * 
	 * @return the listener
	 */
	private ICursorListener getCursorListener() {
		if (fCursorListener == null) {
			fCursorListener= new ICursorListener() {
				
				public void keyPressed(KeyEvent e) {
					fStatusLineItem.setText(getCursorPosition());
				}
				
				public void keyReleased(KeyEvent e) {
				}
				
				public void mouseDoubleClick(MouseEvent e) {
				}
				
				public void mouseDown(MouseEvent e) {
				}
				
				public void mouseUp(MouseEvent e) {
					fStatusLineItem.setText(getCursorPosition());
				}
			};
		}
		return fCursorListener;
	}
	
	protected String getCursorPosition() {
		
		if (getDetailViewer() == null) {
			return ""; //$NON-NLS-1$
		}
		
		StyledText styledText= getDetailViewer().getTextWidget();
		int caret= styledText.getCaretOffset();
		IDocument document= getDetailViewer().getDocument();

		if (document == null) {
			return ""; //$NON-NLS-1$
		}
	
		try {
			
			int line= document.getLineOfOffset(caret);

			int lineOffset= document.getLineOffset(line);
			int tabWidth= styledText.getTabs();
			int column= 0;
			for (int i= lineOffset; i < caret; i++)
				if ('\t' == document.getChar(i)) {
					column += tabWidth - (tabWidth == 0 ? 0 : column % tabWidth);
				} else {
					column++;
				}
					
			fLineLabel.fValue= line + 1;
			fColumnLabel.fValue= column + 1;
			return MessageFormat.format(fPositionLabelPattern, fPositionLabelPatternArguments);
			
		} catch (BadLocationException x) {
			return ""; //$NON-NLS-1$
		}
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
		if (fDetailsJob != null) {
			fDetailsJob.cancel();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.IViewerUpdateListener#viewerUpdatesComplete()
	 */
	public synchronized void viewerUpdatesComplete() {
		if (fVisitor.isTriggerDetails()) {
			fTriggerDetailsJob.schedule();
		}
	}	
}
