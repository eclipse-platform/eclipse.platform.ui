/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime;

/**
 * Plug-in events describe changes in the lifecycle of plug-ins. 
 * <p> 
 * This interface is not intended to be implemented by clients.
 * </p>
 * @since 3.0
 */
public interface IPluginEvent {
	/**
	 * This plugin has been installed.
	 * <p>The value of <tt>INSTALLED</tt> is 0x00000001.
	 *
	 */
	public final static int INSTALLED = 0x00000001;

	/**
	 * This plugin has been started.
	 * <p>The value of <tt>STARTED</tt> is 0x00000002.
	 *
	 */
	public final static int STARTED = 0x00000002;

	/**
	 * This plugin has been stopped.
	 * <p>The value of <tt>STOPPED</tt> is 0x00000004.
	 *
	 */
	public final static int STOPPED = 0x00000004;

	/**
	 * This plugin has been updated.
	 * <p>The value of <tt>UPDATED</tt> is 0x00000008.
	 *
	 */
	public final static int UPDATED = 0x00000008;

	/**
	 * This plugin has been uninstalled.
	 * <p>The value of <tt>UNINSTALLED</tt> is 0x00000010.
	 *
	 */
	public final static int UNINSTALLED = 0x00000010;

	/**
	 * This plugin has been resolved.
	 * <p>The value of <tt>RESOLVED</tt> is 0x00000020.
	 *
	 */
	public final static int RESOLVED = 0x00000020;

	/**
	 * This plugin is no longer resolved.
	 * <p>The value of <tt>UNRESOLVED</tt> is 0x00000040.
	 *
	 */
	public final static int UNRESOLVED = 0x00000040;

	/**
	 * Returns the plugin descriptor for a plugin which had a lifecycle 
	 * change. This plugin is the source of the event.
	 *
	 * @return A plugin descriptor for a plugin that had a change 
	 * occur in its lifecycle.
	 */
	public IPluginDescriptor getPluginDescriptor();

	/**
	 * Returns the type of lifecyle event.
	 * The type values are:
	 * <ul>
	 * <li>{@link #INSTALLED}
	 * <li>{@link #STARTED}
	 * <li>{@link #STOPPED}
	 * <li>{@link #UPDATED}
	 * <li>{@link #UNINSTALLED}
	 * </ul>
	 *
	 * @return The type of lifecycle event.
	 */

	public int getType();
}
