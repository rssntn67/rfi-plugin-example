# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Full build
mvn clean install

# Build without installing
mvn clean package

# Run unit tests only (excludes *IT.java)
mvn test

# Run a single unit test class
mvn test -Dtest=AlarmForwarderTest

# Run a single test method
mvn test -Dtest=AlarmForwarderTest#testMethodName

# Run integration tests (*IT.java) — requires WireMock/network mocking setup
mvn integration-test

# Run a single integration test
mvn integration-test -Dit.test=AlarmForwarderIT
```

## Deploy to OpenNMS/Karaf

```bash
# Option 1: Karaf feature install
feature:repo-add mvn:it.xeniaprogetti/karaf-features/0.1.0-SNAPSHOT/xml
feature:install opennms-plugins-rfi-plugin-example

# Option 2: Copy KAR bundle directly
cp assembly/kar/target/opennms-rfi-plugin-example-plugin.kar /opt/opennms/deploy/
```

## Architecture Overview

This is an **OpenNMS integration plugin** packaged as an OSGi bundle for deployment in Apache Karaf (the runtime embedded in OpenNMS). The plugin connects OpenNMS to external network management systems via SNMP and REST APIs.

### Module Structure

- **`plugin/`** — Main OSGi bundle (`packaging: bundle`). All business logic lives here.
- **`karaf-features/`** — Defines the Karaf feature descriptor (`features.xml`) that declares bundle dependencies.
- **`assembly/kar/`** — Assembles the deployable KAR (Karaf Archive) artifact.

### Core Pipelines

**1. Alarm Forwarding**
`AlarmForwarder` implements `AlarmLifecycleListener` (OpenNMS Integration API). On alarm events, it maps `Alarm` → `Alert` (internal DTO) and POSTs to an external URL via `ApiClient` (async OkHttp3). Metrics tracked as `eventsForwarded` / `eventsFailed`, accessible via `StatsCommand`.

**2. Topology Forwarding**
`TopologyForwarder` queries `EdgeDao` for topology edges per protocol, uses the Visitor pattern (`TopologyEdge.EndpointVisitor`) to map edges to `Link` DTOs, aggregates them into `Topology` objects, and forwards to the same external API. `CompletableFuture.allOf()` combines multiple async sends.

**3. SNMP Connection Management**
`ConnectionManager` stores SNMP connection credentials (address, community, version, domain) in OpenNMS's `SecureCredentialsVault`. Connections are identified by alias. Used by `SyncService` and `SnmpClient`.

**4. Event Ingestion**
Multiple `EventIngestor` subpackages (`acom`, `andrew`, `axell`, `pbh`, `scair`, `scr`, `smarts94`, `teko`) each handle events from a specific NMS vendor. `EventConfExtension` registers custom event UEI configurations with OpenNMS.

**5. Provisioning**
`AbstractRequisitionProvider<T>` (template method pattern) parses semicolon-delimited CSV files and generates `RequisitionNode` objects for OpenNMS auto-discovery. Concrete implementations: `DesigoRequisitionProvider`, `PBHRequisitionProvider`.

**6. REST Webhook**
`WebhookHandlerImpl` exposes JAX-RS endpoints under `/rest/rfi-plugin-example/`. Handles incoming webhooks, connection CRUD, sync triggers, and a `/ping` health check.

**7. Karaf Shell Commands**
Commands under scope `opennms-rfi-plugin-example:` — `stats`, `push-topology`, `sync`, `add-connection`, `edit-connection`, `delete-connection`, `list-connection`, `reset-password-connection`.

### Dependency Injection & Configuration

All components are wired via OSGi Blueprint XML at `plugin/src/main/resources/OSGI-INF/blueprint/blueprint.xml`. OpenNMS services (`NodeDao`, `AlarmDao`, `EdgeDao`, `EventForwarder`, `EventSubscriptionService`, `SecureCredentialsVault`, `RuntimeInfo`) are injected as OSGi service references.

Runtime configuration is read from `$OPENNMS_HOME/etc/it.xeniaprogetti.rfi.plugin.example.cfg` with hot-reload support:
- `url` — Target API endpoint (default: `http://127.0.0.1:8888/target`)
- `apiKey` — Bearer token for the external API (default: `TOKEN`)

### Key Technologies

- **OpenNMS Integration API 1.6.1** — `AlarmLifecycleListener`, `EdgeDao`, `RequisitionProvider`, etc.
- **OSGi / Apache Karaf 4.3.10** — Bundle lifecycle, service registry, shell commands
- **OSGi Blueprint (Aries)** — Declarative dependency injection via XML
- **SNMP4J 3.9.2** — SNMP protocol operations (`AdvancedSnmpSet`)
- **OkHttp3 3.10.0** — Async HTTP client in `ApiClient`
- **Jackson 2.11.1** — JSON serialization of `Alert`, `Topology`, `Link` DTOs
- **JUnit 4 + Mockito 2** — Unit tests; **WireMock + Awaitility** — Integration tests

### Test Conventions

- `*Test.java` = unit tests (Surefire, runs on `mvn test`)
- `*IT.java` = integration tests (Failsafe, runs on `mvn integration-test`)