/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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
import org.eclipse.debug.core.IExpressionsListener;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext;


public class ExpressionManagerModelProxy extends AbstractModelProxy implements IExpressionsListener {
        
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.EventHandlerModelProxy#init(org.eclipse.debug.internal.ui.viewers.IPresentationContext)
	 */
	public void init(IPresentationContext context) {
		super.init(context);
		DebugPlugin.getDefault().getExpressionManager().addExpressionListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.EventHandlerModelProxy#dispose()
	 */
	public synchronized void dispose() {
		super.dispose();
		DebugPlugin.getDefault().getExpressionManager().removeExpressionListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IExpressionsListener#expressionsAdded(org.eclipse.debug.core.model.IExpression[])
	 */
	public void expressionsAdded(IExpression[] expressions) {
		updateExpressions(expressions, IModelDelta.ADDED);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IExpressionsListener#expressionsRemoved(org.eclipse.debug.core.model.IExpression[])
	 */
	public void expressionsRemoved(IExpression[] expressions) {
		updateExpressions(expressions, IModelDelta.REMOVED);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IExpressionsListener#expressionsChanged(org.eclipse.debug.core.model.IExpression[])
	 */
	public void expressionsChanged(IExpression[] expressions) {
		updateExpressions(expressions, IModelDelta.CHANGED | IModelDelta.CONTENT | IModelDelta.STATE);		
	}
    
    private void updateExpressions(IExpression[] expressions, int flags) {
		ModelDelta delta = new ModelDelta(DebugPlugin.getDefault() .getExpressionManager(), IModelDelta.NOCHANGE);
		for (int i = 0; i < expressions.length; i++) {
			IExpression expression = expressions[i];
			delta.addNode(expression, flags);
		}
		fireModelChanged(delta);
    }

}
