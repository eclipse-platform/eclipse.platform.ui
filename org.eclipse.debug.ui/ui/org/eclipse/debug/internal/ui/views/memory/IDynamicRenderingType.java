/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.memory;


/**
 * This interface represents a dynamic rendering type to be determined
 * by debug providers at runtime. 
 * 
 * Dynamic rendering types must be tied to a rendering extension.
 * When IMemoryRenderingManager.getAllRenderingInfo(Object obj) is called,
 * the manager will query for the "dynamicRenderingFactory" property from all rendering extensions.
 * 
 * If this property is defined in the rendering, the rendering defined will be considered
 * dynamic.  The manager will ask the dynamicRenderingFactory for a list of rendering types.
 * 
 * The manager will create IMemoryRenderingType ojbect for each of the dynamic rendering
 * type.  The dynamic rendering info will have all the properties defined in the extension.
 * When one of these dynamic renderings is created, it will use the rendering factory defined
 * in the extension to create the rendering.
 * @since 3.1
 */
public interface IDynamicRenderingType {
	
	/**
	 * @return the parent rendering definition of the dynamic rendering type
	 */
	IMemoryRenderingType getParentRenderingType();
	
	/**
	 * @return the rendering id of this rendering
	 */
	String getRenderingId();
	
	/**
	 * @return the name of this dynamic rendering
	 */
	String getName();
}
