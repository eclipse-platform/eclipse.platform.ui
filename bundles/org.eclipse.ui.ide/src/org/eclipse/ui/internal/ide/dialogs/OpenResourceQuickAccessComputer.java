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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
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
		List<QuickAccessElement> res = new ArrayList<>();
		long startTime = System.currentTimeMillis();
		WorkbenchLabelProvider labelProvider = new WorkbenchLabelProvider();
		try {
			ResourcesPlugin.getWorkspace().getRoot().accept(resource -> {
				if (resource.getType() == IResource.FILE) {
					res.add(new QuickAccessElement() {
						@Override
						public String getLabel() {
							return labelProvider.getText(resource);
						}

						@Override
						public ImageDescriptor getImageDescriptor() {
							ImageData imageData = labelProvider.getImage(resource).getImageData();
							return ImageDescriptor.createFromImageDataProvider(zoom -> imageData);
						}

						@Override
						public String getId() {
							return resource.getFullPath().toString();
						}

						@Override
						public void execute() {
							try {
								IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
										(IFile) resource);
							} catch (PartInitException e) {
								IDEWorkbenchPlugin.log(e.getMessage(), e);
							}
						}
					});
				}
				return !monitor.isCanceled() && System.currentTimeMillis() - startTime < TIMEOUT_MS;
			});
		} catch (CoreException e) {
			IDEWorkbenchPlugin.log(e.getMessage(), e);
		}
		labelProvider.dispose();
		return res.toArray(new QuickAccessElement[res.size()]);
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

}
