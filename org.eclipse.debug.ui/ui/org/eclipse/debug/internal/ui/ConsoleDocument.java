package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.*;
import org.eclipse.debug.core.model.*;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.*;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class ConsoleDocument extends AbstractDocument implements IDebugEventListener, IPropertyChangeListener {

	private boolean fClosed= false;

	protected IProcess fProcess;
	private IStreamsProxy fProxy;
	private int fLastStreamWriteEnd= 0;
	private int fLastWritePosition= 0;
	private int fNewStreamWriteEnd= 0;
	protected boolean fNeedsToStartReading= true;
	
	private int fLastSourceStream= ConsoleDocument.OUT;
	private int fMaxSysText= 0; // Maximum amount of text displayed from sys.out and sys.err
	private boolean fSetMaxSysText= false;
	private boolean fCropping= false;
	
	private List fStyleRanges= new ArrayList(2);
	
	public static final int OUT= 0;
	public static final int ERR= 1;
	public static final int IN= 2;

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
		
		IPreferenceStore store= DebugUIPlugin.getDefault().getPreferenceStore();
		store.addPropertyChangeListener(this);
		fMaxSysText= store.getInt(ConsolePreferencePage.CONSOLE_MAX_OUTPUT_SIZE);
		fSetMaxSysText= store.getBoolean(ConsolePreferencePage.CONSOLE_SET_MAX_OUTPUT);	
		
		setTextStore(new ConsoleOutputTextStore(2500));
		setLineTracker(new DefaultLineTracker());
		
		if (process != null) {
			fProxy= process.getStreamsProxy();
			DebugPlugin.getDefault().addDebugEventListener(this);			
		}		
		completeInitialization();
	}

	public void close() {
		stopReading();
		DebugPlugin.getDefault().removeDebugEventListener(this);
		IPreferenceStore store= DebugUIPlugin.getDefault().getPreferenceStore();
		store.removePropertyChangeListener(this);		
		fClosed= true;
		fStyleRanges= new ArrayList(0);
		set("");
	}

	/**
	 * Fires the <code>DocumentEvent</code>, but also
	 * writes to the proxy if the user is entering input and
	 * has hit "Enter".
	 */
	protected void fireDocumentChanged(DocumentEvent event) {
		super.fireDocumentChanged(event);
		if (fCropping) {
			// Our user input detection could get a false positive from
			// cropping if the crop results in the event text being
			// trimmed to a single line delimeter
			return;
		}
		String eventText= event.getText();
		if (eventText == null || 0 >= eventText.length() || eventText.length() > 2) {
			return;
		}
		String[] lineDelimiters= event.getDocument().getLegalLineDelimiters();
		for (int i= 0; i < lineDelimiters.length; i++) {
			if (lineDelimiters[i].equals(eventText)) {
				// Input was a single line delimeter - user hit enter
				try {
					String inText= event.getDocument().get();
					fLastWritePosition = fLastStreamWriteEnd;
					inText= inText.substring(fLastWritePosition, inText.length());
					if (inText.length() == 0) {
						return;
					}
					fProxy.write(inText);
					fLastStreamWriteEnd= getLength();
					return;
				} catch (IOException ioe) {
					DebugUIUtils.logError(ioe);
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
		replace0(pos, replaceLength, text, IN);
	}
	
	/**
	 * Replace text used to add content from streams even though
	 * the process is terminated (and therefore the doc is "read only")
	 */
	protected void replace0(int pos, int replaceLength, String text, int sourceStream) {
		try {		
			super.replace(pos, replaceLength, text);
			colorText(pos, text.length(), sourceStream);
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
				int totalSize= getStore().getLength() + appendedLength;
				fNewStreamWriteEnd= fLastStreamWriteEnd + appendedLength;
				if (fSetMaxSysText && totalSize > fMaxSysText) {
					crop(totalSize - fMaxSysText);
				}
				ConsoleDocument.this.replace0(fLastStreamWriteEnd, 0, text, source);
				fLastStreamWriteEnd= fNewStreamWriteEnd;
			}
		});
	}
	
	/**
	 * Delete <code>amountToLose</code> characters from the text store.
	 * Text is removed from the beginning of the document.
	 * If amountToLose <= 0, do nothing.
	 */
	private void crop(int amountToLose) {
		if (amountToLose <= 0) {
			return;
		}
		fCropping= true;
		ConsoleDocument.this.replace0(0, amountToLose, "", OUT); // empty text. color shouldn't matter.
		fCropping= false;
		fNewStreamWriteEnd= fNewStreamWriteEnd - amountToLose;
		fLastStreamWriteEnd= fLastStreamWriteEnd - amountToLose;
	}
	
	/**
	 * Sets the style ranges to the given ranges
	 */
	protected void setStyleRanges(StyleRange[] ranges) {
		int length= ranges.length;
		List tempRanges= new ArrayList(length);
		for (int i=0; i < length; i++) {
			tempRanges.add(ranges[i]);
		}
		fStyleRanges= tempRanges;
	}

	/**
	 * Returns the cached style ranges that this document stores
	 * when it loses its viewer.
	 */
	protected StyleRange[] getStyleRanges() {
		StyleRange[] ranges= new StyleRange[fStyleRanges.size()];
		for (int i=0; i < ranges.length; i++) {
			ranges[i]= (StyleRange) fStyleRanges.get(i);
		}
		return ranges;
	}
	
	/**
	 * Crop the contents of the store to fit the specified maximum
	 * size (fMaxSysText).
	 * Do nothing if the document has no characters.
	 */
	private void cropContentToFit() {
		int totalSize= getStore().getLength();
		int amountToLose= totalSize-fMaxSysText;
		if (!fSetMaxSysText || totalSize <= 0 || amountToLose <= 0) {
			return;
		}
		crop(amountToLose);
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

	/**
	 * Returns whether the document's underlying process is
	 * terminated.
	 */
	protected boolean isReadOnly() {
		return (fProcess != null) ? fProcess.isTerminated() : true;
	}
	
	protected void colorText(int start, int length, int sourceStream) {
		Color newRangeColor;
		switch (sourceStream) {
			case OUT:
				newRangeColor= ConsolePreferencePage.getPreferenceColor(ConsolePreferencePage.CONSOLE_SYS_OUT_RGB);
				break;
			case ERR:
				newRangeColor= ConsolePreferencePage.getPreferenceColor(ConsolePreferencePage.CONSOLE_SYS_ERR_RGB);
				break;
			case IN:
				newRangeColor= ConsolePreferencePage.getPreferenceColor(ConsolePreferencePage.CONSOLE_SYS_IN_RGB);
				break;
			default:
				newRangeColor= ConsolePreferencePage.getPreferenceColor(ConsolePreferencePage.CONSOLE_SYS_OUT_RGB);
		}
		StyleRange newRange= new StyleRange(start, length, newRangeColor, null);
		if (fConsoleViewer != null) {
			// Add the style range directly to the widget if we have a viewer
			fConsoleViewer.getTextWidget().setStyleRange(newRange);
		} else {
			// If we don't have a viewer, add the range to the backup ranges. The backup
			// ranges will be applied to the viewer when we get one.
			fStyleRanges.add(newRange);
		}
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
		if (fConsoleViewer != null && !fConsoleViewer.getControl().isDisposed()) {
			fConsoleViewer.getControl().getDisplay().asyncExec(runnable);
		} else {
			Display display= DebugUIPlugin.getDefault().getDisplay();
			if (display != null && !display.isDisposed()) {
				display.asyncExec(runnable);
			}
		}
	}

	/**
	 * Sets the console viewer that this document is viewed within.
	 * Can be set to <code>null</code> if no longer currently being
	 * viewed.
	 * When the viewer is set to null, the document stores a copy of
	 * the viewers current style ranges.
	 * When the viewer is set non-null, the document clears its
	 * backup of the style ranges. Clients who want to access
	 * the backup style ranges should do so before setting the
	 * document's viewer.
	 */
	protected void setConsoleViewer(ConsoleViewer viewer) {
		if (viewer == null) {
			// Removing this document from the viewer.
			// Backup the style ranges so we have them if this
			// document is added to the viewer again
			if (fConsoleViewer != null) {
				setStyleRanges(fConsoleViewer.getTextWidget().getStyleRanges());
			}
		} else {
			// Adding this document to a viewer.
			// Clear the style ranges backup.
			fStyleRanges= new ArrayList(2);
		}
		fConsoleViewer = viewer;
	}
	/**
	 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty() != ConsolePreferencePage.CONSOLE_MAX_OUTPUT_SIZE || fProcess == null) {
			return;
		}

		IPreferenceStore store= DebugUIPlugin.getDefault().getPreferenceStore();
		fSetMaxSysText= store.getBoolean(ConsolePreferencePage.CONSOLE_SET_MAX_OUTPUT);
		int newMax= store.getInt(ConsolePreferencePage.CONSOLE_MAX_OUTPUT_SIZE);
		if (newMax != fMaxSysText) {
			fMaxSysText= newMax;
			if (fSetMaxSysText) {
				cropContentToFit();
			}
		}	
	}

}

