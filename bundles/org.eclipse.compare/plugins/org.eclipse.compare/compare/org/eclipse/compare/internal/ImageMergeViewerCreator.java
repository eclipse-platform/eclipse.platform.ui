/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.compare.*;
import org.eclipse.jface.viewers.Viewer;

/**
 * A factory object for the <code>ImageMergeViewer</code>.
 * This indirection is necessary because only objects with a default
 * constructor can be created via an extension point
 * (this precludes Viewers).
 */
public class ImageMergeViewerCreator implements IViewerCreator {

	public Viewer createViewer(Composite parent, CompareConfiguration mp) {
		return new ImageMergeViewer(parent, SWT.NULL, mp);
	}
}
