package org.eclipse.update.ui.forms;

import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;

public abstract class ExpandableGroup {
	private String text;
	private boolean expanded;
	private Composite expansion;
	private Label textLabel;
	private Composite control;
	
class ExpandableLayout extends Layout {
	protected void layout(Composite parent, boolean changed) {
		Rectangle clientArea = parent.getClientArea();
		Point size = textLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, changed);
		textLabel.setBounds(0, 0, size.x, size.y);
		int y = size.y + 2;
		if (expanded) {
			size = expansion.computeSize(clientArea.width, SWT.DEFAULT, changed);
			expansion.setBounds(0, y, size.x, size.y);
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
		return new Point(width, height);
	}
}
	
	public ExpandableGroup() {
	}
	
	public Control getControl() {
		return control;
	}
	
	public void createControl(Composite parent, FormWidgetFactory factory) {
		Composite container = factory.createComposite(parent);
		container.setLayout(new ExpandableLayout());
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
	
	protected void aboutToExpand() {
	}
	
	protected void aboutToCollapse() {
	}
	
	protected void expanded() {
	}
	protected void collapsed() {
	}
}