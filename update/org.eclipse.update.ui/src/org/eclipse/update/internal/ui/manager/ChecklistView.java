package org.eclipse.update.internal.ui.manager;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.SWT;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;
import org.eclipse.update.ui.forms.*;
import org.eclipse.swt.layout.*;
import org.eclipse.update.ui.internal.model.*;
import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.update.core.*;
import org.eclipse.ui.*;
import org.eclipse.update.internal.ui.wizards.*;
import org.eclipse.swt.custom.*;
import org.eclipse.jface.wizard.*;
import java.util.*;

/**
 * Insert the type's description here.
 * @see ViewPart
 */
public class ChecklistView extends BaseTreeView 
				implements IUpdateModelChangedListener {
	private FormWidgetFactory factory;
	private Label totalSizeLabel;
	private Button startButton;
	private FolderObject installs = new FolderObject("Install Candidates");
	private FolderObject uninstalls = new FolderObject("Uninstall Candidates");
	private Image featureImage;
	private Image folderImage;
	private Action deleteAction;
	
class FolderObject {
	String label;
	
	public FolderObject(String label) {
		this.label = label;
	}
	
	public String getLabel() {
		return label;
	}
	public String toString() {
		return getLabel();
	}
}

	
class ChecklistProvider extends DefaultContentProvider 
						implements ITreeContentProvider {
	/**
	 * @see ITreeContentProvider#getChildren(Object)
	 */
	public Object[] getChildren(Object parent) {
		if (parent instanceof UpdateModel) {
			return new Object [] { installs, uninstalls };
		}
		if (parent == installs) {
			return getCandidates(ChecklistJob.INSTALL);
		}
		if (parent == uninstalls) {
			return getCandidates(ChecklistJob.UNINSTALL);
		}
		return new Object[0];
	}

	/**
	 * @see ITreeContentProvider#getParent(Object)
	 */
	public Object getParent(Object child) {
		return null;
	}

	/**
	 * @see ITreeContentProvider#hasChildren(Object)
	 */
	public boolean hasChildren(Object parent) {
		return getChildren(parent).length>0;
	}

	/**
	 * @see IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements(Object input) {
		return getChildren(input);
	}
}

class ChecklistLabelProvider extends LabelProvider {
	public Image getImage(Object obj) {
		if (obj.equals(installs))
		   return folderImage;
		if (obj.equals(uninstalls))
		   return folderImage;
		if (obj instanceof ChecklistJob)
		   return featureImage;
		return null;
	}
}

/**
 * The constructor.
 */
public ChecklistView() {
	UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
	model.addUpdateModelChangedListener(this);
	//eclipseImage = UpdateUIPluginImages.DESC_ECLIPSE_OBJ.createImage();
	//uninstallsImage = UpdateUIPluginImages.DESC_UNINSTALL_OBJ.createImage();
	folderImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
	featureImage = UpdateUIPluginImages.DESC_FEATURE_OBJ.createImage();
}



private Object [] getCandidates(int mode) {
	UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
	return model.getJobs(mode);
}

public void initProviders() {
	viewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
	viewer.setContentProvider(new ChecklistProvider());
	viewer.setLabelProvider(new ChecklistLabelProvider());
	viewer.setInput(UpdateUIPlugin.getDefault().getUpdateModel());
}

public void createPartControl(Composite parent)  {
	factory = new FormWidgetFactory(parent.getDisplay());
	Composite container = factory.createComposite(parent);
	GridLayout layout = new GridLayout();
	layout.marginHeight = 0;
	layout.marginWidth = 0;
	GridData gd;
	container.setLayout(layout);

	Composite statusContainer = factory.createComposite(container);
	gd = new GridData(GridData.FILL_HORIZONTAL);
	statusContainer.setLayoutData(gd);
	layout = new GridLayout();	
	layout.numColumns = 2;
	statusContainer.setLayout(layout);
	totalSizeLabel = factory.createLabel(statusContainer, null);
	gd = new GridData();
	totalSizeLabel.setLayoutData(gd);
	startButton = factory.createButton(statusContainer, "Start...", SWT.PUSH);
	startButton.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			performStart();
		}
	});
	gd = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_BEGINNING);
	startButton.setLayoutData(gd);
	updateStartButton();
	Control sep = factory.createCompositeSeparator(statusContainer);
	gd = new GridData(GridData.FILL_HORIZONTAL);
	gd.horizontalSpan = 2;
	gd.heightHint = 2;
	sep.setLayoutData(gd);
	super.createPartControl(container);
	gd = new GridData(GridData.FILL_BOTH);
	viewer.getTree().setLayoutData(gd);
	updateTotalSize(0);
}

private void updateTotalSize(int size) {
	totalSizeLabel.setText("Total Size: "+size+"KB");
}

public void dispose() {
	UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
	model.removeUpdateModelChangedListener(this);
	featureImage.dispose();
	super.dispose();
}

public void objectAdded(Object parent, Object child) {
	if (child instanceof ChecklistJob) {
		ChecklistJob job = (ChecklistJob)child;
		parent = installs;
		if (job.getJobType()==ChecklistJob.UNINSTALL)
		   parent = uninstalls;
		viewer.add(parent, child);
		viewer.expandToLevel(child, 1);
		updateStartButton();
	}
}

public void objectRemoved(Object parent, Object child) {
	if (child instanceof ChecklistJob) {
		ChecklistJob job = (ChecklistJob)child;
		parent = installs;
		if (job.getJobType()==ChecklistJob.UNINSTALL)
		   parent = uninstalls;
		viewer.remove(child);
		updateStartButton();
	}
}
public void objectChanged(Object object, String property) {
	if (object instanceof ChecklistJob) {
		viewer.update(object, new String [] {});
	}
}

private void updateStartButton() {
	UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
	startButton.setEnabled(model.getJobs().length>0);
}

private void performStart() {
/*
	BusyIndicator.showWhile(startButton.getDisplay(), new Runnable() {
		public void run() {
			InstallWizard wizard = new InstallWizard();
			WizardDialog dialog = new WizardDialog(UpdateUIPlugin.getActiveWorkbenchShell(), wizard);
			dialog.create();
			dialog.getShell().setSize(500, 500);
			dialog.open();
		}
	});
*/
}

protected void makeActions() {
	deleteAction = new Action() {
		public void run() {
			performDelete();
		}
	};
	deleteAction.setText("Remove");
}

protected void fillContextMenu(IMenuManager manager) {
	manager.add(deleteAction);
}

private void performDelete() {
	ISelection selection = viewer.getSelection();
	if (selection.isEmpty()==false && selection instanceof IStructuredSelection) {
		IStructuredSelection ssel = (IStructuredSelection)selection;
		UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
		for (Iterator iter = ssel.iterator(); iter.hasNext();) {
			Object obj = iter.next();
			if (obj instanceof ChecklistJob)
				model.removeJob((ChecklistJob)obj);
		}
	}
}

}