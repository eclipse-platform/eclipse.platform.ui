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

import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Label provider for the Expression Working Set Page, 
 * used while creating a new working set.
 * <p>
 * Returns the text of an expression to display in the checklist.
 * 
 * @since 3.9
 */
public class ExpressionLabelProvider extends BaseLabelProvider implements ITableLabelProvider {

	public Image getColumnImage(Object element, int columnIndex) {
		if (element instanceof IExpression)
		{
			if (columnIndex == 0)
				return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_EXPRESSION);
		}
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof IExpression)
		{
			if (columnIndex == 0)
				return ((IExpression) element).getExpressionText();
		}
		return null;
	}

}
