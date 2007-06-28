/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.viewers.update;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.internal.core.IExpressionsListener2;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy;
import org.eclipse.jface.viewers.Viewer;


/**
 * Model proxy that fires model delta updates for the ExpressionManager.
 * 
 * @see org.eclipse.debug.internal.core.ExpressionManager
 */
public class ExpressionManagerModelProxy extends AbstractModelProxy implements IExpressionsListener2 {
        
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.EventHandlerModelProxy#init(org.eclipse.debug.internal.ui.viewers.IPresentationContext)
	 */
	public void init(IPresentationContext context) {
		super.init(context);
		getExpressionManager().addExpressionListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy#installed(org.eclipse.jface.viewers.Viewer)
	 */
	public void installed(Viewer viewer) {
		updateExpressions(getExpressionManager().getExpressions(), IModelDelta.INSTALL);
	}

	/**
	 * @return the default expression manager from the debug plugin
	 */
	protected IExpressionManager getExpressionManager() {
		return DebugPlugin.getDefault().getExpressionManager();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.EventHandlerModelProxy#dispose()
	 */
	public synchronized void dispose() {
		super.dispose();
		getExpressionManager().removeExpressionListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.IExpressionsListener2#expressionsMoved(org.eclipse.debug.core.model.IExpression[], int)
	 */
	public void expressionsMoved(IExpression[] expressions, int index){
		ModelDelta delta = new ModelDelta(getExpressionManager(), IModelDelta.NO_CHANGE);
		for (int i = 0; i < expressions.length; i++) {
			IExpression expression = expressions[i];
			delta.addNode(expression, IModelDelta.REMOVED);
		}
		for (int i = 0; i < expressions.length; i++) {
			IExpression expression = expressions[i];
			delta.addNode(expression, index+i, IModelDelta.INSERTED);
		}
		fireModelChanged(delta);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.IExpressionsListener2#expressionsInserted(org.eclipse.debug.core.model.IExpression[], int)
	 */
	public void expressionsInserted(IExpression[] expressions, int index){
		ModelDelta delta = new ModelDelta(getExpressionManager(), IModelDelta.NO_CHANGE);
		for (int i = 0; i < expressions.length; i++) {
			IExpression expression = expressions[i];
			delta.addNode(expression, index+i, IModelDelta.INSERTED | IModelDelta.INSTALL);
		}
		fireModelChanged(delta);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IExpressionsListener#expressionsAdded(org.eclipse.debug.core.model.IExpression[])
	 */
	public void expressionsAdded(IExpression[] expressions) {
		updateExpressions(expressions, IModelDelta.ADDED | IModelDelta.INSTALL);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IExpressionsListener#expressionsRemoved(org.eclipse.debug.core.model.IExpression[])
	 */
	public void expressionsRemoved(IExpression[] expressions) {
		updateExpressions(expressions, IModelDelta.REMOVED | IModelDelta.UNINSTALL);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IExpressionsListener#expressionsChanged(org.eclipse.debug.core.model.IExpression[])
	 */
	public void expressionsChanged(IExpression[] expressions) {
		updateExpressions(expressions, IModelDelta.CONTENT | IModelDelta.STATE);		
	}
    
    private void updateExpressions(IExpression[] expressions, int flags) {
		ModelDelta delta = new ModelDelta(getExpressionManager(), IModelDelta.NO_CHANGE);
		for (int i = 0; i < expressions.length; i++) {
			IExpression expression = expressions[i];
			delta.addNode(expression, flags);
		}
		fireModelChanged(delta);
    }

}
