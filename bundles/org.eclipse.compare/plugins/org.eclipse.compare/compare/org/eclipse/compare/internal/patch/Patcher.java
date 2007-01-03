/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Martin Burger <m@rtin-burger.de> patch for #93810 and #93901
 *******************************************************************************/
package org.eclipse.compare.internal.patch;

import java.io.*;
import java.util.*;

import org.eclipse.compare.internal.CompareUIPlugin;
import org.eclipse.compare.internal.Utilities;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

/**
 * A Patcher 
 * - knows how to parse various patch file formats into some in-memory structure,
 * - holds onto the parsed data and the options to use when applying the patches,
 * - knows how to apply the patches to files and folders.
 */
public class Patcher {

	static protected final String REJECT_FILE_EXTENSION= ".rej"; //$NON-NLS-1$

	static protected final String MARKER_TYPE= "org.eclipse.compare.rejectedPatchMarker"; //$NON-NLS-1$

	// diff formats
	//	private static final int CONTEXT= 0;
	//	private static final int ED= 1;
	//	private static final int NORMAL= 2;
	//	private static final int UNIFIED= 3;
	
	private FileDiff[] fDiffs;
	private IResource fTarget;
	// patch options
	private int fStripPrefixSegments;
	private int fFuzz;
	private boolean fIgnoreWhitespace= false;
	private boolean fIgnoreLineDelimiter= true;
	private boolean fPreserveLineDelimiters= false;
	private boolean fReverse= false;
	private boolean fAdjustShift= true;
	private boolean fGenerateRejectFile = true;
	private Set disabledElements = new HashSet();
	private Map diffResults = new HashMap();
	private final Map contentCache = new HashMap();
	private final Map properties = new HashMap();
	private Set mergedHunks = new HashSet();
	
	public Patcher() {
		// nothing to do
	}
	
	/*
	 * Returns an array of Diffs after a sucessfull call to <code>parse</code>.
	 * If <code>parse</code> hasn't been called returns <code>null</code>.
	 */
	public FileDiff[] getDiffs() {
		if (fDiffs == null)
			return new FileDiff[0];
		return fDiffs;
	}
	
	public IPath getPath(FileDiff diff) {
		return diff.getStrippedPath(getStripPrefixSegments(), isReversed());
	}

	/*
	 * Returns <code>true</code> if new value differs from old.
	 */
	boolean setStripPrefixSegments(int strip) {
		if (strip != fStripPrefixSegments) {
			fStripPrefixSegments= strip;
			return true;
		}
		return false;
	}
	
	int getStripPrefixSegments() {
		return fStripPrefixSegments;
	}
	
	/*
	 * Returns <code>true</code> if new value differs from old.
	 */
	boolean setFuzz(int fuzz) {
		if (fuzz != fFuzz) {
			fFuzz= fuzz;
			return true;
		}
		return false;
	}
	
	int getFuzz(){
		return fFuzz;
	}
		
	/*
	 * Returns <code>true</code> if new value differs from old.
	 */
	boolean setIgnoreWhitespace(boolean ignoreWhitespace) {
		if (ignoreWhitespace != fIgnoreWhitespace) {
			fIgnoreWhitespace= ignoreWhitespace;
			return true;
		}
		return false;
	}
		
	//---- parsing patch files

	public void parse(IStorage storage) throws IOException, CoreException {
		BufferedReader reader = createReader(storage);
		try {
			parse(reader);
		} finally {
			try {
				reader.close();
			} catch (IOException e) { //ignored
			}
		}
	}
	
	private static BufferedReader createReader(IStorage storage) throws CoreException {
		return new BufferedReader(new InputStreamReader(storage.getContents()));
	}
	
	public void parse(BufferedReader reader) throws IOException {
		PatchReader patchReader= new PatchReader();
		patchReader.parse(reader);
		patchParsed(patchReader);
	}

	protected void patchParsed(PatchReader patchReader) {
		fDiffs = patchReader.getDiffs();
	}
	
	//---- applying a patch file

