/*******************************************************************************
* Copyright (c) 2024 Ole Osterhagen and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Ole Osterhagen - initial API and implementation
*******************************************************************************/
package org.eclipse.ui.internal.genericeditor.compare;

import java.io.InputStream;

import org.eclipse.compare.IEncodedStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.SharedDocumentAdapter;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.core.resources.IEncodedStorage;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.internal.genericeditor.ExtensionBasedTextViewerConfiguration;
import org.eclipse.ui.internal.genericeditor.GenericEditorPlugin;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class GenericEditorViewer extends Viewer {

	private final SourceViewer sourceViewer;

	private IEditorInput editorInput;

	public GenericEditorViewer(Composite parent) {
		sourceViewer = new SourceViewer(parent, null, SWT.H_SCROLL | SWT.V_SCROLL);
		sourceViewer.setEditable(false);

		// use the same font as the TextMergeViewer
		sourceViewer.getTextWidget().setFont(JFaceResources.getFont(TextMergeViewer.class.getName()));
		sourceViewer.getTextWidget().addDisposeListener(e -> disconnect());
	}

	@Override
	public Control getControl() {
		return sourceViewer.getControl();
	}

	@Override
	public Object getInput() {
		return editorInput;
	}

	@Override
	public ISelection getSelection() {
		return StructuredSelection.EMPTY;
	}

	@Override
	public void refresh() {
		// empty implementation
	}

	@Override
	public void setInput(Object input) {
		disconnect();

		if (!(input instanceof ITypedElement && input instanceof IEncodedStreamContentAccessor)) {
			return;
		}

		IStorage storage = new Storage<>((ITypedElement & IEncodedStreamContentAccessor) input);
		editorInput = new StorageEditorInput(storage);

		IDocumentProvider documentProvider = SharedDocumentAdapter.getDocumentProvider(editorInput);
		try {
			documentProvider.connect(editorInput);
		} catch (CoreException ex) {
			GenericEditorPlugin.getDefault().getLog()
					.log(new Status(IStatus.ERROR, GenericEditorPlugin.BUNDLE_ID, ex.getMessage(), ex));
		}

		sourceViewer.setDocument(documentProvider.getDocument(editorInput));

		ExtensionBasedTextViewerConfiguration configuration = new ExtensionBasedTextViewerConfiguration(null,
				new ChainedPreferenceStore(new IPreferenceStore[] { EditorsUI.getPreferenceStore(),
						GenericEditorPlugin.getDefault().getPreferenceStore() }));
		sourceViewer.unconfigure();
		sourceViewer.configure(configuration);
	}

	@Override
	public void setSelection(ISelection selection, boolean reveal) {
		// empty implementation
	}

	private void disconnect() {
		if (editorInput != null) {
			sourceViewer.setDocument(null);
			SharedDocumentAdapter.getDocumentProvider(editorInput).disconnect(editorInput);
			editorInput = null;
		}
	}

	private static class Storage<T extends ITypedElement & IEncodedStreamContentAccessor> extends PlatformObject
			implements IEncodedStorage {

		private final T element;

		public Storage(T element) {
			this.element = element;
		}

		@Override
		public InputStream getContents() throws CoreException {
			return element.getContents();
		}

		@Override
		public IPath getFullPath() {
			return null;
		}

		@Override
		public String getName() {
			return element.getName();
		}

		@Override
		public boolean isReadOnly() {
			return true;
		}

		@Override
		public String getCharset() throws CoreException {
			return element.getCharset();
		}

	}

	private static class StorageEditorInput extends PlatformObject implements IStorageEditorInput {

		private final IStorage storage;

		public StorageEditorInput(IStorage storage) {
			this.storage = storage;
		}

		@Override
		public boolean exists() {
			return false;
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return PlatformUI.getWorkbench().getEditorRegistry().getImageDescriptor(storage.getName());
		}

		@Override
		public String getName() {
			return storage.getName();
		}

		@Override
		public IPersistableElement getPersistable() {
			return null;
		}

		@Override
		public String getToolTipText() {
			return storage.getName();
		}

		@Override
		public IStorage getStorage() throws CoreException {
			return storage;
		}

	}

}
