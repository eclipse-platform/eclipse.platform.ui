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

package org.eclipse.ant.internal.ui.launchConfigurations;

import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;

/**
 * Constant definitions for Ant launch configurations.
 * <p>
 * Constant definitions only; not to be implemented.
 * </p>
 * @since 3.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IAntLaunchConfigurationConstants {

	/**
	* String attribute indicating the custom runtime classpath to use for an Ant
	* build. Default value is <code>null</code> which indicates that the global
	* classpath is to be used. Format is a comma separated listing of URLs.
	* @deprecated no longer supported: use {@link IJavaLaunchConfigurationConstants#ATTR_CLASSPATH_PROVIDER}
	* @see IJavaLaunchConfigurationConstants#ATTR_DEFAULT_CLASSPATH
	*/
	public static final String ATTR_ANT_CUSTOM_CLASSPATH = IExternalToolConstants.PLUGIN_ID + ".ATTR_ANT_CUSTOM_CLASSPATH"; //$NON-NLS-1$
	/**
	 * String attribute indicating the custom Ant home to use for an Ant build.
	 * Default value is <code>null</code> which indicates that no Ant home is to
	 * be set 
	 * @deprecated no longer supported: use {@link IJavaLaunchConfigurationConstants#ATTR_CLASSPATH_PROVIDER}
	 * @see IJavaLaunchConfigurationConstants#ATTR_DEFAULT_CLASSPATH
	 */
	public static final String ATTR_ANT_HOME = IExternalToolConstants.PLUGIN_ID + ".ATTR_ANT_HOME"; //$NON-NLS-1$
}
