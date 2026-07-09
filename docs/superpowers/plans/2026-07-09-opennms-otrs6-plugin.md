# opennms-otrs6-plugin Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create a new, standalone OpenNMS plugin project, `opennms-otrs6-plugin`, that implements OpenNMS's `TicketingPlugin` API against an OTRS6 `GenericTicketConnector` SOAP endpoint — carved out of the ticketing-related code currently living inside `rfi-plugin-example`.

**Architecture:** Same three-module Maven/OSGi/Karaf skeleton as `rfi-plugin-example` (parent pom, `plugin` OSGi bundle, `karaf-features`, `assembly/kar`), trimmed to only what the ticketing pipeline needs: `Ticketer` (implements `TicketingPlugin`, wired via OSGi Blueprint) delegates to `OtrsClient`, implemented by `Otrs6Client` against JAX-WS stubs generated at build time from `GenericTicketConnector.wsdl` via `cxf-codegen-plugin`.

**Tech Stack:** Java 17, Maven, OSGi/Apache Karaf 4.3.10, OSGi Blueprint (Aries), Apache CXF `cxf-codegen-plugin` (wsdl2java), JAX-WS/JAXB, OpenNMS Integration API 1.6.1, JUnit 4 + Mockito 2 + Hamcrest.

## Global Constraints

- New repo root: `/Users/antonio/Rcs/rssntn67/opennms-otrs6-plugin`, fresh independent git repository.
- groupId: `it.arsinfo.opennms.plugins`
- base Java package: `it.arsinfo.opennms.plugins.otrs6`
- parent artifactId: `opennms-otrs6-plugin`
- plugin module artifactId: `otrs6-plugin` (directory `plugin/`)
- karaf feature name: `opennms-plugins-otrs6`
- KAR final name: `opennms-otrs6-plugin`
- config PID: `it.arsinfo.opennms.plugins.otrs6` (file `it.arsinfo.opennms.plugins.otrs6.cfg`)
- version: `0.1.0-SNAPSHOT`
- Java version: 17
- Do not carry over: alarm forwarding, topology forwarding, SNMP connection management, provisioning, vendor event ingestors, REST webhook, IDE metadata files. Full rationale in `docs/superpowers/specs/2026-07-09-opennms-otrs6-plugin-design.md` (in the `rfi-plugin-example` repo).
- Commit messages: do NOT add a `Co-Authored-By` / `Claude-Session` trailer.

---

### Task 1: Scaffold the Maven multi-module skeleton and git repo

**Files:**
- Create: `/Users/antonio/Rcs/rssntn67/opennms-otrs6-plugin/.gitignore`
- Create: `/Users/antonio/Rcs/rssntn67/opennms-otrs6-plugin/pom.xml`
- Create: `/Users/antonio/Rcs/rssntn67/opennms-otrs6-plugin/assembly/pom.xml`
- Create: `/Users/antonio/Rcs/rssntn67/opennms-otrs6-plugin/assembly/kar/pom.xml`
- Create: `/Users/antonio/Rcs/rssntn67/opennms-otrs6-plugin/karaf-features/pom.xml`
- Create: `/Users/antonio/Rcs/rssntn67/opennms-otrs6-plugin/karaf-features/src/main/resources/features.xml`
- Create: `/Users/antonio/Rcs/rssntn67/opennms-otrs6-plugin/plugin/pom.xml`

**Interfaces:**
- Produces: the full 5-pom Maven reactor (`opennms-otrs6-plugin` parent, `assembly`, `kar`, `karaf-features`, `otrs6-plugin`) that every later task builds inside. Plugin module artifactId `otrs6-plugin`, groupId `it.arsinfo.opennms.plugins`, is what `karaf-features/pom.xml` and `features.xml` reference as a dependency/bundle.

- [ ] **Step 1: Create the directory and initialize git**

```bash
mkdir -p /Users/antonio/Rcs/rssntn67/opennms-otrs6-plugin
git -C /Users/antonio/Rcs/rssntn67/opennms-otrs6-plugin init
```

- [ ] **Step 2: Create `.gitignore`**

```
*~
.swp
.*.swp
.swp
.gwt
.gwt-tmp
target
.classpath
.externalToolBuilders
.project
.settings
.springBeans
.vscode
.DS_Store
*.iml
*.iws
*.ipr
.launch
.*launch
.idea
.jqwik-database
npm-debug.log
yarn-error.log
.factorypath
.password

.project
*.bak

.settings/

# Ignore jenv local version files
.java-version

# Antora artifacts
/build/
/public/
```

