/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Abstract implementation of <code>ILineTracker</code>. It lets the definition of line
 * delimiters to subclasses. Assuming that '\n' is the only line delimiter, this abstract
 * implementation defines the following line scheme:
 * <ul>
 * <li> "" -&gt; [0,0]
 * <li> "a" -&gt; [0,1]
 * <li> "\n" -&gt; [0,1], [1,0]
 * <li> "a\n" -&gt; [0,2], [2,0]
 * <li> "a\nb" -&gt; [0,2], [2,1]
 * <li> "a\nbc\n" -&gt; [0,2], [2,3], [5,0]
 * </ul>
 * <p>
 * This class must be subclassed.
 * </p>
 */
public abstract class AbstractLineTracker implements ILineTracker, ILineTrackerExtension {

	/**
	 * Tells whether this class is in debug mode.
	 *
	 * @since 3.1
	 */
	private static final boolean DEBUG= false;

	/**
	 * Combines the information of the occurrence of a line delimiter. <code>delimiterIndex</code>
	 * is the index where a line delimiter starts, whereas <code>delimiterLength</code>,
	 * indicates the length of the delimiter.
	 * @since 3.10
	 */
	public static class DelimiterInfo {
		public int delimiterIndex;
		public int delimiterLength;
		public String delimiter;
	}

	/**
	 * Representation of replace and set requests.
	 *
	 * @since 3.1
	 */
	protected static class Request {
		public final int offset;
		public final int length;
		public final String text;

		public Request(int offset, int length, String text) {
			this.offset= offset;
			this.length= length;
			this.text= text;
		}

		public Request(String text) {
			this.offset= -1;
			this.length= -1;
			this.text= text;
		}

		public boolean isReplaceRequest() {
			return this.offset > -1 && this.length > -1;
		}
	}
	
	/**
	 * Holder of the active {@link DocumentRewriteSession} with associated list of {@link Request}
	 * objects.
	 * <p>
	 * On starting new {@link DocumentRewriteSession} or on the end of the active
	 * {@link DocumentRewriteSession} this object is being replaced by another one.
	 * <p>
	 * 
	 * @see AbstractLineTracker#startRewriteSession(DocumentRewriteSession)
	 * @see AbstractLineTracker#stopRewriteSession(DocumentRewriteSession, String)
	 */
	private static class SessionData {

		/**
		 * The active rewrite session. Not final, but can only be changed to null.
		 *
		 * @since 3.1
		 */
		private volatile DocumentRewriteSession fActiveRewriteSession;
		/**
		 * The list of pending requests.
		 *
		 * @since 3.1
		 */
		private List<Request> fPendingRequests;
		
		/**
		 * @param activeRewriteSession may be null
		 */
		SessionData(DocumentRewriteSession activeRewriteSession){
			fActiveRewriteSession = activeRewriteSession;
			if (activeRewriteSession != null) {
				fPendingRequests = new ArrayList<>(20);
			} else {
				fPendingRequests = Collections.emptyList();
			}
		}
		
		boolean isSessionActive() {
			return fActiveRewriteSession != null;
		}
		
		boolean setIfActive(String text) {
			if (isSessionActive()) {
				synchronized (this) {
					if (!isSessionActive()) {						
						return false;
					}
					fPendingRequests.clear();
					fPendingRequests.add(new Request(text));
				}
				return true;
			} else {
				return false;
			}
		}

		boolean addIfActive(int offset, int length, String text) {
			if (isSessionActive()) {
				synchronized (this) {
					if (!isSessionActive()) {						
						return false;
					}
					fPendingRequests.add(new Request(offset, length, text));
				}
				return true;
			} else {
				return false;
			}
		}
		
		Iterator<Request> flush() {
			synchronized (this) {				
				fActiveRewriteSession = null;
				Iterator<Request> requests = fPendingRequests.iterator();
				fPendingRequests = Collections.emptyList();
				return requests;
			}
		}

		boolean sameSession(DocumentRewriteSession session) {
			return fActiveRewriteSession == session;
		}
		
		@Override
		public String toString() {
			StringBuilder builder= new StringBuilder();
			builder.append("SessionData ["); //$NON-NLS-1$
			builder.append("activeRewriteSession="); //$NON-NLS-1$
			builder.append(fActiveRewriteSession);
			builder.append(", "); //$NON-NLS-1$
			builder.append("pendingRequests="); //$NON-NLS-1$
			builder.append(fPendingRequests);
			builder.append("]"); //$NON-NLS-1$
			return builder.toString();
		}
	}

	private volatile SessionData sessionData;
	private final Object sessionLock = new Object();
	
	/**
	 * The implementation that this tracker delegates to.
	 *
	 * @since 3.2
	 */
	private volatile ILineTracker fDelegate= new ListLineTracker() {
		@Override
		public String[] getLegalLineDelimiters() {
			return AbstractLineTracker.this.getLegalLineDelimiters();
		}

		@Override
		protected DelimiterInfo nextDelimiterInfo(String text, int offset) {
			return AbstractLineTracker.this.nextDelimiterInfo(text, offset);
		}
	};
	/**
	 * Whether the delegate needs conversion when the line structure is modified.
	 */
	private boolean fNeedsConversion= true;

