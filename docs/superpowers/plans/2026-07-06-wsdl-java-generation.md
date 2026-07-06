# WSDL Java Generation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Generate Java stubs from `GenericTicketConnector.wsdl` into the plugin module via Apache CXF's `cxf-codegen-plugin`, so that `mvn clean compile` produces JAX-WS/JAXB classes in `target/generated-sources/cxf/`.

**Architecture:** The WSDL contains three copy/paste bugs (duplicate XSD element names) that must be removed before codegen can succeed. Once fixed, `cxf-codegen-plugin` is added to `plugin/pom.xml` bound to the `generate-sources` phase; the existing `build-helper-maven-plugin` already adds `target/generated-sources` to the compile source path, so no further source-root wiring is needed.

**Tech Stack:** Apache CXF 3.5.8 (`cxf-codegen-plugin`), JAXB API 2.3.1, Maven 3.x, Java 17

## Global Constraints

- CXF version: 3.5.8 (3.x series — uses `javax.*`, not `jakarta.*`, matching Karaf 4.3.10)
- JAXB API version: 2.3.1, scope `provided`
- Target package: `it.xeniaprogetti.rfi.plugin.example.clients.otrs`
- WSDL path: `plugin/src/main/wsdl/GenericTicketConnector.wsdl`
- Output directory: `${project.build.directory}/generated-sources/cxf` (default for cxf-codegen-plugin)
- Do NOT modify `OtrsClient`, `Otrs6Client`, or `Otrs2026Client`

---

## File Map

| File | Action | Purpose |
|---|---|---|
| `plugin/src/main/wsdl/GenericTicketConnector.wsdl` | Modify | Remove 3 duplicate XSD element declarations |
| `plugin/pom.xml` | Modify | Add `cxf-codegen-plugin` build plugin + `jaxb-api` dependency |

---

### Task 1: Fix Duplicate XSD Elements in the WSDL

**Files:**
- Modify: `plugin/src/main/wsdl/GenericTicketConnector.wsdl`

**Interfaces:**
- Produces: a valid WSDL that CXF codegen can process without name-collision errors

There are three duplicate element declarations in the WSDL. Each is a copy/paste mistake — the first occurrence is correct, the second must be removed.

- [ ] **Step 1: Remove the second `DynamicFields` in `OTRS_TicketGet`**

Find the complex type `OTRS_TicketGet`. It has two `<xsd:element name="DynamicFields" ...>` declarations. The first is around line 731; the second is around line 744. Remove this second block:

```xml
                    <xsd:element
                        name="DynamicFields"
                        type="xsd:positiveInteger"
                        minOccurs="0" maxOccurs="1">
                    </xsd:element>
```

Only the *second* occurrence (the one that comes after `AllArticles`) should be removed. The first occurrence (which comes before `Extended`) stays.

- [ ] **Step 2: Remove the second `ToRealname` in `OTRS_TicketGetResponse_Article`**

Find the complex type `OTRS_TicketGetResponse_Article`. It has two `<xsd:element name="ToRealname" ...>` declarations. Remove the second block:

```xml
                    <xsd:element
                        name="ToRealname"
                        type="xsd:string"
                        ninOccurs="1"
                        maxOccurs="1">
                    </xsd:element>
```

Keep the first `ToRealname` element; remove only the duplicate that immediately follows it.

- [ ] **Step 3: Remove the second `CreatedStateIDs` in `OTRS_TicketSearch`**

Find the complex type `OTRS_TicketSearch`. It has two `<xsd:element name="CreatedStateIDs" ...>` declarations. Remove the second block:

```xml
                    <xsd:element
                        name="CreatedStateIDs"
                        type="xsd:positiveInteger"
                        minOccurs="0"
                        maxOccurs="unbounded">
                    </xsd:element>
```

Keep the first `CreatedStateIDs` (which comes right after `CreatedQueues`); remove the duplicate that follows.

- [ ] **Step 4: Verify the WSDL has no remaining duplicates**

Run:
```bash
grep -n "name=\"DynamicFields\"" plugin/src/main/wsdl/GenericTicketConnector.wsdl
grep -n "name=\"ToRealname\""    plugin/src/main/wsdl/GenericTicketConnector.wsdl
grep -n "name=\"CreatedStateIDs\"" plugin/src/main/wsdl/GenericTicketConnector.wsdl
```

Expected: each grep returns **exactly one line**.

- [ ] **Step 5: Commit**

```bash
git add plugin/src/main/wsdl/GenericTicketConnector.wsdl
git commit -m "fix: remove duplicate XSD elements in GenericTicketConnector.wsdl"
```

---

### Task 2: Add CXF Codegen Plugin and JAXB Dependency to plugin/pom.xml

**Files:**
- Modify: `plugin/pom.xml`

**Interfaces:**
- Consumes: fixed WSDL from Task 1
- Produces: `mvn clean compile` generates Java classes under `target/generated-sources/cxf/it/xeniaprogetti/rfi/plugin/example/clients/otrs/`

- [ ] **Step 1: Add `cxf-codegen-plugin` to the `<build><plugins>` section**

In `plugin/pom.xml`, inside the existing `<build><plugins>` block (after the closing `</plugin>` of `build-helper-maven-plugin`), add:

```xml
            <plugin>
                <groupId>org.apache.cxf</groupId>
                <artifactId>cxf-codegen-plugin</artifactId>
                <version>3.5.8</version>
                <executions>
                    <execution>
                        <id>generate-sources</id>
                        <phase>generate-sources</phase>
                        <goals>
                            <goal>wsdl2java</goal>
                        </goals>
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

- [ ] **Step 2: Add `jaxb-api` dependency**

In `plugin/pom.xml`, inside the `<dependencies>` block (after the last existing compile-scope dependency, before the `<!-- Test -->` comment), add:

```xml
        <dependency>
            <groupId>javax.xml.bind</groupId>
            <artifactId>jaxb-api</artifactId>
            <version>2.3.1</version>
            <scope>provided</scope>
        </dependency>
```

- [ ] **Step 3: Run `mvn clean compile` on the plugin module**

```bash
cd plugin && mvn clean compile
```

Expected: BUILD SUCCESS with no errors. You will see CXF log output similar to:
```
[INFO] --- cxf-codegen-plugin:3.5.8:wsdl2java (generate-sources) @ rfi-plugin-example-plugin ---
[INFO] Running wsdl2java with -d .../target/generated-sources/cxf ...
```

If you see `Duplicate element` or `already defined` JAXB errors, a duplicate was not fully removed in Task 1 — go back and re-check that grep returns exactly one line for each element name.

- [ ] **Step 4: Verify generated sources exist**

```bash
find plugin/target/generated-sources/cxf -name "*.java" | sort
```

Expected output includes at minimum:
```
plugin/target/generated-sources/cxf/it/xeniaprogetti/rfi/plugin/example/clients/otrs/GenericTicketConnector.java
plugin/target/generated-sources/cxf/it/xeniaprogetti/rfi/plugin/example/clients/otrs/GenericTicketConnectorInterface.java
plugin/target/generated-sources/cxf/it/xeniaprogetti/rfi/plugin/example/clients/otrs/OTRSTicketCreate.java
plugin/target/generated-sources/cxf/it/xeniaprogetti/rfi/plugin/example/clients/otrs/OTRSTicketCreateResponse.java
```

The exact filenames may vary slightly by CXF version but the package directory and service class must be present.

- [ ] **Step 5: Commit**

```bash
cd ..
git add plugin/pom.xml
git commit -m "feat: add cxf-codegen-plugin to generate Java from GenericTicketConnector.wsdl"
```
