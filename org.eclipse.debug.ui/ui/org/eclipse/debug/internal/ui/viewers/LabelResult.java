/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers;

import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.graphics.Image;


/**
 * @since 3.2
 *
 */
class LabelResult extends LabelRequestMonitor implements ILabelResult {

	public LabelResult(ModelNode node, AsynchronousModel model) {
		super(node, model);
	}
	
	protected synchronized void scheduleViewerUpdate(long ms) {
		notifyAll();
	}
	
	public synchronized boolean isDone() {
		return super.isDone();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.ILabelResult#getElement()
	 */
	public Object getElement() {
		return getNode().getElement();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.ILabelResult#getImages()
	 */
	public Image[] getImages() {
		return getModel().getViewer().getImages(getImageDescriptors());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.ILabelResult#getLabels()
	 */
	public String[] getLabels() {
		return super.getLabels();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.ILabelResult#getTreePath()
	 */
	public TreePath getTreePath() {
		return getNode().getTreePath();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.ILabelResult#getDepth()
	 */
	public int getDepth() {
		int level = 0;
		ModelNode node = getNode().getParentNode();
		while (node != null) {
			node = node.getParentNode();
			level++;
		}
		return level;
	}

	
}
