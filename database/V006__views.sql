CREATE VIEW finance.v_income_expense AS

SELECT
    'CR-' || i.income_id AS transaction_id,
    i.apartment_id,
    a.apartment_name,
    i.transaction_date,
    i.txn_year,
    i.txn_month,
    lc.category_name,
    pm.payment_mode_name,
    lc.transaction_type,
    i.reference_number,
    i.remarks,
    lc.is_flat_maintenance,
    f.flat_number,
    f.owner_name,
    i.amount AS income_amount,
    NULL AS expense_amount
FROM finance.income AS i
JOIN master.ledger_category AS lc
    ON lc.ledger_category_id = i.ledger_category_id
JOIN master.payment_mode AS pm
    ON pm.payment_mode_id = i.payment_mode_id
JOIN core.apartment AS a
    ON a.apartment_id = i.apartment_id
LEFT JOIN core.flat AS f
    ON f.flat_id = i.flat_id

UNION ALL

SELECT
    'DR-' || e.expense_id AS transaction_id,
    e.apartment_id,
    a.apartment_name,
    e.transaction_date,
    e.txn_year,
    e.txn_month,
    lc.category_name,
    pm.payment_mode_name,
    lc.transaction_type,
    e.reference_number,
    e.remarks,
    lc.is_flat_maintenance,
    NULL AS flat_number,
    NULL AS owner_name,
    NULL AS income_amount,
    e.amount AS expense_amount
FROM finance.expense AS e
JOIN master.ledger_category AS lc
    ON lc.ledger_category_id = e.ledger_category_id
JOIN master.payment_mode AS pm
    ON pm.payment_mode_id = e.payment_mode_id
JOIN core.apartment AS a
    ON a.apartment_id = e.apartment_id;