	public void applyAll(IProgressMonitor pm, Shell shell, String title) throws CoreException {

		final int WORK_UNIT= 10;
		
		int i;
		
		IFile singleFile= null;	// file to be patched
		IContainer container= null;
		if (fTarget instanceof IContainer)
			container= (IContainer) fTarget;
		else if (fTarget instanceof IFile) {
			singleFile= (IFile) fTarget;
			container= singleFile.getParent();
		} else {
			Assert.isTrue(false);
		}
		
		// get all files to be modified in order to call validateEdit
		List list= new ArrayList();
		if (singleFile != null)
			list.add(singleFile);
		else {
			for (i= 0; i < fDiffs.length; i++) {
				FileDiff diff= fDiffs[i];
				if (isEnabled(diff)) {
					switch (diff.getDiffType(isReversed())) {
					case Differencer.CHANGE:
						list.add(createPath(container, getPath(diff)));
						break;
					}
				}
			}
		}
		if (! Utilities.validateResources(list, shell, title))
			return;
		
		if (pm != null) {
			String message= PatchMessages.Patcher_Task_message;	
			pm.beginTask(message, fDiffs.length*WORK_UNIT);
		}
		
		for (i= 0; i < fDiffs.length; i++) {
			
			int workTicks= WORK_UNIT;
			
			FileDiff diff= fDiffs[i];
			if (isEnabled(diff)) {
				
				IPath path= getPath(diff);
				if (pm != null)
					pm.subTask(path.toString());
			
				IFile file= singleFile != null
								? singleFile
								: createPath(container, path);
					
				List failed= new ArrayList();
				List result= null;
				
				int type= diff.getDiffType(isReversed());
				switch (type) {
				case Differencer.ADDITION:
					// patch it and collect rejected hunks
					result= apply(diff, file, true, failed);
					store(createString(result), file, new SubProgressMonitor(pm, workTicks));
					workTicks-= WORK_UNIT;
					break;
				case Differencer.DELETION:
					file.delete(true, true, new SubProgressMonitor(pm, workTicks));
					workTicks-= WORK_UNIT;
					break;
				case Differencer.CHANGE:
					// patch it and collect rejected hunks
					result= apply(diff, file, false, failed);
					store(createString(result), file, new SubProgressMonitor(pm, workTicks));
					workTicks-= WORK_UNIT;
					break;
				}

				if (fGenerateRejectFile && failed.size() > 0) {
					IPath pp= null;
					if (path.segmentCount() > 1) {
						pp= path.removeLastSegments(1);
						pp= pp.append(path.lastSegment() + REJECT_FILE_EXTENSION);
					} else
						pp= new Path(path.lastSegment() + REJECT_FILE_EXTENSION);
					file= createPath(container, pp);
					if (file != null) {
						store(getRejected(failed), file, pm);
						try {
							IMarker marker= file.createMarker(MARKER_TYPE);
							marker.setAttribute(IMarker.MESSAGE, PatchMessages.Patcher_Marker_message);	
							marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
						} catch (CoreException ex) {
							// NeedWork
						}
					}
				}
			}
			
			if (pm != null) {
				if (pm.isCanceled())
					break;
				if (workTicks > 0)
					pm.worked(workTicks);
			}
		}
	}
	
	/*
	 * Reads the contents from the given file and returns them as
	 * a List of lines.
	 */
	List load(IFile file, boolean create) {
		List lines= null;
		if (!create && file != null) {
			// read current contents
			String charset = Utilities.getCharset(file);
			InputStream is= null;
			try {
				is= file.getContents();
				
				Reader streamReader= null;
				try {
					streamReader= new InputStreamReader(is, charset);
				} catch (UnsupportedEncodingException x) {
					// use default encoding
					streamReader= new InputStreamReader(is);
				}
				
				BufferedReader reader= new BufferedReader(streamReader);
				lines = readLines(reader);
			} catch(CoreException ex) {
				// TODO
				CompareUIPlugin.log(ex);
			} finally {
				if (is != null)
					try {
						is.close();
					} catch(IOException ex) {
						// silently ignored
					}
			}
		}
		
		if (lines == null)
			lines= new ArrayList();
		return lines;
	}

