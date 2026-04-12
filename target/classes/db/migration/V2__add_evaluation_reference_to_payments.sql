ALTER TABLE payments
ADD COLUMN evaluation_reference VARCHAR(255);

ALTER TABLE payments
ADD COLUMN fraud_check_status VARCHAR(50);
