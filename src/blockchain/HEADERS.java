package blockchain;

public enum HEADERS {
    LOGIN_SUCCESFULL((byte) 1),
    LOGIN_UNSUCCESFULL((byte) 2),
    REGISTRATION_SUCCESFULL((byte) 3),
    REGISTRATION_UNSUCCESFULL((byte) 4),
    HANDSHAKE_INIT((byte) 5),
    HANDSHAKE_OK((byte) 6),
    SESSION_KEY_VALID((byte) 7),
    SESSION_KEY_INVALID((byte) 8),
    LOGIN_SELECTED((byte) 9),
    REGISTRATION_SELECTED((byte) 10),
    NO_ENOUGH_MONEY((byte) 11),
    TRANSACTION_SUCCESFULL((byte) 12);
    public byte data;

    HEADERS(byte b) {
        data = b;
    }
}

