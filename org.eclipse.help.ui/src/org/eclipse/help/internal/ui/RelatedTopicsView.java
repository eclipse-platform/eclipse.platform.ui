package org.eclipse.help.internal.ui;
import org.eclipse.jface.viewers.*;
import org.eclipse.help.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
public class RelatedTopicsView extends ViewPart {
	public final static String ID =
		"org.eclipse.help.internal.ui.RelatedTopicsView";
	ListViewer viewer;
	/*
	 * @see WorkbenchPart#setFocus()
	 */
	public void setFocus() {
	}
	/*
	 * @see WorkbenchPart#createPartControl(Composite)
	 */
	public void createPartControl(Composite parent) {
		Composite contents = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		contents.setLayout(layout);
		contents.setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer = new ListViewer(contents, SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.setContentProvider(TreeContentProvider.getDefault());
		viewer.setLabelProvider(ElementLabelProvider.getDefault());
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			/*
			 * @see ISelectionChangedListener#selectionChanged(SelectionChangedEvent)
			 */
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection sel=event.getSelection();
				if(sel.isEmpty())
					return;
				if(sel instanceof IStructuredSelection){
					Object o=((IStructuredSelection)sel).getFirstElement();
					if(o instanceof IHelpResource){
						IHelpResource t=(IHelpResource)o;
						String href=t.getHref();
						DefaultHelp.getInstance().displayHelp(null, href);
					}
				}
					
			}
		});
		//WorkbenchHelp.setHelp(contents.getControl(),new String[] {IHelpUIConstants.RELATED_TOPICS_VIEWER,});
		// create the pop-up menus in the viewer
		// For now, do this only for win32. 
	}
	/**
	 * Shows the related links, in related topics view
	 */
	public void displayHelp(IHelpResource[] relatedTopics, IHelpResource topic) {
		if(viewer!=null){
			viewer.setInput(null);
			viewer.add(relatedTopics);
			viewer.setSelection(new StructuredSelection(topic), true);
		}
	}
}
