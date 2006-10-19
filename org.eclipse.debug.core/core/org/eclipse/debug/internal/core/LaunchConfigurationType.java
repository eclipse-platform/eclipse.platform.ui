/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core;

 
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationMigrationDelegate;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputer;

import com.ibm.icu.text.MessageFormat;

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
	 *  a listing of modes contributed to this launch configuration type
	 *  @since 3.3
	 *  
	 *  <p>
	 * <strong>EXPERIMENTAL</strong>. This field has been added as
	 * part of a work in progress. There is no guarantee that this API will
	 * remain unchanged during the 3.3 release cycle. Please do not use this API
	 * without consulting with the Platform/Debug team.
	 * </p>
	 */
	private Set fModes = null;
	
	/**
	 * the default source path computer for this config type
	 * @since 3.3
	 * 
	 * <p>
	 * <strong>EXPERIMENTAL</strong>. This field has been added as
	 * part of a work in progress. There is no guarantee that this API will
	 * remain unchanged during the 3.3 release cycle. Please do not use this API
	 * without consulting with the Platform/Debug team.
	 * </p>
	 */
	private ISourcePathComputer fSourcePathComputer = null;
	
	/**
	 * The source locator id for this config type
	 */
	private String fSourceLocator = null;
	
	/**
	 * The delegates for launch configurations of this type.
	 * Delegates are instantiated lazily as required. There may
	 * be different delegates for different modes (since 3.0).
	 * Map of modes (Set of modes) to list of delegates
	 */
	private Map fDelegates;
	
	private LaunchDelegate fSourceProvider;
	
	/**
	 * Constructs a new launch configuration type on the
	 * given configuration element.
	 * 
	 * @param element configuration element
	 */
	protected LaunchConfigurationType(IConfigurationElement element) {
		fElement = element;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationType#getAttribute(java.lang.String)
	 */
	public String getAttribute(String attributeName) {
		return fElement.getAttribute(attributeName);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationType#getCategory()
	 */
	public String getCategory() {
		return fElement.getAttribute(IConfigurationElementConstants.CATEGORY);
	}
	
	/**
	 * @see org.eclipse.debug.core.ILaunchConfigurationType#getDelegate()
	 */
	public ILaunchConfigurationDelegate getDelegate() throws CoreException {
		return getDelegate(ILaunchManager.RUN_MODE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationType#getDelegate(java.lang.String)
	 */
	public ILaunchConfigurationDelegate getDelegate(String mode) throws CoreException {
		Set modes = new HashSet();
		modes.add(mode);
		ILaunchConfigurationDelegate[] delegates = getDelegates(modes);
		if (delegates.length > 0) {
			return delegates[0];
		}
		IStatus status = null;
		ILaunchMode launchMode = DebugPlugin.getDefault().getLaunchManager().getLaunchMode(mode);
		if (launchMode == null) {
			status = new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(),
					MessageFormat.format(DebugCoreMessages.LaunchConfigurationType_7,
							new String[]{mode}));
		} else {
			status = new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(),
				MessageFormat.format(DebugCoreMessages.LaunchConfigurationType_7,
						new String[]{((LaunchManager)DebugPlugin.getDefault().getLaunchManager()).getLaunchModeName(mode)}));
		}
		throw new CoreException(status);
	}
	
	public ILaunchConfigurationDelegate[] getDelegates(Set modes) throws CoreException {
		initializeDelegates();
		Object[] theModes = modes.toArray();
		for (int i = 0; i < theModes.length; i++) {
			String mode = (String) theModes[i];
			if (!supportsMode(mode)) {
				throw new CoreException(new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugPlugin.INTERNAL_ERROR,
						MessageFormat.format(DebugCoreMessages.LaunchConfigurationType_9, new String[] {mode, getIdentifier()}), null));  
			}			
		}
		List delegates = (List) fDelegates.get(modes);
		if (delegates == null) {
			delegates = Collections.EMPTY_LIST;
		}
		ILaunchConfigurationDelegate[] result = new ILaunchConfigurationDelegate[delegates.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = ((LaunchDelegate)delegates.get(i)).getDelegate();
		}
		return result;
	}
	
	private synchronized void initializeDelegates() {
		if (fDelegates == null) {
			// initialize delegate
			fDelegates = new Hashtable();
			LaunchDelegate[] launchDelegates = getLaunchDelegateExtensions();
			for (int i = 0; i < launchDelegates.length; i++) {
				LaunchDelegate delegate = launchDelegates[i];
				List combintaions = delegate.getModeCombinations();
				Iterator iterator = combintaions.iterator();
				while (iterator.hasNext()) {
					Set combination = (Set) iterator.next();
					registerDelegate(delegate, combination);
				}
			}
		}		
	}

	/**
	 * Returns all launch delegate extensions registered for this configuration type.
	 * 
	 * @return all launch delegate extensions
	 */
	private LaunchDelegate[] getLaunchDelegateExtensions() {
		return ((LaunchManager) DebugPlugin.getDefault().getLaunchManager()).getLaunchDelegates(getIdentifier());
	}
	
	private void registerDelegate(LaunchDelegate delegate, Set modes) {
		List delegatesForModes = (List) fDelegates.get(modes);
		if (delegatesForModes == null) {
			delegatesForModes = new ArrayList();
			fDelegates.put(modes, delegatesForModes);
		}
		delegatesForModes.add(delegate);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationType#getIdentifier()
	 */
	public String getIdentifier() {
		return fElement.getAttribute(IConfigurationElementConstants.ID);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationType#getName()
	 */
	public String getName() {
		return fElement.getAttribute(IConfigurationElementConstants.NAME);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationType#getPluginId()
	 */
	public String getPluginIdentifier() {
		return fElement.getContributor().getName();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationType#getSourceLocatorId()
	 */
	public String getSourceLocatorId() {
		if(fSourceLocator == null) {
			fSourceLocator = getAttribute(IConfigurationElementConstants.SOURCE_LOCATOR);
			//see if the cached source provider knows about it
			if(fSourceProvider != null) {
				fSourceLocator = fSourceProvider.getSourceLocatorId();
			}
			//if not provided check all the applicable delegates for one and record the delegate if found,
			//so it can be reused to try and find the source path computer
			if(fSourceLocator == null) {
				LaunchDelegate[] delegates = getLaunchDelegateExtensions();
				for(int i = 0; i < delegates.length; i++) {
					fSourceLocator = delegates[i].getSourceLocatorId();
					if(fSourceLocator != null) {
						fSourceProvider = delegates[i];
						return fSourceLocator;
					}
				}
				fSourceProvider = null;
			}
		}
		return fSourceLocator;
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationType#getSourcePathComputer()
	 */
	public ISourcePathComputer getSourcePathComputer() {
		if(fSourcePathComputer == null) {
			//get the id
			String id = fElement.getAttribute(IConfigurationElementConstants.SOURCE_PATH_COMPUTER);
			//ask if the source provider knows about it
			if(fSourceProvider != null) {
				id = fSourceProvider.getSourcePathComputerId();
			}
			if(id != null) {
				fSourcePathComputer = DebugPlugin.getDefault().getLaunchManager().getSourcePathComputer(id);
			}
			else { 
			//if not provided check all the applicable delegates for one and record the delegate if found,
			//so it can be reused to try and find the source path computer
				LaunchDelegate[] delegates = getLaunchDelegateExtensions();
				for(int i = 0; i < delegates.length; i++) {
					id = delegates[i].getSourcePathComputerId();
					if(id != null) {
						fSourceProvider = delegates[i];
						fSourcePathComputer = DebugPlugin.getDefault().getLaunchManager().getSourcePathComputer(id);
						if(fSourcePathComputer != null) {
							return fSourcePathComputer;
						}
					}
				}
				fSourceProvider = null;
			}
			
		}
		return fSourcePathComputer;
	}

	/**
	 * @see org.eclipse.debug.core.ILaunchConfigurationType#getSupportedModes()
	 */
	public Set getSupportedModes() {
		if(fModes == null) {
			fModes = new HashSet();
			LaunchDelegate[] delegates = getLaunchDelegateExtensions();
			for(int i= 0; i < delegates.length; i++) {
				fModes.addAll(delegates[i].getModes());
			}
		}
		return fModes;
	}

	/**
	 * determines if the specified candidate is suitable for migration by loading it delegate.
	 * @param candidate the candidate to inspect for migration suitability
	 * @return true if the specified launch configuration is suitable for migration, false otherwise
	 * @throws CoreException
	 * 
	 * @since 3.2
	 */
	public boolean isMigrationCandidate(ILaunchConfiguration candidate) throws CoreException {
		if(getAttribute(IConfigurationElementConstants.MIGRATION_DELEGATE) != null) {
			if(fDelegates == null) {
				fDelegates = new Hashtable();
			}
			Object delegate = fDelegates.get(IConfigurationElementConstants.MIGRATION_DELEGATE);
			if(delegate == null) {
				delegate = fElement.createExecutableExtension(IConfigurationElementConstants.MIGRATION_DELEGATE);
				fDelegates.put(IConfigurationElementConstants.MIGRATION_DELEGATE, delegate);
			}
			if(delegate instanceof ILaunchConfigurationMigrationDelegate) {
				return ((ILaunchConfigurationMigrationDelegate)delegate).isCandidate(candidate);
			}
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationType#isPublic()
	 */
	public boolean isPublic() {
		String publicString = fElement.getAttribute(IConfigurationElementConstants.PUBLIC);
		if (publicString != null) {
			if (publicString.equalsIgnoreCase("false")) { //$NON-NLS-1$
				return false;
			}
		} 
		return true;
	}

	/**
	 * migrates the specified launch configuration by loading its delegate
	 * @param candidate the candidate launch configuration to migrate
	 * @throws CoreException
	 * 
	 * @since 3.2
	 */
	public void migrate(ILaunchConfiguration candidate) throws CoreException {
		if(getAttribute(IConfigurationElementConstants.MIGRATION_DELEGATE) != null) { 
			if(fDelegates == null) {
				fDelegates = new Hashtable();
			}
			Object delegate = fDelegates.get(IConfigurationElementConstants.MIGRATION_DELEGATE);
			if(delegate == null) {
				delegate = fElement.createExecutableExtension(IConfigurationElementConstants.MIGRATION_DELEGATE);
				fDelegates.put(IConfigurationElementConstants.MIGRATION_DELEGATE, delegate);
			}
			if(delegate instanceof ILaunchConfigurationMigrationDelegate) {
				((ILaunchConfigurationMigrationDelegate)delegate).migrate(candidate);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationType#newInstance(org.eclipse.core.resources.IContainer, java.lang.String)
	 */
	public ILaunchConfigurationWorkingCopy newInstance(IContainer container, String name) {
		return new LaunchConfigurationWorkingCopy(container, name, this);
	}

	/**
	 * @see org.eclipse.debug.core.ILaunchConfigurationType#supportsMode(java.lang.String)
	 */
	public boolean supportsMode(String mode) {
		return getSupportedModes().contains(mode);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationType#getContributorName()
	 */
	public String getContributorName() {
		return fElement.getContributor().getName();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationType#getImageDescriptorId()
	 */
	public String getImageDescriptorPath() {
		return fElement.getAttribute(IConfigurationElementConstants.ICON);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationType#getSupportedModeCombinations()
	 */
	public Set[] getSupportedModeCombinations() {
		initializeDelegates();
		Set combinations = fDelegates.keySet();
		return (Set[])combinations.toArray(new Set[combinations.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationType#supportsModeCombination(java.util.Set)
	 */
	public boolean supportsModeCombination(Set modes) {
		initializeDelegates();
		return fDelegates.containsKey(modes);
	}
}

