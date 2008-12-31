/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor.rulers;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;

import org.eclipse.ui.texteditor.ITextEditor;


/**
 * Helper class for contributions to the
 * <code>org.eclipse.ui.texteditor.rulerColumns</code> extension point.
 * <p>
 * Subclasses must have a zero-argument constructor so that they can be created by
 * {@link IConfigurationElement#createExecutableExtension(String)}.</p>
 *
 * @since 3.3
 */
public abstract class AbstractContributedRulerColumn implements IContributedRulerColumn {
	/** The contribution descriptor. */
	private RulerColumnDescriptor fDescriptor;
	/** The target editor. */
	private ITextEditor fEditor;


	/*
	 * @see org.eclipse.ui.texteditor.rulers.IContributedRulerColumn#getDescriptor()
	 */
	public final RulerColumnDescriptor getDescriptor() {
		return fDescriptor;
	}

	/*
	 * @see org.eclipse.ui.texteditor.rulers.IContributedRulerColumn#setDescriptor(org.eclipse.ui.texteditor.rulers.RulerColumnDescriptor)
	 */
	public final void setDescriptor(RulerColumnDescriptor descriptor) {
		Assert.isLegal(descriptor != null);
		Assert.isTrue(fDescriptor == null);
		fDescriptor= descriptor;
	}

	/*
	 * @see org.eclipse.ui.texteditor.rulers.IContributedRulerColumn#setEditor(org.eclipse.ui.texteditor.ITextEditor)
	 */
	public final void setEditor(ITextEditor editor) {
		Assert.isLegal(editor != null);
		Assert.isTrue(fEditor == null);
		fEditor= editor;
	}

	/*
	 * @see org.eclipse.ui.texteditor.rulers.IContributedRulerColumn#getEditor()
	 */
	public final ITextEditor getEditor() {
		return fEditor;
	}

	/*
	 * @see org.eclipse.ui.texteditor.rulers.IContributedRulerColumn#columnCreated()
	 */
	public void columnCreated() {
	}

	/*
	 * @see org.eclipse.ui.texteditor.rulers.IContributedRulerColumn#columnRemoved()
	 */
	public void columnRemoved() {
	}
}
