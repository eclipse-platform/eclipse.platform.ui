package org.eclipse.jface.text.contentassist;

/**
 * A completion listener is informed before the content assistant computes completion proposals.
 * <p>
 * Clients may implement this interface.
 * </p>
 * <p>
 * XXX this API is provisional and may change anytime during the course of 3.2
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
}