CREATE TABLE IF NOT EXISTS CLIENTS
(
    `UserID`    int AUTO_INCREMENT NOT NULL,
    `Username`  varchar(50) unique                   NOT NULL,
    `Password`  CHAR(128)                            NOT NULL,
    `Mail`      varchar(100)                         NOT NULL,
    `AvatarId`  int,
    check (`AvatarId` BETWEEN 1 AND 8),
    PRIMARY KEY (`UserID`)
    );

CREATE TABLE IF NOT EXISTS STATISTIC
(
    `UserID`    int,
    `placement` int not null,
    `DateTime`  DATETIME,
    PRIMARY KEY (`UserID`, `DateTime`),
    FOREIGN KEY (`UserID`) REFERENCES CLIENTS (`UserID`) ON DELETE CASCADE
    );

-- INSERT INTO CLIENTS (Username, Password, Mail, AvatarID) VALUES ('Charles', '123', 'a@b.de', '3');
-- INSERT INTO CLIENTS (Username, Password, Mail, AvatarID) VALUES ('Chloe', '321', 'b@a.de', '1');