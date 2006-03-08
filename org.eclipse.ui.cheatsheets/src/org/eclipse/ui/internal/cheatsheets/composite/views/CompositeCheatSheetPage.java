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

import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.cheatsheets.ICompositeCheatSheetTask;
import org.eclipse.ui.cheatsheets.IEditableTask;
import org.eclipse.ui.cheatsheets.TaskEditor;
import org.eclipse.ui.cheatsheets.TaskExplorer;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.ScrolledFormText;
import org.eclipse.ui.internal.cheatsheets.CheatSheetPlugin;
import org.eclipse.ui.internal.cheatsheets.ICheatSheetResource;
import org.eclipse.ui.internal.cheatsheets.Messages;
import org.eclipse.ui.internal.cheatsheets.actions.IMenuContributor;
import org.eclipse.ui.internal.cheatsheets.composite.model.AbstractTask;
import org.eclipse.ui.internal.cheatsheets.composite.model.CompositeCheatSheetModel;
import org.eclipse.ui.internal.cheatsheets.composite.model.CompositeCheatSheetSaveHelper;
import org.eclipse.ui.internal.cheatsheets.composite.model.EditableTask;
import org.eclipse.ui.internal.cheatsheets.composite.model.SuccesorTaskFinder;
import org.eclipse.ui.internal.cheatsheets.composite.parser.ICompositeCheatsheetTags;
import org.eclipse.ui.internal.cheatsheets.registry.CheatSheetRegistryReader;
import org.eclipse.ui.internal.cheatsheets.registry.CheatSheetRegistryReader.TaskExplorerNode;
import org.eclipse.ui.internal.cheatsheets.views.Page;
import org.eclipse.ui.part.PageBook;

/**
 * A page which represents a composite cheat sheet
 */

public class CompositeCheatSheetPage extends Page implements ISelectionChangedListener, IMenuContributor {

	private static final String REVIEW_TAG = "Review:"; //$NON-NLS-1$
	private static final String NEXT_TASK_TAG = "Next:"; //$NON-NLS-1$
	private ManagedForm mform;
	private PageBook explorerContainer;
	private PageBook taskEditorContainer;
	private CompositeCheatSheetModel model;
	private TaskExplorer currentExplorer;
	private ScrolledFormText descriptionPanel;
	private ScrolledFormText completePanel;
	
	private CompositeCheatSheetSaveHelper saveHelper;
	
	private static final String START_HREF = "__start__"; //$NON-NLS-1$
	private static final String SKIP_HREF = "__skip__"; //$NON-NLS-1$
	private ICompositeCheatSheetTask selectedTask;
	
	public CompositeCheatSheetPage(CompositeCheatSheetModel model) {
		this.model = model;
		saveHelper = new CompositeCheatSheetSaveHelper();
	}

