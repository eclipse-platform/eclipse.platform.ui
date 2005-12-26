/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.forms.widgets;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ILayoutExtension;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.SizeCache;
import org.eclipse.ui.internal.forms.Messages;

/**
 * Form header moved out of the form class.
 */
public class FormHeading extends Composite {
	private int TITLE_HMARGIN = 10;

	private int TITLE_VMARGIN = 5;

	private int TITLE_GAP = 5;

	private static final int S_TILED = 1 << 1;

	private static final int S_CLIPPED = 1 << 2;

	private static final int S_SEPARATOR = 1 << 3;

	private int style = S_CLIPPED | SWT.LEFT;

	private Image gradientImage;

	private Image image;

	private Color baseBg;

	private Color separatorColor;

	private GradientInfo gradientInfo;

	private String text;

	private ToolBarManager toolBarManager;

	private SizeCache titleCache = new SizeCache();

	private SizeCache toolbarCache = new SizeCache();

	private SizeCache clientCache = new SizeCache();

	private BusyIndicator busyLabel;

	private Label titleLabel;

	private Control headClient;

	private MessageArea messageArea;

	private class GradientInfo {
		Color[] gradientColors;

		int[] percents;

		boolean vertical;
	}

	private class MessageArea extends Composite {
		static final int BUTTON_BORDER = SWT.COLOR_WIDGET_DARK_SHADOW;

		static final int BUTTON_FILL = SWT.COLOR_LIST_BACKGROUND;

		static final int BUTTON_SIZE = 18;

		private Image normal;

		private Image hot;

		static final int CLOSED = 0;

		static final int OPENNING = 1;

		static final int OPEN = 2;

		static final int CLOSING = 3;

		private CLabel label;

		private ImageHyperlink rlink;

		private ImageHyperlink mlink;

		private int state = CLOSED;

		private boolean minimized;

		private boolean animationStart;