	private List readLines(BufferedReader reader) {
		List lines;
		LineReader lr= new LineReader(reader);
		if (!"carbon".equals(SWT.getPlatform()))	//$NON-NLS-1$
			lr.ignoreSingleCR();
		lines= lr.readLines();
		return lines;
	}
	
	List apply(FileDiff diff, IFile file, boolean create, List failedHunks) {
		FileDiffResult result = getDiffResult(diff);
		List lines = result.apply(file, create);
		failedHunks.addAll(result.getFailedHunks());
		return lines;
	}
	
	/*
	 * Converts the string into bytes and stores them in the given file.
	 */
	protected void store(String contents, IFile file, IProgressMonitor pm) throws CoreException {

		byte[] bytes;
		try {
			bytes= contents.getBytes(Utilities.getCharset(file));
		} catch (UnsupportedEncodingException x) {
			// uses default encoding
			bytes= contents.getBytes();
		}
		
		store(bytes,file, pm);
	}

	protected void store(byte[] bytes, IFile file, IProgressMonitor pm) throws CoreException {
		InputStream is= new ByteArrayInputStream(bytes);
		try {
			if (file.exists()) {
				file.setContents(is, false, true, pm);
			} else {
				file.create(is, false, pm);
			}
		} finally {
			if (is != null)
				try {
					is.close();
				} catch(IOException ex) {
					// silently ignored
				}
		}
	}

	
	
	/*
	 * Concatenates all strings found in the given List.
	 */
	protected String createString(List lines) {
		StringBuffer sb= new StringBuffer();
		Iterator iter= lines.iterator();
		if (fPreserveLineDelimiters) {
			while (iter.hasNext())
				sb.append((String)iter.next());
		} else {
			String lineSeparator= System.getProperty("line.separator"); //$NON-NLS-1$
			while (iter.hasNext()) {
				String line= (String)iter.next();
				int l= length(line);
				if (l < line.length()) {	// line has delimiter
					sb.append(line.substring(0, l));
					sb.append(lineSeparator);
				} else {
					sb.append(line);
				}
			}
		}
		return sb.toString();
	}

	String getRejected(List failedHunks) {
		if (failedHunks.size() <= 0)
			return null;
		
		String lineSeparator= System.getProperty("line.separator"); //$NON-NLS-1$
		StringBuffer sb= new StringBuffer();
		Iterator iter= failedHunks.iterator();
		while (iter.hasNext()) {
			Hunk hunk= (Hunk) iter.next();
			sb.append(hunk.getRejectedDescription());
			sb.append(lineSeparator);
			sb.append(hunk.getContent());
		}
		return sb.toString();
	}
	
	/*
	 * Ensures that a file with the given path exists in
	 * the given container. Folder are created as necessary.
	 */
	protected IFile createPath(IContainer container, IPath path) throws CoreException {
		if (path.segmentCount() > 1) {
			IContainer childContainer;
			if (container instanceof IWorkspaceRoot) {
				IProject project = ((IWorkspaceRoot)container).getProject(path.segment(0));
				if (!project.exists())
					project.create(null);
				if (!project.isOpen())
					project.open(null);
				childContainer = project;
			} else {
				IFolder f= container.getFolder(path.uptoSegment(1));
				if (!f.exists())
					f.create(false, true, null);
				childContainer = f;
			}
			return createPath(childContainer, path.removeFirstSegments(1));
		}
		// a leaf
		return container.getFile(path);
	}
	
	/*
	 * Returns the length (excluding a line delimiter CR, LF, CR/LF)
	 * of the given string.
	 */
	/* package */ static int length(String s) {
		int l= s.length();
		if (l > 0) {
			char c= s.charAt(l-1);
			if (c == '\r')
				return l-1;
			if (c == '\n') {
				if (l > 1 && s.charAt(l-2) == '\r')
					return l-2;
				return l-1;
			}
		}
		return l;
	}

	public IResource getTarget() {
		return fTarget;
	}

	public void setTarget(IResource target) {
		fTarget= target;
	}
	

