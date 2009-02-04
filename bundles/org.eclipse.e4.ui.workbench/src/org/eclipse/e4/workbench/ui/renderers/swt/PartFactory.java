package org.eclipse.e4.workbench.ui.renderers.swt;

import java.util.Iterator;
import java.util.List;

import org.eclipse.e4.core.services.IContributionFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MItem;
import org.eclipse.e4.ui.model.application.MItemPart;
import org.eclipse.e4.ui.model.application.MMenu;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MToolBar;
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
	public static final String OWNING_ME = "modelElement"; //$NON-NLS-1$

	protected PartRenderer renderer;
	protected IContributionFactory contributionFactory;
	private IEclipseContext context;
	protected EMFDataBindingContext dbc;

	public PartFactory() {
		dbc = new EMFDataBindingContext();
	}

	public void init(PartRenderer renderer, IEclipseContext context,
			IContributionFactory contributionFactory) {
		this.renderer = renderer;
		this.contributionFactory = contributionFactory;
		this.context = context;
	}

	public abstract Object createWidget(MPart<?> element);

	public <P extends MPart<?>> void processContents(MPart<P> me) {
		Widget parentWidget = getParentWidget(me);
		if (parentWidget == null)
			return;

		// Process any contents of the newly created ME
		List<P> parts = me.getChildren();
		if (parts != null) {
			for (Iterator<P> childIter = parts.iterator(); childIter.hasNext();) {
				MPart<?> childME = childIter.next();
				renderer.createGui(childME);
			}
		}
	}

	public void postProcess(MPart<?> childME) {
	}

	public void bindWidget(MPart<?> me, Object widget) {
		me.setWidget(widget);
		((Widget) widget).setData(OWNING_ME, me);
	}

	protected Widget getParentWidget(MPart<?> element) {
		return (element.getParent() instanceof MPart) ? (Widget) ((MPart<?>) (element
				.getParent())).getWidget()
				: null;
	}

	public void disposeWidget(MPart<?> part) {
		Widget curWidget = (Widget) part.getWidget();
		part.setWidget(null);
		if (curWidget != null)
			curWidget.dispose();
	}

	public void hookControllerLogic(final MPart<?> me) {
		Widget widget = (Widget) me.getWidget();

		// Clean up if the widget is disposed
		widget.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				MPart<?> model = (MPart<?>) e.widget.getData(OWNING_ME);
				model.setWidget(null);
			}
		});

		// add an accessibility listener (not sure if this is in the wrong place
		// (factory?)
		if (widget instanceof Control && me instanceof MItemPart<?>) {
			((Control) widget).getAccessible().addAccessibleListener(
					new AccessibleAdapter() {
						public void getName(AccessibleEvent e) {
							e.result = ((MItemPart<?>) me).getName();
						}
					});
		}
	}

	public void childAdded(MPart<?> parentElement, MPart<?> element) {
		// Ensure the child's widget is under the new parent
		if (parentElement.getWidget() instanceof Composite
				&& element.getWidget() instanceof Control) {
			Composite comp = (Composite) parentElement.getWidget();
			Control ctrl = (Control) element.getWidget();
			ctrl.setParent(comp);
		}
	}

	public void childRemoved(MPart<?> parentElement, MPart<?> child) {
	}

	protected Image getImage(MApplicationElement element) {
		if (element instanceof MItem) {
			IEclipseContext localContext = context;
			if (element instanceof MPart<?>) {
				localContext = getContext((MPart<?>) element);
			}
			String iconURI = ((MItem) element).getIconURI();
			if (iconURI != null && !iconURI.equals("null")) { //$NON-NLS-1$
				ResourceUtility resUtils = (ResourceUtility) localContext
						.get(ResourceUtility.class.getName());
				ImageDescriptor desc = resUtils.imageDescriptorFromURI(URI
						.createURI(iconURI));
				if (desc != null)
					return desc.createImage();
			}
		}
		return null;
	}

	public void createMenu(MPart<?> part, Object widgetObject, MMenu menu) {
	}

	public void createToolBar(MPart<?> part, Object widgetObject, MToolBar toolBar) {
	}

	/**
	 * Return a parent context for this part.
	 * 
	 * @param part
	 *            the part to start searching from
	 * @return the parent's closest context, or global context if none in the
	 *         hierarchy
	 */
	protected IEclipseContext getContextForParent(MPart<?> part) {
		MPart<?> parent = part.getParent();
		while (parent != null) {
			if (parent.getContext() != null) {
				return parent.getContext();
			}
			parent = parent.getParent();
		}
		return context;
	}

	/**
	 * Return a context for this part.
	 * 
	 * @param part
	 *            the part to start searching from
	 * @return the closest context, or global context if none in the hierarchy
	 */
	protected IEclipseContext getContext(MPart<?> part) {
		if (part.getContext() != null) {
			return part.getContext();
		}
		return getContextForParent(part);
	}
}
