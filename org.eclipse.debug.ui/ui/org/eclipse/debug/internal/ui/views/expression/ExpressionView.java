/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.expression;

 
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.internal.ui.views.AbstractViewerState;
import org.eclipse.debug.internal.ui.views.variables.AvailableLogicalStructuresAction;
import org.eclipse.debug.internal.ui.views.variables.VariablesView;
import org.eclipse.debug.internal.ui.views.variables.VariablesViewMessages;
import org.eclipse.debug.internal.ui.views.variables.VariablesViewer;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchActionConstants;
 
/**
 * Displays expressions and their values with a detail
 * area.
 */
public class ExpressionView extends VariablesView {
	
	private AbstractViewerState fState;
	
	/**
	 * @see AbstractDebugView#getHelpContextId()
	 */
	protected String getHelpContextId() {
		return IDebugHelpContextIds.EXPRESSION_VIEW;		
	}	
	
	/**
	 * Initializes the viewer input on creation
	 */
	protected void setInitialContent() {
		getViewer().setInput(DebugPlugin.getDefault().getExpressionManager());
	}	
	
	/**
	 * Configures the toolBar.
	 * 
	 * @param tbm The toolbar that will be configured
	 */
	protected void configureToolBar(IToolBarManager tbm) {
		super.configureToolBar(tbm);
		tbm.add(new Separator(IDebugUIConstants.EMPTY_EXPRESSION_GROUP));		
		tbm.add(new Separator(IDebugUIConstants.EXPRESSION_GROUP));
	}	
	
   /**
	* Adds items to the tree viewer's context menu including any extension defined
	* actions.
	* 
	* @param menu The menu to add the item to.
	*/
	protected void fillContextMenu(IMenuManager menu) {

		menu.add(new Separator(IDebugUIConstants.EMPTY_EXPRESSION_GROUP));
		menu.add(new Separator(IDebugUIConstants.EXPRESSION_GROUP));
		menu.add(getAction(FIND_ELEMENT));
		menu.add(getAction("ChangeVariableValue")); //$NON-NLS-1$
		IAction action = new AvailableLogicalStructuresAction(this);
        if (action.isEnabled()) {
            menu.add(action);
        }
		menu.add(new Separator(IDebugUIConstants.EMPTY_RENDER_GROUP));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	protected void contextActivated(ISelection selection) {
		if (!isVisible()) {
			return;
		}
		// update actions
		updateAction("ContentAssist"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.variables.VariablesView#treeSelectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	protected void treeSelectionChanged(SelectionChangedEvent event) {
		super.treeSelectionChanged(event);
		ISelection selection = event.getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ssel= (IStructuredSelection)selection;
			if (ssel.size() == 1) {
				Object input= ssel.getFirstElement();
				if (input instanceof IDebugElement) {
					getDetailViewer().setEditable(true);
					return;
				} 
			}
		}
		getDetailViewer().setEditable(false);
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
	 * @see org.eclipse.debug.ui.AbstractDebugView#createActions()
	 */
	protected void createActions() {
		super.createActions();
		setInitialContent();
	}

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.views.variables.VariablesView#restoreState()
     */
    protected void restoreState() {
    	if (fState != null) {
    		fState.restoreState(getVariablesViewer());
    	}
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.variables.VariablesView#dispose()
	 */
	public void dispose() {
		super.dispose();
		fState = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.variables.VariablesView#becomesHidden()
	 */
	protected void becomesHidden() {
		fState = getViewerState();
		super.becomesHidden();
		getViewer().setInput(null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.variables.VariablesView#becomesVisible()
	 */
	protected void becomesVisible() {
		super.becomesVisible();
		setInitialContent();
		restoreState();
	}
    
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.variables.VariablesView#createVariablesViewer(org.eclipse.swt.widgets.Composite)
	 */
	protected VariablesViewer createVariablesViewer(Composite parent) {
		return new VariablesViewer(parent, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.VIRTUAL, this);
	}	
    
}
