insert into master.approval_status 
(status_code,status_name,display_order) 
values ('P','PENDING',1), 
('A','APPROVED',2),
('R','REJECTED',3);


INSERT INTO master.payment_mode
(payment_mode_name)
VALUES
('Cash'),
('UPI'),
('Bank Transfer'),
('Cheque');


INSERT INTO master.role
(role_name)
VALUES
('ADMIN'),
('USER'),
('APPROVER');

---- Ledger categoris --
insert into master.ledger_category (category_name,transaction_type,is_flat_maintenance,display_order) 
values ('Monthly maintenance','CR',true,1),
 ('Corpus fund','CR',false,2),
 ('Event contributions','CR',false,3),
 ('Bank interest','CR',false,4),
 ('Parking charges','CR',false,5),
 ('Scrap / miscellaneous income','CR',false,6),
-- expenses 
('Housekeeping','DR',false,1),
('Electricity (common areas)','DR',false,2),
('Water & sewage','DR',false,3),
('Lift maintenance','DR',false,4),
('R&M - Labour','DR',false,5),
('R&M - Materials','DR',false,6),
('Internet/Phone','DR',false,7),
('Security/CCTV','DR',false,8),
('Community events','DR',false,9),
('Pest control','DR',false,10),
('Capital expenses','DR',false,11),
('Miscellaneous','DR',false,12),
('Gardening & landscaping','DR',false,13),
('Statutory & legal','DR',false,14),
('Insurance','DR',false,15),
('Generator / DG','DR',false,16);
