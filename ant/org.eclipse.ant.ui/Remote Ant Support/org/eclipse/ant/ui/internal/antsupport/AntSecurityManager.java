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
package org.eclipse.ant.ui.internal.antsupport;


import java.io.FileDescriptor;
import java.net.InetAddress;
import java.net.SocketPermission;
import java.security.Permission;


/**
 * A security manager that always throws an <code>AntSecurityException</code>
 * if the calling thread attempts to cause the Java Virtual Machine to 
 * exit/halt.
 * Otherwise this manager just delegates to the pre-existing manager
 * passed in the constructor or mimics the default security manager behavior
 */
public class AntSecurityManager extends SecurityManager {

	protected SecurityManager securityManager= null;
	
	protected AntSecurityManager(SecurityManager securityManager) {
		this.securityManager= securityManager;
	}
	/**
	 * @see java.lang.SecurityManager#checkExit(int)
	 */
	public void checkExit(int status) {
		//no exit allowed...System.exit is being called
		//by some ant task...do not want Eclipse to exit if
		//in the same VM,
		//The message is used in loggers to determine that this is
		//not really an exception case.
		throw new AntSecurityException();
	}
	/**
	 * @see java.lang.SecurityManager#checkAccept(java.lang.String, int)
	 */
	public void checkAccept(String host, int port) {
		if (securityManager != null) {
			securityManager.checkAccept(host, port);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkAccess(java.lang.Thread)
	 */
	public void checkAccess(Thread t) {
		if (securityManager != null) {
			securityManager.checkAccess(t);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkAccess(java.lang.ThreadGroup)
	 */
	public void checkAccess(ThreadGroup g) {
		if (securityManager != null) {
			securityManager.checkAccess(g);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkAwtEventQueueAccess()
	 */
	public void checkAwtEventQueueAccess() {
		if (securityManager != null) {
			securityManager.checkAwtEventQueueAccess();
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkConnect(java.lang.String, int, java.lang.Object)
	 */
	public void checkConnect(String host, int port, Object context) {
		if (securityManager != null) {
			securityManager.checkConnect(host, port, context);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkConnect(java.lang.String, int)
	 */
	public void checkConnect(String host, int port) {
		if (securityManager != null) {
			securityManager.checkConnect(host, port);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkCreateClassLoader()
	 */
	public void checkCreateClassLoader() {
		if (securityManager != null) {
			securityManager.checkCreateClassLoader();
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkDelete(java.lang.String)
	 */
	public void checkDelete(String file) {
		if (securityManager != null) {
			securityManager.checkDelete(file);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkExec(java.lang.String)
	 */
	public void checkExec(String cmd) {
		if (securityManager != null) {
			securityManager.checkExec(cmd);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkLink(java.lang.String)
	 */
	public void checkLink(String lib) {
		if (securityManager != null) {
			securityManager.checkLink(lib);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkListen(int)
	 */
	public void checkListen(int port) {
		if (securityManager != null) {
			securityManager.checkListen(port);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkMemberAccess(java.lang.Class, int)
	 */
	public void checkMemberAccess(Class clazz, int which) {
		if (securityManager != null) {
			securityManager.checkMemberAccess(clazz, which);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkMulticast(java.net.InetAddress, byte)
	 * @deprecated
	 */
	public void checkMulticast(InetAddress maddr, byte ttl) {
		if (securityManager != null) {
			String host = maddr.getHostAddress();
			if (!host.startsWith("[") && host.indexOf(':') != -1) { //$NON-NLS-1$
	   			host = "[" + host + "]"; //$NON-NLS-1$ //$NON-NLS-2$
			}
	    	checkPermission(new SocketPermission(host, "accept,connect")); //$NON-NLS-1$
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkMulticast(java.net.InetAddress)
	 */
	public void checkMulticast(InetAddress maddr) {
		if (securityManager != null) {
			securityManager.checkMulticast(maddr);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkPackageAccess(java.lang.String)
	 */
	public void checkPackageAccess(String pkg) {
		if (securityManager != null) {
			securityManager.checkPackageAccess(pkg);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkPackageDefinition(java.lang.String)
	 */
	public void checkPackageDefinition(String pkg) {
		if (securityManager != null) {
			securityManager.checkPackageDefinition(pkg);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkPermission(java.security.Permission, java.lang.Object)
	 */
	public void checkPermission(Permission perm, Object context) {
		if (securityManager != null) {
			securityManager.checkPermission(perm, context);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkPermission(java.security.Permission)
	 */
	public void checkPermission(Permission perm) {
		if (securityManager != null) {
			securityManager.checkPermission(perm);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkPrintJobAccess()
	 */
	public void checkPrintJobAccess() {
		if (securityManager != null) {
			securityManager.checkPrintJobAccess();
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkPropertiesAccess()
	 */
	public void checkPropertiesAccess() {
		if (securityManager != null) {
			securityManager.checkPropertiesAccess();
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkPropertyAccess(java.lang.String)
	 */
	public void checkPropertyAccess(String key) {
		if (securityManager != null) {
			securityManager.checkPropertyAccess(key);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkRead(java.io.FileDescriptor)
	 */
	public void checkRead(FileDescriptor fd) {
		if (securityManager != null) {
			securityManager.checkRead(fd);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkRead(java.lang.String, java.lang.Object)
	 */
	public void checkRead(String file, Object context) {
		if (securityManager != null) {
			securityManager.checkRead(file, context);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkRead(java.lang.String)
	 */
	public void checkRead(String file) {
		if (securityManager != null) {
			securityManager.checkRead(file);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkSecurityAccess(java.lang.String)
	 */
	public void checkSecurityAccess(String target) {
		if (securityManager != null) {
			securityManager.checkSecurityAccess(target);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkSetFactory()
	 */
	public void checkSetFactory() {
		if (securityManager != null) {
			securityManager.checkSetFactory();
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkSystemClipboardAccess()
	 */
	public void checkSystemClipboardAccess() {
		if (securityManager != null) {
			securityManager.checkSystemClipboardAccess();
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkTopLevelWindow(java.lang.Object)
	 */
	public boolean checkTopLevelWindow(Object window) {
		if (securityManager != null) {
			return securityManager.checkTopLevelWindow(window);
		}
		return super.checkTopLevelWindow(window);
	}

	/**
	 * @see java.lang.SecurityManager#checkWrite(java.io.FileDescriptor)
	 */
	public void checkWrite(FileDescriptor fd) {
		if (securityManager != null) {
			securityManager.checkWrite(fd);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkWrite(java.lang.String)
	 */
	public void checkWrite(String file) {
		if (securityManager != null) {
			securityManager.checkWrite(file);
		}
	}

	/**
	 * @see java.lang.SecurityManager#getInCheck()
	 * @deprecated
	 */
	public boolean getInCheck() {
		if (securityManager != null) {
			return securityManager.getInCheck();
		}
		return super.getInCheck();
	}

	/**
	 * @see java.lang.SecurityManager#getSecurityContext()
	 */
	public Object getSecurityContext() {
		if (securityManager != null) {
			return securityManager.getSecurityContext();
		}
		return super.getSecurityContext();
	}

	/**
	 * @see java.lang.SecurityManager#getThreadGroup()
	 */
	public ThreadGroup getThreadGroup() {
		if (securityManager != null) {
			securityManager.getThreadGroup();
		}
		return super.getThreadGroup();
	}
}
