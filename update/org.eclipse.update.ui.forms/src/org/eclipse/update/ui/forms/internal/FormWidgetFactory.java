package org.eclipse.update.ui.forms.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.swt.layout.*;
import org.eclipse.swt.graphics.*;
import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.resource.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.custom.*;
import org.eclipse.ui.IWorkbenchActionConstants;


public class FormWidgetFactory {
	public static final String DEFAULT_HEADER_COLOR = "__default__header__";
	public static final String COLOR_BACKGROUND = "__bg";
	public static final String COLOR_BORDER = "__border";
	public static final String COLOR_COMPOSITE_SEPARATOR = "__compSep";
	public static final String COLOR_HYPERLINK="__hyperlink";
	
	private Hashtable colorRegistry = new Hashtable();
	private Color backgroundColor;
	private Color clientAreaColor;
	private KeyListener deleteListener;
	private Color foregroundColor;
	private Display display;
	public static final int BORDER_STYLE = SWT.NONE;//SWT.BORDER;
	private BorderPainter borderPainter;
	private Color borderColor;
	private HyperlinkHandler hyperlinkHandler;


	class BorderPainter implements PaintListener {
		public void paintControl(PaintEvent event) {
			Composite composite = (Composite) event.widget;
			Control[] children = composite.getChildren();
			for (int i = 0; i < children.length; i++) {
				Control c = children[i];
				if (c.isEnabled()==false) continue;
				if (c instanceof SelectableFormLabel) continue;
				if (c instanceof Text
					|| c instanceof Canvas
					|| c instanceof CCombo) {
					Rectangle b = c.getBounds();
					GC gc = event.gc;
					gc.setForeground(c.getBackground());
					gc.drawRectangle(b.x - 1, b.y - 1, b.width + 1, b.height + 1);
					gc.setForeground(foregroundColor);
					gc.drawRectangle(b.x - 2, b.y - 2, b.width + 3, b.height + 3);
				}
				else if (c instanceof Table
					|| c instanceof Tree
					|| c instanceof TableTree) {
					Rectangle b = c.getBounds();
					GC gc = event.gc;
					gc.setForeground(borderColor);
					//gc.drawRectangle(b.x - 2, b.y - 2, b.width + 3, b.height + 3);
					gc.drawRectangle(b.x-1, b.y-1, b.width+2, b.height+2);
				}
			}
		}
	}


public FormWidgetFactory() {
	this(Display.getCurrent());
}
public FormWidgetFactory(Display display) {
	this.display = display;
	initialize();
}
public Button createButton(Composite parent, String text, int style) {
	int flatStyle = BORDER_STYLE == SWT.BORDER ? SWT.NULL : SWT.FLAT;
	//int flatStyle = SWT.NULL;
	Button button = new Button(parent, style | flatStyle);
	button.setBackground(backgroundColor);
	button.setForeground(foregroundColor);
	if (text!=null) button.setText(text);
	return button;
}
public Composite createComposite(Composite parent) {
	return createComposite(parent, SWT.NULL);
}
public Composite createComposite(Composite parent, int style) {
	Composite composite = new Composite(parent, style);
	composite.setBackground(backgroundColor);
	composite.addMouseListener(new MouseAdapter() {
		public void mousePressed(MouseEvent e) {
			((Control)e.widget).setFocus();
		}
	});
	return composite;
}
public Composite createCompositeSeparator(Composite parent) {
	Composite composite = new Composite(parent, SWT.NONE);
	composite.setBackground(getColor(COLOR_COMPOSITE_SEPARATOR));
	return composite;
}

public Label createHeadingLabel(Composite parent, String text) {
	return createHeadingLabel(parent, text, null, SWT.NONE);
}

public Label createHeadingLabel(Composite parent, String text, Color bg) {
	return createHeadingLabel(parent, text, bg, SWT.NONE);
}

public Label createHeadingLabel(Composite parent, String text, Color bg, int style) {
	Label label = new Label(parent, style);
	if (text!=null) label.setText(text);
	label.setBackground(backgroundColor);
	label.setForeground(foregroundColor);
    label.setFont(JFaceResources.getFontRegistry().get(JFaceResources.BANNER_FONT));
	return label;
}

public Label createHyperlinkLabel(Composite parent, String text, IHyperlinkListener listener) {
	return createHyperlinkLabel(parent, text, listener, SWT.NULL);
}
public Label createHyperlinkLabel(Composite parent, String text, IHyperlinkListener listener, int style) {
	Label label = createLabel(parent, text, style);
	turnIntoHyperlink(label, listener);
	return label;
}
public Label createLabel(Composite parent, String text) {
	return createLabel(parent, text, SWT.NONE);
}
public Label createLabel(Composite parent, String text, int style) {
	Label label = new Label(parent, style);
	if (text!=null) label.setText(text);
	label.setBackground(backgroundColor);
	label.setForeground(foregroundColor);
	return label;
}

public SelectableFormLabel createSelectableLabel(Composite parent, String text) {
	return createSelectableLabel(parent, text, SWT.NONE);
}

public SelectableFormLabel createSelectableLabel(Composite parent, String text, int style) {
	SelectableFormLabel label = new SelectableFormLabel(parent, style);
	if (text!=null) label.setText(text);
	label.setBackground(backgroundColor);
	label.setForeground(foregroundColor);
	return label;	
}

public Label createSeparator(Composite parent, int style) {
	Label label = new Label(parent, SWT.SEPARATOR | style);
	label.setBackground(backgroundColor);
	label.setForeground(borderColor);
	return label;
}
public Table createTable(Composite parent, int style) {
	Table table = new Table(parent, BORDER_STYLE | style);
	table.setBackground(backgroundColor);
	table.setForeground(foregroundColor);
	hookDeleteListener(table);
	return table;
}
public Text createText(Composite parent, String value) {
	return createText(parent, value, BORDER_STYLE | SWT.SINGLE);
}
public Text createText(Composite parent, String value, int style) {
	Text text = new Text(parent, style);
	text.setText(value);
	text.setBackground(clientAreaColor);
	text.setForeground(foregroundColor);
	return text;
}
public Tree createTree(Composite parent, int style) {
	Tree tree = new Tree(parent, BORDER_STYLE | style);
	tree.setBackground(backgroundColor);
	tree.setForeground(foregroundColor);
	hookDeleteListener(tree);
	return tree;
}
private void deleteKeyPressed(Widget widget) {
	if (!(widget instanceof Control)) return;
	Control control = (Control)widget;
	for (Control parent = control.getParent();
		parent != null;
		parent = parent.getParent()) {
		if (parent.getData() instanceof FormSection) {
			FormSection section = (FormSection) parent.getData();
			section.doGlobalAction(IWorkbenchActionConstants.DELETE);
			break;
		}
	}
}
public void dispose() {
	Enumeration colors= colorRegistry.elements();
	while (colors.hasMoreElements()) {
		Color c = (Color)colors.nextElement();
		c.dispose();
	}
	hyperlinkHandler.dispose();
	colorRegistry=null;
}
public Color getBackgroundColor() {
	return backgroundColor;
}
public Color getBorderColor() {
	return borderColor;
}
public Cursor getBusyCursor() {
	return hyperlinkHandler.getBusyCursor();
}
public Color getClientAreaColor() {
	return clientAreaColor;
}
public Color getColor(String key) {
	return (Color)colorRegistry.get(key);
}
public Color getForegroundColor() {
	return foregroundColor;
}
public HyperlinkHandler getHyperlinkHandler() {
	return hyperlinkHandler;
}
public Cursor getHyperlinkCursor() {
	return hyperlinkHandler.getHyperlinkCursor();
}
public Color getHyperlinkColor() {
	return hyperlinkHandler.getForeground();
}
public Color getHyperlinkHoverColor() {
	return hyperlinkHandler.getActiveForeground();
}
public int getHyperlinkUnderlineMode() {
	return hyperlinkHandler.getHyperlinkUnderlineMode();
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
	clientAreaColor = display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
	//registerColor(COLOR_BACKGROUND, 0xff, 0xfe, 0xf9);
	registerColor(COLOR_BORDER, 195, 191, 179);
	registerColor(COLOR_COMPOSITE_SEPARATOR, 152, 170, 203);
	registerColor(DEFAULT_HEADER_COLOR, 0x48, 0x70, 0x98);
	//backgroundColor = getColor(COLOR_BACKGROUND);
	backgroundColor = clientAreaColor;
	borderColor = getColor(COLOR_BORDER);
	foregroundColor = display.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
	hyperlinkHandler = new HyperlinkHandler();
	hyperlinkHandler.setForeground(registerColor(COLOR_HYPERLINK, 0, 0, 153));
	hyperlinkHandler.setBackground(backgroundColor);
}

public void paintBordersFor(Composite parent) {
	if (BORDER_STYLE == SWT.BORDER) return;
	if (borderPainter==null) borderPainter = new BorderPainter();
	parent.addPaintListener(borderPainter);
}
public Color registerColor(String key, int r, int g, int b) {
	Color c = new Color(display, r, g, b);
	colorRegistry.put(key, c);
	return c;
}
public void setHyperlinkColor(Color color) {
	hyperlinkHandler.setForeground(color);
}
public void setHyperlinkHoverColor(org.eclipse.swt.graphics.Color hoverColor) {
	hyperlinkHandler.setActiveForeground(hoverColor);
}
public void setHyperlinkUnderlineMode(int newHyperlinkUnderlineMode) {
	hyperlinkHandler.setHyperlinkUnderlineMode(newHyperlinkUnderlineMode);
}
public void turnIntoHyperlink(Control control, IHyperlinkListener listener) {
	hyperlinkHandler.registerHyperlink(control, listener);
}
}