- [ ] **Step 3: Create the parent `pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <artifactId>opennms-otrs6-plugin</artifactId>
    <groupId>it.arsinfo.opennms.plugins</groupId>
    <name>OpenNMS :: Plugins :: OTRS6 Ticketing Plugin :: Parent</name>
    <packaging>pom</packaging>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <hamcrest.version>1.3</hamcrest.version>
        <java.version>17</java.version>
        <junit.version>4.13.1</junit.version>
        <karaf.version>4.3.10</karaf.version>
        <log4j.version>2.17.2</log4j.version>
        <mockito.version>2.18.0</mockito.version>
        <opennms.api.version>1.6.1</opennms.api.version>
        <slf4j-api.version>1.7.30</slf4j-api.version>
    </properties>

    <version>0.1.0-SNAPSHOT</version>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.opennms.integration.api</groupId>
                <artifactId>common</artifactId>
                <version>${opennms.api.version}</version>
            </dependency>
            <dependency>
                <groupId>org.slf4j</groupId>
                <artifactId>slf4j-api</artifactId>
                <version>${slf4j-api.version}</version>
            </dependency>

            <!-- Test -->
            <dependency>
                <groupId>junit</groupId>
                <artifactId>junit</artifactId>
                <version>${junit.version}</version>
                <scope>test</scope>
            </dependency>
            <dependency>
                <groupId>org.hamcrest</groupId>
                <artifactId>hamcrest-library</artifactId>
                <version>${hamcrest.version}</version>
                <scope>test</scope>
            </dependency>
            <dependency>
                <groupId>org.mockito</groupId>
                <artifactId>mockito-core</artifactId>
                <version>${mockito.version}</version>
                <scope>test</scope>
            </dependency>
            <dependency>
                <groupId>org.apache.logging.log4j</groupId>
                <artifactId>log4j-core</artifactId>
                <version>${log4j.version}</version>
                <scope>test</scope>
            </dependency>
            <dependency>
                <groupId>org.apache.logging.log4j</groupId>
                <artifactId>log4j-slf4j-impl</artifactId>
                <version>${log4j.version}</version>
                <scope>test</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.apache.felix</groupId>
                    <artifactId>maven-bundle-plugin</artifactId>
                    <version>5.1.9</version>
                    <extensions>true</extensions>
                    <configuration>
                        <instructions>
                            <Bundle-RequiredExecutionEnvironment>JavaSE-${java.version}</Bundle-RequiredExecutionEnvironment>
                        </instructions>
                    </configuration>
                </plugin>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <version>3.5.1</version>
                    <configuration>
                        <source>${java.version}</source>
                        <target>${java.version}</target>
                    </configuration>
                </plugin>
                <plugin>
                    <groupId>org.codehaus.mojo</groupId>
                    <artifactId>build-helper-maven-plugin</artifactId>
                    <version>3.0.0</version>
                </plugin>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-resources-plugin</artifactId>
                    <version>3.0.2</version>
                </plugin>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-surefire-plugin</artifactId>
                    <version>2.22.1</version>
                    <configuration>
                        <excludes>
                            <exclude>**/*IT.java</exclude>
                        </excludes>
                    </configuration>
                    <executions>
                        <execution>
                            <id>integration-test</id>
                            <goals>
                                <goal>test</goal>
                            </goals>
                            <phase>integration-test</phase>
                            <configuration>
                                <excludes>
                                    <exclude>none</exclude>
                                </excludes>
                                <includes>
                                    <include>**/*IT.java</include>
                                </includes>
                            </configuration>
                        </execution>
                    </executions>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>

    <repositories>
        <repository>
            <id>sonatype.org-snapshot</id>
            <name>Sonatype OSS Snapshots Repository</name>
            <url>https://oss.sonatype.org/content/repositories/snapshots</url>
            <releases>
                <enabled>false</enabled>
            </releases>
            <snapshots>
            </snapshots>
        </repository>
    </repositories>

    <modules>
        <module>assembly</module>
        <module>plugin</module>
        <module>karaf-features</module>
    </modules>
</project>
```

