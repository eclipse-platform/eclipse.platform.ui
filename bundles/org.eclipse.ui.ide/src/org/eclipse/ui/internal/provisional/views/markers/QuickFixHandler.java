package org.eclipse.ui.internal.provisional.views.markers;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * QuickFixHandler is the command handler for the quick fix dialog.
 * 
 * @since 3.4
 * 
 */
public class QuickFixHandler extends MarkerViewHandler {
	
	private class QuickFixWizardDialog extends WizardDialog{

		/**
		 * @param parentShell
		 * @param newWizard
		 */
		public QuickFixWizardDialog(Shell parentShell, IWizard newWizard) {
			super(parentShell, newWizard);
			setShellStyle(SWT.CLOSE | SWT.MAX | SWT.TITLE | SWT.BORDER
					| SWT.MODELESS | SWT.RESIZE | getDefaultOrientation());
		}
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {

		final ExtendedMarkersView view = getView(event);
		if (view == null)
			return this;

		final Map resolutions = new HashMap();

		IRunnableWithProgress resolutionsRunnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				monitor.beginTask(
						MarkerMessages.resolveMarkerAction_computationAction,
						100);

				IMarker[] selected = view.getSelectedMarkers();
				for (int i = 0; i < selected.length; i++) {
					monitor.worked(100 / selected.length);
					IMarkerResolution[] found = IDE.getMarkerHelpRegistry()
							.getResolutions(selected[i]);
					if (found.length > 0)
						resolutions.put(selected[i], found);
				}
				monitor.done();
			}
		};

		Object service = view.getSite().getAdapter(
				IWorkbenchSiteProgressService.class);

		IRunnableContext context = new ProgressMonitorDialog(view.getSite()
				.getShell());

		try {
			if (service == null) {
				PlatformUI.getWorkbench().getProgressService().runInUI(context,
						resolutionsRunnable, null);
			} else {
				((IWorkbenchSiteProgressService) service).runInUI(context,
						resolutionsRunnable, null);
			}
		} catch (InvocationTargetException exception) {
			throw new ExecutionException(exception.getLocalizedMessage(),
					exception);
		} catch (InterruptedException exception) {

			throw new ExecutionException(exception.getLocalizedMessage(),
					exception);
		}

		if (resolutions.isEmpty()) {
			Status newStatus = new Status(IStatus.WARNING,
					IDEWorkbenchPlugin.IDE_WORKBENCH,
					MarkerMessages.MarkerResolutionDialog_NoResolutionsFound);
			StatusAdapter adapter = new StatusAdapter(newStatus);
			adapter.setProperty(StatusAdapter.TITLE_PROPERTY,
					MarkerMessages.MarkerResolutionDialog_CannotFixTitle);
			StatusManager.getManager().handle(adapter, StatusManager.SHOW);
		} else {
			IWizard wizard = new QuickFixWizard(resolutions);
			WizardDialog dialog = new QuickFixWizardDialog(view.getSite().getShell(),
					wizard);
			dialog.addPageChangedListener(new IPageChangedListener() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.jface.dialogs.IPageChangedListener#pageChanged(org.eclipse.jface.dialogs.PageChangedEvent)
				 */
				public void pageChanged(PageChangedEvent event) {
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					if(window == null)
						return;
					IWorkbenchPage page = window.getActivePage();
					if(page == null)
						return;
					QuickFixPage quickPage = (QuickFixPage) event.getSelectedPage();
					ExtendedMarkersView.openMarkerInEditor(quickPage.getMarker(),
							view.getSite().getPage());

				}
			});
			dialog.open();
		}
		return this;
	}
}
