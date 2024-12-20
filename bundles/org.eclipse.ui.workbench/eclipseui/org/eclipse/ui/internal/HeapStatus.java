/*******************************************************************************
 * Copyright (c) 2005, 2018 IBM Corporation and others.
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
 *     Brock Janicyak - brockj@tpg.com.au
 *     		- Fix for Bug 11142 [HeapStatus] Heap status is updated too frequently
 *          - Fix for Bug 192996 [Workbench] Reduce amount of garbage created by HeapStatus
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 422040, 372517, 463652, 466275
 *******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.util.PrefUtil;

/**
 * The Heap Status control, which shows the heap usage statistics in the window
 * trim.
 *
 * @since 3.1
 */
public class HeapStatus extends Composite {

	private boolean armed;
	private Image gcImage;
	private Image disabledGcImage;
	private Color bgCol, usedMemCol, lowMemCol, freeMemCol, topLeftCol, bottomRightCol, sepCol, textCol, markCol,
			armCol;
	private Canvas canvas;
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
	private Rectangle imgBounds = new Rectangle(0, 0, 12, 12);
	private final long maxMem;
	private boolean maxMemKnown;
	private float lowMemThreshold = 0.05f;
	private boolean showLowMemThreshold = true;
	private boolean updateTooltip = false;

	protected volatile boolean isInGC = false;

	private final Runnable timer = new Runnable() {
		@Override
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

	private final IPropertyChangeListener prefListener = event -> {
		if (IHeapStatusConstants.PREF_UPDATE_INTERVAL.equals(event.getProperty())) {
			setUpdateIntervalInMS(prefStore.getInt(IHeapStatusConstants.PREF_UPDATE_INTERVAL));
		} else if (IHeapStatusConstants.PREF_SHOW_MAX.equals(event.getProperty())) {
			showMax = prefStore.getBoolean(IHeapStatusConstants.PREF_SHOW_MAX);
		}
	};

	/**
	 * Creates a new heap status control with the given parent, and using the given
	 * preference store to obtain settings such as the refresh interval.
	 *
	 * @param parent    the parent composite
	 * @param prefStore the preference store
	 */
	public HeapStatus(Composite parent, IPreferenceStore prefStore) {
		super(parent, SWT.NONE);

		maxMem = Runtime.getRuntime().maxMemory();
		maxMemKnown = maxMem != Long.MAX_VALUE;

		this.prefStore = prefStore;
		prefStore.addPropertyChangeListener(prefListener);

		setUpdateIntervalInMS(prefStore.getInt(IHeapStatusConstants.PREF_UPDATE_INTERVAL));
		showMax = prefStore.getBoolean(IHeapStatusConstants.PREF_SHOW_MAX);

		canvas = new Canvas(this, SWT.NONE);
		canvas.setToolTipText(WorkbenchMessages.HeapStatus_buttonToolTip);

		ImageDescriptor imageDesc = WorkbenchImages.getWorkbenchImageDescriptor("elcl16/trash.svg"); //$NON-NLS-1$
		Display display = getDisplay();
		gcImage = imageDesc.createImage();
		if (gcImage != null) {
			imgBounds = gcImage.getBounds();
			disabledGcImage = new Image(display, gcImage, SWT.IMAGE_DISABLE);
		}

		usedMemCol = new Color(display, 160, 160, 160); // gray
		lowMemCol = new Color(display, 255, 70, 70); // medium red
		freeMemCol = new Color(display, 255, 190, 125); // light orange
		sepCol = topLeftCol = armCol = usedMemCol;
		bgCol = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
		bottomRightCol = display.getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW);
		markCol = textCol = display.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);

		createContextMenu();

		Listener listener = event -> {
			switch (event.type) {
			case SWT.Dispose:
				doDispose();
				break;
			case SWT.Resize:
				Rectangle rect = getClientArea();
				canvas.setBounds(rect.width - imgBounds.width - 1, 1, imgBounds.width, rect.height - 2);
				break;
			case SWT.Paint:
				if (event.widget == HeapStatus.this) {
					paintComposite(event.gc);
				} else if (event.widget == canvas) {
					paintButton(event.gc);
				}
				break;
			case SWT.MouseUp:
				if (event.button == 1) {
					if (!isInGC) {
						arm(false);
						gc();
					}
				}
				break;
			case SWT.MouseDown:
				if (event.button == 1) {
					if (event.widget == HeapStatus.this) {
						setMark();
					} else if (event.widget == canvas) {
						if (!isInGC)
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
				} else if (event.widget == canvas) {
					arm(false);
				}
				break;
			}
		};
		addListener(SWT.Dispose, listener);
		addListener(SWT.MouseDown, listener);
		addListener(SWT.Paint, listener);
		addListener(SWT.Resize, listener);
		addListener(SWT.MouseEnter, listener);
		addListener(SWT.MouseExit, listener);
		canvas.addListener(SWT.MouseDown, listener);
		canvas.addListener(SWT.MouseExit, listener);
		canvas.addListener(SWT.MouseUp, listener);
		canvas.addListener(SWT.Paint, listener);

		// make sure stats are updated before first paint
		updateStats();

		getDisplay().asyncExec(() -> {
			if (!isDisposed()) {
				getDisplay().timerExec(updateInterval, timer);
			}
		});
	}

	@Override
	public void setBackground(Color color) {
		bgCol = color;
		canvas.redraw();
	}

	@Override
	public void setForeground(Color color) {
		if (color == null) {
			markCol = textCol = getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
		} else {
			markCol = textCol = color;
		}

		canvas.redraw();
	}

