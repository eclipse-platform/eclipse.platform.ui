/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search2.internal.ui.text;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.resources.IFile;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.LocationKind;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.ui.IQueryListener;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultListener;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.SearchResultEvent;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IFileMatchAdapter;
import org.eclipse.search.ui.text.Match;
import org.eclipse.search.ui.text.MatchEvent;
import org.eclipse.search.ui.text.RemoveAllEvent;


public class PositionTracker implements IQueryListener, ISearchResultListener, IFileBufferListener {

	private Map fMatchesToPositions= new HashMap();
	private Map fMatchesToSearchResults= new HashMap();
	private Map fFileBuffersToMatches= new HashMap();

	private interface IFileBufferMatchOperation {
		void run(ITextFileBuffer buffer, Match match);
	}

	public PositionTracker() {
		NewSearchUI.addQueryListener(this);
		FileBuffers.getTextFileBufferManager().addFileBufferListener(this);
	}

	// tracking search results --------------------------------------------------------------
	public void queryAdded(ISearchQuery query) {
		if (query.getSearchResult() instanceof AbstractTextSearchResult) {
			query.getSearchResult().addListener(this);
		}
	}

	public void queryRemoved(ISearchQuery query) {
		ISearchResult result= query.getSearchResult();
		if (result instanceof AbstractTextSearchResult) {
			untrackAll((AbstractTextSearchResult)result);
			result.removeListener(this);
		}
	}

	// tracking matches ---------------------------------------------------------------------
	public void searchResultChanged(SearchResultEvent e) {
		if (e instanceof MatchEvent) {
			MatchEvent evt= (MatchEvent)e;
			Match[] matches = evt.getMatches();
			int kind = evt.getKind();
			AbstractTextSearchResult result = (AbstractTextSearchResult) e.getSearchResult();
			for (int i = 0; i < matches.length; i++) {
				ITextFileBuffer fb= getTrackedFileBuffer(result, matches[i].getElement());
				if (fb != null) {
					updateMatch(matches[i], fb, kind, result);
				}
			}
		} else if (e instanceof RemoveAllEvent) {
			RemoveAllEvent evt= (RemoveAllEvent)e;
			ISearchResult result= evt.getSearchResult();
			untrackAll((AbstractTextSearchResult)result);
		}
	}

	private void updateMatch(Match match, ITextFileBuffer fb, int kind, AbstractTextSearchResult result) {
		if (kind == MatchEvent.ADDED) {
			trackPosition(result, fb, match);
		} else if (kind == MatchEvent.REMOVED) {
			untrackPosition(fb, match);
		}
	}

	private void untrackAll(AbstractTextSearchResult result) {
		Set matchSet= new HashSet(fMatchesToPositions.keySet());
		for (Iterator matches= matchSet.iterator(); matches.hasNext();) {
			Match match= (Match) matches.next();
			AbstractTextSearchResult matchContainer= (AbstractTextSearchResult) fMatchesToSearchResults.get(match);
			if (result.equals(matchContainer)) {
				ITextFileBuffer fb= getTrackedFileBuffer(result, match.getElement());
				if (fb != null) {
					untrackPosition(fb, match);
				}
			}
		}
	}

	private void untrackPosition(ITextFileBuffer fb, Match match) {
		Position position= (Position) fMatchesToPositions.get(match);
		if (position != null) {
			removeFileBufferMapping(fb, match);
			fMatchesToSearchResults.remove(match);
			fMatchesToPositions.remove(match);
			fb.getDocument().removePosition(position);
		}
	}

	private void trackPosition(AbstractTextSearchResult result, ITextFileBuffer fb, Match match) {
		int offset = match.getOffset();
		int length = match.getLength();
		if (offset < 0 || length < 0)
			return;

		try {
			IDocument doc= fb.getDocument();
			Position position= new Position(offset, length);
			if (match.getBaseUnit() == Match.UNIT_LINE) {
				position= convertToCharacterPosition(position, doc);
			}
			doc.addPosition(position);
			fMatchesToSearchResults.put(match, result);
			fMatchesToPositions.put(match, position);
			addFileBufferMapping(fb, match);
		} catch (BadLocationException e) {
			// the match is outside the document
			result.removeMatch(match);
		}
	}

