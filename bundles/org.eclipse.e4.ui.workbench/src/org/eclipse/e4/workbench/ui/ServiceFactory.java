package org.eclipse.e4.workbench.ui;

import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.services.AbstractServiceFactory;
import org.eclipse.e4.core.services.IBackgroundRunner;
import org.eclipse.e4.core.services.IRunnableWithProgress;
import org.eclipse.e4.core.services.IServiceLocator;
import org.eclipse.e4.ui.services.ISelectionService;
import org.eclipse.jface.viewers.IStructuredSelection;

public class ServiceFactory extends AbstractServiceFactory {

	static class SelectionWritableValue extends WritableValue implements
			ISelectionService {

		public Object getSelection(Class api) {
			Object value = getValue();
			if (api.isInstance(value)) {
				return value;
			}
			
			if (value instanceof IStructuredSelection) {
				value = ((IStructuredSelection) value).getFirstElement();
			}
			if (api.isInstance(value)) {
				return value;
			} else if (value != null) {
				return Platform.getAdapterManager().loadAdapter(value, api.getName());
			}
			return null;
		}
	}

	public Object create(Class serviceInterface, IServiceLocator parentLocator,
			IServiceLocator locator) {
		if (serviceInterface == ISelectionService.class) {
			return new SelectionWritableValue();
		} else if (serviceInterface == IProgressMonitor.class) {
			return new NullProgressMonitor();
		} else if (serviceInterface == IBackgroundRunner.class) {
			return new IBackgroundRunner() {
				public void schedule(long delay, String name,
						final IRunnableWithProgress runnable) {
					new Job(name) {
						protected IStatus run(IProgressMonitor monitor) {
							return runnable.run(monitor);
						}
					}.schedule(delay);
				}
			};
		}
		return null;
	}

}
