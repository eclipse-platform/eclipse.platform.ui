package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;

/**
 * Verifier. This interface abstracts the archive verification step
 * performed by specific feature implementations. The actual details
 * of the verification are the responsibility of the concrete implementation.
 * <p>
 * Clients may implement this interface.
 * </p>
 * @see org.eclipse.update.core.IVerificationResult
 * @see org.eclipse.update.core.IFeatureContentProvider#getVerifier()
 * @since 2.0
 */
public interface IVerifier {

	/**
	 * Perform verification of the specified archive.
	 * 
	 * @param feature feature containing this archive
	 * @param reference actual archive reference
	 * @param isFeatureVerification <code>true</code> indicates the specified
	 * reference should be considered as part of the feature description
	 * information (ie. verifying the overall feature), 
	 * <code>false</code> indicates the specified reference is a plug-in
	 * or a non-plug-in archive file (ie. verifying a component of the
	 * feature)
	 * @param monitor progress monitor, can be <code>null</code>
	 * @return verification result
	 * @exception CoreException
	 * @since 2.0
	 */
	public IVerificationResult verify(
		IFeature feature,
		ContentReference reference,
		boolean isFeatureVerification,
		InstallMonitor monitor)
		throws CoreException;
}