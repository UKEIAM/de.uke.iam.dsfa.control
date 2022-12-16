package de.uke.iam.dsfa.control.util;

import de.uke.iam.dsfa.control.db.DatabaseConfiguration;
import de.uke.iam.dsfa.control.db.DatabaseUtil;
import de.uke.iam.dsfa.control.db.jooq.tables.pojos.DamagingEvent;
import de.uke.iam.dsfa.control.db.jooq.tables.pojos.RiskSource;
import org.apache.poi.xwpf.usermodel.Borders;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record4;
import org.jooq.Result;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static de.uke.iam.dsfa.control.db.jooq.Tables.USE_CASE_DAMAGING_EVENT;

public class WordWriter {
    public static String saveFile (XWPFDocument document, String fileName, String filePath) throws IOException {
        File file = new File(filePath + "/" + fileName);
        file.delete();
        file.createNewFile();
        document.write(new FileOutputStream(file));

        document.close();

        return filePath + "/" + fileName;
    }

    private static void damagingEventFooterWithPageBreak (XWPFDocument document, String sectionDescription ) {
        /*  create the paragraph after the last damageEvent in the list */
        createParagraphText(document, "Aus der Folgenabschätzung zur "+ sectionDescription +" ergibt sich, dass technische und organisatorische Sicherungsmaßnahmen zur Reduzierung des Risikos ergriffen werden müssen.");
        /*  start with a new page if all risk sources are written */
        XWPFParagraph paragraphSeperator = document.createParagraph();
        paragraphSeperator.setPageBreak(true);
    }

    private static void damagingEventTemplate (XWPFDocument document, DSLContext dsl, String damagingEventID) {

        DamagingEvent damagingEvent = DatabaseUtil.selectDamageEventByID(dsl, damagingEventID);
        createParagraphDamageEvent(document, damagingEvent.getId());

        RiskSource riskSource = DatabaseUtil.selectRiskSourceByDamageEventID(dsl, damagingEvent.getId());
        createParagraphRiskSource(document, riskSource.getId());
        createParagraphRiskSourceText(document, riskSource.getDescription());
        createParagraphHeadline(document, "Folge:", 11);

        createParagraphText(document, damagingEvent.getDescription());

        createParagraphRatingMatrix(document);

        createParagraphProbabilityOfOccurrence(document, String.valueOf(damagingEvent.getProbabilityOfOccurrenceValue()), damagingEvent.getProbabilityOfOccurrenceDescription());
        createParagraphDamageSeverity(document, String.valueOf(damagingEvent.getDamageSeverityValue()), damagingEvent.getDamageSeverityDescription());
        createParagraphRiskRating(document, String.valueOf(damagingEvent.getRiskRatingValue()));

        XWPFParagraph paragraphSeperator = document.createParagraph();
        paragraphSeperator.setBorderBottom(Borders.SINGLE);
    }

    private static void tomTemplate (XWPFDocument document, DSLContext dsl, String damagingEventID) {

        DamagingEvent damagingEvent = DatabaseUtil.selectDamageEventByID(dsl, damagingEventID);
        createParagraphDamageEvent(document, damagingEvent.getId());

        RiskSource riskSource = DatabaseUtil.selectRiskSourceByDamageEventID(dsl, damagingEvent.getId());
        createParagraphRiskSource(document, riskSource.getId());
        createParagraphRiskSourceText(document, riskSource.getDescription());

        createParagraphText(document, "Zur Eindämmungen werden folgende technische und organisatorische Maßnahmen festgelegt:");

        List<String> listOfTomIDs = DatabaseUtil.selectAllTomIDByDamagingEventID(dsl, damagingEvent.getId());

        for (String listOfTomID : listOfTomIDs) {
            if (listOfTomID != null) {
                HashMap<String,String> tomDescription = DatabaseUtil.selectTomByID(dsl, listOfTomID);
                createParagraphText(document, tomDescription.get("TomDescription"));
            }
        }

        createParagraphRatingMatrixTOM(document);

        createParagraphProbabilityOfOccurrenceWithTOM(document, String.valueOf(damagingEvent.getProbabilityOfOccurrenceWithTomValue()), damagingEvent.getProbabilityOfOccurrenceWithTomDescription());
        createParagraphDamageSeverityWithTOM(document, String.valueOf(damagingEvent.getDamageSeverityWithTomValue()), damagingEvent.getDamageSeverityWithTomDescription());
        createParagraphResidualRiskRating(document, String.valueOf(damagingEvent.getResidualRiskRatingValue()));

        XWPFParagraph paragraphSeparator = document.createParagraph();
        paragraphSeparator.setBorderBottom(Borders.SINGLE);
    }


