/*******************************************************************************
 * Copyright (c) 2026 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vector Informatik GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.tests;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.core.resources.IMarker;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.part.EditorPart;

/**
 * Test editor that is not an {@code ITextEditor} and only adapts to {@link IGotoMarker}. It records
 * every {@code gotoMarker} call.
 */
public class GotoMarkerOnlyEditor extends EditorPart {

	public static final String ID= "org.eclipse.search.tests.GotoMarkerOnlyEditor"; //$NON-NLS-1$

	/**
	 * One {@code gotoMarker} call: the marker range and whether the marker existed at call time.
	 */
	public record GotoCall(int charStart, int charEnd, boolean markerExisted) {
	}

	private final List<GotoCall> fCalls= new CopyOnWriteArrayList<>();

	private final IGotoMarker fGotoMarker= marker -> fCalls.add(new GotoCall(marker.getAttribute(IMarker.CHAR_START, -1),
			marker.getAttribute(IMarker.CHAR_END, -1), marker.exists()));

	public List<GotoCall> getGotoCalls() {
		return fCalls;
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) {
		setSite(site);
		setInput(input);
	}

	@Override
	public void createPartControl(Composite parent) {
		new Label(parent, SWT.NONE).setText(getEditorInput().getName());
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == IGotoMarker.class) {
			return adapter.cast(fGotoMarker);
		}
		return super.getAdapter(adapter);
	}

	@Override
	public void setFocus() {
		// nothing to focus
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// never dirty
	}

	@Override
	public void doSaveAs() {
		// not allowed
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
}
