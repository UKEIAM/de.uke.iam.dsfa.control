package de.uke.iam.dsfa.control.util;

import de.uke.iam.dsfa.control.db.DatabaseConfiguration;
import de.uke.iam.dsfa.control.db.DatabaseUtil;
import de.uke.iam.dsfa.control.db.jooq.tables.pojos.DamagingEvent;
import de.uke.iam.dsfa.control.db.jooq.tables.pojos.RiskSource;
import de.uke.iam.dsfa.control.db.jooq.tables.pojos.Tom;
import de.uke.iam.dsfa.control.db.jooq.tables.pojos.UseCase;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelReader {
    private static Logger logger = LoggerFactory.getLogger(ExcelReader.class);

    static DSLContext dsl = DatabaseConfiguration.get().getDsl();
    static List<String> errorLines = new ArrayList<String>();

    //check if the name of the sheet corresponds
    // to a name of a risSource sheet: like (RQ NameOfCategory)
    public static boolean isRiskSourceSheet(String sheetName) {

        Pattern pattern = Pattern.compile("RQ\\s+(.+)");
        Matcher matcher = pattern.matcher(sheetName);

        return matcher.find();
    }

    // get the Category of the riskSource from the sheetName if this sheet is
    // a riskSource sheet (RQ Beantragung) -> Beantragung (is the Category)
    public static String getRiskSourceCategoryFromSheetName(String sheetName) {

        Pattern pattern = Pattern.compile("RQ\\s+(.+)");
        Matcher matcher = pattern.matcher(sheetName);

        // if the name of the sheet is like the name of RiskSourceSheet
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    // check if the value of the cell is like UC(Number) (Name)
    // example : UC1 First Case -> True
    public static boolean isUsecaseCell(Cell cell) {

        Pattern pattern = Pattern.compile("UC(\\d+)\\s*(.*)");
        Matcher matcher = pattern.matcher(cell.getStringCellValue());

        return matcher.find();
    }

    // if the cell is like UC(Number) (Name) then a usecase object,
    // example : UC1 First Case -> usecaseId = 1, usecaseName = First Case
    public static UseCase getUsecaseFromCell(Cell cell) {

        Pattern pattern = Pattern.compile("UC(\\d+)\\s*(.*)");
        Matcher matcher = pattern.matcher(cell.getStringCellValue());
        UseCase usecase = new UseCase();

        if (matcher.find()) {
            usecase.setId(Integer.valueOf(matcher.group(1)));

            if (!matcher.group(2).isEmpty()) {
                usecase.setDescription(matcher.group(2));
            }
        }

        return usecase;

    }

    // add all risk Sources in the given sheet to the Database and report an error by no sucess
    private static void ProcessRiskSourcesSheet(Sheet sheet, String riskSourceCategoryDescription) {

        for (Row row : sheet) {

            // (row.getRowNum() > 0) is to ignore the first line in this sheet
            if (row.getRowNum() > 0 && row.getCell(0) != null && row.getCell(1) != null) {
                // riskSourceCategoryId will be set inside insertRiskSource method
                // thats why we set that to null here
                RiskSource riskSource = new RiskSource(row.getCell(0).getStringCellValue(),
                        row.getCell(1).getStringCellValue(), null);

                try {
                    DatabaseUtil.insertRiskSource(dsl, riskSource, riskSourceCategoryDescription);
                } catch (DataAccessException e) {
                    errorLines.add(
                            "An Error in Sheet " + sheet.getSheetName() + " and line Number " + (row.getRowNum()
                                    + 1) + " detected");
                }
            }
        }

    }

    // Convert the sheets that have two columns to a hashmap,
    // where the first column is considered a key and the second a value
    private static HashMap<String, String> sheetToHashMap(Sheet sheet, Integer NrOfIgnoredRows) {

        HashMap<String, String> values = new HashMap<>();

        for (Row row : sheet) {

            if (row.getRowNum() >= NrOfIgnoredRows && row.getCell(0) != null && row.getCell(1) != null) {

                values.put(row.getCell(0).getStringCellValue(),
                        row.getCell(1).getStringCellValue());
            }
        }

        return values;
    }

    // add all Toms in the given sheet to the Database and report an error by no sucess
    private static void ProcessTomsSheet(Sheet sheet) {

        for (Row row : sheet) {

            if (row.getRowNum() >= 2 && row.getCell(0) != null && row.getCell(3) != null) {
                Tom tom = new Tom(row.getCell(0).getStringCellValue(),
                        row.getCell(3).getStringCellValue());

                try {
                    DatabaseUtil.insertTom(dsl, tom);
                } catch (DataAccessException e) {
                    errorLines.add(
                            "An Error in Sheet " + sheet.getSheetName() + " and line Number " + (row.getRowNum()
                                    + 1) + " detected");
                    e.printStackTrace();
                }
            }
        }

    }

    // read the value of each row
    private static void ProcessTomCells(HashMap<String, Integer> columnIndexes, Row row)
            throws DataAccessException {

        String damagingEventId = row.getCell(columnIndexes.get("Schaden-(sereignis)"))
                .getStringCellValue();
        Cell tom1Cell = row.getCell(columnIndexes.get("TOM 1"));
        Cell tom2Cell = row.getCell(columnIndexes.get("TOM 2"));
        Cell tom3Cell = row.getCell(columnIndexes.get("TOM 3"));
        Cell tom4Cell = row.getCell(columnIndexes.get("TOM 4"));
        Cell tom5Cell = row.getCell(columnIndexes.get("TOM 5"));
        Cell tom6Cell = row.getCell(columnIndexes.get("TOM 6"));
        Cell tom7Cell = row.getCell(columnIndexes.get("TOM 7"));
        Cell tom8Cell = row.getCell(columnIndexes.get("TOM 8"));
        Cell tom9Cell = row.getCell(columnIndexes.get("TOM 9"));
        Cell tom10Cell = row.getCell(columnIndexes.get("TOM 10"));
        Cell tom11Cell = row.getCell(columnIndexes.get("TOM 11"));
        Cell tom12Cell = row.getCell(columnIndexes.get("TOM 12"));
        Cell tom13Cell = row.getCell(columnIndexes.get("TOM 13"));
        if (tom1Cell != null) {
            String tom1 = tom1Cell.getStringCellValue();
            if (!tom1.equals("")) {
                DatabaseUtil.insertDamagingEventTom(dsl, damagingEventId, tom1);
            }
        }
        if (tom2Cell != null) {
            String tom2 = tom2Cell.getStringCellValue();
            if (!tom2.equals("")) {
                DatabaseUtil.insertDamagingEventTom(dsl, damagingEventId, tom2);
            }
        }
        if (tom3Cell != null) {
            String tom3 = tom3Cell.getStringCellValue();
            if (!tom3.equals("")) {
                DatabaseUtil.insertDamagingEventTom(dsl, damagingEventId, tom3);
            }
        }
        if (tom4Cell != null) {
            String tom4 = tom4Cell.getStringCellValue();
            if (!tom4.equals("")) {
                DatabaseUtil.insertDamagingEventTom(dsl, damagingEventId, tom4);
            }
        }
        if (tom5Cell != null) {
            String tom5 = tom5Cell.getStringCellValue();
            if (!tom5.equals("")) {
                DatabaseUtil.insertDamagingEventTom(dsl, damagingEventId, tom5);
            }
        }
        if (tom6Cell != null) {
            String tom6 = tom6Cell.getStringCellValue();
            if (!tom6.equals("")) {
                DatabaseUtil.insertDamagingEventTom(dsl, damagingEventId, tom6);
            }
        }
        if (tom7Cell != null) {
            String tom7 = tom7Cell.getStringCellValue();
            if (!tom7.equals("")) {
                DatabaseUtil.insertDamagingEventTom(dsl, damagingEventId, tom7);
            }
        }
        if (tom8Cell != null) {
            String tom8 = tom8Cell.getStringCellValue();
            if (!tom8.equals("")) {
                DatabaseUtil.insertDamagingEventTom(dsl, damagingEventId, tom8);
            }
        }
        if (tom9Cell != null) {
            String tom9 = tom9Cell.getStringCellValue();
            if (!tom9.equals("")) {
                DatabaseUtil.insertDamagingEventTom(dsl, damagingEventId, tom9);
            }
        }
        if (tom10Cell != null) {
            String tom10 = tom10Cell.getStringCellValue();
            if (!tom10.equals("")) {
                DatabaseUtil.insertDamagingEventTom(dsl, damagingEventId, tom10);
            }
        }
        if (tom11Cell != null) {
            String tom11 = tom11Cell.getStringCellValue();
            if (!tom11.equals("")) {
                DatabaseUtil.insertDamagingEventTom(dsl, damagingEventId, tom11);
            }
        }
        if (tom12Cell != null) {
            String tom12 = tom12Cell.getStringCellValue();
            if (!tom12.equals("")) {
                DatabaseUtil.insertDamagingEventTom(dsl, damagingEventId, tom12);
            }
        }
        if (tom13Cell != null) {
            String tom13 = tom13Cell.getStringCellValue();
            if (!tom13.equals("")) {
                DatabaseUtil.insertDamagingEventTom(dsl, damagingEventId, tom13);
            }
        }

    }

    public static void ProcessUsecaseCells(HashMap<String, Integer> columnIndexes, Row row,
                                           HashMap<String, Integer> usecaseNameId) throws DataAccessException {
        String riskSourceId = row.getCell(columnIndexes.get("Risiko-quelle")).getStringCellValue();
        String damagingEventId = row.getCell(columnIndexes.get("Schaden-(sereignis)")).getStringCellValue();
        for (String usecaseCellName : usecaseNameId.keySet()) {
            if (row.getCell(columnIndexes.get(usecaseCellName)) != null) {
                // get the value of the cell for each usecase column
                String usecaseCellValue = row.getCell(columnIndexes.get(usecaseCellName))
                        .getStringCellValue();
                // if the value of the cell is x then map this usecase to risksource
                if (usecaseCellValue.equals("x")) {
                    // get the id of the usecase from the name of column (header)
                    Integer usecaseId = usecaseNameId.get(usecaseCellName);
                    DatabaseUtil.insertUsecaseRisksource(dsl, usecaseId, riskSourceId);
                    DatabaseUtil.insertUsecaseDamagingEvent(dsl, usecaseId, damagingEventId);
                }
            }
        }
    }


    // Todo , which Exception could be thrown if there is not text for the value
    public static DamagingEvent ProcessDamagingEventCells(HashMap<String, Integer> columnIndexes,
                                                          Row row,
                                                          HashMap<String, String> damagingEventHashMap,
                                                          HashMap<String, String> probabilityOfOccurrence,
                                                          HashMap<String, String> damageSeverity,
                                                          HashMap<String, String> probabilityOfOccurrenceWithTom,
                                                          HashMap<String, String> damageSeverityWithTom) throws DataAccessException {

        String damagingEventId = row.getCell(columnIndexes.get("Schaden-(sereignis)"))
                .getStringCellValue();
        // get the Integer Values of the row
        Integer probabilityOfOccurrenceValue =
                (int) row.getCell(columnIndexes.get("Eintritts-wahrschein-lichkeit"))
                        .getNumericCellValue();

        Integer damageSeverityValue =
                (int) row.getCell(columnIndexes.get("Schwere des Schadens")).getNumericCellValue();

        Integer riskRatingValue =
                (int) row.getCell(columnIndexes.get("Risiko-abstufung")).getNumericCellValue();

        Integer probabilityOfOccurrenceWithTomValue =
                (int) row.getCell(columnIndexes.get("Eintritts-wahrschein-lichkeit mit TOM"))
                        .getNumericCellValue();

        Integer damageSeverityWithTomValue =
                (int) row.getCell(columnIndexes.get("Schwere des Schadens mit TOM"))
                        .getNumericCellValue();

        Integer residualRiskRatingValue =
                (int) row.getCell(columnIndexes.get("Rest-Risiko-abstufung"))
                        .getNumericCellValue();

        // set DamagingEvent Element
        DamagingEvent damagingEvent = new DamagingEvent();
        damagingEvent.setId(damagingEventId);
        // set the Text of each value from its Hashmap,
        // which we extract earlier using sheetToHashMap Method
        // Todo : Do we have to check if the value of the text is null ?
        damagingEvent.setDescription(
                damagingEventHashMap.get(damagingEvent.getId()));
        damagingEvent.setProbabilityOfOccurrenceValue(probabilityOfOccurrenceValue);
        damagingEvent.setProbabilityOfOccurrenceDescription(
                probabilityOfOccurrence.get(damagingEventId));
        damagingEvent.setDamageSeverityValue(damageSeverityValue);
        damagingEvent.setDamageSeverityDescription(
                damageSeverity.get(damagingEventId));
        damagingEvent.setRiskRatingValue(riskRatingValue);
        damagingEvent.setProbabilityOfOccurrenceWithTomValue(probabilityOfOccurrenceWithTomValue);
        damagingEvent.setProbabilityOfOccurrenceWithTomDescription(
                probabilityOfOccurrenceWithTom.get(damagingEventId));
        damagingEvent.setDamageSeverityWithTomValue(damageSeverityWithTomValue);
        damagingEvent.setDamageSeverityWithTomDescription(
                damageSeverityWithTom.get(damagingEventId));
        damagingEvent.setResidualRiskRatingValue(residualRiskRatingValue);

        return damagingEvent;
    }


    private static void ProcessTotalSheet(XSSFWorkbook workbook,
                                          HashMap<String, String> damagingEventHashMap,
                                          HashMap<String, String> probabilityOfOccurrence,
                                          HashMap<String, String> damageSeverity,
                                          HashMap<String, String> probabilityOfOccurrenceWithTom,
                                          HashMap<String, String> damageSeverityWithTom) {

        HashMap<String, Integer> columnIndexes = new HashMap<>();
        HashMap<String, Integer> usecaseNameId = new HashMap<>();

        XSSFSheet sheet = workbook.getSheet("gesamt");

        for (Row row : sheet) {

            Integer rowNr = row.getRowNum();
            if (rowNr == 1) {

                for (Cell cell : row) {

                    // save the Index of each column value in the second line (header of columns)
                    // This step is to make sure that the reader will work even if the columns are not sorted
                    // as given in the excel file
                    String cellStringValue = cell.getStringCellValue();
                    columnIndexes.put(cellStringValue, cell.getColumnIndex());

                    //if the cell name is like : UC(Number) (NameOfUsecase)
                    if (isUsecaseCell(cell)) {
                        // save the infos about the cell
                        UseCase usecase = getUsecaseFromCell(cell);
                        // save the cell name and the id of the usecase to use it later
                        usecaseNameId.put(cellStringValue, usecase.getId());

                        try {
                            DatabaseUtil.insertUsecase(dsl, usecase);
                        } catch (DataAccessException e) {
                            errorLines.add("Some of Usecases could not be imported, so all damaging events will"
                                    + " not be sorted to usecases");
                        }

                    }
                }
            }
            // start reading from the third line
            else if (rowNr > 1 && row.getCell(0) != null && !row.getCell(0).getStringCellValue().equals("")) {

                // Get the values of damagingEvent and riskSource from the cell
                String damagingEventId = row.getCell(columnIndexes.get("Schaden-(sereignis)"))
                        .getStringCellValue();

                String riskSourceId = row.getCell(columnIndexes.get("Risiko-quelle")).getStringCellValue();

                DamagingEvent damagingEvent = ProcessDamagingEventCells(columnIndexes, row,
                        damagingEventHashMap, probabilityOfOccurrence, damageSeverity,
                        probabilityOfOccurrenceWithTom, damageSeverityWithTom);

                try {
                    DatabaseUtil.insertDamagingEvent(dsl, damagingEvent);
                } catch (DataAccessException e) {
                    errorLines.add("DamagingEvent in line " + (rowNr + 1) + " can not be saved to the Db");
                }

                try {
                    ProcessUsecaseCells(columnIndexes, row, usecaseNameId);
                } catch (DataAccessException e) {
                    errorLines.add(
                            "Usecase in line " + (rowNr + 1) + " can not be mapped to the DamagingEvent");
                    continue;
                }

                try {
                    ProcessTomCells(columnIndexes, row);
                } catch (DataAccessException e) {
                    errorLines.add("Tom in line " + (rowNr + 1) + " can not be mapped to the DamagingEvent");
                    continue;
                }

                try {
                    DatabaseUtil.insertDamagingEventRiskSource(dsl, riskSourceId, damagingEventId);
                } catch (DataAccessException e) {
                    errorLines.add(
                            "RiskSource in line " + (rowNr + 1) + " can not be mapped to DamagingEvent");
                }


            }
        }
    }

    public static void readFile(File excelFile, String logFilePath) {
        DSLContext dsl = DatabaseConfiguration.get().getDsl();
        /*  delete all entries of all tables to write a new entries */
        DatabaseUtil.truncateAllTables(dsl);

        HashMap<String, String> damagingEvent = null;
        HashMap<String, String> probabilityOfOccurrence = null;
        HashMap<String, String> damageSeverity = null;
        HashMap<String, String> probabilityOfOccurrenceWithTom = null;
        HashMap<String, String> damageSeverityWithTom = null;

        try {
            FileInputStream file = new FileInputStream(excelFile);
            //Create Workbook instance holding reference to .xlsx file
            XSSFWorkbook workbook = new XSSFWorkbook(file);

            for (Sheet sheet : workbook) {
                String actualSheetName = sheet.getSheetName();

                if (actualSheetName.equals("Darstellung Schaden(sereignis)")) {
                    damagingEvent = sheetToHashMap(sheet, 0);
                }
                if (actualSheetName.equals("Begr EW")) {
                    probabilityOfOccurrence = sheetToHashMap(sheet, 0);
                }
                if (actualSheetName.equals("Begr SdS")) {
                    damageSeverity = sheetToHashMap(sheet, 0);
                }
                if (actualSheetName.equals("Begr EW m. TOM")) {
                    probabilityOfOccurrenceWithTom = sheetToHashMap(sheet,
                            0);
                }
                if (actualSheetName.equals("Begr SdS m. TOM")) {
                    damageSeverityWithTom = sheetToHashMap(sheet, 0);
                }
                if (actualSheetName.equals("TOMs")) {
                    ProcessTomsSheet(sheet);
                }
                if (isRiskSourceSheet(actualSheetName)) {
                    String riskSourceCategoryDescription = getRiskSourceCategoryFromSheetName(
                            actualSheetName);
                    DatabaseUtil.insertRiskSourceCategory(dsl, riskSourceCategoryDescription);
                    ProcessRiskSourcesSheet(sheet, riskSourceCategoryDescription);
                }
            }

            ProcessTotalSheet(workbook,
                    damagingEvent,
                    probabilityOfOccurrence,
                    damageSeverity,
                    probabilityOfOccurrenceWithTom,
                    damageSeverityWithTom);

            file.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            logger.error("File not found : Please check the path of your file");
        } catch (IOException e) {
            logger.error("File could not be read");
        }
        // if errors are found, then save them to log file
        if (!errorLines.isEmpty()) {
            Path out = Paths.get(logFilePath + "log.txt");
            try {
                Files.write(out, errorLines, Charset.defaultCharset());
                logger.error("Log File was created with occuring Erros");
            } catch (IOException e) {
                logger.error("Log File could not be created");
                e.printStackTrace();
            }
            // no errors found
        } else {
            logger.debug("Excel File imported without any problems");
        }

    }
}
