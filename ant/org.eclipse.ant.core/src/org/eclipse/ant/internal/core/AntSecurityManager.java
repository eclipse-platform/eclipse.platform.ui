/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.core;

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.net.SocketPermission;
import java.security.Permission;
import java.util.PropertyPermission;

import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.ant.core.AntSecurityException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

/**
 * A security manager that always throws an <code>AntSecurityException</code> if the calling thread attempts to cause the Java Virtual Machine to
 * exit/halt or if the restricted thread attempts to set a System property. Otherwise this manager just delegates to the pre-existing manager passed
 * in the constructor or mimics the default security manager behavior
 */
public class AntSecurityManager extends SecurityManager {

	private SecurityManager fSecurityManager = null;
	private Thread fRestrictedThread = null;
	// ensure that the PropertyPermission class is loaded before we
	// start checking permissions: bug 85908
	private static final PropertyPermission fgPropertyPermission = new PropertyPermission("*", "write"); //$NON-NLS-1$ //$NON-NLS-2$

	private boolean fAllowSettingSystemProperties = true;

	public AntSecurityManager(SecurityManager securityManager, Thread restrictedThread, boolean allowSettingProperties) {
		fSecurityManager = securityManager;
		fRestrictedThread = restrictedThread;
		fAllowSettingSystemProperties = allowSettingProperties;
	}

