package org.eclipse.update.internal.security;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.jar.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.internal.core.Policy;
import org.eclipse.update.internal.core.UpdateManagerPlugin;

/**
 * The JarVerifier will check the integrity of the JAR.
 * If the Jar is signed and the integrity is validated,
 * it will check if one of the certificate of each file
 * is in one of the keystore.
 *
 */

public class JarVerifier {

	private JarVerificationResult result;

	/**
	 * List of initialized keystores
	 */
	List /* of KeyStore */
	listOfKeystores;

	/**
	 * Number of files in the JarFile
	 */
	private int entries;

	/**
	 * ProgressMonitor during integrity validation
	 */
	private IProgressMonitor monitor;

	/**
	 * JAR File Name: used in the readJarFile.
	 */
	private String jarFileName;

	//RESULT VALUES
	public static final int NOT_SIGNED = 0;
	public static final int CORRUPTED = 1;
	public static final int INTEGRITY_VERIFIED = 2;
	public static final int SOURCE_VERIFIED = 3;
	public static final int UNKNOWN_ERROR = 4;
	public static final int VERIFICATION_CANCELLED = 5;

	/**
	 * Default Constructor
	 */
	public JarVerifier() {
	}
	/**
	 * 
	 */
	public JarVerifier(IProgressMonitor monitor) {
		this.monitor = monitor;
	}
	/**
	 * Returns the list of certificates of the keystore.
	 *
	 * Can be optimize, within an operation, we only need to get the
	 * list of certificate once.
	 */
	private List getKeyStores() throws CoreException {
		if (listOfKeystores == null) {
			listOfKeystores = new ArrayList(0);
			KeyStores listOfKeystoreHandles = new KeyStores();
			InputStream in = null;
			KeyStore keystore = null;
			while (listOfKeystoreHandles.hasNext()) {
				try {
					KeystoreHandle handle = listOfKeystoreHandles.next();
					in = handle.getLocation().openStream();
					try {
						keystore = KeyStore.getInstance(handle.getType());
						keystore.load(in, null); // no password
					} catch (NoSuchAlgorithmException e) {
						throw newCoreException("Unable to find encryption algorithm", e);

					} catch (CertificateException e) {
						throw newCoreException("Unable to load a certificate in the keystore", e);
					} catch (KeyStoreException e) {
						throw newCoreException("Unable to find provider for the keystore type", e);
					} finally {
						if (in != null) {
							try {
								in.close();
							} catch (IOException e) {
							} // nothing
						}
					} // try loading a keyStore

					// keystore was loaded
					listOfKeystores.add(keystore);
				} catch (IOException e) {
					// nothing... if the keystore doesn't exist, continue	
				}

			} // while all key stores

		}

		return listOfKeystores;
	}

	/**
	 * initialize instance variables
	 */
	private void initializeVariables(File jarFile) throws IOException {
		result = new JarVerificationResult();
		result.setVerificationCode(UNKNOWN_ERROR);
		result.setResultCode(JarVerificationResult.ASK_USER);
		result.setResultException(
			new Exception(
				Policy.bind("JarVerifier.InvalidJarFile", jarFile.getAbsolutePath())));
		//$NON-NLS-1$
		JarFile jar = new JarFile(jarFile);
		entries = jar.size();
		try {
			jar.close();
		} catch (java.io.IOException ex) {
			// unchecked
		}
		jarFileName = jarFile.getName();
	}
	/**
	 * Returns true if one of the certificate exists in the keystore
	 */
	private boolean existsInKeystore(Certificate cert) throws CoreException {
		try {
			List keyStores = getKeyStores();
			if (!keyStores.isEmpty()) {
				Iterator listOfKeystores = keyStores.iterator();
				while (listOfKeystores.hasNext()) {
					KeyStore keystore = (KeyStore) listOfKeystores.next();

					if (keystore.getCertificateAlias(cert) != null) {
						return true;
					}
				}
			}
		} catch (KeyStoreException e) {
			throw newCoreException("KeyStore not loaded", e);
		}
		return false;
	}

