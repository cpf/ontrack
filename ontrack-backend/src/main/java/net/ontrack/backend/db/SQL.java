package net.ontrack.backend.db;

public interface SQL {

    // Projects

    String PROJECT = "SELECT * FROM PROJECT WHERE ID = :id";
    String PROJECT_LIST = "SELECT ID, NAME, DESCRIPTION FROM PROJECT ORDER BY NAME";
    String PROJECT_CREATE = "INSERT INTO PROJECT (NAME, DESCRIPTION) VALUES (:name, :description)";
    String PROJECT_DELETE = "DELETE FROM PROJECT WHERE ID = :id";
    String PROJECT_UPDATE = "UPDATE PROJECT SET NAME = :name, DESCRIPTION = :description WHERE ID = :id";
    String PROJECT_BY_NAME = "SELECT * FROM PROJECT WHERE NAME = :name";

    // Branches
    String BRANCH = "SELECT * FROM BRANCH WHERE ID = :id";
    String BRANCH_LIST = "SELECT ID, PROJECT, NAME, DESCRIPTION FROM BRANCH WHERE PROJECT = :project ORDER BY NAME ASC";
    String BRANCH_CREATE = "INSERT INTO BRANCH (PROJECT, NAME, DESCRIPTION) VALUES (:project, :name, :description)";
    String BRANCH_DELETE = "DELETE FROM BRANCH WHERE ID = :id";
    String BRANCH_UPDATE = "UPDATE BRANCH SET NAME = :name, DESCRIPTION = :description WHERE ID = :id";
    String BRANCH_BY_NAME = "SELECT * FROM BRANCH WHERE NAME = :name";

    // Builds
    String BUILD = "SELECT * FROM BUILD WHERE ID = :id";
    String BUILD_LAST_BY_BRANCH = "SELECT * FROM BUILD WHERE BRANCH = :branch ORDER BY ID DESC LIMIT 1";
    String BUILD_LIST = "SELECT * FROM BUILD WHERE BRANCH = :branch ORDER BY ID DESC LIMIT :count OFFSET :offset";
    String BUILD_CREATE = "INSERT INTO BUILD (BRANCH, NAME, DESCRIPTION) VALUES (:branch, :name, :description)";
    String BUILD_BY_NAME = "SELECT * FROM BUILD WHERE NAME = :name";
    String BUILD_BY_BRANCH_AND_NAME = "SELECT ID FROM BUILD WHERE BRANCH = :branch AND NAME = :name";
    String BUILD_BY_BRANCH_AND_NUMERIC_NAME = "SELECT ID FROM (SELECT * FROM BUILD WHERE BRANCH = :branch AND NAME REGEXP '[0-9]+') WHERE  CONVERT(NAME,INT) >= CONVERT(:name,INT)  ORDER BY CONVERT(NAME,INT)";
    String BUILD_LAST_FOR_PROMOTION_LEVEL = "SELECT BUILD FROM PROMOTED_RUN WHERE PROMOTION_LEVEL = :promotionLevel ORDER BY BUILD DESC LIMIT 1";
    String BUILD_DELETE = "DELETE FROM BUILD WHERE ID = :id";
    String BUILD_UPDATE = "UPDATE BUILD SET NAME = :name, DESCRIPTION = :description WHERE ID = :id";

