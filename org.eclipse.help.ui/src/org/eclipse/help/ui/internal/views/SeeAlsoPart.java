/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.views;

import org.eclipse.help.ui.internal.*;
import org.eclipse.help.ui.internal.HelpUIResources;
import org.eclipse.jface.action.*;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.*;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.forms.events.HyperlinkAdapter;

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
		text.setWhitespaceNormalized(false);
		text.setLayoutData(new GridData(GridData.FILL_BOTH));
		text.marginWidth = 5;
		text.setColor(FormColors.TITLE, toolkit.getColors().getColor(
				FormColors.TITLE));
		text.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(final HyperlinkEvent e) {
				container.getDisplay().asyncExec(new Runnable() {
					public void run() {
						SeeAlsoPart.this.parent.showPage((String) e.getHref(), true);
					}
				});
			}
		});
	}

	private void hookImage(String key) {
		text.setImage(key, HelpUIResources.getImage(key));
	}

	private void loadText() {
		StringBuffer buf = new StringBuffer();
		buf.append("<form>"); //$NON-NLS-1$
		buf.append("<p><span color=\""); //$NON-NLS-1$
		buf.append(FormColors.TITLE);
		buf.append("\">"); //$NON-NLS-1$
		buf.append(HelpUIResources.getString("SeeAlsoPart.goto")); //$NON-NLS-1$
		buf.append("</span></p>"); //$NON-NLS-1$
		buf.append("<p>"); //$NON-NLS-1$
		if ((parent.getStyle() & ReusableHelpPart.ALL_TOPICS) != 0)
			addPageLink(buf, HelpUIResources.getString("SeeAlsoPart.allTopics"), IHelpUIConstants.HV_ALL_TOPICS_PAGE, //$NON-NLS-1$
				IHelpUIConstants.IMAGE_TOC_OPEN);
		if ((parent.getStyle() & ReusableHelpPart.SEARCH) != 0) {
			addSpace(buf, 3);
			addPageLink(buf, HelpUIResources.getString("SeeAlsoPart.search"), IHelpUIConstants.HV_FSEARCH_PAGE, //$NON-NLS-1$
				IHelpUIConstants.IMAGE_HELP_SEARCH);
		}
		if ((parent.getStyle() & ReusableHelpPart.CONTEXT_HELP) != 0) {
			addSpace(buf, 3);
			addPageLink(buf, HelpUIResources.getString("SeeAlsoPart.contextHelp"), //$NON-NLS-1$
				IHelpUIConstants.HV_CONTEXT_HELP_PAGE,
				IHelpUIConstants.IMAGE_CONTAINER);
		}
		buf.append("</p>"); //$NON-NLS-1$
		buf.append("</form>"); //$NON-NLS-1$
		text.setText(buf.toString(), true, false);
	}
	
	private void addSpace(StringBuffer buf, int count) {
		for (int i=0; i<count; i++) {
			buf.append(" "); //$NON-NLS-1$
		}
	}

	private void addPageLink(StringBuffer buf, String text, String id,
			String imgRef) {
		buf.append("<img href=\""); //$NON-NLS-1$
		buf.append(imgRef);
		buf.append("\"/>"); //$NON-NLS-1$
		addSpace(buf, 1);
		buf.append("<a href=\""); //$NON-NLS-1$
		buf.append(id);
		buf.append("\">"); //$NON-NLS-1$
		buf.append(text);
		buf.append("</a>"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#getControl()
	 */
	public Control getControl() {
		return container;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#init(org.eclipse.help.ui.internal.views.NewReusableHelpPart)
	 */
	public void init(ReusableHelpPart parent, String id) {
		this.parent = parent;
		this.id = id;
		hookImage(IHelpUIConstants.IMAGE_HELP_SEARCH);
		hookImage(IHelpUIConstants.IMAGE_TOC_OPEN);
		hookImage(IHelpUIConstants.IMAGE_CONTAINER);
		loadText();	
	}

	public String getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		container.setVisible(visible);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public boolean fillContextMenu(IMenuManager manager) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#hasFocusControl(org.eclipse.swt.widgets.Control)
	 */
	public boolean hasFocusControl(Control control) {
		return text.equals(control);
	}

	public IAction getGlobalAction(String id) {
		if (id.equals(ActionFactory.COPY.getId()))
			return parent.getCopyAction();
		return null;
	}
	public void stop() {
	}
}