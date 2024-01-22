import os
from flask import Flask, request, jsonify
from flask_mail import Mail, Message
from database_change_password import ChangePasswordDatabase
from database_email_code import EmailsCodesDatabase
from database_sessions import SessionsDatabase, SessionAlreadyExist, SessionNotFound, SessionTimeoutExceeded
from database_users import UsersDatabase, UsernameIsBusy, EmailIsBusy, UserIsNotFound, PasswordIsWrong, \
    PasswordNotCorrect, EmailNotCorrect, AttemptsLimit
from rsa_operations import rsa_encrypt
from aes_operations import aes_encrypt, aes_decrypt
from session_key import generate_session_key
from config import *

app = Flask(__name__)
app.config['SECRET_KEY'] = os.urandom(16)
app.config['MAIL_SERVER'] = MAIL_SERVER
app.config['MAIL_PORT'] = MAIL_PORT
app.config['MAIL_USE_TLS'] = MAIL_USE_TLS
app.config['MAIL_USE_SSL'] = MAIL_USE_SSL
app.config['MAIL_USERNAME'] = MAIL_USERNAME
app.config['MAIL_PASSWORD'] = MAIL_PASSWORD
app.config['MAIL_DEFAULT_SENDER'] = MAIL_DEFAULT_SENDER

mail = Mail(app)


@app.route('/create_session_key', methods=['POST'])
def create_session_key():
    SessionsDatabase.delete_sessions()
    EmailsCodesDatabase.delete_sessions()

    data = request.get_json()

    public_key = data.get('public_key')

    while True:
        try:
            session_key, aes_key = SessionsDatabase.generate_session_key()
            break
        except SessionAlreadyExist:
            # try again
            pass

    try:
        code_cipher = rsa_encrypt(aes_key, public_key)
        ciphertext = aes_encrypt(session_key, aes_key)

        code_cipher['session_key'] = ciphertext

        return jsonify(code_cipher), 200
    except:
        return jsonify('Error!'), 404


@app.route('/register_user', methods=['POST'])
def register_user():
    data = request.get_json()

    session_key = data.get('session_key')
    username = data.get('username')
    email = data.get('email')
    password = data.get('password')

    try:
        session_key, next_session_key, aes_key, next_aes_key = SessionsDatabase.update_session_key(session_key)[0]
    except SessionNotFound:
        return jsonify('Session not found!'), 404
    except SessionTimeoutExceeded:
        return jsonify('Session timeout exceeded!'), 408

    try:
        user_id = UsersDatabase.sing_up(aes_decrypt(username, aes_key),
                                        aes_decrypt(email, aes_key),
                                        aes_decrypt(password, aes_key))[0][0]
    except UsernameIsBusy:
        return jsonify({'ciphertext': aes_encrypt(next_aes_key, aes_key),
                        'session_key': aes_encrypt(next_session_key, next_aes_key),
                        'error': 'Username is busy'}), 201
    except EmailIsBusy:
        return jsonify({'ciphertext': aes_encrypt(next_aes_key, aes_key),
                        'session_key': aes_encrypt(next_session_key, next_aes_key),
                        'error': 'Email is busy'}), 201
    except PasswordNotCorrect:
        return jsonify({'ciphertext': aes_encrypt(next_aes_key, aes_key),
                        'session_key': aes_encrypt(next_session_key, next_aes_key),
                        'error': 'Password format not corrected'}), 201
    except EmailNotCorrect:
        return jsonify({'ciphertext': aes_encrypt(next_aes_key, aes_key),
                        'session_key': aes_encrypt(next_session_key, next_aes_key),
                        'error': 'Email format not corrected'}), 201

    _, next_session_key, _, next_aes_key = SessionsDatabase.update_session_key(next_session_key, user_id)[0]
    return jsonify({'ciphertext': aes_encrypt(next_aes_key, aes_key),
                    'session_key': aes_encrypt(next_session_key, next_aes_key)}), 200


