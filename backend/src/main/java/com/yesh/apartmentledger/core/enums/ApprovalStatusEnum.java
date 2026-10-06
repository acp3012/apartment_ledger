package com.yesh.apartmentledger.core.enums;

import com.yesh.apartmentledger.exception.BadRequestException;
import lombok.Getter;

@Getter
public enum ApprovalStatusEnum {
    PENDING(1L),
    APPROVED(2L),
    REJECTED(3L);

    private final Long value;
    ApprovalStatusEnum(Long value) { this.value = value; }

    // Case-insensitive lookup method
    public static Long getValueByText(String text) {
        text = text.replace("\",","");
        for (ApprovalStatusEnum status : ApprovalStatusEnum.values()) {
            if (status.name().equalsIgnoreCase(text)) {
                return status.getValue();
            }
        }
        throw new BadRequestException("Unknown Approval status: " + text);
    }
}