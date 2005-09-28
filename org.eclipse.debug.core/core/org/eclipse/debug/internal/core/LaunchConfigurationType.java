/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core;

 
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputer;

/**
 * A launch configuration type wrappers a configuration
 * element for a <code>launchConfigurationType</code>
 * extension.
 */
public class LaunchConfigurationType extends PlatformObject implements ILaunchConfigurationType {
	
	/**
	 * The configuration element of the extension.
	 */
	private IConfigurationElement fElement;
	
	/**
	 * Base modes this type supports.
	 */
	private Set fBaseModes;
	
	/**
	 * Modes that delegates have been contributed for
	 */
	private Set fContributedModes;
	
	/**
	 * The delegates for launch configurations of this type.
	 * Delegates are instantiated lazily as required. There may
	 * be different delegates for different modes (since 3.0).
	 * Map of mode -> delegate
	 */
	private Map fDelegates;
	
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
		return getBaseModes().contains(mode) || getContributedModes().contains(mode);
	}

	/**
	 * Returns the set of modes specified in the configuration data.
	 * 
	 * @return the set of modes specified in the configuration data
	 */
	protected Set getBaseModes() {
		if (fBaseModes == null) {
			String modes= getConfigurationElement().getAttribute("modes"); //$NON-NLS-1$
			if (modes == null) {
				return new HashSet(0);
			}
			StringTokenizer tokenizer= new StringTokenizer(modes, ","); //$NON-NLS-1$
			fBaseModes = new HashSet(tokenizer.countTokens());
			while (tokenizer.hasMoreTokens()) {
				fBaseModes.add(tokenizer.nextToken().trim());
			}
		}
		return fBaseModes;
	}
	
	/**
	 * Returns all of the modes supported by this launch configuration type
	 * fix for bug 79709
	 * @return the complete listing of supported modes for this launch configuration
	 * @since 3.2
	 */
	public Set getSupportedModes() {
		HashSet modes = new HashSet(getBaseModes());
		modes.addAll(getContributedModes());
		return modes;
	}//end getSupportedModes
	
	/**
	 * Returns the set of modes delegates have been contributed for
	 * 
	 * @return the set of modes delegates have been contributed for
	 */
	protected Set getContributedModes() {
		if (fContributedModes == null) {
			fContributedModes = new HashSet(0);
			// add modes for contributed delegates
			List delegates = ((LaunchManager)DebugPlugin.getDefault().getLaunchManager()).getContributedDelegates();
			Iterator iterator = delegates.iterator();
			while (iterator.hasNext()) {
				ContributedDelegate delegate = (ContributedDelegate)iterator.next();
				if (delegate.getLaunchConfigurationType().equals(getIdentifier())) {
					fContributedModes.addAll(delegate.getModes());
				}
			}
		}
		return fContributedModes;
	}

	/**
	 * @see ILaunchConfigurationType#getName()
	 */
	public String getName() {
		return getConfigurationElement().getAttribute("name"); //$NON-NLS-1$
	}
	
	/**
	 * @see ILaunchConfigurationType#getSourcePathComputer()
	 */
	public ISourcePathComputer getSourcePathComputer() {
		String id = getConfigurationElement().getAttribute("sourcePathComputerId"); //$NON-NLS-1$
		if (id == null) {
			// check for specification by mode specific delegate
			List delegates = ((LaunchManager)DebugPlugin.getDefault().getLaunchManager()).getContributedDelegates();
			Iterator iterator = delegates.iterator();
			while (iterator.hasNext() && id == null) {
				ContributedDelegate delegate = (ContributedDelegate)iterator.next();
				if (delegate.getLaunchConfigurationType().equals(getIdentifier())) {
					id = delegate.getSourcePathComputerId();
				}
			}
		}
		if (id != null && id.length() > 0) {
			return DebugPlugin.getDefault().getLaunchManager().getSourcePathComputer(id);
		}
		return null;
	}

	/**
	 * @see ILaunchConfigurationType#getIdentifier()
	 */
	public String getIdentifier() {
		return getConfigurationElement().getAttribute("id"); //$NON-NLS-1$
	}

	/**
	 * @see ILaunchConfigurationType#getCategory()
	 */
	public String getCategory() {
		return getConfigurationElement().getAttribute("category"); //$NON-NLS-1$
	}
	
	/**
	 * @see ILaunchConfigurationType#getAttribute(String)
	 */
	public String getAttribute(String attributeName) {
		return getConfigurationElement().getAttribute(attributeName);
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
	public ILaunchConfigurationWorkingCopy newInstance(IContainer container, String name) {
			return new LaunchConfigurationWorkingCopy(container, name, this);
	}
	
	/**
	 * Returns the launch configuration delegate for launch
	 * configurations of this type. The first time this method
	 * is called, the delegate is instantiated.
	 * 
	 * @see org.eclipse.debug.core.ILaunchConfigurationType#getDelegate()
	 * @return launch configuration delegate
	 * @exception CoreException if unable to instantiate the
	 *  delegate
	 * @deprecated use <code>getDelegate(String)</code> to specify mode
	 */
	public ILaunchConfigurationDelegate getDelegate() throws CoreException {
		return getDelegate(ILaunchManager.RUN_MODE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationType#getDelegate(java.lang.String)
	 */
	public ILaunchConfigurationDelegate getDelegate(String mode) throws CoreException {
		if (!supportsMode(mode)) {
			throw new CoreException(new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugPlugin.INTERNAL_ERROR, MessageFormat.format(DebugCoreMessages.LaunchConfigurationType_9, new String[] {mode, getIdentifier()}), null));  
		}
		if (fDelegates == null) {
			// initialize delegate table with base modes
			fDelegates = new Hashtable(3);
		}
		ILaunchConfigurationDelegate delegate = (ILaunchConfigurationDelegate)fDelegates.get(mode);
		if (delegate == null) {
			Set modes = getBaseModes();
			if (modes.contains(mode)) {
				Object object = getConfigurationElement().createExecutableExtension("delegate"); //$NON-NLS-1$
				if (object instanceof ILaunchConfigurationDelegate) {
					Iterator iter = modes.iterator();
					while (iter.hasNext()) {
						fDelegates.put(iter.next(), object);
					}
					return (ILaunchConfigurationDelegate)object;
				} 
				throw new CoreException(new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugPlugin.INTERNAL_ERROR, MessageFormat.format(DebugCoreMessages.LaunchConfigurationType_Launch_delegate_for__0__does_not_implement_required_interface_ILaunchConfigurationDelegate__1, new String[]{getName()}), null)); 
			} 
			// contributed modes
			List contributed = ((LaunchManager)DebugPlugin.getDefault().getLaunchManager()).getContributedDelegates();
			Iterator iterator = contributed.iterator();
			while (iterator.hasNext()) {
				ContributedDelegate contributedDelegate = (ContributedDelegate)iterator.next();
				if (getIdentifier().equals(contributedDelegate.getLaunchConfigurationType())) {
					modes = contributedDelegate.getModes();
					if (modes.contains(mode)) {
						delegate = contributedDelegate.getDelegate();
						Iterator modesIterator = modes.iterator();
						while (modesIterator.hasNext()) {
							fDelegates.put(modesIterator.next(), delegate); 
						}
						return delegate;
					}
				}
			}
		} else {
			return delegate;
		}
		throw new CoreException(new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugPlugin.INTERNAL_ERROR, MessageFormat.format(DebugCoreMessages.LaunchConfigurationType_10, new String[] {getIdentifier(), mode}), null)); 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationType#getSourceLocatorId()
	 */
	public String getSourceLocatorId() {
		String id = getAttribute("sourceLocatorId"); //$NON-NLS-1$
		if (id == null) {
			// check for specification by mode specific delegate
			List delegates = ((LaunchManager)DebugPlugin.getDefault().getLaunchManager()).getContributedDelegates();
			Iterator iterator = delegates.iterator();
			while (iterator.hasNext() && id == null) {
				ContributedDelegate delegate = (ContributedDelegate)iterator.next();
				if (delegate.getLaunchConfigurationType().equals(getIdentifier())) {
					id = delegate.getSourceLocaterId();
				}
			}
		}
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationType#getPluginId()
	 */
	public String getPluginIdentifier() {
		return fElement.getNamespace();
	}
}

