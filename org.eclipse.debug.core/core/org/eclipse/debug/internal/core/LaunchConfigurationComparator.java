package org.eclipse.debug.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.Comparator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.debug.core.DebugPlugin;

/**
 * Proxy to a runtime classpath entry resolver extension.
 */
public class LaunchConfigurationComparator implements Comparator {

	private IConfigurationElement fConfigurationElement;
	
	private Comparator fDelegate;
	
	/**
	 * Constructs a new resolver on the given configuration element
	 */
	public LaunchConfigurationComparator(IConfigurationElement element) {
		fConfigurationElement = element;
	}
		
	/**
	 * Returns the resolver delegate (and creates if required) 
	 */
	protected Comparator getComparator() {
		if (fDelegate == null) {
			try {
				fDelegate = (Comparator)fConfigurationElement.createExecutableExtension("class"); //$NON-NLS-1$
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
