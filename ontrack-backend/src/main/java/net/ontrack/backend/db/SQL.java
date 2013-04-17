package net.ontrack.backend.db;

public interface SQL {

    // Project groups

    String PROJECT_GROUP_LIST = "SELECT ID, NAME, DESCRIPTION FROM PROJECT_GROUP ORDER BY NAME";

    String PROJECT_GROUP_CREATE = "INSERT INTO PROJECT_GROUP (NAME, DESCRIPTION) VALUES (:name, :description)";

    // Projects

    String PROJECT = "SELECT * FROM PROJECT WHERE ID = :id";

    String PROJECT_LIST = "SELECT ID, NAME, DESCRIPTION FROM PROJECT ORDER BY NAME";

    String PROJECT_CREATE = "INSERT INTO PROJECT (NAME, DESCRIPTION) VALUES (:name, :description)";

    String PROJECT_DELETE = "DELETE FROM PROJECT WHERE ID = :id";

    String PROJECT_UPDATE = "UPDATE PROJECT SET NAME = :name, DESCRIPTION = :description WHERE ID = :id";

    // Branches

    String BRANCH = "SELECT * FROM BRANCH WHERE ID = :id";

    String BRANCH_LIST = "SELECT ID, PROJECT, NAME, DESCRIPTION FROM BRANCH WHERE PROJECT = :project ORDER BY NAME";

    String BRANCH_CREATE = "INSERT INTO BRANCH (PROJECT, NAME, DESCRIPTION) VALUES (:project, :name, :description)";

    String BRANCH_DELETE = "DELETE FROM BRANCH WHERE ID = :id";

    String BRANCH_UPDATE = "UPDATE BRANCH SET NAME = :name, DESCRIPTION = :description WHERE ID = :id";

    // Builds

    String BUILD = "SELECT * FROM BUILD WHERE ID = :id";

    String BUILD_LAST_BY_BRANCH = "SELECT * FROM BUILD WHERE BRANCH = :branch ORDER BY ID DESC LIMIT 1";

    String BUILD_LIST = "SELECT * FROM BUILD WHERE BRANCH = :branch ORDER BY ID DESC LIMIT :count OFFSET :offset";

    String BUILD_CREATE = "INSERT INTO BUILD (BRANCH, NAME, DESCRIPTION) VALUES (:branch, :name, :description)";

    // Validation stamps

    long VALIDATION_STAMP_IMAGE_MAXSIZE = 4096;

    String VALIDATION_STAMP = "SELECT ID, BRANCH, NAME, DESCRIPTION, PROMOTION_LEVEL FROM VALIDATION_STAMP WHERE ID = :id";

    String VALIDATION_STAMP_BY_BRANCH_AND_NAME = "SELECT ID, BRANCH, NAME, DESCRIPTION, PROMOTION_LEVEL FROM VALIDATION_STAMP WHERE BRANCH = :branch AND NAME = :name";

    String VALIDATION_STAMP_LIST = "SELECT ID, BRANCH, NAME, DESCRIPTION, PROMOTION_LEVEL FROM VALIDATION_STAMP WHERE BRANCH = :branch ORDER BY NAME";

    String VALIDATION_STAMP_CREATE = "INSERT INTO VALIDATION_STAMP (BRANCH, NAME, DESCRIPTION) VALUES (:branch, :name, :description)";

    String VALIDATION_STAMP_DELETE = "DELETE FROM VALIDATION_STAMP WHERE ID = :id";

    String VALIDATIONSTAMP_IMAGE_UPDATE = "UPDATE VALIDATION_STAMP SET IMAGE = :image WHERE ID = :id";

    String VALIDATIONSTAMP_IMAGE = "SELECT IMAGE FROM VALIDATION_STAMP WHERE ID = :id";

    String VALIDATION_STAMP_PROMOTION_LEVEL = "UPDATE VALIDATION_STAMP SET PROMOTION_LEVEL = :promotionLevel WHERE ID = :id";

    String VALIDATION_STAMP_FOR_PROMOTION_LEVEL = "SELECT ID, BRANCH, NAME, DESCRIPTION, PROMOTION_LEVEL FROM VALIDATION_STAMP WHERE PROMOTION_LEVEL = :promotionLevel ORDER BY NAME";

    String VALIDATION_STAMP_WITHOUT_PROMOTION_LEVEL = "SELECT ID, BRANCH, NAME, DESCRIPTION, PROMOTION_LEVEL FROM VALIDATION_STAMP WHERE BRANCH = :branch AND PROMOTION_LEVEL IS NULL ORDER BY NAME";

    String VALIDATION_STAMP_UPDATE = "UPDATE VALIDATION_STAMP SET NAME = :name, DESCRIPTION = :description WHERE ID = :id";

    // Promotion levels

    long PROMOTION_LEVEL_IMAGE_MAXSIZE = 4096;

