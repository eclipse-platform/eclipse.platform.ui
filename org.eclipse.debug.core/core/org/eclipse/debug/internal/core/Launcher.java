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
	protected IConfigurationElement fConfigElement = null;
	
	/**
	 * The underlying launcher, which is <code>null</code> until the
	 * it needs to be instantiated.
	 */
	protected ILauncherDelegate fDelegate = null;
	
	/**
	 * Cache of the modes this launcher supports
	 */
	protected Set fModes;
	
	/**
	 * Constructs a handle for a launcher extension.
	 */
	public Launcher(IConfigurationElement element) {
		fConfigElement = element;
	}
	
	/**
	 * @see ILauncher
	 */
	public String getIdentifier() {
		return fConfigElement.getAttribute("id");
	}
	
	/**
	 * Returns the set of modes specified in the configuration data.
	 * The set contains "mode" constants defined in <code>ILaunchManager</code>
	 */
	public Set getModes() {
		if (fModes == null) {
			String modes= fConfigElement.getAttribute("modes");
			if (modes == null) {
				return null;
			}
			StringTokenizer tokenizer= new StringTokenizer(modes, ",");
			fModes = new HashSet(tokenizer.countTokens());
			while (tokenizer.hasMoreTokens()) {
				fModes.add(tokenizer.nextToken().trim());
			}
		}
		return fModes;
	}
	
	/**
	 * @see ILauncher.
	 */
	public String getLabel() {
		return fConfigElement.getAttribute("label");
	}
	
	/**
	 * @see ILauncher
	 */
	public String getPerspectiveIdentifier() {
		return fConfigElement.getAttribute("perspective");
	}

	/**
	 * Returns the launcher for this handle, instantiating it if required.
	 */
	public ILauncherDelegate getDelegate() {
		if (fDelegate == null) {
			try {
				fDelegate = (ILauncherDelegate)fConfigElement.createExecutableExtension("class");
			} catch (CoreException e) {
				//status logged in the #createExecutableExtension code
			}
		}	
		return fDelegate;
	}
		
	/**
	 * @see ILauncher
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
	 * @see ILanucher.
	 */
	public String getIconPath() {
		return fConfigElement.getAttribute("icon");
	}
}