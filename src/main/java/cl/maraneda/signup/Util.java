package cl.maraneda.signup;

import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.regex.Pattern;

public class Util {
    public static final Pattern PASSWORD_PATTERN =
        Pattern.compile("^(?=[a-z0-9]*[A-Z][a-z0-9]*$)(?=[a-zA-Z]*[0-9][a-zA-Z]*[0-9][a-zA-Z]*$)[a-zA-Z0-9]{8,12}$");
    public static final Pattern JWT_PATTERN =
        Pattern.compile("^[A-Za-z0-9\\-_]+\\.[A-Za-z0-9\\-_]+\\.[A-Za-z0-9\\-_]+$");

    public static SecretKey getJWTSignatureFromString(String word) throws InvalidKeySpecException, NoSuchAlgorithmException {
        char[] wordChars = word.toCharArray();
        byte[] salt = "genericSalt".getBytes();
        int iterations = 10000;
        int keyLength = 256; // 32 bytes for HS256

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spec = new PBEKeySpec(wordChars, salt, iterations, keyLength);
        byte[] derivedKey = factory.generateSecret(spec).getEncoded();

        return Keys.hmacShaKeyFor(derivedKey);
    }
}
