package org.eclipse.debug.internal.ui;

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

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.views.AbstractDebugView;
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
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.FindReplaceAction;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * This view shows variables and their values for a particular stack frame
 */
public class VariablesView extends AbstractDebugView implements ISelectionListener, 
																	IPropertyChangeListener,
																	IValueDetailListener {

	
	/**
	 * The model presentation used as the label provider for the tree viewer,
	 * and also as the detail information provider for the detail pane.
	 */
	private IDebugModelPresentation fModelPresentation;
	
	/**
	 * The UI construct that provides a sliding sash between the variables tree
	 * and the detail pane.
	 */
	private SashForm fSashForm;
	
	/**
	 * The detail pane viewer and its associated document.
	 */
	private TextViewer fDetailTextViewer;
	private IDocument fDetailDocument;
	
	/**
	 * Various listeners used to update the enabled state of actions and also to
	 * populate the detail pane.
	 */
	private ISelectionChangedListener fTreeSelectionChangedListener;
	private ISelectionChangedListener fDetailSelectionChangedListener;
	private IDocumentListener fDetailDocumentListener;
	
	/**
	 * Collections for tracking actions.
	 */
	private Map fGlobalActions= new HashMap(3);	
	private List fSelectionActions = new ArrayList(3);
	
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
		getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
		DebugUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
		getDetailDocument().removeDocumentListener(getDetailDocumentListener());
		super.dispose();
	}

	/** 
	 * The <code>VariablesView</code> listens for selection changes in the <code>LaunchesView</code>
	 *
	 * @see ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection sel) {
		if (part.getSite().getId().equals(IDebugUIConstants.ID_DEBUG_VIEW)) {
			if (sel instanceof IStructuredSelection) {
				setViewerInput((IStructuredSelection)sel);
			}
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

		((VariablesContentProvider)getViewer().getContentProvider()).clearCache();
		getViewer().setInput(frame);
	}
	
	/**
	 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		String propertyName= event.getProperty();
		if (!propertyName.equals(IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_ORIENTATION)) {
			return;
		}
		setDetailPaneOrientation((String)event.getNewValue());
	}
	
	/**
	 * @see AbstractDebugView#createViewer(Composite)
	 */
	public StructuredViewer createViewer(Composite parent) {
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
		
		fModelPresentation = new DelegatingModelPresentation();
		DebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
		// create the sash form that will contain the tree viewer & text viewer
		fSashForm = new SashForm(parent, SWT.NONE);
		IPreferenceStore prefStore = DebugUIPlugin.getDefault().getPreferenceStore();
		String orientString = prefStore.getString(IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_ORIENTATION);
		setDetailPaneOrientation(orientString);
		
		// add tree viewer
		TreeViewer vv = new TreeViewer(getSashForm(), SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		vv.setContentProvider(new VariablesContentProvider(this));
		vv.setLabelProvider(getModelPresentation());
		vv.setUseHashlookup(true);
		
		// add text viewer
		fDetailTextViewer = new TextViewer(getSashForm(), SWT.V_SCROLL | SWT.H_SCROLL);
		getDetailTextViewer().setDocument(getDetailDocument());
		getDetailDocument().addDocumentListener(getDetailDocumentListener());
		getDetailTextViewer().setEditable(false);
		getSashForm().setMaximizedControl(vv.getControl());
		vv.addSelectionChangedListener(getTreeSelectionChangedListener());
		getDetailTextViewer().getSelectionProvider().addSelectionChangedListener(getDetailSelectionChangedListener());
				
		// add a context menu to the detail area
		createDetailContextMenu(getDetailTextViewer().getTextWidget());
	
		setInitialContent();
		return vv;
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
	protected void toggleDetailPane(boolean on) {
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
		IStructuredSelection selection = (IStructuredSelection) getViewer().getSelection();
		populateDetailPaneFromSelection(selection);		
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

	protected void setInitialContent() {
		IWorkbenchWindow window= DebugUIPlugin.getActiveWorkbenchWindow();
		if (window == null) {
			return;
		}
		IWorkbenchPage p= window.getActivePage();
		if (p == null) {
			return;
		}
		IViewPart view= p.findView(IDebugUIConstants.ID_DEBUG_VIEW);
		if (view != null) {
			ISelectionProvider provider= view.getSite().getSelectionProvider();
			if (provider != null) {
				provider.getSelection();
				ISelection selection= provider.getSelection();
				if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
					setViewerInput((IStructuredSelection) selection);
				}
			}
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
		getSite().registerContextMenu(IDebugUIConstants.VARIABLE_VIEW_DETAIL_ID, menuMgr, getDetailTextViewer().getSelectionProvider());		
	}
	
	/**
	 * @see AbstractDebugView#createActions()
	 */
	protected void createActions() {
		IAction action = new ShowTypesAction(getViewer());
		action.setChecked(false);
		setAction("ShowTypeNames",action);
		
		action = new ShowQualifiedAction(getViewer());
		action.setChecked(false);
		setAction("ShowQualifiedNames", action);
		
		setAction("AddToInspector", new AddToInspectorAction(getViewer()));
		
		action = new ChangeVariableValueAction(getViewer());
		action.setEnabled(false);
		setAction("ChangeVariableValue", action);
		setAction(DOUBLE_CLICK_ACTION, action);
		
		setAction("CopyToClipboard", new ControlAction(getViewer(), new CopyVariablesToClipboardActionDelegate()));
		
		action = new ShowVariableDetailPaneAction(this);
		action.setChecked(false);
		setAction("ShowDetailPane", action);
	
		IActionBars actionBars= getViewSite().getActionBars();
		TextViewerAction textAction= new TextViewerAction(getDetailTextViewer(), getDetailTextViewer().getTextOperationTarget().COPY);
		textAction.configureAction(DebugUIMessages.getString("ConsoleView.&Copy@Ctrl+C_6"), DebugUIMessages.getString("ConsoleView.Copy_7"), DebugUIMessages.getString("ConsoleView.Copy_8")); //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
		setGlobalAction(actionBars, ITextEditorActionConstants.COPY, textAction);

		//XXX Still using "old" resource access
		ResourceBundle bundle= ResourceBundle.getBundle("org.eclipse.debug.internal.ui.DebugUIMessages"); //$NON-NLS-1$
		setGlobalAction(actionBars, ITextEditorActionConstants.FIND, new FindReplaceAction(bundle, "find_replace_action.", this));	 //$NON-NLS-1$
	
		fSelectionActions.add(ITextEditorActionConstants.COPY);
		updateAction(ITextEditorActionConstants.FIND);
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
		tbm.add(getAction("ShowTypeNames"));
		tbm.add(getAction("ShowQualifiedNames"));
		tbm.add(new Separator("TOGGLE_VIEW")); //$NON-NLS-1$
		tbm.add(getAction("ShowDetailPane"));
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
		menu.add(getAction("AddToInspector"));
		menu.add(getAction("ChangeVariableValue"));
		menu.add(getAction("CopyToClipboard"));
		menu.add(new Separator(IDebugUIConstants.EMPTY_RENDER_GROUP));
		menu.add(new Separator(IDebugUIConstants.RENDER_GROUP));
		menu.add(getAction("ShowTypeNames"));
		menu.add(getAction("ShowQualifiedNames"));
		
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
   /**
	* Adds items to the detail area's context menu including any extension defined
	* actions.
	* 
	* @param menu The menu to add the item to.
	*/
	protected void fillDetailContextMenu(IMenuManager menu) {
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
				IVariable var = (IVariable)selection.getFirstElement();
				IValue val = var.getValue();
				getModelPresentation().computeDetail(val, this);
			} else {
				getDetailDocument().set(""); //$NON-NLS-1$
			}
		} catch (DebugException de) {
			DebugUIPlugin.logError(de);
		}				
	}
	
	/*
	 * @see IValueDetailListener#detailComputed(IValue, String)
	 */
	public void detailComputed(IValue value, final String result) {
		Runnable runnable = new Runnable() {
			public void run() {
				getDetailDocument().set(result);
			}
		};
		getViewer().getControl().getDisplay().asyncExec(runnable);		
	}
	
	/**
	 * Lazily instantiate and return a selection listener that updates the enabled
	 * state of the selection oriented actions in this view.
	 */
	protected ISelectionChangedListener getDetailSelectionChangedListener() {
		if (fDetailSelectionChangedListener == null) {
			fDetailSelectionChangedListener = new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
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
	
	protected ITextViewer getDetailTextViewer() {
		return fDetailTextViewer;
	}
	
	protected SashForm getSashForm() {
		return fSashForm;
	}
	
	/**
	 * @see WorkbenchPart#getAdapter(Class)
	 */
	public Object getAdapter(Class required) {
		if (IFindReplaceTarget.class.equals(required)) {
			return getDetailTextViewer().getFindReplaceTarget();
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
		if (action instanceof IUpdate)
			((IUpdate) action).update();
	}
	
}

