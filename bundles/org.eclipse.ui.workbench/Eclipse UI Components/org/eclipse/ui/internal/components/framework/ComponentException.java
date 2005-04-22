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
package org.eclipse.ui.internal.components.framework;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.components.ComponentMessages;
import org.eclipse.ui.internal.components.ComponentUtil;
import org.eclipse.ui.internal.misc.StatusUtil;

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
    
	private Throwable cause;
    
    /**
     * Creates an exception indicating failure to create a particular component. 
     * 
     * @param componentKey identifies the component being created
     * @param reason exception that prevented the component from being created, or null if none
     */
    public ComponentException(Object componentKey, Throwable reason) {
        this(componentKey, getMessage(componentKey, reason), reason);
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
        super(message);
        // don't pass the cause to super, to allow compilation against JCL Foundation
        this.cause = ComponentUtil.getMostSpecificCause(reason);
        
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
        
    private static String getMessage(Object key, Throwable reason) {
        
        String longName = getLongName(key);
        String shortName = getShortName(key);
        
        Throwable current = reason;
        while (current != null) {
            if (current instanceof ComponentException) {
                ComponentException ce = (ComponentException)current;
                if (shortName.equals(ce.getComponentName())) {
                    return ce.getLocalizedMessage();
                }
                return getMessageString(longName, (ComponentException)current);
            }
            
            Throwable cause = ComponentUtil.getCause(current);
            
            if (cause == current) {
                break;
            }
            
            current = cause;
        }
        
        reason = ComponentUtil.getCause(reason);
        
        if (reason != null) {
            String userString = StatusUtil.getLocalizedMessage(reason);
            
            if (userString != null) {
                return NLS.bind(ComponentMessages.ComponentException_unable_to_instantiate_because, longName, userString);
            }
        }
        
        return NLS.bind(ComponentMessages.ComponentException_unable_to_instantiate, longName); 
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
    
    /**
     * Returns the cause of this throwable or <code>null</code> if the
     * cause is nonexistent or unknown. 
     *
     * @return the cause or <code>null</code>
     * @since 3.1
     */
    public Throwable getCause() {
        return cause;
    }
    
}
