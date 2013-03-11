/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River - Pawel Piech - Drag/Drop to Expressions View (Bug 184057)
 *     Wind River - Pawel Piech - Fix viewer input race condition (Bug 234908)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.expression;

 
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.actions.expressions.EditWatchExpressinInPlaceAction;
import org.eclipse.debug.internal.ui.actions.expressions.PasteWatchExpressionsAction;
import org.eclipse.debug.internal.ui.actions.variables.ChangeVariableValueAction;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.internal.ui.viewers.model.ViewerAdapterService;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ITreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerInputUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.debug.internal.ui.views.variables.AvailableLogicalStructuresAction;
import org.eclipse.debug.internal.ui.views.variables.SelectionDragAdapter;
import org.eclipse.debug.internal.ui.views.variables.VariablesView;
import org.eclipse.debug.internal.ui.views.variables.VariablesViewMessages;
import org.eclipse.debug.internal.ui.views.variables.details.AvailableDetailPanesAction;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.actions.ActionFactory;
 
/**
 * Displays expressions and their values with a detail
 * pane.
 */
public class ExpressionView extends VariablesView {

	// The preference name for autoselect working sets toggle
	private static final String PREF_WORKINGSETS_AUTOSELECT = DebugUIPlugin.getUniqueIdentifier() + ".workingSetAutoselect"; //$NON-NLS-1$

	// The preference name for saving fWorkingSets
	private static final String PREF_ELEMENT_WORKINGSET = DebugUIPlugin.getUniqueIdentifier() + ".workingSet"; //$NON-NLS-1$

	// The preference name for saving fWorkingSetMementos.
	private static final String PREF_ELEMENT_WORKINGSET_MEMENTOS = DebugUIPlugin.getUniqueIdentifier() + ".workingSetMementos"; //$NON-NLS-1$

	// Limit on the number of entries in the working sets / selection map.
	private static final int MAX_WORKING_SETS_MEMENTOS = 100;
	
	private static final IWorkingSet[] EMPTY_WORKING_SETS = new IWorkingSet[0];
	
    private PasteWatchExpressionsAction fPasteAction;
    private EditWatchExpressinInPlaceAction fEditInPlaceAction;
    
    private IWorkingSet[] fWorkingSets;
    
	private boolean fAutoSelectnWorkingSets = true;

	private Map fWorkingSetMementos = new LinkedHashMap(16, (float)0.75, true) {
		private static final long serialVersionUID = 1L;

		protected boolean removeEldestEntry(java.util.Map.Entry eldest) {
			return size() > MAX_WORKING_SETS_MEMENTOS;
		}
	};
    
	private Set fPendingCompareRequests;
	