	protected IFile getTargetFile(FileDiff diff) {
		IPath path = diff.getStrippedPath(getStripPrefixSegments(), isReversed());
		return existsInTarget(path);
	}
	
	/**
	 * Iterates through all of the resources contained in the Patch Wizard target
	 * and looks to for a match to the passed in file 
	 * @param path
	 * @return IFile which matches the passed in path or null if none found
	 */
	public IFile existsInTarget(IPath path) {
		if (fTarget instanceof IFile) { // special case
			IFile file= (IFile) fTarget;
			if (matches(file.getFullPath(), path))
				return file;
		} else if (fTarget instanceof IContainer) {
			IContainer c= (IContainer) fTarget;
			if (c.exists(path))
				return c.getFile(path);
		}
		return null;
	}

	/**
	 * Returns true if path completely matches the end of fullpath
	 * @param fullpath 
	 * @param path 
	 * @return true if path matches, false otherwise
	 */
	private boolean matches(IPath fullpath, IPath path) {
		for (IPath p= fullpath; path.segmentCount()<=p.segmentCount(); p= p.removeFirstSegments(1)) {
			if (p.equals(path))
				return true;
		}
		return false;
	}

	public int calculatePrefixSegmentCount() {
		//Update prefix count - go through all of the diffs and find the smallest
		//path segment contained in all diffs.
		int length= 99;
		if (fDiffs!=null)
			for (int i= 0; i<fDiffs.length; i++) {
				FileDiff diff= fDiffs[i];
				length= Math.min(length, diff.segmentCount());
			}
		return length;
	}
	
	public void setGenerateRejects(boolean generateRejects){
		this.fGenerateRejectFile = generateRejects;
	}
	
	public void addDiff(FileDiff newDiff){
		FileDiff[] temp = new FileDiff[fDiffs.length + 1];
		System.arraycopy(fDiffs,0, temp, 0, fDiffs.length);
		temp[fDiffs.length] = newDiff;
		fDiffs = temp;
	}
	
	public void removeDiff(FileDiff diffToRemove){
		FileDiff[] temp = new FileDiff[fDiffs.length - 1];
		int counter = 0;
		for (int i = 0; i < fDiffs.length; i++) {
			if (fDiffs[i] != diffToRemove){
				temp[counter++] = fDiffs[i];
			}
		}
		fDiffs = temp;
	}
	
	public void setEnabled(Object element, boolean enabled) {
		if (enabled) {
			disabledElements.remove(element);
		} else {
			disabledElements.add(element);
		}
	}

	public boolean isEnabled(Object element) {
		if (disabledElements.contains(element)) 
			return false;
		Object parent = getElementParent(element);
		if (parent == null)
			return true;
		return isEnabled(parent);
	}

	protected Object getElementParent(Object element) {
		if (element instanceof Hunk) {
			Hunk hunk = (Hunk) element;
			return hunk.getParent();
		}
		return null;
	}

	public boolean isGenerateRejectFile() {
		return fGenerateRejectFile;
	}
	
	/**
	 * Calculate the fuzz factor that will allow the most hunks to be matched.
	 * @param monitor a progress monitor
	 * @return the fuzz factor or <code>-1</code> if no hunks could be matched
	 */
	public int guessFuzzFactor(IProgressMonitor monitor) {
		try {
			monitor.beginTask(PatchMessages.PreviewPatchPage_GuessFuzzProgress_text, IProgressMonitor.UNKNOWN);
			FileDiff[] diffs= getDiffs();
			if (diffs==null||diffs.length<=0)
				return -1;
			int fuzz= -1;
			for (int i= 0; i<diffs.length; i++) {
				FileDiff d= diffs[i];
				IFile file= getTargetFile(d);
				if (file != null && file.exists()) {
					List lines= load(file, false);
					FileDiffResult result = getDiffResult(d);
					int f = result.calculateFuzz(lines, monitor);
					if (f > fuzz)
						fuzz = f;
				}
			}
			return fuzz;
		} finally {
			monitor.done();
		}
	}
	
