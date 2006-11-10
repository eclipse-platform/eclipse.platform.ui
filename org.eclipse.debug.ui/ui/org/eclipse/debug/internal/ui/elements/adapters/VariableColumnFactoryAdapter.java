/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.elements.adapters;

import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactoryAdapter;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.provisional.IColumnEditor;
import org.eclipse.debug.internal.ui.viewers.provisional.IColumnEditorFactoryAdapter;
import org.eclipse.debug.ui.IDebugUIConstants;

/**
 * Factory for default variable column presentation.
 * 
 * @since 3.2
 */
public class VariableColumnFactoryAdapter implements IColumnPresentationFactoryAdapter, IColumnEditorFactoryAdapter {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresenetationFactoryAdapter#createColumnPresentation(org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext, java.lang.Object)
	 */
	public IColumnPresentation createColumnPresentation(IPresentationContext context, Object element) {
		String id = context.getId();
		if (IDebugUIConstants.ID_VARIABLE_VIEW.equals(id) || IDebugUIConstants.ID_REGISTER_VIEW.equals(id)) {
			if (element instanceof IStackFrame) {
				return new VariableColumnPresentation();
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresenetationFactoryAdapter#getColumnPresentationId(org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext, java.lang.Object)
	 */
	public String getColumnPresentationId(IPresentationContext context, Object element) {
		String id = context.getId();
		if (IDebugUIConstants.ID_VARIABLE_VIEW.equals(id) || IDebugUIConstants.ID_REGISTER_VIEW.equals(id)) {
			if (element instanceof IStackFrame) {
				return VariableColumnPresentation.DEFAULT_VARIABLE_COLUMN_PRESENTATION;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnEditorFactoryAdapter#createColumnEditor(org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext, java.lang.Object)
	 */
	public IColumnEditor createColumnEditor(IPresentationContext context, Object element) {
		String id = context.getId();
		if (IDebugUIConstants.ID_VARIABLE_VIEW.equals(id) || IDebugUIConstants.ID_REGISTER_VIEW.equals(id)) {
			if (element instanceof IVariable) {
				return new VariableColumnEditor();
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnEditorFactoryAdapter#getColumnEditorId(org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext, java.lang.Object)
	 */
	public String getColumnEditorId(IPresentationContext context, Object element) {
		String id = context.getId();
		if (IDebugUIConstants.ID_VARIABLE_VIEW.equals(id) || IDebugUIConstants.ID_REGISTER_VIEW.equals(id)) {
			if (element instanceof IVariable) {
				return VariableColumnEditor.DEFAULT_VARIABLE_COLUMN_EDITOR;
			}
		}
		return null;
	}

}
