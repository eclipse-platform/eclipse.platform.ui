package org.eclipse.ui.examples.readmetool;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.LabelRetargetAction;
import org.eclipse.ui.actions.RetargetAction;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.eclipse.jface.action.*;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.texteditor.BasicTextEditorActionContributor;

/**
 * This class demonstrates action contribution for the readme editor.
 * A number of menu, toolbar, and status line contributions are defined
 * in the workbench window.  These actions are shared among all 
 * readme editors, and are only visible when a readme editor is 
 * active.  Otherwise, they are invisible.
 */
public class ReadmeEditorActionBarContributor extends BasicTextEditorActionContributor 
{
	private EditorAction action1;
	private RetargetAction action2;
	private LabelRetargetAction action3;
	private EditorAction handler2;
	private EditorAction handler3;
	private DirtyStateContribution dirtyStateContribution;
	
	class EditorAction extends Action {
		private Shell shell;
		private IEditorPart activeEditor;
		public EditorAction(String label) {
			super(label);
		}
		public void setShell(Shell shell) {
			this.shell = shell;
		}
		public void run() {
			String editorName = MessageUtil.getString("Empty_Editor_Name"); //$NON-NLS-1$
			if (activeEditor != null)
				editorName = activeEditor.getTitle();
			MessageDialog.openInformation(shell,
				MessageUtil.getString("Readme_Editor"),  //$NON-NLS-1$
				MessageUtil.format("ReadmeEditorActionExecuted", new Object[] {getText(), editorName})); //$NON-NLS-1$
		}
		public void setActiveEditor(IEditorPart part) {
			activeEditor = part;
		}
	}
/**
 * Creates a new ReadmeEditorActionBarContributor.
 */
public ReadmeEditorActionBarContributor() {
	ImageRegistry registry = ReadmePlugin.getDefault().getImageRegistry();
	action1 = new EditorAction(MessageUtil.getString("Editor_Action1")); //$NON-NLS-1$
	action1.setToolTipText(MessageUtil.getString("Readme_Editor_Action1")); //$NON-NLS-1$
	action1.setDisabledImageDescriptor(ReadmeImages.EDITOR_ACTION1_IMAGE_DISABLE);
	action1.setImageDescriptor(ReadmeImages.EDITOR_ACTION1_IMAGE_ENABLE);
	action1.setHoverImageDescriptor(ReadmeImages.EDITOR_ACTION1_IMAGE);
	WorkbenchHelp.setHelp(action1, IReadmeConstants.EDITOR_ACTION1_CONTEXT);
	
	action2 = new RetargetAction(IReadmeConstants.RETARGET2, MessageUtil.getString("Editor_Action2")); //$NON-NLS-1$
	action2.setToolTipText(MessageUtil.getString("Readme_Editor_Action2")); //$NON-NLS-1$
	action2.setDisabledImageDescriptor(ReadmeImages.EDITOR_ACTION2_IMAGE_DISABLE);
	action2.setImageDescriptor(ReadmeImages.EDITOR_ACTION2_IMAGE_ENABLE);
	action2.setHoverImageDescriptor(ReadmeImages.EDITOR_ACTION2_IMAGE);

	action3 = new LabelRetargetAction(IReadmeConstants.LABELRETARGET3, MessageUtil.getString("Editor_Action3")); //$NON-NLS-1$
	action3.setDisabledImageDescriptor(ReadmeImages.EDITOR_ACTION3_IMAGE_DISABLE);
	action3.setImageDescriptor(ReadmeImages.EDITOR_ACTION3_IMAGE_ENABLE);
	action3.setHoverImageDescriptor(ReadmeImages.EDITOR_ACTION3_IMAGE);

	handler2 = new EditorAction(MessageUtil.getString("Editor_Action2")); //$NON-NLS-1$
	WorkbenchHelp.setHelp(action2, IReadmeConstants.EDITOR_ACTION2_CONTEXT);

	handler3 = new EditorAction(MessageUtil.getString("Editor_Action3")); //$NON-NLS-1$
	handler3.setToolTipText(MessageUtil.getString("Readme_Editor_Action3")); //$NON-NLS-1$
	WorkbenchHelp.setHelp(action3, IReadmeConstants.EDITOR_ACTION3_CONTEXT);

	dirtyStateContribution = new DirtyStateContribution();
}
/** (non-Javadoc)
 * Method declared on EditorActionBarContributor
 */
public void contributeToMenu(IMenuManager menuManager) {
	// Run super.
	super.contributeToMenu(menuManager);
	
	// Editor-specitic menu
	MenuManager readmeMenu = new MenuManager(MessageUtil.getString("Readme_Menu")); //$NON-NLS-1$
	// It is important to append the menu to the
	// group "additions". This group is created
	// between "Project" and "Tools" menus
	// for this purpose.
	menuManager.insertAfter("additions", readmeMenu); //$NON-NLS-1$
	readmeMenu.add(action1);
	readmeMenu.add(action2);
	readmeMenu.add(action3);
}
/** (non-Javadoc)
 * Method declared on EditorActionBarContributor
 */
public void contributeToStatusLine(IStatusLineManager statusLineManager) {
	// Run super.
	super.contributeToStatusLine(statusLineManager);
	// Test status line.	
	statusLineManager.setMessage(MessageUtil.getString("Editor_is_active")); //$NON-NLS-1$
	statusLineManager.add(dirtyStateContribution);
}
	
/** (non-Javadoc)
 * Method declared on EditorActionBarContributor
 */
public void contributeToToolBar(IToolBarManager toolBarManager) {
	// Run super.
	super.contributeToToolBar(toolBarManager);
	
	// Add toolbar stuff.
	toolBarManager.add(new Separator("ReadmeEditor")); //$NON-NLS-1$
	toolBarManager.add(action1);
	toolBarManager.add(action2);
	toolBarManager.add(action3);
}
/** (non-Javadoc)
 * Method declared on IEditorActionBarContributor
 */
public void init(IActionBars bars) {
	super.init(bars);
	bars.setGlobalActionHandler(IReadmeConstants.RETARGET2, handler2);
	bars.setGlobalActionHandler(IReadmeConstants.LABELRETARGET3, handler3);
}

/** (non-Javadoc)
 * Method declared on IEditorActionBarContributor
 */
public void setActiveEditor(IEditorPart editor) {
	// Run super.
	super.setActiveEditor(editor);
	
	// Ensure retarget actions are page listeners
	IWorkbenchPage page = editor.getSite().getPage();	
	page.addPartListener(action2);
	page.addPartListener(action3);

	// Target shared actions to new editor
	action1.setActiveEditor(editor);
	handler2.setActiveEditor(editor);
	handler3.setActiveEditor(editor);
	dirtyStateContribution.editorChanged(editor);
}
}
