package io.bdrc.am.audit.shell;

// https://commons.apache.org/proper/commons-cli/usage.html

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ArgParser {

    /* Persist the options */
    private CommandLine cl;

    private final String infileOptionShort = "i";
    private final String infileOptionLong = "inputFile";

    private final String logHome = "l";
    private final String logHomeLong = "log_home";
    private final String infileOptionStdin = "-";
    private final String argsep = ",";

    private final Logger logger = LoggerFactory.getLogger("shellLogger");

    private Boolean isParsed;
    private List<String> nonOptionArgs;

    /**
     * ArgParser. Returns values of arguments.
     *
     * @param args command line input after Java enviro vars parsed and removed.
     */
    ArgParser(String[] args) {


        // Create the parser
        CommandLineParser clp = new DefaultParser();

        Options options = new Options();

        options.addOption("d", "debug", false, "Show debugging information");



        options.addOption(Option.builder(infileOptionShort)
                .longOpt(infileOptionLong)
                .hasArg()
                .desc("Input file, one path per line")
                .build());

//         instead of adding to the group, add to mainline options

        options.addOption(Option.builder(logHome)
                .longOpt(logHomeLong)
                .hasArg(true)
                .desc("Test Result log directory. Must be writable. Default is <UserHome>/audit-test-logs/TestResults" +
                              ". Created if not exists ")
                .required(false)
                .build());


        try {
            cl = clp.parse(options, args);
            isParsed = true;
            nonOptionArgs = cl.getArgList();
        } catch (ParseException exc) {
            logger.error("Failed to parse {}", exc.getMessage());

            printHelp(options);
            isParsed = false;
        }

        // sanity check. One of these must be true
        if (!has_Dirlist() && !getReadStdIn())
        {
            printHelp(options);
            isParsed = false;
        }

        // Log home directory must be writable
        String TestLogHome =  System.getProperty("user.home") + "/audit-test-logs/TestResults";
        String logDir = cl.getOptionValue("l",TestLogHome);
        if (!madeWritableDir(logDir))
        {
            printHelp(options);
        }


    }

    /**
     * Test for input as argument or as a file
     *
     * @return true when the infile option is set or a list of directories is given on the command line.
     */
    Boolean has_Dirlist() {
        Boolean rc = cl.hasOption(infileOptionShort) || get_IfArgsCommandLine();
        logger.debug("hasOption {} !getReadStdin {} has_Dirlist net: {} " ,cl.hasOption(infileOptionShort), !getReadStdIn
                        (),
                rc );

        return rc ;
    }

    /**
     *
     * @return If the user has specified reading from standard input
     */
    Boolean getReadStdIn() {
        logger.debug("nonoption args empty {}",nonOptionArgs.isEmpty());
        return (!nonOptionArgs.isEmpty()) && nonOptionArgs.get(0).equals
                (infileOptionStdin);
    }

    /**
     *
     * @return If the user has specified reading from standard input
     */
    Boolean get_IfArgsCommandLine() {
        logger.debug("get_IfArgsCommandLine nonoption args empty {}",nonOptionArgs.isEmpty());
        return !nonOptionArgs.isEmpty() && !nonOptionArgs.get(0).equals
                (infileOptionStdin);
    }

    /**
     * Extracts the dir arguments
     *
     * @return contents of the "-i --input" argument or the ,delimited list of arguments
     */
    ArrayList<String> getDirs() throws IOException {

        List<String> fileArgs;
        ArrayList<String> returned = new ArrayList<>();

        // if we have an
        if (!isParsed)
            return returned;

        // If an infile was specified, read it
        if (cl.hasOption(infileOptionShort)) {
            String argFile = cl.getOptionValue(infileOptionShort);
            try {
                fileArgs =
                        Files.readAllLines(Paths.get(argFile), StandardCharsets.UTF_8);
                returned = new ArrayList<>(fileArgs);

            } catch (FileNotFoundException fnfe) {
                logger.error("{} {}", argFile, " not found.");
            } catch (IOException e) {
                e.printStackTrace();
                throw e;

            }
        }
        else {
            // If args isnt the magic stdin delimiter, parse a delimited list of paths
            String inArgs = nonOptionArgs.get(0);
            if (!getReadStdIn())
            {
                returned = new ArrayList<>(Arrays.asList(inArgs.split(argsep)));
            }

        }
        return returned;
    }


    private void printHelp(final Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("AuditTest [options] { - | Directory,Directory,Directory}\nwhere:\n\t- read " +
                        "folders from " +
                        "standard input\n\t" +
                        "Directory,.... is a list of directories separated by ,\n[options] are:",
                options);
    }

    /**
     * Creates a directory, or checks an existing one for writability
     * @param dirPath full path to directory (caller must resolve)
     * @return true if directory is writable, false if not or if it cannot create.
     */
    private boolean madeWritableDir(String dirPath) {

        Path createdPath = Paths.get(dirPath);
        boolean ok = false;

        if (!Files.exists(createdPath))
        {
            try
            {
                Files.createDirectories(createdPath);
                ok = Files.isWritable(createdPath);
            } catch (IOException e)
            {
                ok = false ;
            }
        }
        else
        {
            if (Files.isDirectory(createdPath)) {
                ok = Files.isWritable(createdPath);
            }
        }
        return ok;
    }


}

