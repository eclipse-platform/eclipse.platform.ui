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
public class WelcomePortalForm extends WebForm {
	private WelcomePortalPart portal;
	private IConfigurationElement pageConfig;

	public WelcomePortalForm(WelcomePortalPart portal, IConfigurationElement pageConfig) {
		this.portal = portal;
		this.pageConfig = pageConfig;
		setHeadingVisible(false);
	}
	
	public void initialize(Object model) {
		update();
	}
	
	public WelcomePortalPart getPortal() {
		return portal;
	}
	
	protected void createContents(Composite parent) {
		HTMLTableLayout layout = new HTMLTableLayout();
		parent.setLayout(layout);
		layout.numColumns = getInteger(pageConfig, "numColumns", 1);
		layout.topMargin = 0;
		layout.leftMargin = layout.rightMargin = 10;
		layout.horizontalSpacing=15;
		layout.verticalSpacing = 5;
		//parent.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_GREEN));
		layout.makeColumnsEqualWidth=true;
		SelectableFormLabel customize = getFactory().createSelectableLabel(parent, "Customize...");
		getFactory().turnIntoHyperlink(customize, new HyperlinkAdapter() {
			public void linkActivated(Control link) {
			}
		});
		TableData td = new TableData();
		td.align = TableData.CENTER;
		td.colspan = layout.numColumns;
		customize.setLayoutData(td);
		
		IConfigurationElement [] sectionRefs = pageConfig.getChildren("section");
		for (int i=0; i<sectionRefs.length; i++) {
			IConfigurationElement sectionRef = sectionRefs[i];
			String id = sectionRef.getAttribute("id");
			SectionDescriptor desc = portal.findSection(id);
			if (desc==null) continue;
			PortalSection section = new PortalSection(desc, this);
			Control control = section.createControl(parent, getFactory());
			int span = getInteger(sectionRef, "span", 1);
			td = new TableData(TableData.FILL, TableData.TOP);
			td.colspan = span;
			td.grabHorizontal = true;
			control.setLayoutData(td);
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
