/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ssh2;


/**
 * Defines the constants used by the SSH2 Plugin
 */
public interface ISSHContants {
    
    public static String KEY_PROXY="CVSSSH2PreferencePage.PROXY"; //$NON-NLS-1$
    public static String KEY_PROXY_TYPE="CVSSSH2PreferencePage.PROXY_TYPE"; //$NON-NLS-1$
    public static String KEY_PROXY_HOST="CVSSSH2PreferencePage.PROXY_HOST"; //$NON-NLS-1$
    public static String KEY_PROXY_PORT="CVSSSH2PreferencePage.PROXY_PORT"; //$NON-NLS-1$
    public static String KEY_PROXY_AUTH="CVSSSH2PreferencePage.PROXY_AUTH"; //$NON-NLS-1$
    public static String KEY_PROXY_USER="CVSSSH2PreferencePage.PROXY_USER"; //$NON-NLS-1$
    public static String KEY_PROXY_PASS="CVSSSH2PreferencePage.PROXY_PASS"; //$NON-NLS-1$
    public static String KEY_SSH2HOME="CVSSSH2PreferencePage.SSH2HOME"; //$NON-NLS-1$
    public static String KEY_KEYFILE="CVSSSH2PreferencePage.KEYFILE"; //$NON-NLS-1$
    public static String KEY_PRIVATEKEY="CVSSSH2PreferencePage.PRIVATEKEY"; //$NON-NLS-1$

    static String SOCKS5="SOCKS5"; //$NON-NLS-1$
    static String HTTP="HTTP"; //$NON-NLS-1$
    static String HTTP_DEFAULT_PORT="80"; //$NON-NLS-1$
    static String SOCKS5_DEFAULT_PORT="1080"; //$NON-NLS-1$
    static String PRIVATE_KEYS_DEFAULT="id_dsa,id_rsa"; //$NON-NLS-1$

    static String DSA="DSA"; //$NON-NLS-1$
    static String RSA="RSA"; //$NON-NLS-1$
	
}