		public MessageArea(Composite parent, int style) {
			super(parent, SWT.NULL);
			Composite container = new Composite(this, SWT.NULL);
			GridLayout glayout = new GridLayout();
			glayout.numColumns = 2;
			glayout.marginWidth = 0;
			glayout.marginHeight = 0;
			container.setLayout(glayout);
			label = new CLabel(container, SWT.NULL);
			label.setLayoutData(new GridData(GridData.FILL_BOTH));
			mlink = new ImageHyperlink(container, SWT.NULL);
			mlink.addHyperlinkListener(new HyperlinkAdapter() {
				public void linkActivated(HyperlinkEvent e) {
					setMinimized(true);
				}
			});
			mlink.setToolTipText(Messages.Form_tooltip_minimize);
			rlink = new ImageHyperlink(this, SWT.NULL);
			rlink.addHyperlinkListener(new HyperlinkAdapter() {
				public void linkActivated(HyperlinkEvent e) {
					setMinimized(false);
				}
			});
			if (gradientInfo == null && getBackgroundImage() == null)
				rlink.setBackground(getBackground());
			rlink.setVisible(false);
			rlink.setToolTipText(Messages.Form_tooltip_restore);
			createMinimizedImages();
			addPaintListener(new PaintListener() {
				public void paintControl(PaintEvent e) {
					onPaint(e);
				}
			});
			addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					disposeMinimizeImages();
				}
			});
			setLayout(new Layout() {
				public void layout(Composite parent, boolean changed) {
					Rectangle carea = getClientArea();
					if (minimized) {
						rlink.setBounds(carea.x, carea.y, carea.width,
								carea.height);
					} else {
						label.getParent().setBounds(carea.x + 2, carea.y + 2,
								carea.width - 4, carea.height - 4);
					}
				}

				public Point computeSize(Composite parent, int wHint,
						int hHint, boolean changed) {
					Point size = new Point(0, 0);
					if (minimized)
						size = rlink.computeSize(wHint, hHint, changed);
					else
						size = label.getParent().computeSize(wHint, hHint,
								changed);
					if (!minimized) {
						size.x += 4;
						size.y += 4;
					}
					return size;
				}
			});
		}

		public void setMinimized(boolean minimized) {
			setBackground(minimized ? null : baseBg);
			this.minimized = minimized;
			if (minimized) {
				rlink.setImage(label.getImage());
			}
			rlink.setVisible(minimized);
			label.getParent().setVisible(!minimized);
			layout();

			FormHeading.this.layout();
			FormHeading.this.redraw();
		}

		public boolean isMinimized() {
			return minimized;
		}

		public synchronized void setState(int state) {
			this.state = state;
			if (state == OPENNING)
				setVisible(true);
			else if (state == CLOSED)
				setVisible(false);
		}

		public int getState() {
			return state;
		}

		public void setBackground(Color bg) {
			super.setBackground(bg);
			label.setBackground(bg);
			mlink.setBackground(bg);
			rlink.setBackground(bg);
			label.getParent().setBackground(bg);
			createMinimizedImages();
		}

		public void setText(String text) {
			this.label.setText(text);
		}

		public void setImage(Image image) {
			this.label.setImage(image);
		}

		public boolean isInTransition() {
			return state == OPENNING || state == CLOSING;
		}

		private void onPaint(PaintEvent e) {
			if (minimized)
				return;
			Rectangle carea = getClientArea();
			e.gc.setForeground(getForeground());
			e.gc.drawPolyline(new int[] { carea.x, carea.y + carea.height - 1,
					carea.x, carea.y + 2, carea.x + 2, carea.y,
					carea.x + carea.width - 3, carea.y,
					carea.x + carea.width - 1, carea.y + 2,
					carea.x + carea.width - 1, carea.y + carea.height - 1 });
		}

		public boolean isAnimationStart() {
			return animationStart;
		}

		public void setAnimationStart(boolean animationStart) {
			this.animationStart = animationStart;
		}

		private void createMinimizedImages() {
			disposeMinimizeImages();
			normal = new Image(getDisplay(), BUTTON_SIZE, BUTTON_SIZE);
			GC gc = new GC(normal);
			paintNormalImage(getDisplay(), gc);
			gc.dispose();
			hot = new Image(getDisplay(), BUTTON_SIZE, BUTTON_SIZE);
			gc = new GC(hot);
			paintHotImage(getDisplay(), gc);
			gc.dispose();
			mlink.setImage(normal);
			mlink.setHoverImage(hot);
		}

		private void disposeMinimizeImages() {
			if (normal != null) {
				normal.dispose();
				normal = null;
			}
			if (hot != null) {
				hot.dispose();
				hot = null;
			}
		}

		private void paintNormalImage(Display display, GC gc) {
			gc.setForeground(display.getSystemColor(BUTTON_BORDER));
			// gc.setBackground(display.getSystemColor(BUTTON_FILL));
			gc.setBackground(getBackground());
			paintInnerContent(gc);
		}

		private void paintHotImage(Display display, GC gc) {
			gc.setForeground(display.getSystemColor(BUTTON_BORDER));
			// gc.setBackground(display.getSystemColor(BUTTON_FILL));
			// gc.setBackground(getBackground());
			// gc.fillRoundRectangle(0, 0, BUTTON_SIZE, BUTTON_SIZE, 6, 6);
			gc.drawRoundRectangle(0, 0, BUTTON_SIZE - 1, BUTTON_SIZE - 1, 6, 6);
			paintInnerContent(gc);
		}

		private void paintInnerContent(GC gc) {
			int x = BUTTON_SIZE / 2 - 5;
			int y = 2;
			gc.fillRectangle(x, y, 9, 3);
			gc.drawRectangle(x, y, 9, 3);
		}
	}

	private class FormHeadingLayout extends Layout implements ILayoutExtension {
		public int computeMinimumWidth(Composite composite, boolean flushCache) {
			return computeSize(composite, 5, SWT.DEFAULT, flushCache).x;
		}

		public int computeMaximumWidth(Composite composite, boolean flushCache) {
			return computeSize(composite, SWT.DEFAULT, SWT.DEFAULT, flushCache).x;
		}

		public Point computeSize(Composite composite, int wHint, int hHint,
				boolean flushCache) {
			if (flushCache) {
				titleCache.flush();
				toolbarCache.flush();
				clientCache.flush();
			}

			int width = 0;
			int height = 0;
			Point tbsize = null;

			if (headClient != null) {
				clientCache.setControl(headClient);
				Point clsize = clientCache
						.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				if (text != null)
					width += TITLE_GAP;
				width += clsize.x;
				height += clsize.y;
			}

			if (toolBarManager != null) {
				ToolBar toolBar = toolBarManager.getControl();
				if (toolBar != null) {
					toolbarCache.setControl(toolBar);
					tbsize = toolbarCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
					if (headClient == null) {
						if (text != null)
							width += TITLE_GAP;
						width += tbsize.x;
						height = tbsize.y;
					}
				}
			}
			int iwidth = 0;
			int iheight = 0;
			if (messageArea != null && messageArea.isMinimized()) {
				Point rsize = messageArea.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				iwidth = Math.max(iwidth, rsize.x);
				iheight = Math.max(iheight, rsize.y);
			}
			if (busyLabel != null) {
				Point bsize = busyLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				iwidth = Math.max(iwidth, bsize.x);
				iheight = Math.max(iheight, bsize.y);
			}
			if (iwidth > 0) {
				if (text != null)
					width += TITLE_GAP;
				width += iwidth;
				height = Math.max(height, iheight);
			}
			if (text != null) {
				GC gc = new GC(composite);
				gc.setFont(getFont());

				if (wHint != SWT.DEFAULT) {
					int twHint = wHint - width;
					Point wsize = FormUtil.computeWrapSize(gc, text, twHint);
					width += wsize.x;
					height = Math.max(wsize.y, height);
				} else {
					Point extent = gc.textExtent(text);
					width += extent.x;
					height = Math.max(extent.y, height);
				}
				gc.dispose();
			}
			int secondRowHeight = 0;
			if (headClient != null) {
				if (tbsize != null)
					secondRowHeight = tbsize.y;
			}

			if (messageArea != null) {
				Point masize = messageArea
						.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				if (headClient == null)
					height = Math.max(masize.y, height);
				else {
					secondRowHeight = Math.max(secondRowHeight, masize.y);
				}
			}
			if (secondRowHeight > 0)
				height += secondRowHeight;
			if (isSeparatorVisible())
				height += 2;
			if (height != 0)
				height += TITLE_VMARGIN * 2;
			if (width != 0)
				width += TITLE_HMARGIN * 2;
			int ihHint = hHint;
			if (ihHint > 0 && ihHint != SWT.DEFAULT)
				ihHint -= height;
			return new Point(width, height);
		}

		protected void layout(Composite composite, boolean flushCache) {
			if (flushCache) {
				toolbarCache.flush();
			}
			Rectangle carea = composite.getClientArea();
			int height = 0;
			Point tbsize = null;
			Point clsize = null;
			int twidth = carea.width - TITLE_HMARGIN * 2;
			if ((image != null || text != null || (messageArea != null && messageArea
					.isMinimized()))
					&& toolBarManager != null) {
				ToolBar toolBar = toolBarManager.getControl();
				if (toolBar != null) {
					toolbarCache.setControl(toolBar);
					tbsize = toolbarCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
					if (headClient == null) {
						toolbarCache.setBounds(carea.width - 1 - TITLE_HMARGIN
								- tbsize.x, TITLE_VMARGIN, tbsize.x, tbsize.y);
						height = tbsize.y;
					}
				}
			}

			Rectangle clientRect = null;

			if (headClient != null) {
				clientCache.setControl(headClient);
				clsize = clientCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				clientRect = new Rectangle(carea.width - 1 - TITLE_HMARGIN
						- clsize.x, TITLE_VMARGIN, clsize.x, clsize.y);
				height = clsize.y;
				twidth -= clsize.x + TITLE_GAP;
			}
			if (headClient == null && tbsize != null) {
				twidth -= tbsize.x + TITLE_GAP;
			}
			int tx = TITLE_HMARGIN;
			int iwidth = 0;
			if (image != null) {
				Rectangle ibounds = image.getBounds();
				iwidth = ibounds.width;
			}
			Point msize = null;
			Point bsize = null;
			int mx = 0;
			if (messageArea != null && messageArea.isMinimized()) {
				msize = messageArea.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				iwidth = Math.max(msize.x, iwidth);
				mx = tx;
			}
			if (busyLabel != null) {
				bsize = busyLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				iwidth = Math.max(bsize.x, iwidth);
				mx = tx;
			}
			if (iwidth > 0) {
				tx += iwidth + TITLE_GAP;
				if (text != null)
					twidth -= TITLE_GAP;
				twidth -= iwidth;
			}
			Rectangle titleRect = new Rectangle(0, 0, 0, 0);
			if (text != null) {
				Point tsize = titleCache.computeSize(twidth, SWT.DEFAULT);
				height = tsize.y;
				if (headClient == null) {
					if (tbsize != null)
						height = Math.max(tbsize.y, height);
				} else {
					height = Math.max(clsize.y, height);
				}
				int minTextWidth = tsize.x;
				Point realtsize = titleCache.computeSize(SWT.DEFAULT,
						SWT.DEFAULT);
				minTextWidth = Math.min(realtsize.x, minTextWidth);
				titleCache.setBounds(tx, TITLE_VMARGIN, minTextWidth, height);
				titleRect = titleCache.getControl().getBounds();
				if (minTextWidth < tsize.x && headClient != null) {
					// fix up the head client to use the extra space
					int hx = tx + minTextWidth + TITLE_GAP;
					int hwidth = carea.width - TITLE_HMARGIN - hx;
					clientRect.x = hx;
					clientRect.width = hwidth;
				}
			}
			if (headClient != null && clientRect != null)
				clientCache.setBounds(clientRect);

			if (msize != null) {
				messageArea.setBounds(mx, titleRect.y + titleRect.height / 2
						- msize.y / 2, msize.x, msize.y);
			}
			if (bsize != null) {
				busyLabel.setBounds(mx, titleRect.y + titleRect.height / 2
						- bsize.y / 2, bsize.x, bsize.y);
			}
			if (headClient != null) {
				if (tbsize != null) {
					height += TITLE_GAP;
					toolbarCache.setBounds(carea.width - 1 - TITLE_HMARGIN
							- tbsize.x, TITLE_VMARGIN + height, tbsize.x,
							tbsize.y);
					height += tbsize.y;
				}
			}
			if (height > 0)
				height += TITLE_VMARGIN * 2;
			if (isSeparatorVisible())
				height += 2;
			if (messageArea != null
					&& !messageArea.isMinimized()
					&& (messageArea.isAnimationStart() || !messageArea
							.isInTransition()) && messageArea.isVisible()) {
				Point masize = messageArea
						.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				int may = messageArea.isAnimationStart()
						&& messageArea.getState() == MessageArea.OPENNING ? height - 1
						: height - 1 - masize.y;
				if (headClient!=null)
					 may -= TITLE_GAP;
				int mawidth = carea.width - TITLE_HMARGIN - TITLE_HMARGIN;
				if (tbsize != null)
					mawidth -= tbsize.x + TITLE_GAP;
				messageArea.setBounds(TITLE_HMARGIN, may, mawidth, masize.y);
				messageArea.setAnimationStart(false);
			}
		}
	}

	/**
	 * Creates the form content control as a child of the provided parent.
	 * 
	 * @param parent
	 *            the parent widget
	 */
	public FormHeading(Composite parent, int style) {
		super(parent, style);
		setBackgroundMode(SWT.INHERIT_DEFAULT);
		addListener(SWT.Paint, new Listener() {
			public void handleEvent(Event e) {
				onPaint(e.gc);
			}
		});
		addListener(SWT.Dispose, new Listener() {
			public void handleEvent(Event e) {
				if (gradientImage != null) {
					gradientImage.dispose();
					gradientImage = null;
				}
			}
		});
		addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event e) {
				if (gradientInfo != null)
					updateGradientImage();
			}
		});
		super.setLayout(new FormHeadingLayout());
		titleLabel = new Label(this, SWT.WRAP);
		titleCache = new SizeCache(titleLabel);
	}

	/**
	 * Fully delegates the size computation to the internal layout manager.
	 */
	public final Point computeSize(int wHint, int hHint, boolean changed) {
		return ((FormHeadingLayout) getLayout()).computeSize(this, wHint,
				hHint, changed);
	}

	/**
	 * Prevents from changing the custom control layout.
	 */
	public final void setLayout(Layout layout) {
	}

	/**
	 * Returns the title text that will be rendered at the top of the form.
	 * 
	 * @return the title text
	 */
	public String getText() {
		return text;
	}

	/**
	 * Returns the title image that will be rendered to the left of the title.
	 * 
	 * @return the title image
	 * @since 3.2
	 */
	public Image getImage() {
		return image;
	}

	/**
	 * Sets the background color of the header.
	 */
	public void setBackground(Color bg) {
		super.setBackground(bg);
		titleLabel.setBackground(bg);
		if (toolBarManager != null)
			toolBarManager.getControl().setBackground(bg);
		baseBg = bg;
	}

	/**
	 * Sets the foreground color of the header.
	 */
	public void setForeground(Color fg) {
		super.setForeground(fg);
		titleLabel.setForeground(fg);
	}

	/**
	 * Sets the text to be rendered at the top of the form above the body as a
	 * title.
	 * 
	 * @param text
	 *            the title text
	 */
	public void setText(String text) {
		this.text = text;
		if (toolBarManager != null) {
			toolBarManager.getControl().setVisible(
					image != null || text != null);
		}
		if (text != null) {
			titleCache.setControl(titleLabel);
			titleLabel.setText(text);
		}
		titleLabel.setVisible(text != null);
		layout();
		redraw();
	}

	public void setFont(Font font) {
		super.setFont(font);
		titleLabel.setFont(font);
	}

	/**
	 * Sets the image to be rendered to the left of the title.
	 * 
	 * @param image
	 *            the title image or <code>null</code> to show no image.
	 * @since 3.2
	 */
	public void setImage(Image image) {
		this.image = image;
		if (toolBarManager != null) {
			toolBarManager.getControl().setVisible(
					image != null || text != null);
		}
		if (image != null) {
			createBusyLabel();
		} else if (image == null && busyLabel != null) {
			if (!busyLabel.isBusy()) {
				busyLabel.dispose();
				busyLabel = null;
			}
		}
		busyLabel.setImage(image);
		layout();
	}

	private void createBusyLabel() {
		if (busyLabel == null) {
			busyLabel = new BusyIndicator(this, SWT.NULL);
			if (gradientInfo == null && getBackgroundImage() == null)
				busyLabel.setBackground(getBackground());
		}
	}

	public void setTextBackground(Color[] gradientColors, int[] percents,
			boolean vertical) {
		gradientInfo = new GradientInfo();
		gradientInfo.gradientColors = gradientColors;
		gradientInfo.percents = percents;
		gradientInfo.vertical = vertical;
		titleLabel.setBackground(null);
		super.setBackground(null);
		if (toolBarManager != null)
			toolBarManager.getControl().setBackground(null);
		if (busyLabel != null)
			busyLabel.setBackground(null);
		updateGradientImage();
	}

	public void setBackgroundImage(Image image) {
		super.setBackgroundImage(image);
		if (image != null) {
			titleLabel.setBackground(null);
			if (toolBarManager != null)
				toolBarManager.getControl().setBackground(null);
			if (busyLabel != null)
				busyLabel.setBackground(null);
		}
	}

	/**
	 * Returns the tool bar manager that is used to manage tool items in the
	 * form's title area.
	 * 
	 * @return form tool bar manager
	 */
	public IToolBarManager getToolBarManager() {
		if (toolBarManager == null) {
			toolBarManager = new ToolBarManager(SWT.FLAT);
			ToolBar toolbar = toolBarManager.createControl(this);
			toolbar.setBackground(getBackground());
			toolbar.setForeground(getForeground());
			toolbar.setCursor(FormsResources.getHandCursor());
			addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					if (toolBarManager != null) {
						toolBarManager.dispose();
						toolBarManager = null;
					}
				}
			});
		}
		return toolBarManager;
	}

	/**
	 * Updates the local tool bar manager if used. Does nothing if local tool
	 * bar manager has not been created yet.
	 */
	public void updateToolBar() {
		if (toolBarManager != null)
			toolBarManager.update(false);
	}

	private void onPaint(GC gc) {
		if (!isSeparatorVisible())
			return;
		Rectangle carea = getClientArea();
		gc.setForeground(baseBg);
		gc.drawLine(0, carea.height - 2, carea.width - 2, carea.height - 2);
		if (separatorColor != null)
			gc.setForeground(separatorColor);
		else
			gc.setForeground(getForeground());
		gc.drawLine(0, carea.height - 1, carea.width - 1, carea.height - 1);
	}

	private void updateGradientImage() {
		Rectangle rect = getBounds();
		boolean vertical = gradientInfo.vertical;
		if (gradientImage != null)
			gradientImage.dispose();
		int width = vertical ? 1 : rect.width;
		int height = vertical ? rect.height : 1;
		gradientImage = new Image(getDisplay(), Math.max(width, 1), Math.max(
				height, 1));
		GC gc = new GC(gradientImage);
		drawTextGradient(gc, width, height);
		gc.dispose();
		setBackgroundImage(gradientImage);
	}

	private void drawTextGradient(GC gc, int width, int height) {
		final Color oldBackground = gc.getBackground();
		if (gradientInfo.gradientColors.length == 1) {
			if (gradientInfo.gradientColors[0] != null)
				gc.setBackground(gradientInfo.gradientColors[0]);
			gc.fillRectangle(0, 0, width, height);
		} else {
			final Color oldForeground = gc.getForeground();
			Color lastColor = gradientInfo.gradientColors[0];
			if (lastColor == null)
				lastColor = oldBackground;
			int pos = 0;
			for (int i = 0; i < gradientInfo.percents.length; ++i) {
				gc.setForeground(lastColor);
				lastColor = gradientInfo.gradientColors[i + 1];
				if (lastColor == null)
					lastColor = oldBackground;
				gc.setBackground(lastColor);
				if (gradientInfo.vertical) {
					final int gradientHeight = (gradientInfo.percents[i]
							* height / 100)
							- pos;
					gc.fillGradientRectangle(0, pos, width, gradientHeight,
							true);
					pos += gradientHeight;
				} else {
					final int gradientWidth = (gradientInfo.percents[i] * width / 100)
							- pos;
					gc.fillGradientRectangle(pos, 0, gradientWidth, height,
							false);
					pos += gradientWidth;
				}
			}
			if (gradientInfo.vertical && pos < height) {
				gc.setBackground(baseBg);
				gc.fillRectangle(0, pos, width, height - pos);
			}
			if (!gradientInfo.vertical && pos < width) {
				gc.setBackground(baseBg);
				gc.fillRectangle(pos, 0, width - pos, height);
			}
			gc.setForeground(oldForeground);
		}
	}

	/**
	 * TODO add javadoc
	 * 
	 * @return Returns the backgroundImageTiled.
	 */
	public boolean isBackgroundImageTiled() {
		return (style & S_TILED) != 0;
	}

	/**
	 * TODO add javadoc
	 * 
	 * @param backgroundImageTiled
	 *            The backgroundImageTiled to set.
	 */
	public void setBackgroundImageTiled(boolean backgroundImageTiled) {
		if (backgroundImageTiled)
			style |= S_TILED;
		else
			style &= (~S_TILED);
		if (isVisible())
			redraw();
	}

	/**
	 * @return Returns the backgroundImageAlignment. TODO add javadoc
	 * @since 3.1
	 */
	public int getBackgroundImageAlignment() {
		if ((style & SWT.LEFT) > 0)
			return SWT.LEFT;
		if ((style & SWT.RIGHT) > 0)
			return SWT.RIGHT;
		if ((style & SWT.CENTER) > 0)
			return SWT.CENTER;
		return SWT.NULL;
	}

	/**
	 * @param backgroundImageAlignment
	 *            The backgroundImageAlignment to set. TODO add javadoc
	 * @since 3.1
	 */
	public void setBackgroundImageAlignment(int backgroundImageAlignment) {
		style &= (~getBackgroundImageAlignment());
		style |= backgroundImageAlignment;
		if (isVisible())
			redraw();
	}

	/**
	 * @return Returns the backgroundImageClipped.
	 * @since 3.1
	 */
	public boolean isBackgroundImageClipped() {
		return (style & S_CLIPPED) != 0;
	}

	/**
	 * @param backgroundImageClipped
	 *            The backgroundImageClipped to set.
	 * @since 3.1
	 */
	public void setBackgroundImageClipped(boolean backgroundImageClipped) {
		if (backgroundImageClipped)
			style |= S_CLIPPED;
		else
			style &= (~S_CLIPPED);
	}

	/**
	 * TODO add javadoc experimental - do not use yet
	 * 
	 * @return <code>true</code> if the receiver is a visible separator,
	 *         <code>false</code> otherwise
	 */
	public boolean isSeparatorVisible() {
		return (style & S_SEPARATOR) != 0;
	}

	/**
	 * experimental - do not use yet TODO add javadoc
	 */
	public void setSeparatorVisible(boolean addSeparator) {
		if (addSeparator)
			style |= S_SEPARATOR;
		else
			style &= (~S_SEPARATOR);
	}

	/**
	 * experimental - do not use yet TODO add javadoc
	 */

	public Color getSeparatorColor() {
		return separatorColor;
	}

	/**
	 * experimental - do not use yet TODO add javadoc
	 */
	public void setSeparatorColor(Color separatorColor) {
		this.separatorColor = separatorColor;
	}

	/**
	 * Sets the message for this form.
	 * 
	 * @param message
	 *            the message, or <code>null</code> to clear the message
	 * @since 3.2
	 */
	public void setMessage(String message) {
		this.setMessage(message, IMessageProvider.NONE);
	}

	/**
	 * Sets the message for this form with an indication of what type of message
	 * it is.
	 * <p>
	 * The valid message types are one of <code>NONE</code>,
	 * <code>INFORMATION</code>,<code>WARNING</code>, or
	 * <code>ERROR</code>.
	 * </p>
	 * <p>
	 * 
	 * @param newMessage
	 *            the message, or <code>null</code> to clear the message
	 * @param newType
	 *            the message type
	 * @since 3.2
	 */

	public void setMessage(String newMessage, int newType) {
		Image newImage = null;
		if (newMessage != null) {
			switch (newType) {
			case IMessageProvider.NONE:
				break;
			case IMessageProvider.INFORMATION:
				newImage = JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_INFO);
				break;
			case IMessageProvider.WARNING:
				newImage = JFaceResources
						.getImage(Dialog.DLG_IMG_MESSAGE_WARNING);
				break;
			case IMessageProvider.ERROR:
				newImage = JFaceResources
						.getImage(Dialog.DLG_IMG_MESSAGE_ERROR);
				break;
			}
		}
		showMessage(newMessage, newImage);
	}

	private void showMessage(String newMessage, Image newImage) {
		if (newMessage == null) {
			if (messageArea != null)
				setMessageAreaVisible(false);
		} else {
			if (messageArea == null) {
				messageArea = new MessageArea(this, SWT.NULL);
				messageArea.setBackground(baseBg);
				messageArea.setForeground(separatorColor);
			}
			messageArea.setText(newMessage);
			messageArea.setImage(newImage);
			setMessageAreaVisible(true);
		}
	}

	private void setMessageAreaVisible(boolean visible) {
		if (messageArea.isMinimized()) {
			if (!visible)
				messageArea.setState(MessageArea.CLOSED);
			messageArea.setMinimized(false);
		}
		// check if we need to do anything
		switch (messageArea.getState()) {
		case MessageArea.OPENNING:
		case MessageArea.OPEN:
			if (visible)
				return;
			break;
		case MessageArea.CLOSING:
		case MessageArea.CLOSED:
			if (!visible)
				return;
			break;
		}
		// we do
		messageArea.moveAbove(null);
		messageArea.setAnimationStart(true);
		messageArea.setState(visible ? MessageArea.OPENNING
				: MessageArea.CLOSING);
		layout(true);
		Rectangle startBounds = messageArea.getBounds();
		final int endY = visible ? startBounds.y - startBounds.height
				: startBounds.y + startBounds.height;

		Runnable runnable = new Runnable() {
			public void run() {
				final boolean[] result = new boolean[1];
				/*
				 * getDisplay().syncExec(new Runnable() { public void run() { if
				 * (headClient!=null) headClient.setRedraw(false); } });
				 */
				for (;;) {
					getDisplay().syncExec(new Runnable() {
						public void run() {
							Point loc = messageArea.getLocation();
							if (messageArea.getState() == MessageArea.OPENNING) {
								// opening
								loc.y--;
								if (loc.y > endY)
									messageArea.setLocation(loc);
								else {
									result[0] = true;
									messageArea.setState(MessageArea.OPEN);
									layout(true);
								}
							} else {
								// closing
								loc.y++;
								if (loc.y < endY)
									messageArea.setLocation(loc);
								else {
									result[0] = true;
									messageArea.setState(MessageArea.CLOSED);
									layout(true);
								}
							}
						}
					});
					if (result[0]) {
						/*
						 * getDisplay().syncExec(new Runnable() { public void
						 * run() { if (headClient!=null)
						 * headClient.setRedraw(true); } });
						 */
						break;
					}
					Thread.yield();
					try {
						Thread.sleep(5);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		Thread t = new Thread(runnable);
		t.start();
	}

	/**
	 * Tests if the form is in the 'busy' state.
	 * 
	 * @return <code>true</code> if busy, <code>false</code> otherwise.
	 */

	public boolean isBusy() {
		return busyLabel != null && busyLabel.isBusy();
	}

	/**
	 * Sets the form's busy state. Busy form will display 'busy' animation in
	 * the area of the title image.
	 * 
	 * @param busy
	 *            the form's busy state
	 */

	public void setBusy(boolean busy) {
		if (busy)
			createBusyLabel();
		if (busy == busyLabel.isBusy())
			return;
		busyLabel.setBusy(busy);
		layout();
	}

	public Control getHeadClient() {
		return headClient;
	}

	public void setHeadClient(Control headClient) {
		Assert.isTrue(headClient.getParent() == this);
		this.headClient = headClient;
		layout();
	}
}