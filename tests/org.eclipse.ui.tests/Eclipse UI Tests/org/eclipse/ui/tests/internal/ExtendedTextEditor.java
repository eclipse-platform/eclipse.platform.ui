package org.eclipse.ui.tests.internal;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;

import org.eclipse.ui.editors.text.TextEditor;

public class ExtendedTextEditor extends TextEditor {

	/**
	 * Constructor for TextSelectionActionFilterEditor.
	 */
	public ExtendedTextEditor() {
		super();
	}

	/**
	 * Creates the source viewer to be used by this editor.
	 * Subclasses may re-implement this method.
	 *
	 * @param parent the parent control
	 * @param ruler the vertical ruler
	 * @param styles style bits
	 * @return the source viewer
	 */
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		return new ExtendedSourceViewer(parent, ruler, styles);
	}

	public boolean isDirty() {
		return false;
	}
		
	/**
	 * Set the text in the editor.
	 */
	public void setText(String text) {
		ExtendedSourceViewer viewer = (ExtendedSourceViewer)getSourceViewer();
		StyledText widget = viewer.getTextWidget();
		widget.setText(text);
		viewer.setSelectedRange(0, text.length());
	}

}