	@Override
	public Color getForeground() {
		if (usedMemCol != null) {
			return usedMemCol;
		}
		return getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
	}

	private void setUpdateIntervalInMS(int interval) {
		updateInterval = Math.max(100, interval);
	}

	private void doDispose() {
		prefStore.removePropertyChangeListener(prefListener);
		if (gcImage != null) {
			gcImage.dispose();
		}
		if (disabledGcImage != null) {
			disabledGcImage.dispose();
		}

	}

	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		GC gc = new GC(this);
		Point p = gc.textExtent(WorkbenchMessages.HeapStatus_widthStr);
		int height = imgBounds.height;
		// choose the largest of
		// - Text height + margins
		// - Image height + margins
		// - Default Trim heightin
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
		canvas.redraw();
	}

	private void gcRunning(boolean isInGC) {
		if (this.isInGC == isInGC) {
			return;
		}
		this.isInGC = isInGC;
		canvas.redraw();
	}

	/**
	 * Creates the context menu
	 */
	private void createContextMenu() {
		MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(this::fillMenu);
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
		updateStats(); // get up-to-date stats before taking the mark
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
		gcRunning(true);
		Thread t = new Thread() {
			@Override
			public void run() {
				busyGC();
				getDisplay().asyncExec(() -> {
					if (!isDisposed()) {
						gcRunning(false);
					}
				});
			}
		};
		t.start();
	}

	private void busyGC() {
		for (int i = 0; i < 2; ++i) {
			System.gc();
			System.runFinalization();
		}
	}

	private void paintButton(GC gc) {
		Rectangle rect = canvas.getClientArea();
		if (isInGC) {
			if (disabledGcImage != null) {
				int buttonY = (rect.height - imgBounds.height) / 2 + rect.y;
				gc.drawImage(disabledGcImage, rect.x, buttonY);
			}
			return;
		}
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
		if (bgCol != null) {
			gc.setBackground(bgCol);
		}
		gc.fillRectangle(rect);
		gc.setForeground(sepCol);
		gc.drawLine(dx, y, dx, y + h);
		gc.drawLine(ux, y, ux, y + h);
		gc.setForeground(topLeftCol);
		gc.drawLine(x, y, x + w, y);
		gc.drawLine(x, y, x, y + h);
		gc.setForeground(bottomRightCol);
		gc.drawLine(x + w - 1, y, x + w - 1, y + h);
		gc.drawLine(x, y + h - 1, x + w, y + h - 1);

		gc.setBackground(usedMemCol);
		gc.fillRectangle(x + 1, y + 1, uw, h - 2);

		String s = NLS.bind(WorkbenchMessages.HeapStatus_status, convertToMegString(usedMem),
				convertToMegString(totalMem));
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

		if (bgCol != null) {
			gc.setBackground(bgCol);
		}
		gc.fillRectangle(rect);
		gc.setForeground(sepCol);
		gc.drawLine(dx, y, dx, y + h);
		gc.drawLine(ux, y, ux, y + h);
		gc.drawLine(tx, y, tx, y + h);
		gc.setForeground(topLeftCol);
		gc.drawLine(x, y, x + w, y);
		gc.drawLine(x, y, x, y + h);
		gc.setForeground(bottomRightCol);
		gc.drawLine(x + w - 1, y, x + w - 1, y + h);
		gc.drawLine(x, y + h - 1, x + w, y + h - 1);

		if (lowMemThreshold != 0 && ((double) (maxMem - usedMem) / (double) maxMem < lowMemThreshold)) {
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

		String s = NLS.bind(WorkbenchMessages.HeapStatus_status, convertToMegString(usedMem),
				convertToMegString(totalMem));
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
		gc.drawLine(x, y + 1, x, y + h - 2);
		gc.drawLine(x - 1, y + 1, x + 1, y + 1);
		gc.drawLine(x - 1, y + h - 2, x + 1, y + h - 2);
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
		String toolTip = NLS.bind(WorkbenchMessages.HeapStatus_memoryToolTip,
				new Object[] { usedStr, totalStr, maxStr, markStr });
		if (!toolTip.equals(getToolTipText())) {
			setToolTipText(toolTip);
		}
	}

	/**
	 * Converts the given number of bytes to a printable number of megabytes
	 * (rounded up).
	 */
	private String convertToMegString(long numBytes) {
		return NLS.bind(WorkbenchMessages.HeapStatus_meg, Long.valueOf(convertToMeg(numBytes)));
	}

	/**
	 * Converts the given number of bytes to the corresponding number of megabytes
	 * (rounded up).
	 */
	private long convertToMeg(long numBytes) {
		return (numBytes + (512 * 1024)) / (1024 * 1024);
	}

	class SetMarkAction extends Action {
		SetMarkAction() {
			super(WorkbenchMessages.SetMarkAction_text);
		}

		@Override
		public void run() {
			setMark();
		}
	}

	class ClearMarkAction extends Action {
		ClearMarkAction() {
			super(WorkbenchMessages.ClearMarkAction_text);
		}

		@Override
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

		@Override
		public void run() {
			prefStore.setValue(IHeapStatusConstants.PREF_SHOW_MAX, isChecked());
			redraw();
		}
	}

	static class CloseHeapStatusAction extends Action {

		CloseHeapStatusAction() {
			super(WorkbenchMessages.WorkbenchWindow_close);
		}

		@Override
		public void run() {
			WorkbenchWindow wbw = (WorkbenchWindow) PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (wbw != null) {
				wbw.showHeapStatus(false);
				PrefUtil.getAPIPreferenceStore().setValue(IWorkbenchPreferenceConstants.SHOW_MEMORY_MONITOR, false);
			}
		}
	}

}
