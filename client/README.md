<div align="center">

# Client VWS
[![GPLv3 License](https://img.shields.io/badge/License-GPL%20v3-yellow.svg)](LICENSE)  
</div>

# Table of contents
1. [Screenshots](#Screenshots)
2. [Tech Stack](#Stack)
3. [Features](#features)
4. [Run Locally](#run)
5. [Environment Variables](#environment)
6. [Feedback](#Feedback)
7. [License](#License)
<div id="Screenshots">

## Screenshots

### Login:
![App Screenshot](../img/login.png)

### Register:
![App Screenshot](../img/registration.png)


### Menu:
![App Screenshot](../img/menu.png)

</div>
<div id="Stack">

## Tech Stack

**Client:** Java, JavaFX

</div>
<div id="Features">

## Features

- Authorizing user
- Registration new user
- Changing password
- Changing username
- Password recovery
- Hiding info in video
- Protection against video interception by another user
</div>
<div id="run">

## Run Locally

Clone the project

~~~bash
git clone https://github.com/ka9mal6t/secret_info_in_video.git
~~~

Go to the project directory

~~~bash
cd client/vws
~~~

Install dependencies 

~~~bash
mvn install
~~~

Start the client

~~~bash
mvn javafx:run
~~~
</div>
<div id="environment">

## Environment Variables

To run this project, you will need to edit the following environment variables to your Config.java file

`url`
</div>
<div id="Feedback">

## Feedback

If you have any feedback, please reach out to us at vladimyr.kilko@gmail.com
</div>
<div id="License">

## License

[GPLv3](LICENSE)
</div>