    // Validation stamps
    long VALIDATION_STAMP_IMAGE_MAXSIZE = 4096;
    String VALIDATION_STAMP = "SELECT ID, BRANCH, NAME, DESCRIPTION, PROMOTION_LEVEL, ORDERNB, OWNER_ID FROM VALIDATION_STAMP WHERE ID = :id";
    String VALIDATION_STAMP_BY_BRANCH_AND_NAME = "SELECT ID, BRANCH, NAME, DESCRIPTION, PROMOTION_LEVEL, ORDERNB, OWNER_ID FROM VALIDATION_STAMP WHERE BRANCH = :branch AND NAME = :name";
    String VALIDATION_STAMP_LIST = "SELECT ID, BRANCH, NAME, DESCRIPTION, PROMOTION_LEVEL, ORDERNB, OWNER_ID FROM VALIDATION_STAMP WHERE BRANCH = :branch ORDER BY ORDERNB";
    String VALIDATION_STAMP_CREATE = "INSERT INTO VALIDATION_STAMP (BRANCH, NAME, DESCRIPTION, ORDERNB) VALUES (:branch, :name, :description, :orderNb)";
    String VALIDATION_STAMP_DELETE = "DELETE FROM VALIDATION_STAMP WHERE ID = :id";
    String VALIDATIONSTAMP_IMAGE_UPDATE = "UPDATE VALIDATION_STAMP SET IMAGE = :image WHERE ID = :id";
    String VALIDATIONSTAMP_IMAGE = "SELECT IMAGE FROM VALIDATION_STAMP WHERE ID = :id";
    String VALIDATION_STAMP_PROMOTION_LEVEL = "UPDATE VALIDATION_STAMP SET PROMOTION_LEVEL = :promotionLevel WHERE ID = :id";
    String VALIDATION_STAMP_FOR_PROMOTION_LEVEL = "SELECT ID, BRANCH, NAME, DESCRIPTION, PROMOTION_LEVEL, ORDERNB, OWNER_ID FROM VALIDATION_STAMP WHERE PROMOTION_LEVEL = :promotionLevel ORDER BY ORDERNB";
    String VALIDATION_STAMP_WITHOUT_PROMOTION_LEVEL = "SELECT ID, BRANCH, NAME, DESCRIPTION, PROMOTION_LEVEL, ORDERNB, OWNER_ID FROM VALIDATION_STAMP WHERE BRANCH = :branch AND PROMOTION_LEVEL IS NULL ORDER BY ORDERNB";
    String VALIDATION_STAMP_UPDATE = "UPDATE VALIDATION_STAMP SET NAME = :name, DESCRIPTION = :description WHERE ID = :id";
    String VALIDATION_STAMP_COUNT = "SELECT COUNT(*) FROM VALIDATION_STAMP WHERE BRANCH = :branch";
    String VALIDATION_STAMP_HIGHER = "SELECT ID FROM VALIDATION_STAMP WHERE BRANCH = :branch AND ORDERNB > :orderNb ORDER BY ORDERNB ASC LIMIT 1";
    String VALIDATION_STAMP_LOWER = "SELECT ID FROM VALIDATION_STAMP WHERE BRANCH = :branch AND ORDERNB < :orderNb ORDER BY ORDERNB DESC LIMIT 1";
    String VALIDATION_STAMP_BY_BRANCH_AND_ORDER = "SELECT ID FROM VALIDATION_STAMP WHERE BRANCH = :branch AND ORDERNB = :orderNb";
    String VALIDATION_STAMP_LEVELNB = "SELECT ORDERNB FROM VALIDATION_STAMP WHERE ID = :id";
    String VALIDATION_STAMP_SET_LEVELNB = "UPDATE VALIDATION_STAMP SET ORDERNB = :orderNb WHERE ID = :id";
    String VALIDATION_STAMP_INC_ORDERNB = "UPDATE VALIDATION_STAMP SET ORDERNB = ORDERNB + 1 WHERE BRANCH = :branch AND ORDERNB >= :low AND ORDERNB < :high";
    String VALIDATION_STAMP_DEC_ORDERNB = "UPDATE VALIDATION_STAMP SET ORDERNB = ORDERNB - 1 WHERE BRANCH = :branch AND ORDERNB > :low AND ORDERNB <= :high";
    String VALIDATION_STAMP_CHANGE_OWNER = "UPDATE VALIDATION_STAMP SET OWNER_ID = :owner WHERE ID = :id";

