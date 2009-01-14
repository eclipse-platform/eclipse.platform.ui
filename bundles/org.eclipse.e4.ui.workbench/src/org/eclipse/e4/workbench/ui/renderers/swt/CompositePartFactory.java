package org.eclipse.e4.workbench.ui.renderers.swt;

import org.eclipse.e4.ui.model.application.Part;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Widget;

/**
 * Factory for <code>org.eclipse.e4.workbench.emf.workbench.Composite</code>
 */
public class CompositePartFactory extends SWTPartFactory {
	
	public CompositePartFactory() {
		super();
	}

	public Object createWidget(Part part) {
		final Widget parentWidget = getParentWidget(part);
		
		Widget newWidget = null;
		if (part instanceof Part<?>) {
			Part<?> compositeModel = (Part<?>) part;
			String policy = compositeModel.getPolicy();
			if (policy!=null && (policy.equals("HorizontalComposite")
					|| policy.equals("VerticalComposite"))) {
				Composite composite = new Composite((Composite) parentWidget,
						SWT.NONE);
				newWidget = composite;
				bindWidget(part, newWidget);
			}
		}
		return newWidget;
	}

	@Override
	public void postProcess(Part<?> part) {
		super.postProcess(part);
		
		if (part.getPolicy() != null && part.getPolicy().endsWith("Composite")) {
			Composite composite = (Composite) part.getWidget();
			Control[] children = composite.getChildren();
			GridLayout gl = new GridLayout(
					part.getPolicy().startsWith("Horizontal") ? children.length : 1, false);
			gl.horizontalSpacing = 0;
			gl.verticalSpacing = 0;
			gl.marginHeight = 0;
			gl.marginWidth = 0;
			composite.setLayout(gl);
			for (int i = 0; i < children.length; i++) {
				Control child = children[i];
				boolean grabV = !(child instanceof ToolBar);
				boolean grabH = !(child instanceof ToolBar);
				child.setLayoutData(new GridData(SWT.FILL, SWT.FILL, grabH,
						grabV));
			}
		}
	}
}
