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

import java.lang.ref.WeakReference;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.internal.components.ComponentMessages;
import org.eclipse.ui.internal.components.ComponentUtil;

public class SingletonFactory extends ComponentFactory implements IExecutableExtension {

    private WeakReference singletonInstance;
    
    private ClassIdentifier classId = null;
    
    /**
     * Should only be called when this factory is created through an extension point.
     * If this constructor is used, the caller MUST call setInitializationData before
     * attempting to use the factory. Call one of the other constructors if instantiating
     * this object programmatically.
     */
    public SingletonFactory() {
    }
   
    /**
     * Creates a factory that creates instances of the given class
     * 
     * @param classId name of the class to instantiate
     */
    public SingletonFactory(ClassIdentifier classId) {
        this.classId = classId;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    public void setInitializationData(IConfigurationElement config,
            String propertyName, Object data) throws CoreException {
    
        classId = ComponentUtil.getClassFromInitializationData(config, data);
    }
    
    public ComponentHandle createHandle(IServiceProvider availableServices)
            throws ComponentException {
        
        if (singletonInstance != null) {
            Object result = singletonInstance.get();
            if (result != null) {
                return new NonDisposingHandle(result);
            }
            singletonInstance = null;
        }
        
        Class clazz = ComponentUtil.loadClass(classId);
        try {
            Object ref = clazz.newInstance();
            singletonInstance = new WeakReference(ref);
            return new NonDisposingHandle(ref);
        } catch (InstantiationException e) {
            throw new ComponentException(clazz, NLS.bind(ComponentMessages.Components_instantiationException, clazz.getName()), e);
        } catch (Exception e) {
            throw new ComponentException(clazz, e);
        }
    }

}
