package org.eclipse.update.internal.security;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.File;import java.net.MalformedURLException;import java.net.URL;import java.util.ArrayList;import java.util.Iterator;import java.util.List;
/**
 * Class to manage the different KeyStores we should
 * check for certificates of Signed JAR
 */
public class KeyStores {

	/**
	 * true if check default and user keystore at each startup
	 */
	public static boolean ALWAYS_INITIALIZE = true;

	/**
	 * java.policy files properties of the java.security file
	 */
	public static final String JAVA_POLICY = "policy.url"; //$NON-NLS-1$

	/**
	 * List of URL pointing of valid KeyStores
	 * the URL is not tested yet...
	 */
	private List listOfKeyStores;

	/**
	 * Iterator
	 */
	private Iterator iterator;
	/**
	 * KeyStores constructor comment.
	 */
	public KeyStores() {
		super();
		// If the user always wnat to initialize.
		// this may take some time if some keystore
		// are remote or if the connection to the keystore
		// is down. 
		if (ALWAYS_INITIALIZE) {
			initializeDefaultKeyStores();
		}
		else {
			readKeystoresFromPersistent();
		}
	}
	/**
	 * 
	 */
	private Iterator getIterator() {
		if (iterator == null)
			iterator = listOfKeyStores.iterator();
		return iterator;
	}
	/**
	 * returns trus if there is more Keystores in the list
	 */
	public boolean hasNext() {
		return getIterator().hasNext();
	}
	/**
	 * populate the list of Keystores
	 * should be done with Dialog with Cancel/Skip button if
	 * the connection to the URL is down...
	 */
	private void initializeDefaultKeyStores() {

		listOfKeyStores = new ArrayList(5);

		// get JRE cacerts
		try {
			listOfKeyStores.add(new URL("file", null, 0, System.getProperty("java.home") + File.separator + "lib" + File.separator + "security" + File.separator + "cacerts")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		}
		catch (MalformedURLException e) {
			// should not happen, hardcoded...
		}

		// get java.home .keystore
		try {
			listOfKeyStores.add(new URL("file", null, 0, System.getProperty("user.home") + File.separator + ".keystore")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		catch (MalformedURLException e) {
			// should not happen, hardcoded...
		}

		// get KeyStores from policy files...
		//Security.getProperty(JAVA_POLICY);

	}
	/**
	 * returns the URL for the Next Keystore
	 */
	public URL next() {
		return (URL) getIterator().next();
	}
	/**
	 * populate the list of Keystores from the previously saved list
	 * if there is no list, initialize with default...
	 * [future]
	 */
	private void readKeystoresFromPersistent() {
		// not implemented
	}
}