    private static void createParagraphDamageEvent (XWPFDocument document) {
        XWPFParagraph paragraphDamageEvent = document.createParagraph();
        XWPFRun runDamageEvent = paragraphDamageEvent.createRun();

        runDamageEvent.setBold(true);
        runDamageEvent.setFontSize(11);
        runDamageEvent.setText("Schadensereignis:");
    }

    private static void createParagraphDamageEvent (XWPFDocument document, String damagingEventID) {
        XWPFParagraph paragraphDamageEvent = document.createParagraph();
        XWPFRun runDamageEvent = paragraphDamageEvent.createRun();

        runDamageEvent.setBold(true);
        runDamageEvent.setFontSize(11);
        runDamageEvent.setText("Schadensereignis: ");
        runDamageEvent.setText(damagingEventID);
    }

    private static void createParagraphRiskSource (XWPFDocument document) {
        XWPFParagraph paragraphRiskSource = document.createParagraph();
        XWPFRun runRiskSource = paragraphRiskSource.createRun();

        runRiskSource.setBold(true);
        runRiskSource.setFontSize(11);
        runRiskSource.setText("Risikoquelle:");
    }

    private static void createParagraphRiskSource (XWPFDocument document, String riskSourceID) {
        XWPFParagraph paragraphRiskSource = document.createParagraph();
        XWPFRun runRiskSource = paragraphRiskSource.createRun();

        runRiskSource.setBold(true);
        runRiskSource.setFontSize(11);
        runRiskSource.setText("Risikoquelle: ");
        runRiskSource.setText(riskSourceID);
    }

    private static void createParagraphRiskSourceText (XWPFDocument document, String riskSourceText) {
        XWPFParagraph paragraphRiskSource = document.createParagraph();
        XWPFRun runRiskSource = paragraphRiskSource.createRun();

        runRiskSource.setFontSize(11);
        runRiskSource.setText(riskSourceText);
    }

    private static void createParagraphHeadline (XWPFDocument document, String text, int fontSize) {
        XWPFParagraph paragraphHeadline = document.createParagraph();
        XWPFRun runHeadline = paragraphHeadline.createRun();

        runHeadline.setBold(true);
        runHeadline.setFontSize(fontSize);
        runHeadline.setText(text);
    }

    private static void createParagraphText (XWPFDocument document, String text) {
        XWPFParagraph paragraphText = document.createParagraph();
        XWPFRun runText = paragraphText.createRun();

        runText.setFontSize(11);
        runText.setText(text);
    }

    private static void createParagraphRatingMatrix (XWPFDocument document) {
        XWPFParagraph paragraphRatingMatrix = document.createParagraph();
        XWPFRun runRatingMatrix = paragraphRatingMatrix.createRun();

        runRatingMatrix.setBold(true);
        runRatingMatrix.setFontSize(11);
        runRatingMatrix.setText("Bewertungsmatrix:");
    }

    private static void createParagraphProbabilityOfOccurrence (XWPFDocument document, String value, String text) {
        XWPFParagraph paragraphRatingMatrix = document.createParagraph();
        XWPFRun runRatingMatrix = paragraphRatingMatrix.createRun();

        runRatingMatrix.setFontSize(11);
        runRatingMatrix.setText("Eintrittswahrscheinlichkeit: ");
        runRatingMatrix.setText(value + " - ");
        runRatingMatrix.setText(text);
    }

    private static void createParagraphDamageSeverity (XWPFDocument document, String value, String text) {
        XWPFParagraph paragraphRatingMatrix = document.createParagraph();
        XWPFRun runRatingMatrix = paragraphRatingMatrix.createRun();

        runRatingMatrix.setFontSize(11);
        runRatingMatrix.setText("Schwere des Schadens: ");
        runRatingMatrix.setText(value + " - ");
        runRatingMatrix.setText(text);
    }

    private static void createParagraphRiskRating (XWPFDocument document, String value) {
        XWPFParagraph paragraphRatingMatrix = document.createParagraph();
        XWPFRun runRatingMatrix = paragraphRatingMatrix.createRun();

        runRatingMatrix.setFontSize(11);
        runRatingMatrix.setText("Risikoabstufung: ");
        runRatingMatrix.setText(value);
    }

