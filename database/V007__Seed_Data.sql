INSERT INTO master.approval_status
(status_name)
VALUES
('DRAFT'),
('APPROVED'),
('REJECTED');

INSERT INTO master.payment_mode
(payment_mode_name)
VALUES
('Cash'),
('UPI'),
('Bank Transfer'),
('Cheque'),
('Card');

INSERT INTO master.reference_type
(reference_type_name)
VALUES
('Receipt'),
('Transaction'),
('Cheque'),
('UPI Ref'),
('NEFT Ref');

INSERT INTO master.role
(role_name)
VALUES
('ADMIN'),
('TREASURER'),
('SECRETARY'),
('FLAT_OWNER');
