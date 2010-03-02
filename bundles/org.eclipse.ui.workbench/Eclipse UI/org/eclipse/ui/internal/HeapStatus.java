/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brock Janicyak - brockj@tpg.com.au 
 *     		- Fix for Bug 11142 [HeapStatus] Heap status is updated too frequently
 *          - Fix for Bug 192996 [Workbench] Reduce amount of garbage created by HeapStatus
 *******************************************************************************/

package org.eclipse.ui.internal;

import java.lang.reflect.Method;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;

/**
 * The Heap Status control, which shows the heap usage statistics in the window trim.
 * 
 * @since 3.1
 */
public class HeapStatus extends Composite {

	private boolean armed;
	private Image gcImage;
	private Color bgCol, usedMemCol, lowMemCol, freeMemCol, topLeftCol, bottomRightCol, sepCol, textCol, markCol, armCol;  
    private Canvas button;
	private IPreferenceStore prefStore;
	private int updateInterval;
	private boolean showMax;
    private long totalMem;
    private long prevTotalMem = -1L;
    private long prevUsedMem = -1L;
    private boolean hasChanged;
    private long usedMem;
    private long mark = -1;
    // start with 12x12
	private Rectangle imgBounds = new Rectangle(0,0,12,12);
	private long maxMem = Long.MAX_VALUE;
	private boolean maxMemKnown;
	private float lowMemThreshold = 0.05f;
	private boolean showLowMemThreshold = true;
	private boolean updateTooltip = false;
	
    private final Runnable timer = new Runnable() {
        public void run() {
            if (!isDisposed()) {
                updateStats();
                if (hasChanged) {
                	if (updateTooltip) {
                		updateToolTip();
                	}
                    redraw();
                    hasChanged = false;
                }
                getDisplay().timerExec(updateInterval, this);
            }
        }
    };
    