	private ExpressionElementMementoRequest fPendingMementoRequest;
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.variables.VariablesView#getHelpContextId()
	 */
	protected String getHelpContextId() {
		return IDebugHelpContextIds.EXPRESSION_VIEW;		
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.variables.VariablesView#configureToolBar(org.eclipse.jface.action.IToolBarManager)
	 */
	protected void configureToolBar(IToolBarManager tbm) {
		super.configureToolBar(tbm);
		tbm.add(new Separator(IDebugUIConstants.EMPTY_EXPRESSION_GROUP));		
		tbm.add(new Separator(IDebugUIConstants.EXPRESSION_GROUP));
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.variables.VariablesView#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	protected void fillContextMenu(IMenuManager menu) {
		menu.add(new Separator(IDebugUIConstants.EMPTY_EXPRESSION_GROUP));
		menu.add(new Separator(IDebugUIConstants.EXPRESSION_GROUP));
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
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.variables.VariablesView#contextActivated(org.eclipse.jface.viewers.ISelection)
	 */
	protected void contextActivated(ISelection selection) {
		if (!isAvailable() || !isVisible()) {
			return;
		}
		if (selection == null || selection.isEmpty()) {
            super.contextActivated(new StructuredSelection(DebugPlugin.getDefault().getExpressionManager()));
		} else {
			super.contextActivated(selection);
			if (fAutoSelectnWorkingSets) {
		        Object element = ((IStructuredSelection)selection).getFirstElement();
		        compareElementMementos(element);
			}
		}
        if (isAvailable() && isVisible()) {
            updateAction("ContentAssist"); //$NON-NLS-1$
        }
	}

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.views.variables.VariablesView#viewerInputUpdateComplete(IViewerInputUpdate)
     */
	protected void viewerInputUpdateComplete(IViewerInputUpdate update) {
        IStatus status = update.getStatus();
        if ( (status == null || status.isOK()) && update.getElement() != null) {
            setViewerInput(update.getInputElement());
        } else {
            setViewerInput(DebugPlugin.getDefault().getExpressionManager());
        }
        updateAction(FIND_ACTION);
	}
	
	public void setAutoSelectWoringSets(boolean autoSelect) {
		if (fAutoSelectnWorkingSets != autoSelect) {
			fAutoSelectnWorkingSets = autoSelect;
		}
	}
	
	public boolean isAutoSelectWorkingSets() {
		return fAutoSelectnWorkingSets;
	}
	
	public void elementCompareComplete(String[] workingSetNames) {
		IWorkingSetManager mgr = PlatformUI.getWorkbench().getWorkingSetManager();
		List workingSetList = new ArrayList();
		for (int j = 0; j < workingSetNames.length; j++) {
			IWorkingSet workingSet = mgr.getWorkingSet(workingSetNames[j]);
			if (workingSet != null) {
				workingSetList.add(workingSet);
			}
		}
		doApplyWorkingSets((IWorkingSet[])workingSetList.toArray(new IWorkingSet[workingSetList.size()]));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.variables.VariablesView#getDetailPanePreferenceKey()
	 */
	protected String getDetailPanePreferenceKey() {
		return IDebugPreferenceConstants.EXPRESSIONS_DETAIL_PANE_ORIENTATION;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.variables.VariablesView#getToggleActionLabel()
	 */
	protected String getToggleActionLabel() {
		return VariablesViewMessages.ExpressionView_4; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.variables.VariablesView#getPresentationContextId()
	 */
	protected String getPresentationContextId() {
		return IDebugUIConstants.ID_EXPRESSION_VIEW;
	}	
	
    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.views.variables.VariablesView#initDragAndDrop(org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer)
     */
    protected void initDragAndDrop(TreeModelViewer viewer) {
        viewer.addDragSupport(DND.DROP_MOVE, new Transfer[] {LocalSelectionTransfer.getTransfer()}, new SelectionDragAdapter(viewer));
        viewer.addDropSupport(DND.DROP_MOVE|DND.DROP_COPY, new Transfer[] {LocalSelectionTransfer.getTransfer(), TextTransfer.getInstance()}, new ExpressionDropAdapter(getSite(), viewer));
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.views.variables.VariablesView#createActions()
     */
    protected void createActions() {
    	super.createActions();
    	fPasteAction = new PasteWatchExpressionsAction(this);
    	configure(fPasteAction, IWorkbenchCommandConstants.EDIT_PASTE, PASTE_ACTION, ISharedImages.IMG_TOOL_PASTE);
    	fEditInPlaceAction = new EditWatchExpressinInPlaceAction(this);
        configure(fEditInPlaceAction, IWorkbenchCommandConstants.FILE_RENAME, ActionFactory.RENAME.getId(), null);
    }
    
    public void dispose() {
        fEditInPlaceAction.dispose();
        cancelPendingCompareRequests();
        if (fPendingMementoRequest != null) fPendingMementoRequest.cancel();
        super.dispose();
    }
    
    /**
     * Configures the action to override the global action, and registers the
     * action with this view.
     * 
     * @param action
     * 		action
     * @param defId
     * 		action definition id
     * @param globalId
     * 		global action id
     * @param imgId
     * 		image identifier
     */
    private void configure(IAction action, String defId, String globalId,
    		String imgId) {
    	setAction(globalId, action);
    	action.setActionDefinitionId(defId);
    	setGlobalAction(globalId, action);
    	if (imgId != null) {
    	    action.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(imgId));
    	}
    }
    
    /**
     * Returns whether the given selection can be pasted into the expressions
     * view.
     * 
     * @return whether the given selection can be pasted into the given target
     */
    public boolean canPaste() {
    	String clipboardText = getClipboardText();
    	if (clipboardText != null && clipboardText.length() > 0) {
    		return true;
    	}
    	return false;
    }
       
    /**
     * Pastes the selection into the given target
     * 
     * @return whether successful
     */
    public boolean performPaste() {
    	String clipboardText = getClipboardText();
    	if (clipboardText != null && clipboardText.length() > 0) {
    		IExpressionManager expressionManager = DebugPlugin.getDefault().getExpressionManager();
    		IWatchExpression watchExpression = expressionManager
    				.newWatchExpression(clipboardText);
    		expressionManager.addExpression(watchExpression);
    		watchExpression.setExpressionContext(getContext());
    		return true;
    	}
    	return false;
    }
    
    // TODO: duplicate code from WatchExpressionAction
    protected IDebugElement getContext() {
        IAdaptable object = DebugUITools.getPartDebugContext(getSite());
        IDebugElement context = null;
        if (object instanceof IDebugElement) {
            context = (IDebugElement) object;
        } else if (object instanceof ILaunch) {
            context = ((ILaunch) object).getDebugTarget();
        }
        return context;
    }
    
    protected String getClipboardText() {
    	Clipboard clipboard = new Clipboard(Display.getDefault());
    	try {
    		TextTransfer textTransfer = TextTransfer.getInstance();
    		return (String) clipboard.getContents(textTransfer);
    	} finally {
    		clipboard.dispose();
    	}
    }

    public Viewer createViewer(Composite parent) {
    	TreeModelViewer viewer = (TreeModelViewer)super.createViewer(parent);

    	initWorkingSets();
    	initWorkingSetMementos();
		getWorkingSetFilter(viewer).setSelectedWorkingSets(fWorkingSets);
		updateWorkingSetsProperty(viewer.getPresentationContext());    	

    	return viewer;
    }

    private String[] loadWorkingSetNames(IMemento memento) {
		IMemento[] workingsets = memento.getChildren(PREF_ELEMENT_WORKINGSET);
		if (workingsets != null) {
			List list = new ArrayList();
			for (int j=0; j<workingsets.length; j++) {
				list.add(workingsets[j].getID());
			}
			return (String[])list.toArray(new String[list.size()]);
		} 
		return new String[0];
    }

    private void initWorkingSets() {
    	String[] workingSetNames = loadWorkingSetNames(getMemento());
		List list = new ArrayList();
		for (int j = 0; j < workingSetNames.length; j++) {
			IWorkingSet workingsetObject = PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSet(workingSetNames[j]);
			if (workingsetObject != null) {
				list.add(workingsetObject);
			}
		}
		fWorkingSets = (IWorkingSet[]) list.toArray(new IWorkingSet[list.size()]);
    }

    private void initWorkingSetMementos() {
		IMemento[] workingsetMementos = getMemento().getChildren(PREF_ELEMENT_WORKINGSET_MEMENTOS);
		if (workingsetMementos != null) {
			for (int j=0; j<workingsetMementos.length; j++) {
				String[] workingSetNames = loadWorkingSetNames(workingsetMementos[j]);
				String string = workingsetMementos[j].getID();
		        if(string.length() > 0 && workingSetNames != null) {
		            ByteArrayInputStream bin = new ByteArrayInputStream(string.getBytes());
		            InputStreamReader reader = new InputStreamReader(bin);
		            try {
		                XMLMemento workingSetsKey = XMLMemento.createReadRoot(reader);
		                fWorkingSetMementos.put(workingSetsKey, workingSetNames);
		            } catch (WorkbenchException e) {
		            } finally {
		                try {
		                    reader.close();
		                    bin.close();
		                } catch (IOException e){}
		            }
		        }
			}
		}
    }

    public void saveViewerState(IMemento memento) {
    	super.saveViewerState(memento);

    	memento.putBoolean(PREF_WORKINGSETS_AUTOSELECT, fAutoSelectnWorkingSets);
    	if (!fAutoSelectnWorkingSets) {
    		saveWorkingSets(memento, getWorkingSetNames());
    	}
    	saveWorkingSetMementos(memento);
    }

    private void saveWorkingSets(IMemento memento, String[] workingSetNames) {
		for (int i=0; i<workingSetNames.length; i++) {
			memento.createChild(PREF_ELEMENT_WORKINGSET, workingSetNames[i]);
		}    	
    }
    
    private void saveWorkingSetMementos(IMemento memento) {
    	for (Iterator itr = fWorkingSetMementos.entrySet().iterator(); itr.hasNext();) {
    		Map.Entry entry = (Map.Entry)itr.next();
    		String keyMementoString = getMenentoString((XMLMemento)entry.getKey());
            IMemento workingSetsForElementMemento = memento.createChild(PREF_ELEMENT_WORKINGSET_MEMENTOS, keyMementoString);
            saveWorkingSets(workingSetsForElementMemento, (String[])entry.getValue());
    	}
    }
    
    private String getMenentoString(XMLMemento memento) {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		OutputStreamWriter writer = new OutputStreamWriter(bout);
	
		try {
			memento.save(writer);
			return bout.toString();
		} catch (IOException e) {
		} finally {
			try {
				writer.close();
				bout.close();
			} catch (IOException e) {
			}
		}
		return null;
    }
    
    public void applyWorkingSets(IWorkingSet[] selectedWorkingSets) {
    	doApplyWorkingSets(selectedWorkingSets);
		saveWorkingSetsForInput();
    }

    private void doApplyWorkingSets(IWorkingSet[] selectedWorkingSets) {
    	fWorkingSets = selectedWorkingSets;
    	TreeModelViewer viewer = (TreeModelViewer)getViewer(); 
		getWorkingSetFilter(viewer).setSelectedWorkingSets(fWorkingSets);
		updateWorkingSetsProperty(viewer.getPresentationContext());
		getViewer().refresh();    	
    }
    
    private void saveWorkingSetsForInput() {
    	if (fPendingMementoRequest != null) {
    		fPendingMementoRequest.cancel();
    	}
    	Object element = getDebugContextElement();
		IElementMementoProvider provider = ViewerAdapterService.getMementoProvider(element);
		if (provider == null) return;
		
        XMLMemento expressionMemento = XMLMemento.createWriteRoot("EXPRESSION_WORKING_SETS_MEMENTO"); //$NON-NLS-1$
        fPendingMementoRequest = new ExpressionElementMementoRequest(
        		this, getPresentationContext(), getDebugContextElement(), expressionMemento, getWorkingSetNames());
        provider.encodeElements(new IElementMementoRequest[] { fPendingMementoRequest });
    }
    
    void mementoRequestFinished(ExpressionElementMementoRequest request) {
		if (!request.isCanceled()) {
			fWorkingSetMementos.put(request.getMemento(), request.getWorkingSets());
		}    	
    }
    
    private String[] getWorkingSetNames() {
    	String[] names = new String[fWorkingSets.length];
    	for (int i = 0; i < fWorkingSets.length; i++) {
    		names[i] = fWorkingSets[i].getName();
    	}
    	return names;
    }
    
    private Object getDebugContextElement() {
    	ISelection selection = getDebugContext();
    	if (selection == null || selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
    		return DebugPlugin.getDefault().getExpressionManager();
    	}
    	return ((IStructuredSelection)selection).getFirstElement();
    }

    private void updateWorkingSetsProperty(IPresentationContext presentationContext) {
		if (fWorkingSets.length > 0) {
			String[] workingSetNames = new String[fWorkingSets.length];
			for (int j=0; j<fWorkingSets.length; j++) {
				workingSetNames[j] = fWorkingSets[j].getName();
			}		
			presentationContext.setProperty(IDebugUIConstants.PROP_EXPRESSIONS_WORKING_SETS, workingSetNames);
		} else {
			presentationContext.setProperty(IDebugUIConstants.PROP_EXPRESSIONS_WORKING_SETS, new String[0]);
		}
    }
    
    public IWorkingSet[] getWorkingSets() {
    	return fWorkingSets;
    }
    
	private ExpressionWorkingSetFilter getWorkingSetFilter(ITreeModelViewer viewer) {
        ViewerFilter[] existingFilters = viewer.getFilters();
        for (int i=0; i<existingFilters.length; i++) {
        	ViewerFilter existingFilter = existingFilters[i];
        	if (existingFilter instanceof ExpressionWorkingSetFilter) {
        		return (ExpressionWorkingSetFilter) existingFilter;
        	}
        }
        
        ExpressionWorkingSetFilter workingSetFilter = new ExpressionWorkingSetFilter();
        viewer.addFilter(workingSetFilter);
        return workingSetFilter;
	}

	private void compareElementMementos(Object source) {
		IElementMementoProvider provdier = ViewerAdapterService.getMementoProvider(source);
		if (provdier != null) {
	        Set requests = new HashSet(fWorkingSetMementos.size() * 4/3);
	        for (Iterator itr = fWorkingSetMementos.entrySet().iterator(); itr.hasNext();) {
	        	Map.Entry entry = (Map.Entry)itr.next();
	        	requests.add( new  ExpressionElementCompareRequest(
	        			this, getPresentationContext(), source, (IMemento)entry.getKey(), (String[])entry.getValue()) );
	        }
	
			// cancel any pending update
			cancelPendingCompareRequests();
			if (!requests.isEmpty()) {
				fPendingCompareRequests = requests;
				provdier.compareElements((IElementCompareRequest[])
						fPendingCompareRequests.toArray(new IElementCompareRequest[fPendingCompareRequests.size()]) );
			}
		} else {
			doApplyWorkingSets(EMPTY_WORKING_SETS);
		}
	}
	
	void compareRequestFinished(final ExpressionElementCompareRequest request) {
		if (fPendingCompareRequests != null && fPendingCompareRequests.remove(request)) {
			if (!request.isCanceled() && request.isEqual()) {
				elementCompareComplete(request.getWorkingSets());
				cancelPendingCompareRequests();
			} else if (fPendingCompareRequests.isEmpty()) {
				elementCompareComplete(new String[0]);
				fPendingCompareRequests = null;
			}
		}
	}
	
	private void cancelPendingCompareRequests() {
		if (fPendingCompareRequests == null) return;
        for (Iterator itr = fPendingCompareRequests.iterator(); itr.hasNext();) {
        	((IElementCompareRequest)itr.next()).cancel();
        }
		fPendingCompareRequests = null;
	}
	
}
