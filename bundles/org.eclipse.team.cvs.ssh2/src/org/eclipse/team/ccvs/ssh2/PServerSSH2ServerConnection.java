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
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.connection.CVSAuthenticationException;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;

import com.jcraft.jsch.*;

public class PServerSSH2ServerConnection implements IServerConnection {

	private ICVSRepositoryLocation location;
	private String password;

	private Session session;
	private Channel channel;

	private static int localport = 2402;

	private IServerConnection psc = null;

	protected PServerSSH2ServerConnection(ICVSRepositoryLocation location, String password) {
		this.location = location;
		this.password = password;
	}

	public void close() throws IOException {
		psc.close();
	}

	public InputStream getInputStream() {
		return psc.getInputStream();
	}
	public OutputStream getOutputStream() {
		return psc.getOutputStream();
	}

	public void open(IProgressMonitor monitor) throws IOException, CVSAuthenticationException {
		monitor.subTask("PServerSSH2ServerConnection.open");
		monitor.worked(1);
		String cvs_root = location.getRootDirectory();
		int cvs_port = location.getPort();
		if (cvs_port == 0)
			cvs_port = 2401;
		String cvs_host = location.getHost();
		String ssh_host = cvs_host;
		String ssh_user = location.getUsername();

		String host = cvs_host;
		if (host.indexOf('@') != -1) {
			cvs_host = host.substring(host.lastIndexOf('@') + 1);
			host = host.substring(0, host.lastIndexOf('@'));
			if (host.indexOf('@') != -1) {
				ssh_host = host.substring(host.lastIndexOf('@') + 1);
				if (ssh_host.length() == 0)
					ssh_host = cvs_host;
				ssh_user = host.substring(0, host.lastIndexOf('@'));
			} else {
				ssh_host = host;
			}
		}

		int ssh_port = 0;
		if (ssh_host.indexOf('#') != -1) {
			try {
				ssh_port = Integer.parseInt(ssh_host.substring(ssh_host.lastIndexOf('#') + 1));
				ssh_host = ssh_host.substring(0, ssh_host.lastIndexOf('#'));
			} catch (Exception e) {
			}
		}

		int lport = cvs_port;
		String rhost = (cvs_host.equals(ssh_host) ? "localhost" : cvs_host);
		int rport = cvs_port;

		// ssh -L lport:rhost:rport ssh_user@ssh_host
		int retry = 1;
		while (true) {
			try {
				session = JSchSession.getSession(location, ssh_user, "", ssh_host, ssh_port, monitor);
				String[] list = session.getPortForwardingL();
				String name = ":" + rhost + ":" + rport;
				boolean done = false;
				for (int i = 0; i < list.length; i++) {
					if (list[i].endsWith(name)) {
						try {
							String foo = list[i].substring(0, list[i].indexOf(':'));
							lport = Integer.parseInt(foo);
						} catch (Exception ee) {
						}
						done = true;
						break;
					}
				}
				if (!done) {
					lport = localport++;
					session.setPortForwardingL(lport, rhost, rport);
				}
			} catch (JSchException ee) {
				if (!session.isConnected()) {
					//System.out.println("sesssion is down");
					retry--;
					if (retry < 0) {
						throw new CVSAuthenticationException(ee.toString());
					}
					continue;
				}
				throw new CVSAuthenticationException(ee.toString());
			}
			break;
		}

		// CVSROOT=":pserver:localhost:"+lport+""cvs_root
		try {
			Properties prop = new Properties();
			prop.put("connection", "pserver");
			prop.put("user", location.getUsername());
			prop.put("password", password);
			prop.put("host", "localhost");
			prop.put("port", Integer.toString(lport));
			prop.put("root", cvs_root);

			CVSRepositoryLocation cvsrl = CVSRepositoryLocation.fromProperties(prop);

			IConnectionMethod method = cvsrl.getMethod();
			psc = method.createConnection(cvsrl, password);
		} catch (Exception e) {
			throw new CVSAuthenticationException(e.toString());
		}
		psc.open(monitor);
	}
}