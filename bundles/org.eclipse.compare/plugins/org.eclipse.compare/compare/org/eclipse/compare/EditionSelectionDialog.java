/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 */
package org.eclipse.compare;

import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashSet;
import java.text.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Button;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.dialogs.IDialogConstants;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;

import org.eclipse.compare.internal.*;
import org.eclipse.compare.structuremergeviewer.*;

/**
 * A dialog where one input element can be compared against
 * a list of historic variants (editions) of the same input element.
 * The dialog can be used to implement functions like "Replace with Version" or
 * "Replace with Edition" on workbench resources.
 * <p>
 * In addition it is possible to specify a subsection of the input element
 * (e.g. a method in a Java source file) by means of a "path".
 * In this case the dialog compares only the subsection (as specified by the path)
 * with the corresponding subsection in the list of editions.
 * Only those editions are shown where the subsection differs from the same subsection in
 * another edition thereby minimizing the number of presented variants.
 * This functionality can be used to implement "Replace with Method Edition"
 * for the Java language.
 * <p>
 * Subsections of an input element are determined by first finding an
 * <code>IStructureCreator</code> for the input's type.
 * Then the method <code>locate</code> is used to extract the subsection.
 * <p>
 * Each edition (variant in the list of variants) must implement the <code>IModificationDate</code> interface
 * so that the dialog can sort the editions and present them in a tree structure where every
 * node corresponds one day.
 * <p>
 * The functionality is surfaced in a single function <code>selectEdition</code>.
 * <p>
 * Clients may instantiate this class; it is not intended to be subclassed.
 * </p>
 *
 * @see IModificationDate
 * @see ITypedElement
 */
public class EditionSelectionDialog extends Dialog {
	
	private static class Pair {
		
		private ITypedElement fEdition;
		private ITypedElement fItem;
		private String fContent;
				
		Pair(ITypedElement edition, ITypedElement item, String content) {
			fEdition= edition;
			fItem= item;
			fContent= content;
		}
		
		ITypedElement getEdition() {
			return fEdition;
		}

		ITypedElement getItem() {
			return fItem;
		}
						
		public boolean equals(Object other) {
			if (other != null && other.getClass() == getClass()) {
				String otherContent= ((Pair)other).fContent;
				if (otherContent != null && fContent != null && fContent.equals(otherContent))
					return true;
			}
			return super.equals(other);
		}
	}
	
	private static final int ONE_DAY_MS= 86400 * 1000; // one day in milli seconds
	private static final boolean HIDE_IDENTICAL= true;
	
	private Button fCommitButton;
	private boolean fReplaceMode= true;
	
	private ResourceBundle fBundle;
	private boolean fTargetIsRight;
	
	/**
	 * Maps from members to their corresponding editions.
	 * Has only a single entry if dialog is used in "Replace" (and not "Add") mode.
	 */
	private HashMap fMemberEditions;
	/** The editions of the current selected member */
	private List fCurrentEditions;
	private Thread fThread;

	private ITypedElement fTargetItem;
	/** The selected edition in the edition viewer */
	private ITypedElement fSelectedItem;
	
	private Table fMemberTable;
	private Pane fMemberPane;
	
	private Tree fEditionTree;
	private Pane fEditionPane;
	private Image fDateImage;
	private Image fTimeImage;
	
	private CompareViewerSwitchingPane fContentPane;
	private CompareConfiguration fCompareConfiguration;
	
