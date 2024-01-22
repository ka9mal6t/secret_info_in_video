<div align="center">

# Server (API) VWS
[![GPLv3 License](https://img.shields.io/badge/License-GPL%20v3-yellow.svg)](LICENSE)  
</div>

# Table of contents
1. [Tech Stack](#Stack)
2. [Features](#features)
3. [Run Locally](#run)
4. [Environment Variables](#Environment)
5. [API Reference](#Reference)
6. [Feedback](#Feedback)
7. [License](#License)

<div id="Stack">

## Tech Stack

**Server:** Python, Flask, SQLite
</div>
<div id="Features">

## Features

- Additional protection of the communication session with the server
- Authorizing user
- Registration new user
- Changing password
- Changing username
- Password recovery
- Encrypting messages
- Decrypting messages
</div>
<div id="run">

## Run Locally

Clone the project

~~~bash
git clone https://github.com/ka9mal6t/secret_info_in_video.git
~~~

Go to the project directory

~~~bash
cd server
~~~


Install dependencies

~~~bash
pip install -r requirements.txt
~~~

Create database

~~~bash
python create_database.py
~~~

Start the server

~~~bash
python main.py
~~~
</div>
<div id="Environment">

## Environment Variables

To run this project, you will need to edit the following environment variables to your config.py file

`MAIL_SERVER`

`MAIL_PORT`

`MAIL_USE_TLS`

`MAIL_USE_SSL`

`MAIL_USERNAME`

`MAIL_PASSWORD`

`MAIL_DEFAULT_SENDER`
</div>
<div id="Reference">

## API Reference
[What is `session_key`, `public_key`?](../img/scheme.png)
#### Return session key and key (AES encryption)  

```http
  POST /create_session_key
```  

| Parameter    | Type     | Description                                  |
|:-------------|:---------|:---------------------------------------------|
| `public_key` | `string` | **Required**. Public key from RSA encryption |

#### Register new user

~~~http
  POST /register_user
~~~

| Parameter     | Type     | Description   |
|:--------------|:---------|:--------------|
| `session_key` | `string` | **Required**. |
| `username`    | `string` | **Required**. |  
| `email`       | `string` | **Required**. |
| `password`    | `string` | **Required**. |  



#### Login in account

~~~http
  POST /login_user
~~~

| Parameter     | Type     | Description   |
|:--------------|:---------|:--------------|
| `session_key` | `string` | **Required**. | 
| `email`       | `string` | **Required**. |
| `password`    | `string` | **Required**. |  

#### Encrypt message for user

~~~http
  POST /encrypt/<string:username>
~~~

| Parameter     | Type     | Description                                                     |
|:--------------|:---------|:----------------------------------------------------------------|
| `session_key` | `string` | **Required**.                                                   | 
| `username`    | `string` | **Required**. User's username for whom the message is encrypted |
| `message`     | `string` | **Required**.                                                   |  

#### Decrypt message

~~~http
  POST /decrypt
~~~

| Parameter     | Type     | Description   |
|:--------------|:---------|:--------------|
| `session_key` | `string` | **Required**. | 
| `message`     | `string` | **Required**. |  

#### Change username

~~~http
  POST /change_username
~~~

| Parameter      | Type     | Description   |
|:---------------|:---------|:--------------|
| `session_key`  | `string` | **Required**. | 
| `new_username` | `string` | **Required**. |  
| `password`     | `string` | **Required**. |  

#### Decrypt message

~~~http
  POST /change_password
~~~

| Parameter      | Type     | Description   |
|:---------------|:---------|:--------------|
| `session_key`  | `string` | **Required**. | 
| `password`     | `string` | **Required**. |  
| `new_password` | `string` | **Required**. |  

#### Recovery account (step 1)

~~~http
  POST /forgot_password
~~~

| Parameter     | Type     | Description   |
|:--------------|:---------|:--------------|
| `session_key` | `string` | **Required**. | 
| `email`       | `string` | **Required**. |  

#### Recovery account (step 2)

~~~http
  POST /confirm_forgot_password
~~~

| Parameter     | Type     | Description                                        |
|:--------------|:---------|:---------------------------------------------------|
| `session_key` | `string` | **Required**.                                      | 
| `code`        | `string` | **Required**. Code will be sending on user's email |  

#### Recovery account (step 3)

~~~http
  POST /final_forgot_password
~~~

| Parameter      | Type     | Description   |
|:---------------|:---------|:--------------|
| `session_key`  | `string` | **Required**. | 
| `new_password` | `string` | **Required**. |  
</div>
<div id="Feedback">

## Feedback

If you have any feedback, please reach out to us at vladimyr.kilko@gmail.com
</div>
<div id="License">

## License

[GPLv3](LICENSE)
</div>