- [ ] **Step 4: Create `assembly/pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?><project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <parent>
    <groupId>it.arsinfo.opennms.plugins</groupId>
    <artifactId>opennms-otrs6-plugin</artifactId>
    <version>0.1.0-SNAPSHOT</version>
  </parent>
  <modelVersion>4.0.0</modelVersion>
  <artifactId>assembly</artifactId>
  <name>OpenNMS :: Plugins :: OTRS6 Ticketing Plugin :: Assembly</name>
  <packaging>pom</packaging>
  <modules>
    <module>kar</module>
  </modules>
</project>
```

- [ ] **Step 5: Create `assembly/kar/pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <groupId>it.arsinfo.opennms.plugins</groupId>
        <artifactId>assembly</artifactId>
        <version>0.1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>
    <artifactId>kar</artifactId>
    <name>OpenNMS :: Plugins :: OTRS6 Ticketing Plugin :: Assembly :: KAR</name>
    <packaging>kar</packaging>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.karaf.tooling</groupId>
                <artifactId>karaf-maven-plugin</artifactId>
                <version>${karaf.version}</version>
                <extensions>true</extensions>
                <executions>
                    <execution>
                        <id>create-kar</id>
                        <goals>
                            <goal>kar</goal>
                        </goals>
                        <configuration>
                            <featuresFile>mvn:it.arsinfo.opennms.plugins/karaf-features/${project.version}/xml</featuresFile>
                            <finalName>opennms-otrs6-plugin</finalName>
                            <ignoreDependencyFlag>true</ignoreDependencyFlag>
                            <archive>
                              <manifestEntries>
                                <Karaf-Feature-Start>false</Karaf-Feature-Start>
                              </manifestEntries>
                            </archive>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

    <dependencies>
        <dependency>
            <groupId>it.arsinfo.opennms.plugins</groupId>
            <artifactId>karaf-features</artifactId>
            <version>${project.version}</version>
            <type>xml</type>
            <scope>provided</scope>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 6: Create `karaf-features/pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <groupId>it.arsinfo.opennms.plugins</groupId>
        <artifactId>opennms-otrs6-plugin</artifactId>
        <version>0.1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>
    <artifactId>karaf-features</artifactId>
    <name>OpenNMS :: Plugins :: OTRS6 Ticketing Plugin :: Karaf Features</name>
    <packaging>pom</packaging>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-resources-plugin</artifactId>
                <executions>
                    <execution>
                        <id>copy-resources</id>
                        <phase>generate-resources</phase>
                        <goals>
                            <goal>copy-resources</goal>
                        </goals>
                        <configuration>
                            <outputDirectory>${project.build.outputDirectory}</outputDirectory>
                            <resources>
                                <resource>
                                    <directory>src/main/resources</directory>
                                    <filtering>true</filtering>
                                </resource>
                            </resources>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>build-helper-maven-plugin</artifactId>
                <executions>
                    <execution>
                        <id>attach-artifacts</id>
                        <phase>package</phase>
                        <goals>
                            <goal>attach-artifact</goal>
                        </goals>
                        <configuration>
                            <artifacts>
                                <artifact>
                                    <file>${project.build.outputDirectory}/features.xml</file>
                                    <type>xml</type>
                                </artifact>
                            </artifacts>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <groupId>org.apache.karaf.tooling</groupId>
                <artifactId>karaf-maven-plugin</artifactId>
                <version>${karaf.version}</version>
                <extensions>true</extensions>
                <configuration>
                  <skip>${skipTests}</skip>
                </configuration>
                <executions>
                    <execution>
                        <id>verify</id>
                        <phase>process-resources</phase>
                        <goals>
                            <goal>verify</goal>
                        </goals>
                        <configuration>
                            <javase>${java.version}</javase>
                            <descriptors>
                                <descriptor>mvn:org.apache.karaf.features/framework/${karaf.version}/xml/features</descriptor>
                                <descriptor>mvn:org.apache.karaf.features/standard/${karaf.version}/xml/features</descriptor>
                                <descriptor>mvn:org.opennms.integration.api/karaf-features/${opennms.api.version}/xml</descriptor>
                                <descriptor>file:${project.build.directory}/classes/features.xml</descriptor>
                            </descriptors>
                            <distribution>org.apache.karaf.features:framework</distribution>
                            <framework>
                                <feature>framework</feature>
                            </framework>
                            <features>
                                <feature>opennms-plugins-otrs6</feature>
                            </features>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

    <dependencies>
        <!-- Framework distribution -->
        <dependency>
            <groupId>org.apache.karaf.features</groupId>
            <artifactId>framework</artifactId>
            <version>${karaf.version}</version>
            <type>kar</type>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.opennms.integration.api</groupId>
            <artifactId>karaf-features</artifactId>
            <version>${opennms.api.version}</version>
            <type>xml</type>
            <scope>provided</scope>
        </dependency>

        <dependency>
            <groupId>it.arsinfo.opennms.plugins</groupId>
            <artifactId>otrs6-plugin</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 7: Create `karaf-features/src/main/resources/features.xml`**

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<features name="opennms-plugins-otrs6-features" xmlns="http://karaf.apache.org/xmlns/features/v1.4.0">

    <feature name="opennms-plugins-otrs6" description="OpenNMS :: Plugins :: OTRS6 Ticketing Plugin" version="${project.version}">
        <feature dependency="true">aries-blueprint</feature>
        <feature dependency="true">shell</feature>
        <feature version="${opennms.api.version}" dependency="true">opennms-integration-api</feature>
        <bundle>mvn:it.arsinfo.opennms.plugins/otrs6-plugin/${project.version}</bundle>
    </feature>