@app.route('/login_user', methods=['POST'])
def login_user():
    data = request.get_json()

    session_key = data.get('session_key')
    email = data.get('email')
    password = data.get('password')

    try:
        session_key, next_session_key, aes_key, next_aes_key = SessionsDatabase.update_session_key(session_key)[0]
    except SessionNotFound:
        return jsonify('Session not found!'), 404
    except SessionTimeoutExceeded:
        return jsonify('Session timeout exceeded!'), 408

    try:
        user_id = UsersDatabase.log_in(aes_decrypt(email, aes_key),
                                       aes_decrypt(password, aes_key))[0][0]
    except UserIsNotFound:
        return jsonify({'ciphertext': aes_encrypt(next_aes_key, aes_key),
                        'session_key': aes_encrypt(next_session_key, next_aes_key),
                        'error': 'User is not found'}), 201
    except PasswordIsWrong:
        return jsonify({'ciphertext': aes_encrypt(next_aes_key, aes_key),
                        'session_key': aes_encrypt(next_session_key, next_aes_key),
                        'error': 'Password is wrong'}), 201
    except PasswordNotCorrect:
        return jsonify({'ciphertext': aes_encrypt(next_aes_key, aes_key),
                        'session_key': aes_encrypt(next_session_key, next_aes_key),
                        'error': 'Password format not corrected'}), 201
    except AttemptsLimit:
        return jsonify({'ciphertext': aes_encrypt(next_aes_key, aes_key),
                        'session_key': aes_encrypt(next_session_key, next_aes_key),
                        'error': 'Account block. Change the password!'}), 202
    except EmailNotCorrect:
        return jsonify({'ciphertext': aes_encrypt(next_aes_key, aes_key),
                        'session_key': aes_encrypt(next_session_key, next_aes_key),
                        'error': 'Email format not corrected'}), 201

    _, next_session_key, _, next_aes_key = SessionsDatabase.update_session_key(next_session_key, user_id)[0]
    return jsonify({'ciphertext': aes_encrypt(next_aes_key, aes_key),
                    'session_key': aes_encrypt(next_session_key, next_aes_key)}), 200


@app.route('/encrypt/<string:username>', methods=['POST'])
def encrypt(username):
    data = request.get_json()
    session_key = data.get('session_key')
    message = data.get('message')
    try:
        session_key, next_session_key, aes_key, next_aes_key = SessionsDatabase.update_session_key(session_key)[0]
    except SessionNotFound:
        return jsonify('Session not found!'), 404
    except SessionTimeoutExceeded:
        return jsonify('Session timeout exceeded!'), 408

    user_id = SessionsDatabase.get_user_id(next_session_key)
    if user_id is None:
        return jsonify({'ciphertext': aes_encrypt(next_aes_key, aes_key),
                        'session_key': aes_encrypt(next_session_key, next_aes_key),
                        'error': 'No authorization'}), 201

    try:
        message = UsersDatabase.use_user_public_key(username, aes_decrypt(message, aes_key))
    except UserIsNotFound:
        return jsonify({'ciphertext': aes_encrypt(next_aes_key, aes_key),
                        'session_key': aes_encrypt(next_session_key, next_aes_key),
                        'error': 'User is not found'}), 201
    except:
        return jsonify({'ciphertext': aes_encrypt(next_aes_key, aes_key),
                        'session_key': aes_encrypt(next_session_key, next_aes_key),
                        'error': 'Error server'}), 201
    else:
        return jsonify({'ciphertext': aes_encrypt(next_aes_key, aes_key),
                        'session_key': aes_encrypt(next_session_key, next_aes_key),
                        'message': aes_encrypt(message, next_aes_key)}), 200


@app.route('/decrypt', methods=['POST'])
def decrypt():
    data = request.get_json()

    session_key = data.get('session_key')
    message = data.get('message')

    try:
        session_key, next_session_key, aes_key, next_aes_key = SessionsDatabase.update_session_key(session_key)[0]
    except SessionNotFound:
        return jsonify('Session not found!'), 404
    except SessionTimeoutExceeded:
        return jsonify('Session timeout exceeded!'), 408

    user_id = SessionsDatabase.get_user_id(next_session_key)
    if user_id is None:
        return jsonify({'ciphertext': aes_encrypt(next_aes_key, aes_key),
                        'session_key': aes_encrypt(next_session_key, next_aes_key),
                        'error': 'No authorization'}), 201
    try:
        message = UsersDatabase.use_user_private_key(user_id, aes_decrypt(message, aes_key))
    except:
        return jsonify({'ciphertext': aes_encrypt(next_aes_key, aes_key),
                        'session_key': aes_encrypt(next_session_key, next_aes_key),
                        'error': 'No authorization'}), 201
    else:
        return jsonify({'ciphertext': aes_encrypt(next_aes_key, aes_key),
                        'session_key': aes_encrypt(next_session_key, next_aes_key),
                        'message': aes_encrypt(message, next_aes_key)}), 200


