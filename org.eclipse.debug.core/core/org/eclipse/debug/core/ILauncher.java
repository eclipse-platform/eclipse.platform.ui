package org.eclipse.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.debug.core.model.ILauncherDelegate;

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
	 * Returns the result of invoking this launcher's underlying
	 * delegate's launch method. This causes the delegate to be instantiated
	 * (if not already instantiated). 
	 *
	 * @param elements the elements providing context for the launch
	 * @param mode the mode in which to launch - run or debug
	 * @return whether the launch was successful
	 * @see ILauncherDelegate
	 */
	public boolean launch(Object[] elements, String mode);
	
	/**
	 * Returns the modes that the underlying launcher extension
	 * supports - run and/or debug. This will not cause the extension to
	 * be instantiated.
	 *
	 * @return a set of <code>String</code> constants - one or both of
	 *	<code>ILaunchManager.RUN_MODE</code>, <code>ILaunchManager.DEBUG_MODE</code>
	 * @see ILaunchManager
	 */
	public Set getModes();
	
	/**
	 * Returns the identifier of the perspective associated with the 
	 * underlying launcher extension. This attribute determines the layout
	 * used by the debug UI plug-in when presenting this launch.
	 * This will not cause the extension to be instantiated.
	 *
	 * @return a perspective identifier, as defined by the
	 *    <code>"org.eclipse.ui.perspective"</code> extension point
	 */
	public String getPerspectiveIdentifier();
	
	/**
	 * Returns the label defined by the underlying launcher extension.
	 * This will not cause the extension to be instantiated.
	 *
	 * @return a human readable label
	 */
	public String getLabel();
	
	/**
	 * Returns the configuration element associated with the underlying
	 * extension. This will not cause the extension to be instantiated.
	 *
	 * @return the extension's configuration element
	 */
	public IConfigurationElement getConfigurationElement();
	
	/**
	 * Returns the unique identifier of the underlying launcher extension.
	 * This will not cause the extension to be instantiated.
	 *
	 * @return the extension's identifier attribute
	 */
	public String getIdentifier();
	
	/**
	 * Returns the underlying launcher delegate associated with this
	 * launcher. This causes the launcher delegate to be instantiated (if
	 * not already instantiated).
	 *
	 * @return the underlying launcher delegate
	 */
	public ILauncherDelegate getDelegate();
	
	/**
	 * Returns the icon path defined by the underlying launcher extension.
	 * The path is relative to the plugin.xml of the contributing plug-in.
	 * This will not cause the extension to be instantiated. Returns
	 * <code>null</code> if no icon attribute is specified.
	 *
	 * @return a relative path to an icon or <code>null</code>
	 */
	public String getIconPath();	
}
