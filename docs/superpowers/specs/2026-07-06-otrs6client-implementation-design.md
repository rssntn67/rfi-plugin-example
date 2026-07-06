# Otrs6Client Implementation Design

**Date:** 2026-07-06
**Status:** Approved

## Goal

Implement `Otrs6Client` using the JAX-WS stubs generated from `GenericTicketConnector.wsdl` to back the `OtrsClient` interface with real SOAP calls to an OTRS 6 instance.

## Context

- `OtrsClient` interface: `getAll()`, `get(String ticketId)`, `savaORUpdate(Ticket ticket)`
- Generated stubs in package `it.xeniaprogetti.rfi.plugin.example.clients.otrs`:
  - `GenericTicketConnector` (JAX-WS service class, extends `javax.xml.ws.Service`)
  - `GenericTicketConnectorInterface` (port interface with `ticketGet`, `ticketSearch`, `ticketCreate`, `ticketUpdate`, `sessionCreate`)
  - JAXB types: `OTRSTicketCreate`, `OTRSTicketCreateTicket`, `OTRSArticle`, `OTRSTicketGet`, `OTRSTicketGetResponse`, `OTRSTicketGetResponseTicket`, `OTRSTicketUpdate`, `OTRSTicketUpdateTicket`, `OTRSTicketSearch`, `OTRSTicketSearchResponse`, etc.
- OpenNMS `Ticket` (from `org.opennms.integration.api.v1.ticketing`): `id`, `summary`, `details`, `state` (`OPEN`/`CLOSED`/`CANCELLED`), `user`, `alarmId`, `nodeId`, `ipAddress`, `attributes`
- `ImmutableTicket.newBuilder()` is used to construct `Ticket` instances
- `Ticketer` already holds an `OtrsClient` and delegates to it
- Blueprint config pattern: `cm:property-placeholder` reading from `.cfg` file

## Authentication

Per-call `UserLogin` + `Password` on every SOAP request. No session state.

## Architecture

```
Otrs6Client(otrsUrl, otrsUser, otrsPassword)
  └─ port: GenericTicketConnectorInterface
       obtained from: new GenericTicketConnector(new URL(otrsUrl))
                          .getGenericTicketConnectorEndPoint()
```

The port is created once in the constructor and reused for all calls (JAX-WS ports are thread-safe for concurrent calls on the same proxy).

## Method Mapping

### `getAll() → List<Ticket>`

1. Build `OTRSTicketSearch` with `userLogin` + `password` only (no filters)
2. Call `port.ticketSearch(search)` → `OTRSTicketSearchResponse` containing a list of `TicketID` values
3. For each `TicketID`, call `get(id.toString())` to fetch the full ticket
4. Return the assembled list (empty list if response has no IDs)

### `get(String ticketId) → Ticket`

1. Build `OTRSTicketGet` with `userLogin`, `password`, `ticketID = new BigInteger(ticketId)`
2. Call `port.ticketGet(req)` → `OTRSTicketGetResponse`
3. Take the first `OTRSTicketGetResponseTicket` from the response
4. Map to `Ticket` via `ImmutableTicket.newBuilder()`:
   - `id` ← `ticket.getTicketID().toString()`
   - `summary` ← `ticket.getTitle()`
   - `details` ← `ticket.getState()` (OTRS state string)
   - `state` ← if OTRS state contains `"closed"` → `Ticket.State.CLOSED`, else → `Ticket.State.OPEN`
5. Return the built `Ticket`; return `null` if response ticket list is empty

### `savaORUpdate(Ticket ticket) → String`

**Create** (when `ticket.getId()` is null or blank):

1. Build `OTRSTicketCreate`:
   - `userLogin` + `password`
   - `ticket`: `Title=ticket.getSummary()`, `Queue="Raw"`, `State="new"`, `Priority="3 normal"`, `CustomerUser=otrsUser`
   - `article`: `Subject=ticket.getSummary()`, `Body=ticket.getDetails()`, `ContentType="text/plain; charset=UTF-8"`
2. Call `port.ticketCreate(req)` → `OTRSTicketCreateResponse`
3. Return `response.getTicketID().toString()`

**Update** (when `ticket.getId()` is non-blank):

1. Build `OTRSTicketUpdate`:
   - `userLogin` + `password`
   - `ticketID = new BigInteger(ticket.getId())`
   - `ticket`: `OTRSTicketUpdateTicket` with `Title=ticket.getSummary()` only
2. Call `port.ticketUpdate(req)` → `OTRSTicketUpdateResponse`
3. Return `response.getTicketID().toString()`

## Files Changed

| File | Action |
|---|---|
| `plugin/src/main/java/.../clients/Otrs6Client.java` | Replace empty stub with full implementation |
| `plugin/src/main/resources/OSGI-INF/blueprint/blueprint.xml` | Add `otrs6Client` bean + 3 config properties + wire into `Ticketer` |

## Configuration

New properties in the `cm:property-placeholder` default block of `blueprint.xml`:

```
otrsUrl      = http://127.0.0.1/otrs/nph-genericinterface.pl/Webservice/GenericTicketConnector
otrsUser     = root@localhost
otrsPassword = root
```

These can be overridden in `$OPENNMS_HOME/etc/it.xeniaprogetti.rfi.plugin.example.cfg`.

## Blueprint Wiring

```xml
<bean id="otrs6Client" class="it.xeniaprogetti.rfi.plugin.example.clients.Otrs6Client">
    <argument value="${otrsUrl}"/>
    <argument value="${otrsUser}"/>
    <argument value="${otrsPassword}"/>
</bean>

<bean id="ticketer" class="it.xeniaprogetti.rfi.plugin.example.ticketing.Ticketer">
    <argument ref="otrs6Client"/>
</bean>
<service ref="ticketer" interface="org.opennms.integration.api.v1.ticketing.TicketingPlugin"/>
```

## OSGi Considerations

`GenericTicketConnector` (the JAX-WS service class) uses `javax.xml.ws.Service` which is provided by Karaf's system bundle. The `provided`-scope dependencies already added in the previous task (`jaxws-api`, `javax.jws-api`, `jaxb-api`) cover all runtime imports. No additional OSGi changes needed.

## Out of Scope

- `Otrs2026Client` — remains as stub
- OTRS ticket state changes on update (state changes require OTRS-specific state name strings; mapped separately if needed)
- Session-based auth (`SessionCreate`)
- Error handling beyond logging (SOAP faults propagate as runtime exceptions)

## Success Criteria

- `mvn test` passes (unit test mocks the port and verifies mapping for each method)
- `Otrs6Client` compiles with no errors against the generated stubs
- `Ticketer` is wired to `otrs6Client` in blueprint
