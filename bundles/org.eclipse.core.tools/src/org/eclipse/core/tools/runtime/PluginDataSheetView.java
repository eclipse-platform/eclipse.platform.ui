/**********************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tools.runtime;

import java.util.ArrayList;
import java.util.Iterator;
import org.eclipse.core.internal.registry.Extension;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.tools.BaseTextView;
import org.eclipse.core.tools.DeepSize;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.framework.stats.ClassloaderStats;
import org.eclipse.osgi.framework.stats.ResourceBundleStats;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

/**
 * View displaying information about the resources for a given plugin
 *
 */
public class PluginDataSheetView extends BaseTextView implements ISelectionListener {

	/** The id by which this view is known in the plug-in registry */
	public static final String VIEW_ID = PluginDataSheetView.class.getName();

	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		getViewSite().getPage().addSelectionListener(this);
	}

	public void dispose() {
		getViewSite().getPage().removeSelectionListener(this);
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			Object first = ((IStructuredSelection) selection).getFirstElement();
			if (first != null && first instanceof IPluginDescriptor) {
				viewer.getDocument().set(printStats((IPluginDescriptor) first));
				viewer.refresh();
			}
		}
	}

	private String printStats(IPluginDescriptor descriptor) {
		StringBuffer result = new StringBuffer(200);
		ClassloaderStats classloader = ClassloaderStats.getLoader(descriptor.getUniqueIdentifier());
		printResourceBundleStats(result, classloader == null ? null : classloader.getBundles(), descriptor);
		result.append('\n');
		printExtensionLoadingStats(result, descriptor.getExtensions());
		return result.toString();
	}

	private void printExtensionLoadingStats(StringBuffer result, IExtension[] extensions) {
		if (extensions.length == 0) {
			result.append("No extensions contributed by this plug-in\n"); //$NON-NLS-1$
			return;
		}
		result.append("Extension loading stats:\n"); //$NON-NLS-1$
		for (int i = 0; i < extensions.length; i++) {
			Extension extension = (Extension) extensions[i];
			result.append('\t');
			result.append(extension.getExtensionPointUniqueIdentifier());
			result.append(" <- "); //$NON-NLS-1$
			result.append(extension.getUniqueIdentifier());
			if (!extension.isFullyLoaded())
				result.append(" (not loaded yet)"); //$NON-NLS-1$
			result.append('\n');
		}
	}

	private void printResourceBundleStats(StringBuffer result, ArrayList bundles, IPluginDescriptor descriptor) {
		if (bundles == null || bundles.size() == 0) {
			result.append("No resources loaded by this plug-in\n"); //$NON-NLS-1$
			return;
		}
		result.append("Resource bundles stats:\n"); //$NON-NLS-1$
		for (Iterator iterator = bundles.iterator(); iterator.hasNext();) {
			ResourceBundleStats bundle = (ResourceBundleStats) iterator.next();
			result.append('\t');
			result.append(bundle.getFileName());
			result.append("\tElements: #" + bundle.getKeyCount()); //$NON-NLS-1$
			long totalSize;
			// if hashsize == 0, we should compute the total size using DeepSize
			if (bundle.getHashSize() == 0) {
				DeepSize.reset();
				DeepSize calculator = new DeepSize();
				calculator.deepSize(descriptor.getResourceBundle());
				totalSize = calculator.getSize();
			} else
				totalSize = bundle.getTotalSize();
			result.append(" \ttotal: " + totalSize); //$NON-NLS-1$
			result.append("b \tkeys: " + bundle.getKeySize()); //$NON-NLS-1$
			result.append("b \tvalues: " + bundle.getValueSize() + "b\n"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

}