# Eclipse Platform UI Project

Thanks for your interest in this project.


## Project Description

Platform UI provides the basic building blocks for user interfaces built with Eclipse.

Some of these form the Eclipse Rich Client Platform (RCP) and can be used for arbitrary rich client applications, while others are specific to the [Eclipse IDE](https://www.eclipse.org/eclipseide/). The Platform UI codebase is built on top of the Eclipse Standard Widget Toolkit ([SWT](https://www.eclipse.org/swt/)), which is developed as an independent project.

For more information, refer to the [Eclipse Platform project page](https://projects.eclipse.org/projects/eclipse.platform) and the [Platform UI wiki page](https://wiki.eclipse.org/Platform_UI).


## How to Contribute

Contributions are most welcome. There are many ways to contribute, from entering high quality bug reports, to contributing code or documentation changes.

For a complete guide, see the https://github.com/eclipse-platform/.github/blob/main/CONTRIBUTING.md.


## Test Dependencies

Several test plug-ins have a dependency to the Mockito and Hamcrest libraries.
Please install them by installing "Eclipse Test Framework" from the [current release stream p2 repo](https://download.eclipse.org/eclipse/updates/I-builds/).


## How to Build on the Command Line

You need Maven 3.8.x installed. After this you can run the build via the following command:

```
mvn clean verify -Pbuild-individual-bundles
```


## Issue Tracking

This project uses Github to track ongoing development and issues. In case you have an issue, please read the information about Eclipse being a [community project](https://github.com/eclipse-platform#community) and bear in mind that this project is almost entirely developed by volunteers. So the contributors may not be able to look into every reported issue. You will also find the information about [how to find and report issues](https://github.com/eclipse-platform#reporting-issues) in repositories of the `eclipse-platform` organization there. Be sure to search for existing issues before you create another one.

In case you want to report an issue that is specific to this `eclipse.platform.ui` repository, you can [find existing issues](https://github.com/eclipse-platform/eclipse.platform.ui/issues) or [create new issues](https://github.com/eclipse-platform/eclipse.platform.ui/issues/new) within this repository.


## Contact

Contact the project developers via the project's "dev" list.

- <https://accounts.eclipse.org/mailing-list/platform-dev>


## License

[Eclipse Public License (EPL) 2.0](https://www.eclipse.org/legal/epl-2.0/)
