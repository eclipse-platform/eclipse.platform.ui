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

import java.lang.reflect.Field;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.ExpressionTagNames;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IPatternMatchListenerDelegate;

import com.ibm.icu.text.MessageFormat;

public class PatternMatchListenerExtension implements IPluginContribution {

    private IConfigurationElement fConfig;
    private Expression fEnablementExpression;
    private String fPattern;
    private String fQualifier;
    private int fFlags = -1;
   
    public PatternMatchListenerExtension(IConfigurationElement extension) {
        fConfig = extension;
    }    

    /*
     * returns the integer value of the flags defined in java.util.regex.Pattern.
     * Both <code>Pattern.MULTILINE</code> and <code>MULTILINE</code> will return
     * the same value.
     */
    public int parseFlags(String flagsElement) {
        int val = 0;
        if (flagsElement == null) {
            return val;
        }
            
        try {
            flagsElement = flagsElement.replaceAll("Pattern.", ""); //$NON-NLS-1$ //$NON-NLS-2$
            String[] tokens = flagsElement.split("\\s\\|\\s"); //$NON-NLS-1$
            Class clazz = Class.forName("java.util.regex.Pattern"); //$NON-NLS-1$
            
            for (int i = 0; i < tokens.length; i++) {
                Field field = clazz.getDeclaredField(tokens[i]);
                val |= field.getInt(null);
            }
        } catch (ClassNotFoundException e) {
            ConsolePlugin.log(e);
        } catch (NoSuchFieldException e) {
            ConsolePlugin.log(e);
        } catch (IllegalAccessException e) {
            ConsolePlugin.log(e);
        }
        return val;
    }
    
    public boolean isEnabledFor(IConsole console) throws CoreException {
        EvaluationContext context = new EvaluationContext(null, console);
        EvaluationResult evaluationResult = getEnablementExpression().evaluate(context);
        return evaluationResult == EvaluationResult.TRUE;
    }
    
    public IPatternMatchListenerDelegate createDelegate() throws CoreException {
        return (IPatternMatchListenerDelegate) fConfig.createExecutableExtension("class"); //$NON-NLS-1$
    }
    
    public Expression getEnablementExpression() throws CoreException {
		if (fEnablementExpression == null) {
			IConfigurationElement[] elements = fConfig.getChildren(ExpressionTagNames.ENABLEMENT);
            if (elements.length == 0) {
                String message = MessageFormat.format("{0} " +getLocalId() + " {1} " + getPluginId() + " {2}", new String[] {ConsoleMessages.PatternMatchListenerExtension_3,ConsoleMessages.PatternMatchListenerExtension_4,ConsoleMessages.PatternMatchListenerExtension_5}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                ConsolePlugin.log(new Status(IStatus.WARNING, ConsolePlugin.getUniqueIdentifier(), IStatus.OK, message, null));
            }
			IConfigurationElement enablement = elements.length > 0 ? elements[0] : null; 

			if (enablement != null) {
			    fEnablementExpression = ExpressionConverter.getDefault().perform(enablement);
			}
		}
		return fEnablementExpression;
    }
    
    /*
     * returns the regular expression to be matched.
     */
    public String getPattern() {
        if (fPattern == null) {
            fPattern = fConfig.getAttribute("regex"); //$NON-NLS-1$
            try {
            	fPattern = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(fPattern, false);
            } catch (CoreException e) {
            	ConsolePlugin.log(e);
            }
        }
        return fPattern;
    }

    /*
     * returns the flags to be used by <code>Pattern.compile(pattern, flags)</code>
     */
    public int getCompilerFlags() {
        if(fFlags < 0) {
            String flagsAttribute = fConfig.getAttribute("flags"); //$NON-NLS-1$
            fFlags = parseFlags(flagsAttribute);
        }
        return fFlags;
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

    public String getQuickPattern() {
    	if (fQualifier == null) {
    		fQualifier = fConfig.getAttribute("qualifier"); //$NON-NLS-1$
    		try {
    			if (fQualifier != null) {
    				fQualifier = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(fQualifier, false);
    			}
			} catch (CoreException e) {
				ConsolePlugin.log(e);
			}
    	}
    	return fQualifier;
    }
}
