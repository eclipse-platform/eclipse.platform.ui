package org.eclipse.debug.core;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.debug.core.model.ILauncherDelegate;
import org.eclipse.core.runtime.IConfigurationElement;
import java.util.Set;

/**
 * A launcher is a proxy to a launcher extension (<code>"org.eclipse.debug.core.launchers"</code>).
 * It manages the attributes of a launcher extension, and lazily instantiates the
 * delegate when required.
 * <p>
 * This interface is not intended to be implemented by clients. Clients
 * that define a launcher extension implement the <code>ILauncherDelegate</code>
 * interface.
 * </p>
 * <p>
 * <b>NOTE:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see ILauncherDelegate
 */
public interface ILauncher {
	/**
	 * Returns the result of invoking this underlying launcher extension's launch
	 * method. This causes the extension to be instantiated (if not already). 
	 *
	 * @param elements the elements providing context for the launch
	 * @param mode the mode in which to launch - run or debug
	 * @return whether the launch was successful
	 * @see ILauncherDelegate
	 */
	boolean launch(Object[] elements, String mode);
	
	/**
	 * Returns the modes that the underlying launcher extension
	 * supports - run and/or debug. This will not cause the extension to
	 * be instantiated.
	 *
	 * @return a set of <code>String</code> constants - one or both of
	 *	<code>ILaunchManager.RUN_MODE</code>, <code>ILaunchManager.DEBUG_MODE</code>
	 * @see ILaunchManager
	 */
	Set getModes();
	
	/**
	 * Returns the identifier of the perspective associated with the 
	 * underlying launcher extension. This attribute determines the layout
	 * used by the debug UI plug-in when presenting this launch.
	 * This will not cause the extension to be instantiated.
	 *
	 * @return a perspective identifier, as defined by the
	 *    <code>"org.eclipse.ui.perspective"</code> extension point
	 */
	String getPerspectiveIdentifier();
	
	/**
	 * Returns the label defined by the underlying launcher extension.
	 * This will not cause the extension to be instantiated.
	 *
	 * @return a human readable label
	 */
	String getLabel();
	
	/**
	 * Returns the configuration element associated with the underlying
	 * extension. This will not cause the extension to be instantiated.
	 *
	 * @return the extension's configuration element
	 */
	IConfigurationElement getConfigurationElement();
	
	/**
	 * Returns the unique identifier of the underlying launcher extension.
	 * This will not cause the extension to be instantiated.
	 *
	 * @return the extension's identifier attribute
	 */
	String getIdentifier();
	
	/**
	 * Returns the underlying launcher delegate associated with this
	 * launcher. This causes the launcher delegate to be instantiated (if
	 * not already).
	 *
	 * @return the underlying launcher extension
	 */
	ILauncherDelegate getDelegate();
}
