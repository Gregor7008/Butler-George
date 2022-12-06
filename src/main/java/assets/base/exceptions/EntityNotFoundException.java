package assets.base.exceptions;

public class EntityNotFoundException extends Exception {

    private final ReferenceType type;
    private final long id;
    
    public EntityNotFoundException(ReferenceType type) {
        this.type = type;
        this.id = 0L;
    }
    
    public EntityNotFoundException(ReferenceType type, long id) {
        this.type = type;
        this.id = id;
    }

    public EntityNotFoundException(ReferenceType type, String message) {
        super(message);
        this.type = type;
        this.id = 0L;
    }

    public EntityNotFoundException(ReferenceType type, String message, long id) {
        super(message);
        this.type = type;
        this.id = id;
    }
    
    public EntityNotFoundException(ReferenceType type, Throwable cause) {
        super(cause);
        this.type = type;
        this.id = 0L;
    }

    public EntityNotFoundException(ReferenceType type, Throwable cause, long id) {
        super(cause);
        this.type = type;
        this.id = id;
    }
    
    public EntityNotFoundException(ReferenceType type, String message, Throwable cause) {
        super(message, cause);
        this.type = type;
        this.id = 0L;
    }

    public EntityNotFoundException(ReferenceType type, String message, Throwable cause, long id) {
        super(message, cause);
        this.type = type;
        this.id = id;
    }
    
    public EntityNotFoundException(ReferenceType type, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.type = type;
        this.id = 0L;
    }

    public EntityNotFoundException(ReferenceType type, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, long id) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.type = type;
        this.id = id;
    }
    
    public ReferenceType getReferenceType() {
        return type;
    }
    
    public long getReferenceId() {
        return id;
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