/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.viewers.Viewer;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.IViewerCreator;


/**
 * A factory object for the <code>TextMergeViewer</code>.
 * This indirection is necessary because only objects with a default
 * constructor can be created via an extension point
 * (this precludes Viewers).
 */
public class TextViewerCreator implements IViewerCreator {

	public Viewer createViewer(Composite parent, CompareConfiguration mp) {
		return new SimpleTextViewer(parent);
	}
}