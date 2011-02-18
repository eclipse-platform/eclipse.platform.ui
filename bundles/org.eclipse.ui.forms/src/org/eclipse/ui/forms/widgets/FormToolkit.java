/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Michael Williamson (eclipse-bugs@magnaworks.com) - patch (see Bugzilla #92545) 
 *******************************************************************************/
package org.eclipse.ui.forms.widgets;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.HyperlinkGroup;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.internal.forms.widgets.FormFonts;
import org.eclipse.ui.internal.forms.widgets.FormUtil;

/**
 * The toolkit is responsible for creating SWT controls adapted to work in
 * Eclipse forms. In addition to changing their presentation properties (fonts,
 * colors etc.), various listeners are attached to make them behave correctly in
 * the form context.
 * <p>
 * In addition to being the control factory, the toolkit is also responsible for
 * painting flat borders for select controls, managing hyperlink groups and
 * control colors.
 * <p>
 * The toolkit creates some of the most common controls used to populate Eclipse
 * forms. Controls that must be created using their constructors,
 * <code>adapt()</code> method is available to change its properties in the
 * same way as with the supported toolkit controls.
 * <p>
 * Typically, one toolkit object is created per workbench part (for example, an
 * editor or a form wizard). The toolkit is disposed when the part is disposed.
 * To conserve resources, it is possible to create one color object for the
 * entire plug-in and share it between several toolkits. The plug-in is
 * responsible for disposing the colors (disposing the toolkit that uses shared
 * color object will not dispose the colors).
 * <p>
 * FormToolkit is normally instantiated, but can also be subclassed if some of
 * the methods needs to be modified. In those cases, <code>super</code> must
 * be called to preserve normal behaviour.
 * 
 * @since 3.0
 */
public class FormToolkit {
	public static final String KEY_DRAW_BORDER = "FormWidgetFactory.drawBorder"; //$NON-NLS-1$

	public static final String TREE_BORDER = "treeBorder"; //$NON-NLS-1$

	public static final String TEXT_BORDER = "textBorder"; //$NON-NLS-1$

	private int borderStyle = SWT.NULL;

	private FormColors colors;

	private int orientation = Window.getDefaultOrientation();

	// private KeyListener deleteListener;
	private BorderPainter borderPainter;

	private BoldFontHolder boldFontHolder;

	private HyperlinkGroup hyperlinkGroup;
		
	private boolean isDisposed = false;

	/* default */
	VisibilityHandler visibilityHandler;

	/* default */
	KeyboardHandler keyboardHandler;

	private class BorderPainter implements PaintListener {
		public void paintControl(PaintEvent event) {
			Composite composite = (Composite) event.widget;
			Control[] children = composite.getChildren();
			for (int i = 0; i < children.length; i++) {
				Control c = children[i];
				boolean inactiveBorder = false;
				boolean textBorder = false;
				if (!c.isVisible())
					continue;
				/*
				 * if (c.getEnabled() == false && !(c instanceof CCombo))
				 * continue;
				 */
				if (c instanceof Hyperlink)
					continue;
				Object flag = c.getData(KEY_DRAW_BORDER);
				if (flag != null) {
					if (flag.equals(Boolean.FALSE))
						continue;
					if (flag.equals(TREE_BORDER))
						inactiveBorder = true;
					else if (flag.equals(TEXT_BORDER))
						textBorder = true;
				}
				if (getBorderStyle() == SWT.BORDER) {
					if (!inactiveBorder && !textBorder) {
						continue;
					}
					if (c instanceof Text || c instanceof Table
							|| c instanceof Tree)
						continue;
				}
				if (!inactiveBorder
						&& (c instanceof Text || c instanceof CCombo || textBorder)) {
					Rectangle b = c.getBounds();
					GC gc = event.gc;
					gc.setForeground(c.getBackground());
					gc.drawRectangle(b.x - 1, b.y - 1, b.width + 1,
							b.height + 1);
					// gc.setForeground(getBorderStyle() == SWT.BORDER ? colors
					// .getBorderColor() : colors.getForeground());
					gc.setForeground(colors.getBorderColor());
					if (c instanceof CCombo)
						gc.drawRectangle(b.x - 1, b.y - 1, b.width + 1,
								b.height + 1);
					else
						gc.drawRectangle(b.x - 1, b.y - 2, b.width + 1,
								b.height + 3);
				} else if (inactiveBorder || c instanceof Table
						|| c instanceof Tree) {
					Rectangle b = c.getBounds();
					GC gc = event.gc;
					gc.setForeground(colors.getBorderColor());
					gc.drawRectangle(b.x - 1, b.y - 1, b.width + 1,
							b.height + 1);
				}
			}
		}
	}

	private static class VisibilityHandler extends FocusAdapter {
		private boolean handleNextFocusGained = true;
		
		public void focusGained(FocusEvent e) {
			if (!handleNextFocusGained) {
				handleNextFocusGained = true;
			} else {
				Widget w = e.widget;
				if (w instanceof Control) {
					FormUtil.ensureVisible((Control) w);
				}
			}
		}
		
		public void focusLost(FocusEvent e) {
			Widget w = e.widget;
			if (w instanceof Control) {
				handleNextFocusGained = w.getDisplay().getActiveShell() == ((Control) w).getShell(); 
			}
		}
	}

	private static class KeyboardHandler extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			Widget w = e.widget;
			if (w instanceof Control) {
				if (e.doit)
					FormUtil.processKey(e.keyCode, (Control) w);
			}
		}
	}

	private class BoldFontHolder {
		private Map fontMap;

		public BoldFontHolder() {
		}

		public Font getBoldFont(Font font) {
			if (font == null ) {
				return null;
			}
			
			if (fontMap == null) {
				fontMap = new HashMap();
			}
			
			if (fontMap.containsKey(font)) {
				return (Font) fontMap.get(font);
			}

			Font boldFont = FormFonts.getInstance().getBoldFont(colors.getDisplay(),
						font);
			fontMap.put(font, boldFont);
			return boldFont;

		}

		public void dispose() {
			if (fontMap == null) {
				return;
			}
			for (Iterator iter = fontMap.values().iterator(); iter.hasNext();) {
				Font boldFont = (Font) iter.next();
				if (boldFont != null && colors.getDisplay() != null) {
					FormFonts.getInstance().markFinished(boldFont,
							colors.getDisplay());
					boldFont = null;
				}
			}
		}
	}

	/**
	 * Creates a toolkit that is self-sufficient (will manage its own colors).
	 * <p>
	 * Clients that call this method must call {@link #dispose()} when they
	 * are finished using the toolkit.
	 * 
	 */
	public FormToolkit(Display display) {
		this(new FormColors(display));
	}

	/**
	 * Creates a toolkit that will use the provided (shared) colors. The toolkit
	 * will dispose the colors if and only if they are <b>not</b> marked as
	 * shared via the <code>markShared()</code> method.
	 * <p>
	 * Clients that call this method must call {@link #dispose()} when they
	 * are finished using the toolkit.
	 * 
	 * @param colors
	 *            the shared colors
	 */
	public FormToolkit(FormColors colors) {
		this.colors = colors;
		initialize();
	}

	/**
	 * Creates a button as a part of the form.
	 * 
	 * @param parent
	 *            the button parent
	 * @param text
	 *            an optional text for the button (can be <code>null</code>)
	 * @param style
	 *            the button style (for example, <code>SWT.PUSH</code>)
	 * @return the button widget
	 */
	public Button createButton(Composite parent, String text, int style) {
		Button button = new Button(parent, style | SWT.FLAT | orientation);
		if (text != null)
			button.setText(text);
		adapt(button, true, true);
		return button;
	}

	/**
	 * Creates the composite as a part of the form.
	 * 
	 * @param parent
	 *            the composite parent
	 * @return the composite widget
	 */
	public Composite createComposite(Composite parent) {
		return createComposite(parent, SWT.NULL);
	}

	/**
	 * Creates the composite as part of the form using the provided style.
	 * 
	 * @param parent
	 *            the composite parent
	 * @param style
	 *            the composite style
	 * @return the composite widget
	 */
	public Composite createComposite(Composite parent, int style) {
		Composite composite = new LayoutComposite(parent, style | orientation);
		adapt(composite);
		return composite;
	}

	/**
	 * Creats the composite that can server as a separator between various parts
	 * of a form. Separator height should be controlled by setting the height
	 * hint on the layout data for the composite.
	 * 
	 * @param parent
	 *            the separator parent
	 * @return the separator widget
	 */
	public Composite createCompositeSeparator(Composite parent) {
		final Composite composite = new Composite(parent, orientation);
		composite.addListener(SWT.Paint, new Listener() {
			public void handleEvent(Event e) {
				if (composite.isDisposed())
					return;
				Rectangle bounds = composite.getBounds();
				GC gc = e.gc;
				gc.setForeground(colors.getColor(IFormColors.SEPARATOR));
				if (colors.getBackground() != null)
					gc.setBackground(colors.getBackground());
				gc.fillGradientRectangle(0, 0, bounds.width, bounds.height,
						false);
			}
		});
		if (parent instanceof Section)
			((Section) parent).setSeparatorControl(composite);
		return composite;
	}

	/**
	 * Creates a label as a part of the form.
	 * 
	 * @param parent
	 *            the label parent
	 * @param text
	 *            the label text
	 * @return the label widget
	 */
	public Label createLabel(Composite parent, String text) {
		return createLabel(parent, text, SWT.NONE);
	}

	/**
	 * Creates a label as a part of the form.
	 * 
	 * @param parent
	 *            the label parent
	 * @param text
	 *            the label text
	 * @param style
	 *            the label style
	 * @return the label widget
	 */
	public Label createLabel(Composite parent, String text, int style) {
		Label label = new Label(parent, style | orientation);
		if (text != null)
			label.setText(text);
		adapt(label, false, false);
		return label;
	}

	/**
	 * Creates a hyperlink as a part of the form. The hyperlink will be added to
	 * the hyperlink group that belongs to this toolkit.
	 * 
	 * @param parent
	 *            the hyperlink parent
	 * @param text
	 *            the text of the hyperlink
	 * @param style
	 *            the hyperlink style
	 * @return the hyperlink widget
	 */
	public Hyperlink createHyperlink(Composite parent, String text, int style) {
		Hyperlink hyperlink = new Hyperlink(parent, style | orientation);
		if (text != null)
			hyperlink.setText(text);
		hyperlink.addFocusListener(visibilityHandler);
		hyperlink.addKeyListener(keyboardHandler);
		hyperlinkGroup.add(hyperlink);
		return hyperlink;
	}

	/**
	 * Creates an image hyperlink as a part of the form. The hyperlink will be
	 * added to the hyperlink group that belongs to this toolkit.
	 * 
	 * @param parent
	 *            the hyperlink parent
	 * @param style
	 *            the hyperlink style
	 * @return the image hyperlink widget
	 */
	public ImageHyperlink createImageHyperlink(Composite parent, int style) {
		ImageHyperlink hyperlink = new ImageHyperlink(parent, style
				| orientation);
		hyperlink.addFocusListener(visibilityHandler);
		hyperlink.addKeyListener(keyboardHandler);
		hyperlinkGroup.add(hyperlink);
		return hyperlink;
	}

	/**
	 * Creates a rich text as a part of the form.
	 * 
	 * @param parent
	 *            the rich text parent
	 * @param trackFocus
	 *            if <code>true</code>, the toolkit will monitor focus
	 *            transfers to ensure that the hyperlink in focus is visible in
	 *            the form.
	 * @return the rich text widget
	 */
	public FormText createFormText(Composite parent, boolean trackFocus) {
		FormText engine = new FormText(parent, SWT.WRAP | orientation);
		engine.marginWidth = 1;
		engine.marginHeight = 0;
		engine.setHyperlinkSettings(getHyperlinkGroup());
		adapt(engine, trackFocus, true);
		engine.setMenu(parent.getMenu());
		return engine;
	}

	/**
	 * Adapts a control to be used in a form that is associated with this
	 * toolkit. This involves adjusting colors and optionally adding handlers to
	 * ensure focus tracking and keyboard management.
	 * 
	 * @param control
	 *            a control to adapt
	 * @param trackFocus
	 *            if <code>true</code>, form will be scrolled horizontally
	 *            and/or vertically if needed to ensure that the control is
	 *            visible when it gains focus. Set it to <code>false</code> if
	 *            the control is not capable of gaining focus.
	 * @param trackKeyboard
	 *            if <code>true</code>, the control that is capable of
	 *            gaining focus will be tracked for certain keys that are
	 *            important to the underlying form (for example, PageUp,
	 *            PageDown, ScrollUp, ScrollDown etc.). Set it to
	 *            <code>false</code> if the control is not capable of gaining
	 *            focus or these particular key event are already used by the
	 *            control.
	 */
	public void adapt(Control control, boolean trackFocus, boolean trackKeyboard) {
		control.setBackground(colors.getBackground());
		control.setForeground(colors.getForeground());
		if (control instanceof ExpandableComposite) {
			ExpandableComposite ec = (ExpandableComposite) control;
			if (ec.toggle != null) {
				if (trackFocus)
					ec.toggle.addFocusListener(visibilityHandler);
				if (trackKeyboard)
					ec.toggle.addKeyListener(keyboardHandler);
			}
			if (ec.textLabel != null) {
				if (trackFocus)
					ec.textLabel.addFocusListener(visibilityHandler);
				if (trackKeyboard)
					ec.textLabel.addKeyListener(keyboardHandler);
			}
			return;
		}
		if (trackFocus)
			control.addFocusListener(visibilityHandler);
		if (trackKeyboard)
			control.addKeyListener(keyboardHandler);
	}

	/**
	 * Adapts a composite to be used in a form associated with this toolkit.
	 * 
	 * @param composite
	 *            the composite to adapt
	 */
	public void adapt(Composite composite) {
		composite.setBackground(colors.getBackground());
		composite.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				((Control) e.widget).setFocus();
			}
		});
		if (composite.getParent() != null)
			composite.setMenu(composite.getParent().getMenu());
	}

	/**
	 * A helper method that ensures the provided control is visible when
	 * ScrolledComposite is somewhere in the parent chain. If scroll bars are
	 * visible and the control is clipped, the client of the scrolled composite
	 * will be scrolled to reveal the control.
	 * 
	 * @param c
	 *            the control to reveal
	 */
	public static void ensureVisible(Control c) {
		FormUtil.ensureVisible(c);
	}

	/**
	 * Creates a section as a part of the form.
	 * 
	 * @param parent
	 *            the section parent
	 * @param sectionStyle
	 *            the section style
	 * @return the section widget
	 */
	public Section createSection(Composite parent, int sectionStyle) {
		Section section = new Section(parent, orientation, sectionStyle);
		section.setMenu(parent.getMenu());
		adapt(section, true, true);
		if (section.toggle != null) {
			section.toggle.setHoverDecorationColor(colors
					.getColor(IFormColors.TB_TOGGLE_HOVER));
			section.toggle.setDecorationColor(colors
					.getColor(IFormColors.TB_TOGGLE));
		}
		section.setFont(boldFontHolder.getBoldFont(parent.getFont()));
		if ((sectionStyle & Section.TITLE_BAR) != 0
				|| (sectionStyle & Section.SHORT_TITLE_BAR) != 0) {
			colors.initializeSectionToolBarColors();
			section.setTitleBarBackground(colors.getColor(IFormColors.TB_BG));
			section.setTitleBarBorderColor(colors
					.getColor(IFormColors.TB_BORDER));
		}
		// call setTitleBarForeground regardless as it also sets the label color
		section.setTitleBarForeground(colors
				.getColor(IFormColors.TB_TOGGLE));
		return section;
	}

	/**
	 * Creates an expandable composite as a part of the form.
	 * 
	 * @param parent
	 *            the expandable composite parent
	 * @param expansionStyle
	 *            the expandable composite style
	 * @return the expandable composite widget
	 */
	public ExpandableComposite createExpandableComposite(Composite parent,
			int expansionStyle) {
		ExpandableComposite ec = new ExpandableComposite(parent, orientation,
				expansionStyle);
		ec.setMenu(parent.getMenu());
		adapt(ec, true, true);
		ec.setFont(boldFontHolder.getBoldFont(ec.getFont()));
		return ec;
	}

	/**
	 * Creates a separator label as a part of the form.
	 * 
	 * @param parent
	 *            the separator parent
	 * @param style
	 *            the separator style
	 * @return the separator label
	 */
	public Label createSeparator(Composite parent, int style) {
		Label label = new Label(parent, SWT.SEPARATOR | style | orientation);
		label.setBackground(colors.getBackground());
		label.setForeground(colors.getBorderColor());
		return label;
	}

	/**
	 * Creates a table as a part of the form.
	 * 
	 * @param parent
	 *            the table parent
	 * @param style
	 *            the table style
	 * @return the table widget
	 */
	public Table createTable(Composite parent, int style) {
		Table table = new Table(parent, style | borderStyle | orientation);
		adapt(table, false, false);
		// hookDeleteListener(table);
		return table;
	}

	/**
	 * Creates a text as a part of the form.
	 * 
	 * @param parent
	 *            the text parent
	 * @param value
	 *            the text initial value
	 * @return the text widget
	 */
	public Text createText(Composite parent, String value) {
		return createText(parent, value, SWT.SINGLE);
	}

	/**
	 * Creates a text as a part of the form.
	 * 
	 * @param parent
	 *            the text parent
	 * @param value
	 *            the text initial value
	 * @param style
	 *            the text style
	 * @return the text widget
	 */
	public Text createText(Composite parent, String value, int style) {
		Text text = new Text(parent, borderStyle | style | orientation);
		if (value != null)
			text.setText(value);
		text.setForeground(colors.getForeground());
		text.setBackground(colors.getBackground());
		text.addFocusListener(visibilityHandler);
		return text;
	}

	/**
	 * Creates a tree widget as a part of the form.
	 * 
	 * @param parent
	 *            the tree parent
	 * @param style
	 *            the tree style
	 * @return the tree widget
	 */
	public Tree createTree(Composite parent, int style) {
		Tree tree = new Tree(parent, borderStyle | style | orientation);
		adapt(tree, false, false);
		// hookDeleteListener(tree);
		return tree;
	}

	/**
	 * Creates a scrolled form widget in the provided parent. If you do not
	 * require scrolling because there is already a scrolled composite up the
	 * parent chain, use 'createForm' instead.
	 * 
	 * @param parent
	 *            the scrolled form parent
	 * @return the form that can scroll itself
	 * @see #createForm
	 */
	public ScrolledForm createScrolledForm(Composite parent) {
		ScrolledForm form = new ScrolledForm(parent, SWT.V_SCROLL
				| SWT.H_SCROLL | orientation);
		form.setExpandHorizontal(true);
		form.setExpandVertical(true);
		form.setBackground(colors.getBackground());
		form.setForeground(colors.getColor(IFormColors.TITLE));
		form.setFont(JFaceResources.getHeaderFont());
		return form;
	}

	/**
	 * Creates a form widget in the provided parent. Note that this widget does
	 * not scroll its content, so make sure there is a scrolled composite up the
	 * parent chain. If you require scrolling, use 'createScrolledForm' instead.
	 * 
	 * @param parent
	 *            the form parent
	 * @return the form that does not scroll
	 * @see #createScrolledForm
	 */
	public Form createForm(Composite parent) {
		Form formContent = new Form(parent, orientation);
		formContent.setBackground(colors.getBackground());
		formContent.setForeground(colors.getColor(IFormColors.TITLE));
		formContent.setFont(JFaceResources.getHeaderFont());
		return formContent;
	}

	/**
	 * Takes advantage of the gradients and other capabilities to decorate the
	 * form heading using colors computed based on the current skin and
	 * operating system.
	 * 
	 * @since 3.3
	 * @param form
	 *            the form to decorate
	 */

	public void decorateFormHeading(Form form) {
		Color top = colors.getColor(IFormColors.H_GRADIENT_END);
		Color bot = colors.getColor(IFormColors.H_GRADIENT_START);
		form.setTextBackground(new Color[] { top, bot }, new int[] { 100 },
				true);
		form.setHeadColor(IFormColors.H_BOTTOM_KEYLINE1, colors
				.getColor(IFormColors.H_BOTTOM_KEYLINE1));
		form.setHeadColor(IFormColors.H_BOTTOM_KEYLINE2, colors
				.getColor(IFormColors.H_BOTTOM_KEYLINE2));
		form.setHeadColor(IFormColors.H_HOVER_LIGHT, colors
				.getColor(IFormColors.H_HOVER_LIGHT));
		form.setHeadColor(IFormColors.H_HOVER_FULL, colors
				.getColor(IFormColors.H_HOVER_FULL));
		form.setHeadColor(IFormColors.TB_TOGGLE, colors
				.getColor(IFormColors.TB_TOGGLE));
		form.setHeadColor(IFormColors.TB_TOGGLE_HOVER, colors
				.getColor(IFormColors.TB_TOGGLE_HOVER));
		form.setSeparatorVisible(true);
	}

	/**
	 * Creates a scrolled page book widget as a part of the form.
	 * 
	 * @param parent
	 *            the page book parent
	 * @param style
	 *            the text style
	 * @return the scrolled page book widget
	 */
	public ScrolledPageBook createPageBook(Composite parent, int style) {
		ScrolledPageBook book = new ScrolledPageBook(parent, style
				| orientation);
		adapt(book, true, true);
		book.setMenu(parent.getMenu());
		return book;
	}

	/**
	 * Disposes the toolkit.
	 */
	public void dispose() {
		if (isDisposed) {
			return;
		}
		isDisposed = true;
		boldFontHolder.dispose();
		if (colors.isShared() == false) {
			colors.dispose();
			colors = null;
		}
	}

	/**
	 * Returns the hyperlink group that manages hyperlinks for this toolkit.
	 * 
	 * @return the hyperlink group
	 */
	public HyperlinkGroup getHyperlinkGroup() {
		return hyperlinkGroup;
	}

	/**
	 * Sets the background color for the entire toolkit. The method delegates
	 * the call to the FormColors object and also updates the hyperlink group so
	 * that hyperlinks and other objects are in sync.
	 * 
	 * @param bg
	 *            the new background color
	 */
	public void setBackground(Color bg) {
		hyperlinkGroup.setBackground(bg);
		colors.setBackground(bg);
	}

	/**
	 * Refreshes the hyperlink colors by loading from JFace settings.
	 */
	public void refreshHyperlinkColors() {
		hyperlinkGroup.initializeDefaultForegrounds(colors.getDisplay());
	}

	/**
	 * Paints flat borders for widgets created by this toolkit within the
	 * provided parent. Borders will not be painted if the global border style
	 * is SWT.BORDER (i.e. if native borders are used). Call this method during
	 * creation of a form composite to get the borders of its children painted.
	 * Care should be taken when selection layout margins. At least one pixel
	 * margin width and height must be chosen to allow the toolkit to paint the
	 * border on the parent around the widgets.
	 * <p>
	 * Borders are painted for some controls that are selected by the toolkit by
	 * default. If a control needs a border but is not on its list, it is
	 * possible to force borders in the following ways:
	 * 
	 * <pre>
	 *             widget.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
	 *             
	 *             or
	 *             
	 *             widget.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
	 * </pre>
	 * <p>
	 * If borders would normally be painted for a control, but they are not wanted, it
	 * is possible to suppress them in the following way:
	 * 
	 * <pre>
	 *             widget.setData(FormToolkit.KEY_DRAW_BORDER, Boolean.FALSE);
	 * </pre>
	 * 
	 * @param parent
	 *            the parent that owns the children for which the border needs
	 *            to be painted.
	 */
	public void paintBordersFor(Composite parent) {
		// if (borderStyle == SWT.BORDER)
		// return;
		if (borderPainter == null)
			borderPainter = new BorderPainter();
		parent.addPaintListener(borderPainter);
	}

	/**
	 * Returns the colors used by this toolkit.
	 * 
	 * @return the color object
	 */
	public FormColors getColors() {
		return colors;
	}

	/**
	 * Returns the border style used for various widgets created by this
	 * toolkit. The intent of the toolkit is to create controls with styles that
	 * yield a 'flat' appearance. On systems where the native borders are
	 * already flat, we set the style to SWT.BORDER and don't paint the borders
	 * ourselves. Otherwise, the style is set to SWT.NULL, and borders are
	 * painted by the toolkit.
	 * 
	 * @return the global border style
	 */
	public int getBorderStyle() {
		return borderStyle;
	}

	/**
	 * Returns the margin required around the children whose border is being
	 * painted by the toolkit using {@link #paintBordersFor(Composite)}. Since
	 * the border is painted around the controls on the parent, a number of
	 * pixels needs to be reserved for this border. For windowing systems where
	 * the native border is used, this margin is 0.
	 * 
	 * @return the margin in the parent when children have their border painted
	 * @since 3.3
	 */
	public int getBorderMargin() {
		return getBorderStyle() == SWT.BORDER ? 0 : 2;
	}

	/**
	 * Sets the border style to be used when creating widgets. The toolkit
	 * chooses the correct style based on the platform but this value can be
	 * changed using this method.
	 * 
	 * @param style
	 *            <code>SWT.BORDER</code> or <code>SWT.NULL</code>
	 * @see #getBorderStyle
	 */
	public void setBorderStyle(int style) {
		this.borderStyle = style;
	}

	/**
	 * A utility method that ensures that the control is visible in the scrolled
	 * composite. The prerequisite for this method is that the control has a
	 * class that extends ScrolledComposite somewhere in the parent chain. If
	 * the control is partially or fully clipped, the composite is scrolled to
	 * set by setting the origin to the control origin.
	 * 
	 * @param c
	 *            the control to make visible
	 * @param verticalOnly
	 *            if <code>true</code>, the scrolled composite will be
	 *            scrolled only vertically if needed. Otherwise, the scrolled
	 *            composite origin will be set to the control origin.
	 * @since 3.1
	 */
	public static void setControlVisible(Control c, boolean verticalOnly) {
		ScrolledComposite scomp = FormUtil.getScrolledComposite(c);
		if (scomp == null)
			return;
		Point location = FormUtil.getControlLocation(scomp, c);
		scomp.setOrigin(location);
	}

	private void initialize() {
		initializeBorderStyle();
		hyperlinkGroup = new HyperlinkGroup(colors.getDisplay());
		hyperlinkGroup.setBackground(colors.getBackground());
		visibilityHandler = new VisibilityHandler();
		keyboardHandler = new KeyboardHandler();
		boldFontHolder = new BoldFontHolder();
	}

	private void initializeBorderStyle() {
		String osname = System.getProperty("os.name"); //$NON-NLS-1$
		String osversion = System.getProperty("os.version"); //$NON-NLS-1$
		if (osname.startsWith("Windows") && "5.1".compareTo(osversion) <= 0) { //$NON-NLS-1$ //$NON-NLS-2$
			// Skinned widgets used on newer Windows (e.g. XP (5.1), Vista
			// (6.0))
			// Check for Windows Classic. If not used, set the style to BORDER
			RGB rgb = colors.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
			if (rgb.red != 212 || rgb.green != 208 || rgb.blue != 200)
				borderStyle = SWT.BORDER;
		} else if (osname.startsWith("Mac")) //$NON-NLS-1$
			borderStyle = SWT.BORDER;
	}

	/**
	 * Returns the orientation that all the widgets created by this toolkit will
	 * inherit, if set. Can be <code>SWT.NULL</code>,
	 * <code>SWT.LEFT_TO_RIGHT</code> and <code>SWT.RIGHT_TO_LEFT</code>.
	 * 
	 * @return orientation style for this toolkit, or <code>SWT.NULL</code> if
	 *         not set. The default orientation is inherited from the Window
	 *         default orientation.
	 * @see org.eclipse.jface.window.Window#getDefaultOrientation()
	 * @since 3.1
	 */

	public int getOrientation() {
		return orientation;
	}

	/**
	 * Sets the orientation that all the widgets created by this toolkit will
	 * inherit. Can be <code>SWT.NULL</code>, <code>SWT.LEFT_TO_RIGHT</code>
	 * and <code>SWT.RIGHT_TO_LEFT</code>.
	 * 
	 * @param orientation
	 *            style for this toolkit.
	 * @since 3.1
	 */

	public void setOrientation(int orientation) {
		this.orientation = orientation;
	}
}
