# Vently Platform - Roles & Permissions

This document outlines all user roles in the Vently Event Volunteer Management Platform and their associated permissions.

## Roles Overview

The platform has three main roles:
1. **VOLUNTEER** - Users who apply to volunteer at events
2. **ORGANIZER** - Users who create and manage events
3. **ADMIN** - Platform administrators with full system access

---

## VOLUNTEER Role

### Description
Volunteers are users who browse events and apply to participate in them. They can manage their profile, view events, and track their applications.

### Permissions

#### Profile Management
- ✅ View own profile
- ✅ Update own profile (bio, skills, availability, experience, phone)
- ✅ Upload profile picture
- ✅ View email notification preferences
- ✅ Update email notification preferences
- ✅ View own statistics (events attended, ratings, no-show count)

#### Event Management
- ✅ Browse published events
- ✅ Search and filter events by date, location, payment, category
- ✅ View event details
- ✅ View organizer profile and ratings
- ❌ Create events
- ❌ Edit events
- ❌ Publish events
- ❌ Cancel events

#### Applications
- ✅ Apply to events (subject to tier limits: 5 active applications for FREE tier, unlimited for PREMIUM)
- ✅ View own applications
- ✅ Withdraw pending applications
- ✅ Confirm accepted applications (within 48-hour window)
- ✅ Decline accepted applications
- ❌ Accept/reject applications (organizer only)

#### Attendance
- ✅ Mark attendance using attendance code
- ✅ View attendance status for events they participated in
- ❌ Generate attendance codes
- ❌ Upload attendance via Excel

#### Ratings & Reviews
- ✅ Rate organizers (1-5 stars with optional review)
- ✅ Rate other volunteers (1-5 stars with optional review)
- ✅ View ratings received
- ✅ View average rating on profile
- ❌ Edit/delete ratings

#### Disputes
- ✅ Create disputes for events they participated in
- ✅ Upload evidence for disputes
- ✅ View own disputes
- ❌ Resolve disputes (admin only)

#### Payments & Payouts
- ✅ View payout history
- ✅ View payment status for confirmed applications
- ❌ Process payments (Razorpay)
- ❌ Manage refunds

#### Subscriptions
- ✅ View current subscription tier (FREE or PREMIUM)
- ✅ Upgrade to PREMIUM tier
- ❌ Downgrade subscription (automatic after expiry)

#### Notifications
- ✅ View in-app notifications
- ✅ Mark notifications as read
- ✅ Delete notifications
- ✅ Configure email notification preferences

#### Tier Limits (FREE)
- Maximum 5 active applications at a time
- Upgrade to PREMIUM for unlimited applications

---

## ORGANIZER Role

### Description
Organizers are users who create and manage events, accept/reject volunteer applications, and handle event logistics including attendance tracking and payments.

### Permissions

#### Profile Management
- ✅ View own profile
- ✅ Update own profile (bio, organization name, organization details, phone)
- ✅ Upload profile picture
- ✅ View email notification preferences
- ✅ Update email notification preferences
- ✅ View own statistics (events created, volunteers managed, ratings)

#### Event Management
- ✅ Create events (subject to tier limits: 3 active events for FREE tier, unlimited for PREMIUM)
- ✅ Edit events (only DRAFT status)
- ✅ Publish events (transition DRAFT → PUBLISHED)
- ✅ Cancel events (with refund handling based on timing)
- ✅ View own events
- ✅ View event applications
- ✅ View event details
- ❌ Edit published events
- ❌ View other organizers' events

#### Applications
- ✅ Accept applications (change PENDING → ACCEPTED, starts 48-hour confirmation timer)
- ✅ Reject applications (change PENDING → REJECTED)
- ✅ View all applications for own events
- ✅ View volunteer details for applicants
- ❌ Apply to events
- ❌ Withdraw applications

#### Attendance
- ✅ Generate attendance codes for confirmed volunteers
- ✅ Mark attendance by code
- ✅ Upload attendance via Excel file
- ✅ Download attendance template
- ✅ View attendance records
- ❌ Mark attendance for other events

#### Payments & Deposits
- ✅ Create deposit payment intent (Razorpay)
- ✅ Confirm deposit payment
- ✅ View payment history
- ✅ View payout status
- ✅ Process refunds for no-shows
- ✅ Handle cancellation refunds (100%/50%/0% based on timing)
- ❌ Manually adjust payments (admin only)

#### Ratings & Reviews
- ✅ Rate volunteers (1-5 stars with optional review)
- ✅ View ratings received
- ✅ View average rating on profile
- ❌ Rate other organizers
- ❌ Edit/delete ratings

#### Disputes
- ✅ Create disputes for events they organized
- ✅ Upload evidence for disputes
- ✅ View own disputes
- ❌ Resolve disputes (admin only)

#### Subscriptions
- ✅ View current subscription tier (FREE or PREMIUM)
- ✅ Upgrade to PREMIUM tier
- ❌ Downgrade subscription (automatic after expiry)

#### Notifications
- ✅ View in-app notifications
- ✅ Mark notifications as read
- ✅ Delete notifications
- ✅ Configure email notification preferences

