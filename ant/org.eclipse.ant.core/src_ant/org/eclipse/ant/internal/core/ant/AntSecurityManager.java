package org.eclipse.ant.internal.core.ant;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.Permission;

import org.eclipse.ant.core.AntSecurityException;

/**
 * A security manager that always throws a <code>AntSecurityException</code>
 * if the calling thread attempts to cause the Java Virtual Machine to 
 * exit/halt.
 * Otherwise this manager just delegates to the pre-existing manager
 * passed in the constructor. */
public class AntSecurityManager extends SecurityManager {

	protected SecurityManager fSecurityManager= null;
	
	protected AntSecurityManager(SecurityManager securityManager) {
		fSecurityManager= securityManager;
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
		if (fSecurityManager != null) {
			fSecurityManager.checkAccept(host, port);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkAccess(java.lang.Thread)
	 */
	public void checkAccess(Thread t) {
		if (fSecurityManager != null) {
			fSecurityManager.checkAccess(t);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkAccess(java.lang.ThreadGroup)
	 */
	public void checkAccess(ThreadGroup g) {
		if (fSecurityManager != null) {
			fSecurityManager.checkAccess(g);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkAwtEventQueueAccess()
	 */
	public void checkAwtEventQueueAccess() {
		if (fSecurityManager != null) {
			fSecurityManager.checkAwtEventQueueAccess();
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkConnect(java.lang.String, int, java.lang.Object)
	 */
	public void checkConnect(String host, int port, Object context) {
		if (fSecurityManager != null) {
			fSecurityManager.checkConnect(host, port, context);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkConnect(java.lang.String, int)
	 */
	public void checkConnect(String host, int port) {
		if (fSecurityManager != null) {
			fSecurityManager.checkConnect(host, port);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkCreateClassLoader()
	 */
	public void checkCreateClassLoader() {
		if (fSecurityManager != null) {
			fSecurityManager.checkCreateClassLoader();
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkDelete(java.lang.String)
	 */
	public void checkDelete(String file) {
		if (fSecurityManager != null) {
			fSecurityManager.checkDelete(file);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkExec(java.lang.String)
	 */
	public void checkExec(String cmd) {
		if (fSecurityManager != null) {
			fSecurityManager.checkExec(cmd);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkLink(java.lang.String)
	 */
	public void checkLink(String lib) {
		if (fSecurityManager != null) {
			fSecurityManager.checkLink(lib);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkListen(int)
	 */
	public void checkListen(int port) {
				if (fSecurityManager != null) {
			fSecurityManager.checkListen(port);
		}

	}

	/**
	 * @see java.lang.SecurityManager#checkMemberAccess(java.lang.Class, int)
	 */
	public void checkMemberAccess(Class clazz, int which) {
		if (fSecurityManager != null) {
			fSecurityManager.checkMemberAccess(clazz, which);
		}

	}

	/**
	 * @see java.lang.SecurityManager#checkMulticast(java.net.InetAddress, byte)
	 * @deprecated
	 */
	public void checkMulticast(InetAddress maddr, byte ttl) {
		if (fSecurityManager != null) {
			fSecurityManager.checkMulticast(maddr, ttl);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkMulticast(java.net.InetAddress)
	 */
	public void checkMulticast(InetAddress maddr) {
		if (fSecurityManager != null) {
			fSecurityManager.checkMulticast(maddr);
		}

	}

	/**
	 * @see java.lang.SecurityManager#checkPackageAccess(java.lang.String)
	 */
	public void checkPackageAccess(String pkg) {
		if (fSecurityManager != null) {
			fSecurityManager.checkPackageAccess(pkg);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkPackageDefinition(java.lang.String)
	 */
	public void checkPackageDefinition(String pkg) {
		if (fSecurityManager != null) {
			fSecurityManager.checkPackageDefinition(pkg);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkPermission(java.security.Permission, java.lang.Object)
	 */
	public void checkPermission(Permission perm, Object context) {
		if (fSecurityManager != null) {
			fSecurityManager.checkPermission(perm, context);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkPermission(java.security.Permission)
	 */
	public void checkPermission(Permission perm) {
		if (fSecurityManager != null) {
			fSecurityManager.checkPermission(perm);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkPrintJobAccess()
	 */
	public void checkPrintJobAccess() {
		if (fSecurityManager != null) {
			fSecurityManager.checkPrintJobAccess();
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkPropertiesAccess()
	 */
	public void checkPropertiesAccess() {
		if (fSecurityManager != null) {
			fSecurityManager.checkPropertiesAccess();
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkPropertyAccess(java.lang.String)
	 */
	public void checkPropertyAccess(String key) {
		if (fSecurityManager != null) {
			fSecurityManager.checkPropertyAccess(key);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkRead(java.io.FileDescriptor)
	 */
	public void checkRead(FileDescriptor fd) {
		if (fSecurityManager != null) {
			fSecurityManager.checkRead(fd);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkRead(java.lang.String, java.lang.Object)
	 */
	public void checkRead(String file, Object context) {
		if (fSecurityManager != null) {
			fSecurityManager.checkRead(file, context);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkRead(java.lang.String)
	 */
	public void checkRead(String file) {
		if (fSecurityManager != null) {
			fSecurityManager.checkRead(file);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkSecurityAccess(java.lang.String)
	 */
	public void checkSecurityAccess(String target) {
		if (fSecurityManager != null) {
			fSecurityManager.checkSecurityAccess(target);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkSetFactory()
	 */
	public void checkSetFactory() {
		if (fSecurityManager != null) {
			fSecurityManager.checkSetFactory();
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkSystemClipboardAccess()
	 */
	public void checkSystemClipboardAccess() {
		if (fSecurityManager != null) {
			fSecurityManager.checkSystemClipboardAccess();
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkTopLevelWindow(java.lang.Object)
	 */
	public boolean checkTopLevelWindow(Object window) {
		if (fSecurityManager != null) {
			return fSecurityManager.checkTopLevelWindow(window);
		}
		return false;
	}

	/**
	 * @see java.lang.SecurityManager#checkWrite(java.io.FileDescriptor)
	 */
	public void checkWrite(FileDescriptor fd) {
		if (fSecurityManager != null) {
			fSecurityManager.checkWrite(fd);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkWrite(java.lang.String)
	 */
	public void checkWrite(String file) {
		if (fSecurityManager != null) {
			fSecurityManager.checkWrite(file);
		}
	}

	/**
	 * @see java.lang.SecurityManager#classDepth(java.lang.String)
	 * @deprecated
	 */
	protected int classDepth(String name) {
		return super.classDepth(name);
	}

	/**
	 * @see java.lang.SecurityManager#classLoaderDepth()
	 * @deprecated
	 */
	protected int classLoaderDepth() {
		return super.classLoaderDepth();
	}

	/**
	 * @see java.lang.SecurityManager#currentClassLoader()
	 * @deprecated
	 */
	protected ClassLoader currentClassLoader() {
		return super.currentClassLoader();
	}

	/**
	 * @see java.lang.SecurityManager#currentLoadedClass()
	 * @deprecated
	 */
	protected Class currentLoadedClass() {
		return super.currentLoadedClass();
	}

	/**
	 * @see java.lang.SecurityManager#getClassContext()
	 */
	protected Class[] getClassContext() {
		return super.getClassContext();
	}

	/**
	 * @see java.lang.SecurityManager#getInCheck()
	 * @deprecated
	 */
	public boolean getInCheck() {
		if (fSecurityManager != null) {
			return fSecurityManager.getInCheck();
		}
		return false;
	}

	/**
	 * @see java.lang.SecurityManager#getSecurityContext()
	 */
	public Object getSecurityContext() {
		if (fSecurityManager != null) {
			return fSecurityManager.getSecurityContext();
		}
		return null;
	}

	/**
	 * @see java.lang.SecurityManager#getThreadGroup()
	 */
	public ThreadGroup getThreadGroup() {
		if (fSecurityManager != null) {
			fSecurityManager.getThreadGroup();
		}
		return null;
	}

	/**
	 * @see java.lang.SecurityManager#inClass(java.lang.String)
	 * @deprecated
	 */
	protected boolean inClass(String name) {
		return super.inClass(name);
	}

	/**
	 * @see java.lang.SecurityManager#inClassLoader()
	 * @deprecated
	 */
	protected boolean inClassLoader() {
		return super.inClassLoader();
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return super.hashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return super.toString();
	}

	/**
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable {
		super.finalize();
	}
}
