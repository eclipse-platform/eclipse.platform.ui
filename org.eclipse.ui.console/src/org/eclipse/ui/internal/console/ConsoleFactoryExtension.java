/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.console;

import java.net.URL;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.ExpressionTagNames;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleFactory;
import org.osgi.framework.Bundle;

/**
 * @since 3.1
 */
public class ConsoleFactoryExtension implements IPluginContribution {

    private IConfigurationElement fConfig;
    private Expression fEnablementExpression;
    private String fLabel;
    private ImageDescriptor fImageDescriptor;
    private IConsoleFactory fFactory;

    ConsoleFactoryExtension(IConfigurationElement config) {
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
        return fConfig.getContributor().getName();
    }

    public boolean isEnabled() {
        try {
            Expression enablementExpression = getEnablementExpression();
            if (enablementExpression == null) {
                return true;
            } 
            EvaluationContext context = new EvaluationContext(null, this);
            EvaluationResult evaluationResult = enablementExpression.evaluate(context);
            return evaluationResult != EvaluationResult.FALSE;
        } catch (CoreException e) {
            ConsolePlugin.log(e);
            return false;
        }
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

    public String getLabel() {
        if (fLabel == null) {
            fLabel = fConfig.getAttribute("label"); //$NON-NLS-1$
        }
        return fLabel;
    }

    /**
     * @return
     */
    public ImageDescriptor getImageDescriptor() {
        if (fImageDescriptor == null) {
            String path = fConfig.getAttribute("icon"); //$NON-NLS-1$
            if (path != null) {
                Bundle bundle = Platform.getBundle(getPluginId());
                URL url = FileLocator.find(bundle, new Path(path), null);
                if (url != null) {
                	fImageDescriptor =  ImageDescriptor.createFromURL(url);
                }
            }
        }
        return fImageDescriptor;
    }

    /**
     * @return
     * @throws CoreException
     */
    public IConsoleFactory createFactory() throws CoreException {
        if (fFactory == null) {
            fFactory = (IConsoleFactory) fConfig.createExecutableExtension("class"); //$NON-NLS-1$
        }
        return fFactory;
    }
}