    private static void createParagraphTOM (XWPFDocument document) {
        XWPFParagraph paragraphTOM = document.createParagraph();
        XWPFRun runTOM = paragraphTOM.createRun();

        runTOM.setBold(true);
        runTOM.setFontSize(11);
        runTOM.setText("TOM:");
    }

    private static void createParagraphRatingMatrixTOM (XWPFDocument document) {
        XWPFParagraph paragraphRatingMatrixTOM = document.createParagraph();
        XWPFRun runRatingMatrixTOM = paragraphRatingMatrixTOM.createRun();

        runRatingMatrixTOM.setBold(true);
        runRatingMatrixTOM.setFontSize(11);
        runRatingMatrixTOM.setText("Bewertungsmatrix mit TOM:");
    }

    private static void createParagraphProbabilityOfOccurrenceWithTOM (XWPFDocument document, String value, String text) {
        XWPFParagraph paragraphRatingMatrix = document.createParagraph();
        XWPFRun runRatingMatrix = paragraphRatingMatrix.createRun();

        runRatingMatrix.setFontSize(11);
        runRatingMatrix.setText("Eintrittswahrscheinlichkeit: ");
        runRatingMatrix.setText(value + " - ");
        runRatingMatrix.setText(text);
    }

    private static void createParagraphDamageSeverityWithTOM (XWPFDocument document, String value, String text) {
        XWPFParagraph paragraphRatingMatrix = document.createParagraph();
        XWPFRun runRatingMatrix = paragraphRatingMatrix.createRun();

        runRatingMatrix.setFontSize(11);
        runRatingMatrix.setText("Schwere des Schadens: ");
        runRatingMatrix.setText(value + " - ");
        runRatingMatrix.setText(text);
    }

    private static void createParagraphResidualRiskRating (XWPFDocument document, String value) {
        XWPFParagraph paragraphRatingMatrix = document.createParagraph();
        XWPFRun runRatingMatrix = paragraphRatingMatrix.createRun();

        runRatingMatrix.setFontSize(11);
        runRatingMatrix.setText("Risikoabstufung: ");
        runRatingMatrix.setText(value);
    }

