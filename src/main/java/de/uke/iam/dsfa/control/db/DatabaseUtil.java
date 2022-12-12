package de.uke.iam.dsfa.control.db;

import static de.uke.iam.dsfa.control.db.jooq.Tables.DAMAGING_EVENT;
import static de.uke.iam.dsfa.control.db.jooq.Tables.DAMAGING_EVENT_RISK_SOURCE;
import static de.uke.iam.dsfa.control.db.jooq.Tables.DAMAGING_EVENT_TOM;
import static de.uke.iam.dsfa.control.db.jooq.Tables.RISK_SOURCE;
import static de.uke.iam.dsfa.control.db.jooq.Tables.RISK_SOURCE_CATEGORY;
import static de.uke.iam.dsfa.control.db.jooq.Tables.TOM;
import static de.uke.iam.dsfa.control.db.jooq.Tables.USE_CASE;
import static de.uke.iam.dsfa.control.db.jooq.Tables.USE_CASE_DAMAGING_EVENT;
import static de.uke.iam.dsfa.control.db.jooq.Tables.USE_CASE_RISK_SOURCE;

import de.uke.iam.dsfa.control.db.jooq.tables.Tom;
import de.uke.iam.dsfa.control.db.jooq.tables.pojos.DamagingEvent;
import de.uke.iam.dsfa.control.db.jooq.tables.pojos.RiskSource;
import de.uke.iam.dsfa.control.db.jooq.tables.pojos.RiskSourceCategory;
import de.uke.iam.dsfa.control.db.jooq.tables.pojos.UseCase;
import de.uke.iam.dsfa.control.db.jooq.tables.pojos.UseCaseRiskSource;
import de.uke.iam.dsfa.control.db.jooq.tables.records.DamagingEventRecord;
import de.uke.iam.dsfa.control.db.jooq.tables.records.RiskSourceRecord;
import de.uke.iam.dsfa.control.db.jooq.tables.records.TomRecord;
import de.uke.iam.dsfa.control.db.jooq.tables.records.UseCaseRecord;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Result;
import org.jooq.exception.DataAccessException;

public class DatabaseUtil {
  private static DatabaseUtil instance;

  /**
   * A private constructor, since we want no instances of this
   */
  private DatabaseUtil() {
  }

  public static void truncateAllTables(DSLContext dsl){
    dsl.truncate(DAMAGING_EVENT).cascade().execute();
    dsl.truncate(DAMAGING_EVENT_RISK_SOURCE).cascade().execute();
    dsl.truncate(DAMAGING_EVENT_TOM).cascade().execute();
    dsl.truncate(RISK_SOURCE).cascade().execute();
    dsl.truncate(RISK_SOURCE_CATEGORY).cascade().execute();
    dsl.truncate(TOM).cascade().execute();
    dsl.truncate(USE_CASE).cascade().execute();
    dsl.truncate(USE_CASE_RISK_SOURCE).cascade().execute();
  }


  public static void insertTom(DSLContext dsl, Tom tom) {

    TomRecord tomRecord = dsl.newRecord(TOM, tom);
    tomRecord.store();

  }

  public static boolean isRiskSourceCategory(DSLContext dsl,
      String risksourceCategoryDescription) {

    return dsl.fetchExists(dsl.selectFrom(RISK_SOURCE_CATEGORY)
        .where(RISK_SOURCE_CATEGORY.DESCRIPTION
            .eq(risksourceCategoryDescription)));

  }

  public static void insertRiskSourceCategory(DSLContext dsl, String risksourceCategoryDescription) {

    // check if this category is not already saved in the table
    if (!isRiskSourceCategory(dsl, risksourceCategoryDescription)) {

      dsl.insertInto(RISK_SOURCE_CATEGORY, RISK_SOURCE_CATEGORY.DESCRIPTION)
          .values(risksourceCategoryDescription)
          .onDuplicateKeyIgnore()
          .execute();

    }

  }

  // get the Id of a Risksource if exists or a null if not
  public static Integer getRiskSourceCategoryId(DSLContext dsl,
      String risksourceCategoryDescription) {

    Integer risksourceCategoryId = null;

    // check if the category are stored in the table
    try {

      risksourceCategoryId = dsl.select()
          .from(RISK_SOURCE_CATEGORY)
          .where(RISK_SOURCE_CATEGORY.DESCRIPTION
              .eq(risksourceCategoryDescription)).fetchOne()
          .getValue(RISK_SOURCE_CATEGORY.ID);
      return risksourceCategoryId;

      // Todo : check the catch
      // if the category not stored in the table, then catch the error and return null
    } catch (Exception e) {

      return risksourceCategoryId;

    }

  }

