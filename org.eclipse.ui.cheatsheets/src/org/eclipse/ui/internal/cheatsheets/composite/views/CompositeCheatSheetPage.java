/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.cheatsheets.composite.views;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.cheatsheets.ICompositeCheatSheetTask;
import org.eclipse.ui.cheatsheets.ITaskEditor;
import org.eclipse.ui.cheatsheets.ITaskExplorer;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.ScrolledFormText;
import org.eclipse.ui.internal.cheatsheets.composite.model.CompositeCheatSheetModel;
import org.eclipse.ui.internal.cheatsheets.composite.model.CompositeCheatSheetSaveHelper;
import org.eclipse.ui.internal.cheatsheets.composite.parser.ICompositeCheatsheetTags;
import org.eclipse.ui.internal.cheatsheets.views.Page;

/**
 * A page which represents a composite cheat sheet
 */

public class CompositeCheatSheetPage extends Page implements ISelectionChangedListener {

	private static final String TASK = "task"; //$NON-NLS-1$
	private static final String EDITOR = "editor"; //$NON-NLS-1$
	private static final String STARTED = "started"; //$NON-NLS-1$
	private static final String TRUE = "true"; //$NON-NLS-1$
	private ManagedForm mform;
	private Composite explorerContainer;
	private Composite taskEditorContainer;
	private CompositeCheatSheetModel model;
	private ITaskExplorer currentExplorer;
	private ScrolledFormText descriptionPanel;
	private ScrolledFormText completePanel;
	
	private CompositeCheatSheetSaveHelper saveHelper;
	
	private static final String START_HREF = "__start__"; //$NON-NLS-1$
	private ICompositeCheatSheetTask selectedTask;
	public CompositeCheatSheetPage(CompositeCheatSheetModel model) {
		this.model = model;
		saveHelper = new CompositeCheatSheetSaveHelper();
	}

