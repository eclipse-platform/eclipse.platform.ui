
package org.eclipse.update.core;

import org.eclipse.core.runtime.CoreException;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
 
/**
 * Verify a feature
 */
public interface IVerifier {
	
	/**
	 * Throws CoreException if the verification was unsucessful and the installation should not continue
	 */
	IVerificationResult verify(IFeature feature,ContentReference[] references,InstallMonitor monitor) throws CoreException;

}
