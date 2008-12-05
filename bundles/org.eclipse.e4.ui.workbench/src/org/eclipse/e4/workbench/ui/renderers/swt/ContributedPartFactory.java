package org.eclipse.e4.workbench.ui.renderers.swt;

import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.services.IContributionFactory;
import org.eclipse.e4.core.services.IServiceLocator;
import org.eclipse.e4.ui.model.application.ContributedPart;
import org.eclipse.e4.ui.model.application.Part;
import org.eclipse.e4.ui.services.ISelectionService;
import org.eclipse.e4.workbench.ui.IHandlerService;
import org.eclipse.e4.workbench.ui.behaviors.IHasInput;
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
			ContributedPart contributedPart = (ContributedPart) part;
			final IHandlerService hs = new PartHandlerService(part);
			Object newPart = contributionFactory.create(contributedPart.getURI(), new IServiceLocator() {

				public Object getService(Class api) {
					if (api == Composite.class) {
						newComposite.setData("MODEL", part);
						newComposite.setData("LOCATOR", this);
						return newComposite;
					} else if (api == IHandlerService.class) {
						return hs;
					}
					return serviceLocator.getService(api);
				}

				public boolean hasService(Class api) {
					if (api == Composite.class) {
						return true;
					} else if (api == IHandlerService.class) {
						return true;
					}
					return serviceLocator.hasService(api);
				}
			});
			if (newPart instanceof IHasInput) {
				final IHasInput hasInput = (IHasInput) newPart;
				ISelectionService selectionService = (ISelectionService) serviceLocator.getService(ISelectionService.class);
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
