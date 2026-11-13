/*******************************************************************************
 * Copyright (c) 2000, 2026 IBM Corporation and others.
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
package org.eclipse.search.internal.ui.text;

import java.util.Map;
import java.util.Objects;

import org.eclipse.swt.widgets.Display;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;

import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.ui.NewSearchUI;

public class EditorOpener {

	/**
	 * Family of the background jobs that create and delete the temporary search
	 * markers. It lets tests wait for these jobs.
	 */
	public static final Object MARKER_JOB_FAMILY = new Object();

	/**
	 * Lets the marker requests run one after the other, so that at most one of them
	 * waits for the workspace lock. Static, because openers can be created per
	 * call.
	 */
	private static final ISchedulingRule MARKER_RULE = new ISchedulingRule() {
		@Override
		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}

		@Override
		public boolean isConflicting(ISchedulingRule rule) {
			return rule == this;
		}
	};

	private IEditorReference fReusedEditor;

	/**
	 * The latest request that goes through a temporary marker, shared by all
	 * openers so that only the latest click counts; only accessed in the UI thread.
	 */
	private static MarkerRequest fgMarkerRequest;

	public IEditorPart open(IWorkbenchPage wbPage, IFile file, boolean activate) throws PartInitException {
		cancelMarkerRequest();
		if (NewSearchUI.reuseEditor()) {
			return showWithReuse(file, wbPage, getEditorID(file), activate);
		}
		return showWithoutReuse(file, wbPage, getEditorID(file), activate);
	}

	/**
	 * Opens the file and selects the given range. For editors that neither are nor
	 * adapt to {@link ITextEditor}, the range is selected asynchronously through a
	 * temporary marker, so that the UI thread does not wait for the workspace lock.
	 */
	public IEditorPart openAndSelect(IWorkbenchPage wbPage, IFile file, int offset, int length, boolean activate) throws PartInitException {
		cancelMarkerRequest();
		String editorId= null;
		IEditorDescriptor desc = IDE.getEditorDescriptor(file, true, true);
		if (desc == null || !desc.isInternal()) {
			editorId= "org.eclipse.ui.DefaultTextEditor"; //$NON-NLS-1$
		} else {
			editorId= desc.getId();
		}

		IEditorPart editor;
		if (NewSearchUI.reuseEditor()) {
			editor= showWithReuse(file, wbPage, editorId, activate);
		} else {
			editor= showWithoutReuse(file, wbPage, editorId, activate);
		}

		if (editor == null) {
			return null;
		}
		ITextEditor textEditor = Adapters.adapt(editor, ITextEditor.class);
		if (textEditor != null) {
			textEditor.selectAndReveal(offset, length);
		} else {
			showWithMarker(wbPage, editor, file, offset, length);
		}
		return editor;
	}

	private IEditorPart showWithoutReuse(IFile file, IWorkbenchPage wbPage, String editorID, boolean activate) throws PartInitException {
		return IDE.openEditor(wbPage, file, editorID, activate);
	}


	private String getEditorID(IFile file) throws PartInitException {
		IEditorDescriptor desc = IDE.getEditorDescriptor(file, true, true);
		if (desc == null) {
			return PlatformUI.getWorkbench().getEditorRegistry().findEditor(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID)
					.getId();
		}
		return desc.getId();
	}

	private IEditorPart showWithReuse(IFile file, IWorkbenchPage page, String editorId, boolean activate) throws PartInitException {
		IEditorInput input= new FileEditorInput(file);
		IEditorPart editor= page.findEditor(input);
		if (editor != null) {
			page.bringToTop(editor);
			if (activate) {
				page.activate(editor);
			}
			return editor;
		}
		IEditorReference reusedEditorRef= fReusedEditor;
		if (reusedEditorRef !=  null) {
			boolean isOpen= reusedEditorRef.getEditor(false) != null;
			boolean canBeReused= isOpen && !reusedEditorRef.isDirty() && !reusedEditorRef.isPinned();
			if (canBeReused) {
				boolean showsSameInputType= reusedEditorRef.getId().equals(editorId);
				if (!showsSameInputType) {
					page.closeEditors(new IEditorReference[] { reusedEditorRef }, false);
					fReusedEditor= null;
				} else {
					editor= reusedEditorRef.getEditor(true);
					if (editor instanceof IReusableEditor) {
						((IReusableEditor) editor).setInput(input);
						page.bringToTop(editor);
						if (activate) {
							page.activate(editor);
						}
						return editor;
					}
				}
			}
		}
		editor= page.openEditor(input, editorId, activate);
		if (editor instanceof IReusableEditor) {
			IEditorReference reference= (IEditorReference) page.getReference(editor);
			fReusedEditor= reference;
		} else {
			fReusedEditor= null;
		}
		return editor;
	}

	private void showWithMarker(IWorkbenchPage page, IEditorPart editor, IFile file, int offset, int length) {
		MarkerRequest request = new MarkerRequest(page, editor, file, offset, length);
		fgMarkerRequest = request;
		request.schedule();
	}

	private static void cancelMarkerRequest() {
		if (fgMarkerRequest != null) {
			fgMarkerRequest.cancel();
			fgMarkerRequest = null;
		}
	}

	/**
	 * Creates the temporary marker in the background, reveals it in the editor in
	 * the UI thread and deletes it in the background again.
	 */
	private final class MarkerRequest extends Job {

		private final Display fDisplay;
		private final IWorkbenchPage fPage;
		private final IEditorPart fEditor;
		private final IEditorInput fInput;
		private final IFile fFile;
		private final int fOffset;
		private final int fLength;

		MarkerRequest(IWorkbenchPage page, IEditorPart editor, IFile file, int offset, int length) {
			super(SearchMessages.EditorOpener_selectMatch_jobName);
			fDisplay = Display.getCurrent();
			fPage = page;
			fEditor = editor;
			fInput = editor.getEditorInput();
			fFile = file;
			fOffset = offset;
			fLength = length;
			setSystem(true);
			setRule(MARKER_RULE);
		}

		@Override
		public boolean belongsTo(Object family) {
			return family == MARKER_JOB_FAMILY;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if (monitor.isCanceled()) {
				clearIfLatest();
				return Status.CANCEL_STATUS;
			}
			IMarker marker;
			try {
				marker = fFile.createMarker(NewSearchUI.SEARCH_MARKER, Map.of(IMarker.CHAR_START,
						Integer.valueOf(fOffset), IMarker.CHAR_END, Integer.valueOf(fOffset + fLength)));
			} catch (CoreException e) {
				SearchPlugin.log(
						new Status(IStatus.ERROR, SearchPlugin.getID(), SearchMessages.FileSearchPage_error_marker, e));
				clearIfLatest();
				return Status.OK_STATUS;
			}
			if (monitor.isCanceled() || fDisplay == null || fDisplay.isDisposed()) {
				deleteMarkerNow(marker);
				clearIfLatest();
				return Status.CANCEL_STATUS;
			}
			try {
				fDisplay.asyncExec(() -> {
					boolean latest = isLatestRequest();
					if (latest) {
						fgMarkerRequest = null;
					}
					try {
						if (latest && isEditorStillShowingInput()) {
							IDE.gotoMarker(fEditor, marker);
						}
					} finally {
						deleteMarker(marker);
					}
				});
			} catch (RuntimeException e) {
				// display disposed in the meantime
				deleteMarkerNow(marker);
			}
			return Status.OK_STATUS;
		}

		/** Clears the latest request in the UI thread if it still is this one. */
		private void clearIfLatest() {
			if (fDisplay == null || fDisplay.isDisposed()) {
				return;
			}
			try {
				fDisplay.asyncExec(() -> {
					if (isLatestRequest()) {
						fgMarkerRequest = null;
					}
				});
			} catch (RuntimeException e) {
				// display disposed in the meantime
			}
		}

		private boolean isLatestRequest() {
			return fgMarkerRequest == this;
		}

		private boolean isEditorStillShowingInput() {
			return isOpen() && Objects.equals(fInput, fEditor.getEditorInput());
		}

		private boolean isOpen() {
			for (IEditorReference reference : fPage.getEditorReferences()) {
				if (reference.getEditor(false) == fEditor) {
					return true;
				}
			}
			return false;
		}
	}

	private static void deleteMarkerNow(IMarker marker) {
		try {
			marker.delete();
		} catch (CoreException e) {
			// the resource is gone already
		}
	}

	private static void deleteMarker(IMarker marker) {
		Job deleteJob = new Job(SearchMessages.EditorOpener_deleteMarker_jobName) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				deleteMarkerNow(marker);
				return Status.OK_STATUS;
			}

			@Override
			public boolean belongsTo(Object family) {
				return family == MARKER_JOB_FAMILY;
			}
		};
		deleteJob.setSystem(true);
		deleteJob.schedule();
	}

}
