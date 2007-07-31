/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchDelegate;
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
          <modeCombination 
    		modes="run, profile">
    		perspective="com.example.Perspective">
   		  </modeCombination>
      </launchDelegate>
 * </pre>
 * 
 * Clients are NOT intended to subclass this class
 * 
 * @see IConfigurationElementConstants
 * 
 * @since 3.3
 */
public final class LaunchDelegate implements ILaunchDelegate {
	
	/**
	 * The configuration element for this delegate
	 */
	private IConfigurationElement fElement = null;
	
	/**
	 * The cached delegate. Remains null until asked for, then persisted
	 */
	private ILaunchConfigurationDelegate fDelegate = null;
	
	//a listing of sets of 
	private List fLaunchModes = null;
	private String fType = null;
	private HashMap fPerspectiveIds = null;
	
	/**
	 * Constructor
	 * @param element the configuration element to associate with this launch delegate
	 */
	public LaunchDelegate(IConfigurationElement element) {
		fElement = element;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchDelegateProxy#getDelegate()
	 */
	public ILaunchConfigurationDelegate getDelegate() throws CoreException {
		if(fDelegate == null) {
			Object obj = fElement.createExecutableExtension(IConfigurationElementConstants.DELEGATE);
			if(obj instanceof ILaunchConfigurationDelegate) {
				fDelegate = (ILaunchConfigurationDelegate)obj;
			} else {
				throw new CoreException(new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugPlugin.ERROR, MessageFormat.format(DebugCoreMessages.LaunchDelegate_3, new String[]{getId()}), null)); 
			}
		}
		return fDelegate;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchDelegateProxy#getId()
	 */
	public String getId() {
		return fElement.getAttribute(IConfigurationElementConstants.ID);
	}

	/**
	 * Returns the id of the associated <code>ILaunchConfigurationType</code> or <code>null</code> if none provided
	 * @return the id of the <code>ILaunchConfigurationType</code> associated with this delegate
	 */
	public String getLaunchConfigurationTypeId() {
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
	 * Simple method to parse mode strings (separated by commas)
	 * @param element the config element to read the mode string from
	 * @return a set of the parsed strings or an empty collection
	 * @since 3.3
	 */
	private Set parseModes(IConfigurationElement element) {
		HashSet set = new HashSet();
		String modes = element.getAttribute(IConfigurationElementConstants.MODES); 
		if (modes != null) {
			String[] strings = modes.split(","); //$NON-NLS-1$
			for (int i = 0; i < strings.length; i++) {
				set.add(strings[i].trim());
			}
		}
		return set;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchDelegateProxy#getModes()
	 */
	public List getModes() {
		if(fLaunchModes == null) {
			fLaunchModes = new ArrayList();
			fPerspectiveIds = new HashMap();
			IConfigurationElement[] children = fElement.getChildren(IConfigurationElementConstants.MODE_COMBINATION);
			Set modeset = null;
			for (int i = 0; i < children.length; i++) {
				modeset = parseModes(children[i]);
				fLaunchModes.add(modeset);
				fPerspectiveIds.put(modeset, children[i].getAttribute(IConfigurationElementConstants.PERSPECTIVE));
			}
			//try to get the modes from the old definition and make each one
			//a separate set of one element
			modeset = null;
			String modes = fElement.getAttribute(IConfigurationElementConstants.MODES); 
			if (modes != null) {
				String[] strings = modes.split(","); //$NON-NLS-1$
				for (int i = 0; i < strings.length; i++) {
					modeset = new HashSet();
					modeset.add(strings[i].trim());
					fLaunchModes.add(modeset);
				}
			}
		}
		return fLaunchModes;
	}
	
	/**
	 * Returns the human readable name for this launch delegate
	 * @return the human readable name for this launch delegate, or <code>null</code> if none
	 */
	public String getName() {
		//try a delegateName attribute first, in the event this delegate was made from an ILaunchConfigurationType
		String name = fElement.getAttribute(IConfigurationElementConstants.DELEGATE_NAME);
		if(name == null) {
			name = fElement.getAttribute(IConfigurationElementConstants.NAME);
			if (name == null) {
				name = getContributorName();
			}
			name = name.trim();
			if (Character.isUpperCase(name.charAt(0))) {
				name =  MessageFormat.format(DebugCoreMessages.LaunchDelegate_1, new String[]{name});
			} else {
				name = MessageFormat.format(DebugCoreMessages.LaunchDelegate_2, new String[]{name});
			}
		}
		return name;
	}
	
	/**
	 * Returns the contributor name of this delegate (plug-in name).
	 * 
	 * @return contributor name
	 */
	public String getContributorName() {
		return fElement.getContributor().getName();
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
	 * @see org.eclipse.debug.core.ILaunchDelegate#getDescription()
	 */
	public String getDescription() {
		String desc = fElement.getAttribute(IConfigurationElementConstants.DELEGATE_DESCRIPTION);
		if(desc == null) {
			return DebugCoreMessages.LaunchDelegate_0;
		}
		return desc;
	}
	
	/**
	 * @see org.eclipse.debug.core.ILaunchDelegate#getPluginIdentifier()
	 */
	public String getPluginIdentifier() {
		return fElement.getContributor().getName();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if(obj == null) {
			return false;
		}
		return obj instanceof ILaunchDelegate && getId() != null && getId().equals(((ILaunchDelegate)obj).getId());
	}

	/**
	 * @see org.eclipse.debug.core.ILaunchDelegate#getPerspectiveId(java.util.Set)
	 */
	public String getPerspectiveId(Set modes) {
		if(fPerspectiveIds == null) {
			getModes();
		}
		return (String) fPerspectiveIds.get(modes);
	}
}
