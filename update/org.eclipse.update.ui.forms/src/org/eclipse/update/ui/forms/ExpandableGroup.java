package org.eclipse.update.ui.forms;

import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;

public abstract class ExpandableGroup {
	private String text;
	private boolean expanded;
	private Composite expansion;
	private Label textLabel;
	private Composite control;
	private int style;
	
class ExpandableLayout extends Layout {
	protected void layout(Composite parent, boolean changed) {
		Rectangle clientArea = parent.getClientArea();
		Point size = textLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, changed);
		int x = 8 + 8;
		textLabel.setBounds(x, 0, size.x, size.y);
		int y = Math.max(size.y, 8) + 2;
		if (expanded) {
			size = expansion.computeSize(clientArea.width, SWT.DEFAULT, changed);
			expansion.setBounds(x, y, size.x, size.y);
		}
	}
	protected Point computeSize(Composite parent, int wHint, int hHint, boolean changed) {
		int width = 0, height = 0;
		Point size = textLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, changed);
		width = size.x;
		height = size.y + 2;
		if (expanded) {
			size = expansion.computeSize(wHint, SWT.DEFAULT, changed);
			width = Math.max(width, size.x);
			height += size.y;
		}
		height = Math.max(height, 8);
		width += 8 + 8;
		return new Point(width, height);
	}
}

	public ExpandableGroup() {
	}
	
	public ExpandableGroup(int style) {
		this.style = style;
	}
	
	public Control getControl() {
		return control;
	}
	
	public void createControl(Composite parent, FormWidgetFactory factory) {
		Composite container = factory.createComposite(parent, style);
		container.setLayout(new ExpandableLayout());
		container.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				repaint(e);
			}
		});
		container.addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				Rectangle box = getBoxBounds(null);
				if (box.contains(e.x, e.y)) {
					setExpanded(!isExpanded());
				}
			}
		});
		textLabel = factory.createHyperlinkLabel(container, null, new HyperlinkAdapter () {
			public void linkActivated(Control link) {
				setExpanded(!isExpanded());
			}
		});
		if (text!=null) textLabel.setText(text);
		expansion = factory.createComposite(container);
		fillExpansion(expansion, factory);
		this.control = container;
	}
	
	public abstract void fillExpansion(Composite expansion, FormWidgetFactory factory);
	
	public void setText(String text) {
		this.text = text;
		if (textLabel!=null)
		   textLabel.setText(text);
	}
	
	public String getText() {
		return text;
	}
	
	public boolean isExpanded() {
		return expanded;
	}
	
	public void setExpanded(boolean expanded) {
		if (this.expanded != expanded) {
			if (expanded) {
				aboutToExpand();
			}
			else {
				aboutToCollapse();
			}
			this.expanded = expanded;
			control.layout();
			if (expanded) {
				this.expanded();
			}
			else {
				collapsed();
			}
		}
	}
	
	private void repaint(PaintEvent e) {
		GC gc = e.gc;
		Rectangle box = getBoxBounds(gc);
		gc.setForeground(control.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
		gc.drawRectangle(box);
		gc.setForeground(control.getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND));
		gc.drawLine(box.x+2, box.y+4, box.x+6, box.y+4);
		if (!isExpanded()) {
			gc.drawLine(box.x+4, box.y+2, box.x+4, box.y+6);
		}
	}
	
	private Rectangle getBoxBounds(GC gc) {
		int x = 0;
		int y = 0;
		boolean noGC = false;
	
		if (gc==null) {
			gc = new GC(control);
			noGC = true;
		}
		gc.setFont(textLabel.getFont());
		int height = gc.getFontMetrics().getHeight();
		y = height/2 - 4;
		y = Math.max(y, 0);
		if (noGC) gc.dispose();
		return new Rectangle(x, y, 8, 8);
	}
	
	protected void aboutToExpand() {
	}
	
	protected void aboutToCollapse() {
	}
	
	protected void expanded() {
	}
	protected void collapsed() {
	}
}