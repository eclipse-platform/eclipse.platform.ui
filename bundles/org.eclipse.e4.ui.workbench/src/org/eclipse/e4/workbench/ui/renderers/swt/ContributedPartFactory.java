package org.eclipse.e4.workbench.ui.renderers.swt;

import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.services.IContributionFactory;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.model.application.ContributedPart;
import org.eclipse.e4.ui.model.application.Part;
import org.eclipse.e4.ui.services.ISelectionService;
import org.eclipse.e4.workbench.ui.IHandlerService;
import org.eclipse.e4.workbench.ui.behaviors.IHasInput;
import org.eclipse.e4.workbench.ui.internal.UIContextScheduler;
import org.eclipse.jface.viewers.IStructuredSelection;
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

	public Object createWidget(final Part part) {
		Widget parentWidget = getParentWidget(part);
		Widget newWidget = null;

		if (part instanceof ContributedPart) {
			final Composite newComposite = new Composite((Composite) parentWidget, SWT.NONE);
			newWidget = newComposite;
			bindWidget(part, newWidget);
			ContributedPart contributedPart = (ContributedPart) part;
			final IHandlerService hs = new PartHandlerService(part);
			IEclipseContext localContext = EclipseContextFactory.create("ContributedPart", context, UIContextScheduler.instance); //$NON-NLS-1$
			newComposite.setData("LOCATOR", localContext); //$NON-NLS-1$
			localContext.set(Composite.class.getName(), newComposite);
			localContext.set(IHandlerService.class.getName(), hs);
			Object newPart = contributionFactory.create(contributedPart.getURI(), localContext);
			if (newPart instanceof IHasInput) {
				final IHasInput hasInput = (IHasInput) newPart;
				ISelectionService selectionService = (ISelectionService) context.get(ISelectionService.class.getName());
				selectionService.addValueChangeListener(new IValueChangeListener(){
					public void handleValueChange(ValueChangeEvent event) {
						Class adapterType = hasInput.getInputType();
						Object newInput = null;
						Object newValue = event.diff.getNewValue();
						if (newValue instanceof IStructuredSelection) {
							newValue = ((IStructuredSelection) newValue).getFirstElement();
						}
						if (adapterType.isInstance(newValue)) {
							newInput = newValue;
						} else if (newValue != null) {
							newInput = Platform.getAdapterManager().loadAdapter(newValue, adapterType.getName());
						}
						hasInput.setInput(newInput);
					}
				});
			}
		}

		return newWidget;
	}
}