	public AntSecurityManager(SecurityManager securityManager, Thread restrictedThread) {
		this(securityManager, restrictedThread, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.SecurityManager#checkExit(int)
	 */
	@Override
	public void checkExit(int status) {
		// no exit allowed from the restricted thread...System.exit is being called
		// by some ant task...do not want Eclipse to exit if
		// in the same VM.
		if (Thread.currentThread() == fRestrictedThread) {
			throw new AntSecurityException();
		}
		if (fSecurityManager != null) {
			fSecurityManager.checkExit(status);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.SecurityManager#checkAccept(java.lang.String, int)
	 */
	@Override
	public void checkAccept(String host, int port) {
		if (fSecurityManager != null) {
			fSecurityManager.checkAccept(host, port);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.SecurityManager#checkAccess(java.lang.Thread)
	 */
	@Override
	public void checkAccess(Thread t) {
		if (fSecurityManager != null) {
			fSecurityManager.checkAccess(t);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.SecurityManager#checkAccess(java.lang.ThreadGroup)
	 */
	@Override
	public void checkAccess(ThreadGroup g) {
		if (fSecurityManager != null) {
			fSecurityManager.checkAccess(g);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.SecurityManager#checkAwtEventQueueAccess()
	 */
	@Override
	public void checkAwtEventQueueAccess() {
		if (fSecurityManager != null) {
			fSecurityManager.checkAwtEventQueueAccess();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.SecurityManager#checkConnect(java.lang.String, int, java.lang.Object)
	 */
	@Override
	public void checkConnect(String host, int port, Object context) {
		if (fSecurityManager != null) {
			fSecurityManager.checkConnect(host, port, context);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.SecurityManager#checkConnect(java.lang.String, int)
	 */
	@Override
	public void checkConnect(String host, int port) {
		if (fSecurityManager != null) {
			fSecurityManager.checkConnect(host, port);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.SecurityManager#checkCreateClassLoader()
	 */
	@Override
	public void checkCreateClassLoader() {
		if (fSecurityManager != null) {
			fSecurityManager.checkCreateClassLoader();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.SecurityManager#checkDelete(java.lang.String)
	 */
	@Override
	public void checkDelete(String file) {
		if (fSecurityManager != null) {
			fSecurityManager.checkDelete(file);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.SecurityManager#checkExec(java.lang.String)
	 */
	@Override
	public void checkExec(String cmd) {
		if (fSecurityManager != null) {
			fSecurityManager.checkExec(cmd);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.SecurityManager#checkLink(java.lang.String)
	 */
	@Override
	public void checkLink(String lib) {
		if (fSecurityManager != null) {
			fSecurityManager.checkLink(lib);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.SecurityManager#checkListen(int)
	 */
	@Override
	public void checkListen(int port) {
		if (fSecurityManager != null) {
			fSecurityManager.checkListen(port);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.SecurityManager#checkMemberAccess(java.lang.Class, int)
	 */
	@Override
	public void checkMemberAccess(Class<?> clazz, int which) {
		if (fSecurityManager != null) {
			fSecurityManager.checkMemberAccess(clazz, which);
		}
	}

	/**
	 * @see java.lang.SecurityManager#checkMulticast(java.net.InetAddress, byte)
	 * @deprecated
	 */
	@Deprecated
	@Override
	public void checkMulticast(InetAddress maddr, byte ttl) {
		if (fSecurityManager != null) {
			String host = maddr.getHostAddress();
			if (!host.startsWith("[") && host.indexOf(':') != -1) { //$NON-NLS-1$
				host = "[" + host + "]"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			checkPermission(new SocketPermission(host, "accept,connect")); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.SecurityManager#checkMulticast(java.net.InetAddress)
	 */
	@Override
	public void checkMulticast(InetAddress maddr) {
		if (fSecurityManager != null) {
			fSecurityManager.checkMulticast(maddr);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.SecurityManager#checkPackageAccess(java.lang.String)
	 */
	@Override
	public void checkPackageAccess(String pkg) {
		if (fSecurityManager != null) {
			fSecurityManager.checkPackageAccess(pkg);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.SecurityManager#checkPackageDefinition(java.lang.String)
	 */
	@Override
	public void checkPackageDefinition(String pkg) {
		if (fSecurityManager != null) {
			fSecurityManager.checkPackageDefinition(pkg);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.SecurityManager#checkPermission(java.security.Permission, java.lang.Object)
	 */
	@Override
	public void checkPermission(Permission perm, Object context) {
		if (fSecurityManager != null) {
			fSecurityManager.checkPermission(perm, context);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.SecurityManager#checkPermission(java.security.Permission)
	 */
	@Override
	public void checkPermission(Permission perm) {
		if (!fAllowSettingSystemProperties && fgPropertyPermission.implies(perm) && fRestrictedThread == Thread.currentThread()) {
			// attempting to write a system property
			throw new AntSecurityException();
		}
		if (fSecurityManager != null) {
			fSecurityManager.checkPermission(perm);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.SecurityManager#checkPrintJobAccess()
	 */
	@Override
	public void checkPrintJobAccess() {
		if (fSecurityManager != null) {
			fSecurityManager.checkPrintJobAccess();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.SecurityManager#checkPropertiesAccess()
	 */
	@Override
	public void checkPropertiesAccess() {
		if (fSecurityManager != null) {
			fSecurityManager.checkPropertiesAccess();
		}
		super.checkPropertiesAccess();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.SecurityManager#checkPropertyAccess(java.lang.String)
	 */
	@Override
	public void checkPropertyAccess(String key) {
		if (fSecurityManager != null) {
			fSecurityManager.checkPropertyAccess(key);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.SecurityManager#checkRead(java.io.FileDescriptor)
	 */
	@Override
	public void checkRead(FileDescriptor fd) {
		if (fSecurityManager != null) {
			fSecurityManager.checkRead(fd);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.SecurityManager#checkRead(java.lang.String, java.lang.Object)
	 */
	@Override
	public void checkRead(String file, Object context) {
		if (fSecurityManager != null) {
			fSecurityManager.checkRead(file, context);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.SecurityManager#checkRead(java.lang.String)
	 */
	@Override
	public void checkRead(String file) {
		if (fSecurityManager != null) {
			fSecurityManager.checkRead(file);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.SecurityManager#checkSecurityAccess(java.lang.String)
	 */
	@Override
	public void checkSecurityAccess(String target) {
		if (fSecurityManager != null) {
			fSecurityManager.checkSecurityAccess(target);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.SecurityManager#checkSetFactory()
	 */
	@Override
	public void checkSetFactory() {
		if (fSecurityManager != null) {
			fSecurityManager.checkSetFactory();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.SecurityManager#checkSystemClipboardAccess()
	 */
	@Override
	public void checkSystemClipboardAccess() {
		if (fSecurityManager != null) {
			fSecurityManager.checkSystemClipboardAccess();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.SecurityManager#checkTopLevelWindow(java.lang.Object)
	 */
	@Override
	public boolean checkTopLevelWindow(Object window) {
		if (fSecurityManager != null) {
			return fSecurityManager.checkTopLevelWindow(window);
		}
		return super.checkTopLevelWindow(window);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.SecurityManager#checkWrite(java.io.FileDescriptor)
	 */
	@Override
	public void checkWrite(FileDescriptor fd) {
		if (fSecurityManager != null) {
			fSecurityManager.checkWrite(fd);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.SecurityManager#checkWrite(java.lang.String)
	 */
	@Override
	public void checkWrite(String file) {
		if (fSecurityManager != null) {
			fSecurityManager.checkWrite(file);
		}
	}

	/**
	 * @see java.lang.SecurityManager#getInCheck()
	 * @deprecated
	 */
	@Deprecated
	// @Override Super class method has been removed in JDK 10
	public boolean getInCheck() {
		try {
			SecurityManager.class.getDeclaredMethod("getInCheck", (Class[]) null); //$NON-NLS-1$
			if (fSecurityManager != null) {
				return fSecurityManager.getInCheck();
			}
			return super.getInCheck();
		}
		catch (NoSuchMethodException | SecurityException e) {
			Platform.getLog(AntCorePlugin.getPlugin().getBundle()).log(new Status(IStatus.WARNING, AntCorePlugin.PI_ANTCORE, InternalCoreAntMessages.AntSecurityManager_0));
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.SecurityManager#getSecurityContext()
	 */
	@Override
	public Object getSecurityContext() {
		if (fSecurityManager != null) {
			return fSecurityManager.getSecurityContext();
		}
		return super.getSecurityContext();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.SecurityManager#getThreadGroup()
	 */
	@Override
	public ThreadGroup getThreadGroup() {
		if (fSecurityManager != null) {
			fSecurityManager.getThreadGroup();
		}
		return super.getThreadGroup();
	}
}
