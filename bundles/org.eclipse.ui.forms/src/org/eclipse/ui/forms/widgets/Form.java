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
package org.eclipse.ui.forms.widgets;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.internal.forms.widgets.FormUtil;
import org.eclipse.ui.internal.forms.widgets.FormsResources;
/**
 * Form is a custom control that renders a title and
 * an optional background image above the body composite.
 * It can be used alone when part of parents that are scrolled.
 * If scrolling is required, use <code>ScrolledForm</code> instead
 * because it has an instance of <code>Form</code> and adds scrolling
 * capability.
 * <p>
 * Form can have a title if set. If not set, title area will
 * not be left empty - form body will be resized to fill the entire
 * form.
 * <p>
 * Form can have a background image behind the title text. The image
 * can be painted as-is, or tiled as many times as needed to fill
 * the title area.
 * <p>Form has a custom layout manager that is wrap-enabled. If a form
 * is placed in a composite whose layout manager implements ILayoutExtension,
 * the body of the worm will participate in wrapping as long as its
 * layout manager implements ILayoutExtension as well.
 * <p>
 * Children of the form should typically be created using FormToolkit to match
 * the appearance and behaviour. When creating children, use the form body as a
 * parent by calling 'getBody()' on the form instance. Example:
 * 
 * <pre>
 *  FormToolkit toolkit = new FormToolkit(parent.getDisplay());
 *  Form form = toolkit.createForm(parent);
 *  formContent.setText(&quot;Sample form&quot;);
 *  formContent.getBody().setLayout(new GridLayout());
 *  toolkit.createButton(formContent.getBody(), &quot;Checkbox&quot;, SWT.CHECK);
 * </pre>
 * 
 * <p>
 * No layout manager has been set on the body. Clients are required to set the
 * desired layout manager explicitly.
 * <p>
 * Although the class is not final, it should not be subclassed.
 * @since 3.0
 */
public class Form extends Composite {
	private int TITLE_HMARGIN = 10;
	private int TITLE_VMARGIN = 5;
	private int TITLE_GAP = 5;
	private Image backgroundImage;
	private boolean backgroundImageTiled;
	private boolean backgroundImageClipped=true;
	private int backgroundImageAlignment=SWT.LEFT;
	private GradientInfo gradientInfo;
	private String text;
	private Composite body;
	private ToolBarManager toolBarManager;
	private SizeCache bodyCache = new SizeCache();
	private SizeCache toolbarCache = new SizeCache();
	private FormText selectionText;
	private Rectangle titleRect;
	
	private class GradientInfo {
		Color [] gradientColors;
		int [] percents;
		boolean vertical;
	}

