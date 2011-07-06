/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core;

 
import java.util.Comparator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.debug.core.DebugPlugin;

/**
 * Proxy to a runtime classpath entry resolver extension.
 * 
 * @see IConfigurationElementConstants
 */
public class LaunchConfigurationComparator implements Comparator {

	private IConfigurationElement fConfigurationElement;
	
	private Comparator fDelegate;
	
	/**
	 * Constructs a new resolver on the given configuration element
	 * 
	 * @param element configuration element
	 */
	public LaunchConfigurationComparator(IConfigurationElement element) {
		fConfigurationElement = element;
	}
		
	/**
	 * Returns the {@link Comparator} delegate
	 * 
	 * @return the {@link Comparator}
	 */
	protected Comparator getComparator() {
		if (fDelegate == null) {
			try {
				fDelegate = (Comparator)fConfigurationElement.createExecutableExtension(IConfigurationElementConstants.CLASS);
			} catch (CoreException e) {
				DebugPlugin.log(e);
			}
		}
		return fDelegate;
	}
	

	/**
	 * @see Comparator#compare(Object, Object)
	 */
	public int compare(Object o1, Object o2) {
		return getComparator().compare(o1, o2);
	}

	/**
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object obj) {
		return getComparator().equals(obj);
	}

}
