/*
 * Created on Jun 19, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.eclipse.welcome.internal.portal;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.update.ui.forms.internal.*;
import org.eclipse.update.ui.forms.internal.WebForm;
import org.eclipse.welcome.internal.WelcomePortalImages;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class WelcomeForm extends WebForm {
	
	class TabListener implements IHyperlinkListener {
		public void linkActivated(Control linkLabel) {
		}

		public void linkEntered(Control linkLabel) {
		}

		public void linkExited(Control linkLabel) {
		}
	}
	
	private TabListener tabListener = new TabListener();
	private HyperlinkHandler tabHandler = new HyperlinkHandler();
	
	public WelcomeForm() {
		setHeadingImage(WelcomePortalImages.get(WelcomePortalImages.IMG_FORM_BANNER_SHORT));
		setHeadingUnderlineImage(
		WelcomePortalImages.get(WelcomePortalImages.IMG_FORM_UNDERLINE));
	}
	
	public Control createControl(Composite parent) {
		return super.createControl(parent);
	}

	protected void createContents(final Composite parent) {
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.numColumns = 2;
		parent.setLayout(layout);
		parent.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_GREEN));
		createTabs(parent);
	}
	
	private void createTabs(Composite parent) {
		FormWidgetFactory factory = getFactory();
		Composite tabContainer = factory.createComposite(parent);
		tabContainer.setBackground(factory.getColor(FormWidgetFactory.COLOR_COMPOSITE_SEPARATOR));
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		tabContainer.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_VERTICAL);
		tabContainer.setLayoutData(gd);
		createTab(tabContainer, "Home", factory);
		createTab(tabContainer, "News", factory);
		createTab(tabContainer, "Samples", factory);
		createTab(tabContainer, "Community", factory);
	}
	
	private void createTab(Composite parent, String name, FormWidgetFactory factory) {
		tabHandler.setBackground(factory.getColor(FormWidgetFactory.COLOR_COMPOSITE_SEPARATOR));
		tabHandler.setForeground(factory.getBackgroundColor());
		SelectableFormLabel tab = factory.createSelectableLabel(parent, name);
		GridData gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		tabHandler.registerHyperlink(tab, tabListener);
		tab.setFont(JFaceResources.getBannerFont());
	}

	public void initialize(Object model) {
		super.initialize(model);
		setHeadingText("Welcome");
		((Composite) getControl()).layout(true);
		updateSize();
	}
}
