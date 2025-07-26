# Example for providing an icon pack to Eclipse IDE / RCP

This example demonstrate how to create an icon pack for the Eclipse IDE or a custom RCP application.

An icon pack on a technical level is a simple bundle that can be installed like any other piece of software and
ships one or more alternative representations of an existing icon resource to one or more bundles already installed
in the system. This allows an icon pack to customize the look and feel of Eclipse RCP Applications even further
beyond what is already possible with css.

For a bundle to be recognized by the system one needs the following things demonstrated in this example:

1. The bundle should list the following header in its manifest to make sure the equinox transform hook is made available when installing it:
`Require-Capability: osgi.extender;filter:="(osgi.extender=equinox.transforms.hook)"`
2. It needs to maintain transformer rules file, in our example this is done by the file `transform.txt` but any 
name / extension is valid here, the format of the file is described below. 
3. It must list the location in the manifest with the `Equinox-Transformer: /transform.txt` bundle header to be recognized by the framework
4. Make sure that the icons and the transform rules files are included in the `bin.include` in the `build.properties`!

## writing transformer rules file

a transformer rules file is a simple text file that (similar to rewrite rules in a webserver) tells the framework what resources in a bundle should be replaced
with another.

The general format is `<bundle pattern>,<resource pattern>,<transformer instructions>,<transformer>`, where the last part can be omitted here as it defaults
to `replace` what is the one we need for icon packs.

### replace one icon by another

Now assume we want to replace the save icon because a floppy-disk is really nothing people today know and might wonder what this strange quadratic thing should actually
mean to them and today a downwards arrow is more common (even though for demonstration purpose our icon looks quite ugly so it is easily spotted as being replaced).

1. We first need to know the bundle that provides this what in this case is the `org.eclipse.ui` so our pattern for matching this bundle would be `org\.eclipse\.ui`, 
what would only match this single bundle. If an icon has to be replaced in multiple bundles we can either use a less narrow pattern or use two different rules.
2. Then one needs to find the icon to replace in this case it is `icons/full/etool16/save_edit.png`, as before we could use an exact match or we can use a more
generic form to possibly match multiple path. In the example we use `icons/.*/save_edit.png`, please note that you even can replace a gif file by a png or whatever
fileformat fits your needs. The code will still see the old name so this might still be used with care if the caller maybe make decisions based on the extension!
3. Of course we need our replacement resource that will be used and we put it in as the last instruction as we are using the default replace transformer that just keeps
the provided resource as a replacement for the original.

The full line then looks like this:

```
org\.eclipse\.ui,icons/.*/save_edit.png,/myicons/saveme.png
```

### replace multiple icons

A transformer rules file can contain as many lines as you want and you can replace also icons from as many bundles as you like in the same icon pack.

## Running the example

If you want to run the example, make sure that you add the following to your run configuration (or extend an existing configuration):

```
-Dosgi.framework.extensions=org.eclipse.equinox.transforms.hook
```

this enables the equinox transforms hook extension also make sure that at least version `1.4.200` is used as it includes the required enhancements
used in this example.

Also make sure the example is included in the launch e.g. add it to an existing feature of your product / launch configuration.

If everything worked well, you should see your new save icon in the toolbar like this:

![example showing replaced icon](replaced_icon.png)

## Future enhancements

This example is currently only a very bare example, everyone is encouraged to help enhancing it and sharing ideas how this can be made more comfortable,
also there is currently no tooling around this so any help is appreciated.

Here is an (incomplete) list of things that might be useful in future versions:

1. supporting capture groups in a transform instruction to match a large amount of icon with one line and also support `@2x` variants more easily (currently it requires a duplicate line)
2. having a way to exactly match a bundle without the need to escape special chars like `.` and supporting simple ant style glob pattern
3. some tooling that allows to select a bundle and generate a list of icon replacements automatically
4. support systemproperty or variable placeholders to support for example a theming system
5. ...