package org.eclipse.debug.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;

/**
 * A launch configuration type wrappers a configuration
 * element for a <code>launchConfigurationType</code>
 * extension.
 */
public class LaunchConfigurationType implements ILaunchConfigurationType {
	
	/**
	 * The configuration element of the extension.
	 */
	private IConfigurationElement fElement;
	
	/**
	 * Modes this type supports.
	 */
	private Set fModes;
	
	/**
	 * The delegate for launch configurations of this type.
	 * Delegates are instantiated lazily as required.
	 */
	private ILaunchConfigurationDelegate fDelegate;
	
	/**
	 * Constructs a new launch configuration type on the
	 * given configuration element.
	 * 
	 * @param element configuration element
	 */
	protected LaunchConfigurationType(IConfigurationElement element) {
		setConfigurationElement(element);
	}
	
	/**
	 * Sets this type's configuration element.
	 * 
	 * @param element this type's configuration element
	 */
	private void setConfigurationElement(IConfigurationElement element) {
		fElement = element;
	}
	
	/**
	 * Returns this type's configuration element.
	 * 
	 * @return this type's configuration element
	 */
	protected IConfigurationElement getConfigurationElement() {
		return fElement;
	}	


	/**
	 * @see ILaunchConfigurationType#supportsMode(String)
	 */
	public boolean supportsMode(String mode) {
		return getModes().contains(mode);
	}

	/**
	 * Returns the set of modes specified in the configuration data.
	 * 
	 * @return the set of modes specified in the configuration data
	 */
	protected Set getModes() {
		if (fModes == null) {
			String modes= getConfigurationElement().getAttribute("modes"); //$NON-NLS-1$
			if (modes == null) {
				return new HashSet(0);
			}
			StringTokenizer tokenizer= new StringTokenizer(modes, ","); //$NON-NLS-1$
			fModes = new HashSet(tokenizer.countTokens());
			while (tokenizer.hasMoreTokens()) {
				fModes.add(tokenizer.nextToken().trim());
			}
		}
		return fModes;
	}
	
	/**
	 * @see ILaunchConfigurationType#getName()
	 */
	public String getName() {
		return getConfigurationElement().getAttribute("name"); //$NON-NLS-1$
	}

	/**
	 * @see ILaunchConfigurationType#getIdentifier()
	 */
	public String getIdentifier() {
		return getConfigurationElement().getAttribute("id"); //$NON-NLS-1$
	}

	/**
	 * @see ILaunchConfigurationType#isPublic()
	 */
	public boolean isPublic() {
		String publicString = getConfigurationElement().getAttribute("public"); //$NON-NLS-1$
		if (publicString != null) {
			if (publicString.equalsIgnoreCase("false")) { //$NON-NLS-1$
				return false;
			}
		} 
		return true;
	}

	/**
	 * @see ILaunchConfigurationType#newInstance(IContainer, String)
	 */
	public ILaunchConfigurationWorkingCopy newInstance(
		IContainer container,
		String name)
		throws CoreException {
			return new LaunchConfigurationWorkingCopy(container, name, this);
	}
	
	/**
	 * Returns the launch configuration delegate for launch
	 * configurations of this type. The first time this method
	 * is called, the delegate is instantiated.
	 * 
	 * @return launch configuration delegate
	 * @exception CoreException if unable to instantiate the
	 *  delegate
	 */
	public ILaunchConfigurationDelegate getDelegate() throws CoreException {
		if (fDelegate == null) {
			fDelegate = (ILaunchConfigurationDelegate)getConfigurationElement().createExecutableExtension("delegate"); //$NON-NLS-1$
		}
		return fDelegate;
	}

}

