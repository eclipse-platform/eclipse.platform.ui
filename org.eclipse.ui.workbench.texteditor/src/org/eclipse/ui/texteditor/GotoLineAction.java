/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import java.util.ResourceBundle;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.internal.texteditor.NLSUtility;
import org.eclipse.ui.internal.texteditor.TextEditorPlugin;


/**
 * Action for jumping to a particular line in the editor's text viewer. The user is requested to
 * enter the line number into an input dialog. The action is initially associated with a text editor
 * via the constructor, but that can be subsequently changed using <code>setEditor</code>.
 * <p>
 * The following keys, prepended by the given option prefix, are used for retrieving resources from
 * the given bundle:
 * <ul>
 * <li><code>"dialog.invalid_range"</code> - to indicate an invalid line number</li>
 * <li><code>"dialog.invalid_input"</code> - to indicate an invalid line number format</li>
 * <li><code>"dialog.title"</code> - the input dialog's title</li>
 * <li><code>"dialog.message"</code> - the input dialog's message</li>
 * </ul>
 * </p>
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
public class GotoLineAction extends TextEditorAction {

	/**
	 * Validates whether the text found in the input field of the dialog forms a valid line number.
	 * A number is valid if it is one to which can be jumped.
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
	 * Standard input dialog with custom dialog bounds strategy and settings.
	 * 
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
		 * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsSettings()
		 * @since 3.2
		 */
		protected IDialogSettings getDialogBoundsSettings() {
			String sectionName= getClass().getName() + "_dialogBounds"; //$NON-NLS-1$
			IDialogSettings settings= TextEditorPlugin.getDefault().getDialogSettings();
			IDialogSettings section= settings.getSection(sectionName);
			if (section == null)
				section= settings.addNewSection(sectionName);
			return section;
		}

		/*
		 * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsStrategy()
		 * @since 3.2
		 */
		protected int getDialogBoundsStrategy() {
			return DIALOG_PERSISTLOCATION;
		}
	}

	/** The biggest valid line number of the presented document */
	private int fLastLine;

	/** This action's resource bundle */
	private ResourceBundle fBundle;

	/** This action's prefix used for accessing the resource bundle */
	private String fPrefix;


	/**
	 * Creates a new action for the given text editor. The action configures its visual
	 * representation from the given resource bundle.
	 * 
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys (described in
	 *            <code>ResourceAction</code> constructor), or <code>null</code> if none
	 * @param editor the text editor
	 * @see TextEditorAction#TextEditorAction(ResourceBundle, String, ITextEditor)
	 */
	public GotoLineAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
		super(bundle, prefix, editor);
		fBundle= bundle;
		fPrefix= prefix;
	}

	/**
	 * Creates a new action for the given text editor. The action configures its visual
	 * representation with default values.
	 * 
	 * @param editor the text editor
	 * @see TextEditorAction#TextEditorAction(ResourceBundle, String, ITextEditor)
	 * @since 3.5
	 */
	public GotoLineAction(ITextEditor editor) {
		super(EditorMessages.getBundleForConstructedKeys(), "Editor.GotoLine.", editor); //$NON-NLS-1$
		fBundle= EditorMessages.getBundleForConstructedKeys();
		fPrefix= "Editor.GotoLine."; //$NON-NLS-1$
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
		ITextEditor editor= getTextEditor();

		if (editor == null)
			return;

		IDocumentProvider docProvider= editor.getDocumentProvider();
		if (docProvider == null)
			return;

		IDocument document= docProvider.getDocument(editor.getEditorInput());
		if (document == null)
			return;

		try {
			fLastLine= document.getLineOfOffset(document.getLength()) + 1;
		} catch (BadLocationException ex) {
			IStatus status= new Status(IStatus.ERROR, TextEditorPlugin.PLUGIN_ID, IStatus.OK, "Go to Line failed", ex); //$NON-NLS-1$
			TextEditorPlugin.getDefault().getLog().log(status);
			return;
		}

		String title= fBundle.getString(fPrefix + "dialog.title"); //$NON-NLS-1$
		String message= NLSUtility.format(fBundle.getString(fPrefix + "dialog.message"), new Integer(fLastLine)); //$NON-NLS-1$

		String currentLineStr= ""; //$NON-NLS-1$
		ISelection selection= editor.getSelectionProvider().getSelection();
		if (selection instanceof ITextSelection) {
			ITextSelection textSelection= (ITextSelection)selection;
			Control textWidget= (Control)editor.getAdapter(Control.class);
			boolean caretAtStartOfSelection= false;
			if (textWidget instanceof StyledText)
				caretAtStartOfSelection= ((StyledText)textWidget).getSelection().x == ((StyledText)textWidget).getCaretOffset();
			int currentLine;
			if (caretAtStartOfSelection)
				currentLine= textSelection.getStartLine();
			else {
				int endOffset= textSelection.getOffset() + textSelection.getLength();
				try {
					currentLine= document.getLineOfOffset(endOffset);
				} catch (BadLocationException ex) {
					currentLine= -1;
				}
			}
			if (currentLine > -1)
				currentLineStr= Integer.toString(currentLine + 1);
		}

		GotoLineDialog d= new GotoLineDialog(editor.getSite().getShell(), title, message, currentLineStr, new NumberValidator());
		if (d.open() == Window.OK) {
			try {
				int line= Integer.parseInt(d.getValue());
				gotoLine(line - 1);
			} catch (NumberFormatException x) {
			}
		}
	}
}
