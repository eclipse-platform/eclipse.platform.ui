/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atsuhiko Yamanaka, JCraft,Inc. - initial API and implementation.
 *     IBM Corporation - ongoing maintenance
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ssh2;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.util.Util;

import com.jcraft.jsch.*;

class JSchSession {
	private static final int SSH_DEFAULT_PORT = 22;
	private static JSch jsch=new JSch();
	private static java.util.Hashtable pool = new java.util.Hashtable();

	private static String current_ssh_home = null;
	private static String current_pkeys = ""; //$NON-NLS-1$
    private final Session session;
    private final UserInfo prompter;
    private final ICVSRepositoryLocation location;

    protected static int getCVSTimeoutInMillis() {
        //return CVSProviderPlugin.getPlugin().getTimeout() * 1000;
    	// TODO Hard-code the timeout for now since Jsch doesn't respect CVS timeout
    	// See bug 92887
    	return 60000;
    }
    
	public static class SimpleSocketFactory implements SocketFactory {
		InputStream in = null;
		OutputStream out = null;
		public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
			Socket socket = null;
			socket = new Socket(host, port);
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
	}
	
	public static class ResponsiveSocketFacory extends SimpleSocketFactory {
		private IProgressMonitor monitor;
		public ResponsiveSocketFacory(IProgressMonitor monitor) {
			this.monitor = monitor;
		}
		public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
			Socket socket = null;
			socket = Util.createSocket(host, port, monitor);
			// Null out the monitor so we don't hold onto anything
			// (i.e. the SSH2 session will keep a handle to the socket factory around
			monitor = new NullProgressMonitor();
			// Set the socket timeout
			socket.setSoTimeout(getCVSTimeoutInMillis());
			return socket;
		}
	}
	
    /**
     * UserInfo wrapper class that will time how long each prompt takes
     */
    private static class UserInfoTimer implements UserInfo, UIKeyboardInteractive {

        private UserInfo wrappedInfo;
        private long startTime;
        private long endTime;
        private boolean prompting;
        
        public UserInfoTimer(UserInfo wrappedInfo) {
            this.wrappedInfo = wrappedInfo;
        }
        
        private synchronized void startTimer() {
            prompting = true;
            startTime = System.currentTimeMillis();
        }
        
        private synchronized void endTimer() {
            prompting = false;
            endTime = System.currentTimeMillis();
        }
        
        public long getLastDuration() {
            return Math.max(0, endTime-startTime);
        }
        
        public boolean hasPromptExceededTimeout() {
            if (!isPrompting()) {
                return getLastDuration() > getCVSTimeoutInMillis();
            }
            return false;
        }
        
        public String getPassphrase() {
            return wrappedInfo.getPassphrase();
        }

        public String getPassword() {
            return wrappedInfo.getPassword();
        }

        public boolean promptPassword(String arg0) {
            try {
                startTimer();
                return wrappedInfo.promptPassword(arg0);
            } finally {
                endTimer();
            }
        }

        public boolean promptPassphrase(String arg0) {
            try {
                startTimer();
                return wrappedInfo.promptPassphrase(arg0);
            } finally {
                endTimer();
            }
        }

        public boolean promptYesNo(String arg0) {
            try {
                startTimer();
                return wrappedInfo.promptYesNo(arg0);
            } finally {
                endTimer();
            }
        }

        public void showMessage(String arg0) {
        	if(arg0.length()!=0){
	            try {
	                startTimer();	                
	                wrappedInfo.showMessage(arg0);  
	            } finally {
	                endTimer();
	            }
        	}
        }

        public String[] promptKeyboardInteractive(String arg0, String arg1, String arg2, String[] arg3, boolean[] arg4) {
            try {
                startTimer();
                return ((UIKeyboardInteractive)wrappedInfo).promptKeyboardInteractive(arg0, arg1, arg2, arg3, arg4);
            } finally {
                endTimer();
            }
        }

        public boolean isPrompting() {
            return prompting;
        }
    }
    
	/**
	 * User information delegates to the IUserAuthenticator. This allows
	 * headless access to the connection method.
	 */
	private static class MyUserInfo implements UserInfo, UIKeyboardInteractive {
		private String username;
		private String password;
		private String passphrase;
		private ICVSRepositoryLocation location;
		private IUserAuthenticator authenticator;
        private int attemptCount;
        private boolean passwordChanged;
		
		MyUserInfo(String username, String password, ICVSRepositoryLocation location) {
			this.location = location;
			this.username = username;
			this.password = password;
			ICVSRepositoryLocation _location=location;
			if(_location==null){
				String dummy=":extssh:dummy@dummy:/"; //$NON-NLS-1$
				try{
					_location=CVSRepositoryLocation.fromString(dummy);
				}
				catch(CVSException e){
				}
			}
			authenticator = _location.getUserAuthenticator();
			
		}
		public String getPassword() {
			return password;
		}
		public String getPassphrase() {
			return passphrase;
		}
		public boolean promptYesNo(String str) {
			int prompt = authenticator.prompt(
					location, 
					IUserAuthenticator.QUESTION, 
					CVSSSH2Messages.JSchSession_5,  
					str, 
					new int[] {IUserAuthenticator.YES_ID, IUserAuthenticator.NO_ID}, 
					0 //yes the default
					);
			return prompt == 0;
		}
		private String promptSecret(String message, boolean includeLocation) throws CVSException{
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
			try{
				authenticator.promptForUserInfo(includeLocation ? location : null, info,	message);
			}
			catch(OperationCanceledException e){
				_password[0]=null;
			}
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
					if(location!=null)
						((CVSRepositoryLocation)location).setPassword(password);
				}
				return _password!=null;
			}
			catch(CVSException e){
				return false;
			}
		}
		public void showMessage(String message) {
    		authenticator.prompt(
    				location,
    				IUserAuthenticator.INFORMATION,
    				CVSSSH2Messages.JSchSession_5, 
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
		    if (prompt.length == 0) {
		        // No need to prompt, just return an empty String array
		        return new String[0];
		    }
			try{
			    if (attemptCount == 0 && password != null && prompt.length == 1 && prompt[0].trim().equalsIgnoreCase("password:")) { //$NON-NLS-1$
			        // Return the provided password the first time but always prompt on subsequent tries
			        attemptCount++;
			        return new String[] { password };
			    }
				String[] result=
					authenticator.promptForKeyboradInteractive(location,
																destination,   
																name,   	
																instruction,
																prompt,   
																echo);
                if (result == null) 
                    return null; // canceled
			    if (result.length == 1 && prompt.length == 1 && prompt[0].trim().equalsIgnoreCase("password:")) { //$NON-NLS-1$
			        password = result[0];
			        passwordChanged = true;
			    }
			    attemptCount++;
				return result;
			}
			catch(OperationCanceledException e){
				return null;
			}
			catch(CVSException e){
				return null;
			}
		}
		
        /**
         * Callback to indicate that a connection is about to be attempted
         */
        public void aboutToConnect() {
            attemptCount = 0;
            passwordChanged = false;
        }
        
        /**
         * Callback to indicate that a connection was made
         */
        public void connectionMade() {
            attemptCount = 0;
            if (passwordChanged && password != null && location != null) {
                // We were prompted for and returned a password so record it with the location
                location.setPassword(password);
            }
        }
	}

    public static boolean isAuthenticationFailure(JSchException ee) {
        return ee.getMessage().equals("Auth fail"); //$NON-NLS-1$
    }
    
    static JSchSession getSession(ICVSRepositoryLocation location, String username, String password, String hostname, int port, IProgressMonitor monitor) throws JSchException {

        if (port == ICVSRepositoryLocation.USE_DEFAULT_PORT)
            port = getPort(location);
        
		IPreferenceStore store = CVSSSH2Plugin.getDefault().getPreferenceStore();
		String ssh_home = store.getString(ISSHContants.KEY_SSH2HOME);
		String pkeys = store.getString(ISSHContants.KEY_PRIVATEKEY);

		try {
			if (current_ssh_home == null || !current_ssh_home.equals(ssh_home)) {
				loadKnownHosts();
				current_ssh_home = ssh_home;
			}

			if (ssh_home.length() == 0)
				ssh_home = CVSSSH2Plugin.SSH_HOME_DEFAULT;

			if (!current_pkeys.equals(pkeys)) {
				java.io.File file;
				String[] pkey = pkeys.split(","); //$NON-NLS-1$
				String[] _pkey = current_pkeys.split(","); //$NON-NLS-1$
				current_pkeys = ""; //$NON-NLS-1$
				for (int i = 0; i < pkey.length; i++) {
					file = new java.io.File(pkey[i]);
					if (!file.isAbsolute()) {
						file = new java.io.File(ssh_home, pkey[i]);
					}
					if (file.exists()) {
						boolean notyet = true;
						for (int j = 0; j < _pkey.length; j++) {
							if (pkey[i].equals(_pkey[j])) {
								notyet = false;
								break;
							}
						}
						if (notyet)
							jsch.addIdentity(file.getPath());
						if (current_pkeys.length() == 0) {
							current_pkeys = pkey[i];
						} else {
							current_pkeys += ("," + pkey[i]); //$NON-NLS-1$
						}
					}
				}
			}
		} catch (Exception e) {
		}
		

		String key = getPoolKey(username, hostname, port);

		try {
			JSchSession jschSession = (JSchSession) pool.get(key);
			if (jschSession != null && !jschSession.getSession().isConnected()) {
				pool.remove(key);
                jschSession = null;
			}

			if (jschSession == null) {
				boolean useProxy = CVSProviderPlugin.getPlugin().isUseProxy();
                Proxy proxy = null;
				if (useProxy) {
					String _type = CVSProviderPlugin.getPlugin().getProxyType();
					String _host = CVSProviderPlugin.getPlugin().getProxyHost();
					String _port = CVSProviderPlugin.getPlugin().getProxyPort();

					boolean useAuth = CVSProviderPlugin.getPlugin().isUseProxyAuth();
					String _user = ""; //$NON-NLS-1$
					String _pass = ""; //$NON-NLS-1$
					
					// Retrieve username and password from keyring.
					if(useAuth){
						_user=CVSProviderPlugin.getPlugin().getProxyUser();
						_pass=CVSProviderPlugin.getPlugin().getProxyPassword();
					}

					String proxyhost = _host + ":" + _port; //$NON-NLS-1$
					if (_type.equals(CVSProviderPlugin.PROXY_TYPE_HTTP)) {
						proxy = new ProxyHTTP(proxyhost);
						if (useAuth) {
							((ProxyHTTP) proxy).setUserPasswd(_user, _pass);
						}
					} else if (_type.equals(CVSProviderPlugin.PROXY_TYPE_SOCKS5)) {
						proxy = new ProxySOCKS5(proxyhost);
						if (useAuth) {
							((ProxySOCKS5) proxy).setUserPasswd(_user, _pass);
						}
					} else {
						proxy = null;
					}
				}

                MyUserInfo ui = new MyUserInfo(username, password, location);
                UserInfoTimer wrapperUI = new UserInfoTimer(ui);
                ui.aboutToConnect();
                
                Session session = null;
                try {
                    session = createSession(username, password, hostname, port, new JSchSession.ResponsiveSocketFacory(monitor), proxy, wrapperUI);
                } catch (JSchException e) {
                    if (isAuthenticationFailure(e) && wrapperUI.hasPromptExceededTimeout()) {
                        // Try again since the previous prompt may have obtained the proper credentials from the user
                        session = createSession(username, password, hostname, port, new JSchSession.ResponsiveSocketFacory(monitor), proxy, wrapperUI);
                    } else {
                        throw e;
                    }
                }
                ui.connectionMade();
                JSchSession schSession = new JSchSession(session, location, wrapperUI);
                pool.put(key, schSession);
                return schSession;
			} else {
                return jschSession;
            }
		} catch (JSchException e) {
			pool.remove(key);
			if(e.toString().indexOf("Auth cancel")!=-1){  //$NON-NLS-1$
				throw new OperationCanceledException();
			}
			throw e;
		}
	}

    private static Session createSession(String username, String password, String hostname, int port, SocketFactory socketFactory, Proxy proxy, UserInfo wrapperUI) throws JSchException {
        Session session = jsch.getSession(username, hostname, port);
        if (proxy != null) {
            session.setProxy(proxy);
        }
        session.setTimeout(getCVSTimeoutInMillis());
        if (password != null)
			session.setPassword(password);
        session.setUserInfo(wrapperUI);
        session.setSocketFactory(socketFactory);
        // This is where the server is contacted and authentication occurs
        try {
            session.connect();
        } catch (JSchException e) {
            if (session.isConnected())
                session.disconnect();
            throw e;
        }
        return session;
    }

    private static String getPoolKey(String username, String hostname, int port) {
        return username + "@" + hostname + ":" + port; //$NON-NLS-1$ //$NON-NLS-2$
    }

    private static String getPoolKey(ICVSRepositoryLocation location){
        return location.getUsername() + "@" + location.getHost() + ":" + getPort(location); //$NON-NLS-1$ //$NON-NLS-2$
    }

	private static int getPort(ICVSRepositoryLocation location) {
        int port = location.getPort();
        if (port == ICVSRepositoryLocation.USE_DEFAULT_PORT)
            port = SSH_DEFAULT_PORT;
        return port;
    }

    static void loadKnownHosts(){
		IPreferenceStore store = CVSSSH2Plugin.getDefault().getPreferenceStore();
		String ssh_home = store.getString(ISSHContants.KEY_SSH2HOME);

		if (ssh_home.length() == 0)
			ssh_home = CVSSSH2Plugin.SSH_HOME_DEFAULT;

		try {
		  java.io.File file;
		  file=new java.io.File(ssh_home, "known_hosts"); //$NON-NLS-1$
		  jsch.setKnownHosts(file.getPath());
		} catch (Exception e) {
		}
	}

	static void shutdown() {
		if (jsch != null && pool.size() > 0) {
			for (Enumeration e = pool.elements(); e.hasMoreElements(); ) {
                JSchSession session = (JSchSession) (e.nextElement());
				try {
					session.getSession().disconnect();
				} catch (Exception ee) {
				}
			}
			pool.clear();
		}
	}
  static JSch getJSch(){
    return jsch;
  }
  
    private JSchSession(Session session, ICVSRepositoryLocation location, UserInfo prompter) {
        this.session = session;
        this.location = location;
        this.prompter = prompter;
    }

    public Session getSession() {
        return session;
    }

    public UserInfo getPrompter() {
        return prompter;
    }

    public boolean hasPromptExceededTimeout() {
        if (prompter instanceof UserInfoTimer) {
            UserInfoTimer timer = (UserInfoTimer) prompter;
            if (!timer.isPrompting()) {
                return timer.getLastDuration() > getCVSTimeoutInMillis();
            }
        }
        return false;
    }
    
    public void dispose() {
        if (session.isConnected()) {
            session.disconnect();
        }
        pool.remove(getPoolKey(location));
    }

}