    private final IPropertyChangeListener prefListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			if (IHeapStatusConstants.PREF_UPDATE_INTERVAL.equals(event.getProperty())) {
				setUpdateIntervalInMS(prefStore.getInt(IHeapStatusConstants.PREF_UPDATE_INTERVAL));
			}
			else if (IHeapStatusConstants.PREF_SHOW_MAX.equals(event.getProperty())) {
				showMax = prefStore.getBoolean(IHeapStatusConstants.PREF_SHOW_MAX);
			}
		}
	};

    /**
     * Creates a new heap status control with the given parent, and using
     * the given preference store to obtain settings such as the refresh
     * interval.
     * 
     * @param parent the parent composite
     * @param prefStore the preference store
     */
	public HeapStatus(Composite parent, IPreferenceStore prefStore) {
		super(parent, SWT.NONE);

		maxMem = getMaxMem();
		maxMemKnown = maxMem != Long.MAX_VALUE;

        this.prefStore = prefStore;
        prefStore.addPropertyChangeListener(prefListener);
        
        setUpdateIntervalInMS(prefStore.getInt(IHeapStatusConstants.PREF_UPDATE_INTERVAL));
        showMax = prefStore.getBoolean(IHeapStatusConstants.PREF_SHOW_MAX);
		
        button = new Canvas(this, SWT.NONE);
        button.setToolTipText(WorkbenchMessages.HeapStatus_buttonToolTip);
        
		ImageDescriptor imageDesc = WorkbenchImages.getWorkbenchImageDescriptor("elcl16/trash.gif"); //$NON-NLS-1$
		gcImage = imageDesc.createImage();
		if (gcImage != null) {
			imgBounds = gcImage.getBounds();
		}
		Display display = getDisplay();
		usedMemCol = display.getSystemColor(SWT.COLOR_INFO_BACKGROUND);
		lowMemCol = new Color(display, 255, 70, 70);  // medium red 
		freeMemCol = new Color(display, 255, 190, 125);  // light orange
		bgCol = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
		sepCol = topLeftCol = armCol = display.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
		bottomRightCol = display.getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW);
		markCol = textCol = display.getSystemColor(SWT.COLOR_INFO_FOREGROUND);
		
		createContextMenu();
		
        Listener listener = new Listener() {

            public void handleEvent(Event event) {
                switch (event.type) {
                case SWT.Dispose:
                	doDispose();
                    break;
                case SWT.Resize:
                    Rectangle rect = getClientArea();
                    button.setBounds(rect.width - imgBounds.width - 1, 1, imgBounds.width, rect.height - 2);
                    break;
                case SWT.Paint:
                    if (event.widget == HeapStatus.this) {
                    	paintComposite(event.gc);
                    }
                    else if (event.widget == button) {
                        paintButton(event.gc);
                    }
                    break;
                case SWT.MouseUp:
                    if (event.button == 1) {
                        gc();
                        arm(false);
                    }
                    break;
                case SWT.MouseDown:
                    if (event.button == 1) {
	                    if (event.widget == HeapStatus.this) {
							setMark();
						} else if (event.widget == button) {
							arm(true);
						}
                    }
                    break;
                case SWT.MouseEnter:
                	HeapStatus.this.updateTooltip = true;
                	updateToolTip();
                	break;
                case SWT.MouseExit:
                    if (event.widget == HeapStatus.this) {
                    	HeapStatus.this.updateTooltip = false;
					} else if (event.widget == button) {
						arm(false);
					}
                    break;
                }
            }

        };
        addListener(SWT.Dispose, listener);
        addListener(SWT.MouseDown, listener);
        addListener(SWT.Paint, listener);
        addListener(SWT.Resize, listener);
        addListener(SWT.MouseEnter, listener);
        addListener(SWT.MouseExit, listener);
        button.addListener(SWT.MouseDown, listener);
        button.addListener(SWT.MouseExit, listener);
        button.addListener(SWT.MouseUp, listener);
        button.addListener(SWT.Paint, listener);

		// make sure stats are updated before first paint
		updateStats();

        getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (!isDisposed()) {
					getDisplay().timerExec(updateInterval, timer);
				}
			}
		});
   	}

	/**
	 * Returns the maximum memory limit, or Long.MAX_VALUE if the max is not known.
	 */
	private long getMaxMem() {
		long max = Long.MAX_VALUE;
		try {
			// Must use reflect to allow compilation against JCL/Foundation
			Method maxMemMethod = Runtime.class.getMethod("maxMemory", new Class[0]); //$NON-NLS-1$
			Object o = maxMemMethod.invoke(Runtime.getRuntime(), new Object[0]);
			if (o instanceof Long) {
				max = ((Long) o).longValue();
			}
		}
		catch (Exception e) {
			// ignore if method missing or if there are other failures trying to determine the max
		}
		return max;
	}
	
	private void setUpdateIntervalInMS(int interval) {
		updateInterval = Math.max(100, interval);
	}

	private void doDispose() {
        prefStore.removePropertyChangeListener(prefListener);
    	if (gcImage != null) {
			gcImage.dispose();
		}
       
        if (lowMemCol != null) {
			lowMemCol.dispose();
		}
        if (freeMemCol != null) {
			freeMemCol.dispose();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Composite#computeSize(int, int, boolean)
	 */
	public Point computeSize(int wHint, int hHint, boolean changed) {
        GC gc = new GC(this);
        Point p = gc.textExtent(WorkbenchMessages.HeapStatus_widthStr);
        int height = imgBounds.height;
        // choose the largest of 
        // 	- Text height + margins
        //	- Image height + margins
        //	- Default Trim heightin 
        height = Math.max(height, p.y) + 4;
        height = Math.max(TrimUtil.TRIM_DEFAULT_HEIGHT, height);
        gc.dispose();
		return new Point(p.x + 15, height);
	}
	
    private void arm(boolean armed) {
        if (this.armed == armed) {
			return;
		}
        this.armed = armed;
        button.redraw();
        button.update();
    }

    /**
     * Creates the context menu
     */
    private void createContextMenu() {
        MenuManager menuMgr = new MenuManager();
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager menuMgr) {
				fillMenu(menuMgr);
			}
		});
        Menu menu = menuMgr.createContextMenu(this);
        setMenu(menu);
    }
    
    private void fillMenu(IMenuManager menuMgr) {
        menuMgr.add(new SetMarkAction());
        menuMgr.add(new ClearMarkAction());
        menuMgr.add(new ShowMaxAction());
        menuMgr.add(new CloseHeapStatusAction());
//        if (isKyrsoftViewAvailable()) {
//        	menuMgr.add(new ShowKyrsoftViewAction());
//        }
    }

    /**
     * Sets the mark to the current usedMem level. 
     */
    private void setMark() {
    	updateStats();  // get up-to-date stats before taking the mark
        mark = usedMem;
        hasChanged = true;
        redraw();
    }

    /**
     * Clears the mark. 
     */
    private void clearMark() {
        mark = -1;
        hasChanged = true;
        redraw();
    }
    
    private void gc() {
    	BusyIndicator.showWhile(getDisplay(), new Runnable() {
			public void run() {
				Thread t = new Thread() {
					public void run() {
						busyGC();
					}};
				t.start();
				while(t.isAlive()) {
					try {
						Display d = getDisplay();
						while(d != null && !d.isDisposed() && d.readAndDispatch()) {
							// loop
						}
						t.join(10);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			}
		});
    }

    private void busyGC() {
        for (int i = 0; i < 2; ++i) {
	        System.gc();
	        System.runFinalization();
        }
    }
    
    private void paintButton(GC gc) {
        Rectangle rect = button.getClientArea();
        
        if (armed) {
            gc.setBackground(armCol);
            gc.fillRectangle(rect.x, rect.y, rect.width, rect.height);
        }
        if (gcImage != null) {
			int by = (rect.height - imgBounds.height) / 2 + rect.y; // button y
			gc.drawImage(gcImage, rect.x, by);
        }
    }

    private void paintComposite(GC gc) {
		if (showMax && maxMemKnown) {
			paintCompositeMaxKnown(gc);
		} else {
			paintCompositeMaxUnknown(gc);
		}
    }
    
    private void paintCompositeMaxUnknown(GC gc) {
        Rectangle rect = getClientArea();
        int x = rect.x;
        int y = rect.y;
        int w = rect.width;
        int h = rect.height;
        int bw = imgBounds.width; // button width
        int dx = x + w - bw - 2; // divider x
        int sw = w - bw - 3; // status width 
        int uw = (int) (sw * usedMem / totalMem); // used mem width
        int ux = x + 1 + uw; // used mem right edge
        
        gc.setBackground(bgCol);
        gc.fillRectangle(rect);
        gc.setForeground(sepCol);
		gc.drawLine(dx, y, dx, y + h);
		gc.drawLine(ux, y, ux, y + h);
        gc.setForeground(topLeftCol);
        gc.drawLine(x, y, x+w, y);
		gc.drawLine(x, y, x, y+h);
		gc.setForeground(bottomRightCol);
        gc.drawLine(x+w-1, y, x+w-1, y+h);
		gc.drawLine(x, y+h-1, x+w, y+h-1);
		
		gc.setBackground(usedMemCol);
        gc.fillRectangle(x + 1, y + 1, uw, h - 2);
        
        String s = NLS.bind(WorkbenchMessages.HeapStatus_status, convertToMegString(usedMem), convertToMegString(totalMem));
        Point p = gc.textExtent(s);
        int sx = (rect.width - 15 - p.x) / 2 + rect.x + 1;
        int sy = (rect.height - 2 - p.y) / 2 + rect.y + 1;
        gc.setForeground(textCol);
        gc.drawString(s, sx, sy, true);
        
        // draw an I-shaped bar in the foreground colour for the mark (if present)
        if (mark != -1) {
            int ssx = (int) (sw * mark / totalMem) + x + 1;
            paintMark(gc, ssx, y, h);
        }
    }

    private void paintCompositeMaxKnown(GC gc) {
        Rectangle rect = getClientArea();
        int x = rect.x;
        int y = rect.y;
        int w = rect.width;
        int h = rect.height;
        int bw = imgBounds.width; // button width
        int dx = x + w - bw - 2; // divider x
        int sw = w - bw - 3; // status width 
        int uw = (int) (sw * usedMem / maxMem); // used mem width
        int ux = x + 1 + uw; // used mem right edge
        int tw = (int) (sw * totalMem / maxMem); // current total mem width
        int tx = x + 1 + tw; // current total mem right edge
        
        gc.setBackground(bgCol);
        gc.fillRectangle(rect);
        gc.setForeground(sepCol);
		gc.drawLine(dx, y, dx, y + h);
		gc.drawLine(ux, y, ux, y + h);
		gc.drawLine(tx, y, tx, y + h);
        gc.setForeground(topLeftCol);
        gc.drawLine(x, y, x+w, y);
		gc.drawLine(x, y, x, y+h);
		gc.setForeground(bottomRightCol);
        gc.drawLine(x+w-1, y, x+w-1, y+h);
		gc.drawLine(x, y+h-1, x+w, y+h-1);
		
        if (lowMemThreshold != 0 && ((double)(maxMem - usedMem) / (double)maxMem < lowMemThreshold)) {
            gc.setBackground(lowMemCol);
        } else {
            gc.setBackground(usedMemCol);
        }
        gc.fillRectangle(x + 1, y + 1, uw, h - 2);
        
        gc.setBackground(freeMemCol);
        gc.fillRectangle(ux + 1, y + 1, tx - (ux + 1), h - 2);

        // paint line for low memory threshold
        if (showLowMemThreshold && lowMemThreshold != 0) {
            gc.setForeground(lowMemCol);
            int thresholdX = x + 1 + (int) (sw * (1.0 - lowMemThreshold));
            gc.drawLine(thresholdX, y + 1, thresholdX, y + h - 2);
        }

        String s = NLS.bind(WorkbenchMessages.HeapStatus_status, 
				convertToMegString(usedMem), convertToMegString(totalMem));
        Point p = gc.textExtent(s);
        int sx = (rect.width - 15 - p.x) / 2 + rect.x + 1;
        int sy = (rect.height - 2 - p.y) / 2 + rect.y + 1;
        gc.setForeground(textCol);
        gc.drawString(s, sx, sy, true);
        
        // draw an I-shaped bar in the foreground colour for the mark (if present)
        if (mark != -1) {
            int ssx = (int) (sw * mark / maxMem) + x + 1;
            paintMark(gc, ssx, y, h);
        }
    }

	private void paintMark(GC gc, int x, int y, int h) {
        gc.setForeground(markCol);
		gc.drawLine(x, y+1, x, y+h-2);
		gc.drawLine(x-1, y+1, x+1, y+1);
		gc.drawLine(x-1, y+h-2, x+1, y+h-2);
	}

    private void updateStats() {
        Runtime runtime = Runtime.getRuntime();
        totalMem = runtime.totalMemory();
        long freeMem = runtime.freeMemory();
        usedMem = totalMem - freeMem;

        if (convertToMeg(prevUsedMem) != convertToMeg(usedMem)) {
            prevUsedMem = usedMem;
            this.hasChanged = true;
        }
        
        if (prevTotalMem != totalMem) {
            prevTotalMem = totalMem;
            this.hasChanged = true;
        }
    }

    private void updateToolTip() {
    	String usedStr = convertToMegString(usedMem);
    	String totalStr = convertToMegString(totalMem);
    	String maxStr = maxMemKnown ? convertToMegString(maxMem) : WorkbenchMessages.HeapStatus_maxUnknown;
    	String markStr = mark == -1 ? WorkbenchMessages.HeapStatus_noMark : convertToMegString(mark);
        String toolTip = NLS.bind(WorkbenchMessages.HeapStatus_memoryToolTip, new Object[] { usedStr, totalStr, maxStr, markStr });
        if (!toolTip.equals(getToolTipText())) {
            setToolTipText(toolTip);
        }
    }
	
    /**
     * Converts the given number of bytes to a printable number of megabytes (rounded up).
     */
    private String convertToMegString(long numBytes) {
        return NLS.bind(WorkbenchMessages.HeapStatus_meg, new Long(convertToMeg(numBytes)));
    }

    /**
     * Converts the given number of bytes to the corresponding number of megabytes (rounded up).
     */
	private long convertToMeg(long numBytes) {
		return (numBytes + (512 * 1024)) / (1024 * 1024);
	}


    class SetMarkAction extends Action {
        SetMarkAction() {
            super(WorkbenchMessages.SetMarkAction_text);
        }
        
        public void run() {
            setMark();
        }
    }
    
    class ClearMarkAction extends Action {
        ClearMarkAction() {
            super(WorkbenchMessages.ClearMarkAction_text);
        }
        
        public void run() {
            clearMark();
        }
    }

    class ShowMaxAction extends Action {
    	ShowMaxAction() {
            super(WorkbenchMessages.ShowMaxAction_text, IAction.AS_CHECK_BOX);
            setEnabled(maxMemKnown);
            setChecked(showMax);
        }
        
        public void run() {
            prefStore.setValue(IHeapStatusConstants.PREF_SHOW_MAX, isChecked());
            redraw();
        }
    }

    class CloseHeapStatusAction extends Action{
    	
    	CloseHeapStatusAction(){
    		super(WorkbenchMessages.WorkbenchWindow_close );
    	}
    	
    	/* (non-Javadoc)
    	 * @see org.eclipse.jface.action.IAction#run()
    	 */
    	public void run(){
    		dispose();
    	}
    }

//    /**
//     * Returns whether the Kyrsoft memory monitor view is available.
//     * 
//     * @return <code>true</code> if available, <code>false</code> otherwise
//     */
//    private boolean isKyrsoftViewAvailable() {
//        return (Platform.getBundle(IHeapStatusConstants.KYRSOFT_PLUGIN_ID) != null) && PlatformUI.getWorkbench().getViewRegistry().find(IHeapStatusConstants.KYRSOFT_VIEW_ID) != null; 
//    }
//    
//    class ShowKyrsoftViewAction extends Action {
//        ShowKyrsoftViewAction() {
//            super(WorkbenchMessages.ShowKyrsoftViewAction_text);
//        }
//        public void run() {
//            if (!isKyrsoftViewAvailable()) { 
//                MessageDialog.openError(getShell(), WorkbenchMessages.HeapStatus_Error, WorkbenchMessages.ShowKyrsoftViewAction_KyrsoftNotInstalled);
//                return;
//            }
//			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
//            IWorkbenchPage page = window == null ? null : window.getActivePage();
//            if (page == null) {
//                MessageDialog.openError(getShell(), WorkbenchMessages.HeapStatus_Error, WorkbenchMessages.ShowKyrsoftViewAction_OpenPerspectiveFirst);
//                return;
//            }
//            try {
//                page.showView(IHeapStatusConstants.KYRSOFT_VIEW_ID);
//            }
//            catch (PartInitException e) {
//                String msg = WorkbenchMessages.ShowKyrsoftViewAction_ErrorShowingKyrsoftView;
//                IStatus status = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, msg, e);
//                ErrorDialog.openError(getShell(), WorkbenchMessages.HeapStatus_Error, msg, status);
//            }
//            
//        }
//    }

}

