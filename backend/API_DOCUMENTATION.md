# Apartment Ledger API Documentation

This document lists the backend REST endpoints currently exposed by the application.

Base URL pattern:

- `/api/v1/...`

---

## 1. Authentication

### Auth Controller

Base path: `/api/v1/auth`

#### Register user

- Method: `POST`
- Endpoint: `/api/v1/auth/register`
- Description: Registers a new user.
- Request body: `RegisterRequest`
- Response: `AuthResponse`

#### Login user

- Method: `POST`
- Endpoint: `/api/v1/auth/login`
- Description: Authenticates an existing user.
- Request body: `LoginRequest`
- Response: `AuthResponse`

---

## 2. Apartment and Flat APIs

### Flat Controller

Base path: `/api/v1/apartments`

#### Get flats by apartment

- Method: `GET`
- Endpoint: `/api/v1/apartments/{apartmentId}/flats`
- Description: Returns all flats belonging to an apartment.
- Path parameter:
  - `apartmentId` (Long)
- Response: `List<FlatResponse>`

---

## 3. Expense APIs

### Expense Controller

Base path: `/api/v1/apartments/{apartmentId}/expenses`

#### Create expense draft

- Method: `POST`
- Endpoint: `/api/v1/apartments/{apartmentId}/expenses/drafts`
- Description: Saves a new expense draft.
- Path parameter:
  - `apartmentId` (Long)
- Query parameter:
  - `apartmentId` (also read as request param in the controller)
- Request body: `ExpenseDraftRequest`
- Response: `String`

#### Update expense draft

- Method: `PUT`
- Endpoint: `/api/v1/apartments/{apartmentId}/expenses/drafts/{draftId}`
- Description: Updates an existing expense draft.
- Path parameters:
  - `apartmentId` (Long)
  - `draftId` (Long)
- Request body: `ExpenseDraftRequest`
- Response: `String`

#### Get pending expense drafts

- Method: `GET`
- Endpoint: `/api/v1/apartments/{apartmentId}/expenses/drafts/pending`
- Description: Fetches draft expenses awaiting approval.
- Query parameter:
  - `apartmentId` (Long)
- Response: `List<ExpensePendingDraftResponse>`

#### Approve expense drafts

- Method: `POST`
- Endpoint: `/api/v1/apartments/{apartmentId}/expenses/drafts/approve`
- Description: Approves one or more expense drafts.
- Query parameter:
  - `apartmentId` (Long)
- Request body: `ExpenseApprovalRequest`
- Response: `String`

---

## 4. Income APIs

### Income Controller

Base path: `/api/v1/apartments/{apartmentId}/incomes`

#### Create income draft

- Method: `POST`
- Endpoint: `/api/v1/apartments/{apartmentId}/incomes/drafts`
- Description: Saves a new income draft.
- Path parameter:
  - `apartmentId` (Long)
- Request body: `IncomeDraftRequest`
- Response: `String`

#### Update income draft

- Method: `PUT`
- Endpoint: `/api/v1/apartments/{apartmentId}/incomes/drafts/{draftId}`
- Description: Updates an existing income draft.
- Path parameters:
  - `apartmentId` (Long)
  - `draftId` (Long)
- Request body: `IncomeDraftRequest`
- Response: `Void`

#### Get income drafts

- Method: `GET`
- Endpoint: `/api/v1/apartments/{apartmentId}/incomes/drafts`
- Description: Fetches income drafts filtered by review status and date.
- Path parameter:
  - `apartmentId` (Long)
- Query parameters:
  - `approvalStatus` (String, optional)
  - `year` (Short, optional)
  - `month` (Short, optional)
- Response: `List<IncomeDraftResponse>`

#### Approve income drafts in bulk

- Method: `POST`
- Endpoint: `/api/v1/apartments/{apartmentId}/incomes/drafts/approve-bulk`
- Description: Approves a group of draft incomes.
- Path parameter:
  - `apartmentId` (Long)
- Request body: `DraftApprovalRequest`
- Response: `String`

#### Reject income drafts in bulk

- Method: `POST`
- Endpoint: `/api/v1/apartments/{apartmentId}/incomes/drafts/reject-bulk`
- Description: Rejects a group of draft incomes.
- Path parameter:
  - `apartmentId` (Long)
