import sqlite3

connection = sqlite3.connect('database.db')
cursor = connection.cursor()

cursor.execute('''
CREATE TABLE users (
    user_id     INTEGER PRIMARY KEY,
    nickname    TEXT    UNIQUE
                        NOT NULL,
    email       TEXT    NOT NULL,
    password    TEXT    NOT NULL,
    public_key  TEXT    NOT NULL,
    private_key TEXT    NOT NULL
                        UNIQUE,
    attempts    INTEGER NOT NULL DEFAULT (0)
);
CREATE TABLE sessions (
    session_key TEXT    UNIQUE
                        NOT NULL,
    aes_key     TEXT    NOT NULL,
    user_id     INTEGER REFERENCES users (user_id) ON DELETE SET NULL
                                                   ON UPDATE CASCADE,
    time        TEXT
);
CREATE TABLE session_email (
    session_key TEXT NOT NULL,
    email_code  TEXT NOT NULL,
    user_id          NOT NULL,
    time        TEXT NOT NULL
);
CREATE TABLE change_password (
    session_key TEXT    NOT NULL,
    user_id     INTEGER NOT NULL
);
''')

connection.commit()
connection.close()
