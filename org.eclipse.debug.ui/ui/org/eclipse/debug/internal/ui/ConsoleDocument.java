package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.debug.core.*;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.jface.text.*;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class ConsoleDocument extends AbstractDocument implements IDebugEventListener {

	private boolean fClosed= false;
	
	protected boolean fCanWrite= true;

	protected IProcess fProcess;
	private IStreamsProxy fProxy;
	private int fLastStreamWriteEnd= 0;
	private int fNewStreamWriteEnd= 0;
	protected boolean fNeedsToStartReading= true;
	
	public static final int OUT= 0;
	public static final int ERR= 1;
	
	protected List fStyleRanges= new ArrayList(2);

	protected ConsoleViewer fConsoleViewer= null;
	
	protected IStreamListener fSystemOutListener= new IStreamListener() {
				public void streamAppended(String newText, IStreamMonitor monitor) {
					systemOutAppended(newText);
				}
			};
			
	protected IStreamListener fSystemErrListener= new IStreamListener() {
				public void streamAppended(String newText, IStreamMonitor monitor) {
					systemErrAppended(newText);
				}
			};

	public ConsoleDocument(IProcess process) {
		super();
		fProcess= process;
		setTextStore(new ConsoleOutputTextStore(2500));
		setLineTracker(new DefaultLineTracker());
		
		if (process != null) {
			fProxy= process.getStreamsProxy();			
		}
		if (process != null) {
			DebugPlugin.getDefault().addDebugEventListener(this);
		}
		completeInitialization();
	}

	public void close() {
		stopReading();
		DebugPlugin.getDefault().removeDebugEventListener(this);
		fClosed= true;
		fStyleRanges= Collections.EMPTY_LIST;
		set("");
	}

	/**
	 * Fires the <code>DocumentEvent</code>, but also
	 * writes to the proxy if the user is entering input and
	 * has hit "Enter".
	 */
	protected void fireDocumentChanged(DocumentEvent event) {
		super.fireDocumentChanged(event);
		
		String eventText= event.getText();
		if (eventText == null || 0 >= eventText.length() || eventText.length() > 2) {
			return;
		}
		String[] lineDelimiters= event.getDocument().getLegalLineDelimiters();
		for (int i= 0; i < lineDelimiters.length; i++) {
			if (lineDelimiters[i].equals(eventText)) {
				String inText= event.getDocument().get();
				inText= inText.substring(fNewStreamWriteEnd, inText.length());
				if (inText.length() == 0) {
					return;
				}					
				if (fCanWrite) {
					final String input= inText;
					Runnable runnable= new Runnable() {
						public void run() {
							try {
								fCanWrite= false;
								fProxy.write(input);
								fCanWrite= true;
							} catch (IOException ioe) {
								DebugUIUtils.logError(ioe);
							}
						}
					};
					Thread proxyWriter= new Thread(runnable);
					proxyWriter.start();		
					fLastStreamWriteEnd= getLength();
				} 
			}
		}
	}

	public boolean isClosed() {
		return fClosed;
	}
	
	public void replace(int pos, int replaceLength, String text) {
		if (isReadOnly() || pos < getStartOfEditableContent()) {
			return;
		}

		replace0(pos, replaceLength, text);
		int docLength= getLength();
		if (docLength == fNewStreamWriteEnd) {
			//removed all of the user input text
			fStyleRanges.remove(fStyleRanges.size() - 1);
		} else {
			updateInputStyleRange(docLength);
			//notify the viewer that the style ranges have changed.
			fireDocumentChanged(new DocumentEvent(this, 0, 0, ""));
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
			DebugUIUtils.logError(ble);
		}
		
	}

	public void set(String text) {
		fNewStreamWriteEnd= text.length();
		super.set(text);
		fLastStreamWriteEnd= fNewStreamWriteEnd;
	}

	protected void startReading() {
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
	}

	protected void stopReading() {
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
	 */
	protected void streamAppended(final String text, final int source) {
		update(new Runnable() {
			public void run() {
				int appendedLength= text.length();
				fNewStreamWriteEnd= fLastStreamWriteEnd + appendedLength;
				ConsoleDocument.this.replace0(fLastStreamWriteEnd, 0, text);
				updateOutputStyleRanges(source);
				fLastStreamWriteEnd= fNewStreamWriteEnd;
			}
		});
	}
		
	/**
	 * @see IInputStreamListener
	 */
	protected void systemErrAppended(String text) {
		streamAppended(text, ERR);
	}

	/**
	 * @see IInputStreamListener
	 */
	protected void systemOutAppended(String text) {
		streamAppended(text, OUT);
	}

	public boolean equals(Object obj) {
			boolean correctInstance= obj instanceof ConsoleDocument;
			if (fProcess != null) {
				return correctInstance && fProcess.equals(((ConsoleDocument)obj).fProcess);
			} else {
				return correctInstance && ((ConsoleDocument)obj).fProcess == null;
			}
    }
    
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
	protected void updateInputStyleRange(int docLength) {
		if (fClosed) {
			return;
		}
		if (docLength != fNewStreamWriteEnd) {
			StyleRange input= 
				new StyleRange(fNewStreamWriteEnd, docLength - fNewStreamWriteEnd, 
								ConsolePreferencePage.getPreferenceColor(ConsolePreferencePage.CONSOLE_SYS_IN_RGB),
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

	protected void updateOutputStyleRanges(int sourceStream) {
		if (fClosed) {
			return;
		}
		int docLength= getLength();
		if (docLength == 0) {
			return;
		}
		
		if ((fNewStreamWriteEnd == 0) && (0 == fLastStreamWriteEnd)) {
			return;
		}
		
		if (fNewStreamWriteEnd == fLastStreamWriteEnd) {
			return;
		}

		Color newRangeColor= 
			(sourceStream == ConsoleDocument.OUT) ? ConsolePreferencePage.getPreferenceColor(ConsolePreferencePage.CONSOLE_SYS_OUT_RGB) : ConsolePreferencePage.getPreferenceColor(ConsolePreferencePage.CONSOLE_SYS_ERR_RGB);

		StyleRange newRange= new StyleRange(fLastStreamWriteEnd, fNewStreamWriteEnd - fLastStreamWriteEnd, newRangeColor, null);
		if (!fStyleRanges.isEmpty()) {
			if ((docLength != fNewStreamWriteEnd) && 
				((StyleRange)fStyleRanges.get(fStyleRanges.size() - 1)).foreground ==
				ConsolePreferencePage.getPreferenceColor(ConsolePreferencePage.CONSOLE_SYS_IN_RGB)) {
				//remove the top "input" range..it will get recalculated in updateInputStyleRanges
				fStyleRanges.remove(fStyleRanges.size() - 1);
			}
		}
		
		addNewStyleRange(newRange);
		coaleseRanges();
		updateInputStyleRange(docLength);
		//notify the viewer that the style ranges have changed.
		fireDocumentChanged(new DocumentEvent(this, 0, 0, null));
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
		fStyleRanges= new ArrayList(2);
		set("");
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
	 * @see IDebugEventListener
	 */
	public void handleDebugEvent(DebugEvent event) {
		if (fProcess == null) {
			return;
		}
		if (event.getKind() == DebugEvent.TERMINATE) {
			Object element= event.getSource();
			if (element != null && element.equals(fProcess)) {
				update( new Runnable() {
					public void run() {
						fireDocumentChanged(new DocumentEvent(ConsoleDocument.this, 0, 0, null));
					}
				});
			}					
		}
	}
	
	/**
	 * Posts the update code "behind" the running operation if the 
	 * UI will be updated.
	 */
	protected void update(Runnable runnable) {
		if (fConsoleViewer != null) {
			fConsoleViewer.getControl().getDisplay().asyncExec(runnable);
		} else {
			Display display= DebugUIPlugin.getDefault().getDisplay();
			if (display != null) {
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
}

