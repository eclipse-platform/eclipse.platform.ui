package org.eclipse.debug.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.debug.core.ILauncher;
import org.eclipse.debug.core.model.ILauncherDelegate;

/**
 * A handle to a launcher extension that instantiates the actual
 * extension lazily.
 */
public class Launcher implements ILauncher {
	
	/**
	 * The configuration element that defines this launcher handle
	 */
	private IConfigurationElement fConfigElement = null;
	
	/**
	 * The underlying launcher, which is <code>null</code> until
	 * it needs to be instantiated.
	 */
	private ILauncherDelegate fDelegate = null;
	
	/**
	 * Cache of the modes this launcher supports
	 */
	private Set fModes;
	
	/**
	 * Constructs a handle for a launcher extension.
	 */
	public Launcher(IConfigurationElement element) {
		fConfigElement = element;
	}
	
	/**
	 * @see ILauncher#getIdentifier()
	 */
	public String getIdentifier() {
		return fConfigElement.getAttribute("id"); //$NON-NLS-1$
	}
	
	/**
	 * Returns the set of modes specified in the configuration data.
	 * The set contains "mode" constants defined in <code>ILaunchManager</code>
	 */
	public Set getModes() {
		if (fModes == null) {
			String modes= fConfigElement.getAttribute("modes"); //$NON-NLS-1$
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
	 * @see ILauncher#getLabel()
	 */
	public String getLabel() {
		return fConfigElement.getAttribute("label"); //$NON-NLS-1$
	}
	
	/**
	 * @see ILauncher#getPerspectiveIdentifier()
	 */
	public String getPerspectiveIdentifier() {
		return fConfigElement.getAttribute("perspective"); //$NON-NLS-1$
	}

	/**
	 * Returns the launcher for this handle, instantiating it if required.
	 */
	public ILauncherDelegate getDelegate() {
		if (fDelegate == null) {
			try {
				fDelegate = (ILauncherDelegate)fConfigElement.createExecutableExtension("class"); //$NON-NLS-1$
			} catch (CoreException e) {
				//status logged in the #createExecutableExtension code
			}
		}	
		return fDelegate;
	}
		
	/**
	 * @see ILauncher#launch(Object[], String)
	 */
	public boolean launch(Object[] elements, String mode) {
		return getDelegate().launch(elements, mode, this);
	}
		
	/**
	 * Returns the configuration element for this extension
	 */
	public IConfigurationElement getConfigurationElement() {
		return fConfigElement;
	}
	
	/**
	 * @see ILauncher#getIconPath()
	 */
	public String getIconPath() {
		return fConfigElement.getAttribute("icon"); //$NON-NLS-1$
	}
}