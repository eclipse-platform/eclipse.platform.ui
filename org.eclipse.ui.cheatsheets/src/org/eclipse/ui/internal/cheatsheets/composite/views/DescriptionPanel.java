/*******************************************************************************
 * Copyright (c) 2005 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.cheatsheets.composite.views;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.cheatsheets.ICompositeCheatSheetTask;
import org.eclipse.ui.cheatsheets.IEditableTask;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.ScrolledFormText;
import org.eclipse.ui.internal.cheatsheets.CheatSheetPlugin;
import org.eclipse.ui.internal.cheatsheets.ICheatSheetResource;
import org.eclipse.ui.internal.cheatsheets.Messages;
import org.eclipse.ui.internal.cheatsheets.composite.model.AbstractTask;
import org.eclipse.ui.internal.cheatsheets.composite.model.SuccesorTaskFinder;
import org.eclipse.ui.internal.cheatsheets.composite.model.TaskStateUtilities;
import org.eclipse.ui.internal.cheatsheets.composite.parser.ICompositeCheatsheetTags;

/**
 * The description panel of a composite cheat sheet panel. This panel shows the introduction
 * message and depending upon the type and state of the task a selection of the following:
 * Completion message
 * A message indicating that the task has been skipped.
 * A message indicating that a parent task has been skipped.
 * A message indicating that the task is blocked.
 * A message indicationg that a parent choice is already satisfied.
 * A link to a successor task.
 * All tasks completed message.
 */

public class DescriptionPanel {
	
	private static final String REVIEW_IMAGE = "review"; //$NON-NLS-1$
	private static final String GOTO_IMAGE = "goto"; //$NON-NLS-1$
	private static final String SKIP_IMAGE = "skip"; //$NON-NLS-1$
	private static final String START_IMAGE = "start"; //$NON-NLS-1$
	private ScrolledFormText panel;
	
	public DescriptionPanel(ManagedForm mform, Composite container) {
		FormText text;
		panel = new ScrolledFormText(container, false);
		mform.getToolkit().adapt(panel, false, false);			
		text = mform.getToolkit().createFormText(panel, true);
		text.marginWidth = 5;
		text.marginHeight = 5;
		text.setFont("header", JFaceResources.getHeaderFont()); //$NON-NLS-1$
		text.setColor("title", mform.getToolkit().getColors().getColor(FormColors.TITLE)); //$NON-NLS-1$
		text.setImage(START_IMAGE, CheatSheetPlugin.getPlugin().getImage(ICheatSheetResource.COMPOSITE_TASK_START));
		text.setImage(SKIP_IMAGE, CheatSheetPlugin.getPlugin().getImage(ICheatSheetResource.COMPOSITE_TASK_SKIP));
		text.setImage(GOTO_IMAGE, CheatSheetPlugin.getPlugin().getImage(ICheatSheetResource.COMPOSITE_GOTO_TASK)); 
		text.setImage(REVIEW_IMAGE, CheatSheetPlugin.getPlugin().getImage(ICheatSheetResource.COMPOSITE_TASK_REVIEW));
		panel.setFormText(text);
	}
	
	public Control getControl() {
		return panel;
	}
	
	public void addHyperlinkListener(IHyperlinkListener listener) {
		panel.getFormText().addHyperlinkListener(listener);
		
	}
	
