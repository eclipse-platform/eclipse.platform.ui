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
package org.eclipse.debug.internal.ui.elements.adapters;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.internal.ui.viewers.IPresentationContext;
import org.eclipse.debug.internal.ui.views.launch.DebugElementHelper;
import org.eclipse.swt.graphics.RGB;

/**
 * Label adapter for variables.
 * 
 * @since 3.2
 */
public class VariableLabelAdapter extends AsynchronousDebugLabelAdapter {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.viewers.AsynchronousLabelAdapter#getForeground(java.lang.Object, org.eclipse.debug.ui.viewers.IPresentationContext)
	 */
	protected RGB[] getForegrounds(Object element, IPresentationContext context) throws CoreException {
        if (element instanceof IVariable) {
        	IVariable variable = (IVariable) element;
        	try {
				if (variable.hasValueChanged()) {
					return new RGB[] {DebugUIPlugin.getPreferenceColor(IDebugPreferenceConstants.CHANGED_VARIABLE_COLOR).getRGB()};
				}
			} catch (DebugException e) {
			}
			return new RGB[] {DebugElementHelper.getForeground(element)};
        }
        return super.getForegrounds(element, context);
	}

	
}