	private class FormLayout extends Layout implements ILayoutExtension {
		public int computeMinimumWidth(Composite composite, boolean flushCache) {
			return computeSize(composite, 5, SWT.DEFAULT, flushCache).x;
		}
		public int computeMaximumWidth(Composite composite, boolean flushCache) {
			return computeSize(composite, SWT.DEFAULT, SWT.DEFAULT, flushCache).x;
		}
		public Point computeSize(Composite composite, int wHint, int hHint,
				boolean flushCache) {
		    if (flushCache) {
		        bodyCache.flush();
		        toolbarCache.flush();
		    }
			bodyCache.setControl(body);
		    
			int width = 0;
			int height = 0;
			if (toolBarManager != null) {
				ToolBar toolBar = toolBarManager.getControl();
				if (toolBar != null) {
					toolbarCache.setControl(toolBar);
					Point tbsize = toolbarCache.computeSize(SWT.DEFAULT,
							SWT.DEFAULT);
					if (text != null)
						width += TITLE_GAP;
					width += tbsize.x;
					height = tbsize.y;
				}
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
			if (backgroundImage!=null && !backgroundImageClipped) {
				Rectangle ibounds = backgroundImage.getBounds();
				if (height < ibounds.height)
					height = ibounds.height;
			}
			if (height != 0)
				height += TITLE_VMARGIN * 2;
			if (width != 0)
				width += TITLE_HMARGIN * 2;
			int ihHint = hHint;
			if (ihHint > 0 && ihHint != SWT.DEFAULT)
				ihHint -= height;
			
			Point bsize = bodyCache.computeSize(FormUtil.getWidthHint(wHint, body),
					FormUtil.getHeightHint(ihHint, body));
			width = Math.max(bsize.x, width);
			height += bsize.y;
			return new Point(width, height);
		}
		
		protected void layout(Composite composite, boolean flushCache) {
		    if (flushCache) {
		        bodyCache.flush();
		        toolbarCache.flush();
		    }
		    bodyCache.setControl(body);
		    
			Rectangle carea = composite.getClientArea();
			int height = 0;
			Point tbsize = null;
			int twidth = carea.width - TITLE_HMARGIN * 2;
			if (text!=null && toolBarManager != null) {
				ToolBar toolBar = toolBarManager.getControl();
				if (toolBar != null) {
				    toolbarCache.setControl(toolBar);
					tbsize = toolbarCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
					toolbarCache.setBounds(carea.width - 1 - TITLE_HMARGIN
							- tbsize.x, TITLE_VMARGIN, tbsize.x, tbsize.y);
					height = tbsize.y;
				}
			}
			if (tbsize != null) {
				twidth -= tbsize.x + TITLE_GAP;
			}
			if (text != null) {
				GC gc = new GC(composite);
				gc.setFont(getFont());
				height = FormUtil.computeWrapSize(gc, text, twidth).y;
				gc.dispose();
				if (tbsize != null)
					height = Math.max(tbsize.y, height);
				titleRect = new Rectangle(TITLE_HMARGIN, TITLE_VMARGIN, twidth, height);
			}
			if (backgroundImage!=null && !backgroundImageClipped) {
				Rectangle ibounds = backgroundImage.getBounds();
				if (height < ibounds.height)
					height = ibounds.height;
			}
			if (height > 0)
				height += TITLE_VMARGIN * 2;
			
			bodyCache.setBounds(0, height, carea.width, carea.height - height);			
		}
	}
	/**
	 * Creates the form content control as a child of the provided parent.
	 * 
	 * @param parent
	 *            the parent widget
	 */
	public Form(Composite parent, int style) {
		super(parent, SWT.NO_BACKGROUND|style);
		addListener(SWT.Paint, new Listener() {
			public void handleEvent(Event e) {
				onPaint(e.gc);
			}
		});
		super.setLayout(new FormLayout());
		body = new LayoutComposite(this, SWT.NULL);
		body.setMenu(parent.getMenu());
	}
/**
 * Passes the menu to the form body.
 *@param menu
 */
	public void setMenu(Menu menu) {
		super.setMenu(menu);
		body.setMenu(menu);
	}
	/**
	 * Fully delegates the size computation to the internal
	 * layout manager.
	 */
	public final Point computeSize(int wHint, int hHint, boolean changed) {
		return ((FormLayout) getLayout()).computeSize(this, wHint, hHint,
				changed);
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
	 * Sets the foreground color of the form. This color will also be used for
	 * the body.
	 */
	public void setForeground(Color fg) {
		super.setForeground(fg);
		body.setForeground(fg);
	}
	/**
	 * Sets the background color of the form. This color will also be used for
	 * the body.
	 */
	public void setBackground(Color bg) {
		super.setBackground(bg);
		body.setBackground(bg);
		if (toolBarManager != null)
			toolBarManager.getControl().setBackground(bg);
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
			toolBarManager.getControl().setVisible(text!=null);
		}
		layout();
		redraw();
	}

	public void setTextBackground(Color [] gradientColors, int [] percents, boolean vertical) {
		gradientInfo = new GradientInfo();
		gradientInfo.gradientColors = gradientColors;
		gradientInfo.percents = percents;
		gradientInfo.vertical = vertical;
	}
	
