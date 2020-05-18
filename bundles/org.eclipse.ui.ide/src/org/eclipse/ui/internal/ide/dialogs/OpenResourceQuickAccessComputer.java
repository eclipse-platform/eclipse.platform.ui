/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ui.internal.ide.dialogs;

import java.util.LinkedHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SearchPattern;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.quickaccess.IQuickAccessComputer;
import org.eclipse.ui.quickaccess.IQuickAccessComputerExtension;
import org.eclipse.ui.quickaccess.QuickAccessElement;

/**
 * @since 3.16.100
 */
public class OpenResourceQuickAccessComputer implements IQuickAccessComputer, IQuickAccessComputerExtension {

	private static final long TIMEOUT_MS = 200;

	@Override
	public QuickAccessElement[] computeElements(String query, IProgressMonitor monitor) {
		SearchPattern searchPattern = new SearchPattern();
		searchPattern.setPattern(query);

		LinkedHashMap<String, IFile> res = new LinkedHashMap<>();
		long startTime = System.currentTimeMillis();
		try {
			ResourcesPlugin.getWorkspace().getRoot().accept(resourceProxy -> {
				if (resourceProxy.isDerived() || !resourceProxy.isAccessible()) {
					return false;
				}

				if (resourceProxy.getType() == IResource.FILE) {
					String name = resourceProxy.getName();
					if (searchPattern.matches(name)) {
						IFile file = (IFile) resourceProxy.requestResource();
						if (!res.containsKey(name)) {
							res.put(name, file);
						} else {
							IFile previousFile = res.get(name);
							if (previousFile.getFullPath().segmentCount() > file.getFullPath().segmentCount()) {
								// take file pointer that's the "less deep" inside project
								res.put(file.getName(), file);
							}
						}
					}
				}
				return !monitor.isCanceled() && System.currentTimeMillis() - startTime < TIMEOUT_MS;
			}, IResource.NONE);
		} catch (CoreException e) {
			IDEWorkbenchPlugin.log(e.getMessage(), e);
		}
		WorkbenchLabelProvider labelProvider = new WorkbenchLabelProvider();
		QuickAccessElement[] proposals = res.values().stream() //
				.map(file -> new ResourceElement(labelProvider, file)) //
				.toArray(QuickAccessElement[]::new);
		labelProvider.dispose();
		return proposals;
	}

	@Override
	public QuickAccessElement[] computeElements() {
		return new QuickAccessElement[0];
	}

	@Override
	public void resetState() {
		// stateless, nothing to do
	}

	@Override
	public boolean needsRefresh() {
		return false;
	}

	private static class ResourceElement extends QuickAccessElement {
		private final WorkbenchLabelProvider fLabelProvider;
		private final IFile fFile;

		private ResourceElement(WorkbenchLabelProvider labelProvider, IFile resource) {
			fLabelProvider = labelProvider;
			fFile = resource;
		}

		@Override
		public String getLabel() {
			return fLabelProvider.getText(fFile);
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return ImageDescriptor.createFromImageDataProvider(zoom -> fLabelProvider.getImage(fFile).getImageData());
		}

		@Override
		public String getId() {
			return fFile.getFullPath().toString();
		}

		@Override
		public void execute() {
			try {
				IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), fFile);
			} catch (PartInitException e) {
				IDEWorkbenchPlugin.log(e.getMessage(), e);
			}
		}
	}
}
