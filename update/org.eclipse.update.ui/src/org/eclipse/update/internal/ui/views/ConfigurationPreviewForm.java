/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.views;

import java.net.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.help.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.ui.forms.internal.*;
import org.eclipse.update.ui.forms.internal.engine.*;

public class ConfigurationPreviewForm extends WebForm implements IUpdateModelChangedListener {
	private Control focusControl;
	private ConfigurationView view;
	private FormEngine desc;
	private FormEngine taskList;
	private IPreviewTask [] tasks;

	public ConfigurationPreviewForm(ConfigurationView view) {
		this.view = view;
		UpdateModel model = UpdateUI.getDefault().getUpdateModel();
		model.addUpdateModelChangedListener(this);
	}
	
	public void dispose() {
		UpdateModel model = UpdateUI.getDefault().getUpdateModel();
		model.removeUpdateModelChangedListener(this);
		super.dispose();
	}
	
	public Control getScrollingControl() {
		return scrollComposite;
	}

	public void objectsAdded(Object parent, Object [] children) {
	}
	public void objectsRemoved(Object parent, Object [] children) {
	}
	public void objectChanged(Object object, String property) {
	}

	public void initialize(Object model) {
		super.initialize(model);
		if (isWhiteBackground()) {
			setHeadingImage(UpdateUIImages.get(UpdateUIImages.IMG_FORM_BANNER_SHORT));
/*
			setHeadingUnderlineImage(
				UpdateUIImages.get(UpdateUIImages.IMG_FORM_UNDERLINE));
*/
		}
		refreshSize();
	}
	private boolean isWhiteBackground() {
		Color color = getFactory().getBackgroundColor();
		return (
			color.getRed() == 255 && color.getGreen() == 255 && color.getBlue() == 255);
	}
	
	protected void refreshSize() {
		((Composite) getControl()).layout();
		updateSize();
	}
	
	protected void setFocusControl(Control control) {
		focusControl = control;
	}
	public void setFocus() {
		if (focusControl!=null) focusControl.setFocus();
	}
	
	protected void createContents(Composite parent) {
		HTMLTableLayout layout = new HTMLTableLayout();
		parent.setLayout(layout);
		layout.leftMargin = layout.rightMargin = 10;
		layout.topMargin = 10;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 20;
		layout.numColumns = 1;

		FormWidgetFactory factory = getFactory();

		HTTPAction action = new HTTPAction() {
			public void linkActivated(IHyperlinkSegment link) {
				String url = link.getArg();
				if (url!=null)
					UpdateUI.showURL(url);
			}
		};
		IActionBars bars = view.getViewSite().getActionBars();
		action.setStatusLineManager(bars.getStatusLineManager());
		
		HTTPAction taskAction = new HTTPAction() {
			public void linkActivated(IHyperlinkSegment link) {
				String indexArg = link.getArg();
				try {
					int index = Integer.parseInt(indexArg);
					if (tasks!=null)
						tasks[index].run();
				}
				catch (NumberFormatException e) {
				}
			}
		};
		taskAction.setStatusLineManager(bars.getStatusLineManager());

		desc = factory.createFormEngine(parent);
		desc.setHyperlinkSettings(factory.getHyperlinkHandler());
		desc.registerTextObject(FormEngine.URL_HANDLER_ID, action);
		desc.load("", false, false);
		setFocusControl(desc);

		TableData td = new TableData();
		td.align = TableData.FILL;
		td.grabHorizontal = true;
		desc.setLayoutData(td);
		
		taskList = factory.createFormEngine(parent);
		taskList.setHyperlinkSettings(factory.getHyperlinkHandler());
		taskList.registerTextObject("task", taskAction);
		taskList.load("", false, false);
		//factory.setHyperlinkUnderlineMode(HyperlinkSettings.UNDERLINE_ROLLOVER);	

		td = new TableData();
		td.align = TableData.FILL;
		td.grabHorizontal = true;
		taskList.setLayoutData(td);

		WorkbenchHelp.setHelp(parent, "org.eclipse.update.ui.SiteForm");
	}

	
	/**
	 * @see IForm#expandTo(Object)
	 */
	public void expandTo(Object object) {
		tasks = view.getPreviewTasks(object);
		String title = getObjectLabel(object);
		setHeadingText(title);
		String description = getObjectDescription(object);
		boolean tags = description.startsWith("<form>");
		desc.load(description, tags, !tags);
		String taskText = getTasksText();
		taskList.load(taskText, true, false);
		taskList.getParent().layout();
		((Composite) getControl()).layout();
		updateSize();
		getControl().redraw();
	}
	
	private String getObjectLabel(Object object) {
		if (object==null) return "";
		TreeViewer viewer = view.getTreeViewer();
		LabelProvider provider = (LabelProvider)viewer.getLabelProvider();
		return provider.getText(object);
	}
	
	private String getObjectDescription(Object object) {
		if (object instanceof IFeatureAdapter) {
			return getFeatureDescription((IFeatureAdapter)object);
		}
		if (object instanceof IConfiguredSiteAdapter) {
			return UpdateUI.getString("InstallableSitePage.desc");
		}
		if (object instanceof ILocalSite) {
			return "This is the description of the current configuration.";
		}
		return "";
	}
	
	private String getFeatureDescription(IFeatureAdapter adapter) {
		try {
			IFeature feature = adapter.getFeature(null);
			IURLEntry entry = feature.getDescription();
			if (entry!=null) {
				String text = entry.getAnnotation();
				if (text!=null) {
					URL url = entry.getURL();
					if (url==null) return text;
					else {
						String link = " <a href=\"urlHandler\" arg=\""+url+"\">More info...</a>";
						String fullText = "<form><p>"+text+link+"</p></form>";
						return fullText;
					}
				}
			}
		}
		catch (CoreException e) {
		}
		return "";
	}

	private String getTasksText() {
		if (tasks==null || tasks.length==0) return "<form/>";
		boolean hasEnabledTasks=false;
		for (int i=0; i<tasks.length; i++) {
			if (tasks[i].isEnabled()) {
				hasEnabledTasks = true;
				break;
			}
		}
		if (!hasEnabledTasks) return "<form/>";
		StringBuffer buf = new StringBuffer();
		buf.append("<form><p><b>Available Tasks</b></p>");
		for (int i=0; i<tasks.length; i++) {
			IPreviewTask task = tasks[i];
			if (task.isEnabled()==false) continue;
			buf.append("<li style=\"text\" indent=\"0\"><a href=\"task\" arg=\""+i+"\">"+task.getName()+"</a></li>");
			buf.append("<li style=\"text\" indent=\"10\" addVerticalSpace=\"false\">"+task.getDescription()+"</li>");
		}
		buf.append("</form>");
		return buf.toString();
	}
}