    // Promotion levels
    long PROMOTION_LEVEL_IMAGE_MAXSIZE = 4096;
    String PROMOTION_LEVEL = "SELECT * FROM PROMOTION_LEVEL WHERE ID = :id";
    String PROMOTION_LEVEL_LIST = "SELECT * FROM PROMOTION_LEVEL WHERE BRANCH = :branch ORDER BY LEVELNB DESC";
    String PROMOTION_LEVEL_COUNT = "SELECT COUNT(*) FROM PROMOTION_LEVEL WHERE BRANCH = :branch";
    String PROMOTION_LEVEL_CREATE = "INSERT INTO PROMOTION_LEVEL (BRANCH, NAME, DESCRIPTION, LEVELNB, AUTOPROMOTE) VALUES (:branch, :name, :description, :levelNb, FALSE)";
    String PROMOTION_LEVEL_IMAGE_UPDATE = "UPDATE PROMOTION_LEVEL SET IMAGE = :image WHERE ID = :id";
    String PROMOTION_LEVEL_IMAGE = "SELECT IMAGE FROM PROMOTION_LEVEL WHERE ID = :id";
    String PROMOTION_LEVEL_HIGHER = "SELECT ID FROM PROMOTION_LEVEL WHERE BRANCH = :branch AND LEVELNB > :levelNb ORDER BY LEVELNB ASC LIMIT 1";
    String PROMOTION_LEVEL_LOWER = "SELECT ID FROM PROMOTION_LEVEL WHERE BRANCH = :branch AND LEVELNB < :levelNb ORDER BY LEVELNB DESC LIMIT 1";
    String PROMOTION_LEVEL_LEVELNB = "SELECT LEVELNB FROM PROMOTION_LEVEL WHERE ID = :id";
    String PROMOTION_LEVEL_SET_LEVELNB = "UPDATE PROMOTION_LEVEL SET LEVELNB = :levelNb WHERE ID = :id";
    String PROMOTION_LEVEL_DELETE = "DELETE FROM PROMOTION_LEVEL WHERE ID = :id";
    String PROMOTION_LEVEL_UPDATE_LEVEL_NB_AFTER_DELETE = "UPDATE PROMOTION_LEVEL SET LEVELNB = LEVELNB - 1 WHERE LEVELNB > :levelNb";
    String PROMOTION_LEVEL_UPDATE = "UPDATE PROMOTION_LEVEL SET NAME = :name, DESCRIPTION = :description WHERE ID = :id";
    String PROMOTION_LEVEL_AUTO_PROMOTE = "UPDATE PROMOTION_LEVEL SET AUTOPROMOTE = :flag WHERE ID = :id";
    String PROMOTION_LEVEL_BY_BRANCH_AND_NAME = "SELECT * FROM PROMOTION_LEVEL WHERE BRANCH = :branch AND NAME = :name";

    // Validation runs
    String VALIDATION_RUN = "SELECT R.* FROM VALIDATION_RUN R WHERE R.ID = :id";
    String VALIDATION_RUN_CREATE = "INSERT INTO VALIDATION_RUN (BUILD, VALIDATION_STAMP, DESCRIPTION, RUN_ORDER) VALUES (:build, :validationStamp, :description, :runOrder)";
    String VALIDATION_RUN_FOR_BUILD_AND_STAMP = "SELECT * FROM VALIDATION_RUN WHERE BUILD = :build AND VALIDATION_STAMP = :validationStamp ORDER BY ID ASC";
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
    String VALIDATION_RUN_LAST_OF_BUILD_BY_VALIDATION_STAMP = "SELECT DISTINCT(BUILD) FROM VALIDATION_RUN WHERE VALIDATION_STAMP = :validationStamp ORDER BY BUILD DESC LIMIT :limit";
    String VALIDATION_RUN_DELETE = "DELETE FROM VALIDATION_RUN WHERE ID = :id";

    // Validation run statuses
    String VALIDATION_RUN_STATUS_CREATE = "INSERT INTO VALIDATION_RUN_STATUS (VALIDATION_RUN, STATUS, DESCRIPTION, AUTHOR, AUTHOR_ID, STATUS_TIMESTAMP) VALUES (:validationRun, :status, :description, :author, :authorId, :statusTimestamp)";
    String VALIDATION_RUN_STATUS_LAST = "SELECT * FROM VALIDATION_RUN_STATUS WHERE VALIDATION_RUN = :id ORDER BY ID DESC LIMIT 1";
    String VALIDATION_RUN_STATUS_BY_NAME = "SELECT * FROM VALIDATION_RUN_STATUS WHERE UPPER(DESCRIPTION) LIKE :text";
    String VALIDATION_RUN_STATUS_BY_RUN = "SELECT * FROM VALIDATION_RUN_STATUS WHERE VALIDATION_RUN = :run ORDER BY ID DESC";
    String VALIDATION_RUN_STATUS_RENAME_AUTHOR = "UPDATE VALIDATION_RUN_STATUS SET AUTHOR = :name WHERE AUTHOR_ID = :id";

