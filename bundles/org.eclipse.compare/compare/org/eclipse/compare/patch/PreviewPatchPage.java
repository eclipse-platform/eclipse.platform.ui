/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.patch;

import java.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;

import org.eclipse.jface.wizard.*;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.resources.IFile;

import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.internal.DiffImage;
import org.eclipse.compare.internal.CompareUIPlugin;


/**
 * Shows the parsed patch file and any mismatches
 * between files, hunks and the currently selected
 * resources.
 */
/* package */ class PreviewPatchPage extends WizardPage {
	
	private PatchWizard fPatchWizard;
	
	private Tree fTree;
	private Combo fStripPrefixSegments;
	private StyledText fText;
	
	private Image fNullImage;
	private Image fAddImage;
	private Image fDelImage;
		
	
	/* package */ PreviewPatchPage(PatchWizard pw) {
		super("Preview Patch", "Preview Patch", null);
		fPatchWizard= pw;
		//setPageComplete(false);
		
		int w= 16;
		fNullImage= new DiffImage(null, null, w).createImage();
		fAddImage= new DiffImage(null, CompareUIPlugin.getImageDescriptor("ovr16/add_ov.gif"), w).createImage();
		fDelImage= new DiffImage(null, CompareUIPlugin.getImageDescriptor("ovr16/del_ov.gif"), w).createImage();
	}
	
	/* (non-Javadoc)
	 * Method declared in WizardPage
	 */
	public void setVisible(boolean visible) {
		if (visible)
			update();
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
	
	/**
	 * Updates tree and text controls from
	 * list of Diffs.
	 */
	private void update() {
		setPageComplete(true);
		if (fTree != null && !fTree.isDisposed()) {
			fTree.removeAll();
			fText.setText("");
			
			int length= 99;
			
			Diff[] diffs= fPatchWizard.getDiffs();			
			if (diffs != null) {
				for (int i= 0; i < diffs.length; i++) {
					Diff diff= diffs[i];
					TreeItem d= new TreeItem(fTree, SWT.NULL);
					d.setData(diff);
					d.setText(diff.getDescription(0));
					d.setImage(getImage(diff));
					
					if (diff.fOldPath != null)
						length= Math.min(length, diff.fOldPath.segmentCount());
					if (diff.fNewPath != null)
						length= Math.min(length, diff.fNewPath.segmentCount());
					
					boolean isOk= false;
					IFile file= null;
					if (diff.getType() == Differencer.ADDITION) {
						file= fPatchWizard.existsInSelection(diff.fNewPath);
						isOk= file == null;
					} else {
						file= fPatchWizard.existsInSelection(diff.fOldPath);
						isOk= file != null;
					}
						
					java.util.List hunks= diff.fHunks;
					java.util.Iterator iter= hunks.iterator();
					while (iter.hasNext()) {
						Hunk hunk= (Hunk) iter.next();
						TreeItem h= new TreeItem(d, SWT.NULL);
						h.setData(hunk);
						h.setText(hunk.getDescription());
						
						h.setChecked(isOk);
					}
					
					d.setChecked(isOk);
				}
			}
			if (length != 99)
				for (int i= 1; i < length; i++)
					fStripPrefixSegments.add(Integer.toString(i));
		}
	}
	
	private void update(int strip) {
		if (fTree == null || fTree.isDisposed())
			return;
		TreeItem[] children= fTree.getItems();
		for (int i= 0; i < children.length; i++) {
			TreeItem item= children[i];
			Diff diff= (Diff) item.getData();
								
			item.setText(diff.getDescription(strip));
			
			boolean isOk= false;
			IFile file= null;
			if (diff.getType() == Differencer.ADDITION) {
				IPath p= diff.fNewPath;
				if (strip > 0 && strip < p.segmentCount())
					p= p.removeFirstSegments(strip);
				file= fPatchWizard.existsInSelection(p);
				isOk= file == null;
			} else {
				IPath p= diff.fOldPath;
				if (strip > 0 && strip < p.segmentCount())
					p= p.removeFirstSegments(strip);
				file= fPatchWizard.existsInSelection(p);
				isOk= file != null;
			}
			item.setChecked(isOk);
			
			TreeItem[] hunkItems= item.getItems();
			for (int h= 0; h < hunkItems.length; h++)
				hunkItems[h].setChecked(isOk);
		}
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
		
		fText= new StyledText(composite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		fText.setEditable(false);
		GridData data2= new GridData();
		data2.verticalAlignment= GridData.FILL;
		data2.horizontalAlignment= GridData.FILL;
		data2.grabExcessHorizontalSpace= true;
		data2.grabExcessVerticalSpace= true;
		fText.setLayoutData(data2);
		
		update();

		fTree.addSelectionListener(
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					Object data= e.item.getData();
					String s= "";
					if (data instanceof Hunk) {
						Hunk hunk= (Hunk) data;
						s= hunk.getContent();
					}
					fText.setText(s);
				} 
			}
		);
		
		// WorkbenchHelp.setHelp(composite, new DialogPageContextComputer(this, PATCH_HELP_CONTEXT_ID));								
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
	
		new Label(group, SWT.NONE).setText("Number of leading directories to ignore in path names:");

		fStripPrefixSegments= new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.SIMPLE);
		fStripPrefixSegments.add("0");
		fStripPrefixSegments.setText("0");
		fStripPrefixSegments.addSelectionListener(
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					int ix= fStripPrefixSegments.getSelectionIndex();
					//System.out.println("ix: " + ix);
					update(ix);
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


