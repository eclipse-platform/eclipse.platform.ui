/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.ui.dialogs;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IFileEditorMapping;

/**
 * A label provider for displaying of <code>IFileEditorMapping</code> objects in
 * viewers.
 * <p>
 * This class has a singleton instance,
 * <code>FileEditorMappingLabelProvider.INSTANCE</code>, which can be used any
 * place this kind of label provider is needed.
 * </p>
 * <p>
 * The singleton instance hangs on to images, which get freed up when
 * <code>dispose</code> is called.
 * </p>
 *
 * @see org.eclipse.jface.viewers.ILabelProvider
 */
public class FileEditorMappingLabelProvider extends LabelProvider implements ITableLabelProvider {

	/**
	 * Singleton instance accessor.
	 */
	public static final FileEditorMappingLabelProvider INSTANCE = new FileEditorMappingLabelProvider();

	/**
	 * Images that will require disposal.
	 */
	private List<Image> imagesToDispose = new ArrayList<>();

	/**
	 * Creates an instance of this class. The private visibility of this constructor
	 * ensures that this class is only usable as a singleton.
	 */
	private FileEditorMappingLabelProvider() {
		super();
	}

	@Override
	public void dispose() {
		super.dispose();
		for (Image e : imagesToDispose) {
			e.dispose();
		}
		imagesToDispose.clear();
	}

	/**
	 * The <code>ResourceTypeEditorMappingLabelProvider</code> implementation of
	 * this <code>ITableLabelProvider</code> method creates and returns an new
	 * image. The image is remembered internally and will be deallocated by
	 * <code>dispose</code>.
	 */
	@Override
	public Image getColumnImage(Object element, int row) {
		return getImage(element);
	}

	@Override
	public String getColumnText(Object element, int row) {
		return getText(element);
	}

	/**
	 * The <code>ResourceTypeEditorMappingLabelProvider</code> implementation of
	 * this <code>ILabelProvider</code> method creates and returns an new image. The
	 * image is remembered internally and will be deallocated by
	 * <code>dispose</code>.
	 */
	@Override
	public Image getImage(Object element) {
		if (element instanceof IFileEditorMapping) {
			Image image = ((IFileEditorMapping) element).getImageDescriptor().createImage();
			imagesToDispose.add(image);
			return image;
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof IFileEditorMapping) {
			return TextProcessor.process(((IFileEditorMapping) element).getLabel(), "*."); //$NON-NLS-1$
		}

		return null;
	}
}
