/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.viewers;

/**
 * TreeViewerLabelProvider is the ViewerLabelProvider that handles TreePaths.
 *
 * @since 3.3
 */
public class TreeColumnViewerLabelProvider extends
		TableColumnViewerLabelProvider {
	private ITreePathLabelProvider treePathProvider = new ITreePathLabelProvider() {

		@Override
		public void updateLabel(ViewerLabel label, TreePath elementPath) {
			// Do nothing by default

		}

		@Override
		public void dispose() {
			// Do nothing by default

		}

		@Override
		public void addListener(ILabelProviderListener listener) {
			// Do nothing by default

		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
			// Do nothing by default

		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

	};

	/**
	 * Create a new instance of the receiver with the supplied labelProvider.
	 *
	 * @param labelProvider the label provider
	 */
	public TreeColumnViewerLabelProvider(IBaseLabelProvider labelProvider) {
		super(labelProvider);
	}

	/**
	 * Update the label for the element with TreePath.
	 *
	 * @param label       the label to update
	 * @param elementPath the path of the element being decorated
	 */
	public void updateLabel(ViewerLabel label, TreePath elementPath) {
		treePathProvider.updateLabel(label, elementPath);

	}

	@Override
	public void setProviders(Object provider) {
		super.setProviders(provider);
		if (provider instanceof ITreePathLabelProvider)
			treePathProvider = (ITreePathLabelProvider) provider;
	}

	/**
	 * Return the ITreePathLabelProvider for the receiver.
	 *
	 * @return Returns the treePathProvider.
	 */
	public ITreePathLabelProvider getTreePathProvider() {
		return treePathProvider;
	}

}
