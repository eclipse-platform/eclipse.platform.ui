package org.eclipse.team.internal.ui.target;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.target.Site;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class MappingSelectionPage extends TargetWizardPage {
	private IPath path;
	private Site site;
	private TreeViewer viewer;
	Text textPath;
	
	public MappingSelectionPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
		setDescription(Policy.bind("MappingSelectionPage.description")); //$NON-NLS-1$
	}

	public void setSite(Site site) {
		this.site = site;
	}
	
	public void createControl(Composite p) {
		Composite composite = createComposite(p, 1);
		viewer = new TreeViewer(composite, SWT.BORDER | SWT.MULTI);
		
		GridData data = new GridData (GridData.FILL_BOTH);
		viewer.getTree().setLayoutData(data);

		
		viewer.setContentProvider(new SiteLazyContentProvider());
		viewer.setLabelProvider(new WorkbenchLabelProvider());
		viewer.setSorter(new SiteViewSorter());
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateTextPath();
			}
		});
				
		textPath = createTextField(composite);
		
		setControl(composite);
		setPageComplete(true);
	}

	/**
	 * Method updateTextPath.
	 */
	private void updateTextPath() {
		IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
		if (!selection.isEmpty()) {
			final List filesSelection = new ArrayList();
			Iterator it = selection.iterator();
			while(it.hasNext()) {
				Object o = it.next();
				if(o instanceof RemoteResourceElement) {
					try {
						textPath.setText(((RemoteResourceElement)o).getRemoteResource().getURL().toExternalForm());
					} catch (TeamException e) {
					}
					return;
				}
			}
		}
	}

	public IPath getMapping() {
		return new Path(textPath.getText());
	}
	/**
	 * @see IWizardPage#setPreviousPage(IWizardPage)
	 */
	public void setPreviousPage(IWizardPage page) {
		try {
			if(viewer!=null) {
				viewer.setInput(new RemoteResourceElement(site.getRemoteResource(), false));
			}
		} catch (TeamException e) {
		}
	}
}