	public static Position convertToCharacterPosition(Position linePosition, IDocument doc) throws BadLocationException {
		int lineOffset= linePosition.getOffset();
		int lineLength= linePosition.getLength();

		int charOffset= doc.getLineOffset(lineOffset);
		int charLength= 0;
		if (lineLength > 0) {
			int lastLine= lineOffset+lineLength-1;
			int endPosition= doc.getLineOffset(lastLine)+doc.getLineLength(lastLine);
			charLength= endPosition-charOffset;
		}
		return new Position(charOffset, charLength);
	}

	private void addFileBufferMapping(ITextFileBuffer fb, Match match) {
		Set matches= (Set) fFileBuffersToMatches.get(fb);
		if (matches == null) {
			matches= new HashSet();
			fFileBuffersToMatches.put(fb, matches);
		}
		matches.add(match);
	}

	private void removeFileBufferMapping(ITextFileBuffer fb, Match match) {
		Set matches= (Set) fFileBuffersToMatches.get(fb);
		if (matches != null) {
			matches.remove(match);
			if (matches.size() == 0)
				fFileBuffersToMatches.remove(fb);
		}
	}

	private ITextFileBuffer getTrackedFileBuffer(AbstractTextSearchResult result, Object element) {
		IFileMatchAdapter adapter= result.getFileMatchAdapter();
		if (adapter == null)
			return null;
		IFile file= adapter.getFile(element);
		if (file == null)
			return null;
		if (!file.exists())
			return null;
		return FileBuffers.getTextFileBufferManager().getTextFileBuffer(file.getFullPath(), LocationKind.IFILE);
	}

	public Position getCurrentPosition(Match match) {
		Position pos= (Position)fMatchesToPositions.get(match);
		if (pos == null)
			return pos;
		AbstractTextSearchResult result= (AbstractTextSearchResult) fMatchesToSearchResults.get(match);
		if (match.getBaseUnit() == Match.UNIT_LINE && result != null) {
			ITextFileBuffer fb= getTrackedFileBuffer(result, match.getElement());
			if (fb != null) {
				IDocument doc= fb.getDocument();
				try {
					pos= convertToLinePosition(pos, doc);
				} catch (BadLocationException e) {

				}
			}
		}

		return pos;
	}

	public static Position convertToLinePosition(Position pos, IDocument doc) throws BadLocationException {
		int offset= doc.getLineOfOffset(pos.getOffset());
		int end= doc.getLineOfOffset(pos.getOffset()+pos.getLength());
		int lineLength= end-offset;
		if (pos.getLength() > 0 && lineLength == 0) {
			// if the character length is > 0, add the last line, too
			lineLength++;
		}
		return new Position(offset, lineLength);
	}

	public void dispose() {
		NewSearchUI.removeQueryListener(this);
		FileBuffers.getTextFileBufferManager().removeFileBufferListener(this);
	}

	// IFileBufferListener implementation ---------------------------------------------------------------------
	/* (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#bufferCreated(org.eclipse.core.filebuffers.IFileBuffer)
	 */
	public void bufferCreated(IFileBuffer buffer) {
		final int[] trackCount= new int[1];
		if (!(buffer instanceof ITextFileBuffer))
			return;

		IPath location= buffer.getLocation();
		if (location == null)
			return;

		IFile file= FileBuffers.getWorkspaceFileAtLocation(location);
		if (file == null)
			return;

		ISearchQuery[] queries= NewSearchUI.getQueries();
		for (int i = 0; i < queries.length; i++) {
			ISearchResult result = queries[i].getSearchResult();
			if (result instanceof AbstractTextSearchResult) {
				AbstractTextSearchResult textResult = (AbstractTextSearchResult) result;
				IFileMatchAdapter adapter = textResult.getFileMatchAdapter();
				if (adapter != null) {
					Match[] matches = adapter.computeContainedMatches(textResult, file);
					for (int j = 0; j < matches.length; j++) {
						trackCount[0]++;
						trackPosition((AbstractTextSearchResult) result, (ITextFileBuffer) buffer, matches[j]);
					}
				}
			}
		}
	}

	private void doForExistingMatchesIn(IFileBuffer buffer, IFileBufferMatchOperation operation) {
		if (!(buffer instanceof ITextFileBuffer))
			return;
		Set matches= (Set) fFileBuffersToMatches.get(buffer);
		if (matches != null) {
			Set matchSet= new HashSet(matches);
			for (Iterator matchIterator= matchSet.iterator(); matchIterator.hasNext();) {
				Match element= (Match) matchIterator.next();
				operation.run((ITextFileBuffer) buffer, element);
			}
		}
	}