    // Promoted runs
    String PROMOTED_RUN_DELETE = "DELETE FROM PROMOTED_RUN WHERE PROMOTION_LEVEL = :promotionLevel AND BUILD = :build";
    String PROMOTED_RUN_CREATE = "INSERT INTO PROMOTED_RUN (PROMOTION_LEVEL, BUILD, AUTHOR_ID, AUTHOR, CREATION, DESCRIPTION) VALUES (:promotionLevel, :build, :authorId, :author, :creation, :description)";
    String PROMOTED_RUN = "SELECT * FROM PROMOTED_RUN WHERE BUILD = :build AND PROMOTION_LEVEL = :promotionLevel";
    String PROMOTION_LEVEL_FOR_BUILD = "SELECT L.* FROM PROMOTION_LEVEL L, PROMOTED_RUN R WHERE R.PROMOTION_LEVEL = L.ID AND R.BUILD = :build ORDER BY L.LEVELNB";
    String PROMOTED_EARLIEST_RUN = "SELECT BUILD FROM PROMOTED_RUN WHERE BUILD >= :build AND PROMOTION_LEVEL = :promotionLevel ORDER BY BUILD ASC LIMIT 1";
    String PROMOTED_RUN_BY_PROMOTION_LEVEL = "SELECT * FROM PROMOTED_RUN WHERE PROMOTION_LEVEL = :promotionLevel ORDER BY BUILD DESC LIMIT :count OFFSET :offset";
    String PROMOTED_RUN_BY_BUILD = "SELECT PR.* FROM PROMOTED_RUN PR INNER JOIN PROMOTION_LEVEL PL ON PL.ID = PR.PROMOTION_LEVEL  WHERE PR.BUILD = :build ORDER BY PL.LEVELNB ";
    String PROMOTED_RUN_REMOVE = "DELETE FROM PROMOTED_RUN WHERE BUILD = :build AND PROMOTION_LEVEL = :promotionLevel";

    // Audit
    String ENTITY_NAME = "SELECT %s FROM %s WHERE ID = :id";
    String EVENT_VALUE_INSERT = "INSERT INTO EVENT_VALUES (EVENT, PROP_NAME, PROP_VALUE) VALUES (:id, :name, :value)";
    String EVENT_VALUE_LIST = "SELECT PROP_NAME, PROP_VALUE FROM EVENT_VALUES WHERE EVENT = :id";
    String EVENT = "SELECT * FROM EVENTS WHERE ID = :id";
    String EVENTS_TO_SEND = "SELECT * FROM EVENTS WHERE SENT IS NULL OR SENT IS FALSE ORDER BY ID ASC";
    String EVENT_SENT = "UPDATE EVENTS SET SENT = TRUE WHERE ID = :id";
    String EVENTS_RENAME_AUTHOR = "UPDATE EVENTS SET AUTHOR = :name WHERE AUTHOR_ID = :id";

