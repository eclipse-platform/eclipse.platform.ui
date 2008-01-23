/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River Systems - integration with non-standard debug models (Bug 209883)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.expressions;

import java.util.Iterator;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.internal.ui.views.variables.IndexedVariablePartition;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.actions.IWatchExpressionFactoryAdapter;
import org.eclipse.debug.ui.actions.IWatchExpressionFactoryAdapter2;
import org.eclipse.debug.ui.actions.IWatchExpressionFactoryAdapterExtension;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

/**
 * 
 */
public class WatchAction implements IViewActionDelegate {

	private IStructuredSelection fSelection;

	public void init(IViewPart view) {
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (fSelection == null) {
			return;
		}
		Iterator iter = fSelection.iterator();
		while (iter.hasNext()) {
			Object element = iter.next();
			createExpression(element);
		}
	}

	private void showExpressionsView() {
		IWorkbenchPage page = DebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IViewPart part = page.findView(IDebugUIConstants.ID_EXPRESSION_VIEW);
		if (part == null) {
			try {
				page.showView(IDebugUIConstants.ID_EXPRESSION_VIEW);
			} catch (PartInitException e) {
			}
		} else {
			page.bringToTop(part);
		}

	}

	private void createExpression(Object element) {
	    String expressionString;
        try {
    		if (element instanceof IVariable) {
    		    IVariable variable = (IVariable)element;
        		IWatchExpressionFactoryAdapter factory = getFactory(variable);
                expressionString = variable.getName();
                if (factory != null) {
                    expressionString = factory.createWatchExpression(variable);
                }
    		} else {
                IWatchExpressionFactoryAdapter2 factory2 = getFactory2(element);
                if (factory2 != null) {
                    expressionString = factory2.createWatchExpression(element);
                } else {
                    // Action should not have been enabled
                    Assert.isTrue(false);
                    return;
                }
    		}
        } catch (CoreException e) {
            DebugUIPlugin.errorDialog(DebugUIPlugin.getShell(), ActionMessages.WatchAction_0, ActionMessages.WatchAction_1, e); // 
            return;
        }
		
        IWatchExpression expression;
            expression = DebugPlugin.getDefault().getExpressionManager().newWatchExpression(expressionString);
		DebugPlugin.getDefault().getExpressionManager().addExpression(expression);
		IAdaptable object = DebugUITools.getDebugContext();
		IDebugElement context = null;
		if (object instanceof IDebugElement) {
			context = (IDebugElement) object;
		} else if (object instanceof ILaunch) {
			context = ((ILaunch) object).getDebugTarget();
		}
		expression.setExpressionContext(context);
		showExpressionsView();
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
        fSelection = null;
        int enabled = 0;
        int size = -1;
        if (selection instanceof IStructuredSelection) {
            fSelection = (IStructuredSelection) selection;
            size = fSelection.size();
            Iterator iterator = fSelection.iterator();
            while (iterator.hasNext()) {
                Object element = iterator.next();
                if (element instanceof IndexedVariablePartition) {
                	break;
                } else if (isFactoryEnabled(element)) {
                    enabled++;
                } else {
                    break;
                }
            }
        }
        action.setEnabled(enabled > 0 && enabled == size);
	}

	/**
	 * Returns whether the factory adapter for the given variable is currently enabled.
	 * 
	 * @param variable
	 * @return whether the factory is enabled
	 */
	private boolean isFactoryEnabled(Object element) {
	    
	    if (element instanceof IVariable) {
	        IVariable variable = (IVariable)element;
            DebugPlugin.getDefault().getExpressionManager().hasWatchExpressionDelegate(variable.getModelIdentifier());
	        
    		IWatchExpressionFactoryAdapter factory = getFactory(variable);
    		if (factory instanceof IWatchExpressionFactoryAdapterExtension) {
    			IWatchExpressionFactoryAdapterExtension ext = (IWatchExpressionFactoryAdapterExtension) factory;
    			return ext.canCreateWatchExpression(variable);
    		}
            return true;
	    } else {
            IWatchExpressionFactoryAdapter2 factory2 = getFactory2(element);
            if (factory2 != null) {
                return factory2.canCreateWatchExpression(element);
            }
	        return false;
	    }
	}

	/**
	 * Returns the factory adapter for the given variable or <code>null</code> if none.
	 * 
	 * @param variable
	 * @return factory or <code>null</code>
	 */
	private IWatchExpressionFactoryAdapter getFactory(IVariable variable) {
		return (IWatchExpressionFactoryAdapter) variable.getAdapter(IWatchExpressionFactoryAdapter.class);		
	}

    /**
     * Returns the factory adapter for the given variable or <code>null</code> if none.
     * 
     * @param variable
     * @return factory or <code>null</code>
     */
    private IWatchExpressionFactoryAdapter2 getFactory2(Object element) {
        if (element instanceof IAdaptable) {
            return (IWatchExpressionFactoryAdapter2)((IAdaptable)element).getAdapter(IWatchExpressionFactoryAdapter2.class);
        }
        return null;
    }
}
