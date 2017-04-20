package org.eclipse.ui.internal.forms.css.dom;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.CompositeElement;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

public class ExpandableCompositeElement extends CompositeElement {

	public ExpandableCompositeElement(ExpandableComposite composite, CSSEngine engine) {
		super(composite, engine);
	}

	@Override
	public void reset() {
		super.reset();
		getExpandableComposite().setTitleBarForeground(null);
	}

	private ExpandableComposite getExpandableComposite() {
		return (ExpandableComposite) getNativeWidget();
	}

}
