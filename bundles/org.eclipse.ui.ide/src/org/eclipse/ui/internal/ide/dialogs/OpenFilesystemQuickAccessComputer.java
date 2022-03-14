/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ui.internal.ide.dialogs;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.dialogs.OpenResourceQuickAccessComputer.ResourceElement;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.quickaccess.IQuickAccessComputerExtension;
import org.eclipse.ui.quickaccess.QuickAccessElement;

/**
 * @since 3.18.600
 */
public class OpenFilesystemQuickAccessComputer implements IQuickAccessComputerExtension {

	private class FileElement extends QuickAccessElement {

		private File file;

		public FileElement(File file) {
			this.file = file;
		}

		@Override
		public String getLabel() {
			return file.getAbsolutePath();
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return null;
		}

		@Override
		public String getId() {
			return file.getAbsolutePath();
		}

		@Override
		public void execute() {
			new UIJob(getLabel()) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					try {
						IDE.openEditorOnFileStore(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
								EFS.getStore(file.toURI()));
					} catch (CoreException e) {
						return new Status(IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH, e.getMessage(), e);
					}
					return Status.OK_STATUS;
				}
			}.schedule();
		}

	}

	private static final QuickAccessElement[] NOTHING = new QuickAccessElement[0];

	@Override
	public QuickAccessElement[] computeElements() {
		return NOTHING;
	}

	@Override
	public void resetState() {
		// Nothing to do
	}

	@Override
	public boolean needsRefresh() {
		return true;
	}

	@Override
	public QuickAccessElement[] computeElements(String query, IProgressMonitor monitor) {
		File file = new File(query);
		if (file.isFile() && file.canRead()) {
			return Arrays.stream(ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(file.toURI())) //
					.sorted(Comparator.comparingInt(resource -> resource.getFullPath().segmentCount())) //
					.findFirst() //
					.map(resource -> (QuickAccessElement) new ResourceElement(new WorkbenchLabelProvider(), resource)) //
					.or(() -> Optional.of(new FileElement(file))) //
					.map(element -> new QuickAccessElement[] { element }) //
					.orElse(NOTHING);
		}
		return NOTHING;
	}

}
