/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.resource;


/**
 * @since 3.1
 */
public class DeviceResourceException extends Exception {
    
	/**
	 * All serializable objects should have a stable serialVersionUID
	 */
	private static final long serialVersionUID = 11454598756198L;
    
	/**
	 * Creates a DeviceResourceException indicating an error attempting to
	 * create a resource 
	 * 
	 * @param missingResource
	 */
    public DeviceResourceException(DeviceResourceDescriptor missingResource) {
        super("Unable to create resource " + missingResource.toString(), null); //$NON-NLS-1$
    }
}
