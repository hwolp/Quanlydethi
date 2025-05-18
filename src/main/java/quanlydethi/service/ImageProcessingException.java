package quanlydethi.service; // Hoặc package bạn muốn đặt lớp Exception

public class ImageProcessingException extends Exception {

    public ImageProcessingException(String message) {
        super(message);
    }

    public ImageProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}