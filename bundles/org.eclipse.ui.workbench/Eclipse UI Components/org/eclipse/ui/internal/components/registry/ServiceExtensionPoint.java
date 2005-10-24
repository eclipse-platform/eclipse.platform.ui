/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.components.registry;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.ui.internal.components.ExecutableExtensionFactory;
import org.eclipse.ui.internal.components.framework.ClassIdentifier;

/**
 * @since 3.1
 */
public class ServiceExtensionPoint {
    
    private static final String ATT_IMPLEMENTATION = "class"; //$NON-NLS-1$
    private static final String ATT_COMPONENT = "component"; //$NON-NLS-1$
    private static final String ATT_INTERFACE = "interface"; //$NON-NLS-1$
    private static final String ATT_INTERFACES = "services"; //$NON-NLS-1$
    
    private IExtensionPointMonitor extensionPointMonitor = new IExtensionPointMonitor() {
        /* (non-Javadoc)
         * @see org.eclipse.ui.internal.component.IExtensionPointMonitor#added(org.eclipse.core.runtime.IExtension)
         */
        public void added(IExtension newExtension) {
            processExtension(newExtension, true);
        }
        /* (non-Javadoc)
         * @see org.eclipse.ui.internal.component.IExtensionPointMonitor#removed(org.eclipse.core.runtime.IExtension)
         */
        public void removed(IExtension oldExtension) {
            processExtension(oldExtension, false);
        }
    };
    
    private ComponentRegistry registry;
    private ExtensionPointManager manager;
    
    
    public ServiceExtensionPoint(ExtensionPointManager manager, ComponentRegistry scope) {
        this.manager = manager;
        registry = scope;
        manager.addMonitor(ATT_INTERFACES, extensionPointMonitor);
    }
    
    public void dispose() {
        manager.removeMonitor(ATT_INTERFACES, extensionPointMonitor);
    }
    
    private ClassIdentifier getType(IConfigurationElement element, String attributeName) {
    	return new ClassIdentifier(element.getNamespace(), element.getAttribute(attributeName));
    }
    
    private void processExtension(IExtension extension, boolean added) {
        IConfigurationElement[] elements = extension.getConfigurationElements();
        
        for (int i = 0; i < elements.length; i++) {
            IConfigurationElement element = elements[i];
            
            if (element.getName().equals(ATT_COMPONENT)) {
            	ClassIdentifier interfaceName = getType(element, ATT_INTERFACE);
                String scopeId = element.getAttribute("scope"); //$NON-NLS-1$
                                
                if (added) {
                    ExecutableExtensionFactory factory = new ExecutableExtensionFactory(element, ATT_IMPLEMENTATION);
                    registry.addType(scopeId, interfaceName, factory);
                } else {
                    registry.removeType(scopeId, interfaceName);
                }
            } else if (element.getName().equals("scope")) { //$NON-NLS-1$
                String id = element.getAttribute("id"); //$NON-NLS-1$
                
                if (added) {
                    
                    ScopeDefinition def = new ScopeDefinition();
                    
                    // Get extended scopes
                    IConfigurationElement[] children = element.getChildren();
                    
                    for (int j = 0; j < children.length; j++) {
                        IConfigurationElement child = children[j];
                        
                        String name = child.getName();
                        
                        if (name.equals("requiresScope")) { //$NON-NLS-1$
                            String scopeName = child.getAttribute("id"); //$NON-NLS-1$
                            def.addExtends(new SymbolicScopeReference(scopeName, IScopeReference.REL_REQUIRES));
                        } else if (name.equals("extendsScope")) { //$NON-NLS-1$
                            String scopeName = child.getAttribute("id"); //$NON-NLS-1$
                            def.addExtends(new SymbolicScopeReference(scopeName, IScopeReference.REL_EXTENDS));
                        } else if (name.equals("requiresInterface")) { //$NON-NLS-1$
                            String typeName = child.getAttribute("id"); //$NON-NLS-1$
                            def.addDependency(new ClassIdentifier(extension.getNamespace(), typeName));
                        }                        
                    }
                    
                    registry.loadScope(id, def);
                } else {
                    registry.unloadScope(id);
                }
            } 
            
//            else if (element.getName().equals("modifier")) {
//            	IComponentType className = getType(element, "implementation");
//            	IComponentType interfaceName = getType(element, ATT_INTERFACE);
//            	IComponentType protocolName = getType(element, "protocol");
//            	String scopeId = element.getAttribute("scope");
//                
//            	if (added) {
//            		registry.addModifier(scopeId, className, protocolName, new ComponentTypeFactory(interfaceName));
//            	} else {
//            		registry.removeModifier(scopeId, className, protocolName);
//            	}
//            }
        }
    }
}
