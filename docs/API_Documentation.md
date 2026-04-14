# API Documentation

## Overview
This document serves as the guide for the Frontend and integrations team on how to consume the Intel Backend API. 

> [!WARNING]
> Please note that the backend is currently actively transitioning between its "Shallow Models" and "Deep Models" for secure-by-design access controls.
> **All endpoints below represent the current active, functional state. They do not enforce strict security bounds yet.** These will be replaced in future updates, so expect breaking changes regarding authentication and required headers. 

---

## 1. Business Card Portal API

### Generate Access Token
- **Endpoint**: `GET /api/business-card/token`
- **Description**: Generates a generic token to be used on the portal. Currently, this endpoint does not challenge for user credentials.
- **Response**:
```json
{
  "accessToken": "SUPER_PRIVILEGED_TOKEN_123"
}
```

### Search Employees
- **Endpoint**: `GET /api/business-card/employees`
- **Description**: Retrieves employee data based on a search filter.
- **Parameters**: 
  - `token` (String, required): The token from `/token`.
  - `search` (String, optional): The name filter to search for. If omitted, returns all employees.
- **Response**:
```json
{
  "count": 270000,
  "data": [ ... ]
}
```

---

## 2. Product Hierarchy API

### Login
- **Endpoint**: `POST /api/product/login`
- **Description**: Authenticate using username and password.
- **Parameters**:
  - `username` (String)
  - `password` (String)
- **Response**:
```json
{
  "authenticated": true,
  "username": "admin",
  "role": "ADMIN"
}
```

### Get Product Hierarchy
- **Endpoint**: `GET /api/product/hierarchy`
- **Description**: Fetch all available product records.
- **Response**:
```json
{
  "count": 14,
  "data": [ ... ]
}
```

### Get Admin Credentials
- **Endpoint**: `GET /api/product/credentials`
- **Description**: Administrative diagnostic endpoint to verify system credentials.
- **Response**:
```json
{
  "count": 4,
  "data": [ ... ]
}
```

---

## 3. Supplier EHS IP Management System (SEIMS) API

### Application Authentication
- **Endpoint**: `POST /api/seims/auth`
- **Headers**:
  - `Authorization: Bearer <Token_String>`
- **Description**: Verifies a session. Currently accepts any non-empty bearer token.
- **Response**:
```json
{
  "authenticated": true,
  "token": "...",
  "userId": "anonymous"
}
```

### List All Suppliers
- **Endpoint**: `GET /api/seims/suppliers`
- **Description**: Dumps the complete list of active suppliers.

### Get Supplier by ID
- **Endpoint**: `GET /api/seims/suppliers/{id}`
- **Description**: Fetch exact supplier metadata utilizing sequential ID patterns (1, 2, 3...).

### Get Supplier NDAs
- **Endpoint**: `GET /api/seims/nda/{supplierId}`
- **Description**: Fetch confidentiality agreements associated with a supplier. Warning: Current functionality returns both Standard and Top Secret NDAs.
