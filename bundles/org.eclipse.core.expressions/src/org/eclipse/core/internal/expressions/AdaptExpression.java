/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.expressions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPluginDescriptor;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.IEvaluationContext;

public class AdaptExpression extends CompositeExpression {

	private static final String ATT_TYPE= "type"; //$NON-NLS-1$
	
	private String fTypeName;
	private Class fType;

	private IPluginDescriptor fPluginDescriptor;
	
	public AdaptExpression(IConfigurationElement configElement) throws CoreException {
		fTypeName= configElement.getAttribute(ATT_TYPE);
		Expressions.checkAttribute(ATT_TYPE, fTypeName);
		fPluginDescriptor= configElement.getDeclaringExtension().getDeclaringPluginDescriptor();
	}
	
	/* (non-Javadoc)
	 * @see Expression#evaluate(IVariablePool)
	 */
	public EvaluationResult evaluate(IEvaluationContext context) throws CoreException {
		if (fTypeName == null)
			return EvaluationResult.FALSE;
		Object var= context.getDefaultVariable();
		if (!(var instanceof IAdaptable))
			return EvaluationResult.FALSE;
		
		if (fType == null) {
			ClassLoader loader= fPluginDescriptor.getPluginClassLoader();
			try {
				fType= loader.loadClass(fTypeName);
			} catch (ClassNotFoundException e) {
				fTypeName= null;
				return EvaluationResult.FALSE;
			}
		}
		Object adapted= ((IAdaptable)var).getAdapter(fType);
		if (adapted == null)
			return EvaluationResult.FALSE;
		return evaluateAnd(new DefaultVariable(context, adapted));
	}
}