    String PROMOTION_LEVEL = "SELECT ID, BRANCH, NAME, DESCRIPTION, LEVELNB FROM PROMOTION_LEVEL WHERE ID = :id";

    String PROMOTION_LEVEL_LIST = "SELECT ID, BRANCH, NAME, DESCRIPTION, LEVELNB FROM PROMOTION_LEVEL WHERE BRANCH = :branch ORDER BY LEVELNB DESC";

    String PROMOTION_LEVEL_COUNT = "SELECT COUNT(*) FROM PROMOTION_LEVEL WHERE BRANCH = :branch";

    String PROMOTION_LEVEL_CREATE = "INSERT INTO PROMOTION_LEVEL (BRANCH, NAME, DESCRIPTION, LEVELNB) VALUES (:branch, :name, :description, :levelNb)";

    String PROMOTION_LEVEL_IMAGE_UPDATE = "UPDATE PROMOTION_LEVEL SET IMAGE = :image WHERE ID = :id";

    String PROMOTION_LEVEL_IMAGE = "SELECT IMAGE FROM PROMOTION_LEVEL WHERE ID = :id";

    String PROMOTION_LEVEL_HIGHER = "SELECT ID FROM PROMOTION_LEVEL WHERE BRANCH = :branch AND LEVELNB > :levelNb ORDER BY LEVELNB ASC LIMIT 1";

    String PROMOTION_LEVEL_LOWER = "SELECT ID FROM PROMOTION_LEVEL WHERE BRANCH = :branch AND LEVELNB < :levelNb ORDER BY LEVELNB DESC LIMIT 1";

    String PROMOTION_LEVEL_LEVELNB = "SELECT LEVELNB FROM PROMOTION_LEVEL WHERE ID = :id";

    String PROMOTION_LEVEL_SET_LEVELNB = "UPDATE PROMOTION_LEVEL SET LEVELNB = :levelNb WHERE ID = :id";

    String PROMOTION_LEVEL_DELETE = "DELETE FROM PROMOTION_LEVEL WHERE ID = :id";

    String PROMOTION_LEVEL_UPDATE_LEVEL_NB_AFTER_DELETE = "UPDATE PROMOTION_LEVEL SET LEVELNB = LEVELNB - 1 WHERE LEVELNB > :levelNb";

    // Validation runs

    String VALIDATION_RUN = "SELECT R.* FROM VALIDATION_RUN R WHERE R.ID = :id";

    String VALIDATION_RUN_CREATE = "INSERT INTO VALIDATION_RUN (BUILD, VALIDATION_STAMP, DESCRIPTION, RUN_ORDER) VALUES (:build, :validationStamp, :description, :runOrder)";

    String VALIDATION_RUN_FOR_BUILD_AND_STAMP = "SELECT * FROM VALIDATION_RUN WHERE BUILD = :build AND VALIDATION_STAMP = :validationStamp ORDER BY ID DESC";

    String VALIDATION_RUN_LAST_FOR_BUILD_AND_STAMP = "SELECT * FROM VALIDATION_RUN WHERE BUILD = :build AND VALIDATION_STAMP = :validationStamp ORDER BY ID DESC LIMIT 1";

    String VALIDATION_RUN_COUNT_FOR_BUILD_AND_STAMP = "SELECT COUNT(*) FROM VALIDATION_RUN WHERE BUILD = :build AND VALIDATION_STAMP = :validationStamp";

    String VALIDATION_RUN_HISTORY = "(SELECT VR.ID AS VRID, VR.RUN_ORDER AS RUN_ORDER, B.ID AS BUILD, NULL AS STATUS, CONTENT, AUTHOR, AUTHOR_ID, COMMENT_TIMESTAMP AS EVENT_TIMESTAMP\n" +
            "FROM COMMENT C\n" +
            "INNER JOIN VALIDATION_RUN VR ON VR.ID = C.VALIDATION_RUN\n" +
            "INNER JOIN BUILD B ON B.ID = VR.BUILD\n" +
            "WHERE B.BRANCH = :branch AND VR.VALIDATION_STAMP = :validationStamp AND VR.ID <= :validationRun)\n" +
            "UNION\n" +
            "(SELECT VR.ID, VR.RUN_ORDER AS RUN_ORDER, B.ID AS BUILD, VRS.STATUS, VRS.DESCRIPTION AS CONTENT, VRS.AUTHOR, VRS.AUTHOR_ID, VRS.STATUS_TIMESTAMP AS EVENT_TIMESTAMP\n" +
            "FROM VALIDATION_RUN_STATUS VRS\n" +
            "INNER JOIN VALIDATION_RUN VR ON VR.ID = VRS.VALIDATION_RUN\n" +
            "INNER JOIN BUILD B ON B.ID = VR.BUILD\n" +
            "WHERE B.BRANCH = :branch AND VR.VALIDATION_STAMP = :validationStamp AND VR.ID <= :validationRun)\n" +
            "ORDER BY BUILD DESC, RUN_ORDER DESC, EVENT_TIMESTAMP DESC\n" +
            "LIMIT :count OFFSET :offset";

