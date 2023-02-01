Contributing to Eclipse Platform UI project
===========================================

Thanks for your interest in this project.

Project description:
--------------------

Platform UI provides the basic building blocks for user interfaces built with Eclipse. 

Some of these form the Eclipse Rich Client Platform (RCP) and can be used for arbitrary rich client applications, while others are specific to the Eclipse IDE. The Platform UI codebase is built on top of the Eclipse Standard Widget Toolkit ([SWT](https://www.eclipse.org/swt/)), which is developed as an independent project.

Website: <https://www.eclipse.org/eclipse/>

- <https://projects.eclipse.org/projects/eclipse.platform>

For more information, refer to the [Platform UI wiki page](https://wiki.eclipse.org/Platform_UI).

How to contribute:
--------------------
Contributions to Platform UI are most welcome. There are many ways to contribute,
from entering high quality bug reports, to contributing code or documentation changes.
For a complete guide, see the [Platform UI - How to contribute wiki page](https://wiki.eclipse.org/Platform_UI/How_to_Contribute) page on the wiki.

Test dependencies
-----------------

Several test plug-ins have a dependency to the Mockito and Hamcrest libraries.
Please install them by installing "Eclipse Test Framework" from the [current release stream p2 repo](https://download.eclipse.org/eclipse/updates/I-builds/).

How to build on the command line
--------------------------------

You need Maven 3.8.x installed. After this you can run the build via the following command:

```
mvn clean verify -Pbuild-individual-bundles
```


Developer resources:
--------------------

Information regarding source code management, builds, coding standards, and more.

- <https://projects.eclipse.org/projects/eclipse.platform/developer>

Contributor License Agreement:
------------------------------

Before your contribution can be accepted by the project, you need to create and electronically sign the Eclipse Foundation Contributor License Agreement (CLA).

- <https://www.eclipse.org/legal/CLA.php>


Issue Tracking:
----------------

This project uses Github to track ongoing development and issues.

- https://github.com/issues?user=eclipse-platform

Be sure to search for existing bugs before you create another one. Remember that contributions are always welcome!

Contact:
--------

Contact the project developers via the project's "dev" list.

- <https://accounts.eclipse.org/mailing-list/platform-dev>


License
-------

[Eclipse Public License (EPL) 2.0](https://www.eclipse.org/legal/epl-2.0/)
