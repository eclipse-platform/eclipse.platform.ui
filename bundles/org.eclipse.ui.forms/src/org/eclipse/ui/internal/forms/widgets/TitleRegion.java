/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.forms.widgets;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.osgi.service.environment.Constants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEffect;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.ILayoutExtension;
import org.eclipse.ui.forms.widgets.SizeCache;
import org.eclipse.ui.forms.widgets.Twistie;
import org.eclipse.ui.internal.forms.IMessageToolTipManager;

/**
 * Form heading title.
 */
public class TitleRegion extends Canvas {
	public static final int STATE_NORMAL = 0;
	public static final int STATE_HOVER_LIGHT = 1;
	public static final int STATE_HOVER_FULL = 2;
	private int hoverState;
	private static final int HMARGIN = 1;
	private static final int VMARGIN = 5;
	private static final int SPACING = 5;
	private static final int ARC_WIDTH = 20;
	private static final int ARC_HEIGHT = 20;
	private Image image;
	private BusyIndicator busyLabel;
	private Label titleLabel;
	private SizeCache titleCache;
	private int fontHeight = -1;
	private int fontBaselineHeight = -1;
	private MenuHyperlink menuHyperlink;
	private MenuManager menuManager;
	private boolean dragSupport;
	private int dragOperations;
	private Transfer[] dragTransferTypes;
	private DragSourceListener dragListener;
	private DragSource dragSource;
	private Image dragImage;

	private class HoverListener implements MouseTrackListener,
			MouseMoveListener {

		public void mouseEnter(MouseEvent e) {
			setHoverState(STATE_HOVER_FULL);
		}

		public void mouseExit(MouseEvent e) {
			setHoverState(STATE_NORMAL);
		}

		public void mouseHover(MouseEvent e) {
		}

		public void mouseMove(MouseEvent e) {
			if (e.button > 0)
				setHoverState(STATE_NORMAL);
			else
				setHoverState(STATE_HOVER_FULL);
		}
	}

	private class MenuHyperlink extends Twistie {
		private boolean firstTime = true;

		public MenuHyperlink(Composite parent, int style) {
			super(parent, style);
			setExpanded(true);
		}

		public void setExpanded(boolean expanded) {
			if (firstTime) {
				super.setExpanded(expanded);
				firstTime = false;
			} else {
				Menu menu = menuManager.createContextMenu(menuHyperlink);
				menu.setVisible(true);
			}
		}
	}

	private class TitleRegionLayout extends Layout implements ILayoutExtension {

		protected Point computeSize(Composite composite, int wHint, int hHint,
				boolean flushCache) {
			return layout(composite, false, 0, 0, wHint, hHint, flushCache);
		}

		protected void layout(Composite composite, boolean flushCache) {
			Rectangle carea = composite.getClientArea();
			layout(composite, true, carea.x, carea.y, carea.width,
					carea.height, flushCache);
		}

