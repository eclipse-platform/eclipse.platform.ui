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
package org.eclipse.ui.internal.commands.ws;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.commands.AbstractHandler;
import org.eclipse.ui.commands.ExecutionException;

/**
 * Handles the cut command in both dialogs and windows. This handler is enabled
 * if the focus control supports the "cut" method.
 * 
 * @since 3.0
 */
public class WidgetMethodHandler extends AbstractHandler implements
        IExecutableExtension {

    /**
     * The name of the attribute controlling the enabled state.
     */
    private static final String ATTRIBUTE_ENABLED = "enabled"; //$NON-NLS-1$

    /**
     * The name of the attribute for the identifier.
     */
    private static final String ATTRIBUTE_ID = "id"; //$NON-NLS-1$

    /**
     * The parameters to pass to the method this handler invokes. This handler
     * always passes no parameters.
     */
    protected static final Class[] NO_PARAMETERS = new Class[0];

    /**
     * The name of the method to be invoked by this handler. This value should
     * never be <code>null</code>.
     */
    protected String methodName;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.commands.IHandler#execute(java.lang.Object)
     */
    public Object execute(Map parameterValuesByName) throws ExecutionException {
        final Method methodToExecute = getMethodToExecute();
        if (methodToExecute != null) {
            try {
                final Control focusControl = Display.getCurrent()
                        .getFocusControl();
                methodToExecute.invoke(focusControl, null);
            } catch (IllegalAccessException e) {
                // The method is protected, so do nothing.
            } catch (InvocationTargetException e) {
                throw new ExecutionException(
                        "An exception occurred while executing " //$NON-NLS-1$
                                + getMethodToExecute(), e.getTargetException());
            }
        }

        return null;
    }

    public Map getAttributeValuesByName() {
        Map attributeValuesByName = new HashMap();
        attributeValuesByName.put(ATTRIBUTE_ENABLED,
                getMethodToExecute() == null ? Boolean.FALSE : Boolean.TRUE);
        attributeValuesByName.put(ATTRIBUTE_ID, null);
        return Collections.unmodifiableMap(attributeValuesByName);
    }

    /**
     * Looks up the method on the focus control.
     * 
     * @return The method on the focus control; <code>null</code> if none.
     */
    protected Method getMethodToExecute() {
        final Control focusControl = Display.getCurrent().getFocusControl();
        try {
            if (focusControl != null) {
                return focusControl.getClass().getMethod(methodName, NO_PARAMETERS);
            }
        } catch (NoSuchMethodException e) {
            // Fall through....
        }
        
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement,
     *      java.lang.String, java.lang.Object)
     */
    public void setInitializationData(IConfigurationElement config,
            String propertyName, Object data) {
        // The data is really just a string (i.e., the method name).
        methodName = data.toString();
    }
}
