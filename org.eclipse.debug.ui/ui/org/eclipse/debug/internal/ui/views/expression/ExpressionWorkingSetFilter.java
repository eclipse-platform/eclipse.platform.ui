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
package org.eclipse.debug.internal.ui.views.expression;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ITreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewerFilter;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IWorkingSet;

/**
 * Holds a list of working sets selected for an instance of Expression view,
 * and filters in only those expressions which are part of the selected working sets.
 * If no working sets are selected for a view, it filters in all expressions.
 * 
 * This filter always allows the "Add new expression" node.
 * @since 3.9
 */
public class ExpressionWorkingSetFilter extends TreeModelViewerFilter {
	
	private IWorkingSet[] selectedWorkingSets;

	public IWorkingSet[] getSelectedWorkingSets() {
		return selectedWorkingSets;
	}

	public void setSelectedWorkingSets(IWorkingSet[] selectedWorkingSets) {
		this.selectedWorkingSets = selectedWorkingSets;
	}

	public boolean select(Viewer viewer, Object parentElement, Object element) {
		
		if (selectedWorkingSets == null || selectedWorkingSets.length == 0) return true;
		
		if (element instanceof IAdaptable) {
			IExpression expressionToFilter = (IExpression) ((IAdaptable) element).getAdapter(IExpression.class);
			if (expressionToFilter != null) {
				return isInWorkingSet(expressionToFilter);
			} 
		}
		
		// Do not filter out elements which do not adapt to IExpression.  These may 
		// include special elements, such as the "Add New Expression..." element. 
		return true;
	}

	private boolean isInWorkingSet(IExpression expression)
	{
		for (int i=0; i<selectedWorkingSets.length; i++)
		{
			IWorkingSet exprWorkingSet = selectedWorkingSets[i];
			IAdaptable[] workingSetElements = exprWorkingSet.getElements();
			for (int j=0; j<workingSetElements.length; j++)
			{
				IAdaptable workingSetElement = workingSetElements[j];
				IExpression workingSetExpression = (IExpression) workingSetElement.getAdapter(IExpression.class);
				if (expression.getExpressionText().equals(workingSetExpression.getExpressionText()))
					return true;
			}
		}
		
		return false;
	}

	public boolean isApplicable(ITreeModelViewer viewer, Object parentElement) {
		return true;
	}
}
