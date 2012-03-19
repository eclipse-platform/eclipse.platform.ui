/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation and others.
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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
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

/**
 * @since 3.3
 */
public class TreeModelLabelProvider extends ColumnLabelProvider 
    implements ITreeModelLabelProvider, IModelChangedListener 
{
	
	private IInternalTreeModelViewer fViewer;

	/**
	 * Note: access this variable should be synchronized with <code>this</code>.
	 */
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
	 */
	private Map fPendingUpdates = new HashMap();
	
	/**
	 * A runnable that will send the label update requests.
	 * This variable allows the job to be canceled and re-scheduled if 
	 * new updates are requested.  
	 */
	private Runnable fPendingUpdatesRunnable;
	
	/**
	 * List of updates in progress
	 */
	private List fUpdatesInProgress = new ArrayList();
	
    /**
     * Delta visitor actively cancels the outstanding label updates for 
     * elements that are changed and are about to be updated.
     */
    class CancelPendingUpdatesVisitor implements IModelDeltaVisitor {
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
    private CancelPendingUpdatesVisitor fCancelPendingUpdatesVisitor = new CancelPendingUpdatesVisitor();
	
	/**
	 * Constructs a new label provider on the given display
	 * @param viewer Viewer that this label provider is used with.
	 */
	public TreeModelLabelProvider(IInternalTreeModelViewer viewer) {
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
        Assert.isTrue(fViewer.getDisplay().getThread() == Thread.currentThread());
        
	    fViewer.removeModelChangedListener(this);
	    fViewer = null;
	    
	    List complete = null;
	    synchronized(this) {
	        complete = fComplete;
	        fComplete = null;
	    }
	    if (complete != null) {
            for (Iterator itr = complete.iterator(); itr.hasNext();) {
                ((ILabelUpdate)itr.next()).cancel();
            }
	    }
	    
		Iterator updatesInProgress = fUpdatesInProgress.iterator();
		while (updatesInProgress.hasNext()) {
			ILabelUpdate currentUpdate = (ILabelUpdate) updatesInProgress.next();
			currentUpdate.cancel();			
		}

		if (fPendingUpdatesRunnable != null) {
			fPendingUpdatesRunnable = null;
		}
		for (Iterator itr = fPendingUpdates.values().iterator(); itr.hasNext();) {
		    List updateList = (List)itr.next();
		    for (Iterator listItr = updateList.iterator(); listItr.hasNext();) {
		        ((LabelUpdate)listItr.next()).cancel();
		    }
		}
		fPendingUpdates.clear();
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

	private boolean isDisposed() {
	    return fViewer == null;
	}
	
	public void update(ViewerCell cell) {
		// NOT USED - the viewer updates each row instead 
	}	
	
	public boolean update(TreePath elementPath) {
        Assert.isTrue(fViewer.getDisplay().getThread() == Thread.currentThread());
	    
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
		    fPendingUpdatesRunnable = new Runnable() {
		        public void run() {
		            if (isDisposed()) return;
                    startRequests(this);
		        }
		    };
		    fViewer.getDisplay().asyncExec(fPendingUpdatesRunnable);
			return true;
		} else {
		    return false;
		}
	}
	
	/**
     * Cancel any outstanding updates that are running for this element. 
	 * @param elementPath Element to cancel updates for.
     */
    private void cancelPathUpdates(TreePath elementPath) {
        Assert.isTrue(fViewer.getDisplay().getThread() == Thread.currentThread());
        
        Iterator updatesInProgress = fUpdatesInProgress.iterator();
        while (updatesInProgress.hasNext()) {
            ILabelUpdate currentUpdate = (ILabelUpdate) updatesInProgress.next();
            if (elementPath.equals(currentUpdate.getElementPath())) {
                currentUpdate.cancel();
            }
        }
    }

    /**
     * Sets the element's display information in the viewer.
     * 
     * @param path Element path. 
     * @param numColumns Number of columns in the data.
     * @param labels Array of labels.  The array cannot to be 
     * <code>null</code>, but values within the array may be.
     * @param images Array of image descriptors, may be <code>null</code>.
     * @param fontDatas Array of fond data objects, may be <code>null</code>.
     * @param foregrounds Array of RGB values for foreground colors, may be 
     * <code>null</code>.
     * @param backgrounds Array of RGB values for background colors, may be 
     * <code>null</code>.
     * @param checked Whether given item should be checked.
     * @param grayed Whether given item's checkbox should be grayed.
     */
    void setElementData(TreePath path, int numColumns, String[] labels, ImageDescriptor[] images,
        FontData[] fontDatas, RGB[] foregrounds, RGB[] backgrounds, boolean checked, boolean grayed) 
    {
        fViewer.setElementData(path, numColumns, labels, images, fontDatas, foregrounds, backgrounds);
        fViewer.setElementChecked(path, checked, grayed);
    }

    
	private void startRequests(Runnable runnable) {
        if (runnable != fPendingUpdatesRunnable) {
            return;
        }

	    if (!fPendingUpdates.isEmpty()) {
            for (Iterator itr = fPendingUpdates.keySet().iterator(); itr.hasNext();) {
                IElementLabelProvider provider = (IElementLabelProvider)itr.next();
                List list = (List)fPendingUpdates.get(provider);
                for (Iterator listItr = list.iterator(); listItr.hasNext();) {
                    updateStarted((ILabelUpdate)listItr.next());
                }
                provider.update( (ILabelUpdate[])list.toArray(new ILabelUpdate[list.size()]) );
            }
	    }
	    fPendingUpdates.clear();
	    fPendingUpdatesRunnable = null;
	}
	
    /**
    * Cancels all running updates for the given element.  If seachFullPath is true,
    * all updates will be canceled which have the given element anywhere in their 
    * patch.   
    * @param element element to search for.
    * @param searchFullPath flag whether to look for the element in the full path
    * of the update
    */
   private void cancelElementUpdates(Object element, boolean searchFullPath) {
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

	/**
	 * Returns the presentation context for this label provider.
	 * 
	 * @return presentation context
	 */
	private IPresentationContext getPresentationContext() {
		return fViewer.getPresentationContext();
	}

    /**
     * A label update is complete.
     * 
     * @param update Update that is to be completed.
     */
    synchronized void complete(ILabelUpdate update) {
        if (fViewer == null) return;
        
		if (fComplete == null) {
			fComplete = new LinkedList();
			fViewer.getDisplay().asyncExec(new Runnable() {
			    public void run() {
			        if (isDisposed()) return;
                    List updates = null;
                    synchronized (TreeModelLabelProvider.this) {
                        updates = fComplete;
                        fComplete = null;
                    }
                    for (Iterator itr = updates.iterator(); itr.hasNext();) {
                        LabelUpdate itrUpdate = (LabelUpdate)itr.next();
                        if (itrUpdate.isCanceled()) {
                            updateComplete(itrUpdate);
                        } else {
                            itrUpdate.performUpdate();
                        }
                    }
			    }
			});
		}
		fComplete.add(update);
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
	 * @param update Update that was started
	 */
	void updateStarted(ILabelUpdate update) {
	    Assert.isTrue(fViewer.getDisplay().getThread() == Thread.currentThread());
	    
		boolean begin = fUpdatesInProgress.isEmpty();
		fUpdatesInProgress.add(update);
		
		if (begin) {
			if (DebugUIPlugin.DEBUG_UPDATE_SEQUENCE && DebugUIPlugin.DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
				DebugUIPlugin.trace("LABEL SEQUENCE BEGINS"); //$NON-NLS-1$
			}
			notifyUpdate(TreeModelContentProvider.UPDATE_SEQUENCE_BEGINS, null);
		}
        if (DebugUIPlugin.DEBUG_UPDATE_SEQUENCE && DebugUIPlugin.DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
        	DebugUIPlugin.trace("\tBEGIN - " + update); //$NON-NLS-1$
		}
		notifyUpdate(TreeModelContentProvider.UPDATE_BEGINS, update);
	}
	
	/**
	 * Notification an update request has completed
	 * 
	 * @param update Update that completed.
	 */
	void updateComplete(ILabelUpdate update) {
		fUpdatesInProgress.remove(update);
		
        if (DebugUIPlugin.DEBUG_UPDATE_SEQUENCE && DebugUIPlugin.DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
        	DebugUIPlugin.trace("\tEND - " + update); //$NON-NLS-1$
		}
		notifyUpdate(TreeModelContentProvider.UPDATE_COMPLETE, update);
		if (fUpdatesInProgress.isEmpty()) {
            if (DebugUIPlugin.DEBUG_UPDATE_SEQUENCE && DebugUIPlugin.DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
            	DebugUIPlugin.trace("LABEL SEQUENCE ENDS"); //$NON-NLS-1$
			}
			notifyUpdate(TreeModelContentProvider.UPDATE_SEQUENCE_COMPLETE, null);
		}
	}
	
	private void notifyUpdate(final int type, final ILabelUpdate update) {
		if (!fLabelListeners.isEmpty()) {
			Object[] listeners = fLabelListeners.getListeners();
			for (int i = 0; i < listeners.length; i++) {
				final ILabelUpdateListener listener = (ILabelUpdateListener) listeners[i];
				SafeRunner.run(new ISafeRunnable() {
					public void run() throws Exception {
						switch (type) {
							case TreeModelContentProvider.UPDATE_SEQUENCE_BEGINS:
								listener.labelUpdatesBegin();
								break;
							case TreeModelContentProvider.UPDATE_SEQUENCE_COMPLETE:
								listener.labelUpdatesComplete();
								break;
							case TreeModelContentProvider.UPDATE_BEGINS:
								listener.labelUpdateStarted(update);
								break;
							case TreeModelContentProvider.UPDATE_COMPLETE:
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
	    delta.accept(fCancelPendingUpdatesVisitor);
    }
	
}
