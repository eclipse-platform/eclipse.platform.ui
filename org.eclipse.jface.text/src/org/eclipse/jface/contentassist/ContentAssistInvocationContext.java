/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.contentassist;

/**
 * Describes the context of an invocation of content assist. For a text editor, the context would
 * typically include the document (or the viewer) and the selection range, while source code editors
 * may provide specific context information such as an AST.
 * <p>
 * An invocation context may also compute additional context information on demand and cache it to
 * make it available to all {@link org.eclipse.jface.text.contentassist.ICompletionProposalComputer}s
 * contributing proposals to one content assist invocation.
 * </p>
 * <p>
 * Clients may subclass but must be careful to adhere to the described
 * {@link #equals(Object) equality} contract.
 * </p>
 * <p>
 * XXX this API is provisional and may change anytime during the course of 3.2
 * </p>
 * 
 * @since 3.2
 */
public abstract class ContentAssistInvocationContext {
	
	/**
	 * Invocation contexts are equal if they describe the same context and are of the same type.
	 * This implementation checks for <code>null</code> values and class equality. Subclasses
	 * should extend this method by adding checks for their context relevant fields (but not
	 * necessarily cached values).
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * class MyContext extends ContentAssistInvocationContext {
	 * 	private final Object fState;
	 * 	private Object fCachedInfo;
	 * 
	 * 	...
	 * 
	 * 	public boolean equals(Object obb) {
	 * 		if (!super.equals(obj))
	 * 			return false;
	 * 		MyContext other= (MyContext) obj;
	 * 		return fState.equals(other.fState);
	 * 	}
	 * }
	 * </pre>
	 * 
	 * </p>
	 * <p>
	 * Subclasses should also extend {@link Object#hashCode()}.
	 * </p>
	 * 
	 * @param obj {@inheritDoc}
	 * @return {@inheritDoc}
	 */
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!getClass().equals(obj.getClass()))
			return false;

		return true;
	}
	
	/*
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return 23459213; // random
	}

}