    // Accounts
    String ACCOUNT_AUTHENTICATE = "SELECT ID, NAME, FULLNAME, EMAIL, ROLENAME, MODE, LOCALE FROM ACCOUNTS WHERE MODE = 'builtin' AND NAME = :user AND PASSWORD = :password";
    String ACCOUNT_ROLE = "SELECT ROLENAME FROM ACCOUNTS WHERE MODE = :mode AND NAME = :user";
    String ACCOUNT_BY_NAME = "SELECT ID, NAME, FULLNAME, EMAIL, ROLENAME, MODE, LOCALE FROM ACCOUNTS WHERE MODE = :mode AND NAME = :user";
    String ACCOUNT = "SELECT * FROM ACCOUNTS WHERE ID = :id";
    String ACCOUNT_LIST = "SELECT ID, NAME, FULLNAME, EMAIL, ROLENAME, MODE, LOCALE FROM ACCOUNTS ORDER BY NAME";
    String ACCOUNT_CREATE = "INSERT INTO ACCOUNTS (NAME, FULLNAME, EMAIL, ROLENAME, MODE, PASSWORD) VALUES (:name, :fullName, :email, :roleName, :mode, :password)";
    String ACCOUNT_DELETE = "DELETE FROM ACCOUNTS WHERE ID = :id";
    String ACCOUNT_UPDATE = "UPDATE ACCOUNTS SET NAME = :name, FULLNAME = :fullName, EMAIL = :email, ROLENAME = :roleName WHERE ID = :id";
    String ACCOUNT_CHANGE_PASSWORD = "UPDATE ACCOUNTS SET PASSWORD = :newPassword WHERE ID = :id AND MODE = 'builtin' AND PASSWORD = :oldPassword";
    String ACCOUNT_RESET_PASSWORD = "UPDATE ACCOUNTS SET PASSWORD = :password WHERE ID = :id AND MODE = 'builtin'";
    String ACCOUNT_CHANGE_EMAIL = "UPDATE ACCOUNTS SET EMAIL = :email WHERE ID = :id AND MODE = 'builtin' AND PASSWORD = :password";
    String ACCOUNT_CHANGE_LOCALE = "UPDATE ACCOUNTS SET LOCALE = :locale WHERE ID = :id";

    // Configuration
    String CONFIGURATION_GET = "SELECT VALUE FROM CONFIGURATION WHERE NAME = :name";
    String CONFIGURATION_DELETE = "DELETE FROM CONFIGURATION WHERE NAME = :name";
    String CONFIGURATION_INSERT = "INSERT INTO CONFIGURATION (NAME, VALUE) VALUES (:name, :value)";

    // Comments
    String COMMENT_CREATE = "INSERT INTO COMMENT (%s, CONTENT, AUTHOR, AUTHOR_ID, COMMENT_TIMESTAMP) VALUES (:id, :content, :author, :author_id, :comment_timestamp)";
    String COMMENT_RENAME_AUTHOR = "UPDATE COMMENT SET AUTHOR = :name WHERE AUTHOR_ID = :id";
    String COMMENT_FOR_ENTITY = "SELECT * FROM COMMENT WHERE %s = :id ORDER BY ID DESC LIMIT :count OFFSET :offset";

    // Properties
    String PROPERTY_DELETE = "DELETE FROM PROPERTIES WHERE %s = :entityId AND EXTENSION = :extension AND NAME = :name";
    String PROPERTY_INSERT = "INSERT INTO PROPERTIES (EXTENSION, NAME, VALUE, %s) VALUES (:extension, :name, :value, :entityId)";
    String PROPERTY_ALL = "SELECT * FROM PROPERTIES WHERE %s = :entityId ORDER BY EXTENSION, NAME";
    String PROPERTY_VALUE = "SELECT * FROM PROPERTIES WHERE %s = :entityId AND EXTENSION = :extension AND NAME = :name";

    // Subscriptions
    String SUBSCRIPTION_DELETE = "DELETE FROM SUBSCRIPTION WHERE ACCOUNT = :account AND %s = :entityId";
    String SUBSCRIPTION_CREATE = "INSERT INTO SUBSCRIPTION (ACCOUNT, %s) VALUES (:account, :entityId)";
    String SUBSCRIPTION_BY_ACCOUNT = "SELECT * FROM SUBSCRIPTION WHERE ACCOUNT = :account";

    // Filter on validation stamps
    String VALIDATION_STAMP_SELECTION_EXISTS = "SELECT ID FROM VALIDATION_STAMP_SELECTION WHERE ACCOUNT = :account AND VALIDATION_STAMP = :validationStamp LIMIT 1";
    String VALIDATION_STAMP_SELECTION_DELETE = "DELETE VALIDATION_STAMP_SELECTION WHERE ACCOUNT = :account AND VALIDATION_STAMP = :validationStamp";
    String VALIDATION_STAMP_SELECTION_INSERT = "INSERT INTO VALIDATION_STAMP_SELECTION (ACCOUNT, VALIDATION_STAMP) VALUES (:account, :validationStamp)";

