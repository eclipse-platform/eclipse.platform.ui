/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.expressions;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.actions.IWatchExpressionFactoryAdapter;
import org.eclipse.debug.ui.actions.IWatchExpressionFactoryAdapter2;
import org.eclipse.debug.ui.actions.IWatchExpressionFactoryAdapterExtension;

/**
 * This class is used to check whether a given element can be used to create 
 * a watch expression.  A single property can be tested 
 * "canCreateWatchExpression".  Test for this property ignores the value and 
 * always returns a boolean.
 *
 *	@since 3.4
 */
public class WatchExpressionFactoryTester extends PropertyTester {

	public static final String CAN_CREATE_WATCH_EXPRESSION_PROPERTY = "canCreateWatchExpression"; //$NON-NLS-1$
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.PropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	public boolean test(Object element, String property, Object[] args, Object expectedValue) {
		if (CAN_CREATE_WATCH_EXPRESSION_PROPERTY.equals(property)){
	        if (element instanceof IVariable) {
	            IVariable variable = (IVariable)element;
	            if (DebugPlugin.getDefault().getExpressionManager().hasWatchExpressionDelegate(variable.getModelIdentifier())) {
		            IWatchExpressionFactoryAdapter factory = WatchHandler.getFactory(variable);
		            if (factory instanceof IWatchExpressionFactoryAdapterExtension) {
		                IWatchExpressionFactoryAdapterExtension ext = (IWatchExpressionFactoryAdapterExtension) factory;
		                return ext.canCreateWatchExpression(variable);
		            }
		            return true;
	            }
	        } else {
	            IWatchExpressionFactoryAdapter2 factory2 = WatchHandler.getFactory2(element);
	            if (factory2 != null) {
	                return factory2.canCreateWatchExpression(element);
	            }
	        }
		} 
		return false;
	}
}
