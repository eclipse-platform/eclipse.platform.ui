package org.eclipse.update.internal.ui.forms;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.SiteManager;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.pages.UpdateFormPage;
import org.eclipse.update.internal.ui.views.ConfigurationView;
import org.eclipse.update.ui.forms.internal.*;
import org.eclipse.update.ui.forms.internal.engine.*;

public class PreserveSection extends UpdateSection {
	// NL keys
	private static final String KEY_TITLE = "InstallConfigurationPage.PreserveSection.title"; //$NON-NLS-1$
	private static final String KEY_DESC = "InstallConfigurationPage.PreserveSection.desc"; //$NON-NLS-1$
	private static final String KEY_TEXT = "InstallConfigurationPage.PreserveSection.text"; //$NON-NLS-1$
	private static final String KEY_PRESERVE_TEXT = "InstallConfigurationPage.PreserveSection.preserveText"; //$NON-NLS-1$
	private static final String KEY_PRESERVE_BUTTON = "InstallConfigurationPage.PreserveSection.preserveButton"; //$NON-NLS-1$

	private Composite container;
	private FormWidgetFactory factory;
	private IInstallConfiguration config;
	private FormEngine textLabel;
	private Label preserveLabel;
	private Button preserveButton;

	public PreserveSection(UpdateFormPage page) {
		super(page);
		setAddSeparator(false);
		setHeaderText(UpdateUIPlugin.getResourceString(KEY_TITLE));
		UpdateUIPlugin.getDefault().getLabelProvider().connect(this);
	}
	
	public void dispose() {
		UpdateUIPlugin.getDefault().getLabelProvider().disconnect(this);
		super.dispose();
	}

	public Composite createClient(
		Composite parent,
		FormWidgetFactory factory) {
		HTMLTableLayout layout = new HTMLTableLayout();
		this.factory = factory;
		updateHeaderColor();
		layout.leftMargin = layout.rightMargin = 0;
		layout.topMargin = 0;
		container = factory.createComposite(parent);
		container.setLayout(layout);
		layout.numColumns = 2;
		textLabel = factory.createFormEngine(container); //$NON-NLS-1$
		TableData td = new TableData();
		td.align = TableData.FILL;
		td.grabHorizontal = true;
		td.colspan = 2;
		textLabel.setLayoutData(td);
		textLabel.load(UpdateUIPlugin.getResourceString(KEY_TEXT), true, false);
		Image configsImage = UpdateUIPlugin.getDefault().getLabelProvider().get(UpdateUIPluginImages.DESC_CONFIGS_VIEW);
		HyperlinkAction action = new HyperlinkAction() {
			public void linkActivated(IHyperlinkSegment link) {
				ConfigurationView view =
					(ConfigurationView) showView(UpdatePerspective
						.ID_CONFIGURATION);
				if (view != null)
					view.expandPreservedConfigurations();
			}
		};
		textLabel.registerTextObject("link", action);
		textLabel.registerTextObject("image", configsImage);
		preserveLabel = factory.createLabel(container, UpdateUIPlugin.getResourceString(KEY_PRESERVE_TEXT));
		td = new TableData();
		td.valign = TableData.MIDDLE;
				
		preserveButton = factory.createButton(container, UpdateUIPlugin.getResourceString(KEY_PRESERVE_BUTTON), SWT.PUSH); //$NON-NLS-1$
		preserveButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				performPreserve();
			}
		});
		preserveButton.setEnabled(false);
		return container;
	}
	
	public void configurationChanged(IInstallConfiguration config) {
		this.config = config;
		preserveButton.setEnabled(config!=null);
	}

	private void performPreserve() {
		try {
			ILocalSite localSite = SiteManager.getLocalSite();
			IInstallConfiguration target = config;
			localSite.addToPreservedConfigurations(config);
			localSite.save();
		} catch (CoreException e) {
			UpdateUIPlugin.logException(e);
		}
	}
	
	private IViewPart showView(String viewId) {
		try {
			IViewPart part = UpdateUIPlugin.getActivePage().showView(viewId);
			return part;
		} catch (PartInitException e) {
			return null;
		}
	}

	public Control getFocusControl() {
		return preserveButton;
	}
}