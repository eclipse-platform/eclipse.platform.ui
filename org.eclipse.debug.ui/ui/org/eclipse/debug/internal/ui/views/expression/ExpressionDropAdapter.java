/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 	   Wind River - Pawel Piech - Initial Implementation - Drag/Drop to Expressions View (Bug 184057)
 *     IBM Corporation - further implementation and documentation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.expression;

import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.debug.internal.ui.views.variables.IndexedVariablePartition;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.actions.IWatchExpressionFactoryAdapter;
import org.eclipse.debug.ui.actions.IWatchExpressionFactoryAdapterExtension;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Item;

/**
 * Drop Adapter allowing text and variables to be dragged and dropped into the Expression View.
 * A new watch expression is created.
 * 
 * @see org.eclipse.debug.internal.ui.views.variables.VariablesDragAdapter
 * @since 3.4
 */
public class ExpressionDropAdapter extends ViewerDropAdapter {

	private Item fTarget = null;
	private TransferData fCurrentTransferType = null;

    /**
     * Constructor takes the viewer this drop adapter applies to.
     * @param viewer the viewer to add drop to
     */
    protected ExpressionDropAdapter(TreeModelViewer viewer) {
        super(viewer);
        setFeedbackEnabled(false);
        setSelectionFeedbackEnabled(false);
        setScrollExpandEnabled(false);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerDropAdapter#dragEnter(org.eclipse.swt.dnd.DropTargetEvent)
     */
    public void dragEnter(DropTargetEvent event) {
        super.dragEnter(event);

        for (int i = 0; i < event.dataTypes.length; i++) {
            if (LocalSelectionTransfer.getTransfer().isSupportedType(event.dataTypes[i])) {
                if (validateSelectionDrop()) {
                    event.currentDataType = event.dataTypes[i];
                    event.detail = DND.DROP_COPY; 
                    return;
                }
            }
        }

        for (int i = 0; i < event.dataTypes.length; i++) {
            if (TextTransfer.getInstance().isSupportedType(event.dataTypes[i])) {
                event.currentDataType = event.dataTypes[i];
                event.detail = DND.DROP_COPY; 
                return;
            }
        }

        event.detail = DND.DROP_NONE; 

    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerDropAdapter#drop(org.eclipse.swt.dnd.DropTargetEvent)
     */
    public void drop(DropTargetEvent event) {
        fCurrentTransferType = event.currentDataType;
        super.drop(event);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerDropAdapter#performDrop(java.lang.Object)
     */
    public boolean performDrop(Object data) {
        if (LocalSelectionTransfer.getTransfer().isSupportedType(fCurrentTransferType)) {
            IStructuredSelection selection = (IStructuredSelection) LocalSelectionTransfer.getTransfer().getSelection();
            return performSelectionDrop(selection);
        } else if (TextTransfer.getInstance().isSupportedType(fCurrentTransferType)) {
            if (data != null) {
                createExpression((String)data);
                return true;
            }
        }
        return false;
    }

    /**
     * If the dragged data is a structured selection, get any IVariables in it and create expressions
     * for each of them.
     * 
     * @param selection Structured selection containing IVariables
     * @return whether expression creation was successful
     */
    private boolean performSelectionDrop(IStructuredSelection selection) {
        boolean retVal = false;
        for (Iterator itr = selection.iterator(); itr.hasNext(); ) {
            Object element = itr.next();
            if (element instanceof IVariable) {
                retVal |= createExpression((IVariable)element);
            }
        }        
        return retVal;
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#determineTarget(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	protected Object determineTarget(DropTargetEvent event) {
		fTarget = (Item) event.item;
		return fTarget;
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerDropAdapter#validateDrop(java.lang.Object, int, org.eclipse.swt.dnd.TransferData)
     */
    public boolean validateDrop(Object target, int operation, TransferData transferType) {
        if (LocalSelectionTransfer.getTransfer().isSupportedType(transferType)) {
            return validateSelectionDrop();
        } else if (TextTransfer.getInstance().isSupportedType(transferType)) {
            return true;
        }
        return false;
    }
    
    /**
     * Validates the local selection transfer to ensure that a watch expression can be
     * created for it.
     * @return whether the selection is valid
     */
    private boolean validateSelectionDrop() {
        IStructuredSelection selection = (IStructuredSelection) LocalSelectionTransfer.getTransfer().getSelection();
        int enabled = 0;
        int size = -1;
        if (selection != null) {
            size = selection.size();
            IExpressionManager manager = DebugPlugin.getDefault().getExpressionManager();
            Iterator iterator = selection.iterator();
            while (iterator.hasNext()) {
                Object element = iterator.next();
                if (!(element instanceof IVariable)) {
                    break;
                }
                
                IVariable variable = (IVariable) element;
                if (variable instanceof IndexedVariablePartition) {
                    break;
                } else if (manager.hasWatchExpressionDelegate(variable.getModelIdentifier()) && isFactoryEnabled(variable)) {
                    enabled++;
                } else {
                    break;
                }
            }
        }
        return enabled == size;
    }
    
    /**
     * Creates a new watch expression from an IVariable using the watch expression factory
     * adapter for that variable.
     * 
     * @param variable the variable to use to create the watch expression
     * @return whether the creation was successful
     */
    private boolean createExpression(IVariable variable) {
        IWatchExpressionFactoryAdapter factory = getFactory(variable);
        try {
            String exp = variable.getName();
            if (factory != null) {
                exp = factory.createWatchExpression(variable);
                createExpression(exp);
                return true;
            } else {
            	DebugUIPlugin.log(new Status(IStatus.ERROR,DebugUIPlugin.getUniqueIdentifier(),"Drop failed.  Watch Expression Factory could not be found for variable " + variable)); //$NON-NLS-1$
            }
        } catch (CoreException e) {
        	DebugUIPlugin.log(e.getStatus());
        }
        return false;
    }

    /**
     * Creates a new watch expression from a string using the default expression manager.
     * 
     * @param exp the string to use to create the expression
     */
    private void createExpression(String exp) {
        IWatchExpression expression = DebugPlugin.getDefault().getExpressionManager().newWatchExpression(exp);
        DebugPlugin.getDefault().getExpressionManager().addExpression(expression);
        IAdaptable object = DebugUITools.getDebugContext();
        IDebugElement context = null;
        if (object instanceof IDebugElement) {
            context = (IDebugElement) object;
        } else if (object instanceof ILaunch) {
            context = ((ILaunch) object).getDebugTarget();
        }
        expression.setExpressionContext(context);
    }

    
    /**
     * Returns whether the factory adapter for the given variable is currently enabled.
     * 
     * @param variable the variable to ask for the adapter
     * @return whether the factory is enabled
     */
    private boolean isFactoryEnabled(IVariable variable) {
        IWatchExpressionFactoryAdapter factory = getFactory(variable);
        if (factory instanceof IWatchExpressionFactoryAdapterExtension) {
            IWatchExpressionFactoryAdapterExtension ext = (IWatchExpressionFactoryAdapterExtension) factory;
            return ext.canCreateWatchExpression(variable);
        }
        return false;
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

}
