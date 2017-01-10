package controllers;

import org.jetbrains.annotations.NotNull;

import javax.json.JsonObject;


public class CustomResponse {
    public static final int OK = 0;
    public static final int NOT_FOUND = 1;
    public static final int INVALID_REQUEST = 2;
    public static final int INCORRECT_REQUEST = 3;
    public static final int UNKNOWN_ERROR = 4;
    public static final int ALREADY_EXIST = 5;

    @NotNull
    private int code;
    @NotNull
    private Object response;

    public CustomResponse() {
        response = "";
        code = -1;
    }

    @NotNull
    public Object getResponse() { return response; }
    public void setResponse(@NotNull Object response) { this.response = response; }
    @NotNull
    public int getCode() { return code; }
    public void setCode(@NotNull int code) { this.code = code; }
}