	/**
	 * Returns the optional background image of this form. The image is
	 * rendered starting at the position 0,0 and is painted behind the title.
	 * 
	 * @return Returns the background image.
	 */
	public Image getBackgroundImage() {
		return backgroundImage;
	}
	/**
	 * Sets the optional background image to be rendered behind the title
	 * starting at the position 0,0.
	 * 
	 * @param backgroundImage
	 *            The backgroundImage to set.
	 */
	public void setBackgroundImage(Image backgroundImage) {
		this.backgroundImage = backgroundImage;
		redraw();
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
	/**
	 * Returns the container that occupies the body of the form (the form area
	 * below the title). Use this container as a parent for the controls that
	 * should be in the form. No layout manager has been set on the form body.
	 * 
	 * @return Returns the body of the form.
	 */
	public Composite getBody() {
		return body;
	}
	private void onPaint(GC gc) {
		if (text==null) return;
		Rectangle carea = getClientArea();
		gc.setFont(getFont());
		int theight=0;
		
		theight = TITLE_VMARGIN + titleRect.height + TITLE_VMARGIN;
		if (toolBarManager!=null) {
			ToolBar toolBar = toolBarManager.getControl();
			if (toolBar != null) {
				Point tbsize = toolBar.getSize();
				theight = Math.max(theight, tbsize.y);
			}
		}
		if (backgroundImage!=null && !backgroundImageClipped) {
			theight = Math.max(theight, backgroundImage.getBounds().height);
		}
		Image buffer= new Image(getDisplay(), carea.width, theight);
		GC bufferGC = new GC(buffer, gc.getStyle());
		bufferGC.setBackground(getBackground());
		bufferGC.setForeground(getForeground());
		bufferGC.setFont(getFont());
		bufferGC.fillRectangle(0, 0, carea.width, theight);
		if (backgroundImage != null) {
			drawBackgroundImage(bufferGC, carea.width, TITLE_VMARGIN+titleRect.height+TITLE_VMARGIN);
		}
		else if (gradientInfo != null) {
			drawTextGradient(bufferGC, carea.width, TITLE_VMARGIN+titleRect.height+TITLE_VMARGIN);
		}
		FormUtil.paintWrapText(bufferGC, text, titleRect);
		gc.drawImage(buffer, 0, 0);
		bufferGC.dispose();
		buffer.dispose();
	}
	private void drawBackgroundImage(GC gc, int width, int height) {
		Rectangle ibounds = backgroundImage.getBounds();		
		if (backgroundImageTiled) {
			int x=0;
			int y=0;
			// loop and tile image until the entire title area is covered
			for (;;) {
				gc.drawImage(backgroundImage, x, y);
				x+=ibounds.width;
				if (x>width) {
					//wrap
					x = 0;
					y += ibounds.height;
					if (y>height) break;
				}
			}
		}
		else {
			switch (backgroundImageAlignment) {
				case SWT.LEFT:
					gc.drawImage(backgroundImage, 0, 0);
					break;
				case SWT.RIGHT:
					//Rectangle clientArea = getClientArea();
					gc.drawImage(backgroundImage, width-ibounds.width, 0);
					break;
			}
		}
	}
	private void drawTextGradient(GC gc, int width, int height) {
		final Color oldBackground = gc.getBackground();
		if (gradientInfo.gradientColors.length == 1) {
			if (gradientInfo.gradientColors[0] != null) gc.setBackground(gradientInfo.gradientColors[0]);
			gc.fillRectangle(0, 0, width, height);
		} else {
			final Color oldForeground = gc.getForeground();
			Color lastColor = gradientInfo.gradientColors[0];
			if (lastColor == null) lastColor = oldBackground;
			int pos = 0;
			for (int i = 0; i < gradientInfo.percents.length; ++i) {
				gc.setForeground(lastColor);
				lastColor = gradientInfo.gradientColors[i + 1];
				if (lastColor == null) lastColor = oldBackground;
				gc.setBackground(lastColor);
				if (gradientInfo.vertical) {
					final int gradientHeight = (gradientInfo.percents[i] * height / 100) - pos;
					gc.fillGradientRectangle(0, pos, width, gradientHeight, true);
					pos += gradientHeight;
				} else {
					final int gradientWidth = (gradientInfo.percents[i] * width / 100) - pos;
					gc.fillGradientRectangle(pos, 0, gradientWidth, height, false);
					pos += gradientWidth;
				}
			}
			if (gradientInfo.vertical && pos < height) {
				gc.setBackground(getBackground());
				gc.fillRectangle(0, pos, width, height - pos);
			}
			if (!gradientInfo.vertical && pos < width) {
				gc.setBackground(getBackground());
				gc.fillRectangle(pos, 0, width - pos, height);
			}
			gc.setForeground(oldForeground);
		}		
	}
	/**
	 * @return Returns the backgroundImageTiled.
	 */
	public boolean isBackgroundImageTiled() {
		return backgroundImageTiled;
	}
	/**
	 * @param backgroundImageTiled The backgroundImageTiled to set.
	 */
	public void setBackgroundImageTiled(boolean backgroundImageTiled) {
		this.backgroundImageTiled = backgroundImageTiled;
		if (isVisible())
			redraw();
	}
	/**
	 * @return Returns the backgroundImageAlignment.
	 * @since 3.1
	 */
	public int getBackgroundImageAlignment() {
		return backgroundImageAlignment;
	}
	/**
	 * @param backgroundImageAlignment The backgroundImageAlignment to set.
	 * @since 3.1
	 */
	public void setBackgroundImageAlignment(int backgroundImageAlignment) {
		this.backgroundImageAlignment = backgroundImageAlignment;
		if (isVisible())
			redraw();
	}
	/**
	 * @return Returns the backgroundImageClipped.
	 * @since 3.1
	 */
	public boolean isBackgroundImageClipped() {
		return backgroundImageClipped;
	}
	/**
	 * @param backgroundImageClipped The backgroundImageClipped to set.
	 * @since 3.1
	 */
	public void setBackgroundImageClipped(boolean backgroundImageClipped) {
		this.backgroundImageClipped = backgroundImageClipped;
	}
	void setSelectionText(FormText text) {
		if (selectionText!=null && selectionText!=text) {
			selectionText.clearSelection();
		}
		this.selectionText = text;
	}
}
