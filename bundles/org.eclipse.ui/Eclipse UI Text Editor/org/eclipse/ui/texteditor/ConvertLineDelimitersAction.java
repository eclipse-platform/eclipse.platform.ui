package org.eclipse.ui.texteditor;

import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;

import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IRegion;

/**
 * An action to convert line delimiters of a text editor document to a particular line delimiter.
 */
public class ConvertLineDelimitersAction extends TextEditorAction implements IDocumentListener {

	/** The target line delimiter. */
	private final String fLineDelimiter;
	
	private boolean fInitialized;

	/**
	 * Creates a line delimiter conversion action.
	 * 
	 * @param editor the editor
	 * @param lineDelimiter the target line delimiter to convert the editor's document to
	 */
	public ConvertLineDelimitersAction(ITextEditor editor, String lineDelimiter) {
		this(EditorMessages.getResourceBundle(), "dummy", editor, lineDelimiter); //$NON-NLS-1$
	}

	/**
	 * Creates a line delimiter conversion action.
	 * 
	 * @param editor the editor
	 * @param lineDelimiter the target line delimiter to convert the editor's document to
	 */
	public ConvertLineDelimitersAction(ResourceBundle bundle, String prefix, ITextEditor editor, String lineDelimiter) {
		super(bundle, prefix, editor);
		fLineDelimiter= lineDelimiter;
		
		String platformLineDelimiter= System.getProperty("line.separator"); //$NON-NLS-1$
		setText(EditorMessages.getString(getLabelKey(fLineDelimiter, platformLineDelimiter)));

		update();
	}
		
	/*
	 * @see Action#run()
	 */
	public void run() {

		try {

			IDocument document= getDocument();
			if (document != null) {
				Shell shell= getTextEditor().getSite().getShell();
				ConvertRunnable runnable= new ConvertRunnable(document, fLineDelimiter);

				if (document.getNumberOfLines() < 40) {
					BusyIndicator.showWhile(shell.getDisplay(), runnable);
					
				} else {				
					ProgressMonitorDialog dialog= new ProgressMonitorDialog(shell);
					dialog.run(false, true, runnable);
				}
			}

		} catch (InterruptedException e) {
			// action cancelled				

		} catch (InvocationTargetException e) {
			// should not happen
		}
	}

	/**
	 * Converts all line delimiters of the document to <code>lineDelimiter</code>.
	 */
	private static class ConvertRunnable implements IRunnableWithProgress, Runnable {
		
		private final IDocument fDocument;
		private final String fLineDelimiter;
		
		public ConvertRunnable(IDocument document, String lineDelimiter) {
			fDocument= document;
			fLineDelimiter= lineDelimiter;	
		}
		
		private static class DummyMonitor implements IProgressMonitor {		
			public void beginTask(String name, int totalWork) {}
			public void done() {}
			public void internalWorked(double work) {}
			public boolean isCanceled() {return false;}
			public void setCanceled(boolean value) {}
			public void setTaskName(String name) {}
			public void subTask(String name) {}
			public void worked(int work) {}
		}
		
		/*
		 * @see IRunnableWithProgress#run(IProgressMonitor)
		 */
		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

			final int lineCount= fDocument.getNumberOfLines();
			monitor.beginTask(EditorMessages.getString("Editor.ConvertLineDelimiter.title"), lineCount); //$NON-NLS-1$
			
			try {
				for (int i= 0; i < lineCount; i++) {
					if (monitor.isCanceled())
						throw new InterruptedException();
					
					final String delimiter= fDocument.getLineDelimiter(i);
					if (delimiter != null && delimiter.length() > 0 && !delimiter.equals(fLineDelimiter)) {
						IRegion region= fDocument.getLineInformation(i);
						fDocument.replace(region.getOffset() + region.getLength(), delimiter.length(), fLineDelimiter);
					}

					monitor.worked(1);
				}

			} catch (BadLocationException e) {
				throw new InvocationTargetException(e);

			} finally {
				monitor.done();
			}
		}
		
		/*
		 * @see Runnable#run()
		 */
		public void run() {
			try {
				run(new DummyMonitor());

			} catch (InterruptedException e) {
				// cancelled, can't happen with dummy monitor
				
			} catch (InvocationTargetException e) {
				// should not happen				
			}
		}
	}

	private IDocument getDocument() {

		ITextEditor editor= getTextEditor();
		if (editor == null)
			return null;

		IDocumentProvider documentProvider= editor.getDocumentProvider();
		return documentProvider.getDocument(editor.getEditorInput());				
	}

	private static boolean usesLineDelimiterExclusively(IDocument document, String lineDelimiter) throws BadLocationException {

		try {
			final int lineCount= document.getNumberOfLines();
			for (int i= 0; i < lineCount; i++) {
				final String delimiter= document.getLineDelimiter(i);
				if (delimiter != null && delimiter.length() > 0 && !delimiter.equals(lineDelimiter))
					return false;
			}

		} catch (BadLocationException e) {
			throw e;
		}
		
		return true;
	}

	private static String getLabelKey(String lineDelimiter, String platformLineDelimiter) {
		if (lineDelimiter.equals(platformLineDelimiter)) {

			if (lineDelimiter.equals("\r\n")) //$NON-NLS-1$
				return "Editor.ConvertLineDelimiter.toWindows.default.label"; //$NON-NLS-1$
			
			if (lineDelimiter.equals("\n")) //$NON-NLS-1$
				return "Editor.ConvertLineDelimiter.toUNIX.default.label"; //$NON-NLS-1$

			if (lineDelimiter.equals("\r")) //$NON-NLS-1$
				return "Editor.ConvertLineDelimiter.toMac.default.label"; //$NON-NLS-1$
			
		} else {

			if (lineDelimiter.equals("\r\n")) //$NON-NLS-1$
				return "Editor.ConvertLineDelimiter.toWindows.label"; //$NON-NLS-1$
			
			if (lineDelimiter.equals("\n")) //$NON-NLS-1$
				return "Editor.ConvertLineDelimiter.toUNIX.label"; //$NON-NLS-1$

			if (lineDelimiter.equals("\r")) //$NON-NLS-1$
				return "Editor.ConvertLineDelimiter.toMac.label"; //$NON-NLS-1$
		}
		
		return null;
	}
	
	/*
	 * @see IUpdate#update()
	 */
	public void update() {
		super.update();

		try {
			IDocument document= getDocument();
			setEnabled(isEnabled() && document != null && !usesLineDelimiterExclusively(document, fLineDelimiter));
			
		} catch (BadLocationException e) {
		}
	}	

	/*
	 * @see TextEditorAction#setEditor(ITextEditor)
	 */
	public void setEditor(ITextEditor editor) {
		IDocument document= getDocument();
		if (document != null)
			document.removeDocumentListener(this);
		
		super.setEditor(editor);

		document= getDocument();
		if (document != null)
			document.addDocumentListener(this);
	}

	/*
	 * @see IDocumentListener#documentAboutToBeChanged(DocumentEvent)
	 */
	public void documentAboutToBeChanged(DocumentEvent event) {
	}

	/*
	 * @see IDocumentListener#documentChanged(DocumentEvent)
	 */
	public void documentChanged(DocumentEvent event) {

		IDocument document= getDocument();

		// detach from document if no longer connected to editor
		if (!event.getDocument().equals(document)) {
			event.getDocument().removeDocumentListener(this);
			return;
		}

		update();		
	}

}