	/**
	 * Creates a new modal, resizable dialog.
	 * Various titles, icons, and labels are configured from the given resource bundle.
	 * The following resource keys are used:
	 * <pre>
	 *	key         type          description
	 *	title       String        dialog title
	 *	width       Integer       initial width of dialog
	 *	height      Integer       initial height of dialog
	 *	mode        String        "replace" or "add"; default is "replace"
	 * 	targetSide  String	      whether target is on "right" or "left" side; default is "right"
	 *	treeTitleFormat   MessageFormat pane title for edition tree; arg 0 is the target
	 *	dateIcon    String        icon for node in edition tree; path relative to plugin
	 *	timeIcon    String        icon for leaf in edition tree; path relative to plugin
	 *	todayFormat MessageFormat format string if date is todays date; arg 0 is date
	 *	yesterdayFormat MessageFormat format string if date is yesterdays date; arg 0 is date
	 *	dayFormat   MessageFormat format string if date is any other date; arg 0 is date
	 *	editionLabel String        label for editions side of compare viewer; arg 0 is the date
	 *	targetLabel  String        label for target side of compare viewer; arg 0 is 
	 * </pre>
	 *
	 * @param parent if not <code>null</code> the new dialog stays on top of this parent shell
	 * @param bundle <code>ResourceBundle</code> to configure the dialog
	 */
	public EditionSelectionDialog(Shell parent, ResourceBundle bundle) {
		super(parent);
		setShellStyle(SWT.CLOSE | SWT.APPLICATION_MODAL | SWT.RESIZE);
		
		fBundle= bundle;
	
		fTargetIsRight= "right".equals(Utilities.getString(fBundle, "targetSide", "right"));
		fReplaceMode= "replace".equals(Utilities.getString(fBundle, "mode", "replace"));
									    
		fCompareConfiguration= new CompareConfiguration();
		fCompareConfiguration.setLeftEditable(false);
		fCompareConfiguration.setRightEditable(false);
				
		String iconName= Utilities.getString(fBundle, "dateIcon", "obj16/day_obj.gif");
		ImageDescriptor id= CompareUIPlugin.getImageDescriptor(iconName);
		if (id != null)
			fDateImage= id.createImage();
		iconName= Utilities.getString(fBundle, "timeIcon", "obj16/resource_obj.gif");
		id= CompareUIPlugin.getImageDescriptor(iconName);
		if (id != null)
			fTimeImage= id.createImage();
	}
		
	/**
	 * Presents this modal dialog with the functionality described in the class comment above.
	 *
	 * @param target the input object against which the editions are compared; must not be <code>null</code>
	 * @param editions the list of editions (element type: <code>ITypedElement</code>s)
	 * @param path If <code>null</code> dialog shows full input; if non <code>null</code> it extracts a subsection
	 * @return returns the selected edition or <code>null</code> if dialog was cancelled.
	 * The returned <code>ITypedElement</code> is one of the original editions
	 * if <code>path</code> was <code>null</code>; otherwise
	 * it is an <code>ITypedElement</code> returned from <code>IStructureCreator.locate(path, item)</code>
	 */
	public ITypedElement selectEdition(final ITypedElement target, ITypedElement[] inputEditions, Object ppath) {
		
		Assert.isNotNull(target);
		fTargetItem= target;

		// sort input editions
		final int count= inputEditions.length;
		final IModificationDate[] editions= new IModificationDate[count];
		for (int i= 0; i < count; i++)
			editions[i]= (IModificationDate) inputEditions[i];
		if (count > 1)
			internalSort(editions, 0, count-1);
			
		// find StructureCreator if ppath is not null
		IStructureCreator structureCreator= null;
		if (ppath != null) {
			String type= target.getType();
			IStructureCreatorDescriptor scd= CompareUIPlugin.getStructureCreator(type);
			if (scd != null)
				structureCreator= scd.createStructureCreator();
		}

		if (fReplaceMode) {
			
			if (structureCreator != null) {
				Object item= structureCreator.locate(ppath, target);
				if (item instanceof ITypedElement)
					fTargetItem= (ITypedElement) item;
				else
					ppath= null;	// couldn't extract item
			}
			
			// set the left and right labels for the compare viewer
			String targetLabel= getTargetLabel(target, fTargetItem);
			if (fTargetIsRight)
				fCompareConfiguration.setRightLabel(targetLabel);
			else
				fCompareConfiguration.setLeftLabel(targetLabel);
			
			if (structureCreator != null && ppath != null) {	// extract sub element
				
				final IStructureCreator sc= structureCreator;
				final Object path= ppath;
				
				// construct the comparer thread
				// and perform the background extract
				fThread= new Thread() {
					public void run() {
																				
						// from front (newest) to back (oldest)
						for (int i= 0; i < count; i++) {
								
							if (fEditionTree == null || fEditionTree.isDisposed())
								break;
							ITypedElement edition= (ITypedElement) editions[i];
							
							// extract sub element from edition
							Object r= sc.locate(path, edition);
							if (r instanceof ITypedElement) {	// if not empty
								ITypedElement item= (ITypedElement) r;
								sendPair(new Pair(edition, item, sc.getContents(item, false)));
							}
						}
						sendPair(null);
					}
				};
			} else {
				// create tree widget
				create();
				
				// from front (newest) to back (oldest)
				for (int i= 0; i < count; i++)
					addMemberEdition(new Pair((ITypedElement) editions[i], (ITypedElement) editions[i], null));
			}
			
		} else {
			
			final Object container= ppath;
			Assert.isNotNull(container);
								
			if (structureCreator == null)
				return null;	// error
		
			// extract all elements of container
			final HashSet current= new HashSet();
			IStructureComparator sco= structureCreator.locate(container, target);
			if (sco != null) {
				Object[] children= sco.getChildren();
				if (children != null)
					for (int i= 0; i < children.length; i++)
						current.add(children[i]);
			} else
				return null; 	// error
			
			final IStructureCreator sc= structureCreator;
			
			// construct the comparer thread
			// and perform the background extract
			fThread= new Thread() {
				public void run() {
					
					// from front (newest) to back (oldest)
					for (int i= 0; i < count; i++) {
							
						if (fEditionTree == null || fEditionTree.isDisposed())
							break;
						ITypedElement edition= (ITypedElement) editions[i];
						
						IStructureComparator sco2= sc.locate(container, edition);
						if (sco2 != null) {
							Object[] children= sco2.getChildren();
							if (children != null) {
								for (int i2= 0; i2 < children.length; i2++) {
									ITypedElement child= (ITypedElement) children[i2];
									if (!current.contains(child))
										sendPair(new Pair(edition, child, sc.getContents(child, false)));
								}
							}
						}
					}
					sendPair(null);
				}
			};
		}
		
		open();
		
		if (getReturnCode() == OK)
			return fSelectedItem;
		return null;
	}
		
