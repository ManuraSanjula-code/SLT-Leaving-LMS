USE SLT;

DELIMITER //
CREATE TRIGGER hash_password_before_insert
    BEFORE INSERT ON temp_user
    FOR EACH ROW
BEGIN
    DECLARE salt VARCHAR(32);
    IF NEW.password_en THEN
        SET salt = REPLACE(UUID(), '-', ''); -- Generate 32-character salt
        SET NEW.password = CONCAT(
                salt,
                ':',
                SHA2(CONCAT(NEW.password, salt), 512)
                           );
    END IF;
END; //
DELIMITER ;


INSERT INTO temp_user
(id,user_id, password, is_new, first_name, last_name, email, peo_tv_id, expire_time, password_en)
VALUES
    (1,'12345', '12345', true, 'John', 'Doe','john@example.com', 'PTV123', '2025-12-31 23:59:59', true);


SELECT id, user_id, email, peo_tv_id, expire_time, is_new, password_en
FROM temp_user
WHERE email = 'john@example.com'
  AND (
    (password_en = TRUE
        AND password = CONCAT(
                SUBSTRING_INDEX(password, ':', 1),
                ':',
                SHA2(CONCAT('12345', SUBSTRING_INDEX(password, ':', 1)), 512)
                       ))
        OR
    (password_en = FALSE
        AND password = '12345')
    );

