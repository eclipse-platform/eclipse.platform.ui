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
package org.eclipse.debug.internal.ui.memory;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.ExpressionTagNames;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.memory.IMemoryRenderingType;
import org.eclipse.debug.ui.memory.IMemoryRenderingTypeProvider;

/**
 * Represents a renderingBindings element of a memoryRenderings
 * contribution.
 * 
 * @since 3.1
 */
class RenderingBindings {
    
    // element
    protected IConfigurationElement fConfigurationElement;
    
    // rendering type provider, or null (optional)
    private IMemoryRenderingTypeProvider fProvider;
    // optional exprssion
    private Expression fExpression;
    
    // cached rendering ids, if specified
    private String[] fRenderingIds;

    // element attribute
    public static final String ATTR_RENDERING_IDS = "renderingIds"; //$NON-NLS-1$
    public static final String ATTR_PROVIDER = "class"; //$NON-NLS-1$
    
    /**
     * Constructs a bindings element from the given contribution.
     * 
     * @param element contribution
     */
    RenderingBindings(IConfigurationElement element) {
        fConfigurationElement = element;
    }
    
    /**
     * Returns the rendering ids attribute as an array of ids, or <code>null</code>
     * if none.
     * @return the rendering ids attribute as an array of ids, or <code>null</code>
     * if none
     */
    String[] getRenderingIds() {
        if (fRenderingIds == null) {
            String ids = fConfigurationElement.getAttribute(ATTR_RENDERING_IDS);
            if (ids != null) {
                String[] strings = ids.split(","); //$NON-NLS-1$
                fRenderingIds = new String[strings.length];
                for (int i = 0; i < strings.length; i++) {
                    String string = strings[i];
                    fRenderingIds[i] = string.trim();
                }
            }
        }
        return fRenderingIds;
    }
    
    /**
     * Returns the rendering type ids applicable to the given memory block.
     * 
     * @param block
     * @return
     */
    String[] getRenderingIds(IMemoryBlock block) {
        String[] ids = getRenderingIds();
        if (ids == null) {
            IMemoryRenderingTypeProvider provider = getProvider();
            if (provider == null) {
                return new String[0];
            }
            IMemoryRenderingType[] types = provider.getRenderingTypes(block);
            ids = new String[types.length];
            for (int i = 0; i < types.length; i++) {
                ids[i] = types[i].getId();
            }
        }
        return ids;
    }
    
    /**
     * Returns the provider for this binding or <code>null</code> of none.
     * 
     * @return
     */
    IMemoryRenderingTypeProvider getProvider() {
        if (fProvider == null) {
            String name = fConfigurationElement.getAttribute(ATTR_PROVIDER);
            if (name != null) {
                try {
                    fProvider = (IMemoryRenderingTypeProvider) fConfigurationElement.createExecutableExtension(name);
                } catch (CoreException e) {
                    DebugUIPlugin.log(e);
                }
            }
        }
        return fProvider;
    }
    
    /**
     * Returns whether this binding is enabled for the given memory block.
     * 
     * @param block memory block
     * @return whether this binding is enabled for the given memory block
     */
    boolean isEnabled(IMemoryBlock block) {
        Expression expression = getExpression();
        if (expression != null) {
            IEvaluationContext context = new EvaluationContext(null, block);
            try {
                return expression.evaluate(context) == EvaluationResult.TRUE;
            } catch (CoreException e) {
                DebugUIPlugin.log(e);
                return false;
            }
        }
        return true;
    }
    
    /**
     * Validates this contribution.
     * 
     * @exception CoreException if invalid
     */
    void validate() throws CoreException {
        if (fConfigurationElement.getAttribute(ATTR_RENDERING_IDS) == null
                && fConfigurationElement.getAttribute(ATTR_PROVIDER) == null) {
            Status status = new Status(IStatus.ERROR, DebugUIPlugin.getUniqueIdentifier(), IDebugUIConstants.INTERNAL_ERROR,
                    "<renderingBindings> element must specify one of " + ATTR_RENDERING_IDS + " or " + ATTR_PROVIDER, null); //$NON-NLS-1$ //$NON-NLS-2$
            throw new CoreException(status);
        }
    }
    
    /**
     * Returns this binding's enablement expression, or <code>null</code> if none.
     * 
     * @return enablement expression, or <code>null</code> if none
     */
    Expression getExpression() {
        if (fExpression == null) {
            IConfigurationElement[] elements = fConfigurationElement.getChildren(ExpressionTagNames.ENABLEMENT);
            IConfigurationElement enablement = elements.length > 0 ? elements[0] : null; 
            if (enablement != null) {
                try {
                    fExpression = ExpressionConverter.getDefault().perform(enablement);
                } catch (CoreException e) {
                    DebugUIPlugin.log(e);
                }
            }
        }
        return fExpression;
    }
    
}
