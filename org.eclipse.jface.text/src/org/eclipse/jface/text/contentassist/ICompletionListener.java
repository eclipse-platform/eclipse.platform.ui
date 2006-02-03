package org.eclipse.jface.text.contentassist;

/**
 * A completion listener is informed before the content assistant computes completion proposals.
 * <p>
 * Clients may implement this interface.
 * </p>
 * 
 * @since 3.2
 */
public interface ICompletionListener {
	/**
	 * Called when code assist is invoked when there is no current code assist session.
	 * 
	 * @param event the content assist event
	 */
	void assistSessionStarted(ContentAssistEvent event);

	/**
	 * Called when a code assist session ends (for example, the proposal popup is closed).
	 * 
	 * @param event the content assist event
	 */
	void assistSessionEnded(ContentAssistEvent event);

	/**
	 * Called when the selection in the proposal popup is changed or if the insert-mode changed.
	 * 
	 * @param proposal the newly selected proposal, possibly <code>null</code>
	 * @param smartToggle <code>true</code> if the insert-mode toggle is being pressed,
	 *        <code>false</code> otherwise
	 */
	void selectionChanged(ICompletionProposal proposal, boolean smartToggle);
}