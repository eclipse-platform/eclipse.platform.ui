/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
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
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy;

/**
 * Event handler for an expression.
 * 
 * @since 3.2
 *
 */
public class ExpressionEventHandler extends DebugEventHandler {

    public ExpressionEventHandler(AbstractModelProxy proxy) {
        super(proxy);
    }

    protected boolean handlesEvent(DebugEvent event) {
        return event.getKind() == DebugEvent.CHANGE;
    }

    protected void handleChange(DebugEvent event) {
    	ModelDelta delta = new ModelDelta(DebugPlugin.getDefault().getExpressionManager(), IModelDelta.NO_CHANGE);
		IExpression expression = null;
    	if (event.getSource() instanceof IExpression) {
    		expression = (IExpression) event.getSource();
    		int flags = IModelDelta.NO_CHANGE;
    		if ((event.getDetail() & DebugEvent.STATE) != 0) {
    			flags = flags | IModelDelta.STATE;
    		}
    		if ((event.getDetail() & DebugEvent.CONTENT) != 0) {
    			flags = flags | IModelDelta.CONTENT;
    		} 
	    	delta.addNode(expression, flags);
			fireDelta(delta);
		}
    }

    protected void refreshRoot(DebugEvent event) {
        ModelDelta delta = new ModelDelta(DebugPlugin.getDefault().getExpressionManager(), IModelDelta.CONTENT);
        fireDelta(delta);
    }
    
}