	public void createPart(Composite parent) {
		//////
		init(parent.getDisplay());
	    //////
		form = toolkit.createScrolledForm(parent);		
		form.setLayoutData(new GridData(GridData.FILL_BOTH));
		mform = new ManagedForm(toolkit, form);
		FillLayout flayout = new FillLayout();
		flayout.marginHeight = 0;
		flayout.marginWidth = 0;
		form.getBody().setLayout(flayout);
		final SashForm sash = new SashForm(form.getBody(), SWT.NULL);
		sash.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				Point size = sash.getSize();
				if (size.x>size.y)
					sash.setOrientation(SWT.HORIZONTAL);
				else
					sash.setOrientation(SWT.VERTICAL);
			}
		});
		//toolkit.adapt(sash, false, false);
		explorerContainer = toolkit.createComposite(sash);
		StackLayout layout = new StackLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		explorerContainer.setLayout(layout);
		taskEditorContainer = toolkit.createComposite(sash);
		layout = new StackLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		taskEditorContainer.setLayout(layout);
	}

	public void dispose() {
		mform.dispose();
		super.dispose();
	}

	private void setInputModel(CompositeCheatSheetModel model) {
		this.model = model;
		mform.getForm().setText(model.getName());
		String explorerId = model.getTaskExplorerId();
		ITaskExplorer explorer = getTaskExplorer(explorerId);
		if (explorer!=null) {
			explorer.setCompositeCheatSheet(model);
			explorer.setFocus();
			setExplorerVisible(explorerId);
		}
		model.addObserver(new Observer() {
			public void update(Observable o, Object arg) {
				ICompositeCheatSheetTask task = (ICompositeCheatSheetTask)arg;
				if (currentExplorer!=null)
					currentExplorer.taskUpdated(task);
				updateTask(task);
			}
		});
	}

	private void setExplorerVisible(String id) {
		Control [] excontrols = explorerContainer.getChildren();
		ITaskExplorer explorer=null;
		StackLayout layout = (StackLayout)explorerContainer.getLayout();
		for (int i=0; i<excontrols.length; i++) {
			Control excontrol = excontrols[i];
			explorer = (ITaskExplorer)excontrol.getData(ICompositeCheatsheetTags.EXPLORER);
			if (explorer.getId().equals(id)) {
				layout.topControl = excontrol;
				explorerContainer.layout();
				setCurrentExplorer(explorer);
				return;
			}
		}
	}
	
	private void setCurrentExplorer(ITaskExplorer explorer) {
		if (currentExplorer!=null) {
			currentExplorer.getSelectionProvider().removeSelectionChangedListener(this);
		}
		currentExplorer = explorer;
		currentExplorer.getSelectionProvider().addSelectionChangedListener(this);
		updateForSelection(currentExplorer.getSelectionProvider().getSelection());
	}

	private ITaskExplorer getTaskExplorer(String id) {
		Control [] excontrols = explorerContainer.getChildren();
		ITaskExplorer explorer=null;
		for (int i=0; i<excontrols.length; i++) {
			Control excontrol = excontrols[i];
			Object data = excontrol.getData(ICompositeCheatsheetTags.EXPLORER);
			if (data instanceof ITaskExplorer) {
			    explorer = (ITaskExplorer)data;
			    if (explorer.getId().equals(id)) {
				    return explorer;
			    }
			}
		}
	
		// Load the explorer from an extension point

		TaskExplorerManager explorerManager = TaskExplorerManager.getInstance();
		explorer = explorerManager.getExplorer(id); 
		if (explorer != null) {
			explorer.createControl(explorerContainer, mform.getToolkit());
			explorer.getControl().setData(ICompositeCheatsheetTags.EXPLORER, explorer);
		}
		return explorer;
	}

	public void selectionChanged(SelectionChangedEvent event) {
		updateForSelection(event.getSelection());
	}

	private void updateForSelection(ISelection selection) {
		selectedTask = (ICompositeCheatSheetTask)((IStructuredSelection)selection).getFirstElement();
		updateTask(selectedTask);
	}

	private void updateTask(ICompositeCheatSheetTask task) {
		if (task==null) return;
		int state = task.getState();
		switch (state) {
		case ICompositeCheatSheetTask.NOT_STARTED:
			// not started or no kind - show description
			showDescription(task);
			break;
		case ICompositeCheatSheetTask.IN_PROGRESS:
			// In progress, show the task editor
			startIfSelected(task);
			ITaskEditor editor = getTaskEditor(task);
			setCurrentEditor(editor.getControl().getParent());
			break;
		case ICompositeCheatSheetTask.COMPLETED:
			// complete task - show the completion panel
			showComplete(task);
			break;
		}
		saveGuideState();
	}

	private void saveGuideState() {
		saveHelper.saveCompositeState(model);	
	}

	private void showDescription(final ICompositeCheatSheetTask task) {
		if (descriptionPanel==null) {
			descriptionPanel = new ScrolledFormText(taskEditorContainer, false);
			mform.getToolkit().adapt(descriptionPanel, false, false);			
			FormText text = mform.getToolkit().createFormText(descriptionPanel, true);
			descriptionPanel.setFormText(text);
			text.addHyperlinkListener(new HyperlinkAdapter() {
				public void linkActivated(HyperlinkEvent e) {
					if (e.getHref().equals(START_HREF)) {
						ICompositeCheatSheetTask task = (ICompositeCheatSheetTask)descriptionPanel.getData(ICompositeCheatsheetTags.TASK);
						task.advanceState();
						startIfSelected(task);
					}
				}
			});
		}
		String desc = task.getDescription().trim();
		StringBuffer buf = new StringBuffer();
		buf.append("<form>"); //$NON-NLS-1$
		if (desc.charAt(0)!='<') {
			buf.append("<p>"); //$NON-NLS-1$
			buf.append(desc);
			buf.append("</p>"); //$NON-NLS-1$
		}
		else
			buf.append(desc);
		if (task.getKind()!=null) {
			if (task.isStartable()) {
				buf.append("<p/>"); //$NON-NLS-1$
				buf.append("<p><a href=\""); //$NON-NLS-1$
				buf.append(START_HREF);
				buf.append("\">"); //$NON-NLS-1$
				buf.append("Start working on the task."); //$NON-NLS-1$
				buf.append("</a></p>"); //$NON-NLS-1$
			} else {
				buf.append("<p/>"); //$NON-NLS-1$
				buf.append("<p>"); //$NON-NLS-1$
				buf.append("This task cannot be started until all prerequisite tasks are completed."); //$NON-NLS-1$
				buf.append("</p>"); //$NON-NLS-1$
				buf.append("Task \"Create a java project\" is not complete."); //$NON-NLS-1$
				buf.append("<p>"); //$NON-NLS-1$
				buf.append("</p>");	 //$NON-NLS-1$
			}
		}
		buf.append("</form>"); //$NON-NLS-1$

		descriptionPanel.setText(buf.toString());
		descriptionPanel.setData(ICompositeCheatsheetTags.TASK, task);
		setCurrentEditor(descriptionPanel);
	}
	
	/*
	 * Ensure that if this task is visible and in a runnable state that it has been started
	 */
	private void startIfSelected(ICompositeCheatSheetTask task) {
		if (task == selectedTask) {
			ITaskEditor editor = getTaskEditor(task);
			if (editor!=null) {
				if (!TRUE.equals(editor.getControl().getData(STARTED))) {
				    editor.start(task);
				    editor.getControl().setData(STARTED, TRUE);
				}
				setCurrentEditor(editor.getControl().getParent());
			}
		}
	}
	
	private void setCurrentEditor(Control c) {
		StackLayout layout = (StackLayout)taskEditorContainer.getLayout();
		layout.topControl = c;
		taskEditorContainer.layout();
	}
	
	private void showComplete(ICompositeCheatSheetTask task) {
		if (completePanel==null) {
			completePanel = new ScrolledFormText(taskEditorContainer, false);
			mform.getToolkit().adapt(completePanel, false, false);
			FormText text = mform.getToolkit().createFormText(completePanel, true);
			completePanel.setFormText(text);
		}
		String desc = task.getCompletionMessage().trim();
		StringBuffer buf = new StringBuffer();
		buf.append("<form>"); //$NON-NLS-1$
		if (desc.charAt(0)!='<') {
			buf.append("<p>"); //$NON-NLS-1$
			buf.append(desc);
			buf.append("</p>"); //$NON-NLS-1$
		} else {
			buf.append(desc);
	    }
		
		// TODO add links to the next task
		/*
		// Add the completion message
		buf.append("<p/>");
		buf.append("<p><a href=\"");
		buf.append(START_HREF);
		buf.append("\">");
		buf.append("Go to task \"Deploy a java project\"");
		buf.append("</a></p>");
		*/
		
		buf.append("</form>"); //$NON-NLS-1$

		completePanel.setText(buf.toString());
		setCurrentEditor(completePanel);
	}
	
	private ITaskEditor getTaskEditor(ICompositeCheatSheetTask task) {
		Control [] controls = taskEditorContainer.getChildren();
		for (int i=0; i<controls.length; i++) {
			Control control = controls[i];
			if (control==descriptionPanel || control==completePanel)
				continue;
			ICompositeCheatSheetTask ctask = (ICompositeCheatSheetTask)control.getData(ICompositeCheatsheetTags.TASK);
			if (task==ctask)
				return (ITaskEditor)control.getData(EDITOR);
		}
		// Create a new editor using the extension point data
		ITaskEditor editor = TaskEditorManager.getInstance().getEditor(task.getKind());
		if (editor != null) {
			Composite c = mform.getToolkit().createComposite(taskEditorContainer);
			FillLayout layout = new FillLayout();
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			c.setLayout(layout);
			editor.createControl(c, mform.getToolkit());
			c.setData(TASK, task);
			c.setData(EDITOR, editor);
		}
		return editor;
	}
	
	// TODO enable a reset mechanism
	/*
	 *
	private class ResetAction extends Action {
		public ResetAction() {
			setText("Reset Guide State");
		}
		
		public void run() {
			if (model instanceof CompositeCheatSheetModel) {
				resetTask(((CompositeCheatSheetModel)model).getRootTask());
			}
		}

		private void resetTask(IGuideTask task) {
			GuideTask gTask = (GuideTask)task;
			gTask.setState(0);
			gTask.setPercentageComplete(0);
			IGuideTask[] subtasks = gTask.getSubtasks();
			for (int i = 0; i < subtasks.length; i++) {
				resetTask(subtasks[i]);
			}
		}
	}
	*/

	public Control getControl() {
		return form;
	}

	protected String getTitle() {
		return model.getName();
	}

	public void initialized() {
		// Open the model
		saveHelper.loadCompositeState(model, null);
		setInputModel(model);
		model.setSaveHelper(saveHelper);	
	}

}
