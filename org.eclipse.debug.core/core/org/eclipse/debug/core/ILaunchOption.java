/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.core;

/**
 * This interface describes a launch option.
 * Clients can contribute launch options via the <code>launchOptions</code> extension point.
 * 
 * Example contribution of the debug launch option:
 * <pre>
 * <extension
         point="org.eclipse.debug.core.launchOptions">
      <launchOption
            id="org.eclipse.debug.core.debug"
            label="Debug"
            option="debug">
      </launchOption>
   </extension>   
 * </pre>
 * 
 * Clients are NOT intended to implement this interface
 * 
 * @since 3.3
 *
 */
public interface ILaunchOption {

	
	/**
	 * @return the human readable label for this launch option e.g. 'Debug'
	 */
	public String getLabel();
	
	/**
	 * Returns the launch option defined for this extension. The option is non-translatable, one word and
	 * all lowercase.
	 * @return the option defined by this extension
	 */
	public String getOption();
	
	/**
	 * @return the unique id provided for this option e.g. org.eclipse.debug.core.debug
	 */
	public String getIdentifier();
}
