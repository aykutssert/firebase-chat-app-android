package com.example.realtimechatapp.utils;

import android.util.Log;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.Lists;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

public class AccessToken {
    private static final String firebaseMessagingScope = "https://www.googleapis.com/auth/firebase.messaging";

    public  String getAccessToken() {
        try {

           String jsonString = "{\n" +
                   "  \"type\": \"service_account\",\n" +
                   "  \"project_id\": \"your_project_id\",\n" +
                   "  \"private_key_id\": \"your_private_id\",\n" +
                   "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEuwIBADANBgkqhkiG9w0BAQEFAASCBKUwggShAgEAAoIBAQCtH175dD4Hdx06\\nwAnc8vQxNBWxGlFuJLVVHK6TeNqA6G2TQ2faVGnDc48Muy7+lJMK7c5dd1qXOcSt\\nS/3daUBkpqpCNZp0SXh5HTRmkt8HS6Tqt2L8CkposMiQPysajjrN7NXRqQ123KcP\\nvbBtxL9f0G8D+Eztmc8DoyKQ6KSmk+bJEYF/ifgOk0gxsuZ0P42Ah4flpBV3MRfv\\ni5eCoawoH6oVimpIxPqS5c9FTJcwWbq/a/mlK1SFDOzhVguAcwI849Ou4Z17n99C\\ng2GUsLuLBNSDlRtBeRlIiHGe5jA/JiacK1CkHESqd6R9NkHXY8wSdZiiLL1Q8KQa\\nLeoXk+9fAgMBAAECggEAJa0Cl8o255wCFfOOEfIjt4OMxvahyxUdC3FLAtgVwWET\\n260bMFYXkEwZUR15bDj8STAYrZmYcvnE9ivoCyPpOh5VRkRxISaOG9mBumLzO5y4\\nPsZ7ObupluAGPfIpWMWkQYVtwQuH3RrHFrNi7niXZ2wuhNAoObsz5Kg/kQa4MnH4\\nepakdeEBruq+uRtuRcN2PlUMdT/znC+gWQgo4hpVvrE1XGg1u+bbWC/uK6abCdZD\\nrqg8P6KySWiCfLfiayHjQel/zRRDVOtSuMlkhZgkBWOL1b0OOEwMo3qtmY4UQubu\\nYSb5jE5/kWUUngJs8UyCeu5C/XfCTi2LQSqtp65j2QKBgQDo1XsR0TMT1foSo/Zg\\nFLV8q3ePIlBUtnnlGexiMDKFc1OjOlQmtxX/J/r7VQdQbaYBeSfmldDLlwtv2W62\\n5NA+svQE9xpEu1qGBP37WMhjDWQuCvbvEutTXkZUGi9bdlAhwtb9ixf+5u3MWPp6\\nT8vTVZRT8BffPEZFqq7YfKsvaQKBgQC+WPr1rh2bCOQQTYVAqgPi+dEpkgW9SUAt\\nUjvewedQi5YmwkU0P1lcMuJQRV8jRGEcwldMuVecTdAqfM8R2s1pu9b9Kz6aQNaH\\nJLIBp/VraJucjJRNIVRT9I4utaPxZ6D7qpw+SiI9q7C4092HeVoRpLK1qd/QV34y\\ne7P32b+XhwKBgG64Xg7p+a0cGxBUMiwcjPVdILzLJ4IxSCP/QN6sZsn5PrOnYC8M\\n74Pnwj1UgObpTYDz2VqYsbKvADxPHIwSoUi/lrTpwO8gXPrQUur8nEYmLxaKFkU4\\nLPn3IeCKlyyaRZ7YZZ4qT93ieQCs62fQnG/CCYgk62U3537nnL/MASfJAoGBALM3\\nA9IcxTMbA991nWhsqowhuZTyLwUw2oPRUR1CY4+qXQzRDGdM3c24+uuo5RJHZ0e0\\nko1iU+KOQNg/+pZM8+oBOUHUXXTOZy2GlzKH+MHQE/MpSNf18Xe6YcQIsPqxHxjU\\nYZckJWj7JcBfecv7rZm+/6Oa9WcreV3+qktJcOIxAn83gMQ7sRoYLTrcwJzzIuPG\\nayFolQ4oRK09S9/gZXT+VVNB7HPPyCvWgvUS+EpERmFLQmt5lclV0//5xf0wY+f1\\nbQvWJyRtvOtvo+ZiYD3vWC2brruK3e+FQrPXKEKzRPGgnCk1FBjSwX+kJ2uBmuVM\\n+zW0uyKXIf8xRlLyv1wE\\n-----END PRIVATE KEY-----\\n\",\n" +
                   "  \"client_email\": \"firebase-adminsdk-oo9y9@chat-app-d6748.iam.gserviceaccount.com\",\n" +
                   "  \"client_id\": \"115320927357946610349\",\n" +
                   "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                   "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                   "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                   "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-oo9y9%40chat-app-d6748.iam.gserviceaccount.com\",\n" +
                   "  \"universe_domain\": \"googleapis.com\"\n" +
                   "}\n";
            InputStream stream = new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));

            GoogleCredentials googleCredentials = GoogleCredentials.fromStream(stream)
                    .createScoped(Lists.newArrayList(firebaseMessagingScope));
            googleCredentials.refreshIfExpired();
            return googleCredentials.getAccessToken().getTokenValue();

        } catch (IOException e) {

            Log.e("Error Access:", "" + e.getMessage());
            return null;
        }


    }

}
