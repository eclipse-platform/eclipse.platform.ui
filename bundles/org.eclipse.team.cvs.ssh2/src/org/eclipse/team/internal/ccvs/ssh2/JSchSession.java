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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jsch.core.IJSchService;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.IUserAuthenticator;
import org.eclipse.team.internal.ccvs.core.IUserInfo;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.util.Util;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SocketFactory;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

class JSchSession {
	private static final int SSH_DEFAULT_PORT = 22;
	private static JSch jsch=new JSch();
	private static java.util.Hashtable pool = new java.util.Hashtable();
	
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
		
		String key = getPoolKey(username, hostname, port);

		try {
			JSchSession jschSession = (JSchSession) pool.get(key);
			if (jschSession != null && !jschSession.getSession().isConnected()) {
				pool.remove(key);
                jschSession = null;
			}

			if (jschSession == null) {
                MyUserInfo ui = new MyUserInfo(username, password, location);
                UserInfoTimer wrapperUI = new UserInfoTimer(ui);
                ui.aboutToConnect();
                
                Session session = null;
                try {
                    session = createSession(username, password, hostname, port, wrapperUI, monitor);
                } catch (JSchException e) {
                    if (isAuthenticationFailure(e) && wrapperUI.hasPromptExceededTimeout()) {
                        // Try again since the previous prompt may have obtained the proper credentials from the user
                        session = createSession(username, password, hostname, port, wrapperUI, monitor);
                    } else {
                        throw e;
                    }
                }
                if (session == null)
                	throw new JSchException("The JSch service is not available");
                ui.connectionMade();
                JSchSession schSession = new JSchSession(session, location, wrapperUI);
                pool.put(key, schSession);
                return schSession;
			}
            return jschSession;
		} catch (JSchException e) {
			pool.remove(key);
			if(e.toString().indexOf("Auth cancel")!=-1){  //$NON-NLS-1$
				throw new OperationCanceledException();
			}
			throw e;
		}
	}

    private static Session createSession(String username, String password, String hostname, int port, UserInfo wrapperUI, IProgressMonitor monitor) throws JSchException {
        IJSchService service = CVSSSH2Plugin.getDefault().getJSchService();
        if (service == null)
        	return null;
        Session session = service.createSession(hostname, port, username);
        session.setTimeout(getCVSTimeoutInMillis());
        if (password != null)
			session.setPassword(password);
        session.setUserInfo(wrapperUI);
        service.connect(session, getCVSTimeoutInMillis(), monitor);
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
