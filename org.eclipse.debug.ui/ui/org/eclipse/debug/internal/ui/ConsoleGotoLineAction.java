package org.eclipse.debug.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 */

import java.util.ResourceBundle;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

public class ConsoleGotoLineAction extends ConsoleViewerAction {

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
					return fBundle.getString(fPrefix + "dialog.invalid_range");

			} catch (NumberFormatException x) {
				return fBundle.getString(fPrefix + "dialog.invalid_input");
			}

			return "";
		}
	};

	protected int fLastLine;
	protected ResourceBundle fBundle;
	protected String fPrefix;
	protected ConsoleViewer fConsoleViewer;
	
	/**
	 * Constructs a goto line action for the console using the provided resource bundle
	 */
	public ConsoleGotoLineAction(ResourceBundle bundle, String prefix, ConsoleViewer viewer) {
		super(bundle, prefix, viewer, -1);
		fBundle= bundle;
		fPrefix= prefix;
		fConsoleViewer= viewer;
	}

	/**
	 * @see TextEditorAction
	 */
	public void update() {
	}

	/**
	 * Jumps to the line.
	 */
	protected void gotoLine(int line) {

		IDocument document= fConsoleViewer.getDocument();
		try {
			int start= document.getLineOffset(line);
			int length= document.getLineLength(line);

			fConsoleViewer.getTextWidget().setSelection(start, start + length);
			fConsoleViewer.revealRange(start, length);
		} catch (BadLocationException x) {
			// ignore
		}
	}

	/**
	 * @see Action
	 */
	public void run() {
		try {
			Point selection= fConsoleViewer.getTextWidget().getSelection();
			IDocument document= fConsoleViewer.getDocument();
			fLastLine= document.getLineOfOffset(document.getLength()) + 1;
			int startLine= selection == null ? 1 : fConsoleViewer.getTextWidget().getLineAtOffset(selection.x) + 1;
			String title= fBundle.getString(fPrefix + "dialog.title");
			String message= fBundle.getString(fPrefix + "dialog.message");
			String value= Integer.toString(startLine);
			Shell activeShell= DebugUIPlugin.getActiveWorkbenchWindow().getShell();
			InputDialog d= new InputDialog(activeShell, title, message, value, new NumberValidator());
			d.open();

			try {
				int line= Integer.parseInt(d.getValue());
				gotoLine(line - 1);
			} catch (NumberFormatException x) {
			}
		} catch (BadLocationException x) {
			return;
		}
	}
}

