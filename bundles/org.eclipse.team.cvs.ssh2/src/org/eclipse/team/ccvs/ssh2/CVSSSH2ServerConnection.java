/* -*-mode:java; c-basic-offset:2; -*- */
/*******************************************************************************
 * Copyright (c) 2003, Atsuhiko Yamanaka, JCraft,Inc. and others. All rights
 * reserved. This program and the accompanying materials are made available
 * under the terms of the Common Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Atsuhiko Yamanaka, JCraft,Inc. - initial API and
 * implementation.
 ******************************************************************************/
package org.eclipse.team.ccvs.ssh2;

import java.io.*;
import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.IServerConnection;
import org.eclipse.team.internal.ccvs.core.connection.CVSAuthenticationException;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;

import com.jcraft.jsch.*;

public class CVSSSH2ServerConnection implements IServerConnection {

	private static final String COMMAND = "cvs server";

	private ICVSRepositoryLocation location;
	private String password;

	private InputStream inputStream;
	private OutputStream outputStream;

	private Session session;
	private Channel channel;

  private IServerConnection ssh1;

	protected CVSSSH2ServerConnection(ICVSRepositoryLocation location, String password) {
		this.location = location;
		this.password = password;
	}

	public void close() throws IOException {
	  if(ssh1!=null){
	    ssh1.close();
	    return;
	  }
		if (channel != null)
			channel.disconnect();
	}

	public InputStream getInputStream() {
	  if(ssh1!=null){
	    return ssh1.getInputStream();
	  }
		return inputStream;
	}
	public OutputStream getOutputStream() {
	  if(ssh1!=null){
	    return ssh1.getOutputStream();
	  }
		return outputStream;
	}

	public void open(IProgressMonitor monitor) throws IOException, CVSAuthenticationException {
	  if(ssh1!=null){
	    ssh1.open(monitor);
	    return;
	  }
		monitor.subTask(Policy.bind("CVSSSH2ServerConnection.open", location.getHost())); //$NON-NLS-1$
		monitor.worked(1);
		try {
			String hostname = location.getHost();
			String username = location.getUsername();
			int port = location.getPort();
			if (port == ICVSRepositoryLocation.USE_DEFAULT_PORT)
				port = 0;

			int retry = 1;
			OutputStream channel_out;
			InputStream channel_in;

			while (true) {
				session = JSchSession.getSession(location, username, password, hostname, port, monitor);
				channel = session.openChannel("exec");
				((ChannelExec) channel).setCommand(COMMAND);

				channel_out = channel.getOutputStream();
				channel_in = channel.getInputStream();

				try {
					channel.connect();
				} catch (JSchException ee) {
					if (!session.isConnected()) {
						//System.out.println("sesssion is down");
						//channel.disconnect();
						retry--;
						if (retry < 0) {
							throw new CVSAuthenticationException("session is down");
						}
						continue;
					}
					throw ee;
				}
				break;
			}

			inputStream = channel_in;
			outputStream = channel_out;
		} catch (JSchException e) {
		  if(e.toString().indexOf("invalid server's version string")==-1){
			//e.printStackTrace();
			throw new CVSAuthenticationException(e.toString());
		  }
		  ssh1=getServerConnection("extssh1");
		  if(ssh1==null){
		    throw new CVSAuthenticationException(e.toString());
		  }
		  ssh1.open(monitor);
		}
	}

  private IServerConnection getServerConnection(String ctype){
    try {
      Properties prop = new Properties();
      prop.put("connection", ctype);
      prop.put("user", location.getUsername());
      prop.put("password", password);
      prop.put("host", location.getHost());
      prop.put("port", Integer.toString(location.getPort()));
      prop.put("root", location.getRootDirectory());

      CVSRepositoryLocation cvsrl=CVSRepositoryLocation.fromProperties(prop);
      IConnectionMethod method=cvsrl.getMethod();
      return  method.createConnection(cvsrl, password);
    } 
    catch (CVSException e) {
    }
    return null;
  }
}
