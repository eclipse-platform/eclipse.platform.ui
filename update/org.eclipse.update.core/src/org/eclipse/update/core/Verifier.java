package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;

/**
 * Convenience implementation of a verifier.
 * <p>
 * This class may be subclassed by clients.
 * </p> 
 * @see org.eclipse.update.core.IVerifier
 * @since 2.0
 */
public abstract class Verifier implements IVerifier {
	
	private IVerifier parent;

	/**
	 * @see IVerifier#verify(IFeature, ContentReference, boolean, InstallMonitor)
	 */
	public abstract IVerificationResult verify(
		IFeature feature,
		ContentReference reference,
		boolean isFeatureVerification,
		InstallMonitor monitor)
		throws CoreException ;

	/**
	 * @see IVerifier#verify(IFeature, ContentReference, boolean, InstallMonitor)
	 */
	public void setParent(IVerifier parentVerifier){
		if (this.parent==null){
			this.parent = parentVerifier;
		}
	}
	
	/**
	 * Returns the parent verifier
	 * 
	 * @return the parent verifier
	 * @since 2.0
	 */
	public IVerifier getParent(){
		return parent;
	}

}
