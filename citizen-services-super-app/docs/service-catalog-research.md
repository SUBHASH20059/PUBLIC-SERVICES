# Service Catalog Research Notes

The National Portal of India describes itself as a single-window access point for government information and services at multiple levels. Its official services page groups access through broad areas such as government services, schemes, utilities, citizen engagement, directories, helplines, and other categories. The portal explicitly notes that service content is owned and managed by the relevant ministries and departments, so this application should present a catalog and routing layer rather than imply that every service is centrally executed by this app.

The official UMANG portal is a national aggregation platform for e-governance services spanning central, state, and local government bodies. The expanded catalog therefore uses service families rather than a claim of literal exhaustiveness. The initial families are identity and cards, civil registration and certificates, social welfare, tax and finance, transport, land and property, business and startup, education, health, agriculture, utilities, employment, justice and grievances, travel and citizenship, and emergency or public-safety support.

Implementation decision: every catalog item will carry a module type, responsible administrative level, application availability state, and an official-source or department label. Where a service is state-specific or external, the UI will clearly identify it as an external or department-routed service instead of pretending that the platform completes the transaction itself.

References:
- https://www.india.gov.in/services — National Portal of India: Services
- https://web.umang.gov.in/ — UMANG official portal
- https://www.india.gov.in/ — National Portal of India home

Research date: 2026-08-16
        
        
