/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * A tree viewer that obtains labels and images asyncronously with underlying
 * elements.
 */
public class AsyncTreeViewer extends TreeViewer implements Runnable {
	
	/**
	 * Tables of elements to current & previous labels/images
	 */
	private Hashtable fLabels = new Hashtable(10);
	private Hashtable fPrevLabels = new Hashtable(10);
	private Hashtable fImages = new Hashtable(10);
	private Hashtable fPrevImages = new Hashtable(10);
	
	/**
	 * Thread used to get labels in the background
	 */
	private Thread fThread;
	
	/**
	 * Queue of elements to retrieve labels for
	 */
	private Vector fQueue = new Vector(10);
	
	/**
	 * Disposed flag
	 */
	private boolean fDisposed = false;
	
	/**
	 * Number of elements to process before updating UI
	 */
	private static final int BATCH_SIZE = 10;
	
	/**
	 * @param parent
	 */
	public AsyncTreeViewer(Composite parent) {
		this(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
	}

	/**
	 * @param parent
	 * @param style
	 */
	public AsyncTreeViewer(Composite parent, int style) {
		this(new Tree(parent, style));
	}

	/**
	 * @param tree
	 */
	public AsyncTreeViewer(Tree tree) {
		super(tree);
		setUseHashlookup(true);
		getControl().addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				dispose();
			}
		});
	}

	private void dispose() {
		fDisposed = true;
		synchronized (fQueue) {
			fQueue.notifyAll();
		}
	}
	/**
	 * Queues the element & item for label/image update
	 */
	protected void queueElement(Object element) {
		if (!fQueue.contains(element)) {
			// retrieve label
			fQueue.add(element);
			if (fThread == null) {
				fThread = new Thread(this, "Async label provider"); //$NON-NLS-1$
				fThread.start();
			}			
			synchronized (fQueue) {
				fQueue.notifyAll();
			}
		}		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.StructuredViewer#unmapElement(java.lang.Object)
	 */
	protected void unmapElement(Object element) {
		super.unmapElement(element);
		fQueue.remove(element);
		fLabels.remove(element);
		fPrevLabels.remove(element);
		fImages.remove(element);
		fPrevImages.remove(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#doUpdateItem(org.eclipse.swt.widgets.Item, java.lang.Object)
	 */
	protected void doUpdateItem(Item item, Object element) {
		// update icon and label
		
		queueElement(element);
		item.setText(getCurrentLabel(element));
		Image image = getCurrentImage(element);
		if (item.getImage() != image) {
			item.setImage(image);
		}
		
		ILabelProvider provider = (ILabelProvider) getLabelProvider();
		if (provider instanceof IColorProvider) {
			IColorProvider cp = (IColorProvider) provider;
			TreeItem treeItem = (TreeItem) item;
			treeItem.setForeground(cp.getForeground(element));
			treeItem.setBackground(cp.getBackground(element));
		}
	}
	
	private String getCurrentLabel(Object element) {
		String label = (String)fLabels.get(element);
		if (label != null) {
			return label;
		}
		label = (String)fPrevLabels.get(element);
		if (label != null) {
			return label;
		}
		return ""; //$NON-NLS-1$
	}
	
	private Image getCurrentImage(Object element) {
		Image image= (Image)fImages.get(element);
		if (image != null) {
			return image;
		}
		image = (Image)fPrevImages.get(element);
		if (image!= null) {
			return image;
		}
		return null;
	}	
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while (!isDisposed()) {
			if (fQueue.isEmpty()) {
				synchronized (fQueue) {
					try {
						fQueue.wait();
					} catch (InterruptedException e) {
					}
				}
			}
			List elements = new ArrayList(BATCH_SIZE);
			int count = 0;
			while (!isDisposed() && !fQueue.isEmpty() && count < BATCH_SIZE) {
				Object element = fQueue.remove(0);
				ILabelProvider provider = (ILabelProvider)getLabelProvider(); 
				String label = provider.getText(element);
				if (label != null) {
					Object prev = fLabels.get(element);
					if (prev != null) {
						fPrevLabels.put(element, prev); 
					}
					fLabels.put(element, label);
					elements.add(element);
					Image image = provider.getImage(element);
					if (image != null) {
						prev = fImages.get(element);
						if (prev != null) {
							fPrevImages.put(element, prev);
						}
						fImages.put(element, image);
					}					
				}
				count++;
			}
			updateItems(elements.toArray());
		}
		
		// clean up on exit
		fLabels = null;
		fPrevLabels = null;
		fImages = null;
		fPrevImages = null;
	}	
	
	private void updateItems(final Object[] elements) {
		if (!isDisposed()) {
			Runnable r = new Runnable() {
				public void run() {
					if (!isDisposed()) {
						for (int i = 0; i < elements.length; i++) {
							Object object = elements[i];
							TreeItem item = (TreeItem)findItem(object);
							if (item != null) {
								String text = (String)fLabels.get(object);
								if (text != null) {
									item.setText(text);
								}
								Image image = (Image)fImages.get(object);
								if (item.getImage() != image) {
									item.setImage(image);
								}
							}
						}
					}
				}
			};
			getTree().getDisplay().asyncExec(r);
		}
	}	
	
	private boolean isDisposed() {
		return fDisposed;
	}
}
