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
package org.eclipse.jsch.core;

import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jsch.internal.core.*;

import com.jcraft.jsch.*;

/**
 * A static class whose purpose is to ensure that all the Jsch preferences are properly pushed into Jsch
 * before a {@link Session} is created.
 * @since 1.0
 */
public class JSchProvider {
  
  /**
   * Create a {@link Session} that can be used to make SSH2 connections. This method ensures that 
   * all preferences are properly propagated into JSch before creating the session and also
   * ensures that the session uses the appropriate proxy settings.
   * <p>
   * Calling this method does not connect the session (see {@link #connect(Session, int, IProgressMonitor)}, if connection
   * throws an exception, clients should check to see if the session is still connected (see {@link Session#isConnected()}.
   * If it is, they should call {@link Session#disconnect()}.
   * 
   * @param host the host name
   * @param port the port or -1 if the default port is to be used
   * @param username the user name or <code>null</code> if there is no user name or the user name is not known

   * @return the created session
   * @throws JSchException if errors occur
   */
  public static Session createSession(String host, int port, String username) throws JSchException {
    if(port == -1)
      port = IConstants.SSH_DEFAULT_PORT;

    if(JSchCorePlugin.getPlugin().isNeedToLoadKnownHosts()){
      JSchCorePlugin.getPlugin().loadKnownHosts();
    }

    if(JSchCorePlugin.getPlugin().isNeedToLoadKeys()){
      JSchCorePlugin.getPlugin().loadPrivateKeys();
    }
    
    return Utils.createSession(JSchCorePlugin.getPlugin().getJSch(), username, host, port);
  }
  
  /**
   * Connect the session using a responsive socket factory. The timeout value is used
   * for socket creation only. Clients that desire a timeout on the session must
   * call {@link Session#setTimeout(int)}. If session connection fails due to an exception,
   * the session will be disconnected by this method.
   * 
   * @param session the session to be connected
   * @param timeout
   *          a timeout in milliseconds
   * @param monitor
   *          a progress monitor or <code>null</code> if progress and
   *          cancelation is not desired
   * @throws JSchException if an exception occurs connecting the session.
   */
  public static void connect(Session session, int timeout,
      IProgressMonitor monitor) throws JSchException{
    session.setSocketFactory(new ResponsiveSocketFactory(monitor, timeout));
    try{
      session.connect();
    }
    catch(JSchException e){
      if(session.isConnected())
        session.disconnect();
      throw e;
    }
  }
  
  /**
   * Return the proxy that should be used to connect to the given host or <code>null</code>
   * if no proxy is specified for the host.
   * @param host the host
   * @param proxyType the proxy type (either {@link IProxyData#HTTPS_PROXY_TYPE} or {@link IProxyData#SOCKS_PROXY_TYPE})
   * @return the proxy that should be used to connect to the given host or <code>null</code>
   * if no proxy is specified for the host
   */
  public static Proxy getProxyForHost(String host, String proxyType) {
    return Utils.getProxyForHost(host, proxyType);
  }

}
