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
package org.eclipse.ui.console.actions;


import java.text.MessageFormat;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.internal.console.ConsoleMessages;

public class TextViewerGotoLineAction extends TextViewerAction {

	/**
	 * Validates whether the text found in the input field of the
	 * dialog forms a valid line number, i.e. one to which can be 
	 * jumped.
	 */
	class NumberValidator implements IInputValidator {

		public String isValid(String input) {
			try {
				int i= Integer.parseInt(input);
				if (i <= 0 || fLastLine < i)
					return ConsoleMessages.getString("TextViewerGotoLineAction.Line_number_out_of_range_1"); //$NON-NLS-1$

			} catch (NumberFormatException x) {
				return ConsoleMessages.getString("TextViewerGotoLineAction.Not_a_number_2"); //$NON-NLS-1$
			}

			return null;
		}
	}

	protected int fLastLine;
	protected ITextViewer fTextViewer;
	
	/**
	 * Constructs a goto line action for the viewer using the provided resource bundle
	 */
	public TextViewerGotoLineAction(ITextViewer viewer) {
		super(viewer, -1);
		fTextViewer= viewer;
		setText(ConsoleMessages.getString("TextViewerGotoLineAction.Go_to_&Line...@Ctrl+L_4")); //$NON-NLS-1$
		setToolTipText(ConsoleMessages.getString("TextViewerGotoLineAction.Go_To_Line_1")); //$NON-NLS-1$
		setDescription(ConsoleMessages.getString("TextViewerGotoLineAction.Go_To_Line_1"));		 //$NON-NLS-1$
	}
	
	/**
	 * @see TextViewerAction#update()
	 */
	public void update() {
	}

	/**
	 * Jumps to the line.
	 */
	protected void gotoLine(int line) {

		IDocument document= fTextViewer.getDocument();
		try {
			int start= document.getLineOffset(line);
			int length= document.getLineLength(line);
			fTextViewer.getTextWidget().setSelection(start, start + length);
			fTextViewer.revealRange(start, length);
		} catch (BadLocationException x) {
			ConsolePlugin.errorDialog(fTextViewer.getTextWidget().getShell(), ConsoleMessages.getString("TextViewerGotoLineAction.Go_To_Line_1"), ConsoleMessages.getString("TextViewerGotoLineAction.Exceptions_occurred_attempt_to_go_to_line_2"), x); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		try {
			Point selection= fTextViewer.getTextWidget().getSelection();
			IDocument document= fTextViewer.getDocument();
			fLastLine= document.getLineOfOffset(document.getLength()) + 1;
			int startLine= selection == null ? 1 : fTextViewer.getTextWidget().getLineAtOffset(selection.x) + 1;
			String title= ConsoleMessages.getString("TextViewerGotoLineAction.Go_To_Line_1"); //$NON-NLS-1$
			String message= MessageFormat.format(ConsoleMessages.getString("TextViewerGotoLineAction.Enter_line_number__8"), new Object[] {new Integer(fLastLine)}); //$NON-NLS-1$
			String value= Integer.toString(startLine);
			Shell activeShell= fTextViewer.getTextWidget().getShell();
			InputDialog d= new InputDialog(activeShell, title, message, value, new NumberValidator());
			if (d.open() == Window.OK) {
				try {
					int line= Integer.parseInt(d.getValue());
					gotoLine(line - 1);
				} catch (NumberFormatException x) {
					ConsolePlugin.errorDialog(activeShell, ConsoleMessages.getString("TextViewerGotoLineAction.Go_To_Line_1"), ConsoleMessages.getString("TextViewerGotoLineAction.Exceptions_occurred_attempt_to_go_to_line_2"), x); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		} catch (BadLocationException x) {
			ConsolePlugin.errorDialog(fTextViewer.getTextWidget().getShell(), ConsoleMessages.getString("TextViewerGotoLineAction.Go_To_Line_1"), ConsoleMessages.getString("TextViewerGotoLineAction.Exceptions_occurred_attempt_to_go_to_line_2"), x); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
	}
}