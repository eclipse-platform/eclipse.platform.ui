package org.eclipse.ui.examples.readmetool;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;

/**
 * This class implements the Readme editor.  Since the readme
 * editor is mostly just a text editor, there is very little
 * implemented in this actual class.  It can be regarded as
 * simply decorating the text editor with a content outline.
 */
public class ReadmeEditor extends TextEditor {
	protected ReadmeContentOutlinePage page;
	
	private final static String ATT_1 = IReadmeConstants.PREFIX + "attribute1"; //$NON-NLS-1$
	private final static String ATT_2 = IReadmeConstants.PREFIX + "attribute2"; //$NON-NLS-1$
	private final static String ATT_3 = IReadmeConstants.PREFIX + "attribute3"; //$NON-NLS-1$
	private final static String ATT_4 = IReadmeConstants.PREFIX + "attribute4"; //$NON-NLS-1$
	private final static String ATT_5 = IReadmeConstants.PREFIX + "attribute5"; //$NON-NLS-1$
	
	/**
	 * Creates a new ReadmeEditor.
	 */
	public ReadmeEditor() {
		super();
	}
	/** (non-Javadoc)
	 * Method declared on IEditorPart
	 */
	public void doSave(IProgressMonitor monitor) {
		super.doSave(monitor);
		if (page != null)
			page.update();
	}
	/** (non-Javadoc)
	 * Method declared on IAdaptable
	 */
	public Object getAdapter(Class key) {
		if (key.equals(IContentOutlinePage.class)) {
			IEditorInput input = getEditorInput();
			if (input instanceof IFileEditorInput) {
				page = new ReadmeContentOutlinePage(((IFileEditorInput)input).getFile());
				return page;
			}
		}
		return super.getAdapter(key);
	}
	
	/** (non-Javadoc)
	 * Method declared on AbstractTextEditor
	 */
	protected void editorContextMenuAboutToShow(IMenuManager parentMenu) {
		super.editorContextMenuAboutToShow(parentMenu);
		parentMenu.add(new Separator());
		IMenuManager subMenu = new MenuManager(MessageUtil.getString("Add"));
		parentMenu.add(subMenu);
		if (subMenu != null) {
			// Add readme actions with various attributes
			Object[][] att = new Object[][] {{IReadmeConstants.MARKER_ATT_ID, new Integer(1234)}}; 
			subMenu.add(new AddReadmeMarkerAction(this, 
				MessageUtil.getString("Add_readme_marker_action_label") + "1", //$NON-NLS-1$ //$NON-NLS-2$
				att, 
				MessageUtil.getString("Readme_marker_message_example") + " id=1234")); //$NON-NLS-1$ //$NON-NLS-2$

			att = new Object[][] {{IReadmeConstants.MARKER_ATT_LEVEL, new Integer(7)}}; 
			subMenu.add(new AddReadmeMarkerAction(this, 
				MessageUtil.getString("Add_readme_marker_action_label") + "2", //$NON-NLS-1$ //$NON-NLS-2$
				att, 
				MessageUtil.getString("Readme_marker_message_example") + " level=7")); //$NON-NLS-1$ //$NON-NLS-2$

			att = new Object[][] {{IReadmeConstants.MARKER_ATT_LEVEL, new Integer(7)},
					 {IReadmeConstants.MARKER_ATT_DEPT, "infra"}}; //$NON-NLS-1$
			subMenu.add(new AddReadmeMarkerAction(this, 
				MessageUtil.getString("Add_readme_marker_action_label") + "3", //$NON-NLS-1$ //$NON-NLS-2$
				att, 
				MessageUtil.getString("Readme_marker_message_example") + " level=7, department=infra")); //$NON-NLS-1$ //$NON-NLS-2$

			att = new Object[][] {{IReadmeConstants.MARKER_ATT_CODE, "red"}}; //$NON-NLS-1$
			subMenu.add(new AddReadmeMarkerAction(this, 
				MessageUtil.getString("Add_readme_marker_action_label") + "4", //$NON-NLS-1$ //$NON-NLS-2$
				att, 
				MessageUtil.getString("Readme_marker_message_example") + " code=red")); //$NON-NLS-1$ //$NON-NLS-2$

			att = new Object[][] {{IReadmeConstants.MARKER_ATT_LANG, "english"}}; //$NON-NLS-1$
			subMenu.add(new AddReadmeMarkerAction(this, 
				MessageUtil.getString("Add_readme_marker_action_label") + "5", //$NON-NLS-1$ //$NON-NLS-2$
				att, 
				MessageUtil.getString("Readme_marker_message_example") + " language=english")); //$NON-NLS-1$ //$NON-NLS-2$

			att = new Object[][] {{IReadmeConstants.MARKER_ATT_ID, new Integer(1234)},
					{IReadmeConstants.MARKER_ATT_LEVEL, new Integer(7)},
					{IReadmeConstants.MARKER_ATT_DEPT, "infra"}, //$NON-NLS-1$
					{IReadmeConstants.MARKER_ATT_CODE, "red"}, //$NON-NLS-1$
					{IReadmeConstants.MARKER_ATT_LANG, "english"}}; //$NON-NLS-1$
			subMenu.add(new AddReadmeMarkerAction(this, 
				MessageUtil.getString("Add_readme_marker_action_label") + "6", //$NON-NLS-1$ //$NON-NLS-2$
				att, 
				MessageUtil.getString("Readme_marker_message_example") + //$NON-NLS-1$
				" id=1234, level=7, department=infra, code=red, language=english")); //$NON-NLS-1$

			att = new Object[0][0];
			subMenu.add(new AddReadmeMarkerAction(this, 
				MessageUtil.getString("Add_readme_marker_action_label") + "7", //$NON-NLS-1$ //$NON-NLS-2$
				att, 
				MessageUtil.getString("Readme_marker_message_example") + " No attributes specified")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

}