	/**
	 * Returns the input target that has been specified with the most recent call
	 * to <code>selectEdition</code>. If a not <code>null</code> path was specified this method
	 * returns a subsection of this target (<code>IStructureCreator.locate(path, target)</code>)
	 * instead of the input target.
	 * <p>
	 * For example if the <code>target</code> is a Java compilation unit and <code>path</code> specifies
	 * a method, the value returned from <code>getTarget</code> will be the method not the compilation unit.
	 *
	 * @return the last specified target or a subsection thereof.
	 */
	public ITypedElement getTarget() {
		return fTargetItem;
	}
 	
 	/**
 	 * Returns a label for identifying the target side of a compare viewer.
 	 * This implementation extracts the value for the key "targetLabel" from the resource bundle
 	 * and passes it as the format argument to <code>MessageFormat.format</code>.
 	 * The single format argument for <code>MessageFormat.format</code> ("{0}" in the format string)
 	 * is the name of the given input element.
	 * <p>
	 * Subclasses may override to create their own label.
	 * </p>
 	 *
 	 * @param target the target element for which a label must be returned
 	 * @param item if a path has been specified in <code>selectEdition</code> a sub element of the given target; otherwise the same as target
 	 * @return a label the target side of a compare viewer
  	 */
	protected String getTargetLabel(ITypedElement target, ITypedElement item) {
		String format= Utilities.getString(fBundle, "targetLabel", "targetLabel");
		return MessageFormat.format(format, new Object[] { target.getName() });
	}
	
 	/**
 	 * Returns a label for identifying the edition side of a compare viewer.
 	 * This implementation extracts the value for the key "editionLabel" from the resource bundle
 	 * and passes it as the format argument to <code>MessageFormat.format</code>.
 	 * The single format argument for <code>MessageFormat.format</code> ("{0}" in the format string)
 	 * is the formatted modification date of the given input element.
 	 * <p>
	 * Subclasses may override to create their own label.
	 * </p>
	 *
	 * @param selectedEdition the selected edition for which a label must be returned
 	 * @param item if a path has been specified in <code>selectEdition</code> a sub element of the given selectedEdition; otherwise the same as selectedEdition
 	 * @return a label the edition side of a compare viewer
  	 */
	protected String getEditionLabel(ITypedElement selectedEdition, ITypedElement item) {
		String label= Utilities.getString(fBundle, "editionLabel", "editionLabel");

		if (selectedEdition instanceof IModificationDate) {
			long modDate= ((IModificationDate)selectedEdition).getModificationDate();
			String date= DateFormat.getDateTimeInstance().format(new Date(modDate));
			label= MessageFormat.format(label, new Object[] { date });
		}

		return label;
	}
	
