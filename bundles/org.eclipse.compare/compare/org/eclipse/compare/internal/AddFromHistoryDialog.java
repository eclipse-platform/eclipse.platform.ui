package org.eclipse.compare.internal;

import java.io.*;
import java.text.*;
import java.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.compare.*;


public class AddFromHistoryDialog extends org.eclipse.jface.dialogs.Dialog {
	
	static class HistoryInput implements ITypedElement, IStreamContentAccessor, IModificationDate {
		IFile fFile;
		IFileState fFileState;
		
		HistoryInput(IFile file, IFileState fileState) {
			fFile= file;
			fFileState= fileState;
		}
		public InputStream getContents() throws CoreException {
			return new BufferedInputStream(fFileState.getContents());
		}
		public String getName() {
			return fFile.getName();
		}
		public String getType() {
			return fFile.getFileExtension();
		}
		public Image getImage() {
			return CompareUI.getImage(fFile);
		}
		public long getModificationDate() {
			return fFileState.getModificationTime();
		}
	}

	private CompareConfiguration fCompareConfiguration;
	private ResourceBundle fBundle;
	private HistoryInput fSelectedItem;

	// SWT controls
	private CompareViewerSwitchingPane fContentPane;
	private Button fCommitButton;
	private Table fMemberTable;
	private CompareViewerPane fMemberPane;
	private Tree fEditionTree;
	private CompareViewerPane fEditionPane;
	private Image fDateImage;
	private Image fTimeImage;
	private CompareViewerSwitchingPane fStructuredComparePane;


	public AddFromHistoryDialog(Shell parent, ResourceBundle bundle) {
		super(parent);
		setShellStyle(SWT.CLOSE | SWT.APPLICATION_MODAL | SWT.RESIZE);
		
		fBundle= bundle;
					
		String iconName= Utilities.getString(fBundle, "dateIcon", "obj16/day_obj.gif"); //$NON-NLS-2$ //$NON-NLS-1$
		ImageDescriptor id= CompareUIPlugin.getImageDescriptor(iconName);
		if (id != null)
			fDateImage= id.createImage();
		iconName= Utilities.getString(fBundle, "timeIcon", "obj16/resource_obj.gif"); //$NON-NLS-1$ //$NON-NLS-2$
		id= CompareUIPlugin.getImageDescriptor(iconName);
		if (id != null)
			fTimeImage= id.createImage();
	}
	
	public boolean select(IContainer root, IFile[] files) {
		
		create();	// create widgets
		
		String format= Utilities.getString(fBundle, "memberPaneTitle");	//$NON-NLS-1$
		String title= MessageFormat.format(format, new Object[] { root.getName() });
		fMemberPane.setImage(CompareUI.getImage(root));
		fMemberPane.setText(title);
		
		String prefix= root.getFullPath().toString();
		
		if (fMemberTable != null && !fMemberTable.isDisposed()) {
			for (int i= 0; i < files.length; i++) {
				IFile file= files[i];
				String path= file.getFullPath().toString();
				if (path.startsWith(prefix))
					path= path.substring(prefix.length()+1);
				TableItem ti= new TableItem(fMemberTable, SWT.NONE);
				ti.setImage(CompareUI.getImage(file));
				ti.setText(path);
				ti.setData(file);
			}
		}
		
		open();
		
		return (getReturnCode() == OK) && (fSelectedItem != null);
	}
	
	IFile getSelectedFile() {
		if (fSelectedItem != null)
			return fSelectedItem.fFile;
		return null;
	}
			
	IFileState getSelectedFileState() {
		if (fSelectedItem != null)
			return fSelectedItem.fFileState;
		return null;
	}
			
