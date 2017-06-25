Change Log
==========

Version 0.9.5
-------------

* fix resource flushing and cleanup when adding files to xar archives

Version 0.9.4
-------------

* fix resource leak (#5)
* replace all calls to `System.out` with `slf4j-simple` in `xar-cli`
* added TavisCI configuration

Version 0.9.3
-------------

* massively increased performance for extracting files - at least for a lot of small files in an archive
* fix XarEntry.OnEntryExtractedListener not being called
* removed dependency to commons-lang3

Version 0.9.2
-------------

 * use Okio instead of commons-codec and commons-io (#4)
 * added unit tests for creating xar archives
 * fixed bugs in creating xar archives (improves compatibility with original xar tool written in C)
 * massively cleaned up and refactored code base
 * create code coverage report when packaging
 * set Java compiler source and target to 1.7

Version 0.9.1
-------------

 * convert to multi-module project
   * `xar`: the Java library
   * `xar-cli`: the Java command-line interface
 * updated .gitignore to ignore all IntelliJ-based IDEs
 * updated to newest dependencies
 * release on Maven central (#2)

```xml
<dependency>
  <groupId>com.sprylab.xar</groupId>
  <artifactId>xar</artifactId>
  <version>0.9.1</version>
</dependency>
```

Version 0.9.0
-------------

 * initial public release on github
