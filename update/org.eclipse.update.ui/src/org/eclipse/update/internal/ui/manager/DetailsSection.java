package org.eclipse.update.internal.ui.manager;
import org.eclipse.swt.widgets.*;
import java.net.*;
import org.eclipse.jface.util.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.SWT;
import org.eclipse.jface.resource.*;
import org.eclipse.update.ui.forms.*;
import org.eclipse.swt.layout.*;
import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.core.*;

public class DetailsSection extends UpdateSection {
	private String name;
	private Label nameLabel;
	private String provider;
	private Label providerLabel;
	private String size;
	private Label sizeLabel;
	private String description;
	private Label descriptionLabel;
	private Composite control;
	private Label infoLinkLabel;
	private String infoLink;
	private URL infoLinkURL;
	private int mode;
	
public DetailsSection(UpdateFormPage page, int mode) {
	super(page);
	setHeaderText("Details");
	this.mode = mode;
}

public final Composite createClient(
	Composite parent,
	FormWidgetFactory factory) {
	Composite container = factory.createComposite(parent);
	GridLayout layout = new GridLayout();
	container.setLayout(layout);

	nameLabel = factory.createHeadingLabel(container, "");
	GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
	nameLabel.setLayoutData(gd);
	providerLabel = factory.createLabel(container, "");
	gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
	providerLabel.setLayoutData(gd);
	sizeLabel = factory.createLabel(container, "");
	gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
	sizeLabel.setLayoutData(gd);
	descriptionLabel = factory.createLabel(container, "", SWT.WRAP);
	gd = new GridData(GridData.FILL_BOTH);
	descriptionLabel.setLayoutData(gd); 
   	infoLinkLabel = factory.createHyperlinkLabel(container,
   						infoLink, new IHyperlinkListener() {
	   		public void linkActivated(Control linkLabel) {
	   			openInfoURL();
	   		}
			public void linkEntered(Control linkLabel) {
			}
			public void linkExited(Control linkLabel) {
			}
	   });
	return container;
}

public void initialize(Object model) {
	inputChanged(null);
}

private void inputChanged(IFeature feature) {
	if (feature==null) {
		return;
	}
	nameLabel.setText(feature.getLabel());
	providerLabel.setText(feature.getProvider());
	sizeLabel.setText("0Kb");
	descriptionLabel.setText(feature.getDescription());
	infoLinkURL = feature.getInfoURL();
	infoLinkLabel.setVisible(infoLinkURL!=null);
}

public void sectionChanged(FormSection source, int type, Object object) {
	if (type == FormSection.SELECTION)
	   inputChanged((IFeature)object);
}

private void openInfoURL() {
}

}