@app.route('/change_username', methods=['POST'])
def change_username():
    data = request.get_json()

    session_key = data.get('session_key')
    password = data.get('password')
    new_username = data.get('new_username')

    try:
        session_key, next_session_key, aes_key, next_aes_key = SessionsDatabase.update_session_key(session_key)[0]
    except SessionNotFound:
        return jsonify('Session not found!'), 404
    except SessionTimeoutExceeded:
        return jsonify('Session timeout exceeded!'), 408

    user_id = SessionsDatabase.get_user_id(next_session_key)
    if user_id is None:
        return jsonify({'ciphertext': aes_encrypt(next_aes_key, aes_key),
                        'session_key': aes_encrypt(next_session_key, next_aes_key),
                        'error': 'No authorization'}), 201

    try:
        UsersDatabase.change_username(user_id, aes_decrypt(new_username, aes_key), aes_decrypt(password, aes_key))
    except UsernameIsBusy:
        return jsonify({'ciphertext': aes_encrypt(next_aes_key, aes_key),
                        'session_key': aes_encrypt(next_session_key, next_aes_key),
                        'error': 'Username is busy'}), 201
    except PasswordIsWrong:
        return jsonify({'ciphertext': aes_encrypt(next_aes_key, aes_key),
                        'session_key': aes_encrypt(next_session_key, next_aes_key),
                        'error': 'Password is wrong'}), 201
    except PasswordNotCorrect:
        return jsonify({'ciphertext': aes_encrypt(next_aes_key, aes_key),
                        'session_key': aes_encrypt(next_session_key, next_aes_key),
                        'error': 'Password format not corrected'}), 201
    except AttemptsLimit:
        return jsonify({'ciphertext': aes_encrypt(next_aes_key, aes_key),
                        'session_key': aes_encrypt(next_session_key, next_aes_key),
                        'error': 'Account block. Change the password!'}), 202
    else:
        return jsonify({'ciphertext': aes_encrypt(next_aes_key, aes_key),
                        'session_key': aes_encrypt(next_session_key, next_aes_key)}), 200


@app.route('/change_password', methods=['POST'])
def change_password():
    data = request.get_json()

    session_key = data.get('session_key')
    password = data.get('password')
    new_password = data.get('new_password')

    try:
        session_key, next_session_key, aes_key, next_aes_key = SessionsDatabase.update_session_key(session_key)[0]
    except SessionNotFound:
        return jsonify('Session not found!'), 404
    except SessionTimeoutExceeded:
        return jsonify('Session timeout exceeded!'), 408

    # проверка входа в акк?
    user_id = SessionsDatabase.get_user_id(next_session_key)
    if user_id is None:
        return jsonify({'ciphertext': aes_encrypt(next_aes_key, aes_key),
                        'session_key': aes_encrypt(next_session_key, next_aes_key),
                        'error': 'No authorization'}), 401

    try:
        UsersDatabase.change_password(user_id, aes_decrypt(new_password, aes_key), aes_decrypt(password, aes_key))
    except PasswordIsWrong:
        return jsonify({'ciphertext': aes_encrypt(next_aes_key, aes_key),
                        'session_key': aes_encrypt(next_session_key, next_aes_key),
                        'error': 'Password is wrong'}), 201
    except PasswordNotCorrect:
        return jsonify({'ciphertext': aes_encrypt(next_aes_key, aes_key),
                        'session_key': aes_encrypt(next_session_key, next_aes_key),
                        'error': 'Password format not corrected'}), 201
    except AttemptsLimit:
        return jsonify({'ciphertext': aes_encrypt(next_aes_key, aes_key),
                        'session_key': aes_encrypt(next_session_key, next_aes_key),
                        'error': 'Account block. Change the password!'}), 202
    else:
        return jsonify({'ciphertext': aes_encrypt(next_aes_key, aes_key),
                        'session_key': aes_encrypt(next_session_key, next_aes_key)}), 200


