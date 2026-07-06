# Otrs6Client Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement `Otrs6Client` with real SOAP calls backed by the generated JAX-WS stubs, and wire it into Blueprint so `Ticketer` uses it.

**Architecture:** `Otrs6Client` receives the OTRS endpoint URL, username, and password via constructor. It obtains a `GenericTicketConnectorInterface` port from the generated `GenericTicketConnector` service class and reuses it for all calls. Each method authenticates per-call using `UserLogin`+`Password` fields. Note: `OTRSTicketSearch` uses a `getContent()` list with `ObjectFactory`-created `JAXBElement` wrappers for its fields; `OTRSTicketGet`, `OTRSTicketCreate`, and `OTRSTicketUpdate` use direct setters.

**Tech Stack:** Java 17, JAX-WS (`GenericTicketConnectorInterface`), JAXB (`OTRSTicketGet`, `OTRSTicketCreate`, etc.), OpenNMS Integration API (`ImmutableTicket`), JUnit 4, Mockito 2

## Global Constraints

- Package: `it.xeniaprogetti.rfi.plugin.example.clients`
- Generated stubs package: `it.xeniaprogetti.rfi.plugin.example.clients.otrs`
- Auth: `UserLogin` + `Password` per call — no session state
- Do NOT modify `OtrsClient` interface or `Otrs2026Client`
- `ImmutableTicket.newBuilder()` is the only way to construct `Ticket` objects
- State mapping: OTRS state string containing `"closed"` → `Ticket.State.CLOSED`; else → `Ticket.State.OPEN`
- Default hardcoded OTRS ticket fields on create: `Queue="Raw"`, `State="new"`, `Priority="3 normal"`, article `ContentType="text/plain; charset=UTF-8"`

---

## File Map

| File | Action | Responsibility |
|---|---|---|
| `plugin/src/main/java/it/xeniaprogetti/rfi/plugin/example/clients/Otrs6Client.java` | Replace | Full SOAP implementation of `OtrsClient` |
| `plugin/src/test/java/it/xeniaprogetti/rfi/plugin/example/clients/Otrs6ClientTest.java` | Create | Unit tests with Mockito-mocked port |
| `plugin/src/main/resources/OSGI-INF/blueprint/blueprint.xml` | Modify | Add 3 config properties, `otrs6Client` bean, `ticketer` bean+service |

---

### Task 1: Implement `Otrs6Client` with tests

**Files:**
- Modify: `plugin/src/main/java/it/xeniaprogetti/rfi/plugin/example/clients/Otrs6Client.java`
- Create: `plugin/src/test/java/it/xeniaprogetti/rfi/plugin/example/clients/Otrs6ClientTest.java`

**Interfaces:**
- Consumes: `GenericTicketConnectorInterface` (generated), `OTRSTicketGet`, `OTRSTicketGetResponse`, `OTRSTicketGetResponseTicket`, `OTRSTicketSearch`, `OTRSTicketSearchResponse`, `OTRSTicketCreate`, `OTRSTicketCreateTicket`, `OTRSArticle`, `OTRSTicketCreateResponse`, `OTRSTicketUpdate`, `OTRSTicketUpdateTicket`, `OTRSTicketUpdateResponse`, `ObjectFactory` (all in `...clients.otrs`), `ImmutableTicket` (`...ticketing.immutables`), `Ticket` (`...ticketing`)
- Produces: `Otrs6Client(String otrsUrl, String otrsUser, String otrsPassword)` — implementing `OtrsClient`

- [ ] **Step 1: Write the failing tests**

Create `plugin/src/test/java/it/xeniaprogetti/rfi/plugin/example/clients/Otrs6ClientTest.java`:

