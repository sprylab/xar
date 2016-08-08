package com.sprylab.xar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcabi.manifests.Manifests;
import com.sprylab.xar.toc.ToCFactory;
import com.sprylab.xar.toc.model.ChecksumAlgorithm;
import com.sprylab.xar.writer.XarPacker;

/**
 * @author rzimmer
 */
public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    /**
     * Verbose option.
     */
    private static final String V_OPTION = "v";

    /**
     * Create option.
     */
    private static final String C_OPTION = "c";

    /**
     * Extract option.
     */
    private static final String X_OPTION = "x";

    /**
     * List option.
     */
    private static final String T_OPTION = "t";

    /**
     * File option.
     */
    private static final String F_OPTION = "f";

    private static final String FILENAME_OPTION = "filename";

    private static final String DUMP_TOC_OPTION = "dump-toc";

    private static final String DUMP_HEADER_OPTION = "dump-header";

    private static final String VERSION_OPTION = "version";

    private static final String USAGE = "java -jar xar -[tx][v] -f <archive> ...";

    private static final String VERSION_MANIFEST_KEY = "Version";

    public static void main(final String[] args) {
        final Options options = createCommandLineOptions();
        final CommandLineParser parser = new DefaultParser();
        try {
            final CommandLine line = parser.parse(options, args);

            if (line.hasOption(F_OPTION)) {
                final File archiveFile = new File(line.getOptionValue(F_OPTION));
                final List<String> argList = line.getArgList();

                if (line.hasOption(C_OPTION)) {
                    final XarPacker packer = new XarPacker(archiveFile);
                    for (final String additionalArgument : argList) {
                        final File fileToAdd = new File(additionalArgument);
                        if (fileToAdd.isDirectory()) {
                            try {
                                packer.addDirectory(fileToAdd, false, null);
                            } catch (final Exception e) {
                                LOG.error("Cannot add {} as it's not a directory.", fileToAdd.getAbsolutePath(), e);
                            }
                        }
                    }
                    packer.write();
                } else {
                    final XarFile xarFile = new XarFile(archiveFile);

                    if (line.hasOption(X_OPTION)) {

                        final File destination;
                        if (argList == null || argList.isEmpty()) {
                            destination = new File(getWorkingDir());
                        } else {
                            destination = new File(argList.get(0));
                        }
                        XarFile.DEBUG = line.hasOption(V_OPTION);
                        extractFiles(xarFile, destination);
                    } else if (line.hasOption(T_OPTION)) {
                        listEntries(xarFile);
                    } else if (line.hasOption(DUMP_HEADER_OPTION)) {
                        dumpHeader(xarFile);
                    } else if (line.hasOption(DUMP_TOC_OPTION)) {
                        final File tocFile = new File(line.getOptionValue(DUMP_TOC_OPTION));
                        dumpToC(xarFile, tocFile);
                    } else {
                        printHelp(options);
                    }
                }
            } else if (line.hasOption(VERSION_OPTION)) {
                printVersion();
            } else {
                printHelp(options);
            }
        } catch (final ParseException e) {
            printHelp(options);
            LOG.error("Parsing commandline failed.", e);
        } catch (final IOException e) {
            LOG.error("Opening xar file failed.", e);
        } catch (final Exception e) {
            LOG.error("Unknown error.", e);
        }
    }

    private static String getWorkingDir() {
        return System.getProperty("user.dir");
    }

    private static void extractFiles(final XarFile xarFile, final File destinationDir) throws IOException {
        xarFile.extractAll(destinationDir, false);
    }

    private static void listEntries(final XarFile xarfile) {
        final List<XarEntry> entries = xarfile.getEntries();
        for (final XarEntry entry : entries) {
            LOG.info(entry.toString());
        }
    }

    private static void dumpHeader(final XarFile xarFile) {
        final XarFile.Header header = xarFile.getHeader();

        final String headerStatus = header.hasValidMagic() ? "OK" : "INVALID";

        LOG.info("magic:                   {} ({})", String.format("%#x", header.getMagic().intValue()), headerStatus);
        LOG.info("size:                    {}", header.getSize());
        LOG.info("version:                 {}", header.getVersion());
        LOG.info("Compressed TOC length:   {}", header.getTocLengthCompressed());
        LOG.info("Uncompressed TOC length: {}", header.getTocLengthUncompressed());
        final int cksumAlg = header.getCksumAlg().intValue();
        LOG.info("Checksum algorithm:      {} ({})", cksumAlg, ChecksumAlgorithm.values()[cksumAlg].toString().toLowerCase());
    }

    private static void dumpToC(final XarFile xarFile, final File tocFile) {
        try {
            ToCFactory.copy(xarFile.getToCStream(), new FileOutputStream(tocFile));
        } catch (final Exception e) {
            LOG.error("Failed dumping header.", e);
        }
    }

    private static Options createCommandLineOptions() {
        final Option create = new Option(C_OPTION, "Creates an archive");

        final Option extract = new Option(X_OPTION, "Extracts an archive");

        final Option list = new Option(T_OPTION, "Lists an archive");

        final Option file = Option.builder(F_OPTION)
                                  .hasArg()
                                  .argName(FILENAME_OPTION)
                                  .desc("Specifies an archive to operate on [REQUIRED!]")
                                  .build();

        final Option dumpToc = Option.builder()
                                     .longOpt(DUMP_TOC_OPTION)
                                     .hasArg()
                                     .argName(FILENAME_OPTION)
                                     .desc("Has xar dump the xml header into the specified file.")
                                     .build();

        final Option dumpHeader = Option.builder()
                                        .longOpt(DUMP_HEADER_OPTION)
                                        .desc("Prints out the xar binary header information")
                                        .build();

        final Option verbose = Option.builder(V_OPTION)
                                     .desc("Print filenames as they are archived")
                                     .build();

        final Option version = Option.builder()
                                     .longOpt(VERSION_OPTION)
                                     .desc("Print xar's version number")
                                     .build();

        final Options options = new Options();
        options.addOption(create);
        options.addOption(extract);
        options.addOption(list);
        options.addOption(file);
        options.addOption(dumpToc);
        options.addOption(dumpHeader);
        options.addOption(verbose);
        options.addOption(version);
        return options;
    }

    private static void printHelp(final Options options) {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(USAGE, options);
    }

    private static void printVersion() {
        LOG.info("xar {}", Manifests.read(VERSION_MANIFEST_KEY));
        LOG.info("This is a port to pure Java.");
    }

}
