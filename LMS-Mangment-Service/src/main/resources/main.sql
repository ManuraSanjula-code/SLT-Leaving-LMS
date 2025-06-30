CREATE TABLE holidays (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    holiday_date DATE NOT NULL,
    description VARCHAR(255),
    is_recurring BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================

DELIMITER //

CREATE FUNCTION add_holiday(
    p_holiday_date DATE,
    p_description VARCHAR(255),
    p_is_recurring BOOLEAN
) 
RETURNS BIGINT
DETERMINISTIC
BEGIN
    DECLARE new_id BIGINT;
    
    INSERT INTO holidays (holiday_date, description, is_recurring)
    VALUES (p_holiday_date, p_description, p_is_recurring);
    
    SET new_id = LAST_INSERT_ID();
    
    RETURN new_id;
END //

DELIMITER ;


-- ==============================================

DELIMITER //

CREATE FUNCTION add_holiday_with_validation(
    p_holiday_date DATE,
    p_description VARCHAR(255),
    p_is_recurring BOOLEAN
) 
RETURNS VARCHAR(255)
DETERMINISTIC
BEGIN
    DECLARE holiday_exists INT;
    DECLARE result_message VARCHAR(255);
    
    -- Check if holiday already exists for this date
    SELECT COUNT(*) INTO holiday_exists
    FROM holidays
    WHERE holiday_date = p_holiday_date;
    
    -- Validate description is not empty
    IF p_description IS NULL OR p_description = '' THEN
        RETURN 'Error: Holiday description cannot be empty';
    END IF;
    
    -- Validate date is not in the past (if not recurring)
    IF p_holiday_date < CURDATE() AND p_is_recurring = FALSE THEN
        RETURN 'Error: Cannot add past dates for non-recurring holidays';
    END IF;
    
    -- Check for duplicate
    IF holiday_exists > 0 THEN
        RETURN 'Error: Holiday already exists for this date';
    END IF;
    
    -- Insert the new holiday
    INSERT INTO holidays (holiday_date, description, is_recurring)
    VALUES (p_holiday_date, p_description, p_is_recurring);
    
    SET result_message = CONCAT('Success: Holiday added with ID ', LAST_INSERT_ID());
    
    RETURN result_message;
END //

DELIMITER ;