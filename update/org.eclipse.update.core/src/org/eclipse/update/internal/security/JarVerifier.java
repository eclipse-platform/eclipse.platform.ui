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

import org.apache.xerces.utils.regex.REUtil;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.core.*;
import org.eclipse.update.core.IVerifier;
import org.eclipse.update.internal.core.Policy;
import org.eclipse.update.internal.core.UpdateManagerPlugin;

/**
 * The JarVerifier will check the integrity of the JAR.
 * If the Jar is signed and the integrity is validated,
 * it will check if one of the certificate of each file
 * is in one of the keystore.
 *
 */

public class JarVerifier implements IVerifier {

	private JarVerificationResult result;
	private List /*of CertificatePair*/
	trustedCertificates = null;	
	private boolean acceptUnsignedFiles = false;	

	/**
	 * List of initialized keystores
	 */
	private List /* of KeyStore */
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
	 * Returns the list of the keystores.
	 *
	 */
	private List getKeyStores() throws CoreException {
		if (listOfKeystores == null) {
			listOfKeystores = new ArrayList(0);
			KeyStores listOfKeystoreHandles = new KeyStores();
			InputStream in = null;
			KeyStore keystore = null;
			KeystoreHandle handle = null;
			while (listOfKeystoreHandles.hasNext()) {
				try {
					handle = listOfKeystoreHandles.next();
					in = handle.getLocation().openStream();
					try {
						keystore = KeyStore.getInstance(handle.getType());
						keystore.load(in, null); // no password
					} catch (NoSuchAlgorithmException e) {
						throw Utilities.newCoreException(Policy.bind("JarVerifier.UnableToFindEncryption", handle.getLocation().toExternalForm()), e); //$NON-NLS-1$
					} catch (CertificateException e) {
						throw Utilities.newCoreException(Policy.bind("JarVerifier.UnableToLoadCertificate", handle.getLocation().toExternalForm()), e); //$NON-NLS-1$
					} catch (KeyStoreException e) {
						throw Utilities.newCoreException(Policy.bind("JarVerifier.UnableToFindProviderForKeystore", handle.getType()), e); //$NON-NLS-1$
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
	private void initializeVariables(File jarFile, IFeature feature, ContentReference contentRef) throws IOException {
		result = new JarVerificationResult();
		result.setVerificationCode(IVerificationResult.UNKNOWN_ERROR);
		result.setResultException(null);
		result.setFeature(feature);
		result.setContentReference(contentRef);
		
		// # of entries
		JarFile jar = new JarFile(jarFile);
		entries = jar.size();
		try {
			jar.close();
		} catch (IOException ex) {
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
			throw Utilities.newCoreException(Policy.bind("JarVerifier.KeyStoreNotLoaded"), e); //$NON-NLS-1$
		}
		return false;
	}

	/**
	 * Throws exception or set the resultcode to UNKNOWN_ERROR
	 */
	private List readJarFile(JarFile jarFile) throws IOException, InterruptedException {
		List list = new ArrayList();
		byte[] buffer = new byte[4096];
		Enumeration entries = jarFile.entries();
		JarEntry currentEntry = null;
		InputStream in = null;
		if (monitor != null)
			monitor.beginTask(Policy.bind("JarVerifier.Verify", jarFile.getName()), jarFile.size()); //$NON-NLS-1$ 
		
		try {
			while (entries.hasMoreElements()) {
				currentEntry = (JarEntry) entries.nextElement();
				list.add(currentEntry);
				in = jarFile.getInputStream(currentEntry);
				while ((in.read(buffer, 0, buffer.length)) != -1) {
					// Security error thrown if tempered
				}
				in.close();
			}
		} catch (IOException e) {
			result.setVerificationCode(IVerificationResult.UNKNOWN_ERROR);
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

	/*
	* @see IVerifier#verify(IFeature feature,ContentReference, InstallMonitor)
	*/
	public IVerificationResult verify(IFeature feature, ContentReference reference, InstallMonitor monitor) throws CoreException {
		if (reference == null )
			return result;

		setMonitor(monitor);
			if (reference instanceof JarContentReference) {
				JarContentReference jarReference = (JarContentReference) reference;
				try {
					File jarFile = jarReference.asFile(); 
					initializeVariables(jarFile, feature, reference);
					return verify(jarFile.getAbsolutePath());
				} catch (IOException e){
					throw Utilities.newCoreException("Unable to access JAR file:"+jarReference.toString(),e);
				}
			}
	
		result.setVerificationCode(IVerificationResult.TYPE_ENTRY_UNRECOGNIZED);
		return result;
	}

	/**
	* Verifies integrity and the validity of a valid JAR File
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
	private JarVerificationResult verify(String file) {

		try {

			// verify integrity
			verifyIntegrity(file);

			// do not close input stream
			// as verifyIntegrity already did it

			//if user already said yes
			if (alreadyValidated()) {
				result.setVerificationCode(IVerificationResult.TYPE_ENTRY_ALREADY_ACCEPTED);			
				return result;
			}

			// verify source certificate
			if (result.getVerificationCode() == IVerificationResult.TYPE_ENTRY_SIGNED_UNRECOGNIZED){
				verifyAuthentication();
			} 
				
			// save the fact the file is not signed, so the user will not be prompted again 
			if (result.getVerificationCode() == IVerificationResult.TYPE_ENTRY_NOT_SIGNED){
				acceptUnsignedFiles = true;
			}
		

		} catch (Exception e) {
			result.setVerificationCode(IVerificationResult.UNKNOWN_ERROR);
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

		// If all the certificate of an entry are
		// not found in the list of known certifcate
		// the certificate is not trusted by any keystore.
		for (int i = 0; i < entries.length; i++) {
			certificateFound = existsInKeystore(entries[i].getRoot());
			if (certificateFound) {
				result.setVerificationCode(IVerificationResult.TYPE_ENTRY_SIGNED_RECOGNIZED);
				result.setFoundCertificate(entries[i]);
				return;
			}
		}
	}
	
	/**
	 * Verifies the integrity of the JAR
	 */
	private void verifyIntegrity(String file) {

		JarFile jarFile = null;
		Collection certificateEntries; // of Certificate[] 

		try {
			// If the JAR is signed and not valid
			// a security exception will be thrown
			// while reading it
			jarFile = new JarFile(file, true);
			List filesInJar = readJarFile(jarFile);
			jarFile.close();

			// you have to read all the files once
			// before getting the certificates 
			if (jarFile.getManifest()!=null) {
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
					result.setVerificationCode(IVerificationResult.TYPE_ENTRY_SIGNED_UNRECOGNIZED);
				else
					result.setVerificationCode(IVerificationResult.TYPE_ENTRY_NOT_SIGNED);
			} else {
				Exception e = new Exception("The File is not a valid JAR file. The file does not contain a Manifest." + file);
				result.setResultException(e);
				result.setVerificationCode(IVerificationResult.TYPE_ENTRY_NOT_SIGNED);				
				
				// DEBUG
				if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS){
					IStatus status = Utilities.newCoreException(e.getMessage(),e).getStatus();					
					UpdateManagerPlugin.getPlugin().getLog().log(status);
				}
				
			}
		} catch (SecurityException e) {
			// Jar file is signed
			// but content has changed since signed
			result.setVerificationCode(IVerificationResult.TYPE_ENTRY_CORRUPTED);
		} catch (InterruptedException e) {
			result.setVerificationCode(IVerificationResult.VERIFICATION_CANCELLED);
		} catch (Exception e) {
			result.setVerificationCode(IVerificationResult.UNKNOWN_ERROR);
			result.setResultException(e);
		} 

	}

	private boolean alreadyValidated() {

		if (result.getVerificationCode()==IVerificationResult.TYPE_ENTRY_NOT_SIGNED)
			return (acceptUnsignedFiles);		

		if (trustedCertificates != null) {
			// check if this is not a user accepted certificate for this feature
			Iterator iter = getTrustedCertificates().iterator();
			CertificatePair currentPair = null;
			while (iter.hasNext()) {
				CertificatePair trustedCertificate = (CertificatePair) iter.next();
				CertificatePair[] pairs = result.getRootCertificates();
				for (int i = 0; i < pairs.length; i++) {
					currentPair = pairs[i];
					if (trustedCertificate.equals(currentPair)) {
						return true;
					}
				}
			}
			
			// if certificate pair not found in trusted add it for next time
			addTrustedCertificate(currentPair);
		}

		return false;
	}

	private void addTrustedCertificate(CertificatePair pair) {
		if (trustedCertificates == null)
			trustedCertificates = new ArrayList();
		if (pair != null)
			trustedCertificates.add(pair);
	}

	private List getTrustedCertificates() {
		if (trustedCertificates == null)
			trustedCertificates = new ArrayList();
		return trustedCertificates;
	}

	
}