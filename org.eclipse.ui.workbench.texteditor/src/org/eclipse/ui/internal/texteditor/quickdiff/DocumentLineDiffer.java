/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor.quickdiff;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.SortedMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;

import org.eclipse.jface.util.Assert;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModelEvent;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.IAnnotationModelListenerExtension;
import org.eclipse.jface.text.source.ILineDiffInfo;
import org.eclipse.jface.text.source.ILineDiffer;

import org.eclipse.ui.texteditor.quickdiff.IQuickDiffReferenceProvider;

/**
 * Standard implementation of <code>ILineDiffer</code> as an incremental diff engine. A 
 * <code>DocumentLineDiffer</code> can be initialized to some start state. Once connected to a 
 * <code>IDocument</code> and a reference document has been set, changes reported via the 
 * <code>IDocumentListener</code> interface will be tracked and the incremental diff updated.
 * 
 * <p>The diff state can be queried using the <code>ILineDiffer</code> interface.</p>
 * 
 * <p>Since diff information is model information attached to a document, this class implements 
 * <code>IAnnotationModel</code> and can be attached to <code>IAnnotationModelExtension</code>s.</p>
 * 
 * <p>This class is not supposed to be subclassed.</p>
 * 
 * @since 3.0
 * @see IAnnotationModelExtension
 * @see DiffInitializer
 */
public class DocumentLineDiffer implements ILineDiffer, IDocumentListener, IAnnotationModel {

	/**
	 * Manages the reference document. Upon changes of the reference document, the differ is
	 * reinitialized.
	 */
	private class DocumentListener implements IDocumentListener {
		/** The reference document */
		private IDocument fDocument;

		/**
		 * Installs a new reference document. Any previously installed listener is removed.
		 * 
		 * @param doc the new reference document, or <code>null</code>.
		 */
		public void installDocument(IDocument doc) {
			if (fDocument != null)
				fDocument.removeDocumentListener(this);
			fDocument= doc;
			if (fDocument != null)
				fDocument.addDocumentListener(this);
		}

		/*
		 * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
		 */
		public void documentAboutToBeChanged(DocumentEvent event) {
		}

		/*
		 * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
		 */
		public void documentChanged(DocumentEvent event) {
			initialize();
		}

	}

	/**
	 * Call object returned by <code>analyzeEvent</code>. Contains the number of changed and either
	 * added or deleted lines, plus the first and last lines of the modification in the original (still
	 * unchanged) document.
	 */
	private final static class LineAnalysis {
		/** The first concerned line. */
		int firstLine;
		/** The last concerned line in the unchanged document. */
		int lastLine;
		/** 
		 * The number of lines deleted in the document; &lt;= lastLine - firstLine + 1. Either 
		 * <code>added</code> or <code>deleted</code> must be zero.
		 */
		int deleted;
		/** The number of inserted lines. Either <code>added</code> or <code>deleted</code> must be zero. */
		int added;
		/** The number of changed lines. */
		int changed;
	}

	/**
	 * The local implementation of <code>ILineDiffInfo</code>. As instances are also <code>Annotation</code>s,
	 * they can be used in <code>DocumentLineDiffer</code>s <code>IAnnotationModel</code> protocol.
	 */
	private static final class DiffRegion extends Annotation implements ILineDiffInfo {
		/** The region's type. */
		int type;
		/** The number of deleted lines after this line. */
		int deletedBehind;
		/** The number of deleted lines before this line. */
		int deletedBefore;
		/** The line's original content. */
		String restore;
		/** The original content of any deleted lines behind this line. */
		ArrayList hidden;

		/**
		 * Equivalent to <code>DiffRegion(type, removedLines, 0)</code>.
		 */
		DiffRegion(int type, int removedLines) {
			this(type, removedLines, 0);
		}

		/**
		 * Creates a new instance.
		 * 
		 * @param type the type of the instance, either <code>ADDED</code>, <code>CHANGED</code>, 
		 * or <code>UNCHANGED</code>.
		 * @param removedLines the number of deleted lines after this line, must be &gt;= 0.
		 * @param removedBefore the number of deleted lines after this line, must be &gt;= 0.
		 */
		DiffRegion(int type, int removedAfter, int removedBefore) {
			this.type= type;
			deletedBehind= removedAfter;
			deletedBefore= removedBefore;
			Assert.isTrue(type == ADDED || type == UNCHANGED || type == CHANGED);
			Assert.isTrue(removedAfter >= 0);
			Assert.isTrue(removedBefore >= 0);
		}

		/*
		 * @see org.eclipse.ui.internal.editors.text.LineNumberChangeRulerColumn.LineDiffer.LineDiffInfo#getRemovedLinesBelow()
		 */
		public int getRemovedLinesBelow() {
			return deletedBehind;
		}

