package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;

/**
 * Verifier. This interface abstract the archive verification step
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
	 * @param monitor progress monitor, can be <code>null</code>
	 * @return verification result
	 * @exception CoreException
	 * @since 2.0
	 */
	public IVerificationResult verify(
		IFeature feature,
		ContentReference reference,
		InstallMonitor monitor)
		throws CoreException;
}