package org.eclipse.team.internal.ui.target;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class MappingSelectionPage extends TargetWizardPage {
	private IPath path;
	
	public MappingSelectionPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 2);
		
		Label description = new Label(composite, SWT.WRAP);
		GridData data = new GridData();
		data.horizontalSpan = 2;
		data.widthHint = 350;
		description.setLayoutData(data);
		description.setText("Enter the path:");

		final Text pathText = createTextField(composite);
		pathText.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event e) {
				path = new Path(pathText.getText());
			}
		});

		setControl(composite);
		setPageComplete(true);
	}

	public IPath getMapping() {
		return path;
	}
}