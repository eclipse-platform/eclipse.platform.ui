package org.eclipse.team.tests.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import junit.framework.Assert;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.team.internal.ccvs.core.CVSStatus;
import org.eclipse.ui.internal.dialogs.InternalErrorDialog;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.eclipse.ui.wizards.datatransfer.ZipFileStructureProvider;

/**
 * Provides helpers for:
 * <ul>
 *   <li>Resource manipulation</li>
 *   <li>Diff trees</li>
 *   <li>UI automation</li>
 *   <li>Parallel development simulation</li>
 * </ul>
 * 
 * Note: This class is referenced from the VCM 1.0 performance tests.
 */public class Util {
	/*** RESOURCE MANIPULATION SUPPORT ***/
	
	/**
	 * Gets a handle for a project of a given name.
	 * @param name the project name
	 * @return the project handle
	 */
	public static IProject getProject(String name) throws CoreException {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
	}
	
	/**
	 * Creates a new project.
	 * @param name the project name
	 * @return the project handle
	 */
	public static IProject createProject(String name) throws CoreException {
		IProject project = getProject(name);
		if (!project.exists()) project.create(null);
		if (!project.isOpen()) project.open(null);
		return project;
	}

	/**
	 * Deletes a project.
	 * @param project the project
	 */
	public static void deleteProject(IProject project) throws CoreException {
		project.delete(false /*force*/, null);
	}
	
	/**
	 * Deletes a file and prunes empty containing folders.
	 * @param file the file to delete
	 */
	public static void deleteFileAndPrune(IFile file) throws CoreException {
		file.delete(false /*force*/, null);
		IContainer container = file.getParent();
		while (container != null && container instanceof IFolder &&
			isFolderEmpty((IFolder) container)) {
			deleteFolder((IFolder) container);
			container = container.getParent();
		}
	}
	
	/**
	 * Deletes a folder.
	 */
	public static void deleteFolder(IFolder folder) throws CoreException {
		try {
			folder.delete(false /*force*/, null);
		} catch (CoreException e) {
			IStatus status = e.getStatus();
			// ignore errors caused by attempting to delete folders that CVS needs to have around
			if (findStatusByCode(status, CVSStatus.FOLDER_NEEDED_FOR_FILE_DELETIONS) == null) {
				throw e;
			}
		}
	}
	
	/**
	 * Finds an IStatus instance in a multi-status by status code.
	 */
	public static IStatus findStatusByCode(IStatus status, int code) {
		if (status.getCode() == code) return status;
		IStatus[] children = status.getChildren();
		for (int i = 0; i < children.length; i++) {
			IStatus found = findStatusByCode(children[i], code);
			if (found != null) return found;
		}
		return null;
	}

	/**
	 * Creates a uniquely named project.
	 * @param prefix a string prepended to the generated name
	 * @return the new project
	 */
	public static IProject createUniqueProject(String prefix) throws CoreException {
		return createProject(makeUniqueName(null, prefix, null));
	}

	/**
	 * Creates a uniquely named file in the parent folder or project with random contents.
	 * @param gen the sequence generator
	 * @param parent the parent IFolder or IProject for the new file
	 * @param meanSize the mean size of file to create (in bytes)
	 * @param variance 69% of files with be within this amount of the mean
	 * @param probBinary the probability of a new file being binary as a percentage
	 * @return the new file
	 */
	public static IFile createUniqueFile(SequenceGenerator gen, IContainer parent,
		int meanSize, int variance, int probBinary) throws IOException, CoreException {
		int fileSize;
		do {
			fileSize = (int) Math.abs(gen.nextGaussian() * variance + meanSize);
		} while (fileSize > meanSize + variance * 4); // avoid huge files
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		String fileName;
		if (gen.nextInt(100) < probBinary) {
			fileName = makeUniqueName(gen, "file", "class"); // binary
			writeRandomBytes(gen, os, fileSize);
		} else {
			fileName = makeUniqueName(gen, "file", "txt"); // text
			writeRandomText(gen, os, fileSize);
		}
		IFile file = parent.getFile(new Path(fileName));
		file.create(new ByteArrayInputStream(os.toByteArray()), true, null);
		os.close();
		return file;
	}

	/**
	 * Creates a uniquely named folder in the parent folder.
	 * @param gen the sequence generator
	 * @param parent the parent IFolder or IProject for the new folder
	 * @return the new folder
	 */
	public static IFolder createUniqueFolder(SequenceGenerator gen, IContainer parent) throws CoreException {
		IFolder folder = parent.getFolder(new Path(Util.makeUniqueName(gen, "folder", null)));
		folder.create(false /*force*/, true /*local*/, null);
		return folder;
	}
	
	/**
	 * Renames a resource.
	 * The resource handle becomes invalid.
	 * @param resource the existing resource
	 * @param newName the new name for the resource
	 */
	public static void renameResource(IResource resource, String newName) throws CoreException {
		switch (resource.getType()) {
			case IResource.PROJECT: {
				IProject project = (IProject) resource;
				IProjectDescription desc = project.getDescription();
				desc.setName(newName);
				project.move(desc, false /*force*/, true /*keepHistory*/, null);
			} break;
			case IResource.FOLDER: {
				try {
					resource.move(new Path(newName), false /*force*/, null);
				} catch (CoreException e) {
					IStatus status = e.getStatus();
					// ignore errors caused by attempting to delete folders that CVS needs to have around
					if (findStatusByCode(status, CVSStatus.FOLDER_NEEDED_FOR_FILE_DELETIONS) == null) {
						throw e;
					}
				}
			} break;
			default:
				resource.move(new Path(newName), false /*force*/, null);
				break;
		}
	}

	/**
	 * Modified a resource.
	 * @param gen the sequence generator
	 * @param file the file to modify
	 */
	public static void modifyFile(SequenceGenerator gen, IFile file)
		throws IOException, CoreException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			InputStream is = file.getContents(true);
			try {
				byte[] buffer = new byte[8192];
				int rsize;
				boolean changed = false;
				while ((rsize = is.read(buffer)) != -1) {
					double gaussian;
					do {
						gaussian = gen.nextGaussian() * 0.5; // large changes are less likely than small ones
					} while (gaussian > 1.0 || gaussian < -1.0);
					int changeSize = (int) (gaussian * rsize);
					changed = changed || changeSize != 0;
					os.write(buffer, 0, changeSize < 0 ? - changeSize : rsize); // shrink file
					writeRandomText(gen, os, changeSize); // enlarge file
				}
				if (! changed) os.write('!'); // make sure we actually did change the file
				file.setContents(new ByteArrayInputStream(os.toByteArray()), false /*force*/, true /*keepHistory*/, null);
			} finally {
				is.close();
			}
		} finally {
			os.close();
		}
	}
	
	/**
	 * Creates a unique name.
	 * Ensures that a deterministic sequence of names is generated for all files
	 * and folders within a project, though not across sessions.
	 * 
	 * @param gen the generator, or null if this name is to be globally unique
	 * @param prefix a string prepended to the generated name
	 * @param extension the file extension not including the period, null if none
	 * @return the new name
	 */
	public static String makeUniqueName(SequenceGenerator gen, String prefix, String extension)
		throws CoreException {
		StringBuffer name = new StringBuffer(prefix);
		name.append('-');
		if (gen == null) {
			name.append(SequenceGenerator.nextGloballyUniqueLong());
		} else {
			name.append(gen.nextUniqueInt());
		}
		if (extension != null) {
			name.append('.');
			name.append(extension);
		}
		return name.toString();
	}
	
	/**
	 * Imports a .zip file into a project's root folder.
	 * @param project the project
	 * @param file the path of the .zip file
	 */
	public static void importZipIntoProject(IProject project, File file)
		throws IOException, ZipException, InterruptedException, InvocationTargetException {
		ZipFile zipFile = new ZipFile(file);
		ZipFileStructureProvider provider = new ZipFileStructureProvider(zipFile);
		ImportOperation importOperation = new ImportOperation(project.getFullPath(),
			provider.getRoot(), provider, null);
		importOperation.setOverwriteResources(true); // don't ask
		importOperation.run(new NullProgressMonitor());
		Assert.assertTrue(importOperation.getStatus().isOK());
	}

	/**
	 * Writes random text to an output stream.
	 * @param gen the sequence generator
	 */
	public static void writeRandomText(SequenceGenerator gen, OutputStream os, int count) throws IOException {
		while (count-- > 0) {
			int c = gen.nextInt(99);
			os.write((c >= 95) ? '\n' : c + ' ');
		}
	}

	/**
	 * Writes random bytes to an output stream.
	 * @param gen the sequence generator
	 */
	public static void writeRandomBytes(SequenceGenerator gen, OutputStream os, int count) throws IOException {
		while (count-- > 0) {
			os.write(gen.nextInt(256));
		}
	}

	/**
	 * Creates a random folder deeply below the root folder.
	 * @param gen the sequence generator
	 * @param root the root IFolder or IProject for the operation
	 * @return the new folder
	 */
	public static IFolder createRandomDeepFolder(SequenceGenerator gen, IContainer root) throws CoreException {
		IContainer container = pickRandomDeepContainer(gen, root);
		for (;;) {
			IFolder folder = createUniqueFolder(gen, container);
			container = folder;
			// 12.5% chance of creating a nested folder
			if (gen.nextInt(8) != 0) return folder;
		}
	}
	
	/**
	 * Creates several random files deeply below the root folder.
	 * @param gen the sequence generator
	 * @param root the root IFolder or IProject for the operation
	 * @param count the number of files to create
	 * @param meanSize the mean size of file to create (in bytes)
	 * @param probBinary the probability of a new file being binary as a percentage
	 */
	public static void createRandomDeepFiles(SequenceGenerator gen, IContainer root, int count,
		int meanSize, int variance, int probBinary) throws IOException, CoreException  {
		while (count-- > 0) {
			createUniqueFile(gen, pickRandomDeepContainer(gen, root), meanSize, variance, probBinary);
		}
	}

	/**
	 * Deletes several random files deeply below the root folder.
	 * @param gen the sequence generator
	 * @param root the root IFolder or IProject for the operation
	 * @param count the number of files to delete
	 */
	public static void deleteRandomDeepFiles(SequenceGenerator gen, IContainer root, int count) throws CoreException  {
		while (count-- > 0) {
			IFile file = pickRandomDeepFile(gen, root);
			if (file == null) break;
			deleteFileAndPrune(file);
		}
	}

	/**
	 * Modifies several random files deeply below the root folder.
	 * @param gen the sequence generator
	 * @param root the root IFolder or IProject for the operation
	 * @param count the number of files to modify
	 */
	public static void modifyRandomDeepFiles(SequenceGenerator gen, IContainer root, int count)
		throws IOException, CoreException  {
		// perhaps we can add a parameter for the "magnitude" of the change
		while (count-- > 0) {
			IFile file = pickRandomDeepFile(gen, root);
			if (file == null) break;
			modifyFile(gen, file);
		}
	}
	
	/**
	 * Touches several random files deeply below the root folder.
	 * @param gen the sequence generator
	 * @param root the root IFolder or IProject for the operation
	 * @param count the number of files to touch
	 */
	public static void touchRandomDeepFiles(SequenceGenerator gen, IContainer root, int count) throws CoreException  {
		while (count-- > 0) {
			IFile file = pickRandomDeepFile(gen, root);
			if (file == null) break;
			file.touch(null);
		}
	}
	
	/**
	 * Renames several random files deeply below the root folder.
	 * @param gen the sequence generator
	 * @param root the root IFolder or IProject for the operation
	 * @param count the number of files to touch
	 */
	public static void renameRandomDeepFiles(SequenceGenerator gen, IContainer root, int count) throws CoreException  {
		IProject project = root.getProject();
		while (count-- > 0) {
			IFile file = pickRandomDeepFile(gen, root);
			if (file == null) break;
			renameResource(file, makeUniqueName(gen, "file", file.getFileExtension()));
		}
	}
	
	/**
	 * Picks a random file from the parent folder or project.
	 * @param gen the sequence generator
	 * @param parent the parent IFolder or IProject for the operation
	 * @return the file that was chosen, or null if no suitable files
	 */
	public static IFile pickRandomFile(SequenceGenerator gen, IContainer parent) throws CoreException  {
		IResource[] members = filterResources(parent.members());
		for (int size = members.length; size != 0; --size) {
			int elem = gen.nextInt(size);
			if (members[elem] instanceof IFile) return (IFile) members[elem];			
			System.arraycopy(members, elem + 1, members, elem, size - elem - 1);
		}
		return null;
	}

	/**
	 * Picks a random folder from the parent folder or project.
	 * @param gen the sequence generator
	 * @param parent the parent IFolder or IProject for the operation
	 * @return the folder, or null if no suitable folders
	 */
	public static IFolder pickRandomFolder(SequenceGenerator gen, IContainer parent) throws CoreException {
		IResource[] members = filterResources(parent.members());
		for (int size = members.length; size != 0; --size) {
			int elem = gen.nextInt(size);
			if (members[elem] instanceof IFolder) return (IFolder) members[elem];
			System.arraycopy(members, elem + 1, members, elem, size - elem - 1);
		}
		return null;
	}

	/**
	 * Picks a random file deeply from the root folder or project.
	 * @param gen the sequence generator
	 * @param root the root IFolder or IProject for the operation
	 * @return the file that was chosen, or null if no suitable files
	 */
	public static IFile pickRandomDeepFile(SequenceGenerator gen, IContainer root) throws CoreException  {
		IResource[] members = filterResources(root.members());
		for (int size = members.length; size != 0; --size) {
			int elem = gen.nextInt(size);
			IResource resource = members[elem];
			if (resource instanceof IFile) return (IFile) resource;
			if (resource instanceof IFolder) {
				IFile file = pickRandomDeepFile(gen, (IFolder) resource);
				if (file != null) return file;
			}
			System.arraycopy(members, elem + 1, members, elem, size - elem - 1);
		}
		return null;
	}

	/**
	 * Picks a random folder deeply from the root folder or project.
	 * May pick the project's root container.
	 * @param gen the sequence generator
	 * @param root the root IFolder or IProject for the operation
	 * @return the container that was chosen, never null
	 */
	public static IContainer pickRandomDeepContainer(SequenceGenerator gen, IContainer root) throws CoreException {
		if (gen.nextInt(6) == 0) {
			IResource[] members = filterResources(root.members());
			for (int size = members.length; size != 0; --size) {
				int elem = gen.nextInt(size);
				IResource resource = members[elem];
				if (resource instanceof IFolder) {
					return pickRandomDeepContainer(gen, (IFolder) resource);
				}
				System.arraycopy(members, elem + 1, members, elem, size - elem - 1);
			}
		}
		Assert.assertTrue(isValidContainer(root));
		return root;
	}
	
	/**
	 * Returns true if the folder does not contain any real files.
	 */
	public static boolean isFolderEmpty(IFolder folder) throws CoreException {
		IResource[] members = folder.members();
		for (int i = 0; i < members.length; ++i) {
			if (isValidFile(members[i]) || isValidFolder(members[i])) return false;
		}
		return true;
	}

	/**
	 * Returns true iff file is a valid IFile (that should not be ignored).
	 */
	public static boolean isValidFile(IResource file) throws CoreException {
		String name = file.getName();
		return file instanceof IFile
			&& ! file.isPhantom()
			&& ! name.equals(".classpath")
			&& ! name.equals(".project")
			&& ! name.equals(".vcm_meta");
	}

	/**
	 * Returns true iff folder is a valid IFolder (that should not be ignored).
	 */
	public static boolean isValidFolder(IResource folder) throws CoreException {
		String name = folder.getName();
		return folder instanceof IFolder
			&& ! folder.isPhantom()
			&& ! name.equals("CVS")
			&& ! name.equals("bin");
	}

	/**
	 * Returns true iff container is a valid IFolder or IProject (that should not be ignored).
	 */
	public static boolean isValidContainer(IResource container) throws CoreException {
		return container instanceof IProject || isValidFolder(container);
	}
	
	/**
	 * Returns true iff resource is a valid IFile, IFolder or IProject (that should not be ignored).
	 */
	public static boolean isValidResource(IResource resource) throws CoreException {
		return isValidFile(resource) || isValidContainer(resource);
	}

	/**
	 * Filters and sorts an array of resources to ensure deterministic behaviour across
	 * sessions.  The general idea is to guarantee that given a known sequence of
	 * pseudo-random numbers, we will always pick the same sequence of files and
	 * folders each time we repeat the test.
	 */
	public static IResource[] filterResources(IResource[] resources) throws CoreException {
		List list = new ArrayList(resources.length);
		for (int i = 0; i < resources.length; ++i) {
			if (isValidResource(resources[i])) list.add(resources[i]);
		}
		if (list.size() != resources.length) {
			resources = (IResource[]) list.toArray(new IResource[list.size()]);
		}
		Arrays.sort(resources, new Comparator() {
			public int compare(Object a, Object b) {
				return ((IResource) a).getName().compareTo(((IResource) b).getName());
			}
		});
		return resources;
	}
	
	/*** DIFF SUPPORT ***/
	
	public static boolean isEmpty(IDiffContainer node) {
		if (node == null) return true;
		if (node.getKind() != 0) return false;
		IDiffElement[] children = node.getChildren();
		for (int i = 0; i < children.length; i++) {
			if (!isEmpty(children[i])) return false;
		}
		return true;
	}
	public static boolean isEmpty(IDiffElement element) {
		if (element == null) return true;
		if (element.getKind() != 0) return false;
		if (element instanceof IDiffContainer) {
			IDiffElement[] children = ((DiffNode)element).getChildren();
			for (int i = 0; i < children.length; i++) {
				if (!isEmpty(children[i])) return false;
			}
		}
		return true;
	}
	
	/*** UI SUPPORT ***/
	
	/**
	 * Opens the specified wizard, then notifies the waiter.
	 * The WizardDialog instance is passed as argument to notify() in the waiter.
	 */
	public static void waitForWizardToOpen(Shell parent, IWizard wizard, final Waiter waiter) {
		WizardDialog dialog = new WizardDialog(parent, wizard) {
			public int open() {
				// create the window's controls
				create();
				// hook into the event loop so we get called back when the wizard is up and running
				final Display display = getContents().getDisplay();
				final WizardDialog dialog = this;
				display.asyncExec(new Runnable() {
					public void run() {
						while (display.readAndDispatch()); // process any other pending messages first
						waiter.notify(dialog);
					}
				});
				// call open (does not create the window's controls a second time)
				return super.open();
			}
		};
		dialog.open();
	}
	
	/**
	 * Notifies the waiter when a Shell matching the specified criteria opens.
	 * The Shell instance is passed as argument to notify() in the waiter.
	 * 
	 * @param display the root display
	 * @param pollingPeriod the number of milliseconds to wait between polls
	 * @param value a value used for matching
	 * @param criteria a strategy for matching the controls against a value,
	 *           or null to match any Shell.
	 * @param waiter the waiter to be notified
	 */
	public static void waitForShellToOpen(final Display display, final int pollingPeriod,
		final Object value, final ICriteria criteria, final Waiter waiter) {
		final Runnable hook = new Runnable() {
			public void run() {
				if (display.isDisposed()) return;
				Shell[] shells = display.getShells();
				for (int i = 0; i < shells.length; ++i) {
					Shell shell = shells[i];
					if (criteria != null && ! criteria.test(shell, value)) continue;
					if (! waiter.notify(shell)) return;
				}
				// poll again as soon as possible
				if (waiter.keepWaiting()) {
					display.timerExec(pollingPeriod, this);
				}
			}
		};
		hook.run();
	}

	
	/**
	 * Installs a watchdog for JFace error dialogs for the current display.
	 * The Dialog instance is passed as argument to notify() in the waiter.
	 * Recognized dialogs:
	 *   - ErrorDialog
	 *   - InternalErrorDialog
	 * 
	 * @param display the root display
	 * @param pollingPeriod the number of milliseconds to wait between polls
	 * @param waiter the waiter to be notified
	 */
	public static void waitForErrorDialog(Display display, int pollingPeriod, final Waiter waiter) {
		ICriteria criteria = new ICriteria() {
			public boolean test(Object candidate, Object value) {
				Shell shell = (Shell) candidate;
				if (shell.isDisposed()) return false;
				Object data = shell.getData();
				if (data == null) return false;
				return data instanceof ErrorDialog || data instanceof InternalErrorDialog;
			}
		};
		waitForShellToOpen(display, pollingPeriod, null, criteria, new Waiter() {
			public boolean keepWaiting() {
				return waiter.keepWaiting();
			}

			public boolean notify(Object object) {
				return waiter.notify(((Shell) object).getData());
			}
		});
	}

	/**
	 * Finds a Control in a Composite hierarchy matching the specified criteria.
	 * 
	 * @param root the root of the hierarchy to search
	 * @param clazz the Class representing the precise type of Control to find
	 * @param value a value used for matching
	 * @param criteria a strategy for matching the controls against a value,
	 *           or null to match anything of the right class.
	 * @return the first matching Control, or null if none found.
	 */
	public static Control findControl(Composite root, Class clazz, Object value, ICriteria criteria) {
		if (clazz.isAssignableFrom(root.getClass())) {
			if (criteria == null || criteria.test(root, value)) return root;
		}
		Control[] children = root.getChildren();
		for (int i = 0; i < children.length; ++i) {
			final Control candidate = children[i];
			if (candidate instanceof Composite) {
				Control c = findControl((Composite) candidate, clazz, value, criteria);
				if (c != null) return c;
			} else {
				if (clazz.isAssignableFrom(candidate.getClass())) {
					if (criteria == null || criteria.test(candidate, value)) return candidate;
				}
			}
		}
		return null;
	}
	
	/**
	 * Finds a Control in a Composite hierarchy with the specified text string.
	 * Note: clazz must specify a Control subclass that defines getText()
	 * 
	 * @param root the root of the hierarchy to search
	 * @param clazz the Class representing the precise type of Control to find
	 * @param text the text string to find
	 * @return the first matching Control, or null if none found.
	 */
	public static Control findControlWithText(Composite root, Class clazz, String text) {
		return findControl(root, clazz, text, new ICriteria() {
			public boolean test(Object control, Object value) {
				// getText is only defined on certain subclasses of Composite
				// so we must use reflection to find the method
				try {
					Method m = control.getClass().getMethod("getText", new Class[0]);
					String text = (String) m.invoke(control, new Object[0]);
					return value.equals(stripMnemonicEscapes(text));
				} catch (Exception e) {
					e.printStackTrace();
					Assert.fail("Could not invoke method getText()");
				}
				return false;
			}
		});
	}

	/**
	 * Posts a fake event to the queue.
	 * Fills in the event type and widget fields.
	 * @param event the Event
	 */
	public static void postEvent(final Widget widget, final int eventType, final Event event) {
		Display display = widget.getDisplay();
		event.type = eventType;
		event.widget = widget;
		display.asyncExec(new Runnable() {
			public void run() {
				widget.notifyListeners(eventType, event);
			}
		});
	}
	
	/**
	 * Strips mnemonic escapes from a text label.
	 */
	public static String stripMnemonicEscapes(String label) {
		StringBuffer buf = new StringBuffer();
		int length = label.length();
		for (int i = 0; i < length; ++i) {
			char c = label.charAt(i);
			if (c == '&') {
				i += 1;
				if (i < length) c = label.charAt(i);
			}
			buf.append(c);
		}
		return buf.toString();
	}
	
	/**
	 * Process pending events for the current display, until at least the
	 * specified number of milliseconds elapses.
	 */
	public static void processEventsUntil(int hiatus) {
		Display display = Display.getCurrent();
		Assert.assertNotNull(display);
		final boolean done[] = new boolean[] { hiatus == 0 };
		if (hiatus != 0) display.timerExec(hiatus, new Runnable() {
			public void run() { done[0] = true; }
		});
		for (;;) {
			while (display.readAndDispatch());
			if (done[0]) return;
			display.sleep();
		}
	}
	
	/**
	 * Process pending events for the current display, until resumed by the user.
	 * Very useful for inspecting intermediate results while debugging.
	 */
	public static void processEventsUntilResumed(String title) {
		Display display = Display.getCurrent();
		Assert.assertNotNull(display);
		Shell shell = new Shell(display, SWT.CLOSE);
		shell.setText("Close me to resume: " + title);
		shell.setBounds(0, 0, 500, 30);
		shell.open();
		while (! shell.isDisposed()) {
			while (! display.readAndDispatch()) display.sleep();
		}
	}
}
