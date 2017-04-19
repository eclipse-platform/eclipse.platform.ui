package org.eclipse.ui.internal.forms.css.dom;
import org.eclipse.e4.ui.css.core.dom.IElementProvider;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.w3c.dom.Element;

public class ExpandableCompositeElementProvider implements IElementProvider {

	@Override
	public Element getElement(Object element, CSSEngine engine) {
		if (element instanceof ExpandableComposite) {
			return new ExpandableCompositeElement((ExpandableComposite) element, engine);
		}
		return null;
	}

}