	/* (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#bufferDisposed(org.eclipse.core.filebuffers.IFileBuffer)
	 */
	public void bufferDisposed(IFileBuffer buffer) {
		final int[] trackCount= new int[1];
		doForExistingMatchesIn(buffer, new IFileBufferMatchOperation() {
			/* (non-Javadoc)
			 * @see org.eclipse.search.internal.model.PositionTracker.FileBufferMatchRunnable#run(org.eclipse.core.filebuffers.ITextFileBuffer, org.eclipse.search.ui.model.text.Match)
			 */
			public void run(ITextFileBuffer textBuffer, Match match) {
				trackCount[0]++;
				untrackPosition(textBuffer, match);
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#bufferContentAboutToBeReplaced(org.eclipse.core.filebuffers.IFileBuffer)
	 */
	public void bufferContentAboutToBeReplaced(IFileBuffer buffer) {
		// not interesting for us.
	}

	public void bufferContentReplaced(IFileBuffer buffer) {
		final int[] trackCount= new int[1];
		doForExistingMatchesIn(buffer, new IFileBufferMatchOperation() {
			public void run(ITextFileBuffer textBuffer, Match match) {
				trackCount[0]++;
				AbstractTextSearchResult result= (AbstractTextSearchResult) fMatchesToSearchResults.get(match);
				untrackPosition(textBuffer, match);
				trackPosition(result, textBuffer, match);
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#stateChanging(org.eclipse.core.filebuffers.IFileBuffer)
	 */
	public void stateChanging(IFileBuffer buffer) {
		// not interesting for us
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#dirtyStateChanged(org.eclipse.core.filebuffers.IFileBuffer, boolean)
	 */
	public void dirtyStateChanged(IFileBuffer buffer, boolean isDirty) {
		if (isDirty)
			return;
		final int[] trackCount= new int[1];
		doForExistingMatchesIn(buffer, new IFileBufferMatchOperation() {
			/* (non-Javadoc)
			 * @see org.eclipse.search.internal.model.PositionTracker.FileBufferMatchRunnable#run(org.eclipse.core.filebuffers.ITextFileBuffer, org.eclipse.search.ui.model.text.Match)
			 */
			public void run(ITextFileBuffer textBuffer, Match match) {
				trackCount[0]++;
				Position pos= (Position) fMatchesToPositions.get(match);
				if (pos != null) {
					if (pos.isDeleted()) {
						AbstractTextSearchResult result= (AbstractTextSearchResult) fMatchesToSearchResults.get(match);
						// might be that the containing element has been removed.
						if (result != null) {
							result.removeMatch(match);
						}
						untrackPosition(textBuffer, match);
					} else {
						if (match.getBaseUnit() == Match.UNIT_LINE) {
							try {
								pos= convertToLinePosition(pos, textBuffer.getDocument());
							} catch (BadLocationException e) {
								SearchPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, SearchPlugin.getID(), 0, e.getLocalizedMessage(), e));
							}
						}
						match.setOffset(pos.getOffset());
						match.setLength(pos.getLength());
					}
				}
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#stateValidationChanged(org.eclipse.core.filebuffers.IFileBuffer, boolean)
	 */
	public void stateValidationChanged(IFileBuffer buffer, boolean isStateValidated) {
		// not interesting for us.
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#underlyingFileMoved(org.eclipse.core.filebuffers.IFileBuffer, org.eclipse.core.runtime.IPath)
	 */
	public void underlyingFileMoved(IFileBuffer buffer, IPath path) {
		// not interesting for us.
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#underlyingFileDeleted(org.eclipse.core.filebuffers.IFileBuffer)
	 */
	public void underlyingFileDeleted(IFileBuffer buffer) {
		// not interesting for us.
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#stateChangeFailed(org.eclipse.core.filebuffers.IFileBuffer)
	 */
	public void stateChangeFailed(IFileBuffer buffer) {
		// not interesting for us.
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.IQueryListener#queryStarting(org.eclipse.search.ui.ISearchQuery)
	 */
	public void queryStarting(ISearchQuery query) {
		// not interested here
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.IQueryListener#queryFinished(org.eclipse.search.ui.ISearchQuery)
	 */
	public void queryFinished(ISearchQuery query) {
		// not interested
	}

}
