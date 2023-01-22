package de.uke.iam.dsfa.control.util;

import de.uke.iam.dsfa.control.db.DatabaseConfiguration;
import de.uke.iam.dsfa.control.db.DatabaseUtil;
import de.uke.iam.dsfa.control.db.jooq.tables.pojos.DamagingEvent;
import de.uke.iam.dsfa.control.db.jooq.tables.pojos.RiskSource;
import de.uke.iam.dsfa.control.db.jooq.tables.pojos.Tom;
import de.uke.iam.dsfa.control.db.jooq.tables.pojos.UseCase;
import de.uke.iam.dsfa.control.model.ExcelReaderResponse;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;


import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
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

    static String damagingEventHeader = "Schaden-(sereignis)";
    static List<String> tomHeaders = Arrays.asList("TOM 1", "TOM 2", "TOM 3", "TOM 4", "TOM 5", "TOM 6", "TOM 7", "TOM 8",
            "TOM 9", "TOM 10", "TOM 11", "TOM 12", "TOM 13");
    static String probabilityOfOccurrenceHeader = "Eintritts-wahrschein-lichkeit";
    static String damageSeverityHeader = "Schwere des Schadens";
    static String riskRatingHeader = "Risiko-abstufung";
    static String probabilityOfOccurrenceWithTomHeader = "Eintritts-wahrschein-lichkeit mit TOM";
    static String damageSeverityWithTomHeader = "Schwere des Schadens mit TOM";
    static String residualRiskRatingHeader = "Rest-Risiko-abstufung";
    static String riskSourceHeader = "Risiko-quelle";
    static String totalSheetName = "gesamt";
    static String damagingEventSheetName = "Darstellung Schaden(sereignis)";
    static String probabilityOfOccurrenceSheetName = "Begr EW";
    static String damageSeveritySheetName ="Begr SdS";
    static String probabilityOfOccurrenceWithTomSheetName = "Begr EW m. TOM";
    static String damageSeverityWithTomSheetName = "Begr SdS m. TOM";
    static String TomsSheetName = "TOMs";

    static DSLContext dsl = DatabaseConfiguration.get().getDsl();

    static ExcelReaderResponse response = new ExcelReaderResponse();
    static List<String> errorLines = new ArrayList<String>();
    public static ExcelReaderResponse getResponse() {
        return response;
    }

    public static XSSFWorkbook checkFileStructure(InputStream file) {
        List<String> headers = new ArrayList<String>(Arrays.asList(damagingEventHeader, damageSeverityHeader, riskRatingHeader, probabilityOfOccurrenceWithTomHeader,
            damageSeverityWithTomHeader, residualRiskRatingHeader, riskSourceHeader));
        List<String> sheets = Arrays.asList(totalSheetName, damagingEventSheetName, probabilityOfOccurrenceSheetName, damageSeveritySheetName,
            probabilityOfOccurrenceWithTomSheetName, damageSeverityWithTomSheetName, TomsSheetName);
        try{
            //Create Workbook instance holding reference to .xlsx file
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            for (Sheet sheet : workbook) {
                String actualSheetName = sheet.getSheetName();
                if (sheets.contains(actualSheetName)) {
                    if(actualSheetName.equals(totalSheetName)){
                        XSSFSheet totalsheet = workbook.getSheet(totalSheetName);
                        for (Row row : totalsheet) {
                            Integer rowNr = row.getRowNum();
                            if (rowNr == 1) {
                                for (Cell cell : row) {
                                    if (!headers.contains(cell.getStringCellValue())) {
                                        errorLines.add("The Header '" + cell.getStringCellValue() + "' in 'gesamt' sheet could not be"
                                            + " recognize, please provide a file with the right structure");
                                    }
                                }
                            }
                        }
                    }
                } else {
                    errorLines.add("The Sheet '" + actualSheetName + "' could not be"
                        + " recognize, please provide a file with the right structure");
                }
            }
            return workbook;
        } catch (IOException e){
            errorLines.add("Error by reading file, please provide an excel file");
            response.setStatus("ERROR");
            response.setComments(errorLines);
            e.printStackTrace();
            return null;
        }


    }

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

    private static void ProcessTomCell(HashMap<String, Integer> columnIndexes, Row row, String tomHeader,
                                       String damagingEventId){
        Cell tomCell = row.getCell(columnIndexes.get(tomHeader));
        if (tomCell != null) {
            String tom1 = tomCell.getStringCellValue();
            if (!tom1.equals("")) {
                DatabaseUtil.insertDamagingEventTom(dsl, damagingEventId, tom1);
            }
        }
    }

    // read the value of each row
    private static void ProcessTomCells(HashMap<String, Integer> columnIndexes, Row row)
            throws DataAccessException {
        String damagingEventId = row.getCell(columnIndexes.get(damagingEventHeader))
                .getStringCellValue();
        for (String tomHeader : tomHeaders) {
            ProcessTomCell(columnIndexes, row, tomHeader, damagingEventId);
        }
    }

    public static void ProcessUsecaseCells(HashMap<String, Integer> columnIndexes, Row row,
                                           HashMap<String, Integer> usecaseNameId) throws DataAccessException {
        String riskSourceId = row.getCell(columnIndexes.get(riskSourceHeader)).getStringCellValue();
        String damagingEventId = row.getCell(columnIndexes.get(damagingEventHeader)).getStringCellValue();
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

        String damagingEventId = row.getCell(columnIndexes.get(damagingEventHeader))
                .getStringCellValue();
        // get the Integer Values of the row
        Integer probabilityOfOccurrenceValue =
                (int) row.getCell(columnIndexes.get(probabilityOfOccurrenceHeader))
                        .getNumericCellValue();

        Integer damageSeverityValue =
                (int) row.getCell(columnIndexes.get(damageSeverityHeader)).getNumericCellValue();

        Integer riskRatingValue =
                (int) row.getCell(columnIndexes.get(riskRatingHeader)).getNumericCellValue();

        Integer probabilityOfOccurrenceWithTomValue =
                (int) row.getCell(columnIndexes.get(probabilityOfOccurrenceWithTomHeader))
                        .getNumericCellValue();

        Integer damageSeverityWithTomValue =
                (int) row.getCell(columnIndexes.get(damageSeverityWithTomHeader))
                        .getNumericCellValue();

        Integer residualRiskRatingValue =
                (int) row.getCell(columnIndexes.get(residualRiskRatingHeader))
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

        XSSFSheet sheet = workbook.getSheet(totalSheetName);
        long startTime = System.nanoTime();
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
                String damagingEventId = row.getCell(columnIndexes.get(damagingEventHeader))
                        .getStringCellValue();

                String riskSourceId = row.getCell(columnIndexes.get(riskSourceHeader)).getStringCellValue();

                DamagingEvent damagingEvent = ProcessDamagingEventCells(columnIndexes, row,
                        damagingEventHashMap, probabilityOfOccurrence, damageSeverity,
                        probabilityOfOccurrenceWithTom, damageSeverityWithTom);

                try {
                    DatabaseUtil.insertDamagingEvent(dsl, damagingEvent);
                } catch (DataAccessException e) {
                    errorLines.add("DamagingEvent " + damagingEventId + " in line " + (rowNr + 1)
                            + " can not be saved to the Db, Check duplicates");
                }

                try {
                    ProcessUsecaseCells(columnIndexes, row, usecaseNameId);
                } catch (DataAccessException e) {
                    errorLines.add("Usecase in line " + (rowNr + 1) + " can not be mapped to the damaging event " +
                            "or risk source");
                    continue;
                }

                try {
                    ProcessTomCells(columnIndexes, row);
                } catch (DataAccessException e) {
                    errorLines.add("Tom in line " + (rowNr + 1) + " can not be mapped to the damaging event "
                            + damagingEventId);
                    continue;
                }

                try {
                    DatabaseUtil.insertDamagingEventRiskSource(dsl, riskSourceId, damagingEventId);
                } catch (DataAccessException e) {
                    errorLines.add("RiskSource in line " + (rowNr + 1) + " can not be mapped to DamagingEvent "
                            + damagingEventId);
                }


            }
        }
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        System.out.println("Time of Process Total Sheet is : " + duration);
    }

    public static FileInputStream fileToInputStream(File file) throws FileNotFoundException {
        return new FileInputStream(file);
    }

    public static void readFile(InputStream file) {
        DSLContext dsl = DatabaseConfiguration.get().getDsl();
        /*  delete all entries of all tables to write a new entries */
        DatabaseUtil.truncateAllTables(dsl);

        HashMap<String, String> damagingEvent = null;
        HashMap<String, String> probabilityOfOccurrence = null;
        HashMap<String, String> damageSeverity = null;
        HashMap<String, String> probabilityOfOccurrenceWithTom = null;
        HashMap<String, String> damageSeverityWithTom = null;

        XSSFWorkbook workbook = checkFileStructure(file);
        if (workbook != null){
            for (Sheet sheet : workbook) {
                String actualSheetName = sheet.getSheetName();

                if (actualSheetName.equals(damagingEventSheetName)) {
                    damagingEvent = sheetToHashMap(sheet, 0);
                }
                else if (actualSheetName.equals(probabilityOfOccurrenceSheetName)) {
                    probabilityOfOccurrence = sheetToHashMap(sheet, 0);
                }
                else if (actualSheetName.equals(damageSeveritySheetName)) {
                    damageSeverity = sheetToHashMap(sheet, 0);
                }
                else if (isRiskSourceSheet(actualSheetName)) {
                    String riskSourceCategoryDescription = getRiskSourceCategoryFromSheetName(
                        actualSheetName);
                    DatabaseUtil.insertRiskSourceCategory(dsl, riskSourceCategoryDescription);
                    ProcessRiskSourcesSheet(sheet, riskSourceCategoryDescription);
                }
                else if (actualSheetName.equals(TomsSheetName)) {
                    ProcessTomsSheet(sheet);
                }
                else if (actualSheetName.equals(probabilityOfOccurrenceWithTomSheetName)) {
                    probabilityOfOccurrenceWithTom = sheetToHashMap(sheet,
                        0);
                }
                else if (actualSheetName.equals(damageSeverityWithTomSheetName)) {
                    damageSeverityWithTom = sheetToHashMap(sheet, 0);
                }
            }
            if(workbook.getSheet(totalSheetName) != null){
                ProcessTotalSheet(workbook, damagingEvent, probabilityOfOccurrence, damageSeverity,
                    probabilityOfOccurrenceWithTom, damageSeverityWithTom);
            }
            else {
                errorLines.add(totalSheetName + " sheet could not be found in your excel file");
                response.setStatus("ERROR");
                response.setComments(errorLines);
            }
        }
        // if errors are found, then save them to log file
        if (!errorLines.isEmpty()) {
            response.setStatus("ERROR");
            response.setComments(errorLines);
            // if any errors are occurs then delete all values in the tables
            DatabaseUtil.truncateAllTables(dsl);
            // no errors found
        } else {
            response.setStatus("DONE");
            errorLines.add("No Problems are detected by importing");
            response.setComments(errorLines);
        }
    }
}
