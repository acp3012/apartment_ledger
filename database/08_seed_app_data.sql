--------------------------------
-- Application related seed 
---------------------------------

DO $$
DECLARE
    v_code           varchar(5)   := 'PAA';
    v_apartment_name varchar(200) := 'PREM AISHWARYAM & ABINAYAM';
    v_address        varchar(500) := 'PLOT A,B, 5th Cross street, Maxwroth nagar Phase-2, Kollapakkam';
    v_city           varchar(50)  := 'Chennai';
    v_state          varchar(100) := 'Tamilnadu';
    v_pincode        varchar(10)  := '600128';
    v_email          varchar(100) := lower(concat('admin', '@', v_code, '.com'));
    v_mobile         varchar(15)  := '9999999999';
    v_display_name   varchar(100) := 'Admin';
	v_password_hash  varchar(255) := '%pcX5908h*#,ikJ@8' ;
    v_apartment_id   bigint;
    v_user_id        bigint;
    v_role_id        bigint;
BEGIN
    SELECT role_id INTO STRICT v_role_id
    FROM master.role
    WHERE role_name = 'ADMIN';

    INSERT INTO core.apartment (apartment_code, apartment_name, address, city, state, pincode)
    VALUES (v_code, v_apartment_name, v_address, v_city, v_state, v_pincode)
    RETURNING apartment_id INTO v_apartment_id;

    INSERT INTO core.app_user (apartment_id, email, mobile_number, password_hash, display_name, is_active)
    VALUES (v_apartment_id, v_email, v_mobile,v_password_hash, v_display_name, true)
    RETURNING user_id INTO v_user_id;

    INSERT INTO core.user_role (user_id, role_id, is_active)
    VALUES (v_user_id, v_role_id, true);

    RAISE NOTICE 'Created apartment_id=%, user_id=%, role_id=%',
                 v_apartment_id, v_user_id, v_role_id;
END $$;