	public void refresh() {
		diffResults.clear();
		refresh(getDiffs());
	}
	
	protected void refresh(FileDiff[] diffs) {
		for (int i = 0; i < diffs.length; i++) {
			FileDiff diff = diffs[i];
			FileDiffResult result = getDiffResult(diff);
			result.refresh();
		}
	}
	
	public FileDiffResult getDiffResult(FileDiff diff) {
		FileDiffResult result = (FileDiffResult)diffResults.get(diff);
		if (result == null) {
			result = new FileDiffResult(diff, this);
			diffResults.put(diff, result);
		}
		return result;
	}

	public boolean isAdjustShift() {
		return fAdjustShift;
	}

	/**
	 * Return the project that contains this diff or <code>null</code>
	 * if the patch is not a workspace patch.
	 * @param diff the diff
	 * @return the project that contains the diff
	 */
	public DiffProject getProject(FileDiff diff) {
		return diff.getProject();
	}

	/*
	 * Returns <code>true</code> if new value differs from old.
	 */
	public boolean setReversed(boolean reverse) {
		if (fReverse != reverse) {
			fReverse= reverse;
			refresh();
			return true;
		}
		return false;
	}
	
	public boolean isReversed() {
		return fReverse;
	}

	public boolean isIgnoreWhitespace() {
		return fIgnoreWhitespace;
	}

	public boolean isIgnoreLineDelimiter() {
		return fIgnoreLineDelimiter;
	}
	
	/**
	 * Cache the contents for the given file diff. These contents
	 * will be used for the diff when the patch is applied. When the
	 * patch is applied, it is assumed that the provided contents 
	 * already have all relevant hunks applied.
	 * @param diff the file diff
	 * @param contents the contents for the file diff
	 */
	public void cacheContents(FileDiff diff, byte[] contents) {
		contentCache.put(diff, contents);
	}
	
	/**
	 * Return whether contents have been cached for the 
	 * given file diff.
	 * @param diff the file diff
	 * @return whether contents have been cached for the file diff
	 * @see #cacheContents(FileDiff, byte[])
	 */
	public boolean hasCachedContents(FileDiff diff) {
		return contentCache.containsKey(diff);
	}

	/**
	 * Return the content lines that are cached for the given 
	 * file diff.
	 * @param diff the file diff
	 * @return the content lines that are cached for the file diff
	 */
	public List getCachedLines(FileDiff diff) {
		byte[] contents = (byte[])contentCache.get(diff);
		if (contents != null) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(contents)));
			return readLines(reader);
		}
		return null;
	}

	/**
	 * Return the contents that are cached for the given diff or
	 * <code>null</code> if there is no contents cached.
	 * @param diff the diff
	 * @return the contents that are cached for the given diff or
	 * <code>null</code>
	 */
	public byte[] getCachedContents(FileDiff diff) {
		return (byte[])contentCache.get(diff);
	}
	
	/**
	 * Return whether the patcher has any cached contents.
	 * @return whether the patcher has any cached contents
	 */
	public boolean hasCachedContents() {
		return !contentCache.isEmpty();
	}

	/**
	 * Clear any cached contents.
	 */
	public void clearCachedContents() {
		contentCache.clear();
		mergedHunks.clear();
	}
	
	public void setProperty(String key, Object value) {
		properties.put(key, value);
	}
	
	public Object getProperty(String key) {
		return properties.get(key);
	}

	public boolean isManuallyMerged(Hunk hunk) {
		return mergedHunks.contains(hunk);
	}

	public void setManuallyMerged(Hunk hunk, boolean merged) {
		if (merged)
			mergedHunks.add(hunk);
		else 
			mergedHunks.remove(hunk);
	}

	public IProject getTargetProject(FileDiff diff) {
		DiffProject dp = getProject(diff);
		if (dp != null)
			return dp.getProject();
		IResource tr = getTarget();
		if (tr instanceof IWorkspaceRoot) {
			IWorkspaceRoot root = (IWorkspaceRoot) tr;
			return root.getProject(diff.getPath(isReversed()).segment(0));
		}
		return tr.getProject();
	}
}