  public static void insertRiskSource(DSLContext dsl, RiskSource riskSource, String risksourceCategoryDescription) {

    Integer riskSourceCategoryID = null;
    try {
      riskSourceCategoryID = getRiskSourceCategoryId(dsl, risksourceCategoryDescription);
    }
    // If the riskSourceCategoryID is not exists in the DB
    catch (NullPointerException e) {

      insertRiskSourceCategory(dsl, risksourceCategoryDescription);
      riskSourceCategoryID = getRiskSourceCategoryId(dsl, risksourceCategoryDescription);

    } finally {

      riskSource.setRiskSourceCategoryId(riskSourceCategoryID);

    }

    RiskSourceRecord riskSourceRecord = dsl.newRecord(RISK_SOURCE, riskSource);
    riskSourceRecord.store();

  }


  public static void insertDamagingEvent(DSLContext dsl, DamagingEvent damagingEvent) {

    DamagingEventRecord damagingEventRecord = dsl.newRecord(DAMAGING_EVENT, damagingEvent);
    damagingEventRecord.store();

  }

  public static void insertUsecase(DSLContext dsl, UseCase usecase) {

    UseCaseRecord useCaseRecord = dsl.newRecord(USE_CASE, usecase);
    useCaseRecord.store();

  }

  public static void insertUsecaseRisksource(DSLContext dsl, Integer useCaseId,
      String riskSourceId) throws DataAccessException {
    dsl.insertInto(USE_CASE_RISK_SOURCE, USE_CASE_RISK_SOURCE.USE_CASE_ID,
            USE_CASE_RISK_SOURCE.RISK_SOURCE_ID)
        .values(useCaseId, riskSourceId)
        .onDuplicateKeyIgnore()
        .execute();
  }

  public static void insertDamagingEventRiskSource(DSLContext dsl, String riskSourceId,
      String damagingEventId) {
    dsl.insertInto(DAMAGING_EVENT_RISK_SOURCE, DAMAGING_EVENT_RISK_SOURCE.RISK_SOURCE_ID,
            DAMAGING_EVENT_RISK_SOURCE.DAMAGING_EVENT_ID)
        .values(riskSourceId, damagingEventId)
        .execute();
  }

  public static void insertDamagingEventTom(DSLContext dsl, String damagingEventId, String tomId) {
    dsl.insertInto(DAMAGING_EVENT_TOM, DAMAGING_EVENT_TOM.DAMAGING_EVENT_ID,
            DAMAGING_EVENT_TOM.TOM_ID)
        .values(damagingEventId, tomId)
        .execute();
  }

  public static void insertUsecaseDamagingEvent(DSLContext dsl, Integer useCaseId, String damagingEventId) {
    dsl.insertInto(USE_CASE_DAMAGING_EVENT, USE_CASE_DAMAGING_EVENT.DAMAGING_EVENT_ID,
            USE_CASE_DAMAGING_EVENT.USE_CASE_ID)
        .values(damagingEventId, useCaseId)
        .execute();
  }




  public static List<UseCase> selectAllUseCases(DSLContext dsl) {
    return dsl.select().from(USE_CASE).fetchInto(UseCase.class);
  }

  public static List<RiskSourceCategory> selectAllRiskSourceCategories(DSLContext dsl) {
    return dsl.select().from(RISK_SOURCE_CATEGORY).fetchInto(RiskSourceCategory.class);
  }

  /*public static List<String> selectRiskSourceIDsByUseCaseID(DSLContext dsl, int useCaseID) {
    Result<Record1<String>> records = dsl.select(USE_CASE_RISK_SOURCE.RISK_SOURCE_ID)
            .from(USE_CASE_RISK_SOURCE)
            .where(USE_CASE_RISK_SOURCE.USE_CASE_ID.eq(useCaseID))
            .fetch();

    List<String> listOfRiskSourcesByUseCase = new ArrayList<>();

    for (Record1<String> record : records) {
      listOfRiskSourcesByUseCase.add(record.get(USE_CASE_RISK_SOURCE.RISK_SOURCE_ID));
    }
    return listOfRiskSourcesByUseCase;
  }*/

