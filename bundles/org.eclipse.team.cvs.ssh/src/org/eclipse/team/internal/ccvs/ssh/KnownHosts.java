/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ssh;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Vector;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;

/**
 * I represent a database of known hosts usually placed in ~/.ssh/known_hosts
 * on Unix/Linux systems.
 * Currently, only RSA keys are supported, as these are the only keys we
 * have to deal with during SSH1 key exchange.
 */
public class KnownHosts {

	private String filename;
	
	public KnownHosts() {
		this.filename = KnownHosts.defaultFilename();
	}
	
	static String defaultFilename() {
		if (!Platform.getOS().equals(Platform.OS_LINUX)) return internalFilename();
		String HOME = System.getProperty("user.home"); //$NON-NLS-1$
		if (HOME==null) return internalFilename();
		return HOME+"/.ssh/known_hosts"; //$NON-NLS-1$
	}

	private static String internalFilename() {
		return SSHPlugin.getPlugin().getStateLocation().append("known_hosts").toOSString(); //$NON-NLS-1$
	}
	
	/**
	 * Verify if the public key for the specified host is known.
	 * If the public key matches, return true.
	 * If the key does not match, return false.
	 * If the key is not listed in <code>known_hosts</code>, or
	 * <code>known_hosts</code> does not exist, assume we are connecting
	 * to the authentic server, add the key, and return true.
	 * @param e key exponent
	 * @param n key modulus
	 * @return boolean whether the key is correct
	 */
	public boolean verifyKey(String hostname, byte[] host_key_bits, BigInteger e, BigInteger n) {
		FileReader f;
		BigInteger nbits = new BigInteger(1, host_key_bits);
		try {
			f= new FileReader(filename);
		} catch (FileNotFoundException ex) {
			createHostFile();
			addHost(hostname, nbits, e, n);
			return true;
		}
		BufferedReader r = new BufferedReader(f);
		try {
			String line;
			while ((line = r.readLine()) != null) {
				if (line.trim().length()==0) continue;
				if (line.startsWith("#")) continue; //$NON-NLS-1$
				String[] tokens=subStrings(line);
				if (tokens.length==4 && Character.isDigit(tokens[1].charAt(0)) && tokens[0].equalsIgnoreCase(hostname)) {
					if (nbits.equals(new BigInteger(tokens[1])) && e.equals(new BigInteger(tokens[2])) && n.equals(new BigInteger(tokens[3]))) {
						f.close();
						return true;
					} else {
						f.close();
						return false;
					}
				}
			}
			f.close();
			addHost(hostname, nbits, e, n);
			return true;
		} catch (IOException ex) {
			SSHPlugin.log(IStatus.ERROR, CVSSSHMessages.KnownHosts_8, ex); 
			return false;
		}
	}
	
	/*
	 * Append the host key information to known_hosts.
	 * Always assume the file exists.
	 */
	void addHost(String hostname, BigInteger key_bits, BigInteger e, BigInteger n) {
		try {
			FileWriter w = new FileWriter(defaultFilename(), true);
			w.write(Character.LINE_SEPARATOR);
			w.write(hostname + " " + key_bits.toString(10) + " " + e.toString(10) + " " + n.toString(10)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			w.close();
			String message = NLS.bind(CVSSSHMessages.Client_addedHostKey, (new String[] {hostname, defaultFilename()})); 
			SSHPlugin.log(IStatus.INFO, message, null);
		} catch (IOException ex) {
			SSHPlugin.log(IStatus.ERROR, CVSSSHMessages.KnownHosts_9, ex); 
		}
	}
	
	/*
	 * Create the known_hosts file in the default location.
	 * Fail if the file can not be created (issue a warning in the log).
	 */
	void createHostFile() {
		try {
			File file = new File(defaultFilename());
			// Ensure the parent directory exists
			File parentDir = file.getParentFile();
			parentDir.mkdirs();
			// Create the file
			file.createNewFile();
		} catch (IOException ee) {
			SSHPlugin.log(IStatus.ERROR, CVSSSHMessages.KnownHosts_10, ee); 
		}

	}
	private static String[] subStrings(String s) {
		Vector v = subStringsVector(s);
		String[] substrings = new String[v.size()];
		v.copyInto(substrings);
		return substrings;
	}
	private static Vector subStringsVector(String s) {
		Vector v = new Vector();
		s = s.trim();
		if (s.length()==0) return v;
		int first1 = s.indexOf(' ');
		int first2 = s.indexOf('\t');
		int first;
		if ((first1==-1)&&(first2==-1)) first=-1;
		else if ((first1!=-1)&&(first2!=-1)) first = Math.min(first1, first2);
		else if (first1!=-1) first=first1; else first=first2;
		if (first==-1) {
			v.add(s);
			return v;
		}
		v.add(s.substring(0,first));
		v.addAll(subStringsVector(s.substring(first+1)));
		return v;
	}
}