- Request body: `DraftApprovalRequest`
- Response: `String`

#### Get approved income records

- Method: `GET`
- Endpoint: `/api/v1/apartments/{apartmentId}/incomes`
- Description: Returns incomes for a given apartment and optional year/month filter.
- Path parameter:
  - `apartmentId` (Long)
- Query parameters:
  - `year` (Short, optional)
  - `month` (Short, optional)
- Response: `List<IncomeResponse>`

---

## 5. Ledger APIs

### Ledger Controller

Base path: `/api/v1/apartments/{apartmentId}/ledger`

#### Get active ledger period

- Method: `GET`
- Endpoint: `/api/v1/apartments/{apartmentId}/ledger/active-period`
- Description: Returns the current ledger period for the apartment.
- Path parameter:
  - `apartmentId` (Long)
- Response: `LedgerPeriodResponse`

#### Preview month-end close

- Method: `GET`
- Endpoint: `/api/v1/apartments/{apartmentId}/ledger/preview`
- Description: Shows a preview of the month-end close for a given year/month.
- Path parameters:
  - `apartmentId` (Long)
- Query parameters:
  - `year` (Short)
  - `month` (Short)
- Response: `MonthEndCloseResponse`

#### Close ledger month

- Method: `POST`
- Endpoint: `/api/v1/apartments/{apartmentId}/ledger/ledger/close`
- Description: Closes the month-end ledger for a given apartment and period.
- Path parameter:
  - `apartmentId` (Long)
- Query parameters:
  - `year` (Short)
  - `month` (Short)
- Response: `String`

> Note: The current route includes a duplicated `ledger` segment due to the class-level and method-level mappings.

---

## 6. Maintenance Rate API

### Maintenance Rate Controller

Base path: `/api/v1/apartments/{apartmentId}/maintenance-rates`

#### Get applicable maintenance rate

- Method: `GET`
- Endpoint: `/api/v1/apartments/{apartmentId}/maintenance-rates/current`
- Description: Returns the maintenance fee rate applicable for a flat and date.
- Path parameter:
  - `apartmentId` (Long)
- Query parameters:
  - `flatId` (Long)
  - `date` (LocalDate)
- Response: `MaintenanceRateResponse`

---

## 7. Master Data APIs

### Master Data Controller

Base path: `/api/v1/master`

#### Get payment modes

- Method: `GET`
- Endpoint: `/api/v1/master/payment-modes`
- Description: Lists all active payment modes.
- Response: `List<PaymentModeResponse>`

#### Get payment mode by name

- Method: `GET`
- Endpoint: `/api/v1/master/payment-modes/{paymentModeName}`
- Description: Fetches a payment mode by its name.
- Path parameter:
  - `paymentModeName` (String)
- Response: `PaymentMode`

#### Get all ledger categories

- Method: `GET`
- Endpoint: `/api/v1/master/ledger-categories`
- Description: Returns all ledger categories.
- Response: `List<LedgerCategoryResponse>`

#### Get ledger categories by transaction type

- Method: `GET`
- Endpoint: `/api/v1/master/ledger-categories/{transactionType}`
- Description: Returns ledger categories filtered by transaction type.
- Path parameter:
  - `transactionType` (String)
- Response: `List<LedgerCategoryResponse>`

---

## Quick Reference Table

| Module      | Endpoint Pattern                                       |
| ----------- | ------------------------------------------------------ |
| Auth        | `/api/v1/auth/*`                                       |
| Flats       | `/api/v1/apartments/{apartmentId}/flats`               |
| Expenses    | `/api/v1/apartments/{apartmentId}/expenses/*`          |
| Incomes     | `/api/v1/apartments/{apartmentId}/incomes/*`           |
| Ledger      | `/api/v1/apartments/{apartmentId}/ledger/*`            |
| Maintenance | `/api/v1/apartments/{apartmentId}/maintenance-rates/*` |
| Master Data | `/api/v1/master/*`                                     |

---

## Notes

- This documentation reflects the current controller mappings in the Java backend.
- Some endpoints currently accept parameters both in the path and as query parameters; these could be normalized later for cleaner API design.
- The ledger close route currently has a duplicate `ledger` segment and may be a candidate for cleanup.