    public static XWPFDocument getWord (List<Integer> useCaseID) throws IOException {
        XWPFDocument document = new XWPFDocument();

        DSLContext dsl = DatabaseConfiguration.get().getDsl();

        Result<Record4<String, Integer, String, String>> listOfDamagingEventsWithVtr = DatabaseUtil.selectDamaingEventsByUseCaseIDs(dsl, useCaseID, "Vtr%");
        for (Record damagingEvent : listOfDamagingEventsWithVtr) {
            damagingEventTemplate(document, dsl, damagingEvent.get(USE_CASE_DAMAGING_EVENT.DAMAGING_EVENT_ID));
        }
        damagingEventFooterWithPageBreak(document, "Vertraulichkeit");

        Result<Record4<String, Integer, String, String>> listOfDamagingEventsWithMini = DatabaseUtil.selectDamaingEventsByUseCaseIDs(dsl, useCaseID, "Data-Mini%");
        for (Record damagingEvent : listOfDamagingEventsWithMini) {
            damagingEventTemplate(document, dsl, damagingEvent.get(USE_CASE_DAMAGING_EVENT.DAMAGING_EVENT_ID));
        }
        damagingEventFooterWithPageBreak(document, "Datenminimierung");

        Result<Record4<String, Integer, String, String>> listOfDamagingEventsWithVerfüg = DatabaseUtil.selectDamaingEventsByUseCaseIDs(dsl, useCaseID, "Verfüg%");
        for (Record damagingEvent : listOfDamagingEventsWithVerfüg) {
            damagingEventTemplate(document, dsl, damagingEvent.get(USE_CASE_DAMAGING_EVENT.DAMAGING_EVENT_ID));
        }
        damagingEventFooterWithPageBreak(document, "Verfügbarkeit");

        Result<Record4<String, Integer, String, String>> listOfDamagingEventsWithInteg = DatabaseUtil.selectDamaingEventsByUseCaseIDs(dsl, useCaseID, "Integ%");
        for (Record damagingEvent : listOfDamagingEventsWithInteg) {
            damagingEventTemplate(document, dsl, damagingEvent.get(USE_CASE_DAMAGING_EVENT.DAMAGING_EVENT_ID));
        }
        damagingEventFooterWithPageBreak(document, "Integrität");

        Result<Record4<String, Integer, String, String>> listOfDamagingEventsWithVerk = DatabaseUtil.selectDamaingEventsByUseCaseIDs(dsl, useCaseID, "Nicht-Verk%");
        for (Record damagingEvent : listOfDamagingEventsWithVerk) {
            damagingEventTemplate(document, dsl, damagingEvent.get(USE_CASE_DAMAGING_EVENT.DAMAGING_EVENT_ID));
        }
        damagingEventFooterWithPageBreak(document, "Nichtverkettung");

        Result<Record4<String, Integer, String, String>> listOfDamagingEventsWithTransp = DatabaseUtil.selectDamaingEventsByUseCaseIDs(dsl, useCaseID, "Transp%");
        for (Record damagingEvent : listOfDamagingEventsWithTransp) {
            damagingEventTemplate(document, dsl, damagingEvent.get(USE_CASE_DAMAGING_EVENT.DAMAGING_EVENT_ID));
        }
        damagingEventFooterWithPageBreak(document, "Transparenz");

        Result<Record4<String, Integer, String, String>> listOfDamagingEventsWithInterv = DatabaseUtil.selectDamaingEventsByUseCaseIDs(dsl, useCaseID, "Interv%");
        for (Record damagingEvent : listOfDamagingEventsWithInterv) {
            damagingEventTemplate(document, dsl, damagingEvent.get(USE_CASE_DAMAGING_EVENT.DAMAGING_EVENT_ID));
        }
        damagingEventFooterWithPageBreak(document, "Intervenierbarkeit");

        Result<Record4<String, Integer, String, String>> listOfDamagingEventsWithDiskr = DatabaseUtil.selectDamaingEventsByUseCaseIDs(dsl, useCaseID, "Diskr%");
        for (Record damagingEvent : listOfDamagingEventsWithDiskr) {
            damagingEventTemplate(document, dsl, damagingEvent.get(USE_CASE_DAMAGING_EVENT.DAMAGING_EVENT_ID));
        }
        damagingEventFooterWithPageBreak(document, "Diskriminierung");

        Result<Record4<String, Integer, String, String>> listOfDamagingEventsWithIdentD = DatabaseUtil.selectDamaingEventsByUseCaseIDs(dsl, useCaseID, "IdentD%");
        for (Record damagingEvent : listOfDamagingEventsWithIdentD) {
            damagingEventTemplate(document, dsl, damagingEvent.get(USE_CASE_DAMAGING_EVENT.DAMAGING_EVENT_ID));
        }
        damagingEventFooterWithPageBreak(document, "Identitätsdiebstahl");

        Result<Record4<String, Integer, String, String>> listOfDamagingEventsWithUnDePseud = DatabaseUtil.selectDamaingEventsByUseCaseIDs(dsl, useCaseID, "UnDePseud%");
        for (Record damagingEvent : listOfDamagingEventsWithUnDePseud) {
            damagingEventTemplate(document, dsl, damagingEvent.get(USE_CASE_DAMAGING_EVENT.DAMAGING_EVENT_ID));
        }
        damagingEventFooterWithPageBreak(document, "unberechtigten De-Pseudonymisierung");

        Result<Record4<String, Integer, String, String>> listOfDamagingEventsWithRecht = DatabaseUtil.selectDamaingEventsByUseCaseIDs(dsl, useCaseID, "Recht%");
        for (Record damagingEvent : listOfDamagingEventsWithRecht) {
            damagingEventTemplate(document, dsl, damagingEvent.get(USE_CASE_DAMAGING_EVENT.DAMAGING_EVENT_ID));
        }
        damagingEventFooterWithPageBreak(document, "YYYYY");


        Result<Record4<String, Integer, String, String>> listOfTomsWithVtr = DatabaseUtil.selectDamaingEventsByUseCaseIDs(dsl, useCaseID, "Vtr%");
        for (Record damagingEvent : listOfTomsWithVtr) {
            tomTemplate(document, dsl, damagingEvent.get(USE_CASE_DAMAGING_EVENT.DAMAGING_EVENT_ID));
        }
        damagingEventFooterWithPageBreak(document, "Vertraulichkeit");

        Result<Record4<String, Integer, String, String>> listOfTomsWithMini = DatabaseUtil.selectDamaingEventsByUseCaseIDs(dsl, useCaseID, "Data-Mini%");
        for (Record damagingEvent : listOfTomsWithMini) {
            tomTemplate(document, dsl, damagingEvent.get(USE_CASE_DAMAGING_EVENT.DAMAGING_EVENT_ID));
        }
        damagingEventFooterWithPageBreak(document, "Datenminimierung");

        Result<Record4<String, Integer, String, String>> listOfTomsWithVerfüg = DatabaseUtil.selectDamaingEventsByUseCaseIDs(dsl, useCaseID, "Verfüg%");
        for (Record damagingEvent : listOfTomsWithVerfüg) {
            tomTemplate(document, dsl, damagingEvent.get(USE_CASE_DAMAGING_EVENT.DAMAGING_EVENT_ID));
        }
        damagingEventFooterWithPageBreak(document, "Verfügbarkeit");

        Result<Record4<String, Integer, String, String>> listOfTomsWithInteg = DatabaseUtil.selectDamaingEventsByUseCaseIDs(dsl, useCaseID, "Integ%");
        for (Record damagingEvent : listOfTomsWithInteg) {
            tomTemplate(document, dsl, damagingEvent.get(USE_CASE_DAMAGING_EVENT.DAMAGING_EVENT_ID));
        }
        damagingEventFooterWithPageBreak(document, "Integrität");

        Result<Record4<String, Integer, String, String>> listOfTomsWithVerk = DatabaseUtil.selectDamaingEventsByUseCaseIDs(dsl, useCaseID, "Nicht-Verk%");
        for (Record damagingEvent : listOfTomsWithVerk) {
            tomTemplate(document, dsl, damagingEvent.get(USE_CASE_DAMAGING_EVENT.DAMAGING_EVENT_ID));
        }
        damagingEventFooterWithPageBreak(document, "Nichtverkettung");

        Result<Record4<String, Integer, String, String>> listOfTomsWithTransp = DatabaseUtil.selectDamaingEventsByUseCaseIDs(dsl, useCaseID, "Transp%");
        for (Record damagingEvent : listOfTomsWithTransp) {
            tomTemplate(document, dsl, damagingEvent.get(USE_CASE_DAMAGING_EVENT.DAMAGING_EVENT_ID));
        }
        damagingEventFooterWithPageBreak(document, "Transparenz");

        Result<Record4<String, Integer, String, String>> listOfTomsWithInterv = DatabaseUtil.selectDamaingEventsByUseCaseIDs(dsl, useCaseID, "Interv%");
        for (Record damagingEvent : listOfTomsWithInterv) {
            tomTemplate(document, dsl, damagingEvent.get(USE_CASE_DAMAGING_EVENT.DAMAGING_EVENT_ID));
        }
        damagingEventFooterWithPageBreak(document, "Intervenierbarkeit");

        Result<Record4<String, Integer, String, String>> listOfTomsWithDiskr = DatabaseUtil.selectDamaingEventsByUseCaseIDs(dsl, useCaseID, "Diskr%");
        for (Record damagingEvent : listOfTomsWithDiskr) {
            tomTemplate(document, dsl, damagingEvent.get(USE_CASE_DAMAGING_EVENT.DAMAGING_EVENT_ID));
        }
        damagingEventFooterWithPageBreak(document, "Diskriminierung");

        Result<Record4<String, Integer, String, String>> listOfTomsWithIdentD = DatabaseUtil.selectDamaingEventsByUseCaseIDs(dsl, useCaseID, "IdentD%");
        for (Record damagingEvent : listOfTomsWithIdentD) {
            tomTemplate(document, dsl, damagingEvent.get(USE_CASE_DAMAGING_EVENT.DAMAGING_EVENT_ID));
        }
        damagingEventFooterWithPageBreak(document, "Identitätsdiebstahl");

        Result<Record4<String, Integer, String, String>> listOfTomsWithUnDePseud = DatabaseUtil.selectDamaingEventsByUseCaseIDs(dsl, useCaseID, "UnDePseud%");
        for (Record damagingEvent : listOfTomsWithUnDePseud) {
            tomTemplate(document, dsl, damagingEvent.get(USE_CASE_DAMAGING_EVENT.DAMAGING_EVENT_ID));
        }
        damagingEventFooterWithPageBreak(document, "unberechtigten De-Pseudonymisierung");

        Result<Record4<String, Integer, String, String>> listOfTomsWithRecht = DatabaseUtil.selectDamaingEventsByUseCaseIDs(dsl, useCaseID, "Recht%");
        for (Record damagingEvent : listOfTomsWithRecht) {
            tomTemplate(document, dsl, damagingEvent.get(USE_CASE_DAMAGING_EVENT.DAMAGING_EVENT_ID));
        }
        damagingEventFooterWithPageBreak(document, "YYYYY");

        return document;
    }
}
