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

 
import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
 
/**
 * Displays expressions and their values with a detail
 * pane.
 */
public class ExpressionView extends VariablesView {
	
	/**
	 * the preference name for the view part of the sash form
	 * @since 3.9
	 */
	private static final String PREF_ELEMENT_WORKINGSET = DebugUIPlugin.getUniqueIdentifier() + ".workingSet"; //$NON-NLS-1$

	
    private PasteWatchExpressionsAction fPasteAction;
    private EditWatchExpressinInPlaceAction fEditInPlaceAction;
    
    private IWorkingSet[] fWorkingSets;
    
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
    	
		List list = new ArrayList();
		IMemento[] workingsetMementos = getMemento().getChildren(PREF_ELEMENT_WORKINGSET);
		for (int j=0; j<workingsetMementos.length; j++) {
			IMemento workingSetMemento = workingsetMementos[j];
			String workingSetName = workingSetMemento.getID();
			IWorkingSet workingSet = PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSet(workingSetName);
			if (workingSet != null)
				list.add(workingSet);
		}
    	fWorkingSets = (IWorkingSet[]) list.toArray(new IWorkingSet[list.size()]);
		getWorkingSetFilter(viewer).setSelectedWorkingSets(fWorkingSets);
		updateWorkingSetsProperty(viewer.getPresentationContext());

    	return viewer;
    }
    
    public void saveViewerState(IMemento memento) {
    	super.saveViewerState(memento);
    	
    	
		for (int i=0; i<fWorkingSets.length; i++) {
			memento.createChild(PREF_ELEMENT_WORKINGSET, fWorkingSets[i].getName());
		}
    }

    public void applyWorkingSets(IWorkingSet[] selectedWorkingSets) {
    	fWorkingSets = selectedWorkingSets;
    	TreeModelViewer viewer = (TreeModelViewer)getViewer(); 
		getWorkingSetFilter(viewer).setSelectedWorkingSets(fWorkingSets);
		updateWorkingSetsProperty(viewer.getPresentationContext());
		getViewer().refresh();
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

}
