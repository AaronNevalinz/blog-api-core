package com.blog_api_core.payload;


public class ApiResponse <T>{
    private int status;
    private String message;
    private T data;


    public ApiResponse(int status, T data) {
        this.status = status;
        this.data = data;
    }

    public static <T> ApiResponse<T> success( T data) {
        return new ApiResponse<>(200, data);
    }

    public static <T> ApiResponse<T> notFound(T data) {
        return new ApiResponse<>(404,  data);
    }

    public static <T> ApiResponse<T> validationErrorResponse(T data) {
        return new ApiResponse<> (403, data);
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