	protected synchronized Control createDialogArea(Composite parent) {
		
		getShell().setText(Utilities.getString(fBundle, "title")); //$NON-NLS-1$
		
		Splitter vsplitter= new Splitter(parent,  SWT.VERTICAL);
		vsplitter.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL
					| GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_VERTICAL));

		vsplitter.addDisposeListener(
			new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					if (fDateImage != null)
						fDateImage.dispose();
					if (fTimeImage != null)
						fTimeImage.dispose();
				}
			}
		);
		
		// we need two panes: the left for the elements, the right one for the editions
		Splitter hsplitter= new Splitter(vsplitter,  SWT.HORIZONTAL);
		
		fMemberPane= new CompareViewerPane(hsplitter, SWT.BORDER | SWT.FLAT);
		fMemberTable= new Table(fMemberPane, SWT.H_SCROLL + SWT.V_SCROLL);
		fMemberTable.addSelectionListener(
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					handleMemberSelect(e.item);
				}
			}
		);
		
		fMemberPane.setContent(fMemberTable);
		
		fEditionPane= new CompareViewerPane(hsplitter, SWT.BORDER | SWT.FLAT);
		
		fEditionTree= new Tree(fEditionPane, SWT.H_SCROLL + SWT.V_SCROLL);
		fEditionTree.addSelectionListener(
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					feedContent(e.item);
				}
			}
		);
		fEditionPane.setContent(fEditionTree);		
		
		fContentPane= new CompareViewerSwitchingPane(vsplitter, SWT.BORDER | SWT.FLAT) {
			protected Viewer getViewer(Viewer oldViewer, Object input) {
				return CompareUIPlugin.findContentViewer(oldViewer, input, this, fCompareConfiguration);	
			}
		};
		vsplitter.setWeights(new int[] { 30, 70 });
				
		return vsplitter;
	}
	
	/**
	 * Feeds selection from member viewer to edition viewer.
	 */
	private void handleMemberSelect(Widget w) {
		Object data= w.getData();
		if (data instanceof IFile) {
			IFile file= (IFile) data;
			IFileState[] states= null;
			try {
				states= file.getHistory(new NullProgressMonitor());
			} catch (CoreException ex) {
			}
			
			fEditionPane.setImage(CompareUI.getImage(file));
			String pattern= Utilities.getString(fBundle, "treeTitleFormat"); //$NON-NLS-1$
			String title= MessageFormat.format(pattern, new Object[] { file.getName() });
			fEditionPane.setText(title);
			
			if (fEditionTree != null) {
				fEditionTree.removeAll();
				for (int i= 0; i < states.length; i++) {
					addEdition(new HistoryInput(file, states[i]));
				}
			}
		}
	}
	
	/**
	 * Adds the given Pair to the edition tree.
	 * It takes care of creating tree nodes for different dates.
	 */
	private void addEdition(HistoryInput input) {
		if (fEditionTree == null || fEditionTree.isDisposed())
			return;
		
		IFileState state= input.fFileState;
		
		// find last day
		TreeItem[] days= fEditionTree.getItems();
		TreeItem lastDay= null;
		if (days.length > 0)
			lastDay= days[days.length-1];
		
		boolean first= lastDay == null;
				
		long ldate= state.getModificationTime();		
		long day= dayNumber(ldate);
		Date date= new Date(ldate);
		if (lastDay == null || day != dayNumber(((Date)lastDay.getData()).getTime())) {
			lastDay= new TreeItem(fEditionTree, SWT.NONE);
			lastDay.setImage(fDateImage);
			String df= DateFormat.getDateInstance().format(date);
			long today= dayNumber(System.currentTimeMillis());
			
			String formatKey;
			if (day == today)
				formatKey= "todayFormat"; //$NON-NLS-1$
			else if (day == today-1)
				formatKey= "yesterdayFormat"; //$NON-NLS-1$
			else
				formatKey= "dayFormat"; //$NON-NLS-1$
			String pattern= Utilities.getString(fBundle, formatKey);
			if (pattern != null)
				df= MessageFormat.format(pattern, new String[] { df });
			lastDay.setText(df);
			lastDay.setData(date);
		}
		TreeItem ti= new TreeItem(lastDay, SWT.NONE);
		ti.setImage(fTimeImage);
		ti.setText(DateFormat.getTimeInstance().format(date));
		ti.setData(input);
		
		if (first) {
			lastDay.setExpanded(true);
			fEditionTree.setSelection(new TreeItem[] { ti });
			feedContent(ti);
		}
	}
						
	/**
	 * Returns the number of s since Jan 1st, 1970.
	 * The given date is converted to GMT and daylight saving is taken into account too.
	 */
	private long dayNumber(long date) {
		int ONE_DAY_MS= 24*60*60 * 1000; // one day in milli seconds
		
		Calendar calendar= Calendar.getInstance();
		long localTimeOffset= calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET);
		
		return (date + localTimeOffset) / ONE_DAY_MS;
	}
		
	private void feedContent(Widget w) {
		if (fContentPane != null && !fContentPane.isDisposed()) {
			Object o= w.getData();
			if (o instanceof HistoryInput) {
				fSelectedItem= (HistoryInput) o;
				fContentPane.setInput(fSelectedItem);
				fContentPane.setText(getEditionLabel(fSelectedItem));
				fContentPane.setImage(fTimeImage);
			} else {
				fSelectedItem= null;
				fContentPane.setInput(null);
			}
		}
		if (fCommitButton != null)
			fCommitButton.setEnabled(fSelectedItem != null);
	}
	
	protected String getEditionLabel(HistoryInput input) {
		String format= Utilities.getString(fBundle, "historyEditionLabel", null);	//$NON-NLS-1$
		if (format == null)
			format= Utilities.getString(fBundle, "editionLabel");	//$NON-NLS-1$
		if (format == null)
			format= "x{0}";	//$NON-NLS-1$
		
		long modDate= input.getModificationDate();
		String date= DateFormat.getDateTimeInstance().format(new Date(modDate));
		
		return MessageFormat.format(format, new Object[] { date });
	}
		
	/* (non Javadoc)
	 * Returns the size initialized with the constructor.
	 */
	protected Point getInitialSize() {
		Point size= new Point(Utilities.getInteger(fBundle, "width", 0), //$NON-NLS-1$
					Utilities.getInteger(fBundle, "height", 0)); //$NON-NLS-1$
		
		Shell shell= getParentShell();
		if (shell != null) {
			Point parentSize= shell.getSize();
			if (size.x <= 0)
				size.x= parentSize.x-300;
			if (size.y <= 0)
				size.y= parentSize.y-200;
		}
		if (size.x < 700)
			size.x= 700;
		if (size.y < 500)
			size.y= 500;
		return size;
	}
	
	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		String buttonLabel= Utilities.getString(fBundle, "buttonLabel", IDialogConstants.OK_LABEL); //$NON-NLS-1$
		// a 'Cancel' and a 'Add' button
		fCommitButton= createButton(parent, IDialogConstants.OK_ID, buttonLabel, true);
		fCommitButton.setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
}
