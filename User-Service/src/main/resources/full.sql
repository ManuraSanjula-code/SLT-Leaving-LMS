CREATE TABLE temp_user (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,  -- Auto-incremented primary key
                           user_id VARCHAR(255) NOT NULL UNIQUE,  -- Unique user ID
                           password VARCHAR(255) NOT NULL,        -- Hashed password (salt:hashed_password)
                           isNew BOOLEAN NOT NULL DEFAULT TRUE,   -- Flag to indicate if the user is new
                           first_name VARCHAR(255) NOT NULL,      -- First name
                           last_name VARCHAR(255) NOT NULL,       -- Last name
                           email VARCHAR(255) NOT NULL,           -- Email address
                           peo_tv_id VARCHAR(255) NOT NULL,       -- Custom identifier
                           expire_time DATETIME NOT NULL,         -- Expiration time (2 days from creation)
                           password_en BOOLEAN NOT NULL DEFAULT FALSE -- Password enabled flag
);

DELIMITER //

CREATE PROCEDURE CreateTempUser(
    IN p_id BIGINT,
    IN p_user_id VARCHAR(255),
    IN p_password VARCHAR(255),
    IN p_first_name VARCHAR(255),
    IN p_last_name VARCHAR(255),
    IN p_email VARCHAR(255),
    IN p_peo_tv_id VARCHAR(255)
)
BEGIN
    DECLARE salt VARCHAR(32);
    DECLARE hashed_password VARCHAR(128);

    -- Generate a 32-character salt
    SET salt = REPLACE(UUID(), '-', '');

    -- Hash the password with the salt using SHA-512
    SET hashed_password = SHA2(CONCAT(p_password, salt), 512);

    -- Insert the new user with hashed password, salt, passwordEn set to TRUE, and expire_time set to 2 days from now
    INSERT INTO temp_user (id, user_id, password, first_name, last_name, email, peo_tv_id, expire_time, password_en, isNew)
    VALUES (p_id, p_user_id, CONCAT(salt, ':', hashed_password), p_first_name, p_last_name, p_email, p_peo_tv_id, DATE_ADD(NOW(), INTERVAL 2 DAY), TRUE, TRUE);
END //

DELIMITER ;

-- ++++++++++++++++++++++

DELIMITER //

CREATE TRIGGER hash_password_before_insert
    BEFORE INSERT ON temp_user
    FOR EACH ROW
BEGIN
    DECLARE salt VARCHAR(32);
    IF NEW.password_en THEN
        -- Generate a 32-character salt
        SET salt = REPLACE(UUID(), '-', '');

        -- Hash the password with the salt using SHA-512
        SET NEW.password = CONCAT(
                salt,
                ':',
                SHA2(CONCAT(NEW.password, salt), 512)
                           );
    END IF;
END; //

DELIMITER ;

-- ++++++++++++++++++++++

DELIMITER //

CREATE EVENT DeleteExpiredTempUsers
    ON SCHEDULE EVERY 1 DAY
        STARTS CURRENT_TIMESTAMP
    DO
    BEGIN
        DELETE FROM temp_user WHERE expire_time < NOW();
    END //

DELIMITER ;

CALL CreateTempUser(1,'user123', 'securepassword', 'John', 'Doe', 'john.doe@example.com', 'peo123');

DELIMITER //

CREATE TRIGGER prevent_direct_insert
    BEFORE INSERT ON temp_user
    FOR EACH ROW
BEGIN
    -- Prevent direct inserts
    SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Direct inserts are not allowed. Use the CreateTempUser procedure.';
END //

-- =================
-- =================

DELIMITER //

CREATE TRIGGER prevent_direct_update
    BEFORE UPDATE ON temp_user
    FOR EACH ROW
BEGIN
    -- Prevent direct updates to passwordEn and expire_time
    IF NEW.password_en <> OLD.password_en OR NEW.expire_time <> OLD.expire_time THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Direct updates to passwordEn or expire_time are not allowed.';
    END IF;
END //

DELIMITER ;


-- =================
-- =================

DELIMITER //

CREATE PROCEDURE CreateTempUser(
    IN p_user_id VARCHAR(255),
    IN p_password VARCHAR(255),
    IN p_first_name VARCHAR(255),
    IN p_last_name VARCHAR(255),
    IN p_email VARCHAR(255),
    IN p_peo_tv_id VARCHAR(255)
)
BEGIN
    DECLARE salt VARCHAR(32);
    DECLARE hashed_password VARCHAR(128);

    -- Generate a 32-character salt
    SET salt = REPLACE(UUID(), '-', '');

    -- Hash the password with the salt using SHA-512
    SET hashed_password = SHA2(CONCAT(p_password, salt), 512);

    -- Insert the new user with hashed password, salt, passwordEn set to TRUE, and expire_time set to 2 days from now
    INSERT INTO temp_user (user_id, password, first_name, last_name, email, peo_tv_id, expire_time, password_en)
    VALUES (p_user_id, CONCAT(salt, ':', hashed_password), p_first_name, p_last_name, p_email, p_peo_tv_id, DATE_ADD(NOW(), INTERVAL 2 DAY), TRUE);
END //

DELIMITER ;