CREATE TABLE config.application_setting
(
    setting_id          BIGSERIAL PRIMARY KEY,

    apartment_id        BIGINT NOT NULL,

    setting_key         VARCHAR(100) NOT NULL,

    setting_value       VARCHAR(500) NOT NULL,

    description         VARCHAR(500),

    is_active           BOOLEAN NOT NULL DEFAULT TRUE,

    created_by          BIGINT NOT NULL,

    created_date        TIMESTAMP NOT NULL
                        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_setting
        UNIQUE(apartment_id,setting_key),

    CONSTRAINT fk_setting_apartment
        FOREIGN KEY(apartment_id)
        REFERENCES core.apartment(apartment_id),

    CONSTRAINT fk_setting_created_by
        FOREIGN KEY(created_by)
        REFERENCES core.app_user(user_id)
);

