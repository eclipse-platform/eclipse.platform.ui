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
package org.eclipse.ui.components;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.components.ComponentMessages;
import org.eclipse.ui.internal.components.ComponentUtil;

/**
 * Exception thrown when unable to construct a component.
 * 
 * Not intended to be subclassed by clients
 * 
 * <p>EXPERIMENTAL: The components framework is currently under active development. All
 * aspects of this class including its existence, name, and public interface are likely
 * to change during the development of Eclipse 3.1</p>
 * 
 * @since 3.1
 */
public class ComponentException extends Exception {
	
    private static final long serialVersionUID = 6009335074727417445L;
    
    private String componentName;
    
    /**
     * Creates an exception indicating failure to create a particular component. 
     * 
     * @param componentKey identifies the component being created
     * @param reason exception that prevented the component from being created, or null if none
     */
    public ComponentException(Object componentKey, Throwable reason) {
        this(componentKey, getMessage(getLongName(componentKey), reason), reason);
    }
    
    /**
     * Creates a component exception, given the name of the class the caller was 
     * attempting to create, a string describing the error, and the Throwable that 
     * caused the error.
     * 
     * @param componentKey the fully-qualified name of the component class
     * @param message explanation of the error
     * @param reason exception that caused the error
     */
    public ComponentException(Object componentKey, String message, Throwable reason) {
        super(message, getCause(reason));
        
        this.componentName = getShortName(componentKey);
    }
    
    private static String getShortName(Object key) {
        if (key instanceof Class) {
            return ComponentUtil.getSimpleClassName(((Class)key).getName());
        }
        return key.toString();
    }
    
    private static String getLongName(Object key) {
        if (key instanceof Class) {
            return ((Class)key).getName();
        }
        return key.toString();
    }
    
    
    private static Throwable getCause(Throwable toQuery) {
        if (toQuery == null) {
            return null;
        }
        
        Throwable cause = toQuery;
        
        if (toQuery instanceof CoreException) {
            CoreException e = (CoreException)toQuery;
            cause = e.getStatus().getException();
            
            if (cause == null) {
                cause = toQuery;
            }
        }
        
        cause = cause.getCause();
        
        if (cause != null) {
            return cause;
        }
        
        return toQuery;
    }
    
    private static String getMessage(String dep, Throwable reason) {      
        reason = getCause(reason);
        
        if (reason instanceof ComponentException) {
            return getMessageString(dep, (ComponentException)reason);
        }
        
        if (reason != null) {
            String userString = reason.getLocalizedMessage();
            if (reason instanceof CoreException) {
                userString = ((CoreException)reason).getStatus().getMessage();
            }
            
            if (userString != null) {
                return NLS.bind(ComponentMessages.ComponentException_unable_to_instantiate_because, dep, userString);
            }
        }
        
        return NLS.bind(ComponentMessages.ComponentException_unable_to_instantiate, dep); 
    }
    
    private static String getMessageString(String componentName, ComponentException e) {
        return NLS.bind(ComponentMessages.ComponentException_recursive_requires_string, 
                new Object[] {
                componentName, 
                e.getComponentName(),
                ComponentUtil.getMessage(e)});
        
    }
    
    /**
     * Describes the component being created.
     *
     * @return description of the component being created
     */
    public String getComponentName() {
        return componentName;
    }
    
    /**
     * Returns an IStatus suitable for logging this exception or embedding it 
     * within a CoreException.
     *
     * @return an IStatus suitable for embedding this exception within a CoreException.
     */
    public IStatus getStatus() {
        return WorkbenchPlugin.getStatus(getMessage(), ComponentUtil.getCause(this));
    }
}
