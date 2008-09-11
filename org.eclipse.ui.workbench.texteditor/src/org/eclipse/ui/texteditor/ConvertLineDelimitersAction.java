/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.texteditor;


import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.IRewriteTarget;
import org.eclipse.jface.text.TextUtilities;


/**
 * An action to convert line delimiters of a text editor document to a
 * particular line delimiter.
 *
 * @since 2.0
 * @deprecated since 3.1. Line delimiter conversion has been modified to work on groups of files rather than being editor specific
 */
public class ConvertLineDelimitersAction extends TextEditorAction {


	/** The target line delimiter. */
	private final String fLineDelimiter;

	/**
	 * Creates a line delimiter conversion action.
	 *
	 * @param editor the editor
	 * @param lineDelimiter the target line delimiter to convert the editor's document to
	 */
	public ConvertLineDelimitersAction(ITextEditor editor, String lineDelimiter) {
		this(EditorMessages.getBundleForConstructedKeys(), "dummy", editor, lineDelimiter); //$NON-NLS-1$
	}

	/**
	 * Creates a line delimiter conversion action.
	 *
	 * @param bundle the resource bundle
	 * @param prefix the prefix for the resource bundle lookup
	 * @param editor the editor
	 * @param lineDelimiter the target line delimiter to convert the editor's document to
	 */
	public ConvertLineDelimitersAction(ResourceBundle bundle, String prefix, ITextEditor editor, String lineDelimiter) {
		super(bundle, prefix, editor);
		fLineDelimiter= lineDelimiter;

		String platformLineDelimiter= System.getProperty("line.separator"); //$NON-NLS-1$
		setText(getString(getLabelKey(fLineDelimiter, platformLineDelimiter)));

		update();
	}

	/*
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {

		try {

			ITextEditor editor= getTextEditor();
			if (editor == null)
				return;

			if (!validateEditorInputState())
				return;

			Object adapter= editor.getAdapter(IRewriteTarget.class);
			if (adapter instanceof IRewriteTarget) {

				IRewriteTarget target= (IRewriteTarget) adapter;
				IDocument document= target.getDocument();
				if (document != null) {
					Shell shell= getTextEditor().getSite().getShell();
					ConvertRunnable runnable= new ConvertRunnable(target, fLineDelimiter);

					if (document.getNumberOfLines() < 40) {
						BusyIndicator.showWhile(shell.getDisplay(), runnable);

					} else {
						ProgressMonitorDialog dialog= new ProgressMonitorDialog(shell);
						dialog.run(false, true, runnable);
					}
				}
			}

		} catch (InterruptedException e) {
			// action canceled
		} catch (InvocationTargetException e) {
			// should not happen
		}
	}

	/**
	 * A runnable that converts all line delimiters of a document to <code>lineDelimiter</code>.
	 */
	private static class ConvertRunnable implements IRunnableWithProgress, Runnable {

		/** The rewrite target */
		private final IRewriteTarget fRewriteTarget;
		/** The line delimiter to which to convert to */
		private final String fLineDelimiter;

		/**
		 * Returns a new runnable for converting all line delimiters in the
		 * <code>rewriteTarget</code> to <code>lineDelimter</code>.
		 *
		 * @param rewriteTarget the rewrite target
		 * @param lineDelimiter the line delimiter
		 */
		public ConvertRunnable(IRewriteTarget rewriteTarget, String lineDelimiter) {
			fRewriteTarget= rewriteTarget;
			fLineDelimiter= lineDelimiter;
		}

		/*
		 * @see IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

			IDocument document= fRewriteTarget.getDocument();
			final int lineCount= document.getNumberOfLines();
			monitor.beginTask(EditorMessages.Editor_ConvertLineDelimiter_title, lineCount);

			final boolean isLargeUpdate= lineCount > 50;
			if (isLargeUpdate)
				fRewriteTarget.setRedraw(false);
			fRewriteTarget.beginCompoundChange();

			Map partitioners= TextUtilities.removeDocumentPartitioners(document);

			try {
				for (int i= 0; i < lineCount; i++) {
					if (monitor.isCanceled())
						throw new InterruptedException();

					final String delimiter= document.getLineDelimiter(i);
					if (delimiter != null && delimiter.length() > 0 && !delimiter.equals(fLineDelimiter)) {
						IRegion region= document.getLineInformation(i);
						document.replace(region.getOffset() + region.getLength(), delimiter.length(), fLineDelimiter);
					}

					monitor.worked(1);
				}

			} catch (BadLocationException e) {
				throw new InvocationTargetException(e);

			} finally {

				if (partitioners != null)
					TextUtilities.addDocumentPartitioners(document, partitioners);

				fRewriteTarget.endCompoundChange();
				if (isLargeUpdate)
					fRewriteTarget.setRedraw(true);

				monitor.done();
			}
		}

		/*
		 * @see Runnable#run()
		 */
		public void run() {
			try {
				run(new NullProgressMonitor());

			} catch (InterruptedException e) {
				// should not happen

			} catch (InvocationTargetException e) {
				// should not happen
			}
		}
	}

//	/**
//	 * Returns whether the given document uses only the given line delimiter.
//	 * @param document the document to check
//	 * @param lineDelimiter the line delimiter to check for
//	 */
//	private static boolean usesLineDelimiterExclusively(IDocument document, String lineDelimiter) {
//
//		try {
//			final int lineCount= document.getNumberOfLines();
//			for (int i= 0; i < lineCount; i++) {
//				final String delimiter= document.getLineDelimiter(i);
//				if (delimiter != null && delimiter.length() > 0 && !delimiter.equals(lineDelimiter))
//					return false;
//			}
//
//		} catch (BadLocationException e) {
//			return false;
//		}
//
//		return true;
//	}

	/**
	 * Computes and returns the key to be used to lookup the action's label in
	 * its resource bundle.
	 *
	 * @param lineDelimiter the line delimiter
	 * @param platformLineDelimiter the platform line delimiter
	 * @return the key used to lookup the action's label
	 */
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
	 * @since 3.1
	 */
	private static String getString(String key) {
		try {
			return EditorMessages.getBundleForConstructedKeys().getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!";//$NON-NLS-2$ //$NON-NLS-1$
		}
	}

	/*
	 * @see IUpdate#update()
	 */
	public void update() {
		super.update();
		setEnabled(canModifyEditor());
	}

}
