package org.eclipse.debug.internal.ui.views;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.internal.ui.ConsoleOutputTextStore;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugPreferenceConstants;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.ITextStore;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class ConsoleDocument extends AbstractDocument implements IDebugEventSetListener {

	private boolean fClosed= false;

	protected IProcess fProcess;
	private IStreamsProxy fProxy;
	private int fLastStreamWriteEnd= 0;
	private int fLastWritePosition= 0;
	private int fNewStreamWriteEnd= 0;
	protected boolean fNeedsToStartReading= true;
	
	class StreamEntry {
		/**
		 * Whether written to std out or std err - one of OUT/ERR
		 */
		private int fKind = -1;
		/**
		 * The text written
		 */
		private String fText = null;
		
		StreamEntry(String text, int kind) {
			fText = text;
			fKind = kind;
		}
		
		/**
		 * Returns the kind of entry - OUT or ERR
		 */
		public int getKind() {
			return fKind;
		}
		
		/**
		 * Returns the text written
		 */
		public String getText() {
			return fText;
		}
	}
	
	/**
	 * A queue of stream entries written to standard out and standard err.
	 * Entries appended to the end of the queue and removed from the front.
	 * Intentionally a vector to obtain synchronization as entries are
	 * added and removed.
	 */
	private Vector fQueue = new Vector(10);
	
	/**
	 * Thread that polls the queue for new output
	 */
	private Thread fPollingThread = null;
	
	/**
	 * Whether an append is still in  progress or to be run
	 */
	private boolean fAppending = false;
	
	/**
	 * Whether associated process has terminated
	 */
	private boolean fTerminated = false;
	
	/**
	 * Whether to keep polling
	 */
	private boolean fPoll = false;
	
	/**
	 * The base number of milliseconds to pause
	 * between polls.
	 */
	private static final long BASE_DELAY= 50L;
		
	public static final int OUT= 0;
	public static final int ERR= 1;
	
	protected List fStyleRanges= new ArrayList(2);

	protected ConsoleViewer fConsoleViewer= null;
	
	protected IStreamListener fSystemOutListener= new IStreamListener() {
				public void streamAppended(String newText, IStreamMonitor monitor) {
					DebugUIPlugin.getDefault().aboutToWriteSystemOut();
					systemOutAppended(newText);
				}
			};
			
	protected IStreamListener fSystemErrListener= new IStreamListener() {
				public void streamAppended(String newText, IStreamMonitor monitor) {
					DebugUIPlugin.getDefault().aboutToWriteSystemErr();
					systemErrAppended(newText);
				}
			};

	public ConsoleDocument(IProcess process) {
		super();
		fProcess= process;
		if (process != null) {
			fProxy= process.getStreamsProxy();			
			fTerminated = process.isTerminated();
			DebugPlugin.getDefault().addDebugEventListener(this);
			setTextStore(new ConsoleOutputTextStore(2500));
		} else {
			fClosed= true;
			fTerminated = true;
			setTextStore(new ConsoleOutputTextStore(0));	
		}
		setLineTracker(new DefaultLineTracker());
		completeInitialization();
	}

	public void close() {
		fClosed= true;
		stopReading();
		DebugPlugin.getDefault().removeDebugEventListener(this);
		fStyleRanges= Collections.EMPTY_LIST;
		set(""); //$NON-NLS-1$
	}

	public boolean isClosed() {
		return fClosed;
	}
	
	/**
	 * The user has typed into the console.
	 * 
	 * @see IDocument#replace(int, int, String)
	 */
	public void replace(int pos, int replaceLength, String text) {
		if (isReadOnly() || pos < getStartOfEditableContent()) {
			return;
		}

		replace0(pos, replaceLength, text);
		
		if (!isClosed()) {
			// echo the data to the std in of the associated process
			String[] lineDelimiters= getLegalLineDelimiters();
			for (int i= 0; i < lineDelimiters.length; i++) {
				if (lineDelimiters[i].equals(text)) {
					try {
						String inText= get();
						fLastWritePosition = fLastStreamWriteEnd;
						inText= inText.substring(fLastWritePosition, inText.length());
						if (inText.length() == 0) {
							break;
						}
						fProxy.write(inText);
						fLastStreamWriteEnd= getLength();
						break;
					} catch (IOException ioe) {
						DebugUIPlugin.log(ioe);
					}
				}
			}
		}
				
		int docLength= getLength();
		if (docLength == fNewStreamWriteEnd) {
			//removed all of the user input text
			fStyleRanges.remove(fStyleRanges.size() - 1);
		} else {
			updateInputStyleRange(docLength, fNewStreamWriteEnd);
			//notify the viewer that the style ranges have changed.
			fireDocumentChanged(new DocumentEvent(this, 0, 0, "")); //$NON-NLS-1$
		}
	}
	
	/**
	 * Replace text used to add content from streams even though
	 * the process is terminated (and therefore the doc is "read only")
	 */
	protected void replace0(int pos, int replaceLength, String text) {
		try {		
			super.replace(pos, replaceLength, text);
		} catch (BadLocationException ble) {
			DebugUIPlugin.log(ble);
		}
	}

	
	/**
	 * @see IDocument#set(String)
	 */
	public void set(String text) {
		fNewStreamWriteEnd= text.length();
		super.set(text);
		fLastStreamWriteEnd= fNewStreamWriteEnd;
	}

	public void startReading() {
		if (fProxy == null) {
			return;
		}
		
		if (!fNeedsToStartReading) {
			return;
		}
		fNeedsToStartReading= false;
		IStreamMonitor monitor= fProxy.getOutputStreamMonitor();
		if (monitor != null) {
			monitor.addListener(fSystemOutListener);
			String contents= monitor.getContents();
			if (contents.length() > 0) {
				systemOutAppended(contents);
			}
		}
		monitor= fProxy.getErrorStreamMonitor();
		if (monitor != null) {
			monitor.addListener(fSystemErrListener);
			String contents= monitor.getContents();
			if (contents.length() > 0) {
				systemErrAppended(contents);
			}
		}
		Runnable r = new Runnable() {
			public void run() {
				pollAndSleep();
			}
		};
		fPoll = true;
		fPollingThread = new Thread(r, "Console Polling Thread"); //$NON-NLS-1$
		fPollingThread.start();
	}
	
	/**
	 * Polls and sleeps until closed or the associated
	 * process terminates
	 */
	protected void pollAndSleep() {
		while (fPoll && (!fTerminated || !fQueue.isEmpty())) {
			poll();
			try {
				Thread.sleep(BASE_DELAY);
			} catch (InterruptedException e) {
			}
		}
	}
	
	/**
	 * Polls the queue for new output and updates this document
	 */
	protected void poll() {
		if (isAppendInProgress()) {
			return;
		}
		synchronized(fQueue) {
			StringBuffer buffer = null;
			StreamEntry prev = null;
			int processed = 0;
			int amount = 0;
			while (processed < fQueue.size() && amount < 8096) {
				StreamEntry entry = (StreamEntry)fQueue.get(processed);
				if (prev == null) {
					buffer = new StringBuffer(entry.getText());
				} else {
					if (prev.getKind() == entry.getKind()) {
						buffer.append(entry.getText());
					} else {
						// only do one append per poll
						break;
					}
				}
				prev = entry;
				processed++;
				amount+= entry.getText().length();
			}
			if (buffer != null) {
				appendToDocument(buffer.toString(), prev.getKind());
			}
			for (int i = 0; i < processed; i++) {
				fQueue.remove(0);
			}
		}
	}

	protected void stopReading() {
		fPoll = false;
		if (fProxy == null) {
			return;
		}
		fNeedsToStartReading= true;
		IStreamMonitor monitor= fProxy.getOutputStreamMonitor();
		monitor.removeListener(fSystemOutListener);
		monitor= fProxy.getErrorStreamMonitor();
		monitor.removeListener(fSystemErrListener);
	}

	/**
	 * System out or System error has had text append to it.
	 * Adds the new text to the document.
	 * 
	 * @see IStreamListener#streamAppended(String, IStreamMonitor)
	 */
	protected void appendToDocument(final String text, final int source) {
		setAppendInProgress(true);
		update(new Runnable() {
			public void run() {
				int appendedLength= text.length();
				fNewStreamWriteEnd= fLastStreamWriteEnd + appendedLength;
				updateOutputStyleRanges(source, getLength() + appendedLength, fLastStreamWriteEnd, fNewStreamWriteEnd);
				replace0(fLastStreamWriteEnd, 0, text);
				fLastStreamWriteEnd= fNewStreamWriteEnd;
				setAppendInProgress(false);
			}
		});
	}
	
	/**
	 * System out or System error has had text append to it.
	 * Adds a new entry to the queue.
	 */
	protected void streamAppended(String text, int source) {
		fQueue.add(new StreamEntry(text, source));
	}
			
	/**
	 * @see IStreamListener#streamAppended(String, IStreamMonitor)
	 */
	protected void systemErrAppended(String text) {
		streamAppended(text, ERR);
	}

	/**
	 * @see IStreamListener#streamAppended(String, IStreamMonitor)
	 */
	protected void systemOutAppended(String text) {
		streamAppended(text, OUT);
	}

	
	/**
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object obj) {
		boolean correctInstance= obj instanceof ConsoleDocument;
		if (fProcess != null) {
			return correctInstance && fProcess.equals(((ConsoleDocument)obj).fProcess);
		} else {
			return correctInstance && ((ConsoleDocument)obj).fProcess == null;
		}
    }
    
	/**
	 * @see Object#hashCode()
	 */
    public int hashCode() {
    	return (fProcess != null) ? fProcess.hashCode() : super.hashCode();
    }
    
	protected StyleRange[] getStyleRanges() {
		if (fStyleRanges.isEmpty()) {
			return new StyleRange[]{};
		} 
		StyleRange[] sRanges= new StyleRange[fStyleRanges.size()];
		return (StyleRange[])fStyleRanges.toArray(sRanges);
	}
	
	/**
	 * Coalese that last two style ranges if they are similar
	 */
	protected void coaleseRanges() {
		int size= fStyleRanges.size();
		if (size > 1) {
			StyleRange last= (StyleRange) fStyleRanges.get(size - 1);
			StyleRange nextToLast= (StyleRange) fStyleRanges.get(size - 2);
			if (last.similarTo(nextToLast)) {//same color?
				StyleRange newRange= new StyleRange(nextToLast.start, last.length + nextToLast.length, last.foreground, null);
				fStyleRanges.remove(size - 1);
				fStyleRanges.remove(size - 2);
				addNewStyleRange(newRange);
			}
		}
	}

	/**
	 * Returns whether the document's underlying process is
	 * terminated.
	 */
	protected boolean isReadOnly() {
		return (fProcess != null) ? fProcess.isTerminated() : true;
	}
	
	/**
	 * Updates the current input style range.
	 */
	protected void updateInputStyleRange(int docLength, int newWriteEnd) {
		if (docLength != newWriteEnd) {
			StyleRange input= 
				new StyleRange(newWriteEnd, docLength - newWriteEnd, 
						DebugUIPlugin.getPreferenceColor(IDebugPreferenceConstants.CONSOLE_SYS_IN_RGB),
						null);
			if (!fStyleRanges.isEmpty()) {
				if (((StyleRange)fStyleRanges.get(fStyleRanges.size() - 1)).similarTo(input)) {
					//remove the top "input" range...continuing input
					fStyleRanges.remove(fStyleRanges.size() - 1);
				}
			} 
			
			addNewStyleRange(input);
		}
	}

	protected void updateOutputStyleRanges(int sourceStream, int docLength, int prevWriteEnd, int newWriteEnd) {
		if (docLength == 0) {
			return;
		}
		
		if ((newWriteEnd == 0) && (0 == prevWriteEnd)) {
			return;
		}
		
		if (newWriteEnd == prevWriteEnd) {
			return;
		}

		Color newRangeColor= 
			(sourceStream == ConsoleDocument.OUT) ? DebugUIPlugin.getPreferenceColor(IDebugPreferenceConstants.CONSOLE_SYS_OUT_RGB) : DebugUIPlugin.getPreferenceColor(IDebugPreferenceConstants.CONSOLE_SYS_ERR_RGB);

		StyleRange newRange= new StyleRange(prevWriteEnd, newWriteEnd - prevWriteEnd, newRangeColor, null);
		if (!fStyleRanges.isEmpty()) {
			if ((docLength != newWriteEnd) && 
				((StyleRange)fStyleRanges.get(fStyleRanges.size() - 1)).foreground ==
				DebugUIPlugin.getPreferenceColor(IDebugPreferenceConstants.CONSOLE_SYS_IN_RGB)) {
				//remove the top "input" range..it will get recalculated in updateInputStyleRanges
				fStyleRanges.remove(fStyleRanges.size() - 1);
			}
		}
		
		addNewStyleRange(newRange);
		coaleseRanges();
		updateInputStyleRange(docLength, newWriteEnd);
	}	
	
	/**
	 * Adds a new style range if the document is not closed.
	 * Note that the document can be closed by a separate thread.
	 * This is the reason for the copy of the style ranges.
	 */
	protected void addNewStyleRange(StyleRange newRange) {
		List tempRanges= fStyleRanges;
		if (fClosed) {
			return;
		}
		tempRanges.add(newRange);
	}
	
	protected void setStyleRanges(List ranges) {
		fStyleRanges= ranges;
	}

	protected void clearDocument() {
		fQueue.clear();
		fStyleRanges= new ArrayList(2);
		set(""); //$NON-NLS-1$
	}
	
	/**
	 * Returns the position after which editing of the
	 * content is allowable.
	 */
	protected int getStartOfEditableContent() {
		return fLastStreamWriteEnd;
	}
	
	/**
	 * Make visible to the ConsoleViewer
	 */
	protected ITextStore getStore() {
		return super.getStore();
	}
	
	/**
	 * @see IDebugEventSetListener#handleDebugEvents(DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		if (fProcess == null) {
			return;
		}
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			if (event.getKind() == DebugEvent.TERMINATE) {
				Object element= event.getSource();
				if (element != null && element.equals(fProcess)) {
					fTerminated = true;
					update( new Runnable() {
						public void run() {
							fireDocumentChanged(new DocumentEvent(ConsoleDocument.this, 0, 0, null));
						}
					});
				}					
			}
		}
	}
	
	/**
	 * Posts the update code "behind" the running operation if the 
	 * UI will be updated.
	 */
	protected void update(Runnable runnable) {
		if (fConsoleViewer != null && fConsoleViewer.getControl() != null && !fConsoleViewer.getControl().isDisposed()) {
			fConsoleViewer.getControl().getDisplay().asyncExec(runnable);
		} else {
			Display display= DebugUIPlugin.getDefault().getStandardDisplay();
			if (display != null && !display.isDisposed()) {
				display.asyncExec(runnable);
			}
		}
	}

	/**
	 * Sets the console viewer that this document is viewed within.
	 * Can be set to <code>null</code> if no longer currently being
	 * viewed.
	 */
	protected void setConsoleViewer(ConsoleViewer viewer) {
		fConsoleViewer = viewer;
	}
	
	/**
	 * Sets whether a runnable has been submitted to update the console
	 * document.
	 */
	protected void setAppendInProgress(boolean appending) {
		fAppending = appending;
	}

	/**
	 * Sets whether a runnable has been submitted to update the console
	 * document.
	 */
	protected boolean isAppendInProgress() {
		return fAppending;
	}
}