		private Point layout(Composite composite, boolean move, int x, int y,
				int width, int height, boolean flushCache) {
			int iwidth = width == SWT.DEFAULT ? SWT.DEFAULT : width - HMARGIN
					* 2;
			Point bsize = null;
			Point tsize = null;
			Point msize = null;

			if (busyLabel != null) {
				bsize = busyLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			}
			if (menuManager != null) {
				menuHyperlink.setVisible(!menuManager.isEmpty()
						&& titleLabel.getVisible());
				if (menuHyperlink.getVisible())
					msize = menuHyperlink.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			}
			if (flushCache)
				titleCache.flush();
			titleCache.setControl(titleLabel);
			int twidth = iwidth == SWT.DEFAULT ? iwidth : iwidth - SPACING * 2;
			if (bsize != null && twidth != SWT.DEFAULT)
				twidth -= bsize.x + SPACING;
			if (msize != null && twidth != SWT.DEFAULT)
				twidth -= msize.x + SPACING;
			if (titleLabel.getVisible()) {
				tsize = titleCache.computeSize(twidth, SWT.DEFAULT);
				if (twidth != SWT.DEFAULT) {
					// correct for the case when width hint is larger
					// than the maximum width - this is when the text
					// can be rendered on one line with width to spare
					int maxWidth = titleCache.computeSize(SWT.DEFAULT,
							SWT.DEFAULT).x;
					tsize.x = Math.min(tsize.x, maxWidth);
					// System.out.println("twidth="+twidth+",
					// tsize.x="+tsize.x); //$NON-NLS-1$//$NON-NLS-2$
				}
			} else
				tsize = new Point(0, 0);
			Point size = new Point(width, height);
			if (!move) {
				// compute size
				size.x = tsize.x > 0 ? HMARGIN * 2 + SPACING * 2 + tsize.x : 0;
				size.y = tsize.y;
				if (bsize != null) {
					size.x += bsize.x + SPACING;
					size.y = Math.max(size.y, bsize.y);
				}
				if (msize != null) {
					size.x += msize.x + SPACING;
					size.y = Math.max(size.y, msize.y);
				}
				if (size.y > 0)
					size.y += VMARGIN * 2;
				// System.out.println("Compute size: width="+width+",
				// size.x="+size.x); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				// position controls
				int xloc = x + HMARGIN + SPACING;
				int yloc = y + VMARGIN;
				if (bsize != null) {
					busyLabel.setBounds(xloc,
							// yloc + height / 2 - bsize.y / 2,
							yloc + (getFontHeight() >= bsize.y ? getFontHeight() : bsize.y) - 1 - bsize.y,
							bsize.x, bsize.y);
					xloc += bsize.x + SPACING;
				}
				if (titleLabel.getVisible()) {
					int tw = width - HMARGIN * 2 - SPACING * 2;
					String os = System.getProperty("os.name"); //$NON-NLS-1$
					if (Constants.OS_LINUX.equalsIgnoreCase(os)) {
						tw += 1; // See Bug 342610
					}
					if (bsize != null)
						tw -= bsize.x + SPACING;
					if (msize != null)
						tw -= msize.x + SPACING;
					titleLabel.setBounds(xloc,
					// yloc + height / 2 - tsize.y / 2,
							yloc, tw, tsize.y);
					// System.out.println("tw="+tw); //$NON-NLS-1$
					xloc += tw + SPACING;
				}
				if (msize != null) {
					menuHyperlink.setBounds(xloc, yloc
							+ getFontHeight() / 2 - msize.y / 2,
							msize.x, msize.y);
				}
			}
			return size;
		}

		public int computeMaximumWidth(Composite parent, boolean changed) {
			return computeSize(parent, SWT.DEFAULT, SWT.DEFAULT, changed).x;
		}

		public int computeMinimumWidth(Composite parent, boolean changed) {
			return computeSize(parent, 0, SWT.DEFAULT, changed).x;
		}
	}

