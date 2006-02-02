package org.eclipse.jface.text.contentassist;

/**
 * Describes the state that the content assistant is in when completing proposals.
 * <p>
 * Clients may use this class.
 * </p>
 * 
 * @since 3.2
 * @see ICompletionListener
 */
public final class ContentAssistEvent {
	/**
	 * Creates a new event.
	 * 
	 * @param ca the assistant
	 * @param proc the processor
	 */
	ContentAssistEvent(IContentAssistant ca, IContentAssistProcessor proc) {
		assistant= ca;
		processor= proc;
	}

	/**
	 * The content assistant computing proposals.
	 */
	public final IContentAssistant assistant;
	/**
	 * The processor for the current partition.
	 */
	public final IContentAssistProcessor processor;
}