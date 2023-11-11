package assets.base.exceptions;

public class DataNotFoundException extends Exception {
 
	private static final long serialVersionUID = -5748618338146782831L;
	public final String path, key;

    public DataNotFoundException(String path, String key) {
        this.path = path;
        this.key = key;
    }

    public DataNotFoundException(String message, String path, String key) {
        super(message);
        this.path = path;
        this.key = key;
    }

    public DataNotFoundException(Throwable cause, String path, String key) {
        super(cause);
        this.path = path;
        this.key = key;
    }

    public DataNotFoundException(String message, Throwable cause, String path, String key) {
        super(message, cause);
        this.path = path;
        this.key = key;
    }

    public DataNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, String path, String key) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.path = path;
        this.key = key;
    }
    
    public String getPath() {
        return this.path;
    }
    
    public String getKey() {
        return this.key;
    }
    
    public String getFullPath() {
        return this.path + ":" + this.key;
    }
}