	public void createPart(Composite parent) {
		init(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);		
		form.setLayoutData(new GridData(GridData.FILL_BOTH));
		FormColors colors = toolkit.getColors();
		colors.initializeSectionToolBarColors();
		Color gbg = colors.getColor(FormColors.TB_GBG);
		Color bg = colors.getBackground();
		form.getForm().setTextBackground(new Color[]{bg, gbg}, new int [] {100}, true);
		form.getForm().setSeparatorColor(colors.getColor(FormColors.TB_BORDER));
		form.getForm().setSeparatorVisible(true);
		mform = new ManagedForm(toolkit, form);
		GridLayout glayout = new GridLayout();
		glayout.marginHeight = 0;
		glayout.marginWidth = 0;
		form.getBody().setLayout(glayout);
		final SashForm sash = new SashForm(form.getBody(), SWT.NULL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 10;
		gd.heightHint = 10;
		sash.setLayoutData(gd);
		sash.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				Point size = sash.getSize();
				if (size.x>size.y)
					sash.setOrientation(SWT.HORIZONTAL);
				else
					sash.setOrientation(SWT.VERTICAL);
				updateSashPanelMargins(sash);
			}
		});
		sash.setBackground(colors.getColor(FormColors.TB_GBG));
		
		Composite explorerPanel = new Composite(sash, SWT.NULL);
		explorerPanel.setBackground(colors.getColor(FormColors.TB_BORDER));
		GridLayout playout = new GridLayout();
		playout.marginWidth = 0;
		playout.marginHeight = 0;
		explorerPanel.setLayout(playout);
		explorerContainer = new PageBook(explorerPanel, SWT.NULL);
		explorerContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
		Composite editorPanel = new Composite(sash, SWT.NULL);
		playout = new GridLayout();
		playout.marginWidth = 0;
		playout.marginHeight = 0;
		editorPanel.setLayout(playout);
		editorPanel.setBackground(colors.getColor(FormColors.TB_BORDER));		
		taskEditorContainer = new PageBook(editorPanel, SWT.NULL);
		toolkit.adapt(taskEditorContainer);
		taskEditorContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
	}
	
	private void updateSashPanelMargins(SashForm sash) {
		Control [] children = sash.getChildren();
		int orientation = sash.getOrientation();
		// update task explorer panel
		GridLayout layout = (GridLayout)((Composite)children[0]).getLayout();
		if (orientation==SWT.HORIZONTAL) {
			layout.marginBottom = 0;
			layout.marginRight = 1;
		}
		else {
			layout.marginBottom = 1;
			layout.marginRight = 0;
		}
		// update task editor panel
		layout = (GridLayout)((Composite)children[1]).getLayout();
		if (orientation==SWT.HORIZONTAL) {
			layout.marginTop = 0;
			layout.marginLeft = 1;
		}
		else {
			layout.marginTop = 1;
			layout.marginLeft = 0;
		}
		((Composite)children[0]).layout();
		((Composite)children[1]).layout();
	}

	public void dispose() {
		mform.dispose();
		super.dispose();
	}

	private void setInputModel(CompositeCheatSheetModel model) {
		this.model = model;
		mform.getForm().setText(model.getName());
		String explorerId = model.getTaskExplorerId();
		setCurrentExplorerFromId(explorerId);
		model.addObserver(new Observer() {
			public void update(Observable o, Object arg) {
				ICompositeCheatSheetTask task = (ICompositeCheatSheetTask)arg;
				if (currentExplorer!=null)
					currentExplorer.taskUpdated(task);
				updateTask(task);
			}
		});
	}

	private void setCurrentExplorerFromId(String explorerId) {
		TaskExplorer explorer = getTaskExplorer(explorerId);
		if (explorer!=null) {
			explorer.setCompositeCheatSheet(this.model);
			explorer.setFocus();
			setExplorerVisible(explorerId);
		}
	}

	private void setExplorerVisible(String id) {
		Control [] excontrols = explorerContainer.getChildren();
		TaskExplorer explorer=null;
		for (int i=0; i<excontrols.length; i++) {
			Control excontrol = excontrols[i];
			explorer = (TaskExplorer)excontrol.getData(ICompositeCheatsheetTags.EXPLORER);
			if (explorer.getId().equals(id)) {
				explorerContainer.showPage(excontrol);
				setCurrentExplorer(explorer);
				return;
			}
		}
	}
	
	private void setCurrentExplorer(TaskExplorer explorer) {
		if (currentExplorer!=null) {
			currentExplorer.getSelectionProvider().removeSelectionChangedListener(this);
		}
		currentExplorer = explorer;
		currentExplorer.getSelectionProvider().addSelectionChangedListener(this);
		updateForSelection(currentExplorer.getSelectionProvider().getSelection());
	}

	private TaskExplorer getTaskExplorer(String id) {
		Control [] excontrols = explorerContainer.getChildren();
		TaskExplorer explorer=null;
		for (int i=0; i<excontrols.length; i++) {
			Control excontrol = excontrols[i];
			Object data = excontrol.getData(ICompositeCheatsheetTags.EXPLORER);
			if (data instanceof TaskExplorer) {
			    explorer = (TaskExplorer)data;
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
			if (task instanceof IEditableTask) {
			    showTaskEditor((EditableTask) task);
			} else {
				showDescription(task);
			}
			break;
		case ICompositeCheatSheetTask.COMPLETED:
		case ICompositeCheatSheetTask.SKIPPED:
			// complete task - show the completion panel
			showComplete(task);
			break;
		}
	}

	private void showTaskEditor(EditableTask task) {
		startIfSelected(task, false);
		TaskEditor editor = getTaskEditor(task, false);
		if (editor != null) {
		    setCurrentEditor(editor.getControl().getParent());
		}
	}

	public void saveState() {
		saveHelper.saveCompositeState(model);	
	}

	private void showDescription(final ICompositeCheatSheetTask task) {
		FormText text;
		if (descriptionPanel==null) {
			descriptionPanel = new ScrolledFormText(taskEditorContainer, false);
			mform.getToolkit().adapt(descriptionPanel, false, false);			
			text = mform.getToolkit().createFormText(descriptionPanel, true);
			text.marginWidth = 5;
			text.marginHeight = 5;
			text.setFont("header", JFaceResources.getHeaderFont()); //$NON-NLS-1$
			text.setColor("title", mform.getToolkit().getColors().getColor(FormColors.TITLE)); //$NON-NLS-1$
			text.setImage("start", CheatSheetPlugin.getPlugin().getImage(ICheatSheetResource.CHEATSHEET_START)); //$NON-NLS-1$
			text.setImage("skip", CheatSheetPlugin.getPlugin().getImage(ICheatSheetResource.CHEATSHEET_ITEM_BUTTON_SKIP)); //$NON-NLS-1$
			descriptionPanel.setFormText(text);
			text.addHyperlinkListener(new DescriptionLinkListener());
		} else {
			text = descriptionPanel.getFormText();
		}
		String desc = task.getDescription().trim();
		StringBuffer buf = new StringBuffer();
		buf.append("<form>"); //$NON-NLS-1$
		buf.append("<p><span color=\"title\" font=\"header\">"); //$NON-NLS-1$
		buf.append(task.getName());
		buf.append("</span></p>"); //$NON-NLS-1$		
		if (desc.charAt(0)!='<') {
			buf.append("<p>"); //$NON-NLS-1$
			buf.append(desc);
			buf.append("</p>"); //$NON-NLS-1$
		} else {
			buf.append(desc);
		}
		
		if (!task.requiredTasksCompleted()) {
			showBlockedTasks(task, buf);
		} else {
			if (task instanceof IEditableTask) {
			    addStartHyperlink(buf);
			} else {
				showSuccesorTaskLinks(task, buf);
			}
			AbstractTask abstractTask = (AbstractTask)task;
			if (abstractTask.isSkippable()) {
				addSkipHyperlink(buf);
			}
		}
	
		buf.append("</form>"); //$NON-NLS-1$

		text.setText(buf.toString(), true, false);
		descriptionPanel.setData(ICompositeCheatsheetTags.TASK, task);
		descriptionPanel.reflow(true);
		setCurrentEditor(descriptionPanel);
	}

	private void showBlockedTasks(final ICompositeCheatSheetTask task, StringBuffer buf) {
		buf.append("<p/>"); //$NON-NLS-1$
		buf.append("<p>"); //$NON-NLS-1$
		buf.append("<b>"); //$NON-NLS-1$
		buf.append(Messages.COMPOSITE_PAGE_BLOCKED);
		buf.append("</b>"); //$NON-NLS-1$
		buf.append("</p>");	 //$NON-NLS-1$// Add the list of blocking tasks
		
		ICompositeCheatSheetTask[] requiredTasks = task.getRequiredTasks();
		for (int i = 0; i < requiredTasks.length; i++) {
			addIfIncomplete(buf, requiredTasks[i]);
		}
		buf.append("<p>"); //$NON-NLS-1$
		buf.append("</p>");	 //$NON-NLS-1$
	}
	
	private void addSkipHyperlink(StringBuffer buf) {
		buf.append("<p/>"); //$NON-NLS-1$
		buf.append("<p><a href=\""); //$NON-NLS-1$
		buf.append(SKIP_HREF);
		buf.append("\">"); //$NON-NLS-1$
		buf.append("<img href=\"skip\"/> "); //$NON-NLS-1$
		buf.append(Messages.COMPOSITE_PAGE_SKIP_TASK);
		buf.append("</a></p>"); //$NON-NLS-1$
	}

	private void addStartHyperlink(StringBuffer buf) {
		buf.append("<p/>"); //$NON-NLS-1$
		buf.append("<p><a href=\""); //$NON-NLS-1$
		buf.append(START_HREF);
		buf.append("\">"); //$NON-NLS-1$
		buf.append("<img href=\"start\"/> "); //$NON-NLS-1$
		buf.append(Messages.COMPOSITE_PAGE_START_TASK);
		buf.append("</a></p>"); //$NON-NLS-1$
	}
	
	private void addIfIncomplete(StringBuffer buf, ICompositeCheatSheetTask task) {
		if (task.getState() != ICompositeCheatSheetTask.COMPLETED &&
			task.getState() != ICompositeCheatSheetTask.SKIPPED	) {
			buf.append("<li>"); //$NON-NLS-1$
			String message = NLS.bind(Messages.COMPOSITE_PAGE_TASK_NOT_COMPLETE, (new Object[] {task.getName()}));	
			buf.append(message);
			buf.append("</li>"); //$NON-NLS-1$
	    }
	}

	/*
	 * Ensure that if this task is visible and in a runnable state that it has been started
	 */
	private void startIfSelected(ICompositeCheatSheetTask task, boolean initialize) {
		if (task == selectedTask) {
			TaskEditor editor = getTaskEditor(task, initialize);
			if (editor!=null) {
				setCurrentEditor(editor.getControl());
			}
		}
	}
	
	private void setCurrentEditor(Control c) {
		taskEditorContainer.showPage(c);
	}
	
	private final class DescriptionLinkListener extends HyperlinkAdapter {
		public void linkActivated(HyperlinkEvent e) {
			String ref = (String)e.getHref();
			if (ref.equals(START_HREF)) {
				Object data = descriptionPanel.getData(ICompositeCheatsheetTags.TASK);
				if (data instanceof EditableTask) {
				    EditableTask task = (EditableTask)data;
				    task.setStarted();
				    startIfSelected(task, true);
				}
			}     
            if (ref.equals(SKIP_HREF)) {
				Object data = descriptionPanel.getData(ICompositeCheatsheetTags.TASK);
				if (data instanceof AbstractTask) {
				    AbstractTask task = (AbstractTask)data;
				    task.setState(ICompositeCheatSheetTask.SKIPPED);
				}
			}
            checkForTaskLink(ref);
		}
	}

	private final class CompletionHyperlinkAdapter extends HyperlinkAdapter {

		public void linkActivated(HyperlinkEvent e) {
			String ref = (String)e.getHref();
			if (ref.startsWith(REVIEW_TAG)) {
				String review = ref.substring(REVIEW_TAG.length());
				AbstractTask reviewTask =
				    model.getDependencies().getTask(review);
				if (reviewTask instanceof EditableTask) {
				    showTaskEditor((EditableTask) reviewTask);
				}
			}
			checkForTaskLink(ref);
		}
	}
	
	private void checkForTaskLink(String ref) {
		if (ref.startsWith(NEXT_TASK_TAG)) {
			String next = ref.substring(NEXT_TASK_TAG.length());
			AbstractTask nextTask =
			    model.getDependencies().getTask(next);
			currentExplorer.setSelection
			    (new StructuredSelection(nextTask), true);				
		}
	}


	private void showComplete(ICompositeCheatSheetTask task) {
		if (completePanel==null) {
			completePanel = new ScrolledFormText(taskEditorContainer, false);
			mform.getToolkit().adapt(completePanel, false, false);
			FormText text = mform.getToolkit().createFormText(completePanel, true);
			text.marginWidth = 5;
			text.marginHeight = 5;
			text.setFont("header", JFaceResources.getHeaderFont()); //$NON-NLS-1$
			text.setColor("title", mform.getToolkit().getColors().getColor(FormColors.TITLE)); //$NON-NLS-1$		
			completePanel.setFormText(text);
			text.addHyperlinkListener(new CompletionHyperlinkAdapter());
		}
		String desc = task.getCompletionMessage().trim();
		StringBuffer buf = new StringBuffer();
		buf.append("<form>"); //$NON-NLS-1$
		buf.append("<p><span color=\"title\" font=\"header\">"); //$NON-NLS-1$
		buf.append(task.getName());
		buf.append("</span></p>"); //$NON-NLS-1$	
		if (desc.charAt(0)!='<') {
			buf.append("<p>"); //$NON-NLS-1$
			buf.append(desc);
			buf.append("</p>"); //$NON-NLS-1$
		} else {
			buf.append(desc);
	    }
		
		showSuccesorTaskLinks(task, buf);
		
		if (task instanceof IEditableTask) {
	        // Add a link to redisplay the editor
			buf.append("<p/>"); //$NON-NLS-1$
			buf.append("<p><a href=\""); //$NON-NLS-1$
			buf.append(REVIEW_TAG);
			buf.append(task.getId());
			buf.append("\">"); //$NON-NLS-1$
			buf.append(Messages.COMPOSITE_PAGE_REVIEW_TASK);
			buf.append("</a></p>"); //$NON-NLS-1$
		}
				
		buf.append("</form>"); //$NON-NLS-1$
		completePanel.setText(buf.toString());
		completePanel.reflow(true);
		setCurrentEditor(completePanel);
	}

	private void showSuccesorTaskLinks(ICompositeCheatSheetTask task, StringBuffer buf) {
		// Add the links to the next tasks
		ICompositeCheatSheetTask[] successorTasks = new SuccesorTaskFinder(task).getRecommendedSuccessors();
		for (int i = 0; i < successorTasks.length; i++) {
			addSuccessorTask(buf, successorTasks[i]);
		}
	}
	
	private void addSuccessorTask(StringBuffer buf, ICompositeCheatSheetTask task) {
		buf.append("<p/>"); //$NON-NLS-1$
		buf.append("<p><a href=\""); //$NON-NLS-1$
		buf.append(NEXT_TASK_TAG);
		buf.append(task.getId());
		buf.append("\">"); //$NON-NLS-1$	
		buf.append(NLS.bind(Messages.COMPOSITE_PAGE_GOTO_TASK, (new Object[] {task.getName()})));	
		buf.append("</a></p>"); //$NON-NLS-1$
	}

	/*
	 * Get the task editor for this task. If no editor exists create one
	 */
	private TaskEditor getTaskEditor(ICompositeCheatSheetTask task, boolean initialize) {
		if (task instanceof EditableTask) {
			EditableTask editable = (EditableTask)task;
			if (editable.getEditor() == null) {
                // Create a new editor using the extension point data
				TaskEditor editor = TaskEditorManager.getInstance().getEditor(task.getKind());
				if (editor != null) {
					editor.createControl(taskEditorContainer, mform.getToolkit());
					editor.setInput(editable, model.getTaskMemento(task.getId()));
					editable.setEditor(editor);
				} else if (initialize) {
					editor.setInput(editable, model.getTaskMemento(task.getId()));
				}
			}
			return editable.getEditor();
		}
		
		return null;
	}

	public Control getControl() {
		return form;
	}

	protected String getTitle() {
		return model.getName();
	}

	public void initialized() {
		// Open the model
		model.setSaveHelper(saveHelper);
		model.loadState();
		setInputModel(model);
	}

	public int contributeToViewMenu(Menu menu, int index) {	
		index = contributeExplorerItem(menu, index);
		return contributeRestartItem(menu, index);
	}

	private int contributeRestartItem(Menu menu, int index) {
		MenuItem item = new MenuItem(menu, SWT.PUSH, index++);
		item.setText(Messages.RESTART_ALL_MENU);
		item.setImage(CheatSheetPlugin.getPlugin().getImage(ICheatSheetResource.CHEATSHEET_ITEM_BUTTON_RESTART));
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (model != null) {
					if (MessageDialog.openConfirm(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
							Messages.COMPOSITE_RESTART_DIALOG_TITLE, 
							Messages.COMPOSITE_RESTART_CONFIRM_MESSAGE)) {
					    restart(null);	
				    }
				}
			}
		});
		return index;
	}

	private int contributeExplorerItem(Menu menu, int index) {
		String[] explorerIds = CheatSheetRegistryReader.getInstance().getExplorerIds();
		if (explorerIds.length == 1) {
			return index;  // no other explorer to chosse from
		}
        MenuItem menuItem = new MenuItem(menu, SWT.CASCADE, index++);

        menuItem.setText(Messages.EXPLORER_PULLDOWN_MENU);
    
        Menu subMenu = new Menu(menu);
        menuItem.setMenu(subMenu);
        
        for (int i = 0; i < explorerIds.length; i++) {
        	final String id = explorerIds[i];
        	TaskExplorerNode node = CheatSheetRegistryReader.getInstance().findTaskExplorer(id);
        	boolean isCurrentExplorer = id.equals(currentExplorer.getId());
        	int style = isCurrentExplorer ? SWT.RADIO: SWT.PUSH;
        	MenuItem item = new MenuItem(subMenu, style);
    		item.setText(node.getName());
    		item.setSelection(isCurrentExplorer);
    		item.setImage(TaskExplorerManager.getInstance().getImage(id));
    		item.addSelectionListener(new SelectionAdapter() {
    			public void widgetSelected(SelectionEvent e) {
    				setCurrentExplorerFromId(id);
    			}
    		});
        }

		return index;
	}

	public void restart(Map cheatSheetData) {
		model.resetAllTasks(cheatSheetData);
		currentExplorer.setSelection
		    (new StructuredSelection(model.getRootTask()), true);
		
	}

}
