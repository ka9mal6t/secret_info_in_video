import secrets
import string


def generate_session_key(count):
    characters = string.ascii_letters + string.digits
    password = ''.join(secrets.choice(characters) for _ in range(count))
    return password
