/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.action;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;

/**
 * A contribution item which delegates to an action.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class ActionContributionItem extends ContributionItem {

	/**
	 * Mode bit: Show text on tool items, even if an image is present.
	 * If this mode bit is not set, text is only shown on tool items if there is 
	 * no image present.
	 */
	public static int MODE_FORCE_TEXT = 1;
	/**
	 * This is the lower bound of the continuous range of accelerator values
	 * that should be handled specially on GTK.  This is to circumnavigate the
	 * special input mode in some cases.
	 */
	private static final int LOWER_GTK_ACCEL_BOUND = SWT.MOD1 | SWT.MOD2 | 'A';
	/**
	 * This is the lower bound of the continuous range of accelerator values
	 * that should be handled specially on GTK.  This is to circumnavigate the
	 * special input mode in some cases.
	 */
	private static final int UPPER_GTK_ACCEL_BOUND = SWT.MOD1 | SWT.MOD2 | 'F';

	/** a string inserted in the middle of text that has been shortened */
	private static final String ellipsis = "..."; //$NON-NLS-1$
	
	/**
     * A weakly referenced cache of image descriptors to image instances. This
     * is used to hold images in memory for action contribution items, while
     * they are defined. When the image descriptor becomes weakly referred to,
     * the corresponding image will be disposed.
     */
    private static final class ImageCache {
        
        /**
         * This class spoofs a few method calls by passing them through to the
         * underlying weakly referred object (if available). This allows the
         * weak reference to be used as a key in a <code>HashMap</code>.
         * 
         * @since 3.0
         */
        private static final class HashableWeakReference extends WeakReference {

            /**
             * Constructs a new instance of <code>HashableWeakReference</code>.
             * 
             * @param referent
             *            The object to refer to; may be <code>null</code>.
             * @param referenceQueue
             *            The reference queue to use; should not be
             *            <code>null</code>.
             */
            private HashableWeakReference(final Object referent,
                    final ReferenceQueue referenceQueue) {
                super(referent, referenceQueue);
            }

            /**
             * @see Object#hashCode()
             */
            public final int hashCode() {
                final Object referent = get();
                if (referent == null) { return super.hashCode(); }

                return referent.hashCode();
            }

            /**
             * @see Object#equals(java.lang.Object)
             */
            public final boolean equals(Object object) {
                final Object referent = get();
                if (referent == null) { return super.equals(object); }

                if (object instanceof HashableWeakReference) {
                    object = ((HashableWeakReference) object).get();
                }

                return referent.equals(object);
            }
        }

        /**
         * A thread for cleaner up the reference queues as the garbage collector
         * fills them. It takes a map and a reference queue. When an item
         * appears in the reference queue, it uses it as a key to remove values
         * from the map. If the value is an image, then it is disposed. To
         * shutdown the thread, call <code>stopCleaning()</code>.
         * 
         * @since 3.0
         */
        private static class ReferenceCleanerThread extends Thread {
            
            /**
             * The number of reference cleaner threads created.
             */
            private static int threads = 0;
            
            /**
             * A marker indicating that the reference cleaner thread should
             * exit.  This is enqueued when the thread is told to stop.  Any
             * referenced enqueued after the thread is told to stop will not be
             * cleaned up.
             */
            private final WeakReference endMarker;

            /**
             * The reference queue to check; will not be <code>null</code>.
             */
            private final ReferenceQueue referenceQueue;

            /**
             * The map from which to remove values. This value will not be
             * <code>null</code>.
             */
            private final Map map;

            /**
             * Constructs a new instance of <code>ReferenceCleanerThread</code>.
             * 
             * @param referenceQueue
             *            The reference queue to check for garbage; mmmmm....
             *            garbage. This value must not be <code>null</code>.
             * @param map
             *            The map to check for values; must not be
             *            <code>null</code>. It is expected that the keys are
             *            <code>Reference</code> instances. The values are
             *            expected to be <code>Image</code> objects, but it is
             *            okay if they are not.
             */
            private ReferenceCleanerThread(final ReferenceQueue referenceQueue,
                    final Map map) {
                super("Reference Cleaner - " + ++threads); //$NON-NLS-1$
                
                if (referenceQueue == null) { throw new NullPointerException(
                        "The reference queue should not be null."); } //$NON-NLS-1$

                if (map == null) { throw new NullPointerException(
                        "The map should not be null."); } //$NON-NLS-1$

                this.endMarker = new WeakReference(referenceQueue, referenceQueue);
                this.referenceQueue = referenceQueue;
                this.map = map;
            }

            /**
             * Tells this thread to stop trying to clean up. This is usually run
             * when the cache is shutting down.
             */
            private final void stopCleaning() {
                endMarker.enqueue();
            }

            /**
             * Waits for new garbage. When new garbage arriving, it removes it,
             * clears it, and disposes of any corresponding images.
             */
            public final void run() {
                while (true) {
                    // Get the next reference to dispose.
                    Reference reference = null;
                    try {
                        reference = referenceQueue.remove();
                    } catch (final InterruptedException e) {
                        // Reference will be null.
                    }
                    
                    // Check to see if we've been told to stop.
                    if (reference == endMarker) {
                        break;
                    }

                    // Remove the image and dispose it.
                    final Object value = map.remove(reference);
                    if (value instanceof Image) {
                        Display.getCurrent().syncExec(new Runnable() {

                            public void run() {
                                final Image image = (Image) value;
                                if (!image.isDisposed()) {
                                    image.dispose();
                                }
                            }
                        });
                    }

                    // Clear the reference.
                    if (reference != null) {
                        reference.clear();
                    }
                }
            }
        }
        
        /**
         * The thread responsible for cleaning out greyed images that are no
         * longer needed.
         */
        private final ReferenceCleanerThread greyCleaner;
        
        /**
         * A map of image descriptors to the corresponding greyed images. The
         * image descriptors are actually weak references to image descriptors.
         * As the weak references become suitable for collection, the
         * corresponding images (i.e., native resources) will be disposed. This
         * value may be empty, but it is never <code>null</code>.
         */
        private final Map greyMap = new HashMap();

        /**
         * A queue of references waiting to be garbage collected. This value is
         * never <code>null</code>. This is the queue for
         * <code>greyMap</code>.
         */
        private final ReferenceQueue greyReferenceQueue = new ReferenceQueue();
        
        /**
         * The thread responsible for cleaning out images that are no longer
         * needed.
         */
        private final ReferenceCleanerThread imageCleaner;

        /**
         * A map of image descriptors to the corresponding loaded images. The
         * image descriptors are actually weak references to image descriptors.
         * As the weak references become suitable for collection, the
         * corresponding images (i.e., native resources) will be disposed. This
         * value may be empty, but it is never <code>null</code>.
         */
        private final Map imageMap = new HashMap();

        /**
         * A queue of references waiting to be garbage collected. This value is
         * never <code>null</code>. This is the queue for
         * <code>imageMap</code>.
         */
        private final ReferenceQueue imageReferenceQueue = new ReferenceQueue();

        /**
         * The image to display when no image is available. This value is
         * <code>null</code> until it is first used.
         */
        private Image missingImage = null;
        
        /**
         * Constructs a new instance of <code>ImageCache</code>, and starts a
         * couple of threads to monitor the reference queues.
         */
        private ImageCache() {
            greyCleaner = new ReferenceCleanerThread(greyReferenceQueue,
                    greyMap);
            imageCleaner = new ReferenceCleanerThread(imageReferenceQueue,
                    imageMap);
            
            greyCleaner.start();
            imageCleaner.start();

        }

        /**
         * Cleans up all images in the cache. This disposes of all of the
         * images, and drops references to them. This should only be called when
         * all of the action contribution items are disappearing.
         */
        private final void dispose() {
            // Clean up the missing image.
            if ((missingImage != null) && (!missingImage.isDisposed())) {
                missingImage.dispose();
                missingImage = null;
            }

            /*
             * Stop the image cleaner thread, clear all of the weak references
             * and dispose of all of the images.
             */
            imageCleaner.stopCleaning();
            final Iterator imageItr = imageMap.entrySet().iterator();
            while (imageItr.hasNext()) {
                final Map.Entry entry = (Map.Entry) imageItr.next();

                final WeakReference reference = (WeakReference) entry.getKey();
                reference.clear();

                final Image image = (Image) entry.getValue();
                if ((image != null) && (!image.isDisposed())) {
                    image.dispose();
                }
            }
            imageMap.clear();

            /*
             * Stop the greyed image cleaner thread, clear all of the weak
             * references and dispose of all of the greyed images.
             */
            greyCleaner.stopCleaning();
            final Iterator greyItr = greyMap.entrySet().iterator();
            while (greyItr.hasNext()) {
                final Map.Entry entry = (Map.Entry) greyItr.next();

                final WeakReference reference = (WeakReference) entry.getKey();
                reference.clear();

                final Image image = (Image) entry.getValue();
                if ((image != null) && (!image.isDisposed())) {
                    image.dispose();
                }
            }
            greyMap.clear();
        }

        /**
         * Returns the greyed image (i.e., disabled) for the given image
         * descriptor. This caches the result so that future attempts to get the
         * greyed image for the same descriptor will only access the cache. When
         * the last reference to the image descriptor is dropped, the image will
         * be cleaned up. This clean up makes no time guarantees about how long
         * this will take.
         * 
         * @param descriptor
         *            The image descriptor for which a greyed image should be
         *            created; may be <code>null</code>.
         * @return The greyed image, either newly created or from the cache.
         *         This value is <code>null</code> if the parameter passed in
         *         is <code>null</code>.
         */
        private final Image getGrayImage(final ImageDescriptor descriptor) {
            if (descriptor == null) { return null; }

            // Try to load a cached image.
            final HashableWeakReference key = new HashableWeakReference(
                    descriptor, imageReferenceQueue);
            final Object value = greyMap.get(key);
            if (value instanceof Image) {
                key.clear();
                return (Image) value;
            }

            // Try to create a grey image from the regular image.
            final Image image = getImage(descriptor);
            if (image != null) {
                final Image greyImage = new Image(null, image, SWT.IMAGE_GRAY);
                greyMap.put(key, greyImage);
                return greyImage;
            }

            // All attempts have failed.
            return null;
        }

        /**
         * Returns the regular image (i.e., enabled) for the given image
         * descriptor. This caches the result so that future attempts to get the
         * image for the same descriptor will only access the cache. When the
         * last reference to the image descriptor is dropped, the image will be
         * cleaned up. This clean up makes no time guarantees about how long
         * this will take.
         * 
         * @param descriptor
         *            The image descriptor for which an image should be created;
         *            may be <code>null</code>.
         * @return The image, either newly created or from the cache. This value
         *         is <code>null</code> if the parameter passed in is
         *         <code>null</code>.
         */
        private final Image getImage(final ImageDescriptor descriptor) {
            if (descriptor == null) { return null; }

            // Try to load the cached value.
            final HashableWeakReference key = new HashableWeakReference(
                    descriptor, imageReferenceQueue);
            final Object value = imageMap.get(key);
            if (value instanceof Image) {
                key.clear();
                return (Image) value;
            }

            // Use the descriptor to create the image.
            final Image image = descriptor.createImage();
            imageMap.put(key, image);
            return image;
        }

        /**
         * Returns the image to display when no image can be found, or none is
         * specified. This image is only disposed when the cache is disposed.
         * 
         * @return The image to display for missing images. This value will
         *         never be <code>null</code>.
         */
        private final Image getMissingImage() {
            if (missingImage == null) {
                missingImage = getImage(ImageDescriptor
                        .getMissingImageDescriptor());
            }

            return missingImage;
        }
    }

	private static ImageCache globalImageCache;

	private static boolean USE_COLOR_ICONS = true;

	/**
	 * Returns whether color icons should be used in toolbars.
	 * 
	 * @return <code>true</code> if color icons should be used in toolbars, 
	 *   <code>false</code> otherwise
	 */
	public static boolean getUseColorIconsInToolbars() {
		return USE_COLOR_ICONS;
	}

	/**
	 * Sets whether color icons should be used in toolbars.
	 * 
	 * @param useColorIcons <code>true</code> if color icons should be used in toolbars, 
	 *   <code>false</code> otherwise
	 */
	public static void setUseColorIconsInToolbars(boolean useColorIcons) {
		USE_COLOR_ICONS = useColorIcons;
	}

	/**
	 * The presentation mode.
	 */
	private int mode = 0;

	/**
	 * The action.
	 */
	private IAction action;
	
	/**
     * The listener for changes to the text of the action contributed by an
     * external source.
     */
    private final IPropertyChangeListener actionTextListener = new IPropertyChangeListener() {

        /**
         * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
         */
        public void propertyChange(PropertyChangeEvent event) {
            update(event.getProperty());
        }
    };

	/**
	 * Listener for SWT button widget events.
	 */
	private Listener buttonListener;

	/**
	 * Listener for SWT menu item widget events.
	 */
	private Listener menuItemListener;

	/**
	 * Listener for action property change notifications.
	 */
	private final IPropertyChangeListener propertyListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			actionPropertyChange(event);
		}
	};

	/**
	 * Listener for SWT tool item widget events.
	 */
	private Listener toolItemListener;

	/**
	 * The widget created for this item; <code>null</code>
	 * before creation and after disposal.
	 */
	private Widget widget = null;

	/**
     * Creates a new contribution item from the given action. The id of the
     * action is used as the id of the item.
     * 
     * @param action
     *            the action
     */
    public ActionContributionItem(IAction action) {
        super(action.getId());
        this.action = action;
    }
	
	/**
	 * Handles a property change event on the action (forwarded by nested listener).
	 */
	private void actionPropertyChange(final PropertyChangeEvent e) {
		// This code should be removed. Avoid using free asyncExec

		if (isVisible() && widget != null) {
			Display display = widget.getDisplay();
			if (display.getThread() == Thread.currentThread()) {
				update(e.getProperty());
			} else {
				display.asyncExec(new Runnable() {
					public void run() {
						update(e.getProperty());
					}
				});
			}

		}
	}

	/**
	 * Compares this action contribution item with another object.
	 * Two action contribution items are equal if they refer to the identical Action.
	 */
	public boolean equals(Object o) {
		if (!(o instanceof ActionContributionItem)) {
			return false;
		}
		return action.equals(((ActionContributionItem) o).action);
	}
	/**
	 * The <code>ActionContributionItem</code> implementation of this
	 * <code>IContributionItem</code> method creates an SWT <code>Button</code> for
	 * the action using the action's style. If the action's checked property has
	 * been set, the button is created and primed to the value of the checked
	 * property.
	 */
	public void fill(Composite parent) {
		if (widget == null && parent != null) {
			int flags = SWT.PUSH;
			if (action != null) {
				if (action.getStyle() == IAction.AS_CHECK_BOX)
					flags = SWT.TOGGLE;
				if (action.getStyle() == IAction.AS_RADIO_BUTTON)
					flags = SWT.RADIO;
			}

			Button b = new Button(parent, flags);
			b.setData(this);
			b.addListener(SWT.Dispose, getButtonListener());
			// Don't hook a dispose listener on the parent
			b.addListener(SWT.Selection, getButtonListener());
			if (action.getHelpListener() != null)
				b.addHelpListener(action.getHelpListener());
			widget = b;

			update(null);

			// Attach some extra listeners.
			action.addPropertyChangeListener(propertyListener);
	        if (action != null) {
	            String commandId = action.getActionDefinitionId();
	            ExternalActionManager.ICallback callback = ExternalActionManager.getInstance()
	                    .getCallback();

	            if ((callback != null) && (commandId != null)) {
	                callback.addPropertyChangeListener(commandId, actionTextListener);
	            }
	        }
		}
	}
	/**
	 * The <code>ActionContributionItem</code> implementation of this
	 * <code>IContributionItem</code> method creates an SWT <code>MenuItem</code>
	 * for the action using the action's style. If the action's checked property has
	 * been set, a button is created and primed to the value of the checked
	 * property. If the action's menu creator property has been set, a cascading
	 * submenu is created.
	 */
	public void fill(Menu parent, int index) {
		if (widget == null && parent != null) {
			Menu subMenu = null;
			int flags = SWT.PUSH;
			if (action != null) {
				int style = action.getStyle();
				if (style == IAction.AS_CHECK_BOX)
					flags = SWT.CHECK;
				else if (style == IAction.AS_RADIO_BUTTON)
					flags = SWT.RADIO;
				else if (style == IAction.AS_DROP_DOWN_MENU) {
					IMenuCreator mc = action.getMenuCreator();
					if (mc != null) {
						subMenu = mc.getMenu(parent);
						flags = SWT.CASCADE;
					}
				}
			}

			MenuItem mi = null;
			if (index >= 0)
				mi = new MenuItem(parent, flags, index);
			else
				mi = new MenuItem(parent, flags);
			widget = mi;

			mi.setData(this);
			mi.addListener(SWT.Dispose, getMenuItemListener());
			mi.addListener(SWT.Selection, getMenuItemListener());
			if (action.getHelpListener() != null)
				mi.addHelpListener(action.getHelpListener());

			if (subMenu != null)
				mi.setMenu(subMenu);

			update(null);

			// Attach some extra listeners.
			action.addPropertyChangeListener(propertyListener);
	        if (action != null) {
	            String commandId = action.getActionDefinitionId();
	            ExternalActionManager.ICallback callback = ExternalActionManager.getInstance()
	                    .getCallback();

	            if ((callback != null) && (commandId != null)) {
	                callback.addPropertyChangeListener(commandId, actionTextListener);
	            }
	        }
		}
	}
	/**
	 * The <code>ActionContributionItem</code> implementation of this ,
	 * <code>IContributionItem</code> method creates an SWT <code>ToolItem</code>
	 * for the action using the action's style. If the action's checked property has
	 * been set, a button is created and primed to the value of the checked
	 * property. If the action's menu creator property has been set, a drop-down
	 * tool item is created.
	 */
	public void fill(ToolBar parent, int index) {
		if (widget == null && parent != null) {
			int flags = SWT.PUSH;
			if (action != null) {
				int style = action.getStyle();
				if (style == IAction.AS_CHECK_BOX)
					flags = SWT.CHECK;
				else if (style == IAction.AS_RADIO_BUTTON)
					flags = SWT.RADIO;
				else if (style == IAction.AS_DROP_DOWN_MENU)
					flags = SWT.DROP_DOWN;
			}

			ToolItem ti = null;
			if (index >= 0)
				ti = new ToolItem(parent, flags, index);
			else
				ti = new ToolItem(parent, flags);
			ti.setData(this);
			ti.addListener(SWT.Selection, getToolItemListener());
			ti.addListener(SWT.Dispose, getToolItemListener());

			widget = ti;

			update(null);

			// Attach some extra listeners.
			action.addPropertyChangeListener(propertyListener);
	        if (action != null) {
	            String commandId = action.getActionDefinitionId();
	            ExternalActionManager.ICallback callback = ExternalActionManager.getInstance()
	                    .getCallback();

	            if ((callback != null) && (commandId != null)) {
	                callback.addPropertyChangeListener(commandId, actionTextListener);
	            }
	        }
		}
	}
	/**
	 * Returns the action associated with this contribution item.
	 *
	 * @return the action
	 */
	public IAction getAction() {
		return action;
	}
	/**
	 * Returns the listener for SWT button widget events.
	 * 
	 * @return a listener for button events
	 */
	private Listener getButtonListener() {
		if (buttonListener == null) {
			buttonListener = new Listener() {
				public void handleEvent(Event event) {
					switch (event.type) {
						case SWT.Dispose :
							handleWidgetDispose(event);
							break;
						case SWT.Selection :
							Widget ew = event.widget;
							if (ew != null) {
								handleWidgetSelection(event, ((Button) ew).getSelection());
							}
							break;
					}
				}
			};
		}
		return buttonListener;
	}
	/**
	 * Returns the image cache.
	 * The cache is global, and is shared by all action contribution items.
	 * This has the disadvantage that once an image is allocated, it is never freed until the display
	 * is disposed.  However, it has the advantage that the same image in different contribution managers
	 * is only ever created once.
	 */
	private static ImageCache getImageCache() {
		ImageCache cache = globalImageCache;
		if (cache == null) {
			globalImageCache = cache = new ImageCache();
			Display display = Display.getDefault();
			if (display != null) {
				display.disposeExec(new Runnable() {
					public void run() {
						if (globalImageCache != null) {
							globalImageCache.dispose();
							globalImageCache = null;
						}
					}
				});
			}
		}
		return cache;
	}
	/**
	 * Returns the listener for SWT menu item widget events.
	 * 
	 * @return a listener for menu item events
	 */
	private Listener getMenuItemListener() {
		if (menuItemListener == null) {
			menuItemListener = new Listener() {
				public void handleEvent(Event event) {
					switch (event.type) {
						case SWT.Dispose :
							handleWidgetDispose(event);
							break;
						case SWT.Selection :
							Widget ew = event.widget;
							if (ew != null) {
								handleWidgetSelection(event, ((MenuItem) ew).getSelection());
							}
							break;
					}
				}
			};
		}
		return menuItemListener;
	}
	/**
	 * Returns the presentation mode, which is the bitwise-or of the 
	 * <code>MODE_*</code> constants.  The default mode setting is 0, meaning
	 * that for menu items, both text and image are shown (if present), but for
	 * tool items, the text is shown only if there is no image.
	 * 
	 * @return the presentation mode settings
	 * 
	 * @since 3.0
	 */
	public int getMode() {
		return mode;
	}
	
	/**
	 * Returns the listener for SWT tool item widget events.
	 * 
	 * @return a listener for tool item events
	 */
	private Listener getToolItemListener() {
		if (toolItemListener == null) {
			toolItemListener = new Listener() {
				public void handleEvent(Event event) {
					switch (event.type) {
						case SWT.Dispose :
							handleWidgetDispose(event);
							break;
						case SWT.Selection :
							Widget ew = event.widget;
							if (ew != null) {
								handleWidgetSelection(event, ((ToolItem) ew).getSelection());
							}
							break;
					}
				}
			};
		}
		return toolItemListener;
	}
	/**
	 * Handles a widget dispose event for the widget corresponding to this item.
	 */
	private void handleWidgetDispose(Event e) {
	    // Check if our widget is the one being disposed.
		if (e.widget == widget) {
			// Dispose of the menu creator.
			if (action.getStyle() == IAction.AS_DROP_DOWN_MENU) {
				IMenuCreator mc = action.getMenuCreator();
				if (mc != null) {
					mc.dispose();
				}
			}
			
			// Unhook all of the listeners.
			action.removePropertyChangeListener(propertyListener);
	        if (action != null) {
	            String commandId = action.getActionDefinitionId();
	            ExternalActionManager.ICallback callback = ExternalActionManager.getInstance()
	                    .getCallback();

	            if ((callback != null) && (commandId != null)) {
	                callback.removePropertyChangeListener(commandId, actionTextListener);
	            }
	        }
	        
	        // Clear the widget field.
			widget = null;
		}
	}
	/**
	 * Handles a widget selection event.
	 */
	private void handleWidgetSelection(Event e, boolean selection) {

		Widget item = e.widget;
		if (item != null) {
			int style = item.getStyle();

			if ((style & (SWT.TOGGLE | SWT.CHECK)) != 0) {
				if (action.getStyle() == IAction.AS_CHECK_BOX) {
					action.setChecked(selection);
				}
			} else if ((style & SWT.RADIO) != 0) {
				if (action.getStyle() == IAction.AS_RADIO_BUTTON) {
					action.setChecked(selection);
				}
			} else if ((style & SWT.DROP_DOWN) != 0) {
				if (e.detail == 4) { // on drop-down button
					if (action.getStyle() == IAction.AS_DROP_DOWN_MENU) {
						IMenuCreator mc = action.getMenuCreator();
						ToolItem ti = (ToolItem) item;
						// we create the menu as a sub-menu of "dummy" so that we can use
						// it in a cascading menu too.
						// If created on a SWT control we would get an SWT error...
						//Menu dummy= new Menu(ti.getParent());
						//Menu m= mc.getMenu(dummy);
						//dummy.dispose();
						if (mc != null) {
							Menu m = mc.getMenu(ti.getParent());
							if (m != null) {
								// position the menu below the drop down item
								Rectangle b = ti.getBounds();
								Point p = ti.getParent().toDisplay(new Point(b.x, b.y + b.height));
								m.setLocation(p.x, p.y); // waiting for SWT 0.42
								m.setVisible(true);
								return; // we don't fire the action
							}
						}
					}
				}
			}

			// Ensure action is enabled first.
			// See 1GAN3M6: ITPUI:WINNT - Any IAction in the workbench can be executed while disabled.
			if (action.isEnabled()) {
				boolean trace = Policy.TRACE_ACTIONS;
				
				long ms = System.currentTimeMillis();
				if (trace)
					System.out.println("Running action: " + action.getText()); //$NON-NLS-1$

				action.runWithEvent(e);

				if (trace)
					System.out.println((System.currentTimeMillis() - ms) + " ms to run action: " + action.getText()); //$NON-NLS-1$
			}
		}
	}
	/* (non-Javadoc)
	 * Method declared on Object.
	 */
	public int hashCode() {
		return action.hashCode();
	}

	/**
	 * Returns whether the given action has any images.
	 * 
	 * @param actionToCheck the action
	 * @return <code>true</code> if the action has any images, <code>false</code> if not
	 */
	private boolean hasImages(IAction actionToCheck) {
		return actionToCheck.getImageDescriptor() != null
			|| actionToCheck.getHoverImageDescriptor() != null
			|| actionToCheck.getDisabledImageDescriptor() != null;
	}

	/**
	 * Returns whether the command corresponding to this action
	 * is active.
	 */
	private boolean isCommandActive() {
		IAction actionToCheck = getAction();

		if (actionToCheck != null) {
			String commandId = actionToCheck.getActionDefinitionId();
			ExternalActionManager.ICallback callback = ExternalActionManager.getInstance().getCallback();

			if (callback != null)
				return callback.isActive(commandId);
		}
		return true;
	}
	/**
	 * The action item implementation of this <code>IContributionItem</code>
	 * method returns <code>true</code> for menu items and <code>false</code>
	 * for everything else.
	 */
	public boolean isDynamic() {
		if (widget instanceof MenuItem) {
			//Optimization. Only recreate the item is the check or radio style has changed. 
			boolean itemIsCheck = (widget.getStyle() & SWT.CHECK) != 0;
			boolean actionIsCheck =
				getAction() != null && getAction().getStyle() == IAction.AS_CHECK_BOX;
			boolean itemIsRadio = (widget.getStyle() & SWT.RADIO) != 0;
			boolean actionIsRadio =
				getAction() != null && getAction().getStyle() == IAction.AS_RADIO_BUTTON;
			return (itemIsCheck != actionIsCheck) || (itemIsRadio != actionIsRadio);
		}
		return false;
	}

	/* (non-Javadoc)
	 * Method declared on IContributionItem.
	 */
	public boolean isEnabled() {
		return action != null && action.isEnabled();
	}
	/**
	 * Returns <code>true</code> if this item is allowed to enable,
	 * <code>false</code> otherwise.
	 * 
	 * @return if this item is allowed to be enabled
	 * @since 2.0
	 */
	protected boolean isEnabledAllowed() {
		if (getParent() == null)
			return true;
		Boolean value = getParent().getOverrides().getEnabled(this);
		return (value == null) ? true : value.booleanValue();
	}

	/**
	 * The <code>ActionContributionItem</code> implementation of this 
	 * <code>ContributionItem</code> method extends the super implementation
	 * by also checking whether the command corresponding to this action is active.
	 */
	public boolean isVisible() {
		return super.isVisible() && isCommandActive();
	}

	/**
	 * Sets the presentation mode, which is the bitwise-or of the 
	 * <code>MODE_*</code> constants.
	 * 
	 * @return the presentation mode settings
	 * 
	 * @since 3.0
	 */
	public void setMode(int mode) {
		this.mode = mode;
		update();
	}
	
	/**
	 * The action item implementation of this <code>IContributionItem</code>
	 * method calls <code>update(null)</code>.
	 */
	public final void update() {
		update(null);
	}
	/**
	 * Synchronizes the UI with the given property.
	 *
	 * @param propertyName the name of the property, or <code>null</code> meaning all applicable
	 *   properties 
	 */
	public void update(String propertyName) {
		if (widget != null) {
			// determine what to do			
			boolean textChanged = propertyName == null || propertyName.equals(IAction.TEXT);
			boolean imageChanged = propertyName == null || propertyName.equals(IAction.IMAGE);
			boolean tooltipTextChanged =
				propertyName == null || propertyName.equals(IAction.TOOL_TIP_TEXT);
			boolean enableStateChanged =
				propertyName == null
					|| propertyName.equals(IAction.ENABLED)
					|| propertyName.equals(IContributionManagerOverrides.P_ENABLED);
			boolean checkChanged =
				(action.getStyle() == IAction.AS_CHECK_BOX
					|| action.getStyle() == IAction.AS_RADIO_BUTTON)
					&& (propertyName == null || propertyName.equals(IAction.CHECKED));

			if (widget instanceof ToolItem) {
				ToolItem ti = (ToolItem) widget;
				String text = action.getText();
				// the set text is shown only if there is no image or if forced by MODE_FORCE_TEXT
				boolean showText = text != null && ((getMode() & MODE_FORCE_TEXT) != 0 || !hasImages(action));
				
				// only do the trimming if the text will be used
				if (showText && text != null) {
					text = Action.removeAcceleratorText(text);
					text = Action.removeMnemonics(text);
				}

				if (textChanged) {
					String textToSet = showText ? text : ""; //$NON-NLS-1$
					boolean rightStyle = (ti.getParent().getStyle() & SWT.RIGHT) != 0;
					if (rightStyle || !ti.getText().equals(textToSet)) {
						// In addition to being required to update the text if it
						// gets nulled out in the action, this is also a workaround 
						// for bug 50151: Using SWT.RIGHT on a ToolBar leaves blank space
						ti.setText(textToSet);
					}
				}

				if (imageChanged) {
					// only substitute a missing image if it has no text
					updateImages(!showText);
				}

				if (tooltipTextChanged || textChanged) {
					String toolTip = action.getToolTipText();
					// if the text is showing, then only set the tooltip if different
					if (!showText || toolTip != null && !toolTip.equals(text)) {
						ti.setToolTipText(action.getToolTipText());
					}
					else {
						ti.setToolTipText(null);
					}
				}

				if (enableStateChanged) {
					boolean shouldBeEnabled = action.isEnabled() && isEnabledAllowed();

					if (ti.getEnabled() != shouldBeEnabled)
						ti.setEnabled(shouldBeEnabled);
				}

				if (checkChanged) {
					boolean bv = action.isChecked();

					if (ti.getSelection() != bv)
						ti.setSelection(bv);
				}
				return;
			}

			if (widget instanceof MenuItem) {
				MenuItem mi = (MenuItem) widget;

				if (textChanged) {
					int accelerator = 0;
					String acceleratorText = null;
					IAction updatedAction = getAction();
					String text = null;

					// Set the accelerator using the action's accelerator.
					accelerator = updatedAction.getAccelerator();
					
					/* Process accelerators on GTK in a special way to avoid
					 * Bug 42009.  We will override the native input method by
					 * allowing these reserved accelerators to be placed on the
					 * menu.  We will only do this for "Ctrl+Shift+[A-F]".
					 */
					ExternalActionManager.ICallback callback =
						ExternalActionManager.getInstance().getCallback();
					String commandId = updatedAction.getActionDefinitionId();
					if (SWT.getPlatform().equals("gtk")) { //$NON-NLS-1$
						if ((callback != null) && (commandId != null)) {
						    Integer commandAccelerator = callback.getAccelerator(commandId);
						    if (commandAccelerator != null) {
						        int accelInt = callback.getAccelerator(commandId).intValue();
						        if ((accelInt >= LOWER_GTK_ACCEL_BOUND) && (accelInt <= UPPER_GTK_ACCEL_BOUND)) {
						            accelerator = accelInt;
						            acceleratorText = callback.getAcceleratorText(commandId);
						        }
						    }
					    }
					}
					
					if (accelerator == 0) {
						if ((callback != null) && (commandId != null)) {
						    acceleratorText = callback.getAcceleratorText(commandId);
						}
					} else {
						acceleratorText = Action.convertAccelerator(accelerator);
					}

					IContributionManagerOverrides overrides = null;

					if (getParent() != null)
						overrides = getParent().getOverrides();

					if (overrides != null)
						text = getParent().getOverrides().getText(this);

					mi.setAccelerator(accelerator);

					if (text == null)
						text = updatedAction.getText();

					if (text == null)
						text = ""; //$NON-NLS-1$
					else
						text = Action.removeAcceleratorText(text);

					if (acceleratorText == null)
						mi.setText(text);
					else
						mi.setText(text + '\t' + acceleratorText);
				}

				if (imageChanged)
					updateImages(false);

				if (enableStateChanged) {
					boolean shouldBeEnabled = action.isEnabled() && isEnabledAllowed();

					if (mi.getEnabled() != shouldBeEnabled)
						mi.setEnabled(shouldBeEnabled);
				}

				if (checkChanged) {
					boolean bv = action.isChecked();

					if (mi.getSelection() != bv)
						mi.setSelection(bv);
				}

				return;
			}

			if (widget instanceof Button) {
				Button button = (Button) widget;

				if (imageChanged && updateImages(false))
					textChanged = false; // don't update text if it has an image

				if (textChanged) {
					String text = action.getText();

					if (text != null)
						button.setText(text);
				}

				if (tooltipTextChanged)
					button.setToolTipText(action.getToolTipText());

				if (enableStateChanged) {
					boolean shouldBeEnabled = action.isEnabled() && isEnabledAllowed();

					if (button.getEnabled() != shouldBeEnabled)
						button.setEnabled(shouldBeEnabled);
				}

				if (checkChanged) {
					boolean bv = action.isChecked();

					if (button.getSelection() != bv)
						button.setSelection(bv);
				}
				return;
			}
		}
	}
	/**
	 * Updates the images for this action.
	 *
	 * @param forceImage <code>true</code> if some form of image is compulsory,
	 *  and <code>false</code> if it is acceptable for this item to have no image
	 * @return <code>true</code> if there are images for this action, <code>false</code> if not
	 */
	private boolean updateImages(boolean forceImage) {
		ImageCache cache = getImageCache();

		if (widget instanceof ToolItem) {
			if (USE_COLOR_ICONS) {
				Image image = cache.getImage(action.getHoverImageDescriptor());
				if (image == null) {
					image = cache.getImage(action.getImageDescriptor());
				}
				Image disabledImage = cache.getImage(action.getDisabledImageDescriptor());

				// Make sure there is a valid image.
				if (image == null && forceImage) {
					image = cache.getMissingImage();
				}

				// performance: more efficient in SWT to set disabled and hot image before regular image
				if (disabledImage != null) {
					// Set the disabled image if we were able to create one.
					// Assumes that SWT.ToolItem will use platform's default
					// behavior to show item when it is disabled and a disabled
					// image has not been set. 
					 ((ToolItem) widget).setDisabledImage(disabledImage);
				}
				((ToolItem) widget).setImage(image);

				return image != null;
			} else {
				Image image = cache.getImage(action.getImageDescriptor());
				Image hoverImage = cache.getImage(action.getHoverImageDescriptor());
				Image disabledImage = cache.getImage(action.getDisabledImageDescriptor());

				// If there is no regular image, but there is a hover image,
				// convert the hover image to gray and use it as the regular image.
				if (image == null && hoverImage != null) {
					image = cache.getGrayImage(action.getHoverImageDescriptor());
				} else {
					// If there is no hover image, use the regular image as the hover image,
					// and convert the regular image to gray
					if (hoverImage == null && image != null) {
						hoverImage = image;
						image = cache.getGrayImage(action.getImageDescriptor());
					}
				}

				// Make sure there is a valid image.
				if (hoverImage == null && image == null && forceImage) {
					image = cache.getMissingImage();
				}

				// performance: more efficient in SWT to set disabled and hot image before regular image
				if (disabledImage != null) {
					// Set the disabled image if we were able to create one.
					// Assumes that SWT.ToolItem will use platform's default
					// behavior to show item when it is disabled and a disabled
					// image has not been set. 
					 ((ToolItem) widget).setDisabledImage(disabledImage);
				}
				((ToolItem) widget).setHotImage(hoverImage);
				((ToolItem) widget).setImage(image);

				return image != null;
			}
		} else if (widget instanceof Item || widget instanceof Button) {
			// Use hover image if there is one, otherwise use regular image.
			Image image = cache.getImage(action.getHoverImageDescriptor());
			if (image == null) {
				image = cache.getImage(action.getImageDescriptor());
			}
			// Make sure there is a valid image.
			if (image == null && forceImage) {
				image = cache.getMissingImage();
			}
			if (widget instanceof Item) {
				((Item) widget).setImage(image);
			} else if (widget instanceof Button) {
				((Button) widget).setImage(image);
			}
			return image != null;
		}
		return false;
	}

	/**
	 * Shorten the given text <code>t</code> so that its length doesn't
	 * exceed the given width. The default implementation replaces characters
	 * in the center of the original string with an ellipsis ("..."). Override
	 * if you need a different strategy.
	 */
	protected String shortenText(String textValue , ToolItem item) {
		if (textValue == null)
			return null;

		GC gc = new GC(item.getDisplay());

		int maxWidth = item.getImage().getBounds().width * 4;
		
		if(gc.textExtent(textValue).x < maxWidth) {
			gc.dispose();
			return textValue ;
		}
		
		for (int i = textValue.length(); i > 0; i--) {
			String test = textValue .substring(0, i);
			test = test + ellipsis;
			if(gc.textExtent(test).x < maxWidth) {
				gc.dispose();
				return test ;
			}
				
		}	
		gc.dispose();
		//If for some reason we fall through abort
		return textValue ;
	}
}
