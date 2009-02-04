package org.eclipse.e4.workbench.ui.renderers.swt;

import org.eclipse.e4.core.services.IContributionFactory;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextInjectionFactory;
import org.eclipse.e4.ui.model.application.MContributedPart;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.workbench.ui.IHandlerService;
import org.eclipse.e4.workbench.ui.internal.UIContextScheduler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;

public class ContributedPartFactory extends SWTPartFactory {

	private IContributionFactory contributionFactory;

	public ContributedPartFactory() {
		super();
	}
	
	public void setContributionFactory(IContributionFactory contributionFactory) {
		this.contributionFactory = contributionFactory;
	}

	public Object createWidget(final MPart part) {
		Widget parentWidget = getParentWidget(part);
		Widget newWidget = null;

		if (part instanceof MContributedPart) {
			final Composite newComposite = new Composite((Composite) parentWidget, SWT.NONE);
			newWidget = newComposite;
			bindWidget(part, newWidget);
			MContributedPart contributedPart = (MContributedPart) part;
			final IHandlerService hs = new PartHandlerService(part);
			IEclipseContext localContext = EclipseContextFactory.create("ContributedPart", context, UIContextScheduler.instance); //$NON-NLS-1$
			newComposite.setData("LOCATOR", localContext); //$NON-NLS-1$
			localContext.set(Composite.class.getName(), newComposite);
			localContext.set(IHandlerService.class.getName(), hs);
			// XXX remove context as constructor argument and rely on injection only
			Object newPart = contributionFactory.create(contributedPart.getURI(), localContext);
			ContextInjectionFactory.inject(newPart, localContext);
		}

		return newWidget;
	}
}
