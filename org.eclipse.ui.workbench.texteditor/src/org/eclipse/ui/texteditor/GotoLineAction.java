/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.texteditor;


import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.window.Window;

import org.eclipse.ui.IWorkbenchPage;


/**
 * Action for jumping to a particular line in the editor's text viewer.
 * The user is requested to enter the line number into an input dialog.
 * The action is initially associated with a text editor via the constructor,
 * but that can be subsequently changed using <code>setEditor</code>.
 * <p>
 * The following keys, prepended by the given option prefix,
 * are used for retrieving resources from the given bundle:
 * <ul>
 *   <li><code>"dialog.invalid_range"</code> - to indicate an invalid line number</li>
 *   <li><code>"dialog.invalid_input"</code> - to indicate an invalid line number format</li>
 *   <li><code>"dialog.title"</code> - the input dialog's title</li>
 *   <li><code>"dialog.message"</code> - the input dialog's message</li>
 * </ul></p>
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class GotoLineAction extends TextEditorAction {

	/**
	 * Validates whether the text found in the input field of the
	 * dialog forms a valid line number. A number is valid if it is
	 * one to which can be jumped.
	 */
	class NumberValidator implements IInputValidator {

		/*
		 * @see IInputValidator#isValid(String)
		 */
		public String isValid(String input) {

			if (input == null || input.length() == 0)
				return " "; //$NON-NLS-1$

			try {
				int i= Integer.parseInt(input);
				if (i <= 0 || fLastLine < i)
					return fBundle.getString(fPrefix + "dialog.invalid_range"); //$NON-NLS-1$

			} catch (NumberFormatException x) {
				return fBundle.getString(fPrefix + "dialog.invalid_input"); //$NON-NLS-1$
			}

			return null;
		}
	}

	/**
	 * Standard input dialog which additionally sets the focus to the
	 * text input field. Workaround for <code>InputDialog</code> issue.
	 * 1GIJZOO: ITPSRCEDIT:ALL - Gotodialog's edit field has no initial focus
	 * @since 2.0
	 */
	static class GotoLineDialog extends InputDialog {

		/*
		 * @see InputDialog#InputDialog(org.eclipse.swt.widgets.Shell, java.lang.String, java.lang.String, java.lang.String, org.eclipse.jface.dialogs.IInputValidator)
		 */
		public GotoLineDialog(Shell parent, String title, String message, String initialValue, IInputValidator validator) {
			super(parent, title, message, initialValue, validator);
		}

		/*
		 * @see InputDialog#createDialogArea(Composite)
		 */
		protected Control createDialogArea(Composite parent) {
			Control result= super.createDialogArea(parent);
			getText().setFocus();
			applyDialogFont(result);
			return result;
		}
	}

	/** The biggest valid line number of the presented document */
	private int fLastLine;
	/** This action's resource bundle */
	private ResourceBundle fBundle;
	/** This action's prefix used for accessing the resource bundle */
	private String fPrefix;

	/**
	 * Creates a new action for the given text editor. The action configures its
	 * visual representation from the given resource bundle.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or
	 *   <code>null</code> if none
	 * @param editor the text editor
	 * @see TextEditorAction#TextEditorAction(ResourceBundle, String, ITextEditor)
	 */
	public GotoLineAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
		super(bundle, prefix, editor);
		fBundle= bundle;
		fPrefix= prefix;
	}

	/**
	 * Jumps to the given line.
	 *
	 * @param line the line to jump to
	 */
	private void gotoLine(int line) {

		ITextEditor editor= getTextEditor();

		IDocumentProvider provider= editor.getDocumentProvider();
		IDocument document= provider.getDocument(editor.getEditorInput());
		try {

			int start= document.getLineOffset(line);
			editor.selectAndReveal(start, 0);

			IWorkbenchPage page= editor.getSite().getPage();
			page.activate(editor);

		} catch (BadLocationException x) {
			// ignore
		}
	}

	/*
	 * @see Action#run()
	 */
	public void run() {
		try {

			ITextEditor editor= getTextEditor();

			if (editor == null)
				return;

			IDocumentProvider docProvider= editor.getDocumentProvider();
			if (docProvider == null)
				return;

			IDocument document= docProvider.getDocument(editor.getEditorInput());
			if (document == null)
				return;

			fLastLine= document.getLineOfOffset(document.getLength()) + 1;

			String title= fBundle.getString(fPrefix + "dialog.title"); //$NON-NLS-1$
			String message= MessageFormat.format(fBundle.getString(fPrefix + "dialog.message"), new Object[] {new Integer(fLastLine)}); //$NON-NLS-1$

			GotoLineDialog d= new GotoLineDialog(editor.getSite().getShell(), title, message, "", new NumberValidator()); //$NON-NLS-1$
			if (d.open() == Window.OK) {
				try {
					int line= Integer.parseInt(d.getValue());
					gotoLine(line - 1);
				} catch (NumberFormatException x) {
				}
			}

		} catch (BadLocationException x) {
		}
	}
}
