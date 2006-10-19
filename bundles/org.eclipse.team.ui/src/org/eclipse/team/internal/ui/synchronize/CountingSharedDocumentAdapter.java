package org.eclipse.team.internal.ui.synchronize;

import org.eclipse.compare.SharedDocumentAdapter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IElementStateListener;

/**
 * A shared document adapter that tracks whether the element is
 * connected to a shared document in order to ensure that
 * any saves/commits that occur while connected are performed
 * through the shared buffer.
 */
public class CountingSharedDocumentAdapter extends
		SharedDocumentAdapter implements IElementStateListener {
	
	private int connectionCount;
	private LocalResourceTypedElement element;

	/**
	 * Create the shared document adapter for the given element.
	 * @param element the element
	 */
	public CountingSharedDocumentAdapter(LocalResourceTypedElement element) {
		super();
		this.element = element;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.SharedDocumentAdapter#getDocumentKey(java.lang.Object)
	 */
	public IEditorInput getDocumentKey(Object element) {
		if (this.element.exists())
			return super.getDocumentKey(element);
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.compare.SharedDocumentAdapter#connect(org.eclipse.ui.texteditor.IDocumentProvider, org.eclipse.ui.IEditorInput)
	 */
	public void connect(IDocumentProvider provider, IEditorInput documentKey)
			throws CoreException {
		super.connect(provider, documentKey);
		connectionCount++;
		if (connectionCount == 1) {
			provider.addElementStateListener(this);
			element.updateTimestamp();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.SharedDocumentAdapter#disconnect(org.eclipse.ui.texteditor.IDocumentProvider, org.eclipse.ui.IEditorInput)
	 */
	public void disconnect(IDocumentProvider provider,
			IEditorInput documentKey) {
		try {
			super.disconnect(provider, documentKey);
		} finally {
			if (connectionCount > 0)
				connectionCount--;
			if (connectionCount == 0) {
				provider.removeElementStateListener(this);
			}
		}
	}

	/**
	 * Return whether the element is connected to a shared document.
	 * @return whether the element is connected to a shared document
	 */
	public boolean isConnected() {
		return connectionCount > 0;
	}
	
	/**
	 * Save the shared document of the element of this adapter.
	 * @param input the document key of the element.
	 * @param monitor a progress monitor
	 * @return whether the save succeeded or not
	 * @throws CoreException
	 */
	public boolean saveDocument(IEditorInput input, IProgressMonitor monitor) throws CoreException {
		if (isConnected()) {
			IDocumentProvider provider = SharedDocumentAdapter.getDocumentProvider(input);
			flushDocument(provider, input, provider.getDocument(input), false, monitor);
			return true;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.SharedDocumentAdapter#flushDocument(org.eclipse.ui.texteditor.IDocumentProvider, org.eclipse.ui.IEditorInput, org.eclipse.jface.text.IDocument, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void flushDocument(IDocumentProvider provider,
			IEditorInput documentKey, IDocument document,
			boolean overwrite, IProgressMonitor monitor)
			throws CoreException {
		super.flushDocument(provider, documentKey, document, overwrite, monitor);
		// After flushing, fire a content change event
		// TODO: Causes an overflow
		// this.element.fireContentChanged();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IElementStateListener#elementContentAboutToBeReplaced(java.lang.Object)
	 */
	public void elementContentAboutToBeReplaced(Object element) {
		// Nothing to do
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IElementStateListener#elementContentReplaced(java.lang.Object)
	 */
	public void elementContentReplaced(Object element) {
		// Nothing to do
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IElementStateListener#elementDeleted(java.lang.Object)
	 */
	public void elementDeleted(Object element) {
		this.element.update();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IElementStateListener#elementDirtyStateChanged(java.lang.Object, boolean)
	 */
	public void elementDirtyStateChanged(Object element, boolean isDirty) {
		if (!isDirty) {
			this.element.updateTimestamp();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IElementStateListener#elementMoved(java.lang.Object, java.lang.Object)
	 */
	public void elementMoved(Object originalElement, Object movedElement) {
		// Nothing to do
	}
}