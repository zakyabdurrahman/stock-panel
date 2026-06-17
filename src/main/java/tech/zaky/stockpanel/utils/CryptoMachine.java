package tech.zaky.stockpanel.utils;

import org.mindrot.jbcrypt.BCrypt;

public class CryptoMachine {
    public CryptoMachine() {

    }

    public static String hashPassword(String string) {
        return BCrypt.hashpw(string, BCrypt.gensalt());
    }

    public static Boolean checkPassword (String plain, String hashed) {
        return BCrypt.checkpw(plain, hashed);
    }
}
