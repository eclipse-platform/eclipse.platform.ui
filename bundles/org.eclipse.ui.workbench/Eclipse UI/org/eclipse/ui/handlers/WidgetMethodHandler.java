/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.handlers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import org.eclipse.ui.commands.AbstractHandler;
import org.eclipse.ui.commands.ExecutionException;
import org.eclipse.ui.commands.NoSuchAttributeException;

/**
 * Handles the cut command in both dialogs and windows. This handler is enabled
 * if the focus control supports the "cut" method.
 * 
 * @since 3.0
 */
public class WidgetMethodHandler extends AbstractHandler implements IExecutableExtension {

    /**
     * The attribute names supported by this handler.  This value is never
     * <code>null</code>, but may be empty.
     */
    private static final Set ATTRIBUTE_NAMES = new HashSet();

    /**
     * The name of the attribute controlling the enabled state.
     */
    private static final String ATTRIBUTE_ENABLED = "enabled"; //$NON-NLS-1$

    /**
     * The name of the attribute for the identifier.
     */
    private static final String ATTRIBUTE_ID = "id"; //$NON-NLS-1$

    /**
     * The parameters to pass to the method this handler invokes.  This handler
     * always passes no parameters.
     */
    private static final Class[] METHOD_PARAMETERS = new Class[0];

    static {
        ATTRIBUTE_NAMES.add(ATTRIBUTE_ENABLED);
        ATTRIBUTE_NAMES.add(ATTRIBUTE_ID);
    }

    /**
     * The name of the method to be invoked by this handler.  This value should
     * never be <code>null</code>.
     */
    private String methodName;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.commands.IHandler#execute(java.lang.Object)
     */
    public final void execute(Object parameter) throws ExecutionException {
        final Method methodToExecute = getMethodToExecute();
        if (methodToExecute != null) {
            try {
                final Control focusControl = Display.getCurrent()
                        .getFocusControl();
                methodToExecute.invoke(focusControl, null);
            } catch (IllegalAccessException e) {
                // The method is protected, so do nothing.
            } catch (InvocationTargetException e) {
                throw new ExecutionException(e.getTargetException());
            }
        }
    }

    /**
     * Looks up the "cut" method on the focus control.
     * 
     * @return The "cut" method on the focus control; <code>null</code> if
     *         none.
     */
    private final Method getMethodToExecute() {
        final Control focusControl = Display.getCurrent().getFocusControl();
        try {
            return focusControl.getClass().getMethod(methodName,
                    METHOD_PARAMETERS);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.commands.IHandler#getAttributeValue(java.lang.String)
     */
    public final Object getAttributeValue(String attributeName)
            throws NoSuchAttributeException {
        if (attributeName.equals(ATTRIBUTE_ENABLED)) {
            return (getMethodToExecute() == null) ? Boolean.FALSE
                    : Boolean.TRUE;
        } else if (attributeName.equals(ATTRIBUTE_ID)) {
            return null;
        } else {
            throw new NoSuchAttributeException(
                    "This handler doesn't have the '" + attributeName //$NON-NLS-1$
                            + "' attribute"); //$NON-NLS-1$
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.commands.IHandler#getDefinedAttributeNames()
     */
    public final Set getDefinedAttributeNames() {
        return ATTRIBUTE_NAMES;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
        // The data is really just a string (i.e., the method name).
        methodName = data.toString();        
    }
}
