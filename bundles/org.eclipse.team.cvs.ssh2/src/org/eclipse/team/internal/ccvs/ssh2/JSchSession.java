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
package org.eclipse.team.internal.ccvs.ssh2;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.util.Util;

import com.jcraft.jsch.*;

class JSchSession {
	private static final int SSH_DEFAULT_PORT = 22;
	private static JSch jsch=new JSch();
	private static java.util.Hashtable pool = new java.util.Hashtable();

	static String default_ssh_home = null;
	static {
		String ssh_dir_name = ".ssh"; //$NON-NLS-1$
		
		// Windows doesn't like files or directories starting with a dot.
		if (BootLoader.getOS().equals(BootLoader.OS_WIN32)) {
			ssh_dir_name = "ssh"; //$NON-NLS-1$
		}
		default_ssh_home = System.getProperty("user.home"); //$NON-NLS-1$
		if (default_ssh_home != null) {
			default_ssh_home = default_ssh_home + java.io.File.separator + ssh_dir_name;
		} else {
			
		}
	}

	private static String current_ssh_home = null;

	/**
	 * User information delegates to the IUserAuthenticator. This allows
	 * headless access to the connection method.
	 */
	private static class MyUserInfo implements UserInfo, UIKeyboardInteractive {
		private String username;
		private String password;
		private String passphrase;
		private ICVSRepositoryLocation location;
		
		MyUserInfo(String username, ICVSRepositoryLocation location) {
			this.location = location;
			this.username = username;
		}
		public String getPassword() {
			return password;
		}
		public String getPassphrase() {
			return passphrase;
		}
		public boolean promptYesNo(String str) {
			IUserAuthenticator authenticator = location.getUserAuthenticator();
			int prompt = authenticator.prompt(
					location, 
					IUserAuthenticator.QUESTION, 
					Policy.bind("JSchSession.5"),  //$NON-NLS-1$
					str, 
					new int[] {IUserAuthenticator.YES_ID, IUserAuthenticator.NO_ID}, 
					0 //yes the default
					);
			return prompt == 0;
		}
		private String promptSecret(String message, boolean includeLocation) throws CVSException{
			IUserAuthenticator authenticator = location.getUserAuthenticator();
			final String[] _password = new String[1];
			IUserInfo info = new IUserInfo() {
				public String getUsername() {
					return username;
				}
				public boolean isUsernameMutable() {
					return false;
				}
				public void setPassword(String password) {
					_password[0] = password;
				}
				public void setUsername(String username) {
				}
			};
			authenticator.promptForUserInfo(includeLocation ? location : null, info,	message);
			return _password[0];	
		}
		public boolean promptPassphrase(String message) {
			try{
				String _passphrase=promptSecret(message, false);
				if(_passphrase!=null){
				  passphrase=_passphrase;
				}
				return _passphrase!=null;
			}
			catch(CVSException e){
				return false;
			}
		}
		public boolean promptPassword(String message) {
			try{
				String _password=promptSecret(message, true);
				if(_password!=null){
					password=_password;
					// Cache the password with the repository location on the memory.
					((CVSRepositoryLocation)location).setPassword(password);
				}
				return _password!=null;
			}
			catch(CVSException e){
				return false;
			}
		}
		public void showMessage(String message) {
			IUserAuthenticator authenticator = location.getUserAuthenticator();
			authenticator.prompt(
					location,
					IUserAuthenticator.INFORMATION,
					Policy.bind("JSchSession.5"), //$NON-NLS-1$
					message,
					new int[] {IUserAuthenticator.OK_ID},
					IUserAuthenticator.OK_ID
					);
		}
		public String[] promptKeyboardInteractive(String destination,   
				String name,   
				String instruction,   
				String[] prompt,   
				boolean[] echo){   
			IUserAuthenticator authenticator = location.getUserAuthenticator();
			try{
				String[] result=
					authenticator.promptForKeyboradInteractive(location,
																destination,   
																name,   	
																instruction,
																prompt,   
																echo);   
				return result;
			}
			catch(CVSException e){
				return null;
			}
		} 		
	}
	
