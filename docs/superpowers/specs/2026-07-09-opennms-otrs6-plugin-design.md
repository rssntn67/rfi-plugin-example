# opennms-otrs6-plugin — Design

## Purpose

Split the OTRS6 ticketing integration out of `rfi-plugin-example` into its own,
independent OpenNMS plugin project: `opennms-otrs6-plugin`. The new project
reuses the Maven/OSGi/Karaf skeleton of `rfi-plugin-example` but is scoped to
exactly one job: implement OpenNMS's `TicketingPlugin` API backed by OTRS6
(SOAP `GenericTicketConnector`). It does not carry over alarm forwarding,
topology forwarding, SNMP connection management, provisioning, vendor event
ingestors, or the REST webhook — those are specific to `rfi-plugin-example`
and unrelated to ticketing.

## Location & repo

- New sibling directory: `/Users/antonio/Rcs/rssntn67/opennms-otrs6-plugin`
- Fresh, independent git repository (`git init`, initial commit). Not a fork
  or clone of `rfi-plugin-example`'s history.

## Coordinates

- groupId: `it.arsinfo.opennms.plugins`
- base Java package: `it.arsinfo.opennms.plugins.otrs6`
- parent artifactId: `opennms-otrs6-plugin` (`packaging: pom`)
- plugin module artifactId: `otrs6-plugin` (directory `plugin/`, `packaging: bundle`)
- karaf-features module: `karaf-features`, feature name `opennms-plugins-otrs6`
- assembly/kar module: `kar`, final KAR artifact name `opennms-otrs6-plugin`
- config PID / file: `it.arsinfo.opennms.plugins.otrs6.cfg`
  - properties: `otrsUrl`, `otrsUser`, `otrsPassword`

## Module structure

Mirrors `rfi-plugin-example`:

```
opennms-otrs6-plugin/
  pom.xml                (parent)
  assembly/
    pom.xml
    kar/
      pom.xml
  karaf-features/
    pom.xml
    src/main/resources/features.xml
  plugin/
    pom.xml
    src/main/java/it/arsinfo/opennms/plugins/otrs6/...
    src/main/wsdl/GenericTicketConnector.wsdl
    src/main/resources/OSGI-INF/blueprint/blueprint.xml
    src/test/java/it/arsinfo/opennms/plugins/otrs6/...
```

## What is carried over (adapted)

- **Parent `pom.xml`** — properties, `dependencyManagement`, `pluginManagement`
  (bundle/compiler/build-helper/resources/surefire config), repositories.
  Trimmed: drop `snmp4j`, `jexl`, `okhttp`, `okhttp.bundle`, `okio.bundle`,
  `jackson`, `metrics` properties — none are needed by the ticketing client.
- **`assembly/` and `assembly/kar/`** — unchanged structure; kar `finalName`
  becomes `opennms-otrs6-plugin`, `featuresFile` points at the new
  `karaf-features` artifact.
- **`karaf-features/`** — feature `opennms-plugins-otrs6` depends on
  `aries-blueprint`, `opennms-integration-api`; bundle list trimmed to just
  the plugin bundle plus any JAX-WS/JAXB runtime bundles it needs at deploy
  time (to be confirmed during implementation — check what CXF-generated
  stubs require on the OSGi runtime). Drop the `okhttp`/`jackson` custom
  features and the snmp4j/commons-* bundles.
- **`plugin/pom.xml`** — keep `maven-bundle-plugin`, `cxf-codegen-plugin`
  (wsdl2java over `GenericTicketConnector.wsdl`, package retargeted to
  `it.arsinfo.opennms.plugins.otrs6.clients.otrs`), `build-helper-maven-plugin`
  add-source. Dependencies kept: `org.opennms.integration.api:common`,
  `org.opennms.integration.api:config`, `slf4j-api`, `jaxb-api`, `jaxws-api`,
  `javax.jws-api`, JUnit/Mockito/hamcrest (test). Dropped: okhttp
  logging-interceptor, jackson-databind, metrics-core, snmp4j,
  karaf-shell-core, wiremock/awaitility/jsonassert (no HTTP client, no shell
  commands, no IT tests remain), commons-lang3, commons-csv, guava.
- **`src/main/wsdl/GenericTicketConnector.wsdl`** — copied byte-for-byte.
- **Java sources** (repackaged under `it.arsinfo.opennms.plugins.otrs6`):
  - `ticketing/Ticketer.java`
  - `clients/OtrsClient.java`
  - `clients/Otrs6Client.java`
- **Tests** (repackaged): `clients/Otrs6ClientTest.java`
- **`blueprint.xml`** — trimmed to:
  - `cm:property-placeholder` for `it.arsinfo.opennms.plugins.otrs6`
    (`otrsUrl`, `otrsUser`, `otrsPassword`)
  - `otrs6Client` bean
  - `ticketer` bean
  - `<service>` export of `org.opennms.integration.api.v1.ticketing.TicketingPlugin`
  - Note: `Ticketer` uses `@Reference AlarmDao` (Karaf shell API annotation
    injection) — this wiring pattern is preserved as-is from the original.
- **`README.md` / `CLAUDE.md`** — rewritten to describe the trimmed scope,
  build commands, and deploy steps (feature name, kar filename updated).

## What is dropped entirely

- `clients/Otrs2026Client.java` — empty, unimplemented stub for a future/other
  OTRS version; out of scope for a project specifically named for OTRS6.
- `clients/ApiClient.java`, `clients/SnmpClient.java`, `clients/SnmpCredentials.java`
- `connection/*` (ConnectionManager, Connection, ConnectionValidationError)
- `events/*` (all vendor event ingestors: acom, andrew, axell, pbh, scair,
  scr, smarts94, teko — and `EventConfExtension`)
- `model/*` (Alert, Link, Topology)
- `provisioning/*` (desigo, netact, pbh, AbstractRequisitionProvider)
- `shell/*` (all Karaf shell commands — none relate to ticketing)
- `webhook/*` (WebhookHandler, WebhookHandlerImpl, ConnectionDto)
- `snmp/*` (AdvancedSnmpGet, AdvancedSnmpSet)
- `AlarmForwarder.java`, `TopologyForwarder.java`, `SyncService.java`
- `src/main/resources/events/*` (event UEI XML definitions)
- All test files other than `Otrs6ClientTest.java`
- IDE metadata (`.idea`, `.classpath`, `.project`, `.settings`, `.iml`) — not
  copied; regenerated fresh by each developer's IDE if needed.

## Testing

- Unit tests: `mvn test` (Surefire), same conventions as `rfi-plugin-example`
  (`*Test.java`).
- No integration tests (`*IT.java`) are carried over — the only IT test in
  the source project (`AlarmForwarderIT`) tests dropped code.

## Deploy

Same two options as the source project, updated names:

```bash
# Karaf feature install
feature:repo-add mvn:it.arsinfo.opennms.plugins/karaf-features/0.1.0-SNAPSHOT/xml
feature:install opennms-plugins-otrs6

# Or copy KAR bundle directly
cp assembly/kar/target/opennms-otrs6-plugin.kar /opt/opennms/deploy/
```

## Out of scope / open questions for implementation

- Whether any extra OSGi runtime bundles are required for the CXF-generated
  JAX-WS stubs to resolve inside Karaf (the source project never deployed
  this in isolation — it always ran alongside the full RFI bundle's
  dependencies). This should be verified during implementation/build.
