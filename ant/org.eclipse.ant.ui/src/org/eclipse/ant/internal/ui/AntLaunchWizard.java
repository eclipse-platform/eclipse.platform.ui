package org.eclipse.ant.internal.ui;/* * (c) Copyright IBM Corp. 2000, 2001. * All Rights Reserved. */import java.lang.reflect.InvocationTargetException;import java.util.*;import org.apache.tools.ant.Target;import org.eclipse.ant.core.*;import org.eclipse.core.resources.IFile;import org.eclipse.core.runtime.*;import org.eclipse.jface.operation.IRunnableWithProgress;import org.eclipse.jface.wizard.Wizard;
/** * The wizard used to run an Ant script file written in xml. * <p> * Note: Currently there is only one page in this wizard. */
public class AntLaunchWizard extends Wizard {
	/**	 * The project described in the xml file.	 */	
	private EclipseProject project = null;		/**	 * The first page of the wizard.	 */
	private AntLaunchWizardPage page1 = null;		/**	 * The file that contains the Ant script.	 */
	private IFile antFile = null;
		/**	 * The string used to separate the previously selected target names in the persistent properties of the file.	 */
	private final static String SEPARATOR_TARGETS = "\"";		/**	 * The identifier of the property.	 */
	private final static String PROPERTY_SELECTEDTARGETS = "selectedTargets";
			/**	 * Creates a new wizard, given the project described in the file and the file itself.	 * 	 * @param project	 * @param antFile	 */
	public AntLaunchWizard(EclipseProject project,IFile antFile) {
		super();
		this.project = project;
		this.antFile = antFile;
	}
	/**	 * Adds pages to the wizard and initialize them.	 * 	 */	
	public void addPages() {
		page1 = new AntLaunchWizardPage(project);
		addPage(page1);
		page1.setInitialTargetSelections(getTargetNamesToPreselect());
	}
	/**	 * Retrieves (from the persistent properties of the file) the targets selected	 * during the last build of the file.	 * 	 * @return String[] the name of the targets	 */	
	public String[] getTargetNamesToPreselect() {
		String propertyString = null;
		try {
			propertyString = antFile.getPersistentProperty(
				new QualifiedName(AntUIPlugin.PI_ANTUI,PROPERTY_SELECTEDTARGETS));
		} catch (CoreException e) {
			new Status(
				IStatus.WARNING,
				AntUIPlugin.PI_ANTUI,
				IStatus.WARNING,
				Policy.bind("status.targetNotRead", antFile.getFullPath().toString()),
				e);
		}
				if (propertyString == null)			return new String[0];
		StringTokenizer tokenizer = new StringTokenizer(propertyString,SEPARATOR_TARGETS);
		String result[] = new String[tokenizer.countTokens()];
		int index = 0;
		while (tokenizer.hasMoreTokens())
			result[index++] = tokenizer.nextToken();

		return result;
	}
			
	/**	 * Builds the Ant file according to the selected targets and the arguments given in the command line.	 *	 * @return boolean	 */			
	public boolean performFinish() {				Vector targetVect = page1.getSelectedTargets();
		
		// DON'T NEED IT ANYMORE
		// project.executeTargets(targetVect);				String[] args = createArgumentsArray(targetVect);		
//		this.getContainer().run(true,true,new IRunnableWithProgress() {
//			public void run(IProgressMonitor monitor) {//				
//				monitor.beginTask("Running Ant", IProgressMonitor.UNKNOWN);

				try {
					//TBD: should remove the build listener somehow
					new AntRunner().run(args/*, new UIBuildListener(monitor, antFile)*/);
				} 
				catch (BuildCanceledException e) {
					// build was canceled don't propagate exception
					return false;
				}
				catch (Exception e) {
					//throw new InvocationTargetException(e);					// should do something here					return false;
				}
//				finally {
//					monitor.done();
//				}
//			};
//		});


		// asuming that all went well...
		storeTargetsOnFile(targetVect);		
		return true;
	}		/**	 * Creates an array that contains all the arguments needed to run AntRunner: 	 * 	- the name of the file to build	 *  - the arguments such as "-verbose", ...	 *  - target names	 * 	 * @param targets the vector that contains the targets built during the parsing	 * @return String[] the tokenized arguments	 */	private String[] createArgumentsArray(Vector targets) {			Vector argsVector = new Vector();		Vector targetVect = targets;		String argString = page1.getArgumentsFromField().trim();				// if there are arguments, then we have to tokenize them		if (argString.length() != 0) {			int indexOfToken;						// Checks if the string starts with a bracket or not so that we know where the composed arguments start			if (argString.charAt(0)=='"') 				indexOfToken = 1;			else				indexOfToken=0;					// First tokenize the command line with the separator bracket			StringTokenizer tokenizer1 = new StringTokenizer(argString,"\"");					while (tokenizer1.hasMoreTokens()) {				if (indexOfToken%2 == 0) {					// this is a string that needs to be tokenized with the separator space					StringTokenizer tokenizer2 = new StringTokenizer(tokenizer1.nextToken()," ");					while (tokenizer2.hasMoreTokens())						argsVector.add(tokenizer2.nextToken());				} else {					argsVector.add(tokenizer1.nextToken());				}				indexOfToken++;			}				}						// Finally create the array of String for AntRunner		String args[] = new String[argsVector.size()+targetVect.size()+2];		int index = 0;		args[index++] = "-buildfile";		args[index++] = antFile.getLocation().toOSString();				Iterator argsIterator = argsVector.iterator();		while (argsIterator.hasNext())			args[index++] = (String) argsIterator.next();		argsIterator = targetVect.iterator();		while (argsIterator.hasNext())			args[index++] = ((Target) argsIterator.next()).getName();					/*//TEST			int i = args.length;		for (int y=0; y<i; y++)			System.out.println("- "+args[y]);*/						return args;			}
	/**	 * Stores the name of the selected targets in the persistent properties of the file,	 * so that next time the user wants to build this file, those targets are pre-selected.	 * 	 * @param targets the vector that contains the targets built during the parsing	 */
	protected void storeTargetsOnFile(Vector targets) {
		StringBuffer targetString = new StringBuffer();
		Iterator targetsIt = targets.iterator();
		
		while (targetsIt.hasNext()) {
			targetString.append(((Target)targetsIt.next()).getName());
			targetString.append(SEPARATOR_TARGETS);
		}
		
		try {
			antFile.setPersistentProperty(
				new QualifiedName(AntUIPlugin.PI_ANTUI,PROPERTY_SELECTEDTARGETS),
				targetString.toString());
		} catch (CoreException e) {
			AntUIPlugin.getPlugin().getLog().log(
				new Status(
					IStatus.WARNING,
					AntUIPlugin.PI_ANTUI,
					IStatus.WARNING,
					Policy.bind("status.targetNotWritten", antFile.getFullPath().toString()),
					e));
		}
	}
}
