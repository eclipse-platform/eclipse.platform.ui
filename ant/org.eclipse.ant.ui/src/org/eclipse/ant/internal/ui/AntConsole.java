package org.eclipse.ant.internal.ui;import java.net.*;import org.eclipse.core.internal.runtime.Assert;import org.eclipse.jface.action.*;import org.eclipse.jface.resource.ImageDescriptor;import org.eclipse.jface.text.*;import org.eclipse.swt.SWT;import org.eclipse.swt.layout.*;import org.eclipse.swt.widgets.*;import org.eclipse.ui.part.ViewPart;
public class AntConsole extends ViewPart {	public final static String CONSOLE_ID = "org.eclipse.ant.ui.antconsole";		private TextViewer viewer;	private Action copyAction;	private Action selectAllAction;

	/**
	 * Constructor for AntConsole
	 */
	public AntConsole() {
		super();
	}
	protected void addContributions() {		// Create the actions.		copyAction = new Action("&Copy") {			public void run() {				copySelectionToClipboard();			}		};		copyAction.setImageDescriptor(getImageDescriptor("icons/copy_edit.gif"));				selectAllAction = new Action("Select A&ll") {			public void run() {				selectAllText();			}		};				MenuManager mgr = new MenuManager();		mgr.setRemoveAllWhenShown(true);		mgr.addMenuListener(new IMenuListener() {			public void menuAboutToShow(IMenuManager mgr) {				fillContextMenu(mgr);			}		});		Menu menu = mgr.createContextMenu(viewer.getControl());		viewer.getControl().setMenu(menu);	}	public void append(final String value) {		getViewSite().getShell().getDisplay().syncExec(new Runnable() {			public void run() {				viewer.setDocument(new Document(value));			}		});	}		protected void copySelectionToClipboard() {		viewer.doOperation(viewer.COPY);	}		protected void fillContextMenu(IMenuManager manager) {		copyAction.setEnabled(viewer.canDoOperation(viewer.COPY));		selectAllAction.setEnabled(viewer.canDoOperation(viewer.SELECT_ALL));		manager.add(copyAction);		manager.add(selectAllAction);	}	protected ImageDescriptor getImageDescriptor(String relativePath) {		try {			URL installURL = AntUIPlugin.getPlugin().getDescriptor().getInstallURL();			URL url = new URL(installURL,relativePath);			return ImageDescriptor.createFromURL(url);		}		catch (MalformedURLException e) {			Assert.isTrue(false);			return null;		}	}	protected void selectAllText() {		viewer.doOperation(viewer.SELECT_ALL);	}	
	/**
	 * @see WorkbenchPart#setFocus()
	 */
	public void setFocus() {
	}

	/**
	 * @see WorkbenchPart#createPartControl(Composite)
	 */
	public void createPartControl(Composite parent) {		Composite composite = new Composite(parent, SWT.NULL);		composite.setLayout(new GridLayout());		composite.setLayoutData(new GridData(			GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));		viewer = new TextViewer(composite,SWT.WRAP | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);		GridData data = new GridData(GridData.FILL_BOTH);		viewer.setEditable(false);		viewer.getControl().setLayoutData(data);		addContributions();	}

}
