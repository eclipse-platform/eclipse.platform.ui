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
	ContentAssistEvent(ContentAssistant ca, TextContentAssistInvocationContext ctx,
			IContentAssistProcessor proc, int rep) {
		assistant= ca;
		context= ctx;
		processor= proc;
		repetition= rep;
	}

	/**
	 * The content assistant computing proposals.
	 */
	public final ContentAssistant assistant;

	/**
	 * The content assist invocation context.
	 */
	public final TextContentAssistInvocationContext context;

	/**
	 * The content assist processor that will be queried for proposals, depending on the current
	 * partition type.
	 */
	public final IContentAssistProcessor processor;

	/**
	 * The repetition count, i.e. how many times that content assist was invoked in one content
	 * assist session. The counter is reset once the popup is closed.
	 */
	public final int repetition;
}