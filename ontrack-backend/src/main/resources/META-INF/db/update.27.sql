-- Saving the filters
CREATE TABLE ACCOUNT_FILTER (
  ID INTEGER NOT NULL AUTO_INCREMENT,
  ACCOUNT INTEGER NOT NULL,
  PROJECT INTEGER NOT NULL,
  FILTERNAME VARCHAR(80) NOT NULL,
  FILTER LONGVARCHAR NOT NULL,
  CONSTRAINT FK_ACCOUNT_FILTER_ACCOUNT FOREIGN KEY (ACCOUNT) REFERENCES ACCOUNTS (ID) ON DELETE CASCADE,
  CONSTRAINT FK_ACCOUNT_FILTER_PROJECT FOREIGN KEY (PROJECT) REFERENCES PROJECT (ID) ON DELETE CASCADE,
  CONSTRAINT UQ_ACCOUNT_FILTER UNIQUE (ACCOUNT, PROJECT, FILTERNAME)
);

-- @rollback
DROP TABLE IF EXISTS ACCOUNT_FILTER;