	/* (non Javadoc)
	 * Returns the size initialized with the constructor.
	 */
	protected Point getInitialSize() {
		Point size= new Point(Utilities.getInteger(fBundle, "width", 0),
					Utilities.getInteger(fBundle, "height", 0));
		
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

 	/* (non Javadoc)
 	 * Creates SWT control tree.
 	 */
	protected synchronized Control createDialogArea(Composite parent) {
		
		getShell().setText(Utilities.getString(fBundle, "title", "title"));
		
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
		
		if (!fReplaceMode) {
	
			Splitter hsplitter= new Splitter(vsplitter,  SWT.HORIZONTAL);
			
			fMemberPane= new Pane(hsplitter, SWT.NONE);
			fMemberPane.setText("Available Members");
			fMemberTable= new Table(fMemberPane, SWT.H_SCROLL + SWT.V_SCROLL);
			fMemberTable.addSelectionListener(
				new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						handleMemberSelect(e.item);
					}
				}
			);
			
			fMemberPane.setContent(fMemberTable);
			
			fEditionPane= new Pane(hsplitter, SWT.NONE);
		} else {
			fEditionPane= new Pane(vsplitter, SWT.NONE);
		}
		
		String titleFormat= Utilities.getString(fBundle, "treeTitleFormat", "treeTitleFormat");
		String title= MessageFormat.format(titleFormat, new Object[] { fTargetItem.getName() });
		fEditionPane.setText(title);
		