  public static Result<Record> selectDamaingEventsByUseCaseIDs(DSLContext dsl, int useCaseID, String like) {
    Result<Record> listOfUseCaseRiskSource = dsl.select()
        .from(USE_CASE_DAMAGING_EVENT)
        .join(DAMAGING_EVENT_RISK_SOURCE).on(DAMAGING_EVENT_RISK_SOURCE.DAMAGING_EVENT_ID.eq(USE_CASE_DAMAGING_EVENT.DAMAGING_EVENT_ID))
        .join(RISK_SOURCE).on(RISK_SOURCE.ID.eq(DAMAGING_EVENT_RISK_SOURCE.RISK_SOURCE_ID))
        .join(RISK_SOURCE_CATEGORY).on(RISK_SOURCE_CATEGORY.ID.eq(RISK_SOURCE.RISK_SOURCE_CATEGORY_ID))
        .where(USE_CASE_DAMAGING_EVENT.USE_CASE_ID.eq(useCaseID).and(USE_CASE_DAMAGING_EVENT.DAMAGING_EVENT_ID.like(like)))
        .orderBy(RISK_SOURCE_CATEGORY.ID.asc(),RISK_SOURCE.ID.asc(), DAMAGING_EVENT_RISK_SOURCE.DAMAGING_EVENT_ID.asc())
        .fetch();

    return listOfUseCaseRiskSource;
  }

  public static List<UseCaseRiskSource> selectRiskSourceUseCasesByUseCaseIDs(DSLContext dsl, int useCaseID) {
    List<UseCaseRiskSource> listOfUseCaseRiskSource = dsl.select()
        .from(USE_CASE_RISK_SOURCE)
        .where(USE_CASE_RISK_SOURCE.USE_CASE_ID.eq(useCaseID))
        .fetchInto(UseCaseRiskSource.class);

    return listOfUseCaseRiskSource;
  }

  public static List<RiskSource> selectRiskSourcesByUseCaseID(DSLContext dsl, int useCaseID) {
    List<RiskSource> riskSources = dsl.select()
        .from(RISK_SOURCE)
        .join(USE_CASE_RISK_SOURCE).on(USE_CASE_RISK_SOURCE.RISK_SOURCE_ID.eq(RISK_SOURCE.ID))
        .where(USE_CASE_RISK_SOURCE.USE_CASE_ID.eq(useCaseID))
        .fetchInto(RiskSource.class);

    return riskSources;
  }

  public static Result<Record> selectRiskSourcesByUseCaseIDWithRiskSourceCategory(DSLContext dsl, int useCaseID, int riskSourceCategory) {
    Result<Record> riskSources = dsl.select()
        .from(RISK_SOURCE)
        .leftOuterJoin(USE_CASE_RISK_SOURCE).on(USE_CASE_RISK_SOURCE.RISK_SOURCE_ID.eq(RISK_SOURCE.ID))
        .leftOuterJoin(RISK_SOURCE_CATEGORY).on(RISK_SOURCE_CATEGORY.ID.eq(RISK_SOURCE.RISK_SOURCE_CATEGORY_ID))
        .where(USE_CASE_RISK_SOURCE.USE_CASE_ID.eq(useCaseID).and(RISK_SOURCE_CATEGORY.ID.eq(riskSourceCategory)))
        .fetch();

/*    HashMap<String, String> listOfRiskSources = new HashMap<>();

    for (Record riskSource : riskSources) {
      listOfRiskSources.put("RiskSourceID", riskSource.get(RISK_SOURCE.ID));
      listOfRiskSources.put("RiskSourceDescription", riskSource.get(RISK_SOURCE.DESCRIPTION));
      listOfRiskSources.put("RiskSourceUseCaseID", String.valueOf(riskSource.get(USE_CASE_RISK_SOURCE.USE_CASE_ID)));
      listOfRiskSources.put("RiskSourceCategoryID", String.valueOf(riskSource.get(RISK_SOURCE_CATEGORY.ID)));
    }*/

    return riskSources;
  }