</features>
```

- [ ] **Step 8: Create `plugin/pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <groupId>it.arsinfo.opennms.plugins</groupId>
        <artifactId>opennms-otrs6-plugin</artifactId>
        <version>0.1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>
    <artifactId>otrs6-plugin</artifactId>
    <name>OpenNMS :: Plugins :: OTRS6 Ticketing Plugin :: Plugin</name>
    <packaging>bundle</packaging>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.felix</groupId>
                <artifactId>maven-bundle-plugin</artifactId>
                <extensions>true</extensions>
                <configuration>
                    <instructions>
                        <!-- Nothing to export -->
                        <Export-Package></Export-Package>
                    </instructions>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>build-helper-maven-plugin</artifactId>
                <executions>
                    <execution>
                        <id>add-generated-sources</id>
                        <phase>generate-sources</phase>
                        <goals>
                            <goal>add-source</goal>
                        </goals>
                        <configuration>
                            <sources>
                                <source>${basedir}/target/generated-sources</source>
                            </sources>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
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
                                        <packagename>it.arsinfo.opennms.plugins.otrs6.clients.otrs</packagename>
                                    </packagenames>
                                </wsdlOption>
                            </wsdlOptions>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

    <dependencies>
        <dependency>
            <groupId>org.opennms.integration.api</groupId>
            <artifactId>common</artifactId>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
        </dependency>
        <dependency>
            <groupId>org.apache.karaf.shell</groupId>
            <artifactId>org.apache.karaf.shell.core</artifactId>
            <version>${karaf.version}</version>
        </dependency>

        <dependency>
            <groupId>javax.xml.bind</groupId>
            <artifactId>jaxb-api</artifactId>
            <version>2.3.1</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>javax.xml.ws</groupId>
            <artifactId>jaxws-api</artifactId>
            <version>2.3.1</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>javax.jws</groupId>
            <artifactId>javax.jws-api</artifactId>
            <version>1.1</version>
            <scope>provided</scope>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-core</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.hamcrest</groupId>
            <artifactId>hamcrest-library</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-core</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-slf4j-impl</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 9: Verify the reactor parses**

Run: `mvn -f /Users/antonio/Rcs/rssntn67/opennms-otrs6-plugin/pom.xml validate`
Expected: `BUILD SUCCESS` for all 5 modules (no source code exists yet, so this only checks POM correctness — do not run `compile` yet, `plugin` has no `src/main/wsdl` file until Task 2).

---

### Task 2: Copy the WSDL and verify SOAP stub generation

**Files:**
- Create: `/Users/antonio/Rcs/rssntn67/opennms-otrs6-plugin/plugin/src/main/wsdl/GenericTicketConnector.wsdl` (copy of `/Users/antonio/Rcs/rssntn67/rfi-plugin-example/plugin/src/main/wsdl/GenericTicketConnector.wsdl`, byte-for-byte, no changes needed)

