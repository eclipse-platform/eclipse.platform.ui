/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.model;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.jface.internal.text.html.HTMLPrinter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.jface.text.TextSelection;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;

/**
 * Viewer which displays a summary of a pending refactoring.
 *
 * @since 3.2
 */
public class RefactoringDescriptorViewer extends Viewer {

	/** The viewer control */
	protected final Browser fBrowser;

	/** The viewer input, or <code>null</code> */
	private RefactoringDescriptorProxy fDescriptor= null;

	/**
	 * Creates a new refactoring descriptor viewer.
	 *
	 * @param parent
	 *            the parent control
	 * @param style
	 *            the style
	 */
	public RefactoringDescriptorViewer(final Composite parent, final int style) {
		Assert.isNotNull(parent);
		fBrowser= new Browser(parent, style);
		fBrowser.setJavascriptEnabled(false);
		
		final Display display= parent.getDisplay();
		fBrowser.setForeground(display.getSystemColor(SWT.COLOR_LIST_FOREGROUND));
		fBrowser.setBackground(display.getSystemColor(SWT.COLOR_LIST_BACKGROUND));
	}

	/**
	 * {@inheritDoc}
	 */
	public Control getControl() {
		return fBrowser;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getInput() {
		return fDescriptor;
	}

	/**
	 * Returns the input text for the specified refactoring descriptor proxy.
	 *
	 * @param proxy
	 *            the refactoring descriptor proxy, or <code>null</code>
	 * @return the input text
	 */
	protected String getInputText(final RefactoringDescriptorProxy proxy) {
		final StringBuffer buffer= new StringBuffer();

		// XXX: should use style sheet and set dialog font.

		HTMLPrinter.insertPageProlog(buffer, 0);
		if (proxy != null) {
			HTMLPrinter.addSmallHeader(buffer, HTMLPrinter.convertToHTMLContent(proxy.getDescription()));
			final RefactoringDescriptor descriptor= proxy.requestDescriptor(new NullProgressMonitor());
			if (descriptor != null) {
				final String comment= descriptor.getComment();
				if (comment != null && !"".equals(comment)) //$NON-NLS-1$
					HTMLPrinter.addParagraph(buffer, HTMLPrinter.convertToHTMLContent(comment));
				HTMLPrinter.startBulletList(buffer);
				final int flags= descriptor.getFlags();
				if ((flags & RefactoringDescriptor.BREAKING_CHANGE) > 0)
					HTMLPrinter.addBullet(buffer, ModelMessages.RefactoringDescriptorViewer_breaking_change_message);
				if ((flags & RefactoringDescriptor.STRUCTURAL_CHANGE) > 0)
					HTMLPrinter.addBullet(buffer, ModelMessages.RefactoringDescriptorViewer_structural_change_message);
				if ((flags & RefactoringDescriptor.MULTI_CHANGE) > 0)
					HTMLPrinter.addBullet(buffer, ModelMessages.RefactoringDescriptorViewer_closure_change_message);
				HTMLPrinter.endBulletList(buffer);
			}
		}
		HTMLPrinter.addPageEpilog(buffer);
		return buffer.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public ISelection getSelection() {
		return new TextSelection(0, 0);
	}

	/**
	 * {@inheritDoc}
	 */
	public void refresh() {
		String text= getInputText(fDescriptor);
		if (text != null && text.length() > 0) {
			if ((fBrowser.getShell().getStyle() & SWT.RIGHT_TO_LEFT) != 0) {
				final StringBuffer buffer= new StringBuffer(text);
				HTMLPrinter.insertStyles(buffer, new String[] { "direction:rtl", "overflow:hidden"}); //$NON-NLS-1$ //$NON-NLS-2$
				text= buffer.toString();
			}
		}
		fBrowser.setText(text);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setInput(final Object input) {
		if (input instanceof RefactoringDescriptorProxy) {
			fDescriptor= (RefactoringDescriptorProxy) input;
			refresh();
		} else
			fDescriptor= null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSelection(final ISelection selection, final boolean reveal) {
		// Do nothing
	}
}
