package org.eclipse.ui.wizards.datatransfer;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.IWorkbench;

/**
 * FOR USE BY TESTS ONLY!
 * <p>
 * Stub class that provides access to classes visible to the package
 * <code>org.eclipse.ui.wizards.datatransfer</code>.  For the purpose
 * of testing.
 * </p>
 * @private
 */
public class DataTransferTestStub {
	//Prevent instantiation
	private DataTransferTestStub(){}
	
	/**
	 * Gives access to an instance of WizardFileSystemResourceExportPage1.
	 * @return IWizardPage an instance of WizardFileSystemResourceExportPage1
	 */
	public static IWizardPage newFileSystemResourceExportPage1(IStructuredSelection selection) {
		return new WizardFileSystemResourceExportPage1(selection);
	}
	/**
	 * Gives access to an instance of WizardFileSystemResourceImportPage1.
	 * @return IWizardPage an instance of WizardFileSystemResourceImportPage1
	 */
	public static IWizardPage newFileSystemResourceImportPage1(IWorkbench workbench, IStructuredSelection selection) {
		return new WizardFileSystemResourceImportPage1(workbench, selection);
	}
	/**
	 * Gives access to an instance of WizardZipFileResourceExportPage1.
	 * @return IWizardPage an instance of WizardZipFileResourceExportPage1
	 */
	public static IWizardPage newZipFileResourceExportPage1(IStructuredSelection selection) {
		return new WizardZipFileResourceExportPage1(selection);
	}
	/**
	 * Gives access to an instance of WizardZipFileResourceImportPage1.
	 * @return IWizardPage an instance of WizardZipFileResourceImportPage1
	 */
	public static IWizardPage newZipFileResourceImportPage1(IStructuredSelection selection) {
		return new WizardZipFileResourceExportPage1(selection);
	}
}