		/*
		 * @see org.eclipse.ui.internal.editors.text.LineNumberChangeRulerColumn.LineDiffer.LineDiffInfo#getType()
		 */
		public int getType() {
			return type;
		}

		/*
		 * @see org.eclipse.ui.internal.editors.text.LineNumberChangeRulerColumn.LineDiffer.LineDiffInfo#getRemovedLinesAbove()
		 */
		public int getRemovedLinesAbove() {
			return deletedBefore;
		}

		/*
		 * @see org.eclipse.jface.text.source.Annotation#paint(org.eclipse.swt.graphics.GC, org.eclipse.swt.widgets.Canvas, org.eclipse.swt.graphics.Rectangle)
		 */
		public void paint(GC gc, Canvas canvas, Rectangle bounds) {
			// oh nothing painting is done by the line number bar...
		}

		/*
		 * @see org.eclipse.ui.internal.editors.text.LineNumberChangeRulerColumn.LineDiffer.LineDiffInfo#hasChanges()
		 */
		public boolean hasChanges() {
			return type != UNCHANGED || deletedBefore > 0 || deletedBehind > 0;
		}

		/*
		 * @see org.eclipse.ui.internal.editors.text.LineNumberChangeRulerColumn.LineDiffer.LineDiffInfo#getOriginalText()
		 */
		public String[] getOriginalText() {
			ArrayList ret= new ArrayList();
			if (restore != null)
				ret.add(restore);
			if (hidden != null)
				ret.addAll(hidden);
			return (String[])ret.toArray(new String[ret.size()]);
		}
	}
	
	private class FoldingIterator implements Iterator {

		/*
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			// TODO Auto-generated method stub
			return false;
		}

		/*
		 * @see java.util.Iterator#next()
		 */
		public Object next() {
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			// TODO Auto-generated method stub
			
		}
		
	}

	/** The provider for the reference document. */
	IQuickDiffReferenceProvider fReferenceProvider;
	/** Whether this differ is in sync with the model. */
	private boolean fIsSynchronized;
	/** The number of clients connected to this model. */
	private int fOpenConnections;
	/** Flag, when true, additional lines are not marked deleted. */
	private boolean fIsRestoring;
	/** The current document being tracked. */
	private IDocument fDocument;
	/** 
	 * Flag to indicate whether a change has been made to the line table and any clients should 
	 * update their presentation.
	 */
	private boolean fUpdateNeeded;
	/**
	 * The line table. We want a <code>List</code> with the position in the list being the line number
	 * of a change. A LinkedList would be fast on changes on the line set (i.e. additions and 
	 * deletions of lines) but would be expensive on random access, which we want to be fast in order
	 * to accomodate access to only a certain range of lines (for display...). 
	 */
	private List fLines= new ArrayList(100);
	/** The listeners on this annotation model. */
	private List fAnnotationModelListeners= new ArrayList();
	/** Our document listener on the reference document. */
	final DocumentListener fReferenceListener= new DocumentListener();
	/** The lines changed in one document modification. The lines are checked whether they are real changes after their application. */
	private Set fModified= new HashSet();
	/** The job currently initializing the differ, or <code>null</code> if there is none. */
	private Job fInitializationJob;
	/** Stores <code>DocumentEvents</code> while an initialization is going on. */
	private List fStoredEvents= new ArrayList();

	/**
	 * Creates a new differ.
	 */
	public DocumentLineDiffer() {
	}

	/* ILineDiffer implementation */

