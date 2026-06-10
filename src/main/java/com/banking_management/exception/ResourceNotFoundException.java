package com.banking_management.exception;

/**
 * Ném ra khi không tìm thấy resource trong DB (User, Account, Transaction, KycProfile...).
 * GlobalExceptionHandler -> HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceName;
    private final Object resourceId;

    public ResourceNotFoundException(String resourceName, Object resourceId) {
        super(String.format("%s not found with id: %s", resourceName, resourceId));
        this.resourceName = resourceName;
        this.resourceId = resourceId;
    }

    // Dùng khi chỉ cần message tuỳ ý
    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceName = null;
        this.resourceId = null;
    }

    public String getResourceName() { return resourceName; }
    public Object getResourceId()   { return resourceId; }
}