/*******************************************************************************
 * Copyright (c) 2018 SAP SE and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP SE - initial version
 *******************************************************************************/
package org.eclipse.urischeme.internal.registration;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.prefs.Preferences;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Wraps Windows Registry to read and write values. Can only be used for Keys
 * below HKEY_CURRENT_USER. The Windows Registry code has been tested in Java 8,
 * 9, 10 and 11
 *
 */
public class WinRegistry implements IWinRegistry {
	private static final Preferences USER_ROOT = Preferences.userRoot();
	private static final int KEY_READ = 1;
	private static final int KEY_SET = 2;
	private static final int KEY_DELETE = 0x10000;

	private static Method METHOD_stringToByteArray;
	private static Method METHOD_WinRegQueryValueEx;
	private static Method METHOD_toJavaValueString;
	private static Method METHOD_openKey;
	private static Method METHOD_closeKey;
	private static Method METHOD_WinRegSetValueEx1;
	private static Method METHOD_WinRegDeleteKey;

	static {
		// method handles are cached for performance reasons
		try {
			Class<?> prefClass = USER_ROOT.getClass();
			METHOD_stringToByteArray = prefClass.getDeclaredMethod("stringToByteArray", String.class); //$NON-NLS-1$
			METHOD_toJavaValueString = prefClass.getDeclaredMethod("toJavaValueString", byte[].class); //$NON-NLS-1$
			METHOD_openKey = prefClass.getDeclaredMethod("openKey", byte[].class, int.class, int.class); //$NON-NLS-1$
			Class<?> parameterType = int.class;
			try {
				// up to java 10 the "hkey" parameter of the close key method is of type int
				METHOD_closeKey = prefClass.getDeclaredMethod("closeKey", parameterType); //$NON-NLS-1$
			} catch (NoSuchMethodException e1) {
				// starting with java 11 the "hkey" parameter is of type long
				parameterType = long.class;
				METHOD_closeKey = prefClass.getDeclaredMethod("closeKey", parameterType); //$NON-NLS-1$
			}
			METHOD_WinRegQueryValueEx = prefClass.getDeclaredMethod("WindowsRegQueryValueEx", parameterType, //$NON-NLS-1$
					byte[].class);
			METHOD_WinRegSetValueEx1 = prefClass.getDeclaredMethod("WindowsRegSetValueEx1", parameterType, byte[].class, //$NON-NLS-1$
					byte[].class);
			METHOD_WinRegDeleteKey = prefClass.getDeclaredMethod("WindowsRegDeleteKey", parameterType, byte[].class); //$NON-NLS-1$

			AccessibleObject[] allMethods = new AccessibleObject[] { METHOD_stringToByteArray, METHOD_toJavaValueString,
					METHOD_openKey, METHOD_closeKey, METHOD_WinRegQueryValueEx, METHOD_WinRegSetValueEx1,
					METHOD_WinRegDeleteKey };
			AccessibleObject.setAccessible(allMethods, true);
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	@Override
	public void setValueForKey(String key, String attribute, String value) throws WinRegistryException {
		try {
			Object handle = METHOD_openKey.invoke(USER_ROOT, toByteArray(key), KEY_SET, KEY_SET);
			int result = (Integer) METHOD_WinRegSetValueEx1.invoke(null, handle, toByteArray(attribute),
					toByteArray(value));
			METHOD_closeKey.invoke(USER_ROOT, handle);
			if (result != 0) {
				throw new WinRegistryException("Unable to write to registry. Key = " + key + attribute + //$NON-NLS-1$
						", value: " + value); //$NON-NLS-1$

			}
		} catch (Exception e) {
			throw new WinRegistryException(e.getMessage(), e);
		}
	}

	@Override
	public String getValueForKey(String key, String attribute) throws WinRegistryException {
		try {
			Object handle = METHOD_openKey.invoke(USER_ROOT, toByteArray(key), KEY_READ, KEY_READ);
			byte[] valb = (byte[]) METHOD_WinRegQueryValueEx.invoke(null, handle, toByteArray(attribute));
			METHOD_closeKey.invoke(USER_ROOT, handle);
			String vals = (valb != null ? toString(valb) : null);
			return vals;
		} catch (Exception e) {
			throw new WinRegistryException(e.getMessage(), e);
		}
	}

	@Override
	public void deleteKey(String key) throws WinRegistryException {
		try {
			IPath keyPath = new Path(key);
			String parent = keyPath.removeLastSegments(1).toOSString();
			String child = keyPath.lastSegment();
			Object parentHandle = METHOD_openKey.invoke(USER_ROOT, toByteArray(parent), KEY_DELETE, KEY_DELETE);
			int result = (Integer) METHOD_WinRegDeleteKey.invoke(null, parentHandle, toByteArray(child));
			METHOD_closeKey.invoke(USER_ROOT, parentHandle);
			if (result != 0) {
				throw new WinRegistryException("Unable to delete key = " + keyPath); //$NON-NLS-1$
			}
		} catch (Exception e) {
			throw new WinRegistryException(e.getMessage(), e);
		}
	}

	private static byte[] toByteArray(String str) {
		if (str == null) {
			return new byte[] { 0 };
		}
		try {
			return (byte[]) METHOD_stringToByteArray.invoke(null, str);
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	/**
	 * Converts a null-terminated byte array to java string
	 *
	 * @throws UnsupportedEncodingException
	 */
	private static String byteArrayToString(byte[] array) throws UnsupportedEncodingException {
		byte[] truncatedArray = new byte[array.length - 1];
		System.arraycopy(array, 0, truncatedArray, 0, truncatedArray.length);
		String result;
		try {
			result = new String(truncatedArray, "Windows-1252"); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			result = new String(truncatedArray, "ISO-8859-1"); //$NON-NLS-1$
		}
		return result;
	}

	private static String toString(byte[] bytes) {
		try {
			return byteArrayToString(bytes);
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
}