	public TitleRegion(Composite parent) {
		super(parent, SWT.NULL);
		titleLabel = new Label(this, SWT.WRAP);
		titleLabel.setVisible(false);
		titleCache = new SizeCache();
		super.setLayout(new TitleRegionLayout());
		hookHoverListeners();
		addListener(SWT.Dispose, new Listener() {
			public void handleEvent(Event e) {
				if (dragImage != null) {
					dragImage.dispose();
					dragImage = null;
				}
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#forceFocus()
	 */
	public boolean forceFocus() {
		return false;
	}

	private Color getColor(String key) {
		return (Color) ((FormHeading) getParent()).colors.get(key);
	}

	private void hookHoverListeners() {
		HoverListener listener = new HoverListener();
		addMouseTrackListener(listener);
		addMouseMoveListener(listener);
		titleLabel.addMouseTrackListener(listener);
		titleLabel.addMouseMoveListener(listener);
		addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				onPaint(e);
			}
		});
	}

	private void onPaint(PaintEvent e) {
		if (hoverState == STATE_NORMAL)
			return;
		GC gc = e.gc;
		Rectangle carea = getClientArea();
		gc.setBackground(getHoverBackground());
		int savedAntialias = gc.getAntialias();
		FormUtil.setAntialias(gc, SWT.ON);
		gc.fillRoundRectangle(carea.x + HMARGIN, carea.y + 2, carea.width
				- HMARGIN * 2, carea.height - 4, ARC_WIDTH, ARC_HEIGHT);
		FormUtil.setAntialias(gc, savedAntialias);
	}

	private Color getHoverBackground() {
		if (hoverState == STATE_NORMAL)
			return null;
		Color color = getColor(hoverState == STATE_HOVER_FULL ? IFormColors.H_HOVER_FULL
				: IFormColors.H_HOVER_LIGHT);
		if (color == null)
			color = getDisplay()
					.getSystemColor(
							hoverState == STATE_HOVER_FULL ? SWT.COLOR_WIDGET_BACKGROUND
									: SWT.COLOR_WIDGET_LIGHT_SHADOW);
		return color;
	}

	public void setHoverState(int state) {
		if (dragSource == null || this.hoverState == state)
			return;
		this.hoverState = state;
		Color color = getHoverBackground();
		titleLabel.setBackground(color != null ? color
				: getColor(FormHeading.COLOR_BASE_BG));
		if (busyLabel != null)
			busyLabel.setBackground(color != null ? color
					: getColor(FormHeading.COLOR_BASE_BG));
		if (menuHyperlink != null)
			menuHyperlink.setBackground(color != null ? color
					: getColor(FormHeading.COLOR_BASE_BG));
		redraw();
	}

	/**
	 * Fully delegates the size computation to the internal layout manager.
	 */
	public final Point computeSize(int wHint, int hHint, boolean changed) {
		return ((TitleRegionLayout) getLayout()).computeSize(this, wHint,
				hHint, changed);
	}

	public final void setLayout(Layout layout) {
		// do nothing
	}

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
	}

	public void updateImage(Image newImage, boolean doLayout) {
		Image theImage = newImage != null ? newImage : this.image;

		if (theImage != null) {
			ensureBusyLabelExists();
		} else if (busyLabel != null) {
			if (!busyLabel.isBusy()) {
				busyLabel.dispose();
				busyLabel = null;
			}
		}
		if (busyLabel != null) {
			busyLabel.setImage(theImage);
		}
		if (doLayout)
			layout();
	}

	public void updateToolTip(String toolTip) {
		if (busyLabel != null)
			busyLabel.setToolTipText(toolTip);
	}

	public void setBackground(Color bg) {
		super.setBackground(bg);
		titleLabel.setBackground(bg);
		if (busyLabel != null)
			busyLabel.setBackground(bg);
		if (menuHyperlink != null)
			menuHyperlink.setBackground(bg);
	}

	public void setForeground(Color fg) {
		super.setForeground(fg);
		titleLabel.setForeground(fg);
		if (menuHyperlink != null)
			menuHyperlink.setForeground(fg);
	}

	public void setText(String text) {
		if (text != null)
			titleLabel.setText(text);
		titleLabel.setVisible(text != null);
		layout();
		redraw();
	}

	public String getText() {
		return titleLabel.getText();
	}

	public void setFont(Font font) {
		super.setFont(font);
		titleLabel.setFont(font);
		fontHeight = -1;
		fontBaselineHeight = -1;
		layout();
	}

	private void ensureBusyLabelExists() {
		if (busyLabel == null) {
			busyLabel = new BusyIndicator(this, SWT.NULL);
			busyLabel.setBackground(getColor(FormHeading.COLOR_BASE_BG));
			HoverListener listener = new HoverListener();
			busyLabel.addMouseTrackListener(listener);
			busyLabel.addMouseMoveListener(listener);
			if (menuManager != null)
				busyLabel.setMenu(menuManager.createContextMenu(this));
			if (dragSupport)
				addDragSupport(busyLabel, dragOperations, dragTransferTypes, dragListener);
			IMessageToolTipManager mng = ((FormHeading) getParent())
					.getMessageToolTipManager();
			if (mng != null)
				mng.createToolTip(busyLabel, true);
		}
	}

