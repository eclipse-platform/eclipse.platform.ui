/*
 * Created on Jun 2, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.ui.views.progress;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.*;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.internal.progress.JobInfoWithProgress;

/**
 * @author tod
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ProgressTreeViewer extends TreeViewer {

	/**
	 * Create an instance of the receiver.
	 * @param parent
	 */
	public ProgressTreeViewer(Composite parent) {
		super(parent);
	}

	/**
	 * Create an instance of the receiver.
	 * @param parent
	 * @param style
	 */
	public ProgressTreeViewer(Composite parent, int style) {
		super(parent, style);
	}

	/**
	 * Create an instance of the receiver.
	 * @param tree
	 */
	public ProgressTreeViewer(Tree tree) {
		super(tree);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#doUpdateItem(org.eclipse.swt.widgets.Item, java.lang.Object)
	 */
	protected void doUpdateItem(Item item, Object element) {
		super.doUpdateItem(item, element);
		
		final TreeItem finalItem = (TreeItem) item;
		if(!(element instanceof JobInfoWithProgress))	
			return;

		JobInfoWithProgress job = (JobInfoWithProgress) element;
		Tree tree = getTree();
		final TreeEditor editor = new TreeEditor(tree);

		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		editor.minimumWidth = 50;

		final Composite editorComposite = new Composite(getTree(), SWT.NONE);
		editorComposite.setBackground(getTree().getBackground());
		FormLayout layout = new FormLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		editorComposite.setLayout(layout);

		final Label displayLabel = new Label(editorComposite, SWT.NONE);
		displayLabel.setBackground(tree.getBackground());
		displayLabel.setText(item.getText());

		FormData data = new FormData();
		data.top = new FormAttachment(0, 0);
		data.left = new FormAttachment(0, 0);
		data.bottom = new FormAttachment(100, 0);
		displayLabel.setLayoutData(data);

		final ProgressIndicator indicator =
			new ProgressIndicator(editorComposite);
		indicator.setBackground(getTree().getBackground());
		job.setProgressIndicator(indicator);

		data = new FormData();
		data.top = new FormAttachment(0, 0);
		data.left = new FormAttachment(displayLabel);
		data.right = new FormAttachment(100, 0);
		data.bottom = new FormAttachment(100, 0);

		indicator.setLayoutData(data);

		final FocusListener listener = new FocusListener() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
			 */
			public void focusGained(FocusEvent e) {
				displayLabel.setText(finalItem.getText());
				editor.setEditor(editorComposite, finalItem);
			}
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
			 */
			public void focusLost(FocusEvent e) {
			}

		};
		getTree().addFocusListener(listener);

		item.addDisposeListener(new DisposeListener() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
			 */
			public void widgetDisposed(DisposeEvent e) {
				getTree().removeFocusListener(listener);
				indicator.done();
				editor.dispose();
				indicator.dispose();
				displayLabel.dispose();
			}

		});
	}

}
