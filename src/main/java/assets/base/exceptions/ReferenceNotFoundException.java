package assets.base.exceptions;

public class ReferenceNotFoundException extends Exception {

    private final ReferenceType type;
    private long id;
    
    public ReferenceNotFoundException(ReferenceType type) {
        this.type = type;
    }

    public ReferenceNotFoundException(ReferenceType type, String message) {
        super(message);
        this.type = type;
    }

    public ReferenceNotFoundException(ReferenceType type, Throwable cause) {
        super(cause);
        this.type = type;
    }

    public ReferenceNotFoundException(ReferenceType type, String message, Throwable cause) {
        super(message, cause);
        this.type = type;
    }

    public ReferenceNotFoundException(ReferenceType type, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.type = type;
    }
    
    public ReferenceType getReferenceType() {
        return type;
    }
    
    public long getReferenceId() {
        return id;
    }
    
    public ReferenceNotFoundException setReferenceId(long id) {
        this.id = id;
        return this;
    }
    
    public static enum ReferenceType {
        GUILD,
        USER,
        MEMBER,
        ROLE,
        TEXT_CHANNEL,
        VOICE_CHANNEL,
        PRIVATE_CHANNEL,
        CATEGORY,
        MESSAGE;
    }
}