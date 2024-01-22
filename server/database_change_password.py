import sqlite3
from database_sessions import SessionNotFound


class ChangePasswordDatabase:
    @staticmethod
    def create(session: str, user_id):
        __connection = sqlite3.connect('database.db')
        __cursor = __connection.cursor()
        with __connection:
            __cursor.execute(
                "INSERT INTO `change_password` (`session_key`, `user_id`) "
                "VALUES (?, ?)",
                (session, user_id))

    @staticmethod
    def check(session: str):
        __connection = sqlite3.connect('database.db')
        __cursor = __connection.cursor()

        with __connection:
            try:
                return __cursor.execute("SELECT `user_id` FROM `change_password` WHERE `session_key` = (?)",
                                        (session,)).fetchmany(1)[0][0]

            except IndexError:
                raise SessionNotFound

    @staticmethod
    def delete(session: str):
        __connection = sqlite3.connect('database.db')
        __cursor = __connection.cursor()
        with __connection:
            return __cursor.execute("DELETE FROM `change_password` WHERE `session_key` = (?)",
                                    (session,)).fetchmany(1)
