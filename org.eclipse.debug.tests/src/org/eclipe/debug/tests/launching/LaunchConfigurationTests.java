/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipe.debug.tests.launching;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.core.LaunchConfiguration;
import org.eclipse.debug.internal.core.LaunchManager;
import org.eclipse.debug.tests.TestsPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Tests for launch configurations
 */
public class LaunchConfigurationTests extends AbstractLaunchTest implements ILaunchConfigurationListener {
	
	/**
	 * Identifier of test launch configuration type extension
	 */
	public static final String ID_TEST_LAUNCH_TYPE = "org.eclipse.debug.tests.launch.type"; //$NON-NLS-1$
	
	/**
	 * The from/to handles during rename operations
	 */
	protected ILaunchConfiguration fFrom;
	protected ILaunchConfiguration fTo;
	
	protected Object fLock = new Object();
	protected ILaunchConfiguration fAdded;
	protected ILaunchConfiguration fRemoved;
	
	/**
	 * Class to hold resource description infos
	 * @since 3.9.0
	 */
	class ResourceItem {
		public ResourceItem(String path, Integer type) {
			this.path = path;
			this.type = type;
		}
		String path;
		Integer type;
	}
	
	class Listener implements ILaunchConfigurationListener {
		
		private final List addedList = new ArrayList();
		private final List removedList = new ArrayList();
		private final List changedList = new ArrayList();

		/* (non-Javadoc)
		 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationAdded(org.eclipse.debug.core.ILaunchConfiguration)
		 */
		public void launchConfigurationAdded(ILaunchConfiguration configuration) {
			addedList.add(configuration);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationChanged(org.eclipse.debug.core.ILaunchConfiguration)
		 */
		public void launchConfigurationChanged(ILaunchConfiguration configuration) {
			changedList.add(configuration);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationRemoved(org.eclipse.debug.core.ILaunchConfiguration)
		 */
		public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
			removedList.add(configuration);
		}
		
		public List getAdded() {
			return addedList;
		}
		public List getChanged() {
			return changedList;
		}
		public List getRemoved() {
			return removedList;
		}
		
	}
	
