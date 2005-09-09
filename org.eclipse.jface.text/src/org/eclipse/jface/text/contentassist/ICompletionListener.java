package org.eclipse.jface.text.contentassist;

/**
 * A completion listener is informed before the content assisant computes completion proposals.
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
	 * Informs the receiver that completion proposals will be computed.
	 * 
	 * @param event the content assist event
	 */
	void computingProposals(ContentAssistEvent event);
}