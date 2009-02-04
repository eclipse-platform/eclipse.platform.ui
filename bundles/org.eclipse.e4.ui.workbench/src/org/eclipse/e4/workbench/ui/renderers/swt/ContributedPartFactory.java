package org.eclipse.e4.workbench.ui.renderers.swt;

import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.services.IContributionFactory;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.model.application.MContributedPart;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.workbench.ui.IHandlerService;
import org.eclipse.e4.workbench.ui.behaviors.IHasInput;
import org.eclipse.e4.workbench.ui.internal.UIContextScheduler;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
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
			final Composite newComposite = new Composite((Composite) parentWidget,
					SWT.NONE);
			newWidget = newComposite;
			bindWidget(part, newWidget);
			final MContributedPart contributedPart = (MContributedPart) part;
			final IHandlerService hs = new PartHandlerService(part);
			IEclipseContext localContext = EclipseContextFactory.create(
					"ContributedPart", context, UIContextScheduler.instance); //$NON-NLS-1$
			IEclipseContext outputContext = EclipseContextFactory.create(
					"ContributedPart-output", null, UIContextScheduler.instance); //$NON-NLS-1$
			associate(newComposite, localContext);
			localContext.set(Composite.class.getName(), newComposite);
			localContext.set(IHandlerService.class.getName(), hs);
			newComposite.addListener(SWT.Activate, new Listener() {
				public void handleEvent(Event event) {
					activate(part);
				}
			});
			localContext.set(IServiceConstants.OUTPUTS, outputContext);
			localContext.set(IEclipseContext.class.getName(), outputContext);
			Object newPart = contributionFactory.create(contributedPart.getURI(),
					localContext);
			if (newPart instanceof IHasInput) {
				final IHasInput hasInput = (IHasInput) newPart;
				context.runAndTrack(new Runnable() {
					public void run() {
						Class adapterType = hasInput.getInputType();
						Object newInput = null;
						Object newValue = context.get(IServiceConstants.SELECTION);
						if (newValue instanceof IStructuredSelection) {
							newValue = ((IStructuredSelection) newValue)
									.getFirstElement();
						}
						if (adapterType.isInstance(newValue)) {
							newInput = newValue;
						} else if (newValue != null) {
							newInput = Platform.getAdapterManager().loadAdapter(newValue,
									adapterType.getName());
						}
						hasInput.setInput(newInput);
					}
				}, "selection-updater"); //$NON-NLS-1$ 
			}
		}

		return newWidget;
	}

}
