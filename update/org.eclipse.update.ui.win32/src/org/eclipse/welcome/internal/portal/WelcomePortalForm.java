/*
 * Created on Jun 19, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.eclipse.welcome.internal.portal;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.ui.forms.internal.*;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class WelcomePortalForm extends ScrollableSectionForm {
	private WelcomePortalPart portal;
	private IConfigurationElement pageConfig;

	public WelcomePortalForm(WelcomePortalPart portal, IConfigurationElement pageConfig) {
		this.portal = portal;
		this.pageConfig = pageConfig;
		//setVerticalFit(true);
		setHeadingVisible(false);
	}
	
	public void initialize(Object model) {
		update();
	}
	
	public WelcomePortalPart getPortal() {
		return portal;
	}
	
	protected Composite createParent(Composite root) {
		Composite parent =  super.createParent(root);
		((ScrolledComposite)parent).setExpandHorizontal(true);
		return parent;
	}
	
	protected void createFormClient(Composite parent) {
		GridLayout layout = new GridLayout();
		parent.setLayout(layout);
		layout.numColumns = getInteger(pageConfig, "numColumns", 1);
		layout.marginWidth = 10;
		layout.horizontalSpacing=15;
		layout.makeColumnsEqualWidth=true;
		SelectableFormLabel customize = getFactory().createSelectableLabel(parent, "Customize...");
		getFactory().turnIntoHyperlink(customize, new HyperlinkAdapter() {
			public void linkActivated(Control link) {
			}
		});
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gd.horizontalSpan = layout.numColumns;
		customize.setLayoutData(gd);
		
		IConfigurationElement [] sectionRefs = pageConfig.getChildren("section");
		for (int i=0; i<sectionRefs.length; i++) {
			IConfigurationElement sectionRef = sectionRefs[i];
			String id = sectionRef.getAttribute("id");
			SectionDescriptor desc = portal.findSection(id);
			if (desc==null) continue;
			PortalSection section = new PortalSection(desc, this);
			Control control = section.createControl(parent, getFactory());
			int span = getInteger(sectionRef, "span", 1);
			gd = new GridData(GridData.FILL_HORIZONTAL|GridData.VERTICAL_ALIGN_BEGINNING);
			gd.horizontalSpan = span;
			control.setLayoutData(gd);
			registerSection(section);
		}
	}

	private int getInteger(IConfigurationElement config, String attName, int def) {
		String value = config.getAttribute(attName);
		if (value!=null) {
			try {
				return Integer.parseInt(value);
			}
			catch (NumberFormatException e) {
			}
		}
		return def;
	}
}