	private void createMenuHyperlink() {
		menuHyperlink = new MenuHyperlink(this, SWT.NULL);
		menuHyperlink.setBackground(getColor(FormHeading.COLOR_BASE_BG));
		menuHyperlink.setDecorationColor(getForeground());
		menuHyperlink.setHoverDecorationColor(getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND));
		HoverListener listener = new HoverListener();
		menuHyperlink.addMouseTrackListener(listener);
		menuHyperlink.addMouseMoveListener(listener);
		if (dragSupport)
			addDragSupport(menuHyperlink, dragOperations, dragTransferTypes, dragListener);
	}

	/**
	 * Sets the form's busy state. Busy form will display 'busy' animation in
	 * the area of the title image.
	 * 
	 * @param busy
	 *            the form's busy state
	 */

	public boolean setBusy(boolean busy) {
		if (busy)
			ensureBusyLabelExists();
		else if (busyLabel == null)
			return false;
		if (busy == busyLabel.isBusy())
			return false;
		busyLabel.setBusy(busy);
		if (busyLabel.getImage() == null) {
			layout();
			return true;
		}
		return false;
	}

	public boolean isBusy() {
		return busyLabel != null && busyLabel.isBusy();
	}

	/*
	 * Returns the complete height of the font.
	 */
	public int getFontHeight() {
		if (fontHeight == -1) {
			Font font = getFont();
			GC gc = new GC(getDisplay());
			gc.setFont(font);
			FontMetrics fm = gc.getFontMetrics();
			fontHeight = fm.getHeight();
			gc.dispose();
		}
		return fontHeight;
	}

	/*
	 * Returns the height of the font starting at the baseline,
	 * i.e. without the descent.
	 */
	public int getFontBaselineHeight() {
		if (fontBaselineHeight == -1) {
			Font font = getFont();
			GC gc = new GC(getDisplay());
			gc.setFont(font);
			FontMetrics fm = gc.getFontMetrics();
			fontBaselineHeight = fm.getHeight() - fm.getDescent();
			gc.dispose();
		}
		return fontBaselineHeight;
	}

	public IMenuManager getMenuManager() {
		if (menuManager == null) {
			menuManager = new MenuManager();
			Menu menu = menuManager.createContextMenu(this);
			setMenu(menu);
			titleLabel.setMenu(menu);
			if (busyLabel != null)
				busyLabel.setMenu(menu);
			createMenuHyperlink();
		}
		return menuManager;
	}

	public void addDragSupport(int operations, Transfer[] transferTypes,
			DragSourceListener listener) {
		dragSupport = true;
		dragOperations = operations;
		dragTransferTypes = transferTypes;
		dragListener = listener;
		dragSource = addDragSupport(titleLabel, operations, transferTypes,
				listener);
		addDragSupport(this, operations, transferTypes, listener);
		if (busyLabel != null)
			addDragSupport(busyLabel, operations, transferTypes, listener);
		if (menuHyperlink != null)
			addDragSupport(menuHyperlink, operations, transferTypes, listener);
	}

	private DragSource addDragSupport(Control control, int operations,
			Transfer[] transferTypes, DragSourceListener listener) {
		DragSource source = new DragSource(control, operations);
		source.setTransfer(transferTypes);
		source.addDragListener(listener);
		source.setDragSourceEffect(new DragSourceEffect(control) {
			public void dragStart(DragSourceEvent event) {
				event.image = createDragEffectImage();
			}
		});
		return source;
	}

	private Image createDragEffectImage() {
		/*
		 * if (dragImage != null) { dragImage.dispose(); } GC gc = new GC(this);
		 * Point size = getSize(); dragImage = new Image(getDisplay(), size.x,
		 * size.y); gc.copyArea(dragImage, 0, 0); gc.dispose(); return
		 * dragImage;
		 */
		return null;
	}

	public void addDropSupport(int operations, Transfer[] transferTypes,
			DropTargetListener listener) {
		final DropTarget target = new DropTarget(this, operations);
		target.setTransfer(transferTypes);
		target.addDropListener(listener);
	}
}