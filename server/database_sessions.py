import json
import sqlite3
import time as time_checker

from aes_operations import aes_generate_key
from session_key import generate_session_key


class SessionTimeoutExceeded(Exception):
    def __str__(self):
        return "Session timeout exceeded"


class SessionAlreadyExist(Exception):
    def __str__(self):
        return "Session is already exist"


class SessionNotFound(Exception):
    def __str__(self):
        return "Session is not found"


class SessionsDatabase:
    @staticmethod
    def generate_session_key():
        __connection = sqlite3.connect('database.db')
        __cursor = __connection.cursor()
        session_key = generate_session_key(400)
        aes_key = aes_generate_key()
        next_time = json.dumps(time_checker.time())
        with __connection:
            try:
                __cursor.execute("SELECT * FROM `sessions` WHERE `session_key` = (?)",
                                 (session_key,)).fetchmany(1)[0]
            except IndexError:
                pass
            else:
                raise SessionAlreadyExist
        with __connection:
            __cursor.execute(
                "INSERT INTO `sessions` (`session_key`, `aes_key`, `time`) "
                "VALUES (?, ?, ?)",
                (session_key, aes_key, next_time))
            return [session_key, aes_key]

    @staticmethod
    def update_session_key(session_key, user_id=None):
        __connection = sqlite3.connect('database.db')
        __cursor = __connection.cursor()
        # get session's time and aes_key
        with __connection:
            try:
                time, aes_key = __cursor.execute("SELECT `time`, `aes_key` FROM `sessions` WHERE "
                                                 "`session_key` = (?)",
                                                 (session_key,)).fetchmany(1)[0]
            except IndexError:
                raise SessionNotFound
        #  if session_key time more than 24h, key - delete
        if time_checker.time() - json.loads(time) >= 60 * 60:
            with __connection:
                __cursor.execute(
                    "DELETE FROM `sessions` WHERE `session_key` = (?)",
                    (session_key,))
                raise SessionTimeoutExceeded

        next_session_key = generate_session_key(400)
        next_time = json.dumps(time_checker.time())
        next_aes_key = aes_generate_key()

        if user_id is None:
            user_id = __cursor.execute("SELECT `user_id` FROM `sessions` WHERE "
                                       "`session_key` = (?)",
                                       (session_key,)).fetchmany(1)[0][0]
        with __connection:
            __cursor.execute(
                "DELETE FROM `sessions` WHERE `session_key` = (?)",
                (session_key,))
            __cursor.execute(
                "INSERT INTO `sessions` (`session_key`, `aes_key`, `user_id`, `time`) "
                "VALUES (?, ?, ?, ?)",
                (next_session_key, next_aes_key, user_id, next_time,))
            return [(session_key, next_session_key, aes_key, next_aes_key)]

    @staticmethod
    def get_user_id(session_key):
        __connection = sqlite3.connect('database.db')
        __cursor = __connection.cursor()
        with __connection:
            return __cursor.execute("SELECT `user_id` FROM `sessions` WHERE `session_key` = (?)",
                                    (session_key,)).fetchmany(1)[0][0]

    @staticmethod
    def delete_sessions():
        __connection = sqlite3.connect('database.db')
        __cursor = __connection.cursor()
        with __connection:
            data = __cursor.execute("SELECT `time`, `session_key` FROM `sessions`", ).fetchall()
            for time, session in data:
                if time_checker.time() - json.loads(time) >= 60 * 60:
                    __cursor.execute(
                        "DELETE FROM `sessions` WHERE `session_key` = (?)",
                        (session,))