```java
package it.xeniaprogetti.rfi.plugin.example.clients;

import it.xeniaprogetti.rfi.plugin.example.clients.otrs.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opennms.integration.api.v1.ticketing.Ticket;
import org.opennms.integration.api.v1.ticketing.immutables.ImmutableTicket;

import java.math.BigInteger;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class Otrs6ClientTest {

    private GenericTicketConnectorInterface port;
    private Otrs6Client client;

    @Before
    public void setUp() {
        port = mock(GenericTicketConnectorInterface.class);
        // Use the package-private constructor that accepts a pre-built port (avoids real URL)
        client = new Otrs6Client(port, "testUser", "testPass");
    }

    @Test
    public void get_mapsTicketFieldsCorrectly() {
        OTRSTicketGetResponseTicket raw = new OTRSTicketGetResponseTicket();
        raw.setTicketID(BigInteger.valueOf(42));
        raw.setTitle("Test title");
        raw.setState("new");

        OTRSTicketGetResponse response = new OTRSTicketGetResponse();
        response.getTicket().add(raw);
        when(port.ticketGet(any())).thenReturn(response);

        Ticket ticket = client.get("42");

        assertThat(ticket.getId(), equalTo("42"));
        assertThat(ticket.getSummary(), equalTo("Test title"));
        assertThat(ticket.getState(), equalTo(Ticket.State.OPEN));
    }

    @Test
    public void get_returnsNullWhenResponseIsEmpty() {
        OTRSTicketGetResponse response = new OTRSTicketGetResponse();
        when(port.ticketGet(any())).thenReturn(response);

        Ticket ticket = client.get("99");

        assertThat(ticket, nullValue());
    }

    @Test
    public void get_mapsClosedStateCorrectly() {
        OTRSTicketGetResponseTicket raw = new OTRSTicketGetResponseTicket();
        raw.setTicketID(BigInteger.valueOf(7));
        raw.setTitle("Closed one");
        raw.setState("closed successful");

        OTRSTicketGetResponse response = new OTRSTicketGetResponse();
        response.getTicket().add(raw);
        when(port.ticketGet(any())).thenReturn(response);

        Ticket ticket = client.get("7");

        assertThat(ticket.getState(), equalTo(Ticket.State.CLOSED));
    }

    @Test
    public void getAll_returnsTicketsForEachId() {
        // TicketSearch returns two IDs
        OTRSTicketSearchResponse searchResp = new OTRSTicketSearchResponse();
        searchResp.getTicketID().add(BigInteger.valueOf(1));
        searchResp.getTicketID().add(BigInteger.valueOf(2));
        when(port.ticketSearch(any())).thenReturn(searchResp);

        // Each get returns a ticket
        OTRSTicketGetResponseTicket t1 = new OTRSTicketGetResponseTicket();
        t1.setTicketID(BigInteger.ONE); t1.setTitle("T1"); t1.setState("new");
        OTRSTicketGetResponse r1 = new OTRSTicketGetResponse();
        r1.getTicket().add(t1);

        OTRSTicketGetResponseTicket t2 = new OTRSTicketGetResponseTicket();
        t2.setTicketID(BigInteger.TWO); t2.setTitle("T2"); t2.setState("closed successful");
        OTRSTicketGetResponse r2 = new OTRSTicketGetResponse();
        r2.getTicket().add(t2);

        when(port.ticketGet(any())).thenReturn(r1, r2);

        List<Ticket> all = client.getAll();

        assertThat(all.size(), equalTo(2));
        assertThat(all.get(0).getId(), equalTo("1"));
        assertThat(all.get(1).getState(), equalTo(Ticket.State.CLOSED));
    }

    @Test
    public void savaORUpdate_createsWhenIdIsNull() {
        OTRSTicketCreateResponse createResp = new OTRSTicketCreateResponse();
        createResp.setTicketID(BigInteger.valueOf(55));
        when(port.ticketCreate(any())).thenReturn(createResp);

        Ticket ticket = ImmutableTicket.newBuilder()
                .setSummary("New ticket")
                .setDetails("Details here")
                .setState(Ticket.State.OPEN)
                .build();

        String id = client.savaORUpdate(ticket);

        assertThat(id, equalTo("55"));
        verify(port).ticketCreate(any());
        verify(port, never()).ticketUpdate(any());
    }

    @Test
    public void savaORUpdate_updatesWhenIdIsPresent() {
        OTRSTicketUpdateResponse updateResp = new OTRSTicketUpdateResponse();
        updateResp.setTicketID(BigInteger.valueOf(10));
        when(port.ticketUpdate(any())).thenReturn(updateResp);

        Ticket ticket = ImmutableTicket.newBuilder()
                .setId("10")
                .setSummary("Updated title")
                .setDetails("Updated body")
                .setState(Ticket.State.OPEN)
                .build();

        String id = client.savaORUpdate(ticket);

        assertThat(id, equalTo("10"));
        verify(port).ticketUpdate(any());
        verify(port, never()).ticketCreate(any());
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

```bash
cd plugin && mvn test -Dtest=Otrs6ClientTest 2>&1 | tail -15
```

Expected: FAIL — `Otrs6Client` has no constructor accepting `(GenericTicketConnectorInterface, String, String)`.

- [ ] **Step 3: Implement `Otrs6Client`**

Replace `plugin/src/main/java/it/xeniaprogetti/rfi/plugin/example/clients/Otrs6Client.java` entirely:

```java
package it.xeniaprogetti.rfi.plugin.example.clients;

