package org.eclipse.ui.tests.internal;

import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;

import org.eclipse.ui.IActionFilter;

public class ExtendedTextSelection extends TextSelection 
	implements IAdaptable
{
	static private ExtendedTextSelectionActionFilter filter =
		new ExtendedTextSelectionActionFilter();

	/**
	 * Constructor for ExtendedTextSelection.
	 * @param offset
	 * @param length
	 */
	public ExtendedTextSelection(int offset, int length) {
		super(offset, length);
	}

	/**
	 * Constructor for ExtendedTextSelection.
	 * @param document
	 * @param offset
	 * @param length
	 */
	public ExtendedTextSelection(IDocument document, int offset, int length) {
		super(document, offset, length);
	}

	/*
	 * @see IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IActionFilter.class) {
			return filter;
		}
		return null;
	}

}