	static Session getSession(ICVSRepositoryLocation location, String username, String password, String hostname, int port, final IProgressMonitor monitor) throws JSchException {
		if (port == 0)
			port = SSH_DEFAULT_PORT;

		IPreferenceStore store = CVSSSH2Plugin.getDefault().getPreferenceStore();
		String ssh_home = store.getString(CVSSSH2PreferencePage.KEY_SSH2HOME);

		if (current_ssh_home == null || !current_ssh_home.equals(ssh_home)) {
			current_ssh_home = ssh_home;
			if (ssh_home.length() == 0)
				ssh_home = default_ssh_home;

			try {
			  java.io.File file;
			  file=new java.io.File(ssh_home, "known_hosts"); //$NON-NLS-1$
			  jsch.setKnownHosts(file.getPath());

			  String pkeys=store.getString(CVSSSH2PreferencePage.KEY_PRIVATEKEY);
			  String[] pkey=pkeys.split(","); //$NON-NLS-1$
			  for(int i=0; i<pkey.length;i++){
			    file = new java.io.File(ssh_home, pkey[i]);
			    if (file.exists())
			      jsch.addIdentity(file.getPath());
			  }
			} catch (Exception e) {
			}
		}

		String key = username + "@" + hostname + ":" + port; //$NON-NLS-1$ //$NON-NLS-2$

		try {
			Session session = (Session) pool.get(key);
			if (session != null && !session.isConnected()) {
				pool.remove(key);
				session = null;
			}

			if (session == null) {
				session = jsch.getSession(username, hostname, port);

				boolean useProxy = store.getString(CVSSSH2PreferencePage.KEY_PROXY).equals("true"); //$NON-NLS-1$
				if (useProxy) {
					String _type = store.getString(CVSSSH2PreferencePage.KEY_PROXY_TYPE);
					String _host = store.getString(CVSSSH2PreferencePage.KEY_PROXY_HOST);
					String _port = store.getString(CVSSSH2PreferencePage.KEY_PROXY_PORT);

					boolean useAuth = store.getString(CVSSSH2PreferencePage.KEY_PROXY_AUTH).equals("true"); //$NON-NLS-1$
					String _user = store.getString(CVSSSH2PreferencePage.KEY_PROXY_USER);
					String _pass = store.getString(CVSSSH2PreferencePage.KEY_PROXY_PASS);

					Proxy proxy = null;
					String proxyhost = _host + ":" + _port; //$NON-NLS-1$
					if (_type.equals(CVSSSH2PreferencePage.HTTP)) {
						proxy = new ProxyHTTP(proxyhost);
						if (useAuth) {
							((ProxyHTTP) proxy).setUserPasswd(_user, _pass);
						}
					} else if (_type.equals(CVSSSH2PreferencePage.SOCKS5)) {
						proxy = new ProxySOCKS5(proxyhost);
						if (useAuth) {
							((ProxySOCKS5) proxy).setUserPasswd(_user, _pass);
						}
					} else {
						proxy = null;
					}
					if (proxy != null) {
						session.setProxy(proxy);
					}
				}

				session.setPassword(password);

				UserInfo ui = new MyUserInfo(username, location);
				session.setUserInfo(ui);

				session.setSocketFactory(new SocketFactory() {
					InputStream in = null;
					OutputStream out = null;
					public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
						Socket socket = null;
						socket = Util.createSocket(host, port, monitor);
						return socket;
					}
					public InputStream getInputStream(Socket socket) throws IOException {
						if (in == null)
							in = socket.getInputStream();
						return in;
					}
					public OutputStream getOutputStream(Socket socket) throws IOException {
						if (out == null)
							out = socket.getOutputStream();
						return out;
					}
				});

				session.connect();
				pool.put(key, session);
			}
			return session;
		} catch (JSchException e) {
			pool.remove(key);
			throw e;
		}
	}

	static void shutdown() {
		if (jsch != null && pool.size() > 0) {
			for (Enumeration e = pool.elements(); e.hasMoreElements(); ) {
				Session session = (Session) (e.nextElement());
				try {
					session.disconnect();
				} catch (Exception ee) {
				}
			}
			pool.clear();
		}
	}
  static JSch getJSch(){
    return jsch;
  }
}