import it.xeniaprogetti.rfi.plugin.example.clients.otrs.*;
import org.opennms.integration.api.v1.ticketing.Ticket;
import org.opennms.integration.api.v1.ticketing.immutables.ImmutableTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.ws.BindingProvider;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Otrs6Client implements OtrsClient {

    private static final Logger LOG = LoggerFactory.getLogger(Otrs6Client.class);

    private final GenericTicketConnectorInterface port;
    private final String otrsUser;
    private final String otrsPassword;
    private final ObjectFactory factory = new ObjectFactory();

    // Blueprint constructor
    public Otrs6Client(String otrsUrl, String otrsUser, String otrsPassword) {
        this.otrsUser = otrsUser;
        this.otrsPassword = otrsPassword;
        try {
            GenericTicketConnector service = new GenericTicketConnector(new URL(otrsUrl));
            this.port = service.getGenericTicketConnectorEndPoint();
            ((BindingProvider) port).getRequestContext()
                    .put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, otrsUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid OTRS URL: " + otrsUrl, e);
        }
    }

    // Test constructor — accepts a pre-built mock port
    Otrs6Client(GenericTicketConnectorInterface port, String otrsUser, String otrsPassword) {
        this.port = port;
        this.otrsUser = otrsUser;
        this.otrsPassword = otrsPassword;
    }

    @Override
    public List<Ticket> getAll() {
        OTRSTicketSearch search = factory.createOTRSTicketSearch();
        search.getContent().add(factory.createOTRSTicketSearchUserLogin(otrsUser));
        search.getContent().add(factory.createOTRSTicketSearchPassword(otrsPassword));

        OTRSTicketSearchResponse response = port.ticketSearch(search);
        List<Ticket> tickets = new ArrayList<>();
        for (BigInteger id : response.getTicketID()) {
            Ticket t = get(id.toString());
            if (t != null) {
                tickets.add(t);
            }
        }
        return tickets;
    }

    @Override
    public Ticket get(String ticketId) {
        OTRSTicketGet req = new OTRSTicketGet();
        req.setUserLogin(otrsUser);
        req.setPassword(otrsPassword);
        req.setTicketID(new BigInteger(ticketId));

        OTRSTicketGetResponse response = port.ticketGet(req);
        if (response.getTicket().isEmpty()) {
            return null;
        }
        return toTicket(response.getTicket().get(0));
    }

    @Override
    public String savaORUpdate(Ticket ticket) {
        if (ticket.getId() == null || ticket.getId().isBlank()) {
            return create(ticket);
        }
        return update(ticket);
    }

    private String create(Ticket ticket) {
        OTRSTicketCreateTicket t = new OTRSTicketCreateTicket();
        t.setTitle(ticket.getSummary());
        t.setQueue("Raw");
        t.setState("new");
        t.setPriority("3 normal");
        t.setCustomerUser(otrsUser);

        OTRSArticle article = new OTRSArticle();
        article.setSubject(ticket.getSummary());
        article.setBody(ticket.getDetails() != null ? ticket.getDetails() : "");
        article.setContentType("text/plain; charset=UTF-8");

        OTRSTicketCreate req = new OTRSTicketCreate();
        req.setUserLogin(otrsUser);
        req.setPassword(otrsPassword);
        req.setTicket(t);
        req.setArticle(article);

        OTRSTicketCreateResponse response = port.ticketCreate(req);
        return response.getTicketID().toString();
    }

    private String update(Ticket ticket) {
        OTRSTicketUpdateTicket t = new OTRSTicketUpdateTicket();
        t.setTitle(ticket.getSummary());

        OTRSTicketUpdate req = new OTRSTicketUpdate();
        req.setUserLogin(otrsUser);
        req.setPassword(otrsPassword);
        req.setTicketID(new BigInteger(ticket.getId()));
        req.setTicket(t);

        OTRSTicketUpdateResponse response = port.ticketUpdate(req);
        return response.getTicketID().toString();
    }

    private Ticket toTicket(OTRSTicketGetResponseTicket raw) {
        Ticket.State state = raw.getState() != null && raw.getState().contains("closed")
                ? Ticket.State.CLOSED
                : Ticket.State.OPEN;
        return ImmutableTicket.newBuilder()
                .setId(raw.getTicketID().toString())
                .setSummary(raw.getTitle())
                .setDetails(raw.getState())
                .setState(state)
                .build();
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

```bash
cd plugin && mvn test -Dtest=Otrs6ClientTest 2>&1 | tail -15
```

Expected:
```
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

- [ ] **Step 5: Run full test suite to check for regressions**

```bash
cd plugin && mvn test 2>&1 | tail -10
```

Expected: `Tests run: 59, Failures: 0, Errors: 0, Skipped: 0` (53 existing + 6 new)

- [ ] **Step 6: Commit**

```bash
git add plugin/src/main/java/it/xeniaprogetti/rfi/plugin/example/clients/Otrs6Client.java \
        plugin/src/test/java/it/xeniaprogetti/rfi/plugin/example/clients/Otrs6ClientTest.java
git commit -m "feat: implement Otrs6Client using generated JAX-WS stubs"
```

---

### Task 2: Wire `Otrs6Client` into Blueprint

**Files:**
- Modify: `plugin/src/main/resources/OSGI-INF/blueprint/blueprint.xml`

**Interfaces:**
- Consumes: `Otrs6Client(String, String, String)` from Task 1
- Produces: `otrs6Client` OSGi bean; `ticketer` OSGi service under `TicketingPlugin`

- [ ] **Step 1: Add the three new config properties to the `cm:default-properties` block**

In `blueprint.xml`, find the existing `<cm:default-properties>` block (currently containing `url` and `apiKey`) and add:

```xml
    <cm:property-placeholder id="rfi-plugin-examplePluginProperties" persistent-id="it.xeniaprogetti.rfi.plugin.example" update-strategy="reload">
        <cm:default-properties>
            <cm:property name="url" value="http://127.0.0.1:8888/target" />
            <cm:property name="apiKey" value="TOKEN" />
            <cm:property name="otrsUrl" value="http://127.0.0.1/otrs/nph-genericinterface.pl/Webservice/GenericTicketConnector" />
            <cm:property name="otrsUser" value="root@localhost" />
            <cm:property name="otrsPassword" value="root" />
        </cm:default-properties>
    </cm:property-placeholder>
```

- [ ] **Step 2: Add `otrs6Client` bean and `ticketer` bean+service**

In `blueprint.xml`, after the existing `<bean id="apiClient" ...>` block and before `<bean id="connectionManager" ...>`, add:

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

- [ ] **Step 3: Verify the bundle compiles**

```bash
cd plugin && mvn clean compile 2>&1 | tail -10
```

Expected: `BUILD SUCCESS`

- [ ] **Step 4: Run the full test suite**

```bash
cd plugin && mvn test 2>&1 | tail -10
```

Expected: `BUILD SUCCESS` — all tests pass.

- [ ] **Step 5: Commit**

```bash
git add plugin/src/main/resources/OSGI-INF/blueprint/blueprint.xml
git commit -m "feat: wire Otrs6Client and Ticketer into OSGi blueprint"
```
