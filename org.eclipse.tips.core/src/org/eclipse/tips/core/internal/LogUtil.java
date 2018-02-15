/*******************************************************************************
 * Copyright (c) 2018 Remain Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     wim.jongman@remainsoftware.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.tips.core.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.FrameworkUtil;

public class LogUtil {

	public static IStatus getStatus(int severity, String bundleId, String message, Throwable throwable) {
		return new Status(severity, bundleId, message, throwable);
	}

	public static IStatus error(Throwable throwable) {
		return getStatus(IStatus.ERROR, "org.eclipse.tips.core", throwable.getMessage(), throwable);
	}

	public static IStatus warn(Throwable throwable) {
		return getStatus(IStatus.WARNING, "org.eclipse.tips.core", throwable.getMessage(), throwable);
	}

	public static IStatus info(Throwable throwable) {
		return getStatus(IStatus.INFO, "org.eclipse.tips.core", throwable.getMessage(), throwable);
	}

	public static IStatus error(Class<?> clazz, Throwable throwable) {
		return getStatus(IStatus.ERROR, getBundleId(clazz), throwable.getMessage(), throwable);
	}

	public static IStatus warn(Class<?> clazz, Throwable throwable) {
		return getStatus(IStatus.WARNING, getBundleId(clazz), throwable.getMessage(), throwable);
	}

	public static IStatus info(Class<?> clazz, Throwable throwable) {
		return getStatus(IStatus.INFO, getBundleId(clazz), throwable.getMessage(), throwable);
	}

	public static IStatus info(Class<?> clazz, String message) {
		return getStatus(IStatus.INFO, getBundleId(clazz), message, null);
	}

	public static IStatus warn(Class<?> clazz, String pessage) {
		return getStatus(IStatus.WARNING, getBundleId(clazz), pessage, null);
	}

	public static IStatus error(Class<?> clazz, String message) {
		return getStatus(IStatus.ERROR, getBundleId(clazz), message, null);
	}

	private static String getBundleId(Class<?> clazz) {
		if (FrameworkUtil.getBundle(clazz) != null) {
			return FrameworkUtil.getBundle(clazz).getSymbolicName();
		}
		return "osgi.not.running";
	}

	public static IStatus info(String message) {
		return getStatus(IStatus.INFO, "org.eclipse.tips.core", message, null);
	}
}