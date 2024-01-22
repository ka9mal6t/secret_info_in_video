import json
import sqlite3
from database_sessions import SessionNotFound, SessionTimeoutExceeded
import time as time_checker


class EmailsCodesDatabase:
    @staticmethod
    def create(session: str, code: str, user_id):
        __connection = sqlite3.connect('database.db')
        __cursor = __connection.cursor()
        next_time = json.dumps(time_checker.time())
        with __connection:
            __cursor.execute(
                "INSERT INTO `session_email` (`session_key`, `email_code`, `user_id`, `time`) "
                "VALUES (?, ?, ?, ?)",
                (session, code, user_id, next_time,))

    @staticmethod
    def check(session: str, code: str):
        __connection = sqlite3.connect('database.db')
        __cursor = __connection.cursor()

        with __connection:
            try:
                email_code = __cursor.execute("SELECT `email_code` FROM `session_email` WHERE `session_key` = (?)",
                                              (session,)).fetchmany(1)[0][0]
            except IndexError:
                raise SessionNotFound

        with __connection:
            time = __cursor.execute("SELECT `time` FROM `session_email` WHERE `session_key` = (?)",
                                    (session,)).fetchmany(1)[0][0]
            if time_checker.time() - json.loads(time) >= 5 * 60:
                with __connection:
                    __cursor.execute(
                        "DELETE FROM `session_email` WHERE `session_key` = (?)",
                        (session,))
                    raise SessionTimeoutExceeded
        if email_code == code:
            with __connection:
                user_id = __cursor.execute("SELECT `user_id` FROM `session_email` WHERE `session_key` = (?)",
                                           (session,)).fetchmany(1)[0][0]
            with __connection:
                __cursor.execute(
                    "DELETE FROM `session_email` WHERE `session_key` = (?)",
                    (session,))
            return user_id
        return False

    @staticmethod
    def delete_sessions():
        __connection = sqlite3.connect('database.db')
        __cursor = __connection.cursor()
        with __connection:
            data = __cursor.execute("SELECT `time`, `session_key` FROM `session_email`",).fetchall()
            for time, session in data:
                if time_checker.time() - json.loads(time) >= 5 * 60:
                    with __connection:
                        __cursor.execute(
                            "DELETE FROM `session_email` WHERE `session_key` = (?)",
                            (session,))