    // Build cleanup
    String BUILD_CLEANUP_DELETE = "DELETE FROM BUILD_CLEANUP WHERE BRANCH = :branch";
    String BUILD_CLEANUP_INSERT = "INSERT INTO BUILD_CLEANUP (BRANCH, RETENTION) VALUES (:branch, :retention)";
    String BUILD_CLEANUP_PROMOTION_INSERT = "INSERT INTO BUILD_CLEANUP_PROMOTION (BUILD_CLEANUP, PROMOTION_LEVEL) VALUES (:buildCleanup, :promotionLevel)";
    String BUILD_CLEANUP_FIND_BY_BRANCH = "SELECT * FROM BUILD_CLEANUP WHERE BRANCH = :branch";
    String BUILD_CLEANUP_PROMOTION_BY_ID = "SELECT PROMOTION_LEVEL FROM BUILD_CLEANUP_PROMOTION WHERE BUILD_CLEANUP = :id";
    String BUILD_CLEANUP = "SELECT DISTINCT(B.ID)" +
            "FROM BUILD B\n" +
            "INNER JOIN EVENTS E ON E.BUILD = B.ID AND E.EVENT_TYPE = 'BUILD_CREATED'\n" +
            "WHERE B.BRANCH = :branch\n" +
            "AND TIMESTAMPDIFF(DAY, E.EVENT_TIMESTAMP, :now) > :retention\n" +
            "AND (SELECT COUNT(*) FROM PROMOTED_RUN PR WHERE PR.BUILD = B.ID AND PR.PROMOTION_LEVEL IN (%s)) = 0\n" +
            "ORDER BY B.ID";

    // Dashboards
    String DASHBOARD_VALIDATION_STAMP_EXISTS = "SELECT VALIDATION_STAMP FROM DASHBOARD_VALIDATION_STAMP WHERE VALIDATION_STAMP = :validationStamp AND BRANCH = :branch";
    String DASHBOARD_VALIDATION_STAMP_INSERT = "INSERT INTO DASHBOARD_VALIDATION_STAMP (BRANCH, VALIDATION_STAMP) VALUES (:branch, :validationStamp)";
    String DASHBOARD_VALIDATION_STAMP_DELETE = "DELETE FROM DASHBOARD_VALIDATION_STAMP WHERE VALIDATION_STAMP = :validationStamp AND BRANCH = :branch";

    // Account filters
    String ACCOUNT_FILTER_DELETE = "DELETE FROM ACCOUNT_FILTER WHERE ACCOUNT = :account AND BRANCH = :branch AND FILTERNAME = :filterName";
    String ACCOUNT_FILTER_INSERT = "INSERT INTO ACCOUNT_FILTER (ACCOUNT, BRANCH, FILTERNAME, FILTER) VALUES (:account, :branch, :filterName, :filter)";
    String ACCOUNT_FILTER_LIST = "SELECT * FROM ACCOUNT_FILTER WHERE ACCOUNT = :account AND BRANCH = :branch ORDER BY FILTERNAME ASC";

    // Stats & charts
    String VALIDATION_RUN_COUNT_OF_STATUS_AND_VALIDATION_STAMP = "SELECT COUNT(*) " +
            "FROM VALIDATION_RUN_STATUS VRS " +
            "INNER JOIN VALIDATION_RUN VR ON VR.ID = VRS.VALIDATION_RUN " +
            "WHERE VR.VALIDATION_STAMP = :validationStamp " +
            "AND VRS.STATUS = :status " +
            "AND VR.RUN_ORDER = (SELECT MAX(VR2.RUN_ORDER) FROM VALIDATION_RUN VR2 WHERE VR2.VALIDATION_STAMP = VR.VALIDATION_STAMP AND VR2.BUILD = VR.BUILD) ";

    // Reordering of branches
    /**
     * See {@link net.ontrack.backend.dao.jdbc.DBFixReorderingValidationStamps}
     */
    String VALIDATION_REORDERING_BRANCHES = "SELECT ID FROM BRANCH";
}
