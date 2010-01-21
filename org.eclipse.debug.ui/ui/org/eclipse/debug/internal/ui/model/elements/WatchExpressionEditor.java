/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.model.elements;

import org.eclipse.debug.internal.ui.elements.adapters.WatchExpressionCellModifier;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementEditor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;

/**
 * @since 3.6
 */
public class WatchExpressionEditor implements IElementEditor {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementEditor#getCellEditor(org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, java.lang.String, java.lang.Object, org.eclipse.swt.widgets.Composite)
	 */
	public CellEditor getCellEditor(IPresentationContext context, String columnId, Object element, Composite parent) {
		return new TextCellEditor(parent);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementEditor#getCellModifier(org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, java.lang.Object)
	 */
	public ICellModifier getCellModifier(IPresentationContext context, Object element) {
		return new WatchExpressionCellModifier();
	}

}