**Interfaces:**
- Consumes: `plugin/pom.xml` from Task 1 (the `cxf-codegen-plugin` execution already targets this exact path and package `it.arsinfo.opennms.plugins.otrs6.clients.otrs`).
- Produces: generated JAX-WS stub classes under `it.arsinfo.opennms.plugins.otrs6.clients.otrs` (in `plugin/target/generated-sources`) — `GenericTicketConnector`, `GenericTicketConnectorInterface`, `ObjectFactory`, `OTRSTicketGet`, `OTRSTicketGetResponse`, `OTRSTicketGetResponseTicket`, `OTRSTicketCreate`, `OTRSTicketCreateResponse`, `OTRSTicketCreateTicket`, `OTRSTicketUpdate`, `OTRSTicketUpdateResponse`, `OTRSTicketUpdateTicket`, `OTRSTicketSearch`, `OTRSTicketSearchResponse`, `OTRSArticle`, used by Task 3 (`Otrs6Client`) and Task 5 (`Otrs6ClientTest`).

- [ ] **Step 1: Copy the WSDL file**

```bash
mkdir -p /Users/antonio/Rcs/rssntn67/opennms-otrs6-plugin/plugin/src/main/wsdl
cp /Users/antonio/Rcs/rssntn67/rfi-plugin-example/plugin/src/main/wsdl/GenericTicketConnector.wsdl \
   /Users/antonio/Rcs/rssntn67/opennms-otrs6-plugin/plugin/src/main/wsdl/GenericTicketConnector.wsdl
```

- [ ] **Step 2: Generate the SOAP stubs and verify they compile**

Run: `mvn -f /Users/antonio/Rcs/rssntn67/opennms-otrs6-plugin/pom.xml -pl plugin -am generate-sources`
Expected: `BUILD SUCCESS`; `plugin/target/generated-sources/it/arsinfo/opennms/plugins/otrs6/clients/otrs/` contains the generated `.java` files listed above.

---

### Task 3: Port `OtrsClient` and `Otrs6Client`

**Files:**
- Create: `/Users/antonio/Rcs/rssntn67/opennms-otrs6-plugin/plugin/src/main/java/it/arsinfo/opennms/plugins/otrs6/clients/OtrsClient.java`
- Create: `/Users/antonio/Rcs/rssntn67/opennms-otrs6-plugin/plugin/src/main/java/it/arsinfo/opennms/plugins/otrs6/clients/Otrs6Client.java`

**Interfaces:**
- Consumes: generated SOAP stubs from Task 2 (`it.arsinfo.opennms.plugins.otrs6.clients.otrs.*`), `org.opennms.integration.api.v1.ticketing.Ticket` / `Ticket.State` / `ImmutableTicket` (from the `org.opennms.integration.api:common` dependency already in `plugin/pom.xml`).
- Produces: `OtrsClient` interface (`getAll(): List<Ticket>`, `get(String ticketId): Ticket`, `savaORUpdate(Ticket ticket): String`) and its `Otrs6Client` implementation (constructors `Otrs6Client(String otrsUrl, String otrsUser, String otrsPassword)` and package-private `Otrs6Client(GenericTicketConnectorInterface port, String otrsUser, String otrsPassword)`), consumed by Task 4 (`Ticketer`, blueprint) and Task 5 (`Otrs6ClientTest`).

- [ ] **Step 1: Create `OtrsClient.java`**

```java
package it.arsinfo.opennms.plugins.otrs6.clients;

import org.opennms.integration.api.v1.ticketing.Ticket;

import java.util.List;

public interface OtrsClient {
    List<Ticket> getAll();
    Ticket get(String ticketId);
    String savaORUpdate(Ticket ticket);
}
```

- [ ] **Step 2: Create `Otrs6Client.java`**

```java
package it.arsinfo.opennms.plugins.otrs6.clients;

import it.arsinfo.opennms.plugins.otrs6.clients.otrs.*;
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

- [ ] **Step 3: Verify it compiles**

Run: `mvn -f /Users/antonio/Rcs/rssntn67/opennms-otrs6-plugin/pom.xml -pl plugin -am compile`
Expected: `BUILD SUCCESS`

- [ ] **Step 4: Commit**

```bash
git -C /Users/antonio/Rcs/rssntn67/opennms-otrs6-plugin add plugin/src/main/java/it/arsinfo/opennms/plugins/otrs6/clients
git -C /Users/antonio/Rcs/rssntn67/opennms-otrs6-plugin commit -m "feat: port OtrsClient and Otrs6Client from rfi-plugin-example"
```

---

### Task 4: Port `Ticketer` and wire OSGi Blueprint

**Files:**
- Create: `/Users/antonio/Rcs/rssntn67/opennms-otrs6-plugin/plugin/src/main/java/it/arsinfo/opennms/plugins/otrs6/ticketing/Ticketer.java`
- Create: `/Users/antonio/Rcs/rssntn67/opennms-otrs6-plugin/plugin/src/main/resources/OSGI-INF/blueprint/blueprint.xml`

**Interfaces:**
- Consumes: `OtrsClient` from Task 3 (constructor argument), `org.opennms.integration.api.v1.dao.AlarmDao` / `org.opennms.integration.api.v1.ticketing.Ticket` / `TicketingPlugin` (from `common` dependency), `org.apache.karaf.shell.api.action.lifecycle.Reference` (from `org.apache.karaf.shell.core` dependency, already in `plugin/pom.xml`).
- Produces: `Ticketer` OSGi service registered as `org.opennms.integration.api.v1.ticketing.TicketingPlugin`, driven entirely by `blueprint.xml` — nothing downstream in Java code depends on `Ticketer`, only the packaged bundle's blueprint wiring.

- [ ] **Step 1: Create `Ticketer.java`**

```java
package it.arsinfo.opennms.plugins.otrs6.ticketing;