		fEditionTree= new Tree(fEditionPane, SWT.H_SCROLL + SWT.V_SCROLL);
		fEditionTree.addSelectionListener(
			new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
					handleDefaultSelected();
				}
				public void widgetSelected(SelectionEvent e) {
					feedInput(e.item);
				}
			}
		);
		fEditionPane.setContent(fEditionTree);		
		
		// now start the thread (and forget about it)
		if (fThread != null) {
			fThread.start();
			fThread= null;
		}
		
		fContentPane= new CompareViewerSwitchingPane(vsplitter, SWT.NONE) {
			protected Viewer getViewer(Viewer oldViewer, Object input) {
				return CompareUIPlugin.findContentViewer(oldViewer, input, this, fCompareConfiguration);
			}
		};
		vsplitter.setWeights(new int[] { 30, 70 });
				
		return vsplitter;
	}
	
	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		String buttonLabel= Utilities.getString(fBundle, "buttonLabel", "buttonLabel");
		fCommitButton= createButton(parent, IDialogConstants.OK_ID, buttonLabel, true);
		fCommitButton.setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	//---- private stuff ----------------------------------------------------------------------------------------
				
	/**
	 * Asynchroneously sends a Pair (or null) to the UI thread.
	 */
	private void sendPair(final Pair pair) {		
		if (fEditionTree != null && !fEditionTree.isDisposed()) {
			Display display= fEditionTree.getDisplay();
			display.asyncExec(
				new Runnable() {
					public void run() {
						addMemberEdition(pair);
					}
				}
			);
		}
	}

	private void handleDefaultSelected() {
		if (fSelectedItem != null)
			okPressed();
	}
	
	private static void internalSort(IModificationDate[] keys, int left, int right) { 
	
		int original_left= left;
		int original_right= right;
		
		IModificationDate mid= keys[(left + right) / 2]; 
		do { 
			while (keys[left].getModificationDate() > mid.getModificationDate())
				left++; 
			
			while (mid.getModificationDate() > keys[right].getModificationDate())
				right--; 
		
			if (left <= right) { 
				IModificationDate tmp= keys[left]; 
				keys[left]= keys[right]; 
				keys[right]= tmp;			
				left++; 
				right--; 
			} 
		} while (left <= right);
		
		if (original_left < right)
			internalSort(keys, original_left, right); 
		
		if (left < original_right)
			internalSort(keys, left, original_right); 
		 
	}
	
	/**
	 * Adds the given Pair to 
	 * If HIDE_IDENTICAL is true the new Pair is only added if its contents
	 * is different from the preceeding Pair.
	 * If the argument is <code>null</code> the message "No Editions found" is shown
	 * in the member or edition viewer.
	 */
	private void addMemberEdition(Pair pair) {
		
		if (pair == null) {
			if (fMemberTable != null) {
				if (!fMemberTable.isDisposed() && fMemberTable.getItemCount() == 0) {
					TableItem ti= new TableItem(fMemberTable, SWT.NONE);
					ti.setText("No additional members found");
				}
				return;
			}			
			if (fEditionTree != null && !fEditionTree.isDisposed() && fEditionTree.getItemCount() == 0) {
				TreeItem ti= new TreeItem(fEditionTree, SWT.NONE);
				ti.setText("No editions found");
			}
			return;
		}
		
		if (fMemberEditions == null)
			fMemberEditions= new HashMap();
		
		ITypedElement item= pair.getItem();
		List editions= (List) fMemberEditions.get(item);
		if (editions == null) {
			editions= new ArrayList();
			fMemberEditions.put(item, editions);
			if (fMemberTable != null && !fMemberTable.isDisposed()) {
				ITypedElement te= (ITypedElement)item;
				String name= te.getName();
				
				// find position
				TableItem[] items= fMemberTable.getItems();
				int where= items.length;
				for (int i= 0; i < where; i++) {
					String n= items[i].getText();
					if (n.compareTo(name) > 0) {
						where= i;
						break;
					}
				}
				
				TableItem ti= new TableItem(fMemberTable, where, SWT.NULL);
				ti.setImage(te.getImage());
				ti.setText(name);
				ti.setData(editions);
			}
		}
		if (HIDE_IDENTICAL) {
			int size= editions.size();
			if (size > 0) {
				Pair last= (Pair) editions.get(size-1);
				if (last != null && last.equals(pair))
					return;	// don't add since the new one is equal to old
			}
		}
		editions.add(pair);
		
		if (fReplaceMode || editions == fCurrentEditions)
			addEdition(pair);
	}
	
	/**
	 * Adds the given Pair to the edition tree.
	 * It takes care of creating tree nodes for different dates.
	 */
	private void addEdition(Pair pair) {
		if (fEditionTree == null || fEditionTree.isDisposed())
			return;
		
		// find last day
		TreeItem[] days= fEditionTree.getItems();
		TreeItem lastDay= null;
		if (days.length > 0)
			lastDay= days[days.length-1];
		
		boolean first= lastDay == null;
		
		ITypedElement edition= pair.getEdition();
		ITypedElement item= pair.getItem();
		
		long ldate= ((IModificationDate)edition).getModificationDate();
		long day= ldate / ONE_DAY_MS;
		Date date= new Date(ldate);
		if (lastDay == null || day != ((Date)lastDay.getData()).getTime() / ONE_DAY_MS) {
			lastDay= new TreeItem(fEditionTree, SWT.NONE);
			lastDay.setImage(fDateImage);
			String df= DateFormat.getDateInstance().format(date);
			long today= System.currentTimeMillis() / ONE_DAY_MS;
			
			String formatKey;
			if (day == today)
				formatKey= "todayFormat";
			else if (day == today-1)
				formatKey= "yesterdayFormat";
			else
				formatKey= "dayFormat";
			String pattern= Utilities.getString(fBundle, formatKey, null);
			if (pattern != null)
				df= MessageFormat.format(pattern, new Object[] { df });
			lastDay.setText(df);
			lastDay.setData(date);
		}
		TreeItem ti= new TreeItem(lastDay, SWT.NONE);
		ti.setImage(fTimeImage);
		ti.setText(DateFormat.getTimeInstance().format(date));
		ti.setData(new Pair(edition, item, null));
		if (first) {
			fEditionTree.setSelection(new TreeItem[] {ti});
			if (fReplaceMode)
				fEditionTree.setFocus();
			feedInput(ti);
		}
		if (first) // expand first node
			lastDay.setExpanded(true);
	}
					
	/**
	 * Feeds selection from member viewer to edition viewer.
	 */
	private void handleMemberSelect(Widget w) {
		Object data= w.getData();
		if (data instanceof List) {
			List editions= (List) data;
			if (editions != fCurrentEditions) {
				fCurrentEditions= editions;
				fEditionTree.removeAll();
				fEditionPane.setText("Editions of " + ((Item)w).getText());
				Iterator iter= editions.iterator();
				while (iter.hasNext()) {
					Object item= iter.next();
					if (item instanceof Pair)
						addEdition((Pair) item);
				}
			}
		}
	}
	
	/*
	 * Feeds selection from edition viewer to content viewer.
	 */
	private void feedInput(Widget w) {
		Object input= w.getData();
		if (input instanceof Pair) {
			Pair pair= (Pair) input;
			fSelectedItem= pair.getItem();
			
			String editionLabel= getEditionLabel(pair.getEdition(), fSelectedItem);
			
			if (!fReplaceMode) {
				fContentPane.setInput(fSelectedItem);
				fContentPane.setText(editionLabel);
			} else {
				if (fTargetIsRight) {
					fCompareConfiguration.setLeftLabel(editionLabel);
					fContentPane.setInput(new DiffNode(fSelectedItem, fTargetItem));
				} else {
					fCompareConfiguration.setRightLabel(editionLabel);
					fContentPane.setInput(new DiffNode(fTargetItem, fSelectedItem));
				}
			}
		} else {
			fSelectedItem= null;
			fContentPane.setInput(null);
		}
		fCommitButton.setEnabled(fSelectedItem != null);
	}
}

