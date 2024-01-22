from cryptography.hazmat.primitives import padding
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes
from cryptography.hazmat.backends import default_backend
import os
import base64
import binascii


def aes_generate_key():
    key = os.urandom(32)
    return binascii.hexlify(key).decode()


def aes_encrypt(message, key):
    key_bytes = binascii.unhexlify(key)
    cipher = Cipher(algorithms.AES(key_bytes), modes.ECB(), backend=default_backend())

    # PKCS7 padding
    padder = padding.PKCS7(algorithms.AES.block_size).padder()
    padded_data = padder.update(message.encode()) + padder.finalize()

    encryptor = cipher.encryptor()
    ciphertext = encryptor.update(padded_data) + encryptor.finalize()

    return base64.b64encode(ciphertext).decode()


def aes_decrypt(cipher_text, key):
    key_bytes = binascii.unhexlify(key)
    cipher_text_bytes = base64.b64decode(cipher_text)
    cipher = Cipher(algorithms.AES(key_bytes), modes.ECB(), backend=default_backend())

    decryptor = cipher.decryptor()
    decrypted_padded_data = decryptor.update(cipher_text_bytes) + decryptor.finalize()

    # PKCS7 unpadding
    unpadder = padding.PKCS7(algorithms.AES.block_size).unpadder()
    decrypted_data = unpadder.update(decrypted_padded_data) + unpadder.finalize()

    return decrypted_data.decode()
