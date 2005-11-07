/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IErrorReportingExpression;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.internal.ui.views.DebugViewInterimLabelProvider;
import org.eclipse.debug.internal.ui.views.DebugViewLabelDecorator;
import org.eclipse.debug.internal.ui.views.RemoteTreeViewer;
import org.eclipse.debug.internal.ui.views.variables.AvailableLogicalStructuresAction;
import org.eclipse.debug.internal.ui.views.variables.RemoteVariablesContentProvider;
import org.eclipse.debug.internal.ui.views.variables.VariablesView;
import org.eclipse.debug.internal.ui.views.variables.VariablesViewEventHandler;
import org.eclipse.debug.internal.ui.views.variables.VariablesViewMessages;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
 
/**
 * Displays expressions and their values with a detail
 * area.
 */
public class ExpressionView extends VariablesView {
	
	/**
	 * A decorating label provider which provides color for expressions.
	 */
	protected class ExpressionViewDecoratingLabelProvider extends VariablesView.VariablesViewDecoratingLabelProvider {
		/**
		 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
		 */
		public Color getForeground(Object element) {
			boolean expressionWithError= false;
			IErrorReportingExpression expression= null;
			if (element instanceof IErrorReportingExpression) {
				expression= (IErrorReportingExpression) element;
			} else if (element instanceof String) {
				Object parent= ((ITreeContentProvider)getVariablesViewer().getContentProvider()).getParent(element);
				if (parent instanceof IErrorReportingExpression) {
					expression= (IErrorReportingExpression) parent;
				}
			}
			if (expression != null && expression.hasErrors()) {
				expressionWithError= true;
			}
			if (expressionWithError) {
				return Display.getDefault().getSystemColor(SWT.COLOR_RED);
			}
			return super.getForeground(element);
		}

		public ExpressionViewDecoratingLabelProvider(StructuredViewer viewer, ILabelProvider provider, DebugViewLabelDecorator decorator) {
			super(viewer, provider, decorator);
		}
	}
	/**
	 * @see org.eclipse.debug.internal.ui.views.variables.VariablesView#createLabelProvider()
	 */
	protected IBaseLabelProvider createLabelProvider(StructuredViewer viewer) {
		return new ExpressionViewDecoratingLabelProvider(viewer, new DebugViewInterimLabelProvider(getModelPresentation()), new DebugViewLabelDecorator(getModelPresentation()));
	}

	/**
	 * Creates this view's content provider.
	 * 
	 * @return a content provider
	 */
	protected RemoteVariablesContentProvider createContentProvider(Viewer viewer) {
		return new RemoteExpressionsContentProvider((RemoteTreeViewer)viewer, getSite(), this);
	}
	
	/**
	 * Creates this view's event handler.
	 * 
	 * @return an event handler
	 */
	protected VariablesViewEventHandler createEventHandler() {
		return new ExpressionViewEventHandler(this);
	}		
	
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
		menu.add(getAction("ChangeVariableValue")); //$NON-NLS-1$
		IAction action = new AvailableLogicalStructuresAction(this);
        if (action.isEnabled()) {
            menu.add(action);
        }
		menu.add(new Separator(IDebugUIConstants.EMPTY_RENDER_GROUP));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	/** 
	 * The <code>ExpressionView</code> listens for selection changes in the <code>LaunchesView</code>
	 * to correctly set the editable state of the details pane. Updates the context of
	 * watch expressions.
	 *
	 * @see ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (!isVisible()) {
			return;
		}
		if (selection instanceof IStructuredSelection) {
			IDebugElement context = null;
			IStructuredSelection ss = (IStructuredSelection)selection;
			if (ss.size() < 2) {
				Object object = ss.getFirstElement();
				if (object instanceof IDebugElement) {
					context= (IDebugElement) object;
				} else if (object instanceof ILaunch) {
					context= ((ILaunch) object).getDebugTarget();
				}
			}
			// update watch expressions with new context
			IExpression[] expressions = DebugPlugin.getDefault().getExpressionManager().getExpressions();
			for (int i = 0; i < expressions.length; i++) {
				IExpression expression = expressions[i];
				if (expression instanceof IWatchExpression) {
					((IWatchExpression)expression).setExpressionContext(context);
				}
			}			
		} 

		// update actions
		updateAction("ContentAssist"); //$NON-NLS-1$
	}
	
	/**
	 * Do nothing - the input to this view never changes - 
	 * it is always the expression manager.
	 * 
	 * @see VariablesView#setViewerInput(IStructuredSelection)
	 */
	protected void setViewerInput(IStructuredSelection ssel) {
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
    }
    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewPart#saveState(org.eclipse.ui.IMemento)
     */
    public void saveState(IMemento memento) {
    }
}
