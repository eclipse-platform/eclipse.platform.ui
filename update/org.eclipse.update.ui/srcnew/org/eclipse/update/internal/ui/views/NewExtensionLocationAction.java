package org.eclipse.update.internal.ui.views;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.model.ExtensionRoot;
import org.eclipse.update.internal.ui.model.MyComputer;
import org.eclipse.update.internal.ui.model.MyComputerFile;
import org.eclipse.update.internal.ui.parts.MyComputerContentProvider;
import org.eclipse.update.internal.ui.parts.MyComputerLabelProvider;


public class NewExtensionLocationAction extends Action {

	public NewExtensionLocationAction(String text, ImageDescriptor desc) {
		super(text, desc);
	}
	
	public void run() {
		ElementTreeSelectionDialog dialog =
			new ElementTreeSelectionDialog(
				UpdateUI.getActiveWorkbenchShell(),
				new MyComputerLabelProvider(),
				new MyComputerContentProvider());
				
		dialog.setInput(new MyComputer());
		dialog.setAllowMultiple(false);
		dialog.addFilter(new ViewerFilter() {
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				return !(element instanceof MyComputerFile);
			}
		});
		dialog.setValidator(new ISelectionStatusValidator() {
			public IStatus validate(Object[] selection) {
				if (selection.length == 1 && selection[0] instanceof ExtensionRoot)
					return new Status(
						IStatus.OK,
						UpdateUI.getPluginId(),
						IStatus.OK,
						"",
						null);
				return new Status(
					IStatus.ERROR,
					UpdateUI.getPluginId(),
					IStatus.ERROR,
					"",
					null);
			}
		});
		dialog.setTitle("Extension Location");
		dialog.setMessage("&Select an extension location:");
		if (dialog.open() == ElementTreeSelectionDialog.OK) {
			addExtensionLocation((ExtensionRoot)dialog.getFirstResult());
		}
		
	}
	
	private void addExtensionLocation(ExtensionRoot root) {
		//TODO Dejan to implement this method
	}

}
