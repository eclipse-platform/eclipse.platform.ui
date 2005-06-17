/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.expressions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;

public class TestExpression extends Expression {

	private String fNamespace;
	private String fProperty;
	private Object[] fArgs;
	private Object fExpectedValue;
	
	private static final String ATT_PROPERTY= "property"; //$NON-NLS-1$
	private static final String ATT_ARGS= "args"; //$NON-NLS-1$
	
	private static final TypeExtensionManager fgTypeExtensionManager= new TypeExtensionManager("propertyTesters"); //$NON-NLS-1$
	
	public TestExpression(IConfigurationElement element) throws CoreException {
		String property= element.getAttribute(ATT_PROPERTY);
		int pos= property.lastIndexOf('.');
		if (pos == -1) {
			throw new CoreException(new ExpressionStatus(
				ExpressionStatus.NO_NAMESPACE_PROVIDED,
				ExpressionMessages.TestExpression_no_name_space)); 
		}
		fNamespace= property.substring(0, pos);
		fProperty= property.substring(pos + 1);
		fArgs= Expressions.getArguments(element, ATT_ARGS);
		fExpectedValue= Expressions.convertArgument(element.getAttribute(ATT_VALUE));
	}
	
	public EvaluationResult evaluate(IEvaluationContext context) throws CoreException {
		Object element= context.getDefaultVariable();
		if (System.class.equals(element)) {
			String str= System.getProperty(fProperty);
			if (str == null) 
				return EvaluationResult.FALSE;
			return EvaluationResult.valueOf(str.equals(fArgs[0]));
		}
		Property property= fgTypeExtensionManager.getProperty(element, fNamespace, fProperty);
		if (!property.isInstantiated())
			return EvaluationResult.NOT_LOADED;
		return EvaluationResult.valueOf(property.test(element, fArgs, fExpectedValue));
	}
	
	//---- Debugging ---------------------------------------------------
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer args= new StringBuffer();
		for (int i= 0; i < fArgs.length; i++) {
			Object arg= fArgs[i];
			if (arg instanceof String) {
				args.append('\'');
				args.append(arg);
				args.append('\'');
			} else {
				args.append(arg.toString());
			}
			if (i < fArgs.length - 1)
				args.append(", "); //$NON-NLS-1$
		}
		return "<test property=\"" + fProperty +  //$NON-NLS-1$
		  (fArgs.length != 0 ? "\" args=\"" + args + "\"" : "\"") + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		  (fExpectedValue != null ? "\" value=\"" + fExpectedValue + "\"" : "\"") + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		  "/>"; //$NON-NLS-1$
	}
}