import it.arsinfo.opennms.plugins.otrs6.clients.OtrsClient;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.opennms.integration.api.v1.dao.AlarmDao;
import org.opennms.integration.api.v1.ticketing.Ticket;
import org.opennms.integration.api.v1.ticketing.TicketingPlugin;

import java.util.concurrent.ScheduledExecutorService;

public class Ticketer implements TicketingPlugin {
    @Reference
    AlarmDao alarmDao;

    ScheduledExecutorService scheduler;
    private final OtrsClient client;

    public Ticketer(OtrsClient client) {
        this.client = client;
    }

    @Override
    public Ticket get(String ticketId) {
        return client.get(ticketId);
    }

    @Override
    public String saveOrUpdate(Ticket ticket) {
        return client.savaORUpdate(ticket);
    }


    // add scheduler to get all ticket state on OTRS and then update
    // for each alarm with a ticket check opennms ticket status
    //
}
```

- [ ] **Step 2: Create `blueprint.xml`**

```xml
<blueprint xmlns="http://www.osgi.org/xmlns/blueprint/v1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xmlns:cm="http://aries.apache.org/blueprint/xmlns/blueprint-cm/v1.1.0"
           xsi:schemaLocation="http://www.osgi.org/xmlns/blueprint/v1.0.0
                http://www.osgi.org/xmlns/blueprint/v1.0.0/blueprint.xsd
                http://aries.apache.org/blueprint/xmlns/blueprint-cm/v1.1.0
                http://aries.apache.org/schemas/blueprint-cm/blueprint-cm-1.1.0.xsd">

    <!-- Configuration stored in $OPENNMS_HOME/etc/it.arsinfo.opennms.plugins.otrs6.cfg file -->
    <cm:property-placeholder id="otrs6PluginProperties" persistent-id="it.arsinfo.opennms.plugins.otrs6" update-strategy="reload">
        <cm:default-properties>
            <cm:property name="otrsUrl" value="http://127.0.0.1/otrs/nph-genericinterface.pl/Webservice/GenericTicketConnector" />
            <cm:property name="otrsUser" value="root@localhost" />
            <cm:property name="otrsPassword" value="root" />
        </cm:default-properties>
    </cm:property-placeholder>

    <bean id="otrs6Client" class="it.arsinfo.opennms.plugins.otrs6.clients.Otrs6Client">
        <argument value="${otrsUrl}"/>
        <argument value="${otrsUser}"/>
        <argument value="${otrsPassword}"/>
    </bean>

    <bean id="ticketer" class="it.arsinfo.opennms.plugins.otrs6.ticketing.Ticketer">
        <argument ref="otrs6Client"/>
    </bean>
    <service ref="ticketer" interface="org.opennms.integration.api.v1.ticketing.TicketingPlugin"/>

