package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2002. All Rights Reserved.
 * Contributors:  Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog
 * font should be activated and used by other components.
 */
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import java.util.*;
import java.util.List;
/**
 *	This class implements the identical API to CheckboxDoubleListGroup, but
 *	only displays a single checkbox list. 
 */
class CheckboxSingleListGroup extends Composite implements ICheckStateListener, ISelectionChangedListener {
	private	Object			root;
	private	Object			currentList1Selection;
	private	Map				checkedStateStore = new HashMap(9);
	private	ListenerList	listeners = new ListenerList();
	private	boolean			singleList1Check = false;
	private	boolean			singleList2Check = false;
	
	private	IStructuredContentProvider	list1ContentProvider;
	private	IStructuredContentProvider	list2ContentProvider;
	private ILabelProvider				list1LabelProvider;
	private ILabelProvider				list2LabelProvider;
	
	// widgets
	private	CheckboxTableViewer	list1Viewer;
/**
 *	Create an instance of this class.  Use this constructor if you want
 *	the combined widget to act like others w.r.t. sizing and set its
 *	size according to whatever is required to fill its context.
 *
 *	@param parent org.eclipse.swt.widgets.Composite
 *	@param style int
 *  @param rootObject java.lang.Object
 *	@param childPropertyName java.lang.String
 *	@param parentPropertyName java.lang.String
 *	@param listPropertyName java.lang.String
 */
public CheckboxSingleListGroup(
	Composite parent,Object rootObject,
	IStructuredContentProvider list1ContentProvider,ILabelProvider list1LabelProvider,
	IStructuredContentProvider list2ContentProvider,ILabelProvider list2LabelProvider,
	int style) {
		
	this(
		parent,rootObject,
		list1ContentProvider,list1LabelProvider,
		list2ContentProvider,list2LabelProvider,
		style,-1,-1);
}
/**
 *	Create an instance of this class.  Use this constructor if you wish to specify
 *	the width and/or height of the combined widget (to only hardcode one of the
 *	sizing dimensions, specify the other dimension's value as -1)
 *
 *	@param parent org.eclipse.swt.widgets.Composite
 *	@param style int
 *  @param rootObject java.lang.Object
 *	@param childPropertyName java.lang.String
 *	@param parentPropertyName java.lang.String
 *	@param listPropertyName java.lang.String
 *	@param width int
 *	@param height int
 */
public CheckboxSingleListGroup(
	Composite parent,Object rootObject,
	IStructuredContentProvider list1ContentProvider,ILabelProvider list1LabelProvider,
	IStructuredContentProvider list2ContentProvider,ILabelProvider list2LabelProvider,
	int style,int width,int height) {

	super(parent,style);
	root = rootObject;
	this.list1ContentProvider = list1ContentProvider;
	this.list2ContentProvider = list2ContentProvider;
	this.list1LabelProvider = list1LabelProvider;
	this.list2LabelProvider = list2LabelProvider;
	createContents(parent,width,height);
}
/**
 *	Add the passed listener to self's collection of clients
 *	that listen for changes to element checked states
 *
 *	@param listener ICheckStateListener
 */
public void addCheckStateListener(ICheckStateListener listener) {
	listeners.add(listener);
}
/**
 *	An item was checked in one of self's two views.  Determine which
 *	view this occurred in and delegate appropriately
 *
 *	@param event CheckStateChangedEvent
 */
public void checkStateChanged(CheckStateChangedEvent event) {
	list1ItemChecked(event.getElement(),event.getChecked());
	notifyCheckStateChangeListeners(event);
}
/**
 *	Compute the preferred size.
 *
 *	@return org.eclipse.swt.graphics.Point
 *	@param wHint int
 *	@param hHint int
 *	@param changed boolean
 */
public Point computeSize(int wHint,int hHint,boolean changed) {
	return new Point(-1,-1);
}
/**
 *	Lay out and initialize self's visual components.
 *
 *	@param parent org.eclipse.swt.widgets.Composite
 *	@param width int
 *	@param height int
 */
protected void createContents(Composite parent,int width,int height) {
	Font font = parent.getFont();
	// group pane
	Composite composite = new Composite(parent,SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.numColumns = 2;
	GridData spec = new GridData();
	spec.grabExcessHorizontalSpace = true;
	spec.grabExcessVerticalSpace = true;
	spec.horizontalAlignment = GridData.FILL;
	spec.verticalAlignment = GridData.FILL;
	composite.setLayout(layout);
	composite.setLayoutData(spec);
	composite.setFont(font);
	
	// list 1 view pane.  Add a border to the pane.
	Composite pane = createViewPane(composite, width/2, height/2);
	
	// list 1 viewer
	list1Viewer = CheckboxTableViewer.newCheckList(pane, SWT.NONE);
	list1Viewer.setContentProvider(list1ContentProvider);
	list1Viewer.setLabelProvider(list1LabelProvider);
	list1Viewer.addCheckStateListener(this);
	list1Viewer.addSelectionChangedListener(this);
	list1Viewer.getTable().setFont(font);

	// this has to be done after the viewers have been laid out
	list1Viewer.setInput(root);
}
/**
 *	Create a viewer pane in this group for the passed viewer.
 *
 *	@param parent org.eclipse.swt.widgets.Composite
 *	@param width int
 *	@param height int
 */
protected Composite createViewPane(Composite parent, int width, int height) {
	Composite pane = new Composite(parent, SWT.BORDER);
	GridData spec = new GridData(GridData.FILL_BOTH);
	spec.widthHint = width;
	spec.heightHint = height;
	pane.setLayoutData(spec);
	pane.setLayout(new FillLayout());
	return pane;
}
/**
 *	Answer a collection of all of the checked elements in the list 1
 *	portion of self
 *
 *	@return java.util.Vector
 */
public Set getAllCheckedList1Items() {
	return checkedStateStore.keySet();
}
/**
 *	Answer a flat collection of all of the checked elements in the
 *	list 2 portion of self
 *
 *	@return java.util.List
 */
public List getAllCheckedList2Items() {
	List result = new ArrayList();
	Iterator listCollectionsEnum = checkedStateStore.values().iterator();
	
	while (listCollectionsEnum.hasNext()) {
		Iterator currentCollection = ((List)listCollectionsEnum.next()).iterator();
		while (currentCollection.hasNext())
			result.add(currentCollection.next());
	}
	
	return result;
}
/**
 *	Answer the number of elements that have been checked by the
 *	user.
 *
 *	@return int
 */
public int getCheckedElementCount() {
	return checkedStateStore.size();
}
/**
 *	Set the checked state of the passed list 1 element, as well
 *	as its associated list 2 elements
 */
public void initialCheckList1Item(Object element) {
	checkedStateStore.put(element,new Vector());
	list1Viewer.setChecked(element,true);
}
/**
 *	Handle the checking of a list 1 item
 */
protected void list1ItemChecked(Object listElement,boolean state) {

	if (state) {
		// if only one list 1 item can be checked at a time then clear the
		// previous checked list 1 item, if any
		if (singleList1Check) {
			checkedStateStore = new Hashtable(9);
			list1Viewer.setAllChecked(false);
		}

		checkedStateStore.put(listElement,new Vector());
		
	} else
		checkedStateStore.remove(listElement);

	// the following may seem redundant, but it allows other methods to invoke
	// this method in order to fully simulate the user clicking a list 1 item
	list1Viewer.setChecked(listElement,state);
}
/**
 *	Notify all checked state listeners that the passed element has had
 *	its checked state changed to the passed state
 */
protected void notifyCheckStateChangeListeners(final CheckStateChangedEvent event) {
	Object[] array = listeners.getListeners();
	for (int i = 0; i < array.length; i ++) {
		final ICheckStateListener l = (ICheckStateListener)array[i];
		Platform.run(new SafeRunnable() {
			public void run() {
				l.checkStateChanged(event);
			}
			public void handleException(Throwable e) {
				super.handleException(e);
				//If and unexpected exception happens, remove it
				//to make sure the workbench keeps running.
				removeCheckStateListener(l);
			}
		});
	}		
}
/**
 *	Remove the passed listener from self's collection of clients
 *	that listen for changes to element checked states
 *
 *	@param listener ICheckStateListener
 */
public void removeCheckStateListener(ICheckStateListener listener) {
	listeners.remove(listener);
}
/**
 *	Handle the selection of a list 1 item
 *
 *	@param selection ISelection
 */
public void selectionChanged(SelectionChangedEvent event) {
	IStructuredSelection selection = (IStructuredSelection) event.getSelection();
	currentList1Selection = selection.getFirstElement();
}
/**
 *	Change the list 1 viewer's providers to those passed
 *
 *	@param contentProvider ITreeContentProvider
 *	@param labelProvider ILabelProvider
 */
public void setList1Providers(IStructuredContentProvider contentProvider, ILabelProvider labelProvider) {
	list1Viewer.setContentProvider(contentProvider);
	list1Viewer.setLabelProvider(labelProvider);
}
/**
 *	Set the sorter that is to be applied to self's list 1 viewer
 */
public void setList1Sorter(ViewerSorter sorter) {
	list1Viewer.setSorter(sorter);
}
/**
 *	Change the list 2 viewer's providers to those passed
 *
 *	@param contentProvider ITreeContentProvider
 *	@param labelProvider ILabelProvider
 */
public void setList2Providers(IStructuredContentProvider contentProvider, ILabelProvider labelProvider) {
}
/**
 *	Set the sorter that is to be applied to self's list 2 viewer
 *
 *	@param sorter ViewerSorter
 */
public void setList2Sorter(ViewerSorter sorter) {
}
/**
 *	Set the root element that determines the content of list viewer 1
 */
public void setRoot(Object rootElement) {
	root = rootElement;
	checkedStateStore = new Hashtable(9);
	list1Viewer.setInput(rootElement);
}
/**
 *	If this is set to true then only one list 1 item can be
 *	checked at a time.  The default value for this is false.
 *
 *	@param value boolean
 */
public void setSingleList1Check(boolean value) {
	singleList1Check = value;
}
/**
 *	If this is set to true then only one list 2 item can be
 *	checked at a time.  The default value for this is false.
 *
 *	@param value boolean
 */
public void setSingleList2Check(boolean value) {
	singleList2Check = value;
}
}