  public static HashSet<RiskSource> selectRiskSourcesByUseCaseIDs(DSLContext dsl, List<Integer> useCaseIDs) {
    Result<Record> riskSources = dsl.select()
        .from(RISK_SOURCE)
        .join(USE_CASE_RISK_SOURCE).on(USE_CASE_RISK_SOURCE.RISK_SOURCE_ID.eq(RISK_SOURCE.ID))
        .fetch();

    HashSet<RiskSource> listOfRiskSources = new HashSet<>();

    for (Record riskSource : riskSources) {
      if (useCaseIDs.contains(riskSource.get(USE_CASE_RISK_SOURCE.USE_CASE_ID))) {

        RiskSource rs = new RiskSource();
        rs.setId(riskSource.get(RISK_SOURCE.ID));
        rs.setDescription(riskSource.get(RISK_SOURCE.DESCRIPTION));
        rs.setRiskSourceCategoryId(riskSource.get(RISK_SOURCE.RISK_SOURCE_CATEGORY_ID));

        listOfRiskSources.add(rs);
      }
    }

    return listOfRiskSources;
  }

  public static List<String> selectDamagingEventIDsByUseCaseID(DSLContext dsl, int useCaseID) {
    List<UseCaseRiskSource> useCaseRiskSources = selectRiskSourceUseCasesByUseCaseIDs(dsl, useCaseID);

    List<String> listOfDamagingEventsByUseCaseID = new ArrayList<>();

    for (UseCaseRiskSource useCaseRiskSource : useCaseRiskSources) {
      List<String> damagingEvents = selectDamagingEventIDsByRiskSourceID(dsl, useCaseRiskSource.getRiskSourceId());

      listOfDamagingEventsByUseCaseID.addAll(damagingEvents);
    }
    return listOfDamagingEventsByUseCaseID;
  }

  /*public static List<DamagingEvent> selectDamagingEventByUseCaseID(DSLContext dsl, int useCaseID) {
    List<String> riskSources = selectRiskSourceIDsByUseCaseID(dsl, useCaseID);

    List<DamagingEvent> listOfDamagingEventsByUseCaseID = new ArrayList<>();

    for (String riskSource : riskSources) {
      List<DamagingEvent> damagingEvents = selectDamagingEventsByRiskSourceID(dsl, riskSource);

      listOfDamagingEventsByUseCaseID.addAll(damagingEvents);
    }
    return listOfDamagingEventsByUseCaseID;
  }*/

  public static List<DamagingEvent> selectDamagingEventByUseCaseID(DSLContext dsl, int useCaseID) {
    List<UseCaseRiskSource> riskSourcesUseCaseIDs = selectRiskSourceUseCasesByUseCaseIDs(dsl, useCaseID);

    List<String> listOfRiskSourceIDs = new ArrayList<>();
    for (UseCaseRiskSource riskSourcesUseCaseID : riskSourcesUseCaseIDs) {
      listOfRiskSourceIDs.add(riskSourcesUseCaseID.getRiskSourceId());
    }
    List<DamagingEvent> damagingEvents = selectDamagingEventsByRiskSourceID(dsl, listOfRiskSourceIDs);

    return damagingEvents;
  }

  /*public static List<DamagingEvent> selectDamagingEventsByRiskSourceID(DSLContext dsl, String riskSourceID) {
    List<DamagingEvent> listOfDamagingEvents = dsl.select()
            .from(DAMAGING_EVENT)
            .join(DAMAGING_EVENT_RISK_SOURCE).on(DAMAGING_EVENT_RISK_SOURCE.DAMAGING_EVENT_ID.eq(DAMAGING_EVENT.ID))
            .where(DAMAGING_EVENT_RISK_SOURCE.RISK_SOURCE_ID.eq(riskSourceID))
            .orderBy(DAMAGING_EVENT.ID.asc())
            .fetchInto(DamagingEvent.class);

    return listOfDamagingEvents;
  }*/

  public static List<DamagingEvent> selectDamagingEventsByRiskSourceID(DSLContext dsl, List<String> riskSourceIDs) {
    List<DamagingEvent> listOfDamagingEvents = dsl.select()
        .from(DAMAGING_EVENT)
        .join(DAMAGING_EVENT_RISK_SOURCE).on(DAMAGING_EVENT_RISK_SOURCE.DAMAGING_EVENT_ID.eq(DAMAGING_EVENT.ID))
        .where(DAMAGING_EVENT_RISK_SOURCE.RISK_SOURCE_ID.in(riskSourceIDs))
        .orderBy(DAMAGING_EVENT.ID.asc())
        .fetchInto(DamagingEvent.class);

    return listOfDamagingEvents;
  }

