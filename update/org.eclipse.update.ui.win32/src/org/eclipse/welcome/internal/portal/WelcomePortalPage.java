/*
 * Created on Jun 19, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.eclipse.welcome.internal.portal;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.ui.forms.internal.*;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class WelcomePortalPage implements IFormPage {
	private Control control;
	private String name;
	private WelcomePortalPart portal;
	private WelcomePortalForm form;
	private IConfigurationElement config;
	private SelectableFormLabel tab;
	
	public WelcomePortalPage(WelcomePortalPart portal, IConfigurationElement config) {
		this.portal = portal;
		this.form = new WelcomePortalForm(portal, config);
		this.name = config.getAttribute("name");
		this.config = config;
	}
	
	public void setTab(SelectableFormLabel tab) {
		this.tab = tab;
	}
	
	public SelectableFormLabel getTab() {
		return tab;
	}

	public boolean becomesInvisible(IFormPage newPage) {
		return true;
	}

	public void becomesVisible(IFormPage previousPage) {
	}

	public void createControl(Composite parent) {
		this.control = form.createControl(parent);
		form.initialize(null);
	}

	public Control getControl() {
		return control;
	}

	public String getLabel() {
		return name;
	}

	public String getTitle() {
		return name;
	}

	public boolean isSource() {
		return false;
	}

	public boolean isVisible() {
		return portal.isVisible(this);
	}
}
