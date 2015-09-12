# purgeannotationrefs [![Build Status](https://buildhive.cloudbees.com/job/pfichtner/job/purgeannotationrefs/badge/icon)](https://buildhive.cloudbees.com/job/pfichtner/job/purgeannotationrefs/)

Remove references to annotations out of the java bytecode/classfiles (remove the @Anno tag from annotated elements). 
Now you can use annotations to check constellations in bytecode after compilation but purge the used annotations before releasing the jars.

There are three modules: 
- The library doing the bytecode transformation (could be included in Java projects)
- An Ant Task to include the bytecode transformation in your Ant build
- A Maven Module to include the bytecode transformation in your Maven build

## License

Copyright 2008-2015 Peter Fichtner - Released under the [ GNU Lesser General Public License ](http://www.gnu.de/documents/lgpl-3.0.de.html)
