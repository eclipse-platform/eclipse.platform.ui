Important Note:

The templates directory, and specifically, templates/model/Class.javajet are overrides of the EMF code generator.

templates/model/Class.javajet works around to EMF bugs 400729 and 400847
It is extracted without modification from EMF commit:
https://git.eclipse.org/c/emf/org.eclipse.emf.git/commit/plugins/org.eclipse.emf.codegen.ecore/templates/model/Class.javajet?id=9d4d3510c27d12ee13dc82d1939a9afcd137b09d

The code generation parameters in /org.eclipse.e4.ui.model.workbench/model/UIElements.genmodel have been modified
to reference the templates directory.