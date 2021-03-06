package io.bdrc.am.audit.audittests;

import io.bdrc.am.audit.iaudit.AuditTestConfig;

import java.util.Arrays;
import java.util.Hashtable;

import static io.bdrc.am.audit.audittests.TestArgNames.*;

/**
 * Moved from shell, so I can use class objects here, with names
 * placeholder for true dynamic linking:
 * See http://ronmamo.github.io/reflections/index.html?org/reflections/Reflections.html
 *
 */
@SuppressWarnings("unused")
public class TestDictionary {

    public TestDictionary() {
    }


    public Hashtable<String, AuditTestConfig> getTestDictionary() {
        return _TestDictionary;
    }

    private final Hashtable<String, AuditTestConfig> _TestDictionary = new Hashtable<String, AuditTestConfig>() {
        {
            put("FileSequence", new AuditTestConfig("File Sequence Test",

                    // This statement asserts that the caller has to provide values for these
                    // arguments
                    Arrays.asList(
                            ARC_GROUP_PARENT , DERIVED_GROUP_PARENT),
                    "FileSequence", FileSequence.class));

            //noinspection ArraysAsListWithZeroOrOneArgument
            put("NoFilesInFolder", new AuditTestConfig("No Files in Root Folder",
                    Arrays.asList(""),
                    "NoFilesInFolder",
                    NoFilesInRoot.class));

            put("NoFoldersInImageGroups", new AuditTestConfig("No folders allowed in Image Group folders",
                    Arrays.asList(   ARC_GROUP_PARENT , DERIVED_GROUP_PARENT),"NoFoldersInImageGroups",
                    NoFoldersInImageGroups.class));

            put("WebImageAttributes", new AuditTestConfig("Web Image Attributes",
                    Arrays.asList(DERIVED_GROUP_PARENT),"WebImageAttributes",
                    ImageAttributeTests.class));

            put("FileSizeTests", new AuditTestConfig("File Size Test",
                    Arrays.asList( DERIVED_GROUP_PARENT,MAX_IMAGE_FILE_SIZE),"FileSizeTest",ImageSizeTests
                                                                                                          .class));
        }
    };
}