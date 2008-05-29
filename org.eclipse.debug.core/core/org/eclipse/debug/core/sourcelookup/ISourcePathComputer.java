/*******************************************************************************
 * Copyright (c) 2003, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.core.sourcelookup;

import org.eclipse.debug.core.DebugPlugin;

/**
 * A source path computer computes the default source lookup path (set of source
 * containers that should be considered) for a launch configuration.
 * <p>
 * A source path computer is contributed in plug-in XML via the 
 * <code>sourcePathComputers</code> extension point, that provides a delegate
 * to compute the path specific to a launch configuration. Following
 * is an example contribution.
 * <pre>
 * &lt;extension point=&quot;org.eclipse.debug.core.sourcePathComputers&quot;&gt;
 *    	&lt;sourcePathComputer
 *    		id=&quot;org.eclipse.example.exampleSourcePathComputer&quot;
 *    		class=&quot;org.eclipse.example.SourcePathComputer&quot;&gt;
 *    	&lt;/sourcePathComputer&gt;
 * &lt;/extension&gt;
 * </pre>
 * </p> 
 * <p>
 * A source path computer can be associated with a launch configuration type
 * via the <code>sourcePathComputerId</code> attribute of a launch configuration
 * type extension. As well, a launch configuration can specify its own
 * source path computer to use via the <code>ATTR_SOURCE_PATH_COMPUTER_ID</code>
 * attribute.
 * </p>
 * <p>
 * Clients contributing a source path computer provide an implementation of 
 * {@link org.eclipse.debug.core.sourcelookup.ISourcePathComputerDelegate}.
 * </p>
 * @since 3.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ISourcePathComputer extends ISourcePathComputerDelegate {
	
	/**
	 * Launch configuration attribute to specify a source path computer
	 * that should be used for a launch configuration. The value is an identifier
	 * of a source path computer extension, or unspecified (<code>null</code>), if the
	 * default source path computer should be used. A default source path computer
	 * can be associated with a launch configuration type.
	 */
	public static final String ATTR_SOURCE_PATH_COMPUTER_ID = DebugPlugin.getUniqueIdentifier() + ".SOURCE_PATH_COMPUTER_ID"; //$NON-NLS-1$

	/**
	 * Returns the unique identifier for this source path computer.
	 * 
	 * @return the unique identifier for this source path computer
	 */
	public String getId();
	
}
