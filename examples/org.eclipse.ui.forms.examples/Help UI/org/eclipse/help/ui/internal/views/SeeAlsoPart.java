/*
 * Created on Dec 13, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.help.ui.internal.views;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.*;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.examples.internal.ExamplesPlugin;
import org.eclipse.ui.forms.widgets.*;

/**
 * @author dejan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SeeAlsoPart extends AbstractFormPart implements IHelpPart {
	private Composite container;
	private ReusableHelpPart parent;
	private FormText text;
	private String id;
	/**
	 * @param parent
	 * @param toolkit
	 * @param style
	 */
	public SeeAlsoPart(Composite parent, FormToolkit toolkit) {
		container = toolkit.createComposite(parent);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		container.setLayout(layout);
		Composite sep = toolkit.createCompositeSeparator(container);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.heightHint = 1;
		sep.setLayoutData(gd);
	
		text = toolkit.createFormText(container, true);
		text.setLayoutData(new GridData(GridData.FILL_BOTH));
		text.marginWidth = 5;
		text.setColor(FormColors.TITLE, toolkit.getColors().getColor(
				FormColors.TITLE));		
		text.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				SeeAlsoPart.this.parent.showPage((String)e.getHref());
			}
		});
		hookImage(ExamplesPlugin.IMG_HELP_SEARCH); 
		hookImage(ExamplesPlugin.IMG_HELP_TOC_OPEN); 
		hookImage(ExamplesPlugin.IMG_HELP_CONTAINER); 
		loadText();
	}
	private void hookImage(String key) {
		text.setImage(key, ExamplesPlugin.getDefault().getImage(key));
	}
	private void loadText() {
		StringBuffer buf = new StringBuffer();
		buf.append("<form>");
		buf.append("<p><span color=\"");
		buf.append(FormColors.TITLE);
		buf.append("\">Go To:</span></p>");
		addPageLink(buf, "All Topics", IHelpViewConstants.ALL_TOPICS_PAGE, ExamplesPlugin.IMG_HELP_TOC_OPEN);
		addPageLink(buf, "Search", IHelpViewConstants.SEARCH_PAGE, ExamplesPlugin.IMG_HELP_SEARCH);
		addPageLink(buf, "Context Help", IHelpViewConstants.CONTEXT_HELP_PAGE, ExamplesPlugin.IMG_HELP_CONTAINER);		
		buf.append("</form>");
		text.setText(buf.toString(), true, false);
	}
	
	private void addPageLink(StringBuffer buf, String text, String id, String imgRef) {
		buf.append("<li indent=\"25\" bindent=\"5\" style=\"image\" value=\"");
		buf.append(imgRef);
		buf.append("\">");
		buf.append("<a href=\"");
		buf.append(id);
		buf.append("\">");
		buf.append(text);
		buf.append("</a>");
		buf.append("</li>");		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#getControl()
	 */
	public Control getControl() {
		return container;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#init(org.eclipse.help.ui.internal.views.NewReusableHelpPart)
	 */
	public void init(ReusableHelpPart parent, String id) {
		this.parent = parent;
		this.id = id;
	}
	public String getId() {
		return id;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		container.setVisible(visible);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public boolean fillContextMenu(IMenuManager manager) {
		// TODO Auto-generated method stub
		return false;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#hasFocusControl(org.eclipse.swt.widgets.Control)
	 */
	public boolean hasFocusControl(Control control) {
		return text.equals(control);
	}
}