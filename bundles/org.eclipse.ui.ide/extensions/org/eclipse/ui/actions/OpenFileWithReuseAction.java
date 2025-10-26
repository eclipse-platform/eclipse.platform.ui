/*******************************************************************************
 * Copyright (c) 2025 Vegard IT GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Sebastian Thomschke (Vegard IT GmbH) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.ide.DialogUtil;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Open action that optionally reuses a single editor for opens originating from
 * the Project Explorer, similar to the Search view behavior. When the feature
 * is disabled or outside Project Explorer, behavior falls back to
 * {@link OpenFileAction}.
 *
 * Scope is currently limited to Project Explorer via the creating part id
 * passed in the constructor. Reuse stops when the editor becomes dirty, is
 * pinned, closed, or when the resolved editor id differs from the currently
 * reused editor id.
 *
 * This class is intentionally package-public in org.eclipse.ui.actions to be a
 * drop-in replacement for {@link OpenFileAction} in CNF wiring.
 *
 * @since 3.23
 */
public class OpenFileWithReuseAction extends OpenFileAction {

	private final String hostPartId;
	private IEditorReference reusedRef;
	private boolean disableReuseForRun;

	/**
	 * @param page       workbench page
	 * @param hostPartId id of the part creating this action (used to scope to
	 *                   Project Explorer)
	 */
	public OpenFileWithReuseAction(final IWorkbenchPage page, final String hostPartId) {
		this(page, null, hostPartId);
	}

	/**
	 * @param page       workbench page
	 * @param descriptor editor descriptor to use (or null for default)
	 * @param hostPartId id of the part creating this action (used to scope to
	 *                   Project Explorer)
	 */
	public OpenFileWithReuseAction(final IWorkbenchPage page, final IEditorDescriptor descriptor,
			final String hostPartId) {
		super(page, descriptor);
		this.hostPartId = hostPartId;
	}

	@Override
	public void run() {
		// Disable reuse for multi-file selection to preserve baseline behavior
		int fileCount = 0;
		for (final IResource res : getSelectedResources()) {
			if (res instanceof IFile) {
				fileCount++;
				if (fileCount > 1) {
					break;
				}
			}
		}
		disableReuseForRun = fileCount > 1;
		try {
			super.run();
		} finally {
			disableReuseForRun = false;
		}
	}

	@Override
	void openFile(final IFile file) {
		if (disableReuseForRun || !shouldApplyReuse()) {
			// Delegate to baseline behavior
			super.openFile(file);
			return;
		}

		final IWorkbenchPage page = getWorkbenchPage();
		final boolean activate = OpenStrategy.activateOnOpen();

		// If already open, just bring to top/activate
		final IEditorPart existing = page.findEditor(new FileEditorInput(file));
		if (existing != null) {
			if (activate) {
				page.activate(existing);
			} else {
				page.bringToTop(existing);
			}
			return;
		}

		// Determine target editor id (explicit),
		// falling back to external system editor id
		String editorId = IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID;
		IEditorDescriptor desc = null;
		try {
			desc = IDE.getEditorDescriptor(file, true, true);
		} catch (PartInitException e) {
			// ignore here; will fall back to system external editor id
		}
		if (desc != null) {
			editorId = desc.getId();
		}

		// Do not attempt reuse for external editors
		if (IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID.equals(editorId)) {
			super.openFile(file);
			return;
		}

		// Try reuse if we have a valid reference
		if (reusedRef != null) {
			final boolean open = reusedRef.getEditor(false) != null;
			final boolean valid = open && !reusedRef.isDirty() && !reusedRef.isPinned();
			if (valid) {
				if (!editorId.equals(reusedRef.getId())) {
					// Different editor type needed; close old reusable editor
					page.closeEditors(new IEditorReference[] { reusedRef }, false);
					reusedRef = null;
				} else {
					final IEditorPart part = reusedRef.getEditor(true);
					if (part instanceof IReusableEditor reusableEditor) {
						reusableEditor.setInput(new FileEditorInput(file));
						if (activate) {
							page.activate(part);
						} else {
							page.bringToTop(part);
						}
						return;
					}
					// Not reusable after all
					reusedRef = null;
				}
			} else {
				// Reference no longer valid
				reusedRef = null;
			}
		}

		// Open a new editor and remember it if reusable
		try {
			final IEditorPart opened = IDE.openEditor(page, file, editorId, activate);
			if (opened instanceof IReusableEditor) {
				final IWorkbenchPartReference ref = page.getReference(opened);
				if (ref instanceof IEditorReference editorRef) {
					reusedRef = editorRef;
				} else {
					reusedRef = null;
				}
			} else {
				reusedRef = null;
			}
		} catch (final PartInitException ex) {
			DialogUtil.openError(page.getWorkbenchWindow().getShell(),
					IDEWorkbenchMessages.OpenFileAction_openFileShellTitle, ex.getMessage(), ex);
		}
	}

	private boolean shouldApplyReuse() {
		// Strictly scope to Project Explorer and opt-in preference
		if (hostPartId == null || !hostPartId.startsWith(IPageLayout.ID_PROJECT_EXPLORER)) {
			return false;
		}

		return PrefUtil.getInternalPreferenceStore().getBoolean(IPreferenceConstants.REUSE_LAST_OPENED_EDITOR);
	}
}
