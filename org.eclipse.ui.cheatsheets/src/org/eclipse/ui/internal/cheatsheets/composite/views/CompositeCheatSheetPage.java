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

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
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
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.HyperlinkSettings;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.internal.cheatsheets.CheatSheetPlugin;
import org.eclipse.ui.internal.cheatsheets.ICheatSheetResource;
import org.eclipse.ui.internal.cheatsheets.Messages;
import org.eclipse.ui.internal.cheatsheets.actions.IMenuContributor;
import org.eclipse.ui.internal.cheatsheets.composite.model.AbstractTask;
import org.eclipse.ui.internal.cheatsheets.composite.model.CompositeCheatSheetModel;
import org.eclipse.ui.internal.cheatsheets.composite.model.CompositeCheatSheetSaveHelper;
import org.eclipse.ui.internal.cheatsheets.composite.model.EditableTask;
import org.eclipse.ui.internal.cheatsheets.composite.parser.ICompositeCheatsheetTags;
import org.eclipse.ui.internal.cheatsheets.registry.CheatSheetRegistryReader;
import org.eclipse.ui.internal.cheatsheets.registry.CheatSheetRegistryReader.TaskExplorerNode;
import org.eclipse.ui.internal.cheatsheets.views.Page;
import org.eclipse.ui.internal.provisional.cheatsheets.ICompositeCheatSheetTask;
import org.eclipse.ui.internal.provisional.cheatsheets.IEditableTask;
import org.eclipse.ui.internal.provisional.cheatsheets.TaskEditor;
import org.eclipse.ui.internal.provisional.cheatsheets.TaskExplorer;
import org.eclipse.ui.part.PageBook;

/**
 * A page which represents a composite cheat sheet
 */

public class CompositeCheatSheetPage extends Page implements ISelectionChangedListener, IMenuContributor {

	public static final String REVIEW_TAG = "__review__"; //$NON-NLS-1$
	public static final String GOTO_TASK_TAG = "Goto:"; //$NON-NLS-1$
	public static final String START_HREF = "__start__"; //$NON-NLS-1$
	public static final String SKIP_HREF = "__skip__"; //$NON-NLS-1$
	
	private ManagedForm mform;
	private PageBook explorerContainer;
	private PageBook taskEditorContainer;
	private CompositeCheatSheetModel model;
	private TaskExplorer currentExplorer;
	private DescriptionPanel descriptionPanel;
	
	private CompositeCheatSheetSaveHelper saveHelper;
	
	private ICompositeCheatSheetTask selectedTask;
	
	public CompositeCheatSheetPage(CompositeCheatSheetModel model) {
		this.model = model;
		saveHelper = new CompositeCheatSheetSaveHelper();
	}

	public void createPart(Composite parent) {
		init(parent.getDisplay());
		toolkit.getHyperlinkGroup().setHyperlinkUnderlineMode(HyperlinkSettings.UNDERLINE_HOVER);
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

	private void setInputModel(CompositeCheatSheetModel model, Map layout) {
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
		String selectedTaskId = (String) layout.get(ICompositeCheatsheetTags.SELECTED_TASK);
		if (selectedTaskId != null) {
			AbstractTask selectedTask =
			    model.getDependencies().getTask(selectedTaskId);
			if (selectedTask != null)  {
				currentExplorer.setSelection(new StructuredSelection(selectedTask), true);
			}
		}
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

	/*
	 * Update can be called as a result of a selection  change or a state change
	 */
	private void updateTask(ICompositeCheatSheetTask task) {
		if (task==null) return;
		if (task.getState() == ICompositeCheatSheetTask.IN_PROGRESS && 
				task instanceof IEditableTask) {
		    showTaskEditor((EditableTask) task);
		} else {
			showDescription(task);
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
		Map layout = new HashMap();
		if (selectedTask != null) {
		    layout.put(ICompositeCheatsheetTags.SELECTED_TASK, selectedTask.getId());
		}
		saveHelper.saveCompositeState(model, layout);	
	}

	private void showDescription(final ICompositeCheatSheetTask task) {
		if (descriptionPanel==null) {
			createDescriptionPanel();
		}
		descriptionPanel.showDescription(task);
		setCurrentEditor(descriptionPanel.getControl());
	}

	private void createDescriptionPanel() {
		descriptionPanel = new DescriptionPanel(mform, taskEditorContainer);
		descriptionPanel.addHyperlinkListener(new DescriptionLinkListener());
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
	
	/**
	 * Class which responds to hyperlink events originating from the
	 * description panel.
	 */
	private final class DescriptionLinkListener extends HyperlinkAdapter {
		public void linkActivated(HyperlinkEvent e) {
			String ref = (String)e.getHref();
			if (ref.equals(START_HREF)) {
				Object data = descriptionPanel.getControl().getData(ICompositeCheatsheetTags.TASK);
				if (data instanceof EditableTask) {
				    EditableTask task = (EditableTask)data;
				    task.setStarted();
				    startIfSelected(task, true);
				}
			}     
            if (ref.equals(SKIP_HREF)) {
				Object data = descriptionPanel.getControl().getData(ICompositeCheatsheetTags.TASK);
				if (data instanceof AbstractTask) {
				    AbstractTask task = (AbstractTask)data;
				    task.setState(ICompositeCheatSheetTask.SKIPPED);
				}
			}
			if (ref.equals(REVIEW_TAG)) {
				Object data = descriptionPanel.getControl().getData(ICompositeCheatsheetTags.TASK);
				if (data instanceof EditableTask) {
				    showTaskEditor((EditableTask) data);
				}
			}
			if (ref.startsWith(GOTO_TASK_TAG)) {
				String next = ref.substring(GOTO_TASK_TAG.length());
				AbstractTask nextTask =
				    model.getDependencies().getTask(next);
				currentExplorer.setSelection
				    (new StructuredSelection(nextTask), true);				
			}
		}
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
		Map layout = new HashMap();
		model.loadState(layout);
		setInputModel(model, layout);
	}

	public int contributeToViewMenu(Menu menu, int index) {	
		index = contributeExplorerMenuItem(menu, index);
		return contributeRestartMenuItem(menu, index);
	}

	private int contributeRestartMenuItem(Menu menu, int index) {
		MenuItem item = new MenuItem(menu, SWT.PUSH, index++);
		item.setText(Messages.RESTART_ALL_MENU);
		item.setImage(CheatSheetPlugin.getPlugin().getImage(ICheatSheetResource.COMPOSITE_RESTART_ALL));
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

	private int contributeExplorerMenuItem(Menu menu, int index) {
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
