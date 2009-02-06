/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.stringsubstitution;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.debug.internal.core.variables.ResourceResolver;

import com.ibm.icu.text.MessageFormat;

/**
 * Resolves the currently selected resource.
 * 
 * @since 3.5
 */
public class SelectedResourceResolver extends ResourceResolver implements IDynamicVariableResolver {

	/* (non-Javadoc)
	 * @see org.eclipse.core.variables.IDynamicVariableResolver#resolveValue(org.eclipse.core.variables.IDynamicVariable, java.lang.String)
	 */
	public String resolveValue(IDynamicVariable variable, String argument) throws CoreException {
		IResource resource = SelectedResourceManager.getDefault().getSelectedResource();
		if (resource != null) {
			return translateToValue(resource, variable);
		}
		abort(MessageFormat.format(StringSubstitutionMessages.SelectedResourceResolver_0, new String[]{getReferenceExpression(variable, argument)}), null);	
		return null;
	}

}
