/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.net.core;


/**
 * Event which describes a change in the proxy information managed by
 * the {@link IProxyManager}.
 * <p>
 * This interface is not intended to be implemented by clients.
 * @see IProxyManager
 * @since 1.0
 */
public interface IProxyChangeEvent {
	
	/**
	 * Type constant that indicates that the list of non-proxied hosts has changed.\
	 * @see #getChangeType()
	 */
	public static final int NONPROXIED_HOSTS_CHANGED = 1;
	
	/**
	 * Type constant that indicates that the data for one or more proxies has changed
	 * @see #getChangeType()
	 */
	public static final int PROXY_DATA_CHANGED = 2;

	/**
	 * Type constant that indicates that the enablement of the proxy 
	 * manager has changed. Client should consult the manager to determine
	 * the current enablement
	 * @see #getChangeType()
	 * @see IProxyManager#isProxiesEnabled()
	 */
	public static final int PROXY_MANAGER_ENABLEMENT_CHANGE = 3;
	
	/**
	 * Return the type of change this event represents. Clients
	 * should ignore types they do not recognize.
	 * @return the type of change this event represents
	 * @see #NONPROXIED_HOSTS_CHANGED
	 * @see #PROXY_DATA_CHANGED
	 */
	public int getChangeType();
	
	/**
	 * For a change type of {@link #NONPROXIED_HOSTS_CHANGED}, this method will
	 * return the list of non-proxied hosts before the change occurred.
	 * @return the list of non-proxied hosts before the change occurred
	 */
	public String[] getOldNonProxiedHosts();
	
	/**
	 * For a change type of {@link #NONPROXIED_HOSTS_CHANGED}, this method will
	 * return the list of non-proxied hosts after the change occurred.
	 * @return the list of non-proxied hosts after the change occurred
	 */
	public String[] getNonProxiedHosts();
	
	/**
	 * For a change type of {@link #PROXY_DATA_CHANGED}, this method returns
	 * the state of all known proxies before the change occurred.
	 * @return the state of all known proxies before the change occurred
	 */
	public IProxyData[] getOldProxyData();
	
	/**
	 * For a change type of {@link #PROXY_DATA_CHANGED}, this method returns
	 * the state of the changed known proxies after the change occurred. Clients 
	 * should check the {@link IProxyManager#isProxiesEnabled()} method to see
	 * if the proxy data change was the result of proxies being disabled.
	 * @return the state of the changed known proxies after the change occurred
	 */
	public IProxyData[] getChangedProxyData();

}
