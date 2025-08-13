package com.backend.gogreen.common.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum ErrorStatus {

    /**
     * 400 BAD REQUEST
     */

    /**
     * 401 UNAUTHORIZED
     */

    /**
     * 403 FORBIDDEN
     */

    /**
     * 404 NOT FOUND
     */

    /**
     * 500 SERVER ERROR
     */

    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode() {
        return this.httpStatus.value();
    }
}
