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
package org.eclipse.debug.internal.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;

import com.ibm.icu.text.MessageFormat;

/**
 * Proxy to a launch delegate extension
 * Clients can contribute launch delegates through the <code>launchDelegates</code> extension point
 * 
 * Example contribution of the local java launch delegate
 * <pre>
 * <extension point="org.eclipse.debug.core.launchDelegates">
      <launchDelegate
            delegate="org.eclipse.jdt.launching.JavaLaunchDelegate"
            id="org.eclipse.jdt.launching.localJavaApplicationDelegate"
            modes="run, debug"
            name="%localJavaApplication"
            type="org.eclipse.jdt.launching.localJavaApplication">
      </launchDelegate>
 * </pre>
 * 
 * Clients are NOT intended to subclass this class
 * 
 * @since 3.3
 */
public final class LaunchDelegate {
	
	/**
	 * The configuration element for this delegate
	 */
	private IConfigurationElement fElement = null;
	
	/**
	 * The cached delegate. Remains null until asked for, then persisted
	 */
	private ILaunchConfigurationDelegate fDelegate = null;
	
	//lists of cached entries
	private HashSet fModes = null;
	private HashSet fOptions = null;
	private String fType = null;
	
	/**
	 * Constructor
	 * @param element the configuration element to associate with this launch delegate
	 */
	public LaunchDelegate(IConfigurationElement element) {
		fElement = element;
	}
	
	/**
	 * Returns the actual instance of the launch delegate specified 
	 * @return the launch delegate
	 */
	public ILaunchConfigurationDelegate getDelegate() throws CoreException {
		if(fDelegate == null) {
			Object obj = fElement.createExecutableExtension(IConfigurationElementConstants.DELEGATE);
			if(obj instanceof ILaunchConfigurationDelegate) {
				fDelegate = (ILaunchConfigurationDelegate)obj;
			} else {
				throw new CoreException(new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugPlugin.INTERNAL_ERROR, MessageFormat.format(DebugCoreMessages.LaunchConfigurationType_Launch_delegate_for__0__does_not_implement_required_interface_ILaunchConfigurationDelegate__1, new String[]{getIdentifier()}), null));
			}
		}
		return fDelegate;
	}

	/**
	 * @return returns the unique id of the delegate
	 */
	public String getIdentifier() {
		return fElement.getAttribute(IConfigurationElementConstants.ID);
	}

	/**
	 * Returns the id of the associated <code>ILaunchConfigurationType</code> or <code>null</code> if none provided
	 * @return the id of the <code>ILaunchConfigurationType</code> associated with this delegate
	 */
	public String getLaunchConfigurationType() {
		if(fType == null) {
			//fall back to single association if no appliesTo
			fType = fElement.getAttribute(IConfigurationElementConstants.TYPE);
			if(fType == null) {
				//the case when we have passed a launch configuration type to the launch delegate
				fType = fElement.getAttribute(IConfigurationElementConstants.ID);
			}
		}
		return fType;
	}

	/**
	 * Returns the set of options provided to by this delegate
	 * @return the options associated with this delegate. If no options are specified an empty set is
	 * returned, never <code>null</code>.
	 */
	public Set getOptions() {
		if(fOptions == null) {
			fOptions = new HashSet();
			String option = fElement.getAttribute(IConfigurationElementConstants.OPTIONS);
			if(option != null) {
				String[] options = option.split(","); //$NON-NLS-1$
				for(int i = 0; i < options.length; i++) {
					fOptions.add(options[i].trim());
				}
			}
		}
		return fOptions;
	}
	
	/**
	 * This method is provided as a backward compatibility measure to allow access to modes, if mode-based
	 * launching is still being used.
	 * 
	 * @return a set of modes for this delegate or the empty set if none are found, never <code>null</code>.
	 */
	public Set getModes() {
		if (fModes == null) {
			fModes = new HashSet();
			String modes = fElement.getAttribute(IConfigurationElementConstants.MODES); 
			if (modes != null) {
				String[] strings = modes.split(","); //$NON-NLS-1$
				for (int i = 0; i < strings.length; i++) {
					fModes.add(strings[i].trim());
				}
			}
		}
		return fModes;
	}
	
	/**
	 * Returns the human readable name for this launch delegate
	 * @return the human readable name for this launch delegate, or <code>null</code> if none
	 */
	public String getName() {
		return fElement.getAttribute(IConfigurationElementConstants.NAME);
	}
	
	/**
	 * Returns the associated source locator id or <code>null</code>
	 * @return the associated source locator id or <code>null</code> if not provided
	 */
	public String getSourceLocatorId() {
		return fElement.getAttribute(IConfigurationElementConstants.SOURCE_LOCATOR);
	}

	/**
	 * Returns the associated source path computer id or <code>null</code>
	 * @return the associated source path computer id or <code>null</code> if not provided
	 */
	public String getSourcePathComputerId() {
		return fElement.getAttribute(IConfigurationElementConstants.SOURCE_PATH_COMPUTER);
	}

	/**
	 * Returns all combinations of supported options.
	 * 
	 * @return combinations of supported options
	 */
	private Collection getOptionSets() {
		Set optionSets = new HashSet(); 
		optionSets.add(new HashSet()); // seed with the empty option set
		Object[] options = getOptions().toArray();
		boolean grew = false;
		do {
			grew = false;
			Set[] sets = (Set[]) optionSets.toArray(new Set[optionSets.size()]);
			for (int i = 0; i < sets.length; i++) {
				Set optionSet = sets[i];
				for (int j = 0; j < options.length; j++) {
					Object option = options[j];
					Set newOptionSet = new HashSet(optionSet);
					if (newOptionSet.add(option)) {
						if (optionSets.add(newOptionSet)) {
							grew = true;
						}
					}
				}				
			}                                   
		} while (grew);
		return optionSets;
	}
	
	/**
	 * Returns all supported launch mode combinations as sets of modes.
	 *  
	 * @return all supported launch mode combinations
	 */
	List getModeCombinations() {
		Collection optionSets = getOptionSets();
		Object[] modes = getModes().toArray();
		List combinations = new ArrayList(optionSets.size() * modes.length);
		Iterator iterator = optionSets.iterator();
		while (iterator.hasNext()) {
			Set optionSet = (Set) iterator.next();
			for (int i = 0; i < modes.length; i++) {
				Object mode = modes[i];
				Set set = new HashSet(optionSet);
				set.add(mode);
				combinations.add(set);
			}			
		}
		return combinations;
	}
}
