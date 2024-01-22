package com.ka9mal6t.vws;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;

public class RequestToServer {
    public static ServerAnswer start() {
        try {
            URL url = new URL(Config.url + "/create_session_key");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();


            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);


            Security.addProvider(new BouncyCastleProvider());
            KeyPair keyPair = RsaEncryption.generateRSAKeyPair();

            assert keyPair != null;
            String publicPEM = RsaEncryption.serializePublicKey(keyPair.getPublic());
            assert publicPEM != null;
            byte[] byteArray = publicPEM.getBytes();
            String publicPEMHex = RsaEncryption.bytesToHex(byteArray);

            Map<String, String> requestData = new HashMap<>();
            requestData.put("public_key", publicPEMHex);
            String jsonInputString = "{\"public_key\":\"" + requestData.get("public_key") + "\"}";


            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Map<String, String> responseData = new ObjectMapper()
                        .readValue(connection.getInputStream(), new TypeReference<Map<String, String>>() {
                        });

                String ciphertext;
                String sessionKeyEncrypted;
                try {
                    sessionKeyEncrypted = responseData.get("session_key");
                    ciphertext = responseData.get("ciphertext");


                } catch (NullPointerException e) {
                    System.out.println("Success, but error start");
                    return null;
                }

                String decryptedKey = RsaEncryption.decrypt(ciphertext, keyPair.getPrivate());

                String decryptedSession = AesDecryptor.decrypt(sessionKeyEncrypted, decryptedKey);

                return new ServerAnswer(200, decryptedSession, decryptedKey);

            } else {
                return new ServerAnswer(404, "", "", "Server error");
            }

        } catch (Exception e) {
            return new ServerAnswer(404, "", "", "Bad connection");
        }
    }


    public static ServerAnswer login(String email, String password, ServerAnswer sa) {
        try {

            URL url = new URL(Config.url + "/login_user");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();


            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);


            assert sa != null;
            String emailEncrypted = AesEncryptor.encrypt(email, sa.getAesKey());
            String passwordEncrypted = AesEncryptor.encrypt(password, sa.getAesKey());

            Map<String, String> requestData = new HashMap<>();
            requestData.put("session_key", sa.getSessionKey());
            requestData.put("email", emailEncrypted);
            requestData.put("password", passwordEncrypted);
            String jsonInputString = "{\"session_key\": \"" + requestData.get("session_key") + "\", \"email\": \"" + requestData.get("email") + "\", \"password\": \"" + requestData.get("password") + "\"}";


            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {

                Map<String, String> responseData = new ObjectMapper()
                        .readValue(connection.getInputStream(), new TypeReference<Map<String, String>>() {
                        });

                String ciphertext;
                String sessionKeyEncrypted;
                try {
                    sessionKeyEncrypted = responseData.get("session_key");
                    ciphertext = responseData.get("ciphertext");

                    String new_key = AesDecryptor.decrypt(ciphertext, sa.getAesKey());
                    String sessionKey = AesDecryptor.decrypt(sessionKeyEncrypted, new_key);

                    return new ServerAnswer(200, sessionKey, new_key);

                } catch (NullPointerException e) {
                    System.out.println("Success, but error start");
                    return null;
                }

            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                return new ServerAnswer(404, "", "", "Session not found!");
            } else if (responseCode == HttpURLConnection.HTTP_CLIENT_TIMEOUT) {
                return new ServerAnswer(408, "", "", "Session timeout exceeded!");
            } else if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_ACCEPTED) {
                Map<String, String> responseData = new ObjectMapper()
                        .readValue(connection.getInputStream(), new TypeReference<Map<String, String>>() {
                        });
                String ciphertext;
                String sessionKeyEncrypted;
                String error;
                try {
                    sessionKeyEncrypted = responseData.get("session_key");
                    ciphertext = responseData.get("ciphertext");
                    error = responseData.get("error");

                    String new_key = AesDecryptor.decrypt(ciphertext, sa.getAesKey());
                    String sessionKey = AesDecryptor.decrypt(sessionKeyEncrypted, new_key);

                    return new ServerAnswer(201, sessionKey, new_key, error);

                } catch (NullPointerException e) {
                    System.out.println("Success, but error start");
                    return null;
                }

            }else {
                return new ServerAnswer(404, "", "", "Server error");
            }

        } catch (Exception e) {
            return new ServerAnswer(404, "", "", "Bad connection");
        }
    }
    public static ServerAnswer register(String username, String email, String password, ServerAnswer sa) {
        try {

            URL url = new URL(Config.url + "/register_user");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();


            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);


            assert sa != null;
            String emailEncrypted = AesEncryptor.encrypt(email, sa.getAesKey());
            String passwordEncrypted = AesEncryptor.encrypt(password, sa.getAesKey());
            String usernameEncrypted = AesEncryptor.encrypt(username, sa.getAesKey());

            Map<String, String> requestData = new HashMap<>();
            requestData.put("session_key", sa.getSessionKey());
            requestData.put("email", emailEncrypted);
            requestData.put("password", passwordEncrypted);
            requestData.put("username", usernameEncrypted);
            String jsonInputString = "{\"session_key\": \"" + requestData.get("session_key") + "\", \"email\": \"" + requestData.get("email") + "\", \"password\": \"" + requestData.get("password") + "\", \"username\": \"" + requestData.get("username") + "\"}";


            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {

                Map<String, String> responseData = new ObjectMapper()
                        .readValue(connection.getInputStream(), new TypeReference<Map<String, String>>() {
                        });

                String ciphertext;
                String sessionKeyEncrypted;
                try {
                    sessionKeyEncrypted = responseData.get("session_key");
                    ciphertext = responseData.get("ciphertext");

                    String new_key = AesDecryptor.decrypt(ciphertext, sa.getAesKey());
                    String sessionKey = AesDecryptor.decrypt(sessionKeyEncrypted, new_key);

                    return new ServerAnswer(200, sessionKey, new_key);

                } catch (NullPointerException e) {
                    System.out.println("Success, but error start");
                    return null;
                }

            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                return new ServerAnswer(404, "", "", "Session not found!");
            } else if (responseCode == HttpURLConnection.HTTP_CLIENT_TIMEOUT) {
                return new ServerAnswer(408, "", "", "Session timeout exceeded!");
            } else if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_ACCEPTED) {
                Map<String, String> responseData = new ObjectMapper()
                        .readValue(connection.getInputStream(), new TypeReference<Map<String, String>>() {
                        });
                String ciphertext;
                String sessionKeyEncrypted;
                String error;
                try {
                    sessionKeyEncrypted = responseData.get("session_key");
                    ciphertext = responseData.get("ciphertext");
                    error = responseData.get("error");

                    String new_key = AesDecryptor.decrypt(ciphertext, sa.getAesKey());
                    String sessionKey = AesDecryptor.decrypt(sessionKeyEncrypted, new_key);

                    return new ServerAnswer(201, sessionKey, new_key, error);

                } catch (NullPointerException e) {
                    System.out.println("Success, but error start");
                    return null;
                }

            }else {
                return new ServerAnswer(404, "", "", "Server error");
            }

        } catch (Exception e) {
            return new ServerAnswer(404, "", "", "Bad connection");
        }
    }

    public static ServerAnswer change_username(String newUsername, String password, ServerAnswer sa) {
        try {

            URL url = new URL(Config.url + "/change_username");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();


            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            assert sa != null;
            String newUsernameEncrypted = AesEncryptor.encrypt(newUsername, sa.getAesKey());
            String passwordEncrypted = AesEncryptor.encrypt(password, sa.getAesKey());

            Map<String, String> requestData = new HashMap<>();
            requestData.put("session_key", sa.getSessionKey());
            requestData.put("password", passwordEncrypted);
            requestData.put("new_username", newUsernameEncrypted);
            String jsonInputString = "{\"session_key\": \"" + requestData.get("session_key") + "\", \"password\": \"" + requestData.get("password") + "\", \"new_username\": \"" + requestData.get("new_username") + "\"}";

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {

                Map<String, String> responseData = new ObjectMapper()
                        .readValue(connection.getInputStream(), new TypeReference<Map<String, String>>() {
                        });

                String ciphertext;
                String sessionKeyEncrypted;
                try {
                    sessionKeyEncrypted = responseData.get("session_key");
                    ciphertext = responseData.get("ciphertext");

                    String new_key = AesDecryptor.decrypt(ciphertext, sa.getAesKey());
                    String sessionKey = AesDecryptor.decrypt(sessionKeyEncrypted, new_key);

                    return new ServerAnswer(200, sessionKey, new_key);

                } catch (NullPointerException e) {
                    System.out.println("Success, but error start");
                    return null;
                }

            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                return new ServerAnswer(404, "", "", "Session not found!");
            } else if (responseCode == HttpURLConnection.HTTP_CLIENT_TIMEOUT) {
                return new ServerAnswer(408, "", "", "Session timeout exceeded!");
            } else if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_ACCEPTED) {
                Map<String, String> responseData = new ObjectMapper()
                        .readValue(connection.getInputStream(), new TypeReference<Map<String, String>>() {
                        });
                String ciphertext;
                String sessionKeyEncrypted;
                String error;
                try {
                    sessionKeyEncrypted = responseData.get("session_key");
                    ciphertext = responseData.get("ciphertext");
                    error = responseData.get("error");

                    String new_key = AesDecryptor.decrypt(ciphertext, sa.getAesKey());
                    String sessionKey = AesDecryptor.decrypt(sessionKeyEncrypted, new_key);

                    return new ServerAnswer(201, sessionKey, new_key, error);

                } catch (NullPointerException e) {
                    System.out.println("Success, but error start");
                    return null;
                }

            }else {
                return new ServerAnswer(404, "", "", "Server error");
            }

        } catch (Exception e) {
            return new ServerAnswer(404, "", "", "Bad connection");
        }
    }
    public static ServerAnswer change_password(String newPassword, String password, ServerAnswer sa) {
        try {

            URL url = new URL(Config.url + "/change_password");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            assert sa != null;
            String newPasswordEncrypted = AesEncryptor.encrypt(newPassword, sa.getAesKey());
            String passwordEncrypted = AesEncryptor.encrypt(password, sa.getAesKey());

            Map<String, String> requestData = new HashMap<>();
            requestData.put("session_key", sa.getSessionKey());
            requestData.put("password", passwordEncrypted);
            requestData.put("new_password", newPasswordEncrypted);
            String jsonInputString = "{\"session_key\": \"" + requestData.get("session_key") + "\", \"password\": \"" + requestData.get("password") + "\", \"new_password\": \"" + requestData.get("new_password") + "\"}";

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read and process the response
                // Make sure to replace ObjectMapper with your preferred JSON library
                Map<String, String> responseData = new ObjectMapper()
                        .readValue(connection.getInputStream(), new TypeReference<Map<String, String>>() {
                        });

                String ciphertext;
                String sessionKeyEncrypted;
                try {
                    sessionKeyEncrypted = responseData.get("session_key");
                    ciphertext = responseData.get("ciphertext");

                    String new_key = AesDecryptor.decrypt(ciphertext, sa.getAesKey());
                    String sessionKey = AesDecryptor.decrypt(sessionKeyEncrypted, new_key);

                    return new ServerAnswer(200, sessionKey, new_key);

                } catch (NullPointerException e) {
                    System.out.println("Success, but error start");
                    return null;
                }

            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                return new ServerAnswer(404, "", "", "Session not found!");
            } else if (responseCode == HttpURLConnection.HTTP_CLIENT_TIMEOUT) {
                return new ServerAnswer(408, "", "", "Session timeout exceeded!");
            } else if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_ACCEPTED) {
                Map<String, String> responseData = new ObjectMapper()
                        .readValue(connection.getInputStream(), new TypeReference<Map<String, String>>() {
                        });
                String ciphertext;
                String sessionKeyEncrypted;
                String error;
                try {
                    sessionKeyEncrypted = responseData.get("session_key");
                    ciphertext = responseData.get("ciphertext");
                    error = responseData.get("error");

                    String new_key = AesDecryptor.decrypt(ciphertext, sa.getAesKey());
                    String sessionKey = AesDecryptor.decrypt(sessionKeyEncrypted, new_key);

                    return new ServerAnswer(201, sessionKey, new_key, error);

                } catch (NullPointerException e) {
                    System.out.println("Success, but error start");
                    return null;
                }

            }else {
                return new ServerAnswer(404, "", "", "Server error");
            }

        } catch (Exception e) {
            return new ServerAnswer(404, "", "", "Bad connection");
        }
    }
    public static ServerAnswer forgot_password(String email, ServerAnswer sa) {
        try {

            URL url = new URL(Config.url + "/forgot_password");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            assert sa != null;
            String emailEncrypted = AesEncryptor.encrypt(email, sa.getAesKey());

            Map<String, String> requestData = new HashMap<>();
            requestData.put("session_key", sa.getSessionKey());
            requestData.put("email", emailEncrypted);
            String jsonInputString = "{\"session_key\": \"" + requestData.get("session_key") + "\", \"email\": \"" + requestData.get("email") + "\"}";

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {

                Map<String, String> responseData = new ObjectMapper()
                        .readValue(connection.getInputStream(), new TypeReference<Map<String, String>>() {
                        });

                String ciphertext;
                String sessionKeyEncrypted;
                try {
                    sessionKeyEncrypted = responseData.get("session_key");
                    ciphertext = responseData.get("ciphertext");

                    String new_key = AesDecryptor.decrypt(ciphertext, sa.getAesKey());
                    String sessionKey = AesDecryptor.decrypt(sessionKeyEncrypted, new_key);

                    return new ServerAnswer(200, sessionKey, new_key);

                } catch (NullPointerException e) {
                    System.out.println("Success, but error start");
                    return null;
                }

            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                return new ServerAnswer(404, "", "", "Session not found!");
            } else if (responseCode == HttpURLConnection.HTTP_CLIENT_TIMEOUT) {
                return new ServerAnswer(408, "", "", "Session timeout exceeded!");
            } else if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_ACCEPTED) {
                Map<String, String> responseData = new ObjectMapper()
                        .readValue(connection.getInputStream(), new TypeReference<Map<String, String>>() {
                        });
                String ciphertext;
                String sessionKeyEncrypted;
                String error;
                try {
                    sessionKeyEncrypted = responseData.get("session_key");
                    ciphertext = responseData.get("ciphertext");
                    error = responseData.get("error");

                    String new_key = AesDecryptor.decrypt(ciphertext, sa.getAesKey());
                    String sessionKey = AesDecryptor.decrypt(sessionKeyEncrypted, new_key);

                    return new ServerAnswer(201, sessionKey, new_key, error);

                } catch (NullPointerException e) {
                    System.out.println("Success, but error start");
                    return null;
                }

            }else {
                return new ServerAnswer(404, "", "", "Server error");
            }

        } catch (Exception e) {
            return new ServerAnswer(404, "", "", "Bad connection");
        }
    }

    public static ServerAnswer confirm_forgot_password(String code, ServerAnswer sa) {
        try {

            URL url = new URL(Config.url + "/confirm_forgot_password");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();


            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);


            assert sa != null;
            String codeEncrypted = AesEncryptor.encrypt(code, sa.getAesKey());

            Map<String, String> requestData = new HashMap<>();
            requestData.put("session_key", sa.getSessionKey());
            requestData.put("code", codeEncrypted);
            String jsonInputString = "{\"session_key\": \"" + requestData.get("session_key") + "\", \"code\": \"" + requestData.get("code") + "\"}";


            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {

                Map<String, String> responseData = new ObjectMapper()
                        .readValue(connection.getInputStream(), new TypeReference<Map<String, String>>() {
                        });

                String ciphertext;
                String sessionKeyEncrypted;
                try {
                    sessionKeyEncrypted = responseData.get("session_key");
                    ciphertext = responseData.get("ciphertext");

                    String new_key = AesDecryptor.decrypt(ciphertext, sa.getAesKey());
                    String sessionKey = AesDecryptor.decrypt(sessionKeyEncrypted, new_key);

                    return new ServerAnswer(200, sessionKey, new_key);

                } catch (NullPointerException e) {
                    System.out.println("Success, but error start");
                    return null;
                }

            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                return new ServerAnswer(404, "", "", "Session not found!");
            } else if (responseCode == HttpURLConnection.HTTP_CLIENT_TIMEOUT) {
                return new ServerAnswer(408, "", "", "Session timeout exceeded!");
            } else if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_ACCEPTED) {
                Map<String, String> responseData = new ObjectMapper()
                        .readValue(connection.getInputStream(), new TypeReference<Map<String, String>>() {
                        });
                String ciphertext;
                String sessionKeyEncrypted;
                String error;
                try {
                    sessionKeyEncrypted = responseData.get("session_key");
                    ciphertext = responseData.get("ciphertext");
                    error = responseData.get("error");

                    String new_key = AesDecryptor.decrypt(ciphertext, sa.getAesKey());
                    String sessionKey = AesDecryptor.decrypt(sessionKeyEncrypted, new_key);

                    return new ServerAnswer(201, sessionKey, new_key, error);

                } catch (NullPointerException e) {
                    System.out.println("Success, but error start");
                    return null;
                }

            }else {
                return new ServerAnswer(404, "", "", "Server error");
            }

        } catch (Exception e) {
            return new ServerAnswer(404, "", "", "Bad connection");
        }
    }
    public static ServerAnswer final_forgot_password(String newPassword, ServerAnswer sa) {
        try {

            URL url = new URL(Config.url + "/final_forgot_password");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();


            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);


            assert sa != null;
            String newPasswordEncrypted = AesEncryptor.encrypt(newPassword, sa.getAesKey());

            Map<String, String> requestData = new HashMap<>();
            requestData.put("session_key", sa.getSessionKey());
            requestData.put("new_password", newPasswordEncrypted);
            String jsonInputString = "{\"session_key\": \"" + requestData.get("session_key") + "\", \"new_password\": \"" + requestData.get("new_password") + "\"}";


            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {

                Map<String, String> responseData = new ObjectMapper()
                        .readValue(connection.getInputStream(), new TypeReference<Map<String, String>>() {
                        });

                String ciphertext;
                String sessionKeyEncrypted;
                try {
                    sessionKeyEncrypted = responseData.get("session_key");
                    ciphertext = responseData.get("ciphertext");

                    String new_key = AesDecryptor.decrypt(ciphertext, sa.getAesKey());
                    String sessionKey = AesDecryptor.decrypt(sessionKeyEncrypted, new_key);

                    return new ServerAnswer(200, sessionKey, new_key);

                } catch (NullPointerException e) {
                    System.out.println("Success, but error start");
                    return null;
                }

            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                return new ServerAnswer(404, "", "", "Session not found!");
            } else if (responseCode == HttpURLConnection.HTTP_CLIENT_TIMEOUT) {
                return new ServerAnswer(408, "", "", "Session timeout exceeded!");
            } else if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_ACCEPTED) {
                Map<String, String> responseData = new ObjectMapper()
                        .readValue(connection.getInputStream(), new TypeReference<Map<String, String>>() {
                        });
                String ciphertext;
                String sessionKeyEncrypted;
                String error;
                try {
                    sessionKeyEncrypted = responseData.get("session_key");
                    ciphertext = responseData.get("ciphertext");
                    error = responseData.get("error");

                    String new_key = AesDecryptor.decrypt(ciphertext, sa.getAesKey());
                    String sessionKey = AesDecryptor.decrypt(sessionKeyEncrypted, new_key);

                    return new ServerAnswer(201, sessionKey, new_key, error);

                } catch (NullPointerException e) {
                    System.out.println("Success, but error start");
                    return null;
                }

            }else {
                return new ServerAnswer(404, "", "", "Server error");
            }

        } catch (Exception e) {
            return new ServerAnswer(404, "", "", "Bad connection");
        }
    }
    public static ServerAnswer encrypt(String username, String message, ServerAnswer sa) {
        try {

            URL url = new URL(Config.url + "/encrypt/" + username);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();


            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);


            assert sa != null;
            String messageEncrypted = AesEncryptor.encrypt(message, sa.getAesKey());

            Map<String, String> requestData = new HashMap<>();
            requestData.put("session_key", sa.getSessionKey());
            requestData.put("message", messageEncrypted);
            String jsonInputString = "{\"session_key\": \"" + requestData.get("session_key") + "\", \"message\": \"" + requestData.get("message") + "\"}";


            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {

                Map<String, String> responseData = new ObjectMapper()
                        .readValue(connection.getInputStream(), new TypeReference<Map<String, String>>() {
                        });

                String ciphertext;
                String sessionKeyEncrypted;
                String newMessageEncrypted;
                try {
                    sessionKeyEncrypted = responseData.get("session_key");
                    ciphertext = responseData.get("ciphertext");
                    newMessageEncrypted = responseData.get("message");

                    String new_key = AesDecryptor.decrypt(ciphertext, sa.getAesKey());
                    String sessionKey = AesDecryptor.decrypt(sessionKeyEncrypted, new_key);
                    String newMessageDecrypted = AesDecryptor.decrypt(newMessageEncrypted, new_key);

                    return new ServerAnswer(200, sessionKey, new_key, "", newMessageDecrypted);

                } catch (NullPointerException e) {
                    System.out.println("Success, but error start");
                    return null;
                }

            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                return new ServerAnswer(404, "", "", "Session not found!");
            } else if (responseCode == HttpURLConnection.HTTP_CLIENT_TIMEOUT) {
                return new ServerAnswer(408, "", "", "Session timeout exceeded!");
            } else if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_ACCEPTED) {
                Map<String, String> responseData = new ObjectMapper()
                        .readValue(connection.getInputStream(), new TypeReference<Map<String, String>>() {
                        });
                String ciphertext;
                String sessionKeyEncrypted;
                String error;
                try {
                    sessionKeyEncrypted = responseData.get("session_key");
                    ciphertext = responseData.get("ciphertext");
                    error = responseData.get("error");

                    String new_key = AesDecryptor.decrypt(ciphertext, sa.getAesKey());
                    String sessionKey = AesDecryptor.decrypt(sessionKeyEncrypted, new_key);

                    return new ServerAnswer(201, sessionKey, new_key, error);

                } catch (NullPointerException e) {
                    System.out.println("Success, but error start");
                    return null;
                }

            }else {
                return new ServerAnswer(404, "", "", "Server error");
            }

        } catch (Exception e) {
            return new ServerAnswer(404, "", "", "Bad connection");
        }
    }
    public static ServerAnswer decrypt(String message, ServerAnswer sa) {
        try {

            URL url = new URL(Config.url + "/decrypt");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();


            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);


            assert sa != null;
            String messageEncrypted = AesEncryptor.encrypt(message, sa.getAesKey());

            Map<String, String> requestData = new HashMap<>();
            requestData.put("session_key", sa.getSessionKey());
            requestData.put("message", messageEncrypted);
            String jsonInputString = "{\"session_key\": \"" + requestData.get("session_key") + "\", \"message\": \"" + requestData.get("message") + "\"}";


            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Map<String, String> responseData = new ObjectMapper()
                        .readValue(connection.getInputStream(), new TypeReference<Map<String, String>>() {
                        });

                String ciphertext;
                String sessionKeyEncrypted;
                String newMessage;
                try {
                    sessionKeyEncrypted = responseData.get("session_key");
                    ciphertext = responseData.get("ciphertext");
                    newMessage = responseData.get("message");

                    String new_key = AesDecryptor.decrypt(ciphertext, sa.getAesKey());
                    String sessionKey = AesDecryptor.decrypt(sessionKeyEncrypted, new_key);
                    String newMessageEncrypted = AesDecryptor.decrypt(newMessage, new_key);

                    return new ServerAnswer(200, sessionKey, new_key, "", newMessageEncrypted);

                } catch (NullPointerException e) {
                    System.out.println("Success, but error start");
                    return null;
                }

            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                return new ServerAnswer(404, "", "", "Session not found!");
            } else if (responseCode == HttpURLConnection.HTTP_CLIENT_TIMEOUT) {
                return new ServerAnswer(408, "", "", "Session timeout exceeded!");
            } else if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_ACCEPTED) {
                Map<String, String> responseData = new ObjectMapper()
                        .readValue(connection.getInputStream(), new TypeReference<Map<String, String>>() {
                        });
                String ciphertext;
                String sessionKeyEncrypted;
                String error;
                try {
                    sessionKeyEncrypted = responseData.get("session_key");
                    ciphertext = responseData.get("ciphertext");
                    error = responseData.get("error");

                    String new_key = AesDecryptor.decrypt(ciphertext, sa.getAesKey());
                    String sessionKey = AesDecryptor.decrypt(sessionKeyEncrypted, new_key);

                    return new ServerAnswer(201, sessionKey, new_key, error);

                } catch (NullPointerException e) {
                    System.out.println("Success, but error start");
                    return null;
                }

            }else {
                return new ServerAnswer(404, "", "", "Server error");
            }

        } catch (Exception e) {
            return new ServerAnswer(404, "", "", "Bad connection");
        }
    }
}