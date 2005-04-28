/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
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
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * The Heap Status control, which shows the heap usage statistics in the window trim.
 * 
 * @since 3.1
 */
public class HeapStatus extends Composite {

	private boolean armed;
	private Image gcImage;
	private Color bgCol, usedMemCol, topLeftCol, bottomRightCol, sepCol, textCol, markCol;  
    private Canvas button;
	private IPreferenceStore prefStore;

    private final Runnable timer = new Runnable() {

        public void run() {
            if (!isDisposed()) {
                updateStats();
                updateToolTip();
                adjustPosition();
                redraw();
                getDisplay().timerExec(prefStore.getInt(IHeapStatusConstants.PREF_UPDATE_INTERVAL), this);
            }
        }
    };

    private long totalMem;
    private long usedMem;
    private long mark = -1;


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
        
        this.prefStore = prefStore;
		
        button = new Canvas(this, SWT.NONE);
        button.setToolTipText(WorkbenchMessages.HeapStatus_buttonToolTip);
        
		ImageDescriptor imageDesc = WorkbenchImages.getWorkbenchImageDescriptor("obj16/trash.gif"); //$NON-NLS-1$
		gcImage = imageDesc.createImage();
		Display display = getDisplay();
		usedMemCol = new Color(display, 255, 255, 175);
		bgCol = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
		sepCol = topLeftCol = display.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
		bottomRightCol = display.getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW);
		markCol = textCol = display.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
		
		createContextMenu();
		
        Listener listener = new Listener() {

            public void handleEvent(Event event) {
                switch (event.type) {
                case SWT.Dispose:
                	doDispose();
                    break;
                case SWT.Resize:
                    Rectangle rect = getClientArea();
                    button.setBounds(rect.width - 13, 1, 12, rect.height - 2);
                    break;
                case SWT.Paint:
                    if (event.widget == HeapStatus.this)
                        paintComposite(event.gc);
                    else if (event.widget == button)
                        paintButton(event.gc);
                    break;
                case SWT.MouseUp:
                    if (event.button == 1) {
                        gc();
                        arm(false);
                    }
                    break;
                case SWT.MouseDown:
                    if (event.button == 1) {
	                    if (event.widget == HeapStatus.this)
	                        setMark();
	                    else if (event.widget == button)
	                        arm(true);
                    }
                    break;
                case SWT.MouseExit:
                    arm(false);
                    break;
                }
            }

        };
        addListener(SWT.Dispose, listener);
        addListener(SWT.MouseDown, listener);
        addListener(SWT.Paint, listener);
        addListener(SWT.Resize, listener);
        button.addListener(SWT.MouseDown, listener);
        button.addListener(SWT.MouseExit, listener);
        button.addListener(SWT.MouseUp, listener);
        button.addListener(SWT.Paint, listener);

		// make sure stats are updated before first paint
		updateStats();

        getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (!isDisposed()) {
					getDisplay().timerExec(HeapStatus.this.prefStore.getInt(IHeapStatusConstants.PREF_UPDATE_INTERVAL), timer);
				}
			}
		});
   	}
	
	private void doDispose() {
    	if (gcImage != null)
    		gcImage.dispose();
        if (usedMemCol != null)
        	usedMemCol.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Composite#computeSize(int, int, boolean)
	 */
	public Point computeSize(int wHint, int hHint, boolean changed) {
        GC gc = new GC(this);
        Point p = gc.textExtent(WorkbenchMessages.HeapStatus_widthStr);
        gc.dispose();
		return new Point(p.x + 15, 14);
	}
	
    private void arm(boolean armed) {
        if (this.armed == armed)
            return;
        this.armed = armed;
        button.redraw();
        button.update();
    }

    /**
     * Creates the context menu
     */
    private void createContextMenu() {
        MenuManager menuMgr = new MenuManager();
        menuMgr.add(new SetMarkAction());
        menuMgr.add(new ClearMarkAction());
        menuMgr.add(new ShowKyrsoftViewAction());
        Menu menu = menuMgr.createContextMenu(this);
        setMenu(menu);
    }

    /**
     * Sets the mark to the current usedMem level. 
     */
    private void setMark() {
        mark = usedMem;
        redraw();
    }

    /**
     * Clears the mark. 
     */
    private void clearMark() {
        mark = -1;
        redraw();
    }
    
    private void gc() {
    	BusyIndicator.showWhile(getDisplay(), new Runnable() {
			public void run() {
				busyGC();
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
        Display display = button.getDisplay();
        Rectangle rect = button.getClientArea();
        
        if (armed) {
            gc.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
        }
        gc.fillRectangle(rect.x, rect.y, rect.width, rect.height);
        if (gcImage != null) {
			int by = (rect.height - 12) / 2 + rect.y; // button y
			gc.drawImage(gcImage, rect.x, by);
        }
    }

    private void paintComposite(GC gc) {
        Rectangle rect = getClientArea();
        int x = rect.x;
        int y = rect.y;
        int w = rect.width;
        int h = rect.height;
        int bw = 12; // button width
        int dx = x + w - bw - 2; // divider x
        int sw = w - bw - 3; // status width 
        int uw = (int) (sw * usedMem / totalMem); // used mem width
        int ux = x + 1 + uw; // used mem right edge
        
        gc.setBackground(bgCol);
        gc.fillRectangle(rect);
        gc.setForeground(sepCol);
		gc.drawLine(dx, rect.y, dx, y + h);
		gc.drawLine(ux, rect.y, ux, y + h);
        gc.setForeground(topLeftCol);
        gc.drawLine(x, y, x+w, y);
		gc.drawLine(x, y, x, y+h);
		gc.setForeground(bottomRightCol);
        gc.drawLine(x+w-1, y, x+w-1, y+h);
		gc.drawLine(x, y+h-1, x+w, y+h-1);
		
		if (usedMemCol != null) {
			gc.setBackground(usedMemCol);
		}
        gc.fillRectangle(x + 1, y + 1, uw, y + h - 2);
        
        String s = NLS.bind(WorkbenchMessages.HeapStatus_status, new Integer(
				convertToMeg(usedMem)), new Integer(convertToMeg(totalMem)));
        Point p = gc.textExtent(s);
        int sx = (rect.width - 15 - p.x) / 2 + rect.x + 1;
        int sy = (rect.height - 2 - p.y) / 2 + rect.y + 1;
        gc.setForeground(textCol);
        gc.drawString(s, sx, sy, true);
        
        // draw an I-shaped bar in the foreground colour for the mark (if present)
        gc.setForeground(markCol);
        if (mark != -1) {
            int ssx = (int) (sw * mark / totalMem) + x + 1;
            gc.drawLine(ssx, y+1, ssx, y+h-2);
//            gc.drawLine(ssx-2, y+1, ssx+2, y+1);
            gc.drawLine(ssx-1, y+1, ssx+1, y+1);
            gc.drawLine(ssx-1, y+h-2, ssx+1, y+h-2);
//            gc.drawLine(ssx-2, y+h-2, ssx+2, y+h-2);
        }
        
    }

    private void updateStats() {
        Runtime runtime = Runtime.getRuntime();
        totalMem = runtime.totalMemory();
        long freeMem = runtime.freeMemory();
        usedMem = totalMem - freeMem;
    }

    private void updateToolTip() {
        String toolTip;
        if (mark == -1) {
	        toolTip = NLS
					.bind(WorkbenchMessages.HeapStatus_memoryToolTip, new Integer(
							convertToMeg(totalMem)), new Integer(
							convertToMeg(usedMem)));
        }
        else {
	        toolTip = NLS.bind(
					WorkbenchMessages.HeapStatus_memoryToolTipWithMark,
					new Object[] { new Integer(convertToMeg(totalMem)),
							new Integer(convertToMeg(usedMem)),
							new Integer(convertToMeg(mark)) });
        }
        if (!toolTip.equals(getToolTipText()))
            setToolTipText(toolTip);
    }
	
    /**
     * Converts the given number of bytes to number of megabytes (rounded up).
     */
    private int convertToMeg(long totalMem2) {
        return (int) ((totalMem2 + (512 * 1024)) / (1024 * 1024));
    }


    protected void adjustPosition() {
		// do nothing
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
    
    class ShowKyrsoftViewAction extends Action {
        ShowKyrsoftViewAction() {
            super(WorkbenchMessages.ShowKyrsoftViewAction_text);
        }
        public void run() {
            if (Platform.getBundle(IHeapStatusConstants.KYRSOFT_PLUGIN_ID) == null) { 
                MessageDialog.openError(getShell(), WorkbenchMessages.HeapStatus_Error, WorkbenchMessages.ShowKyrsoftViewAction_KyrsoftNotInstalled);
                return;
            }
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            IWorkbenchPage page = window == null ? null : window.getActivePage();
            if (page == null) {
                MessageDialog.openError(getShell(), WorkbenchMessages.HeapStatus_Error, WorkbenchMessages.ShowKyrsoftViewAction_OpenPerspectiveFirst);
                return;
            }
            try {
                page.showView(IHeapStatusConstants.KYRSOFT_VIEW_ID);
            }
            catch (PartInitException e) {
                String msg = WorkbenchMessages.ShowKyrsoftViewAction_ErrorShowingKyrsoftView;
                IStatus status = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, msg, e);
                ErrorDialog.openError(getShell(), WorkbenchMessages.HeapStatus_Error, msg, status);
            }
            
        }
    }
    

}

