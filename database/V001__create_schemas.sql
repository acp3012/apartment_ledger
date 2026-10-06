/*
ApartmentLedger
│
├── master
│     ├── roles
│     ├── ledger_categories
│     ├── reference_types
│     └── payment_modes
│
├── core
│     ├── apartments
│     ├── flats
│     ├── app_users
│     └── user_roles
│
├── finance
│     ├── income
│     ├── income_draft
│     ├── expense
│     └── expense_draft
│
├── report
│     ├── monthly_income_summary (View)
│     ├── monthly_expense_summary (View)
│     ├── monthly_balance (View)
│     └── outstanding_report (View)
│
└── audit
      ├── login_history
      ├── approval_history
      ├── api_log
      └── activity_log
      
  */
  
 -- =====================================================
-- ApartmentLedger Database Schemas
-- =====================================================

CREATE SCHEMA IF NOT EXISTS master;

CREATE SCHEMA IF NOT EXISTS core;

CREATE SCHEMA IF NOT EXISTS finance;

CREATE SCHEMA IF NOT EXISTS report;

CREATE SCHEMA IF NOT EXISTS audit;

CREATE SCHEMA IF NOT EXISTS config;


