package org.eclipse.ui.internal.misc;

/*
 * (c) Copyright IBM Corp. 2000, 2002. All Rights Reserved.
 * Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog font should be
 * activated and used by other components.
 */
import java.util.*;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.Composite;

/**
 *	Workbench-level composite that combines two CheckboxListViewers.
 *	All viewer selection-driven interactions are handled within this object
 */
public class CheckboxDoubleListGroup extends Composite implements ICheckStateListener, ISelectionChangedListener {
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
	private	CheckboxTableViewer	list2Viewer;
/**
 *	Create an instance of this class.  Use this constructor if you want
 *	the combined widget to act like others w.r.t. sizing and set its
 *	size according to whatever is required to fill its context.
 *
 *	@param parent org.eclipse.swt.widgets.Composite
 *  @param rootObject java.lang.Object
 *	@param style int
 *	@param childPropertyName java.lang.String
 *	@param parentPropertyName java.lang.String
 *	@param listPropertyName java.lang.String
 */
public CheckboxDoubleListGroup(
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
public CheckboxDoubleListGroup(
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
	if (event.getCheckable().equals(list1Viewer))
		list1ItemChecked(event.getElement(),event.getChecked());
	else
		list2ItemChecked(event.getElement(),event.getChecked());

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
	// group pane
	Composite composite = new Composite(parent,SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.numColumns = 2;
	composite.setFont(parent.getFont());
	composite.setLayout(layout);
	composite.setLayoutData(new GridData(
		GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
	
	createList1Viewer(createViewPane(composite, width/2, height/2));
	createList2Viewer(createViewPane(composite, width/2, height/2));
	
	list1Viewer.setInput(root);
}
/**
 *	Create the left viewer for this group.
 *
 *	@param parent org.eclipse.swt.widgets.Composite
 */
protected void createList1Viewer(Composite parent) {
	list1Viewer = CheckboxTableViewer.newCheckList(parent, SWT.NONE);
	list1Viewer.setContentProvider(list1ContentProvider);
	list1Viewer.setLabelProvider(list1LabelProvider);
	list1Viewer.addCheckStateListener(this);
	list1Viewer.addSelectionChangedListener(this);
	list1Viewer.getTable().setFont(parent.getFont());
}
/**
 *	Create the right viewer for this group.
 *
 *	@param parent org.eclipse.swt.widgets.Composite
 */
protected void createList2Viewer(Composite parent) {
	list2Viewer = CheckboxTableViewer.newCheckList(parent, SWT.NONE);
	list2Viewer.setContentProvider(list2ContentProvider);
	list2Viewer.setLabelProvider(list2LabelProvider);
	list2Viewer.addCheckStateListener(this);
	list2Viewer.getTable().setFont(parent.getFont());
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
	pane.setFont(parent.getFont());
	return pane;
}
/**
 *	Answer a collection of all of the checked elements in the list 1
 *	portion of self
 *
 *	@return java.util.Set
 */
public Set getAllCheckedList1Items() {
	return checkedStateStore.keySet();
}
/**
 *	Answer a flat collection of all of the checked elements in the
 *	list 2 portion of self
 *
 *	@return java.util.Vector
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
	checkedStateStore.put(element,new ArrayList());
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
			checkedStateStore.clear();
			list1Viewer.setAllChecked(false);
		}

		checkedStateStore.put(listElement,new ArrayList());
		
	} else {
		checkedStateStore.remove(listElement);
		list2Viewer.setAllChecked(false);
	}

	// the following may seem redundant, but it allows other methods to invoke
	// this method in order to fully simulate the user clicking a list 1 item
	list1Viewer.setChecked(listElement,state);
}
/**
 *	Handle the checking of a list 2 item
 */
protected void list2ItemChecked(Object listElement,boolean state) {
	List checkedListItems = (List)checkedStateStore.get(currentList1Selection);

	if (state) {
		// if only one list 2 item can be checked at a time then clear the
		// previous checked list 2 item, if any
		if (singleList2Check) {
			checkedListItems = null;
			list2Viewer.setAllChecked(false);
			list2Viewer.setChecked(listElement,true);
		}

		if (checkedListItems == null) {
			list1ItemChecked(currentList1Selection,true);
			checkedListItems = (List)checkedStateStore.get(currentList1Selection);
		}

		checkedListItems.add(listElement);

	} else {
		checkedListItems.remove(listElement);
		if (checkedListItems.isEmpty())
			list1ItemChecked(currentList1Selection,false);
	}
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
 *Handle the selection of a list 1 item
 *
 *@param selection ISelection
 */
public void selectionChanged(SelectionChangedEvent event) {
	IStructuredSelection selection = (IStructuredSelection) event.getSelection();
	final Object selectedElement = selection.getFirstElement();
	if (selectedElement == null) {
		currentList1Selection = null;
		list2Viewer.setInput(currentList1Selection);
		return;
	}

	// ie.- if not an item deselection
	if (selectedElement != currentList1Selection) {
		list2Viewer.setInput(selectedElement);
		List listItemsToCheck = (List)checkedStateStore.get(selectedElement);
		if (listItemsToCheck != null) {
			Iterator listItemsEnum = listItemsToCheck.iterator();
			while (listItemsEnum.hasNext())
				list2Viewer.setChecked(listItemsEnum.next(), true);
		}
	}
	currentList1Selection = selectedElement;
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
	list2Viewer.setContentProvider(contentProvider);
	list2Viewer.setLabelProvider(labelProvider);
}
/**
 *	Set the sorter that is to be applied to self's list 2 viewer
 *
 *	@param sorter IViewerSorter
 */
public void setList2Sorter(ViewerSorter sorter) {
	list2Viewer.setSorter(sorter);
}
/**
 *	Set the root element that determines the content of list viewer 1
 */
public void setRoot(Object rootElement) {
	root = rootElement;
	checkedStateStore.clear();
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
