# WSDL Java Generation Design

**Date:** 2026-07-06
**Status:** Approved

## Goal

Generate Java stubs from `GenericTicketConnector.wsdl` (the OTRS SOAP API) into the plugin module using Apache CXF's `cxf-codegen-plugin`. Scope is build-time code generation only; wiring generated classes into existing `OtrsClient` implementations is a separate task.

## Context

- One WSDL: `plugin/src/main/wsdl/GenericTicketConnector.wsdl`
- SOAP operations: `TicketCreate`, `TicketUpdate`, `TicketGet`, `TicketSearch`, `SessionCreate`
- Existing stubs: `OtrsClient` interface, `Otrs6Client`, `Otrs2026Client` — all empty, untouched by this change
- `plugin/pom.xml` already has `build-helper-maven-plugin` adding `target/generated-sources` as a compile source root
- Java 17, OSGi bundle packaging, Karaf 4.3.10 runtime

## Approach

**Approach B: Fix the WSDL, then generate.**

The WSDL contains three duplicate XSD element declarations (copy/paste bugs from the original 2012 OTRS file) that cause JAXB codegen to fail. Fix these first, then add a minimal plugin configuration.

## Changes

### 1. Fix `plugin/src/main/wsdl/GenericTicketConnector.wsdl`

Remove the second (duplicate) occurrence of each element:

| Complex type | Duplicate element |
|---|---|
| `OTRS_TicketGet` | `DynamicFields` (second occurrence, ~line 744) |
| `OTRS_TicketGetResponse_Article` | `ToRealname` (second occurrence, ~line 1344) |
| `OTRS_TicketSearch` | `CreatedStateIDs` (second occurrence, ~line 1648) |

### 2. Add `cxf-codegen-plugin` to `plugin/pom.xml`

```xml
<plugin>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf-codegen-plugin</artifactId>
    <version>3.5.8</version>
    <executions>
        <execution>
            <id>generate-sources</id>
            <phase>generate-sources</phase>
            <goals><goal>wsdl2java</goal></goals>
            <configuration>
                <wsdlOptions>
                    <wsdlOption>
                        <wsdl>${project.basedir}/src/main/wsdl/GenericTicketConnector.wsdl</wsdl>
                        <packagenames>
                            <packagename>it.xeniaprogetti.rfi.plugin.example.clients.otrs</packagename>
                        </packagenames>
                    </wsdlOption>
                </wsdlOptions>
            </configuration>
        </execution>
    </executions>
</plugin>
```

CXF 3.5.8 (latest stable 3.x) uses `javax.*` namespaces, matching Karaf 4.3.x's bundled CXF runtime. Output lands in `target/generated-sources/cxf`, which is already on the compile source path via `build-helper-maven-plugin`.

### 3. Add JAXB API dependency to `plugin/pom.xml`

Java 17 removed JAXB from the JDK. The generated code requires it at compile time:

```xml
<dependency>
    <groupId>javax.xml.bind</groupId>
    <artifactId>jaxb-api</artifactId>
    <version>2.3.1</version>
    <scope>provided</scope>
</dependency>
```

`provided` scope is correct — Karaf 4.3.x ships JAXB at runtime via its system bundle.

## OSGi Considerations

The generated classes use `javax.xml.ws.*` and `javax.xml.bind.*`. Felix BND auto-detects these as `Import-Package` entries from compiled bytecode. Both packages are exposed by Karaf 4.3.x's system bundle, so no manual OSGi configuration is required.

No changes to `blueprint.xml` — generated classes are plain JAXB/JAX-WS POJOs with no OSGi lifecycle.

## Success Criteria

- `mvn clean compile` on the `plugin` module succeeds with no errors
- `target/generated-sources/cxf/it/xeniaprogetti/rfi/plugin/example/clients/otrs/` contains generated Java files including `GenericTicketConnector.java` (service class) and JAXB types for all WSDL complex types
- No changes to existing `OtrsClient`, `Otrs6Client`, `Otrs2026Client`