	/**
	 * Creates a new line tracker.
	 */
	protected AbstractLineTracker() {
		sessionData = new SessionData(null);
	}

	@Override
	public int computeNumberOfLines(String text) {
		return fDelegate.computeNumberOfLines(text);
	}

	@Override
	public String getLineDelimiter(int line) throws BadLocationException {
		checkRewriteSession();
		return fDelegate.getLineDelimiter(line);
	}

	@Override
	public IRegion getLineInformation(int line) throws BadLocationException {
		checkRewriteSession();
		return fDelegate.getLineInformation(line);
	}

	@Override
	public IRegion getLineInformationOfOffset(int offset) throws BadLocationException {
		checkRewriteSession();
		return fDelegate.getLineInformationOfOffset(offset);
	}

	@Override
	public int getLineLength(int line) throws BadLocationException {
		checkRewriteSession();
		return fDelegate.getLineLength(line);
	}

	@Override
	public int getLineNumberOfOffset(int offset) throws BadLocationException {
		checkRewriteSession();
		return fDelegate.getLineNumberOfOffset(offset);
	}

	@Override
	public int getLineOffset(int line) throws BadLocationException {
		checkRewriteSession();
		return fDelegate.getLineOffset(line);
	}

	@Override
	public int getNumberOfLines() {
		try {
			checkRewriteSession();
		} catch (BadLocationException x) {
			// TODO there is currently no way to communicate that exception back to the document
		}
		return fDelegate.getNumberOfLines();
	}

	@Override
	public int getNumberOfLines(int offset, int length) throws BadLocationException {
		checkRewriteSession();
		return fDelegate.getNumberOfLines(offset, length);
	}

	@Override
	public void set(String text) {
		boolean hasActiveRewriteSession = sessionData.setIfActive(text);
		if (hasActiveRewriteSession) {
			return;
		}

		fDelegate.set(text);
	}

	@Override
	public void replace(int offset, int length, String text) throws BadLocationException {
		boolean hasActiveRewriteSession = sessionData.addIfActive(offset, length, text);
		if (hasActiveRewriteSession) {
			return;
		}

		checkImplementation();

		fDelegate.replace(offset, length, text);
	}

	/**
	 * Converts the implementation to be a {@link TreeLineTracker} if it isn't yet.
	 *
	 * @since 3.2
	 */
	private synchronized void checkImplementation() {
		if (fNeedsConversion) {
			fNeedsConversion= false;
			fDelegate= new TreeLineTracker((ListLineTracker) fDelegate) {
				@Override
				protected DelimiterInfo nextDelimiterInfo(String text, int offset) {
					return AbstractLineTracker.this.nextDelimiterInfo(text, offset);
				}

				@Override
				public String[] getLegalLineDelimiters() {
					return AbstractLineTracker.this.getLegalLineDelimiters();
				}
			};
		}
	}

	/**
	 * Returns the information about the first delimiter found in the given text starting at the
	 * given offset.
	 *
	 * @param text the text to be searched
	 * @param offset the offset in the given text
	 * @return the information of the first found delimiter or <code>null</code>
	 */
	protected abstract DelimiterInfo nextDelimiterInfo(String text, int offset);

	@Override
	public final void startRewriteSession(DocumentRewriteSession session) {
		synchronized (sessionLock) {
			if (sessionData.isSessionActive()){
				throw new IllegalStateException("Rewrite session is already active: " + sessionData); //$NON-NLS-1$
			}
			sessionData = new SessionData(session);
		}
	}

	@Override
	public final void stopRewriteSession(DocumentRewriteSession session, String text) {
		synchronized (sessionLock) {
			if (sessionData.sameSession(session)) {
				sessionData = new SessionData(null);
				set(text);
			}
		}
	}

	/**
	 * Tells whether there's an active rewrite session.
	 *
	 * @return <code>true</code> if there is an active rewrite session, <code>false</code>
	 *         otherwise
	 * @since 3.1
	 */
	protected final boolean hasActiveRewriteSession() {
		return sessionData.isSessionActive();
	}

	/**
	 * Flushes the active rewrite session.
	 *
	 * @throws BadLocationException in case the recorded requests cannot be processed correctly
	 * @since 3.1
	 */
	protected final void flushRewriteSession() throws BadLocationException {
		if (DEBUG)
			System.out.println("AbstractLineTracker: Flushing rewrite session: " + sessionData); //$NON-NLS-1$
		synchronized (sessionData) {
			Iterator<Request> e= sessionData.flush();
			while (e.hasNext()) {
				Request request= e.next();
				if (request.isReplaceRequest())
					replace(request.offset, request.length, request.text);
				else
					set(request.text);
			}
		}
	}

	/**
	 * Checks the presence of a rewrite session and flushes it.
	 *
	 * @throws BadLocationException in case flushing does not succeed
	 * @since 3.1
	 */
	protected final void checkRewriteSession() throws BadLocationException {
		if (hasActiveRewriteSession())
			flushRewriteSession();
	}
}
