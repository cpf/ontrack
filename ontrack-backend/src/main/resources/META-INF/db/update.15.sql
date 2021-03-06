CREATE TABLE SUBSCRIPTION (
    ID INTEGER NOT NULL AUTO_INCREMENT,
    ACCOUNT INTEGER NOT NULL,
	PROJECT_GROUP INTEGER NULL,
	PROJECT INTEGER NULL,
	BRANCH INTEGER NULL,
	BUILD INTEGER NULL,
	PROMOTION_LEVEL INTEGER NULL,
	VALIDATION_STAMP INTEGER NULL,
	VALIDATION_RUN INTEGER NULL,
	CONSTRAINT FK_SUBSCRIPTION_PROJECT_GROUP FOREIGN KEY (PROJECT_GROUP) REFERENCES PROJECT_GROUP (ID) ON DELETE CASCADE,
	CONSTRAINT FK_SUBSCRIPTION_PROJECT FOREIGN KEY (PROJECT) REFERENCES PROJECT (ID) ON DELETE CASCADE,
	CONSTRAINT FK_SUBSCRIPTION_BRANCH FOREIGN KEY (BRANCH) REFERENCES BRANCH (ID) ON DELETE CASCADE,
	CONSTRAINT FK_SUBSCRIPTION_BUILD FOREIGN KEY (BUILD) REFERENCES BUILD (ID) ON DELETE CASCADE,
	CONSTRAINT FK_SUBSCRIPTION_PROMOTION_LEVEL FOREIGN KEY (PROMOTION_LEVEL) REFERENCES PROMOTION_LEVEL (ID) ON DELETE CASCADE,
	CONSTRAINT FK_SUBSCRIPTION_VALIDATION_STAMP FOREIGN KEY (VALIDATION_STAMP) REFERENCES VALIDATION_STAMP (ID) ON DELETE CASCADE,
	CONSTRAINT FK_SUBSCRIPTION_VALIDATION_RUN FOREIGN KEY (VALIDATION_RUN) REFERENCES VALIDATION_RUN (ID) ON DELETE CASCADE
);

-- @mysql
-- See update 26
