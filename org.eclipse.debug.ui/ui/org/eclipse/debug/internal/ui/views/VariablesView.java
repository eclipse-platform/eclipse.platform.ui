package org.eclipse.debug.internal.ui.views;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DelegatingModelPresentation;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IDebugPreferenceConstants;
import org.eclipse.debug.internal.ui.LazyModelPresentation;
import org.eclipse.debug.internal.ui.actions.ChangeVariableValueAction;
import org.eclipse.debug.internal.ui.actions.ShowTypesAction;
import org.eclipse.debug.internal.ui.actions.ShowVariableDetailPaneAction;
import org.eclipse.debug.internal.ui.actions.TextViewerAction;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.texteditor.FindReplaceAction;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * This view shows variables and their values for a particular stack frame
 */
public class VariablesView extends AbstractDebugEventHandlerView implements ISelectionChangedListener, 
																	IPropertyChangeListener,
																	IValueDetailListener,
																	IDebugExceptionHandler {
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
	 * The model presentation used as the label provider for the tree viewer,
	 * and also as the detail information provider for the detail pane.
	 */
	private DelegatingModelPresentation fModelPresentation;
	
	/**
	 * The UI construct that provides a sliding sash between the variables tree
	 * and the detail pane.
	 */
	private SashForm fSashForm;
	
	/**
	 * The detail pane viewer and its associated document.
	 */
	private ISourceViewer fDetailViewer;
	private IDocument fDetailDocument;
	
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
	private Map fGlobalActions= new HashMap(3);	
	private List fSelectionActions = new ArrayList(1);
	
	/**
	 * These are used to initialize and persist the position of the sash that
	 * separates the tree viewer from the detail pane.
	 */
	private static final int[] DEFAULT_SASH_WEIGHTS = {6, 2};
	private int[] fLastSashWeights;
	private boolean fToggledDetailOnce;
	
	/**
	 * Remove myself as a selection listener
	 * and preference change listener.
	 *
	 * @see IWorkbenchPart#dispose()
	 */
	public void dispose() {
		// listen to selection in debug view
		DebugSelectionManager.getDefault().removeSelectionChangedListener(this, getSite().getPage(), IDebugUIConstants.ID_DEBUG_VIEW);	
		DebugUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
		getDetailDocument().removeDocumentListener(getDetailDocumentListener());
		Viewer viewer = getViewer();
		if (viewer != null) {
			((VariablesViewer)viewer).dispose();
		}
		super.dispose();
	}

	/** 
	 * The <code>VariablesView</code> listens for selection changes in the <code>LaunchesView</code>
	 *
	 * @see ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection sel = event.getSelection();
		if (sel instanceof IStructuredSelection) {
			setViewerInput((IStructuredSelection)sel);
		}
	}

	protected void setViewerInput(IStructuredSelection ssel) {
		
		IStackFrame frame= null;
		if (ssel.size() == 1) {
			Object input= ssel.getFirstElement();
			if (input instanceof IStackFrame) {
				frame= (IStackFrame)input;
			}
		}

		Object current= getViewer().getInput();
		if (current == null && frame == null) {
			return;
		}

		if (current != null && current.equals(frame)) {
			return;
		}
		if (frame != null) {
			setDebugModel(frame.getModelIdentifier());
		}
		showViewer();
		getViewer().setInput(frame);
	}
	
	/**
	 * Configures the details viewer for the debug model
	 * currently being displayed
	 */
	protected void configureDetailsViewer() {
		LazyModelPresentation mp = (LazyModelPresentation)fModelPresentation.getPresentation(getDebugModel());
		SourceViewerConfiguration svc = null;
		try {
			svc = mp.newDetailsViewerConfiguration();
		} catch (CoreException e) {
			DebugUIPlugin.errorDialog(getSite().getShell(), DebugUIViewsMessages.getString("VariablesView.Error_1"), DebugUIViewsMessages.getString("VariablesView.Unable_to_configure_variable_details_area._2"), e.getStatus()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (svc == null) {
			svc = new SourceViewerConfiguration();
			getDetailViewer().setEditable(false);
		} else {
			getDetailViewer().setEditable(true);
		}
		getDetailViewer().configure(svc);
		//update actions that depend on the configuration of the details viewer
		updateAction("ContentAssist"); //$NON-NLS-1$
		setDetailViewerConfiguration(svc);
	}
	
	/**
	 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		String propertyName= event.getProperty();
		if (propertyName.equals(IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_ORIENTATION)) {
			setDetailPaneOrientation((String)event.getNewValue());
		} else if (propertyName.equals(IDebugUIConstants.PREF_SHOW_VARIABLE_VALUE_CHANGES)) {
			getEventHandler().refresh();
		}
	}
	
	/**
	 * @see AbstractDebugView#createViewer(Composite)
	 */
	public Viewer createViewer(Composite parent) {
		
		fModelPresentation = new DelegatingModelPresentation();
		DebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
		// create the sash form that will contain the tree viewer & text viewer
		fSashForm = new SashForm(parent, SWT.NONE);
		IPreferenceStore prefStore = DebugUIPlugin.getDefault().getPreferenceStore();
		String orientString = prefStore.getString(IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_ORIENTATION);
		setDetailPaneOrientation(orientString);
		
		// add tree viewer
		final TreeViewer vv = new VariablesViewer(getSashForm(), SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		vv.setContentProvider(createContentProvider());
		vv.setLabelProvider(getModelPresentation());
		vv.setUseHashlookup(true);
		vv.getControl().addFocusListener(new FocusAdapter() {
			/**
			 * @see FocusListener#focusGained(FocusEvent)
			 */
			public void focusGained(FocusEvent e) {
				getVariablesViewSelectionProvider().setUnderlyingSelectionProvider(vv);
			}
		});
		vv.addSelectionChangedListener(getTreeSelectionChangedListener());
		getVariablesViewSelectionProvider().setUnderlyingSelectionProvider(vv);
		getSite().setSelectionProvider(getVariablesViewSelectionProvider());
		
		// add text viewer
		SourceViewer dv= new SourceViewer(getSashForm(), null, SWT.V_SCROLL | SWT.H_SCROLL);
		setDetailViewer(dv);
		dv.setDocument(getDetailDocument());
		getDetailDocument().addDocumentListener(getDetailDocumentListener());
		dv.setEditable(false);
		getSashForm().setMaximizedControl(vv.getControl());
		
		dv.getSelectionProvider().addSelectionChangedListener(getDetailSelectionChangedListener());

		dv.getControl().addFocusListener(new FocusAdapter() {
			/**
			 * @see FocusListener#focusGained(FocusEvent)
			 */
			public void focusGained(FocusEvent e) {
				getVariablesViewSelectionProvider().setUnderlyingSelectionProvider(getDetailViewer().getSelectionProvider());
			}
		});
		// add a context menu to the detail area
		createDetailContextMenu(dv.getTextWidget());
		
		// listen to selection in debug view
		DebugSelectionManager.getDefault().addSelectionChangedListener(this, getSite().getPage(), IDebugUIConstants.ID_DEBUG_VIEW);
		setEventHandler(createEventHandler(vv));
		
		return vv;
	}
		
	protected void addVerifyKeyListener() {
		getDetailViewer().getTextWidget().addVerifyKeyListener(new VerifyKeyListener() {
			public void verifyKey(VerifyEvent event) {
				//do code assist for CTRL-SPACE
				if (event.stateMask == SWT.CTRL && event.keyCode == 0) {
					if (event.character == 0x20) {
						IAction action= getAction("ContentAssist"); //$NON-NLS-1$
						if(action != null && action.isEnabled()) {
							action.run();
							event.doit= false;
						}
					}
				}
			}
		});
	}
	
	/**
	 * Creates this view's content provider.
	 * 
	 * @return a content provider
	 */
	protected IContentProvider createContentProvider() {
		VariablesViewContentProvider cp = new VariablesViewContentProvider();
		cp.setExceptionHandler(this);
		return cp;
	}
	
	/**
	 * Creates this view's event handler.
	 * 
	 * @param viewer the viewer associated with this view
	 * @return an event handler
	 */
	protected AbstractDebugEventHandler createEventHandler(Viewer viewer) {
		return new VariablesViewEventHandler(this);
	}	
	
	
	/**
	 * @see AbstractDebugView#getHelpContextId()
	 */
	protected String getHelpContextId() {
		return IDebugHelpContextIds.VARIABLE_VIEW;		
	}
	
	/**
	 * Set the orientation of the sash form to display its controls according to the value
	 * of <code>value</code>.  'VARIABLES_DETAIL_PANE_UNDERNEATH' means that the detail 
	 * pane appears underneath the tree viewer, 'VARIABLES_DETAIL_PANE_RIGHT' means the
	 * detail pane appears to the right of the tree viewer.
	 */
	protected void setDetailPaneOrientation(String value) {
		int orientation = value.equals(IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_UNDERNEATH) ? SWT.VERTICAL : SWT.HORIZONTAL;
		getSashForm().setOrientation(orientation);				
	}
	
	/**
	 * Show or hide the detail pane, based on the value of <code>on</code>.
	 * If showing, reset the sash form to use the relative weights that were
	 * in effect the last time the detail pane was visible, and populate it with
	 * details for the current selection.  If hiding, save the current relative 
	 * weights, unless the detail pane hasn't yet been shown.
	 */
	public void toggleDetailPane(boolean on) {
		if (on) {
			getSashForm().setMaximizedControl(null);
			getSashForm().setWeights(getLastSashWeights());
			populateDetailPane();
			fToggledDetailOnce = true;
		} else {
			if (fToggledDetailOnce) {
				setLastSashWeights(getSashForm().getWeights());
			}
			getSashForm().setMaximizedControl(getViewer().getControl());
		}
	}
	
	/**
	 * Ask the variables tree for its current selection, and use this to populate
	 * the detail pane.
	 */
	protected void populateDetailPane() {
		if (isDetailPaneVisible()) {
			IStructuredSelection selection = (IStructuredSelection) getViewer().getSelection();
			populateDetailPaneFromSelection(selection);		
		}
	}
	
	/**
	 * Return the relative weights that were in effect the last time both panes were
	 * visible in the sash form, or the default weights if both panes have not yet been
	 * made visible.
	 */
	protected int[] getLastSashWeights() {
		if (fLastSashWeights == null) {
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
	 * Initializes the viewer input on creation
	 */
	protected void setInitialContent() {
		ISelection selection = DebugSelectionManager.getDefault().getSelection(getSite().getPage(), IDebugUIConstants.ID_DEBUG_VIEW);
		if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
			setViewerInput((IStructuredSelection) selection);
		}
	}

	/**
	 * Create the context menu particular to the detail pane.  Note that anyone
	 * wishing to contribute an action to this menu must use
	 * <code>IDebugUIConstants.VARIABLE_VIEW_DETAIL_ID</code> as the
	 * <code>targetID</code> in the extension XML.
	 */
	protected void createDetailContextMenu(Control menuControl) {
		MenuManager menuMgr= new MenuManager(); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				fillDetailContextMenu(mgr);
			}
		});
		Menu menu= menuMgr.createContextMenu(menuControl);
		menuControl.setMenu(menu);

		// register the context menu such that other plugins may contribute to it
		getSite().registerContextMenu(IDebugUIConstants.VARIABLE_VIEW_DETAIL_ID, menuMgr, getDetailViewer().getSelectionProvider());		
	}
	
	/**
	 * @see AbstractDebugView#createActions()
	 */
	protected void createActions() {
		IAction action = new ShowTypesAction(getStructuredViewer());
		action.setChecked(false);
		setAction("ShowTypeNames",action); //$NON-NLS-1$
				
		action = new ChangeVariableValueAction(getViewer());
		action.setEnabled(false);
		setAction("ChangeVariableValue", action); //$NON-NLS-1$
		setAction(DOUBLE_CLICK_ACTION, action);
		
		action = new ShowVariableDetailPaneAction(this);
		action.setChecked(false);
		setAction("ShowDetailPane", action); //$NON-NLS-1$
	
		IActionBars actionBars= getViewSite().getActionBars();
		TextViewerAction textAction= new TextViewerAction(getDetailViewer(), getDetailViewer().getTextOperationTarget().COPY);
		textAction.configureAction(DebugUIMessages.getString("ConsoleView.&Copy@Ctrl+C_6"), DebugUIMessages.getString("ConsoleView.Copy_7"), DebugUIMessages.getString("ConsoleView.Copy_8")); //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
		setGlobalAction(actionBars, ITextEditorActionConstants.COPY, textAction);

		//XXX Still using "old" resource access
		ResourceBundle bundle= ResourceBundle.getBundle("org.eclipse.debug.internal.ui.DebugUIMessages"); //$NON-NLS-1$
		setGlobalAction(actionBars, ITextEditorActionConstants.FIND, new FindReplaceAction(bundle, "find_replace_action.", this));	 //$NON-NLS-1$
	
		fSelectionActions.add(ITextEditorActionConstants.COPY);
		updateAction(ITextEditorActionConstants.FIND);
		
		textAction= new TextViewerAction(getDetailViewer(), getDetailViewer().CONTENTASSIST_PROPOSALS);
		textAction.configureAction(DebugUIViewsMessages.getString("VariablesView.Co&ntent_Assist_3"), "",""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		setAction("ContentAssist", textAction); //$NON-NLS-1$
		
		addVerifyKeyListener();
		// set initial content here, as viewer has to be set
		setInitialContent();
	} 

	protected void setGlobalAction(IActionBars actionBars, String actionID, IAction action) {
		fGlobalActions.put(actionID, action); 
		actionBars.setGlobalActionHandler(actionID, action);
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
		tbm.add(new Separator("TOGGLE_VIEW")); //$NON-NLS-1$
		tbm.add(getAction("ShowDetailPane")); //$NON-NLS-1$
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
		menu.add(getAction("ChangeVariableValue")); //$NON-NLS-1$
		menu.add(new Separator(IDebugUIConstants.EMPTY_RENDER_GROUP));
		menu.add(new Separator(IDebugUIConstants.RENDER_GROUP));
		menu.add(getAction("ShowTypeNames")); //$NON-NLS-1$
		
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
		menu.add(getAction("ContentAssist")); //$NON-NLS-1$
		menu.add(new Separator());
		menu.add((IAction)fGlobalActions.get(ITextEditorActionConstants.COPY));

		menu.add(new Separator("FIND")); //$NON-NLS-1$
		menu.add((IAction)fGlobalActions.get(ITextEditorActionConstants.FIND));
		
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	/**
	 * Lazily instantiate and return a selection listener that populates the detail pane,
	 * but only if the detail is currently visible. 
	 */
	protected ISelectionChangedListener getTreeSelectionChangedListener() {
		if (fTreeSelectionChangedListener == null) {
			fTreeSelectionChangedListener = new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					// if the detail pane is not visible, don't waste time retrieving details
					if (getSashForm().getMaximizedControl() == getViewer().getControl()) {
						return;
					}	
					getVariablesViewSelectionProvider().fireSelectionChanged(event);				
					IStructuredSelection selection = (IStructuredSelection)event.getSelection();
					populateDetailPaneFromSelection(selection);
				}					
			};
		}
		return fTreeSelectionChangedListener;
	}
	
	/**
	 * Show the details associated with the first of the selected variables in the 
	 * detail pane.
	 */
	protected void populateDetailPaneFromSelection(IStructuredSelection selection) {
		try {
			if (!selection.isEmpty()) {
				IValue val = null;
				Object obj = selection.getFirstElement();
				if (obj instanceof IVariable) {
					val = ((IVariable)obj).getValue();
				} else if (obj instanceof IExpression) {
					val = ((IExpression)obj).getValue();
				}
				setDebugModel(val.getModelIdentifier());
				getModelPresentation().computeDetail(val, this);
			} else {
				getDetailDocument().set(""); //$NON-NLS-1$
			}
		} catch (DebugException de) {
			DebugUIPlugin.logError(de);
		}				
	}
	
	/**
	 * @see IValueDetailListener#detailComputed(IValue, String)
	 */
	public void detailComputed(IValue value, final String result) {
		Runnable runnable = new Runnable() {
			public void run() {
				getDetailDocument().set(result);
			}
		};
		asyncExec(runnable);		
	}
	
	/**
	 * Lazily instantiate and return a selection listener that updates the enabled
	 * state of the selection oriented actions in this view.
	 */
	protected ISelectionChangedListener getDetailSelectionChangedListener() {
		if (fDetailSelectionChangedListener == null) {
			fDetailSelectionChangedListener = new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					getVariablesViewSelectionProvider().fireSelectionChanged(event);
					updateSelectionDependentActions();				
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
					updateAction(ITextEditorActionConstants.FIND);
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
			fModelPresentation = new DelegatingModelPresentation();
		}
		return fModelPresentation;
	}
	
	/**
	 * Sets the viewer used to display value details.
	 * 
	 * @param viewer source viewer
	 */
	private void setDetailViewer(ISourceViewer viewer) {
		fDetailViewer = viewer;
	}
	
	/**
	 * Returns the viewer used to display value details
	 * 
	 * @return source viewer
	 */
	protected ISourceViewer getDetailViewer() {
		return fDetailViewer;
	}
	
	protected SashForm getSashForm() {
		return fSashForm;
	}
	
	/**
	 * @see WorkbenchPart#getAdapter(Class)
	 */
	public Object getAdapter(Class required) {
		if (IFindReplaceTarget.class.equals(required)) {
			return getDetailViewer().getFindReplaceTarget();
		}
		if (ITextViewer.class.equals(required)) {
			return getDetailViewer();
		}
		return super.getAdapter(required);
	}

	protected void updateSelectionDependentActions() {
		Iterator iterator= fSelectionActions.iterator();
		while (iterator.hasNext())
			updateAction((String)iterator.next());		
	}

	protected void updateAction(String actionId) {
		IAction action= (IAction)fGlobalActions.get(actionId);
		if (action == null) {
			action= getAction(actionId);
		}
		if (action instanceof IUpdate) {
			((IUpdate) action).update();
		}
	}
	
	protected boolean isDetailPaneVisible() {
		IAction action = getAction("ShowDetailPane"); //$NON-NLS-1$
		if (action != null) {
			return action.isChecked();
		}
		return false;
	}
	
	/**
	 * Sets the identifier of the debug model being displayed
	 * in this view, or <code>null</code> if none.
	 * 
	 * @param id debug model identifier of the type of debug
	 *  elements being displayed in this view
	 */
	private void setDebugModel(String id) {
		if (id != fDebugModelIdentifier) {
			fDebugModelIdentifier = id;
			configureDetailsViewer();
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
	
	/**
	 * @see AbstractDebugView#getDefaultControl()
	 */
	protected Control getDefaultControl() {
		return getSashForm();
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
}