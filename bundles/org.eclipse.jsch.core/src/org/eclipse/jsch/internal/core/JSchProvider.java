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
package org.eclipse.jsch.internal.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jsch.core.IJSchService;

import com.jcraft.jsch.*;

/**
 * A static class whose purpose is to ensure that all the JSch preferences are properly pushed into JSch
 * before a {@link Session} is created.
 * @since 1.0
 */
public class JSchProvider implements IJSchService {
  
  private static JSchProvider instance;

  /* (non-Javadoc)
   * @see org.eclipse.jsch.core.IJSchService#createSession(java.lang.String, int, java.lang.String)
   */
  public Session createSession(String host, int port, String username) throws JSchException {
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
  
  /* (non-Javadoc)
   * @see org.eclipse.jsch.core.IJSchService#connect(com.jcraft.jsch.Session, int, org.eclipse.core.runtime.IProgressMonitor)
   */
  public void connect(Session session, int timeout,
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
  
  /* (non-Javadoc)
   * @see org.eclipse.jsch.core.IJSchService#getProxyForHost(java.lang.String, java.lang.String)
   */
  public Proxy getProxyForHost(String host, String proxyType) {
    return Utils.getProxyForHost(host, proxyType);
  }

  public static IJSchService getInstance(){
    if (instance == null)
      instance = new JSchProvider();
    return instance;
  }

}
