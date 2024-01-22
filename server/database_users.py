import sqlite3
import aes_operations
from rsa_operations import rsa_generate_keys
import hashlib
import re


class UsernameIsBusy(Exception):
    def __str__(self):
        return f"Username is busy"


class AttemptsLimit(Exception):
    def __str__(self):
        return (f"The login attempt limit has been exceeded, your account is blocked. Try to recover it by changing "
                f"your password.")


class PasswordNotCorrect(Exception):
    def __str__(self):
        return f"Password not corrected"


class EmailNotCorrect(Exception):
    def __str__(self):
        return f"Email not corrected"


class PasswordIsWrong(Exception):
    def __str__(self):
        return f"Password is wrong"


class EmailIsBusy(Exception):
    def __str__(self):
        return f"Email is busy"


class UserIsNotFound(Exception):
    def __str__(self):
        return f"User is not found"


class UsersDatabase:
    @staticmethod
    def sing_up(username: str, email: str, password: str):
        if not re.match(r'^[\w.-]+@[\w.-]+\.\w+$', email):
            raise EmailNotCorrect
        if not re.match(r'^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&_])[A-Za-z\d@$!%*?&_]{8,}$', password):
            raise PasswordNotCorrect

        __connection = sqlite3.connect('database.db')
        __cursor = __connection.cursor()
        if UsersDatabase.user_exists_by_username(username):
            raise UsernameIsBusy
        if UsersDatabase.user_exists_by_email(email):
            raise EmailIsBusy
        password_hash = hashlib.sha256((email + password).encode('utf-8'))
        key = aes_operations.aes_generate_key()
        while UsersDatabase.user_exists_by_private_key(key):
            keys = rsa_generate_keys()
        with __connection:
            __cursor.execute(
                "INSERT INTO `users` (`nickname`, `email`, `password`,`public_key`, `private_key`) "
                "VALUES (?, ?, ?, ?, ?)",
                (username, email, password_hash.hexdigest(), key, key))
        with __connection:
            return __cursor.execute("SELECT `user_id` FROM `users` WHERE `nickname` = (?)",
                                    (username,)).fetchmany(1)

    @staticmethod
    def log_in(email: str, password: str):
        if not re.match(r'^[\w.-]+@[\w.-]+\.\w+$', email):
            raise EmailNotCorrect
        if not UsersDatabase.user_exists_by_email(email):
            raise UserIsNotFound
        __connection = sqlite3.connect('database.db')
        __cursor = __connection.cursor()
        with __connection:
            __attempts = __cursor.execute("SELECT `attempts` FROM `users` WHERE `email` = (?)",
                                          (email,)).fetchmany(1)[0][0]
            if __attempts > 4:
                raise AttemptsLimit
        # ? Add checker for ------------------------
        # ! PasswordNotCorrect
        # ? ----------------------------------------

        password_hash = hashlib.sha256((email + password).encode('utf-8'))
        with __connection:
            user_id_database = __cursor.execute("SELECT `user_id` FROM `users` WHERE `email` = (?) AND "
                                                "`password` = (?)",
                                                (email, password_hash.hexdigest(),)).fetchmany(1)
            if user_id_database:
                return user_id_database
            with __connection:
                __cursor.execute("UPDATE `users` SET `attempts` = (?) WHERE `email` = (?)",
                                 ((__attempts + 1), email)).fetchmany(1)
            raise PasswordIsWrong

    @staticmethod
    def use_user_public_key(username: str, message: str):
        __connection = sqlite3.connect('database.db')
        __cursor = __connection.cursor()
        if not UsersDatabase.user_exists_by_username(username):
            raise UserIsNotFound
        with __connection:
            public_key = __cursor.execute("SELECT `public_key` FROM `users` WHERE `nickname` = (?)",
                                          (username,)).fetchmany(1)[0][0]
            return aes_operations.aes_encrypt(message, public_key)

    @staticmethod
    def get_username_by_id(username_id: int):
        __connection = sqlite3.connect('database.db')
        __cursor = __connection.cursor()
        if not UsersDatabase.user_exists_by_id(username_id):
            raise UserIsNotFound
        with __connection:
            return __cursor.execute("SELECT `nickname` FROM `users` WHERE `user_id` = (?)",
                                    (username_id,)).fetchmany(1)[0][0]

    @staticmethod
    def get_user_email(username: str):
        __connection = sqlite3.connect('database.db')
        __cursor = __connection.cursor()
        if not UsersDatabase.user_exists_by_username(username):
            raise UserIsNotFound
        with __connection:
            return __cursor.execute("SELECT `email` FROM `users` WHERE `nickname` = (?)",
                                    (username,)).fetchmany(1)[0][0]

    @staticmethod
    def use_user_private_key(username_id: int, message: str):
        __connection = sqlite3.connect('database.db')
        __cursor = __connection.cursor()
        with __connection:
            private_key = __cursor.execute("SELECT `private_key` FROM `users` WHERE `user_id` = (?)",
                                           (username_id,)).fetchmany(1)[0][0]
            return aes_operations.aes_decrypt(message, private_key)

    @staticmethod
    def user_exists_by_username(username: str):
        __connection = sqlite3.connect('database.db')
        __cursor = __connection.cursor()
        with __connection:
            result = __cursor.execute("SELECT * FROM `users` WHERE `nickname` = (?)",
                                      (username,)).fetchmany(1)
            return bool(len(result))

    @staticmethod
    def user_exists_by_id(id: int):
        __connection = sqlite3.connect('database.db')
        __cursor = __connection.cursor()
        with __connection:
            result = __cursor.execute("SELECT * FROM `users` WHERE `user_id` = (?)",
                                      (id,)).fetchmany(1)
            return bool(len(result))

    @staticmethod
    def user_exists_by_email(email: str):
        __connection = sqlite3.connect('database.db')
        __cursor = __connection.cursor()
        with __connection:
            result = __cursor.execute("SELECT * FROM `users` WHERE `email` = (?)",
                                      (email,)).fetchmany(1)
            return bool(len(result))

    @staticmethod
    def user_exists_by_private_key(private_key: str):
        __connection = sqlite3.connect('database.db')
        __cursor = __connection.cursor()
        with __connection:
            result = __cursor.execute("SELECT * FROM `users` WHERE `private_key` = (?)",
                                      (private_key,)).fetchmany(1)
            return bool(len(result))

    @staticmethod
    def change_username(username_id, new_username, password):
        if not re.match(r'^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&_])[A-Za-z\d@$!%*?&_]{8,}$', password):
            raise PasswordNotCorrect

        __connection = sqlite3.connect('database.db')
        __cursor = __connection.cursor()
        if UsersDatabase.user_exists_by_username(new_username):
            raise UsernameIsBusy
        username = UsersDatabase.get_username_by_id(username_id)
        email = UsersDatabase.get_user_email(username)
        try:
            UsersDatabase.log_in(email, password)[0][0]
        except IndexError:
            raise PasswordIsWrong
        password_hash = hashlib.sha256((email + password).encode('utf-8'))
        with __connection:
            return __cursor.execute("UPDATE `users` SET `nickname` = (?), `password` = (?) "
                                    "WHERE `user_id` = (?)",
                                    (new_username, password_hash.hexdigest(), username_id,)).fetchmany(1)

    @staticmethod
    def change_password(username_id, new_password, password):
        if not re.match(r'^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&_])[A-Za-z\d@$!%*?&_]{8,}$', password):
            raise PasswordNotCorrect
        if not re.match(r'^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&_])[A-Za-z\d@$!%*?&_]{8,}$', new_password):
            raise PasswordNotCorrect

        __connection = sqlite3.connect('database.db')
        __cursor = __connection.cursor()
        username = UsersDatabase.get_username_by_id(username_id)
        email = UsersDatabase.get_user_email(username)
        try:
            UsersDatabase.log_in(email, password)[0][0]
        except IndexError:
            raise PasswordIsWrong
        password_hash = hashlib.sha256((email + new_password).encode('utf-8'))
        with __connection:
            return __cursor.execute("UPDATE `users` SET `password` = (?), `attempts` = (?) "
                                    "WHERE `user_id` = (?)",
                                    (password_hash.hexdigest(), 0, username_id,)).fetchmany(1)

    @staticmethod
    def get_user_id_by_email(email: str):
        __connection = sqlite3.connect('database.db')
        __cursor = __connection.cursor()
        with __connection:
            return __cursor.execute("SELECT `user_id` FROM `users` WHERE `email` = (?)",
                                    (email,)).fetchmany(1)[0][0]

    @staticmethod
    def forgot_password(username_id, new_password):
        if not re.match(r'^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&_])[A-Za-z\d@$!%*?&_]{8,}$', new_password):
            raise PasswordNotCorrect

        __connection = sqlite3.connect('database.db')
        __cursor = __connection.cursor()
        username = UsersDatabase.get_username_by_id(username_id)
        email = UsersDatabase.get_user_email(username)
        password_hash = hashlib.sha256((email + new_password).encode('utf-8'))
        with __connection:
            return __cursor.execute("UPDATE `users` SET `password` = (?), `attempts` = (?) "
                                    "WHERE `user_id` = (?)",
                                    (password_hash.hexdigest(), 0, username_id,)).fetchmany(1)
