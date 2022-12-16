package de.uke.iam.dsfa.control.test;


import de.samply.config.util.FileFinderUtil;
import de.uke.iam.dsfa.control.model.ExcelReaderResponse;
import de.uke.iam.dsfa.control.util.ExcelReader;
import de.uke.iam.dsfa.control.util.WordWriter;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UtilTest  {
    private Logger logger = LoggerFactory.getLogger(UtilTest.class);
    @Test
    public void ExcelReaderTest() throws FileNotFoundException {
        // Nutzt bitte den Filefinder oder den Classloader, um Dateien aus "resources" zu laden
        File excelFile = FileFinderUtil.findFile("example.xlsx");

        logger.debug("Found file: " + excelFile.getAbsolutePath());
        FileInputStream file = ExcelReader.fileToInputStream(excelFile);
        ExcelReader.readFile(file);
        ExcelReaderResponse response = ExcelReader.getResponse();
        System.out.println(response.getStatus());
    }
    @Test
    public void ExcelReaderWithErrorsTest() throws FileNotFoundException {
        // Nutzt bitte den Filefinder oder den Classloader, um Dateien aus "resources" zu laden
        File excelFile = FileFinderUtil.findFile("exampleWithErrors.xlsx");

        logger.debug("Found file: " + excelFile.getAbsolutePath());
        FileInputStream file = ExcelReader.fileToInputStream(excelFile);
        ExcelReader.readFile(file);
        ExcelReaderResponse response = ExcelReader.getResponse();
        System.out.println(response.getStatus());
        for (String comment : response.getComments()) {
            System.out.println(comment);
        }
    }

    @Test
    public void WordWriterTest() throws IOException {

        List<Integer> useCaseIDs = new ArrayList<>();

        useCaseIDs.add(3);
        useCaseIDs.add(4);

        XWPFDocument document = WordWriter.getWord(useCaseIDs);
        WordWriter.saveFile(document, "WordExample.docx", ".");
    }

    }
