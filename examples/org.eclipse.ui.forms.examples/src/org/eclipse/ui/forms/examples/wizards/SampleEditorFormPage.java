package org.eclipse.ui.forms.examples.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.forms.*;
import org.eclipse.ui.internal.forms.WizardFormEditor;

public class SampleEditorFormPage extends WizardPage {
	private Label dirLabel;
	private Text projectText;
	private Text dirText;
	private Button dirButton;
	private Button defButton;
	public SampleEditorFormPage() {
		super("sample");
		setTitle("Plug-in Project");
		setDescription("Create a new plug-in project");
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.forms.FormWizardPage#createFormContents(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		FormToolkit toolkit = ((IFormToolkitProvider)getContainer()).getToolkit();
		Composite container = toolkit.createComposite(parent);
		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 2;
		container.setLayout(layout);
		toolkit.createLabel(container, "Project name");
		TableWrapData td;
		projectText = toolkit.createText(container, "");
		td = new TableWrapData(TableWrapData.FILL_GRAB);
		projectText.setLayoutData(td);
		
		addSpacer(container, toolkit, 2);
	
		Section section = toolkit.createSection(container, Section.DESCRIPTION|Section.TITLE_BAR);
		section.setText("Project contents");
		section.setDescription("Specify the location of the project in the file system");
		Composite client = toolkit.createComposite(section);
		section.setClient(client);
		
		GridLayout glayout = new GridLayout();
		client.setLayout(glayout);
		glayout.numColumns = 3;
		defButton = toolkit.createButton(client, "Use default", SWT.CHECK);
		defButton.setSelection(true);
		GridData gd = new GridData();
		gd.horizontalSpan = 3;
		defButton.setLayoutData(gd);
		
		dirLabel = toolkit.createLabel(client, "Directory:");
		dirText = toolkit.createText(client, "");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 200;
		dirText.setLayoutData(gd);
		dirButton = toolkit.createButton(client, "Browse...", SWT.PUSH);
		
		td = new TableWrapData(TableWrapData.FILL);
		td.colspan = 2;
		section.setLayoutData(td);
		defButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				boolean value = !defButton.getSelection();
				setContentsEnabled(value);
			}
		});
		setContentsEnabled(false);
		
		section = toolkit.createSection(container, Section.DESCRIPTION|Section.TITLE_BAR);
		section.setText("Project Settings");
		section.setDescription("Specify if the project will contain Java code and if so, define source and output folders.");
		client = toolkit.createComposite(section);
		section.setClient(client);
		td = new TableWrapData(TableWrapData.FILL);
		td.colspan = 2;
		section.setLayoutData(td);
		
		glayout = new GridLayout();
		glayout.numColumns = 2;
		client.setLayout(glayout);
		Button jb = toolkit.createButton(client, "Create a Java project", SWT.CHECK);
		jb.setSelection(true);
		gd = new GridData();
		gd.horizontalSpan = 2;
		jb.setLayoutData(gd);
		Label l = toolkit.createLabel(client, "Source Folder Name:");
		gd = new GridData();
		gd.horizontalIndent = 10;
		l.setLayoutData(gd);
		Text t = toolkit.createText(client, "src");
		t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		l = toolkit.createLabel(client, "Output Folder Name:");
		gd = new GridData();
		gd.horizontalIndent = 10;
		l.setLayoutData(gd);
		t = toolkit.createText(client, "bin");
		t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		
		section = toolkit.createSection(container, Section.DESCRIPTION|Section.TITLE_BAR|Section.TWISTIE);
		section.setText("Alternate Format (For Advanced Users Only)");
		section.setDescription("Since 3.0, all plug-ins are implemented as OSGi bundles. Bundle manifest is automatically created for them. Optionally, users familiar with OSGi can create the manifest in order to have full control over it.");
		client = toolkit.createComposite(section);
		section.setClient(client);
		td = new TableWrapData(TableWrapData.FILL);
		td.colspan = 2;
		section.setLayoutData(td);
		
		glayout = new GridLayout();
		client.setLayout(glayout);
		Button mb = toolkit.createButton(client, "Create an OSGi bundle manifest for the plug-in", SWT.CHECK);
		l = toolkit.createLabel(client, "Note: this format is not supported by older Eclipse platforms (prior to 3.0)");
		gd = new GridData();
		gd.horizontalIndent = 10;
		l.setLayoutData(gd);

		setControl(container);
		WorkbenchHelp.setHelp(container,"org.eclipse.pde.doc.user."+
				"new_project_structure_page");		
	}
	
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		projectText.setFocus();
	}
	private void addSpacer(Composite parent, FormToolkit toolkit, int span) {
		Label label = toolkit.createLabel(parent, null);
		TableWrapData td = new TableWrapData();
		td.colspan = span;
		label.setLayoutData(td);
	}
	
	private void setContentsEnabled(boolean value) {
		dirLabel.setEnabled(value);
		dirText.setEnabled(value);
		dirButton.setEnabled(value);
	}
}