#### Tier Limits (FREE)
- Maximum 3 active events at a time
- Upgrade to PREMIUM for unlimited events

---

## ADMIN Role

### Description
Admins have full system access and can manage users, disputes, analytics, and platform configuration.

### Permissions

#### User Management
- ✅ View all users with filters (role, status, verification)
- ✅ Suspend users (set suspension duration)
- ✅ Ban users (permanent)
- ✅ Grant verification badge
- ✅ Revoke verification badge
- ✅ Reset user passwords
- ✅ Adjust no-show count (for dispute resolution)

#### Dispute Management
- ✅ View all disputes
- ✅ View dispute details with evidence
- ✅ Resolve disputes
- ✅ Apply payment adjustments
- ✅ Apply penalty adjustments (no-show count)
- ✅ Send notifications to dispute parties

#### Analytics & Reporting
- ✅ View platform analytics (users, events, revenue)
- ✅ View user growth trends
- ✅ View revenue metrics
- ✅ View dispute metrics
- ✅ View average ratings by role
- ✅ View no-show statistics
- ✅ Filter analytics by date range

#### Audit Logging
- ✅ View audit logs
- ✅ Search audit logs by user, action, date range
- ✅ View authentication attempts
- ✅ View payment transactions
- ✅ View admin actions
- ✅ View event state transitions

#### System Configuration
- ✅ Configure platform settings
- ✅ Manage email templates
- ✅ Configure payment gateways
- ✅ Manage subscription tiers

#### Full Access
- ✅ All ORGANIZER permissions
- ✅ All VOLUNTEER permissions
- ✅ View all events
- ✅ View all applications
- ✅ View all payments
- ✅ View all ratings
- ✅ View all notifications

---

## Permission Matrix

| Feature | VOLUNTEER | ORGANIZER | ADMIN |
|---------|-----------|-----------|-------|
| **Profile** | | | |
| View own profile | ✅ | ✅ | ✅ |
| Update own profile | ✅ | ✅ | ✅ |
| Upload profile picture | ✅ | ✅ | ✅ |
| View other profiles | ✅ | ✅ | ✅ |
| **Events** | | | |
| Create events | ❌ | ✅ | ✅ |
| Edit own events | ❌ | ✅ (DRAFT only) | ✅ |
| Publish events | ❌ | ✅ | ✅ |
| Cancel events | ❌ | ✅ | ✅ |
| Browse events | ✅ | ✅ | ✅ |
| View all events | ❌ | ❌ | ✅ |
| **Applications** | | | |
| Apply to events | ✅ | ❌ | ✅ |
| Accept applications | ❌ | ✅ | ✅ |
| Reject applications | ❌ | ✅ | ✅ |
| View own applications | ✅ | ✅ | ✅ |
| View all applications | ❌ | ✅ (own events) | ✅ |
| **Attendance** | | | |
| Mark attendance | ✅ | ✅ | ✅ |
| Generate codes | ❌ | ✅ | ✅ |
| Upload Excel | ❌ | ✅ | ✅ |
| **Payments** | | | |
| View payouts | ✅ | ✅ | ✅ |
| Create deposits | ❌ | ✅ | ✅ |
| Process refunds | ❌ | ✅ | ✅ |
| **Ratings** | | | |
| Rate others | ✅ | ✅ | ✅ |
| View ratings | ✅ | ✅ | ✅ |
| **Disputes** | | | |
| Create disputes | ✅ | ✅ | ✅ |
| Resolve disputes | ❌ | ❌ | ✅ |
| **User Management** | | | |
| Suspend users | ❌ | ❌ | ✅ |
| Ban users | ❌ | ❌ | ✅ |
| Grant badges | ❌ | ❌ | ✅ |
| **Analytics** | | | |
| View analytics | ❌ | ❌ | ✅ |
| View audit logs | ❌ | ❌ | ✅ |

---

## Subscription Tier Limits

### FREE Tier
- **Organizers**: Maximum 3 active events
- **Volunteers**: Maximum 5 active applications
- **Cost**: Free

### PREMIUM Tier
- **Organizers**: Unlimited events
- **Volunteers**: Unlimited applications
- **Cost**: Paid subscription (via Razorpay)

---

## Account Status

Users can have the following account statuses:

- **ACTIVE** - Normal account, can login and use platform
- **SUSPENDED** - Temporary suspension (30 days) after 3 no-shows
- **BANNED** - Permanent ban after 5 no-shows, cannot login

---

## No-Show Penalties

- **1-2 no-shows**: Warning, no restrictions
- **3 no-shows**: Account suspended for 30 days
- **5 no-shows**: Account permanently banned

---

## API Endpoint Authorization

All endpoints require authentication (JWT token) except:
- `POST /api/v1/auth/register` - Public
- `POST /api/v1/auth/login` - Public
- `GET /api/v1/events` - Public (browse events)
- `GET /api/v1/events/{id}` - Public (view event details)

All other endpoints require:
- Valid JWT token in `Authorization: Bearer <token>` header
- Appropriate role for the operation
- Ownership verification where applicable (e.g., can only edit own events)