  public static List<String> selectDamagingEventIDsByRiskSourceID(DSLContext dsl, String riskSourceID) {
    Result<Record1<String>> records = dsl.select(DAMAGING_EVENT_RISK_SOURCE.DAMAGING_EVENT_ID)
        .from(DAMAGING_EVENT_RISK_SOURCE)
        .where(DAMAGING_EVENT_RISK_SOURCE.RISK_SOURCE_ID.eq(riskSourceID))
        .fetch();

    List<String> listOfDamagingEventIDsByRiskSourceID = new ArrayList<>();

    for (Record1<String> record : records) {
      listOfDamagingEventIDsByRiskSourceID.add(record.get(DAMAGING_EVENT_RISK_SOURCE.DAMAGING_EVENT_ID));
    }
    return listOfDamagingEventIDsByRiskSourceID;
  }

  public static Integer countDamagingEventsByRiskSourceID(DSLContext dsl, String riskSourceID) {

    return dsl.selectCount().from(DAMAGING_EVENT_RISK_SOURCE)
        .where(DAMAGING_EVENT_RISK_SOURCE.RISK_SOURCE_ID.eq(riskSourceID))
        .fetchOne(0, int.class);
  }

  public static List<String> selectAllDamageEvents(DSLContext dsl) {
    Result<Record1<String>> records = dsl.select(DAMAGING_EVENT.ID).from(DAMAGING_EVENT).fetch();

    List<String> listOfDamageEventIDs = new ArrayList<>();

    for (Record1<String> record : records) {
      listOfDamageEventIDs.add(record.get(DAMAGING_EVENT.ID));
    }
    return listOfDamageEventIDs;
  }

  public static HashMap<String, String> selectRiskSourceIDByDamageEventID(DSLContext dsl, String damageEventID) {

    Result<Record2<String, String>> records = dsl.select(DAMAGING_EVENT_RISK_SOURCE.DAMAGING_EVENT_ID, DAMAGING_EVENT_RISK_SOURCE.RISK_SOURCE_ID).from(DAMAGING_EVENT_RISK_SOURCE).where(DAMAGING_EVENT_RISK_SOURCE.DAMAGING_EVENT_ID.eq(damageEventID)).fetch();

    HashMap<String, String> listOfRiskSources = new HashMap<>();

    for (Record2<String, String> record : records) {
      listOfRiskSources.put("DamageEventID", record.get(DAMAGING_EVENT_RISK_SOURCE.DAMAGING_EVENT_ID));
      listOfRiskSources.put("RiskSourceID", record.get(DAMAGING_EVENT_RISK_SOURCE.RISK_SOURCE_ID));
    }

    return listOfRiskSources;
  }

  public static RiskSource selectRiskSourceByDamageEventID(DSLContext dsl, String damageEventID) {
    HashMap<String, String> listOfRiskSource = DatabaseUtil.selectRiskSourceIDByDamageEventID(dsl, damageEventID);

    String riskSourceID = listOfRiskSource.get("RiskSourceID");

    return dsl.select().from(RISK_SOURCE).where(RISK_SOURCE.ID.eq(riskSourceID)).fetchOneInto(RiskSource.class);
  }

  public static DamagingEvent selectDamageEventByID(DSLContext dsl, String damageEventID) {
    return dsl.select().from(DAMAGING_EVENT).where(DAMAGING_EVENT.ID.eq(damageEventID)).fetchOneInto(DamagingEvent.class);
  }

  public static List<String> selectAllTomIDByDamagingEventID(DSLContext dsl, String damagingEventID) {
    Result<Record1<String>> records = dsl.select(DAMAGING_EVENT_TOM.TOM_ID).from(DAMAGING_EVENT_TOM).where(DAMAGING_EVENT_TOM.DAMAGING_EVENT_ID.eq(damagingEventID)).fetch();

    List<String> listOfDamageEventIDs = new ArrayList<>();

    for (Record1<String> record : records) {
      listOfDamageEventIDs.add(record.get(DAMAGING_EVENT_TOM.TOM_ID));
    }
    return listOfDamageEventIDs;
  }

  public static HashMap<String,String> selectTomByID(DSLContext dsl, String tomID) {
    Result<Record2<String, String>> tomDescription = dsl.select(TOM.ID, TOM.DESCRIPTION).from(TOM).where(TOM.ID.eq(tomID)).fetch();
    HashMap<String, String> listOfRiskSources = new HashMap<>();

    for (Record2<String, String> record : tomDescription) {
      listOfRiskSources.put("TomID", record.get(TOM.ID));
      listOfRiskSources.put("TomDescription", record.get(TOM.DESCRIPTION));
    }

    return listOfRiskSources;
  }
}