</blueprint>
```

- [ ] **Step 3: Verify the bundle packages**

Run: `mvn -f /Users/antonio/Rcs/rssntn67/opennms-otrs6-plugin/pom.xml -pl plugin -am package -DskipTests`
Expected: `BUILD SUCCESS`; `plugin/target/otrs6-plugin-0.1.0-SNAPSHOT.jar` is produced and contains `OSGI-INF/blueprint/blueprint.xml`.

- [ ] **Step 4: Commit**

```bash
git -C /Users/antonio/Rcs/rssntn67/opennms-otrs6-plugin add plugin/src/main/java/it/arsinfo/opennms/plugins/otrs6/ticketing plugin/src/main/resources/OSGI-INF
git -C /Users/antonio/Rcs/rssntn67/opennms-otrs6-plugin commit -m "feat: port Ticketer and wire OSGi Blueprint"
```

---

### Task 5: Port `Otrs6ClientTest` and run the unit tests

**Files:**
- Create: `/Users/antonio/Rcs/rssntn67/opennms-otrs6-plugin/plugin/src/test/java/it/arsinfo/opennms/plugins/otrs6/clients/Otrs6ClientTest.java`

**Interfaces:**
- Consumes: `Otrs6Client` from Task 3, generated SOAP stubs from Task 2 (`it.arsinfo.opennms.plugins.otrs6.clients.otrs.*`).

- [ ] **Step 1: Create `Otrs6ClientTest.java`**

```java
package it.arsinfo.opennms.plugins.otrs6.clients;

import it.arsinfo.opennms.plugins.otrs6.clients.otrs.*;
import org.junit.Before;
import org.junit.Test;
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
        OTRSTicketSearchResponse searchResp = new OTRSTicketSearchResponse();
        searchResp.getTicketID().add(BigInteger.valueOf(1));
        searchResp.getTicketID().add(BigInteger.valueOf(2));
        when(port.ticketSearch(any())).thenReturn(searchResp);

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

- [ ] **Step 2: Run the tests**

Run: `mvn -f /Users/antonio/Rcs/rssntn67/opennms-otrs6-plugin/pom.xml -pl plugin -am test`
Expected: `BUILD SUCCESS`, `Tests run: 6, Failures: 0, Errors: 0` for `Otrs6ClientTest`.

- [ ] **Step 3: Commit**

```bash
git -C /Users/antonio/Rcs/rssntn67/opennms-otrs6-plugin add plugin/src/test
git -C /Users/antonio/Rcs/rssntn67/opennms-otrs6-plugin commit -m "test: port Otrs6ClientTest from rfi-plugin-example"
```

---

### Task 6: Write README.md and CLAUDE.md

**Files:**
- Create: `/Users/antonio/Rcs/rssntn67/opennms-otrs6-plugin/README.md`
- Create: `/Users/antonio/Rcs/rssntn67/opennms-otrs6-plugin/CLAUDE.md`

**Interfaces:**
- Consumes: nothing (documentation only, describes the finished project from Tasks 1-5).

- [ ] **Step 1: Create `README.md`**

```markdown
# OpenNMS OTRS6 Ticketing Plugin

An OpenNMS integration plugin, packaged as an OSGi bundle for deployment in
Apache Karaf, that implements OpenNMS's `TicketingPlugin` API backed by an
OTRS6 instance via the `GenericTicketConnector` SOAP interface.

## Build

```
mvn clean install
```

## Deploy to OpenNMS/Karaf

Option 1: Karaf feature install
```
feature:repo-add mvn:it.arsinfo.opennms.plugins/karaf-features/0.1.0-SNAPSHOT/xml
feature:install opennms-plugins-otrs6
```

Option 2: Copy KAR bundle directly
```
cp assembly/kar/target/opennms-otrs6-plugin.kar /opt/opennms/deploy/
```

## Configuration

Runtime configuration is read from `$OPENNMS_HOME/etc/it.arsinfo.opennms.plugins.otrs6.cfg`
with hot-reload support:
- `otrsUrl` — OTRS `GenericTicketConnector` SOAP endpoint (default: `http://127.0.0.1/otrs/nph-genericinterface.pl/Webservice/GenericTicketConnector`)
- `otrsUser` — OTRS user login (default: `root@localhost`)
- `otrsPassword` — OTRS user password (default: `root`)
```

- [ ] **Step 2: Create `CLAUDE.md`**

```markdown
# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Full build
mvn clean install

# Build without installing
mvn clean package

# Run unit tests only
mvn test

# Run a single unit test class
mvn test -Dtest=Otrs6ClientTest

# Run a single test method
mvn test -Dtest=Otrs6ClientTest#testMethodName
```

## Deploy to OpenNMS/Karaf

```bash
# Option 1: Karaf feature install
feature:repo-add mvn:it.arsinfo.opennms.plugins/karaf-features/0.1.0-SNAPSHOT/xml
feature:install opennms-plugins-otrs6

