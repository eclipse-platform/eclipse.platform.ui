/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.console;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.ExpressionTagNames;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsolePageParticipant;

public class ConsolePageParticipantExtension implements IPluginContribution {

    private IConfigurationElement fConfig;
    private Expression fEnablementExpression;

    public ConsolePageParticipantExtension(IConfigurationElement config) {
        fConfig = config;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPluginContribution#getLocalId()
     */
    public String getLocalId() {
        return fConfig.getAttribute("id"); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPluginContribution#getPluginId()
     */
    public String getPluginId() {
        return fConfig.getNamespace();
    }
    
    public boolean isEnabledFor(IConsole console) throws CoreException {
        EvaluationContext context = new EvaluationContext(null, console);
        EvaluationResult evaluationResult = getEnablementExpression().evaluate(context);
        return evaluationResult == EvaluationResult.TRUE;
    }
    
    public Expression getEnablementExpression() throws CoreException {
		if (fEnablementExpression == null) {
			IConfigurationElement[] elements = fConfig.getChildren(ExpressionTagNames.ENABLEMENT);
			IConfigurationElement enablement = elements.length > 0 ? elements[0] : null; 

			if (enablement != null) {
			    fEnablementExpression = ExpressionConverter.getDefault().perform(enablement);
			}
		}
		return fEnablementExpression;
    }

    public IConsolePageParticipant createDelegate() throws CoreException {
        return (IConsolePageParticipant) fConfig.createExecutableExtension("class"); //$NON-NLS-1$;
    }

}
