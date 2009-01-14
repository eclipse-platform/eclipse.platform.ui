package org.eclipse.e4.workbench.ui.renderers.swt;

import java.util.Iterator;
import java.util.List;

import org.eclipse.e4.core.services.Context;
import org.eclipse.e4.core.services.IContributionFactory;
import org.eclipse.e4.ui.model.application.ApplicationElement;
import org.eclipse.e4.ui.model.application.Item;
import org.eclipse.e4.ui.model.application.ItemPart;
import org.eclipse.e4.ui.model.application.Menu;
import org.eclipse.e4.ui.model.application.Part;
import org.eclipse.e4.ui.model.application.ToolBar;
import org.eclipse.e4.workbench.ui.utils.ResourceUtility;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

public abstract class PartFactory {
	public static final String OWNING_ME = "modelElement";

	protected PartRenderer renderer;
	protected IContributionFactory contributionFactory;
	protected Context context;
	protected EMFDataBindingContext dbc;
	
	public PartFactory() {		
		dbc = new EMFDataBindingContext();
	}
	
	public void init(PartRenderer renderer, Context context, IContributionFactory contributionFactory) {
		this.renderer = renderer;
		this.contributionFactory = contributionFactory;
		this.context = context;
	}
	
	public abstract Object createWidget(Part<?> element);

	public <P extends Part<?>> void processContents(Part<P> me) {
		Widget parentWidget = getParentWidget(me);
		if (parentWidget == null)
			return;
		
		// Process any contents of the newly created ME
		List<P> parts = me.getChildren();
		if (parts != null) {
			for (Iterator<P> childIter = parts.iterator(); childIter
					.hasNext();) {
				Part<?> childME = childIter.next();
				renderer.createGui(childME);
			}
		}
	}
	
	public void postProcess(Part<?> childME) {
	}

	public void bindWidget(Part<?> me, Object widget) {
		me.setWidget(widget);
		((Widget)widget).setData(OWNING_ME, me);
	}

	protected Widget getParentWidget(Part<?> element) {
		return (element.getParent() instanceof Part) ?
				(Widget) ((Part<?>)(element.getParent())).getWidget() : null;
	}
	
	public void disposeWidget(Part<?> part) {
		Widget curWidget = (Widget) part.getWidget();
		part.setWidget(null);
		if (curWidget != null)
			curWidget.dispose();
	}

	public void hookControllerLogic(final Part<?> me) {
		Widget widget = (Widget) me.getWidget();
		
		// Clean up if the widget is disposed
		widget.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				Part<?> model = (Part<?>) e.widget.getData(OWNING_ME);
				model.setWidget(null);
			}
		});
		
		// add an accessibility listener (not sure if this is in the wrong place (factory?)
		if (widget instanceof Control && me instanceof ItemPart<?>) {
        	((Control)widget).getAccessible().addAccessibleListener(new AccessibleAdapter() {
            	public void getName(AccessibleEvent e) {
            		e.result = ((ItemPart<?>)me).getName();
            	}
            });
		}
	}

	public void childAdded(Part<?> parentElement, Part<?> element) {
		// Ensure the child's widget is under the new parent
		if (parentElement.getWidget() instanceof Composite
				&& element.getWidget() instanceof Control) {
			Composite comp = (Composite) parentElement.getWidget();
			Control ctrl = (Control) element.getWidget();
			ctrl.setParent(comp);
		}
	}

	public void childRemoved(Part<?> parentElement, Part<?> child) {
	}
	
	protected Image getImage(ApplicationElement element) {
		if (element instanceof Item) {
			String iconURI = ((Item) element).getIconURI();
			if (iconURI != null && !iconURI.equals("null")) {
				ResourceUtility resUtils = (ResourceUtility) context
						.get(ResourceUtility.class.getName());
				ImageDescriptor desc = resUtils.imageDescriptorFromURI(URI.createURI(iconURI));
				if (desc != null)
					return desc.createImage();
			}
		}
		return null;
	}

	public void createMenu(Object widgetObject, Menu menu) {
	}

	public void createToolBar(Object widgetObject, ToolBar toolBar) {
	}
}
