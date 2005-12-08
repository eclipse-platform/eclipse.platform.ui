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

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.internal.ui.viewers.IModelDeltaNode;
import org.eclipse.debug.internal.ui.viewers.IModelProxy;

/**
 * Event handler for an expression.
 * 
 * @since 3.2
 *
 */
public class ExpressionEventHandler extends DebugEventHandler {

    public ExpressionEventHandler(IModelProxy proxy) {
        super(proxy);
    }

    protected boolean handlesEvent(DebugEvent event) {
        return event.getKind() == DebugEvent.CHANGE;
    }

    protected void handleChange(DebugEvent event) {
    	ModelDeltaNode delta = new ModelDeltaNode(DebugPlugin.getDefault().getExpressionManager(), IModelDeltaNode.NOCHANGE);
		IExpression expression = null;
    	if (event.getSource() instanceof IExpression) {
    		expression = (IExpression) event.getSource();
		} else {
			IModelProxy modelProxy = getModelProxy();
			if (modelProxy instanceof DefaultExpressionModelProxy) {
				DefaultExpressionModelProxy proxy = (DefaultExpressionModelProxy) modelProxy;
				expression = proxy.getExpression();
			}
		}
    	if (expression != null) {
	    	delta.addNode(expression, IModelDeltaNode.CHANGED | IModelDeltaNode.CONTENT | IModelDeltaNode.STATE);
			fireDelta(delta);
    	}
    }

    protected void refreshRoot(DebugEvent event) {
        ModelDeltaNode delta = new ModelDeltaNode(DebugPlugin.getDefault().getExpressionManager(), IModelDeltaNode.CHANGED | IModelDeltaNode.CONTENT);
        fireDelta(delta);
    }
    
}