	/*
	 * @see org.eclipse.jface.text.source.ILineDiffer#getLineInfo(int)
	 */
	public ILineDiffInfo getLineInfo(int line) {
		if (fLines.size() > line && line >= 0)
			return (DiffRegion)fLines.get(line);
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.source.ILineDiffer#revertLine(int)
	 */
	public synchronized void revertLine(int line) throws BadLocationException {
		if (!isInitialized())
			throw new BadLocationException(QuickDiffMessages.getString("quickdiff.nonsynchronized")); //$NON-NLS-1$
			
		revertLine(line, false);
	}

	/*
	 * @see org.eclipse.jface.text.source.ILineDiffer#revertBlock(int)
	 */
	public synchronized void revertBlock(int line) throws BadLocationException {
		if (!isInitialized())
			throw new BadLocationException(QuickDiffMessages.getString("quickdiff.nonsynchronized")); //$NON-NLS-1$

		/* Grow block forward and backward */
		final int nLines= fLines.size();
		if (line < 0 || line >= nLines)
			throw new BadLocationException("index out of bounds"); //$NON-NLS-1$
		int from= line + 1, to= line - 1;

		// grow backward
		while (from > 0) {
			DiffRegion region= (DiffRegion)fLines.get(from - 1);
			if (region.type == ILineDiffInfo.UNCHANGED)
				break;
			from--;
		}

		// grow forward
		while (to < nLines - 1) {
			DiffRegion region= (DiffRegion)fLines.get(to + 1);
			if (region.type == ILineDiffInfo.UNCHANGED)
				break;
			to++;
		}

		revertSelection(from, to - from + 1);
	}

	/*
	 * @see org.eclipse.jface.text.source.ILineDiffer#revertSelection(int, int)
	 */
	public synchronized void revertSelection(int line, int nLines) throws BadLocationException {
		if (!isInitialized())
			throw new BadLocationException(QuickDiffMessages.getString("quickdiff.nonsynchronized")); //$NON-NLS-1$

		if (nLines < 1)
			return;

		// restore after first line
		line += internalRestoreAfterLine(line - 1);
		int to= line + nLines - 1;
		int lineDelta= 0; // accumulates changes to the line table by restoration / deletion
		while (line <= to) {
			lineDelta += revertLine(lineDelta + line++, true);
		}
	}

	/*
	 * @see org.eclipse.jface.text.source.ILineDiffer#restoreAfterLine(int)
	 */
	public synchronized int restoreAfterLine(int line) throws BadLocationException {
		if (!isInitialized())
			throw new BadLocationException(QuickDiffMessages.getString("quickdiff.nonsynchronized")); //$NON-NLS-1$

		return internalRestoreAfterLine(line);
	}

	/**
	 * Reverts the line with line number <code>line</code>.
	 * 
	 * @param line the line number of the line to be reverted.
	 * @param restore <code>true</code> if deleted lines after the line should also be restored.
	 * @return the number of inserted lines;
	 */
	private int revertLine(int line, boolean restore) throws BadLocationException {
		IDocument doc= getDocument();
		if (doc != null && line < fLines.size()) {
			DiffRegion dr= (DiffRegion)fLines.get(line);
			int deleted= 0;
			if (dr.type == ILineDiffInfo.CHANGED) {
				Assert.isNotNull(dr.restore, "restore buffer is empty"); //$NON-NLS-1$
				fIsRestoring= true;
				doc.replace(doc.getLineOffset(line), doc.getLineLength(line), dr.restore);
				fIsRestoring= false;
			} else if (dr.type == ILineDiffInfo.ADDED) {
				fIsRestoring= true;
				doc.replace(doc.getLineOffset(line), doc.getLineLength(line), ""); //$NON-NLS-1$
				fIsRestoring= false;
				deleted= 1;
			}
			int restored= 0;
			if (restore)
				restored= internalRestoreAfterLine(line);
			return restored - deleted;
		}
		return 0;
	}

	/**
	 * Does the work on <code>restoreAfterLine</code>, without compound change notification.
	 * 
	 * @param line the line where deleted lines are to be restored.
	 * @return the number of restored lines
	 * @throws BadLocationException if there are concurrent modifications to the document
	 */
	private int internalRestoreAfterLine(int line) throws BadLocationException {
		IDocument doc= getDocument();
		if (doc == null)
			return 0;

		if (line >= 0 && line < fLines.size()) {
			DiffRegion dr= (DiffRegion)fLines.get(line);
			final int nRestored= dr.hidden == null ? 0 : dr.hidden.size();
			if (nRestored > 0) {

				// build insertion text
				String insertion= ""; //$NON-NLS-1$
				for (Iterator it= dr.hidden.iterator(); it.hasNext();) {
					insertion += (String)it.next();
				}

				// compute insertion offset, handle EOF
				int offset;
				if (line < fLines.size() - 1) {
					offset= doc.getLineOffset(line + 1);
				} else {
					offset= doc.getLength();
					String delimiter;
					if (line > 0)
						delimiter= doc.getLineDelimiter(line - 1);
					else
						delimiter= doc.getLegalLineDelimiters()[0];
					insertion= delimiter + insertion;
				}

				// insert
				fIsRestoring= true;
				doc.replace(offset, 0, insertion);
				fIsRestoring= false;
			}
			return nRestored;
		}
		return 0;
	}

	/**
	 * Transforms the passed instances of <code>ILineDiffInfo</code> into the format used internally
	 * by this class. The passed instances of <code>ILineDiffInfo</code> will not be modified and no
	 * references to them will be kept inside the differ. The map must be sorted with low line numbers
	 * first. The passed map must not be empty.
	 * 
	 * <p>The document that the diff is based on should be the same as this instance is connected to.</p>
	 * 
	 * @param changes a <code>Map</code> of line numbers (<code>Integer</code>s) to 
	 * <code>ILineDiffInfo</code>s that will initialize the differ
	 * @param nLines the number of lines in the document. This number must correspond to the number of 
	 * lines in the document connected to this instance of <code>ILineDiffer</code>
	 * @return a <code>List</code> of <code>DiffRegion</code>s in the internal format used by this class 
	 */
	List initialize(SortedMap diffs, int nLines) {
		
		ArrayList table= new ArrayList(nLines);

		int nextChangedLine;
		ILineDiffInfo nextInfo= null; // will never be accessed if null
		Iterator it= diffs.keySet().iterator();
		if (it.hasNext()) {
			Integer n= (Integer)it.next();
			nextChangedLine= n.intValue();
			nextInfo= (ILineDiffInfo)diffs.get(n);
		} else {
			nextChangedLine= Integer.MAX_VALUE;
		}
		for (int line= 0; line < nLines; line++) {
			if (line == nextChangedLine) {
				Assert.isNotNull(nextInfo);
				DiffRegion newInfo= new DiffRegion(nextInfo.getType(), nextInfo.getRemovedLinesBelow(), nextInfo.getRemovedLinesAbove());
				String[] original= nextInfo.getOriginalText();
				for (int i= 0; i < original.length; i++) {
					if (i == 0)
						newInfo.restore= original[i];
					else {
						if (newInfo.hidden == null)
							newInfo.hidden= new ArrayList();
						newInfo.hidden.add(original[i]);
					}
				}
				newInfo.deletedBehind= Math.max(original.length - 1, 0);
				Assert.isTrue(newInfo.hidden == null || (newInfo.hidden.size() == newInfo.getRemovedLinesBelow()));

				table.add(newInfo);

				if (it.hasNext()) {
					Integer n= (Integer)it.next();
					nextChangedLine= n.intValue();
					nextInfo= (ILineDiffInfo)diffs.get(n);
				} else {
					nextChangedLine= Integer.MAX_VALUE;
				}
			} else {
				Assert.isTrue(line < nextChangedLine);
				table.add(new DiffRegion(ILineDiffInfo.UNCHANGED, 0));
			}
		}

		// second pass to fix deleted counts before a line entry
		if (table.size() > 0) {
			DiffRegion first, second= null;
			second= (DiffRegion)table.get(0);
			for (int line= 1; line < nLines; line++) {
				first= second;
				second= (DiffRegion)table.get(line);
				second.deletedBefore= first.deletedBehind;
			}
		}
		
		return table;
	}

	/**
	 * Returns the receivers initialization state.
	 * 
	 * @return <code>true</code> if we are initialized and in sync with the document.
	 */
	private boolean isInitialized() {
		return fIsSynchronized;
	}
	
	/**
	 * Returns the receivers synchronization state.
	 * 
	 * @return <code>true</code> if we are initialized and in sync with the document.
	 */
	public synchronized boolean isSynchronized() {
		return fIsSynchronized;
	}

	/**
	 * Sets the reference provider for this instance. If one has been installed before, it is 
	 * disposed.
	 * 
	 * @param provider the new provider
	 */
	public void setReferenceProvider(IQuickDiffReferenceProvider provider) {
		Assert.isNotNull(provider);
		if (provider != fReferenceProvider) {
			if (fReferenceProvider != null)
				fReferenceProvider.dispose();
			fReferenceProvider= provider;
			initialize();
		}
	}

	/**
	 * Returns the reference provider currently installed, or <code>null</code> if none is installed.
	 * 
	 * @return the current reference provider.
	 */
	public IQuickDiffReferenceProvider getReferenceProvider() {
		return fReferenceProvider;
	}

	/**
	 * (Re-)initializes the differ using the current reference and <code>DiffInitializer</code>
	 */
	synchronized void initialize() {
		fIsSynchronized= false;
		
		// for now: if a later invocation 
		if (fInitializationJob != null) {
			fInitializationJob.cancel();
			fInitializationJob= null;
		}
		
		// handle updates to the actual document.... should have temporary read-only copy of the documents
		// the same does not apply for the reference document, since we reinitialize everytime it changes.
		//
		// this will block the update of the document (since documentAboutToBeChanged is synchronized)
		// however, there is no other way to get a consistent known state (?).
		//		
		// TODO: use WorkingCopy for this?
		IDocument current= getDocument();
		if (current == null)
			return;
		final IDocument actual= new Document(current.get());
		
		fInitializationJob= new Job(QuickDiffMessages.getString("quickdiff.initialize")) { //$NON-NLS-1$

			public IStatus run(IProgressMonitor monitor) {
				final IDocument reference= fReferenceProvider == null ? null : fReferenceProvider.getReference();
				if (monitor != null && monitor.isCanceled())
					return Status.CANCEL_STATUS;

				if (reference == null) {
					fireModelChanged();
					return Status.OK_STATUS;
				}

				fReferenceListener.installDocument(reference);
				SortedMap map= DiffInitializer.initializeDiffer(monitor, reference, actual);
				List table= initialize(map, actual.getNumberOfLines());
				
				// set line table
				synchronized (DocumentLineDiffer.this) {
					if (fInitializationJob != this)
						return Status.OK_STATUS;
				
					fInitializationJob= null;
		
					fLines= table;
					fIsSynchronized= true;
					
					// reinject events accumulated in the meantime.
					for (ListIterator iter= fStoredEvents.listIterator(); iter.hasNext();) {
						DocumentEvent event= (DocumentEvent) iter.next();
						handleAboutToBeChanged(event);
						handleChange();
						iter.remove();
					}
			
					fUpdateNeeded= true;
				}
				
				fireModelChanged();
				return Status.OK_STATUS;
			}
			
		};
		
		fInitializationJob.setPriority(Job.DECORATE);
		fInitializationJob.schedule();
	}

	/**
	 * Returns the document we are working on.
	 * 
	 * @return the current document, or <code>null</code>
	 */
	protected IDocument getDocument() {
		return fDocument;
	}

	/* IDocumentListener implementation */

	/*
	 * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
	 */
	public synchronized void documentAboutToBeChanged(DocumentEvent event) {
		// if a initialization is going on, we just store the events in the meantime
		if (!isInitialized()) {
			fStoredEvents.add(event);
			return;
		}

		/* The invariant must hold before the document is changed. It will be temporarily violated
		 * after this method and before the changes are applied to the document, see documentChanged(DocumentEvent)
		 */
		invariant();
		
		handleAboutToBeChanged(event);
	}

	
	/**
	 * Unsynchronized version of <code>documentAboutToBeChanged</code>, called by <code>documentAboutToBeChanged</code>
	 * and {@link initialize(SortedMap, int) sortedMap(SortedMap, int)}. 
	 * 
	 * @param event the document event to be handled
	 */
	void handleAboutToBeChanged(DocumentEvent event) {
		IDocument doc= getDocument();
		if (doc == null)
			return;

		try {
			fModified.clear();
			LineAnalysis analysis= analyzeEvent(doc, event);
			int i= applyChanges(analysis);
			i= applyAdditions(analysis, i);
			applyDeletions(analysis, i);

		} catch (BadLocationException e) {
			// document asychronously modified, reinitialize
			initialize();
		}
	}

	/*
	 * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
	 */
	public synchronized void documentChanged(DocumentEvent event) {
		if (!isInitialized())
			return;
		
		invariant();
		
		handleChange();
		
		// inform listeners about change
		if (fUpdateNeeded) {
			fireModelChanged();
			fUpdateNeeded= false;
		}
	}

	void handleChange() {
		// check false positives - lines that are modified, but actually the same as the original
		for (Iterator it= fModified.iterator(); it.hasNext();) {
			int l= ((Integer)it.next()).intValue();
			DiffRegion r= (DiffRegion)fLines.get(l);
			if (r.getType() == ILineDiffInfo.CHANGED) {
				String cur= null;
				try {
					cur= fDocument.get(fDocument.getLineOffset(l), fDocument.getLineLength(l));
				} catch (BadLocationException e) {
				}
				if (r.restore.equals(cur)) {
					r.type= ILineDiffInfo.UNCHANGED;
					r.restore= null;
					fUpdateNeeded= true;
				}
			}
		}
	}

	/**
	 * Analyzes the event for the changed lines. An change analysis will tell
	 * <ul>
	 * <li>the first and last concerned lines (for changes / deletions)</li>
	 * <li>the number of changed lines (starting with the first line)</li>
	 * <li>the number of added lines (added after the changed lines)</li>
	 * <li>the number of deleted lines (deleted after the changed lines)</li>
	 * </ul>
	 * 
	 * <p>There are either added or deleted lines, but never both.</p>
	 * 
	 * @param doc the actual document
	 * @param event the modification event on that document (not yet applied, though)
	 * @return a <code>LineAnalysis</code> for the modifications of <code>event</code> on <code>document</code>
	 */
	private LineAnalysis analyzeEvent(IDocument doc, DocumentEvent event) throws BadLocationException {
		final int offset= event.getOffset();
		final int length= event.getLength();
		final String text= event.getText();
		final String replaced= doc.get(offset, length);

		final LineAnalysis a= new LineAnalysis();
		/*
		 * IDocument.getNumberOfLines will return at least 1, whereas
		 * IDocument.computeNumberOfLines will count the delimiters, so return at least 0.
		 * We correct this by subtracting one from the deletion count.
		 */
		final int deleted= doc.getNumberOfLines(offset, length) - 1;
		final int added= text == null ? 0 : doc.computeNumberOfLines(text);
		// changed lines: the lines that got deleted and replaced with other content 
		a.changed= Math.min(added, deleted) + 1;
		a.added= Math.max(added - deleted, 0);
		a.deleted= Math.max(deleted - added, 0);
		Assert.isTrue(a.added == 0 || a.deleted == 0);

		a.firstLine= doc.getLineOfOffset(offset);
		a.lastLine= doc.getLineOfOffset(offset + length);

		// adjust for special cases
		delimiterAdjust(doc, offset, length, text, replaced, a);
		return a;
	}

	/**
	 * Alters the analysis to account for special cases. We are not doing anything near full diffing,
	 * but cover the following:
	 * if a line appears to be altered from the change offset, but was just changed at its end or
	 * beginning, we do not consider it altered.
	 * 
	 * @param doc the current document
	 * @param offset the offset of the document change
	 * @param length the length of the document change
	 * @param text the text inserted by the document change
	 * @param replaced the text replaced by the document change
	 * @param a a call object where the changes are returned in
	 * @throws BadLocationException if <code>offset</code> or <code>length</code> are not valid position information for <code>doc</code> 
	 */
	private void delimiterAdjust(IDocument doc, int offset, int length, String text, String replaced, LineAnalysis a) throws BadLocationException {

		// the unaltered text right behind the insertion / deletion, if at EOF, we simulate an end line
		String behindInsert= (doc.getLength() > offset + length ? doc.get(offset + length, 1) : "\r"); //$NON-NLS-1$
		// the unaltered text right before the insertion, if at beginning of file, simulate an end line
		String beforeInsert= (offset > 0 ? doc.get(offset - 1, 1) : "\r"); //$NON-NLS-1$

		// 1: Insertion: 
		// an insertion before a line's delimiter that itself starts with a delimiter does not change
		// the line.
		if (isEmpty(replaced) && startsWithDelimiter(behindInsert) && startsWithDelimiter(text)) {
			firstUnchanged(a);
		}
		// an insertion at the beginning of a line does not change the line if the insertion text 
		// ends with a delimiter
		// Do either the first or this alteration, but not both (when inserting a line in an empty line)
		else if (isEmpty(replaced) && startsWithDelimiter(beforeInsert) && endsWithDelimiter(text)) {
			lastUnchanged(a);
		}

		// 2: Deletion:
		// Deleting an entire line excluding its delimiter, but including the delimiter of the line
		// before, does not change the line before.
		if (startsWithDelimiter(replaced) && startsWithDelimiter(behindInsert)) {
			firstUnchanged(a);
		}

		// Deleting an entire line including its delimiter (delete line) does not change the previous line
		// Do either of the alterations, but not both (when deleting a line preceded by an empty line)
		else if (startsWithDelimiter(beforeInsert) && endsWithDelimiter(replaced)) {
			lastUnchanged(a);
		}
	}

	/**
	 * Takes away the line change in the last line and moves the addition / deletion up one line.
	 * 
	 * @param a the line analysis to be altered.
	 */
	private void lastUnchanged(LineAnalysis a) {
		Assert.isTrue(a.changed > 0, "line must have changed lines"); //$NON-NLS-1$
		if (a.lastLine > a.firstLine)
			a.lastLine--; // additions do not span multiple lines, but are still legal.
		a.changed--;
	}

	/**
	 * Takes away the line change in the first line and moves the addition /deletion down one line.
	 * 
	 * @param a the line analysis to be altered.
	 */
	private void firstUnchanged(LineAnalysis a) {
		Assert.isTrue(a.changed > 0, "line must have changed lines"); //$NON-NLS-1$
		a.firstLine++;
		if (a.lastLine < a.firstLine)
			a.lastLine++;
		a.changed--;
	}

	/**
	 * Applies the changes contained in <code>analysis</code> to the line table.
	 * 
	 * @param analysis the change analysis to be applied
	 * @return the index after the application
	 */
	private int applyChanges(LineAnalysis analysis) throws BadLocationException {
		int i= analysis.firstLine;
		IDocument doc= getDocument();
		if (doc == null)
			throw new NullPointerException();
		for (; i < analysis.firstLine + analysis.changed; i++) {
			DiffRegion dr= (DiffRegion)fLines.get(i);
			if (dr.type == ILineDiffInfo.UNCHANGED) {
				int lineOffset= doc.getLineOffset(i);
				int lineLength= doc.getLineLength(i);
				dr.restore= doc.get(lineOffset, lineLength);
				dr.type= ILineDiffInfo.CHANGED;
				fModified.add(new Integer(i));
				fUpdateNeeded= true;
			} else if (dr.type == ILineDiffInfo.CHANGED && fIsRestoring) {
				fUpdateNeeded= true;
				dr.type= ILineDiffInfo.UNCHANGED;
				dr.restore= null;
			} else if (dr.type == ILineDiffInfo.CHANGED) {
				fModified.add(new Integer(i));
			}
		}
		return i;
	}

	/**
	 * Applies the additions contained in <code>analysis</code> to the line table.
	 * 
	 * @param analysis the change analysis to be applied
	 * @param i the index to add lines at
	 * @return the index after the application
	 */
	private int applyAdditions(LineAnalysis analysis, int i) throws BadLocationException {
		if (analysis.added > 0) {
			int add= analysis.added, change= 0, delCount= 0;
			ArrayList hidden= null;

			if (i > 0) {
				DiffRegion above= (DiffRegion)fLines.get(i - 1);
				change= Math.min(above.deletedBehind, analysis.added);
				add= analysis.added - change;
				delCount= above.deletedBehind - change;
				above.deletedBehind= 0;
				hidden= above.hidden;
				above.hidden= null;
			}
			Assert.isTrue(change > 0 || add > 0, "deleting 0 lines?"); //$NON-NLS-1$
			if (i < fLines.size()) {
				DiffRegion below= (DiffRegion)fLines.get(i);
				below.deletedBefore= delCount;
			}
			DiffRegion last= null; // will never be null, since added > 0
			for (int j= change - 1; j >= 0; j--) {
				if (fIsRestoring) {
					last= new DiffRegion(ILineDiffInfo.UNCHANGED, 0);
				} else {
					last= new DiffRegion(ILineDiffInfo.CHANGED, 0);
					fModified.add(new Integer(i + j));
				}
				if (hidden != null)
					last.restore= (String)hidden.remove(j);
				fLines.add(i, last);
			}
			for (int j= 0; j < add; j++) {
				if (fIsRestoring) {
					last= new DiffRegion(ILineDiffInfo.UNCHANGED, 0);
				} else {
					last= new DiffRegion(ILineDiffInfo.ADDED, 0);
				}
				fLines.add(i, last);
			}
			Assert.isTrue(hidden == null || hidden.size() == delCount, "hidden lines != hidden count"); //$NON-NLS-1$
			last.deletedBehind= delCount;
			last.hidden= hidden;
			fUpdateNeeded= true;
		}
		return i;
	}

	/**
	 * Applies the deletions in <code>analysis</code> to the line table.
	 * 
	 * @param analysis the change analysis to be applied
	 * @param i the index to start with the deletions
	 * @return the index after the application
	 */
	private int applyDeletions(LineAnalysis analysis, int i) throws BadLocationException {
		IDocument doc= getDocument();
		if (doc == null)
			throw new NullPointerException();
		int del= 0;
		ArrayList deletedLines= new ArrayList(10);
		for (int j= 0; j < analysis.deleted; j++) {
			DiffRegion dr= (DiffRegion)fLines.get(i);
			if (dr.type != ILineDiffInfo.ADDED) {
				del++;
				if (dr.restore != null)
					deletedLines.add(dr.restore);
				else
					deletedLines.add(doc.get(doc.getLineOffset(i + j), doc.getLineLength(i + j)));
			}
			del += dr.deletedBehind;
			if (dr.hidden != null)
				deletedLines.addAll(dr.hidden);
			fLines.remove(i);
			fUpdateNeeded= true;
			Assert.isTrue(del == deletedLines.size(), "deletedLines: " + deletedLines.size() + " del: " + del + "!"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
		}
		if (analysis.deleted > 0) {
			DiffRegion above= null, below= null;
			int deleted= del;
			if (i > 0) {
				above= (DiffRegion)fLines.get(i - 1);
				if (deleted > 0 && above.type == ILineDiffInfo.ADDED) {
					above.type= ILineDiffInfo.CHANGED;
					above.restore= (String)deletedLines.remove(0);
					deleted--;
				}
				deleted += above.deletedBehind;
				above.deletedBehind= deleted;
				if (above.hidden == null)
					above.hidden= new ArrayList(deletedLines);
				else
					above.hidden.addAll(deletedLines);
			}
			if (i < fLines.size()) {
				below= (DiffRegion)fLines.get(i);
				below.deletedBefore= deleted;
			}
		}
		return i;
	}

	/**
	 * Invariant that holds except for in the gap between <code>documentAboutToBeChanged</code> and
	 * <code>documentChanged</code>. 
	 */
	private synchronized void invariant() {
		if (!isInitialized())
			return;
		Assert.isTrue(fDocument != null && fReferenceProvider != null);
		IDocument doc= getDocument();
		if (doc != null) {
			Assert.isTrue(fLines.size() == doc.getNumberOfLines(), "Invariant violated (lines: " + doc.getNumberOfLines() + " size: " //$NON-NLS-1$ //$NON-NLS-2$
			+fLines.size() + ")"); //$NON-NLS-1$
		}
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationModel#addAnnotationModelListener(org.eclipse.jface.text.source.IAnnotationModelListener)
	 */
	public void addAnnotationModelListener(IAnnotationModelListener listener) {
		fAnnotationModelListeners.add(listener);
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationModel#removeAnnotationModelListener(org.eclipse.jface.text.source.IAnnotationModelListener)
	 */
	public void removeAnnotationModelListener(IAnnotationModelListener listener) {
		fAnnotationModelListeners.remove(listener);
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationModel#connect(org.eclipse.jface.text.IDocument)
	 */
	public void connect(IDocument document) {
		invariant();
		Assert.isTrue(fDocument == null || fDocument == document);
		++fOpenConnections;
		if (fOpenConnections == 1) {
			fDocument= document;
			document.addDocumentListener(this);
			initialize();
		}
		invariant();
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationModel#disconnect(org.eclipse.jface.text.IDocument)
	 */
	public void disconnect(IDocument document) {
		invariant();
		Assert.isTrue(fDocument == document);

		--fOpenConnections;
		if (fOpenConnections == 0) {
			fDocument.removeDocumentListener(this);
			fReferenceListener.installDocument(null);
			if (fReferenceProvider != null)
				fReferenceProvider.dispose();
			fDocument= null;
			synchronized (this) {
				fIsSynchronized= false;
			}
		}
		invariant();
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationModel#addAnnotation(org.eclipse.jface.text.source.Annotation, org.eclipse.jface.text.Position)
	 */
	public void addAnnotation(Annotation annotation, Position position) {
		throw new UnsupportedOperationException();
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationModel#removeAnnotation(org.eclipse.jface.text.source.Annotation)
	 */
	public void removeAnnotation(Annotation annotation) {
		throw new UnsupportedOperationException();
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationModel#getAnnotationIterator()
	 */
	public Iterator getAnnotationIterator() {
		return new FoldingIterator();
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationModel#getPosition(org.eclipse.jface.text.source.Annotation)
	 */
	public Position getPosition(Annotation annotation) {
		IDocument doc= getDocument();
		if (doc == null)
			return null;
		int pos= fLines.indexOf(annotation);
		if (pos >= 0 && pos < doc.getNumberOfLines()) {
			try {
				return new Position(doc.getLineOffset(pos), doc.getLineLength(pos));
			} catch (BadLocationException e) {
			}
		}
		return null;
	}

	/**
	 * Informs all annotation model listeners that this model has been changed.
	 */
	protected void fireModelChanged() {
		fireModelChanged(new AnnotationModelEvent(this));
	}

	/**
	 * Informs all annotation model listeners that this model has been changed
	 * as described in the annotation model event. The event is sent out
	 * to all listeners implementing <code>IAnnotationModelListenerExtension</code>.
	 * All other listeners are notified by just calling <code>modelChanged(IAnnotationModel)</code>.
	 * 
	 * @param event the event to be sent out to the listeners
	 */
	protected void fireModelChanged(AnnotationModelEvent event) {
		ArrayList v= new ArrayList(fAnnotationModelListeners);
		Iterator e= v.iterator();
		while (e.hasNext()) {
			IAnnotationModelListener l= (IAnnotationModelListener)e.next();
			if (l instanceof IAnnotationModelListenerExtension)
				 ((IAnnotationModelListenerExtension)l).modelChanged(event);
			else
				l.modelChanged(this);
		}
	}

	/* utility methods */

	/**
	 * @return <code>true</code> if <code>c</code> is part of a line delimiter, <code>false</code> otherwise.
	 */
	private boolean isDelimiter(char c) {
		return c == '\r' || c == '\n';
	}

	/**
	 * @return <code>true</code> if <code>s</code> starts with a line delimiter, <code>false</code> otherwise.
	 */
	private boolean startsWithDelimiter(String s) {
		return s != null && s.length() > 0 && isDelimiter(s.charAt(0));
	}

	/**
	 * @return <code>true</code> if <code>s</code> ends with a line delimiter, <code>false</code> otherwise.
	 */
	private boolean endsWithDelimiter(String s) {
		return s != null && s.length() > 0 && isDelimiter(s.charAt(s.length() - 1));
	}

	/**
	 * @return <code>true</code> if <code>s</code> is <code>null</code> or has zero length, <code>false</code> otherwise.
	 */
	private boolean isEmpty(String s) {
		return s == null || s.length() == 0;
	}
}
