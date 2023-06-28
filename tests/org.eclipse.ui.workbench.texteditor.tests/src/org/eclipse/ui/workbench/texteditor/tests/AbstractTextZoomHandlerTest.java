/*******************************************************************************
 * Copyright (c) 2017 SAP SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     SAP SE - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.workbench.texteditor.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.expressions.EvaluationContext;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISources;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.AbstractMultiEditor;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.MultiPageEditorPart;

import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.TextZoomInHandler;

public class AbstractTextZoomHandlerTest {

	@Test
	public void textZoomIsSupportedForAbstractTextEditor() {
		IEditorPart part= new TestAbstractTextEditor();

		assertZoomSupported(part, true);
	}

	@Test
	public void textZoomIsNotSupportedForNonTextEditor() {
		IEditorPart part= new TestNonTextEditor();

		assertZoomSupported(part, false);
	}

	@Test
	public void textZoomIsSupportedForMultiPageEditorPartWithAbstractTextEditorPage() {
		EditorPart part= new TestMultiPageEditorPart(new TestAbstractTextEditor());

		assertZoomSupported(part, true);
	}

	@Test
	public void textZoomIsNotSupportedForMultiPageEditorPartWithNonTextPage() {
		EditorPart part= new TestMultiPageEditorPart(new TestNonTextEditor());

		assertZoomSupported(part, false);
	}

	@Test
	public void textZoomIsSupportedForMultiEditorWithTextPage() {
		EditorPart part= new TestMultiEditor(new TestAbstractTextEditor());

		assertZoomSupported(part, true);
	}

	@Test
	public void textZoomIsNotSupportedForMultiEditorWithNonTextPage() {
		EditorPart part= new TestMultiEditor(new TestNonTextEditor());

		assertZoomSupported(part, false);
	}

	@Test
	public void textZoomIsSupportedForAdaptableToAbstractTextEditor() {
		IEditorPart part= new TestAdaptableToAbstractTestEditor();

		assertZoomSupported(part, true);
	}

	@Test
	public void textZoomIsNotSupportedForGenericObject() {
		assertZoomSupported(new Object(), false);
	}

	private void assertZoomSupported(Object receiver, boolean expectedEnabled) {
		TextZoomInHandler textZoomHandler= new TextZoomInHandler();

		EvaluationContext evaluationContext= new EvaluationContext(null, new Object());
		evaluationContext.addVariable(ISources.ACTIVE_EDITOR_NAME, receiver);

		textZoomHandler.setEnabled(evaluationContext);

		boolean actualEnabled= textZoomHandler.isEnabled();

		assertEquals(expectedEnabled, actualEnabled);
	}

	private static class TestAbstractTextEditor extends AbstractTextEditor {

		@Override
		public <T> T getAdapter(Class<T> adapter) {
			return null;
		}

		@Override
		public void init(IEditorSite site, IEditorInput input) throws PartInitException { //
		}
	}

	private static class TestNonTextEditor extends EditorPart {

		@Override
		public <T> T getAdapter(Class<T> adapter) {
			return null;
		}

		@Override
		public void doSave(IProgressMonitor monitor) { //
		}

		@Override
		public void doSaveAs() { //
		}

		@Override
		public void init(IEditorSite site, IEditorInput input) throws PartInitException { //
		}

		@Override
		public boolean isDirty() { //
			return false;
		}

		@Override
		public boolean isSaveAsAllowed() { //
			return false;
		}

		@Override
		public void createPartControl(Composite parent) { //
		}

		@Override
		public void setFocus() { //
		}
	}

	private static class TestAdaptableToAbstractTestEditor extends TestNonTextEditor {

		@Override
		public <T> T getAdapter(Class<T> adapter) {
			if (adapter == AbstractTextEditor.class) {
				return adapter.cast(new TestAbstractTextEditor());
			}
			return null;
		}
	}

	private static class TestMultiEditor extends AbstractMultiEditor {

		@Override
		public <T> T getAdapter(Class<T> adapter) {
			return null;
		}

		public TestMultiEditor(IEditorPart child) {
			setChildren(new IEditorPart[] { child });
		}

		@Override
		public void createPartControl(Composite parent) { //
		}

		@Override
		protected void innerEditorsCreated() { //
		}

		@Override
		public Composite getInnerEditorContainer(IEditorReference innerEditorReference) {
			return null;
		}
	}

	private static class TestMultiPageEditorPart extends MultiPageEditorPart {

		private IEditorPart child;

		public TestMultiPageEditorPart(IEditorPart child) {
			this.child= child;
		}

		@Override
		public <T> T getAdapter(Class<T> adapter) {
			return null;
		}

		@Override
		public boolean isSaveAsAllowed() {
			return false;
		}

		@Override
		public void doSaveAs() { //
		}

		@Override
		public void doSave(IProgressMonitor monitor) { //
		}

		@Override
		protected void createPages() { //
		}

		@Override
		public void init(IEditorSite site, IEditorInput input) throws PartInitException { //
		}

		@Override
		public Object getSelectedPage() {
			return child;
		}
	}

}
