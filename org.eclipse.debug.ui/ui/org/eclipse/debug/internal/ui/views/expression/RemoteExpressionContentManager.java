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

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IErrorReportingExpression;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.elements.adapters.DeferredExpressionLogicalStructure;
import org.eclipse.debug.internal.ui.views.RemoteTreeViewer;
import org.eclipse.debug.internal.ui.views.variables.RemoteVariableContentManager;
import org.eclipse.debug.internal.ui.views.variables.VariablesView;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;

/**
 * Remote content manager for variables. Creates an appropriate adapter for
 * logical structures.
 */
public class RemoteExpressionContentManager extends RemoteVariableContentManager {

    private IDeferredWorkbenchAdapter fExpressionLogicalStructureAdapter = new DeferredExpressionLogicalStructure();
    
    /**
     * Constructs a remote content manager for a variables view.
     */
    public RemoteExpressionContentManager(ITreeContentProvider provider, RemoteTreeViewer viewer, IWorkbenchPartSite site, VariablesView view) {
        super(provider, viewer, site, view);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.progress.DeferredTreeContentManager#getAdapter(java.lang.Object)
     */
    protected IDeferredWorkbenchAdapter getAdapter(Object element) {
        if (element instanceof IExpression && fView !=null && fView.isShowLogicalStructure()) {
            return fExpressionLogicalStructureAdapter;
        }
        return super.getAdapter(element);
    }

    public boolean mayHaveChildren(Object element) {
        if (element instanceof IErrorReportingExpression) {
            IErrorReportingExpression iere = (IErrorReportingExpression) element;
            if (iere.hasErrors()) {
                //errors are displayed as children of the expression
                return true;
            }
        }
        
        if (element instanceof IExpression) {
            IExpression expression = (IExpression) element;
            IValue value = expression.getValue();
            if (value != null) {
                try {
                    IVariable[] variables = value.getVariables();
                    if (variables.length > 0) {
                        //definitely children...
                        return true;
                    }
                    
                    //returning false because value!=null && variables.length=0 means no children
                    return false;
                } catch (DebugException e) {
                }
            }
        }
        
        //expression has not been evaluated
        return super.mayHaveChildren(element);
    }
    
    
    
}
