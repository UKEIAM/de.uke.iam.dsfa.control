package de.uke.iam.dsfa.control.test;


import de.samply.config.util.FileFinderUtil;
import de.uke.iam.dsfa.control.db.DatabaseConfiguration;
import de.uke.iam.dsfa.control.util.ExcelReader;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;

public class UtilTest  {
    static DSLContext dsl = DatabaseConfiguration.get().getDsl();
    private Logger logger = LoggerFactory.getLogger(UtilTest.class);
    @Test
    public void ExcelReaderTest() throws FileNotFoundException {
        // Nutzt bitte den Filefinder oder den Classloader, um Dateien aus "resources" zu laden
        File excelFile = FileFinderUtil.findFile("example.xlsx");

        logger.debug("Found file: " + excelFile.getAbsolutePath());

        ExcelReader.readFile(excelFile,"target/");
    }

    }
