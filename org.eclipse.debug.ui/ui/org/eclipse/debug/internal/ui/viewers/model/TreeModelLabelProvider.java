/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River - Pawel Piech - Added coalescing of label updates (bug 247575).
 *     Pawel Piech (Wind River) - added support for a virtual tree model viewer (Bug 242489)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelChangedListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDeltaVisitor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;

/**
 * @since 3.3
 */
public class TreeModelLabelProvider extends ColumnLabelProvider 
    implements ITreeModelLabelProvider, IModelChangedListener 
{
	
	private ITreeModelLabelProviderTarget fViewer;
	private List fComplete;
	
	/**
	 * Cache of images used for elements in this label provider. Label updates
	 * use the method <code>getImage(...)</code> to cache images for
	 * image descriptors. The images are disposed with this label provider.
	 */
	private Map fImageCache = new HashMap();

	/**
	 * Cache of the fonts used for elements in this label provider. Label updates
	 * use the method <code>getFont(...)</code> to cache fonts for
	 * FontData objects. The fonts are disposed with this label provider.
	 */
	private Map fFontCache = new HashMap();

	/**
	 * Cache of the colors used for elements in this label provider. Label updates
	 * use the method <code>getColor(...)</code> to cache colors for
	 * RGB values. The colors are disposed with this label provider.
	 */
	private Map fColorCache = new HashMap();
	
	/**
	 * Label listeners
	 */
	private ListenerList fLabelListeners = new ListenerList();
	
	/**
	 * Updates waiting to be sent to the label provider.  The map contains
	 * lists of updates, keyed using the provider. 
     * <p>
     * Note: this variable should only be accessed inside a synchronized section
     * using the enclosing label provider instance.
     * </p>
	 */
	private Map fPendingUpdates = new HashMap();
	
	/**
	 * A job that will send the label update requests.
	 * This variable allows the job to be canceled and re-scheduled if 
	 * new updates are requested.  
	 * <p>
	 * Note: this variable should only be accessed inside a synchronized section
	 * using the enclosing label provider instance.
	 * </p>
	 */
	private UIJob fPendingUpdatesJob;
	
	/**
	 * List of updates in progress
	 */
	private List fUpdatesInProgress = new ArrayList();
	
    /**
     * Delta visitor actively cancels the outstanding label updates for 
     * elements that are changed and are about to be updated.
     */
    class Visitor implements IModelDeltaVisitor {
        /* (non-Javadoc)
         * @see org.eclipse.debug.internal.ui.viewers.provisional.IModelDeltaVisitor#visit(org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta, int)
         */
        public boolean visit(IModelDelta delta, int depth) {
            if ((delta.getFlags() & IModelDelta.CONTENT) > 0) {
                cancelElementUpdates(delta.getElement(), true);
                return false;
            } else if ((delta.getFlags() & IModelDelta.STATE) > 0) {
                cancelElementUpdates(delta.getElement(), false);
                return true;
            } 
            return true;
        }
    }

    /**
     * Delta visitor
     */
    private Visitor fVisitor = new Visitor();
	
	/**
	 * Constructs a new label provider on the given display
	 */
	public TreeModelLabelProvider(ITreeModelLabelProviderTarget viewer) {
		fViewer = viewer;
		fViewer.addModelChangedListener(this);
	}
	
	/**
	 * Returns an image for the given image descriptor or <code>null</code>. Adds the image
	 * to a cache of images if it does not already exist.
	 * 
	 * @param descriptor image descriptor or <code>null</code>
	 * @return image or <code>null</code>
	 */
	public Image getImage(ImageDescriptor descriptor) {
		if (descriptor == null) {
			return null;
		}
		Image image = (Image) fImageCache.get(descriptor);
		if (image == null) {
			image = new Image(getDisplay(), descriptor.getImageData());
			fImageCache.put(descriptor, image);
		}
		return image;
	}

	/**
	 * Returns the display to use for resource allocation.
	 * 
	 * @return display
	 */
	private Display getDisplay() {
		return fViewer.getDisplay();
	}
	
	/**
	 * Returns a font for the given font data or <code>null</code>. Adds the font to the font 
	 * cache if not yet created.
	 * 
	 * @param fontData font data or <code>null</code>
	 * @return font font or <code>null</code>
	 */
	public Font getFont(FontData fontData) {
		if (fontData == null) {
			return null;
		}
		Font font = (Font) fFontCache.get(fontData);
		if (font == null) {
			font = new Font(getDisplay(), fontData);
			fFontCache.put(fontData, font);
		}
		return font;
	}	
	
	/**
	 * Returns a color for the given RGB or <code>null</code>. Adds the color to the color 
	 * cache if not yet created.
	 * 
	 * @param rgb RGB or <code>null</code>
	 * @return color or <code>null</code>
	 */
	public Color getColor(RGB rgb) {
		if (rgb == null) {
			return null;
		}
		Color color = (Color) fColorCache.get(rgb);
		if (color == null) {
			color = new Color(getDisplay(), rgb);
			fColorCache.put(rgb, color);
		}
		return color;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.BaseLabelProvider#dispose()
	 */
	public void dispose() {
	    fViewer.removeModelChangedListener(this);
		synchronized (fUpdatesInProgress) {
			Iterator updatesInProgress = fUpdatesInProgress.iterator();
			while (updatesInProgress.hasNext()) {
				ILabelUpdate currentUpdate = (ILabelUpdate) updatesInProgress.next();
				currentUpdate.cancel();			
			}
		}
		synchronized (this) {
			if (fPendingUpdatesJob != null) {
				fPendingUpdatesJob.cancel();
				fPendingUpdatesJob = null;
			}
		}
		Iterator images = fImageCache.values().iterator();
		while (images.hasNext()) {
			Image image = (Image) images.next();
			image.dispose();
		}
		fImageCache.clear();
		
		Iterator fonts = fFontCache.values().iterator();
		while (fonts.hasNext()) {
			Font font = (Font) fonts.next();
			font.dispose();
		}
		fFontCache.clear();
		
		Iterator colors = fColorCache.values().iterator();
		while (colors.hasNext()) {
			Color color = (Color) colors.next();
			color.dispose();
		}
		fColorCache.clear();

		super.dispose();
	}

	public synchronized void update(ViewerCell cell) {
		// NOT USED - the viewer updates each row instead 
	}	
	
	public synchronized boolean update(TreePath elementPath) {
	    cancelPathUpdates(elementPath);
	    
		String[] visibleColumns = fViewer.getVisibleColumns();
		Object element = elementPath.getLastSegment();
		IElementLabelProvider presentation = ViewerAdapterService.getLabelProvider(element);
		if (presentation != null) {
		    List updates = (List)fPendingUpdates.get(presentation);
		    if (updates == null) {
		        updates = new LinkedList();
		        fPendingUpdates.put(presentation, updates);
		    }
		    updates.add(new LabelUpdate(fViewer.getInput(), elementPath, this, visibleColumns, fViewer.getPresentationContext()));
		    if (fPendingUpdatesJob != null) {
		    	fPendingUpdatesJob.cancel();
		    }
		    fPendingUpdatesJob = new UIJob(fViewer.getDisplay(), "Schedule Pending Label Updates") { //$NON-NLS-1$
				public IStatus runInUIThread(IProgressMonitor monitor) {
					 startRequests(this);
					 return Status.OK_STATUS;
				}
			};
			fPendingUpdatesJob.setSystem(true);
			fPendingUpdatesJob.schedule();
			return true;
		} else {
		    return false;
		}
	}
	
	/**
     * Cancel any outstanding updates that are running for this element. 
     */
    protected void cancelPathUpdates(TreePath elementPath) {
       synchronized (fUpdatesInProgress) {
            Iterator updatesInProgress = fUpdatesInProgress.iterator();
            while (updatesInProgress.hasNext()) {
                ILabelUpdate currentUpdate = (ILabelUpdate) updatesInProgress.next();
                if (elementPath.equals(currentUpdate.getElementPath())) {
                    currentUpdate.cancel();
                }
            }
        }
    }

    /**
     * Sets the element's display information in the viewer.
     * 
     * @see ITreeModelLabelProviderTarget#setElementData(TreePath, int, String[], ImageDescriptor[], FontData[], RGB[], RGB[])
     * @see ITreeModelCheckProviderTarget#setElementChecked(TreePath, boolean, boolean)
     */
    protected void setElementData(TreePath path, int numColumns, String[] labels, ImageDescriptor[] images,
        FontData[] fontDatas, RGB[] foregrounds, RGB[] backgrounds, boolean checked, boolean grayed) 
    {
        fViewer.setElementData(path, numColumns, labels, images, fontDatas, foregrounds, backgrounds);
        
        if (fViewer instanceof ITreeModelCheckProviderTarget)
            ((ITreeModelCheckProviderTarget) fViewer).setElementChecked(path, checked, grayed);
    }

    
	private void startRequests(UIJob updateJob) {
	    // Avoid calling providers inside a synchronized section.  Instead 
	    // copy the updates map into a new variable. 
	    Map updates = null;
	    synchronized(this) {
	        if (updateJob == fPendingUpdatesJob) {
    	        updates = fPendingUpdates;
    	        fPendingUpdates = new HashMap();
    	        fPendingUpdatesJob = null;
	        }
	    }

	    if (updates != null) {
            for (Iterator itr = updates.keySet().iterator(); itr.hasNext();) {
                IElementLabelProvider presentation = (IElementLabelProvider)itr.next();
                List list = (List)updates.get(presentation);
                for (Iterator listItr = list.iterator(); listItr.hasNext();) {
                    updateStarted((ILabelUpdate)listItr.next());
                }
                presentation.update( (ILabelUpdate[])list.toArray(new ILabelUpdate[list.size()]) );
            }
	    }
	}
	
    /**
    * Cancels all running updates for the given element.  If seachFullPath is true,
    * all updtes will be canceled which have the given element anywhere in their 
    * patch.   
    * @param element element to search for.
    * @param searchFullPath flag whether to look for the element in the full path
    * of the update
    */
   protected void cancelElementUpdates(Object element, boolean searchFullPath) {
       synchronized (fUpdatesInProgress) {
           Iterator updatesInProgress = fUpdatesInProgress.iterator();
             while (updatesInProgress.hasNext()) {
                ILabelUpdate currentUpdate = (ILabelUpdate) updatesInProgress.next();
                
                if (searchFullPath) {
                    if (element.equals(fViewer.getInput())) {
                        currentUpdate.cancel();
                    } else {
                        TreePath updatePath = currentUpdate.getElementPath();
                        for (int i = 0; i < updatePath.getSegmentCount(); i++) {
                            if (element.equals(updatePath.getSegment(i))) {
                                currentUpdate.cancel();
                                break; // Exit the for loop, stay in the while loop
                            }                         
                        }
                    }
                } else {
                    if (element.equals(currentUpdate.getElement())) {
                        currentUpdate.cancel();
                    }
                }
            }
        }
    }

	/**
	 * Returns the presentation context for this label provider.
	 * 
	 * @return presentation context
	 */
	protected IPresentationContext getPresentationContext() {
		return fViewer.getPresentationContext();
	}

    /**
     * A label update is complete.
     * 
     * @param update
     */
    protected synchronized void complete(ILabelUpdate update) {
		if (update.isCanceled()){
			updateComplete(update);
		} else {
			if (fComplete == null) {
				fComplete = new ArrayList();
				UIJob job = new UIJob(getDisplay(), "Label Updates") { //$NON-NLS-1$
					public IStatus runInUIThread(IProgressMonitor monitor) {
						LabelUpdate[] updates = null;
						synchronized (TreeModelLabelProvider.this) {
							updates = (LabelUpdate[]) fComplete.toArray(new LabelUpdate[fComplete.size()]);
							fComplete = null;
						}
						for (int i = 0; i < updates.length; i++) {
							updates[i].update();
						}
						return Status.OK_STATUS;
					}
				};
				job.setSystem(true);
				job.schedule(10L);
			}
			fComplete.add(update);
		}
    }
    
	public void addLabelUpdateListener(ILabelUpdateListener listener) {
		fLabelListeners.add(listener);
	}
	
	public void removeLabelUpdateListener(ILabelUpdateListener listener) {
		fLabelListeners.remove(listener);
	}
	
	/**
	 * Notification an update request has started
	 * 
	 * @param update
	 */
	void updateStarted(ILabelUpdate update) {
		boolean begin = false;
		synchronized (fUpdatesInProgress) {
			begin = fUpdatesInProgress.isEmpty();
			fUpdatesInProgress.add(update);
		}
		if (begin) {
			if (ModelContentProvider.DEBUG_UPDATE_SEQUENCE && ModelContentProvider.DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
				System.out.println("LABEL SEQUENCE BEGINS"); //$NON-NLS-1$
			}
			notifyUpdate(ModelContentProvider.UPDATE_SEQUENCE_BEGINS, null);
		}
        if (ModelContentProvider.DEBUG_UPDATE_SEQUENCE && ModelContentProvider.DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
			System.out.println("\tBEGIN - " + update); //$NON-NLS-1$
		}
		notifyUpdate(ModelContentProvider.UPDATE_BEGINS, update);
	}
	
	/**
	 * Notification an update request has completed
	 * 
	 * @param update
	 */
	void updateComplete(ILabelUpdate update) {
		boolean end = false;
		synchronized (fUpdatesInProgress) {
			fUpdatesInProgress.remove(update);
			end = fUpdatesInProgress.isEmpty();
		}
        if (ModelContentProvider.DEBUG_UPDATE_SEQUENCE && ModelContentProvider.DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
			System.out.println("\tEND - " + update); //$NON-NLS-1$
		}
		notifyUpdate(ModelContentProvider.UPDATE_COMPLETE, update);
		if (end) {
            if (ModelContentProvider.DEBUG_UPDATE_SEQUENCE && ModelContentProvider.DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
				System.out.println("LABEL SEQUENCE ENDS"); //$NON-NLS-1$
			}
			notifyUpdate(ModelContentProvider.UPDATE_SEQUENCE_COMPLETE, null);
		}
	}
	
	protected void notifyUpdate(final int type, final ILabelUpdate update) {
		if (!fLabelListeners.isEmpty()) {
			Object[] listeners = fLabelListeners.getListeners();
			for (int i = 0; i < listeners.length; i++) {
				final ILabelUpdateListener listener = (ILabelUpdateListener) listeners[i];
				SafeRunner.run(new ISafeRunnable() {
					public void run() throws Exception {
						switch (type) {
							case ModelContentProvider.UPDATE_SEQUENCE_BEGINS:
								listener.labelUpdatesBegin();
								break;
							case ModelContentProvider.UPDATE_SEQUENCE_COMPLETE:
								listener.labelUpdatesComplete();
								break;
							case ModelContentProvider.UPDATE_BEGINS:
								listener.labelUpdateStarted(update);
								break;
							case ModelContentProvider.UPDATE_COMPLETE:
								listener.labelUpdateComplete(update);
								break;
						}
					}
					public void handleException(Throwable exception) {
						DebugUIPlugin.log(exception);
					}
				});
			}
		}
	}

 
	public void modelChanged(IModelDelta delta, IModelProxy proxy) {
	    delta.accept(fVisitor);
    }
	
}
