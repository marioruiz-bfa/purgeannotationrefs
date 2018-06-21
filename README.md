# purgeannotationrefs [![pipeline status](https://gitlab.com/pfichtner/purgeannotationrefs/badges/master/pipeline.svg)](https://gitlab.com/pfichtner/purgeannotationrefs/commits/master)

Remove references to annotations out of the java bytecode/classfiles (remove the @Anno tag from annotated elements). 
Now you can use annotations to check constellations in bytecode after compilation but purge the used annotations before releasing the jars.

There are three modules: 
- The library doing the bytecode transformation (could be included in Java projects)
- An Ant Task to include the bytecode transformation in your Ant build
- A Maven Module to include the bytecode transformation in your Maven build

## License

Copyright 2008-2015 Peter Fichtner - Released under the [ GNU Lesser General Public License ](https://www.gnu.org/licenses/lgpl-3.0.html)
