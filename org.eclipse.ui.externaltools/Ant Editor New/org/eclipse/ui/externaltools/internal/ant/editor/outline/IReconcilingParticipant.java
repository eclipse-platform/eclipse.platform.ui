package org.eclipse.ui.externaltools.internal.ant.editor.outline;

/**
 * Interface of an object participating in reconciling.
 */
public interface IReconcilingParticipant {
	
	/**
	 * Called after reconciling has been finished.
	 */
	void reconciled();
}