    // Validation run statuses

    String VALIDATION_RUN_STATUS_CREATE = "INSERT INTO VALIDATION_RUN_STATUS (VALIDATION_RUN, STATUS, DESCRIPTION, AUTHOR, AUTHOR_ID, STATUS_TIMESTAMP) VALUES (:validationRun, :status, :description, :author, :authorId, :statusTimestamp)";

    String VALIDATION_RUN_STATUS_LAST = "SELECT * FROM VALIDATION_RUN_STATUS WHERE VALIDATION_RUN = :id ORDER BY ID DESC LIMIT 1";

    // Promoted runs

    String PROMOTED_RUN_CREATE = "INSERT INTO PROMOTED_RUN (PROMOTION_LEVEL, BUILD, DESCRIPTION) VALUES (:promotionLevel, :build, :description)";

    String PROMOTED_RUN = "SELECT ID, PROMOTION_LEVEL, BUILD, DESCRIPTION FROM PROMOTED_RUN WHERE BUILD = :build AND PROMOTION_LEVEL = :promotionLevel";

    String PROMOTION_LEVEL_FOR_BUILD = "SELECT L.* FROM PROMOTION_LEVEL L, PROMOTED_RUN R WHERE R.PROMOTION_LEVEL = L.ID AND R.BUILD = :build ORDER BY L.LEVELNB";

    // Audit

    String ENTITY_NAME = "SELECT %s FROM %s WHERE ID = :id";

    String EVENT_VALUE_INSERT = "INSERT INTO EVENT_VALUES (EVENT, PROP_NAME, PROP_VALUE) VALUES (:id, :name, :value)";

    String EVENT_VALUE_LIST = "SELECT PROP_NAME, PROP_VALUE FROM EVENT_VALUES WHERE EVENT = :id";

    String EVENT = "SELECT * FROM EVENTS WHERE ID = :id";

    // Accounts

    String ACCOUNT_AUTHENTICATE = "SELECT ID, NAME, FULLNAME, EMAIL, ROLENAME, MODE FROM ACCOUNTS WHERE MODE = 'builtin' AND NAME = :user AND PASSWORD = :password";

    String ACCOUNT_ROLE = "SELECT ROLENAME FROM ACCOUNTS WHERE MODE = :mode AND NAME = :user";

    String ACCOUNT_BY_NAME = "SELECT ID, NAME, FULLNAME, EMAIL, ROLENAME, MODE FROM ACCOUNTS WHERE MODE = :mode AND NAME = :user";

    String ACCOUNT = "SELECT * FROM ACCOUNTS WHERE ID = :id";

    String ACCOUNT_LIST = "SELECT ID, NAME, FULLNAME, EMAIL, ROLENAME, MODE FROM ACCOUNTS ORDER BY NAME";

    String ACCOUNT_CREATE = "INSERT INTO ACCOUNTS (NAME, FULLNAME, EMAIL, ROLENAME, MODE, PASSWORD) VALUES (:name, :fullName, :email, :roleName, :mode, :password)";

    // Configuration

    String CONFIGURATION_GET = "SELECT VALUE FROM CONFIGURATION WHERE NAME = :name";

    String CONFIGURATION_DELETE = "DELETE FROM CONFIGURATION WHERE NAME = :name";

    String CONFIGURATION_INSERT = "INSERT INTO CONFIGURATION (NAME, VALUE) VALUES (:name, :value)";

    // Comments

    String COMMENT_CREATE = "INSERT INTO COMMENT (%s, CONTENT, AUTHOR, AUTHOR_ID, COMMENT_TIMESTAMP) VALUES (:id, :content, :author, :author_id, :comment_timestamp)";

    // Properties

    String PROPERTY_DELETE = "DELETE FROM PROPERTIES WHERE %s = :entityId AND EXTENSION = :extension AND NAME = :name";

    String PROPERTY_INSERT = "INSERT INTO PROPERTIES (EXTENSION, NAME, VALUE, %s) VALUES (:extension, :name, :value, :entityId)";

    String PROPERTY_ALL = "SELECT * FROM PROPERTIES WHERE %s = :entityId ORDER BY EXTENSION, NAME";

    String PROPERTY_VALUE = "SELECT * FROM PROPERTIES WHERE %s = :entityId AND EXTENSION = :extension AND NAME = :name";

    // Subscriptions

    String SUBSCRIPTION_DELETE = "DELETE FROM SUBSCRIPTION WHERE ACCOUNT = :account AND %s = :entityId";
    String SUBSCRIPTION_CREATE = "INSERT INTO SUBSCRIPTION (ACCOUNT, %s) VALUES (:account, :entityId)";
    String SUBSCRIPTION_BY_ACCOUNT = "SELECT * FROM SUBSCRIPTION WHERE ACCOUNT = :account";
}
