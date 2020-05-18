/*******************************************************************************
 * Copyright (c) 2018 - 2020 SAP SE and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP SE - initial version
 *     SAP SE - replace reflection API with JNA
 *******************************************************************************/
package org.eclipse.urischeme.internal.registration;

import com.sun.jna.LastErrorException;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinError;
import com.sun.jna.platform.win32.WinReg;

/**
 * Wraps Windows Registry to read and write values. Can only be used for Keys
 * below HKEY_CURRENT_USER.
 */
public class WinRegistry implements IWinRegistry {

	@Override
	public void setValueForKey(String key, String attribute, String value) throws WinRegistryException {
		try {
			Advapi32Util.registryCreateKey(WinReg.HKEY_CURRENT_USER, key);
		} catch (LastErrorException e) {
			throw new WinRegistryException("Unable to create registry key. Key=" + key); //$NON-NLS-1$
		}
		try {
			Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, key, attribute, value);
		} catch (LastErrorException e) {
			throw new WinRegistryException("Unable to set registry value. Key=" + key //$NON-NLS-1$
					+ ", name=" + attribute //$NON-NLS-1$
					+ ", value=" + value, e); //$NON-NLS-1$
		}
	}

	@Override
	public String getValueForKey(String key, String attribute) throws WinRegistryException {
		try {
			return Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER, key, attribute);
		} catch (LastErrorException e) {
			if (e.getErrorCode() == WinError.ERROR_FILE_NOT_FOUND
					|| e.getErrorCode() == WinError.ERROR_PATH_NOT_FOUND) {
				return null;
			}
			throw new WinRegistryException("Unable to read registry value. Key=" + key //$NON-NLS-1$
					+ ", name=" + attribute, e); //$NON-NLS-1$
		}
	}

	@Override
	public void deleteKey(String key) throws WinRegistryException {
		try {
			Advapi32Util.registryDeleteKey(WinReg.HKEY_CURRENT_USER, key);
		} catch (LastErrorException e) {
			throw new WinRegistryException("Unable to delete registry key. Key=" + key, e); //$NON-NLS-1$
		}
	}
}
