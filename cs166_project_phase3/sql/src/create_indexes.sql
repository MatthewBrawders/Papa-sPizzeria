DROP INDEX IF EXISTS login_index;
DROP INDEX IF EXISTS storeIDs_index;

CREATE INDEX login_index
ON Users (login);

CREATE INDEX storeIDs_index
ON Store (storeID);

