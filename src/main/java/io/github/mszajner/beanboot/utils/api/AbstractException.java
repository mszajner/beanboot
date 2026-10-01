package io.github.mszajner.beanboot.utils.api;

import lombok.Getter;

@Getter
abstract public class AbstractException extends RuntimeException {

    protected static final int BAD_REQUEST = 400;
    protected static final int UNAUTHORIZED = 401;
    protected static final int FORBIDDEN = 403;
    protected static final int NOT_FOUND = 404;
    protected static final int METHOD_NOT_ALLOWED = 405;
    protected static final int NOT_ACCEPTABLE = 406;
    protected static final int REQUEST_TIMEOUT = 408;
    protected static final int CONFLICT = 409;
    protected static final int UNSUPPORTED_MEDIA_TYPE = 415;
    protected static final int TOO_MANY_REQUESTS = 429;

    protected static final int INTERNAL_SERVER_ERROR = 500;
    protected static final int NOT_IMPLEMENTED = 501;
    protected static final int BAD_GATEWAY = 502;
    protected static final int SERVICE_UNAVAILABLE = 503;
    protected static final int GATEWAY_TIMEOUT = 504;

    private final int statusCode;
    private final Object object1;
    private final Object object2;

    protected AbstractException(String message, int statusCode) {
        this(message, statusCode, null, null);
    }

    protected AbstractException(String message, int statusCode, Object object1) {
        this(message, statusCode, object1, null);
    }

    protected AbstractException(String message, int statusCode, Object object1, Object object2) {
        this(message, statusCode, object1, object2, null);
    }

    protected AbstractException(String message, int statusCode, Object object1, Object object2, Throwable e) {
        super(message, e);
        this.statusCode = statusCode;
        this.object1 = object1;
        this.object2 = object2;
    }
}
