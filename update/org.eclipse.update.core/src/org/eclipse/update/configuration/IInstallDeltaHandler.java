package org.eclipse.update.configuration;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */


/**
 * Install Delta Handler.
 * Presents the changes the reconciler found to the user
 */
public interface IInstallDeltaHandler{

	/**
	 * Sets the list of session delta to present to the user
	 * 
	 * @param deltas an Array of <code>ISessionDelta</code>
	 * @see org.eclipse.update.configuration.ISesssionDelta
	 * @since 2.0 
	 */
	public void init(ISessionDelta[] deltas);

	/**
	 * Prompt the user to configure or unconfigure
	 * new features found during reconciliation
	 * 
	 * @since 2.0 
	 */
	public void open();

}