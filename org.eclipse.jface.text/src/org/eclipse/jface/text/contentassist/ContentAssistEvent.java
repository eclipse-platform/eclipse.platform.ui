package org.eclipse.jface.text.contentassist;

/**
 * Describes the state that the content assistant is in when completing proposals.
 * <p>
 * Clients may use this class.
 * </p>
 * <p>
 * XXX this API is provisional and may change anytime during the course of 3.2
 * </p>
 * 
 * @since 3.2
 */
public final class ContentAssistEvent {

	ContentAssistEvent(ContentAssistant ca, IContentAssistProcessor proc) {
		assistant= ca;
		processor= proc;
	}

	/**
	 * The content assistant computing proposals.
	 */
	public final ContentAssistant assistant;
	public final IContentAssistProcessor processor;
}