@app.route('/forgot_password', methods=['POST'])
def forgot_password():
    data = request.get_json()

    session_key = data.get('session_key')
    email = data.get('email')

    try:
        session_key, next_session_key, aes_key, next_aes_key = SessionsDatabase.update_session_key(session_key)[0]
    except SessionNotFound:
        return jsonify('Session not found!'), 404
    except SessionTimeoutExceeded:
        return jsonify('Session timeout exceeded!'), 408

    if UsersDatabase.user_exists_by_email(aes_decrypt(email, aes_key)):
        email = aes_decrypt(email, aes_key)
        code = generate_session_key(6)

        recipient = email
        subject = "Reset password"
        html_content = """
               <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <title>Reset password</title>
                        <style>
                            header{
                                background-color: #1e2026;
                                color: #f3c31e;
                                text-align: center;
                                font-size: 30px;
                            }
                            div{
                                padding-left: 5%;
                            }
                            .confirm{
                                font-weight: bold;
                                font-size: 30px;
                            }
                            .certificate{
                                font-weight: lighter;
                                font-size: 24px;
                            }
                            .code{
                                font-weight: bold;
                                font-size: 34px;
                                color: #f2b90b;
                            }
                            footer{
                                text-align: center;
                                font-size: 20px;
                            }
                        </style>
                    </head>
                    <body>
                        <header>
                            <h1>VWS</h1>
                        </header>
                        <main>
                            <div class="confirm"><p>Login confirmation</p></div>
                            <div class="certificate"><p>Your verification code:</p></div>
                            <div class="code"><p>""" + f'{code}' + """</p></div>

                        </main>
                        <footer>
                            © 2023 VWS, All Rights Reserved.
                        </footer>
                    </body>
                </html>
                    """

        message = Message(subject=subject, recipients=[recipient], html=html_content)
        mail.send(message)
        print('+')
        user_id = UsersDatabase.get_user_id_by_email(email)
        EmailsCodesDatabase.create(next_session_key, code, user_id)
        return jsonify({'ciphertext': aes_encrypt(next_aes_key, aes_key),
                        'session_key': aes_encrypt(next_session_key, next_aes_key)}), 200
    return jsonify({'ciphertext': aes_encrypt(next_aes_key, aes_key),
                    'session_key': aes_encrypt(next_session_key, next_aes_key),
                    'error': 'Email not found!'}), 201


@app.route('/confirm_forgot_password', methods=['POST'])
def confirm_forgot_password():
    data = request.get_json()

    session_key = data.get('session_key')
    code = data.get('code')
    try:
        session_key, next_session_key, aes_key, next_aes_key = SessionsDatabase.update_session_key(session_key)[0]
    except SessionNotFound:
        return jsonify('Session not found!'), 404
    except SessionTimeoutExceeded:
        return jsonify('Session timeout exceeded!'), 408

    try:
        user_id = EmailsCodesDatabase.check(session_key, aes_decrypt(code, aes_key))
    except SessionNotFound:
        return jsonify({'ciphertext': aes_encrypt(next_aes_key, aes_key),
                        'session_key': aes_encrypt(next_session_key, next_aes_key),
                        'error': 'Invalid session'}), 201

    if user_id:
        ChangePasswordDatabase.create(next_session_key, user_id)
        return jsonify({'ciphertext': aes_encrypt(next_aes_key, aes_key),
                        'session_key': aes_encrypt(next_session_key, next_aes_key)}), 200

    return jsonify({'ciphertext': aes_encrypt(next_aes_key, aes_key),
                    'session_key': aes_encrypt(next_session_key, next_aes_key),
                    'error': 'Invalid code'}), 201


@app.route('/final_forgot_password', methods=['POST'])
def final_forgot_password():
    data = request.get_json()

    session_key = data.get('session_key')
    new_password = data.get('new_password')

    try:
        session_key, next_session_key, aes_key, next_aes_key = SessionsDatabase.update_session_key(session_key)[0]
    except SessionNotFound:
        return jsonify('Session not found!'), 404
    except SessionTimeoutExceeded:
        return jsonify('Session timeout exceeded!'), 408

    try:
        user_id = ChangePasswordDatabase.check(session_key)
        ChangePasswordDatabase.delete(session_key)
    except SessionNotFound:
        return jsonify({'ciphertext': aes_encrypt(next_aes_key, aes_key),
                        'session_key': aes_encrypt(next_session_key, next_aes_key),
                        'error': 'Invalid session'}), 201
    try:
        UsersDatabase.forgot_password(user_id, aes_decrypt(new_password, aes_key))
    except PasswordNotCorrect:
        return jsonify({'ciphertext': aes_encrypt(next_aes_key, aes_key),
                        'session_key': aes_encrypt(next_session_key, next_aes_key),
                        'error': 'Password format not corrected'}), 201
    return jsonify({'ciphertext': aes_encrypt(next_aes_key, aes_key),
                    'session_key': aes_encrypt(next_session_key, next_aes_key)}), 200


if __name__ == "__main__":
    app.run()