	public void showDescription(final ICompositeCheatSheetTask task) {
		FormText text = panel.getFormText();	
		StringBuffer buf = new StringBuffer();
		buf.append("<form>"); //$NON-NLS-1$
		buf.append("<p><span color=\"title\" font=\"header\">"); //$NON-NLS-1$
		buf.append(task.getName());
		buf.append("</span></p>"); //$NON-NLS-1$		

		buf.append(createParagraph(task.getDescription()));

        boolean startable = false;
        boolean isBlocked = false;
        boolean isSkippable = ((AbstractTask)task).isSkippable();
		
		if (task.getState() == ICompositeCheatSheetTask.COMPLETED) {
			buf.append(createParagraph(task.getCompletionMessage()));
			isSkippable = false;
		} else if (task.getState() == ICompositeCheatSheetTask.SKIPPED) {
			buf.append(createParagraph(Messages.THIS_TASK_SKIPPED));
			isSkippable = false;
		} else if (TaskStateUtilities.findSkippedAncestor(task) != null) {
			ICompositeCheatSheetTask skipped = TaskStateUtilities.findSkippedAncestor(task);
			String skipParentMsg = NLS.bind(Messages.PARENT_SKIPPED, (new Object[] {skipped.getName()}));	
			buf.append(createParagraph(skipParentMsg));
			isSkippable = false;
		} else if (TaskStateUtilities.findCompletedAncestor(task) != null) {
			ICompositeCheatSheetTask completed = TaskStateUtilities.findCompletedAncestor(task);
			String completedParentMsg = NLS.bind(Messages.PARENT_COMPLETED, (new Object[] {completed.getName()}));	
			buf.append(createParagraph(completedParentMsg));
			isSkippable = false;
		} else if (!task.requiredTasksCompleted()) {
			isBlocked = true;
			showBlockingTasks(Messages.COMPOSITE_PAGE_BLOCKED, task, buf);
		} else if (TaskStateUtilities.findBlockedAncestor(task) != null) {
			isBlocked = true;
			ICompositeCheatSheetTask blockedAncestor = TaskStateUtilities.findBlockedAncestor(task);
			String skipParentMsg = NLS.bind(Messages.PARENT_BLOCKED, (new Object[] {blockedAncestor.getName()}));	
			showBlockingTasks(skipParentMsg , blockedAncestor, buf);
		} else {
			startable = task instanceof IEditableTask && task.getState() == ICompositeCheatSheetTask.NOT_STARTED;
		}
		if (startable) {
			addHyperlink(buf, CompositeCheatSheetPage.START_HREF, START_IMAGE, Messages.COMPOSITE_PAGE_START_TASK);
		}
		
		if (isSkippable) {
			addHyperlink(buf, CompositeCheatSheetPage.SKIP_HREF, SKIP_IMAGE, Messages.COMPOSITE_PAGE_SKIP_TASK);
		}

		if (!startable && !isBlocked) {
			showSuccesorTaskLinks(task, buf);
		}
		
		if (task instanceof IEditableTask && task.getState() == ICompositeCheatSheetTask.COMPLETED ) {
			addHyperlink(buf, CompositeCheatSheetPage.REVIEW_TAG, REVIEW_IMAGE, Messages.COMPOSITE_PAGE_REVIEW_TASK);
		}		
	
		buf.append("</form>"); //$NON-NLS-1$

		text.setText(buf.toString(), true, false);
		panel.setData(ICompositeCheatsheetTags.TASK, task);
		panel.reflow(true);
	}

	/*
	 * Add paragraph tags if not already present
	 */
	private String createParagraph(String text) {
		String trimmed = text.trim();
		if (trimmed.charAt(0)!='<') {
			return "<p>" + trimmed + "</p>"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		return trimmed;
	}

	private void showBlockingTasks(String message, final ICompositeCheatSheetTask task, StringBuffer buf) {
		buf.append("<p/>"); //$NON-NLS-1$
		buf.append("<p>"); //$NON-NLS-1$
		buf.append("<b>"); //$NON-NLS-1$
		buf.append(message);
		buf.append("</b>"); //$NON-NLS-1$
		buf.append("</p>");	 //$NON-NLS-1$// Add the list of blocking tasks
		
		ICompositeCheatSheetTask[] requiredTasks = task.getRequiredTasks();
		for (int i = 0; i < requiredTasks.length; i++) {
			warnOfIncompleteTask(buf, requiredTasks[i]);
		}
		buf.append("<p>"); //$NON-NLS-1$
		buf.append("</p>");	 //$NON-NLS-1$
	}
	
	private void addHyperlink(StringBuffer buf, String href, String imageRef, String message) {
		buf.append("<p/>"); //$NON-NLS-1$
		buf.append("<p><a href=\""); //$NON-NLS-1$
		buf.append(href);
		buf.append("\">"); //$NON-NLS-1$
		buf.append("<img href=\""); //$NON-NLS-1$
		buf.append(imageRef);
		buf.append("\"/> "); //$NON-NLS-1$
		buf.append(message);
		buf.append("</a></p>"); //$NON-NLS-1$
	}
	
	/*
	 * If this task is incomplete create a message to that effect
	 */
	private void warnOfIncompleteTask(StringBuffer buf, ICompositeCheatSheetTask task) {
		if (task.getState() != ICompositeCheatSheetTask.COMPLETED &&
			task.getState() != ICompositeCheatSheetTask.SKIPPED	) {
			buf.append("<li>"); //$NON-NLS-1$
			buf.append("<a href=\""); //$NON-NLS-1$
			buf.append(CompositeCheatSheetPage.GOTO_TASK_TAG);
			buf.append(task.getId());
			buf.append("\">"); //$NON-NLS-1$	
			buf.append(NLS.bind(Messages.COMPOSITE_PAGE_TASK_NOT_COMPLETE, (new Object[] {task.getName()})));	
			buf.append("</a>"); //$NON-NLS-1$	
			buf.append("</li>"); //$NON-NLS-1$
	    }
	}
	
	private void showSuccesorTaskLinks(ICompositeCheatSheetTask task, StringBuffer buf) {
		// Add the links to the next tasks
		ICompositeCheatSheetTask[] successorTasks = new SuccesorTaskFinder(task).getRecommendedSuccessors();
		for (int i = 0; i < successorTasks.length; i++) {
			ICompositeCheatSheetTask successor = successorTasks[i];
			String message = NLS.bind(Messages.COMPOSITE_PAGE_GOTO_TASK, (new Object[] {successor.getName()}));
			addHyperlink(buf, CompositeCheatSheetPage.GOTO_TASK_TAG + successor.getId(), GOTO_IMAGE, message);
		}
	}
	
}
