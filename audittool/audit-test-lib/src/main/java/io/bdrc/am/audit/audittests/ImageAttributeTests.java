package io.bdrc.am.audit.audittests;

//import ij.ImagePlus;
//import ij.io.FileInfo;
//import ij.io.TiffDecoder;
import com.google.common.collect.Streams;
import io.bdrc.am.audit.iaudit.Outcome;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Iterator;

//import org.bytedeco.opencv.opencv_core.IplImage;
//import org.opencv.core.* ;
//import org.opencv.core.Size;
//import org.opencv.imgcodecs.Imgcodecs;
//import sun.awt.image.ImageDecoder;
//import sun.awt.image.ImageFormatException;
//import com.sun.media.jai.*;

public class ImageAttributeTests extends PathTestBase {
    /**
     * new AuditTestBase
     *
     * @param logger internal logger
     */
    public ImageAttributeTests(Logger logger)
    {
        super("ImageAttributeTests");
        sysLogger = logger;
    }

    public class ImageAttributes implements AuditTestBase.ITestOperation {

        @Override
        public void run() throws IOException {
            // throw new IOException("Not implemented");
            Path rootFolder = Paths.get(getPath());

            // This test only examines derived image image groups
            Path examineDir = Paths.get(getPath(), testParameters.get("DerivedImageGroupParent"));

// Creating the filter
            DirectoryStream.Filter<Path> filter =
                    entry -> (entry.toFile().isDirectory() && !(entry.toFile().isHidden()));

            try (DirectoryStream<Path> imageGroupDirs = Files.newDirectoryStream(examineDir, filter)) {
                for (Path imagegroup : imageGroupDirs) {
                    TestImages(imagegroup);
                }

            } catch (DirectoryIteratorException die) {
                sysLogger.error("Directory iteration error", die);
                FailTest(Outcome.SYS_EXC, die.getCause().getLocalizedMessage());

            }
        }

        @Override
        public String getName() {
            return getTestName();
        }

    private void TestImages(final Path imageGroup) throws IOException {

        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("JPEG");
        ImageReader aReader;
        while (readers.hasNext()) {
            aReader = readers.next();
            System.out.println("reader: " + aReader);
        }

       readers = ImageIO.getImageReadersByFormatName("TIFF");
        while (readers.hasNext()) {
            aReader = readers.next();
            System.out.println("reader: " + aReader);
        }
        DirectoryStream.Filter<Path> filter =
                entry -> (entry.toFile().isFile() && !(entry.toFile().isHidden()));
        try (DirectoryStream<Path> imageFiles = Files.newDirectoryStream(imageGroup, filter)) {
            for (Path imageFile : imageFiles) {
                File fileObject = imageFile.toAbsolutePath().toFile();
                String fileObjectPathString = imageFile.toAbsolutePath().toString();
                long imageLength = fileObject.length();

//                String fileExt = FilenameUtils.getExtension(fileObjectPathString);
//                Iterator<ImageReader> readers = ImageIO.getImageReadersBySuffix(fileExt);
//                if (!readers.hasNext())
//                {
//                    FailTest(LibOutcome.NO_IMAGE_READER,fileObjectPathString);
//                    continue;
//                }
//                ImageReader firstReader = readers.next();
                ImageInputStream in = ImageIO.createImageInputStream(fileObject);
                try {
                    ImageReader reader =    Streams.stream(ImageIO.getImageReaders(in)).findFirst().orElseThrow
                            (UnsupportedFormatException::new);
                    reader.setInput(in,true);

                    // TODO:  1 image / file !!!!
                    int iiwidth = reader.getWidth(0);
                    int iiHeight = reader.getHeight(0);
                    ImageTypeSpecifier imgType = reader.getImageTypes(0).next();
                    int iibitDepth = imgType.getColorModel().getPixelSize();
                    int imgTypeNum = imgType.getBufferedImageType();

                            IIOMetadata imageMeta = reader.getStreamMetadata();
                            sysLogger.info(imageMeta.getNativeMetadataFormatName());
                }
                catch(UnsupportedFormatException eek) {
                    FailTest(LibOutcome.NO_IMAGE_READER, fileObjectPathString);
                }
            }

        }
    }

}

    @Override
    public void LaunchTest() {
        TestWrapper(new ImageAttributes());
    }

    /**
     * Set all test parameters (not logging or framework)
     * TestProcessedImage expects
     * 1. path - parent container
     * 2. kwargs: These named arguments are required in (String [])(params[1])
     * "DerivedImageGroupParent=folderName
     * "ImageFileSizeLimit=0....n"
     *
     * @param params implementation dependent optional parameters
     */
    @Override
    public void setParams(final Object... params) {
        if ((params == null) || (params.length < 2)) {
            throw new IllegalArgumentException(String.format("Audit test :%s: Required Arguments path, and " +
                            "propertyDictionary not given.",
                    getTestName()));
        }
        super.setParams(params[0]);
        LoadParameters((String[]) (params[1]));
    }
}
