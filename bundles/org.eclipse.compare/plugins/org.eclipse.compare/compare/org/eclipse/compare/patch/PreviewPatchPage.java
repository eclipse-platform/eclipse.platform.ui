/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.patch;

import java.util.*;
import java.io.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;

import org.eclipse.jface.wizard.*;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.resources.IFile;

import org.eclipse.compare.*;
import org.eclipse.compare.internal.*;
import org.eclipse.compare.structuremergeviewer.*;
import org.eclipse.compare.contentmergeviewer.*;


/**
 * Shows the parsed patch file and any mismatches
 * between files, hunks and the currently selected
 * resources.
 */
/* package */ class PreviewPatchPage extends WizardPage {
	
	/**
	 * Used with CompareInput
	 */
	static class HunkInput implements ITypedElement, IStreamContentAccessor {
		String fContent;
		String fType;
		
		HunkInput(String type, String s) {
			fType= type;
			fContent= s;
		}
		public Image getImage() {
			return null;
		}
		public String getName() {
			return "no name";
		}
		public String getType() {
			return fType;
		}
		public InputStream getContents() {
			return new ByteArrayInputStream(fContent.getBytes());
		}
	};
		
	private PatchWizard fPatchWizard;
	
	private Tree fTree;
	private Combo fStripPrefixSegments;
	private CompareViewerSwitchingPane fHunkViewer;
	
	private Image fNullImage;
	private Image fAddImage;
	private Image fDelImage;
	
	private CompareConfiguration fCompareConfiguration;
	
	
	/* package */ PreviewPatchPage(PatchWizard pw) {
		super("Preview Patch", "Preview Patch", null);
		fPatchWizard= pw;
		//setPageComplete(false);
		
		int w= 16;
		fNullImage= new DiffImage(null, null, w).createImage();
		fAddImage= new DiffImage(null, CompareUIPlugin.getImageDescriptor("ovr16/add_ov.gif"), w).createImage();
		fDelImage= new DiffImage(null, CompareUIPlugin.getImageDescriptor("ovr16/del_ov.gif"), w).createImage();
		
		fCompareConfiguration= new CompareConfiguration();
		
		fCompareConfiguration.setLeftEditable(false);
		fCompareConfiguration.setLeftLabel("Original");
		
		fCompareConfiguration.setRightEditable(false);
		fCompareConfiguration.setRightLabel("Result");
	}
	
	/* (non-Javadoc)
	 * Method declared in WizardPage
	 */
	public void setVisible(boolean visible) {
		if (visible)
			buildTree();
		super.setVisible(visible);
	}

	Image getImage(Diff diff) {
		switch (diff.getType()) {
		case Differencer.ADDITION:
			return fAddImage;
		case Differencer.DELETION:
			return fDelImage;
		}
		return fNullImage;
	}
	
	public void createControl(Composite parent) {
				
		Composite composite= new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		setControl(composite);
		
		fTree= new Tree(composite, SWT.CHECK | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		
		GridData data= new GridData();
		data.verticalAlignment= GridData.FILL;
		data.horizontalAlignment= GridData.FILL;
		data.grabExcessHorizontalSpace= true;
		data.grabExcessVerticalSpace= true;
		fTree.setLayoutData(data);
		
		createStripPrefixSegmentsGroup(composite);
				
		fHunkViewer= new CompareViewerSwitchingPane(composite, SWT.BORDER) {
			protected Viewer getViewer(Viewer oldViewer, Object input) {
				return CompareUI.findContentViewer(oldViewer, (ICompareInput)input, this, fCompareConfiguration);
			}
		};
								
		GridData data2= new GridData();
		data2.verticalAlignment= GridData.FILL;
		data2.horizontalAlignment= GridData.FILL;
		data2.grabExcessHorizontalSpace= true;
		data2.grabExcessVerticalSpace= true;
		fHunkViewer.setLayoutData(data2);
		
		fTree.addSelectionListener(
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					TreeItem ti= (TreeItem) e.item;
					Object data= e.item.getData();
					if (e.detail == SWT.CHECK) {
						boolean enabled= ti.getChecked();
						if (data instanceof Hunk) {
							((Hunk)data).fIsEnabled= enabled;
							updateGrayedState(ti);
						} else if (data instanceof Diff) {
							((Diff)data).fIsEnabled= enabled;
							updateCheckedState(ti);
						}
					} else {
						if (data instanceof Hunk)
							PreviewPatchPage.this.fHunkViewer.setInput(createInput((Hunk)data));
						else
							PreviewPatchPage.this.fHunkViewer.setInput(null);
					}
				}
			}
		);
		
		buildTree();

		// WorkbenchHelp.setHelp(composite, new DialogPageContextComputer(this, PATCH_HELP_CONTEXT_ID));								
	}
	
	ICompareInput createInput(Hunk hunk) {
		
		String[] lines= hunk.fLines;
		StringBuffer left= new StringBuffer();
		StringBuffer right= new StringBuffer();
		
		for (int i= 0; i < lines.length; i++) {
			String line= lines[i];
			String rest= line.substring(1);
			switch (line.charAt(0)) {
			case ' ':
				left.append(rest);
				right.append(rest);
				break;
			case '-':
				left.append(rest);
				break;
			case '+':
				right.append(rest);
				break;
			}
		}
		
		Diff diff= hunk.fParent;
		IPath path= diff.getPath();
		String type= path.getFileExtension();
		
		return new DiffNode(new HunkInput(type, left.toString()), new HunkInput(type, right.toString()));
	}		
	
	/**
	 * Builds a tree from list of Diffs.
	 * As a side effect it calculates the maximum number of segments
	 * in all paths.
	 */
	private void buildTree() {
		setPageComplete(true);
		if (fTree != null && !fTree.isDisposed()) {
			fTree.removeAll();
			fHunkViewer.setInput(null);
			
			int length= 99;
			
			Diff[] diffs= fPatchWizard.getPatcher().getDiffs();			
			if (diffs != null) {
				for (int i= 0; i < diffs.length; i++) {
					Diff diff= diffs[i];
					TreeItem d= new TreeItem(fTree, SWT.NULL);
					d.setData(diff);
					d.setImage(getImage(diff));
					
					if (diff.fOldPath != null)
						length= Math.min(length, diff.fOldPath.segmentCount());
					if (diff.fNewPath != null)
						length= Math.min(length, diff.fNewPath.segmentCount());
					
					java.util.List hunks= diff.fHunks;
					java.util.Iterator iter= hunks.iterator();
					while (iter.hasNext()) {
						Hunk hunk= (Hunk) iter.next();
						TreeItem h= new TreeItem(d, SWT.NULL);
						h.setData(hunk);
						h.setText(hunk.getDescription());
					}
				}
			}
			if (length != 99)
				for (int i= 1; i < length; i++)
					fStripPrefixSegments.add(Integer.toString(i));
		}
		
		updateTree(0);
	}
	
	/**
	 * Updates label and checked state of tree items.
	 */
	private void updateTree(int strip) {
		if (fTree == null || fTree.isDisposed())
			return;
		TreeItem[] children= fTree.getItems();
		for (int i= 0; i < children.length; i++) {
			TreeItem item= children[i];
			Diff diff= (Diff) item.getData();
								
			item.setText(diff.getDescription(strip));
			
			IFile file= null;
			if (diff.getType() == Differencer.ADDITION) {
				IPath p= diff.fNewPath;
				if (strip > 0 && strip < p.segmentCount())
					p= p.removeFirstSegments(strip);
				file= fPatchWizard.existsInSelection(p);
				diff.fIsEnabled= file == null;
			} else {
				IPath p= diff.fOldPath;
				if (strip > 0 && strip < p.segmentCount())
					p= p.removeFirstSegments(strip);
				file= fPatchWizard.existsInSelection(p);
				diff.fIsEnabled= file != null;
			}			
			
			boolean checked= false;
				
			ArrayList failedHunks= new ArrayList();		// collect rejected hunks here
			
			java.util.List lines= null;
			InputStream is= null;
			try {
				if (file != null) {
					is= file.getContents();
					BufferedReader reader= new BufferedReader(new InputStreamReader(is));
					lines= new LineReader(reader).readLines();
				}
			} catch(CoreException ex) {
				System.out.println("CoreException: " + ex);
			} finally {
				if (is != null)
					try {
						is.close();
					} catch(IOException ex) {
					}
			}
			
			if (lines == null)
				lines= new ArrayList();
			fPatchWizard.getPatcher().patch(diff, lines, failedHunks);
			
			int checkedSubs= 0;	// counts checked hunk items
			TreeItem[] hunkItems= item.getItems();
			for (int h= 0; h < hunkItems.length; h++) {
				Hunk hunk= (Hunk) hunkItems[h].getData();
				hunk.fIsEnabled= diff.fIsEnabled && !failedHunks.contains(hunk);
				hunkItems[h].setChecked(hunk.fIsEnabled);
				if (hunk.fIsEnabled) {
					checkedSubs++;
					checked= true;
				}
			}
			
			item.setChecked(checked);
			item.setGrayed((checkedSubs > 0 &&  checkedSubs < hunkItems.length));
		}
	}
	
	/**
	 * Updates the gray state of the given diff and the checked state of its children.
	 */
	void updateCheckedState(TreeItem diff) {
		boolean checked= diff.getChecked();
		diff.setGrayed(false);
		TreeItem[] hunks= diff.getItems();
		for (int i= 0; i < hunks.length; i++)
			hunks[i].setChecked(checked);
	}
	
	/**
	 * Updates the gray state of the given items parent.
	 */
	void updateGrayedState(TreeItem hunk) {
		TreeItem diff= hunk.getParentItem();
		TreeItem[] hunks= diff.getItems();
		int checked= 0;
		for (int i= 0; i < hunks.length; i++)
			if (hunks[i].getChecked())
				checked++;
		diff.setChecked(checked > 0);
		diff.setGrayed(checked > 0 && checked < hunks.length);
	}
	
	/**
	 *	Create the group for setting the strip prefix segment size
	 */
	private Composite createStripPrefixSegmentsGroup(Composite parent) {
		
		Composite group= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= 3;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
	
		new Label(group, SWT.NONE).setText("Ignore leading path name segments:");

		fStripPrefixSegments= new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.SIMPLE);
		fStripPrefixSegments.add("0");
		fStripPrefixSegments.setText("0");
		fStripPrefixSegments.addSelectionListener(
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					int ix= fStripPrefixSegments.getSelectionIndex();
					updateTree(ix);
				}
			}
		);
			
		return group;
	}
	
	/* package */ int getStripPrefixSegments() {
		int stripPrefixSegments= 0;
		if (fStripPrefixSegments != null) {
			String s= fStripPrefixSegments.getText();
			try {
				stripPrefixSegments= Integer.parseInt(s);
			} catch(NumberFormatException ex) {
			}
		}
		return stripPrefixSegments;
	}
}