	/**
	 * Returns the given input stream's contents as a character array.
	 * If a length is specified (i.e. if length != -1), this represents the number of bytes in the stream.
	 * Note the specified stream is not closed in this method
	 * @param stream the stream to get convert to the char array 
	 * @return the given input stream's contents as a character array.
	 * @throws IOException if a problem occurred reading the stream.
	 */
	public static char[] getInputStreamAsCharArray(InputStream stream) throws IOException {
		Charset charset = null;
		try {
			charset = Charset.forName("UTF-8"); //$NON-NLS-1$
		} catch (IllegalCharsetNameException e) {
			System.err.println("Illegal charset name : " + "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		} catch(UnsupportedCharsetException e) {
			System.err.println("Unsupported charset : " + "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
		CharsetDecoder charsetDecoder = charset.newDecoder();
		charsetDecoder.onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
		byte[] contents = getInputStreamAsByteArray(stream, -1);
		ByteBuffer byteBuffer = ByteBuffer.allocate(contents.length);
		byteBuffer.put(contents);
		byteBuffer.flip();
		return charsetDecoder.decode(byteBuffer).array();
	}
	
	/**
	 * Returns the given input stream as a byte array
	 * @param stream the stream to get as a byte array
	 * @param length the length to read from the stream or -1 for unknown
	 * @return the given input stream as a byte array
	 * @throws IOException
	 */
	public static byte[] getInputStreamAsByteArray(InputStream stream, int length) throws IOException {
		byte[] contents;
		if (length == -1) {
			contents = new byte[0];
			int contentsLength = 0;
			int amountRead = -1;
			do {
				// read at least 8K
				int amountRequested = Math.max(stream.available(), 8192);
				// resize contents if needed
				if (contentsLength + amountRequested > contents.length) {
					System.arraycopy(contents,
							0,
							contents = new byte[contentsLength + amountRequested],
							0,
							contentsLength);
				}
				// read as many bytes as possible
				amountRead = stream.read(contents, contentsLength, amountRequested);
				if (amountRead > 0) {
					// remember length of contents
					contentsLength += amountRead;
				}
			} while (amountRead != -1);
			// resize contents if necessary
			if (contentsLength < contents.length) {
				System.arraycopy(contents, 0, contents = new byte[contentsLength], 0, contentsLength);
			}
		} else {
			contents = new byte[length];
			int len = 0;
			int readSize = 0;
			while ((readSize != -1) && (len != length)) {
				// See PR 1FMS89U
				// We record first the read size. In this case length is the actual
				// read size.
				len += readSize;
				readSize = stream.read(contents, len, length - len);
			}
		}
		return contents;
	}		
	
	/**
	 * Constructor
	 * @param name
	 */
	public LaunchConfigurationTests(String name) {
		super(name);
	}
	
	/**
	 * Returns a scratch project for launch configurations
	 *  
	 * @return
	 */
	protected IProject getProject() throws CoreException {
		return TestsPlugin.createProject("LaunchConfigurationTests"); //$NON-NLS-1$
	}
	
	/** 
	 * Creates and returns a new launch config the given name, local
	 * or shared, with 4 attributes:
	 *  - String1 = "String1"
	 *  - Int1 = 1
	 *  - Boolean1 = true
	 *  - Boolean2 = false
	 */
	protected ILaunchConfigurationWorkingCopy newConfiguration(IContainer container, String name) throws CoreException {
		 ILaunchConfigurationType type = getLaunchManager().getLaunchConfigurationType(ID_TEST_LAUNCH_TYPE);
		 assertTrue("Should support debug mode", type.supportsMode(ILaunchManager.DEBUG_MODE)); //$NON-NLS-1$
		 assertTrue("Should support run mode", type.supportsMode(ILaunchManager.RUN_MODE)); //$NON-NLS-1$
		 ILaunchConfigurationWorkingCopy wc = type.newInstance(container, name);
		 wc.setAttribute("String1", "String1"); //$NON-NLS-1$ //$NON-NLS-2$
		 wc.setAttribute("Int1", 1); //$NON-NLS-1$
		 wc.setAttribute("Boolean1", true); //$NON-NLS-1$
		 wc.setAttribute("Boolean2", false); //$NON-NLS-1$
		 assertTrue("Should need saving", wc.isDirty()); //$NON-NLS-1$
		 return wc;
	}
		
	/** 
	 * Creates and returns a new launch configuration with the given name, local
	 * or shared, with no attributes
	 */
	protected ILaunchConfigurationWorkingCopy newEmptyConfiguration(IContainer container, String name) throws CoreException {
		 ILaunchConfigurationType type = getLaunchManager().getLaunchConfigurationType(ID_TEST_LAUNCH_TYPE);
		 ILaunchConfigurationWorkingCopy wc = type.newInstance(container, name);
		assertEquals("Should have no attributes", 0, wc.getAttributes().size()); //$NON-NLS-1$
		 return wc;
	}	
		
	/**
	 * Returns whether the given handle is contained in the specified
	 * array of handles.
	 */
	protected boolean existsIn(ILaunchConfiguration[] configs, ILaunchConfiguration config) {
		for (int i = 0; i < configs.length; i++) {
			if (configs[i].equals(config)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Creates a local working copy configuration, sets some attributes,
	 * and saves the working copy, and retrieves the attributes.
	 * 
	 * @throws CoreException
	 */
	public void testCreateLocalConfiguration() throws CoreException {
		 ILaunchConfigurationWorkingCopy wc = newConfiguration(null, "config1"); //$NON-NLS-1$
		 IPath location = wc.getLocation();
		 ILaunchConfiguration handle = wc.doSave();
		 File file = location.toFile();
		 assertTrue("Configuration file should exist", file.exists()); //$NON-NLS-1$
		 
		 // retrieve attributes
		 assertEquals("String1 should be String1", handle.getAttribute("String1", "Missing"), "String1"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		 assertEquals("Int1 should be 1", handle.getAttribute("Int1", 0), 1); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean1 should be true", handle.getAttribute("Boolean1", false));  //$NON-NLS-1$//$NON-NLS-2$
		 assertTrue("Boolean2 should be false", !handle.getAttribute("Boolean2", true));  //$NON-NLS-1$//$NON-NLS-2$
		 
		 // ensure new handle is the index
		 ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations();
		 assertTrue("Configuration should exist in project index", existsIn(configs, handle)); //$NON-NLS-1$
		 
		 // cleanup
		 handle.delete();
		 assertTrue("Config should not exist after deletion", !handle.exists()); //$NON-NLS-1$
	}
	
	/**
	 * Creates a local working copy configuration and tests its name.
	 * 
	 * @throws CoreException
	 */
	public void testLocalName() throws CoreException {
		 ILaunchConfigurationWorkingCopy wc = newConfiguration(null, "localName"); //$NON-NLS-1$
		 ILaunchConfiguration handle = wc.doSave();
		 assertTrue("Configuration should exist", handle.exists()); //$NON-NLS-1$
		 
		 // retrieve attributes
		 assertEquals("Wrong name", handle.getName(), "localName"); //$NON-NLS-1$ //$NON-NLS-2$ 

		 // cleanup
		 handle.delete();
		 assertTrue("Config should not exist after deletion", !handle.exists()); //$NON-NLS-1$
	}	
	
	/**
	 * Creates a shared working copy configuration and tests is name.
	 */
	public void testSharedName() throws CoreException {
		 ILaunchConfigurationWorkingCopy wc = newConfiguration(getProject(), "sharedName"); //$NON-NLS-1$
		 ILaunchConfiguration handle = wc.doSave();
		 assertTrue("Configuration should exist", handle.exists()); //$NON-NLS-1$
		 
		 // retrieve attributes
		 assertEquals("Wrong name", handle.getName(), "sharedName"); //$NON-NLS-1$ //$NON-NLS-2$ 
		 
 		 // cleanup
		 handle.delete();
		 assertTrue("Config should not exist after deletion", !handle.exists()); //$NON-NLS-1$
	}	
	
	/**
	 * Ensures that a launch configuration returns a complete attribute map
	 * @throws CoreException
	 */
	public void testGetAttributes() throws CoreException {
		 ILaunchConfigurationWorkingCopy wc = newConfiguration(null, "config1"); //$NON-NLS-1$
		 IPath location = wc.getLocation();
		 ILaunchConfiguration handle = wc.doSave();
		 File file = location.toFile();
		 assertTrue("Configuration file should exist", file.exists()); //$NON-NLS-1$

		 Map attributes = handle.getAttributes();
		 // retrieve attributes
		 assertEquals("String1 should be String1", "String1", attributes.get("String1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		 assertEquals("Int1 should be 1", new Integer(1), attributes.get("Int1")); //$NON-NLS-1$ //$NON-NLS-2$
		 assertEquals("Boolean1 should be true", Boolean.valueOf(true), attributes.get("Boolean1")); //$NON-NLS-1$ //$NON-NLS-2$
		 assertEquals("Boolean2 should be false", Boolean.valueOf(false), attributes.get("Boolean2")); //$NON-NLS-1$ //$NON-NLS-2$

		 // cleanup
		 handle.delete();
		 assertTrue("Config should not exist after deletion", !handle.exists()); //$NON-NLS-1$
	}	
	
	/**
	 * Ensures that set attributes works
	 * @throws CoreException
	 */
	public void testSetAttributes() throws CoreException {
		 ILaunchConfigurationWorkingCopy wc = newConfiguration(null, "config1"); //$NON-NLS-1$
		 Map map = new HashMap();
		 map.put("ATTR1", "ONE"); //$NON-NLS-1$ //$NON-NLS-2$
		 map.put("ATTR2", "TWO"); //$NON-NLS-1$ //$NON-NLS-2$
		 wc.setAttributes(map);
		 IPath location = wc.getLocation();
		 ILaunchConfiguration handle = wc.doSave();
		 File file = location.toFile();
		 assertTrue("Configuration file should exist", file.exists()); //$NON-NLS-1$

		 Map attributes = handle.getAttributes();
		 assertEquals("should have two attributes", 2, attributes.size()); //$NON-NLS-1$
		 // retrieve attributes
		 assertEquals("ATTR1 should be ONE", "ONE", attributes.get("ATTR1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		 assertEquals("ATTR2 should be TWO", "TWO", attributes.get("ATTR2")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		 // cleanup
		 handle.delete();
		 assertTrue("Config should not exist after deletion", !handle.exists()); //$NON-NLS-1$
	}	
	
	/**
	 * Ensures that set attributes to <code>null</code> works
	 * @throws CoreException
	 */
	public void testSetNullAttributes() throws CoreException {
		 ILaunchConfigurationWorkingCopy wc = newConfiguration(null, "config1"); //$NON-NLS-1$
		 wc.setAttributes(null);
		 IPath location = wc.getLocation();
		 ILaunchConfiguration handle = wc.doSave();
		 File file = location.toFile();
		 assertTrue("Configuration file should exist", file.exists()); //$NON-NLS-1$

		 Map attributes = handle.getAttributes();
		 assertEquals("should have no attributes", 0, attributes.size()); //$NON-NLS-1$
		 // cleanup
		 handle.delete();
		 assertTrue("Config should not exist after deletion", !handle.exists()); //$NON-NLS-1$
	}
		
	/**
	 * Creates a local working copy configuration, sets some attributes,
	 * and saves the working copy, and retrieves the attributes.
	 * Copy the configuration and ensure the original still exists.
	 * @throws CoreException
	 */
	public void testLocalCopy() throws CoreException {
		 ILaunchConfigurationWorkingCopy wc = newConfiguration(null, "configToCopy"); //$NON-NLS-1$
		 IPath location = wc.getLocation();
		 ILaunchConfiguration handle = wc.doSave();
		 File file = location.toFile();
		 assertTrue("Configuration file should exist", file.exists()); //$NON-NLS-1$
		 
		 // retrieve attributes
		 assertTrue("String1 should be String1", handle.getAttribute("String1", "Missing").equals("String1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		 assertTrue("Int1 should be 1", handle.getAttribute("Int1", 0) == 1); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean1 should be true", handle.getAttribute("Boolean1", false)); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean2 should be false", !handle.getAttribute("Boolean2", true)); //$NON-NLS-1$ //$NON-NLS-2$
		 
		 // ensure new handle is the index
		 ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations();
		 assertTrue("Configuration should exist in project index", existsIn(configs, handle)); //$NON-NLS-1$
		 
		 ILaunchConfigurationWorkingCopy softCopy = handle.copy("CopyOf" + handle.getName()); //$NON-NLS-1$
		 assertNull("Original in copy should be null", softCopy.getOriginal()); //$NON-NLS-1$
		 ILaunchConfiguration hardCopy = softCopy.doSave();

		 // retrieve attributes
		 assertTrue("String1 should be String1", hardCopy.getAttribute("String1", "Missing").equals("String1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		 assertTrue("Int1 should be 1", hardCopy.getAttribute("Int1", 0) == 1); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean1 should be true", hardCopy.getAttribute("Boolean1", false)); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean2 should be false", !hardCopy.getAttribute("Boolean2", true));		  //$NON-NLS-1$ //$NON-NLS-2$
		 
		 assertTrue("Original should still exist", handle.exists()); //$NON-NLS-1$
		 
		 // cleanup
		 handle.delete();
		 assertTrue("Config should not exist after deletion", !handle.exists()); //$NON-NLS-1$
		 hardCopy.delete();
		 assertTrue("Config should not exist after deletion", !hardCopy.exists());		 		  //$NON-NLS-1$
	}
		
	/**
	 * Create a config and save it twice, ensuring it only
	 * ends up in the index once.
	 * @throws CoreException
	 */
	public void testDoubleSave() throws CoreException {
		 ILaunchConfigurationWorkingCopy wc = newConfiguration(null, "configDoubleSave"); //$NON-NLS-1$
		 IPath location = wc.getLocation();
		 ILaunchConfiguration handle = wc.doSave();
		 File file = location.toFile();
		 assertTrue("Configuration file should exist", file.exists()); //$NON-NLS-1$
		 
		 // retrieve attributes
		 assertTrue("String1 should be String1", handle.getAttribute("String1", "Missing").equals("String1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		 assertTrue("Int1 should be 1", handle.getAttribute("Int1", 0) == 1); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean1 should be true", handle.getAttribute("Boolean1", false)); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean2 should be false", !handle.getAttribute("Boolean2", true)); //$NON-NLS-1$ //$NON-NLS-2$
		 
		 // ensure new handle is the index
		 ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations();
		 assertTrue("Configuration should exist in project index", existsIn(configs, handle)); //$NON-NLS-1$
		 
		String name = wc.getName();
		wc.rename("newName"); //$NON-NLS-1$
		wc.rename(name);
		assertTrue("Should be dirty", wc.isDirty()); //$NON-NLS-1$
		wc.doSave();
		
		ILaunchConfiguration[] newConfigs = getLaunchManager().getLaunchConfigurations();
		assertTrue("Should be the same number of configs", newConfigs.length == configs.length); //$NON-NLS-1$
		
		 // cleanup
		 handle.delete();
		 assertTrue("Config should not exist after deletion", !handle.exists()); //$NON-NLS-1$
		
	}
		
	/**
	 * Creates a local working copy configuration, sets some attributes,
	 * and saves the working copy, and retrieves the attributes. Deletes
	 * the configuration and ensures it no longer exists.
	 * @throws CoreException
	 */
	public void testDeleteLocalConfiguration() throws CoreException {
		 ILaunchConfigurationWorkingCopy wc = newConfiguration(null, "config2delete"); //$NON-NLS-1$
		 ILaunchConfiguration handle = wc.doSave();
		 File file = wc.getLocation().toFile();
		 assertTrue("Configuration file should exist", file.exists()); //$NON-NLS-1$
		 
		 // retrieve attributes
		 assertTrue("String1 should be String1", handle.getAttribute("String1", "Missing").equals("String1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		 assertTrue("Int1 should be 1", handle.getAttribute("Int1", 0) == 1); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean1 should be true", handle.getAttribute("Boolean1", false)); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean2 should be false", !handle.getAttribute("Boolean2", true)); //$NON-NLS-1$ //$NON-NLS-2$
		 
		 // delete 
		 handle.delete();		 
		 assertTrue("Config should no longer exist", !handle.exists()); //$NON-NLS-1$
		 
		 // ensure handle is not in the index
		 ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations();
		 assertTrue("Configuration should not exist in project index", !existsIn(configs, handle));		  //$NON-NLS-1$
	}	
	
	/**
	 * Creates a local working copy configuration, sets some attributes,
	 * and saves the working copy, and retrieves the attributes. Renames
	 * the configuration and ensures it's old config no longer exists,
	 * and that attributes are retrievable from the new (renamed) config.
	 * @throws CoreException
	 */
	public void testRenameLocalConfiguration() throws CoreException {
		 ILaunchConfigurationWorkingCopy wc = newConfiguration(null, "config2rename"); //$NON-NLS-1$
		 IPath location = wc.getLocation();
		 ILaunchConfiguration handle = wc.doSave();
		 File file = location.toFile();
		 assertTrue("Configuration file should exist", file.exists()); //$NON-NLS-1$
		 
		 // retrieve attributes
		 assertTrue("String1 should be String1", handle.getAttribute("String1", "Missing").equals("String1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		 assertTrue("Int1 should be 1", handle.getAttribute("Int1", 0) == 1); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean1 should be true", handle.getAttribute("Boolean1", false)); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean2 should be false", !handle.getAttribute("Boolean2", true)); //$NON-NLS-1$ //$NON-NLS-2$
		 
		 // rename
		 wc = handle.getWorkingCopy();
		 wc.rename("config-2-rename"); //$NON-NLS-1$
		 addConfigListener();
		 ILaunchConfiguration newHandle = wc.doSave();
		 removeConfigListener();
		 assertTrue("Config should no longer exist", !handle.exists()); //$NON-NLS-1$
		 assertEquals("From should be original", handle, fFrom); //$NON-NLS-1$
		 assertEquals("To should be new handle", newHandle, fTo); //$NON-NLS-1$
		 
		 // retrieve new attributes
		 assertTrue("String1 should be String1", newHandle.getAttribute("String1", "Missing").equals("String1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		 assertTrue("Int1 should be 1", newHandle.getAttribute("Int1", 0) == 1); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean1 should be true", newHandle.getAttribute("Boolean1", false));  //$NON-NLS-1$//$NON-NLS-2$
		 assertTrue("Boolean2 should be false", !newHandle.getAttribute("Boolean2", true));		  //$NON-NLS-1$ //$NON-NLS-2$

		 // ensure new handle is in the index
		 ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations();
		 assertTrue("Renamed configuration should exist in project index", existsIn(configs, newHandle));		  //$NON-NLS-1$
		 assertTrue("Original configuration should NOT exist in project index", !existsIn(configs, handle));	 //$NON-NLS-1$
		 
		 // cleanup
		 newHandle.delete();
		 assertTrue("Config should not exist after deletion", !newHandle.exists());		 	  //$NON-NLS-1$
	}	
	
	/**
	 * Moves a local configuration to a shared location
	 * @throws CoreException
	 */
	public void testMoveLocalToSharedConfiguration() throws CoreException {
		 ILaunchConfigurationWorkingCopy wc = newConfiguration(null, "config2share"); //$NON-NLS-1$
		 IPath location = wc.getLocation();
		 ILaunchConfiguration handle = wc.doSave();
		 File file = location.toFile();
		 assertTrue("Configuration file should exist", file.exists()); //$NON-NLS-1$

		 // retrieve attributes
		 assertTrue("String1 should be String1", handle.getAttribute("String1", "Missing").equals("String1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		 assertTrue("Int1 should be 1", handle.getAttribute("Int1", 0) == 1); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean1 should be true", handle.getAttribute("Boolean1", false)); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean2 should be false", !handle.getAttribute("Boolean2", true)); //$NON-NLS-1$ //$NON-NLS-2$

		 // move
		 wc = handle.getWorkingCopy();
		 wc.setContainer(getProject());
		 addConfigListener();
		 ILaunchConfiguration newHandle = wc.doSave();
		 removeConfigListener();
		 assertTrue("Config should no longer exist", !handle.exists()); //$NON-NLS-1$
		 assertEquals("From should be original", handle, fFrom); //$NON-NLS-1$
		 assertEquals("To should be new handle", newHandle, fTo); //$NON-NLS-1$

		 // retrieve new attributes
		 assertTrue("String1 should be String1", newHandle.getAttribute("String1", "Missing").equals("String1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		 assertTrue("Int1 should be 1", newHandle.getAttribute("Int1", 0) == 1); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean1 should be true", newHandle.getAttribute("Boolean1", false)); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean2 should be false", !newHandle.getAttribute("Boolean2", true)); //$NON-NLS-1$ //$NON-NLS-2$

		 // ensure new handle is in the index
		 ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations();
		 assertTrue("Renamed configuration should exist in project index", existsIn(configs, newHandle)); //$NON-NLS-1$
		 assertTrue("Original configuration should NOT exist in project index", !existsIn(configs, handle)); //$NON-NLS-1$

		 // cleanup
		 newHandle.delete();
		 assertTrue("Config should not exist after deletion", !newHandle.exists()); //$NON-NLS-1$
	}	
	
	/**
	 * Moves a local configuration to a shared location
	 * @throws CoreException
	 */
	public void testMoveSharedToLocalConfiguration() throws CoreException {
		 ILaunchConfigurationWorkingCopy wc = newConfiguration(getProject(), "config2local"); //$NON-NLS-1$
		 IPath location = wc.getLocation();
		 ILaunchConfiguration handle = wc.doSave();
		 File file = location.toFile();
		 assertTrue("Configuration file should exist", file.exists()); //$NON-NLS-1$

		 // retrieve attributes
		 assertTrue("String1 should be String1", handle.getAttribute("String1", "Missing").equals("String1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		 assertTrue("Int1 should be 1", handle.getAttribute("Int1", 0) == 1); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean1 should be true", handle.getAttribute("Boolean1", false)); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean2 should be false", !handle.getAttribute("Boolean2", true)); //$NON-NLS-1$ //$NON-NLS-2$

		 // move
		 wc = handle.getWorkingCopy();
		 wc.setContainer(null);
		 addConfigListener();
		 ILaunchConfiguration newHandle = wc.doSave();
		 removeConfigListener();
		 assertTrue("Config should no longer exist", !handle.exists()); //$NON-NLS-1$
		 assertEquals("From should be original", handle, fFrom); //$NON-NLS-1$
		 assertEquals("To should be new handle", newHandle, fTo); //$NON-NLS-1$

		 // retrieve new attributes
		 assertTrue("String1 should be String1", newHandle.getAttribute("String1", "Missing").equals("String1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		 assertTrue("Int1 should be 1", newHandle.getAttribute("Int1", 0) == 1); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean1 should be true", newHandle.getAttribute("Boolean1", false)); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean2 should be false", !newHandle.getAttribute("Boolean2", true)); //$NON-NLS-1$ //$NON-NLS-2$

		 // ensure new handle is in the index
		 ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations();
		 assertTrue("Renamed configuration should exist in project index", existsIn(configs, newHandle)); //$NON-NLS-1$
		 assertTrue("Original configuration should NOT exist in project index", !existsIn(configs, handle)); //$NON-NLS-1$

		 // cleanup
		 newHandle.delete();
		 assertTrue("Config should not exist after deletion", !newHandle.exists()); //$NON-NLS-1$
	}		
	
	/**
	 * Creates a shared working copy configuration, sets some attributes,
	 * and saves the working copy, and retrieves the attributes.
	 * @throws CoreException
	 */
	public void testCreateSharedConfiguration() throws CoreException {
		 ILaunchConfigurationWorkingCopy wc = newConfiguration(getProject(), "config2"); //$NON-NLS-1$
		 ILaunchConfiguration handle = wc.doSave();
		 assertTrue("Configuration should exist", handle.exists()); //$NON-NLS-1$
		 
		 // retrieve attributes
		 assertTrue("String1 should be String1", handle.getAttribute("String1", "Missing").equals("String1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		 assertTrue("Int1 should be 1", handle.getAttribute("Int1", 0) == 1); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean1 should be true", handle.getAttribute("Boolean1", false)); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean2 should be false", !handle.getAttribute("Boolean2", true)); //$NON-NLS-1$ //$NON-NLS-2$
		 
 		 // ensure new handle is in the index
		 ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations();
		 assertTrue("Configuration should exist in project index", existsIn(configs, handle));  //$NON-NLS-1$
		 
 		 // cleanup
		 handle.delete();
		 assertTrue("Config should not exist after deletion", !handle.exists()); //$NON-NLS-1$
	}	
	
	/**
	 * Creates a shared working copy configuration, sets some attributes,
	 * and saves the working copy, and retrieves the attributes.
	 * Copies the configuration and ensures the original still exists.
	 * @throws CoreException
	 */
	public void testSharedCopy() throws CoreException {
		 ILaunchConfigurationWorkingCopy wc = newConfiguration(getProject(), "config2Copy"); //$NON-NLS-1$
		 ILaunchConfiguration handle = wc.doSave();
		 assertTrue("Configuration should exist", handle.exists()); //$NON-NLS-1$
		 
		 // retrieve attributes
		 assertTrue("String1 should be String1", handle.getAttribute("String1", "Missing").equals("String1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		 assertTrue("Int1 should be 1", handle.getAttribute("Int1", 0) == 1); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean1 should be true", handle.getAttribute("Boolean1", false)); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean2 should be false", !handle.getAttribute("Boolean2", true)); //$NON-NLS-1$ //$NON-NLS-2$
		 
 		 // ensure new handle is in the index
		 ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations();
		 assertTrue("Configuration should exist in project index", existsIn(configs, handle));  //$NON-NLS-1$
		 
		 // copy 
		 ILaunchConfigurationWorkingCopy softCopy = handle.copy("CopyOf" + handle.getName()); //$NON-NLS-1$
		 ILaunchConfiguration hardCopy = softCopy.doSave();
		 
		 // retrieve attributes
		 assertTrue("String1 should be String1", hardCopy.getAttribute("String1", "Missing").equals("String1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		 assertTrue("Int1 should be 1", hardCopy.getAttribute("Int1", 0) == 1); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean1 should be true", hardCopy.getAttribute("Boolean1", false)); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean2 should be false", !hardCopy.getAttribute("Boolean2", true));		  //$NON-NLS-1$ //$NON-NLS-2$
		 
		 assertTrue("Original should still exist", handle.exists()); //$NON-NLS-1$
		 
		 // cleanup
		 handle.delete();
		 assertTrue("Config should not exist after deletion", !handle.exists()); //$NON-NLS-1$
		 hardCopy.delete();
		 assertTrue("Config should not exist after deletion", !hardCopy.exists());		 		 		  //$NON-NLS-1$
	}		
	

	/**
	 * Creates a shared working copy configuration, sets some attributes,
	 * and saves the working copy, and retrieves the attributes. Deletes
	 * the configuration and ensures it no longer exists.
	 * @throws CoreException
	 */
	public void testDeleteSharedConfiguration() throws CoreException {
 		 ILaunchConfigurationWorkingCopy wc = newConfiguration(getProject(), "shared2delete"); //$NON-NLS-1$
		 ILaunchConfiguration handle = wc.doSave();
		 assertTrue("Configuration should exist", handle.exists()); //$NON-NLS-1$
		 
		 // retrieve attributes
		 assertTrue("String1 should be String1", handle.getAttribute("String1", "Missing").equals("String1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		 assertTrue("Int1 should be 1", handle.getAttribute("Int1", 0) == 1); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean1 should be true", handle.getAttribute("Boolean1", false)); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean2 should be false", !handle.getAttribute("Boolean2", true)); //$NON-NLS-1$ //$NON-NLS-2$
		 
		 // delete 
		 handle.delete();		 
		 assertTrue("Config should no longer exist", !handle.exists()); //$NON-NLS-1$
		 
		 // ensure handle is not in the index
		 ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations();
		 assertTrue("Configuration should not exist in project index", !existsIn(configs, handle));		  //$NON-NLS-1$
	}	
	
	/**
	 * Creates a shared working copy configuration, sets some attributes,
	 * and saves the working copy, and retrieves the attributes. Renames
	 * the configuration and ensures it's old config no longer exists,
	 * and that attributes are retrievable from the new (renamed) config.
	 * @throws CoreException
	 */
	public void testRenameSharedConfiguration() throws CoreException {
		 ILaunchConfigurationWorkingCopy wc = newConfiguration(getProject(), "shared2rename"); //$NON-NLS-1$
		 ILaunchConfiguration handle = wc.doSave();
		 assertTrue("Configuration should exist", handle.exists()); //$NON-NLS-1$
		 
		 // retrieve attributes
		 assertTrue("String1 should be String1", handle.getAttribute("String1", "Missing").equals("String1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		 assertTrue("Int1 should be 1", handle.getAttribute("Int1", 0) == 1); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean1 should be true", handle.getAttribute("Boolean1", false)); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean2 should be false", !handle.getAttribute("Boolean2", true)); //$NON-NLS-1$ //$NON-NLS-2$
		 
		 // rename
		 wc = handle.getWorkingCopy();
		 wc.rename("shared-2-rename"); //$NON-NLS-1$
		 addConfigListener();
		 ILaunchConfiguration newHandle = wc.doSave();
		 removeConfigListener();
		 assertTrue("Config should no longer exist", !handle.exists()); //$NON-NLS-1$
		 assertEquals("From should be original", handle, fFrom); //$NON-NLS-1$
		 assertEquals("To should be new handle", newHandle, fTo);		  //$NON-NLS-1$
		 
		 // retrieve new attributes
		 assertTrue("String1 should be String1", newHandle.getAttribute("String1", "Missing").equals("String1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		 assertTrue("Int1 should be 1", newHandle.getAttribute("Int1", 0) == 1); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean1 should be true", newHandle.getAttribute("Boolean1", false)); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean2 should be false", !newHandle.getAttribute("Boolean2", true));		  //$NON-NLS-1$ //$NON-NLS-2$

		 // ensure new handle is in the index
		 ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations();
		 assertTrue("Renamed configuration should exist in project index", existsIn(configs, newHandle));		  //$NON-NLS-1$
		 assertTrue("Original configuration should NOT exist in project index", !existsIn(configs, handle));		  //$NON-NLS-1$
		 
		 // cleanup
		 newHandle.delete();
		 assertTrue("Config should not exist after deletion", !newHandle.exists());		  //$NON-NLS-1$
	}
	
	/**
	 * Closes all editors in the active workbench page.
	 */
	protected void closeAllEditors() {
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		activeWorkbenchWindow.getActivePage().closeAllEditors(false);
	}
	
	/** 
	 * Creates a few configs, closes the project and re-opens the
	 * project to ensure the config index is persisted properly
	 * @throws CoreException
	 */
	public void testPersistIndex() throws CoreException {
		// close all editors before closing project: @see bug 204023
		closeAllEditors();
		
		ILaunchConfigurationWorkingCopy wc1 = newConfiguration(null, "persist1local"); //$NON-NLS-1$
		ILaunchConfigurationWorkingCopy wc2 = newConfiguration(getProject(), "persist2shared"); //$NON-NLS-1$
		ILaunchConfiguration lc1 = wc1.doSave();
		ILaunchConfiguration lc2 = wc2.doSave();
		
		IProject project = getProject();
		ILaunchConfiguration[] before = getLaunchManager().getLaunchConfigurations();
		assertTrue("config should be in index", existsIn(before, lc1)); //$NON-NLS-1$
		assertTrue("config should be in index", existsIn(before, lc2)); //$NON-NLS-1$
		
		project.close(null);
		ILaunchConfiguration[] during = getLaunchManager().getLaunchConfigurations();
		boolean local = true;
		for (int i = 0; i < during.length; i++) {
			// must be local, or not from the closed project
			local = local && (during[i].isLocal() || !during[i].getFile().getProject().equals(project));
		}		
		project.open(null);
		assertTrue("Should only be local configs when closed", local); //$NON-NLS-1$
		ILaunchConfiguration[] after = getLaunchManager().getLaunchConfigurations();
		assertTrue("Should be same number of configs after openning", after.length == before.length); //$NON-NLS-1$
		for (int i = 0; i < before.length; i++) {
			assertTrue("Config should exist after openning", existsIn(after, before[i])); //$NON-NLS-1$
		}

		 // cleanup
		 lc1.delete();
		 assertTrue("Config should not exist after deletion", !lc1.exists()); //$NON-NLS-1$
		 lc2.delete();
		 assertTrue("Config should not exist after deletion", !lc2.exists());		  //$NON-NLS-1$
		 
		
	}	
		
		
	/**
	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationAdded(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void launchConfigurationAdded(ILaunchConfiguration configuration) {
		fFrom = getLaunchManager().getMovedFrom(configuration);
		synchronized (fLock) {
		    fAdded = configuration;
		    fLock.notifyAll();
        }
	}

	/**
	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationChanged(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void launchConfigurationChanged(ILaunchConfiguration configuration) {
	}

	/**
	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationRemoved(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
		fTo = getLaunchManager().getMovedTo(configuration);
		synchronized (fLock) {
		    fRemoved = configuration;
		    fLock.notifyAll();
        }
	}

	protected void addConfigListener() {
		getLaunchManager().addLaunchConfigurationListener(this);
	}
	
	protected void removeConfigListener() {
		getLaunchManager().removeLaunchConfigurationListener(this);
	}
	
	/**
	 * Ensures that a removal notification is sent for a shared config in a project
	 * that is deleted.
	 *  
	 * @throws Exception
	 */
	public void testDeleteProjectWithSharedConfig() throws Exception {
	   IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("DeleteSharedConfig"); //$NON-NLS-1$
	   try {
		   assertFalse("project should not exist yet", project.exists()); //$NON-NLS-1$
		   project.create(null);
		   assertTrue("project should now exist", project.exists()); //$NON-NLS-1$
		   project.open(null);
		   assertTrue("project should be open", project.isOpen()); //$NON-NLS-1$
		   ILaunchConfigurationWorkingCopy wc = newConfiguration(project, "ToBeDeleted"); //$NON-NLS-1$
		   
		   addConfigListener();
		   ILaunchConfiguration configuration = wc.doSave();
		   assertEquals(configuration, fAdded);
		   
		   synchronized (fLock) {
		       fRemoved = null;
		       project.delete(true, false, null);
		       if (fRemoved == null) {
		           fLock.wait(10000);
		       }
		   }
		   assertEquals(configuration, fRemoved);
	   } finally {
	       if (project.exists()) {
	           project.delete(true, false, null);
	       }
	       removeConfigListener();
	   }
	}
	
	/**
	 * Tests a nested working copy.
	 * 
	 * @throws CoreException
	 */
	public void testNestedWorkingCopyLocalConfiguration() throws CoreException {
		 ILaunchConfigurationWorkingCopy wc = newConfiguration(null, "config123"); //$NON-NLS-1$
		 IPath location = wc.getLocation();
		 ILaunchConfiguration handle = wc.doSave();
		 File file = location.toFile();
		 assertTrue("Configuration file should exist", file.exists()); //$NON-NLS-1$
		 
		 // retrieve attributes
		 assertEquals("String1 should be String1", handle.getAttribute("String1", "Missing"), "String1"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		 assertEquals("Int1 should be 1", handle.getAttribute("Int1", 0), 1); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean1 should be true", handle.getAttribute("Boolean1", false)); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean2 should be false", !handle.getAttribute("Boolean2", true)); //$NON-NLS-1$ //$NON-NLS-2$
		 
		 // ensure new handle is the index
		 ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations();
		 assertTrue("Configuration should exist in project index", existsIn(configs, handle)); //$NON-NLS-1$
		 
		 // get a working copy
		 wc = handle.getWorkingCopy();
		 ILaunchConfigurationWorkingCopy nested = wc.getWorkingCopy();
		 
		 // verify nested is same as original
		 assertEquals("String1 should be String1", nested.getAttribute("String1", "Missing"), "String1"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		 assertEquals("Int1 should be 1", nested.getAttribute("Int1", 0), 1); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean1 should be true", nested.getAttribute("Boolean1", false)); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean2 should be false", !nested.getAttribute("Boolean2", true)); //$NON-NLS-1$ //$NON-NLS-2$
		 
		 // change an attribute in the nested working copy
		 nested.setAttribute("String1", "StringOne"); //$NON-NLS-1$ //$NON-NLS-2$
		 assertEquals("Wrong attribute value", nested.getAttribute("String1", "Missing"), "StringOne"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		 assertEquals("Wrong attribute value", wc.getAttribute("String1", "Missing"), "String1"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		 assertEquals("Wrong attribute value", handle.getAttribute("String1", "Missing"), "String1"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		 
		 // save back to parent
		 ILaunchConfigurationWorkingCopy parent = nested.getParent();
		 assertEquals("Wrong parent", wc, parent); //$NON-NLS-1$
		 assertNull("Should have no parent", wc.getParent()); //$NON-NLS-1$
		 nested.doSave();
		 assertEquals("Wrong attribute value", wc.getAttribute("String1", "Missing"), "StringOne");  //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$
		 assertEquals("Wrong attribute value", handle.getAttribute("String1", "Missing"), "String1"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		 
		 // check originals
		 assertEquals("Wrong original config" , handle, wc.getOriginal()); //$NON-NLS-1$
		 assertEquals("Wrong original config" , handle, nested.getOriginal()); //$NON-NLS-1$
		 
		 // cleanup
		 handle.delete();
		 assertTrue("Config should not exist after deletion", !handle.exists()); //$NON-NLS-1$
	}	
	
	/**
	 * Creates a configuration in an EFS linked folder. Deletes configuration directly.
	 * 
	 * @throws CoreException
	 * @throws URISyntaxException
	 */	
	public void testCreateDeleteEFS() throws CoreException, URISyntaxException {
		IFileSystem fileSystem = EFS.getFileSystem("debug"); //$NON-NLS-1$
		assertNotNull("Missing debug EFS", fileSystem); //$NON-NLS-1$
		
		// create folder in EFS
		IFolder folder = getProject().getFolder("efs"); //$NON-NLS-1$
		folder.createLink(new URI("debug", Path.ROOT.toString(), null), 0, null); //$NON-NLS-1$
		
		// create configuration
		ILaunchConfigurationWorkingCopy wc = newConfiguration(folder, "efsConfig"); //$NON-NLS-1$
		ILaunchConfiguration handle = wc.doSave();
		assertTrue("Configuration should exist", handle.exists()); //$NON-NLS-1$
		
		 // retrieve attributes
		 assertTrue("String1 should be String1", handle.getAttribute("String1", "Missing").equals("String1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		 assertTrue("Int1 should be 1", handle.getAttribute("Int1", 0) == 1); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean1 should be true", handle.getAttribute("Boolean1", false)); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean2 should be false", !handle.getAttribute("Boolean2", true)); //$NON-NLS-1$ //$NON-NLS-2$		
		
		// delete configuration
		handle.delete();
		assertTrue("Configuration should not exist", !handle.exists()); //$NON-NLS-1$
		
		// cleanup
		folder.delete(IResource.NONE, null);
	}
	
	/**
	 * Creates a configuration in an EFS linked folder. Deletes the folder to ensure the
	 * configuration is also deleted.
	 * 
	 * @throws CoreException
	 * @throws URISyntaxException
	 */
	public void testCreateDeleteEFSLink() throws CoreException, URISyntaxException {
		IFileSystem fileSystem = EFS.getFileSystem("debug"); //$NON-NLS-1$
		assertNotNull("Missing debug EFS", fileSystem); //$NON-NLS-1$
		
		// create folder in EFS
		IFolder folder = getProject().getFolder("efs2"); //$NON-NLS-1$
		folder.createLink(new URI("debug", Path.ROOT.toString(), null), 0, null); //$NON-NLS-1$
		
		// create configuration
		ILaunchConfigurationWorkingCopy wc = newConfiguration(folder, "efsConfig"); //$NON-NLS-1$
		ILaunchConfiguration handle = wc.doSave();
		assertTrue("Configuration should exist", handle.exists()); //$NON-NLS-1$
		
		 // retrieve attributes
		 assertTrue("String1 should be String1", handle.getAttribute("String1", "Missing").equals("String1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		 assertTrue("Int1 should be 1", handle.getAttribute("Int1", 0) == 1); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean1 should be true", handle.getAttribute("Boolean1", false)); //$NON-NLS-1$ //$NON-NLS-2$
		 assertTrue("Boolean2 should be false", !handle.getAttribute("Boolean2", true)); //$NON-NLS-1$ //$NON-NLS-2$		
				
		// cleanup
		folder.delete(IResource.NONE, null);
		assertTrue("Configuration should not exist", !handle.exists()); //$NON-NLS-1$
	}	
	
	/**
	 * Test that renaming a project with a linked EFS folder containing a shared
	 * launch configuration is properly updated.
	 * 
	 * @throws Exception
	 */
	public void testEFSProjectRename() throws Exception {
        // create test project
		IProject pro = ResourcesPlugin.getWorkspace().getRoot().getProject("RenameEFS"); //$NON-NLS-1$
        if (pro.exists()) {
            pro.delete(true, true, null);
        }
        // create project
		IProject project = TestsPlugin.createProject("RenameEFS"); //$NON-NLS-1$
        
		IFileSystem fileSystem = EFS.getFileSystem("debug"); //$NON-NLS-1$
		assertNotNull("Missing debug EFS", fileSystem); //$NON-NLS-1$
		
		// create folder in EFS
		IFolder folder = project.getFolder("efs2"); //$NON-NLS-1$
		folder.createLink(new URI("debug", Path.ROOT.toString(), null), 0, null); //$NON-NLS-1$
		
		// create configuration
		ILaunchConfigurationWorkingCopy wc = newConfiguration(folder, "efsConfig"); //$NON-NLS-1$
		ILaunchConfiguration handle = wc.doSave();
		assertTrue("Configuration should exist", handle.exists()); //$NON-NLS-1$
		
		// retrieve attributes
		assertTrue("String1 should be String1", handle.getAttribute("String1", "Missing").equals("String1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		assertTrue("Int1 should be 1", handle.getAttribute("Int1", 0) == 1); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue("Boolean1 should be true", handle.getAttribute("Boolean1", false)); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue("Boolean2 should be false", !handle.getAttribute("Boolean2", true)); //$NON-NLS-1$ //$NON-NLS-2$		
		
		// rename project
		IProjectDescription description = project.getDescription();
		description.setName("SFEemaneR"); // reverse name //$NON-NLS-1$
		project.move(description, IResource.SHALLOW, null);
		
		// original configuration should no longer exist - handle out of date
		assertTrue("Configuration should not exist", !handle.exists()); //$NON-NLS-1$
		
		// get the new handle
		project = ResourcesPlugin.getWorkspace().getRoot().getProject("SFEemaneR"); //$NON-NLS-1$
		assertTrue("Project should exist", project.exists()); //$NON-NLS-1$
		IFile file = project.getFile(new Path("efs2/efsConfig.launch")); //$NON-NLS-1$
		assertTrue("launch config file should exist", file.exists()); //$NON-NLS-1$
		handle = getLaunchManager().getLaunchConfiguration(file);
		assertTrue("launch config should exist", handle.exists()); //$NON-NLS-1$
		
		// retrieve attributes
		assertTrue("String1 should be String1", handle.getAttribute("String1", "Missing").equals("String1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		assertTrue("Int1 should be 1", handle.getAttribute("Int1", 0) == 1); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue("Boolean1 should be true", handle.getAttribute("Boolean1", false)); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue("Boolean2 should be false", !handle.getAttribute("Boolean2", true)); //$NON-NLS-1$ //$NON-NLS-2$		
		
		// validate shared location
		assertEquals("Shared location should be updated", file, handle.getFile()); //$NON-NLS-1$
		
		// cleanup
		project.delete(IResource.NONE, null);
		assertTrue("Configuration should not exist", !handle.exists()); //$NON-NLS-1$
        
	}
	
	/**
	 * Tests launch configuration import.
	 * 
	 * @throws Exception
	 */
	public void testImport() throws Exception {
		// create a shared configuration "Import4" in the workspace to be overwritten on import
		 ILaunchConfigurationWorkingCopy wc = newConfiguration(getProject(), "Import4"); //$NON-NLS-1$
		 ILaunchConfiguration handle = wc.doSave();
		 assertTrue("Configuration should exist", handle.exists()); //$NON-NLS-1$
		 
		File dir = TestsPlugin.getDefault().getFileInPlugin(new Path("test-import")); //$NON-NLS-1$
		assertTrue("Import directory does not exist", dir.exists()); //$NON-NLS-1$
		 LaunchManager manager = (LaunchManager) getLaunchManager();

		 Listener listener = new Listener();
		 try {
			 getLaunchManager().addLaunchConfigurationListener(listener);
			 // import
			 manager.importConfigurations(dir.listFiles(
				new FileFilter() {
					public boolean accept(File file) {
						return file.isFile() &&
							file.getName().endsWith(
								ILaunchConfiguration.LAUNCH_CONFIGURATION_FILE_EXTENSION);
					}
				}),
				null);
		 
			 // should be one removed
			 List removed = listener.getRemoved();
			assertEquals("Should be one removed config", 1, removed.size()); //$NON-NLS-1$
			assertTrue("Import4 should be removed", removed.contains(handle)); //$NON-NLS-1$
			 
			 // should be 5 added
			 List added = listener.getAdded();
			assertEquals("Should be 5 added configs", 5, added.size()); //$NON-NLS-1$
			 Set names = new HashSet();
			 Iterator iterator = added.iterator();
			 while (iterator.hasNext()) {
				ILaunchConfiguration lc = (ILaunchConfiguration) iterator.next();
				names.add(lc.getName());
			}
			assertTrue("Missing Name", names.contains("Import1")); //$NON-NLS-1$ //$NON-NLS-2$
			assertTrue("Missing Name", names.contains("Import2")); //$NON-NLS-1$ //$NON-NLS-2$
			assertTrue("Missing Name", names.contains("Import3")); //$NON-NLS-1$ //$NON-NLS-2$
			assertTrue("Missing Name", names.contains("Import4")); //$NON-NLS-1$ //$NON-NLS-2$
			assertTrue("Missing Name", names.contains("Import5")); //$NON-NLS-1$ //$NON-NLS-2$
			
			// should be one changed
			List changed = listener.getChanged();
			assertEquals("Should be 1 changed config", 1, changed.size()); //$NON-NLS-1$
			assertEquals("Wrong changed config", "Import4", ((ILaunchConfiguration) changed.get(0)).getName()); //$NON-NLS-1$ //$NON-NLS-2$
		 } finally {
			 manager.removeLaunchConfigurationListener(listener);
		 }
		 
	}
	
	/**
	 * Tests the location of a local working copy.
	 * 
	 * @throws CoreException
	 */
	public void testWorkingCopyGetLocation() throws CoreException {
		ILaunchConfigurationWorkingCopy workingCopy = newConfiguration(null, "test-get-location"); //$NON-NLS-1$
		IPath location = workingCopy.getLocation();
		assertEquals("Wrong path for local working copy", LaunchManager.LOCAL_LAUNCH_CONFIGURATION_CONTAINER_PATH.append("test-get-location.launch"), location); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * Tests that the framework adds time stamps to launch objects.
	 */
	public void testLaunchTimeStamp() throws CoreException {
		ILaunchConfigurationWorkingCopy workingCopy = newConfiguration(null, "test-time-stamp"); //$NON-NLS-1$
		ILaunch launch = workingCopy.launch(ILaunchManager.DEBUG_MODE, null);
		try {
			String stamp = launch.getAttribute(DebugPlugin.ATTR_LAUNCH_TIMESTAMP);
			assertNotNull("missing time stamp", stamp); //$NON-NLS-1$
			Long.parseLong(stamp); // should be a long - will throw NumberFormatException if not
		} finally {
			if (launch != null) {
				getLaunchManager().removeLaunch(launch);
			}
		}
	}
	
	/**
	 * Tests that attributes in a nested map are persisted in alphabetical order.
	 *   
	 * @throws CoreException
	 */
	public void testMapAttributePersistence() throws CoreException, IOException {
		ILaunchConfigurationWorkingCopy c1 = newEmptyConfiguration(getProject(), "testMapAttributes1"); //$NON-NLS-1$
		HashMap map = new HashMap();
		map.put("Z", "z-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("Y", "y-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("X", "x-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("W", "w-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("V", "v-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("U", "u-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("T", "t-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("S", "s-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("R", "r-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("Q", "q-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("P", "p-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("O", "o-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("N", "n-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("M", "m-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("L", "l-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("K", "k-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("J", "j-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("I", "i-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("H", "h-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("G", "g-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("F", "f-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("E", "e-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("D", "d-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("C", "c-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("B", "b-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("A", "a-value"); //$NON-NLS-1$ //$NON-NLS-2$
		c1.setAttribute("Map-Attribute", map); //$NON-NLS-1$
		c1.doSave();
		
		ILaunchConfigurationWorkingCopy c2 = newEmptyConfiguration(getProject(), "testMapAttributes2"); //$NON-NLS-1$
		map = new HashMap();
		map.put("A", "a-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("Z", "z-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("B", "b-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("Y", "y-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("C", "c-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("X", "x-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("D", "d-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("W", "w-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("E", "e-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("V", "v-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("F", "f-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("U", "u-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("G", "g-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("T", "t-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("H", "h-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("S", "s-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("I", "i-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("R", "r-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("J", "j-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("Q", "q-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("K", "k-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("P", "p-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("L", "l-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("M", "m-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("O", "o-value"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("N", "n-value"); //$NON-NLS-1$ //$NON-NLS-2$
		c2.setAttribute("Map-Attribute", map); //$NON-NLS-1$
		c2.doSave();
		
		// file contents should be the same
		char[] chars1 = getInputStreamAsCharArray(c1.getFile().getContents());
		char[] chars2 = getInputStreamAsCharArray(c2.getFile().getContents());
		assertEquals("Should be the same characters", chars1.length, chars2.length); //$NON-NLS-1$
		for (int i = 0; i < chars2.length; i++) {
			assertEquals("Should be the same character", chars1[i], chars2[i]); //$NON-NLS-1$
		}
		
	}

	/**
	 * Tests that attributes in a nested set are persisted in alphabetical order.
	 *   
	 * @throws CoreException
	 */
	public void testSetAttributePersistence() throws CoreException, IOException {
		ILaunchConfigurationWorkingCopy c1 = newEmptyConfiguration(getProject(), "testSetAttributes1"); //$NON-NLS-1$
		Set set = new HashSet();
		set.add("z-value"); //$NON-NLS-1$
		set.add("y-value"); //$NON-NLS-1$
		set.add("x-value"); //$NON-NLS-1$
		set.add("w-value"); //$NON-NLS-1$
		set.add("v-value"); //$NON-NLS-1$
		set.add("u-value"); //$NON-NLS-1$
		set.add("t-value"); //$NON-NLS-1$
		set.add("s-value"); //$NON-NLS-1$
		set.add("r-value"); //$NON-NLS-1$
		set.add("q-value"); //$NON-NLS-1$
		set.add("p-value"); //$NON-NLS-1$
		set.add("o-value"); //$NON-NLS-1$
		set.add("n-value"); //$NON-NLS-1$
		set.add("m-value"); //$NON-NLS-1$
		set.add("l-value"); //$NON-NLS-1$
		set.add("k-value"); //$NON-NLS-1$
		set.add("j-value"); //$NON-NLS-1$
		set.add("i-value"); //$NON-NLS-1$
		set.add("h-value"); //$NON-NLS-1$
		set.add("g-value"); //$NON-NLS-1$
		set.add("f-value"); //$NON-NLS-1$
		set.add("e-value"); //$NON-NLS-1$
		set.add("d-value"); //$NON-NLS-1$
		set.add("c-value"); //$NON-NLS-1$
		set.add("b-value"); //$NON-NLS-1$
		set.add("a-value"); //$NON-NLS-1$
		c1.setAttribute("Set-Attribute", set); //$NON-NLS-1$
		c1.doSave();
		
		ILaunchConfigurationWorkingCopy c2 = newEmptyConfiguration(getProject(), "testSetAttributes2"); //$NON-NLS-1$
		set = new HashSet();
		set.add("a-value"); //$NON-NLS-1$
		set.add("z-value"); //$NON-NLS-1$
		set.add("b-value"); //$NON-NLS-1$
		set.add("y-value"); //$NON-NLS-1$
		set.add("c-value"); //$NON-NLS-1$
		set.add("x-value"); //$NON-NLS-1$
		set.add("d-value"); //$NON-NLS-1$
		set.add("w-value"); //$NON-NLS-1$
		set.add("e-value"); //$NON-NLS-1$
		set.add("v-value"); //$NON-NLS-1$
		set.add("f-value"); //$NON-NLS-1$
		set.add("u-value"); //$NON-NLS-1$
		set.add("g-value"); //$NON-NLS-1$
		set.add("t-value"); //$NON-NLS-1$
		set.add("h-value"); //$NON-NLS-1$
		set.add("s-value"); //$NON-NLS-1$
		set.add("i-value"); //$NON-NLS-1$
		set.add("r-value"); //$NON-NLS-1$
		set.add("j-value"); //$NON-NLS-1$
		set.add("q-value"); //$NON-NLS-1$
		set.add("k-value"); //$NON-NLS-1$
		set.add("p-value"); //$NON-NLS-1$
		set.add("l-value"); //$NON-NLS-1$
		set.add("m-value"); //$NON-NLS-1$
		set.add("o-value"); //$NON-NLS-1$
		set.add("n-value"); //$NON-NLS-1$
		c2.setAttribute("Set-Attribute", set); //$NON-NLS-1$
		c2.doSave();
		
		// file contents should be the same
		char[] chars1 = getInputStreamAsCharArray(c1.getFile().getContents());
		char[] chars2 = getInputStreamAsCharArray(c2.getFile().getContents());
		assertEquals("Should be the same characters", chars1.length, chars2.length); //$NON-NLS-1$
		for (int i = 0; i < chars2.length; i++) {
			assertEquals("Should be the same character", chars1[i], chars2[i]); //$NON-NLS-1$
		}
		
	}
	
	/**
	 * Ensures that client does not attempt to nest configurations in a sub directory when
	 * using local metadata location. See bug 275741.
	 * 
	 * @throws CoreException
	 */
	public void testIllegalFileSepCharName() {
		try {
			newConfiguration(null, new Path("some").append("nested").append("config").toOSString()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} catch (CoreException e) {
			// i.e. expected code path
			return;
		}
		assertTrue("Should be an illegal argument - cannot nest local configurations", false); //$NON-NLS-1$
	}
	
	/**
	 * Ensures that client can nest configurations in a sub directory when
	 * using a workspace location. See bug 275741. For behavior compatibility
	 * a client should be able to use a slash in the configuration name.
	 * 
	 * @throws CoreException
	 */
	public void testLegalFileSepCharName() {
		try {
			newConfiguration(getProject(), new Path("some").append("nested").append("config").toOSString()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} catch (CoreException e) {
			assertTrue("Should *not* be an illegal argument - can nest shared cofigurations", false); //$NON-NLS-1$
		}
	}	
	
	/**
	 * Test that an illegal name with '<' causes an exception
	 * 
	 * @throws CoreException
	 */
	public void testIllegalCharName() {
		try {
			newConfiguration(getProject(), "<config>"); //$NON-NLS-1$
		} catch (CoreException e) {
			// expected code path
			return;
		}
		assertTrue("Should be an illegal argument - illegal character used in name", false); //$NON-NLS-1$
	}		
	
	/**
	 * Test that moving and renaming a shared configuration at the same time works.
	 * 
	 * @throws CoreException
	 */
	public void testRenameAndMoveShared() throws CoreException {
		IProject project = getProject();
		IFolder f1 = project.getFolder("f1"); //$NON-NLS-1$
		IFolder f2 = project.getFolder("f2"); //$NON-NLS-1$
		f1.create(false, true, null);
		f2.create(false, true, null);
		ILaunchConfigurationWorkingCopy wc = newConfiguration(f1, "start-here"); //$NON-NLS-1$
		ILaunchConfiguration orig = wc.doSave();
		wc = orig.getWorkingCopy();
		
		wc.setContainer(f2);
		wc.rename("end-here"); //$NON-NLS-1$
		ILaunchConfiguration next = wc.doSave();
		
		assertFalse("Original should not exist", orig.exists()); //$NON-NLS-1$
		assertTrue("Renamed and moved config should exist", next.exists()); //$NON-NLS-1$
		
	}
	
	/**
	 * Test support for a URL in the 'icon' part of the launchConfigurationTypeImages extension point
	 * 
	 * Bug 381175 - [patch] launchConfigurationTypeImage to support platform: style icons
	 * @throws Exception
	 */
	public void testGetTypeImageFromURI() throws Exception {
		ImageDescriptor descriptor = DebugUITools.getImageDescriptor("org.eclipse.debug.tests.launch.type1"); //$NON-NLS-1$
		assertNotNull("The image descriptior type.image.1 must exist", descriptor); //$NON-NLS-1$
		assertNotSame("The image descriptor is not type.image.1", ImageDescriptor.getMissingImageDescriptor(), descriptor); //$NON-NLS-1$
	}
	
	/**
	 * Test support for a declared launch configuration type image
	 * @throws Exception
	 */
	public void testGetTyeImage() throws Exception {
		ImageDescriptor descriptor = DebugUITools.getImageDescriptor("org.eclipse.debug.tests.launch.type"); //$NON-NLS-1$
		assertNotNull("The image descriptior type.image.2 must exist", descriptor); //$NON-NLS-1$
		assertNotSame("The image descriptor is not type.image.2", ImageDescriptor.getMissingImageDescriptor(), descriptor); //$NON-NLS-1$
	}
	
	/**
	 * Tests that we can get a project handle from a project name
	 * 
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=395441
	 * @throws Exception
	 * @since 3.9.0
	 */
	public void testGetProjectMappedResource1() throws Exception {
		ILaunchConfiguration lc = newConfiguration(null, "test.project.resource.mapping"); //$NON-NLS-1$
		try {
			ILaunchConfigurationWorkingCopy wc = lc.getWorkingCopy();
			assertNotNull("Should have a working copy of the testig launch configuration", wc); //$NON-NLS-1$
			setResourceMappings(wc, new ResourceItem[] { new ResourceItem("test.project", new Integer(IResource.PROJECT)) }); //$NON-NLS-1$
			IResource[] res = wc.getMappedResources();
			assertNotNull("There should be mapped resources", res); //$NON-NLS-1$
			assertTrue("There should be one project", res.length == 1); //$NON-NLS-1$
		}
		finally {
			lc.delete();
		}
	}
	
	/**
	 * Tests that we cannot get a project handle from a bogus project name
	 * 
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=395441
	 * @throws Exception
	 * @since 3.9.0
	 */
	public void testGetProjectMappedResource2() throws Exception {
		ILaunchConfiguration lc = newConfiguration(null, "test.project.resource.mapping"); //$NON-NLS-1$
		try {
			ILaunchConfigurationWorkingCopy wc = lc.getWorkingCopy();
			assertNotNull("Should have a working copy of the testig launch configuration", wc); //$NON-NLS-1$
			setResourceMappings(wc, new ResourceItem[] { new ResourceItem("test/project", new Integer(IResource.PROJECT)) }); //$NON-NLS-1$
			IResource[] res = wc.getMappedResources();
			assertNull("There should be no mapped resources", res); //$NON-NLS-1$
		}
		finally {
			lc.delete();
		}
	}
	
	/**
	 * Tests that we cannot get a project handle from a bogus project name
	 * 
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=395441
	 * @throws Exception
	 * @since 3.9.0
	 */
	public void testGetProjectMappedResource3() throws Exception {
		ILaunchConfiguration lc = newConfiguration(null, "test.project.resource.mapping"); //$NON-NLS-1$
		try {
			ILaunchConfigurationWorkingCopy wc = lc.getWorkingCopy();
			assertNotNull("Should have a working copy of the testig launch configuration", wc); //$NON-NLS-1$
			setResourceMappings(wc, new ResourceItem[] { new ResourceItem("test\\project", new Integer(IResource.PROJECT)) }); //$NON-NLS-1$
			IResource[] res = wc.getMappedResources();
			if(Platform.OS_WIN32.equals(Platform.getOS())) {
				assertNull("There should be no mapped resources", res); //$NON-NLS-1$
			}
			else {
				assertNotNull("There should be mapped resources", res); //$NON-NLS-1$
			}
		}
		finally {
			lc.delete();
		}
	}

	/**
	 * Tests that we can get a project handle from an absolute project name
	 * 
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=395441
	 * @throws Exception
	 * @since 3.9.0
	 */
	public void testGetProjectMappedResource4() throws Exception {
		ILaunchConfiguration lc = newConfiguration(null, "test.project.resource.mapping"); //$NON-NLS-1$
		try {
			ILaunchConfigurationWorkingCopy wc = lc.getWorkingCopy();
			assertNotNull("Should have a working copy of the testig launch configuration", wc); //$NON-NLS-1$
			setResourceMappings(wc, new ResourceItem[] { new ResourceItem("/project", new Integer(IResource.PROJECT)) }); //$NON-NLS-1$
			IResource[] res = wc.getMappedResources();
			assertNotNull("There should be mapped resources", res); //$NON-NLS-1$
		}
		finally {
			lc.delete();
		}
	}
	
	/**
	 * Proxy to set resource paths, allowing invalid resource paths to be set
	 * @param resources
	 * @since 3.9.0
	 */
	protected void setResourceMappings(ILaunchConfigurationWorkingCopy config, ResourceItem[] resources) {
		List/*<String>*/ paths = null;
		List/*<String>*/ types = null;
		int size = resources.length;
		if(resources != null && size > 0) {
			paths = new ArrayList(size);
			types = new ArrayList(size);
			for(int i = 0; i < size; i++) {
				paths.add(resources[i].path);
				types.add(resources[i].type.toString());
			}
		}
		config.setAttribute(LaunchConfiguration.ATTR_MAPPED_RESOURCE_PATHS, paths);
		config.setAttribute(LaunchConfiguration.ATTR_MAPPED_RESOURCE_TYPES, types);
	}
}
