package org.eclipse.help.ui.internal.views;

import org.eclipse.help.ui.internal.HelpUIResources;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class EngineTypeWizardPage extends WizardPage {
	private TableViewer tableViewer;
	private EngineTypeDescriptor [] engineTypes;
	private EngineTypeDescriptor selection;
	
	class EngineContentProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			return engineTypes;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	class EngineLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getText(Object obj) {
			EngineTypeDescriptor desc = (EngineTypeDescriptor)obj;
			return desc.getLabel();
		}
		public Image getImage(Object obj) {
			EngineTypeDescriptor desc = (EngineTypeDescriptor)obj;
			return desc.getIconImage();
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return getImage(element);
		}
		public String getColumnText(Object element, int columnIndex) {
			return getText(element);
		}
	}

	public EngineTypeWizardPage(EngineTypeDescriptor[] engineTypes) {
		super("engineType"); //$NON-NLS-1$
		setTitle(HelpUIResources.getString("EngineTypeWizardPage.title")); //$NON-NLS-1$
		setDescription(HelpUIResources.getString("EngineTypeWizardPage.desc")); //$NON-NLS-1$
		this.engineTypes = engineTypes;
	}

	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		Label label = new Label(container, SWT.NULL);
		label.setText(HelpUIResources.getString("EngineTypeWizardPage.label")); //$NON-NLS-1$
		tableViewer = new TableViewer(container);
		tableViewer.setContentProvider(new EngineContentProvider());
		tableViewer.setLabelProvider(new EngineLabelProvider());
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				setPageComplete(!event.getSelection().isEmpty());
				selection = (EngineTypeDescriptor)((IStructuredSelection)event.getSelection()).getFirstElement();
			}
		});
		tableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		tableViewer.setInput(engineTypes);
		setControl(container);
		setPageComplete(false);
	}
	public EngineTypeDescriptor getSelectedEngineType() {
		return selection;
	}
}