	/**
	 * Throws exception or set the resultcode to UNKNOWN_ERROR
	 */
	private List readJarFile(final JarInputStream jis)
		throws IOException, InterruptedException, InvocationTargetException {
		final List list = new ArrayList(0);

		byte[] buffer = new byte[4096];
		JarEntry ent;
		if (monitor != null)
			monitor.beginTask(Policy.bind("JarVerifier.Verify", jarFileName), entries);
		//$NON-NLS-1$ //$NON-NLS-2$
		try {
			while ((ent = jis.getNextJarEntry()) != null) {
				list.add(ent);
				if (monitor != null)
					monitor.worked(1);
				while ((jis.read(buffer, 0, buffer.length)) != -1) {
					// Security error thrown if tempered
				}
			}
		} catch (IOException e) {
			result.setVerificationCode(UNKNOWN_ERROR);
			result.setResultException(e);
		} finally {
			if (monitor != null)
				monitor.done();
		}
		return list;
	}
	/**
	 * 
	 * @param newMonitor org.eclipse.core.runtime.IProgressMonitor
	 */
	public void setMonitor(IProgressMonitor newMonitor) {
		monitor = newMonitor;
	}
	/**
	* Verifies integrity and the validity of a valid
	* URL representing a JAR file
	* the possible results are:
	*
	* result == NOT_SIGNED	 				if the jar file is not signed.
	* result == INTEGRITY_VERIFIED		 	if the Jar file has not been
	*										modified since it has been
	*										signed
	* result == CORRUPTED 					if the Jar file has been changed
	* 										since it has been signed.
	* result == SOURCE_VERIFIED	 		if all the files in the Jar
	*										have a certificate that is
	* 										present in the keystore
	* result == UNKNOWN_ERROR		 		an occured during process, do
	*                                      not install.
	* result == VERIFICATION.CANCELLED     if process was cancelled, do
	*										not install.
	* @return int
	*/
	public JarVerificationResult verify(File jarFile) {

		try {
			// new verification, clean instance variables
			initializeVariables(jarFile);

			// verify integrity
			verifyIntegrity(jarFile);

			// do not close input stream
			// as verifyIntegrity already did it

			// verify source certificate
			if (result.getVerificationCode() == INTEGRITY_VERIFIED)
				verifyAuthentication();

		} catch (Exception e) {
			result.setVerificationCode(UNKNOWN_ERROR);
			result.setResultException(e);
		}

		return result;
	}
	/**
	 * Verifies that each file has at least one certificate
	 * valid in the keystore
	 *
	 * At least one certificate from each Certificate Array
	 * of the Jar file must be found in the known Certificates
	 */
	private void verifyAuthentication() throws CoreException {

		CertificatePair[] entries = result.getRootCertificates();
		boolean certificateFound = false;

		// If all the cartificate of an entry are
		// not found in the list of known certifcate
		// the certificate is not trusted by any keystore.
		for (int i = 0; i < entries.length; i++) {
			certificateFound = existsInKeystore(entries[i].getRoot());
			if (certificateFound) {
				result.setVerificationCode(SOURCE_VERIFIED);
				result.setFoundCertificate(entries[i]);
				return;
			}

		}

	}
	/**
	 * Verifies the integrity of the JAR
	 */
	private void verifyIntegrity(File jarFile) {

		JarInputStream jis = null;
		Collection certificateEntries; // of Certificate[] 

		try {
			// If the JAR is signed and not valid
			// a security exception will be thrown
			// while reading it
			jis = new JarInputStream(new FileInputStream(jarFile), true);
			List filesInJar = readJarFile(jis);

			// you have to read all the files once
			// before getting the certificates 
			if (jis.getManifest() != null) {
				Iterator iter = filesInJar.iterator();
				boolean certificateFound = false;
				while (iter.hasNext()) {
					Certificate[] certs = ((JarEntry) iter.next()).getCertificates();
					if ((certs != null) && (certs.length != 0)) {
						certificateFound = true;
						result.addCertificates(certs);
					};
				}

				if (certificateFound)
					result.setVerificationCode(INTEGRITY_VERIFIED);
				else
					result.setVerificationCode(NOT_SIGNED);
			}
		} catch (SecurityException e) {
			// Jar file is signed
			// but content has changed since signed
			result.setVerificationCode(CORRUPTED);
		} catch (InterruptedException e) {
			result.setVerificationCode(VERIFICATION_CANCELLED);
		} catch (Exception e) {
			result.setVerificationCode(UNKNOWN_ERROR);
			result.setResultException(e);
		} finally {
			if (jis != null) {
				try {
					jis.close();
				} catch (IOException e) {
				} // nothing
			}
		}

	}

	/**
	 * 
	 */
	private CoreException newCoreException(String s, Throwable e)
		throws CoreException {
		String id =
			UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
		return new CoreException(new Status(IStatus.ERROR, id, 0, s, e));
	}

}