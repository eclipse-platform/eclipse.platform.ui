package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.views.ConsoleViewer;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

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
					return ActionMessages.getString("TextViewerGotoLineAction.Line_number_out_of_range_1"); //$NON-NLS-1$

			} catch (NumberFormatException x) {
				return ActionMessages.getString("TextViewerGotoLineAction.Not_a_number_2"); //$NON-NLS-1$
			}

			return ""; //$NON-NLS-1$
		}
	};

	protected int fLastLine;
	protected ITextViewer fTextViewer;
	
	/**
	 * Constructs a goto line action for the console using the provided resource bundle
	 */
	public TextViewerGotoLineAction(ConsoleViewer viewer) {
		super(viewer, -1);
		fTextViewer= viewer;
		setText(ActionMessages.getString("TextViewerGotoLineAction.Go_to_&Line...@Ctrl+L_4")); //$NON-NLS-1$
		setToolTipText(ActionMessages.getString("TextViewerGotoLineAction.Go_To_Line_1")); //$NON-NLS-1$
		setDescription(ActionMessages.getString("TextViewerGotoLineAction.Go_To_Line_1"));		 //$NON-NLS-1$
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
			DebugUIPlugin.errorDialog(fTextViewer.getTextWidget().getShell(), ActionMessages.getString("TextViewerGotoLineAction.Go_To_Line_1"), ActionMessages.getString("TextViewerGotoLineAction.Exceptions_occurred_attempt_to_go_to_line_2"), x); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * @see Action#run()
	 */
	public void run() {
		try {
			Point selection= fTextViewer.getTextWidget().getSelection();
			IDocument document= fTextViewer.getDocument();
			fLastLine= document.getLineOfOffset(document.getLength()) + 1;
			int startLine= selection == null ? 1 : fTextViewer.getTextWidget().getLineAtOffset(selection.x) + 1;
			String title= ActionMessages.getString("TextViewerGotoLineAction.Go_To_Line_1"); //$NON-NLS-1$
			String message= MessageFormat.format(ActionMessages.getString("TextViewerGotoLineAction.Enter_line_number__8"), new Object[] {new Integer(fLastLine)}); //$NON-NLS-1$
			String value= Integer.toString(startLine);
			Shell activeShell= fTextViewer.getTextWidget().getShell();
			InputDialog d= new InputDialog(activeShell, title, message, value, new NumberValidator());
			if (d.open() == d.OK) {
				try {
					int line= Integer.parseInt(d.getValue());
					gotoLine(line - 1);
				} catch (NumberFormatException x) {
					DebugUIPlugin.errorDialog(activeShell, ActionMessages.getString("TextViewerGotoLineAction.Go_To_Line_1"), ActionMessages.getString("TextViewerGotoLineAction.Exceptions_occurred_attempt_to_go_to_line_2"), x); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		} catch (BadLocationException x) {
			DebugUIPlugin.errorDialog(fTextViewer.getTextWidget().getShell(), ActionMessages.getString("TextViewerGotoLineAction.Go_To_Line_1"), ActionMessages.getString("TextViewerGotoLineAction.Exceptions_occurred_attempt_to_go_to_line_2"), x); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
	}
}

