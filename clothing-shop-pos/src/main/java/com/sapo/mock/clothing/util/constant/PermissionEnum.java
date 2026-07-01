package com.sapo.mock.clothing.util.constant;

public enum PermissionEnum {
    // Sản phẩm & Danh mục
    VIEW_PRODUCT,
    MANAGE_PRODUCT,
    VIEW_CATEGORY,
    MANAGE_CATEGORY,

    // Bán hàng & Đơn hàng
    CREATE_ORDER,
    VIEW_ORDER,
    CANCEL_ORDER,

    // Trả hàng
    CREATE_RETURN,
    VIEW_RETURN,

    // Khách hàng & CRM
    VIEW_CUSTOMER,
    MANAGE_CUSTOMER,
    VIEW_CAMPAIGN,
    MANAGE_CAMPAIGN,

    // Nhập kho & Nhà cung cấp
    VIEW_RECEIPT,
    MANAGE_RECEIPT,
    VIEW_SUPPLIER,
    MANAGE_SUPPLIER,

    // Ca làm việc
    VIEW_SHIFT,
    MANAGE_SHIFT,

    // AI & Khác
    VIEW_REPORT,
    MANAGE_AI_RECOMMENDATION,

    // Phân quyền & Admin
    MANAGE_USER,
    MANAGE_ROLE
}
