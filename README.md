xar for Java
============

A Java library for reading and writing eXtensible ARchiver files by [sprylab technologies GmbH][1].

Work-in-progress note
--------

This is the public preview version. It works for us, but it still has some rough
corners and thus lacks consistent code style, sufficient unit tests and complete documentation.
Especially the writing support is still experimental.

Use with care! We work on a fully polished version 1.0.0 and will release it, when it's done.

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

Download [the latest JAR][2] or grab via Maven:
```xml
<dependency>
  <groupId>com.sprylab.xar</groupId>
  <artifactId>xar</artifactId>
  <version>0.9.4</version>
</dependency>
```
or Gradle:
```groovy
compile 'com.sprylab.xar:xar:0.9.4'
```

There is also a CLI version, which mimics the behavior of the original C executable.
Download [the latest standalone-JAR][3] (all dependencies included) for direct use at the command line.

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
 [2]: http://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=com.sprylab.xar&a=xar&v=LATEST
 [3]: http://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=com.sprylab.xar&a=xar-cli&c=standalone&v=LATEST
