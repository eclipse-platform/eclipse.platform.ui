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
package org.eclipse.debug.core;

import org.eclipse.debug.core.model.ILogicalStructureTypeDelegate;

/**
 * Provides a value representing the logical structure of a raw implementation value
 * from a debug model. Logical structures are useful for navigating complex data
 * structures. Logical structure types are contributed via extensions in plug-in XML,
 * and provide a delegate for performing logical value computation. Logical 
 * structure types can be retrieved from the <code>DebugPlugin</code>.
 * <p>
 * Following is example plug-in XML to define a logical structure type.
 * </p>
 * <pre>
 * &lt;extension point=&quot;org.eclipse.debug.core.logicalStructureTypes&quot;&gt;
 *  &lt;logicalStructureType
 *   id=&quot;com.example.ExampleLogicalStructure&quot;
 *   class=&quot;com.example.ExampleLogicalStructureDelegate&quot;
 *   modelIdentifier=&quot;com.example.debug.model&quot;
 *   description=&quot;Ordered Collection&quot;&gt;
 *  &lt;/logicalStructureType&gt;
 * &lt;/extension&gt;
 * </pre>
 * <p>
 * The attributes are specified as follows:
 * <ul>
 * <li>id - unique identifier for this logical structure type</li>
 * <li>class - fully qualified name of class that implements 
 *   <code>ILogicalStructureTypeDelegate</code></li>
 * <li>modelIdentifier - identifier of the debug model this logical structure
 *   type is associated with</li>
 * <li>description - description of the logical structure provided</li>
 * </ul>
 * </p>
 * @since 3.0
 * @see org.eclipse.debug.core.model.ILogicalStructureTypeDelegate
 */
public interface ILogicalStructureType extends ILogicalStructureTypeDelegate{
	
	/**
	 * Returns a simple description of the logical structure provided by this
	 * structure type.
	 * 
	 * @return a simple description of the logical structure provided by this
	 * structure type
	 */
	public String getDescription();
	
	/**
	 * Returns this logical structure type's unique identifier, as defined
	 * in plug-in XML.
	 * 
	 * @return this logical structure type's unique identifier
	 */
	public String getId();

}