# Option 2: Copy KAR bundle directly
cp assembly/kar/target/opennms-otrs6-plugin.kar /opt/opennms/deploy/
```

## Architecture Overview

This is an **OpenNMS integration plugin** packaged as an OSGi bundle for
deployment in Apache Karaf (the runtime embedded in OpenNMS). The plugin
implements OpenNMS's `TicketingPlugin` API, backed by an OTRS6 instance's
`GenericTicketConnector` SOAP web service.

### Module Structure

- **`plugin/`** — Main OSGi bundle (`packaging: bundle`). All business logic lives here.
- **`karaf-features/`** — Defines the Karaf feature descriptor (`features.xml`) that declares bundle dependencies.
- **`assembly/kar/`** — Assembles the deployable KAR (Karaf Archive) artifact.

### Ticketing Pipeline

`Ticketer` (`it.arsinfo.opennms.plugins.otrs6.ticketing`) implements
OpenNMS's `TicketingPlugin` interface and delegates to an `OtrsClient`
implementation. `Otrs6Client` (`it.arsinfo.opennms.plugins.otrs6.clients`)
implements `OtrsClient` against OTRS6's `GenericTicketConnector` SOAP
endpoint, using JAX-WS stub classes generated at build time by the
`cxf-codegen-plugin` from `plugin/src/main/wsdl/GenericTicketConnector.wsdl`
into package `it.arsinfo.opennms.plugins.otrs6.clients.otrs`.

### Dependency Injection & Configuration

Components are wired via OSGi Blueprint XML at
`plugin/src/main/resources/OSGI-INF/blueprint/blueprint.xml`.

Runtime configuration is read from
`$OPENNMS_HOME/etc/it.arsinfo.opennms.plugins.otrs6.cfg` with hot-reload support:
- `otrsUrl` — OTRS `GenericTicketConnector` SOAP endpoint (default: `http://127.0.0.1/otrs/nph-genericinterface.pl/Webservice/GenericTicketConnector`)
- `otrsUser` — OTRS user login (default: `root@localhost`)
- `otrsPassword` — OTRS user password (default: `root`)

### Key Technologies

- **OpenNMS Integration API 1.6.1** — `TicketingPlugin`, `AlarmDao`
- **OSGi / Apache Karaf 4.3.10** — Bundle lifecycle, service registry
- **OSGi Blueprint (Aries)** — Declarative dependency injection via XML
- **Apache CXF `cxf-codegen-plugin`** — Generates JAX-WS client stubs from `GenericTicketConnector.wsdl`
- **JUnit 4 + Mockito 2** — Unit tests

### Test Conventions

- `*Test.java` = unit tests (Surefire, runs on `mvn test`)
```

- [ ] **Step 3: Commit**

```bash
git -C /Users/antonio/Rcs/rssntn67/opennms-otrs6-plugin add README.md CLAUDE.md
git -C /Users/antonio/Rcs/rssntn67/opennms-otrs6-plugin commit -m "docs: add README and CLAUDE.md"
```

---

### Task 7: Full reactor build verification

**Files:** none (verification only)

**Interfaces:**
- Consumes: the complete project from Tasks 1-6.

- [ ] **Step 1: Run the full build from a clean state**

Run: `mvn -f /Users/antonio/Rcs/rssntn67/opennms-otrs6-plugin/pom.xml clean install`
Expected: `BUILD SUCCESS` for all 5 modules (`opennms-otrs6-plugin`, `assembly`, `kar`, `karaf-features`, `otrs6-plugin`), including `Tests run: 6, Failures: 0, Errors: 0`.

- [ ] **Step 2: Confirm the KAR artifact was produced**

Run: `ls -la /Users/antonio/Rcs/rssntn67/opennms-otrs6-plugin/assembly/kar/target/opennms-otrs6-plugin.kar`
Expected: file exists.

- [ ] **Step 3: Verify git status is clean**

Run: `git -C /Users/antonio/Rcs/rssntn67/opennms-otrs6-plugin status`
Expected: `nothing to commit, working tree clean` (Maven's `target/` directories are excluded by `.gitignore` from Task 1).

If anything is uncommitted at this point, commit it (no `Co-Authored-By` trailer, per repo convention):

```bash
git -C /Users/antonio/Rcs/rssntn67/opennms-otrs6-plugin add -A
git -C /Users/antonio/Rcs/rssntn67/opennms-otrs6-plugin commit -m "chore: finish opennms-otrs6-plugin initial scaffold"
```
