/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms.parts;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.*;

public class FormToolkit {
	public static final String KEY_DRAW_BORDER = "FormWidgetFactory.drawBorder";
	public static final String TREE_BORDER = "treeBorder";

	private FormColors colors;
	private KeyListener deleteListener;
	private BorderPainter borderPainter;
	private HyperlinkGroup hyperlinkGroup;
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
				if (c.getEnabled() == false && !(c instanceof CCombo))
					continue;
				if (c instanceof Hyperlink)
					continue;
				Object flag = c.getData(KEY_DRAW_BORDER);
				if (flag != null) {
					if (flag.equals(Boolean.FALSE))
						continue;
					if (flag.equals(TREE_BORDER))
						inactiveBorder = true;
				}

				if (!inactiveBorder
					&& (c instanceof Text
						|| c instanceof Canvas
						|| c instanceof CCombo)) {
					Rectangle b = c.getBounds();
					GC gc = event.gc;
					gc.setForeground(c.getBackground());
					gc.drawRectangle(
						b.x - 1,
						b.y - 1,
						b.width + 1,
						b.height + 1);
					gc.setForeground(colors.getForeground());
					if (c instanceof CCombo)
						gc.drawRectangle(
							b.x - 1,
							b.y - 1,
							b.width + 1,
							b.height + 1);
					else
						gc.drawRectangle(
							b.x - 1,
							b.y - 2,
							b.width + 1,
							b.height + 3);
				} else if (
					inactiveBorder
						|| c instanceof Table
						|| c instanceof Tree
						|| c instanceof TableTree) {
					Rectangle b = c.getBounds();
					GC gc = event.gc;
					gc.setForeground(colors.getBorderColor());
					//gc.drawRectangle(b.x - 2, b.y - 2, b.width + 3, b.height
					// + 3);
					gc.drawRectangle(
						b.x - 1,
						b.y - 1,
						b.width + 2,
						b.height + 2);
				}
			}
		}
	}

	private static class VisibilityHandler extends FocusAdapter {
		public void focusGained(FocusEvent e) {
			Widget w = e.widget;
			if (w instanceof Control) {
				FormUtil.ensureVisible((Control) w);
			}
		}
	}

	private static class KeyboardHandler extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			Widget w = e.widget;
			if (w instanceof Control) {
				FormUtil.processKey(e.keyCode, (Control) w);
			}
		}
	}
	/**
	 * Creates a toolkit that is self-sufficient (will manage its own colors).
	 *  
	 */

	public FormToolkit() {
		this(new FormColors());
	}

	/**
	 * Creates a toolkit that will use the provided (shared) colors.
	 * 
	 * @param colors
	 *            the shared colors
	 */
	public FormToolkit(FormColors colors) {
		this.colors = colors;
		initialize();
	}

	public Button createButton(Composite parent, String text, int style) {
		Button button = new Button(parent, style | SWT.FLAT);
		button.setBackground(colors.getBackground());
		button.setForeground(colors.getForeground());
		if (text != null)
			button.setText(text);
		button.addFocusListener(visibilityHandler);
		return button;
	}
	public Composite createComposite(Composite parent) {
		return createComposite(parent, SWT.NULL);
	}
	public Composite createComposite(Composite parent, int style) {
		Composite composite = new Composite(parent, style);
		composite.setBackground(colors.getBackground());
		composite.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				((Control) e.widget).setFocus();
			}
		});
		composite.setMenu(parent.getMenu());
		return composite;
	}
	public Composite createCompositeSeparator(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setBackground(colors.getColor(FormColors.SEPARATOR));
		return composite;
	}

	public Label createHeadingLabel(Composite parent, String text) {
		return createHeadingLabel(parent, text, SWT.NONE);
	}

	public Label createHeadingLabel(Composite parent, String text, int style) {
		Label label = new Label(parent, style);
		if (text != null)
			label.setText(text);
		label.setBackground(colors.getBackground());
		label.setForeground(colors.getForeground());
		label.setFont(
			JFaceResources.getFontRegistry().get(JFaceResources.BANNER_FONT));
		return label;
	}

	public Label createLabel(Composite parent, String text) {
		return createLabel(parent, text, SWT.NONE);
	}
	public Label createLabel(Composite parent, String text, int style) {
		Label label = new Label(parent, style);
		if (text != null)
			label.setText(text);
		label.setBackground(colors.getBackground());
		label.setForeground(colors.getForeground());
		return label;
	}

	public TraversableLabel createSelectableLabel(
		Composite parent,
		String text) {
		return createSelectableLabel(parent, text, SWT.NONE);
	}

	public TraversableLabel createSelectableLabel(
		Composite parent,
		String text,
		int style) {
		TraversableLabel label = new TraversableLabel(parent, style);
		if (text != null)
			label.setText(text);
		label.setBackground(colors.getBackground());
		label.setForeground(colors.getForeground());
		label.addFocusListener(visibilityHandler);
		label.addKeyListener(keyboardHandler);
		return label;
	}

	public RichText createRichText(Composite parent, boolean trackFocus) {
		RichText engine = new RichText(parent, SWT.WRAP);
		engine.setBackground(colors.getBackground());
		engine.setForeground(colors.getForeground());
		engine.marginWidth = 1;
		engine.marginHeight = 0;
		engine.setHyperlinkSettings(getHyperlinkGroup());
		if (trackFocus)
			engine.addFocusListener(visibilityHandler);
		engine.addKeyListener(keyboardHandler);
		engine.setMenu(parent.getMenu());
		return engine;
	}

	public Twistie createTwistie(Composite parent) {
		Twistie twistie = new Twistie(parent, SWT.NULL);
		twistie.setBackground(colors.getBackground());
		twistie.setActiveDecorationColor(
			getHyperlinkGroup().getActiveForeground());
		twistie.setDecorationColor(colors.getColor(FormColors.SEPARATOR));
		twistie.setActiveCursor(FormsResources.getHandCursor());
		twistie.addFocusListener(visibilityHandler);
		twistie.addKeyListener(keyboardHandler);
		return twistie;
	}

	public ExpandableComposite createExpandableComposite(Composite parent) {
		ExpandableComposite ec = new ExpandableComposite(parent, SWT.NULL);
		ec.setBackground(colors.getBackground());
		ec.setForeground(colors.getForeground());
		ec.textLabel.setBackground(colors.getBackground());
		hyperlinkGroup.add(ec.textLabel);
		ec.textLabel.addFocusListener(visibilityHandler);
		ec.textLabel.addKeyListener(keyboardHandler);
		ec.textLabel.setFont(
			JFaceResources.getFontRegistry().get(JFaceResources.BANNER_FONT));
		return ec;
	}

	public Label createSeparator(Composite parent, int style) {
		Label label = new Label(parent, SWT.SEPARATOR | style);
		label.setBackground(colors.getBackground());
		label.setForeground(colors.getBorderColor());
		return label;
	}
	public Table createTable(Composite parent, int style) {
		Table table = new Table(parent, style);
		table.setBackground(colors.getBackground());
		table.setForeground(colors.getForeground());
		hookDeleteListener(table);
		return table;
	}
	public Text createText(Composite parent, String value) {
		return createText(parent, value, SWT.SINGLE);
	}
	public Text createText(Composite parent, String value, int style) {
		Text text = new Text(parent, style);
		if (value != null)
			text.setText(value);
		text.setBackground(colors.getBackground());
		text.setForeground(colors.getForeground());
		text.addFocusListener(visibilityHandler);
		return text;
	}
	public Tree createTree(Composite parent, int style) {
		Tree tree = new Tree(parent, style);
		tree.setBackground(colors.getBackground());
		tree.setForeground(colors.getForeground());
		hookDeleteListener(tree);
		return tree;
	}
	private void deleteKeyPressed(Widget widget) {
		if (!(widget instanceof Control))
			return;
		Control control = (Control) widget;
		for (Control parent = control.getParent();
			parent != null;
			parent = parent.getParent()) {
			if (parent.getData() instanceof SectionPart) {
				SectionPart section = (SectionPart) parent.getData();
				section.doGlobalAction(ActionFactory.DELETE.getId());
				break;
			}
		}
	}
	public void dispose() {
		if (colors.isShared() == false) {
			colors.dispose();
			colors = null;
		}
	}
	public HyperlinkGroup getHyperlinkGroup() {
		return hyperlinkGroup;
	}
	public void hookDeleteListener(Control control) {
		if (deleteListener == null) {
			deleteListener = new KeyAdapter() {
				public void keyPressed(KeyEvent event) {
					if (event.character == SWT.DEL && event.stateMask == 0) {
						deleteKeyPressed(event.widget);
					}
				}
			};
		}
		control.addKeyListener(deleteListener);
	}
	private void initialize() {
		hyperlinkGroup = new HyperlinkGroup();
		hyperlinkGroup.setBackground(colors.getBackground());
		visibilityHandler = new VisibilityHandler();
		keyboardHandler = new KeyboardHandler();
	}

	public void updateHyperlinkColors() {
		hyperlinkGroup.initializeDefaultForegrounds();
	}

	public void paintBordersFor(Composite parent) {
		if (borderPainter == null)
			borderPainter = new BorderPainter();
		parent.addPaintListener(borderPainter);
	}

	public FormColors getColors() {
		return colors;
	}
}
