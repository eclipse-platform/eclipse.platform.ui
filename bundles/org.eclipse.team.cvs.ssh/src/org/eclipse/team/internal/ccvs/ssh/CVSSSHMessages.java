/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.team.internal.ccvs.ssh;

import org.eclipse.osgi.util.NLS;

public class CVSSSHMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.team.internal.ccvs.ssh.messages";//$NON-NLS-1$

	public static String closed;
	public static String stream;

	public static String Client_packetType;
	public static String Client_notConnected;
	public static String Client_cipher;
	public static String Client_socketClosed;
	public static String Client_authenticationFailed;
	public static String Client_socket;
	public static String Client_disconnectDescription;
	public static String Client_noDisconnectDescription;
	public static String Client_sshProtocolVersion;
	public static String Client_hostIdChanged;
	public static String Client_addedHostKey;

	public static String ServerPacket_crc;

	public static String SSHServerConnection_authenticating;

	public static String Misc_missingMD5;

	public static String KnownHosts_8;
	public static String KnownHosts_9;
	public static String KnownHosts_10;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, CVSSSHMessages.class);
	}
}