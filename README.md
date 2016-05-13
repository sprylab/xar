xar for Java
============

A Java library for reading and writing eXtensible ARchiver files by [sprylab technologies GmbH][1].

Work-in-progress note
--------

This is the public preview version 0.9.0. It works for us, but it still has some rough
corners and thus lacks consistent code style, sufficient unit tests and complete documentation.
Especially the writing support is still experimental.

Use with care! We work on a fully polished version and will release it, when it's done.

Thanks for your patience!

Usage
=====

Open a .xar file:

```
XarFile xar = new XarFile(new File("my-file.xar"));
```

Extract a .xar file:

```
xar.extractAll(new File("extract-directory"));
```

Get a list of all entries:

```
List<XarEntry> entries = xar.getEntries();
```

Get a specific entry:

```
XarEntry entry = xar.getEntry("directory/file.txt");
```

Stream, extract or get bytes of a entry:

```
InputStream inputStream = entry.getInputStream();
entry.extract(new File("extract-directory"));
byte[] bytes = entry.getBytes();
```

Create a .xar file from directory:

```
XarPacker xarPacker = new XarPacker(new File("my-file.xar"));
xarPacker.addDirectory(new File("directory"), false, null));
xarPacker.write();
```

Download
--------

Version 1.0.0 will be distributed over Maven Central. At the moment, you have to clone and
build manually from the source (`mvn clean package -Pstandalone` for now).

License
=======

    Copyright 2013-2016 sprylab technologies GmbH

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

Related projects
================

Original C source: http://code.google.com/p/xar/

Fork/clone by @mackyle: https://github.com/mackyle/xar

JavaScript port by @finnp: https://github.com/finnp/xar

 [1]: https://sprylab.com/
