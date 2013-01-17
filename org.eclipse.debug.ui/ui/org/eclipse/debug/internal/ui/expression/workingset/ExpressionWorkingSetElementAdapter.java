/*******************************************************************************
 * Copyright (c) 2012 Tensilica Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Abeer Bagul (Tensilica Inc) - initial API and implementation (Bug 372181)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.expression.workingset;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetElementAdapter;

/**
 * Consulted by workbench pull down actions that add/remove selected elements to/from
 * working sets. Allows expression working sets to select which elements are applicable
 * for adding/removing.
 * 
 * @since 3.9
 */
public class ExpressionWorkingSetElementAdapter implements
		IWorkingSetElementAdapter {

	public IAdaptable[] adaptElements(IWorkingSet ws, IAdaptable[] elements) {
		for (int i = 0; i < elements.length; i++) {
	        IExpression expression = (IExpression)DebugPlugin.getAdapter(elements[i], IExpression.class);			
			if (expression != null) {
				return selectExpressions(elements);
			}
		}
		return elements;
	}
	
	private IAdaptable[] selectExpressions(IAdaptable[] elements) {
		List expressions = new ArrayList(elements.length);
		for (int i = 0; i < elements.length; i++) {
			IExpression expression = (IExpression)DebugPlugin.getAdapter(elements[i], IExpression.class);            
			if (expression != null) {
				expressions.add(expression);
			}
		}
		return (IAdaptable[]) expressions.toArray(new IAdaptable[expressions.size()]);
	}

	public void dispose() {
		
	}

}
