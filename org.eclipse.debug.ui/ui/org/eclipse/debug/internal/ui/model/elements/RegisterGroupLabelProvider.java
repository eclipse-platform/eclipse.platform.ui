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
package org.eclipse.debug.internal.ui.model.elements;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.elements.adapters.VariableColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @since 3.3
 */
public class RegisterGroupLabelProvider extends DebugElementLabelProvider {

	protected ImageDescriptor getImageDescriptor(Object element, IPresentationContext presentationContext, String columnId) throws CoreException {
		if (columnId == null || VariableColumnPresentation.COLUMN_VARIABLE_NAME.equals(columnId)) {
			return super.getImageDescriptor(element, presentationContext, columnId);
		}
		return null;
	}

	protected String getLabel(Object element, IPresentationContext context, String columnId) throws CoreException {
		if (columnId == null || VariableColumnPresentation.COLUMN_VARIABLE_NAME.equals(columnId)) {
			return super.getLabel(element, context, columnId);
		} else {
			return ""; //$NON-NLS-1$
		}
	}
	
}
