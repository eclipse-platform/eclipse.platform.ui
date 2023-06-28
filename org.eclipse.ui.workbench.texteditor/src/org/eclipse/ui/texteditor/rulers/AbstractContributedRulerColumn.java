/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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


	@Override
	public final RulerColumnDescriptor getDescriptor() {
		return fDescriptor;
	}

	@Override
	public final void setDescriptor(RulerColumnDescriptor descriptor) {
		Assert.isLegal(descriptor != null);
		Assert.isTrue(fDescriptor == null);
		fDescriptor= descriptor;
	}

	@Override
	public final void setEditor(ITextEditor editor) {
		Assert.isLegal(editor != null);
		Assert.isTrue(fEditor == null);
		fEditor= editor;
	}

	@Override
	public final ITextEditor getEditor() {
		return fEditor;
	}

	@Override
	public void columnCreated() {
	}

	@Override
	public void columnRemoved() {
	}
}
